package com.easefun.polyv.liveecommerce.modules.player;

import android.arch.lifecycle.LiveData;
import android.graphics.Rect;
import android.view.View;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;

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
     * 是否在播放中
     *
     * @return true：播放中，false：非播放中。
     */
    boolean isPlaying();

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
     * videoView父控件从videoLayout中分离出来
     *
     * @return videoView父控件
     */
    View detachVideoViewParent();

    /**
     * 把videoView父控件附加到videoLayout里
     *
     * @param view videoView父控件
     */
    void attachVideoViewParent(View view);

    /**
     * 销毁，销毁播放器及相关资源
     */
    void destroy();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - live部分，定义 直播播放器布局 独有的方法">

    /**
     * 设置播放器区域位置
     *
     * @param videoViewRect 区域
     */
    void setVideoViewRect(Rect videoViewRect);

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="1、外部直接调用的方法 - playback部分，定义 回放播放器布局 独有的方法">

    /**
     * 获取视频总时长
     *
     * @return 总时长，单位：ms
     */
    int getDuration();

    /**
     * 根据progress占max的百分比，跳转到视频总时间的该百分比进度
     *
     * @param progress 进度
     * @param max      总进度
     */
    void seekTo(int progress, int max);

    /**
     * 获取播放速度
     *
     * @return 播放速度
     */
    float getSpeed();

    /**
     * 设置播放速度
     *
     * @param speed 播放速度，建议为[0,2]
     */
    void setSpeed(float speed);

    /**
     * 获取播放器数据中的播放信息
     *
     * @return 播放信息数据
     */
    LiveData<PLVPlayInfoVO> getPlayInfoVO();
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
    }
    // </editor-fold>
}
