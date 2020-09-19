package com.comaiot.comaiotsdktest.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.comaiot.comaiotsdktest.R;


public class DialogUtil {
    private static DialogUtil mInstance;
    private static Context mContext;

    private DialogUtil() {
    }

    public static DialogUtil get(Context context) {
        mContext = context;
        if (null == mInstance) {
            synchronized (DialogUtil.class) {
                if (null == mInstance)
                    mInstance = new DialogUtil();
            }
        }
        return mInstance;
    }

    public Dialog getVideoDialog(int width, int height, int x, int y, String dialog_content, final DialogCallback callback) {
        final Dialog dialog = new Dialog(mContext, R.style.BottomDialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.continue_video_dialog, null);
        TextView dialogContent = view.findViewById(R.id.dialog_input_text_view);
        dialogContent.setText(dialog_content);
        Button okBtn = view.findViewById(R.id.dialog_ok);
        Button cancelBtn = view.findViewById(R.id.dialog_cancel);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        //ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        //layoutParams.width = mContext.getResources().getDisplayMetrics().widthPixels;
        /*ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.width = mContext.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(mContext, 16f);
        params.bottomMargin = DensityUtil.dp2px(mContext, 8f);
        view.setLayoutParams(params);*/
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.TOP);
        lp.x = x;
        lp.y = y;
        lp.width = width;
        lp.height = height;
        //lp.alpha = 0.6f;
        window.setAttributes(lp);
        //window.setGravity(Gravity.CENTER);
        //dialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != callback) {
                    callback.onOkPress(dialog);
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != callback) {
                    callback.onCancelPress(dialog);
                }
            }
        });
        return dialog;
    }
}
