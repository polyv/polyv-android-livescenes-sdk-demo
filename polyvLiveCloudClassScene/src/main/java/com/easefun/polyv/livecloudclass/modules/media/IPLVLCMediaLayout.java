package com.easefun.polyv.livecloudclass.modules.media;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.liveroom.IPLVLiveLandscapePlayerController;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;

/**
 * 云课堂场景下，针对 播放器布局 进行封装的 接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLCMediaLayout {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - common部分，定义 直播播放器布局 和 回放播放器布局 通用的方法">

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 开始播放
     */
    void startPlay();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 恢复播放
     */
    void resume();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 是否在播放中
     *
     * @return true：在播放，false：不在播放
     */
    boolean isPlaying();

    /**
     * 设置系统音量
     *
     * @param volume，音量值，范围[0,100]
     */
    void setVolume(int volume);

    /**
     * 获取系统音量
     *
     * @return 音量值，范围[0,100]
     */
    int getVolume();

    /**
     * 发送弹幕
     *
     * @param message 弹幕信息
     */
    void sendDanmaku(CharSequence message);

    /**
     * 点击关闭悬浮窗
     */
    void updateOnClickCloseFloatingView();

    /**
     * 获取播放器切换View
     *
     * @return 播放器切换View
     */
    PLVSwitchViewAnchorLayout getPlayerSwitchView();

    /**
     * 获取横屏的聊天布局
     *
     * @return 横屏聊天布局
     */
    PLVLCChatLandscapeLayout getChatLandscapeLayout();

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 添加播放器状态的监听器
     *
     * @param listener 监听器
     */
    void addOnPlayerStateListener(IPLVOnDataChangedListener<PLVPlayerState> listener);

    /**
     * 添加PPT是否显示状态的监听器
     *
     * @param listener 监听器
     */
    void addOnPPTShowStateListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 是否拦截返回事件
     *
     * @return true：拦截，false：不拦截。如果当前处于横屏状态，会拦截返回事件，并切换到竖屏。
     */
    boolean onBackPressed();

    /**
     * 销毁，销毁播放器及相关资源
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - live部分，定义 直播播放器布局 独有的方法">

    /**
     * 设置横屏控制器
     *
     * @param landscapeControllerView 横屏控制器
     */
    void setLandscapeControllerView(@NonNull IPLVLiveLandscapePlayerController landscapeControllerView);

    /**
     * 更新观看热度
     *
     * @param viewerCount 热度数
     */
    void updateViewerCount(long viewerCount);

    /**
     * 当加入RTC时，更新布局
     *
     * @param linkMicLayoutLandscapeWidth 连麦布局在横屏的宽度
     */
    void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth);

    /**
     * 当离开RTC时，更新布局
     */
    void updateWhenLeaveRTC();

    void notifyRTCPrepared();

    /**
     * 添加连麦是否开启状态的监听器
     *
     * @param listener 监听器
     */
    void addOnLinkMicStateListener(IPLVOnDataChangedListener<Pair<Boolean, Boolean>> listener);

    /**
     * 添加sei数据监听器
     *
     * @param listener 监听器
     */
    void addOnSeiDataListener(IPLVOnDataChangedListener<Long> listener);

    void setOnRTCPlayEventListener(IPolyvLiveListenerEvent.OnRTCPlayEventListener listener);

    /**
     * 显示横屏RTC布局。
     */
    void setShowLandscapeRTCLayout();

    /**
     * 隐藏横屏RTC布局。
     */
    void setHideLandscapeRTCLayout();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - playback部分，定义 回放播放器布局 独有的方法">

    /**
     * 获取视频总时长
     *
     * @return 视频总时长，单位：毫秒
     */
    int getDuration();

    /**
     * 根据progress占max的百分比，跳转到视频总时间的该百分比进度
     *
     * @param progress 进度
     * @param max      总进度
     */
    void seekTo(int progress, int max);

    /**
     * 设置播放速度
     *
     * @param speed 速度值，建议范围为[0.5,2]
     */
    void setSpeed(float speed);

    /**
     * 获取播放速度
     *
     * @return 速度值
     */
    float getSpeed();

    /**
     * 设置PPTView
     *
     * @param pptView pptView
     */
    void setPPTView(IPolyvPPTView pptView);

    /**
     * 添加播放信息的监听器
     *
     * @param listener 监听器
     */
    void addOnPlayInfoVOListener(IPLVOnDataChangedListener<PLVPlayInfoVO> listener);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义 播放器布局中UI控件 触发的交互事件的回调方法">

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 点击显示或隐藏浮窗/连麦布局（直播和回放共有）
         *
         * @param toShow true：显示，false：隐藏
         */
        void onClickShowOrHideSubTab(boolean toShow);

        /**
         * 显示皮肤（直播独有）
         *
         * @param show true表示播放器皮肤显示，false表示播放器皮肤隐藏
         */
        void onShowMediaController(boolean show);

        /**
         * 横屏发送的消息应同步到聊天室
         *
         * @param message 发送的信息
         * @return <是否发送成功, 结果码>
         */
        Pair<Boolean, Integer> onSendChatMessageAction(String message);

        /**
         * 显示公告动作（直播独有）
         */
        void onShowBulletinAction();

        /**
         * 发送点赞动作
         */
        void onSendLikesAction();
    }
    // </editor-fold>

}

