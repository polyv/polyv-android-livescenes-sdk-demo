package com.easefun.polyv.livecommon.module.modules.interact.app;

import com.easefun.polyv.livescenes.PolyvSocketEvent;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.model.PolyvInteractiveCallbackVO;
import com.easefun.polyv.livescenes.model.answer.PolyvQuestionnaireSocketVO;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

/**
 * date: 2020/9/2
 * author: HWilliamgo
 * description: 互动问卷
 */
public class PLVInteractQuestionnaire extends PLVInteractAppAbs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractQuestionnaire.class.getSimpleName();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socket消息">
    @Override
    protected void processSocketMsg(String msg, String event) {
        switch (event) {
            //开始问卷调查
            case PolyvSocketEvent.START_QUESTIONNAIRE:
                notifyShow();
                sendMsgToJs(PLVInteractJSBridgeEventConst.QUESTIONNAIRE_START, msg, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "QUESTIONNAIRE_START " + data);
                    }
                });
                break;
            //停止问卷调查
            case PolyvSocketEvent.STOP_QUESTIONNAIRE:
                sendMsgToJs(PLVInteractJSBridgeEventConst.QUESTIONNAIRE_STOP, msg, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "QUESTIONNAIRE_STOP " + data);
                    }
                });
                break;
            //问卷调查结果
            case PolyvSocketEvent.SEND_QUESTIONNAIRE_RESULT:
                sendMsgToJs(PLVInteractJSBridgeEventConst.QUESTIONNAIRE_RESULT, msg, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "QUESTIONNAIRE_RESULT " + data);
                    }
                });
                break;
            //问卷统计相关数据事件
            case PolyvSocketEvent.QUESTIONNAIRE_ACHIEVEMENT:
                notifyShow();
                sendMsgToJs(PLVInteractJSBridgeEventConst.QUESTIONNAIRE_ACHIEVEMENT, msg, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "QUESTIONNAIRE_ACHIEVEMENT " + data);
                    }
                });
                break;
            default:
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收JS消息">
    @Override
    protected void registerMsgReceiverFromJs(IPLVInteractJSBridge interactJSBridge) {
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.QUESTIONNAIRE_CHOOSE, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "QUESTIONNAIRE_CHOOSE " + data);
                List<PolyvQuestionnaireSocketVO.AnswerBean> answerBeanList = new ArrayList<>();
                String questionnaireId = "";
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray jsonArray = jsonObject.getJSONArray("answers");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject singleAnswer = jsonArray.getJSONObject(i);
                        String questionId = singleAnswer.optString("questionId");
                        String answer = singleAnswer.optString("answer");
                        answerBeanList.add(new PolyvQuestionnaireSocketVO.AnswerBean(questionId, answer));
                    }
                    questionnaireId = jsonObject.optString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PolyvQuestionnaireSocketVO socketVO = new PolyvQuestionnaireSocketVO(questionnaireId, answerBeanList);
                sendResultToServer(socketVO);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送结果到server">
    private void sendResultToServer(PolyvQuestionnaireSocketVO socketVO) {
        PLVCommonLog.d(TAG, "发送调查问卷答案");
        socketVO.setNick(viewerName);
        socketVO.setRoomId(channelId);
        socketVO.setUserId(viewerId);
        PolyvChatroomManager.getInstance().sendInteractiveSocketMessage(Socket.EVENT_MESSAGE, socketVO, 3, PolyvInteractiveCallbackVO.EVENT_QUESTIONNAIRE);
    }
    // </editor-fold>
}
