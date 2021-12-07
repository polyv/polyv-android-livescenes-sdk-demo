package com.easefun.polyv.livehiclass.modules.liveroom;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livehiclass.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 成员列表数据类型选择弹层
 */
public class PLVHCMemberTypeWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //window
    private PopupWindow popupWindow;
    //view
    private TextView onlineTv;
    private TextView kickTv;
    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCMemberTypeWindow(View anchor) {
        View contentView = View.inflate(anchor.getContext(), R.layout.plvhc_live_room_member_type_popup_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        popupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        onlineTv = contentView.findViewById(R.id.plvhc_member_online_tv);
        kickTv = contentView.findViewById(R.id.plvhc_member_kick_tv);
        onlineTv.setSelected(true);
        onlineTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onSelectOnlineList();
                }
                v.setSelected(true);
                kickTv.setSelected(false);
                hide();
            }
        });
        kickTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onSelectKickList();
                }
                v.setSelected(true);
                onlineTv.setSelected(false);
                hide();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setIsSimpleLayout() {
        kickTv.setVisibility(View.GONE);
    }

    public void show(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);//window

        int x = location[0] - ConvertUtils.dp2px(4);
        int y = location[1] + view.getHeight() + ConvertUtils.dp2px(8);

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        popupWindow.setOnDismissListener(onDismissListener);
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void updateOnlineCount(int onlineCount) {
        onlineTv.setText("在线学生 ( " + onlineCount + " ) ");
    }

    public void updateKickCount(int kickCount) {
        kickTv.setText("移出学生 ( " + kickCount + " )");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void onSelectOnlineList();

        void onSelectKickList();
    }
    // </editor-fold>
}
