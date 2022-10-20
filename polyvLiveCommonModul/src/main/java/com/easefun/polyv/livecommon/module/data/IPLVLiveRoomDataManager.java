package com.easefun.polyv.livecommon.module.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.livescenes.model.PLVPlaybackChannelDetailVO;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.chat.PLVRewardEvent;

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
    LiveData<PLVStatefulData<PLVCommodityVO2>> getCommodityVO();

    /**
     * 获取直播状态LiveData
     */
    LiveData<PLVStatefulData<PLVLiveRoomDataManager.LiveStatus>> getLiveStatusData();

    /**
     * 获取积分打赏开关MutableLiveData
     */
    MutableLiveData<PLVStatefulData<Boolean>> getPointRewardEnableData();

    /**
     * 获取积分打赏事件数据
     */
    MutableLiveData<PLVRewardEvent> getRewardEventData();

    /**
     * 获取互动应用状态
     */
    MutableLiveData<PLVWebviewUpdateAppStatusVO> getInteractStatusData();

    /**
     * 获取有状态的互动学堂课节详情LiveData
     */
    LiveData<PLVStatefulData<PLVHiClassDataBean>> getFulHiClassDataBean();

    /**
     * 获取互动学堂课节详情LiveData
     */
    LiveData<PLVHiClassDataBean> getHiClassDataBean();

    /**
     * 获取仅音频模式开关
     */
    LiveData<Boolean> getIsOnlyAudioEnabled();

    /**
     * 订阅SessionId
     */
    LiveData<String> getSessionIdLiveData();

    /**
     * 获取回放频道的详细信息LiveData
     * @return
     */
    LiveData<PLVStatefulData<PLVPlaybackChannelDetailVO>> getPlaybackChannelData();

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

    /**
     * 设置是否是仅音频模式（限三分屏场景
     */
    void setOnlyAudio(boolean onlyAudio);

    /**
     * 是否是音频开播
     */
    boolean isOnlyAudio();

    /**
     * 设置是否需要恢复直播
     */
    public void setNeedStreamRecover(boolean isNeed);

    /**
     * 是否需要恢复流直播
     */
    boolean isNeedStreamRecover();

    /**
     * 设置config里面的vid
     * @param vid 回放视频的vid
     */
    public void setConfigVid(String vid);
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

    /**
     * 更新频道名称
     */
    void requestUpdateChannelName();

    /**
     * 获取详情课节数据
     */
    void requestLessonDetail();

    /**
     * 请求回放频道的详细信息
     */
    void requestPlaybackChannelStatus();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="4、销毁">

    /**
     * 销毁，取消所有的接口请求
     */
    void destroy();
    // </editor-fold>
}
