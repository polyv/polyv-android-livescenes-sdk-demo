package com.plv.livecommon.module.modules.chatroom.presenter.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Pair;

import com.plv.livescenes.model.PLVChatFunctionSwitchVO;
import com.plv.livescenes.model.bulletin.PLVBulletinVO;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVLoginEvent;

import java.util.List;

/**
 * 聊天室业务数据，主要用于提供给 非mvp的v 监听/获取聊天室的数据
 */
public class PLVChatroomData {
    //公告信息
    private MutableLiveData<PLVBulletinVO> bulletinVO = new MutableLiveData<>();
    //聊天室功能开关
    private MutableLiveData<List<PLVChatFunctionSwitchVO.DataBean>> functionSwitchData = new MutableLiveData<>();
    //点赞数
    private MutableLiveData<Long> likesCountData = new MutableLiveData<>();
    //观看热度数
    private MutableLiveData<Long> viewerCountData = new MutableLiveData<>();
    //在线人数
    private MutableLiveData<Integer> onlineCountData = new MutableLiveData<>();
    //聊天室收到的文本发言信息(包括自己本地发送的信息)
    private MutableLiveData<Pair<CharSequence, Boolean>> speakMessageData = new MutableLiveData<>();
    //聊天室登录事件
    private MutableLiveData<PLVLoginEvent> loginEventData = new MutableLiveData<>();
    //聊天室打赏事件
    private MutableLiveData<PLVRewardEvent> rewardEventData = new MutableLiveData<>();

    public LiveData<PLVBulletinVO> getBulletinVO() {
        return bulletinVO;
    }

    //bulletin为null时为隐藏公告
    public void postBulletinVO(PLVBulletinVO bulletin) {
        bulletinVO.postValue(bulletin);
    }

    public LiveData<List<PLVChatFunctionSwitchVO.DataBean>> getFunctionSwitchData() {
        return functionSwitchData;
    }

    public void postFunctionSwitchData(List<PLVChatFunctionSwitchVO.DataBean> data) {
        functionSwitchData.postValue(data);
    }

    public LiveData<Long> getLikesCountData() {
        return likesCountData;
    }

    public void postLikesCountData(long likesCount) {
        likesCountData.postValue(likesCount);
    }

    public LiveData<Long> getViewerCountData() {
        return viewerCountData;
    }

    public void postViewerCountData(long viewerCount) {
        viewerCountData.postValue(viewerCount);
    }

    public LiveData<Integer> getOnlineCountData() {
        return onlineCountData;
    }

    public void postOnlineCountData(int onlineCount) {
        onlineCountData.postValue(onlineCount);
    }

    public LiveData<Pair<CharSequence, Boolean>> getSpeakMessageData() {
        return speakMessageData;
    }

    //isSpecialType：是否是特殊类型，包括 我、讲师、助教、管理员、嘉宾
    public void postSpeakMessageData(CharSequence message, boolean isSpecialType) {
        speakMessageData.postValue(new Pair<>(message, isSpecialType));
    }

    public LiveData<PLVLoginEvent> getLoginEventData() {
        return loginEventData;
    }

    public void postLoginEventData(PLVLoginEvent loginEvent) {
        loginEventData.postValue(loginEvent);
    }

    public LiveData<PLVRewardEvent> getRewardEvent() {
        return rewardEventData;
    }

    public void postRewardEvent(PLVRewardEvent rewardEvent) {
        rewardEventData.postValue(rewardEvent);
    }
}
