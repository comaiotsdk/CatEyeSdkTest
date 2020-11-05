package com.comaiot.comaiotsdktest.act;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.app.App;
import com.comaiot.comaiotsdktest.intent.MyIntent;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.net.library.core.CatEyeSDKInterface;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc.Constants;
import sw.AGEventHandler;

@SuppressWarnings("all")
public class AudioActivity extends AppCompatActivity implements View.OnClickListener, AGEventHandler {

    private static final String TAG = "AudioTag";

    private App mApp;

    private Chronometer mAudioTime;
    private TextView mAudioStatus;
    private Button mFinishAudioButton;
    private Button mAcceptAudioButton;

    private String mChannel;
    private int mUid;

    private Timer mTimeTimer;
    private TimerTask mTimeTimerTask;

    private static final int AUDIO_SUCCESS_INTENT = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == AUDIO_SUCCESS_INTENT) {
                audioConnectSuccess();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        mChannel = getIntent().getStringExtra("channel");
        mUid = (int) getIntent().getLongExtra("uid", 0);

        mAudioTime = findViewById(R.id.audio_call_time);
        mAudioStatus = findViewById(R.id.audio_call_status);
        mFinishAudioButton = findViewById(R.id.finish_audio_call);
        mAcceptAudioButton = findViewById(R.id.accept_audio_call);
        mFinishAudioButton.setOnClickListener(this);
        mAcceptAudioButton.setOnClickListener(this);

        mApp = (App) getApplication();
        mApp.getWorkerThread().eventHandler().addEventHandler(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntent.DEVICE_CLOSE_REMOTE_SOCKET);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mRemoteAudioListener, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_audio_call:
                CatEyeSDKInterface.get().hangUpAudio(mChannel);
                finish();
                break;
            case R.id.accept_audio_call:
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, 1)) {
                    acceptAudio();
                } else {
                    Toast.makeText(mApp, "请开启录音权限.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            AppUtils.e("Check Permission: " + permission);
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    private void acceptAudio() {
        CatEyeSDKInterface.get().reportAudio(mChannel);
        mApp.getWorkerThread().getRtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        mApp.getWorkerThread().getRtcEngine().adjustAudioMixingVolume(200);
        mApp.getWorkerThread().getRtcEngine().adjustRecordingSignalVolume(100);
        mApp.getWorkerThread().getRtcEngine().joinChannel(null, mChannel, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you

        mAcceptAudioButton.setVisibility(View.GONE);
        mAudioStatus.setVisibility(View.VISIBLE);
        mAudioStatus.setText("正在连接,请稍等...");
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.d(TAG, "onFirstRemoteVideoDecoded: uid= " + uid + " width= " + width + " height= " + height + " elapsed= " + elapsed);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: channel= " + channel + " uid= " + uid + " elapsed= " + elapsed);
        mApp.getWorkerThread().getRtcEngine().setEnableSpeakerphone(true);

        mHandler.sendEmptyMessage(AUDIO_SUCCESS_INTENT);
    }

    private void audioConnectSuccess() {
        mAudioStatus.setText("连接成功,正在进行通话");
        mAudioTime.setVisibility(View.VISIBLE);

        mAudioTime.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - SystemClock.elapsedRealtime()) / 1000 / 60);
        mAudioTime.setFormat("0" + hour + ":%s");
        mAudioTime.start();

        if (null != mTimeTimerTask) {
            mTimeTimerTask.cancel();
            mTimeTimerTask = null;
        }

        if (null != mTimeTimer) {
            mTimeTimer.cancel();
            mTimeTimer = null;
        }

        mTimeTimer = new Timer();
        mTimeTimerTask = new TimerTask() {

            int cnt = 0;

            @Override
            public void run() {
                runOnUiThread(() -> {
                    mAudioTime.setText(getStringTime(cnt++));
                });
            }
        };

        mTimeTimer.schedule(mTimeTimerTask, 0, 1000);
    }

    private String getStringTime(int cnt) {
        int hour = cnt / 3600;
        int min = cnt % 3600 / 60;
        int second = cnt % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, min, second);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d(TAG, "onUserOffline: uid= " + uid + " reason= " + reason);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.d(TAG, "onUserJoined: uid= " + uid + " elapsed= " + elapsed);
    }

    @Override
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
        Log.d(TAG, "onFirstRemoteVideoFrame: uid= " + uid + " width= " + width + " height= " + height + " elapsed= " + elapsed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioTime.stop();
        if (null != mTimeTimerTask) {
            mTimeTimerTask.cancel();
            mTimeTimerTask = null;
        }
        if (null != mTimeTimer) {
            mTimeTimer.cancel();
            mTimeTimer = null;
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mRemoteAudioListener);
        mApp.getWorkerThread().leaveChannel(mApp.getWorkerThread().getEngineConfig().mChannel);
        mApp.getWorkerThread().eventHandler().removeEventHandler(this);
    }

    private BroadcastReceiver mRemoteAudioListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String devUid = intent.getStringExtra("devUid");
            String mode = intent.getStringExtra("mode");
            if (null != mode && mode.equals("audio") && null != devUid && devUid.equals(mChannel)) {
                Toast.makeText(context, "Audio is turned off by Device", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}
