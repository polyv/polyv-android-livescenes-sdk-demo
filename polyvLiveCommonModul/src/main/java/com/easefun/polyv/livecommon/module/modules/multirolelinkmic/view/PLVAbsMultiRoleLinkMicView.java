package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.hiclass.vo.PLVHCStudentLessonListVO;

import java.util.List;

/**
 * mvp-多角色连麦view层抽象类
 */
public abstract class PLVAbsMultiRoleLinkMicView implements IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView {
    @Override
    public void setPresenter(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter presenter) {

    }

    @Override
    public void onLinkMicEngineCreatedSuccess() {

    }

    @Override
    public void onLinkMicError(int errorCode, Throwable throwable) {

    }

    @Override
    public void onInitLinkMicList(String myLinkMicId, List<PLVLinkMicItemDataBean> linkMicList) {

    }

    @Override
    public void onUsersJoin(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

    }

    @Override
    public void onUsersLeave(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

    }

    @Override
    public void onUserExisted(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

    }

    @Override
    public void onTeacherScreenStream(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isOpen) {

    }

    @Override
    public void onLinkMicListChanged(List<PLVLinkMicItemDataBean> dataBeanList) {

    }

    @Override
    public void onMemberListChanged(List<PLVMemberItemDataBean> dataBeanList) {

    }

    @Override
    public void onMemberItemChanged(int pos) {

    }

    @Override
    public void onMemberItemInsert(int pos) {

    }

    @Override
    public void onMemberItemRemove(int pos) {

    }

    @Override
    public void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand, int linkMicListPos, int memberListPos) {

    }

    @Override
    public void onUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos) {

    }

    @Override
    public void onUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos) {

    }

    @Override
    public void onUserMuteVideo(String uid, boolean mute, int linkMicListPos, int memberListPos) {

    }

    @Override
    public void onUserMuteAudio(String uid, boolean mute, int linkMicListPos, int memberListPos) {

    }

    @Override
    public void onTeacherMuteMyMedia(boolean isVideoType, boolean isMute) {

    }

    @Override
    public void onTeacherControlMyLinkMic(boolean isAllowJoin) {

    }

    @Override
    public boolean onUserNeedAnswerLinkMic() {
        return false;
    }

    @Override
    public void onLocalUserVolumeChanged(int volume) {

    }

    @Override
    public void onRemoteUserVolumeChanged() {

    }

    @Override
    public void onReachTheInteractNumLimit() {

    }

    @Override
    public void onRepeatLogin(String desc) {

    }

    @Override
    public void onNetworkQuality(int quality) {

    }

    @Override
    public void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO) {

    }

    @Override
    public void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO) {

    }

    @Override
    public void onLessonPreparing(long serverTime, long lessonStartTime) {

    }

    @Override
    public void onLessonStarted() {

    }

    @Override
    public void onLessonEnd(long inClassTime, boolean isFromApi, @Nullable PLVHCStudentLessonListVO.DataVO dataVO) {

    }

    @Override
    public void onLessonLateTooLong(long willAutoStopLessonTimeMs) {

    }
}
