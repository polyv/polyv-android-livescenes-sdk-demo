package com.easefun.polyv.livecommon.module.modules.player.live.contract;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveChannelVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVLivePlayerData;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;

import java.util.List;

/**
 * mvp-直播播放器契约接口
 * 定义了：
 * 1、mvp-直播播放器view层接口
 * 2、mvp-直播播放器presenter层接口
 */
public interface IPLVLivePlayerContract {

    // <editor-fold defaultstate="collapsed" desc="1、mvp-直播播放器view层接口">

    /**
     * mvp-直播播放器view层接口
     */
    interface ILivePlayerView {
        /**
         * 设置presenter后的回调
         */
        void setPresenter(@NonNull ILivePlayerPresenter presenter);

        /**
         * 获取主播放器view
         */
        PolyvLiveVideoView getLiveVideoView();

        /**
         * 获取暖场播放器view
         */
        PolyvAuxiliaryVideoview getSubVideoView();

        /**
         * 获取播放器缓冲视图
         */
        View getBufferingIndicator();

        /**
         * 获取暂无直播显示的视图
         */
        View getNoStreamIndicator();

        /**
         * 获取logo
         */
        PLVPlayerLogoView getLogo();

        /**
         * 子播放器开始播放回调
         *
         * @param isFirst 每次加载完成后是否是第一次start播放
         */
        void onSubVideoViewPlay(boolean isFirst);

        /**
         * 子播放器点击事件
         *
         * @param mainPlayerIsPlaying 主播放器是否在播放中
         */
        void onSubVideoViewClick(boolean mainPlayerIsPlaying);

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
         * 主播放器播放失败回调
         *
         * @param error 失败数据
         * @param tips  错误提示
         */
        void onPlayError(PolyvPlayError error, String tips);

        /**
         * 暂无直播回调
         */
        void onNoLiveAtPresent();

        /**
         * 直播推流暂停回调
         */
        void onLiveStop();

        /**
         * 直播结束回调
         */
        void onLiveEnd();

        /**
         * 准备完成回调
         *
         * @param mediaPlayMode 音视频播放模式
         */
        void onPrepared(@PolyvMediaPlayMode.Mode int mediaPlayMode);

        /**
         * 线路切换回调
         *
         * @param linesPos 线路索引
         */
        void onLinesChanged(int linesPos);

        /**
         * 获取跑马灯回调
         *
         * @param marqueeVo  跑马灯数据
         * @param viewerName 观看用户名
         */
        void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo, String viewerName);

        /**
         * 重新开始播放回调
         */
        void onRestartPlay();

        /**
         * 手势触发的亮度改变事件
         *
         * @param changeValue 亮度值，范围：[0,100]
         * @param isEnd       手势是否结束
         * @return 是否要改变亮度
         */
        boolean onLightChanged(int changeValue, boolean isEnd);

        /**
         * 手势触发的音量改变事件，changeValue：，return：是否要改变音量
         *
         * @param changeValue 音量值，范围：[0,100]
         * @param isEnd       手势是否结束
         * @return 是否要改变音量
         */
        boolean onVolumeChanged(int changeValue, boolean isEnd);

        /**
         * 更新播放信息
         * @param playInfoVO
         */
        void updatePlayInfo(PLVPlayInfoVO playInfoVO);

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
         * 断网重连
         *
         * @return true表示不使用播放器内部重连逻辑，false表示使用。
         */
        boolean onNetworkRecover();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="2、mvp-直播播放器presenter层接口">

    /**
     * mvp-直播播放器presenter层接口
     */
    interface ILivePlayerPresenter {
        /**
         * 注册view
         */
        void registerView(@NonNull ILivePlayerView v);

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
         * 重新开始播放
         */
        void restartPlay();

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
         * 是否在播放中
         */
        boolean isPlaying();

        /**
         * 是否已经可以播放视频了
         */
        boolean isInPlaybackState();

        /**
         * 辅助视频是否正在显示（包括暖场视频和片头广告）
         * @return
         */
        boolean isSubVideoViewShow();

        /**
         * 获取可以切换的线路数量
         */
        int getLinesCount();

        //获取当前线路可以切换的码率(清晰度)信息
        @Nullable
        List<PolyvDefinitionVO> getBitrateVO();

        /**
         * 获取播放模式
         *
         * @return @{@link PolyvMediaPlayMode.Mode}
         */
        int getMediaPlayMode();

        /**
         * 改变播放模式
         */
        void changeMediaPlayMode(@PolyvMediaPlayMode.Mode int mediaPlayMode);

        /**
         * 切换线路
         *
         * @param linesPos 线路索引
         */
        void changeLines(int linesPos);

        /**
         * 切换码率
         *
         * @param bitRate 码率索引
         */
        void changeBitRate(int bitRate);

        /**
         * 截图
         *
         * @return 截图的图片
         */
        @Nullable
        Bitmap screenshot();

        /**
         * 获取视频信息
         *
         * @return 视频信息数据
         */
        @Nullable
        PolyvLiveChannelVO getChannelVO();

        /**
         * 获取当前线路索引
         */
        int getLinesPos();

        /**
         * 获取当前码率(清晰度)索引
         */
        int getBitratePos();

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
         * 是否需要手势
         *
         * @param need true：需要，false：不需要
         */
        void setNeedGestureDetector(boolean need);

        /**
         * 返回当前片头广告或者暖场广告的地址
         */
        String getSubVideoViewHerf();

        /**
         * 获取直播播放器数据
         */
        @NonNull
        PLVLivePlayerData getData();

        /**
         * 销毁，包括销毁播放器、解除view
         */
        void destroy();

    }
    // </editor-fold>
}
