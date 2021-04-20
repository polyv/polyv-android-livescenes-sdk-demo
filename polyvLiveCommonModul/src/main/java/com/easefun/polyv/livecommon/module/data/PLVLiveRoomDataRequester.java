package com.easefun.polyv.livecommon.module.data;

import android.support.v4.util.ArrayMap;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livescenes.chatroom.PolyvChatApiRequestHelper;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.PolyvLiveStatusVO;
import com.easefun.polyv.livescenes.model.commodity.saas.PolyvCommodityVO;
import com.easefun.polyv.livescenes.net.PolyvApiManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.net.PLVResponseBean;
import com.plv.foundationsdk.net.PLVResponseExcutor;
import com.plv.foundationsdk.net.PLVrResponseCallback;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.sign.PLVSignCreator;
import com.plv.foundationsdk.utils.PLVFormatUtils;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

/**
 * 直播间数据请求器，主要用于获取直播api相关的数据
 */
public class PLVLiveRoomDataRequester {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLiveRoomDataRequeste";
    //每次请求商品的数量
    public static final int GET_COMMODITY_COUNT = 20;

    //请求商品的rank
    private int commodityRank = -1;

    //直播频道配置参数
    private PLVLiveChannelConfig liveChannelConfig;

    //接口请求disposable
    private Disposable pageViewerDisposable;
    private Disposable channelDetailDisposable;
    private Disposable productListDisposable;
    private Disposable channelSwitchDisposable;
    private Disposable getLiveStatusDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="公共静态方法">
    public static String getErrorMessage(Throwable t) {
        String errorMessage = t.getMessage();
        if (t instanceof HttpException) {
            try {
                errorMessage = ((HttpException) t).response().errorBody().string();
            } catch (Exception e) {
                PLVCommonLog.d(TAG, "getErrorMessage："+e.getMessage());
            }
        }
        return errorMessage;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLiveRoomDataRequester(PLVLiveChannelConfig config) {
        this.liveChannelConfig = config;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观看热度 - 请求、取消">
    void requestPageViewer(final IPLVNetRequestListener<Integer> listener) {
        disposablePageViewer();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        String channelId = getConfig().getChannelId();
        int times = 1;
        long ts = System.currentTimeMillis();
        Map<String, String> paramMap = new ArrayMap<>();
        paramMap.put("channelId", channelId);
        paramMap.put("appId", appId);
        paramMap.put("timestamp", String.valueOf(ts));
        paramMap.put("times", String.valueOf(times));
        String sign = PLVSignCreator.createSign(appSecret, paramMap);
        pageViewerDisposable = PLVResponseExcutor.excuteDataBean(
                PolyvApiManager.getPolyvLiveStatusApi().increasePageViewer(PLVFormatUtils.parseInt(channelId), appId, ts, sign, times),
                Integer.class, new PLVrResponseCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        if (listener != null) {
                            listener.onSuccess(integer);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }

                    @Override
                    public void onFailure(PLVResponseBean<Integer> PLVResponseBean) {
                        super.onFailure(PLVResponseBean);
                        if (listener != null) {
                            String errorMsg = responseBean.toString();
                            listener.onFailed(errorMsg, new Throwable(errorMsg));
                        }
                    }

                    @Override
                    public void onFinish() {
                        PLVCommonLog.d(TAG,"increasePageViewer onFinish");
                    }
                });
    }

    void disposablePageViewer() {
        if (pageViewerDisposable != null) {
            pageViewerDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播详情 - 请求、取消">
    void requestChannelDetail(final IPLVNetRequestListener<PolyvLiveClassDetailVO> listener) {
        disposeChannelDetail();
        String channelId = getConfig().getChannelId();
        String appSecret = PolyvLiveSDKClient.getInstance().getAppSecret();
        String appId = PolyvLiveSDKClient.getInstance().getAppId();
        channelDetailDisposable = PolyvChatApiRequestHelper.getInstance().requestLiveClassDetailApi(channelId, appId, appSecret)
                .retryWhen(new PLVRxBaseRetryFunction(3, 3000))
                .subscribe(new Consumer<PolyvLiveClassDetailVO>() {
                    @Override
                    public void accept(PolyvLiveClassDetailVO liveClassDetailVO) throws Exception {
                        if (listener != null) {
                            listener.onSuccess(liveClassDetailVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }
                });
    }

    void disposeChannelDetail() {
        if (channelDetailDisposable != null) {
            channelDetailDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="功能开关 - 请求、取消">
    void requestChannelSwitch(final IPLVNetRequestListener<PolyvChatFunctionSwitchVO> listener) {
        disposeChannelSwitch();
        String channelId = getConfig().getChannelId();
        channelSwitchDisposable = PolyvChatApiRequestHelper.getInstance().requestFunctionSwitch(channelId)
                .retryWhen(new PLVRxBaseRetryFunction(3, 3000))
                .subscribe(new Consumer<PolyvChatFunctionSwitchVO>() {
                    @Override
                    public void accept(PolyvChatFunctionSwitchVO polyvChatFunctionSwitchVO) throws Exception {
                        if (listener != null) {
                            listener.onSuccess(polyvChatFunctionSwitchVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }
                });
    }

    void disposeChannelSwitch() {
        if (channelSwitchDisposable != null) {
            channelSwitchDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="商品信息 - 请求、取消">
    void requestProductList(final IPLVNetRequestListener<PolyvCommodityVO> listener) {
        requestProductList(-1, listener);
    }

    void requestProductList(int rank, final IPLVNetRequestListener<PolyvCommodityVO> listener) {
        this.commodityRank = rank;
        disposeProductList();
        String channelId = getConfig().getChannelId();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        long timestamp = System.currentTimeMillis();
        int count = GET_COMMODITY_COUNT;
        Map<String, String> map = new ArrayMap<>();
        map.put("appId", appId);
        map.put("timestamp", timestamp + "");
        map.put("channelId", channelId);
        map.put("count", count + "");
        if (rank > -1) {
            map.put("rank", rank + "");
        }
        String sign = PLVSignCreator.createSign(appSecret, map);
        Observable<PolyvCommodityVO> commodityVOObservable;
        if (rank > -1) {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi().getProductList(channelId, appId, timestamp, count, rank, sign);
        } else {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi().getProductList(channelId, appId, timestamp, count, sign);
        }
        productListDisposable = commodityVOObservable.retryWhen(new PLVRxBaseRetryFunction(3, 3000))
                .compose(new PLVRxBaseTransformer<PolyvCommodityVO, PolyvCommodityVO>())
                .subscribe(new Consumer<PolyvCommodityVO>() {
                    @Override
                    public void accept(PolyvCommodityVO polyvCommodityVO) throws Exception {
                        if (listener != null) {
                            listener.onSuccess(polyvCommodityVO);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }
                });
    }

    void disposeProductList() {
        if (productListDisposable != null) {
            productListDisposable.dispose();
        }
    }

    int getCommodityRank() {
        return commodityRank;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播状态 - 请求、取消">
    void requestLiveStatus(final IPLVNetRequestListener<PLVLiveRoomDataManager.LiveStatus> listener) {
        disposeGetLiveStatus();
        String channelId = getConfig().getChannelId();
        getLiveStatusDisposable = PLVResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().getLiveStatusJson2(channelId),
                new PLVrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {
                        if (statusVO != null && statusVO.getCode() == PLVResponseExcutor.CODE_SUCCESS) {
                            PLVLiveRoomDataManager.LiveStatus liveStatus = null;
                            String var = statusVO.getData().split(",")[0];
                            if (PLVLiveRoomDataManager.LiveStatus.LIVE.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomDataManager.LiveStatus.LIVE;
                            } else if (PLVLiveRoomDataManager.LiveStatus.STOP.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomDataManager.LiveStatus.STOP;
                            } else if (PLVLiveRoomDataManager.LiveStatus.END.getValue().equals(var)) {
                                liveStatus = PLVLiveRoomDataManager.LiveStatus.END;
                            }
                            if (listener != null) {
                                listener.onSuccess(liveStatus);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (listener != null) {
                            listener.onFailed(getErrorMessage(throwable), throwable);
                        }
                    }

                    @Override
                    public void onFailure(PLVResponseBean<PolyvLiveStatusVO> polyvResponseBean) {
                        super.onFailure(polyvResponseBean);
                        if (listener != null) {
                            String errorMsg = responseBean.toString();
                            listener.onFailed(errorMsg, new Throwable(errorMsg));
                        }
                    }

                    @Override
                    public void onFinish() {
                        PLVCommonLog.d(TAG,"getLiveStatusJson2 onFinish");
                    }
                });
    }

    void disposeGetLiveStatus() {
        if (getLiveStatusDisposable != null) {
            getLiveStatusDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">
    void destroy() {
        disposablePageViewer();
        disposeChannelDetail();
        disposeProductList();
        disposeChannelSwitch();
        disposeGetLiveStatus();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private PLVLiveChannelConfig getConfig() {
        return liveChannelConfig;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 网络请求监听器">

    /**
     * 网络请求监听器
     *
     * @param <T>
     */
    interface IPLVNetRequestListener<T> {
        /**
         * 请求成功
         */
        void onSuccess(T t);

        /**
         * 请求失败
         *
         * @param msg       错误消息
         * @param throwable throwable
         */
        void onFailed(String msg, Throwable throwable);
    }
    // </editor-fold>
}
