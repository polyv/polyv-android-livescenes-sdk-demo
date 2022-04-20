package com.easefun.polyv.livecommon.module.modules.chatroom.model.repo;

import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.datasource.PLVManagerChatHistoryDataSource;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.datasource.PLVManagerChatOnlineDataSource;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.vo.PLVManagerChatHistoryLoadStatus;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.socket.event.PLVBaseEvent;

import io.reactivex.Observable;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatRepo {

    private final PLVManagerChatHistoryDataSource historyDataSource = new PLVManagerChatHistoryDataSource();
    private final PLVManagerChatOnlineDataSource onlineDataSource = new PLVManagerChatOnlineDataSource();

    private Observable<PLVBaseViewData<PLVBaseEvent>> managerChatObservable;

    public PLVManagerChatRepo() {
        observeDataSource();
    }

    private void observeDataSource() {
        managerChatObservable = Observable.merge(historyDataSource.chatEventObservable, onlineDataSource.chatEventObservable);
    }

    public void init(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        onlineDataSource.init(chatroomPresenter);
    }

    public Observable<PLVBaseViewData<PLVBaseEvent>> getManagerChatObservable() {
        return managerChatObservable;
    }

    public Observable<PLVManagerChatHistoryLoadStatus> getHistoryLoadStatusObservable() {
        return historyDataSource.loadStatusObservable;
    }

    public void requestChatHistory(final String roomId, final int start, final int end) {
        historyDataSource.requestChatHistory(roomId, start, end);
    }

    public Pair<Boolean, Integer> sendTextMessage(PolyvLocalMessage message) {
        return onlineDataSource.sendTextMessage(message);
    }

    public void sendImageMessage(PolyvSendLocalImgEvent message) {
        onlineDataSource.sendImageMessage(message);
    }

}
