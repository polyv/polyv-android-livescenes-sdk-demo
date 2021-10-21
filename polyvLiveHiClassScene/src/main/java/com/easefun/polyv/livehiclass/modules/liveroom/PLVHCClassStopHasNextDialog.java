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
import com.plv.foundationsdk.utils.PLVTimeUtils;

/**
 * 下课后有下节课对话框
 */
public class PLVHCClassStopHasNextDialog {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private Dialog dialog;
    private View view;
    private TextView plvhcLiveRoomClassTimeNumberTv;
    private TextView plvhcLiveRoomConfirmTv;
    private TextView plvhcLiveRoomNextClassTitleTv;
    private TextView plvhcLiveRoomNextClassStartTimeTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCClassStopHasNextDialog(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.plvhc_live_room_stop_has_next_layout, null, false);

        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        plvhcLiveRoomClassTimeNumberTv = findViewById(R.id.plvhc_live_room_class_time_number_tv);
        plvhcLiveRoomConfirmTv = findViewById(R.id.plvhc_live_room_confirm_tv);
        plvhcLiveRoomNextClassTitleTv = findViewById(R.id.plvhc_live_room_next_class_title_tv);
        plvhcLiveRoomNextClassStartTimeTv = findViewById(R.id.plvhc_live_room_next_class_start_time_tv);
    }

    private <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public PLVHCClassStopHasNextDialog setOnPositiveListener(final DialogInterface.OnClickListener listener) {
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

    public PLVHCClassStopHasNextDialog setInClassTime(long inClassTime) {
        plvhcLiveRoomClassTimeNumberTv.setText(PLVTimeUtils.generateTime(inClassTime, true));
        return this;
    }

    public PLVHCClassStopHasNextDialog setClassTitle(String classTitle) {
        plvhcLiveRoomNextClassTitleTv.setText(classTitle);
        return this;
    }

    public PLVHCClassStopHasNextDialog setClassStartTime(String classStartTime) {
        plvhcLiveRoomNextClassStartTimeTv.setText(classStartTime);
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
