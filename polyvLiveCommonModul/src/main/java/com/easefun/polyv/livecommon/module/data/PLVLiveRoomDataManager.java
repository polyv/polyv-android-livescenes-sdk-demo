package com.easefun.polyv.livecommon.module.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.livescenes.model.PLVPlaybackChannelDetailVO;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.livescenes.streamer.transfer.PLVStreamerInnerDataTransfer;
import com.plv.socket.event.chat.PLVRewardEvent;

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
    private MutableLiveData<PLVStatefulData<PLVCommodityVO2>> commodityVO = new MutableLiveData<>();
    //直播状态
    private MutableLiveData<PLVStatefulData<LiveStatus>> liveStatusData = new MutableLiveData<>();
    //积分打赏开关状态
    private MutableLiveData<PLVStatefulData<Boolean>> pointRewardEnableData = new MutableLiveData<>();
    //积分打赏事件
    private MutableLiveData<PLVRewardEvent> pointRewardEventData = new MutableLiveData<>();
    //频道名称
    private MutableLiveData<PLVStatefulData<String>> channelNameData = new MutableLiveData<>();
    //互动应用icon状态
    private MutableLiveData<PLVWebviewUpdateAppStatusVO> interactStatusData = new MutableLiveData<>();
    //有状态的课节详情数据
    private MutableLiveData<PLVStatefulData<PLVHiClassDataBean>> fulClassDataBean = new MutableLiveData<>();
    //课节详情数据
    private MutableLiveData<PLVHiClassDataBean> classDataBean = new MutableLiveData<>();
    //仅音频模式
    private MutableLiveData<Boolean> isOnlyAudio = new MutableLiveData<>();
    //回放频道的详细信息
    private MutableLiveData<PLVStatefulData<PLVPlaybackChannelDetailVO>> playbackChannelDetailVO = new MutableLiveData<>();
    //直播场次Id
    private MutableLiveData<String> sessionIdLiveData = new MutableLiveData<>();
    //直播场次Id
    private String sessionId;
    //是否支持RTC(不同推流客户端对RTC的支持不一样，不支持RTC时无法获取到讲师RTC的流，因此不支持RTC连麦时使用CDN流来显示)
    private boolean isSupportRTC;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLiveRoomDataManager(@NonNull PLVLiveChannelConfig config) {
        this.liveChannelConfig = config;
        liveRoomDataRequester = new PLVLiveRoomDataRequester(config);
        isOnlyAudio.setValue(PLVStreamerInnerDataTransfer.getInstance().isOnlyAudio());
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
    public MutableLiveData<PLVStatefulData<PLVCommodityVO2>> getCommodityVO() {
        return commodityVO;
    }

    @Override
    public MutableLiveData<PLVStatefulData<LiveStatus>> getLiveStatusData() {
        return liveStatusData;
    }

    @Override
    public MutableLiveData<PLVStatefulData<Boolean>> getPointRewardEnableData() {
        return pointRewardEnableData;
    }

    @Override
    public MutableLiveData<PLVRewardEvent> getRewardEventData() {
        return pointRewardEventData;
    }

    @Override
    public MutableLiveData<PLVStatefulData<PLVHiClassDataBean>> getFulHiClassDataBean() {
        return fulClassDataBean;
    }

    @Override
    public LiveData<PLVHiClassDataBean> getHiClassDataBean() {
        return classDataBean;
    }

    @Override
    public LiveData<Boolean> getIsOnlyAudioEnabled() {
        return isOnlyAudio;
    }

    @Override
    public LiveData<PLVStatefulData<PLVPlaybackChannelDetailVO>> getPlaybackChannelData() {
        return playbackChannelDetailVO;
    }

    @Override
    public MutableLiveData<String> getSessionIdLiveData() {
        return sessionIdLiveData;
    }

    @Override
    public MutableLiveData<PLVWebviewUpdateAppStatusVO> getInteractStatusData() {
        return interactStatusData;
    }

    @Override
    public int getCommodityRank() {
        return liveRoomDataRequester.getCommodityRank();
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        sessionIdLiveData.postValue(sessionId);
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

    @Override
    public void setOnlyAudio(boolean onlyAudio) {
        isOnlyAudio.postValue(onlyAudio);
    }

    @Override
    public boolean isOnlyAudio() {
        if(isOnlyAudio.getValue() == null){
            return false;
        }
        return isOnlyAudio.getValue();
    }

    @Override
    public void setNeedStreamRecover(boolean isNeed) {
        //不需要恢复直播，重制状态
        liveChannelConfig.setLiveStreamingWhenLogin(isNeed);
        PLVLiveChannelConfigFiller.setLiveStreamingWhenLogin(isNeed);
    }

    @Override
    public boolean isNeedStreamRecover() {
        return liveChannelConfig.isLiveStreamingWhenLogin();
    }

    @Override
    public void setConfigVid(String vid) {
        liveChannelConfig.setupVid(vid);
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
        liveRoomDataRequester.requestProductList(rank, new PLVLiveRoomDataRequester.IPLVNetRequestListener<PLVCommodityVO2>() {
            @Override
            public void onSuccess(PLVCommodityVO2 polyvCommodityVO) {
                commodityVO.postValue(PLVStatefulData.success(polyvCommodityVO));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                commodityVO.postValue(PLVStatefulData.<PLVCommodityVO2>error(msg, throwable));
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

    //更新频道名称 - 请求

    @Override
    public void requestUpdateChannelName() {
        liveRoomDataRequester.requestUpdateChannelName(new PLVLiveRoomDataRequester.IPLVNetRequestListener<String>() {
            @Override
            public void onSuccess(String s) {
                channelNameData.postValue(PLVStatefulData.success(s));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                channelNameData.postValue(PLVStatefulData.<String>error(msg, throwable));
            }
        });
    }

    @Override
    public void requestLessonDetail() {
        liveRoomDataRequester.requestLessonDetail(new PLVLiveRoomDataRequester.IPLVNetRequestListener<PLVHiClassDataBean>() {
            @Override
            public void onSuccess(PLVHiClassDataBean hiClassDataBean) {
                fulClassDataBean.postValue(PLVStatefulData.success(hiClassDataBean));
                classDataBean.postValue(hiClassDataBean);
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                fulClassDataBean.postValue(PLVStatefulData.<PLVHiClassDataBean>error(msg, throwable));
                classDataBean.postValue(null);
            }
        });
    }

    // 获取回放频道的状态信息
    @Override
    public void requestPlaybackChannelStatus() {
        liveRoomDataRequester.requestPlaybackChannelDetail(new PLVLiveRoomDataRequester.IPLVNetRequestListener<PLVPlaybackChannelDetailVO>() {
            @Override
            public void onSuccess(PLVPlaybackChannelDetailVO detailVO) {
                playbackChannelDetailVO.postValue(PLVStatefulData.success(detailVO));
            }

            @Override
            public void onFailed(String msg, Throwable throwable) {
                playbackChannelDetailVO.postValue(PLVStatefulData.<PLVPlaybackChannelDetailVO>error(msg,throwable));
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
