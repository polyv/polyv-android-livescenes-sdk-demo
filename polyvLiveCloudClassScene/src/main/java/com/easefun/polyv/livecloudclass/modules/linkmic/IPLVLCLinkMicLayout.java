package com.easefun.polyv.livecloudclass.modules.linkmic;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;

/**
 * date: 2020/10/9
 * author: HWilliamgo
 * 云课堂场景下，针对 连麦布局 进行封装的 Interface。
 * 定义了：
 * 1. 云课堂使用的连麦布局接口
 * 2. 需要外部响应的事件监听器
 */
public interface IPLVLCLinkMicLayout {

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">

    /**
     * 初始化，设置数据
     *
     * @param liveRoomDataManager 频道数据
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager, IPLVLCLinkMicControlBar linkMicControlBar);

    /**
     * 销毁
     */
    void destroy();

    /**
     * 显示
     */
    void showAll();

    /**
     * 隐藏
     */
    void hideAll();

    /**
     * 隐藏连麦列表
     */
    void hideLinkMicList();

    /**
     * 显示连麦列表
     */
    void showLinkMicList();

    /**
     * 隐藏连麦控制条
     */
    void hideControlBar();

    /**
     * 显示连麦控制条
     */
    void showControlBar();

    /**
     * 设置是否讲师打开连麦
     *
     * @param isTeacherOpenLinkMic 讲师是否打开连麦
     */
    void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic);

    /**
     * 设置连麦类型
     *
     * @param isAudioLinkMic true表示是音频连麦，false表示是视频连麦
     */
    void setIsAudio(boolean isAudioLinkMic);


    /**
     * 是否已经加入RTC频道
     *
     * @return true表示已经加入了RTC频道，false表示没有加入
     */
    boolean isJoinChannel();

    /**
     * Media是否被切换到连麦列表了
     *
     * @return true表示media在连麦列表，false表示media不在连麦列表
     */
    boolean isMediaShowInLinkMicList();

    /**
     * 设置连麦布局监听器
     *
     * @param onPLVLinkMicLayoutListener 连麦布局监听器
     */
    void setOnPLVLinkMicLayoutListener(OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener);

    /**
     * 获取横屏时连麦布局的宽度
     *
     * @return 横屏宽
     */
    int getLandscapeWidth();

    /**
     * 设置直播开始
     */
    void setLiveStart();

    /**
     * 设置直播结束
     */
    void setLiveEnd();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 需要外部响应的事件监听器">

    /**
     * 连麦布局监听器
     */
    interface OnPLVLinkMicLayoutListener {
        //todo 未来将支持画笔事件

        // <editor-fold defaultstate="collapsed" desc="画笔事件监听">
        /***
         /// 暂时保留，以后需要时再使用
        //        //点击擦除PPT画笔
        //        void onUpdateEraseStatus(boolean toErase);
        //
        //        //更新画笔颜色
        //        void onUpdateBrushColor(String color);
        //
        //        //更新画笔状态
        //        boolean onUpdateBrushStatus(boolean unSelected);
        //        // </editor-fold>
         **/

        // <editor-fold defaultstate="collapsed" desc="连麦事件监听">

        /**
         * 加入连麦成功
         */
        void onJoinChannelSuccess();

        /**
         * 离开连麦
         */
        void onLeaveChannel();

        /**
         * 是否要显示RTC布局
         */
        void onShowLandscapeRTCLayout(boolean show);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="点击事件监听器">

        /**
         * 切换连麦连麦中讲师的位置，因为纯视频的频道连麦时，讲师默认在主屏
         *
         * @param viewSwitcher switchView的切换器
         * @param switchView   讲师连麦Item的switch view
         */
        void onChangeTeacherLocation(PLVViewSwitcher viewSwitcher, PLVSwitchViewAnchorLayout switchView);

        /**
         * 点击连麦Item与media切换，只切换一次。
         * 当点击连麦列表item时，media在主屏幕，或刚好点击的item就是media，那么此时只发生一次切换，即media和连麦Item切换。
         *
         * @param switchView 连麦Item的switch view
         */
        void onClickSwitchWithMediaOnce(PLVSwitchViewAnchorLayout switchView);

        /**
         * 点击连麦Item与media切换，切换两次。
         * 当点击连麦列表item时，media在连麦列表中的其他item，那么要发生两次切换：
         * 1. 先将media和此时主屏幕的那个连麦item进行切换，回到初始默认的位置。
         * 2. 再将此次点击的连麦item与主屏幕的media进行切换。
         *
         * @param switchViewHasMedia     连麦列表的上有media的那个switch view
         * @param switchViewGoMainScreen 被点击的item的switch view，对应将会被切换到主屏幕的连麦item
         */
        void onClickSwitchWithMediaTwice(PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="RTC播放事件监听器">
        void onRTCPrepared();
        // </editor-fold>

    }
// </editor-fold>
}
