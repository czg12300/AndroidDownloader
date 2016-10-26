
package com.jake.library;

/**
 * 描述:下载状态
 *
 * @author jakechen
 * @since 2016/7/29
 */
public interface DownloadState {
    /**
     * 下载中
     */
    int DOWNLOADING = 0x001;

    /**
     * 完成
     */
    int FINISH = 0x002;

    /**
     * 停止
     */
    int STOP = 0x003;
    /**
     * 开始
     */
    int START = 0x004;
}
