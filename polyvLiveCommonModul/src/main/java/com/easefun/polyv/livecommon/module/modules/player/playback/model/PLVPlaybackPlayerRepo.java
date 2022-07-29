package com.easefun.polyv.livecommon.module.modules.player.playback.model;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.plv.foundationsdk.component.livedata.PLVAutoSaveLiveData;
import com.plv.livescenes.playback.vo.PLVPlaybackDataVO;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackPlayerRepo {

    private final MutableLiveData<Map<String, PLVPlayInfoVO>> playbackVideoProgressMap = new PLVAutoSaveLiveData<Map<String, PLVPlayInfoVO>>("plv_playback_video_progress") {};

    public void updatePlaybackProgress(@NonNull final PLVPlaybackDataVO playbackDataVO, @Nullable final PLVPlayInfoVO playInfoVO) {
        final String key = createKeyByPlaybackData(playbackDataVO);
        final Map<String, PLVPlayInfoVO> map = getPlaybackProgressMap();
        map.put(key, playInfoVO);
        playbackVideoProgressMap.postValue(map);
    }

    @Nullable
    public PLVPlayInfoVO getPlaybackProgress(final PLVPlaybackDataVO playbackDataVO) {
        final String key = createKeyByPlaybackData(playbackDataVO);
        return getPlaybackProgressMap().get(key);
    }

    @NonNull
    private Map<String, PLVPlayInfoVO> getPlaybackProgressMap() {
        return getOrDefault(playbackVideoProgressMap.getValue(), new HashMap<String, PLVPlayInfoVO>());
    }

    private static String createKeyByPlaybackData(@NonNull final PLVPlaybackDataVO playbackDataVO) {
        return playbackDataVO.getPlaybackListType().name() + playbackDataVO.getVideoPoolId();
    }

}
