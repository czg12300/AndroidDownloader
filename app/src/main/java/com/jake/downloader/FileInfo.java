package com.jake.downloader;

/**
 * description：
 *
 * @author Administrator
 * @since 2016/10/26 21:30
 */


public class FileInfo {
    public String name;
    public String icon;
    public String url;
    public boolean isDownloading = false;

    public FileInfo(String name, String icon, String url) {
        this.name = name;
        this.icon = icon;
        this.url = url;
    }
}
