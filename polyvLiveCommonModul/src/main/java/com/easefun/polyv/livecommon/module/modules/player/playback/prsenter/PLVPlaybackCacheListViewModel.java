package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.PLVPlaybackCacheRepo;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.usecase.PLVPlaybackCacheListMergeUseCase;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheListViewModel {

    // <editor-fold defaultstate="collapsed" desc="单例 - 构造方法">

    private static volatile PLVPlaybackCacheListViewModel INSTANCE;

    private PLVPlaybackCacheListViewModel(
            final PLVPlaybackCacheRepo playbackCacheRepo,
            final PLVPlaybackCacheListMergeUseCase listMergeUseCase
    ) {
        this.playbackCacheRepo = playbackCacheRepo;
        this.listMergeUseCase = listMergeUseCase;
        init();
    }

    public static PLVPlaybackCacheListViewModel getInstance(
            final PLVPlaybackCacheRepo playbackCacheRepo,
            final PLVPlaybackCacheListMergeUseCase listMergeUseCase
    ) {
        if (INSTANCE == null) {
            synchronized (PLVPlaybackCacheListViewModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVPlaybackCacheListViewModel(playbackCacheRepo, listMergeUseCase);
                }
            }
        }
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVPlaybackCacheRepo playbackCacheRepo;

    private final PLVPlaybackCacheListMergeUseCase listMergeUseCase;

    private final MutableLiveData<List<PLVPlaybackCacheVideoVO>> downloadingListLiveData = new MutableLiveData<>();
    private final List<PLVPlaybackCacheVideoVO> downloadingList = new ArrayList<>();
    private final MutableLiveData<List<PLVPlaybackCacheVideoVO>> downloadedListLiveData = new MutableLiveData<>();
    private final List<PLVPlaybackCacheVideoVO> downloadedList = new ArrayList<>();
    private final MutableLiveData<Event<PLVPlaybackCacheVideoVO>> onRequestLaunchDownloadedPlaybackLiveData = new MutableLiveData<>();

    private Disposable playbackCacheVideoUpdateDisposable;
    private Disposable loadCacheListDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void init() {
        observePlaybackCacheUpdate();

        initLoadPlaybackCacheList();
    }

    private void observePlaybackCacheUpdate() {
        if (playbackCacheVideoUpdateDisposable != null) {
            playbackCacheVideoUpdateDisposable.dispose();
        }
        playbackCacheVideoUpdateDisposable = playbackCacheRepo.playbackCacheVideoUpdateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .retry()
                .subscribe(new Consumer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void accept(PLVPlaybackCacheVideoVO videoVO) throws Exception {
                        final boolean updateDownloadingList = listMergeUseCase.reduceDownloadingList(downloadingList, videoVO);
                        final boolean updateDownloadedList = listMergeUseCase.reduceDownloadedList(downloadedList, videoVO);
                        if (updateDownloadingList) {
                            downloadingListLiveData.postValue(new ArrayList<>(downloadingList));
                        }
                        if (updateDownloadedList) {
                            downloadedListLiveData.postValue(new ArrayList<>(downloadedList));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void initLoadPlaybackCacheList() {
        if (loadCacheListDisposable != null) {
            loadCacheListDisposable.dispose();
        }
        loadCacheListDisposable = playbackCacheRepo.listCacheVideos()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Consumer<List<PLVPlaybackCacheVideoVO>>() {
                    @Override
                    public void accept(List<PLVPlaybackCacheVideoVO> vos) throws Exception {
                        for (PLVPlaybackCacheVideoVO vo : vos) {
                            listMergeUseCase.reduceDownloadingList(downloadingList, vo);
                            listMergeUseCase.reduceDownloadedList(downloadedList, vo);
                        }
                        downloadingListLiveData.postValue(new ArrayList<>(downloadingList));
                        downloadedListLiveData.postValue(new ArrayList<>(downloadedList));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void startDownload(PLVPlaybackCacheVideoVO vo) {
        playbackCacheRepo.startDownloadVideo(vo);
    }

    public void pauseDownload(PLVPlaybackCacheVideoVO vo) {
        playbackCacheRepo.pauseDownloadVideo(vo);
    }

    public void deleteDownload(PLVPlaybackCacheVideoVO vo) {
        playbackCacheRepo.deleteDownloadVideo(vo);
    }

    public void requestLaunchDownloadedPlayback(PLVPlaybackCacheVideoVO vo) {
        this.onRequestLaunchDownloadedPlaybackLiveData.postValue(new Event<>(vo));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外数据源">

    public LiveData<List<PLVPlaybackCacheVideoVO>> getDownloadingListLiveData() {
        return downloadingListLiveData;
    }

    public LiveData<List<PLVPlaybackCacheVideoVO>> getDownloadedListLiveData() {
        return downloadedListLiveData;
    }

    public LiveData<Event<PLVPlaybackCacheVideoVO>> getOnRequestLaunchDownloadedPlaybackLiveData() {
        return onRequestLaunchDownloadedPlaybackLiveData;
    }

    // </editor-fold>

}
