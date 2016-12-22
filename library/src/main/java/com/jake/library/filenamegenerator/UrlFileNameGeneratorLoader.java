package com.jake.library.filenamegenerator;

/**
 * Created by jakechen on 2016/10/26.
 */

public class UrlFileNameGeneratorLoader implements FileNameGeneratorLoader {

    @Override
    public FileNameGenerator getFileNameGenerator() {
        return new UrlFileNameGenerator();
    }
}
