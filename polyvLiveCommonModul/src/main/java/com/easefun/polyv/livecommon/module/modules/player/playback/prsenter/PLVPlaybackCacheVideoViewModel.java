package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.PLVPlaybackCacheRepo;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheViewerInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.livescenes.playback.vo.PLVPlaybackDataVO;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheVideoViewModel {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVPlaybackCacheRepo playbackCacheRepo;
    private final PLVPlaybackCacheVideoConfig playbackCacheVideoConfig;

    private final MutableLiveData<PLVPlaybackCacheVideoVO> playbackCacheUpdateLiveData = new MutableLiveData<>();

    private Disposable playbackCacheUpdateDisposable;
    private Disposable playbackCacheInitDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVPlaybackCacheVideoViewModel(
            final PLVPlaybackCacheRepo playbackCacheRepo,
            final PLVPlaybackCacheVideoConfig playbackCacheVideoConfig
    ) {
        this.playbackCacheRepo = playbackCacheRepo;
        this.playbackCacheVideoConfig = playbackCacheVideoConfig;
        init();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void init() {
        observePlaybackCacheUpdate();

        initPlaybackCache();
    }

    private void observePlaybackCacheUpdate() {
        if (playbackCacheUpdateDisposable != null) {
            playbackCacheUpdateDisposable.dispose();
        }
        playbackCacheUpdateDisposable = playbackCacheRepo.playbackCacheVideoUpdateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .retry()
                .subscribe(new Consumer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void accept(PLVPlaybackCacheVideoVO videoVO) throws Exception {
                        if (videoVO == null || videoVO.getVideoPoolId() == null || !videoVO.getVideoPoolId().equals(playbackCacheVideoConfig.getVideoPoolId())) {
                            return;
                        }
                        playbackCacheUpdateLiveData.postValue(videoVO);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void initPlaybackCache() {
        if (playbackCacheInitDisposable != null) {
            playbackCacheInitDisposable.dispose();
        }
        playbackCacheInitDisposable = playbackCacheRepo.getCacheVideoById(playbackCacheVideoConfig.getChannelId(), playbackCacheVideoConfig.getVideoPoolId(), playbackCacheVideoConfig.getPlaybackListType())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<PLVPlaybackCacheVideoVO>() {
                    @Override
                    public void accept(PLVPlaybackCacheVideoVO videoVO) throws Exception {
                        if (videoVO == null || videoVO.getVideoPoolId() == null || !videoVO.getVideoPoolId().equals(playbackCacheVideoConfig.getVideoPoolId())) {
                            return;
                        }
                        playbackCacheUpdateLiveData.postValue(videoVO);
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
        playbackCacheRepo.startDownloadVideo(appendViewerInfo(vo));
    }

    public void pauseDownload(PLVPlaybackCacheVideoVO vo) {
        playbackCacheRepo.pauseDownloadVideo(appendViewerInfo(vo));
    }

    public void deleteDownload(PLVPlaybackCacheVideoVO vo) {
        playbackCacheRepo.deleteDownloadVideo(appendViewerInfo(vo));
    }

    public void updatePlaybackVideoInfo(PLVPlaybackDataVO playbackDataVO) {
        playbackCacheVideoConfig.setPlaybackListType(playbackDataVO.getPlaybackListType());
        if (playbackDataVO.getPlaybackListType() == PLVPlaybackListType.TEMP_STORE) {
            playbackCacheVideoConfig.setVideoPoolId(playbackDataVO.getFileId());
        } else {
            playbackCacheVideoConfig.setVideoPoolIdByVid(playbackDataVO.getVideoPoolId());
        }
        initPlaybackCache();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外数据源">

    public LiveData<PLVPlaybackCacheVideoVO> getPlaybackCacheUpdateLiveData() {
        return playbackCacheUpdateLiveData;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private PLVPlaybackCacheVideoVO appendViewerInfo(PLVPlaybackCacheVideoVO vo) {
        final PLVPlaybackCacheViewerInfoVO viewerInfoVO = vo.getViewerInfoVO();
        viewerInfoVO.setChannelId(playbackCacheVideoConfig.getChannelId());
        viewerInfoVO.setVid(playbackCacheVideoConfig.getVid());
        viewerInfoVO.setViewerId(playbackCacheVideoConfig.getViewerId());
        viewerInfoVO.setViewerName(playbackCacheVideoConfig.getViewerName());
        viewerInfoVO.setViewerAvatar(playbackCacheVideoConfig.getViewerAvatar());
        viewerInfoVO.setChannelType(playbackCacheVideoConfig.getChannelType());
        viewerInfoVO.setPlaybackListType(playbackCacheVideoConfig.getPlaybackListType());
        return vo;
    }

    // </editor-fold>

}
