package com.jake.library;

import android.text.TextUtils;

import com.jake.library.datafetch.DownloadDataFetch;
import com.jake.library.datafetch.DownloadDataFetchLoader;
import com.jake.library.datafetch.HttpURLConnectionDataFetchLoader;
import com.jake.library.filenamegenerator.FileNameGenerator;
import com.jake.library.filenamegenerator.FileNameGeneratorLoader;
import com.jake.library.filenamegenerator.UrlFileNameGenerator;
import com.jake.library.filenamegenerator.UrlFileNameGeneratorLoader;
import com.jake.library.utils.DownloadUtils;

import java.util.concurrent.ExecutorService;

/**
 * Created by jakechen on 2016/10/26.
 */

public class DownloadConfiguration {
    private ExecutorService mExecutor;
    private String mDownloadDir;
    private FileNameGeneratorLoader mFileNameGeneratorLoader;
    private DownloadDataFetchLoader mDataFetchLoader;

    public ExecutorService getExecutorService() {
        return mExecutor;
    }

    public String getDownloadDir() {
        return mDownloadDir;
    }

    public FileNameGenerator getFileNameGenerator() {
        return mFileNameGeneratorLoader.getFileNameGenerator();
    }

    public DownloadDataFetch getDataFetch() {
        return mDataFetchLoader.getDataFetch();
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

        public Builder setFileNameGeneratorLoader(FileNameGeneratorLoader loader) {
            mDownloadConfiguration.mFileNameGeneratorLoader = loader;
            return this;
        }

        public Builder setDownloadDataFetchLoader(DownloadDataFetchLoader dataFetchLoader) {
            mDownloadConfiguration.mDataFetchLoader = dataFetchLoader;
            return this;
        }

        public DownloadConfiguration build() {
            if (mDownloadConfiguration.mExecutor == null) {
                mDownloadConfiguration.mExecutor = DownloadUtils.getDownloadDefaultThreadExecutor();
            }
            if (mDownloadConfiguration.mFileNameGeneratorLoader == null) {
                mDownloadConfiguration.mFileNameGeneratorLoader = new UrlFileNameGeneratorLoader();
            }
            if (mDownloadConfiguration.mDataFetchLoader == null) {
                mDownloadConfiguration.mDataFetchLoader = new HttpURLConnectionDataFetchLoader();
            }
            if (TextUtils.isEmpty(mDownloadConfiguration.mDownloadDir)) {
                throw new IllegalArgumentException("please setup a legal download directory");
            }
            return mDownloadConfiguration;
        }
    }
}
