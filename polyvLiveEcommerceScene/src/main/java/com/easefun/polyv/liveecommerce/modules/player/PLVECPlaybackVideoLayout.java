package com.easefun.polyv.liveecommerce.modules.player;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.marquee.PLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayErrorMessageUtils;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackPlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.view.PLVAbsPlaybackPlayerView;
import com.easefun.polyv.livecommon.module.modules.watermark.IPLVWatermarkView;
import com.easefun.polyv.livecommon.module.modules.watermark.PLVWatermarkView;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.constant.PLVECFitMode;
import com.easefun.polyv.liveecommerce.modules.player.widget.PLVECLiveNoStreamView;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.List;

/**
 * date: 2020-04-30
 * author: hwj
 * description:回放播放器布局
 */
public class PLVECPlaybackVideoLayout extends FrameLayout implements IPLVECVideoLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVECPlaybackVideoLayo";

    //是否允许播放片头广告
    private boolean isAllowOpenAdhead = true;
    //回放播放器横屏视频的播放器区域位置
    private Rect videoViewRect;
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVSwitchViewAnchorLayout playbackPlayerSwitchAnchorLayout;
    //播放器渲染视图view
    private PolyvPlaybackVideoView videoView;
    //子播放器渲染视图view
    private PolyvAuxiliaryVideoview subVideoView;
    //倒计时
    private LinearLayout llAuxiliaryCountDown;
    private TextView tvCountDown;
    //浮窗关闭按钮
    private ImageView closeFloatingView;
    //播放器presenter
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playbackPlayerPresenter;

    //播放控制按钮
    private ImageView playCenterView;

    //logo view
    private PLVPlayerLogoView logoView;
    //marquee view
    private PLVMarqueeView marqueeView;

    //watermark view
    private PLVWatermarkView watermarkView;

    //播放失败/加载缓慢的View
    private PLVECLiveNoStreamView playErrorView;

    private TextView playbackPlayerFloatingPlayingPlaceholderTv;
    private PLVRoundRectLayout playbackAutoContinueSeekTimeHintLayout;
    private TextView playbackAutoContinueSeekTimeTv;

    //播放器是否小窗播放状态
    private boolean isVideoViewPlayingInFloatWindow;
    //播放器及其布局在VideoLayout中设置的适配方式
    private int fitMode = PLVECFitMode.FIT_NONE;

    //Listener
    private ViewTreeObserver.OnGlobalLayoutListener onSubVideoViewLayoutListener;
    private IPLVECVideoLayout.OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECPlaybackVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECPlaybackVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECPlaybackVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_playback_player_layout, this, true);

        playbackPlayerSwitchAnchorLayout = findViewById(R.id.plvec_playback_player_switch_anchor_layout);
        videoView = findViewById(R.id.plvec_playback_video_item);
        subVideoView = findViewById(R.id.sub_video_view);
        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);
        closeFloatingView = findViewById(R.id.close_floating_iv);
        closeFloatingView.setOnClickListener(this);
        videoView.setSubVideoView(subVideoView);

        playCenterView = findViewById(R.id.play_center);
        hidePlayCenterView();
        playCenterView.setOnClickListener(this);

        logoView = findViewById(R.id.logo_view);

        watermarkView = findViewById(R.id.plvec_watermark_view);
        playErrorView = findViewById(R.id.plvec_play_error_ly);
        marqueeView = ((Activity) getContext()).findViewById(R.id.plvec_marquee_view);

        playbackPlayerFloatingPlayingPlaceholderTv = findViewById(R.id.plvec_playback_player_floating_playing_placeholder_tv);
        playbackAutoContinueSeekTimeHintLayout = findViewById(R.id.plvec_playback_auto_continue_seek_time_hint_layout);
        playbackAutoContinueSeekTimeTv = findViewById(R.id.plvec_playback_auto_continue_seek_time_tv);

        initPlayErrorView();
        initSubVideoViewChangeListener();
        observeFloatingPlayer();
    }

    private void initPlayErrorView() {
        playErrorView.setPlaceHolderImg(R.drawable.plv_bg_player_error_ic);
        playErrorView.setOnRefreshViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playbackPlayerPresenter.startPlay();
            }
        });
    }

    private void initSubVideoViewChangeListener() {
        onSubVideoViewLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (subVideoView == null) {
                    return;
                }
                if (subVideoView.getAdHeadImage() != null) {
                    llAuxiliaryCountDown.setY(subVideoView.getAdHeadImage().getY() + ConvertUtils.dp2px(15));
                } else {
                    float y = subVideoView.getY();

                    int viewHeight = PLVVideoSizeUtils.getVideoWH(subVideoView)[1];
                    if (subVideoView.getAspectRatio() == PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT) {
                        int surHeight = subVideoView.getHeight();
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
                .observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean isShowingBoolean) {
                        final boolean isShowing = isShowingBoolean != null && isShowingBoolean;
                        isVideoViewPlayingInFloatWindow = isShowing;
                        videoView.setNeedGestureDetector(!isShowing);
                        subVideoView.setNeedGestureDetector(!isShowing);
                        playbackPlayerFloatingPlayingPlaceholderTv.setVisibility(isShowing ? VISIBLE : GONE);
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVECVideoLayout定义的common方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        playbackPlayerPresenter = new PLVPlaybackPlayerPresenter(liveRoomDataManager);
        playbackPlayerPresenter.registerView(playbackPlayerView);
        playbackPlayerPresenter.init();
        playbackPlayerPresenter.setAllowOpenAdHead(isAllowOpenAdhead);
    }

    @Override
    public void startPlay() {
        playbackPlayerPresenter.startPlay();
    }

    @Override
    public void pause() {
        playbackPlayerPresenter.pause();
    }

    @Override
    public void resume() {
        playbackPlayerPresenter.resume();
    }

    @Override
    public boolean isInPlaybackState() {
        return playbackPlayerPresenter.isInPlaybackState();
    }

    @Override
    public boolean isPlaying() {
        return playbackPlayerPresenter.isPlaying();
    }

    @Override
    public boolean isSubVideoViewShow() {
        return playbackPlayerPresenter.isSubVideoViewShow();
    }

    @Override
    public String getSubVideoViewHerf() {
        return playbackPlayerPresenter.getSubVideoViewHerf();
    }

    @Override
    public void setPlayerVolume(int volume) {
        playbackPlayerPresenter.setPlayerVolume(volume);
    }

    @Override
    public LiveData<PLVPlayerState> getPlayerState() {
        return playbackPlayerPresenter.getData().getPlayerState();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void setFloatingWindow(boolean b) {
        PLVCommonLog.d(TAG, "setFloatingWindow: " + b);
    }

    @Override
    public PLVSwitchViewAnchorLayout getPlayerSwitchAnchorLayout() {
        return playbackPlayerSwitchAnchorLayout;
    }

    @Override
    public void setVideoViewRect(Rect videoViewRect) {
        this.videoViewRect = videoViewRect;
        if (!isVideoViewPlayingInFloatWindow) {
            fitVideoRatioAndRect();
        }
    }

    @Override
    public void destroy() {
        if (playbackPlayerPresenter != null) {
            playbackPlayerPresenter.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVECVideoLayout定义的live方法，空实现">
    @Override
    public int getLinesPos() {
        return 0;
    }

    @Override
    public int getLinesCount() {
        return 0;
    }

    @Override
    public void changeLines(int linesPos) {
        PLVCommonLog.d(TAG, "直播带货回放场景 暂无切换线路");
    }

    @Override
    public int getBitratePos() {
        return 0;
    }

    @Override
    public List<PolyvDefinitionVO> getBitrateVO() {
        return null;
    }

    @Override
    public void changeBitRate(int bitratePos) {
        PLVCommonLog.d(TAG, "直播带货回放场景 暂无切换码率功能");
    }

    @Override
    public int getMediaPlayMode() {
        return 0;
    }

    @Override
    public void changeMediaPlayMode(int mediaPlayMode) {
        PLVCommonLog.d(TAG, "直播带货回放场景 暂无切换音视频模式的功能");
    }

    @Override
    public boolean isCurrentLowLatencyMode() {
        return false;
    }

    @Override
    public void switchLowLatencyMode(boolean isLowLatencyMode) {
        // 回放没有切换延迟模式
    }

    @Override
    public LiveData<com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO> getLivePlayInfoVO() {
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVECVideoLayout定义的playback方法">
    @Override
    public int getDuration() {
        return playbackPlayerPresenter.getDuration();
    }

    @Override
    public int getVideoCurrentPosition() {
        return playbackPlayerPresenter.getVideoCurrentPosition();
    }

    @Override
    public void seekTo(int progress, int max) {
        playbackPlayerPresenter.seekTo(progress, max);
    }

    @Override
    public void setSpeed(float speed) {
        playbackPlayerPresenter.setSpeed(speed);
    }

    @Override
    public float getSpeed() {
        return playbackPlayerPresenter.getSpeed();
    }

    @Override
    public void addOnSeekCompleteListener(IPLVOnDataChangedListener<Integer> listener) {
        playbackPlayerPresenter.getData().getSeekCompleteVO().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public LiveData<PLVPlayInfoVO> getPlaybackPlayInfoVO() {
        return playbackPlayerPresenter.getData().getPlayInfoVO();
    }

    @Override
    public void changePlaybackVid(String vid) {
        playbackPlayerPresenter.setPlayerVid(vid);
    }

    @Override
    public void changePlaybackVidAndPlay(String vid) {
        playbackPlayerPresenter.setPlayerVidAndPlay(vid);
    }

    @Override
    public String getSessionId() {
        return playbackPlayerPresenter.getSessionId();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式的view层实现">
    private IPLVPlaybackPlayerContract.IPlaybackPlayerView playbackPlayerView = new PLVAbsPlaybackPlayerView() {
        @Override
        public void setPresenter(@NonNull IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter presenter) {
            super.setPresenter(presenter);
            playbackPlayerPresenter = presenter;
        }

        @Override
        public PolyvPlaybackVideoView getPlaybackVideoView() {
            return videoView;
        }

        @Override
        public PolyvAuxiliaryVideoview getSubVideoView() {
            return subVideoView;
        }

        @Override
        public View getPlayErrorIndicator() {
            return playErrorView;
        }

        @Override
        public View getBufferingIndicator() {
            return super.getBufferingIndicator();
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
        public void onPrepared() {
            super.onPrepared();
            fitMode = PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_VIDEOVIEW;
            if (!isVideoViewPlayingInFloatWindow) {
                PLVVideoSizeUtils.fitVideoRatioAndRect(videoView, videoView.getParent(), videoViewRect);

            }
            //需要将水印与视频区域大小匹配
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
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            PLVPlayErrorMessageUtils.showOnPlayError(playErrorView, error, liveRoomDataManager.getConfig().isLive());
            fitMode = PLVECFitMode.FIT_VIDEO_RECT_FALSE;
            if (!isVideoViewPlayingInFloatWindow) {
                PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            }
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
        public void onSubVideoViewCountDown(boolean isOpenAdHead, int totalTime, int remainTime, int adStage) {
            if (isOpenAdHead) {
                llAuxiliaryCountDown.setVisibility(VISIBLE);
                tvCountDown.setText("广告：" + remainTime + "s");
            }
        }

        @Override
        public void onSubVideoViewPlay(boolean isFirst) {
            super.onSubVideoViewPlay(isFirst);
            fitMode = PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_SUB_VIDEOVIEW;
            if (!isVideoViewPlayingInFloatWindow) {
                PLVVideoSizeUtils.fitVideoRatioAndRect(subVideoView, videoView.getParent(), videoViewRect);//传主播放器viewParent
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
        }

        @Override
        public void onBufferStart() {
            super.onBufferStart();
            PLVCommonLog.i(TAG, "开始缓冲");
        }

        @Override
        public void onBufferEnd() {
            super.onBufferEnd();
            PLVCommonLog.i(TAG, "缓冲结束");
        }

        @Override
        public void onAutoContinuePlaySeeked(int seekTo) {
            playbackAutoContinueSeekTimeTv.setText(PLVTimeUtils.generateTime(seekTo));
            PLVViewUtil.showViewForDuration(playbackAutoContinueSeekTimeHintLayout, seconds(3).toMillis());
        }

        @Override
        public void updatePlayInfo(PLVPlayInfoVO playInfoVO) {
            if (playInfoVO != null && isInPlaybackState()
                    && !playInfoVO.isPlaying()) {
                showPlayCenterView();
            } else {
                hidePlayCenterView();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 播放暂停按钮的显示、隐藏">
    private void hidePlayCenterView() {
        playCenterView.setVisibility(GONE);
    }

    private void showPlayCenterView() {
        if (!isSubVideoViewShow()) {
            playCenterView.setVisibility(VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 适配播放器的填充模式及其布局位置">
    private void fitVideoRatioAndRect() {
        if (fitMode == PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_SUB_VIDEOVIEW) {
            PLVVideoSizeUtils.fitVideoRatioAndRect(subVideoView, videoView.getParent(), videoViewRect);//传主播放器viewParent
        } else if (fitMode == PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_VIDEOVIEW) {
            PLVVideoSizeUtils.fitVideoRatioAndRect(videoView, videoView.getParent(), videoViewRect);
        } else if (fitMode == PLVECFitMode.FIT_VIDEO_RECT_FALSE) {
            PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
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
            resume();
        }
    }
    // </editor-fold>
}
