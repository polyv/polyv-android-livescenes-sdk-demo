package com.easefun.polyv.livehiclass.modules.statusbar;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.linkmic.model.PLVNetworkStatusVO;

/**
 * @author suhongtao
 */
public interface IPLVHCStatusBarLayout {

    /**
     * 初始化
     *
     * @param liveRoomDataManager
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 更新为已上课状态
     */
    void onLessonStart();

    /**
     * 更新为已下课状态
     */
    void onLessonEnd();

    /**
     * 更新网络质量
     *
     * @param networkQuality
     */
    void acceptNetworkQuality(int networkQuality);

    /**
     * 上行流量网络状态
     *
     * @param networkStatusVO
     */
    void acceptUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO);

    /**
     * 远端连麦用户网络状态
     *
     * @param networkStatusVO
     */
    void acceptRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO);

    /**
     * 销毁方法
     */
    void destroy();

}
