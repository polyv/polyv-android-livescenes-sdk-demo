package com.easefun.polyv.livecommon.module.modules.ppt.presenter;

import android.support.annotation.Nullable;

import com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic;
import com.easefun.polyv.livecommon.module.modules.ppt.contract.IPLVPPTContract;
import com.easefun.polyv.livescenes.log.PolyvELogSender;
import com.easefun.polyv.livescenes.log.ppt.PolyvPPTElog;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvLivePPTProcessor.CHAT_LOGIN;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvLivePPTProcessor.SEND_SOCKET_EVENT;
import static com.easefun.polyv.livescenes.log.ppt.PolyvPPTElog.PPTEvent.PPT_RECEIVE_WEB_MESSAGE;

/**
 * date: 2020/9/16
 * author: HWilliamgo
 * description: PPTView的presenter
 */
public class PLVPPTPresenter implements IPLVPPTContract.IPLVPPTPresenter {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVPPTPresenter.class.getSimpleName();
    private static final int MSG_DELAY_TIME = 5000;


    @Nullable
    private IPLVPPTContract.IPLVPPTView view;

    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
    private PLVSocketMessageObserver.OnMessageListener followTeacherPptVideoLocationListener;

    //Disposable
    private Disposable delaySendLoginEventDisposable;

    private int delayTime = MSG_DELAY_TIME;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - presenter接口实现">
    @Override
    public void init(final IPLVPPTContract.IPLVPPTView view) {
        this.view = view;
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (PLVEventConstant.Ppt.ON_SLICE_START_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_DRAW_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_CONTROL_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_OPEN_EVENT.equals(event) ||
                        PLVEventConstant.Ppt.ON_SLICE_ID_EVENT.equals(event)) {
                    if (view != null) {
                        view.hideLoading();
                    }
                    if (delayTime > 0) {
                        int lastPos = message.lastIndexOf('}');
                        message = message.substring(0, lastPos) + ",\"delayTime\":" + delayTime + "}";
                    }

                    PLVCommonLog.d(TAG, "receive ppt message: delay" + message);

                    if (view != null) {
                        view.sendMsgToWebView(message);
                    }
                } else if (PLVEventConstant.MESSAGE_EVENT_LOGIN.equals(event)) {
                    //发送login事件到ppt
                    final PLVLoginEvent loginEvent = PLVEventHelper.toMessageEventModel(message, PLVLoginEvent.class);
                    if (loginEvent != null &&
                            loginEvent.getUser().getUserId().
                                    equals(PolyvSocketWrapper.getInstance().getLoginVO().getUserId())) {
                        dispose(delaySendLoginEventDisposable);
                        delaySendLoginEventDisposable = PLVRxTimer.delay(1000, new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                if (view != null) {
                                    view.sendMsgToWebView(CHAT_LOGIN, loginEvent.getUser().toString());
                                }
                            }
                        });
                    }
                } else if (PLVEventConstant.Class.SE_SWITCH_PPT_MESSAGE.equals(event)) {
                    //PPT和主屏幕切换位置
                    PolyvPPTAuthentic pptAuthentic = PLVGsonUtil.fromJson(PolyvPPTAuthentic.class, message);
                    if (pptAuthentic == null) {
                        return;
                    }
                    String status = pptAuthentic.getStatus();
                    if (PolyvPPTAuthentic.PermissionStatus.OK.equals(status)) {
                        if (view != null) {
                            view.switchPPTViewLocation(false);
                        }
                    } else {
                        if (view != null) {
                            view.switchPPTViewLocation(true);
                        }
                    }
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener);

        followTeacherPptVideoLocationListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (PLVOnSliceIDEvent.EVENT.equals(event)) {
                    PLVOnSliceIDEvent eventVo = PLVEventHelper.toMessageEventModel(message, PLVOnSliceIDEvent.class);
                    if (eventVo == null) {
                        return;
                    }
                    PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(this);
                    if (!eventVo.isInClass()) {
                        // 非正在直播状态，不同步主副屏
                        return;
                    }
                    // pptAndVideoPosition 0表示讲师端目前ppt在主屏 1表示讲师端目前播放器在主屏
                    if (view != null) {
                        view.switchPPTViewLocation(eventVo.getPptAndVedioPosition() == 0);
                    }
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(followTeacherPptVideoLocationListener);
    }

    @Override
    public void removeMsgDelayTime() {
        delayTime = 0;
    }

    @Override
    public void recoverMsgDelayTime() {
        delayTime = MSG_DELAY_TIME;
    }

    @Override
    public void sendPPTBrushMsg(String message) {
        PolyvELogSender.send(PolyvPPTElog.class, PPT_RECEIVE_WEB_MESSAGE, "event " + SEND_SOCKET_EVENT + "receive web message :" + message);
        //发送画笔事件
        PolyvSocketWrapper.getInstance().emit(PLVEventConstant.MESSAGE_EVENT, message);
    }


    @Override
    public void destroy() {
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(followTeacherPptVideoLocationListener);
        dispose(delaySendLoginEventDisposable);
        view = null;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>
}
