package com.easefun.polyv.livecommon.module.modules.chatroom.model.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.chatroom.PLVLocalMessage;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageListener;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.plv.foundationsdk.utils.PLVSugarUtil.pair;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatOnlineDataSource {

    // <editor-fold defaultstate="collapsed" desc="对外 - 数据源">

    public final Observable<PLVBaseViewData<PLVBaseEvent>> chatEventObservable = Observable.create(
            new ObservableOnSubscribe<PLVBaseViewData<PLVBaseEvent>>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<PLVBaseViewData<PLVBaseEvent>> observableEmitter) {
                    PLVManagerChatOnlineDataSource.this.emitter = observableEmitter;
                }
            }
    );

    // </editor-fold>

    private static final String TAG = PLVManagerChatOnlineDataSource.class.getSimpleName();

    private ObservableEmitter<PLVBaseViewData<PLVBaseEvent>> emitter;

    @Nullable
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;

    public void init(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        this.chatroomPresenter = chatroomPresenter;

        initPresenter(chatroomPresenter);
        initSendChatImageListener();
    }

    public Pair<Boolean, Integer> sendTextMessage(PolyvLocalMessage message) {
        if (chatroomPresenter == null) {
            PLVCommonLog.e(TAG, "sendTextMessage: chatroomPresenter is null");
            return pair(false, PLVLocalMessage.SENDVALUE_EXCEPTION);
        }
        return chatroomPresenter.sendChatMessage(message);
    }

    public void sendImageMessage(PolyvSendLocalImgEvent message) {
        if (chatroomPresenter == null) {
            PLVCommonLog.e(TAG, "sendImageMessage: chatroomPresenter is null");
            return;
        }
        chatroomPresenter.sendChatImage(message);
    }

    private void initPresenter(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        chatroomPresenter.registerView(chatroomView);
    }

    private final PLVAbsChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent) {
            if (speakEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(speakEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent) {
            if (chatImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(chatImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            if (localMessage != null && localMessage.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localMessage, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {
            if (localImgEvent != null && localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }
    };

    private void initSendChatImageListener() {
        PLVChatroomManager.getInstance().addSendChatImageListener(sendChatImageListener);
    }

    private final PLVSendChatImageListener sendChatImageListener = new PLVSendChatImageListener() {
        @Override
        public void onUploadFail(PLVSendLocalImgEvent localImgEvent, Throwable t) {
            localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
            if (localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onSendFail(PLVSendLocalImgEvent localImgEvent, int sendValue) {
            localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
            if (localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onSuccess(PLVSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
            localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_SUCCESS);
            if (localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onProgress(PLVSendLocalImgEvent localImgEvent, float progress) {
            localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_SENDING);
            localImgEvent.setSendProgress((int) (progress * 100));
            if (localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }

        @Override
        public void onCheckFail(PLVSendLocalImgEvent localImgEvent, Throwable t) {
            localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
            if (localImgEvent.isManagerChatMsg() && emitter != null) {
                emitter.onNext(new PLVBaseViewData<PLVBaseEvent>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_UNDEFINED));
            }
        }
    };

}
