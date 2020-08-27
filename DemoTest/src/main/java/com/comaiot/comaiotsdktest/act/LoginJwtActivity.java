package com.comaiot.comaiotsdktest.act;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.comaiot.comaiotsdktest.R;

import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

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

        String sk = "a9222ecbb94d68194f725475c2dd2d46";                                             //Your SK
        String jti = "89";                                            //Your jti
        String iss = "PointEye";                                            //Your iss
        String phoneNumber = "18211112222";                                    //Your phoneNumber
        long exp = 1296000000;                                      //Your exp
        Log.d("JWT_Token", createJwt(sk, jti, iss, phoneNumber, exp));

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

    /**
     * @param sk          派发的服务器密钥
     * @param jti         客户编号
     * @param iss         客户代码
     * @param phoneNumber 手机号
     * @param ttlMillis   过期时间      示例：1296000000 （15天）= 60*60*24*15*1000 ms
     */
    private String createJwt(String sk, String jti, String iss, String phoneNumber, long ttlMillis) {
        if (TextUtils.isEmpty(sk) || TextUtils.isEmpty(jti) || TextUtils.isEmpty(iss) || TextUtils.isEmpty(phoneNumber)) {
            throw new RuntimeException("Your sk/jti/iss/phoneNumber maybe is empty,Please check code.");
        }
        //指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        //生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        String sub = "{\"userId\":\"" + phoneNumber + "\"}";

        //下面就是在为payload添加各种标准声明和私有声明了
        //这里其实就是new一个JwtBuilder，设置jwt的body
        JwtBuilder builder = Jwts.builder()
                //声明数据格式为JWT
                .setHeaderParam("typ", "JWT")
                //设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                .setId(jti)
                //iat: jwt的签发时间
                .setIssuedAt(now)
                //代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
                .setSubject(sub)
                //iss
                .setIssuer(iss)
                //设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, sk);
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            //设置过期时间
            builder.setExpiration(exp);
        }
        return builder.compact();
    }
}
