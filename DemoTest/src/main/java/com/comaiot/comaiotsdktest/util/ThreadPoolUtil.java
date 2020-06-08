package com.comaiot.comaiotsdktest.util;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static final int MAX_THREAD_NUMBER = 1;

    private static ScheduledExecutorService scheduledExecutorService;

    static {
        synchronized (ThreadPoolUtil.class) {
            if (scheduledExecutorService == null) {
                scheduledExecutorService = Executors.newScheduledThreadPool(MAX_THREAD_NUMBER);
                Log.d("ThreadPoolUtil", "------ newScheduledThreadPool ------");
            }
        }
    }

    public static void scheduleAtFixedRate(Runnable runnable, long initialDelay, long period) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public static void scheduleDelay(Runnable runnable, long delay) {
        scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
