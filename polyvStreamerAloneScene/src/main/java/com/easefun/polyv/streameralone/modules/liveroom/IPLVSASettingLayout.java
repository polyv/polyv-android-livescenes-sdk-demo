package com.easefun.polyv.streameralone.modules.liveroom;

import android.util.Pair;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;

/**
 * 设置页布局的接口定义
 */
public interface IPLVSASettingLayout {

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * 设置前置摄像头状态
     *
     * @param isFront true：前置，false：后置
     */
    void setFrontCameraStatus(boolean isFront);

    /**
     * 设置镜像模式
     *
     * @param isMirrorMode true：开启，false：关闭
     */
    void setMirrorModeStatus(boolean isMirrorMode);

    /**
     * 显示无网络时的对话框
     */
    void showAlertDialogNoNetwork();

    /**
     * 是否显示状态
     */
    boolean isShown();

    /**
     * 是否需要拦截返回事件
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 开始直播
     */
    void liveStart();

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 开始直播
         */
        void onStartLiveAction();

        /**
         * 进入直播间
         */
        void onEnterLiveAction();

        /**
         * 获取当前网络质量
         *
         * @return 网络质量常量
         */
        int getCurrentNetworkQuality();

        /**
         * 设置相机方向
         *
         * @param front true：前置，false：后置
         */
        void setCameraDirection(boolean front);

        /**
         * 设置前置摄像头预览的镜像模式
         *
         * @param isMirror true：开启镜像，false：关闭镜像
         */
        void setMirrorMode(boolean isMirror);

        /**
         * 获取推流的码率信息
         *
         * @return 码率信息<最大支持码率, 选择码率>
         */
        Pair<Integer, Integer> getBitrateInfo();

        /**
         * 切换码率
         *
         * @param bitrate 码率
         */
        void onBitrateClick(int bitrate);

        /**
         * 获取推流Presenter
         *
         * @return
         */
        IPLVStreamerContract.IStreamerPresenter getStreamerPresenter();
    }
}
