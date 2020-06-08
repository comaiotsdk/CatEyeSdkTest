package com.comaiot.comaiotsdktest.act;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.net.library.bean.DeviceAlarmEvent;
import com.comaiot.net.library.core.CatEyeSDKInterface;

@SuppressWarnings("all")
public class DeviceAlarmEventActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private Button mFinishRing;
    private Button mAcceptRing;

    private String mDevUid;
    private DeviceAlarmEvent mDeviceAlarmEvent;

    private Handler mHandler = new Handler();

    private static final long SHOW_RING_TIME = 30 * 1000L;

    private Runnable mShowRingTimeout = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_alarm_event);

        mImageView = findViewById(R.id.url_image_view);
        mFinishRing = findViewById(R.id.finish_ring);
        mAcceptRing = findViewById(R.id.accept_ring);

        mDevUid = getIntent().getStringExtra("devUid");
        mDeviceAlarmEvent = (DeviceAlarmEvent) getIntent().getSerializableExtra("deviceEvent");

        loadUrl();

        mFinishRing.setOnClickListener(this);
        mAcceptRing.setOnClickListener(this);

        mHandler.postDelayed(mShowRingTimeout, SHOW_RING_TIME);
    }

    private void loadUrl() {
        AppUtils.i("image url: " + mDeviceAlarmEvent.getUrl());
        Glide.with(getApplicationContext()).load(mDeviceAlarmEvent.getUrl()).into(mImageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_ring:
                finish();
                break;
            case R.id.accept_ring:
                CatEyeSDKInterface.get().openDeviceVideo(mDevUid);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mShowRingTimeout);
    }
}
