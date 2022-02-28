package com.easefun.polyv.livecommon.module.modules.previous.presenter.data;

import android.arch.lifecycle.MutableLiveData;

import com.plv.livescenes.model.PLVPlaybackListVO;

import java.util.List;

/**
 * 往期回放视频的详细数据
 * 记录当前视频的vid，进度，频道信息
 */
public class PLVPreviousData {

    // <editor-fold defaultstate="collapsed" desc="变量">

    //当前频道的详细信息
    private PLVPlaybackListVO.DataBean.ContentsBean previousDetail;

    //播放回放视频的Vid
    private MutableLiveData<String> playbackVideoVidData = new MutableLiveData<>();

    //播放中回放视频跳转的进度
    private MutableLiveData<Integer> playBackVidoSeekData = new MutableLiveData<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 回放视频数据获取、设置">
    public MutableLiveData<String> getPlaybackVideoVidData() {
        return playbackVideoVidData;
    }

    public void setPlaybackVideoVidData(MutableLiveData<String> playbackVideoVidData) {
        this.playbackVideoVidData = playbackVideoVidData;
    }

    public MutableLiveData<Integer> getPlayBackVidoSeekData() {
        return playBackVidoSeekData;
    }

    public void setPlayBackVidoSeekData(MutableLiveData<Integer> playBackVidoSeekData) {
        this.playBackVidoSeekData = playBackVidoSeekData;
    }

    /**
     * 更新当前播放的往期的信息
     * @param mPlaybackList 当前回放列表
     * @param vid 当前播放视频的vid
     */
    public void update(List<PLVPlaybackListVO.DataBean.ContentsBean> mPlaybackList, String vid) {
        for(PLVPlaybackListVO.DataBean.ContentsBean data : mPlaybackList){
            if(data.getVideoPoolId().equals(vid)){
                previousDetail = data;
            }
        }
    }

    public PLVPlaybackListVO.DataBean.ContentsBean getPreviousDetail(){
        return previousDetail;
    }

    // </editor-fold>
}
