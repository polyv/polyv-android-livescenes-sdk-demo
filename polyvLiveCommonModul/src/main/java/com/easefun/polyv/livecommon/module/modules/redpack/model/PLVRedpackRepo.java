package com.easefun.polyv.livecommon.module.modules.redpack.model;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.PLVRedpackLocalDataSource;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.PLVRedpackMemoryDataSource;
import com.plv.livescenes.feature.redpack.PLVRedpackApiManager;
import com.plv.livescenes.feature.redpack.model.PLVDelayRedpackStatusVO;
import com.plv.livescenes.feature.redpack.model.PLVRedpackReceiveStatusVO;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperForDelayEvent;
import com.plv.socket.event.redpack.PLVRedPaperResultEvent;
import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author Hoshiiro
 */
public class PLVRedpackRepo {

    // <editor-fold defaultstate="collapsed" desc="对外 - 数据源">

    public final Observable<PLVRedPaperResultEvent> redPaperResultEventObservable = Observable.create(new ObservableOnSubscribe<PLVRedPaperResultEvent>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVRedPaperResultEvent> emitter) {
            redPaperResultEventEmitter = emitter;
        }
    });

    public final Observable<PLVRedPaperForDelayEvent> redPaperForDelayEventObservable = Observable.create(new ObservableOnSubscribe<PLVRedPaperForDelayEvent>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVRedPaperForDelayEvent> emitter) {
            redPaperForDelayEventEmitter = emitter;
        }
    });

    // </editor-fold>

    private final PLVRedpackMemoryDataSource memoryDataSource;
    private final PLVRedpackLocalDataSource localDataSource;
    private final PLVRedpackApiManager redpackApiManager;

    private ObservableEmitter<PLVRedPaperResultEvent> redPaperResultEventEmitter;
    private ObservableEmitter<PLVRedPaperForDelayEvent> redPaperForDelayEventEmitter;

    public PLVRedpackRepo(
            PLVRedpackMemoryDataSource memoryDataSource,
            PLVRedpackLocalDataSource localDataSource,
            PLVRedpackApiManager redpackApiManager
    ) {
        this.memoryDataSource = memoryDataSource;
        this.localDataSource = localDataSource;
        this.redpackApiManager = redpackApiManager;
    }

    public void cacheRedPaper(PLVRedPaperEvent redPaperEvent) {
        memoryDataSource.cacheRedPaper(redPaperEvent);
    }

    @Nullable
    public PLVRedPaperEvent getCachedRedPaper(String redpackId) {
        return memoryDataSource.getCachedRedPaper(redpackId);
    }

    public void onRedPaperResultEvent(PLVRedPaperResultEvent redPaperResultEvent) {
        redPaperResultEventEmitter.onNext(redPaperResultEvent);
    }

    public PLVRedPaperReceiveType getCachedReceiveStatus(String redpackId, String viewerId) {
        return localDataSource.getReceiveStatus(redpackId, viewerId);
    }

    public void updateReceiveStatus(PLVRedPaperEvent redPaperEvent, String roomId, String viewerId, PLVRedPaperReceiveType newReceiveType) {
        localDataSource.updateReceiveStatus(redPaperEvent, roomId, viewerId, newReceiveType);
    }

    public Observable<PLVRedpackReceiveStatusVO> getRealReceiveStatus(final PLVRedPaperEvent redPaperEvent, final String channelId, final String viewerId) {
        return redpackApiManager.getReceiveStatus(redPaperEvent, channelId, viewerId);
    }

    public void onRedPaperForDelayEvent(PLVRedPaperForDelayEvent redPaperForDelayEvent) {
        redPaperForDelayEventEmitter.onNext(redPaperForDelayEvent);
    }

    public Observable<PLVDelayRedpackStatusVO> getDelayRedpackStatus(String channelId) {
        return redpackApiManager.getDelayRedpackStatus(channelId);
    }

}
