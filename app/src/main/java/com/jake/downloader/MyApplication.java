package com.jake.downloader;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.jake.library.DownloadConfiguration;
import com.jake.library.Downloader;

import java.io.File;

/**
 * description：
 *
 * @author Administrator
 * @since 2016/10/26 22:18
 */


public class MyApplication extends Application {
 static    Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        DownloadConfiguration.Builder builder = new DownloadConfiguration.Builder();
        builder.setDownloadDir(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "JakeDownload");
        Downloader.install(this, builder.build());
    }
}
