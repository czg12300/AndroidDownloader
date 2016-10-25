
package com.jake.library;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.jake.library.data.http.OkHttpClientManager;
import com.jake.library.db.DownloadFile;
import com.jake.library.db.DownloadFileOperator;
import com.jake.library.db.DownloadPartOperator;
import com.jake.library.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 描述:下载任务
 *
 * @author jakechen
 * @since 2016/7/25
 */
public class DownloadJob
        implements Runnable, DownloadPartTask.IDownloadPartListener, Handler.Callback {
    private static final int MSG_SUCCESS = 0x001;

    private static final int MSG_FAIL = 0x002;

    private static final int MSG_PROGRESS = 0x003;

    private static final int MSG_PAUSE = 0x004;

    private DownloadFile mDownloadFile;

    private Handler mHandler;

    private String mUrl;

    private String mKey;

    // 用于判断当前下载状态
    private boolean mIsFinish = false;

    // 用于判断是否是重新下载
    private boolean mIsReDownload = false;

    private ArrayList<DownloadPartTask> mTaskList;

    public DownloadJob(String url) {
        this(url, false);
    }

    public DownloadJob(String url, boolean isReDownload) {
        this.mUrl = url;
        this.mIsReDownload = isReDownload;
        mKey = DownloadManager.getKey(mUrl);
        mTaskList = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public void run() {
        mDownloadFile = DownloadFileOperator.getInstance().query(mKey);
        if (mDownloadFile != null) {
            if (mIsReDownload) {
                startReDownload();
            } else {
                if (mDownloadFile.isFinish() && !TextUtils.isEmpty(mDownloadFile.path)) {
                    File file = new File(mDownloadFile.path);
                    if (file.exists() && file.isFile()) {
                        finished();
                        sendFinishMsg();
                        return;
                    }
                } else {
                    if (mDownloadFile.partIds != null && mDownloadFile.partIds.length > 0) {
                        startOldPartTask();
                    } else {
                        startNewPartTask();
                    }
                }
            }

        } else {
            startNewPartTask();
        }
        mDownloadFile.state = DownloadState.START;
        DownloadFileOperator.getInstance().update(mKey, mDownloadFile);
        while (!mIsFinish) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDownloadFile.state = DownloadState.DOWNLOADING;
            DownloadFileOperator.getInstance().update(mKey, mDownloadFile);
            mHandler.sendEmptyMessage(MSG_PROGRESS);
        }

    }

    private void startReDownload() {
        DownloadFileOperator.getInstance().delete(mKey);
        DownloadPartOperator.getInstance().deleteAllPart(mKey);
        startNewPartTask();
    }

    // 设置完成状态
    private void finished() {
        mIsFinish = true;
    }

    private void unfinished() {
        mIsFinish = false;
    }

    private void startOldPartTask() {
        boolean isAllPartReady = true;
        ArrayList<DownloadPart> partList = new ArrayList<>();
        for (int i = 0; i < mDownloadFile.partIds.length; i++) {
            final String partId = mDownloadFile.partIds[i];
            if (!TextUtils.isEmpty(partId)) {
                DownloadPart part = DownloadPartOperator.getInstance().query(partId);
                if (part != null) {
                    partList.add(part);
                } else {
                    isAllPartReady = false;
                    break;
                }
            }
        }
        if (isAllPartReady) {
            for (DownloadPart part : partList) {
                if (!part.isFinish()) {
                    startPartTask(part);
                }
            }
        } else {
            mIsReDownload = true;
            startReDownload();
        }
    }

    private void startNewPartTask() {
        long totalSize = getFileSizeByUrl(true);
        String downloadPath = DownloadManager.getDownloadDir() + File.separator
                + DownloadManager.formatFileName(mUrl);
        if (totalSize > 0) {
            // 统一创建下载文件
            FileUtil.createFile(downloadPath, true);
        }
        int partCount = getPartCount(totalSize);
        int temp = (int) (totalSize / partCount);
        String[] partIds = new String[partCount];
        for (int i = 0; i < partCount; i++) {
            long rangeStart = i * temp;
            long rangeEnd = 0;
            if (i < partCount - 1) {
                rangeEnd = rangeStart + temp;
            } else {
                rangeEnd = totalSize;
            }
            final String partId = mKey + i;
            partIds[i] = partId;
            DownloadPart part = createNewDownloadPart(downloadPath, rangeStart, rangeEnd, partId);
            // 插入数据库
            DownloadPartOperator.getInstance().insert(part);
            startPartTask(part);
        }
        // 创建当前下载的文件
        createDownloadFile(downloadPath, totalSize, partCount, partIds);
    }

    private void createDownloadFile(String path, long totalSize, int partCount, String[] partIds) {
        mDownloadFile = new DownloadFile();
        mDownloadFile.id = mKey;
        mDownloadFile.path = path;
        mDownloadFile.url = mUrl;
        mDownloadFile.positionSize = 0;
        mDownloadFile.state = DownloadState.START;
        mDownloadFile.totalSize = totalSize;
        mDownloadFile.partIds = partIds;
        mDownloadFile.partCount = partCount;
        // 插入数据库
        if (mIsReDownload) {
            DownloadFileOperator.getInstance().delete(mKey);
        }
        DownloadFileOperator.getInstance().insert(mDownloadFile);
    }

    private DownloadPart createNewDownloadPart(String path, long rangeStart, long rangeEnd,
                                               String partId) {
        DownloadPart part = new DownloadPart();
        part.id = partId;
        part.rangeStart = rangeStart;
        part.rangeEnd = rangeEnd;
        part.fileId = mKey;
        part.path = path;
        part.positionSize = 0;
        part.totalSize = rangeEnd - rangeStart;
        part.state = DownloadState.START;
        part.url = mUrl;
        return part;
    }

    private int getPartCount(long totalSize) {
        // 按不同文件的大小使用不同的线程数
        final int little = 1 * 1024 * 1024;
        final int middle = 50 * 1024 * 1024;
        int partCount = 1;
        if (totalSize > little && totalSize <= middle) {
            partCount = 5;
        } else if (totalSize > middle) {
            partCount = 8;
        }
        return partCount;
    }

    private long getFileSizeByUrl(boolean tryAgain) {
        long totalSize = 0;
        try {
            Request request = new Request.Builder().url(mUrl).header("Accept-Encoding", "identity")
                    .build();
            Response response = OkHttpClientManager.getInstance().getOkHttpClient().newCall(request)
                    .execute();
            ResponseBody responseBody = response.body();
            totalSize = responseBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            if (tryAgain) {
                totalSize = getFileSizeByUrl(false);
            } else {
                mHandler.sendEmptyMessage(MSG_FAIL);
            }

        }
        return totalSize > 0 ? totalSize : 0;
    }

    private void startPartTask(DownloadPart part) {
        DownloadPartTask task = new DownloadPartTask(part, this);
        mTaskList.add(task);
        DownloadExecutor.submit(task);
    }

    public void pause() {
        finished();
        if (mTaskList != null && mTaskList.size() > 0) {
            for (DownloadPartTask task : mTaskList) {
                task.pause();
            }
            mDownloadFile.state = DownloadState.PAUSE;
            DownloadFileOperator.getInstance().update(mKey, mDownloadFile);
        }
        mHandler.sendEmptyMessage(MSG_PAUSE);
    }

    public void restart() {
        unfinished();
        if (mTaskList != null && mTaskList.size() > 0) {
            for (DownloadPartTask task : mTaskList) {
                task.pause();
                DownloadExecutor.submit(task);
            }
        }
    }

    @Override
    public synchronized void downloadChange(DownloadPart part) {
        mDownloadFile.positionSize += part.positionSize;
        if (mDownloadFile.positionSize >= mDownloadFile.totalSize) {
            finished();
            mDownloadFile.state = DownloadState.FINISH;
            DownloadFileOperator.getInstance().update(mKey, mDownloadFile);
            sendFinishMsg();
        }
    }

    private void sendFinishMsg() {
        DownloadManager.removeDownloadJobFromCache(mKey);
        mHandler.sendEmptyMessage(MSG_SUCCESS);
    }

    @Override
    public synchronized void fail(DownloadPart part) {
        finished();
        pause();
        DownloadManager.removeDownloadJobFromCache(mKey);
        mHandler.sendEmptyMessage(MSG_FAIL);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_FAIL:
                onFail(mUrl);
                break;
            case MSG_SUCCESS:
                onSuccess(mUrl, mDownloadFile.path);
                break;
            case MSG_PROGRESS:
                onProgress(mUrl, mDownloadFile.positionSize, mDownloadFile.totalSize);
                break;
            case MSG_PAUSE:
                onPause(mUrl);
                break;
        }
        return true;
    }

    public void onFail(String url) {
        ArrayList<IDownloadListener> list = DownloadManager.getAllDownloadListener();
        if (list != null && list.size() > 0) {
            for (IDownloadListener listener : list) {
                if (listener != null) {
                    listener.onFail(url);
                }
            }
        }
    }

    public void onProgress(String url, long positionSize, long totalSize) {
        ArrayList<IDownloadListener> list = DownloadManager.getAllDownloadListener();
        if (list != null && list.size() > 0) {
            for (IDownloadListener listener : list) {
                if (listener != null) {
                    listener.onProgress(url, positionSize, totalSize);
                }
            }
        }
    }

    public void onSuccess(String url, String path) {
        ArrayList<IDownloadListener> list = DownloadManager.getAllDownloadListener();
        if (list != null && list.size() > 0) {
            for (IDownloadListener listener : list) {
                if (listener != null) {
                    listener.onSuccess(url, path);
                }
            }
        }
    }

    public void onPause(String url) {
        ArrayList<IDownloadListener> list = DownloadManager.getAllDownloadListener();
        if (list != null && list.size() > 0) {
            for (IDownloadListener listener : list) {
                if (listener != null) {
                    listener.onPause(url);
                }
            }
        }
    }

}
