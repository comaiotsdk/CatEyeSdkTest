package com.comaiot.comaiotsdktest.act;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.util.QrUtil;
import com.comaiot.net.library.bean.AppShareDeviceEntity;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.controller.view.AppShareDeviceReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

@SuppressWarnings("all")
public class ShareDeviceActivity extends AppCompatActivity {

    private ImageView mShareDeviceQrCodeImgView;
    private TextView mShareDeviceNumTextView;
    private PartNerQueryDevice mDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device);
        mDevice = (PartNerQueryDevice) getIntent().getSerializableExtra("device");

        mShareDeviceQrCodeImgView = findViewById(R.id.share_device_qr_code);
        mShareDeviceNumTextView = findViewById(R.id.share_device_number);

        getShareTokenAndNumber();
    }

    private void getShareTokenAndNumber() {
        if (null == mDevice) return;
        String devUid = mDevice.getDev_uid();
        String appAid = mDevice.getApp_aid();
        try {
            CatEyeSDKInterface.get().createShareDeviceQr(appAid, devUid, new AppShareDeviceReqView() {
                @Override
                public void onAppShareDeviceReqSuccess(AppShareDeviceEntity baseAppEntity) {
                    showQrAndNumber(baseAppEntity);
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

    private void showQrAndNumber(AppShareDeviceEntity entity) {
        String shareToken = entity.getContent().getShare_token();
        String shareNum = entity.getContent().getShare_num();
        mShareDeviceNumTextView.setText("分享码: " + shareNum);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                return QrUtil.createQRCode(shareToken, mShareDeviceQrCodeImgView.getWidth());
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mShareDeviceQrCodeImgView.setImageBitmap(bitmap);
            }
        }.execute();
    }
}
