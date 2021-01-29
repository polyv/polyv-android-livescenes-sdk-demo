package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.view.View;

import com.easefun.polyv.businesssdk.api.common.meidacontrol.IPolyvMediaController;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;

/**
 * 回放播放器控制栏接口，继承于回放播放器所需设置的 IPolyvMediaController 接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLCPlaybackMediaController extends IPolyvMediaController<PolyvPlaybackVideoView> {
    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - 定义 控制栏布局中 外部可以直接调用的方法">

    /**
     * 设置mvp模式中的回放播放器presenter
     *
     * @param playerPresenter 回放播放器presenter
     */
    void setPlaybackPlayerPresenter(IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playerPresenter);

    /**
     * 获取横屏弹幕的开关按钮
     *
     * @return 横屏弹幕的开关按钮
     */
    View getLandscapeDanmuSwitchView();

    /**
     * 设置点赞是否开启/关闭
     *
     * @param isSwitchEnabled
     */
    void setOnLikesSwitchEnabled(boolean isSwitchEnabled);

    /**
     * 设置服务端的PPT开关
     *
     * @param enable true表示打开PPT，false表示关闭PPT
     */
    void setServerEnablePPT(boolean enable);

    /**
     * 设置view交互事件监听器
     *
     * @param onViewActionListener 监听器
     */
    void setOnViewActionListener(OnViewActionListener onViewActionListener);

    /**
     * 播放或暂停
     */
    void playOrPause();

    /**
     * 是否拦截返回事件，当倍速布局显示时会拦截并隐藏倍数布局
     */
    boolean onBackPressed();

    /**
     * 点击关闭悬浮窗
     */
    void updateOnClickCloseFloatingView();

    /**
     * 释放
     */
    void clean();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义 控制栏布局中UI控件 触发的交互事件的回调方法">

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 打开发送信息输入框动作
         */
        void onStartSendMessageAction();

        /**
         * 点击显示或隐藏sub布局。如PPT或连麦
         *
         * @param toShow true：显示，false：隐藏
         */
        void onClickShowOrHideSubTab(boolean toShow);

        /**
         * 发送点赞
         */
        void onSendLikesAction();
    }
    // </editor-fold>
}
