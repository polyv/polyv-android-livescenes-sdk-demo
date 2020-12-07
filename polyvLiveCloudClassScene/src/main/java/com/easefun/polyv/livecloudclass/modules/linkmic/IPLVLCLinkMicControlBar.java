package com.easefun.polyv.livecloudclass.modules.linkmic;

/**
 * date: 2020/8/18
 * author: HWilliamgo
 * <p>
 * 云课堂场景下，针对 连麦控制条布局 进行封装的 Interface ，在[PLVLinkMicLayout]中被使用，用于将最新的连麦状态更新到控制条。
 * 定义了：
 * 1. 外部直接调用的方法
 * 2. 需要外部响应的事件监听器
 */
public interface IPLVLCLinkMicControlBar {

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">

    /**
     * 设置监听器
     *
     * @param onPLCLinkMicControlBarListener listener
     */
    void setOnPLCLinkMicControlBarListener(OnPLCLinkMicControlBarListener onPLCLinkMicControlBarListener);

    /**
     * 设置讲师开启或关闭连麦
     *
     * @param isTeacherOpenLinkMic true表示讲师开启连麦
     */
    void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic);

    /**
     * 设置开关摄像头
     *
     * @param toOpen true表示要打开摄像头
     */
    void setCameraOpenOrClose(boolean toOpen);

    /**
     * 开关麦克风
     *
     * @param toOpen true表示要打开麦克风
     */
    void setMicrophoneOpenOrClose(boolean toOpen);


    /**
     * 设置上麦成功
     */
    void setJoinLinkMicSuccess();

    /**
     * 设置离开连麦
     */
    void setLeaveLinkMic();


    /**
     * 设置是音频连麦还是视频连麦
     *
     * @param isAudio true表示是音频连麦，false表示是视频连麦
     */
    void setIsAudio(boolean isAudio);

    /**
     * 显示
     */
    void show();

    /**
     * 隐藏
     */
    void hide();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2. 需要外部响应的事件监听器">
    interface OnPLCLinkMicControlBarListener {
        /**
         * 点击请求上麦
         */
        void onClickRingUpLinkMic();

        /**
         * 点击下麦
         */
        void onClickRingOffLinkMic();

        /**
         * 点击开关摄像头
         *
         * @param toClose true表示关闭。false表示开启
         */
        void onClickCameraOpenOrClose(boolean toClose);

        /**
         * 点击前后置摄像头
         *
         * @param toFront true表示前置，false表示后置
         */
        void onClickCameraFrontOfBack(boolean toFront);

        /**
         * 点击开关麦克风
         *
         * @param toClose true表示关闭。false表示开启
         */
        void onClickMicroPhoneOpenOrClose(boolean toClose);

    }
    // </editor-fold>

}
