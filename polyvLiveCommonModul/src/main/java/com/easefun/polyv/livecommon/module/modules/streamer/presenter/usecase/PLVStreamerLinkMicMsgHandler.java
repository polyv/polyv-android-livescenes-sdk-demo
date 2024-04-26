package com.easefun.polyv.livecommon.module.modules.streamer.presenter.usecase;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.socket.user.PLVSocketUserBean;

import io.socket.client.Ack;

/**
 * @author Hoshiiro
 */
public abstract class PLVStreamerLinkMicMsgHandler {

    protected static final String TAG = PLVStreamerLinkMicMsgHandler.class.getSimpleName();

    protected boolean isVideoLinkMic;
    protected boolean isOpenLinkMic;
    protected boolean isAllowViewerRaiseHand;

    protected PLVStreamerPresenter streamerPresenter = null;

    PLVStreamerLinkMicMsgHandler() {
    }

    public static PLVStreamerLinkMicMsgHandler create(String channelId) {
        boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        if (!isNewLinkMicStrategy) {
            return new PLVStreamerLinkMicMsgHandlerV1();
        } else {
            return new PLVStreamerLinkMicMsgHandlerV2();
        }
    }

    public boolean openChannelLinkMic(boolean isVideo, @Nullable Ack ack) {
        this.isVideoLinkMic = isVideo;
        this.isOpenLinkMic = true;
        return false;
    }

    public boolean closeChannelLinkMic(@Nullable Ack ack) {
        this.isOpenLinkMic = false;
        return false;
    }

    public boolean allowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        this.isAllowViewerRaiseHand = true;
        return false;
    }

    public boolean disallowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        this.isAllowViewerRaiseHand = false;
        return false;
    }

    public boolean changeLinkMicType(boolean isVideo) {
        this.isVideoLinkMic = isVideo;
        return false;
    }

    public void closeAllUserLinkMic(String sessionId, @Nullable Ack ack) {

    }

    public PLVStreamerLinkMicMsgHandler setStreamerPresenter(PLVStreamerPresenter streamerPresenter) {
        this.streamerPresenter = streamerPresenter;
        return this;
    }

    public void acceptRaiseHandLinkMic(PLVSocketUserBean socketUserBean, @Nullable Ack ack) {
        PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, ack);
    }

    public void inviteLinkMic(PLVSocketUserBean socketUserBean, boolean isNeedAnswer, @Nullable Ack ack) {
        PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, isNeedAnswer, ack);
    }

    public void hangUpLinkMic(String linkMicId, @Nullable Ack ack) {
        PLVLinkMicEventSender.getInstance().closeUserLinkMic(linkMicId, ack);
    }

    public boolean isVideoLinkMic() {
        return isVideoLinkMic;
    }

    public boolean isOpenLinkMic() {
        return isOpenLinkMic;
    }

    public boolean isAllowViewerRaiseHand() {
        return isAllowViewerRaiseHand;
    }

}

final class PLVStreamerLinkMicMsgHandlerV1 extends PLVStreamerLinkMicMsgHandler {

    @Override
    public boolean openChannelLinkMic(boolean isVideo, @Nullable Ack ack) {
        super.openChannelLinkMic(isVideo, ack);
        return PLVLinkMicEventSender.getInstance().openLinkMic(isVideo, true, ack);
    }

    @Override
    public boolean closeChannelLinkMic(@Nullable Ack ack) {
        super.closeChannelLinkMic(ack);
        return PLVLinkMicEventSender.getInstance().openLinkMic(isVideoLinkMic, false, ack);
    }

    @Override
    public boolean allowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        // not support
        return false;
    }

    @Override
    public boolean disallowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        // not support
        return false;
    }

    @Override
    public boolean changeLinkMicType(boolean isVideo) {
        // not support
        return false;
    }

    @Override
    public void closeAllUserLinkMic(String sessionId, @Nullable Ack ack) {
        PLVLinkMicEventSender.getInstance().closeAllUserLinkMic(sessionId, ack);
    }

}

final class PLVStreamerLinkMicMsgHandlerV2 extends PLVStreamerLinkMicMsgHandler {

    public PLVStreamerLinkMicMsgHandlerV2() {
        super();
        // 新版连麦逻辑默认开启连麦
        isOpenLinkMic = true;
    }

    @Override
    public boolean openChannelLinkMic(boolean isVideo, @Nullable Ack ack) {
        // not support
        return true;
    }

    @Override
    public boolean closeChannelLinkMic(@Nullable Ack ack) {
        // not support
        return true;
    }

    @Override
    public boolean allowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        super.allowViewerRaiseHandLinkMic(ack);
        return PLVLinkMicEventSender.getInstance().openLinkMic(isVideoLinkMic, true, ack);
    }

    @Override
    public boolean disallowViewerRaiseHandLinkMic(@Nullable Ack ack) {
        super.disallowViewerRaiseHandLinkMic(ack);
        return PLVLinkMicEventSender.getInstance().openLinkMic(isVideoLinkMic, false, ack);
    }

    @Override
    public boolean changeLinkMicType(boolean isVideo) {
        final boolean oldIsVideo = isVideoLinkMic;
        super.changeLinkMicType(isVideo);

        final int streamerStatus = streamerPresenter.getStreamerStatus();
        if (streamerStatus != PLVStreamerPresenter.STREAMER_STATUS_START_SUCCESS) {
            PLVCommonLog.i(TAG, format("streamerStatus {} is not start success, will not change linkmic type to isVideo:{}", streamerStatus, isVideoLinkMic));
            return true;
        }

        boolean success;
        if (isAllowViewerRaiseHand) {
            success = allowViewerRaiseHandLinkMic(null);
        } else {
            success = disallowViewerRaiseHandLinkMic(null);
        }

        if (!success) {
            super.changeLinkMicType(oldIsVideo);
        }
        return success;
    }

    @Override
    public void closeAllUserLinkMic(String sessionId, @Nullable Ack ack) {
        PLVLinkMicEventSender.getInstance().closeAllUserLinkMicV2(ack);
    }

}