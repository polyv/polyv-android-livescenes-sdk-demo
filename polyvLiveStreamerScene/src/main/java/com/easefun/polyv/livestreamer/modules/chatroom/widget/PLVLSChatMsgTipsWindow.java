package com.easefun.polyv.livestreamer.modules.chatroom.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 聊天信息提示窗口
 */
public class PLVLSChatMsgTipsWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    private View orientedView;
    private TextView tipsMsgTv;

    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSChatMsgTipsWindow(View anchor) {
        View contentView = View.inflate(anchor.getContext(), R.layout.plvls_chatroom_msg_tips_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        popupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        popupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        orientedView = contentView.findViewById(R.id.plvls_oriented_view);
        tipsMsgTv = contentView.findViewById(R.id.plvls_chatroom_tips_msg_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void show(View view, String msg, int left, int right, int bottom) {
        int[] location = new int[2];
        view.getLocationInWindow(location);//window
        int parentWidth = right - left;
        int viewCenterPointX = location[0] + view.getWidth() / 2;

        tipsMsgTv.setText(msg);
        View contentView = popupWindow.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = contentView.getMeasuredWidth();

        int orientedViewLeft;
        int tempX;
        if (parentWidth <= popupWidth || (msg.length() >= 5 && msg.length() <= 10)) {
            tempX = left;
            orientedViewLeft = viewCenterPointX - left - ConvertUtils.dp2px(6);
        } else {
            tempX = right - popupWidth;
            orientedViewLeft = viewCenterPointX - tempX - ConvertUtils.dp2px(6);
        }

        if (orientedView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) orientedView.getLayoutParams();
            layoutParams.leftMargin = orientedViewLeft;
            orientedView.setLayoutParams(layoutParams);
        }

        int x = tempX;
        int y = bottom - ConvertUtils.dp2px(4);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                handler.removeCallbacksAndMessages(null);
            }
        });
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.dismiss();
            }
        }, 3000);
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        popupWindow.setOnDismissListener(onDismissListener);
    }
    // </editor-fold>
}
