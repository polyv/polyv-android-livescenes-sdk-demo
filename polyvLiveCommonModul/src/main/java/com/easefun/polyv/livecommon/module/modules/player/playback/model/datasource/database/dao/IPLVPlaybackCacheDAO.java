package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
