package com.jake.library.filenamegenerator;

import com.jake.library.utils.DownloadUtils;

/**
 * Created by jakechen on 2016/10/26.
 */

public class UrlFileNameGenerator implements IFileNameGenerator {
    @Override
    public String name(String url) {
        return DownloadUtils.formatFileNameByUrl(url);
    }
}
