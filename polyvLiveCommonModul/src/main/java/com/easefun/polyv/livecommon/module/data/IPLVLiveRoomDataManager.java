package com.easefun.polyv.livecommon.module.data;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.commodity.saas.PolyvCommodityVO;

/**
 * 直播间数据管理器的接口
 * 定义了：
 * 1、获取config
 * 2、本地数据获取、设置
 * 3、http接口请求
 * 4、销毁
 */
public interface IPLVLiveRoomDataManager {

    // <editor-fold defaultstate="collapsed" desc="1、获取config">

    /**
     * 获取直播频道参数信息
     */
    @NonNull
    PLVLiveChannelConfig getConfig();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、本地数据获取、设置">

    /**
     * 获取观看热度数据liveData
     */
    LiveData<PLVStatefulData<Integer>> getPageViewerData();

    /**
     * 获取直播详情数据LiveData
     */
    LiveData<PLVStatefulData<PolyvLiveClassDetailVO>> getClassDetailVO();

    /**
     * 获取功能开关数据LiveData
     */
    LiveData<PLVStatefulData<PolyvChatFunctionSwitchVO>> getFunctionSwitchVO();

    /**
     * 获取直播商品数据LiveData
     */
    LiveData<PLVStatefulData<PolyvCommodityVO>> getCommodityVO();

    /**
     * 获取直播状态LiveData
     */
    LiveData<PLVStatefulData<PLVLiveRoomDataManager.LiveStatus>> getLiveStatusData();

    /**
     * 获取请求商品接口的rank
     */
    int getCommodityRank();

    /**
     * 设置sessionId
     */
    void setSessionId(String sessionId);

    /**
     * 获取sessionId
     */
    String getSessionId();

    /**
     * 设置是否支持RTC(不同推流客户端对RTC的支持不一样，不支持RTC时无法获取到讲师RTC的流，因此不支持RTC连麦时使用CDN流来显示)
     */
    void setSupportRTC(boolean isSupportRTC);

    /**
     * 获取是否支持RTC(不同推流客户端对RTC的支持不一样，不支持RTC时无法获取到讲师RTC的流，因此不支持RTC连麦时使用CDN流来显示)
     */
    boolean isSupportRTC();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="3、http接口请求">

    /**
     * 上报观看热度
     */
    void requestPageViewer();

    /**
     * 获取直播详情数据
     */
    void requestChannelDetail();

    /**
     * 获取功能开关数据
     */
    void requestChannelSwitch();

    /**
     * 获取商品信息
     */
    void requestProductList();

    /**
     * 获取直播状态
     */
    void requestLiveStatus();

    /**
     * 获取商品信息
     *
     * @param rank 不传排序号会返回列表最前面的数据，传rank后返回rank之后的商品列表。传-1时和不传的结果一致。
     */
    void requestProductList(int rank);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="4、销毁">

    /**
     * 销毁，取消所有的接口请求
     */
    void destroy();
    // </editor-fold>
}
