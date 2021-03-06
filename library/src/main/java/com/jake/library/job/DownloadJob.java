
package com.jake.library.job;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.jake.library.DownloadConfiguration;
import com.jake.library.DownloadKey;
import com.jake.library.DownloadState;
import com.jake.library.utils.DLog;
import com.jake.library.utils.DownloadUtils;
import com.jake.library.Downloader;
import com.jake.library.IDownloadListener;
import com.jake.library.db.DownloadFile;
import com.jake.library.db.DownloadFileOperator;
import com.jake.library.db.DownloadPart;
import com.jake.library.db.DownloadPartOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 描述:下载任务
 *
 * @author jakechen
 * @since 2016/7/25
 */
public class DownloadJob extends BaseJob {

    private Handler mHandler = new Handler(Looper.getMainLooper());
    //线程安全的list
    private CopyOnWriteArrayList<DownloadPartJob> mDownloadPartJobs;
    private DownloadKey mKey;

    public DownloadJob(DownloadKey key) {
        this.mKey = key;
    }

    @Override
    protected void runInThread() {
        DownloadFile downloadFile = DownloadFileOperator.getInstance().query(mKey.getKey());
        if (downloadFile != null) {
            if (downloadFile.isFinish()) {
                if (TextUtils.isEmpty(downloadFile.path)) {
                    DownloadFileOperator.getInstance().delete(mKey.getKey());
                    downloadNew();
                } else {
                    File file = new File(downloadFile.path);
                    if (file.exists() && file.isFile() && file.length() > 0) {
                        onSuccess(mKey.getUrl(), downloadFile.path);
                    } else {
                        file.delete();
                        DownloadFileOperator.getInstance().delete(mKey.getKey());
                        downloadNew();
                    }
                }

            } else {
                if (downloadFile.partIds != null && downloadFile.partIds.length > 0) {
                    ArrayList<DownloadPart> downloadParts = queryDownloadByPartIds(downloadFile.id);
                    downloadParts(downloadParts);
                } else {
                    DownloadFileOperator.getInstance().delete(mKey.getKey());
                    downloadNew();
                }
            }
        } else {
            downloadNew();
        }

    }

    /**
     * 通过partIds下载
     *
     * @param fileId
     */
    private ArrayList<DownloadPart> queryDownloadByPartIds(String fileId) {
        if (TextUtils.isEmpty(fileId)) {
            return null;
        }
        return DownloadPartOperator.getInstance().queryList(fileId);
    }

    /**
     * 下载片段
     *
     * @param downloadParts
     */
    private void downloadParts(ArrayList<DownloadPart> downloadParts) {
        if (downloadParts != null && downloadParts.size() > 0) {
            if (mDownloadPartJobs == null) {
                mDownloadPartJobs = new CopyOnWriteArrayList<>();
            }
            for (DownloadPart part : downloadParts) {
                if (part != null && !part.isFinish()) {
                    DownloadPartJob job = new DownloadPartJob(part);
                    job.start();
                    mDownloadPartJobs.add(job);
                    getDownloadConfiguration().getExecutorService().execute(job);
                }
            }
        }
        //开始监视下载进度
        watchDownloadProgress();
    }

    /**
     * 监听下载进度
     */
    private void watchDownloadProgress() {
        do {
            DownloadFile downloadFile = DownloadFileOperator.getInstance().query(mKey.getKey());
            if (downloadFile == null) {
                onFail(mKey.getUrl());
                return;
            }
            ArrayList<DownloadPart> downloadParts = queryDownloadByPartIds(downloadFile.id);
            if (downloadParts != null) {
                boolean isFinish = true;
                long temp = 0;
                for (DownloadPart part : downloadParts) {
                    if (part != null) {
                        temp += part.positionSize;
                        if (!part.isFinish()) {
                            isFinish = false;
                        }
                    }
                }
                if (temp > 0) {
                    downloadFile.positionSize = temp;
                }
                if (isFinish) {
                    downloadFile.state = DownloadState.FINISH;
                }
            }
            //更新数据库
            DownloadFileOperator.getInstance().update(mKey.getKey(), downloadFile);
            onProgress(mKey.getUrl(), downloadFile.positionSize, downloadFile.totalSize);
            if (mDownloadPartJobs != null && mDownloadPartJobs.size() > 0) {
                for (DownloadPartJob job : mDownloadPartJobs) {
                    if (job != null) {
                        if (job.getDownloadPart().isFinish() || job.isStop()) {
                            mDownloadPartJobs.remove(job);
                        }
                    }
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (mDownloadPartJobs != null && mDownloadPartJobs.size() > 0 && !isStop());
        DownloadFile downloadFile = DownloadFileOperator.getInstance().query(mKey.getKey());
        if (isStop()) {
            onStop(mKey.getUrl());
            if (downloadFile != null) {
                downloadFile.state = DownloadState.STOP;
                DownloadFileOperator.getInstance().update(mKey.getKey(), downloadFile);
            }
        } else {
            if (downloadFile != null && downloadFile.isFinish()) {
                onSuccess(mKey.getUrl(), downloadFile.path);
            } else {
                onFail(mKey.getUrl());
            }
        }
    }


    /**
     * 下载一个新的文件
     */
    private void downloadNew() {
        long totalSize = DownloadUtils.getContentLengthByURL(mKey.getURL());
        if (totalSize > 0) {
            final String fileId = mKey.getKey();
            final String downloadPath = mKey.getFilePath();
            int partCount = getPartCount(totalSize);
            int temp = (int) (totalSize / partCount);
            String[] partIds = new String[partCount];
            ArrayList<DownloadPart> downloadParts = new ArrayList<>();
            for (int i = 0; i < partCount; i++) {
                long rangeStart = i * temp;
                long rangeEnd = 0;
                if (i < partCount - 1) {
                    rangeEnd = rangeStart + temp;
                } else {
                    rangeEnd = totalSize;
                }
                final String partId = mKey.getKey() + i;
                partIds[i] = partId;
                DownloadPart part = createDownloadPart(fileId, downloadPath, rangeStart, rangeEnd, partId);
                downloadParts.add(part);
            }
            DownloadFile downloadFile = createDownloadFile(fileId, downloadPath, totalSize, partIds);
            //片段插入数据库
            DownloadPartOperator.getInstance().insert(downloadParts);
            //文件插入数据库
            DownloadFileOperator.getInstance().insert(downloadFile);
            // 统一创建下载文件
            DownloadUtils.createFile(downloadPath, true);
            downloadParts(downloadParts);
        } else {
            onFail(mKey.getUrl());
        }
    }

    /***
     * 创建下载文件
     *
     * @param fileId
     * @param downloadPath
     * @param fileSize
     * @param partIds
     * @return
     */
    private DownloadFile createDownloadFile(String fileId, String downloadPath, long fileSize, String[] partIds) {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.id = fileId;
        downloadFile.path = downloadPath;
        downloadFile.url = mKey.getUrl();
        downloadFile.positionSize = 0;
        downloadFile.state = DownloadState.START;
        downloadFile.totalSize = fileSize;
        downloadFile.partIds = partIds;
        downloadFile.partCount = partIds.length;
        return downloadFile;
    }

    /**
     * 创建下载片段
     *
     * @param fileId
     * @param path
     * @param rangeStart
     * @param rangeEnd
     * @param partId
     * @return
     */
    private DownloadPart createDownloadPart(String fileId, String path, long rangeStart, long rangeEnd, String partId) {
        DownloadPart part = new DownloadPart();
        part.id = partId;
        part.rangeStart = rangeStart;
        part.rangeEnd = rangeEnd;
        part.fileId = fileId;
        part.path = path;
        part.positionSize = 0;
        part.totalSize = rangeEnd - rangeStart;
        part.state = DownloadState.START;
        part.url = mKey.getUrl();
        return part;
    }

    private DownloadConfiguration getDownloadConfiguration() {
        return Downloader.getInstance().getConfiguration();
    }

    @Override
    public void stop() {
        super.stop();
        if (mDownloadPartJobs != null && mDownloadPartJobs.size() > 0) {
            for (DownloadPartJob job : mDownloadPartJobs) {
                if (job != null) {
                    job.stop();
                }
            }
            mDownloadPartJobs.clear();
            mDownloadPartJobs = null;
        }
    }

    /**
     * 根据文件大小分配下载片
     *
     * @param totalSize
     * @return
     */
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


    private void onFail(final String url) {
        DLog.d("onFail url=" + url);
        final ArrayList<IDownloadListener> list = Downloader.getInstance().getAllDownloadListener();
        if (list != null && list.size() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IDownloadListener listener : list) {
                        if (listener != null) {
                            listener.onFail(url);
                        }
                    }
                }
            });
        }
    }

    private void onProgress(final String url, final long positionSize, final long totalSize) {
        DLog.d("onProgress url=" + url + " positionSize=" + positionSize + " totalSize=" + totalSize);
        final ArrayList<IDownloadListener> list = Downloader.getInstance().getAllDownloadListener();
        if (list != null && list.size() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IDownloadListener listener : list) {
                        if (listener != null) {
                            listener.onProgress(url, positionSize, totalSize);
                        }
                    }
                }
            });
        }
    }

    private void onSuccess(final String url, final String path) {
        DLog.d("onSuccess url=" + url + " path=" + path);
        Downloader.getInstance().removeDownloadJobFromCache(mKey);
        final ArrayList<IDownloadListener> list = Downloader.getInstance().getAllDownloadListener();
        if (list != null && list.size() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IDownloadListener listener : list) {
                        if (listener != null) {
                            listener.onSuccess(url, path);
                        }
                    }
                }
            });

        }
    }

    private void onStop(final String url) {
        DLog.d("onStop url=" + url);
        Downloader.getInstance().removeDownloadJobFromCache(mKey);
        final ArrayList<IDownloadListener> list = Downloader.getInstance().getAllDownloadListener();
        if (list != null && list.size() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IDownloadListener listener : list) {
                        if (listener != null) {
                            listener.onStop(url);
                        }
                    }
                }
            });

        }
    }


}
