package com.easefun.polyv.livecommon.module.modules.interact.app;

import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.PolyvSocketCallbackListener;
import com.easefun.polyv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.easefun.polyv.livescenes.feature.interact.PLVInteractAppAbs;
import com.easefun.polyv.livescenes.model.PolyvInteractiveCallbackVO;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;

/**
 * date: 2020/9/1
 * author: HWilliamgo
 * description: 互动应用通用事件控制器
 */
public class PLVInteractCommonControl extends PLVInteractAppAbs implements IPLVInteractSendServerResultToJs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractCommonControl.class.getSimpleName();

    private OnInteractCommonControlListener onInteractCommonControlListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVInteractCommonControl() {
        //将提交到server的结果，也发送到JS，让webView将提交结果显示出来
        PolyvChatroomManager.getInstance().setSocketCallbackListener(new PolyvSocketCallbackListener() {
            @Override
            public void socketCallback(PolyvInteractiveCallbackVO callbackVO) {
                String msg = PLVGsonUtil.toJsonSimple(callbackVO);
                PLVCommonLog.d(TAG, "PLVInteractCommonControl.socketCallback\n" + msg);
                sendServerResultToJs(msg);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socket消息">
    @Override
    protected void processSocketMsg(String msg, String event) {/**/}
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setOnInteractCommonControlListener(OnInteractCommonControlListener onInteractCommonControlListener) {
        this.onInteractCommonControlListener = onInteractCommonControlListener;
    }

    @Override
    public void sendServerResultToJs(String msg) {
        sendMsgToJs(PLVInteractJSBridgeEventConst.INTERACTIVE_CALLBACK, msg, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                PLVCommonLog.d(TAG, "INTERACTIVE_CALLBACK " + data);
            }
        });
        notifyShow();
    }


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收JS消息">
    @Override
    protected void registerMsgReceiverFromJs(IPLVInteractJSBridge interactJSBridge) {
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.CLOSE_WEB_VIEW_METHOD, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "CLOSE_WEB_VIEW_METHOD " + data);
                if (onInteractCommonControlListener != null) {
                    onInteractCommonControlListener.onWebViewHide();
                }
            }
        });
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.WEB_VIEW_LOAD_FINISHED, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "WEB_VIEW_LOAD_FINISHED " + data);
                if (onInteractCommonControlListener != null) {
                    onInteractCommonControlListener.onWebViewLoadFinished();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    public interface OnInteractCommonControlListener {
        void onWebViewLoadFinished();

        void onWebViewHide();
    }
// </editor-fold>
}
