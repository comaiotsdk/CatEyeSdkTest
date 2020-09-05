package com.comaiot.comaiotsdktest.act;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.net.library.bean.PartNerQueryDevice;
import com.comaiot.net.library.bean.PartnerShareDeviceEntity;
import com.comaiot.net.library.controller.view.PartnerShareDeviceReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareDeviceToPhoneActivity extends AppCompatActivity {

    private EditText mPhoneNumberEditView;
    private EditText mNickNameEditView;
    private Button mBtnShareDevice;

    private PartNerQueryDevice mDevice;
    private String mJwtToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_device_to_phone);

        mDevice = (PartNerQueryDevice) getIntent().getSerializableExtra("device");
        mJwtToken = getIntent().getStringExtra("jwtToken");

        mPhoneNumberEditView = findViewById(R.id.phone_number_edit);
        mNickNameEditView = findViewById(R.id.share_nick_name);
        mBtnShareDevice = findViewById(R.id.btn_share_device);

        mBtnShareDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mPhoneNumberEditView.getText().toString().trim();
                String nickName = mNickNameEditView.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(nickName)) {
                    Toast.makeText(ShareDeviceToPhoneActivity.this, "请输入有效的手机号或者昵称", Toast.LENGTH_SHORT).show();
                    return;
                }

                Pattern p = Pattern.compile("^1(3([0-35-9]\\d|4[1-8])|4[14-9]\\d|5([0125689]\\d|7[1-79])|66\\d|7[2-35-8]\\d|8\\d{2}|9[89]\\d)\\d{7}$");
                Matcher m = p.matcher(phoneNumber);
                boolean find = m.find();
                if (!find) {
                    Toast.makeText(ShareDeviceToPhoneActivity.this, "请输入有效的手机号", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (null == mDevice) return;
                String devUid = mDevice.getDev_uid();

                try {
                    CatEyeSDKInterface.get().partNerShareDevice(devUid, phoneNumber, mJwtToken, nickName, new PartnerShareDeviceReqView() {
                        @Override
                        public void onPartnerShareDeviceReqSuccess(PartnerShareDeviceEntity baseAppEntity) {
                            Toast.makeText(ShareDeviceToPhoneActivity.this, "Share Device Success.", Toast.LENGTH_SHORT).show();
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
                        public void onRequestError(String errorMsg, String methodName) {

                        }
                    });
                } catch (NoAttachViewException e) {
                    e.printStackTrace();
                } catch (NoInternetException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
