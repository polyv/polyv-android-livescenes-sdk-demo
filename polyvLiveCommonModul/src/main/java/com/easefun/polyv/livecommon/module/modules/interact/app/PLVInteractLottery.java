package com.easefun.polyv.livecommon.module.modules.interact.app;

import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livescenes.PolyvSocketEvent;
import com.easefun.polyv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.model.PolyvInteractiveCallbackVO;
import com.easefun.polyv.livescenes.model.lottery.PolyvLotteryEndVO;
import com.easefun.polyv.livescenes.net.PolyvApiManager;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.net.PLVResponseBean;
import com.plv.foundationsdk.net.PLVResponseExcutor;
import com.plv.foundationsdk.net.PLVrResponseCallback;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * date: 2020/9/2
 * author: HWilliamgo
 * description: 互动抽奖
 */
public class PLVInteractLottery extends PLVInteractAppAbs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractLottery.class.getSimpleName();

    private String winnerCode;
    private String lotterySessionId;
    private String lotteryId;

    //中奖信息是否显示
    private boolean isWinLotteryShow = false;

    private IPLVInteractSendServerResultToJs commonControl;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVInteractLottery(IPLVInteractSendServerResultToJs commonControl) {
        this.commonControl = commonControl;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public boolean onBackPress() {
        //在中奖时关闭WebView，则发送事件到JS，并消费掉这次的BackPress事件
        if (isWinLotteryShow) {
            sendMsgToJs(PLVInteractJSBridgeEventConst.LOTTERY_CLOSE_WINNER, null, new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    PLVCommonLog.d(TAG, "LOTTERY_CLOSE_WINNER " + data);
                }
            });
            return true;
        } else {
            return false;
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socekt事件">
    @Override
    protected void processSocketMsg(String msg, String event) {
        switch (event) {
            //开始抽奖
            case PolyvSocketEvent.LOTTERY_START:
                //当前频道正在抽奖
            case PolyvSocketEvent.ON_LOTTERY:
                notifyShow();
                sendMsgToJs(PLVInteractJSBridgeEventConst.LOTTERY_START, null, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "LOTTERY_START " + data);
                    }
                });
                break;
            //停止抽奖
            case PolyvSocketEvent.LOTTERY_END:
                final PolyvLotteryEndVO vo = PLVGsonUtil.fromJson(PolyvLotteryEndVO.class, msg);
                if (vo == null) {
                    return;
                }
                notifyShow();
                //设置winnerCode
                if (!vo.getData().isEmpty()) {
                    winnerCode = vo.getData().get(0).getWinnerCode();
                }
                lotterySessionId = vo.getSessionId();
                lotteryId = vo.getLotteryId();
                sendMsgToJs(PLVInteractJSBridgeEventConst.LOTTERY_STOP, msg, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        if (!vo.getData().isEmpty()) {
                            isWinLotteryShow = true;
                        }
                    }
                });
                break;
            default:
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收JS事件">
    @Override
    protected void registerMsgReceiverFromJs(IPLVInteractJSBridge interactJSBridge) {
        //中奖
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.ON_SEND_WIN_DATA, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "ON_SEND_WIN_DATA " + data);

                isWinLotteryShow = false;
                PLVOrientationManager.getInstance().unlockOrientation();

                sendWinLotteryToServer(data);
            }
        });

        //放弃中奖
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.ON_ABANDON_LOTTERY, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "ON_ABANDON_LOTTERY " + data);

                isWinLotteryShow = false;
                PLVOrientationManager.getInstance().unlockOrientation();

                sendAbandonLotteryToServer();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="将结果发送到server">
    private void sendWinLotteryToServer(String data) {
        String receiveInfo = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            receiveInfo = jsonObject.getString("receiveInfo");
        } catch (JSONException e) {
            PLVCommonLog.d(TAG, "sendWinLotteryToServer：" + e.getMessage());
        }

        PLVResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                        .postLotteryWinnerInfoNew(channelId, lotteryId, winnerCode, viewerId, receiveInfo, lotterySessionId),
                String.class, new PLVrResponseCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        LogUtils.d("抽奖信息上传成功" + s);
                        PolyvInteractiveCallbackVO vo = new PolyvInteractiveCallbackVO(PolyvInteractiveCallbackVO.EVENT_LOTTERY, 200);
                        commonControl.sendServerResultToJs(PLVGsonUtil.toJsonSimple(vo));
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        PolyvInteractiveCallbackVO vo = new PolyvInteractiveCallbackVO(PolyvInteractiveCallbackVO.EVENT_LOTTERY, 400);
                        commonControl.sendServerResultToJs(PLVGsonUtil.toJsonSimple(vo));
                        PLVCommonLog.exception(e);
                        if (e instanceof HttpException) {
                            try {
                                ResponseBody errorBody = ((HttpException) e).response().errorBody();
                                if (errorBody != null) {
                                    PLVCommonLog.e(TAG, errorBody.string());
                                }
                            } catch (IOException e1) {
                                PLVCommonLog.d(TAG, "postLotteryWinnerInfoNew: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(PLVResponseBean<String> responseBean) {
                        super.onFailure(responseBean);
                        PLVCommonLog.e(TAG, "抽奖信息上传失败" + responseBean);
                        PolyvInteractiveCallbackVO vo = new PolyvInteractiveCallbackVO(PolyvInteractiveCallbackVO.EVENT_LOTTERY, 400);
                        commonControl.sendServerResultToJs(PLVGsonUtil.toJsonSimple(vo));
                    }

                    @Override
                    public void onFinish() {
                        PLVCommonLog.d(TAG, "postLotteryWinnerInfoNew onFinish");
                    }
                });
    }

    private void sendAbandonLotteryToServer() {
        PLVResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                        .postLotteryAbandon(channelId, viewerId), String.class,
                new PLVrResponseCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        PLVCommonLog.d(TAG, "放弃领奖信息上传成功 " + s);
                    }

                    @Override
                    public void onFailure(PLVResponseBean<String> responseBean) {
                        super.onFailure(responseBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        PLVCommonLog.e(TAG, "放弃领奖信息上传失败");
                        if (e instanceof HttpException) {
                            try {
                                ResponseBody errorBody = ((HttpException) e).response().errorBody();
                                if (errorBody != null) {
                                    LogUtils.e(errorBody.string());
                                }
                            } catch (IOException e1) {
                                PLVCommonLog.d(TAG,"postLotteryAbandon:"+e1.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        PLVCommonLog.d(TAG, "postLotteryAbandon onFinish");
                    }
                });
    }
    // </editor-fold>

}
