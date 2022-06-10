package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.dao.IPLVPlaybackCacheDAO;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @author Hoshiiro
 */
public class PLVEmptyCacheDatabase extends PLVPlaybackCacheDatabase {

    @Override
    public IPLVPlaybackCacheDAO getPlaybackCacheDAO() {
        return new IPLVPlaybackCacheDAO() {
            @Override
            public void insertPlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO) {

            }

            @Override
            public void deletePlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO) {

            }

            @Override
            public void updatePlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO) {

            }

            @Override
            public PLVPlaybackCacheVideoVO getPlaybackCacheVideo(String id) {
                return null;
            }

            @Override
            public Flowable<List<PLVPlaybackCacheVideoVO>> listPlaybackCacheVideos() {
                return Flowable.empty();
            }
        };
    }

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration databaseConfiguration) {
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }

}
