package com.jake.library;

import android.text.TextUtils;

import com.jake.library.utils.DownloadUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jakechen on 2016/10/26.
 */

public class DownloadKey {
    private URL mURL;
    private String mKey;
    private String mUrl;
    private String mFilePath;

    /**
     * 用于创建对象
     *
     * @param url
     * @return
     */

    public static DownloadKey create(String url, String filePath) {
        return new DownloadKey(url, filePath);
    }

    private DownloadKey(String url, String filePath) {
        if (!TextUtils.isEmpty(url)) {
            mUrl = url;
            mFilePath = filePath;
            try {
                mURL = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(filePath)) {
                mKey = DownloadUtils.getKey(url);
            } else {
                mKey = DownloadUtils.getKey(url + filePath);
            }
        } else {
            throw new NullPointerException(" url is null");
        }
    }

    public URL getURL() {
        return mURL;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getKey() {
        return mKey;
    }

    public String getFilePath() {
        return mFilePath;
    }
}
