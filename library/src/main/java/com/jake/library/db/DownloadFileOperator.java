
package com.jake.library.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jake.library.utils.DLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:下载文件文件
 *
 * @author jakechen
 * @since 2016/7/22
 */
public class DownloadFileOperator {
    private static DownloadFileOperator mInstance;

    public static DownloadFileOperator getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadFileOperator();
        }
        return mInstance;
    }

    private DownloadFileOperator() {
    }

    protected SQLiteDatabase getReadableDatabase() {
        return DownloadDbHelper.getInstance().getReadableDatabase();
    }

    protected SQLiteDatabase getWritableDatabase() {
        return DownloadDbHelper.getInstance().getWritableDatabase();
    }

    public long insert(DownloadFile downloadFile) {
        return insert(getWritableDatabase(), downloadFile);
    }

    public long insert(SQLiteDatabase db, DownloadFile downloadFile) {
        ContentValues cv = createContentValues(downloadFile);
        long createAt = System.currentTimeMillis();
        cv.put(DownloadFile.CREATE_AT, createAt);
        cv.put(DownloadFile.MODIFIED_AT, createAt);
        downloadFile.createAt = createAt;
        downloadFile.modifyAt = createAt;
        return db.insert(DownloadFile.TABLE_NAME, null, cv);
    }

    public long update(ContentValues cv, String selection, String[] selectionArgs) {
        return getWritableDatabase().update(DownloadFile.TABLE_NAME, cv, selection, selectionArgs);
    }


    public long delete(String selection, String[] selectionArgs) {
        return getWritableDatabase().delete(DownloadFile.TABLE_NAME, selection, selectionArgs);
    }

    public List<DownloadFile> query(String selection, String[] selectionArgs, String orderby) {
        List<DownloadFile> result = null;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadFile.TABLE_NAME, null, selection, selectionArgs,
                    null, null, orderby);
            result = getDownloadFileFromCursor(c);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    public List<DownloadFile> query(String selection, String[] selectionArgs, String orderby,
                                    int limit) {
        List<DownloadFile> result = null;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadFile.TABLE_NAME, null, selection, selectionArgs,
                    null, null, orderby, String.valueOf(limit));
            result = getDownloadFileFromCursor(c);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    public DownloadFile query(String selection, String[] selectionArgs) {
        List<DownloadFile> files = query(selection, selectionArgs, null);
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    private List<DownloadFile> getDownloadFileFromCursor(Cursor c) {
        if (c != null) {
            List<DownloadFile> result = new ArrayList<>();
            DownloadFile file = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                file = new DownloadFile();
                file.id = c.getString(c.getColumnIndexOrThrow(DownloadFile.ID));
                file.url = c.getString(c.getColumnIndexOrThrow(DownloadFile.URL));
                file.partCount = c.getInt(c.getColumnIndexOrThrow(DownloadFile.PART_COUNT));
                file.partIds = file
                        .json2ParIds(c.getString(c.getColumnIndexOrThrow(DownloadFile.PART_IDS)));
                file.path = c.getString(c.getColumnIndexOrThrow(DownloadFile.PATH));
                file.positionSize = c.getInt(c.getColumnIndexOrThrow(DownloadFile.POSITION_SIZE));
                file.totalSize = c.getInt(c.getColumnIndexOrThrow(DownloadFile.TOTAL_SIZE));
                file.state = c.getInt(c.getColumnIndexOrThrow(DownloadFile.STATE));
                file.createAt = c.getLong(c.getColumnIndexOrThrow(DownloadFile.CREATE_AT));
                file.modifyAt = c.getLong(c.getColumnIndexOrThrow(DownloadFile.MODIFIED_AT));
                result.add(file);
            }
            return result;

        }
        return null;
    }

    public int getCount(String selection, String[] selectionArgs) {
        String[] projection = {" count(*) "};
        int count = 0;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadFile.TABLE_NAME, projection, selection, selectionArgs, null, null, DownloadFile.DEFAULT_SORT_ORDER);
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }

    public long update(String key, DownloadFile downloadTable) {
        String selection = DownloadFile.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        ContentValues cv = createContentValues(downloadTable);
        long createAt = System.currentTimeMillis();
        cv.put(DownloadFile.MODIFIED_AT, createAt);
        downloadTable.modifyAt = createAt;
        return update(cv, selection, selectionArgs);
    }

    public long delete(String key) {
        String selection = DownloadFile.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        return delete(selection, selectionArgs);
    }

    public DownloadFile query(String key) {
        String selection = DownloadFile.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        return query(selection, selectionArgs);
    }

    private ContentValues createContentValues(DownloadFile downloadTable) {
        ContentValues cv = new ContentValues();
        cv.put(DownloadFile.ID, downloadTable.id);
        cv.put(DownloadFile.PATH, downloadTable.path);
        cv.put(DownloadFile.PART_COUNT, downloadTable.partCount);
        cv.put(DownloadFile.PART_IDS, downloadTable.parIds2Json());
        cv.put(DownloadFile.URL, downloadTable.url);
        cv.put(DownloadFile.POSITION_SIZE, downloadTable.positionSize);
        cv.put(DownloadFile.TOTAL_SIZE, downloadTable.totalSize);
        cv.put(DownloadFile.STATE, downloadTable.state);
        return cv;
    }
}
