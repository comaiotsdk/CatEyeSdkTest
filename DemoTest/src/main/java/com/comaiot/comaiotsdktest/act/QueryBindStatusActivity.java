package com.comaiot.comaiotsdktest.act;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comaiot.comaiotsdktest.R;
import com.comaiot.comaiotsdktest.util.AppUtils;
import com.comaiot.net.library.bean.AppQueryAidBindEntity;
import com.comaiot.net.library.controller.view.AppQueryAidBindRquView;
import com.comaiot.net.library.core.CatEyeSDKInterface;
import com.comaiot.net.library.core.NoAttachViewException;
import com.comaiot.net.library.core.NoInternetException;

import java.util.Random;

@SuppressWarnings("all")
public class QueryBindStatusActivity extends AppCompatActivity {

    private TextView mBindStatusText;
    private ProgressBar mProgressBar;
    private Button mNextButton;

    private String mQueryAid;

    //查询超时 3分钟
    private static final long QUERY_BINT_TIMEOUT_TIME = 3 * 60 * 1000L;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    private Runnable mQueryTask = new Runnable() {
        @Override
        public void run() {
            query();

            queryBindStatus();
        }
    };

    private Runnable mQueryTimeout = new Runnable() {
        @Override
        public void run() {
            mBindStatusText.setText("绑定超时");
            mProgressBar.setVisibility(View.GONE);
            mNextButton.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_bind);

        mQueryAid = getIntent().getStringExtra("aid");

        mBindStatusText = findViewById(R.id.bind_status_text);
        mProgressBar = findViewById(R.id.query_progressbar);
        mNextButton = findViewById(R.id.ok_next_button);

        queryBindStatus();
        mHandler.postDelayed(mQueryTimeout, QUERY_BINT_TIMEOUT_TIME);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okComplete();
            }
        });
    }

    private void queryBindStatus() {
        mHandler.removeCallbacks(mQueryTask);
        mHandler.postDelayed(mQueryTask, getPeriod());
    }

    private int getPeriod() {
        return new Random().nextInt(3000) + 5000;
    }

    private void query() {
        try {
            CatEyeSDKInterface.get().queryBindStatus(mQueryAid, new AppQueryAidBindRquView() {
                @Override
                public void onAppQueryAidBindReqSuccess(AppQueryAidBindEntity appQueryAidBindEntity) {
                    String status = appQueryAidBindEntity.getContent().getStatus();
                    AppUtils.i("query aid bind: " + status);

                    if (!status.equals(AppQueryAidBindEntity.NoRecord)) {
                        parseStatus(status);
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
                public void onRequestError(String s, String s1) {

                }
            });
        } catch (NoAttachViewException e) {
            e.printStackTrace();
        } catch (NoInternetException e) {
            e.printStackTrace();
        }
    }

    private void parseStatus(String status) {
        mHandler.removeCallbacks(mQueryTask);
        mHandler.removeCallbacks(mQueryTimeout);

        mNextButton.setVisibility(View.VISIBLE);
        if (status.equals(AppQueryAidBindEntity.RecordBindBefore)) {
            //绑定成功 -> 之前已经绑定过了
            mBindStatusText.setText("绑定成功 -> 重复绑定");
            mProgressBar.setVisibility(View.GONE);
        } else if (status.equals(AppQueryAidBindEntity.RecordWithDevUid)) {
            //绑定成功
            mBindStatusText.setText("绑定成功");
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void okComplete() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mQueryTask);
        mHandler.removeCallbacks(mQueryTimeout);
    }
}
