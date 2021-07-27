package com.plv.livecommon.module.modules.interact.app;

import android.text.TextUtils;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.model.web.PLVJSResponseVO;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.web.PLVWebview;
import com.plv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.plv.livescenes.PLVSocketEvent;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.plv.livescenes.feature.interact.PLVInteractAppAbs;
import com.plv.livescenes.model.PLVInteractiveCallbackVO;
import com.plv.livescenes.model.answer.PLVJSQuestionVO;
import com.plv.livescenes.model.answer.PLVQuestionResultJsVO;
import com.plv.livescenes.model.answer.PLVQuestionResultVO;
import com.plv.livescenes.model.answer.PLVQuestionSResult;
import com.plv.livescenes.model.answer.PLVQuestionSocketVO;

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
    private Map<String, PLVJSQuestionVO> questions = new ConcurrentHashMap<>();
    //截止答题的消息
    private String socketMsgStopQuestion = "";
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socket事件">
    @Override
    protected void processSocketMsg(final String msg, String event) {
        switch (event) {
            //讲师发题
            case PLVSocketEvent.GET_TEST_QUESTION_CONTENT:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        PLVQuestionSResult plvQuestionSResult = PLVGsonUtil.fromJson(PLVQuestionSResult.class, msg);
                        if (plvQuestionSResult == null) {
                            return;
                        }
                        curQuestionId = plvQuestionSResult.getQuestionId();
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
            case PLVSocketEvent.STOP_TEST_QUESTION:
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
            case PLVSocketEvent.GET_TEST_QUESTION_RESULT:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        PLVQuestionResultVO socketVO;
                        socketVO = PLVGsonUtil.fromJson(PLVQuestionResultVO.class, msg);
                        if (socketVO == null) {
                            return;
                        }
                        notifyShow();

                        PLVJSQuestionVO questionVO = questions.remove(socketVO.getQuestionId());
                        PLVQuestionResultJsVO plvQuestionResultJsVO;
                        if (questionVO == null) {
                            plvQuestionResultJsVO = new PLVQuestionResultJsVO("", msg);
                        } else {
                            plvQuestionResultJsVO = new PLVQuestionResultJsVO(questionVO.getAnswerId(), msg);
                        }

                        sendMsgToJs(PLVInteractJSBridgeEventConst.ANSWER_SHEET_RESULT, plvQuestionResultJsVO.toString(), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                                PLVCommonLog.d(TAG, "GET_TEST_QUESTION_RESULT " + data);
                            }
                        });
                    }
                });
                break;
            default:
                if (event.contains(PLVSocketEvent.TEST_QUESTION)) {
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
            jsonObject.put("EVENT", PLVSocketEvent.STOP_TEST_QUESTION);
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
                result.setStatus(PLVWebview.STATUS_SUCCESS);
                result.setMessage("成功调用方法：" + PLVInteractJSBridgeEventConst.KNOW_ANSWER_METHOD);
                function.onCallBack(PLVGsonUtil.toJson(result));

                isQuestionAnswer = true;
            }
        });
    }

    //保存选择的选项
    private void saveSelectedOption(String data) {
        PLVJSQuestionVO plvJSQuestionVO = PLVGsonUtil.fromJson(PLVJSQuestionVO.class, data);
        PLVCommonLog.d(TAG, "receive result answer " + data);
        if (plvJSQuestionVO != null && !TextUtils.isEmpty(plvJSQuestionVO.getQuestionId())) {
            sendResultToServer(plvJSQuestionVO);
            PLVCommonLog.d(TAG, "save answer :" + plvJSQuestionVO.getQuestionId());
            questions.put(plvJSQuestionVO.getQuestionId(), plvJSQuestionVO);
        }
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送结果到服务端">
    private void sendResultToServer(PLVJSQuestionVO plvJSQuestionVO) {
        PLVQuestionSocketVO socketVO = new PLVQuestionSocketVO(plvJSQuestionVO.getAnswerId(), viewerName, plvJSQuestionVO.getQuestionId(), channelId, viewerId);
        PLVChatroomManager.getInstance().sendInteractiveSocketMessage(Socket.EVENT_MESSAGE, socketVO, 3, PLVInteractiveCallbackVO.EVENT_ANSWER);
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
