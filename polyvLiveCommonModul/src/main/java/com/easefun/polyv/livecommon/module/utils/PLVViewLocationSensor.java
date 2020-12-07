package com.easefun.polyv.livecommon.module.utils;

import android.content.res.Configuration;
import android.view.View;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * date: 2020/9/23
 * author: HWilliamgo
 * description: 检测View位置的工具类
 * <p>
 * 使用方式：
 * 1. 实例化
 * 2. [setListener]设置监听器
 * 3. 在View的回调中调用[onConfigurationChanged]和[onSizeChanged]。
 */
public class PLVViewLocationSensor {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //Listener
    private OnViewLocationSensorListener li;

    //Flag
    //当前是否在悬浮窗
    private boolean isInFloatingView = false;
    //是否是第一次Layout
    private boolean isFirstTimeLayout = true;

    //View
    private View rootView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVViewLocationSensor(View rootView, OnViewLocationSensorListener li) {
        this.rootView = rootView;
        this.li = li;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 设置监听器
     *
     * @param onViewLocationSensorListener 位置监听器
     */
    public void setListener(OnViewLocationSensorListener onViewLocationSensorListener) {
        this.li = onViewLocationSensorListener;
    }

    /**
     * 屏幕旋转回调，需放在View对象的同名回调中调用。
     *
     * @param newConfig Configuration
     */
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isInFloatingView) {
                if (li != null) {
                    li.onLandscapeSmall();
                }
            } else {
                if (li != null) {
                    li.onLandscapeBig();
                }
            }
        } else {
            if (isInFloatingView) {
                if (li != null) {
                    li.onPortraitSmall();
                }
            } else {
                if (li != null) {
                    li.onPortraitBig();
                }
            }
        }
    }

    /**
     * View尺寸改变，需放在View的同名回调中调用。
     */
    public void onSizeChanged(final int w, int h, int oldw, int oldh) {
        final int portScreenWidth = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        if (isFirstTimeLayout) {
            //第一次layout的时候，需要post，才能设置生效。
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    if (w < portScreenWidth) {
                        //宽度小于竖屏屏幕宽时，说明占位图已经被切换到小窗了，此时要改变布局
                        onSwitchToFloatingView();
                    } else {
                        //恢复原有布局
                        onSwitchToMainScreen();
                    }
                }
            });
            isFirstTimeLayout = false;
        } else {
            if (w < portScreenWidth) {
                //宽度小于竖屏屏幕宽时，说明占位图已经被切换到小窗了，此时要改变布局
                onSwitchToFloatingView();
            } else {
                //恢复原有布局
                onSwitchToMainScreen();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="改变">
    //设置切换到悬浮窗
    private void onSwitchToFloatingView() {
        isInFloatingView = true;
        if (ScreenUtils.isLandscape()) {
            if (li != null) {
                li.onLandscapeSmall();
            }
        } else {
            if (li != null) {
                li.onPortraitSmall();
            }
        }
    }

    //设置切换到主屏幕
    private void onSwitchToMainScreen() {
        isInFloatingView = false;
        if (ScreenUtils.isLandscape()) {
            if (li != null) {
                li.onLandscapeBig();
            }
        } else {
            if (li != null) {
                li.onPortraitBig();
            }
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器定义">
    public interface OnViewLocationSensorListener {

        /**
         * 切换到横屏小窗
         */
        void onLandscapeSmall();

        /**
         * 切换到横屏主屏幕
         */
        void onLandscapeBig();

        /**
         * 切换到竖屏小窗
         */
        void onPortraitSmall();

        /**
         * 切换到竖屏主屏幕
         */
        void onPortraitBig();
    }
    // </editor-fold>
}
