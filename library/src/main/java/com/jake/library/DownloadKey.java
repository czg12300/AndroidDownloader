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

    /**
     * 用于创建对象
     *
     * @param url
     * @return
     */
    public static DownloadKey create(String url) {
        return new DownloadKey(url);
    }

    private DownloadKey(String url) {
        if (!TextUtils.isEmpty(url)) {
            mUrl = url;
            try {
                mURL = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            mKey = DownloadUtils.getKey(url);
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
}
