package com.easefun.polyv.livestreamer.modules.statusbar;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSLinkMicControlWindow;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSMemberLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSSettingLayout;

/**
 * 状态布局的接口定义、
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVLSStatusBarLayout {
    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法">

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
     * 获取成员列表布局中的streamerView
     *
     * @return streamerView
     */
    IPLVStreamerContract.IStreamerView getMemberLayoutStreamerView();

    /**
     * 显示无网络时的对话框
     */
    void showAlertDialogNoNetwork();

    /**
     * 更新用户请求连麦的状态
     *
     * @param uid 连麦id
     */
    void updateUserRequestStatus(String uid);

    /**
     * 更新推流时间
     *
     * @param secondsSinceStartTiming 推流时间，单位：秒
     */
    void updateStreamerTime(int secondsSinceStartTiming);

    /**
     * 设置推流状态
     *
     * @param isStartedStatus 推流状态
     */
    void setStreamerStatus(boolean isStartedStatus);

    /**
     * 更新网络状态
     *
     * @param networkQuality 网络状态常量
     */
    void updateNetworkQuality(int networkQuality);

    /**
     * 设置在线人数
     *
     * @param onlineCount 在线人数
     */
    void setOnlineCount(int onlineCount);

    /**
     * 是否拦截返回事件
     *
     * @return true：拦截，false：不拦截
     */
    boolean onBackPressed();

    /**
     * 销毁，释放资源
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器">

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener extends PLVLSMemberLayout.OnViewActionListener
            , PLVLSSettingLayout.OnViewActionListener, PLVLSLinkMicControlWindow.OnViewActionListener {
        /**
         * 上下课控制
         *
         * @param isStart true：上课，false：下课
         */
        void onClassControl(boolean isStart);

        /**
         * 获取当前网络质量
         *
         * @return 网络质量常量
         */
        int getCurrentNetworkQuality();
    }
    // </editor-fold>
}
