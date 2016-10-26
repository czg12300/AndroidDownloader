package com.jake.library;

import android.text.TextUtils;

import com.jake.library.datafetcher.HttpURLConnectionDataFetcher;
import com.jake.library.datafetcher.IDownloadDataFetcher;
import com.jake.library.filenamegenerator.IFileNameGenerator;
import com.jake.library.filenamegenerator.UrlFileNameGenerator;
import com.jake.library.utils.DownloadUtils;

import java.util.concurrent.ExecutorService;

/**
 * Created by jakechen on 2016/10/26.
 */

public class DownloadConfiguration {
    private ExecutorService mExecutor;
    private String mDownloadDir;
    private Class<? extends IFileNameGenerator> mFileNameGenerator;
    private Class<? extends IDownloadDataFetcher> mDataFetcher;

    public ExecutorService getExecutorService() {
        return mExecutor;
    }

    public String getDownloadDir() {
        return mDownloadDir;
    }

    public IFileNameGenerator getFileNameGenerator() {
        IFileNameGenerator fileNameGenerator = null;
        try {
            fileNameGenerator = mFileNameGenerator.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fileNameGenerator;
    }

    public IDownloadDataFetcher getDataFetcher() {
        IDownloadDataFetcher dataFetcher = null;
        try {
            dataFetcher = mDataFetcher.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dataFetcher;
    }

    public static class Builder {
        DownloadConfiguration mDownloadConfiguration;

        public Builder() {
            mDownloadConfiguration = new DownloadConfiguration();
        }

        public Builder setThreadExecutor(ExecutorService executor) {
            mDownloadConfiguration.mExecutor = executor;
            return this;
        }

        public Builder setDownloadDir(String dir) {
            mDownloadConfiguration.mDownloadDir = dir;
            return this;
        }

        public Builder setFileNameGenerator(Class<? extends IFileNameGenerator> fileNameGenerator) {
            mDownloadConfiguration.mFileNameGenerator = fileNameGenerator;
            return this;
        }

        public Builder setDownloadDataFetcher(Class<? extends IDownloadDataFetcher> dataFetcher) {
            mDownloadConfiguration.mDataFetcher = dataFetcher;
            return this;
        }

        public DownloadConfiguration build() {
            if (mDownloadConfiguration.mExecutor == null) {
                mDownloadConfiguration.mExecutor = DownloadUtils.getDownloadDefaultThreadExecutor();
            }
            if (mDownloadConfiguration.mFileNameGenerator == null) {
                mDownloadConfiguration.mFileNameGenerator = UrlFileNameGenerator.class;
            }
            if (mDownloadConfiguration.mDataFetcher == null) {
                mDownloadConfiguration.mDataFetcher = HttpURLConnectionDataFetcher.class;
            }
            if (TextUtils.isEmpty(mDownloadConfiguration.mDownloadDir)) {
                throw new IllegalArgumentException("please setup a legal download directory");
            }
            return mDownloadConfiguration;
        }
    }
}
