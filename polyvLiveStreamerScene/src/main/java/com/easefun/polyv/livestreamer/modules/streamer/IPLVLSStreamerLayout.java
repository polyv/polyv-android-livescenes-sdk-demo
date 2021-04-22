package com.easefun.polyv.livestreamer.modules.streamer;

import android.util.Pair;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;

/**
 * 推流和连麦布局的接口定义
 */
public interface IPLVLSStreamerLayout {

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 开始上课
     */
    void startClass();

    /**
     * 停止上课
     */
    void stopClass();

    /**
     * 设置推流码率
     *
     * @param bitrate 码率
     */
    void setBitrate(@PLVSStreamerConfig.BitrateType int bitrate);

    /**
     * 获取推流的码率信息
     *
     * @return 码率信息<最大支持码率, 选择码率>
     */
    Pair<Integer, Integer> getBitrateInfo();

    /**
     * 是否允许录制声音
     *
     * @param enable true：允许，false：不允许
     */
    boolean enableRecordingAudioVolume(boolean enable);

    /**
     * 是否允许录制视频/打开摄像头
     *
     * @param enable true：允许，false：不允许
     */
    boolean enableLocalVideo(boolean enable);

    /**
     * 设置相机方向
     *
     * @param front true：前置，false：后置
     */
    boolean setCameraDirection(boolean front);

    /**
     * 控制成员列表中的用户加入或离开连麦
     *
     * @param position    列表中的位置
     * @param isAllowJoin true：加入，false：离开
     */
    void controlUserLinkMic(int position, boolean isAllowJoin);

    /**
     * 禁/启用用户媒体
     *
     * @param position    成员列表中的位置
     * @param isVideoType true：视频，false：音频
     * @param isMute      true：禁用，false：启用
     */
    void muteUserMedia(int position, boolean isVideoType, boolean isMute);

    /**
     * 下麦全体连麦用户
     */
    void closeAllUserLinkMic();

    /**
     * 全体连麦用户禁用/开启声音
     *
     * @param isMute true：禁用，false：开启
     */
    void muteAllUserAudio(boolean isMute);

    /**
     * 添加推流状态的监听器
     *
     * @param listener 监听器
     */
    void addOnStreamerStatusListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加网络状态监听器
     *
     * @param listener 监听器
     */
    void addOnNetworkQualityListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 添加推流时间监听器
     *
     * @param listener 监听器
     */
    void addOnStreamerTimeListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 添加因断网延迟20s断流的状态监听器
     *
     * @param listener 监听器
     */
    void addOnShowNetBrokenListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加用户请求连麦的监听器
     *
     * @param listener 监听器
     */
    void addOnUserRequestListener(IPLVOnDataChangedListener<String> listener);

    /**
     * 添加音频状态监听器
     *
     * @param listener 监听器
     */
    void addOnEnableAudioListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加视频状态监听器
     *
     * @param listener 监听器
     */
    void addOnEnableVideoListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 添加摄像头方向监听器
     *
     * @param listener 监听器
     */
    void addOnIsFrontCameraListener(IPLVOnDataChangedListener<Boolean> listener);

    /**
     * 获取网络质量
     *
     * @return 网络质量常量
     */
    int getNetworkQuality();

    /**
     * 是否推流开始成功
     *
     * @return true：成功，false：未成功
     */
    boolean isStreamerStartSuccess();

    /**
     * 获取推流和连麦的presenter
     *
     * @return presenter
     */
    IPLVStreamerContract.IStreamerPresenter getStreamerPresenter();

    /**
     * 销毁，释放资源
     */
    void destroy();
}
