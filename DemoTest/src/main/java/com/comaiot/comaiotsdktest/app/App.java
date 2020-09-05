package com.comaiot.comaiotsdktest.app;

import android.app.Application;

import androidx.multidex.MultiDex;

import android.util.Log;

import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

import sw.WorkerThread;

public class App extends Application {
    private WorkerThread mWorkerThread;

    @Override
    public void onCreate() {
        super.onCreate();

        //Cannot fit requested classes in a single dex file (# methods: 131010 > 65536)
        MultiDex.install(this);

        try {
            String ak = "";             //Your AK
            String sk = "";             //Your SK
            CatEyeSDKInterface.init(this, "86", ak, sk);
        } catch (NoAttachViewException e) {
            AppUtils.e("NoAttachViewException: " + e.toString());
        } catch (NoInternetException e) {
            // TODO 无网络异常
            AppUtils.e("NoInternetException: " + e.toString());
        }

        //音视频使用
        initWorkerThread();
    }

    private void initWorkerThread() {
        if (null == mWorkerThread) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void exitWorkerThread() {
        mWorkerThread.exit();

        try {
            //这里添加join的意思就是等工作线程完全退出之后再将工作线程置NULL
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mWorkerThread = null;
    }
}
