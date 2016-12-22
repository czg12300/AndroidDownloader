
package com.jake.library;

import android.content.Context;
import android.text.TextUtils;

import com.jake.library.datafetch.HttpURLConnectionDataFetch;
import com.jake.library.datafetch.HttpURLConnectionDataFetchLoader;
import com.jake.library.db.DownloadDbHelper;
import com.jake.library.db.DownloadFile;
import com.jake.library.db.DownloadFileOperator;
import com.jake.library.db.DownloadPartOperator;
import com.jake.library.filenamegenerator.UrlFileNameGenerator;
import com.jake.library.filenamegenerator.UrlFileNameGeneratorLoader;
import com.jake.library.job.DownloadJob;
import com.jake.library.utils.DLog;
import com.jake.library.utils.DownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:下载器,支持多线程，断点续传
 *
 * @author jakechen
 * @since 2016/7/21
 */
public class Downloader {
    private Downloader() {
    }

    private static Downloader mInstance;
    private DownloadConfiguration mConfiguration;

    public static Downloader getInstance() {
        if (mInstance == null) {
            throw new NullPointerException("Downloader instance is null,please call install first .Suggest call install in your application");
        }
        return mInstance;
    }

    public static void install(Context context) {
        install(context, null);
    }

    public static void install(Context context, DownloadConfiguration configuration) {
        if (context == null) {
            throw new NullPointerException("Downloader call install context is null");
        }
        DLog.setIsOpenLog(false);
        DownloadDbHelper.install(context);
        mInstance = new Downloader();
        if (configuration == null) {
            configuration = createDownloadConfiguration(context);
        }
        mInstance.mConfiguration = configuration;
    }

    private static DownloadConfiguration createDownloadConfiguration(Context context) {
        DownloadConfiguration.Builder builder = new DownloadConfiguration.Builder();
        builder.setThreadExecutor(DownloadUtils.getDownloadDefaultThreadExecutor());
        builder.setDownloadDir(context.getFilesDir().getAbsolutePath());
        builder.setFileNameGeneratorLoader(new UrlFileNameGeneratorLoader());
        builder.setDownloadDataFetchLoader(new HttpURLConnectionDataFetchLoader());
        return builder.build();
    }

    /**
     * 判断是否是http的url
     *
     * @param url
     * @return
     */
    private boolean isHttpUrl(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public void download(String url) {
        reDownload(url, null);
    }

    public void download(String url, String fileDir) {
        download(url, null, fileDir);
    }

    public void download(String url, String fileName, String fileDir) {
        downloadLogic(url, fileName, fileDir, false);
    }

    public void reDownload(String url) {
        reDownload(url, null);
    }

    public void reDownload(String url, String fileDir) {
        reDownload(url, null, fileDir);
    }

    public void reDownload(final String url, final String fileName, final String fileDir) {
        mConfiguration.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                downloadLogic(url, fileName, fileDir, true);
            }
        });
    }

    private void downloadLogic(String url, String fileName, String fileDir, boolean isReDownload) {
        if (!isHttpUrl(url)) {
            return;
        }
        String filePath = createFilePath(url, fileName, fileDir);
        if (isReDownload) {
            deleteDownloadRecordAndFile(url, filePath);
        }
        final DownloadKey key = DownloadKey.create(url, filePath);
        if (isExistInCache(key)) {
            DownloadJob job = getDownloadJobFromCache(key);
            job.start();
            mConfiguration.getExecutorService().execute(job);
            return;
        }
        DownloadJob job = new DownloadJob(key);
        job.start();
        mConfiguration.getExecutorService().execute(job);
        addDownloadJobToCache(key, job);
    }

    private void deleteDownloadRecordAndFile(String url, String filePath) {
        final DownloadKey key = DownloadKey.create(url, filePath);
        DownloadFile downloadFile = DownloadFileOperator.getInstance().query(key.getKey());
        if (downloadFile != null) {
            DownloadUtils.deleteFile(downloadFile.path);
            DownloadFileOperator.getInstance().delete(key.getKey());
            DownloadPartOperator.getInstance().deleteAllPart(key.getKey());
        }
    }

    public void stop(String url) {
        stop(url, null);
    }

    public void stop(String url, String fileDir) {
        stop(url, null, fileDir);
    }

    public void stop(String url, String fileName, String fileDir) {
        String filePath = createFilePath(url, fileName, fileDir);
        stopLogic(url, filePath);
    }


    /**
     * 创建默认的文件下载地址
     *
     * @param url
     * @return
     */
    private String createFilePath(String url, String fileName, String fileDir) {
        if (TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(url)) {
            fileName = mConfiguration.getFileNameGenerator().name(url);
        }
        if (TextUtils.isEmpty(fileDir)) {
            fileDir = mConfiguration.getDownloadDir();
        }
        return fileDir + File.separator + fileName;
    }

    private void stopLogic(String url, String filePath) {
        if (!isHttpUrl(url)) {
            return;
        }
        final DownloadKey key = DownloadKey.create(url, filePath);
        if (isExistInCache(key)) {
            DownloadJob job = getDownloadJobFromCache(key);
            job.stop();
        }
    }


    public DownloadConfiguration getConfiguration() {
        return mConfiguration;
    }

    private ArrayList<IDownloadListener> mDownloadListeners = new ArrayList<>();

    public synchronized void addDownloadListener(IDownloadListener listener) {
        if (listener != null) {
            mDownloadListeners.add(listener);
        }
    }

    public synchronized void removeDownloadListener(IDownloadListener listener) {
        if (listener != null) {
            mDownloadListeners.remove(listener);
        }
    }

    public synchronized ArrayList<IDownloadListener> getAllDownloadListener() {
        return mDownloadListeners;
    }

    // 下载文件缓存
    private ConcurrentHashMap<String, DownloadJob> mCacheMap = new ConcurrentHashMap<>();

    public synchronized void addDownloadJobToCache(DownloadKey key, DownloadJob job) {
        if (key != null) {
            mCacheMap.put(key.getKey(), job);
        }
    }

    public synchronized void removeDownloadJobFromCache(DownloadKey key) {
        if (key != null) {
            mCacheMap.remove(key.getKey());
        }
    }

    public synchronized DownloadJob getDownloadJobFromCache(DownloadKey key) {
        return key != null ? mCacheMap.get(key.getKey()) : null;
    }

    public synchronized boolean isExistInCache(DownloadKey key) {
        return getDownloadJobFromCache(key) != null;
    }
}
