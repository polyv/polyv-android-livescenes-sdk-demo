package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.businesssdk.api.common.meidacontrol.IPolyvMediaController;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.plv.livescenes.document.model.PLVPPTStatus;

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
    void setLandscapeController(@NonNull IPLVLCLiveLandscapePlayerController landscapeController);

    /**
     * 获取横屏控制器
     */
    IPLVLCLiveLandscapePlayerController getLandscapeController();

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
     * 设置翻页控件状态
     *
     * @param isShow true表示打开翻页控件，false表示关闭翻页控件
     */
    void setTurnPageLayoutStatus(boolean isShow);

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
    void updateWhenSubVideoViewClick(boolean mainVideoViewPlaying);

    /**
     * 当主播放器准备完成后，更新布局，对应{@link #updateWhenSubVideoViewClick(boolean)}的更新
     */
    void updateWhenVideoViewPrepared();

    /**
     * 加入rtc时更新布局
     *
     * @param isHideRefreshButton 是否要隐藏刷新按钮 {@link #updateWhenJoinLinkMic(boolean)}
     */
    void updateWhenJoinRtc(boolean isHideRefreshButton);

    /**
     * 离开rtc时更新布局
     */
    void updateWhenLeaveRtc();

    void updateWhenRequestJoinLinkMic(boolean isRequestJoinLinkMic);

    void updateWhenLinkMicOpenOrClose(boolean isOpenLinkMic);

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
     * 当仅限音频模式时，更新布局
     */
    void updateWhenOnlyAudio(boolean isOnlyAudio);

    /**
     * 点击关闭悬浮窗
     */
    void updateOnClickCloseFloatingView();

    /**
     * 更新PPT状态信息
     */
    void updatePPTStatusChange(PLVPPTStatus plvpptStatus);

    /**
     * 显示更多布局
     */
    void showMoreLayout();

    /**
     * 弹幕切换按钮点击回调
     *
     * @param v danmuView
     */
    void dispatchDanmuSwitchOnClicked(View v);

    /**
     * 更新无延迟观看模式
     */
    void notifyLowLatencyUpdate(boolean isLowLatency);

    /**
     * 更新聊天室房间状态
     * @param isCloseRoomStatus 是否关闭房间状态
     * @param isFocusModeStatus 是否专注模式状态
     */
    void notifyChatroomStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus);

    /**
     * 释放
     */
    void clean();

    /**
     * 更新积分打赏按钮视图，控制是否显示
     * @param enable
     */
    void updateRewardView(boolean enable);

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
         * 打开打赏弹窗
         */
        void onShowRewardView();

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

        /**
         * ppt翻页
         */
        void onPPTTurnPage(String type);

        /**
         * 切换无延迟观看模式
         */
        void onChangeLowLatencyMode(boolean isLowLatency);

        /**
         * rtc观看切换暂停和恢复播放
         *
         * @param toPause 是否切换到暂停状态
         */
        void onRtcPauseResume(boolean toPause);

        /**
         * rtc观看是否正在暂停
         */
        boolean isRtcPausing();

        /**
         * 小窗点击事件
         */
        void onClickFloating();
    }
    // </editor-fold>
}
