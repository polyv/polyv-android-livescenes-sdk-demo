package com.easefun.polyv.livecommon.module.modules.streamer.presenter.data;

import static net.polyv.android.common.libs.lang.state.MutableStateKt.mutableStateOf;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;

import net.polyv.android.common.libs.lang.state.MutableState;
import net.polyv.android.common.libs.lang.state.State;

/**
 * 推流和连麦的数据，主要用于提供给 非mvp的v 监听/获取推流和连麦的数据
 */
public class PLVStreamerData {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //推流状态
    private MutableLiveData<Boolean> streamerStatus = new MutableLiveData<>();

    //推流网络变化状态
    private MutableLiveData<PLVLinkMicConstant.NetworkQuality> networkQuality = new MutableLiveData<>();
    private MutableLiveData<PLVNetworkStatusVO> networkStatus = new MutableLiveData<>();

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

    //是否前置摄像头镜像
    private MutableLiveData<Boolean> isFrontMirrorMode = new MutableLiveData<>();

    //当前设置的码率
    private MutableLiveData<Integer> curBitrate = new MutableLiveData<>();
    // 推流降级策略
    private MutableLiveData<PLVPushDowngradePreference> downgradePreferenceLiveData = new MutableLiveData<>();

    // 推流画面比例
    private MutableLiveData<PLVLinkMicConstant.PushResolutionRatio> pushResolutionRatioLiveData = new MutableLiveData<>();

    //当前连麦人数
    private MutableLiveData<Integer> curLinkMicCount = new MutableLiveData<>();

    private MutableLiveData<Boolean> enableShareScreen = new MutableLiveData<>();

    private MutableLiveData<Boolean> videoLinkMicType = new MutableLiveData<>();
    private MutableLiveData<PLVLinkMicDenoiseType> denoiseType = new MutableLiveData<>();
    private MutableLiveData<Boolean> useExternalAudioInput = new MutableLiveData<>();

    private MutableState<Integer> localAudioCaptureVolume = mutableStateOf(100);
    private MutableState<Integer> mediaOverlayRemoteVolume = mutableStateOf(100);
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

    public void postNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
        networkQuality.postValue(quality);
    }

    public LiveData<PLVLinkMicConstant.NetworkQuality> getNetworkQuality() {
        return networkQuality;
    }

    public void postNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
        networkStatus.postValue(networkStatusVO);
    }

    public LiveData<PLVNetworkStatusVO> getNetworkStatus() {
        return networkStatus;
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

    // <editor-fold defaultstate="collapsed" desc="设置的码率">
    public void postCurBitrate(int bitrate) {
        curBitrate.postValue(bitrate);
    }

    public LiveData<Integer> getCurBitrate() {
        return curBitrate;
    }

    public void postDowngradePreference(PLVPushDowngradePreference downgradePreference) {
        downgradePreferenceLiveData.postValue(downgradePreference);
    }

    public LiveData<PLVPushDowngradePreference> getDowngradePreferenceLiveData() {
        return downgradePreferenceLiveData;
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

    public void postIsFrontMirrorMode(boolean isMirror) {
        isFrontMirrorMode.postValue(isMirror);
    }

    public LiveData<Boolean> getIsFrontMirrorMode() {
        return isFrontMirrorMode;
    }


    public void postEnableShareScreen(boolean isStartShareScreen){
        enableShareScreen.postValue(isStartShareScreen);
    }

    public  LiveData<Boolean> getIsStartShareScreen() {
        return enableShareScreen;
    }

    public void postDenoiseType(PLVLinkMicDenoiseType denoiseType) {
        this.denoiseType.postValue(denoiseType);
    }

    public LiveData<PLVLinkMicDenoiseType> getDenoiseType() {
        return denoiseType;
    }

    public void postUseExternalAudioInput(boolean isUseExternalAudioInput) {
        useExternalAudioInput.postValue(isUseExternalAudioInput);
    }

    public LiveData<Boolean> getUseExternalAudioInput() {
        return useExternalAudioInput;
    }

    public State<Integer> getLocalAudioCaptureVolume() {
        return localAudioCaptureVolume;
    }

    public void postLocalAudioCaptureVolume(int volume) {
        localAudioCaptureVolume.setValue(volume);
    }

    public State<Integer> getMediaOverlayRemoteVolume() {
        return mediaOverlayRemoteVolume;
    }

    public void postMediaOverlayRemoteVolume(int volume) {
        mediaOverlayRemoteVolume.setValue(volume);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦控制">
    public void postLinkMicCount(int count) {
        curLinkMicCount.postValue(count);
    }

    public LiveData<Integer> getLinkMicCount() {
        return curLinkMicCount;
    }

    public void postVideoLinkMicType(boolean isVideoLinkMicType) {
        videoLinkMicType.postValue(isVideoLinkMicType);
    }

    public LiveData<Boolean> getVideoLinkMicType() {
        return videoLinkMicType;
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="推流画面比例">

    public void postPushResolutionRatio(PLVLinkMicConstant.PushResolutionRatio pushResolutionRatio) {
        pushResolutionRatioLiveData.postValue(pushResolutionRatio);
    }

    public LiveData<PLVLinkMicConstant.PushResolutionRatio> getPushResolutionRatio() {
        return pushResolutionRatioLiveData;
    }

    // </editor-fold>
}
