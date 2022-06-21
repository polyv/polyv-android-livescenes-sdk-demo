package com.easefun.polyv.liveecommerce.modules.player.rtc;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;

/**
 * @author suhongtao
 */
public interface IPLVECLiveRtcVideoLayout {

    /**
     * 初始化方法
     *
     * @param liveRoomDataManager
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 通知直播开始
     */
    void setLiveStart();

    /**
     * 通知直播结束
     */
    void setLiveEnd();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 是否正在暂停
     */
    boolean isPausing();

    /**
     * 恢复播放
     */
    void resume();

    void requestLayout();

    /**
     * 设置 RTC 播放器布局回调
     *
     * @param onViewActionListener
     */
    void setOnViewActionListener(OnViewActionListener onViewActionListener);

    /**
     * 销毁方法
     */
    void destroy();

    /**
     * RTC 播放器布局回调
     */
    interface OnViewActionListener {

        /**
         * RTC 播放器准备完成
         */
        void onRtcPrepared();

        /**
         * 播放器布局尺寸变更
         */
        void onSizeChanged(int width, int height);

        /**
         * 更新播放状态回调
         */
        void onUpdatePlayInfo();

        /**
         * 网络质量回调
         *
         * @param quality 网络质量 {@link com.plv.linkmic.PLVLinkMicConstant.NetQuality}
         */
        void acceptNetworkQuality(int quality);

    }

}
