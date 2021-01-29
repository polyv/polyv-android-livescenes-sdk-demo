package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.support.annotation.NonNull;

import com.easefun.polyv.businesssdk.api.common.meidacontrol.IPolyvMediaController;
import com.easefun.polyv.livecloudclass.modules.liveroom.IPLVLiveLandscapePlayerController;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;

/**
 * 直播播放器控制栏接口，继承于直播播放器所需设置的 IPolyvMediaController 接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLCLiveMediaController extends IPolyvMediaController<PolyvLiveVideoView> {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - 定义 控制栏布局中 外部可以直接调用的方法">

    /**
     * 设置mvp模式中的直播播放器presenter
     *
     * @param livePlayerPresenter 直播播放器presenter
     */
    void setLivePlayerPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter);

    /**
     * 设置横屏控制器
     *
     * @param landscapeController 横屏控制器
     */
    void setLandscapeController(@NonNull IPLVLiveLandscapePlayerController landscapeController);

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
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 设置视频标题
     *
     * @param videoName 视频标题
     */
    void setVideoName(String videoName);

    /**
     * 更新观看热度
     *
     * @param viewerCount 热度数
     */
    void updateViewerCount(long viewerCount);

    /**
     * 当子播放器点击唤起控制栏时，更新布局
     */
    void updateWhenSubVideoViewClick();

    /**
     * 当主播放器准备完成后，更新布局，对应{@link #updateWhenSubVideoViewClick()}的更新
     */
    void updateWhenVideoViewPrepared();

    /**
     * 当加入连麦时，更新布局
     *
     * @param isHideRefreshButton 是否要隐藏刷新按钮，频道支持RTC时，由于使用的是rtc视频流+rtc音频流，非cdn视频流不能刷新，因此需要隐藏
     */
    void updateWhenJoinLinkMic(boolean isHideRefreshButton);

    /**
     * 当离开连麦时，更新布局
     */
    void updateWhenLeaveLinkMic();

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
         * 打开公告
         */
        void onShowBulletinAction();

        /**
         * 发送点赞
         */
        void onSendLikesAction();

        /**
         * 显示
         *
         * @param show true 表示显示，false表示隐藏
         */
        void onShow(boolean show);
    }
    // </editor-fold>
}
