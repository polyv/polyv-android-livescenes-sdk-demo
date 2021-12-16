package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;

/**
 * 接收广播通知的对话框
 */
public class PLVHCReceiveBroadcastDialog {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private Dialog dialog;
    private View view;
    private TextView plvhcLinkmicMessageContentTv;
    private TextView plvhcLinkmicMessageConfirmTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCReceiveBroadcastDialog(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.plvhc_linkmic_receive_broadcast_layout, null, false);

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvhcLinkmicMessageContentTv = findViewById(R.id.plvhc_linkmic_message_content_tv);
        plvhcLinkmicMessageConfirmTv = findViewById(R.id.plvhc_linkmic_message_confirm_tv);

        plvhcLinkmicMessageContentTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        plvhcLinkmicMessageContentTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (plvhcLinkmicMessageContentTv.getLineCount() > plvhcLinkmicMessageContentTv.getMaxLines()) {
                    plvhcLinkmicMessageContentTv.setTextSize(12);
                }
                plvhcLinkmicMessageContentTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        plvhcLinkmicMessageConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    private <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public PLVHCReceiveBroadcastDialog setContent(String content) {
        plvhcLinkmicMessageContentTv.setText(content);
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }
    // </editor-fold>
}
