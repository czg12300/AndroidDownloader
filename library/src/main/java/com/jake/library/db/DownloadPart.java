
package com.jake.library.db;


import com.jake.library.DownloadState;

/**
 * 描述:下载片段
 *
 * @author jakechen
 * @since 2016/7/29
 */
public class DownloadPart {
    /**
     * 创建时间
     */
    public static final String CREATE_AT = "create_at";

    /**
     * 修改时间
     */
    public static final String MODIFIED_AT = "modified_at";
    public static final String ID = "file_part_id";

    public static final String RANGE_START = "range_start";

    public static final String RANGE_END = "range_end";

    public static final String POSITION_SIZE = "part_position_size";

    public static final String TOTAL_SIZE = "part_total_size";

    public static final String FILE_ID = "file_id";

    public static final String STATE = "state";

    public static final String URL = "url";

    public static final String PATH = "path";

    public static final String TABLE_NAME = "_filePart";

    public static final String DEFAULT_SORT_ORDER = ID;

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + ID
            + " TEXT PRIMARY KEY," + RANGE_START + " INTEGER," + RANGE_END + " INTEGER,"
            + POSITION_SIZE + " INTEGER," + URL + " TEXT," + STATE + " INTEGER," + TOTAL_SIZE
            + " INTEGER," + PATH + " TEXT," + FILE_ID + " TEXT," + CREATE_AT + " INTEGER,"
            + MODIFIED_AT + " INTEGER" + ");";

    public String id;

    public String url;

    public String fileId;

    public String path;

    public long rangeStart;

    public long rangeEnd;

    public long positionSize;

    public long totalSize;

    public int state;

    public long createAt;

    public long modifyAt;

    public boolean isFinish() {
        return state == DownloadState.FINISH;
    }
}
