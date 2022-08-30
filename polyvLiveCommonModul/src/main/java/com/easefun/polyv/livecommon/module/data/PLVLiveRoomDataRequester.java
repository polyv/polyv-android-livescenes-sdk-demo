package com.easefun.polyv.livecommon.module.data;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livescenes.chatroom.PolyvChatApiRequestHelper;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.net.PolyvApiManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.net.PLVResponseBean;
import com.plv.foundationsdk.net.PLVResponseExcutor;
import com.plv.foundationsdk.net.PLVrResponseCallback;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.livescenes.hiclass.api.PLVHCApiManager;
import com.plv.livescenes.hiclass.vo.PLVHCLessonDetailVO;
import com.plv.livescenes.model.PLVIncreasePageViewerVO;
import com.plv.livescenes.model.PLVLiveStatusVO2;
import com.plv.livescenes.model.PLVPlaybackChannelDetailVO;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.livescenes.net.PLVApiManager;
import com.plv.socket.user.PLVSocketUserConstant;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
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
    private Disposable updateChannelNameDisposable;
    private Disposable lessonDetailDisposable;
    private Disposable playbackChannelDetail;
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
        pageViewerDisposable = PLVResponseExcutor.excuteUndefinData(
                PolyvApiManager.getPolyvLiveStatusApi()
                        .increasePageViewer2(PLVFormatUtils.parseInt(channelId), appId, ts, appSecret, times)
                , new PLVrResponseCallback<PLVIncreasePageViewerVO>() {
                    @Override
                    public void onSuccess(PLVIncreasePageViewerVO vo) {
                        if (listener != null) {
                            listener.onSuccess(vo.getData());
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
                    public void onFailure(PLVResponseBean<PLVIncreasePageViewerVO> PLVResponseBean) {
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
                .compose(new PLVRxBaseTransformer<PolyvLiveClassDetailVO, PolyvLiveClassDetailVO>())
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
                .compose(new PLVRxBaseTransformer<PolyvChatFunctionSwitchVO, PolyvChatFunctionSwitchVO>())
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
    void requestProductList(final IPLVNetRequestListener<PLVCommodityVO2> listener) {
        requestProductList(-1, listener);
    }

    void requestProductList(int rank, final IPLVNetRequestListener<PLVCommodityVO2> listener) {
        this.commodityRank = rank;
        disposeProductList();
        String channelId = getConfig().getChannelId();
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        long timestamp = System.currentTimeMillis();
        int count = GET_COMMODITY_COUNT;
        Observable<PLVCommodityVO2> commodityVOObservable;
        if (rank > -1) {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi()
                    .getProductList2(channelId, appId, timestamp, count, rank, appSecret);
        } else {
            commodityVOObservable = PolyvApiManager.getPolyvLiveStatusApi()
                    .getProductList2(channelId, appId, timestamp, count, appSecret);
        }
        productListDisposable = commodityVOObservable.retryWhen(new PLVRxBaseRetryFunction(3, 3000))
                .compose(new PLVRxBaseTransformer<PLVCommodityVO2, PLVCommodityVO2>())
                .subscribe(new Consumer<PLVCommodityVO2>() {
                    @Override
                    public void accept(PLVCommodityVO2 polyvCommodityVO) throws Exception {
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
        String appId = getConfig().getAccount().getAppId();
        String appSecret = getConfig().getAccount().getAppSecret();
        long timestamp = System.currentTimeMillis();
        getLiveStatusDisposable = PLVResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi()
                        .getLiveStatusJson3(channelId, timestamp + "", appId, appSecret)
                , new PLVrResponseCallback<PLVLiveStatusVO2>() {
                    @Override
                    public void onSuccess(PLVLiveStatusVO2 statusVO) {
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
                    public void onFailure(PLVResponseBean<PLVLiveStatusVO2> polyvResponseBean) {
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

    // <editor-fold defaultstate="collapsed" desc="更新频道名称 - 请求、取消">
    void requestUpdateChannelName(final IPLVNetRequestListener<String> listener) {
        disposeUpdateChannelName();
        String channelId = getConfig().getChannelId();
        final String channelName = getConfig().getChannelName();
        long ptime = System.currentTimeMillis();
        String appId = PolyvLiveSDKClient.getInstance().getAppId();
        String appSecret = PolyvLiveSDKClient.getInstance().getAppSecret();
        updateChannelNameDisposable = PolyvApiManager.getPolyvLiveStatusApi().updateChannelSetting(channelId, ptime, appId, channelName, appSecret)
                .compose(new PLVRxBaseTransformer<ResponseBody, ResponseBody>())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        String body = responseBody.string();
                        JSONObject jsonObject = new JSONObject(body);
                        String status = jsonObject.optString("status");
                        if ("success".equals(status)) {
                            if (listener != null) {
                                listener.onSuccess(channelName);
                            }
                        } else {
                            if (listener != null) {
                                String errorMsg = jsonObject.optString("message");
                                listener.onFailed(errorMsg, new Throwable(errorMsg));
                            }
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

    void disposeUpdateChannelName() {
        if (updateChannelNameDisposable != null) {
            updateChannelNameDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取详情课节数据 - 请求、取消">
    void requestLessonDetail(final IPLVNetRequestListener<PLVHiClassDataBean> listener) {
        disposeLessonDetail();
        boolean isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(getConfig().getUser().getViewerType());
        String courseCode = getConfig().getHiClassConfig().getCourseCode();
        long lessonId = getConfig().getHiClassConfig().getLessonId();
        String token = getConfig().getHiClassConfig().getToken();
        lessonDetailDisposable = PLVHCApiManager.getInstance().getLessonDetail(isTeacherType, courseCode, lessonId, token)
                .retryWhen(new PLVRxBaseRetryFunction(3, 3000))
                .compose(new PLVRxBaseTransformer<PLVHCLessonDetailVO, PLVHCLessonDetailVO>())
                .subscribe(new Consumer<PLVHCLessonDetailVO>() {
                    @Override
                    public void accept(PLVHCLessonDetailVO plvhcLessonDetailVO) throws Exception {
                        if (plvhcLessonDetailVO.isSuccess() != null
                                && plvhcLessonDetailVO.isSuccess()
                                && plvhcLessonDetailVO.getData() != null) {
                            if (listener != null) {
                                listener.onSuccess(plvhcLessonDetailVO.getData());
                            }
                        } else {
                            throw new Exception(plvhcLessonDetailVO.getError().getDesc() + "-" + plvhcLessonDetailVO.getError().getCode());
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

    void disposeLessonDetail() {
        if (lessonDetailDisposable != null) {
            lessonDetailDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取回放频道的信息 - 请求、取消">
    void requestPlaybackChannelDetail(final IPLVNetRequestListener<PLVPlaybackChannelDetailVO> listener){
        disposePlayBackChannelDetail();
        String channelId = getConfig().getChannelId();
        long ptime = System.currentTimeMillis();
        playbackChannelDetail = PLVApiManager.getPlvChannelStatusApi().getPlaybackChannelDetail(channelId, String.valueOf(ptime))
                .compose(new PLVRxBaseTransformer<PLVPlaybackChannelDetailVO, PLVPlaybackChannelDetailVO>())
                .subscribe(new Consumer<PLVPlaybackChannelDetailVO>() {
                    @Override
                    public void accept(PLVPlaybackChannelDetailVO detailVO) throws Exception {
                        if (listener != null) {
                            if (detailVO.getData() == null) {
                                String errormsg = detailVO.getMessage();
                                listener.onFailed(errormsg, new Throwable(errormsg));
                            }
                            listener.onSuccess(detailVO);
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

    void disposePlayBackChannelDetail(){
        if(playbackChannelDetail != null){
            playbackChannelDetail.dispose();
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
        disposeUpdateChannelName();
        disposeLessonDetail();
        disposePlayBackChannelDetail();
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
