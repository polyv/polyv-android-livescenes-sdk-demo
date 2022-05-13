package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

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


    private int showType = PLV_WINDOW_SHOW_ONLY_BACKGROUND;

    private boolean isNeedShow = false;

    // </editor-fold >

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


    // </editor-fold >

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

        wmLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        wmLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="重写方法">

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="activity 生命周期回调">
    Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if(isNeedShow && !isShow) {//需要显示但是还没有显示
                if(showType != PLVAbsFloatingLayout.PLV_WINDOW_SHOW_ONLY_BACKGROUND){
                    attachContentViewToWindow();
                    isShow = true;
                }
            } else if(isShow){
                if(showType == PLVAbsFloatingLayout.PLV_WINDOW_SHOW_ONLY_BACKGROUND){
                    detachContentViewFromWindow();
                    isShow = false;
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if(isNeedShow && !isShow){//需要显示但是还没有显示
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(showType == PLVAbsFloatingLayout.PLV_WINDOW_SHOW_ONLY_BACKGROUND){
                            if(!AppUtils.isAppForeground()){
                                attachContentViewToWindow();
                                isShow = true;
                            } else {
                                Log.d(TAG, "onActivityStopped in foreground");
                            }
                        } else if(showType == PLV_WINDOW_SHOW_EVERYWHERE){
                            attachContentViewToWindow();
                            isShow = true;
                        }
                    }
                }, 200);

            } else if(isShow){
                if(!AppUtils.isAppForeground()){
                    detachContentViewFromWindow();
                    isShow = false;
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
    // </editor-fold >

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
        if (isShow()) {
            Log.d(TAG, "call show window but already show");
            return;
        }
        if(showType == PLV_WINDOW_SHOW_ONLY_BACKGROUND){
            if(AppUtils.isAppForeground()){
                return;
            }
        } else if(showType == PLV_WINDOW_SHOW_ONLY_FOREGROUND){
            if(!AppUtils.isAppForeground()){
                return;
            }
        }

        attachContentViewToWindow();
        isShow = true;

    }

    @Override
    public void hide() {
        isNeedShow = false;
        if (!isShow()) {
            return;
        }
        detachContentViewFromWindow();
        isShow = false;
    }

    @Override
    public void updateFloatSize(int width, int height) {
        wmLayoutParams.width = width;
        wmLayoutParams.height = height;
        floatWindowWidth = width;
        floatWindowHeight = height;
        if (windowManager != null && isShow()) {
            windowManager.updateViewLayout(this, wmLayoutParams);
        }
    }


    @Override
    public void updateFloatLocation(int x, int y) {


        floatingLocationX = x;
        floatingLocationY = y;
        wmLayoutParams.x = x;
        wmLayoutParams.y = y;
        if (windowManager != null && isShow()) {
            Log.d("Testt", "updateFloatLocation "+floatingLocationX);
            windowManager.updateViewLayout(this, wmLayoutParams);
        }
    }

    @Override
    public Point getFloatLocation() {
        Log.d("Testt", "getFloatLocation "+floatingLocationX);
        return new Point(floatingLocationX, floatingLocationY);
    }

    @Override
    public void destroy() {
        Utils.getApp().unregisterActivityLifecycleCallbacks(callbacks);
        originContentParentVG = null;
        contentView = null;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="私有方法">
    private void attachContentViewToWindow(){
        if (originContentParentVG == null) {
            originContentParentVG = (ViewGroup) contentView.getParent();
            if(originContentParentVG != null) {
                originContentParentVG.removeView(contentView);
            }
            addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        windowManager.addView(this, wmLayoutParams);
    }

    private void detachContentViewFromWindow(){
        windowManager.removeView(this);
    }
    // </editor-fold >

}
