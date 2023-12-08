package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVSipLinkMicRepo;
import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.usecase.PLVSipLinkMicMergeViewerUseCase;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingInListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingOutListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicConnectedListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicUiState;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.linkmic.sip.vo.PLVSipChannelInfoVO;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicViewModel implements IPLVLifecycleAwareDependComponent {

    private final PLVSipLinkMicRepo sipLinkMicRepo;
    private final PLVSipLinkMicMergeViewerUseCase mergeViewerUseCase;

    private final MutableLiveData<PLVSipLinkMicCallingInListState> callingInListStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVSipLinkMicCallingOutListState> callingOutListStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVSipLinkMicConnectedListState> connectedListStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PLVSipLinkMicUiState> uiStateLiveData = new MutableLiveData<>();
    private final PLVSipLinkMicCallingInListState callingInListState = new PLVSipLinkMicCallingInListState();
    private final PLVSipLinkMicCallingOutListState callingOutListState = new PLVSipLinkMicCallingOutListState();
    private final PLVSipLinkMicConnectedListState connectedListState = new PLVSipLinkMicConnectedListState();
    private final PLVSipLinkMicUiState uiState = new PLVSipLinkMicUiState();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public PLVSipLinkMicViewModel(
            final PLVSipLinkMicRepo sipLinkMicRepo,
            final PLVSipLinkMicMergeViewerUseCase mergeViewerUseCase
    ) {
        this.sipLinkMicRepo = sipLinkMicRepo;
        this.mergeViewerUseCase = mergeViewerUseCase;

        initUiState();
        observeLinkMicViewer();

        sipLinkMicRepo.updateSipLinkMicViewerList();
    }

    private void initUiState() {
        callingInListState.callingInViewerList = new ArrayList<>();
        callingInListStateLiveData.postValue(callingInListState.copy());
        callingOutListState.callingOutViewerList = new ArrayList<>();
        callingOutListStateLiveData.postValue(callingOutListState.copy());
        connectedListState.connectedViewerList = new ArrayList<>();
        connectedListStateLiveData.postValue(connectedListState.copy());
        uiState.sipCallInNumber = "";
        uiStateLiveData.postValue(uiState.clone());
    }

    private void observeLinkMicViewer() {
        Disposable disposable = sipLinkMicRepo.getLinkMicViewerObservable()
                .observeOn(Schedulers.computation())
                .retry()
                .subscribe(new Consumer<PLVSipLinkMicViewerVO>() {
                    @Override
                    public void accept(PLVSipLinkMicViewerVO viewerVO) throws Exception {
                        if (mergeViewerUseCase.reduceCallingInState(callingInListState, viewerVO)) {
                            callingInListStateLiveData.postValue(callingInListState.copy());
                        }
                        if (mergeViewerUseCase.reduceCallingOutState(callingOutListState, viewerVO)) {
                            callingOutListStateLiveData.postValue(callingOutListState.copy());
                        }
                        if (mergeViewerUseCase.reduceConnectedState(connectedListState, viewerVO)) {
                            connectedListStateLiveData.postValue(connectedListState.copy());
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

    @Override
    public void onCleared() {
        disposables.dispose();
    }

    public void requestSipChannelInfo() {
        Disposable disposable = sipLinkMicRepo.getSipChannelInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVSipChannelInfoVO>() {
                    @Override
                    public void accept(final PLVSipChannelInfoVO sipChannelInfoVO) throws Exception {
                        uiState.sipCallInNumber = nullable(new PLVSugarUtil.Supplier<String>() {
                            @Override
                            public String get() {
                                return sipChannelInfoVO.getData().getUcSipPhone();
                            }
                        });
                        uiStateLiveData.postValue(uiState.clone());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        disposables.add(disposable);
    }

    public void updateIsSupportSip(boolean sipEnable) {
        uiState.sipEnable = sipEnable;
        uiStateLiveData.postValue(uiState.clone());
    }

    public LiveData<PLVSipLinkMicCallingInListState> getCallingInListStateLiveData() {
        return callingInListStateLiveData;
    }

    public LiveData<PLVSipLinkMicCallingOutListState> getCallingOutListStateLiveData() {
        return callingOutListStateLiveData;
    }

    public LiveData<PLVSipLinkMicConnectedListState> getConnectedListStateLiveData() {
        return connectedListStateLiveData;
    }

    public LiveData<PLVSipLinkMicUiState> getUiStateLiveData() {
        return uiStateLiveData;
    }
}
