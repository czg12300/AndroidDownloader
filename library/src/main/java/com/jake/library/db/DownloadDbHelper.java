
package com.jake.library.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 描述:下载数据
 *
 * @author jakechen
 * @since 2016/7/22
 */
public class DownloadDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_NAME = "_download.db";
    private static DownloadDbHelper instance;

    public static DownloadDbHelper getInstance() {
        if (instance == null) {
            throw new NullPointerException("DownloadDbHelper instance is null,you have to call install first!");
        }
        return instance;
    }

    public static void install(Context context) {
        instance = new DownloadDbHelper(context);
    }

    private DownloadDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DownloadFile.CREATE_SQL);
        db.execSQL(DownloadPart.CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
