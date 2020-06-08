package com.comaiot.comaiotsdktest.act;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.adapter.DeviceEventListAdapter;
import com.comaiot.net.library.bean.DeviceEventEntity;
import com.comaiot.net.library.bean.DeviceEventListEntity;
import com.comaiot.net.library.controller.view.AppDownloadFileReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoInternetException;

import java.util.ArrayList;
import java.util.List;

public class DeviceEventListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String mDevUid;
    private String mDevAid;
    private String mBindDate;

    private ListView mDeviceEventListView;
    private DeviceEventListAdapter mDeviceEventListAdapter;

    private List<DeviceEventListEntity> mDeviceEventList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_event_list);
        mDevUid = getIntent().getStringExtra("devUid");
        mDevAid = getIntent().getStringExtra("aid");
        mBindDate = getIntent().getStringExtra("bindDate");

        mDeviceEventListView = findViewById(R.id.device_event_list_view);
        mDeviceEventListAdapter = new DeviceEventListAdapter(this, mDeviceEventList);
        mDeviceEventListView.setAdapter(mDeviceEventListAdapter);

        mDeviceEventListView.setOnItemClickListener(this);

        try {
            CatEyeSDKInterface.get().getDeviceEventList(mDevAid, mDevUid, Long.parseLong(mBindDate), new AppDownloadFileReqView() {
                @Override
                public void onGetEventList(DeviceEventEntity deviceEventEntity) {
                    List<DeviceEventListEntity> deviceEventList = deviceEventEntity.getListEntities();
                    if (deviceEventList.size() == 0) {
                        Toast.makeText(DeviceEventListActivity.this, "No Event.", Toast.LENGTH_SHORT).show();
                    } else {
                        mDeviceEventList.clear();
                        mDeviceEventList.addAll(deviceEventList);
                        mDeviceEventListAdapter.notifyDataSetChanged();
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
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
