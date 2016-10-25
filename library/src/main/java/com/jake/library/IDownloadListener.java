
package com.jake.library;

/**
 * 描述:下载监听器
 *
 * @author jakechen
 * @since 2016/7/21
 */
public interface IDownloadListener {

    void onSuccess(String url, String filePath);

    void onFail(String url);
    void onPause(String url);

    void onProgress(String url, long positionSize, long totalSize);
}
