package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @author Hoshiiro
 */
@Dao
public interface IPLVPlaybackCacheDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO);

    @Delete
    void deletePlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO);

    @Update
    void updatePlaybackCache(PLVPlaybackCacheVideoVO cacheVideoVO);

    @Query("SELECT * FROM playback_cache_video_table WHERE videoPoolId = :id LIMIT 1")
    PLVPlaybackCacheVideoVO getPlaybackCacheVideo(String id);

    @Query("SELECT * FROM playback_cache_video_table")
    Flowable<List<PLVPlaybackCacheVideoVO>> listPlaybackCacheVideos();

}
