package com.easefun.polyv.livehiclass.modules.liveroom;

/**
 * 设备检测页接口
 */
public interface IPLVHCDeviceDetectionLayout {
    /**
     * 接收布局的显示状态
     *
     * @param isShow         是否显示
     * @param enterClassTask 进入上课页任务
     */
    void acceptLayoutVisibility(boolean isShow, Runnable enterClassTask);

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 是否是显示状态
     */
    boolean isShown();

    /**
     * 销毁，释放资源
     */
    void destroy();

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 进入教室
         */
        void onEnterClassAction(boolean isOpenMic,
                                boolean isOpenCamera,
                                boolean isFrontCamera);
    }
}
