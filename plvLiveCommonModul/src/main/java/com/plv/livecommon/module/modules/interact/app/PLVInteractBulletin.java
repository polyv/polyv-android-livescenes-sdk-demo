package com.plv.livecommon.module.modules.interact.app;

import android.app.Activity;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.google.gson.Gson;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.livecommon.module.modules.interact.PLVInteractJSBridgeEventConst;
import com.plv.livecommon.module.utils.PLVWebUtils;
import com.plv.livescenes.PLVSocketEvent;
import com.plv.livescenes.feature.interact.IPLVInteractJSBridge;
import com.plv.livescenes.feature.interact.PLVInteractAppAbs;
import com.plv.livescenes.model.bulletin.PLVBulletinVO;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

/**
 * date: 2020/9/2
 * author: HWilliamgo
 * description:
 */
public class PLVInteractBulletin extends PLVInteractAppAbs {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVInteractBulletin.class.getSimpleName();
    private OnPLVInteractBulletinListener onPLVInteractBulletinListener;
    //公告消息
    private PLVBulletinVO bulletinVO = new PLVBulletinVO();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void setOnPLVInteractBulletinListener(OnPLVInteractBulletinListener onPLVInteractBulletinListener) {
        this.onPLVInteractBulletinListener = onPLVInteractBulletinListener;
    }

    public void showBulletin() {
        if (bulletinVO == null) {
            return;
        }
        notifyShow();
        sendMsgToJs(PLVInteractJSBridgeEventConst.BULLETIN_SHOW, new Gson().toJson(bulletinVO), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                PLVCommonLog.d(TAG, "BULLETIN_SHOW " + data);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接收socket消息">
    @Override
    protected void processSocketMsg(String msg, String event) {
        switch (event) {
            //显示公告
            case PLVSocketEvent.BULLETIN_SHOW:
                bulletinVO = PLVGsonUtil.fromJson(PLVBulletinVO.class, msg);
                showBulletin();
                break;
            //移除公告
            case PLVSocketEvent.BULLETIN_REMOVE:
                sendMsgToJs(PLVInteractJSBridgeEventConst.BULLETIN_REMOVE, null, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        PLVCommonLog.d(TAG, "BULLETIN_REMOVE " + data);
                        if (bulletinVO != null) {
                            bulletinVO.setContent("");
                        }
                        if (onPLVInteractBulletinListener != null) {
                            onPLVInteractBulletinListener.onBulletinDelete();
                        }
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
        //公告栏里的链接跳转
        interactJSBridge.registerMsgReceiverFromJs(PLVInteractJSBridgeEventConst.BULLETIN_LINK_CLICK, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                PLVCommonLog.d(TAG, "BULLETIN_LINK_CLICK " + data);
                Activity topActivity = ActivityUtils.getTopActivity();
                if (topActivity != null) {
                    PLVWebUtils.openWebLink(data, topActivity);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">
    public interface OnPLVInteractBulletinListener {
        void onBulletinDelete();
    }
// </editor-fold>

}
