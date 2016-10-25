package com.jake.library.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 描述:数据库打开helper
 *
 * @author jakechen
 * @since 2016/7/21
 */
public class BaseDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    private static final String NAME = "default_db";

    public BaseDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }

    public BaseDbHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public BaseDbHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public BaseDbHelper(Context context) {
        this(context, NAME, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}