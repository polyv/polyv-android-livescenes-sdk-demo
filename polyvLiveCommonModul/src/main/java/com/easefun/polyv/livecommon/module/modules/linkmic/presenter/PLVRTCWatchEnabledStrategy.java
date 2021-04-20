package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import android.app.Activity;
import android.media.AudioManager;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowModeGetter;
import com.easefun.polyv.livescenes.linkmic.IPolyvLinkMicManager;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicEventListener;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

/**
 * date: 2020/12/23
 * author: HWilliamgo
 * description:
 * 支持RTC观看的RTC调用策略实现
 */
public class PLVRTCWatchEnabledStrategy implements IPLVRTCInvokeStrategy {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private boolean isJoinChannel;
    private boolean isJoinLinkMic;
    private String firstScreenLinkMicId = "";

    /**** core ****/
    private PLVLinkMicPresenter linkMicPresenter;
    private IPolyvLinkMicManager linkMicManager;
    private IPLVLiveRoomDataManager liveRoomDataManager;

    /**** Listener ****/
    private OnJoinLinkMicListener onJoinLinkMicListener;
    private OnJoinRTCChannelWatchListener onJoinRTCChannelWatchListener;
    private OnBeforeJoinChannelListener onBeforeJoinChannelListener;
    private OnLeaveLinkMicListener onLeaveLinkMicListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public PLVRTCWatchEnabledStrategy(PLVLinkMicPresenter linkMicPresenter,
                                      final IPolyvLinkMicManager linkMicManager,
                                      IPLVLiveRoomDataManager ipliveRoomDataManager,
                                      OnJoinRTCChannelWatchListener joinRTCChannelWatchListener,
                                      OnJoinLinkMicListener joinLinkMicListener) {
        this.linkMicPresenter = linkMicPresenter;
        this.linkMicManager = linkMicManager;
        this.liveRoomDataManager = ipliveRoomDataManager;
        this.onJoinLinkMicListener = joinLinkMicListener;
        this.onJoinRTCChannelWatchListener = joinRTCChannelWatchListener;
        setLinkMicEventListener();
    }

    private void setLinkMicEventListener() {
        linkMicPresenter.pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
            @Override
            public void run() {
                linkMicManager.addEventHandler(new PolyvLinkMicEventListener() {
                    @Override
                    public void onJoinChannelSuccess(String uid) {
                        isJoinChannel = true;
                        linkMicManager.switchRoleToAudience();
                        onJoinRTCChannelWatchListener.onJoinRTCChannelWatch();
                        IPLVLinkMicContract.IPLVLinkMicView linkMicView = linkMicPresenter.getLinkMicView();
                        if (linkMicView != null) {
                            linkMicView.onRTCPrepared();
                        }
                    }

                    @Override
                    public void onUserJoined(String uid) {
                        if (PolyvLinkMicConfig.getInstance().isPureRtcOnlySubscribeMainScreenVideo()) {
                            if (!TextUtils.isEmpty(firstScreenLinkMicId) && !firstScreenLinkMicId.equals(uid)) {
                                linkMicManager.muteRemoteVideo(uid, true);
                            }
                        }
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置直播状态">

    /**
     * 直播结束，应发生如下调用
     * <pre>
     * leave channel
     * <pre/>
     */
    @Override
    public void setLiveEnd() {
        linkMicManager.leaveChannel();
        linkMicPresenter.leaveChannel();
        isJoinChannel = false;
    }

    /**
     * 直播开始，应发生如下调用：
     * <pre>
     * join channel
     * no push stream
     * onJoin-->
     *     subscribe all viewer
     *     render list
     * <pre/>
     */
    @Override
    public void setLiveStart() {
        Activity topActivity= ActivityUtils.getTopActivity();
        if (topActivity!=null){
            topActivity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
        if (!isJoinChannel) {
            onBeforeJoinChannelListener.onBeforeJoinChannel(PLVLinkMicListShowModeGetter.getLeavedMicShowMode());
            linkMicManager.joinChannel();
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置连麦状态">

    /**
     * 加入连麦，应发生如下调用：
     * <pre>
     * push stream
     * send [joinSuccess]
     * <pre/>
     */
    @Override
    public void setJoinLinkMic() {
        isJoinLinkMic = true;
        linkMicManager.switchRoleToBroadcaster();
        PLVLinkMicJoinSuccess joinSuccess = linkMicManager.sendJoinSuccessMsg(liveRoomDataManager.getSessionId());
        onJoinLinkMicListener.onJoinLinkMic(joinSuccess);
    }

    /**
     * 离开连麦，应发生如下调用:
     * <pre>
     * no push stream
     * send [joinLeave]
     * <pre/>
     */
    @Override
    public void setLeaveLinkMic() {
        linkMicManager.switchRoleToAudience();
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        onLeaveLinkMicListener.onLeaveLinkMic();
        isJoinLinkMic = false;
    }

    @Override
    public boolean isJoinChannel() {
        return isJoinChannel;
    }

    @Override
    public boolean isJoinLinkMic() {
        return isJoinLinkMic;
    }

    @Override
    public void setFirstScreenLinkMicId(String linkMicId) {
        if (PolyvLinkMicConfig.getInstance().isPureRtcOnlySubscribeMainScreenVideo()) {
            //mute掉原先的第一画面的视频
            linkMicManager.muteRemoteVideo(firstScreenLinkMicId, true);
            firstScreenLinkMicId = linkMicId;
            //订阅新的第一画面的视频
            linkMicManager.muteRemoteVideo(firstScreenLinkMicId, false);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API - 设置监听器">
    @Override
    public void setOnBeforeJoinChannelListener(OnBeforeJoinChannelListener li) {
        this.onBeforeJoinChannelListener = li;
    }

    @Override
    public void setOnLeaveLinkMicListener(OnLeaveLinkMicListener li) {
        onLeaveLinkMicListener = li;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口声明">
    public interface OnJoinRTCChannelWatchListener {
        void onJoinRTCChannelWatch();
    }
    // </editor-fold>
}
