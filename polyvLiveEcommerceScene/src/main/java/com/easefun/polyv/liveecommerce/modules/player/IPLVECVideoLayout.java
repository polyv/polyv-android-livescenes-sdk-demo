package com.easefun.polyv.liveecommerce.modules.player;

import android.arch.lifecycle.LiveData;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.linkmic.PLVLinkMicConstant;

import java.util.List;

/**
 * 直播带货场景下，针对 播放器布局 进行封装的 接口
 * 定义了：
 * 1、外部直接调用的方法
 * 2、需要外部响应的事件监听器
 */
public interface IPLVECVideoLayout {

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - common部分，定义 直播播放器布局 和 回放播放器布局 通用的方法">

    /**
     * 初始化
     *
     * @param liveRoomDataManager 直播间数据管理器
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 开始播放
     */
    void startPlay();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 恢复播放
     */
    void resume();

    /**
     * 主播放器或子播放器否已经正式可播放
     */
    boolean isInPlaybackState();

    /**
     * 视频是否在播放中
     *
     * @return true：播放中，false：非播放中。
     */
    boolean isPlaying();

    /**
     * 子播放器是否正在显示
     */
    boolean isSubVideoViewShow();

    /**
     * 返回当前片头广告或者暖场广告的地址
     */
    String getSubVideoViewHerf();

    /**
     * 获取播放器自定义水印
     *
     * @return logoview
     */
    PLVPlayerLogoView getLogoView();

    /**
     * 设置播放器音量
     *
     * @param volume 音量值，范围：[0,100]
     */
    void setPlayerVolume(int volume);

    /**
     * 获取播放器数据中的播放状态
     *
     * @return 播放状态数据
     */
    LiveData<PLVPlayerState> getPlayerState();

    /**
     * 设置view交互事件监听器
     *
     * @param listener 监听器
     */
    void setOnViewActionListener(OnViewActionListener listener);

    /**
     * view 是否设置为浮窗显示
     *
     * @param isFloating
     */
    void setFloatingWindow(boolean isFloating);

    /**
     * @see View#dispatchTouchEvent(MotionEvent)
     */
    boolean dispatchTouchEvent(MotionEvent ev);

    PLVSwitchViewAnchorLayout getPlayerSwitchAnchorLayout();

    /**
     * 设置播放器区域位置
     *
     * @param videoViewRect 区域
     */
    void setVideoViewRect(Rect videoViewRect);

    /**
     * 添加播放器状态的监听器
     *
     * @param listener 监听器
     */
    void addOnPlayerStateListener(IPLVOnDataChangedListener<PLVPlayerState> listener);

    void updatePlayCenterView();

    /**
     * 销毁，销毁播放器及相关资源
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - live部分，定义 直播播放器布局 独有的方法">
    /**
     * 重新开始播放
     */
    void restartPlay();

    /**
     * 获取线路索引
     *
     * @return 线路索引
     */
    int getLinesPos();

    /**
     * 获取可以切换的线路数
     *
     * @return 线路数
     */
    int getLinesCount();

    /**
     * 切换线路
     *
     * @param linesPos 线路索引
     */
    void changeLines(int linesPos);

    /**
     * 获取当前播放码率(清晰度)索引
     *
     * @return 码率(清晰度)索引
     */
    int getBitratePos();

    /**
     * 获取播放器播放视频的码率(清晰度)信息
     *
     * @return 码率(清晰度)信息
     */
    List<PolyvDefinitionVO> getBitrateVO();

    /**
     * 切换码率(清晰度)
     *
     * @param bitratePos 清晰度索引
     */
    void changeBitRate(int bitratePos);

    /**
     * 获取播放模式
     *
     * @return 播放模式
     */
    int getMediaPlayMode();

    /**
     * 改变播放模式
     *
     * @param mediaPlayMode 播放模式
     */
    void changeMediaPlayMode(@PolyvMediaPlayMode.Mode int mediaPlayMode);

    /**
     * 当前是否无延迟观看模式
     */
    boolean isCurrentLowLatencyMode();

    /**
     * 切换无延迟观看模式
     *
     * @param isLowLatencyMode 是否无延迟观看
     */
    void switchLowLatencyMode(boolean isLowLatencyMode);

    /**
     * 获取直播播放器数据中的播放信息
     *
     * @return 播放信息数据
     */
    LiveData<com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO> getLivePlayInfoVO();

    /**
     * 获取rtc混流观看视图容器
     */
    @Nullable
    ViewGroup getRtcMixStreamContainer();

    /**
     * 当加入RTC时，更新布局
     *
     * @param linkMicLayoutLandscapeWidth 连麦布局在横屏的宽度
     */
    void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth);

    /**
     * 当离开RTC时，更新布局
     */
    void updateWhenLeaveRTC();

    /**
     * 加入连麦
     */
    void updateWhenJoinLinkMic();

    /**
     * 离开连麦
     */
    void updateWhenLeaveLinkMic();

    void notifyRTCPrepared();

    /**
     * 添加连麦是否开启状态的监听器
     *
     * @param listener 监听器
     */
    void addOnLinkMicStateListener(IPLVOnDataChangedListener<Pair<Boolean, Boolean>> listener);

    void setOnRTCPlayEventListener(IPolyvLiveListenerEvent.OnRTCPlayEventListener listener);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - playback部分，定义 回放播放器布局 独有的方法">

    /**
     * 获取视频总时长
     *
     * @return 总时长，单位：ms
     */
    int getDuration();

    /**
     * 获取视频当前播放时间
     *
     * @return 视频当前播放时间，单位：毫秒
     */
    int getVideoCurrentPosition();

    /**
     * 根据progress占max的百分比，跳转到视频总时间的该百分比进度
     *
     * @param progress 进度
     * @param max      总进度
     */
    void seekTo(int progress, int max);

    /**
     * 设置播放速度
     *
     * @param speed 播放速度，建议为[0,2]
     */
    void setSpeed(float speed);

    /**
     * 获取播放速度
     *
     * @return 播放速度
     */
    float getSpeed();

    /**
     * 添加seek完成监听器
     *
     * @param listener 监听器
     */
    void addOnSeekCompleteListener(IPLVOnDataChangedListener<Integer> listener);

    /**
     * 获取回放播放器数据中的播放信息
     *
     * @return 播放信息数据
     */
    LiveData<PLVPlayInfoVO> getPlaybackPlayInfoVO();

    /**
     * 改变回放视频的vid
     * @param vid
     */
    void changePlaybackVid(String vid);

    /**
     * 改变回放视频vid并且立即播放
     *
     * @param vid
     */
    void changePlaybackVidAndPlay(String vid);

    /**
     * 获取sessionId
     */
    String getSessionId();

    /**
     * 获取文件Id
     */
    String getFileId();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、需要外部响应的事件监听器 - 定义 播放器布局中UI控件 触发的交互事件的回调方法">

    /**
     * view交互事件监听器
     */
    interface OnViewActionListener {
        /**
         * 关闭浮窗点击事件
         */
        void onCloseFloatingAction();

        /**
         * 显示更多布局
         */
        void onShowMoreLayoutAction();

        /**
         * 无延迟观看回调
         *
         * @param isLowLatency 是否正在无延迟观看
         */
        void acceptOnLowLatencyChange(boolean isLowLatency);

        /**
         * 网络质量回调
         */
        void acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality networkQuality);

        /**
         * 当前视频是否可以设置全屏
         * @param isCanFullScreen
         */
        void acceptVideoSize(boolean isCanFullScreen);

        /**
         * rtc是否以混流形式播放
         *
         * @return
         */
        boolean isPlayRtcAsMixStream();

        /**
         * rtc混流是否正在播放
         *
         * @return
         */
        boolean isRtcMixStreamPlaying();
    }
    // </editor-fold>
}
