package com.comaiot.comaiotsdktest.act;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.adapter.DeviceListAdapter;
import com.comaiot.comaiotsdktest.intent.MyIntent;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.comaiotsdktest.util.DeviceSettingsCacheUtil;
import com.comaiot.net.library.bean.AppControlDevice;
import com.comaiot.net.library.bean.AppQueryDevConnectEntity;
import com.comaiot.net.library.bean.AudioCallEvent;
import com.comaiot.net.library.bean.ConfigDeviceRegisterFaceNameInfo;
import com.comaiot.net.library.bean.CustomBase64JsonContent;
import com.comaiot.net.library.bean.DevRegisterFaceInfo;
import com.comaiot.net.library.bean.DeviceAlarmEvent;
import com.comaiot.net.library.bean.DeviceOnlineEvent;
import com.comaiot.net.library.bean.DeviceRemoveEvent;
import com.comaiot.net.library.bean.DeviceRestartEvent;
import com.comaiot.net.library.bean.DeviceSettings;
import com.comaiot.net.library.bean.DeviceSvrCacheSettings;
import com.comaiot.net.library.bean.DeviceVideoCloseEvent;
import com.comaiot.net.library.bean.DeviceWorkModeChangeEvent;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.bean.PartNerQueryDeviceEntity;
import com.comaiot.net.library.bean.UpdateDeviceEntity;
import com.comaiot.net.library.bean.UpdateVersionInfo;
import com.comaiot.net.library.controller.view.AppDownloadDevConfigReqView;
import com.comaiot.net.library.controller.view.AppQueryDevConnectReqView;
import com.comaiot.net.library.controller.view.AppRefreshTokenView;
import com.comaiot.net.library.controller.view.AppSubscribeReqView;
import com.comaiot.net.library.controller.view.PartNerQueryDeviceListReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.CatEysListener;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;
import com.comaiot.net.library.core.NotRegisterDeviceException;
import com.comaiot.net.library.inter.GsonUtils;
import com.comaiot.net.library.req_params.AppDownloadDevConfigEntity;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity implements CatEysListener, DeviceListAdapter.OnItemButtonClickListener, AdapterView.OnItemClickListener, DeviceListAdapter.OnItemEventButtonClickListener {

    private ListView mDeviceListView;
    private List<PartNerQueryDevice> devices = new ArrayList<>();

    private static final int RELOGIN_INTENT = 1;
    private static final int REFRESH_TOKEN = 2;

    private int RELOGIN_DELAY_TIME = 3000;
    private int delayTime = 1000;
    private int loginExceptionCount = 0;

    private DeviceListAdapter mDeviceListAdapter;

    private RefreshTokenReceiver mRefreshTokenReceiver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            handleParseMessage(msg);
        }
    };
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String intentJwtToken = getIntent().getStringExtra("jwt");
        if (!TextUtils.isEmpty(intentJwtToken)) {
            jwtToken = intentJwtToken;
        } else {
            jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4OSIsImlhdCI6MTU5ODk1OTMyNywic3ViIjoie1widXNlcklkXCI6XCIxODIxMTExMjIyMlwifSIsImlzcyI6IlBvaW50RXllIiwiZXhwIjoxNjAwMjU1MzI3fQ.QykBXPllwRjfMfN-A5LRFFimenLv8mCbHQ7_XMzNUSs";
        }

        initView();

        // SDK 的API详解可以查看ReadMe文件夹中的index.html
        CatEyeSDKInterface.get().setCatEyeMessageListener(MainActivity.this);
        loginSystem();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyIntent.REFRESH_TOKEN_INTENT);
        mRefreshTokenReceiver = new RefreshTokenReceiver();
        registerReceiver(mRefreshTokenReceiver, intentFilter);
    }

    private void loginSystem() {
        /**
         * 关于JWT，不了解的可以参考这个网址：https://www.jianshu.com/p/576dbf44b2ae
         * 每个账户对应一个JWT，如果一直使用一个JWT，会产生数据混乱。
         */

        try {
            CatEyeSDKInterface.get().loginForJwt(jwtToken, new AppSubscribeReqView() {
                @Override
                public void onSubscribeSuccess(long expire) {
                    RELOGIN_DELAY_TIME = 3000;

                    //在 以后 expire 秒内请求数据有效期，时间到了刷新token，建议提前刷新token
                    setRefreshTokenAlarm(expire);
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
                public void onRequestError(String methodName, String errorMessage) {
                    AppUtils.e(methodName + " " + errorMessage);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RELOGIN_DELAY_TIME *= 2;

                            loginSystem();
                        }
                    }, RELOGIN_DELAY_TIME);

                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            AppUtils.e("The mobile phone is not have internet.");
        } catch (NotRegisterDeviceException e) {
            if (loginExceptionCount >= 5) {
                loginExceptionCount = 0;
                delayTime = 1000;
                throw new RuntimeException("Please check your AK/SK is config complete in com.comaiot.comaiotsdktest.app.App.");
            }
            AppUtils.e("please wait a minute,wait sdk register device complete.");
            mHandler.sendEmptyMessageDelayed(RELOGIN_INTENT, delayTime);
            delayTime *= 2;
            loginExceptionCount++;
        }
    }

    private void setRefreshTokenAlarm(long expire) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), RefreshTokenReceiver.class);
        intent.setAction(MyIntent.REFRESH_TOKEN_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 123, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, (expire - 30) * 1000, pendingIntent);
    }

    private void initView() {
        mDeviceListView = findViewById(R.id.device_list_view);

        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(), devices);
        mDeviceListView.setAdapter(mDeviceListAdapter);
        mDeviceListView.setOnItemClickListener(MainActivity.this);
        mDeviceListAdapter.setOnItemButtonClickListener(MainActivity.this);
        mDeviceListAdapter.setOnItemEventButtonClickListener(MainActivity.this);
    }

    private void getDeviceList() {
        try {
            CatEyeSDKInterface.get().partNerGetDeviceList(new PartNerQueryDeviceListReqView() {
                @Override
                public void onGetPartnerDeviceList(PartNerQueryDeviceEntity partNerQueryDeviceEntity) {
                    String listInfo = partNerQueryDeviceEntity.toString();
                    AppUtils.i(listInfo);

                    List<PartNerQueryDevice> listEntities = partNerQueryDeviceEntity.getListEntities();

                    devices.clear();
                    devices.addAll(listEntities);
                    mDeviceListAdapter.notifyDataSetChanged();

                    queryDeviceConnectStatus(listEntities);

                    if (devices.size() == 0) {
                        Toast.makeText(MainActivity.this, "No Bind Device.", Toast.LENGTH_SHORT).show();
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
                public void onRequestError(String methodName, String errorMessage) {

                }
            });
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    private void queryDeviceConnectStatus(List<PartNerQueryDevice> deviceList) {
        List<String> devUids = new ArrayList<>();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                //第一次循环为了尽可能快的去订阅设备
                for (PartNerQueryDevice device : deviceList) {
                    devUids.add(device.getDev_uid());
                }
                String[] devUidArray = (String[]) devUids.toArray(new String[devUids.size()]);
                subscribeDeviceMessageSocket(devUidArray);

                //第二次循环 有序的请求服务器获取在线状态
                for (PartNerQueryDevice device : deviceList) {
                    devUids.add(device.getDev_uid());

                    queryDeviceConnectStatusInterface(device);
                    try {
                        Thread.sleep(getRequestRandom());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        }.execute();
    }

    //这里delay的时间必须 >= 200ms,否则会请求错误
    private long getRequestRandom() {
        int i = new Random().nextInt(100) + 200;
        AppUtils.e("getRequestRandom >>> " + i);
        return i;
    }

    private void queryDeviceConnectStatusInterface(PartNerQueryDevice device) {
        String aid = device.getApp_aid();
        String devUid = device.getDev_uid();
        try {
            CatEyeSDKInterface.get().queryDeviceConnect(aid, devUid, new AppQueryDevConnectReqView() {
                @Override
                public void onQueryDeviceConnectStatus(AppQueryDevConnectEntity appQueryDevConnectEntity) {
                    String content = appQueryDevConnectEntity.getContent();

                    AppUtils.i("devUid: " + appQueryDevConnectEntity.toString());

                    if (null != content) {
                        device.setOnline(content);
                    } else {
                        device.setOnline("offline");
                    }

                    for (PartNerQueryDevice partNerQueryDevice : devices) {
                        String dev_uid = partNerQueryDevice.getDev_uid();
                        String deviceDevUid = device.getDev_uid();
                        if (deviceDevUid.equals(dev_uid)) {
                            partNerQueryDevice.setOnline(device.getOnline());

                            if (!partNerQueryDevice.getOnline().equals("online")) {
                                getDeviceServerCacheSettings(partNerQueryDevice);
                            }
                            break;
                        }
                    }

                    mDeviceListAdapter.notifyDataSetChanged();
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
                public void onRequestError(String methodName, String errorMessage) {
                    AppUtils.e("query " + devUid + " error method: " + methodName + " msg: " + errorMessage);
                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    private void subscribeDeviceMessageSocket(String[] devUidArray) {
        boolean sdkConnected = CatEyeSDKInterface.get().isSDKConnected();
        if (!sdkConnected) {
            CatEyeSDKInterface.get().reconnectSocket();
            return;
        }
        CatEyeSDKInterface.get().subscribe(devUidArray, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                getAllDeviceInfo(devUidArray);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                getAllDeviceInfo(devUidArray);
            }
        });
    }

    private void getAllDeviceInfo(String[] devUidArray) {
        for (String devUid : devUidArray) {
            CatEyeSDKInterface.get().getDeviceSettings(devUid);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device:
                startActivityForResult(new Intent(this, InputWifiActivity.class), 1);
                return true;
            case R.id.add_share_device:
                startActivityForResult(new Intent(this, AddShareDeviceActivity.class), 2);
                return true;
            case R.id.refresh_list:
                getDeviceList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleParseMessage(Message msg) {
        switch (msg.what) {
            case RELOGIN_INTENT:
                mHandler.postDelayed(() -> {
                    loginSystem();
                }, 1000);
                break;
            case REFRESH_TOKEN:
                refreshToken();
                break;
            default:
                break;
        }
    }

    private void refreshToken() {
        try {
            CatEyeSDKInterface.get().refreshToken(new AppRefreshTokenView() {
                @Override
                public void onRefreshTokenSuccess(long expire) {
                    setRefreshTokenAlarm(expire);
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
                public void onRequestError(String errorMsg, String methodName) {

                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        AppUtils.d("Main ActivityResult requestCode= " + requestCode + " , resultCode= " + resultCode);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            getDeviceList();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            getDeviceList();
        } else if (requestCode == 4 && resultCode == RESULT_OK) {
            getDeviceList();
        }
    }

    @Override
    public void onHttpRequestFailed(String methodName, String errorMessage) {
        AppUtils.e(methodName + " " + errorMessage);
    }

    @Override
    public void onMessageSocketConnectSuccess() {
        AppUtils.d("onMessageSocketConnectSuccess ");

        getDeviceList();
    }

    @Override
    public void onMessageSocketConnectFailed() {
        AppUtils.e("onMessageSocketConnectFailed ");
    }

    @Override
    public void onGetDeviceSettings(String devUid, DeviceSettings deviceSettings) {
        AppUtils.d("get " + devUid + " DeviceSettings: " + deviceSettings.toString());

        for (PartNerQueryDevice device : devices) {
            String dev_uid = device.getDev_uid();
            if (dev_uid.equals(devUid)) {
                String deviceNickName = deviceSettings.getDeviceNickName();
                int wifiRssi = deviceSettings.getWifi_rssi();
                String wifiName = deviceSettings.getWifi();

                device.setDeviceName(deviceNickName);
                device.setRssi(wifiRssi);
                device.setWifiName(wifiName);
                break;
            }
        }

        mDeviceListAdapter.notifyDataSetChanged();

        DeviceSettingsCacheUtil.getInstance().put(devUid, deviceSettings);
        Intent intent = new Intent(MyIntent.DEVICE_SETTINGS_CHANGED_INTENT);
        intent.putExtra("devUid", devUid);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDeviceVideoReady(String devUid, long videoUid) {
        AppUtils.d("onDeviceVideoReady get " + devUid + " videoUid: " + videoUid);

        String joinChannel = devUid;            //音视频频道id
        //videoUid 代表音视频频道中的定位某一台设备的id

        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("channel", joinChannel);
        intent.putExtra("uid", videoUid);
        startActivity(intent);
    }

    @Override
    public void onDeviceAlarm(String devUid, DeviceAlarmEvent deviceAlarmEvent) {
        AppUtils.d("get " + devUid + " DeviceAlarmEvent: " + deviceAlarmEvent.toString());

        String alarmEventUrl = deviceAlarmEvent.getUrl();
        if (null == alarmEventUrl) return;

        String fileType = deviceAlarmEvent.getType();
        String eventMode = deviceAlarmEvent.getMode();

        //如果想展示门铃界面及时显示，可将alarmEventUrl判断为空的条件去掉，前后两次门铃事件的时间戳是一样的，下方注释的属性
        //deviceAlarmEvent.getCreateTime();

        if (eventMode.equals("RING") && alarmEventUrl != null) {
            Intent intent = new Intent(this, DeviceAlarmEventActivity.class);
            intent.putExtra("devUid", devUid);
            intent.putExtra("deviceEvent", deviceAlarmEvent);
            startActivity(intent);
        } else if (eventMode.equals("PIR") && alarmEventUrl != null) {
            long createTime = deviceAlarmEvent.getCreateTime();     //动作发生时间
            Toast.makeText(this, devUid + " 侦测到运动\n文件类型: " + fileType, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeviceRemoved(String devUid, DeviceRemoveEvent deviceRemoveEvent) {
        AppUtils.d("get " + devUid + " DeviceRemoveEvent: " + deviceRemoveEvent.toString());
    }

    @Override
    public void onDeviceVideoClose(String devUid, DeviceVideoCloseEvent deviceVideoCloseEvent) {
        AppUtils.d("get " + devUid + " DeviceVideoCloseEvent: " + deviceVideoCloseEvent.toString());
        String mode = deviceVideoCloseEvent.getMode();

        Intent intent = new Intent(MyIntent.DEVICE_CLOSE_REMOTE_SOCKET);
        intent.putExtra("devUid", devUid);
        intent.putExtra("mode", mode);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDeviceCallAudioEvent(String devUid, AudioCallEvent audioCallEvent) {
        AppUtils.d("get " + devUid + " AudioCallEvent: " + audioCallEvent.toString());
        int audioUid = audioCallEvent.getAudio_uid();

        String joinChannel = devUid;            //音视频频道id
        //audioUid 代表音视频频道中的定位某一台设备的id

        Intent intent = new Intent(this, AudioActivity.class);
        intent.putExtra("channel", joinChannel);
        intent.putExtra("uid", audioUid);
        startActivity(intent);
    }

    @Override
    public void onDeviceOnline(String devUid, DeviceOnlineEvent deviceOnlineEvent) {
        AppUtils.d("get " + devUid + " DeviceOnlineEvent: " + deviceOnlineEvent.toString());

        for (PartNerQueryDevice device : devices) {
            String dev_uid = device.getDev_uid();
            if (dev_uid.equals(devUid)) {
                String online = deviceOnlineEvent.getOnline();
                if (online.equals("online")) {
                    device.setOnline("online");
                } else {
                    device.setOnline("offline");

                    getDeviceServerCacheSettings(device);
                }
                break;
            }
        }

        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceWorkModeChanged(String devUid, DeviceWorkModeChangeEvent deviceWorkModeChangeEvent) {
        AppUtils.d("get " + devUid + " DeviceWorkModeChangeEvent: " + deviceWorkModeChangeEvent.toString());
    }

    @Override
    public void onDeviceRestartEvent(String devUid, DeviceRestartEvent deviceRestartEvent) {
        AppUtils.d("get " + devUid + " DeviceRestartEvent: " + deviceRestartEvent.toString());
    }

    @Override
    public void onMessageSocketLost(Throwable throwable) {
        AppUtils.e("onMessageSocketLost: " + throwable.toString());

        CatEyeSDKInterface.get().reconnectSocket();
    }

    @Override
    public void onDeviceUpdateCheckInfo(String devUid, UpdateDeviceEntity entity) {
        String entityInfoStr = null == entity ? "entity is null" : entity.toString();
        AppUtils.d("onDeviceUpdateCheckInfo: devUid= " + devUid + " , entity= " + entityInfoStr);

        Intent intent = new Intent(MyIntent.DEVICE_UPDATE_INTENT);
        intent.putExtra("updateInfo", entity);
        intent.putExtra("devUid", devUid);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDeviceUpdateInfo(String devUid, UpdateVersionInfo info) {
        String updateVerInfoStr = null == info ? "entity is null" : info.toString();
        AppUtils.d("onDeviceUpdateInfo: devUid= " + devUid + " , info= " + updateVerInfoStr);

        Intent intent = new Intent(MyIntent.DEVICE_UPDATE_STATUS_INTENT);
        intent.putExtra("updateVerInfo", info);
        intent.putExtra("devUid", devUid);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDeviceCustomMessageArrived(String devUid, CustomBase64JsonContent content) {
        String customJsonStr = null == content ? "customJson is null" : content.toString();
        AppUtils.d("onDeviceCustomMessageArrived: devUid= " + devUid + " , customJson= " + customJsonStr);

        if (null != content) {
            String base64CustomJson = content.getBase64_custom_json();
            String customJson = new String(Base64.decode(base64CustomJson, Base64.NO_WRAP));
            AppUtils.d("customJson= " + customJson);
            // TODO 自定义JSON，使用者自行对JSON进行解析，因JSON字符串不固定，Bean类需要自定义
            Toast.makeText(this, "Receive custom json:\n" + customJson, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeviceJoinRegisterFaceCallback(String devUid, AppControlDevice controlDevice) {
        String controlDeviceStr = null == controlDevice ? "controlDevice is null" : controlDevice.toString();
        AppUtils.d("onDeviceRegisterFaceCallback: devUid= " + devUid + " , controlDeviceStr= " + controlDeviceStr);

        if (null != controlDevice) {
            int status = controlDevice.getStatus();
            switch (status) {
                case 1:
                    Toast.makeText(this, "Now,Device is Running Rrgister Face.", Toast.LENGTH_SHORT).show();
                    break;
                case 1001:
                    Toast.makeText(this, "The Face Size Max is 10.", Toast.LENGTH_SHORT).show();
                    break;
                case 1002:
                case 1003:
                    Toast.makeText(this, "Device Open Camera Failed,Please Check Device Camera.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onDeviceRegisterFaceProgress(String devUid, DevRegisterFaceInfo devRegisterFaceInfo) {
        String devRegisterFaceInfoStr = null == devRegisterFaceInfo ? "devRegisterFaceInfo is null" : devRegisterFaceInfo.toString();
        AppUtils.d("onDeviceRegisterFaceProgress: devUid= " + devUid + " , devRegisterFaceInfoStr= " + devRegisterFaceInfoStr);

        if (null != devRegisterFaceInfo) {
            int status = devRegisterFaceInfo.getStatus();
            switch (status) {
                case 1:
                    Toast.makeText(this, "Device Register Face is Running.", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, "Device Register Face Success.", Toast.LENGTH_SHORT).show();

                    CatEyeSDKInterface.get().configDeviceRegisterFace(devUid, "Test" + System.currentTimeMillis() / 1000, 0);
                    break;
                case 3:
                    Toast.makeText(this, "Device Exit Register Face.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onConfigDeviceRegisterFaceInfoCallback(String devUid, ConfigDeviceRegisterFaceNameInfo registerFaceNameInfo) {
        String registerFaceNameInfoStr = null == registerFaceNameInfo ? "registerFaceNameInfo is null" : registerFaceNameInfo.toString();
        AppUtils.d("onConfigDeviceRegisterFaceInfoCallback: devUid= " + devUid + " , registerFaceNameInfoStr= " + registerFaceNameInfoStr);

        if (null != registerFaceNameInfo) {
            int status = registerFaceNameInfo.getStatus();
            if (status != 0) {
                switch (status) {
                    case 1001:
                        Toast.makeText(this, "Config Register Face Name not support empty.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1002:
                        Toast.makeText(this, "Config Register Face Name length not support more than 20.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1003:
                        Toast.makeText(this, "Device Already Have This Name For Register Face.", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                String faceName = registerFaceNameInfo.getFaceName();       //人脸数据名称
                int other = registerFaceNameInfo.getOther();                //识别人脸报警开关

                Toast.makeText(this, "Device Register Face Config Success.\nName: " + faceName + ",AlarmSwitch: " + other, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemButtonClick(int position) {
        AppUtils.i("Main onItemButtonClick position: " + position);
        PartNerQueryDevice device = devices.get(position);
        AppUtils.i("Main onItemButtonClick devices.size: " + devices.size());
        AppUtils.i("Main onItemButtonClick PartNerQueryDevice: " + device);
        DeviceSettings deviceSettings = DeviceSettingsCacheUtil.getInstance().get(device.getDev_uid());

        Intent intent = new Intent(this, DeviceSettingsActivity.class);

        if (null != deviceSettings) {
            intent.putExtra("deviceSettings", deviceSettings);
        }
        intent.putExtra("device", device);
        intent.putExtra("jwtToken", jwtToken);

        startActivityForResult(intent, 4);
    }

    private void getDeviceServerCacheSettings(PartNerQueryDevice device) {
        String appAid = device.getApp_aid();
        String devUid = device.getDev_uid();
        try {
            CatEyeSDKInterface.get().getDeviceConfig(appAid, devUid, new AppDownloadDevConfigReqView() {
                @Override
                public void onGetDeviceConfigSuccess(AppDownloadDevConfigEntity entity) {
                    List<AppDownloadDevConfigEntity.Content> contents = entity.getContents();
                    if (contents.size() > 0) {
                        DeviceSvrCacheSettings settings = contents.get(0).getSettings();

                        int mode = settings.getDevice_status().getMode();
                        String deviceId = settings.getDevice_status().getDeviceId();
                        //mode = -1 休眠； 否则离线

                        for (PartNerQueryDevice partNerQueryDevice : devices) {
                            if (partNerQueryDevice.getDev_uid().equals(deviceId) && mode == -1) {
                                partNerQueryDevice.setOnline("Sleep");
                                break;
                            }
                        }

                        mDeviceListAdapter.notifyDataSetChanged();
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
                public void onRequestError(String errorMsg, String methodName) {

                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PartNerQueryDevice device = devices.get(position);
        if ("online".equals(device.getOnline())) {
            String devUid = device.getDev_uid();
            CatEyeSDKInterface.get().openDeviceVideo(devUid);
        } else {
            Toast.makeText(this, "Device is Offline", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemEventButtonClick(int position) {
        PartNerQueryDevice device = devices.get(position);
        String bindDate = device.getBind_date();

        String devUid = device.getDev_uid();
        String appAid = device.getApp_aid();
        Intent intent = new Intent(this, DeviceEventListActivity.class);
        intent.putExtra("devUid", devUid);
        intent.putExtra("aid", appAid);
        intent.putExtra("bindDate", bindDate);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRefreshTokenReceiver);
    }

    public class RefreshTokenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessage(REFRESH_TOKEN);
        }
    }
}
