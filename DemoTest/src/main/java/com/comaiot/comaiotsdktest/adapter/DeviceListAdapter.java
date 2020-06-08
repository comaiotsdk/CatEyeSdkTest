package com.comaiot.comaiotsdktest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.net.library.bean.PartNerQueryDevice;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {

    private Context mContext;
    private List<PartNerQueryDevice> mList;
    private OnItemButtonClickListener mItemButtonClickListener;
    private OnItemEventButtonClickListener mItemEventButtonClickListener;

    public DeviceListAdapter(Context context, List<PartNerQueryDevice> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PartNerQueryDevice getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_list, null);
            holder = new ViewHolder();
            holder.mDeviceName = convertView.findViewById(R.id.device_name);
            holder.mDeviceStatus = convertView.findViewById(R.id.device_status);
            holder.mDeviceWifi = convertView.findViewById(R.id.device_wifi_name);
            holder.mDeviceRssi = convertView.findViewById(R.id.device_rssi);
            holder.mDeviceSettingsButton = convertView.findViewById(R.id.device_settings);
            holder.mDeviceEventButton = convertView.findViewById(R.id.device_event_list);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PartNerQueryDevice device = mList.get(position);
        holder.mDeviceStatus.setText("在线情况: " + device.getOnline());
        holder.mDeviceName.setText("设备名称: " + (null == device.getDeviceName() ? "" : device.getDeviceName()) + "\nDeviceId: " + device.getDev_uid());
        holder.mDeviceWifi.setText("已连接WiFi: " + (null == device.getWifiName() ? "" : device.getWifiName()));
        holder.mDeviceRssi.setText("Rssi: " + device.getRssi());

        holder.mDeviceSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemButtonClickListener) {
                    mItemButtonClickListener.onItemButtonClick(position);
                }
            }
        });

        holder.mDeviceEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemEventButtonClickListener) {
                    mItemEventButtonClickListener.onItemEventButtonClick(position);
                }
            }
        });

        return convertView;
    }

    public void setOnItemButtonClickListener(OnItemButtonClickListener listener) {
        this.mItemButtonClickListener = listener;
    }

    public void setOnItemEventButtonClickListener(OnItemEventButtonClickListener listener) {
        this.mItemEventButtonClickListener = listener;
    }

    public interface OnItemButtonClickListener {
        void onItemButtonClick(int position);
    }

    public interface OnItemEventButtonClickListener {
        void onItemEventButtonClick(int position);
    }

    static class ViewHolder {
        public TextView mDeviceName;
        public TextView mDeviceStatus;
        public TextView mDeviceWifi;
        public TextView mDeviceRssi;
        public Button mDeviceSettingsButton;
        public Button mDeviceEventButton;
    }
}
