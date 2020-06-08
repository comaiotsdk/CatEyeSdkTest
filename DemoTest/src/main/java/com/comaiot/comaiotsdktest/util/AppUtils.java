package com.comaiot.comaiotsdktest.util;

import android.util.Log;

public class AppUtils {
    private static final String TAG = "ComaiotSDK";
    private static final boolean TAG_FLAG = true;

    public static void i(String tag, String info) {
        if (TAG_FLAG) {
            Log.i(tag, info);
        }
    }

    public static void i(String info) {
        if (TAG_FLAG) {
            Log.i(TAG, info);
        }
    }

    public static void d(String tag, String debug) {
        if (TAG_FLAG) {
            Log.d(tag, debug);
        }
    }

    public static void d(String debug) {
        if (TAG_FLAG) {
            Log.d(TAG, debug);
        }
    }

    public static void e(String tag, String err) {
        if (TAG_FLAG) {
            Log.e(tag, err);
        }
    }

    public static void e(String err) {
        if (TAG_FLAG) {
            Log.e(TAG, err);
        }
    }
}
