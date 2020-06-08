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
import com.comaiot.comaiotsdktest.adapter.SharedUsrListAdapter;
import com.comaiot.net.library.bean.AppQuerySharedDeviceEntity;
import com.comaiot.net.library.bean.AppRemoveSharedDeviceEntity;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.controller.view.AppQuerySharedDeviceReqView;
import com.comaiot.net.library.controller.view.AppRemoveSharedDeviceReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShareDeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private PartNerQueryDevice mDevice;
    private ListView mSharedUsrListView;
    private SharedUsrListAdapter mSharedUsrListAdapter;

    private List<AppQuerySharedDeviceEntity.ShareUser> shareUsers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device_list);
        mDevice = (PartNerQueryDevice) getIntent().getSerializableExtra("device");

        mSharedUsrListView = findViewById(R.id.shared_usr_list);
        mSharedUsrListAdapter = new SharedUsrListAdapter(this, shareUsers);
        mSharedUsrListView.setAdapter(mSharedUsrListAdapter);

        mSharedUsrListView.setOnItemClickListener(this);

        getSharedUsr();
    }

    private void getSharedUsr() {
        if (null == mDevice) return;
        String appAid = mDevice.getApp_aid();
        try {
            CatEyeSDKInterface.get().getShareDevice(appAid, new AppQuerySharedDeviceReqView() {
                @Override
                public void onGetSharedDevice(AppQuerySharedDeviceEntity entity) {
                    Map<String, AppQuerySharedDeviceEntity.ShareUser> list = entity.getContent().getList();
                    shareUsers.clear();
                    shareUsers.addAll(list.values());
                    mSharedUsrListAdapter.notifyDataSetChanged();

                    if (shareUsers.size() == 0) {
                        Toast.makeText(ShareDeviceListActivity.this, "No Shared User.", Toast.LENGTH_SHORT).show();
                        finish();
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
        AppQuerySharedDeviceEntity.ShareUser shareUser = shareUsers.get(position);
        AlertDialog alertDialog = new AlertDialog.Builder(ShareDeviceListActivity.this)
                .setTitle("删除此分享吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSharedUsr(shareUser);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void deleteSharedUsr(AppQuerySharedDeviceEntity.ShareUser shareUser) {
        try {
            CatEyeSDKInterface.get().removeSharedAccount(shareUser.getApp_aid(), new AppRemoveSharedDeviceReqView() {
                @Override
                public void onAppRemoveSharedDeviceSuccess(AppRemoveSharedDeviceEntity baseAppEntity) {
                    getSharedUsr();
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
}
