package com.comaiot.comaiotsdktest.act;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.util.AppUtils;

public class InputWifiActivity extends AppCompatActivity {

    private EditText mEdtSsid;
    private EditText mEdtSsid_pwd;
    private Button mNextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_wifi_info);

        mEdtSsid = findViewById(R.id.input_ssid);
        mEdtSsid_pwd = findViewById(R.id.input_ssid_password);
        mNextButton = findViewById(R.id.input_next_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSsid();
            }
        });
    }

    private void checkSsid() {
        String ssid = mEdtSsid.getText().toString();
        String ssid_password = mEdtSsid_pwd.getText().toString();

        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(ssid_password)) {
            Toast.makeText(this, "not support empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ShowQrActivity.class);
        intent.putExtra("ssid", ssid);
        intent.putExtra("ssid_password", ssid_password);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        AppUtils.d("InputWiFi ActivityResult requestCode= " + requestCode + " , resultCode= " + resultCode);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
