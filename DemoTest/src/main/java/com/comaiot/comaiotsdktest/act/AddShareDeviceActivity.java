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
import com.comaiot.net.library.bean.AppReceiveShareEntity;
import com.comaiot.net.library.controller.view.AppReceiveShareReqView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

public class AddShareDeviceActivity extends AppCompatActivity {

    private EditText mShareNumberEdt;
    private EditText mShareTokenEdt;
    private Button mAddShareNextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_share_device);

        mShareNumberEdt = findViewById(R.id.share_number_value);
        mShareTokenEdt = findViewById(R.id.share_token_value);
        mAddShareNextButton = findViewById(R.id.add_share_next_button);

        mAddShareNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareNumberValue = mShareNumberEdt.getText().toString().trim();
                String shareTokenValue = mShareTokenEdt.getText().toString().trim();

                boolean numberEmpty = TextUtils.isEmpty(shareNumberValue);
                boolean tokenEmpty = TextUtils.isEmpty(shareTokenValue);

                if (numberEmpty && tokenEmpty) {
                    Toast.makeText(AddShareDeviceActivity.this, "Invalid input.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!numberEmpty && !tokenEmpty) {
                    Toast.makeText(AddShareDeviceActivity.this, "分享码 和 分享Token 只能输入一个", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!numberEmpty && shareNumberValue.length() != 8) {
                    Toast.makeText(AddShareDeviceActivity.this, "Invalid Share Number.", Toast.LENGTH_SHORT).show();
                } else if (!numberEmpty) {
                    addShareDevice(shareNumberValue, null);
                } else {
                    addShareDevice(null, shareTokenValue);
                }
            }
        });
    }

    private void addShareDevice(String shareNum, String shareToken) {
        try {
            //shareNum 与 shareToken 互斥为空
            CatEyeSDKInterface.get().joinScanShareDeviceQr(shareNum, shareToken, new AppReceiveShareReqView() {
                @Override
                public void onAppReceiveShareReqSuccess(AppReceiveShareEntity baseAppEntity) {
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
                public void onRequestError(String errorMsg, String methodName) {
                    Toast.makeText(AddShareDeviceActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }
}
