package com.easefun.polyv.livecommon.module.modules.streamer.presenter.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

/**
 * 推流和连麦的数据，主要用于提供给 非mvp的v 监听/获取推流和连麦的数据
 */
public class PLVStreamerData {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //推流状态
    private MutableLiveData<Boolean> streamerStatus = new MutableLiveData<>();

    //推流网络变化状态
    private MutableLiveData<Integer> networkQuality = new MutableLiveData<>();

    //推流时间
    private MutableLiveData<Integer> streamerTime = new MutableLiveData<>();

    //因断网延迟20s断流的状态
    private MutableLiveData<Boolean> showNetBroken = new MutableLiveData<>();

    //用户请求连麦
    private MutableLiveData<String> userRequestData = new MutableLiveData<>();

    //是否启用麦克风
    private MutableLiveData<Boolean> enableAudio = new MutableLiveData<>();

    //是否启用相机
    private MutableLiveData<Boolean> enableVideo = new MutableLiveData<>();

    //是否是前置相机
    private MutableLiveData<Boolean> isFrontCamera = new MutableLiveData<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流状态">
    public void postStreamerStatus(boolean isStarted) {
        streamerStatus.postValue(isStarted);
    }

    public LiveData<Boolean> getStreamerStatus() {
        return streamerStatus;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="网络变化状态">
    public void postNetworkQuality(int quality) {
        networkQuality.postValue(quality);
    }

    public LiveData<Integer> getNetworkQuality() {
        return networkQuality;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流时间">
    public void postStreamerTime(int duration) {
        streamerTime.postValue(duration);
    }

    public LiveData<Integer> getStreamerTime() {
        return streamerTime;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="因断网延迟20s断流的状态">
    public void postShowNetBroken() {
        showNetBroken.postValue(true);
    }

    public LiveData<Boolean> getShowNetBroken() {
        return showNetBroken;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户请求连麦">
    public void postUserRequestData(String uid) {
        userRequestData.postValue(uid);
    }

    public LiveData<String> getUserRequestData() {
        return userRequestData;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="媒体状态">
    public void postEnableAudio(boolean isEnableAudio) {
        enableAudio.postValue(isEnableAudio);
    }

    public LiveData<Boolean> getEnableAudio() {
        return enableAudio;
    }


    public void postEnableVideo(boolean isEnableVideo) {
        enableVideo.postValue(isEnableVideo);
    }

    public LiveData<Boolean> getEnableVideo() {
        return enableVideo;
    }


    public void postIsFrontCamera(boolean frontCamera) {
        isFrontCamera.postValue(frontCamera);
    }

    public LiveData<Boolean> getIsFrontCamera() {
        return isFrontCamera;
    }
    // </editor-fold>
}
