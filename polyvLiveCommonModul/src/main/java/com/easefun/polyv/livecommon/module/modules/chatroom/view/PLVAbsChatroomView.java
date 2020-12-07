package com.easefun.polyv.livecommon.module.modules.chatroom.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;

import java.util.List;

/**
 * mvp-聊天室view层抽象类
 */
public abstract class PLVAbsChatroomView implements IPLVChatroomContract.IChatroomView {
    @Override
    public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {

    }

    @Override
    public void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent) {

    }

    @Override
    public int getSpeakEmojiSize() {
        return 0;
    }

    @Override
    public int getQuizEmojiSize() {
        return 0;
    }

    @Override
    public void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent) {

    }

    @Override
    public void onLikesEvent(@NonNull PLVLikesEvent likesEvent) {

    }

    @Override
    public void onAnswerEvent(@NonNull PLVTAnswerEvent answerEvent) {

    }

    @Override
    public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {

    }

    @Override
    public void onLogoutEvent(@NonNull PLVLogoutEvent logoutEvent) {

    }

    @Override
    public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {

    }

    @Override
    public void onRemoveBulletinEvent() {

    }

    @Override
    public void onProductControlEvent(@NonNull PLVProductControlEvent productControlEvent) {

    }

    @Override
    public void onProductRemoveEvent(@NonNull PLVProductRemoveEvent productRemoveEvent) {

    }

    @Override
    public void onProductMoveEvent(@NonNull PLVProductMoveEvent productMoveEvent) {

    }

    @Override
    public void onProductMenuSwitchEvent(@NonNull PLVProductMenuSwitchEvent productMenuSwitchEvent) {

    }

    @Override
    public void onCloseRoomEvent(@NonNull PLVCloseRoomEvent closeRoomEvent) {

    }

    @Override
    public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {

    }

    @Override
    public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {

    }

    @Override
    public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {

    }

    @Override
    public void onLocalQuestionMessage(@Nullable PolyvQuestionMessage questionMessage) {

    }

    @Override
    public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {

    }

    @Override
    public void onSendProhibitedWord(@NonNull String prohibitedMessage, @NonNull String hintMsg, @NonNull String status) {

    }

    @Override
    public void onSpeakImgDataList(List<PLVBaseViewData> chatMessageDataList) {

    }

    @Override
    public void onHistoryDataList(List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory, int viewIndex) {

    }

    @Override
    public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {

    }
}
