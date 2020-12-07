package com.easefun.polyv.livecloudclass.modules.ppt;

import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;

/**
 * date: 2020/10/9
 * author: HWilliamgo
 * 云课堂场景下定义的PPT悬浮窗布局
 * 定义了：
 * 1. 外部直接调用的方法
 * 2. 需要外部响应的事件监听器
 */
public interface IPLVLCFloatingPPTLayout {

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">

    /**
     * 设置服务端的PPT开关
     *
     * @param enable true表示打开PPT，false表示关闭PPT
     */
    void setServerEnablePPT(boolean enable);

    /**
     * 显示
     */
    void show();

    /**
     * 隐藏
     */
    void hide();

    /**
     * PPT是否在悬浮窗中
     */
    boolean isPPTInFloatingLayout();

    /**
     * 获取PPTView
     */
    IPLVLCPPTView getPPTView();

    /**
     * 设置悬浮窗点击监听器
     *
     * @param li listener
     */
    void setOnFloatingViewClickListener(View.OnClickListener li);

    /**
     * 设置显示关闭悬浮窗的监听器
     *
     * @param onClickCloseListener listener
     */
    void setOnClickCloseListener(IPLVOnClickCloseFloatingView onClickCloseListener);

    /**
     * 获取切换View
     */
    PLVSwitchViewAnchorLayout getPPTSwitchView();

    /**
     * 销毁
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 需要外部响应的事件监听器">

    interface IPLVOnClickCloseFloatingView {
        /**
         * 点击关闭
         */
        void onClickCloseFloatingView();
    }
    // </editor-fold>

}
