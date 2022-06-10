package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheMemoryDataSource {

    private final Map<String, PLVPlaybackCacheVideoVO> playbackCacheVideoVOMap = new ConcurrentHashMap<>();

    public PLVPlaybackCacheVideoVO getCacheVideoById(String id) {
        return playbackCacheVideoVOMap.get(id);
    }

    public void putCacheVideo(PLVPlaybackCacheVideoVO vo) {
        playbackCacheVideoVOMap.put(vo.getVideoPoolId(), vo);
    }

    public void removeCacheVideo(PLVPlaybackCacheVideoVO vo) {
        playbackCacheVideoVOMap.remove(vo.getVideoPoolId());
    }

    public void putCacheVideos(@NonNull List<PLVPlaybackCacheVideoVO> vos) {
        for (PLVPlaybackCacheVideoVO vo : vos) {
            putCacheVideo(vo);
        }
    }

}
