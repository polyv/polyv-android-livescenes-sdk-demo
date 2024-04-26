package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowModeGetter;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;

/**
 * RTC混流观看策略实现
 */
public class PLVRTCMixStreamWatchStrategy implements IPLVRTCInvokeStrategy {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVRTCMixStreamWatchStrategy.class.getSimpleName();

    private boolean isRequireJoinLinkMic = false;
    private boolean isJoinLinkMic;

    /**** core ****/
    private PLVLinkMicPresenter linkMicPresenter;
    private IPLVLinkMicManager linkMicManager;
    private IPLVLiveRoomDataManager liveRoomDataManager;

    private PLVLinkMicEventListener linkMicEventListener;

    /**** Listener ****/
    private OnJoinLinkMicListener onJoinLinkMicListener;
    private OnBeforeJoinChannelListener onBeforeJoinChannelListener;
    private OnLeaveLinkMicListener onLeaveLinkMicListener;
    private final OnRtcWatchListener onRtcWatchListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public PLVRTCMixStreamWatchStrategy(PLVLinkMicPresenter linkMicPresenter,
                                        final IPLVLinkMicManager linkMicManager,
                                        IPLVLiveRoomDataManager ipliveRoomDataManager,
                                        OnJoinLinkMicListener joinLinkMicListener,
                                        @NonNull OnRtcWatchListener onRtcWatchListener) {
        this.linkMicPresenter = linkMicPresenter;
        this.linkMicManager = linkMicManager;
        this.liveRoomDataManager = ipliveRoomDataManager;
        this.onJoinLinkMicListener = joinLinkMicListener;
        this.onRtcWatchListener = onRtcWatchListener;
        setLinkMicEventListener();
    }

    private void setLinkMicEventListener() {
        linkMicPresenter.pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
            @Override
            public void run() {
                linkMicManager.addEventHandler(linkMicEventListener = new PLVLinkMicEventListener() {
                    @Override
                    public void onJoinChannelSuccess(String uid) {
                        if (!isRequireJoinLinkMic) {
                            linkMicManager.switchRoleToAudience();
                            return;
                        }
                        isJoinLinkMic = true;
                        linkMicManager.switchRoleToBroadcaster();
                        PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onJoinChannelSuccess");
                        linkMicManager.sendJoinSuccessMsg(liveRoomDataManager.getSessionId(), new IPLVLinkMicManager.OnSendJoinSuccessMsgListener() {
                            @Override
                            public void onSendJoinSuccessMsg(PLVLinkMicJoinSuccess joinSuccess) {
                                onJoinLinkMicListener.onJoinLinkMic(joinSuccess);
                            }
                        });
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置直播状态">

    /**
     * 直播结束，应发生如下调用：
     * <pre>
     * leave channel
     * <pre/>
     */
    @Override
    public void setLiveEnd() {
        onRtcWatchListener.onStopRtcWatch();
        linkMicManager.leaveChannel();
        linkMicPresenter.leaveChannel();
        isJoinLinkMic = false;
    }

    @Override
    public void setLiveStart() {
        onRtcWatchListener.onStartRtcWatch();
        IPLVLinkMicContract.IPLVLinkMicView linkMicView = linkMicPresenter.getLinkMicView();
        if (linkMicView != null) {
            linkMicView.onRTCPrepared();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置连麦状态">

    /**
     * 加入连麦，应发生如下调用：
     * <pre>
     *  join channel
     *  push stream
     *  onJoin-->
     *      send [joinSuccess]
     *      subscribe all viewer
     *      render list
     * <pre/>
     */
    @Override
    public void setJoinLinkMic() {
        isRequireJoinLinkMic = true;
        onBeforeJoinChannelListener.onBeforeJoinChannel(PLVLinkMicListShowModeGetter.getJoinedMicShowMode(linkMicPresenter.getIsAudioLinkMic()));
        linkMicManager.joinChannel();

    }

    /**
     * 设置离开连麦，应发生如下调用
     * <pre>
     * leave channel
     * send [joinLeave]
     * <pre/>
     */
    @Override
    public void setLeaveLinkMic() {
        isRequireJoinLinkMic = false;
        linkMicManager.leaveChannel();
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        linkMicPresenter.leaveChannel();
        onLeaveLinkMicListener.onLeaveLinkMic();
        isJoinLinkMic = false;
    }

    @Override
    public boolean isJoinChannel() {
        return isJoinLinkMic;
    }

    @Override
    public boolean isJoinLinkMic() {
        return isJoinLinkMic;
    }

    @Override
    public void setFirstScreenLinkMicId(String linkMicId, boolean mute) {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">

    @Override
    public void destroy() {
        setLiveEnd();
        if (linkMicEventListener != null) {
            linkMicManager.removeEventHandler(linkMicEventListener);
            linkMicEventListener = null;
        }

        linkMicPresenter = null;
        linkMicManager = null;
        liveRoomDataManager = null;

        onJoinLinkMicListener = null;
        onLeaveLinkMicListener = null;
        onBeforeJoinChannelListener = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置监听器">
    @Override
    public void setOnBeforeJoinChannelListener(OnBeforeJoinChannelListener li) {
        onBeforeJoinChannelListener = li;
    }

    @Override
    public void setOnLeaveLinkMicListener(OnLeaveLinkMicListener li) {
        onLeaveLinkMicListener = li;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调接口定义">

    public interface OnRtcWatchListener {

        void onStartRtcWatch();

        void onStopRtcWatch();

    }

    // </editor-fold>
}
