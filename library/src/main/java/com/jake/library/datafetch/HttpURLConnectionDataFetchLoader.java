package com.jake.library.datafetch;

/**
 * Created by jakechen on 2016/10/26.
 */

public class HttpURLConnectionDataFetchLoader implements DownloadDataFetchLoader {

    @Override
    public DownloadDataFetch getDataFetch() {
        return new HttpURLConnectionDataFetch();
    }
}
