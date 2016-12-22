package com.jake.library.datafetch;

import android.text.TextUtils;

import com.jake.library.utils.DownloadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jakechen on 2016/10/26.
 */

public class HttpURLConnectionDataFetch implements DownloadDataFetch {

    private HttpURLConnection urlConnection;
    private InputStream stream;

    @Override
    public void cleanup() {
        DownloadUtils.closeIo(stream);
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }


    @Override
    public InputStream loadData(URL url, long start, long end) throws Exception {
        return loadDataByUrl(url, start, end, true);
    }


    private InputStream loadDataByUrl(URL url, long start, long end, boolean isReTry) throws IOException {
        if (url == null) {
            return null;
        }
//        try {
//            if (url.toURI().equals(url.toURI())) {
//                throw new IOException("In re-direct loop");
//            }
//        } catch (URISyntaxException e) {
//            // Do nothing, this is best effort.
//        }
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        urlConnection.setDoInput(true);
        // Connect explicitly to avoid errors in decoders if connection fails.
        urlConnection.connect();
        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
            stream = urlConnection.getInputStream();
            return stream;
        } else if (statusCode / 100 == 3) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new IOException("Received empty or null redirect url");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            return isReTry ? loadDataByUrl(redirectUrl, start, end, false) : null;
        } else {
            if (statusCode == -1) {
                throw new IOException("Unable to retrieve response code from HttpUrlConnection.");
            }
            throw new IOException("Request failed " + statusCode + ": " + urlConnection.getResponseMessage());
        }
    }


}
