package com.easefun.polyv.livecommon.module.modules.interact.app;

import com.easefun.polyv.livescenes.PolyvSocketEvent;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.model.PolyvInteractiveCallbackVO;
import com.easefun.polyv.livescenes.model.signin.PolyvSignIn2JsVO;
import com.easefun.polyv.livescenes.model.signin.PolyvSignIn2SocketVO;
import com.easefun.polyv.livescenes.model.signin.PolyvSignInVO;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
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
    private PolyvSignInVO signInVO;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理socket消息">
    @Override
    protected void processSocketMsg(String msg, String event) {
        switch (event) {
            case PolyvSocketEvent.START_SIGN_IN:
                //开始签到
                signInVO = PLVGsonUtil.fromJson(PolyvSignInVO.class, msg);
                if (signInVO == null) {
                    return;
                }
                notifyShow();
                PolyvSignIn2JsVO signIn2JsVO = new PolyvSignIn2JsVO(signInVO.getData().getLimitTime(), signInVO.getData().getMessage());
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
            case PolyvSocketEvent.STOP_SIGN_IN:
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
                PolyvSignIn2SocketVO socketVO = new PolyvSignIn2SocketVO();
                if (signInVO == null) {
                    LogUtils.eTag(TAG, "signInVO=null");
                    return;
                }
                socketVO.setCheckinId(signInVO.getData
                        ().getCheckinId());
                socketVO.setRoomId(signInVO.getRoomId());
                socketVO.setUser(new PolyvSignIn2SocketVO.UserBean(viewerName, viewerId));
                sendResultToServer(socketVO);
            }
        });
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送结果到server">

    private void sendResultToServer(PolyvSignIn2SocketVO socketVO) {
        PolyvChatroomManager.getInstance().sendInteractiveSocketMessage(Socket.EVENT_MESSAGE, socketVO, 3, PolyvInteractiveCallbackVO.EVENT_SIGN);
    }
// </editor-fold>
}
