
package com.jake.library;

import android.os.Environment;
import android.text.TextUtils;

import com.jake.library.utils.MD5Util;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:下载器,支持多线程，断点续传
 *
 * @author jakechen
 * @since 2016/7/21
 */
public class DownloadManager {
    private static String mDownloadDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static void setDownloadDir(String absolutePath) {
        mDownloadDir = absolutePath;
    }

    public static String getDownloadDir() {
        return mDownloadDir;
    }

    public static void download(String url) {
        final String key = getKey(url);
        if (isExistInCache(key)) {
            DownloadJob job = mCacheMap.get(key);
            job.restart();
            return;
        }
        DownloadJob job = new DownloadJob(url);
        DownloadExecutor.execute(job);
        addDownloadJobToCache(key, job);
    }

    public static void reDownload(String url) {
        final String key = getKey(url);
        if (isExistInCache(key)) {
            DownloadJob job = mCacheMap.get(key);
            job.pause();
            removeDownloadJobFromCache(key);
        }
        DownloadJob job = new DownloadJob(url, true);
        DownloadExecutor.execute(job);
        addDownloadJobToCache(key, job);
    }

    // private static boolean isAlreadyDownloadedInSdcard(String url) {
    // String fileName = formatFileName(url);
    // File file = new File(mDownloadDir, fileName);
    // if (file.exists() && file.isFile()) {
    // success(url, file.getPath());
    // return true;
    // }
    // return false;
    // }

    public synchronized static String formatFileName(String url) {
        if (url != null && url.contains("/")) {
            return url.substring(url.lastIndexOf("/"));
        }
        return url;
    }

    /**
     * 作为数据库保存的唯一标识
     *
     * @param url
     * @return
     */
    public synchronized static String getKey(String url) {
        return MD5Util.getMd5(url);
    }

    public static void pause(String url) {
        final String key = getKey(url);
        if (isExistInCache(key)) {
            DownloadJob job = mCacheMap.get(key);
            job.pause();
        }
    }

    public static void restart(String url) {
        final String key = getKey(url);
        if (isExistInCache(key)) {
            getDownloadJobFromCache(key).restart();
        } else {
            download(url);
        }
    }

    private static ArrayList<IDownloadListener> mDownloadListeners = new ArrayList<>();

    public synchronized static void addDownloadListener(IDownloadListener listener) {
        if (listener != null) {
            mDownloadListeners.add(listener);
        }
    }

    public synchronized static void removeDownloadListener(IDownloadListener listener) {
        if (listener != null) {
            mDownloadListeners.remove(listener);
        }
    }

    public synchronized static ArrayList<IDownloadListener> getAllDownloadListener() {
        return mDownloadListeners;
    }

    // 下载文件缓存
    private static ConcurrentHashMap<String, DownloadJob> mCacheMap = new ConcurrentHashMap<>();

    public synchronized static void addDownloadJobToCache(String key, DownloadJob job) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mCacheMap.put(key, job);
    }

    public synchronized static void removeDownloadJobFromCache(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        mCacheMap.remove(key);
    }

    public synchronized static DownloadJob getDownloadJobFromCache(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        return mCacheMap.get(key);
    }

    public synchronized static boolean isExistInCache(String key) {
        return getDownloadJobFromCache(key) != null;
    }
}
