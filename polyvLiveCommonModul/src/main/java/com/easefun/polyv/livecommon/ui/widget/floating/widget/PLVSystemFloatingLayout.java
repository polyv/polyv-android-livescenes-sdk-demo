package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.AppUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * 跨应用系统级别悬浮窗Layout
 */
public class PLVSystemFloatingLayout extends PLVAbsFloatingLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private final String TAG = getClass().getSimpleName();
    //系统级别悬浮窗实现
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams wmLayoutParams;
    //悬浮窗填充布局
    private View contentView;
    //contentView的原始布局
    private ViewGroup originContentParentVG;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean isNeedShow = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">

    public PLVSystemFloatingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSystemFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSystemFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWindowManager();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initWindowManager() {
        windowManager = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        wmLayoutParams = new WindowManager.LayoutParams();

        Utils.getApp().registerActivityLifecycleCallbacks(callbacks);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        wmLayoutParams.format = PixelFormat.RGBA_8888;
        wmLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        wmLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="activity 生命周期回调">
    private final Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {

        private void updateContentViewAttach() {
            final boolean needShowOnScreen = isNeedShow && currentFitShowType();
            if (needShowOnScreen && !isShowing) {
                attachContentViewToWindow();
                isShowing = true;
            } else if (!needShowOnScreen && isShowing) {
                detachContentViewFromWindow();
                isShowing = false;
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            updateContentViewAttach();
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateContentViewAttach();
                }
            }, 200);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口实现">

    @Override
    public void setContentView(View view) {
        if (contentView != null) {
            removeView(contentView);
            originContentParentVG = null;
        }
        contentView = view;
    }


    @Override
    public View getContentView() {
        return contentView;
    }

    @Override
    public void show(Activity activity) {
        isNeedShow = true;
        if (isShowing()) {
            PLVCommonLog.d(TAG, "call show window but already show");
            return;
        }
        if (!currentFitShowType()) {
            return;
        }

        attachContentViewToWindow();
        isShowing = true;
    }

    @Override
    public void hide() {
        isNeedShow = false;
        if (!isShowing()) {
            return;
        }
        detachContentViewFromWindow();
        isShowing = false;
    }

    @Override
    public void updateFloatSize(int width, int height) {
        wmLayoutParams.width = width;
        wmLayoutParams.height = height;
        floatWindowWidth = width;
        floatWindowHeight = height;
        if (windowManager != null && isShowing()) {
            windowManager.updateViewLayout(this, wmLayoutParams);
        }
    }


    @Override
    public void updateFloatLocation(int x, int y) {
        x = fitInsideScreenX(x);
        y = fitInsideScreenY(y);
        floatingLocationX = x;
        floatingLocationY = y;
        wmLayoutParams.x = x;
        wmLayoutParams.y = y;
        if (windowManager != null && isShowing()) {
            windowManager.updateViewLayout(this, wmLayoutParams);
        }
    }

    @Override
    public Point getFloatLocation() {
        return new Point(floatingLocationX, floatingLocationY);
    }

    @Override
    public void destroy() {
        Utils.getApp().unregisterActivityLifecycleCallbacks(callbacks);
        originContentParentVG = null;
        contentView = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="私有方法">
    private void attachContentViewToWindow() {
        if (originContentParentVG == null) {
            originContentParentVG = (ViewGroup) contentView.getParent();
            if (originContentParentVG != null) {
                originContentParentVG.removeView(contentView);
            }
            addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        windowManager.addView(this, wmLayoutParams);
    }

    private void detachContentViewFromWindow() {
        windowManager.removeView(this);
    }

    private boolean currentFitShowType() {
        switch (showType) {
            case SHOW_ALWAYS:
                return true;
            case SHOW_ONLY_BACKGROUND:
                return AppUtils.isAppBackground();
            case SHOW_ONLY_FOREGROUND:
                return AppUtils.isAppForeground();
            default:
        }
        return false;
    }

    // </editor-fold>

}
