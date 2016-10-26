package com.jake.library.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * 描述:log日志
 *
 * @author jakechen
 * @since 2015/10/23 15:20
 */
public class DLog {
    public static void setIsOpenLog(boolean is) {
        isOpenLog = is;
    }

    private static boolean isOpenLog = true; // true打开日志,false关闭日志

    private static String TAG = "jake";//tag的名称

    public static void setLogTag(String tag) {
        TAG = tag;
    }

    public static void i(Object o) {
        if (!isOpenLog) {
            return;
        }
        if (null == o) {
            Log.i(TAG, "Object is null");
        } else {
            Log.i(TAG, o.toString());
        }
    }

    public static void i(String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.i(TAG, log);
    }

    public static void i(String... strings) {
        if (!isOpenLog) {
            return;
        }
        if (strings != null && strings.length > 0) {
            String log;
            if (strings.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, length = strings.length; i < length; i++) {
                    sb.append(strings[i]);
                }
                log = sb.toString();
                sb.reverse();
                sb = null;
            } else {
                log = strings[0];
            }
            if (!TextUtils.isEmpty(log)) {
                Log.i(TAG, log);
            }
        }
    }

    public static void i(String name, String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.i(name, log);
    }

    public static void d(Object o) {
        if (!isOpenLog) {
            return;
        }
        if (null == o) {
            Log.d(TAG, "Object is null");
        } else {
            Log.d(TAG, o.toString());
        }
    }

    public static void d(String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.d(TAG, log);
    }

    public static void d(String... strings) {
        if (!isOpenLog) {
            return;
        }
        if (strings != null && strings.length > 0) {
            String log;
            if (strings.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, length = strings.length; i < length; i++) {
                    sb.append(strings[i]);
                }
                log = sb.toString();
                sb.reverse();
                sb = null;
            } else {
                log = strings[0];
            }
            if (!TextUtils.isEmpty(log)) {
                Log.d(TAG, log);
            }
        }
    }

    public static void d(String name, String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.d(name, log);
    }

    public static void e(Class<?> c, String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.e(c.getClass().getSimpleName(), log);
    }

    public static void e(String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.e("error", log);
    }

    public static void w(String log) {
        if (!isOpenLog) {
            return;
        }
        if (TextUtils.isEmpty(log)) {
            log = log + "";
        }
        Log.w(TAG, log);
    }
}