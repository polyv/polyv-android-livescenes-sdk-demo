package com.easefun.polyv.livecommon.module.modules.player.live.presenter.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;

/**
 * 直播播放器数据，主要用于提供给 非mvp的v 监听/获取播放器的数据
 */
public class PLVLivePlayerData {
    //当前播放线路索引
    private MutableLiveData<Integer> linesPos = new MutableLiveData<>();

    //播放器状态
    private MutableLiveData<PLVPlayerState> playerState = new MutableLiveData<>();

    //PPT状态
    private MutableLiveData<Boolean> pptShowState = new MutableLiveData<>();

    //讲师连麦状态 <连麦是否打开, 是否是音频连麦>
    private MutableLiveData<Pair<Boolean, Boolean>> linkMicOpen = new MutableLiveData<>();

    //播放信息，每隔一秒回调一次
    private MutableLiveData<PLVPlayInfoVO> playInfoVO = new MutableLiveData<>();

    //sei数据
    private MutableLiveData<Long> seiData = new MutableLiveData<>();

    //投屏初始化状态
    private MutableLiveData<Boolean> castOpen = new MutableLiveData<>();

    // <editor-fold defaultstate="collapsed" desc="播放信息">
    public void postPlayInfoVO(PLVPlayInfoVO playInfo) {
        playInfoVO.postValue(playInfo);
    }

    public LiveData<PLVPlayInfoVO> getPlayInfoVO() {
        return playInfoVO;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器状态">
    public void postPrepared() {
        playerState.postValue(PLVPlayerState.PREPARED);
    }

    public void postNoLive() {
        playerState.postValue(PLVPlayerState.NO_LIVE);
    }

    public void postLiveEnd() {
        playerState.postValue(PLVPlayerState.LIVE_END);
    }

    public void postLiveStop(){
        playerState.postValue(PLVPlayerState.LIVE_STOP);
    }

    public LiveData<PLVPlayerState> getPlayerState() {
        return playerState;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="线路索引">
    public void postLinesChange(int linesPos) {
        this.linesPos.postValue(linesPos);
    }

    public LiveData<Integer> getLinesPos() {
        return linesPos;
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

    // <editor-fold defaultstate="collapsed" desc="讲师是否开启连麦">

    /**
     * 获取连麦状态
     *
     * @return Pair <连麦是否打开, 是否是音频连麦>
     */
    public LiveData<Pair<Boolean, Boolean>> getLinkMicState() {
        return linkMicOpen;
    }

    public void postLinkMicOpen(boolean isLinkMicOpen, boolean isAudio) {
        linkMicOpen.postValue(new Pair<Boolean, Boolean>(isLinkMicOpen, isAudio));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="sei数据">
    public void postSeiData(long data) {
        seiData.postValue(data);
    }

    public LiveData<Long> getSeiData() {
        return seiData;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="投屏是否开启">

    /**
     * 获取投屏初始化是否成功
     * @return
     */
    public LiveData<Boolean> getCastInitState(){
        return castOpen;
    }

    public void postCastInitData(boolean initResult){
        castOpen.postValue(initResult);
    }
    // </editor-fold >
}
