package com.easefun.polyv.livestreamer.modules.liveroom;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.easefun.polyv.livestreamer.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 连麦请求提示弹窗
 */
public class PLVLSLinkMicRequestTipsWindow {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PopupWindow popupWindow;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSLinkMicRequestTipsWindow(View anchor) {
        View contentView = View.inflate(anchor.getContext(), R.layout.plvls_live_room_linkmic_request_tips_layout, null);
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, false);
        popupWindow.setFocusable(false);//这里必须设置为true才能点击区域外或者消失
        popupWindow.setTouchable(false);//这个控制PopupWindow内部控件的点击事件
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void show(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);//window

        //在控件上方显示
        View contentView = popupWindow.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int popupWidth = contentView.getMeasuredWidth();

        int x = location[0] + view.getWidth() / 2 - popupWidth + ConvertUtils.dp2px(18);
        int y = location[1] + view.getHeight();

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        popupWindow.setOnDismissListener(onDismissListener);
    }
    // </editor-fold>
}
