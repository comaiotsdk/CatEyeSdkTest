package com.comaiot.comaiotsdktest.act;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.intent.MyIntent;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.comaiotsdktest.util.DeviceSettingsCacheUtil;
import com.comaiot.net.library.bean.AppRemoveAidEntity;
import com.comaiot.net.library.bean.DeviceSettings;
import com.comaiot.net.library.bean.DeviceSvrCacheSettings;
import com.comaiot.net.library.bean.DeviceUpdateContent;
import com.comaiot.net.library.bean.GZWXCostomJson;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.bean.UpdateDeviceEntity;
import com.comaiot.net.library.bean.UpdateVersionInfo;
import com.comaiot.net.library.controller.view.AppDownloadDevConfigReqView;
import com.comaiot.net.library.controller.view.AppRemoveAidReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;
import com.comaiot.net.library.inter.GsonUtils;
import com.comaiot.net.library.req_params.AppDownloadDevConfigEntity;
import com.google.gson.reflect.TypeToken;

import java.util.List;

@SuppressWarnings("all")
public class DeviceSettingsActivity extends AppCompatActivity {

    private DeviceSettings mDeviceSettings;
    private PartNerQueryDevice mDevice;
    private String mJwtToken;

    private TextView mDeviceId;
    private TextView mDeviceName;
    private TextView mDeviceRing;
    private TextView mDeviceSound;
    private TextView mDeviceRingLight;
    private TextView mDeviceCheckSwitch;
    private TextView mDeviceAutoPic;
    private TextView mDeviceNotification;
    private TextView mDeviceSensitive;
    private TextView mDeviceAlarmMode;
    private TextView mDeviceTakePicNumber;
    private TextView mDeviceOutdoorAlarm;
    private TextView mDeviceOutdoorSound;
    private TextView mDeviceOutdoorRing;
    private TextView mDeviceAlarm;
    private TextView mDeviceLanguage;
    private TextView mDeviceWorkMode;
    private TextView mDeviceApkVer;
    private TextView mDeviceFirmVer;
    private TextView mDeviceTotalStorage;
    private TextView mDeviceUseStorage;
    private TextView mDeviceIntelligentNight;
    private TextView mDeviceDoorbellLight;
    private Button mTestSetDeviceButton;
    private Button mShareDevice2PhoneNumberButton;
    private Button mShareUsrListButton;
    private Button mDeviceDelete;
    private Button mCheckDeviceUpdate;
    private Button mDeviceDisturbSettings;
    private Button mJoinRegisterFaceBtn;

    private AlertDialog mShowUpdateVerInfoDialog;

    private String mDevUid;

    private static final int SHOW_SETTINGS_INTENT = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_SETTINGS_INTENT) {
                showSettings();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_settings);

        mDeviceSettings = (DeviceSettings) getIntent().getSerializableExtra("deviceSettings");
        mDevice = (PartNerQueryDevice) getIntent().getSerializableExtra("device");
        mJwtToken = getIntent().getStringExtra("jwtToken");
        mDevUid = mDevice.getDev_uid();
        AppUtils.i("DeviceSettings PartNerQueryDevice: " + mDevice);
        AppUtils.i("DeviceSettings PartNerDeviceSettings: " + mDeviceSettings);

        initView();

        registerLocalReceiver();

        if (mDeviceSettings == null) {
            CatEyeSDKInterface.get().getDeviceSettings(mDevUid);
            if (null != mDevice && null != mDevice.getOnline()) {
                getDeviceServerCacheSettings();
            }
        }

        showSettings();

        boolean isMasterAccount = com.comaiot.net.library.utils.AppUtils.checkAccountMaster(mDevice.getApp_aid(), mDevice.getDev_uid());
        if (!isMasterAccount) {
            Toast.makeText(this, "此设备为分享设备.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeviceServerCacheSettings() {
        String appAid = mDevice.getApp_aid();
        String devUid = mDevice.getDev_uid();
        try {
            CatEyeSDKInterface.get().getDeviceConfig(appAid, devUid, new AppDownloadDevConfigReqView() {
                @Override
                public void onGetDeviceConfigSuccess(AppDownloadDevConfigEntity appDownloadDevConfigEntity) {
                    List<AppDownloadDevConfigEntity.Content> contents = appDownloadDevConfigEntity.getContents();
                    if (null != contents && contents.size() > 0) {
                        AppDownloadDevConfigEntity.Content content = contents.get(0);
                        DeviceSvrCacheSettings settings = content.getSettings();

                        DeviceSvrCacheSettings.DeviceStatus deviceStatus = settings.getDevice_status();
                        mDeviceSettings = GsonUtils.fromJson(GsonUtils.toJson(deviceStatus), DeviceSettings.class);

                        mDeviceSettings.setCustomJsonContent(settings.getCustomJsonContent());
                        AppUtils.i("DeviceSettings PartNerDeviceSettings: " + mDeviceSettings);

                        DeviceSettingsCacheUtil.getInstance().put(devUid, mDeviceSettings);

                        showSettings();
                    }
                }

                @Override
                public void showLoading() {

                }

                @Override
                public void hideLoading() {

                }

                @Override
                public void onRequestSuccess() {

                }

                @Override
                public void onRequestError(String s, String s1) {

                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    private void registerLocalReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntent.DEVICE_SETTINGS_CHANGED_INTENT);
        intentFilter.addAction(MyIntent.DEVICE_UPDATE_INTENT);
        intentFilter.addAction(MyIntent.DEVICE_UPDATE_STATUS_INTENT);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mLocalSettingsChanged, intentFilter);
    }

    private void initView() {
        mDeviceId = findViewById(R.id.device_id);
        mDeviceName = findViewById(R.id.device_name);
        mDeviceRing = findViewById(R.id.device_ring);
        mDeviceSound = findViewById(R.id.device_sound);
        mDeviceRingLight = findViewById(R.id.device_ring_light);
        mDeviceCheckSwitch = findViewById(R.id.device_switch_check);
        mDeviceAutoPic = findViewById(R.id.device_auto_pic);
        mDeviceNotification = findViewById(R.id.device_notification_duration);
        mDeviceSensitive = findViewById(R.id.device_sensitive);
        mDeviceAlarmMode = findViewById(R.id.device_alarm_mode);
        mDeviceTakePicNumber = findViewById(R.id.device_tack_pic_num);
        mDeviceOutdoorAlarm = findViewById(R.id.device_out_door_alarm);
        mDeviceOutdoorSound = findViewById(R.id.device_out_door_sound);
        mDeviceOutdoorRing = findViewById(R.id.device_out_door_ring);
        mDeviceAlarm = findViewById(R.id.device_alarm);
        mDeviceLanguage = findViewById(R.id.device_language);
        mDeviceWorkMode = findViewById(R.id.device_mode);
        mDeviceApkVer = findViewById(R.id.device_version_info);
        mDeviceFirmVer = findViewById(R.id.device_firm_version);
        mDeviceTotalStorage = findViewById(R.id.device_total_stroage);
        mDeviceUseStorage = findViewById(R.id.device_use_stroage);
        mDeviceIntelligentNight = findViewById(R.id.device_intelligentNight);
        mDeviceDoorbellLight = findViewById(R.id.device_doorbellLight);
        mTestSetDeviceButton = findViewById(R.id.test_set_device_settings);

        mShareDevice2PhoneNumberButton = findViewById(R.id.share_device_to_phone);
        mShareUsrListButton = findViewById(R.id.share_user_list);
        mDeviceDelete = findViewById(R.id.delete_device);
        mCheckDeviceUpdate = findViewById(R.id.check_device_update);
        mDeviceDisturbSettings = findViewById(R.id.device_disturb_settings);
        mJoinRegisterFaceBtn = findViewById(R.id.device_register_face);

        mDeviceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDevice();
            }
        });

        mShareDevice2PhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceSettingsActivity.this, ShareDeviceToPhoneActivity.class);
                intent.putExtra("device", mDevice);
                intent.putExtra("jwtToken", mJwtToken);
                startActivity(intent);
            }
        });

        mShareUsrListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceSettingsActivity.this, ShareDeviceListActivity.class);
                intent.putExtra("device", mDevice);
                startActivity(intent);
            }
        });

        mTestSetDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //此处为模拟修改参数，在真实环境使用下请按照UI规范来操作

                if (null == mDeviceSettings || null == mDevice || null == mDevice.getOnline())
                    return;
                if (null != mDevice && mDevice.getOnline().equals("offline")) {
                    Toast.makeText(DeviceSettingsActivity.this, "Device is Offline.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mDeviceSettings.setDeviceNickName("Test " + System.currentTimeMillis() / 1000);
                mDeviceSettings.setRing(1);
                CatEyeSDKInterface.get().settingDevice(mDevice.getDev_uid(), mDeviceSettings);
            }
        });

        mCheckDeviceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mDeviceSettings || null == mDevice || null == mDevice.getOnline())
                    return;
                if (null != mDevice && mDevice.getOnline().equals("offline")) {
                    Toast.makeText(DeviceSettingsActivity.this, "Device is Offline.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CatEyeSDKInterface.get().checkDeviceVersion(mDevice.getDev_uid());
            }
        });

        mDeviceDisturbSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mDeviceSettings || null == mDevice || null == mDevice.getOnline())
                    return;
                if (null != mDevice && mDevice.getOnline().equals("offline")) {
                    Toast.makeText(DeviceSettingsActivity.this, "Device is Offline.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(DeviceSettingsActivity.this, DeviceDisturbSettingsActivity.class);
                intent.putExtra("deviceSettings", mDeviceSettings);
                intent.putExtra("device", mDevice);
                startActivity(intent);
            }
        });

        mJoinRegisterFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == mDeviceSettings || null == mDevice || null == mDevice.getOnline())
                    return;
                if (null != mDevice && mDevice.getOnline().equals("offline")) {
                    Toast.makeText(DeviceSettingsActivity.this, "Device is Offline.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CatEyeSDKInterface.get().registerFace(mDevice.getDev_uid());
            }
        });
    }

    private void deleteDevice() {
        try {
            CatEyeSDKInterface.get().deleteDevice(mDevice.getApp_aid(), new AppRemoveAidReqView() {
                @Override
                public void onAppRemoveAidReqSuccess(AppRemoveAidEntity appRemoveAidEntity) {
                    Toast.makeText(DeviceSettingsActivity.this, "Delete Device Success.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void showLoading() {

                }

                @Override
                public void hideLoading() {

                }

                @Override
                public void onRequestSuccess() {

                }

                @Override
                public void onRequestError(String s, String s1) {
                    Toast.makeText(DeviceSettingsActivity.this, "Delete Device Failed.", Toast.LENGTH_SHORT).show();
                    AppUtils.e(s + " : " + s1);
                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    private void showSettings() {
        if (null == mDeviceSettings) return;

        mDeviceId.setText("设备 deviceId: " + mDeviceSettings.getDeviceId());

        mDeviceName.setText("设备名称: " + mDeviceSettings.getDeviceNickName());

        int ring = mDeviceSettings.getRing();
        switch (ring) {
            case 0:
                mDeviceRing.setText("门铃铃声: 传统");
                break;
            case 1:
                mDeviceRing.setText("门铃铃声: 鸟叫");
                break;
            case 2:
                mDeviceRing.setText("门铃铃声: 音乐");
                break;
            case 3:
                mDeviceRing.setText("门铃铃声: 人声");
                break;
        }

        int sound = mDeviceSettings.getSound();
        switch (sound) {
            case 0:
                mDeviceSound.setText("门铃音量: 高");
                break;
            case 1:
                mDeviceSound.setText("门铃音量: 高");
                break;
            case 2:
                mDeviceSound.setText("门铃音量: 高");
                break;
        }

        int ringLight = mDeviceSettings.getRing_light();
        mDeviceRingLight.setText("门铃灯: " + (ringLight == 1 ? "开" : "关"));

        int switchCheck = mDeviceSettings.getPerson_check().getSwitch_check();
        mDeviceCheckSwitch.setText("人体侦测: " + (switchCheck == 1 ? "开" : "关"));

        int autoPic = mDeviceSettings.getPerson_check().getAuto_pic();
        mDeviceAutoPic.setText("人体逗留时常报警: " + autoPic + "秒");

        int alarmIntervalNum = mDeviceSettings.getPerson_check().get_alarm_interval_num();
        mDeviceNotification.setText("报警间隔时间: " + alarmIntervalNum + "秒");

        int sensitive = mDeviceSettings.getPerson_check().getSensitive();
        mDeviceSensitive.setText("侦测灵敏度: " + (sensitive == 1 ? "高" : "低"));

        int alarmMode = mDeviceSettings.getPerson_check().getAlarm_mode();
        mDeviceAlarmMode.setText("抓拍方式: " + (alarmMode == 1 ? "录像" : "拍照"));

        int tackPicNum = mDeviceSettings.getPerson_check().getTack_pic_num();
        mDeviceTakePicNumber.setText("拍照张数: " + tackPicNum + "张");

        int outDoorAlarm = mDeviceSettings.getPerson_check().getOut_door_alarm();
        mDeviceOutdoorAlarm.setText("门外报警声音开关: " + (outDoorAlarm == 1 ? "开" : "关"));

        int outDoorSound = mDeviceSettings.getPerson_check().getOut_door_sound();
        switch (outDoorSound) {
            case 0:
                mDeviceOutdoorSound.setText("门外报警音量: 高");
                break;
            case 1:
                mDeviceOutdoorSound.setText("门外报警音量: 中");
                break;
            case 2:
                mDeviceOutdoorSound.setText("门外报警音量: 低");
                break;
        }

        int outDoorRing = mDeviceSettings.getPerson_check().getOut_door_ring();
        switch (outDoorRing) {
            case 0:
                mDeviceOutdoorRing.setText("门外报警声音: 人声");
                break;
            case 1:
                mDeviceOutdoorRing.setText("门外报警声音: 狗叫");
                break;
            case 2:
                mDeviceOutdoorRing.setText("门外报警声音: 雷达");
                break;
            case 3:
                mDeviceOutdoorRing.setText("门外报警声音: 警车");
                break;
        }

        int alarm = mDeviceSettings.getAlarm();
        mDeviceAlarm.setText("拆机报警: " + ((alarm == 1 ? "开" : "关")));

        int language = mDeviceSettings.getLanguage();
        mDeviceLanguage.setText("设备语言: " + (language == 1 ? "中文" : "English"));

        int mode = mDeviceSettings.getMode();
        switch (mode) {
            case 0:
                mDeviceWorkMode.setText("工作模式: 省电模式");
                break;
            case 1:
                mDeviceWorkMode.setText("工作模式: 正常模式");
                break;
            case 2:
                mDeviceWorkMode.setText("工作模式: 智能省电");
                break;
        }

        String versionInfo = mDeviceSettings.getDeviceInfo().getVersion_info();
        mDeviceApkVer.setText("APK版本号: " + versionInfo);

        String firmVersion = mDeviceSettings.getDeviceInfo().getFirm_version();
        mDeviceFirmVer.setText("固件版本号: " + firmVersion);

        long totalStroage = mDeviceSettings.getDeviceInfo().getTotal_stroage();
        mDeviceTotalStorage.setText("设备总容量: " + totalStroage + "KB");

        long useStroage = mDeviceSettings.getDeviceInfo().getUse_stroage();
        mDeviceUseStorage.setText("设备已用容量: " + useStroage + "KB");

        String model = mDeviceSettings.getDeviceInfo().getModel();
        AppUtils.d("机器设备型号: " + model);

        int intelligentNight = mDeviceSettings.getIntelligentNight();
        switch (intelligentNight) {
            case 0:
                mDeviceIntelligentNight.setText("红外夜视: 自动切换");
                break;
            case 1:
                mDeviceIntelligentNight.setText("红外夜视: 始终关闭");
                break;
            case 2:
                mDeviceIntelligentNight.setText("红外夜视: 始终开启");
                break;
        }

        int doorbellLight = mDeviceSettings.getDoorbellLight();
        //无屏幕机器设置关
        mDeviceDoorbellLight.setText("按门铃亮屏: " + (doorbellLight == 1 ? "开" : "关"));

        String base64JsonStr = mDeviceSettings.getCustomJsonContent();
        String customJson = new String(Base64.decode(base64JsonStr, Base64.NO_WRAP));
        //TODO 客户自行解析 每个客户都有自己自定义的需求
        AppUtils.d("customJson: " + customJson);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mLocalSettingsChanged);
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
            } else if (MyIntent.DEVICE_UPDATE_INTENT.equals(intent.getAction())) {
                UpdateDeviceEntity entity = (UpdateDeviceEntity) intent.getSerializableExtra("updateInfo");
                String devUid = intent.getStringExtra("devUid");
                if (null != entity && mDevUid.equals(devUid)) {
                    // 0 : 无新版本 ; 1 : 有新版本
                    int updateStatus = entity.getUpdateStatus();
                    if (updateStatus == 1) {
                        AlertDialog dialog = new AlertDialog.Builder(DeviceSettingsActivity.this)
                                .setCancelable(true)
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        CatEyeSDKInterface.get().confirmDeviceUpdate(devUid);
                                    }
                                })
                                .create();
                        dialog.setTitle("New Fota Ver.");
                        String newFirmVersionContent = entity.getNewFirmVersionContent();

                        List<DeviceUpdateContent> deviceUpdateContents = GsonUtils.fromJson(entity.getNewFirmVersionContent(), new TypeToken<List<DeviceUpdateContent>>() {
                        }.getType());
                        String deviceUpdateContentMessage = "";
                        if (null != deviceUpdateContents) {
                            deviceUpdateContentMessage = deviceUpdateContents.get(0).getContent();
                        }
                        if (null == deviceUpdateContentMessage || deviceUpdateContentMessage.isEmpty()) {
                            deviceUpdateContentMessage = "";
                        }

                        dialog.setMessage("New Ver: \n" + entity.getNewFirmVersionName() + "\n\n"
                                + "New Ver Date: \n" + entity.getNewFirmVersionDate() + "\n\n"
                                + "New Ver DownloadUrl: \n" + entity.getDownloadUrl() + "\n\n"
                                + "New Ver Message: \n" + deviceUpdateContentMessage);
                        dialog.show();
                    } else {
                        Toast.makeText(DeviceSettingsActivity.this, "Not Hava New Ver.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 无新版本
                    Toast.makeText(DeviceSettingsActivity.this, "Not Hava New Ver.", Toast.LENGTH_SHORT).show();
                }
            } else if (MyIntent.DEVICE_UPDATE_STATUS_INTENT.equals(intent.getAction())) {
                UpdateVersionInfo updateVersionInfo = (UpdateVersionInfo) intent.getSerializableExtra("updateVerInfo");
                String devUid = intent.getStringExtra("devUid");

                if (null != updateVersionInfo && mDevUid.equals(devUid)) {
                    if (null == mShowUpdateVerInfoDialog) {

                        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_message_view, null);

                        mShowUpdateVerInfoDialog = new AlertDialog.Builder(DeviceSettingsActivity.this)
                                .setCancelable(true)
                                .setView(view)
                                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        mShowUpdateVerInfoDialog = null;
                                    }
                                })
                                .create();
                        mShowUpdateVerInfoDialog.setTitle("OTA UPDATING...");
                        mShowUpdateVerInfoDialog.show();
                    }

                    TextView textView = mShowUpdateVerInfoDialog.findViewById(R.id.tv_dialog);

                    if (updateVersionInfo.getUpdateState() == 1 || updateVersionInfo.getUpdateState() == 4) {
                        textView.setText("DevUid: \n" + updateVersionInfo.getDevUid() + "\n\n"
                                + "Status: \n" + getUpdateStatus(updateVersionInfo));
                        if (updateVersionInfo.getUpdateState() == 4 && null != mShowUpdateVerInfoDialog && mShowUpdateVerInfoDialog.isShowing()) {
                            mShowUpdateVerInfoDialog.dismiss();
                        }
                    } else if (updateVersionInfo.getUpdateState() == 2) {
                        textView.setText("DevUid: \n" + updateVersionInfo.getDevUid() + "\n\n"
                                + "Status: \n" + getUpdateStatus(updateVersionInfo) + "\n\n"
                                + "Progress: \n" + updateVersionInfo.getDownloadSize() + "/" + updateVersionInfo.getTotalSize());
                    } else if (updateVersionInfo.getUpdateState() == 3) {
                        textView.setText("DevUid: \n" + updateVersionInfo.getDevUid() + "\n\n"
                                + "Status: \n" + getUpdateStatus(updateVersionInfo) + "\n\n"
                                + "Failed Message: \n" + updateVersionInfo.getFailMsg());
                    }
                }
            }
        }
    };

    private String getUpdateStatus(UpdateVersionInfo updateVersionInfo) {
        switch (updateVersionInfo.getUpdateState()) {
            case 1:
                return "Start Downloading";    //开始下载
            case 2:
                return "Downloading...";          //正在下载
            case 3:
                return "Download Failed";         //下载失败
            case 4:
                return "Download Complete And Device is Rebooting...";       //下载成功
        }
        return "";
    }
}
