package com.comaiot.comaiotsdktest.act;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.comaiotsdktest.util.QrUtil;
import com.comaiot.net.library.bean.AppAidEntity;
import com.comaiot.net.library.bean.AppBarcodeReqEntity;
import com.comaiot.net.library.controller.view.AppAidReqView;
import com.comaiot.net.library.controller.view.AppBarcodeReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

@SuppressWarnings("all")
public class ShowQrActivity extends AppCompatActivity {

    private ImageView mCreateQrImage;
    private String mIntentSsid;
    private String mIntentSsid_password;
    private Button mNextButton;

    private String mBindAid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device);

        mIntentSsid = getIntent().getStringExtra("ssid");                       //WIFI SSID
        mIntentSsid_password = getIntent().getStringExtra("ssid_password");     //WIFI PASSWORD

        mCreateQrImage = findViewById(R.id.create_qr_image);
        mNextButton = findViewById(R.id.add_next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toQueryBindStatus();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            CatEyeSDKInterface.get().getAid(new AppAidReqView() {
                @Override
                public void onAppAidReqSuccess(AppAidEntity appAidEntity) {
                    AppAidEntity.Content content = appAidEntity.getContent();
                    String aid = content.getAid();
                    createQrCode(aid);
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

    private void createQrCode(String aid) {
        try {
            CatEyeSDKInterface.get().createQrCode(aid, "86", 8, mIntentSsid, mIntentSsid_password, new AppBarcodeReqView() {
                @Override
                public void onAppBarcodeReqSuccess(AppBarcodeReqEntity appBarcodeReqEntity) {
                    String scan_token = appBarcodeReqEntity.getContent().getScan_token();
                    createQr(scan_token);
                    mBindAid = aid;
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

    private void createQr(String scanToken) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                int width = mCreateQrImage.getWidth();
                AppUtils.i("QrImage width: " + width);
                return QrUtil.createQRCode(scanToken, width);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mCreateQrImage.setImageBitmap(bitmap);
            }
        }.execute();
    }

    private void toQueryBindStatus() {
        Intent intent = new Intent(this, QueryBindStatusActivity.class);
        intent.putExtra("aid", mBindAid);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        AppUtils.d("ShowQr ActivityResult requestCode= " + requestCode + " , resultCode= " + resultCode);
        if (requestCode == 3 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
