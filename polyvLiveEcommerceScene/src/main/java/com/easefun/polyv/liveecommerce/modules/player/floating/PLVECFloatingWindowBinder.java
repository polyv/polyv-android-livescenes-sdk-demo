package com.easefun.polyv.liveecommerce.modules.player.floating;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.scenes.PLVECLiveEcommerceActivity;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 浮窗binder
 */
public class PLVECFloatingWindowBinder extends Binder {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmLayoutParams;
    private View parentView;
    private ViewGroup floatLy;
    private View addedView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECFloatingWindowBinder(Context context) {
        windowManager = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);
        wmLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wmLayoutParams.format = PixelFormat.TRANSLUCENT;
        wmLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmLayoutParams.width = ConvertUtils.dp2px(90);
        wmLayoutParams.height = ConvertUtils.dp2px(160);
        wmLayoutParams.x = ConvertUtils.dp2px(16);
        wmLayoutParams.y = ConvertUtils.dp2px(16);

        parentView = LayoutInflater.from(context).inflate(R.layout.plvec_floating_window_layout, null);
        floatLy = parentView.findViewById(R.id.floating_ly);
        floatLy.setOnTouchListener(new FloatingOnTouchListener());
        floatLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PLVECLiveEcommerceActivity.class);
                //service context 6.0↓(test 7.0~9.0 no error) only use Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, report： Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                v.getContext().startActivity(intent);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //need permission
    public void addView(View view) {
        addView(view, true);
    }

    public void addView(View view, boolean isResetLocation) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(view.getContext())) {
            return;
        }
        if (view == null || view.getParent() != null) {
            return;
        }

        if (parentView != null && parentView.getParent() == null) {
            windowManager.addView(parentView, wmLayoutParams);//need permission
        }

        if (floatLy != null) {
            addedView = view;
            floatLy.addView(addedView);
        }

        if (isResetLocation) {
            wmLayoutParams.x = ConvertUtils.dp2px(16);
            wmLayoutParams.y = ConvertUtils.dp2px(16);
            windowManager.updateViewLayout(parentView, wmLayoutParams);
        }
    }

    public View removeView() {
        if (floatLy != null) {
            floatLy.removeAllViews();
        }
        return addedView;
    }

    public void dismiss() {
        if (parentView != null && parentView.getParent() != null) {
            windowManager.removeView(parentView);//need permission, can touch if no remove
        }
    }

    public boolean isShown() {
        return floatLy != null && floatLy.getChildCount() != 0;
    }

    public void destroy() {
        removeView();
        dismiss();
        parentView = null;
        floatLy = null;//null or else leak
        addedView = null;//null or else leak
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 浮窗点击监听器">
    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
        private boolean isMove;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    isMove = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    wmLayoutParams.x = wmLayoutParams.x - movedX;
                    wmLayoutParams.y = wmLayoutParams.y - movedY;
                    windowManager.updateViewLayout(view, wmLayoutParams);
                    if (Math.abs(movedX) >= 5 || Math.abs(movedY) >= 5) {
                        isMove = true;
                    }
                    break;
                default:
                    break;
            }
            //如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
            return isMove;
        }
    }
    // </editor-fold>
}
