package com.comaiot.comaiotsdktest.act;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.comaiot.comaiotsdktest.R;

import com.comaiot.comaiotsdktest.app.App;
import com.comaiot.net.library.utils.Logger;
import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import sw.AGEventHandler;

import static io.agora.rtc.Constants.VOICE_CHANGER_BABYGIRL;
import static io.agora.rtc.Constants.VOICE_CHANGER_OFF;
import static io.agora.rtc.Constants.VOICE_CHANGER_OLDMAN;

public class VideoActivity extends AppCompatActivity implements AGEventHandler {

    public static final String VIDEO_UID_INTENT = "comaiot.com.wukong.video.uid";
    public static final int REQUEST_MEDIA_PROJECTION = 1;
    public static final int BASE_VALUE_PERMISSION = 0X0001;
    public static final int PERMISSION_REQ_ID_RECORD_AUDIO = BASE_VALUE_PERMISSION + 1;
    public static final int PERMISSION_REQ_ID_CAMERA = BASE_VALUE_PERMISSION + 2;

    @BindView(R.id.local_video_view_container_layout)
    RelativeLayout mContainerLayout;
    @BindView(R.id.local_video_view_container)
    FrameLayout mContainer;
    @BindView(R.id.btn_hang_up)
    ImageButton mHangUp;
    @BindView(R.id.view_animate_connect)
    RelativeLayout mAnimateConnectView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btn_mute)
    RadioButton mBtnMute;
    @BindView(R.id.btn_mute_pressed)
    RadioButton mBtnMutePressed;
    @BindView(R.id.btn_hangup)
    RadioButton mBtnHangup;
    @BindView(R.id.btn_capture)
    RadioButton mBtnCapture;
    @BindView(R.id.VideoGif)
    ImageView VideoGif;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.count_time)
    TextView countTime;
    @BindView(R.id.view_video)
    RelativeLayout viewVideo;
    @BindView(R.id.btn_mic_off)
    RadioButton btnMicOff;
    @BindView(R.id.btn_mic_on)
    RadioButton btnMicOn;
    @BindView(R.id.device_name)
    TextView deviceName;
    @BindView(R.id.muti_btn_layout)
    RelativeLayout mutiBtnLayout;
    @BindView(R.id.btn_voice_default)
    RadioButton btnVoiceDefault;
    @BindView(R.id.btn_voice_uncle)
    RadioButton btnVoiceUncle;
    @BindView(R.id.btn_voice_strange)
    RadioButton btnVoiceStrange;

    private SurfaceView mSurfaceView;
    private MediaProjectionManager mMediaProjectionManager;
    private Timer mTimer;
    private Timer countTimer;
    private Timer continueVideoTimer;
    private Dialog queryDialog;

    private String channel;
    private int mUid;
    private String model;
    private int statusHeight = 0;

    private boolean connected = false;
    private String device_name = "";
    private boolean mute = true;
    private boolean MicOn = false;
    private String voiceType = "default";

    private App mApp;

    private Intent mintent;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    public void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            } else {
                capturePicture(Activity.RESULT_OK, mintent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mUidReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(VIDEO_UID_INTENT)) {
                Logger.ee("intent.getAction():" + intent.getAction() + " mUid = " + mUid);
                if (mUid != 0) return;
                mUid = intent.getIntExtra("video_uid", 0);
                if (mUid != 0) {
                    preViewVideo();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                connected = true;
                FrameLayout.LayoutParams surfaceParams = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
                Logger.ii("handleMessage measuredWidth:" + measuredWidth + " measuredHeight:" + measuredHeight);
                if (measuredWidth > 0 && measuredHeight > 0) {
                    surfaceParams.width = measuredWidth;
                    surfaceParams.height = measuredHeight;
                    mSurfaceView.setLayoutParams(surfaceParams);
                }

                viewVideo.setVisibility(View.VISIBLE);
                mAnimateConnectView.setVisibility(View.GONE);
            }
        }
    };

    private int measuredWidth;
    private int measuredHeight;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        mApp.getWorkerThread().eventHandler().addEventHandler(this);

        checkSelfPermissions();
        registerUidReceiver();
        device_name = getIntent().getStringExtra("device_name");
        channel = getIntent().getStringExtra("channel");
        mUid = (int)getIntent().getLongExtra("uid", 0);
        model = getIntent().getStringExtra("model");
        if (model != null && model.length() >= 3)
            model = model.substring(0, 3);
        Logger.ee("joinId = " + channel + " video_uid = " + mUid + " model:" + model);
        mApp.getWorkerThread().eventHandler().addEventHandler(this);

        mApp.getWorkerThread().getRtcEngine().adjustRecordingSignalVolume(100);
        mApp.getWorkerThread().getRtcEngine().adjustPlaybackSignalVolume(300);
        setContentView(R.layout.activity_video);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.video_backgroud));
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mesureView();
        configEngine();
        preViewVideo();
        if (device_name != null)
            deviceName.setText(device_name);

        mApp.getWorkerThread().getRtcEngine().setLocalVoiceChanger(VOICE_CHANGER_OFF);
        mApp.getWorkerThread().getRtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mApp.getWorkerThread().getRtcEngine().muteLocalAudioStream(true);
        mApp.getWorkerThread().getRtcEngine().muteRemoteAudioStream(mUid, false);
        mApp.getWorkerThread().getRtcEngine().enableLocalVideo(false);
        mApp.getWorkerThread().joinChannel(channel, mApp.getWorkerThread().getEngineConfig().mUid);

    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.dd("VideoActivity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.dd("VideoActivity onPause");
    }

    private void mesureView() {
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                measuredWidth = mContainer.getMeasuredWidth();
                measuredHeight = mContainer.getMeasuredHeight();
                mContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void preViewVideo() {
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

    private void registerUidReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VIDEO_UID_INTENT);
        registerReceiver(mUidReceiver, filter);
    }

    private void unRegisterUidReceiver() {
        unregisterReceiver(mUidReceiver);
    }

    private boolean checkSelfPermissions() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) &&
                checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Logger.dd("checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }

        if (Manifest.permission.CAMERA.equals(permission)) {
            mApp.initWorkerThread();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Logger.dd("onRequestPermissionsResult " + requestCode + " " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mApp.initWorkerThread();
                } else {
                    finish();
                }
                break;
            }
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    capturePicture(Activity.RESULT_OK, mintent);
                } else {
                    Toast.makeText(VideoActivity.this, "您没有权限", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void configEngine() {
        // cRole 1为主播 2为观众
        mApp.getWorkerThread().configEngine(1, Constants.VIDEO_PROFILE_720P);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Logger.ii("onFirstRemoteVideoDecoded uid = " + uid + " width = "
                + width + " height = " + height + " elapsed = " + elapsed);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Logger.ii("onJoinChannelSuccess channel = " + channel + " uid = "
                + uid + " elapsed = " + elapsed);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Logger.ii("onUserOffline uid = " + uid + " reason = "
                + reason);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Logger.ii("onUserJoined uid = " + uid + " elapsed = "
                + elapsed);
    }

    @Override
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
        Logger.ii("onFirstRemoteVideoFrame uid = " + uid + " width = "
                + width + " height = " + height + " elapsed = " + elapsed);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cancel();
                mTimer = null;
                Message message = mHandler.obtainMessage();
                message.what = 1;
                message.sendToTarget();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countTimer != null) {
            countTimer.cancel();
            countTimer = null;
        }
        if (queryDialog != null) {
            queryDialog.dismiss();
            queryDialog = null;
        }
        if (continueVideoTimer != null) {
            continueVideoTimer.cancel();
            continueVideoTimer = null;
        }

        mApp.getWorkerThread().leaveChannel(mApp.getWorkerThread().getEngineConfig().mChannel);
        mApp.getWorkerThread().preview(false, null, 0);
        mApp.getWorkerThread().eventHandler().removeEventHandler(this);
        unRegisterUidReceiver();
    }

    @OnClick(R.id.btn_hang_up)
    public void onHangUpClicked() {
        finish();
    }

    @OnClick(R.id.btn_hangup)
    public void onHangupClicked() {
        finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @OnClick(R.id.btn_capture)
    public void onCaptureClicked() {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mMediaProjectionManager != null)
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "用户取消了", Toast.LENGTH_SHORT).show();
                return;
            }
            mintent = data;
            verifyStoragePermissions(VideoActivity.this);
        }
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        Logger.dd("hasNavigationBar:" + hasNavigationBar);
        return hasNavigationBar;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void capturePicture(int resultCode, Intent data) {
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ImageReader mImageReader = null;
        Logger.dd("VideoActivity onActivityResult run() appBarLayout.getMeasuredHeight():" + appBarLayout.getMeasuredHeight() + "statusHeight:" + statusHeight);
        Logger.dd("VideoActivity onActivityResult run() (int)(202*dm.density):" + (int) (202 * dm.density) + " mSurfaceView.getMeasuredHeight():" + mSurfaceView.getMeasuredHeight());

        if (!checkDeviceHasNavigationBar(VideoActivity.this)) {
            mImageReader = ImageReader.newInstance(mSurfaceView.getMeasuredWidth(), dm.heightPixels - mutiBtnLayout.getMeasuredHeight() - (int) (20 * dm.density), 0x1, 2);
        } else {
            mImageReader = ImageReader.newInstance(mSurfaceView.getMeasuredWidth(), dm.heightPixels - mutiBtnLayout.getMeasuredHeight() - (int) (40 * dm.density), 0x1, 2);
        }
        if(mImageReader == null) return;

        final MediaProjection mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

        final VirtualDisplay mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                dm.widthPixels, dm.heightPixels, dm.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader mImageReader) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final Handler mhandler = new Handler();
                    Runnable mrunnable = new Runnable() {
                        @Override
                        public void run() {
                            Image image = mImageReader.acquireLatestImage();
                            if (image != null) {
                                Logger.ee("onImageAvailable image:" + image.toString());
                                int width = 0;
                                int height = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    width = image.getWidth();
                                    height = image.getHeight();
                                    final Image.Plane[] planes = image.getPlanes();
                                    final ByteBuffer buffer = planes[0].getBuffer();
                                    int pixelStride = planes[0].getPixelStride();
                                    int rowStride = planes[0].getRowStride();
                                    int rowPadding = rowStride - pixelStride * width;
                                    Bitmap mBitmap;
                                    mBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                                    mBitmap.copyPixelsFromBuffer(buffer);
                                    if (!checkDeviceHasNavigationBar(VideoActivity.this)) {
                                        mBitmap = Bitmap.createBitmap(mBitmap, 0, appBarLayout.getMeasuredHeight() + countTime.getMeasuredHeight() + (int) (50 * dm.density), width, height - (appBarLayout.getMeasuredHeight() + countTime.getMeasuredHeight() + (int) (50 * dm.density)));
                                    } else {
                                        mBitmap = Bitmap.createBitmap(mBitmap, 0, appBarLayout.getMeasuredHeight() + countTime.getMeasuredHeight() + (int) (46 * dm.density), width, height - (appBarLayout.getMeasuredHeight() + countTime.getMeasuredHeight() + (int) (46 * dm.density)));
                                    }
                                    Logger.dd("mBitmap.getWidth():" + mBitmap.getWidth() + " mBitmap.getHeight()." + mBitmap.getHeight());
                                    image.close();
                                    if (mBitmap != null) {
                                        //拿到mitmap
                                        final Bitmap finalMBitmap = mBitmap;
                                        saveBitmap(finalMBitmap);
                                        if (mImageReader != null) {
                                            mImageReader.setOnImageAvailableListener(null, null);
                                            mImageReader.close();
                                        }
                                        if (mMediaProjection != null)
                                            mMediaProjection.stop();
                                        if (mVirtualDisplay != null)
                                            mVirtualDisplay.release();
                                    }
                                }
                            }
                        }
                    };
                    mhandler.postDelayed(mrunnable, 300);
                }
            }
        }, null);
    }

    public void saveBitmap(Bitmap bitmap) {
        SimpleDateFormat sTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = sTimeFormat.format(new Date());
        String fileName = date + ".png";
        File sdcard = Environment.getExternalStorageDirectory();
        File directory = new File(sdcard + "/DCIM/Comaiot");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (directory.isDirectory()) {
            File f = new File(directory, fileName);
            try {
                FileOutputStream out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
                Logger.ee("已经保存");
                Toast.makeText(this, "已保存到DCIM/Comaiot", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {//
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.btn_mute)
    public void onMuteClicked() {
        int enableMute = mApp.getWorkerThread().getRtcEngine().muteRemoteAudioStream(mUid, false);
        Logger.dd("VideoActivity onMuteClicked enableMute:" + enableMute);
        if (enableMute == 0) {
            mute = true;
            mBtnMute.setVisibility(View.GONE);
            mBtnMutePressed.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_mic_on)
    public void onMicOnClicked() {
        int enableMute = mApp.getWorkerThread().getRtcEngine().muteLocalAudioStream(true);
        Logger.dd("VideoActivity onMicOnClicked:" + enableMute);
        if (enableMute == 0) {
            MicOn = false;
            btnMicOn.setVisibility(View.GONE);
            btnMicOff.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_mic_off)
    public void onMicOffClicked() {
        btnMicOn.setVisibility(View.VISIBLE);
        btnMicOff.setVisibility(View.GONE);

        int enableMute = mApp.getWorkerThread().getRtcEngine().muteLocalAudioStream(false);
        Logger.dd("VideoActivity mHandler onMicOffClicked:" + enableMute);
        if (enableMute == 0) {
            MicOn = true;
            btnMicOn.setVisibility(View.VISIBLE);
            btnMicOff.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btn_mute_pressed)
    public void onMutePressedClicked() {
        int enableMute = mApp.getWorkerThread().getRtcEngine().muteRemoteAudioStream(mUid, true);
        Logger.dd("VideoActivity onMutePressedClicked enableMute:" + enableMute);
        if (enableMute == 0) {
            mute = false;
            mBtnMute.setVisibility(View.VISIBLE);
            mBtnMutePressed.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.btn_voice_default, R.id.btn_voice_uncle, R.id.btn_voice_strange})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_voice_default:
                // VOICE_CHANGER_OLDMAN    老男孩
                // VOICE_CHANGER_BABYBOY   小男孩
                // VOICE_CHANGER_BABYGIRL  小女孩
                // VOICE_CHANGER_ETHEREAL  空灵
                // VOICE_CHANGER_HULK      浩克
                // VOICE_CHANGER_ZHUBAJIE  猪八戒
                voiceType = "uncle";
                mApp.getWorkerThread().getRtcEngine().setLocalVoiceChanger(VOICE_CHANGER_OLDMAN);
                btnVoiceDefault.setVisibility(View.GONE);
                btnVoiceUncle.setVisibility(View.VISIBLE);
                btnVoiceStrange.setVisibility(View.GONE);
                break;
            case R.id.btn_voice_uncle:
                voiceType = "strange";
                mApp.getWorkerThread().getRtcEngine().setLocalVoiceChanger(VOICE_CHANGER_BABYGIRL);
                btnVoiceDefault.setVisibility(View.GONE);
                btnVoiceUncle.setVisibility(View.GONE);
                btnVoiceStrange.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_voice_strange:
                voiceType = "default";
                mApp.getWorkerThread().getRtcEngine().setLocalVoiceChanger(VOICE_CHANGER_OFF);
                btnVoiceDefault.setVisibility(View.VISIBLE);
                btnVoiceUncle.setVisibility(View.GONE);
                btnVoiceStrange.setVisibility(View.GONE);
                break;
        }
    }
}
