package com.comaiot.comaiotsdktest.act;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.intent.MyIntent;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.comaiotsdktest.util.DeviceSettingsCacheUtil;
import com.comaiot.net.library.bean.DeviceSettings;
import com.comaiot.net.library.bean.GZWXCostomJson;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.inter.GsonUtils;

@SuppressLint("HandlerLeak")
public class DeviceDisturbSettingsActivity extends AppCompatActivity {

    private CheckBox mSwitchOpen;
    private CheckBox mSwitchClose;
    private EditText mStartTime;
    private EditText mEndTime;
    private Button mSettingBtn;

    private DeviceSettings mDeviceSettings;
    private PartNerQueryDevice mDevice;
    private String mDevUid;

    private static final int SHOW_SETTINGS_INTENT = 1;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_SETTINGS_INTENT) {
                checkCallbackMessage();
            }
        }
    };

    private GZWXCostomJson mGzwxCostomJson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disturb_settings);

        mDeviceSettings = (DeviceSettings) getIntent().getSerializableExtra("deviceSettings");
        mDevice = (PartNerQueryDevice) getIntent().getSerializableExtra("device");
        mDevUid = mDevice.getDev_uid();

        AppUtils.i("DeviceDisturbSettingsActivity PartNerQueryDevice: " + mDevice);
        AppUtils.i("DeviceDisturbSettingsActivity PartNerDeviceSettings: " + mDeviceSettings);

        initView();
        registerLocalReceiver();
    }

    private void initView() {
        mSwitchOpen = findViewById(R.id.switch_flag_open);
        mSwitchClose = findViewById(R.id.switch_flag_close);
        mStartTime = findViewById(R.id.start_time_edt);
        mEndTime = findViewById(R.id.end_time_edt);
        mSettingBtn = findViewById(R.id.settings_button);

        mSwitchOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchClose.setChecked(false);
                mSwitchOpen.setChecked(true);
            }
        });

        mSwitchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchOpen.setChecked(false);
                mSwitchClose.setChecked(true);
            }
        });

        mSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startTime = mStartTime.getText().toString().trim();
                String endTime = mEndTime.getText().toString().trim();

                if (mSwitchOpen.isChecked() && (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime))) {
                    Toast.makeText(DeviceDisturbSettingsActivity.this, "参数不合法", Toast.LENGTH_SHORT).show();
                } else if (mSwitchOpen.isChecked() && (startTime.length() != 4 || endTime.length() != 4)) {
                    Toast.makeText(DeviceDisturbSettingsActivity.this, "参数不合法", Toast.LENGTH_SHORT).show();
                } else {
                    mGzwxCostomJson = new GZWXCostomJson();
                    mGzwxCostomJson.setStart_time(startTime);
                    mGzwxCostomJson.setEnd_time(endTime);

                    if (mSwitchClose.isChecked()) {
                        String base64JsonStr = mDeviceSettings.getCustomJsonContent();
                        GZWXCostomJson gzwxCostomJson = GsonUtils.fromJson(new String(Base64.decode(base64JsonStr, Base64.NO_WRAP)), GZWXCostomJson.class);

                        mGzwxCostomJson.setStart_time(gzwxCostomJson.getStart_time());
                        mGzwxCostomJson.setEnd_time(gzwxCostomJson.getEnd_time());
                    }

                    mGzwxCostomJson.setStatus_flag(mSwitchOpen.isChecked() ? 1 : 0);

                    mDeviceSettings.setCustomJsonContent(Base64.encodeToString(GsonUtils.toJson(mGzwxCostomJson).getBytes(), Base64.NO_WRAP));

                    if (null == mDeviceSettings || null == mDevice || null == mDevice.getOnline())
                        return;
                    if (null != mDevice && mDevice.getOnline().equals("offline")) {
                        Toast.makeText(DeviceDisturbSettingsActivity.this, "Device is Offline.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CatEyeSDKInterface.get().settingDevice(mDevUid, mDeviceSettings);
                }
            }
        });
    }

    private void registerLocalReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntent.DEVICE_SETTINGS_CHANGED_INTENT);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mLocalSettingsChanged, intentFilter);
    }

    private void checkCallbackMessage() {
        String base64JsonStr = mDeviceSettings.getCustomJsonContent();
        GZWXCostomJson gzwxCostomJson = GsonUtils.fromJson(new String(Base64.decode(base64JsonStr, Base64.NO_WRAP)), GZWXCostomJson.class);
        if (null != mGzwxCostomJson && null != gzwxCostomJson && gzwxCostomJson.getStatus_flag() == mGzwxCostomJson.getStatus_flag() && gzwxCostomJson.getStart_time().equals(mGzwxCostomJson.getStart_time()) && gzwxCostomJson.getEnd_time().equals(mGzwxCostomJson.getEnd_time())) {
            Toast.makeText(this, "Setting success.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver mLocalSettingsChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUtils.i("LocalSettingsChanged receive");
            if (MyIntent.DEVICE_SETTINGS_CHANGED_INTENT.equals(intent.getAction())) {
                String devUid = intent.getStringExtra("devUid");
                if (mDevUid.equals(devUid)) {
                    mDeviceSettings = DeviceSettingsCacheUtil.getInstance().get(devUid);

                    AppUtils.i("DeviceSettings PartNerDeviceSettings: " + mDeviceSettings);

                    mHandler.sendEmptyMessage(SHOW_SETTINGS_INTENT);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mLocalSettingsChanged);
    }
}
