package com.easefun.polyv.livehiclass.modules.liveroom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;

/**
 * 确认是否 退出上课页/要下课 的对话框
 */
public class PLVHCExitConfirmDialog {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private Dialog dialog;
    private View view;
    private TextView plvhcLiveRoomCancelTv;
    private TextView plvhcLiveRoomConfirmTv;
    private TextView plvhcLiveRoomExitContentTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCExitConfirmDialog(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.plvhc_live_room_exit_layout, null, false);

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
        plvhcLiveRoomCancelTv = findViewById(R.id.plvhc_live_room_cancel_tv);
        plvhcLiveRoomConfirmTv = findViewById(R.id.plvhc_live_room_confirm_tv);
        plvhcLiveRoomExitContentTv = findViewById(R.id.plvhc_live_room_exit_content_tv);

        plvhcLiveRoomCancelTv.setOnClickListener(new View.OnClickListener() {
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
    public PLVHCExitConfirmDialog setOnPositiveListener(final DialogInterface.OnClickListener listener) {
        plvhcLiveRoomConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                }
            }
        });
        return this;
    }

    public PLVHCExitConfirmDialog setContent(String content) {
        plvhcLiveRoomExitContentTv.setText(content);
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
