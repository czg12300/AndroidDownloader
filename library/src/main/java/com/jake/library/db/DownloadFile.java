
package com.jake.library.db;

import android.text.TextUtils;

import com.jake.library.DownloadState;
import com.jake.library.data.db.TableColumns;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 描述:下载文件包
 *
 * @author jakechen
 * @since 2016/7/22
 */
public class DownloadFile implements TableColumns {

    /**
     * 下载的文件标识
     */
    public static final String ID = "_id";

    public static final String PART_COUNT = "part_count";

    public static final String TOTAL_SIZE = "total_size";

    public static final String POSITION_SIZE = "position_size";

    public static final String PATH = "path";

    public static final String STATE = "state";

    public static final String PART_IDS = "part_ids";

    public static final String URL = "url";

    public static final String TABLE_NAME = "_file";

    public static final String DEFAULT_SORT_ORDER = ID;

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID
            + " TEXT PRIMARY KEY," + PART_COUNT + " INTEGER," + TOTAL_SIZE + " INTEGER,"
            + POSITION_SIZE + " INTEGER," + STATE + " INTEGER," + PATH + " TEXT," + URL + " TEXT,"
            + PART_IDS + " TEXT," + CREATE_AT + " INTEGER," + MODIFIED_AT + " INTEGER" + ");";

    public String path;

    public String id;

    public String url;

    public long positionSize;

    public long totalSize;

    public int state;

    public int partCount;

    public String[] partIds;

    public long createAt;

    public long modifyAt;

    public String parIds2Json() {
        if (partIds != null && partIds.length > 0) {
            JSONArray array = new JSONArray();
            for (String str : partIds) {
                array.put(str);
            }
            return array.toString();
        }
        return null;

    }

    public String[] json2ParIds(String partIdsJson) {
        if (!TextUtils.isEmpty(partIdsJson)) {
            try {
                JSONArray array = new JSONArray(partIdsJson);
                if (array != null && array.length() > 0) {
                    String[] result = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        result[i] = array.optString(i);
                    }
                    return result;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public boolean isFinish() {
        return state == DownloadState.FINISH;
    }
}
