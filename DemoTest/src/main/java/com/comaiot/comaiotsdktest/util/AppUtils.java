package com.comaiot.comaiotsdktest.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

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

    public static boolean isActFront(Context context, String actName) {
        if (context == null || TextUtils.isEmpty(actName))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (actName.equals(cpn != null ? cpn.getClassName() : null))
                return true;
        }
        return false;
    }
}
