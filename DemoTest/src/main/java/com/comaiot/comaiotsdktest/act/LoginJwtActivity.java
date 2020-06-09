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
import com.google.zxing.common.StringUtils;

public class LoginJwtActivity extends AppCompatActivity {

    private EditText mJwtEdtView;
    private Button mLoginJwtButton;
    private Button mSkipButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_jwt);

        mJwtEdtView = findViewById(R.id.edt_input_jwt);
        mLoginJwtButton = findViewById(R.id.login_jwt_button);
        mSkipButton = findViewById(R.id.skip_login);

        mLoginJwtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jwtToken = mJwtEdtView.getText().toString().trim();
                if (TextUtils.isEmpty(jwtToken)) {
                    Toast.makeText(LoginJwtActivity.this, "JWT TOKEN is Empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(LoginJwtActivity.this, MainActivity.class);
                intent.putExtra("jwt", jwtToken);
                startActivity(intent);
                finish();
            }
        });

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginJwtActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
