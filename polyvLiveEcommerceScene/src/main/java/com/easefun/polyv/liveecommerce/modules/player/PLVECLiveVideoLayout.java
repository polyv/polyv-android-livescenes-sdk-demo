package com.easefun.polyv.liveecommerce.modules.player;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidacontrol.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.marquee.PLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.player.PLVEmptyMediaController;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayErrorMessageUtils;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.PLVLivePlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.live.view.PLVAbsLivePlayerView;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.watermark.IPLVWatermarkView;
import com.easefun.polyv.livecommon.module.modules.watermark.PLVWatermarkView;
import com.easefun.polyv.livecommon.module.utils.PLVScreenshotHelper;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.widget.PLVECLiveNoStreamView;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveAudioModeView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.business.api.common.player.PLVPlayerConstant;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.log.elog.PLVELogsService;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.log.player.PLVPlayerElog;
import com.plv.livescenes.video.api.IPLVLiveListenerEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * date: 2020-04-29
 * author: hwj
 * description:直播播放器布局
 */
public class PLVECLiveVideoLayout extends FrameLayout implements IPLVECVideoLayout, View.OnClickListener {
    private static final String TAG = "PLVECLiveVideoLayout";
    // <editor-fold defaultstate="collapsed" desc="变量">

    //是否允许播放片头广告
    private boolean isAllowOpenAdHead = true;
    //直播播放器横屏视频、音频模式的播放器区域位置
    private final Rect videoViewRect = new Rect();
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVSwitchViewAnchorLayout livePlayerSwitchAnchorLayout;
    private ViewGroup liveVideoContainerLayout;
    //主播放器渲染视图view
    private PolyvLiveVideoView videoView;
    //子播放器渲染视图view
    private PolyvAuxiliaryVideoview subVideoView;
    //子播放器倒计时显示的view
    private TextView tvCountDown;
    private LinearLayout llAuxiliaryCountDown;
    //广告倒计时是否显示
    private boolean isShowingAdHeadCountDown = true;
    //音频模式view
    private IPolyvLiveAudioModeView audioModeView;
    //控制器
    private IPolyvMediaController mediaController;
    //播放器缓冲显示的view
    private View loadingView;

    //播放控制按钮
    private ImageView playCenterView;

    //当前没有直播显示的view
    private View nostreamView;
    //播放失败/加载失败显示的view
    private PLVECLiveNoStreamView playErrorView;
    //浮窗关闭按钮
    private ImageView closeFloatingView;
    //播放器presenter
    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;

    //logo view
    private PLVPlayerLogoView logoView;
    //marquee view
    private PLVMarqueeView marqueeView = null;
    //watermark view
    private PLVWatermarkView watermarkView;
    //播放器是否小窗播放状态
    private boolean isVideoViewPlayingInFloatWindow;
    //截图，用于刷新直播的时候防止黑屏
    private ImageView screenshotIV;

    private ViewTreeObserver.OnGlobalLayoutListener onSubVideoViewLayoutListener;

    //Listener
    private IPLVECVideoLayout.OnViewActionListener onViewActionListener;

    //是否加入了RTC
    private boolean isJoinRTC;
    //是否加入了连麦
    private boolean isJoinLinkMic;

    /**
     * 暂无直播的图片以及文字
     */
    private ImageView nostreamIv;
    private TextView nostreamTv;
    
    //全屏按钮
    private ImageView fullScreenIv;

    private TextView livePlayerFloatingPlayingPlaceholderTv;
    private ViewGroup livePlayerRtcMixStreamVideoContainer;

    // 是否无延迟观看模式
    private boolean isLowLatency = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    private boolean isLiveStart = false;

    private Observer<Boolean> floatingStatusObserver;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECLiveVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_layout, this, true);

        livePlayerSwitchAnchorLayout = findViewById(R.id.plvec_live_player_switch_anchor_layout);
        liveVideoContainerLayout = findViewById(R.id.plvec_live_video_container_layout);
        videoView = findViewById(R.id.plvec_live_video_view);
        livePlayerRtcMixStreamVideoContainer = findViewById(R.id.plvec_live_player_rtc_mix_stream_video_container);

        playCenterView = findViewById(R.id.play_center);
        hidePlayCenterView();
        playCenterView.setOnClickListener(this);

        subVideoView = findViewById(R.id.sub_video_view);
        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);
        audioModeView = findViewById(R.id.audio_mode_ly);
        loadingView = findViewById(R.id.loading_pb);
        nostreamView = findViewById(R.id.nostream_ly);
        playErrorView = findViewById(R.id.plvec_play_error_ly);
        closeFloatingView = findViewById(R.id.close_floating_iv);
        closeFloatingView.setOnClickListener(this);
        mediaController = new PLVEmptyMediaController();

        nostreamIv = findViewById(R.id.nostream_iv);
        nostreamTv = findViewById(R.id.nostream_tv);
        logoView = findViewById(R.id.logo_view);

        watermarkView = findViewById(R.id.plvec_watermark_view);
        marqueeView = ((Activity) getContext()).findViewById(R.id.plvec_marquee_view);
        screenshotIV = findViewById(R.id.plvec_screenshot_iv);

        livePlayerFloatingPlayingPlaceholderTv = findViewById(R.id.plvec_live_player_floating_playing_placeholder_tv);

        fullScreenIv = findViewById(R.id.plvec_full_screen_iv);
        fullScreenIv.setOnClickListener(this);

        initVideoView();
        initPlayErrorView();
        initSubVideoViewChangeListener();
        observeFloatingPlayer();
    }

    private void initVideoView() {
        videoView.setSubVideoView(subVideoView);
        videoView.setAudioModeView(audioModeView);
        videoView.setPlayerBufferingIndicator(loadingView);
        videoView.setNoStreamIndicator(nostreamView);
        videoView.setMediaController(mediaController);
        videoView.disableScreenCAP((Activity) getContext(), PLVScreenshotHelper.DISABLE_SCREEN_CAP); // 防录屏开关，true为开启

        videoView.setOnLowLatencyNetworkQualityListener(new IPLVLiveListenerEvent.OnLowLatencyNetworkQualityListener() {
            @Override
            public void onNetworkQuality(int networkQuality) {
                if (onViewActionListener == null) {
                    return;
                }
                if (PLVPlayerConstant.NetQuality.isNoConnection(networkQuality)) {
                    onViewActionListener.acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality.DISCONNECT);
                } else if (PLVPlayerConstant.NetQuality.isNetPoor(networkQuality)) {
                    onViewActionListener.acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality.VERY_BAD);
                } else if (PLVPlayerConstant.NetQuality.isNetMiddleOrWorse(networkQuality)) {
                    onViewActionListener.acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality.POOR);
                } else {
                    onViewActionListener.acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality.GOOD);
                }
            }
        });
    }

    private void initPlayErrorView() {
        playErrorView.setPlaceHolderImg(R.drawable.plv_bg_player_error_ic);
        playErrorView.setOnChangeLinesViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowMoreLayoutAction();
                }
            }
        });
        playErrorView.setOnRefreshViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                livePlayerPresenter.restartPlay();
            }
        });
    }

    private void initSubVideoViewChangeListener() {
        onSubVideoViewLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!isShowingAdHeadCountDown) {
                    llAuxiliaryCountDown.setVisibility(GONE);
                    return;
                }
                if (subVideoView == null || !subVideoView.isShow()) {
                    return;
                }
                if (subVideoView.getAdHeadImage() != null) {
                    llAuxiliaryCountDown.setY(subVideoView.getAdHeadImage().getY() + ConvertUtils.dp2px(15));
                } else {
                    float y = subVideoView.getY();
                    int viewHeight = PLVVideoSizeUtils.getVideoWH(subVideoView)[1];
                    if (subVideoView.getAspectRatio() == PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT) {
                        int surHeight = subVideoView.getHeight();
                        if (viewHeight == 0 || surHeight == 0) return;
                        y = y + (float) ((surHeight - viewHeight) >> 1);
                    } else {
                        y = ConvertUtils.dp2px(112);
                    }
                    llAuxiliaryCountDown.setY(y);
                }
            }
        };
    }

    private void observeFloatingPlayer() {
        PLVFloatingPlayerManager.getInstance().getFloatingViewShowState()
                .observeForever(floatingStatusObserver = new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean isShowingBoolean) {
                        final boolean isShowing = isShowingBoolean != null && isShowingBoolean;
                        isVideoViewPlayingInFloatWindow = isShowing;
                        videoView.setNeedGestureDetector(!isShowing);
                        subVideoView.setNeedGestureDetector(!isShowing);
                        livePlayerFloatingPlayingPlaceholderTv.setVisibility(isShowing ? VISIBLE : GONE);

                        updateVideoViewSize();
                        postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (ScreenUtils.isLandscape()) {
                                    updateVideoViewSize();
                                }
                            }
                        }, !isShowing ? 600 : 0); // 小米后台横屏回来时不会触发onConfigurationChanged，需要延迟执行，不然获取的方向不对
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的common方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        livePlayerPresenter = new PLVLivePlayerPresenter(liveRoomDataManager);
        livePlayerPresenter.registerView(livePlayerView);
        livePlayerPresenter.init();
        livePlayerPresenter.setAllowOpenAdHead(isAllowOpenAdHead);
    }

    @Override
    public void startPlay() {
        livePlayerPresenter.startPlay(isLowLatency);
    }

    @Override
    public void pause() {
        livePlayerPresenter.pause();
    }

    @Override
    public void resume() {
        livePlayerPresenter.resume();
    }

    @Override
    public boolean isInPlaybackState() {
        return livePlayerPresenter.isInPlaybackState();
    }

    @Override
    public boolean isPlaying() {
        return livePlayerPresenter.isPlaying();
    }

    @Override
    public boolean isSubVideoViewShow() {
        return livePlayerPresenter.isSubVideoViewShow();
    }

    @Override
    public String getSubVideoViewHerf() {
        return livePlayerPresenter.getSubVideoViewHerf();
    }

    @Override
    public PLVPlayerLogoView getLogoView() {
        return logoView;
    }

    @Override
    public void setPlayerVolume(int volume) {
        livePlayerPresenter.setPlayerVolume(volume);
    }

    @Override
    public LiveData<PLVPlayerState> getPlayerState() {
        return livePlayerPresenter.getData().getPlayerState();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void setFloatingWindow(boolean isFloating) {
        isShowingAdHeadCountDown = !isFloating;
    }

    @Override
    public PLVSwitchViewAnchorLayout getPlayerSwitchAnchorLayout() {
        return livePlayerSwitchAnchorLayout;
    }

    @Override
    public void setVideoViewRect(Rect videoViewRect) {
        this.videoViewRect.set(videoViewRect);
        updateVideoViewSize();
    }

    @Override
    public void addOnPlayerStateListener(IPLVOnDataChangedListener<PLVPlayerState> listener) {
        livePlayerPresenter.getData().getPlayerState().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void destroy() {
        if (audioModeView != null) {
            audioModeView.onHide();
        }
        if (livePlayerPresenter != null) {
            livePlayerPresenter.destroy();
        }
        PLVFloatingPlayerManager.getInstance().getFloatingViewShowState().removeObserver(floatingStatusObserver);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的live方法">
    @Override
    public void restartPlay() {
        livePlayerPresenter.restartPlay();
    }

    @Override
    public int getLinesPos() {
        return livePlayerPresenter.getLinesPos();
    }

    @Override
    public int getLinesCount() {
        return livePlayerPresenter.getLinesCount();
    }

    @Override
    public void changeLines(int linesPos) {
        livePlayerPresenter.changeLines(linesPos);
    }

    @Override
    public int getBitratePos() {
        return livePlayerPresenter.getBitratePos();
    }

    @Override
    public List<PolyvDefinitionVO> getBitrateVO() {
        return livePlayerPresenter.getBitrateVO();
    }

    @Override
    public void changeBitRate(int bitratePos) {
        livePlayerPresenter.changeBitRate(bitratePos);
    }

    @Override
    public int getMediaPlayMode() {
        return livePlayerPresenter.getMediaPlayMode();
    }

    @Override
    public void changeMediaPlayMode(int mediaPlayMode) {
        livePlayerPresenter.changeMediaPlayMode(mediaPlayMode);
    }

    @Override
    public boolean isCurrentLowLatencyMode() {
        return this.isLowLatency;
    }

    @Override
    public void switchLowLatencyMode(boolean isLowLatencyMode) {
        if (this.isLowLatency == isLowLatencyMode) {
            return;
        }
        this.isLowLatency = isLowLatencyMode;
        startPlay();
        if (this.isLowLatency) {
            PLVELogsService.getInstance().addStaticsLog(PLVPlayerElog.class, PLVPlayerElog.Event.SWITCH_TO_NO_DELAY, " isLowLatency: " + this.isLowLatency);
        } else {
            PLVELogsService.getInstance().addStaticsLog(PLVPlayerElog.class, PLVPlayerElog.Event.SWITCH_TO_DELAY, " isLowLatency: " + this.isLowLatency);
        }
        if (onViewActionListener != null) {
            onViewActionListener.acceptOnLowLatencyChange(this.isLowLatency);
        }
    }

    @Override
    public LiveData<com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO> getLivePlayInfoVO() {
        return livePlayerPresenter.getData().getPlayInfoVO();
    }

    @Nullable
    @Override
    public ViewGroup getRtcMixStreamContainer() {
        return livePlayerRtcMixStreamVideoContainer;
    }

    @Override
    public void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth) {
        isJoinRTC = true;

        if (liveRoomDataManager.isSupportRTC()) {
            //如果支持RTC，连麦时停止播放器播放，使用rtc视频流+rtc音频流
            livePlayerPresenter.stop();
        } else {
            //如果不支持RTC，连麦时静音播放器，使用播放器的cdn视频流+rtc音频流
            livePlayerPresenter.setPlayerVolume(0);
        }
        //禁用播放器手势
        livePlayerPresenter.setNeedGestureDetector(!isJoinRTC);
    }

    @Override
    public void updateWhenLeaveRTC() {
        isJoinRTC = false;

        if (liveRoomDataManager.isSupportRTC() && livePlayerPresenter.getData().getPlayerState().getValue() == PLVPlayerState.PREPARED) {
            startPlay();
        }
        //恢复播放器手势
        livePlayerPresenter.setNeedGestureDetector(!isJoinRTC);
        //恢复播放器音量
        livePlayerPresenter.setPlayerVolume(100);
    }

    @Override
    public void updateWhenJoinLinkMic() {
        isJoinLinkMic = true;
        videoView.setIsLinkMic(true);
    }

    @Override
    public void updateWhenLeaveLinkMic() {
        isJoinLinkMic = false;
        videoView.setIsLinkMic(false);
    }

    @Override
    public void notifyRTCPrepared() {
        videoView.rtcPrepared();
    }

    @Override
    public void addOnLinkMicStateListener(IPLVOnDataChangedListener<Pair<Boolean, Boolean>> listener) {
        livePlayerPresenter.getData().getLinkMicState().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void setOnRTCPlayEventListener(IPolyvLiveListenerEvent.OnRTCPlayEventListener listener) {
        videoView.setOnRTCPlayEventListener(listener);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的playback方法，空实现">

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getVideoCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int progress, int max) {
        PLVCommonLog.d(TAG, "live video cannot seek");
    }

    @Override
    public void setSpeed(float speed) {
        PLVCommonLog.d(TAG, "live video cannot set Speed");
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public void addOnSeekCompleteListener(IPLVOnDataChangedListener<Integer> listener) {
    }

    @Override
    public LiveData<PLVPlayInfoVO> getPlaybackPlayInfoVO() {
        return null;
    }

    @Override
    public void changePlaybackVid(String vid) {
        PLVCommonLog.d(TAG, "live video cannot change vid");
    }

    @Override
    public void changePlaybackVidAndPlay(String vid) {
        PLVCommonLog.d(TAG, "live video cannot change vid and play");
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public String getFileId() {
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式的view层实现">
    private IPLVLivePlayerContract.ILivePlayerView livePlayerView = new PLVAbsLivePlayerView() {
        @Override
        public void setPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter presenter) {
            super.setPresenter(presenter);
            livePlayerPresenter = presenter;
        }

        @Override
        public PolyvLiveVideoView getLiveVideoView() {
            return videoView;
        }

        @Override
        public PolyvAuxiliaryVideoview getSubVideoView() {
            return subVideoView;
        }

        @Override
        public View getBufferingIndicator() {
            return loadingView;
        }

        @Override
        public View getNoStreamIndicator() {
            return nostreamView;
        }

        @Override
        public View getPlayErrorIndicator() {
            return playErrorView;
        }

        @Override
        public PLVPlayerLogoView getLogo() {
            return logoView;
        }

        @Override
        public IPLVMarqueeView getMarqueeView() {
            return marqueeView;
        }

        @Override
        public IPLVWatermarkView getWatermarkView() {
            return watermarkView;
        }

        @Override
        public void onSubVideoViewPlay(boolean isFirst) {
            super.onSubVideoViewPlay(isFirst);
            int[] wh = PLVVideoSizeUtils.getVideoWH(subVideoView);
            if (wh[0] > 0 && wh[1] > 0) {
                onVideoSizeChanged(wh[0], wh[1]);
            }
        }

        @Override
        public void onSubVideoViewLoadImage(String imageUrl, ImageView imageView) {
            PLVImageLoader.getInstance().loadImage(subVideoView.getContext(), imageUrl, imageView);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            imageView.setLayoutParams(lp);
        }

        @Override
        public void onSubVideoViewCountDown(boolean isOpenAdHead, int totalTime, int remainTime, int adStage) {
            if (!isShowingAdHeadCountDown) {
                llAuxiliaryCountDown.setVisibility(GONE);
                return;
            }
            if (isOpenAdHead) {
                llAuxiliaryCountDown.setVisibility(VISIBLE);
                tvCountDown.setText(PLVAppUtils.formatString(R.string.plv_player_advertising_count_down, remainTime + ""));
            }
        }

        @Override
        public void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow) {
            if (isOpenAdHead) {
                if (!isShow) {
                    llAuxiliaryCountDown.setVisibility(GONE);
                    subVideoView.getViewTreeObserver().removeOnGlobalLayoutListener(onSubVideoViewLayoutListener);
                } else {
                    subVideoView.getViewTreeObserver().addOnGlobalLayoutListener(onSubVideoViewLayoutListener);
                }
            } else {
                llAuxiliaryCountDown.setVisibility(GONE);
            }
            if (isShow) {
                nostreamIv.setVisibility(View.GONE);
                nostreamTv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            PLVPlayErrorMessageUtils.showOnPlayError(playErrorView, error, liveRoomDataManager.getConfig().isLive());
        }

        @Override
        public void onLoadSlow(int loadedTime, boolean isBufferEvent) {
            super.onLoadSlow(loadedTime, isBufferEvent);
            PLVPlayErrorMessageUtils.showOnLoadSlow(playErrorView, liveRoomDataManager.getConfig().isLive());
            if (isBufferEvent) {
                // 铺满占位图
                playErrorView.setFullLayout();
            }
        }

        @Override
        public void onNoLiveAtPresent() {
            super.onNoLiveAtPresent();
            isLiveStart = false;
            updateVideoViewSize();

            ToastUtils.showShort(R.string.plv_player_toast_no_live);
            hidePlayCenterView();
        }

        @Override
        public void onLiveStop() {
            super.onLiveStop();
            isLiveStart = false;
            updateVideoViewSize();
            hidePlayCenterView();
        }

        @Override
        public void onLiveEnd() {
            super.onLiveEnd();
            isLiveStart = false;
            ToastUtils.showShort(R.string.plv_player_toast_live_end);
            hidePlayCenterView();
            if (isSubVideoViewShow()) {
                nostreamIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPrepared(int mediaPlayMode) {
            super.onPrepared(mediaPlayMode);
            hideScreenShotView();
            isLiveStart = true;
            updateVideoViewSize();

            //水印与视频大小匹配
            post(new Runnable() {
                @Override
                public void run() {
                    if (videoView.getIjkVideoView().getRenderView() != null) {
                        ViewGroup.LayoutParams layoutParams = watermarkView.getLayoutParams();
                        layoutParams.height = videoView.getIjkVideoView().getRenderView().getView().getHeight();
                        watermarkView.setLayoutParams(layoutParams);
                    } else {
                        ViewGroup.LayoutParams layoutParams = watermarkView.getLayoutParams();
                        layoutParams.height = PLVUIUtil.dip2px(getContext(), 206);
                        watermarkView.setLayoutParams(layoutParams);
                    }
                }
            });
        }

        @Override
        public void onRestartPlay() {
            super.onRestartPlay();
            showScreenShotView();
        }

        @Override
        public void onLinesChanged(int linesPos) {
            super.onLinesChanged(linesPos);
        }

        @Override
        public void updatePlayInfo(com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO playInfoVO) {
            updatePlayCenterView();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 截图view显示、隐藏">
    private void hideScreenShotView() {
        screenshotIV.setVisibility(GONE);
    }

    private void showScreenShotView() {
        Bitmap screenshot = livePlayerPresenter.screenshot();
        screenshotIV.setImageBitmap(screenshot);
        screenshotIV.setVisibility(VISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 播放暂停按钮的显示、隐藏">

    @Override
    public void updatePlayCenterView() {
        if (onViewActionListener != null && onViewActionListener.isPlayRtcAsMixStream()) {
            if (onViewActionListener.isRtcMixStreamPlaying()) {
                hidePlayCenterView();
            } else {
                showPlayCenterView();
            }
        } else {
            if (!isPlaying() && !isSubVideoViewShow()) {
                showPlayCenterView();
            } else {
                hidePlayCenterView();
            }
        }
    }

    private void hidePlayCenterView() {
        playCenterView.setVisibility(GONE);
    }

    private void showPlayCenterView() {
        if (!isSubVideoViewShow()) {
            playCenterView.setVisibility(VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转监听">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateVideoViewSize();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI更新">

    private void updateVideoViewSize() {
        int[] size = PLVVideoSizeUtils.getVideoWH(videoView);
        if (size[0] > 0 && size[1] > 0) {
            onVideoSizeChanged(size[0], size[1]);
        }
    }

    private void onVideoSizeChanged(int width, int height) {
        final boolean isLandscape = ScreenUtils.isLandscape();
        final boolean videoLandscape = width > height;
        MarginLayoutParams layoutParams = (MarginLayoutParams) liveVideoContainerLayout.getLayoutParams();
        if (isLandscape || !videoLandscape || isVideoViewPlayingInFloatWindow) {
            layoutParams.height = layoutParams.width;
            layoutParams.topMargin = 0;
        } else {
            layoutParams.height = ScreenUtils.getScreenWidth() * height / width;
            layoutParams.topMargin = videoViewRect.top + (videoViewRect.bottom - videoViewRect.top - layoutParams.height) / 2;
        }
        liveVideoContainerLayout.setLayoutParams(layoutParams);

        final boolean isCanFullScreen = videoLandscape && isLiveStart;
        fullScreenIv.setVisibility(isCanFullScreen ? View.VISIBLE : View.GONE);
        if (onViewActionListener != null) {
            onViewActionListener.acceptVideoSize(isCanFullScreen);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_floating_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onCloseFloatingAction();
            }
        } else if (v.getId() == R.id.play_center) {
            restartPlay();
        } else if (v.getId() == R.id.plvec_full_screen_iv){
            if(PLVScreenUtils.isPortrait(getContext())){
                PLVOrientationManager.getInstance().setLandscape((Activity) getContext());
            }
        }
    }
    // </editor-fold>
}
