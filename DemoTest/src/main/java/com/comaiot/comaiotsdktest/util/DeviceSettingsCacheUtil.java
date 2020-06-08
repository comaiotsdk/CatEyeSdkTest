package com.comaiot.comaiotsdktest.util;

import com.comaiot.net.library.bean.DeviceSettings;

import java.util.HashMap;
import java.util.Map;

public class DeviceSettingsCacheUtil {
    private static DeviceSettingsCacheUtil instance;

    private Map<String, DeviceSettings> mDeviceSettingsMap;

    private DeviceSettingsCacheUtil() {
        mDeviceSettingsMap = new HashMap<>();
    }

    public static DeviceSettingsCacheUtil getInstance() {
        if (instance == null) {
            synchronized (DeviceSettingsCacheUtil.class) {
                if (null == instance) {
                    instance = new DeviceSettingsCacheUtil();
                }
            }
        }
        return instance;
    }

    public void put(String devUid, DeviceSettings deviceSettings) {
        DeviceSettings settings = mDeviceSettingsMap.get(devUid);
        if (null == settings) {
            mDeviceSettingsMap.put(devUid, deviceSettings);
        } else {
            mDeviceSettingsMap.remove(devUid);
            mDeviceSettingsMap.put(devUid, deviceSettings);
        }
    }

    public DeviceSettings get(String devUid) {
        return mDeviceSettingsMap.get(devUid);
    }
}
