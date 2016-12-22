package com.jake.library.datafetch;

import java.io.InputStream;
import java.net.URL;

/**
 * 数据加载，实现这个类就可以实现自定义下载的http请求
 * Created by jakechen on 2016/10/26.
 */

public interface DownloadDataFetch {
    InputStream loadData(URL url, long start, long end) throws Exception;
    void cleanup();
}
