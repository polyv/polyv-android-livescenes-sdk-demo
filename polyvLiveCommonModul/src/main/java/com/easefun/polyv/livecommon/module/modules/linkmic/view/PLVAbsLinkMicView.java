package com.easefun.polyv.livecommon.module.modules.linkmic.view;

import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVViewerLinkMicState;
import com.plv.linkmic.PLVLinkMicConstant;

import java.util.List;

/**
 * @author suhongtao
 */
public abstract class PLVAbsLinkMicView implements IPLVLinkMicContract.IPLVLinkMicView {

    @Override
    public void onLinkMicError(int errorCode, Throwable throwable) {

    }

    @Override
    public void onTeacherOpenLinkMic() {

    }

    @Override
    public void onTeacherCloseLinkMic() {

    }

    @Override
    public void onTeacherAllowJoin() {

    }

    @Override
    public void onLinkMicStateChanged(PLVViewerLinkMicState oldState, PLVViewerLinkMicState newState) {

    }

    @Override
    public void onJoinChannelTimeout() {

    }

    @Override
    public void onLinkMicMemberReachLimit() {

    }

    @Override
    public void onPrepareLinkMicList(String linkMicUid, PLVLinkMicListShowMode linkMicListShowMode, List<PLVLinkMicItemDataBean> linkMicList) {

    }

    @Override
    public void onJoinRtcChannel() {

    }

    @Override
    public void onLeaveRtcChannel() {

    }

    @Override
    public void onChangeListShowMode(PLVLinkMicListShowMode linkMicListShowMode) {

    }

    @Override
    public void onJoinLinkMic() {

    }

    @Override
    public void onLeaveLinkMic() {

    }

    @Override
    public void onUsersJoin(List<String> uids) {

    }

    @Override
    public void onUsersLeave(List<String> uids) {

    }

    @Override
    public void onTeacherHangupMe() {

    }

    @Override
    public void onNotInLinkMicList() {

    }

    @Override
    public void onUserMuteVideo(String uid, boolean mute, int pos) {

    }

    @Override
    public void onUserMuteAudio(String uid, boolean mute, int pos) {

    }

    @Override
    public void onLocalUserMicVolumeChanged() {

    }

    @Override
    public void onRemoteUserVolumeChanged(List<PLVLinkMicItemDataBean> linkMicList) {

    }

    @Override
    public void onNetQuality(PLVLinkMicConstant.NetworkQuality quality) {

    }

    @Override
    public void onVideoSizeChanged(String uid, int width, int height) {

    }

    @Override
    public void onSwitchFirstScreen(String linkMicId) {

    }

    @Override
    public void onAdjustTeacherLocation(String linkMicId, int teacherPos, boolean isNeedSwitchToMain, Runnable onAdjustFinished) {

    }

    @Override
    public void onSwitchPPTViewLocation(boolean toMainScreen) {

    }

    @Override
    public boolean isMediaShowInLinkMicList() {
        return false;
    }

    @Override
    public int getMediaViewIndexInLinkMicList() {
        return 0;
    }

    @Override
    public void performClickInLinkMicListItem(int index) {

    }

    @Override
    public void updateAllLinkMicList() {

    }

    @Override
    public void onRTCPrepared() {

    }

    @Override
    public void updateFirstScreenChanged(String firstScreenLinkMicId, int oldPos, int newPos) {

    }

}
