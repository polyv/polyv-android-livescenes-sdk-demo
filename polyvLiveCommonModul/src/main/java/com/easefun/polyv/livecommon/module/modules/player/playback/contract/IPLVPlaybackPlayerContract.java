package com.easefun.polyv.livecommon.module.modules.player.playback.contract;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlaybackPlayerData;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;

/**
 * mvp-回放播放器契约接口
 * 定义了：
 * 1、mvp-回放播放器view层接口
 * 2、mvp-回放播放器presenter层接口
 */
public interface IPLVPlaybackPlayerContract {

    // <editor-fold defaultstate="collapsed" desc="1、mvp-回放播放器view层接口">

    /**
     * mvp-回放播放器view层接口
     */
    interface IPlaybackPlayerView {
        /**
         * 设置presenter后的回调
         */
        void setPresenter(@NonNull IPlaybackPlayerPresenter presenter);

        /**
         * 获取主播放器view
         */
        PolyvPlaybackVideoView getPlaybackVideoView();

        /**
         * 获取 辅助播放器view
         * @return
         */
        PolyvAuxiliaryVideoview getSubVideoView();

        /**
         * 获取播放器缓冲视图
         */
        View getBufferingIndicator();

        /**
         * 获取logo
         */
        PLVPlayerLogoView getLogo();

        /**
         * 播放器准备完成回调
         */
        void onPrepared();

        /**
         * 播放失败回调
         *
         * @param error 失败数据
         * @param tips  错误提示
         */
        void onPlayError(PolyvPlayError error, String tips);

        /**
         * 播放完成回调
         */
        void onCompletion();

        /**
         * 播放器开始方法回调
         *
         * @param isFirst 每次加载完成后是否是第一次start播放
         */
        void onVideoPlay(boolean isFirst);

        /**
         * 子播放器倒数回调
         * @param isOpenAdHead
         * @param totalTime
         * @param remainTime
         * @param adStage
         */
        void onSubVideoViewCountDown(boolean isOpenAdHead,int totalTime, int remainTime, int adStage);

        /**
         * 子播放器是否可见回调
         * @param isOpenAdHead
         * @param isShow
         */
        void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow);

        /**
         * 播放器暂停方法回调
         */
        void onVideoPause();

        /**
         * 缓冲开始回调
         */
        void onBufferStart();

        /**
         * 缓冲结束回调
         */
        void onBufferEnd();

        /**
         * 手势触发的亮度改变事件
         *
         * @param changeValue 亮度值，范围：[0,100]
         * @param isEnd       手势是否结束
         * @return 是否要改变亮度
         */
        boolean onLightChanged(int changeValue, boolean isEnd);

        /**
         * 手势触发的音量改变事件
         *
         * @param changeValue 音量值，范围：[0,100]
         * @param isEnd       手势是否结束
         * @return 是否要改变音量
         */
        boolean onVolumeChanged(int changeValue, boolean isEnd);

        /**
         * 手势触发的进度改变事件
         *
         * @param seekTime     跳转的进度时间，单位：ms
         * @param totalTime    总时间，单位：ms
         * @param isEnd        手势是否结束
         * @param isRightSwipe 是否是往右滑动
         * @return 是否要跳转进度
         */
        boolean onProgressChanged(int seekTime, int totalTime, boolean isEnd, boolean isRightSwipe);

        /**
         * 双击手势触发
         */
        void onDoubleClick();

        /**
         * 获取跑马灯回调
         *
         * @param marqueeVo  跑马灯数据
         * @param viewerName 观看用户名
         */
        void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo, String viewerName);

        /**
         * 该频道直播的服务端弹幕开关
         *
         * @param isServerDanmuOpen true：开启了弹幕，false：关闭了弹幕
         */
        void onServerDanmuOpen(boolean isServerDanmuOpen);

        /**
         * 根据频道的类型，决定是否要显示ppt
         *
         * @param visible {@link View#VISIBLE}
         */
        void onShowPPTView(int visible);

        /**
         * 子播放器开始播放回调
         *
         * @param isFirst 每次加载完成后是否是第一次start播放
         */
        void onSubVideoViewPlay(boolean isFirst);

        /**
         * 更新播放信息
         * @param playInfoVO
         */
        void updatePlayInfo(PLVPlayInfoVO playInfoVO);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-回放播放器presenter层接口">

    /**
     * mvp-回放播放器presenter层接口
     */
    interface IPlaybackPlayerPresenter {
        /**
         * 注册view
         */
        void registerView(@NonNull IPlaybackPlayerView v);

        /**
         * 解除注册的view
         */
        void unregisterView();

        /**
         * 初始化播放器配置
         */
        void init();

        /**
         * 设置是否打开片头广告
         * @param isAllowOpenAdHead
         */
        void setAllowOpenAdHead(boolean isAllowOpenAdHead);

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
         * 停止播放
         */
        void stop();

        /**
         * 获取视频总时长
         *
         * @return
         */
        int getDuration();

        /**
         * 跳转到指定的视频时间
         *
         * @param duration 时间，单位：ms
         */
        void seekTo(int duration);

        /**
         * 根据progress占max的百分比，跳转到视频总时间的该百分比进度。并且如果视频是播放完成状态，则会开始播放。
         *
         * @param progress 跳转进度
         * @param max      总进度
         */
        void seekTo(int progress, int max);

        /**
         * 视频是否在播放中
         */
        boolean isPlaying();

        /**
         * 视频是否已经在准备状态中
         * @return
         */
        boolean isInPlaybackState();

        /**
         * 视频是否在播放中
         * @return
         */
        boolean isSubVideoViewShow();

        /**
         * 设置播放速度
         *
         * @param speed 速度值，建议范围为：[0.5, 2]
         */
        void setSpeed(float speed);

        /**
         * 获取播放速度
         */
        float getSpeed();

        /**
         * 设置系统音量
         *
         * @param volume 音量值，范围：[0,100]
         */
        void setVolume(int volume);

        /**
         * 获取系统音量
         *
         * @return 音量值，范围：[0,100]
         */
        int getVolume();

        /**
         * 设置播放器音量
         *
         * @param volume 音量值，范围：[0,100]
         */
        void setPlayerVolume(int volume);

        /**
         * 绑定PPTView
         *
         * @param pptView pptView
         */
        void bindPPTView(IPolyvPPTView pptView);

        /**
         * 获取片头广告或暖场链接
         * @return
         */
        String getSubVideoViewHerf();

        /**
         * 获取回放播放器数据
         */
        @NonNull
        PLVPlaybackPlayerData getData();

        /**
         * 获取视频的名称
         */
        @Nullable
        String getVideoName();

        /**
         * 销毁，包括销毁播放器、解除view
         */
        void destroy();
    }
    // </editor-fold>
}
