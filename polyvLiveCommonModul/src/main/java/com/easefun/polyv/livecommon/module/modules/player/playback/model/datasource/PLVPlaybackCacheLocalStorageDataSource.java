package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.livescenes.download.IPLVDownloader;
import com.plv.livescenes.download.IPLVDownloaderListener;
import com.plv.livescenes.download.PLVDownloader;
import com.plv.livescenes.download.PLVDownloaderManager;
import com.plv.livescenes.download.PLVPlaybackCacheVO;
import com.plv.livescenes.download.listener.IPLVDownloaderBeforeStartListener;
import com.plv.livescenes.download.listener.IPLVDownloaderStopListener;
import com.plv.thirdpart.blankj.utilcode.util.FileUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheLocalStorageDataSource {

    // <editor-fold defaultstate="collapsed" desc="单例 - 构造方法">

    private static volatile PLVPlaybackCacheLocalStorageDataSource INSTANCE;

    public static PLVPlaybackCacheLocalStorageDataSource getInstance(
            final PLVDownloaderManager downloaderManager,
            final PLVPlaybackCacheConfig playbackCacheConfig
    ) {
        if (INSTANCE == null) {
            synchronized (PLVPlaybackCacheLocalStorageDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVPlaybackCacheLocalStorageDataSource(downloaderManager);
                }
            }
        }
        INSTANCE.playbackCacheConfig = playbackCacheConfig;
        return INSTANCE;
    }

    private PLVPlaybackCacheLocalStorageDataSource(
            final PLVDownloaderManager downloaderManager
    ) {
        this.downloaderManager = downloaderManager;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVPlaybackCacheLocalStorageDataSource.class.getSimpleName();

    private final PLVDownloaderManager downloaderManager;
    private PLVPlaybackCacheConfig playbackCacheConfig;

    private final Map<String, PLVPlaybackCacheVideoVO> downloaderKeyToVideoMap = new ConcurrentHashMap<>();

    private Emitter<PLVPlaybackCacheVideoVO> playbackCacheUpdateEmitter;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外数据源 - 数据更新事件">

    public final Observable<PLVPlaybackCacheVideoVO> playbackCacheUpdateObservable = Observable.create(new ObservableOnSubscribe<PLVPlaybackCacheVideoVO>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<PLVPlaybackCacheVideoVO> emitter) throws Exception {
            playbackCacheUpdateEmitter = emitter;
        }
    });

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void startDownloadVideo(@NonNull final PLVPlaybackCacheVideoVO videoVO) {
        final PLVPlaybackCacheVideoVO localVideoVO = videoVO.copy();
        final PLVDownloader downloader = getDownloader(localVideoVO);
        downloaderKeyToVideoMap.put(downloader.getKey(), localVideoVO);
        setupDownloaderListener(downloader);

        localVideoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.WAITING);
        notifyPlaybackCacheUpdate(localVideoVO.copy());

        downloaderManager.startDownload(downloader);
    }

    public void pauseDownloadVideo(@NonNull final PLVPlaybackCacheVideoVO videoVO) {
        final PLVDownloader downloader = getDownloader(videoVO);
        if (downloader == null) {
            return;
        }
        downloaderManager.removeDownloader(downloader.getKey());

        final PLVPlaybackCacheVideoVO localVideoVO = downloaderKeyToVideoMap.get(downloader.getKey());
        if (localVideoVO != null) {
            localVideoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.PAUSING);
            notifyPlaybackCacheUpdate(localVideoVO.copy());
        }
    }

    public void deleteDownloadVideo(@NonNull final PLVPlaybackCacheVideoVO videoVO) {
        final PLVDownloader downloader = getDownloader(videoVO);
        if (downloader == null) {
            return;
        }
        downloaderManager.removeDownloader(downloader.getKey());
        downloaderKeyToVideoMap.remove(downloader.getKey());
        downloader.deleteDownloadContent();
    }

    public boolean checkDownloadedVideoExist(@NonNull final PLVPlaybackCacheVideoVO vo) {
        final String videoPath = vo.getVideoPath();
        if (videoPath != null && !FileUtils.isFileExists(videoPath)) {
            return false;
        }
        final String pptPath = vo.getPptPath();
        if (pptPath != null && !FileUtils.isFileExists(pptPath)) {
            return false;
        }
        final String jsPath = vo.getJsPath();
        if (jsPath != null && !FileUtils.isFileExists(jsPath)) {
            return false;
        }
        return true;
    }

    public boolean isDownloading(@NonNull final PLVPlaybackCacheVideoVO vo) {
        return getDownloader(vo).isDownloading();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private PLVDownloader getDownloader(@NonNull final PLVPlaybackCacheVideoVO videoVO) {
        return downloaderManager.addDownloader(
                new IPLVDownloader.Builder(videoVO.getVideoPoolId(), videoVO.getViewerInfoVO().getChannelId())
                        .setPlaybackListType(videoVO.getViewerInfoVO().getPlaybackListType())
                        .downloadDir(playbackCacheConfig.getDownloadRootDirectory())
        );
    }

    private void setupDownloaderListener(final PLVDownloader downloader) {
        downloader.setDownloadBeforeStartListener(new IPLVDownloaderBeforeStartListener<PLVPlaybackCacheVO>() {
            @Override
            public void onBeforeStart(IPLVDownloader ignored, final PLVPlaybackCacheVO playbackCacheVO) {
                PLVPlaybackCacheVideoVO videoVO = downloaderKeyToVideoMap.get(downloader.getKey());
                fillDataToVideoVO(videoVO, playbackCacheVO);
                if (videoVO != null) {
                    videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING);
                    notifyPlaybackCacheUpdate(videoVO.copy());
                }
            }
        });
        downloader.setDownloadListener(new IPLVDownloaderListener() {
            @Override
            public void onProgress(long current, long total) {
                PLVPlaybackCacheVideoVO videoVO = downloaderKeyToVideoMap.get(downloader.getKey());
                if (videoVO != null) {
                    videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.DOWNLOADING);
                    final float progress = (float) current / total;
                    videoVO.setProgress((int) (progress * 100));
                    if (videoVO.getTotalBytes() != null) {
                        videoVO.setDownloadedBytes((long) (videoVO.getTotalBytes() * progress));
                    }
                    notifyPlaybackCacheUpdate(videoVO.copy());
                }
            }

            @Override
            public void onSuccess(PLVPlaybackCacheVO playbackCacheVO) {
                PLVPlaybackCacheVideoVO videoVO = downloaderKeyToVideoMap.get(downloader.getKey());
                if (videoVO != null) {
                    fillDataToVideoVO(videoVO, playbackCacheVO);
                    videoVO.setProgress(100);
                    videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.DOWNLOADED);
                    notifyPlaybackCacheUpdate(videoVO.copy());
                }
            }

            @Override
            public void onFailure(int errorReason) {
                PLVCommonLog.e(TAG, "downloader onFailure errorReason = " + errorReason);

                PLVPlaybackCacheVideoVO videoVO = downloaderKeyToVideoMap.get(downloader.getKey());
                if (videoVO != null) {
                    videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.DOWNLOAD_FAIL);
                    notifyPlaybackCacheUpdate(videoVO.copy());
                }
            }
        });
        downloader.setDownloadStopListener(new IPLVDownloaderStopListener() {
            @Override
            public void onStop() {
                PLVPlaybackCacheVideoVO videoVO = downloaderKeyToVideoMap.get(downloader.getKey());
                if (videoVO != null) {
                    videoVO.setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum.PAUSING);
                    notifyPlaybackCacheUpdate(videoVO.copy());
                }
            }
        });
    }

    private void notifyPlaybackCacheUpdate(PLVPlaybackCacheVideoVO videoVO) {
        if (playbackCacheUpdateEmitter != null) {
            playbackCacheUpdateEmitter.onNext(videoVO);
        }
    }

    private static void fillDataToVideoVO(@Nullable final PLVPlaybackCacheVideoVO videoVO, @Nullable final PLVPlaybackCacheVO playbackCacheVO) {
        if (videoVO == null || playbackCacheVO == null) {
            return;
        }
        final String videoPath = playbackCacheVO.getVideoPath();
        final String pptPath = playbackCacheVO.getPptDir();
        final String jsPath = playbackCacheVO.getJsPath();
        final String title = playbackCacheVO.getTitle();
        final String duration = playbackCacheVO.getDuration();
        final Long totalBytes = playbackCacheVO.getVideoSize();
        final String firstImageUrl = playbackCacheVO.getFirstImage();

        if (videoPath != null) {
            videoVO.setVideoPath(videoPath);
        }
        if (pptPath != null) {
            videoVO.setPptPath(pptPath);
        }
        if (jsPath != null) {
            videoVO.setJsPath(jsPath);
        }
        if (title != null) {
            videoVO.setTitle(title);
        }
        if (duration != null) {
            videoVO.setVideoDuration(duration);
        }
        if (totalBytes != null) {
            videoVO.setTotalBytes(totalBytes);
        }
        if (firstImageUrl != null) {
            videoVO.setFirstImageUrl(firstImageUrl);
        }
    }

    // </editor-fold>

}
