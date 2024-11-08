package com.easefun.polyv.livecommon.module.modules.venue.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.venue.di.PLVMultiVenueModule;
import com.easefun.polyv.livecommon.module.modules.venue.model.PLVMultiVenueRepo;
import com.plv.foundationsdk.component.di.IPLVLifecycleAwareDependComponent;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.livescenes.feature.venues.model.PLVVenueDataVO;
import com.plv.livescenes.feature.venues.model.PLVVenueStatusVO;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PLVMultiVenueViewModel implements IPLVLifecycleAwareDependComponent {

    private static final int DEFAULT_SIZE = 60;

    private static volatile PLVMultiVenueViewModel INSTANCE;
    private final PLVMultiVenueRepo plvMultiVenueRepo;
    private final MutableLiveData<List<PLVVenueDataVO>> venueListLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<PLVVenueStatusVO>> venueStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Pair<String, Boolean>>> onRequestLaunchOtherVenueLiveData = new MutableLiveData<>();
    // 获取所有关联的分会场信息
    private Disposable getVenueListDisposable;
    // 定时轮询关联的分会场当前状态
    private Disposable updateVenueStatusDisposable;

    private PLVMultiVenueViewModel (final PLVMultiVenueRepo repo) {
        this.plvMultiVenueRepo = repo;
    }

    public static PLVMultiVenueViewModel getInstance(final PLVMultiVenueRepo repo) {
        if (INSTANCE == null) {
            synchronized (PLVMultiVenueModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVMultiVenueViewModel(repo);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCleared() {
        if (updateVenueStatusDisposable != null) {
            updateVenueStatusDisposable.dispose();
        }
    }

    private void observeRepo() {
        if (getVenueListDisposable != null) {
            getVenueListDisposable.dispose();
        }
        getVenueListDisposable = plvMultiVenueRepo.venueListObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVVenueDataVO>>() {
                    @Override
                    public void accept(List<PLVVenueDataVO> datas) throws Exception {
                        venueListLiveData.postValue(datas);
                    }
                });
    }

    private void observeVenuesStatus() {
        if (updateVenueStatusDisposable != null) {
            updateVenueStatusDisposable.dispose();
        }
        updateVenueStatusDisposable = plvMultiVenueRepo.venueStatusObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PLVVenueStatusVO>>() {
                    @Override
                    public void accept(List<PLVVenueStatusVO> statusVOS) throws Exception {
                        venueStatusLiveData.postValue(statusVOS);
                    }
                });
    }

    public void getMultipleVenueData(String channelId, String mainVenueId) {
        observeRepo();
        plvMultiVenueRepo.getMultipleVenueData(channelId, mainVenueId, DEFAULT_SIZE);
    }

    public LiveData<List<PLVVenueDataVO>> getVenueListLiveData() {
        return venueListLiveData;
    }

    /**
     * 轮询所有分会场的状态信息
     * @param mainVenueId
     */
    public void updateVenueStatusList(String mainVenueId) {
        observeVenuesStatus();
        plvMultiVenueRepo.updateVenueStatus(mainVenueId);
    }

    public LiveData<List<PLVVenueStatusVO>> getVenueStatusLiveData() {
        return venueStatusLiveData;
    }


    public void setOnRequestLaunchOtherVenue(String channelId, boolean isPlayback) {
        this.onRequestLaunchOtherVenueLiveData.postValue(new Event<Pair<String, Boolean>>(new Pair<>(channelId, isPlayback)));
    }

    public LiveData<Event<Pair<String, Boolean>>> getOnRequestLaunchOtherVenueLiveData() {
        return onRequestLaunchOtherVenueLiveData;
    }
}
