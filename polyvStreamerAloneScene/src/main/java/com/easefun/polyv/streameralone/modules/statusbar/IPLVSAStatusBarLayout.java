package com.easefun.polyv.streameralone.modules.statusbar;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;

/**
 * 状态栏布局API接口
 *
 * @author suhongtao
 */
public interface IPLVSAStatusBarLayout {

    /**
     * 初始化方法
     *
     * @param liveRoomDataManager
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 设置在线人数
     *
     * @param onlineCount
     */
    void setOnlineCount(int onlineCount);

    /**
     * 更新频道名称
     *
     * @param channelName 频道名称
     */
    void updateChannelName(String channelName);

    void setOnViewActionListener(PLVSAStatusBarLayout.OnViewActionListener onViewActionListener);

    void notifyLinkMicTypeChange(boolean isVideoLinkMic, boolean isOpenLinkMic);

    void openMemberLayoutAndHideUserRequestTips();

    void closeMemberLayout();

    void showUserRequestTips();

    void hideUserRequestTips();

    /**
     * 是否拦截返回事件
     */
    boolean onBackPressed();

    /**
     * 销毁方法
     */
    void destroy();

}
