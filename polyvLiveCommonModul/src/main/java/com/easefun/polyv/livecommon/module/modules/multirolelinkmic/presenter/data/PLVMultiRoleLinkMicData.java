package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.presenter.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

/**
 * 多角色连麦的数据，主要用于提供给 非mvp的v 监听/获取连麦的数据
 */
public class PLVMultiRoleLinkMicData {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //是否启用麦克风
    private MutableLiveData<Boolean> enableAudio = new MutableLiveData<>();

    //是否启用相机
    private MutableLiveData<Boolean> enableVideo = new MutableLiveData<>();

    //是否是前置相机
    private MutableLiveData<Boolean> isFrontCamera = new MutableLiveData<>();

    //最大限制连麦人数
    private MutableLiveData<Integer> limitLinkNumber = new MutableLiveData<>();
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

    // <editor-fold defaultstate="collapsed" desc="最大限制连麦人数">
    public void postLimitLinkNumber(int maxLinkNumber) {
        limitLinkNumber.postValue(maxLinkNumber);
    }

    public LiveData<Integer> getLimitLinkNumber() {
        return limitLinkNumber;
    }
    // </editor-fold>
}
