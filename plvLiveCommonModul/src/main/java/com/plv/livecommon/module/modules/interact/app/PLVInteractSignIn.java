package com.plv.livecommon.module.modules.interact.app;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.plv.livescenes.PLVSocketEvent;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.plv.livescenes.feature.interact.PLVInteractAppAbs;
import com.plv.livescenes.model.PLVInteractiveCallbackVO;
import com.plv.livescenes.model.signin.PLVSignIn2JsVO;
import com.plv.livescenes.model.signin.PLVSignIn2SocketVO;
import com.plv.livescenes.model.signin.PLVSignInVO;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;

import io.socket.client.Socket;

/**
 * date: 2020/9/2
 * author: HWilliamgo
 * description: 互动签到
 */
public class PLVInteractSignIn extends PLVInteractAppAbs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractSignIn.class.getSimpleName();

    //签到Id
    private PLVSignInVO signInVO;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理socket消息">
    @Override
    protected void processSocketMsg(String msg, String event) {
        switch (event) {
            case PLVSocketEvent.START_SIGN_IN:
                //开始签到
                signInVO = PLVGsonUtil.fromJson(PLVSignInVO.class, msg);
                if (signInVO == null) {
                    return;
                }
                notifyShow();
                PLVSignIn2JsVO signIn2JsVO = new PLVSignIn2JsVO(signInVO.getData().getLimitTime(), signInVO.getData().getMessage());
                String signJson;
                Gson gson = new Gson();
                signJson = gson.toJson(signIn2JsVO);
                sendMsgToJs(PLVInteractJSBridgeEventConst.SIGN_START, signJson, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "SIGN_START " + data);
                    }
                });
                break;
            case PLVSocketEvent.STOP_SIGN_IN:
                //停止签到
                sendMsgToJs(PLVInteractJSBridgeEventConst.SIGN_STOP, null, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "SIGN_STOP " + data);
                    }
                });
                break;
            default:
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理JS消息">
    @Override
    protected void registerMsgReceiverFromJs(IPLVInteractJSBridge interactJSBridge) {
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.SIGN_SUBMIT, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "SIGN_SUBMIT " + data);
                PLVSignIn2SocketVO socketVO = new PLVSignIn2SocketVO();
                if (signInVO == null) {
                    LogUtils.eTag(TAG, "signInVO=null");
                    return;
                }
                socketVO.setCheckinId(signInVO.getData
                        ().getCheckinId());
                socketVO.setRoomId(signInVO.getRoomId());
                socketVO.setUser(new PLVSignIn2SocketVO.UserBean(viewerName, viewerId));
                sendResultToServer(socketVO);
            }
        });
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送结果到server">

    private void sendResultToServer(PLVSignIn2SocketVO socketVO) {
        PLVChatroomManager.getInstance().sendInteractiveSocketMessage(Socket.EVENT_MESSAGE, socketVO, 3, PLVInteractiveCallbackVO.EVENT_SIGN);
    }
// </editor-fold>
}
