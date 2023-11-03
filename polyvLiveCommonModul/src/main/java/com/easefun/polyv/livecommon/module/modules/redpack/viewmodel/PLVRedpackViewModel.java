package com.easefun.polyv.livecommon.module.modules.redpack.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.enums.PLVRedPaperType;
import com.easefun.polyv.livecommon.module.modules.redpack.model.PLVRedpackRepo;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.vo.PLVDelayRedpackVO;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.feature.redpack.model.PLVDelayRedpackStatusVO;
import com.plv.livescenes.feature.redpack.model.PLVRedpackReceiveStatusVO;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperForDelayEvent;
import com.plv.socket.event.redpack.PLVRedPaperResultEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVRedpackViewModel implements IPLVLifecycleAwareDependComponent {

    private final PLVRedpackRepo redpackRepo;

    private final MutableLiveData<PLVDelayRedpackVO> delayRedpackLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    private String channelId;
    @Nullable
    private String viewerId;

    public PLVRedpackViewModel(
            PLVRedpackRepo redpackRepo
    ) {
        this.redpackRepo = redpackRepo;

        observeRedPaperResultEvent();
        observeRedPaperForDelayEvent();
    }

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void observeRedPaperResultEvent() {
        final Disposable disposable = redpackRepo.redPaperResultEventObservable
                .filter(new Predicate<PLVRedPaperResultEvent>() {
                    @Override
                    public boolean test(@NonNull PLVRedPaperResultEvent redPaperResultEvent) throws Exception {
                        return PLVRedPaperType.isSupportType(redPaperResultEvent.getType());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<PLVRedPaperResultEvent>() {
                    @Override
                    public void accept(PLVRedPaperResultEvent redPaperResultEvent) throws Exception {
                        if (redPaperResultEvent.isRedPaperRunOut()) {
                            final PLVRedPaperEvent redPaperEvent = redpackRepo.getCachedRedPaper(redPaperResultEvent.getRedpackId());
                            if (redPaperEvent != null && redPaperEvent.getReceiveTypeLiveData().getValue() == PLVRedPaperReceiveType.AVAILABLE) {
                                redPaperEvent.updateReceiveType(PLVRedPaperReceiveType.RUN_OUT);
                                if (channelId != null && viewerId != null) {
                                    redpackRepo.updateReceiveStatus(redPaperEvent, channelId, viewerId, PLVRedPaperReceiveType.RUN_OUT);
                                }
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    private void observeRedPaperForDelayEvent() {
        final Disposable disposable = redpackRepo.redPaperForDelayEventObservable
                .filter(new Predicate<PLVRedPaperForDelayEvent>() {
                    @Override
                    public boolean test(@NonNull PLVRedPaperForDelayEvent redPaperForDelayEvent) throws Exception {
                        return PLVRedPaperType.isSupportType(redPaperForDelayEvent.getType());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Consumer<PLVRedPaperForDelayEvent>() {
                    @Override
                    public void accept(PLVRedPaperForDelayEvent redPaperForDelayEvent) throws Exception {
                        if (redPaperForDelayEvent.getDelayTime() == null) {
                            return;
                        }
                        final long sendTime = System.currentTimeMillis() + redPaperForDelayEvent.getDelayTime() * 1000;
                        delayRedpackLiveData.postValue(
                                new PLVDelayRedpackVO()
                                        .setRedpackSendTime(sendTime)
                                        .setRedPaperType(PLVRedPaperType.match(redPaperForDelayEvent.getType(), true))
                                        .setBlessing(redPaperForDelayEvent.getBlessing())
                        );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    // </editor-fold>

    public void initData(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
        this.viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
    }

    public void receiveRedPaper(final PLVRedPaperEvent redPaperEvent, final String channelId, final String viewerId) {
        final PLVRedPaperReceiveType currentReceiveType = redPaperEvent.getReceiveTypeLiveData().getValue();
        if (currentReceiveType == PLVRedPaperReceiveType.AVAILABLE) {
            final PLVRedPaperReceiveType newReceiveType = PLVRedPaperReceiveType.AVAILABLE_CLICKED;
            redPaperEvent.updateReceiveType(newReceiveType);
            redpackRepo.updateReceiveStatus(redPaperEvent, channelId, viewerId, newReceiveType);
        }
        if (currentReceiveType == PLVRedPaperReceiveType.RECEIVED
                || currentReceiveType == PLVRedPaperReceiveType.EXPIRED
                || currentReceiveType == PLVRedPaperReceiveType.RUN_OUT) {
            return;
        }

        final Disposable disposable = redpackRepo.getRealReceiveStatus(redPaperEvent, channelId, viewerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVRedpackReceiveStatusVO>() {
                    @Override
                    public void accept(PLVRedpackReceiveStatusVO status) throws Exception {
                        if (status.getSuccess() == null || !status.getSuccess() || status.getData() == null) {
                            return;
                        }
                        final PLVRedPaperReceiveType newReceiveType = PLVRedPaperReceiveType.matchOrDefault(status.getData().getState(), PLVRedPaperReceiveType.AVAILABLE);
                        if (newReceiveType != PLVRedPaperReceiveType.AVAILABLE) {
                            redPaperEvent.updateReceiveType(newReceiveType);
                            redpackRepo.updateReceiveStatus(redPaperEvent, channelId, viewerId, newReceiveType);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    public void updateRedPaperReceiveStatus(final String redpackId, @Nullable PLVRedPaperReceiveType receiveType) {
        if (channelId == null || viewerId == null || redpackId == null || receiveType == null) {
            return;
        }
        final PLVRedPaperEvent redPaperEvent = redpackRepo.getCachedRedPaper(redpackId);
        if (redPaperEvent == null) {
            return;
        }
        redPaperEvent.updateReceiveType(receiveType);
        redpackRepo.updateReceiveStatus(redPaperEvent, channelId, viewerId, receiveType);
    }

    public void updateDelayRedpackStatus(String channelId) {
        final Disposable disposable = redpackRepo.getDelayRedpackStatus(channelId)
                .filter(new Predicate<PLVDelayRedpackStatusVO>() {
                    @Override
                    public boolean test(@NonNull PLVDelayRedpackStatusVO delayRedpackStatusVO) throws Exception {
                        return PLVRedPaperType.isSupportType(delayRedpackStatusVO.getData().getRedpackType());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVDelayRedpackStatusVO>() {
                    @Override
                    public void accept(PLVDelayRedpackStatusVO delayRedpackStatus) throws Exception {
                        if (delayRedpackStatus.getSuccess() == null
                                || !delayRedpackStatus.getSuccess()
                                || delayRedpackStatus.getData() == null
                                || delayRedpackStatus.getData().getSendTime() == null) {
                            return;
                        }
                        delayRedpackLiveData.postValue(
                                new PLVDelayRedpackVO()
                                        .setRedpackSendTime(delayRedpackStatus.getData().getSendTime())
                                        .setBlessing(delayRedpackStatus.getData().getGreeting())
                                        .setRedPaperType(PLVRedPaperType.match(delayRedpackStatus.getData().getRedpackType(), true))
                        );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    public LiveData<PLVDelayRedpackVO> getDelayRedpackLiveData() {
        return delayRedpackLiveData;
    }

    @Override
    public void onCleared() {
        disposables.dispose();
    }

}
