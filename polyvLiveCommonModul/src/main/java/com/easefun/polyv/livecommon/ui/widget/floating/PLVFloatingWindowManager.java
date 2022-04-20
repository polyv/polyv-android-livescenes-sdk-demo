package com.easefun.polyv.livecommon.ui.widget.floating;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.ui.widget.floating.widget.IPLVFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVAbsFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVAppFloatingLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.widget.PLVSystemFloatingLayout;

/**
 * 单例，悬浮窗管理类
 *
 */
public class PLVFloatingWindowManager {

    // <editor-fold defaultstate="collapsed" desc="变量">

    //单例
    private static volatile PLVFloatingWindowManager INSTANCE = null;

    //悬浮窗builder
    private WindowBuilder windowBuilder;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="单例">
    public static PLVFloatingWindowManager getInstance() {
        if (INSTANCE == null) {
            synchronized (PLVFloatingWindowManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVFloatingWindowManager();
                }
            }
        }
        return INSTANCE;
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    private PLVFloatingWindowManager() {

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外API">
    /**
     * 构造悬浮窗，多次构造仅生效一次，需要配合{@link #destroy()}方法才会重新构建
     * @param activity
     * @param isSystemWindow 是否是系统级别悬浮窗
     */
    public void buildWindow(Activity activity, boolean isSystemWindow){
        if(windowBuilder == null) {
            windowBuilder = new WindowBuilder(activity)
                    .setWindowType(isSystemWindow)
                    .create();
        }
        windowBuilder.activity = activity;
    }

    /**
     * 设置悬浮窗的 ContentView
     */
    public void setContentView(View view){
        if(windowBuilder != null) {
            windowBuilder.setContentView(view);
            windowBuilder.getFloatLayout().setContentView(view);
        }
    }

    /**
     * 获取通过{@link #setContentView(View)} 设置给悬浮窗的ContentView
     * @return
     */
    public View getContentView(){
        if(windowBuilder != null){
            return windowBuilder.getFloatLayout().getContentView();
        }
        return null;
    }

    /**
     * 悬浮窗是否正在显示
     */
    public boolean isFloatingWindowShowing(){
        if(windowBuilder != null){
            return windowBuilder.getFloatLayout().isShow();
        }
        return false;
    }

    /**
     * 更新悬浮窗的尺寸
     * @param width 宽（px）
     * @param height 高（px）
     */
    public void updateFloatSize(int width, int height){
        if(windowBuilder != null) {
            windowBuilder.getFloatLayout().updateFloatSize(width, height);
        }
    }

    /**
     * 更新悬浮窗的坐标位置
     */
    public void updateFloatLocation(int x, int y){
        if(windowBuilder != null) {
            windowBuilder.getFloatLayout().updateFloatLocation(x, y);
        }
    }

    public Point getFloatLocation(){
        if (windowBuilder != null){
            return windowBuilder.getFloatLayout().getFloatLocation();
        }
        return null;
    }

    /**
     * 显示悬浮窗
     */
    public void show(Activity activity){
        if(windowBuilder != null) {
            windowBuilder.getFloatLayout().show(activity);
        }
    }


    /**
     * 隐藏悬浮窗
     */
    public void hide(){
        if(windowBuilder != null) {
            windowBuilder.getFloatLayout().hide();
        }
    }

    /**
     * 销毁悬浮窗
     */
    public void destroy(){
        if(windowBuilder != null) {
            windowBuilder.getFloatLayout().hide();
            windowBuilder.getFloatLayout().destroy();
            windowBuilder = null;
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="悬浮窗构造器Builder">
    private static class WindowBuilder{
        private Activity activity;
        private PLVAbsFloatingLayout floatLayout;
        private boolean isSystemWindow = false;

        private int width,height;
        private View contentView;

        public WindowBuilder(Activity activity){
            this.activity = activity;
        }

        public WindowBuilder setWindowType(boolean isSystemWindow){
            this.isSystemWindow = isSystemWindow;
            return this;
        }

        public WindowBuilder setContentView(View view){
            this.contentView = view;
            return this;
        }

        public WindowBuilder setSize(int width, int height){
            this.width = width;
            this.height = height;
            return this;
        }

        public IPLVFloatingLayout getFloatLayout(){
            return floatLayout;
        }


        public WindowBuilder create(){
            if(activity != null){
                if(isSystemWindow){
                    floatLayout = new PLVSystemFloatingLayout(activity);

                } else {
                    floatLayout = new PLVAppFloatingLayout(activity);
                }

                ViewGroup.LayoutParams layoutParams = floatLayout.getLayoutParams();
                if(layoutParams == null){
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                layoutParams.height = height;
                layoutParams.width = width;
                floatLayout.updateFloatSize(width, height);
                floatLayout.setLayoutParams(layoutParams);
                if(contentView != null) {
                    floatLayout.setContentView(contentView);
                }
                return this;
            }
            return null;
        }




    }

    // </editor-fold >

}
