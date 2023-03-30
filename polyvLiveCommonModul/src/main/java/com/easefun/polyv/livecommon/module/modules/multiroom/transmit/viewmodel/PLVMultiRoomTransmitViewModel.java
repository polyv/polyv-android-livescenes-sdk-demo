package com.easefun.polyv.livecommon.module.modules.multiroom.transmit.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.PLVMultiRoomTransmitRepo;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.vo.PLVMultiRoomTransmitVO;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVMultiRoomTransmitViewModel implements IPLVLifecycleAwareDependComponent {

    private final PLVMultiRoomTransmitRepo multiRoomTransmitRepo;

    private final MutableLiveData<PLVMultiRoomTransmitVO> transmitLiveData = new MutableLiveData<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PLVMultiRoomTransmitViewModel(
            PLVMultiRoomTransmitRepo multiRoomTransmitRepo
    ) {
        this.multiRoomTransmitRepo = multiRoomTransmitRepo;

        observeTransmitData();
    }

    private void observeTransmitData() {
        Disposable disposable = multiRoomTransmitRepo.transmitObservable
                .retry()
                .subscribe(new Consumer<PLVMultiRoomTransmitVO>() {
                    @Override
                    public void accept(PLVMultiRoomTransmitVO multiRoomTransmitVO) throws Exception {
                        transmitLiveData.postValue(multiRoomTransmitVO);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        compositeDisposable.add(disposable);
    }

    public LiveData<PLVMultiRoomTransmitVO> getTransmitLiveData() {
        return transmitLiveData;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
    }
}
