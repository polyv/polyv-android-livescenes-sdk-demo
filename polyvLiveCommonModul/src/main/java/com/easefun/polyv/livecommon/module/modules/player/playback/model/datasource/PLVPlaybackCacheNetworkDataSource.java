package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.plv.livescenes.download.PLVDownloader;
import com.plv.livescenes.download.api.PLVPlaybackDownloadApiManager;
import com.plv.livescenes.model.PLVPlaybackVideoVO;
import com.plv.livescenes.model.PLVTempStorePlaybackVideoVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;

import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonRequestResultVOKt;
import net.polyv.android.player.business.scene.common.model.api.vo.PLVVodVideoJsonVO;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheNetworkDataSource {

    public PLVPlaybackCacheNetworkDataSource(
            final PLVPlaybackDownloadApiManager downloadApiManager
    ) {
        this.downloadApiManager = downloadApiManager;
    }

    private final PLVPlaybackDownloadApiManager downloadApiManager;

    public Observable<PLVPlaybackCacheVideoVO> getPlaybackCacheVideoData(final String channelId, final String videoId, final PLVPlaybackListType playbackListType) {
        if (playbackListType == PLVPlaybackListType.TEMP_STORE) {
            return getTempStoreVideoData(channelId, videoId);
        } else if (playbackListType == PLVPlaybackListType.PLAYBACK) {
            return getPlaybackVideoData(channelId, videoId);
        } else {
            return getVodVideoData(channelId, videoId);
        }
    }

    private Observable<PLVPlaybackCacheVideoVO> getTempStoreVideoData(final String channelId, final String fileId) {
        return downloadApiManager.requestTempStorePlaybackVideoDetail(channelId, fileId)
                .subscribeOn(Schedulers.io())
                .map(new Function<PLVTempStorePlaybackVideoVO, PLVPlaybackCacheVideoVO>() {
                    @Override
                    public PLVPlaybackCacheVideoVO apply(@NonNull PLVTempStorePlaybackVideoVO playbackVideoVO) throws Exception {
                        final PLVPlaybackCacheVideoVO cacheVideoVO = new PLVPlaybackCacheVideoVO();
                        cacheVideoVO.setVideoPoolId(playbackVideoVO.getData().getFileId());
                        cacheVideoVO.setVideoId(playbackVideoVO.getData().getFileId());
                        cacheVideoVO.setTitle(playbackVideoVO.getData().getFilename());
                        cacheVideoVO.setVideoDuration(playbackVideoVO.getData().getDuration());
                        cacheVideoVO.setLiveType(playbackVideoVO.getData().getLiveType());
                        cacheVideoVO.setChannelSessionId(playbackVideoVO.getData().getChannelSessionId());
                        cacheVideoVO.setOriginSessionId(playbackVideoVO.getData().getOriginSessionId());
                        cacheVideoVO.setTotalBytes(PLVDownloader.getVideoDownloadSize(playbackVideoVO));
                        cacheVideoVO.setEnableDownload(
                                playbackVideoVO.getData().isPlaybackCacheEnable()
                        );
                        return cacheVideoVO;
                    }
                });
    }

    private Observable<PLVPlaybackCacheVideoVO> getPlaybackVideoData(final String channelId, final String videoId) {
        return downloadApiManager.requestPlaybackVideoDetail(channelId, videoId)
                .subscribeOn(Schedulers.io())
                .map(new Function<PLVPlaybackVideoVO, PLVPlaybackCacheVideoVO>() {
                    @Override
                    public PLVPlaybackCacheVideoVO apply(@NonNull PLVPlaybackVideoVO playbackVideoVO) throws Exception {
                        final PLVPlaybackCacheVideoVO cacheVideoVO = new PLVPlaybackCacheVideoVO();
                        cacheVideoVO.setVideoPoolId(playbackVideoVO.getData().getVideoPoolId());
                        cacheVideoVO.setVideoId(playbackVideoVO.getData().getVideoId());
                        cacheVideoVO.setTitle(playbackVideoVO.getData().getTitle());
                        cacheVideoVO.setFirstImageUrl(playbackVideoVO.getData().getFirstImage());
                        cacheVideoVO.setVideoDuration(playbackVideoVO.getData().getDuration());
                        cacheVideoVO.setLiveType(playbackVideoVO.getData().getLiveType());
                        cacheVideoVO.setChannelSessionId(playbackVideoVO.getData().getChannelSessionId());
                        cacheVideoVO.setOriginSessionId(playbackVideoVO.getData().getOriginSessionId());
                        cacheVideoVO.setTotalBytes(PLVDownloader.getVideoDownloadSize(playbackVideoVO));
                        cacheVideoVO.setEnableDownload(
                                playbackVideoVO.getData().isPlaybackCacheEnable()
                        );
                        return cacheVideoVO;
                    }
                });
    }

    private Observable<PLVPlaybackCacheVideoVO> getVodVideoData(final String channelId, final String videoId) {
        return Observable.zip(
                downloadApiManager.requestPlaybackVideoDetail(channelId, videoId),
                downloadApiManager.requestVodVideoJson(videoId),
                new BiFunction<PLVPlaybackVideoVO, PLVVodVideoJsonVO, PLVPlaybackCacheVideoVO>() {
                    @NonNull
                    @Override
                    public PLVPlaybackCacheVideoVO apply(@NonNull PLVPlaybackVideoVO playbackVideoVO, @NonNull PLVVodVideoJsonVO vodVideoJsonVO) throws Exception {
                        final PLVPlaybackCacheVideoVO cacheVideoVO = new PLVPlaybackCacheVideoVO();
                        final boolean playbackDataValid = playbackVideoVO.getDataObj() instanceof PLVPlaybackVideoVO.Data && playbackVideoVO.getData().getVideoCache() != null;
                        if (playbackDataValid) {
                            cacheVideoVO.setVideoPoolId(playbackVideoVO.getData().getVideoPoolId());
                            cacheVideoVO.setVideoId(playbackVideoVO.getData().getVideoId());
                            cacheVideoVO.setTitle(playbackVideoVO.getData().getTitle());
                            cacheVideoVO.setFirstImageUrl(playbackVideoVO.getData().getFirstImage());
                            cacheVideoVO.setVideoDuration(playbackVideoVO.getData().getDuration());
                            cacheVideoVO.setLiveType(playbackVideoVO.getData().getLiveType());
                            cacheVideoVO.setChannelSessionId(playbackVideoVO.getData().getChannelSessionId());
                            cacheVideoVO.setOriginSessionId(playbackVideoVO.getData().getOriginSessionId());
                            cacheVideoVO.setTotalBytes(PLVDownloader.getVideoDownloadSize(playbackVideoVO));
                            cacheVideoVO.setEnableDownload(
                                    playbackVideoVO.getData().isPlaybackCacheEnable()
                            );
                        } else {
                            cacheVideoVO.setVideoPoolId(videoId);
                            cacheVideoVO.setTitle(vodVideoJsonVO.getTitle());
                            cacheVideoVO.setFirstImageUrl(vodVideoJsonVO.getFirst_image());
                            cacheVideoVO.setVideoDuration(vodVideoJsonVO.getDuration());
                            if (vodVideoJsonVO.getTsfilesize() != null && !vodVideoJsonVO.getTsfilesize().isEmpty()) {
                                cacheVideoVO.setTotalBytes(vodVideoJsonVO.getTsfilesize().get(0));
                            }
                            cacheVideoVO.setEnableDownload(PLVVodVideoJsonRequestResultVOKt.isVrmEncrypted(vodVideoJsonVO));
                        }
                        return cacheVideoVO;
                    }
                }
        ).subscribeOn(Schedulers.io());
    }

}
