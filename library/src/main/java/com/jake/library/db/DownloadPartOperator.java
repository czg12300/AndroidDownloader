
package com.jake.library.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jake.library.DownloadPart;
import com.jake.library.data.db.BaseDbOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:下载文件文件
 *
 * @author jakechen
 * @since 2016/7/22
 */
public class DownloadPartOperator extends BaseDbOperator<DownloadPart> {
    private static DownloadPartOperator mInstance;

    public static DownloadPartOperator getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadPartOperator();
        }
        return mInstance;
    }

    private DownloadPartOperator() {
    }

    @Override
    protected SQLiteDatabase getReadableDatabase() {
        return DownloadDbHelper.getInstance().getReadableDatabase();
    }

    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return DownloadDbHelper.getInstance().getWritableDatabase();
    }

    @Override
    public long insert(DownloadPart downloadPart) {
        ContentValues cv = createContentValues(downloadPart);
        long createAt = System.currentTimeMillis();
        cv.put(DownloadPart.CREATE_AT, createAt);
        cv.put(DownloadPart.MODIFIED_AT, createAt);
        downloadPart.createAt = createAt;
        downloadPart.modifyAt = createAt;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long result = getWritableDatabase().insert(DownloadPart.TABLE_NAME, null, cv);
        db.endTransaction();
        return result;
    }

    @Override
    public long update(ContentValues cv, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long result = getWritableDatabase().update(DownloadPart.TABLE_NAME, cv, selection,
                selectionArgs);
        db.endTransaction();
        return result;
    }

    @Override
    public long delete(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long result = db.delete(DownloadPart.TABLE_NAME, selection, selectionArgs);
        db.endTransaction();
        return result;
    }

    @Override
    public List<DownloadPart> query(String selection, String[] selectionArgs, String orderby) {
        List<DownloadPart> result = null;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadPart.TABLE_NAME, null, selection, selectionArgs,
                    null, null, orderby);
            result = getDownloadPartFromCursor(c);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return result;
    }

    @Override
    public List<DownloadPart> query(String selection, String[] selectionArgs, String orderby,
                                    int limit) {
        List<DownloadPart> result = null;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadPart.TABLE_NAME, null, selection, selectionArgs,
                    null, null, orderby, String.valueOf(limit));
            result = getDownloadPartFromCursor(c);
        } catch (Exception e) {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        return result;
    }

    @Override
    public DownloadPart query(String selection, String[] selectionArgs) {
        List<DownloadPart> files = query(selection, selectionArgs, null);
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    private List<DownloadPart> getDownloadPartFromCursor(Cursor c) {
        if (c != null) {
            List<DownloadPart> result = new ArrayList<>();
            DownloadPart file = null;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                file = new DownloadPart();
                file.id = c.getString(c.getColumnIndexOrThrow(DownloadPart.ID));
                file.url = c.getString(c.getColumnIndexOrThrow(DownloadPart.URL));
                file.fileId = c.getString(c.getColumnIndexOrThrow(DownloadPart.FILE_ID));
                file.path = c.getString(c.getColumnIndexOrThrow(DownloadPart.PATH));
                file.rangeStart = c.getInt(c.getColumnIndexOrThrow(DownloadPart.RANGE_START));
                file.rangeEnd = c.getInt(c.getColumnIndexOrThrow(DownloadPart.RANGE_END));
                file.positionSize = c.getInt(c.getColumnIndexOrThrow(DownloadPart.POSITION_SIZE));
                file.totalSize = c.getInt(c.getColumnIndexOrThrow(DownloadPart.TOTAL_SIZE));
                file.state = c.getInt(c.getColumnIndexOrThrow(DownloadPart.STATE));
                file.createAt = c.getLong(c.getColumnIndexOrThrow(DownloadPart.CREATE_AT));
                file.modifyAt = c.getLong(c.getColumnIndexOrThrow(DownloadPart.MODIFIED_AT));
                result.add(file);
            }
            return result;

        }
        return null;
    }

    @Override
    public int getCount(String selection, String[] selectionArgs) {
        String[] projection = {
                " count(*) "
        };
        Cursor c = null;
        try {
            c = getReadableDatabase().query(DownloadPart.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, DownloadPart.DEFAULT_SORT_ORDER);
        } catch (Exception e) {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        }
        return count;
    }

    public long update(String key, DownloadPart downloadPart) {
        String selection = DownloadPart.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        ContentValues cv = createContentValues(downloadPart);
        long createAt = System.currentTimeMillis();
        cv.put(DownloadPart.MODIFIED_AT, createAt);
        downloadPart.modifyAt = createAt;
        return update(cv, selection, selectionArgs);
    }

    private ContentValues createContentValues(DownloadPart downloadPart) {
        ContentValues cv = new ContentValues();
        cv.put(DownloadPart.ID, downloadPart.id);
        cv.put(DownloadPart.RANGE_START, downloadPart.rangeStart);
        cv.put(DownloadPart.RANGE_END, downloadPart.rangeEnd);
        cv.put(DownloadPart.FILE_ID, downloadPart.fileId);
        cv.put(DownloadPart.URL, downloadPart.url);
        cv.put(DownloadPart.POSITION_SIZE, downloadPart.positionSize);
        cv.put(DownloadPart.TOTAL_SIZE, downloadPart.totalSize);
        cv.put(DownloadPart.STATE, downloadPart.state);
        cv.put(DownloadPart.PATH, downloadPart.path);
        return cv;
    }

    public long delete(String key) {
        String selection = DownloadPart.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        return delete(selection, selectionArgs);
    }

    public long deleteAllPart(String fileKey) {
        String selection = DownloadPart.FILE_ID + " = ? ";
        String[] selectionArgs = {
                fileKey
        };
        return delete(selection, selectionArgs);
    }

    public DownloadPart query(String key) {
        String selection = DownloadPart.ID + " = ? ";
        String[] selectionArgs = {
                key
        };
        return query(selection, selectionArgs);
    }

    public List<DownloadPart> queryList(String fileId) {
        String selection = DownloadPart.FILE_ID + " = ? ";
        String[] selectionArgs = {
                fileId
        };
        return query(selection, selectionArgs, null);
    }
}
