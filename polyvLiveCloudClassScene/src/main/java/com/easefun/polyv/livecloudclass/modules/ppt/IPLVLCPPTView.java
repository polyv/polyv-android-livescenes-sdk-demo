package com.easefun.polyv.livecloudclass.modules.ppt;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;

/**
 * date: 2020/10/16
 * author: HWilliamgo
 * 云课堂场景下，针对 PPT布局 进行封装的interface，定义了：
 * 1. 外部直接调用的方法
 * 2. 需要外部响应的事件监听器
 */
public interface IPLVLCPPTView {

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法 - live部分，定义 直播PPT独有的方法">

    /**
     * 初始化直播PPT
     *
     * @param onPLVLCPPTViewListener listener
     */
    void initLivePPT(OnPLVLCLivePPTViewListener onPLVLCPPTViewListener);

    /**
     * 移除延迟时间
     */
    void removeDelayTime();

    /**
     * 恢复延迟时间
     */
    void recoverDelayTime();

    /**
     * 发送sei时间戳数据到PPT
     */
    void sendSEIData(long ts);


// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法 - playback部分，定义回放PPT独有的方法">

    /**
     * 初始化回放PPT
     *
     * @param onPlaybackPPTViewListener listener
     */
    void initPlaybackPPT(OnPLVLCPlaybackPPTViewListener onPlaybackPPTViewListener);

    /**
     * 设置当前回放视频播放的位置
     *
     * @param position 播放位置
     */
    void setPlaybackCurrentPosition(int position);

    /**
     * 获取回放专用的PPTView，用于绑定到播放器内部
     *
     * @return 回放专用的PPTView的接口
     */
    IPolyvPPTView getPlaybackPPTViewToBindInPlayer();
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法 - common部分，定义直播PPT和回放PPT通用的方法">

    /**
     * 发送消息到webView
     *
     * @param event   消息的事件
     * @param message 消息内容
     */
    void sendWebMessage(String event, String message);

    /**
     * 重新加载
     */
    void reLoad();

    /**
     * 销毁
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 需要外部响应的事件监听器 - PPT直播事件监听器">

    /**
     * PPT直播事件监听器
     */
    interface OnPLVLCLivePPTViewListener {

        /**
         * 切换PPT View的位置
         *
         * @param toMainScreen true表示切换到主屏幕，false表示切回到悬浮窗
         */
        void onLiveSwitchPPTViewLocation(boolean toMainScreen);

        /**
         * 直播回调，切换横竖屏
         *
         * @param toLandscape true表示切换到横屏，false表示切换到竖屏
         */
        void onLiveChangeToLandscape(boolean toLandscape);

        /**
         * 直播回调，开始或暂停视频
         *
         * @param toStart true表示开始播放视频，false表示暂停播放视频
         */
        void onLiveStartOrPauseVideoView(boolean toStart);

        /**
         * 直播回调，重播视频
         */
        void onLiveRestartVideoView();

        /**
         * 直播回调，回到上一个Activity
         */
        void onLiveBackTopActivity();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 需要外部响应的事件监听器 - PPT回放事件监听器">

    /**
     * PPT回放事件监听器
     */
    interface OnPLVLCPlaybackPPTViewListener {

        /**
         * 回放回调，切换PPTView的位置
         *
         * @param toMainScreen true表示切换到主屏幕，false表示切回到小窗
         */
        void onPlaybackSwitchPPTViewLocation(boolean toMainScreen);
    }
    // </editor-fold>

}
