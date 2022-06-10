package com.easefun.polyv.livecommon.module.modules.player.playback.model;

import android.Manifest;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheDatabaseDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheLocalStorageDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheMemoryDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.PLVPlaybackCacheNetworkDataSource;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.playback.video.PLVPlaybackListType;

import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheRepo {

    // <editor-fold defaultstate="collapsed" desc="单例 - 构造方法">

    private static volatile PLVPlaybackCacheRepo INSTANCE;

    private PLVPlaybackCacheRepo(
            final PLVPlaybackCacheDatabaseDataSource databaseDataSource,
            final PLVPlaybackCacheLocalStorageDataSource localStorageDataSource
    ) {
        this.databaseDataSource = databaseDataSource;
        this.localStorageDataSource = localStorageDataSource;
        init();
    }

    public static PLVPlaybackCacheRepo getInstance(
            final PLVPlaybackCacheDatabaseDataSource databaseDataSource,
            final PLVPlaybackCacheLocalStorageDataSource localStorageDataSource,
            final PLVPlaybackCacheMemoryDataSource memoryDataSource,
            final PLVPlaybackCacheNetworkDataSource networkDataSource
    ) {
        if (INSTANCE == null) {
            synchronized (PLVPlaybackCacheRepo.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVPlaybackCacheRepo(databaseDataSource, localStorageDataSource);
                }
            }
        }
        INSTANCE.memoryDataSource = memoryDataSource;
        INSTANCE.networkDataSource = networkDataSource;
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVPlaybackCacheRepo.class.getSimpleName();

    private final PLVPlaybackCacheDatabaseDataSource databaseDataSource;
    private final PLVPlaybackCacheLocalStorageDataSource localStorageDataSource;
    private PLVPlaybackCacheMemoryDataSource memoryDataSource;
    private PLVPlaybackCacheNetworkDataSource networkDataSource;

    private Emitter<PLVPlaybackCacheVideoVO> cacheVideoUpdateEmitter;

    private Disposable localStorageCacheUpdateDisposable;
    private Disposable localStorageValidateDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void init() {
        observeLocalStorageCacheUpdate();
        validateLocalStorageDownloadContent();
    }

    private void observeLocalStorageCacheUpdate() {
        if (localStorageCacheUpdateDisposable != null) {
            localStorageCacheUpdateDisposable.dispose();
        }
        localStorageCacheUpdateDisposable = localStorageDataSource.playbackCacheUpdateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .retry()
                .subscribe(new Consumer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void accept(PLVPlaybackCacheVideoVO videoVO) throws Exception {
                        databaseDataSource.updateCacheVideo(videoVO);
                        memoryDataSource.putCacheVideo(videoVO);
                        notifyCacheVideoUpdate(videoVO.copy());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void validateLocalStorageDownloadContent() {
        if (localStorageValidateDisposable != null) {
            localStorageValidateDisposable.dispose();
        }
        if (!hasPermission()) {
            return;
        }
        localStorageValidateDisposable = listCacheVideos()
                .observeOn(Schedulers.computation())
                .flatMap(new Function<List<PLVPlaybackCacheVideoVO>, ObservableSource<PLVPlaybackCacheVideoVO>>() {
                    @Override
                    public ObservableSource<PLVPlaybackCacheVideoVO> apply(@NonNull List<PLVPlaybackCacheVideoVO> vos) throws Exception {
                        return Observable.fromIterable(vos);
                    }
                })
                .subscribe(new Consumer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void accept(PLVPlaybackCacheVideoVO vo) throws Exception {
                        if (vo.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED
                                && !localStorageDataSource.checkDownloadedVideoExist(vo)) {
                            PLVCommonLog.w(TAG, "delete downloaded video because local content not exist, id: " + vo.getVideoPoolId());
                            deleteDownloadVideo(vo);
                        }
                        if (vo.getDownloadStatusEnum() == PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING
                                && !localStorageDataSource.isDownloading(vo)) {
                            PLVCommonLog.i(TAG, "pause downloading video because status not consistent, id: " + vo.getVideoPoolId());
                            pauseDownloadVideo(vo);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外数据源 - 数据更新事件">

    public final Observable<PLVPlaybackCacheVideoVO> playbackCacheVideoUpdateObservable = Observable.create(new ObservableOnSubscribe<PLVPlaybackCacheVideoVO>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVPlaybackCacheVideoVO> emitter) throws Exception {
            cacheVideoUpdateEmitter = emitter;
        }
    }).publish().autoConnect();

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public Observable<PLVPlaybackCacheVideoVO> getCacheVideoById(final String channelId, final String videoPoolId, final PLVPlaybackListType playbackListType) {
        return Observable.just(1)
                .observeOn(Schedulers.io())
                .flatMap(new Function<Integer, ObservableSource<? extends PLVPlaybackCacheVideoVO>>() {
                    @Override
                    public ObservableSource<? extends PLVPlaybackCacheVideoVO> apply(@NonNull Integer integer) throws Exception {
                        final PLVPlaybackCacheVideoVO memoryCacheVO = memoryDataSource.getCacheVideoById(videoPoolId);
                        if (memoryCacheVO != null) {
                            return Observable.just(memoryCacheVO);
                        }

                        final PLVPlaybackCacheVideoVO databaseCacheVO = databaseDataSource.getCacheVideoById(videoPoolId);
                        if (databaseCacheVO != null) {
                            memoryDataSource.putCacheVideo(databaseCacheVO);
                            if (databaseCacheVO.getDownloadStatusEnum() != PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST) {
                                return Observable.just(databaseCacheVO);
                            }
                        }

                        return networkDataSource.getPlaybackCacheVideoData(channelId, videoPoolId, playbackListType)
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Consumer<PLVPlaybackCacheVideoVO>() {
                                    @Override
                                    public void accept(PLVPlaybackCacheVideoVO videoVO) throws Exception {
                                        videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST);
                                        databaseDataSource.insertCacheVideo(videoVO);
                                        memoryDataSource.putCacheVideo(videoVO);
                                    }
                                });
                    }
                });
    }

    public Observable<List<PLVPlaybackCacheVideoVO>> listCacheVideos() {
        return databaseDataSource.listCacheVideos()
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<List<PLVPlaybackCacheVideoVO>>() {
                    @Override
                    public void accept(List<PLVPlaybackCacheVideoVO> playbackCacheVideoVOS) throws Exception {
                        if (playbackCacheVideoVOS != null) {
                            memoryDataSource.putCacheVideos(playbackCacheVideoVOS);
                        }
                    }
                });
    }

    public void startDownloadVideo(PLVPlaybackCacheVideoVO vo) {
        if (!hasPermission()) {
            return;
        }
        vo = vo.copy();
        vo.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.WAITING);

        localStorageDataSource.startDownloadVideo(vo);
        databaseDataSource.insertCacheVideo(vo);
        memoryDataSource.putCacheVideo(vo);

        notifyCacheVideoUpdate(vo);
    }

    public void pauseDownloadVideo(PLVPlaybackCacheVideoVO vo) {
        if (!hasPermission()) {
            return;
        }
        vo = vo.copy();
        vo.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.PAUSING);

        localStorageDataSource.pauseDownloadVideo(vo);
        databaseDataSource.updateCacheVideo(vo);
        memoryDataSource.putCacheVideo(vo);

        notifyCacheVideoUpdate(vo);
    }

    public void deleteDownloadVideo(PLVPlaybackCacheVideoVO vo) {
        if (!hasPermission()) {
            return;
        }
        vo = vo.copy();
        vo.clearDownloadStatus();

        localStorageDataSource.deleteDownloadVideo(vo);
        databaseDataSource.deleteCacheVideo(vo);
        memoryDataSource.removeCacheVideo(vo);

        notifyCacheVideoUpdate(vo);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private void notifyCacheVideoUpdate(PLVPlaybackCacheVideoVO vo) {
        if (cacheVideoUpdateEmitter != null) {
            cacheVideoUpdateEmitter.onNext(vo);
        }
    }

    private boolean hasPermission() {
        return PLVFastPermission.hasPermission(PLVAppUtils.getApp(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    // </editor-fold>

}
