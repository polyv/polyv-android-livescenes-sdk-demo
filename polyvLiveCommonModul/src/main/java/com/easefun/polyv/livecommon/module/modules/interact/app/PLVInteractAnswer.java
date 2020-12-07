package com.easefun.polyv.livecommon.module.modules.interact.app;

import android.text.TextUtils;

import com.easefun.polyv.businesssdk.web.PolyvWebview;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.easefun.polyv.livescenes.PolyvSocketEvent;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.model.PolyvInteractiveCallbackVO;
import com.easefun.polyv.livescenes.model.answer.PolyvJSQuestionVO;
import com.easefun.polyv.livescenes.model.answer.PolyvQuestionResultJsVO;
import com.easefun.polyv.livescenes.model.answer.PolyvQuestionResultVO;
import com.easefun.polyv.livescenes.model.answer.PolyvQuestionSResult;
import com.easefun.polyv.livescenes.model.answer.PolyvQuestionSocketVO;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.model.web.PLVJSResponseVO;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.functions.Consumer;
import io.socket.client.Socket;

/**
 * date: 2020/9/1
 * author: HWilliamgo
 * description: 互动答题
 */
public class PLVInteractAnswer extends PLVInteractAppAbs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int DELAY_SOCKET_MSG = 2 * 1000;
    private static final String TAG = PLVInteractAnswer.class.getSimpleName();
    //当前的答题id
    private String curQuestionId;
    //答题卡是否回答了
    private boolean isQuestionAnswer = false;
    //选择的选项
    private Map<String, PolyvJSQuestionVO> questions = new ConcurrentHashMap<>();
    //截止答题的消息
    private String socketMsgStopQuestion = "";
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socket事件">
    @Override
    protected void processSocketMsg(final String msg, String event) {
        switch (event) {
            //讲师发题
            case PolyvSocketEvent.GET_TEST_QUESTION_CONTENT:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        PolyvQuestionSResult polyvQuestionSResult = PLVGsonUtil.fromJson(PolyvQuestionSResult.class, msg);
                        if (polyvQuestionSResult == null) {
                            return;
                        }
                        curQuestionId = polyvQuestionSResult.getQuestionId();
                        isQuestionAnswer = false;
                        notifyShow();
                        saveNewQuestionForStopQuestion(msg);
                        sendMsgToJs(PLVInteractJSBridgeEventConst.ANSWER_SHEET_START, msg, new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                                PLVCommonLog.d(TAG, "GET_TEST_QUESTION_CONTENT " + data);
                            }
                        });
                    }
                });
                break;
            //截止答题
            case PolyvSocketEvent.STOP_TEST_QUESTION:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject stopJson = null;
                        String questionId = "";
                        try {
                            stopJson = new JSONObject(msg);
                            questionId = stopJson.getString("questionId");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        if (!isQuestionAnswer && questionId.equals(curQuestionId)) {
                            notifyShow();
                            sendMsgToJs(PLVInteractJSBridgeEventConst.TEST_QUESTION_METHOD, socketMsgStopQuestion, new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                    PLVCommonLog.d(TAG, "TEST_QUESTION_METHOD " + data);
                                }
                            });
                        }
                    }
                });
                break;
            //讲师发送答题结果
            case PolyvSocketEvent.GET_TEST_QUESTION_RESULT:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        PolyvQuestionResultVO socketVO;
                        socketVO = PLVGsonUtil.fromJson(PolyvQuestionResultVO.class, msg);
                        if (socketVO == null) {
                            return;
                        }
                        notifyShow();

                        PolyvJSQuestionVO questionVO = questions.remove(socketVO.getQuestionId());
                        PolyvQuestionResultJsVO polyvQuestionResultJsVO;
                        if (questionVO == null) {
                            polyvQuestionResultJsVO = new PolyvQuestionResultJsVO("", msg);
                        } else {
                            polyvQuestionResultJsVO = new PolyvQuestionResultJsVO(questionVO.getAnswerId(), msg);
                        }

                        sendMsgToJs(PLVInteractJSBridgeEventConst.ANSWER_SHEET_RESULT, polyvQuestionResultJsVO.toString(), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                                PLVCommonLog.d(TAG, "GET_TEST_QUESTION_RESULT " + data);
                            }
                        });
                    }
                });
                break;
            default:
                if (event.contains(PolyvSocketEvent.TEST_QUESTION)) {
                    sendMsgToJs(PLVInteractJSBridgeEventConst.TEST_QUESTION_METHOD, msg, new CallBackFunction() {
                        @Override
                        public void onCallBack(String data) {
                            PLVCommonLog.d(TAG, "TEST_QUESTION " + data);
                        }
                    });
                }
                break;
        }
    }

    //保存讲师发题的题目信息（为截止答题事件）
    private void saveNewQuestionForStopQuestion(String socketMessage) {
        try {
            JSONObject jsonObject = new JSONObject(socketMessage);
            jsonObject.put("EVENT", PolyvSocketEvent.STOP_TEST_QUESTION);
            socketMsgStopQuestion = jsonObject.toString();
        } catch (JSONException e) {
            PLVCommonLog.e(TAG, "保存问题失败\n" + e);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收JS消息">
    @Override
    protected void registerMsgReceiverFromJs(IPLVInteractJSBridge interactJSBridge) {
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.ANSWER_SHEET_CHOOSE, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                saveSelectedOption(data);

                PLVJSResponseVO<Object> result = new PLVJSResponseVO<>();
                result.setStatus(PolyvWebview.STATUS_SUCCESS);
                result.setMessage("成功调用方法：" + PLVInteractJSBridgeEventConst.KNOW_ANSWER_METHOD);
                function.onCallBack(PLVGsonUtil.toJson(result));

                isQuestionAnswer = true;
            }
        });
    }

    //保存选择的选项
    private void saveSelectedOption(String data) {
        PolyvJSQuestionVO polyvJSQuestionVO = PLVGsonUtil.fromJson(PolyvJSQuestionVO.class, data);
        PLVCommonLog.d(TAG, "receive result answer " + data);
        if (polyvJSQuestionVO != null && !TextUtils.isEmpty(polyvJSQuestionVO.getQuestionId())) {
            sendResultToServer(polyvJSQuestionVO);
            PLVCommonLog.d(TAG, "save answer :" + polyvJSQuestionVO.getQuestionId());
            questions.put(polyvJSQuestionVO.getQuestionId(), polyvJSQuestionVO);
        }
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送结果到服务端">
    private void sendResultToServer(PolyvJSQuestionVO polyvJSQuestionVO) {
        PolyvQuestionSocketVO socketVO = new PolyvQuestionSocketVO(polyvJSQuestionVO.getAnswerId(), viewerName, polyvJSQuestionVO.getQuestionId(), channelId, viewerId);
        PolyvChatroomManager.getInstance().sendInteractiveSocketMessage(Socket.EVENT_MESSAGE, socketVO, 3, PolyvInteractiveCallbackVO.EVENT_ANSWER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="utils">
    private void delay(final Runnable runnable) {
        PLVRxTimer.delay(DELAY_SOCKET_MSG, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (!isDestroyed) {
                    runnable.run();
                }
            }
        });
    }
    // </editor-fold>
}
