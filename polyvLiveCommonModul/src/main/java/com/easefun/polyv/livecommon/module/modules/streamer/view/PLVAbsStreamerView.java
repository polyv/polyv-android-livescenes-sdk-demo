package com.easefun.polyv.livecommon.module.modules.streamer.view;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.List;

/**
 * mvp-推流和连麦view层抽象类
 */
public abstract class PLVAbsStreamerView implements IPLVStreamerContract.IStreamerView {
    @Override
    public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {

    }

    @Override
    public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {

    }

    @Override
    public void onUserMuteVideo(String uid, boolean mute, int streamerListPos, int memberListPos) {

    }

    @Override
    public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {

    }

    @Override
    public void onLocalUserMicVolumeChanged(int volume) {

    }

    @Override
    public void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList) {

    }

    @Override
    public void onUsersJoin(List<PLVLinkMicItemDataBean> dataBeanList) {

    }

    @Override
    public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {

    }

    @Override
    public void onNetworkQuality(int quality) {

    }

    @Override
    public void onUpdateStreamerTime(int secondsSinceStartTiming) {

    }

    @Override
    public void onShowNetBroken() {

    }

    @Override
    public void onStatesToStreamEnded() {

    }

    @Override
    public void onStatesToStreamStarted() {

    }

    @Override
    public void onStreamerError(int errorCode, Throwable throwable) {

    }

    @Override
    public void onUpdateMemberListData(List<PLVMemberItemDataBean> dataBeanList) {

    }

    @Override
    public void onCameraDirection(boolean front, int pos) {

    }

    @Override
    public void onUpdateSocketUserData(int pos) {

    }

    @Override
    public void onAddMemberListData(int pos) {

    }

    @Override
    public void onRemoveMemberListData(int pos) {

    }

    @Override
    public void onReachTheInteractNumLimit() {

    }

    @Override
    public void onUserRequest(String uid) {

    }

    @Override
    public void onStreamLiveStatusChanged(boolean isLive) {

    }

    @Override
    public void onGuestRTCStatusChanged(int pos) {

    }

    @Override
    public void onGuestMediaStatusChanged(int pos) {

    }

    @Override
    public void onScreenShareChange(int position, boolean isShare, int extra) {

    }

    @Override
    public void onFirstScreenChange(String linkMicUserId, boolean isFirstScreen) {

    }

    @Override
    public void onDocumentStreamerViewChange(boolean documentInMainScreen) {

    }

    @Override
    public void onSetPermissionChange(String type, boolean isGranted, boolean isCurrentUser, PLVSocketUserBean user) {

    }
}
