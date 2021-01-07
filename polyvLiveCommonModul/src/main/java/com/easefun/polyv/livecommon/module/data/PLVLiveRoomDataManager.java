package com.easefun.polyv.livecommon.module.data;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.commodity.saas.PolyvCommodityVO;

/**
 * 直播间数据管理器，实现IPLVLiveRoomDataManager接口。
 * 负责调用IPLVLiveRoomDataRequester获取http数据，存放http数据、配置数据、以及各个业务模块间公用的数据，因此需要每个业务模块间持有同个直播间数据管理器对象。
 */
public class PLVLiveRoomDataManager implements IPLVLiveRoomDataManager {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播频道配置信息
    private PLVLiveChannelConfig liveChannelConfig;

    //直播间数据请求器
    private PLVLiveRoomDataRequester liveRoomDataRequester;

    //观看热度是否已经请求成功
    private boolean isRequestedPageViewer;

    //观看热度数据
    private MutableLiveData<PLVStatefulData<Integer>> pageViewerData = new MutableLiveData<>();
    //直播详情数据
    private MutableLiveData<PLVStatefulData<PolyvLiveClassDetailVO>> classDetailVO = new MutableLiveData<>();
    //功能开关数据
    private MutableLiveData<PLVStatefulData<PolyvChatFunctionSwitchVO>> functionSwitchVO = new MutableLiveData<>();
    //商品数据
    private MutableLiveData<PLVStatefulData<PolyvCommodityVO>> commodityVO = new MutableLiveData<>();
    //直播状态
    private MutableLiveData<PLVStatefulData<LiveStatus>> liveStatusData = new MutableLiveData<>();
    //直播场次Id
    private String sessionId;
    //是否支持RTC(不同推流客户端对RTC的支持不一样，不支持RTC时无法获取到讲师RTC的流，因此不支持RTC连麦时使用CDN流来显示)
    private boolean isSupportRTC;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLiveRoomDataManager(@NonNull PLVLiveChannelConfig config) {
        this.liveChannelConfig = config;
        liveRoomDataRequester = new PLVLiveRoomDataRequester(config);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1、获取config">
    @NonNull
    @Override
    public PLVLiveChannelConfig getConfig() {
        return liveChannelConfig;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 2、直播间数据获取、设置">
    @Override
    public MutableLiveData<PLVStatefulData<Integer>> getPageViewerData() {
        return pageViewerData;
    }

    @Override
    public MutableLiveData<PLVStatefulData<PolyvLiveClassDetailVO>> getClassDetailVO() {
        return classDetailVO;
    }

    @Override
    public MutableLiveData<PLVStatefulData<PolyvChatFunctionSwitchVO>> getFunctionSwitchVO() {
        return functionSwitchVO;
    }

    @Override
    public MutableLiveData<PLVStatefulData<PolyvCommodityVO>> getCommodityVO() {
        return commodityVO;
    }

    @Override
    public MutableLiveData<PLVStatefulData<LiveStatus>> getLiveStatusData() {
        return liveStatusData;
    }

    @Override
    public int getCommodityRank() {
        return liveRoomDataRequester.getCommodityRank();
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSupportRTC(boolean supportRTC) {
        isSupportRTC = supportRTC;
    }

    @Override
    public boolean isSupportRTC() {
        if (getConfig().isPPTChannelType()) {
            return true;//三分屏频道类型一定支持RTC
        }
        return isSupportRTC;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 3、http接口请求">
    //功能开关 - 请求
    @Override
    public void requestChannelSwitch() {
        liveRoomDataRequester.requestChannelSwitch(new PLVLiveRoomDataRequester.IPLVNetRequestListener<PolyvChatFunctionSwitchVO>() {
            @Override
            public void onSuccess(PolyvChatFunctionSwitchVO polyvChatFunctionSwitchVO) {
                functionSwitchVO.postValue(PLVStatefulData.success(polyvChatFunctionSwitchVO));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                functionSwitchVO.postValue(PLVStatefulData.<PolyvChatFunctionSwitchVO>error(msg, throwable));
            }
        });
    }

    //观看热度 - 请求
    @Override
    public void requestPageViewer() {
        liveRoomDataRequester.requestPageViewer(new PLVLiveRoomDataRequester.IPLVNetRequestListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                isRequestedPageViewer = true;
                pageViewerData.postValue(PLVStatefulData.success(integer));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                pageViewerData.postValue(PLVStatefulData.<Integer>error(msg, throwable));
            }
        });
    }

    //直播详情 - 请求
    @Override
    public void requestChannelDetail() {
        liveRoomDataRequester.requestChannelDetail(new PLVLiveRoomDataRequester.IPLVNetRequestListener<PolyvLiveClassDetailVO>() {
            @Override
            public void onSuccess(PolyvLiveClassDetailVO liveClassDetailVO) {
                if (!isRequestedPageViewer && liveClassDetailVO.getData() != null) {
                    //如果观看热度还没请求成功，则加上自己
                    liveClassDetailVO.getData().setPageView(liveClassDetailVO.getData().getPageView() + 1);
                }
                classDetailVO.postValue(PLVStatefulData.success(liveClassDetailVO));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                classDetailVO.postValue(PLVStatefulData.<PolyvLiveClassDetailVO>error(msg, throwable));
            }
        });
    }

    //商品信息 - 请求
    @Override
    public void requestProductList() {
        requestProductList(-1);
    }

    @Override
    public void requestProductList(int rank) {
        liveRoomDataRequester.requestProductList(rank, new PLVLiveRoomDataRequester.IPLVNetRequestListener<PolyvCommodityVO>() {
            @Override
            public void onSuccess(PolyvCommodityVO polyvCommodityVO) {
                commodityVO.postValue(PLVStatefulData.success(polyvCommodityVO));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                commodityVO.postValue(PLVStatefulData.<PolyvCommodityVO>error(msg, throwable));
            }
        });
    }

    @Override
    public void requestLiveStatus() {
        liveRoomDataRequester.requestLiveStatus(new PLVLiveRoomDataRequester.IPLVNetRequestListener<LiveStatus>() {
            @Override
            public void onSuccess(LiveStatus liveStatus) {
                liveStatusData.postValue(PLVStatefulData.success(liveStatus));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                liveStatusData.postValue(PLVStatefulData.<LiveStatus>error(msg, throwable));
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 4、销毁">
    @Override
    public void destroy() {
        liveRoomDataRequester.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 直播状态">
    public enum LiveStatus {
        LIVE("live"),//正在直播
        STOP("stop"),//直播暂停
        END("end");//直播结束

        private String value;

        LiveStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    // </editor-fold>
}
