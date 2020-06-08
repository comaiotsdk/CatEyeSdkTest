package com.comaiot.comaiotsdktest.act;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.app.App;
import com.comaiot.comaiotsdktest.intent.MyIntent;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import sw.AGEventHandler;

import static io.agora.rtc.Constants.VOICE_CHANGER_OFF;

@SuppressWarnings("all")
public class VideoActivity extends AppCompatActivity implements AGEventHandler {

    private static final String TAG = "ComaiotVideoTag";

    private App mApp;

    private FrameLayout mContainer;
    private TextView mLoadText;
    private ProgressBar mLoadProgressBar;

    private SurfaceView mSurfaceView;
    private int mUid;
    private String mChannel;

    private int measuredWidth;
    private int measuredHeight;

    private static final int PLAY_VIDEO_INTENT = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == PLAY_VIDEO_INTENT) {
                mLoadText.setVisibility(View.GONE);
                mLoadProgressBar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mChannel = getIntent().getStringExtra("channel");
        mUid = (int) getIntent().getLongExtra("uid", 0);
        Log.e(TAG, "mUid= " + mUid);
        initView();

        mApp = (App) getApplication();
        mApp.getWorkerThread().eventHandler().addEventHandler(this);

        mApp.getWorkerThread().getRtcEngine().adjustRecordingSignalVolume(100);
        mApp.getWorkerThread().getRtcEngine().adjustPlaybackSignalVolume(300);
        mApp.getWorkerThread().configEngine(1, Constants.VIDEO_PROFILE_720P);
        previewVideo();
        mApp.getWorkerThread().getRtcEngine().setLocalVoiceChanger(VOICE_CHANGER_OFF);
        mApp.getWorkerThread().getRtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mApp.getWorkerThread().getRtcEngine().muteLocalAudioStream(true);
        mApp.getWorkerThread().getRtcEngine().muteRemoteAudioStream(mUid, false);
        mApp.getWorkerThread().getRtcEngine().enableLocalVideo(false);

        //每次查看视频只能调用一次，否则会返回错误码，并查看不了视频。
        mApp.getWorkerThread().joinChannel(mChannel, mApp.getWorkerThread().getEngineConfig().mUid);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntent.DEVICE_CLOSE_REMOTE_SOCKET);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mRemoteVideoListener, intentFilter);
    }

    private void initView() {
        mContainer = findViewById(R.id.container_view);
        mLoadText = findViewById(R.id.load_video_text);
        mLoadProgressBar = findViewById(R.id.load_video_progressbar);

        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                measuredWidth = mContainer.getMeasuredWidth();
                measuredHeight = mContainer.getMeasuredHeight();
                mContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                Log.d(TAG, "measuredWidth: " + measuredWidth);
                Log.d(TAG, "measuredHeight: " + measuredHeight);
            }
        });
    }

    private void previewVideo() {
        if (mSurfaceView != null) {
            mContainer.removeView(mSurfaceView);
            mSurfaceView = null;
        }

        mSurfaceView = RtcEngine.CreateRendererView(getApplicationContext());
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.setZOrderMediaOverlay(true);
        mContainer.addView(mSurfaceView);
        mApp.getWorkerThread().preview(true, mSurfaceView, mUid);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.d(TAG, "onFirstRemoteVideoDecoded: uid= " + uid + " width= " + width + " height= " + height + " elapsed= " + elapsed);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: channel= " + channel + " uid= " + uid + " elapsed= " + elapsed);
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
        mHandler.sendEmptyMessage(PLAY_VIDEO_INTENT);
        FrameLayout.LayoutParams surfaceParams = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        if (measuredWidth > 0 && measuredHeight > 0) {
            surfaceParams.width = measuredWidth;
            surfaceParams.height = measuredHeight;
            mSurfaceView.setLayoutParams(surfaceParams);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mRemoteVideoListener);
        mApp.getWorkerThread().leaveChannel(mApp.getWorkerThread().getEngineConfig().mChannel);
        mApp.getWorkerThread().preview(false, null, 0);
        mApp.getWorkerThread().eventHandler().removeEventHandler(this);
    }

    private BroadcastReceiver mRemoteVideoListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String devUid = intent.getStringExtra("devUid");
            String mode = intent.getStringExtra("mode");
            if (null != mode && mode.equals("video") && null != devUid && devUid.equals(mChannel)) {
                Toast.makeText(context, "Video is turned off by Device", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };
}
