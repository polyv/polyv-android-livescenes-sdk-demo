package com.easefun.polyv.livecommon.module.modules.chatroom.presenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.repo.PLVManagerChatRepo;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.vo.PLVManagerChatHistoryLoadStatus;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase.PLVCalculateUnreadMessageCountUseCase;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase.PLVCombineSameUserChatUseCase;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase.PLVMergeChatEventUseCase;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase.PLVWrapChatEventUseCase;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVManagerChatUiState;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVManagerChatVO;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.chatroom.PLVLocalMessage;
import com.plv.socket.event.PLVBaseEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatViewModel extends ViewModel {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVManagerChatRepo chatRepo = new PLVManagerChatRepo();
    private final PLVWrapChatEventUseCase wrapChatEventUseCase = new PLVWrapChatEventUseCase();
    private final PLVMergeChatEventUseCase mergeChatEventUseCase = new PLVMergeChatEventUseCase();
    private final PLVCombineSameUserChatUseCase combineSameUserChatUseCase = new PLVCombineSameUserChatUseCase();
    private final PLVCalculateUnreadMessageCountUseCase calculateUnreadMessageCountUseCase = new PLVCalculateUnreadMessageCountUseCase();

    private final MutableLiveData<PLVManagerChatVO> managerChatLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVManagerChatUiState> uiStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Event<String>>> notifyMsgLiveData = new MutableLiveData<>();

    private final List<PLVChatEventWrapVO> originChatEvents = new ArrayList<>();
    private PLVManagerChatUiState lastUiState = new PLVManagerChatUiState();
    private long lastReadTime;

    private final CompositeDisposable disposes = new CompositeDisposable();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVManagerChatViewModel() {
        initUiState();
        observeManagerChatData();
        observeHistoryLoadStatus();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">

    @Override
    protected void onCleared() {
        disposes.dispose();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initUiState() {
        managerChatLiveData.postValue(new PLVManagerChatVO());
        uiStateLiveData.postValue(lastUiState);
    }

    private void observeManagerChatData() {
        final Disposable chatDataDisposable = chatRepo.getManagerChatObservable()
                .observeOn(Schedulers.computation())
                .map(new Function<PLVBaseViewData<PLVBaseEvent>, PLVChatEventWrapVO>() {
                    @Override
                    public PLVChatEventWrapVO apply(@NonNull PLVBaseViewData<PLVBaseEvent> plvBaseEventPLVBaseViewData) {
                        return wrapChatEventUseCase.wrap(plvBaseEventPLVBaseViewData.getData());
                    }
                })
                .filter(new Predicate<PLVChatEventWrapVO>() {
                    @Override
                    public boolean test(@NonNull PLVChatEventWrapVO plvChatEventWrapVO) {
                        return plvChatEventWrapVO.isValid();
                    }
                })
                .doOnNext(new Consumer<PLVChatEventWrapVO>() {
                    @Override
                    public void accept(PLVChatEventWrapVO plvChatEventWrapVO) {
                        mergeChatEventUseCase.merge(originChatEvents, plvChatEventWrapVO);
                    }
                })
                .map(new Function<PLVChatEventWrapVO, List<PLVChatEventWrapVO>>() {
                    @Override
                    public List<PLVChatEventWrapVO> apply(@NonNull PLVChatEventWrapVO plvChatEventWrapVO) {
                        return combineSameUserChatUseCase.combine(originChatEvents);
                    }
                })
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVChatEventWrapVO>>() {
                    @Override
                    public void accept(List<PLVChatEventWrapVO> chatEventWrapVOList) {
                        PLVManagerChatVO vo = managerChatLiveData.getValue();
                        if (vo == null) {
                            vo = new PLVManagerChatVO();
                        }
                        vo.setChatEventWrapVOList(chatEventWrapVOList);
                        managerChatLiveData.postValue(vo);
                        updateUnreadMessageCount();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposes.add(chatDataDisposable);
    }

    private void observeHistoryLoadStatus() {
        final Disposable historyLoadStatusDisposable = chatRepo.getHistoryLoadStatusObservable()
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVManagerChatHistoryLoadStatus>() {
                    @Override
                    public void accept(PLVManagerChatHistoryLoadStatus loadStatus) {
                        postUiStateUpdate(lastUiState.copy()
                                .setCanLoadMoreHistoryMessage(loadStatus.isCanLoadMore())
                                .setHistoryMessageLoading(loadStatus.isLoading())
                        );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposes.add(historyLoadStatusDisposable);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 初始化调用">

    public void init(IPLVChatroomContract.IChatroomPresenter chatroomPresenter) {
        chatRepo.init(chatroomPresenter);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 状态变更回调">

    public LiveData<PLVManagerChatVO> getManagerChatLiveData() {
        return managerChatLiveData;
    }

    public LiveData<PLVManagerChatUiState> getUiStateLiveData() {
        return uiStateLiveData;
    }

    public LiveData<List<Event<String>>> getNotifyMsgLiveData() {
        return notifyMsgLiveData;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void requestChatHistory(final String roomId, final int start, final int end) {
        chatRepo.requestChatHistory(roomId, start, end);
    }

    public boolean sendTextMessage(String message) {
        if (TextUtils.isEmpty(message) && PLVAppUtils.getApp() != null) {
            addNotifyMsg(PLVAppUtils.getApp().getString(R.string.plv_chat_toast_send_text_empty));
            return false;
        }

        final PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
        localMessage.setIsManagerChatMsg(true);
        if (localMessage.getId() == null) {
            localMessage.setId(String.valueOf(System.currentTimeMillis()));
        }

        final Pair<Boolean, Integer> res = chatRepo.sendTextMessage(localMessage);
        final boolean isSuccess = res.first;
        if (!isSuccess && PLVAppUtils.getApp() != null) {
            addNotifyMsg(format(PLVAppUtils.getApp().getString(R.string.plv_chat_toast_send_msg_failed_param), PLVLocalMessage.sendValueToDescribe(res.second)));
            return false;
        }

        return isSuccess;
    }

    public void sendImageMessage(PolyvSendLocalImgEvent message) {
        message.setIsManagerChatMsg(true);
        if (message.getId() == null) {
            message.setId(String.valueOf(System.currentTimeMillis()));
        }
        chatRepo.sendImageMessage(message);
    }

    public void setMessageAlreadyRead(PLVChatEventWrapVO chatEventWrapVO) {
        if (chatEventWrapVO == null) {
            return;
        }
        if (lastReadTime > chatEventWrapVO.getLastEventTime()) {
            return;
        }
        this.lastReadTime = chatEventWrapVO.getLastEventTime();
        updateUnreadMessageCount();
    }

    public void removeNotifyMsg(Event<String> notifyMsg) {
        final List<Event<String>> notifyMsgList = notifyMsgLiveData.getValue();
        if (notifyMsgList == null) {
            notifyMsgLiveData.postValue(new ArrayList<Event<String>>());
            return;
        }
        notifyMsgList.remove(notifyMsg);
        notifyMsgLiveData.postValue(notifyMsgList);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void updateUnreadMessageCount() {
        final int unreadMessageCount = calculateUnreadMessageCountUseCase.calculate(originChatEvents, lastReadTime);
        postUiStateUpdate(lastUiState.copy().setUnreadMessageCount(unreadMessageCount));
    }

    private void addNotifyMsg(String notifyMsg) {
        List<Event<String>> notifyMsgList = notifyMsgLiveData.getValue();
        if (notifyMsgList == null) {
            notifyMsgList = new ArrayList<>();
        }
        notifyMsgList.add(new Event<>(notifyMsg));
        notifyMsgLiveData.postValue(notifyMsgList);
    }

    private void postUiStateUpdate(PLVManagerChatUiState uiState) {
        lastUiState = uiState;
        uiStateLiveData.postValue(uiState);
    }

    // </editor-fold>

}
