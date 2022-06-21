package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.PLVPlaybackCacheDatabase;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheDatabaseDataSource {

    // <editor-fold defaultstate="collapsed" desc="单例 - 构造方法">

    private static volatile PLVPlaybackCacheDatabaseDataSource INSTANCE;

    private PLVPlaybackCacheDatabaseDataSource() {
    }

    public static PLVPlaybackCacheDatabaseDataSource getInstance(
            final PLVPlaybackCacheDatabase playbackCacheDatabase
    ) {
        if (INSTANCE == null) {
            synchronized (PLVPlaybackCacheDatabaseDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PLVPlaybackCacheDatabaseDataSource();
                }
            }
        }
        INSTANCE.playbackCacheDatabase = playbackCacheDatabase;
        return INSTANCE;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">

    private PLVPlaybackCacheDatabase playbackCacheDatabase;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    @WorkerThread
    @Nullable
    public PLVPlaybackCacheVideoVO getCacheVideoById(String id) {
        return playbackCacheDatabase.getPlaybackCacheDAO().getPlaybackCacheVideo(id);
    }

    public Observable<List<PLVPlaybackCacheVideoVO>> listCacheVideos() {
        return playbackCacheDatabase.getPlaybackCacheDAO().listPlaybackCacheVideos().toObservable();
    }

    public void insertCacheVideo(final PLVPlaybackCacheVideoVO vo) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                playbackCacheDatabase.getPlaybackCacheDAO().insertPlaybackCache(vo);
            }
        });
    }

    public void updateCacheVideo(final PLVPlaybackCacheVideoVO vo) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                playbackCacheDatabase.getPlaybackCacheDAO().updatePlaybackCache(vo);
            }
        });
    }

    public void deleteCacheVideo(final PLVPlaybackCacheVideoVO vo) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                playbackCacheDatabase.getPlaybackCacheDAO().deletePlaybackCache(vo);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private static void runAsync(final Runnable runnable) {
        Schedulers.single().scheduleDirect(runnable);
    }

    // </editor-fold>

}
