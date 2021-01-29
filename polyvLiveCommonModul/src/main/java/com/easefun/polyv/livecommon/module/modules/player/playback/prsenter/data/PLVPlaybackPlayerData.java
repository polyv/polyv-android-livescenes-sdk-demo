package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;

/**
 * 回放播放器数据，主要用于提供给 非mvp的v 监听/获取播放器的数据
 */
public class PLVPlaybackPlayerData {
    //播放器状态
    private MutableLiveData<PLVPlayerState> playerState = new MutableLiveData<>();

    //PPT状态
    private MutableLiveData<Boolean> pptShowState = new MutableLiveData<>();

    //播放信息，每隔一秒回调一次
    private MutableLiveData<PLVPlayInfoVO> playInfoVO = new MutableLiveData<>();

    // <editor-fold defaultstate="collapsed" desc="播放状态">
    public void postPrepared() {
        playerState.postValue(PLVPlayerState.PREPARED);
    }

    public LiveData<PLVPlayerState> getPlayerState() {
        return playerState;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PPT显示状态">
    public void postPPTShowState(boolean visible) {
        pptShowState.postValue(visible);
    }

    public LiveData<Boolean> getPPTShowState() {
        return pptShowState;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放信息">
    public void postPlayInfoVO(PLVPlayInfoVO playInfo) {
        playInfoVO.postValue(playInfo);
    }

    public LiveData<PLVPlayInfoVO> getPlayInfoVO() {
        return playInfoVO;
    }
    // </editor-fold>
}
