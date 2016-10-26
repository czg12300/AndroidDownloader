
package com.jake.library;

import android.content.Context;
import android.text.TextUtils;

import com.jake.library.datafetcher.HttpURLConnectionDataFetcher;
import com.jake.library.db.DownloadDbHelper;
import com.jake.library.filenamegenerator.UrlFileNameGenerator;
import com.jake.library.job.DownloadJob;
import com.jake.library.utils.DownloadUtils;

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
        builder.setFileNameGenerator(UrlFileNameGenerator.class);
        builder.setDownloadDataFetcher(HttpURLConnectionDataFetcher.class);
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
        if (!isHttpUrl(url)) {
            return;
        }
        final DownloadKey key = DownloadKey.create(url);
        if (isExistInCache(key)) {
            DownloadJob job = mCacheMap.get(key);
            job.start();
            mConfiguration.getExecutorService().submit(job);
            return;
        }
        DownloadJob job = new DownloadJob(key);
        job.start();
        mConfiguration.getExecutorService().submit(job);
        addDownloadJobToCache(key, job);
    }

    public void stop(String url) {
        if (!isHttpUrl(url)) {
            return;
        }
        final DownloadKey key = DownloadKey.create(url);
        if (isExistInCache(key)) {
            DownloadJob job = mCacheMap.get(key);
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
    private ConcurrentHashMap<DownloadKey, DownloadJob> mCacheMap = new ConcurrentHashMap<>();

    public synchronized void addDownloadJobToCache(DownloadKey key, DownloadJob job) {
        if (key != null) {
            mCacheMap.put(key, job);
        }
    }

    public synchronized void removeDownloadJobFromCache(DownloadKey key) {
        if (key != null) {
            mCacheMap.remove(key);
        }
    }

    public synchronized DownloadJob getDownloadJobFromCache(DownloadKey key) {
        return key != null ? mCacheMap.get(key) : null;
    }

    public synchronized boolean isExistInCache(DownloadKey key) {
        return getDownloadJobFromCache(key) != null;
    }
}
