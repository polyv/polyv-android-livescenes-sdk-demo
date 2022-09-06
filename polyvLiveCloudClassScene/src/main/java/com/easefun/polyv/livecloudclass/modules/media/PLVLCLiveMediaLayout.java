package com.easefun.polyv.livecloudclass.modules.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.media.controller.IPLVLCLiveLandscapePlayerController;
import com.easefun.polyv.livecloudclass.modules.media.controller.IPLVLCLiveMediaController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCDanmuController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCLandscapeMessageSender;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuFragment;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuWrapper;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCLandscapeMessageSendPanel;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindow;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLightTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLiveAudioModeView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCNetworkTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVideoLoadingLayout;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVolumeTipsView;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayErrorMessageUtils;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.PLVLivePlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.live.view.PLVAbsLivePlayerView;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVRewardSVGAHelper;
import com.easefun.polyv.livecommon.module.modules.watermark.IPLVWatermarkView;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlaceHolderView;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.log.elog.PLVELogsService;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.log.player.PLVPlayerElog;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;
import com.plv.thirdpart.blankj.utilcode.util.TimeUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 云课堂场景下的直播播放器布局，实现 IPLVLCMediaLayout 接口
 */
public class PLVLCLiveMediaLayout extends FrameLayout implements IPLVLCMediaLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLCLiveVideoLayout";
    private static final float RATIO_WH = 16f / 9;//播放器竖屏宽高使用16:9比例
    private static final String DEFAULT_COVER_IMAGE = "https://s1.videocc.net/default-img/channel/default-splash.png";
    /**
     * 横屏聊天布局可见性与弹幕开关同步
     * true -> 当弹幕关闭时，也隐藏横屏聊天布局
     */
    private static final boolean SYNC_LANDSCAPE_CHATROOM_LAYOUT_VISIBILITY_WITH_DANMU = true;

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //播放器SwitchView，支持和外部View切换位置
    private PLVSwitchViewAnchorLayout playerSwitchAnchor;
    private FrameLayout flLivePlayerSwitchView;
    private View playerView;
    //主播放器渲染视图view
    private PolyvLiveVideoView videoView;
    //子播放器渲染视图view
    private PolyvAuxiliaryVideoview subVideoView;

    private TextView tvCountDown;
    private LinearLayout llAuxiliaryCountDown;

    //音频模式view
    private PLVLCLiveAudioModeView audioModeView;
    // Logo
    private PLVPlayerLogoView logoView;
    //主播放器控制栏
    private IPLVLCLiveMediaController mediaController;
    //封面图
    private ImageView coverImageView;

    //播放器缓冲显示的view
    private PLVLCVideoLoadingLayout loadingView;
    //当前没有直播显示的view
    private PLVPlaceHolderView noStreamView;
    //播放失败/加载缓慢的占位View
    private PLVPlaceHolderView playErrorView;
    //直播停止时显示的view
    private View stopStreamView;
    // 网络较差时提示view
    private PLVLCNetworkTipsView networkTipsView;

    //tips view
    private PLVLCLightTipsView lightTipsView;
    private PLVLCVolumeTipsView volumeTipsView;

    //横屏聊天区
    private PLVLCChatLandscapeLayout chatLandscapeLayout;

    //弹幕
    private IPLVLCDanmuController danmuController;
    //弹幕包装器
    private PLVLCDanmuWrapper danmuWrapper;
    //信息发送输入框弹窗
    private IPLVLCLandscapeMessageSender landscapeMessageSender;

    //跑马灯
    private IPLVMarqueeView marqueeView = null;
    private IPLVWatermarkView watermarkView = null;

    //截图，用于刷新直播的时候防止黑屏
    private ImageView screenshotIV;

    //倒计时
    private TextView timeCountDownTv;
    private TextView livePlayerFloatingPlayingPlaceholderTv;
    //开始时间倒计时器
    private CountDownTimer startTimeCountDown;
    //直播开始时间
    private String liveStartTime;

    //横屏时为连麦预留的右偏移量
    private int landscapeMarginRightForLinkMicLayout;
    //是否加入了RTC
    private boolean isJoinRTC;
    //是否加入了连麦
    private boolean isJoinLinkMic;
    // 是否无延迟观看
    private boolean isLowLatency = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();
    //ppt或连麦在点击右下角的显示按钮后的显示状态，默认ppt或者连麦是显示的
    private boolean isClickShowSubTab = true;
    private boolean isShowLandscapeRTCLayout = false;
    private boolean isLandscape;

    //打赏动画
    private SVGAImageView rewardSvgaView;
    //打赏动画辅助类
    private SVGAParser svgaParser;
    private PLVRewardSVGAHelper svgaHelper;
    private boolean isRewardEffectShow = true;

    private boolean isOnlyAudio = false;
    private String coverImage = DEFAULT_COVER_IMAGE;

    //播放器presenter
    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;
    private PLVLCFloatingWindow floatingWindow;

    //Listener
    private IPLVLCMediaLayout.OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLiveMediaLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLiveMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLiveMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_player_layout, this);
        playerSwitchAnchor = findViewById(R.id.plvlc_playback_switch_anchor_player);
        flLivePlayerSwitchView = findViewById(R.id.plvlc_playback_fl_player_switch_view_parent);
        videoView = findViewById(R.id.live_video_view);
        playerView = videoView.findViewById(PolyvBaseVideoView.IJK_VIDEO_ID);
        subVideoView = findViewById(R.id.sub_video_view);
        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);
        audioModeView = findViewById(R.id.audio_mode_ly);
        logoView = findViewById(R.id.live_logo_view);
        loadingView = findViewById(R.id.video_loading_view);
        noStreamView = findViewById(R.id.no_stream_ly);
        playErrorView = findViewById(R.id.play_error_ly);
        stopStreamView = findViewById(R.id.stop_stream_ly);
        lightTipsView = findViewById(R.id.light_view);
        volumeTipsView = findViewById(R.id.volume_view);
        screenshotIV = findViewById(R.id.screenshot_iv);
        timeCountDownTv = findViewById(R.id.time_count_down_tv);
        mediaController = findViewById(R.id.controller_view);
        chatLandscapeLayout = findViewById(R.id.chat_landscape_ly);
        rewardSvgaView = findViewById(R.id.plvlc_reward_svg);

        //初始化
        svgaParser = new SVGAParser(getContext());
        svgaHelper = new PLVRewardSVGAHelper();
        svgaHelper.init(rewardSvgaView, svgaParser);


        coverImageView = findViewById(R.id.plvlc_cover_image_view);
        watermarkView = findViewById(R.id.polyv_watermark_view);
        networkTipsView = (PLVLCNetworkTipsView) findViewById(R.id.network_tips_view);
        livePlayerFloatingPlayingPlaceholderTv = findViewById(R.id.plvlc_live_player_floating_playing_placeholder_tv);

        // 底部占位图
        PLVPlaceHolderView placeHolderView = new PLVPlaceHolderView(getContext());
        placeHolderView.setVisibility(VISIBLE);
        placeHolderView.setPlaceHolderText("");
        addView(placeHolderView, 0);

        initVideoView();
        initPlayErrorView();
        initDanmuView();
        initMediaController();
        initAudioModeView();
        initLoadingView();
        initSwitchView();
        initNetworkTipsLayout();
        initChatLandscapeLayout();
        initLayoutWH();

        initFloatingPlayer();
    }

    private void initVideoView() {
        //设置noStreamView
        noStreamView.setPlaceHolderImg(R.drawable.plvlc_bg_player_no_stream);
        noStreamView.setPlaceHolderText(getResources().getString(R.string.plv_player_video_live_no_stream));

        videoView.setSubVideoView(subVideoView);
        videoView.setAudioModeView(audioModeView);
        videoView.setPlayerBufferingIndicator(loadingView);
        videoView.setNoStreamIndicator(noStreamView);
        videoView.setStopStreamIndicator(stopStreamView);
        videoView.setMediaController(mediaController);
        //设置跑马灯
        marqueeView = ((Activity) getContext()).findViewById(R.id.polyv_marquee_view);
    }

    private void initPlayErrorView() {
        playErrorView.setPlaceHolderImg(R.drawable.plv_bg_player_error_ic);
        playErrorView.setOnChangeLinesViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.showMoreLayout();
            }
        });
        playErrorView.setOnRefreshViewClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                livePlayerPresenter.restartPlay();
            }
        });
    }

    private void initDanmuView() {
        danmuController = new PLVLCDanmuFragment();
        FragmentTransaction fragmentTransaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.danmu_ly, (Fragment) danmuController, "danmuFragment").commitAllowingStateLoss();

        danmuWrapper = new PLVLCDanmuWrapper(this);
        danmuWrapper.setDanmuController(danmuController);

        landscapeMessageSender = new PLVLCLandscapeMessageSendPanel((AppCompatActivity) getContext(), this);
        landscapeMessageSender.setOnSendMessageListener(new IPLVLCLandscapeMessageSender.OnSendMessageListener() {
            @Override
            public void onSend(String message) {
                if (onViewActionListener != null) {
                    //发送信息到聊天室
                    Pair<Boolean, Integer> result = onViewActionListener.onSendChatMessageAction(message);
                    if (!result.first) {
                        ToastUtils.showShort(getResources().getString(R.string.plv_chat_toast_send_msg_failed) + ": " + result.second);
                    }
                }
            }
        });
    }

    private void initMediaController() {
        mediaController.setOnViewActionListener(new IPLVLCLiveMediaController.OnViewActionListener() {
            @Override
            public void onStartSendMessageAction() {
                landscapeMessageSender.openMessageSender();
            }

            @Override
            public void onClickShowOrHideSubTab(boolean toShow) {
                isClickShowSubTab = toShow;
                //如果加入连麦，并且是横屏状态，则在显示和隐藏连麦布局时，播放器布局的尺寸也要变化。
                if (isJoinRTC && PLVScreenUtils.isLandscape(getContext())) {
                    if (toShow) {
                        //switch anchor
                        showLandscapeRTCLayout(true);
                    } else {
                        //switch anchor
                        showLandscapeRTCLayout(false);
                    }
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onClickShowOrHideSubTab(toShow);
                }
            }

            @Override
            public void onShowBulletinAction() {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowBulletinAction();
                }
            }

            @Override
            public void onShowRewardView() {
                if(onViewActionListener != null){
                    onViewActionListener.onShowRewardAction();
                }
            }

            @Override
            public void onSendLikesAction() {
                if (onViewActionListener != null) {
                    onViewActionListener.onSendLikesAction();
                }
            }

            @Override
            public void onShow(boolean show) {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowMediaController(show);
                }
            }

            @Override
            public void onPPTTurnPage(String type) {
                onViewActionListener.onPPTTurnPage(type);
            }

            @Override
            public void onChangeLowLatencyMode(boolean isLowLatency) {
                PLVLCLiveMediaLayout.this.isLowLatency = isLowLatency;
                livePlayerPresenter.startPlay(isLowLatency);
                if (isLowLatency) {
                    PLVELogsService.getInstance().addStaticsLog(PLVPlayerElog.class, PLVPlayerElog.Event.SWITCH_TO_NO_DELAY, " isLowLatency: " + isLowLatency);
                } else {
                    PLVELogsService.getInstance().addStaticsLog(PLVPlayerElog.class, PLVPlayerElog.Event.SWITCH_TO_DELAY, " isLowLatency: " + isLowLatency);
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onWatchLowLatency(isLowLatency);
                }
                networkTipsView.setIsLowLatency(isLowLatency);
            }

            @Override
            public void onRtcPauseResume(boolean toPause) {
                if (onViewActionListener != null) {
                    onViewActionListener.onRtcPauseResume(toPause);
                }
            }

            @Override
            public boolean isRtcPausing() {
                if (onViewActionListener != null) {
                    return onViewActionListener.isRtcPausing();
                }
                return false;
            }

            @Override
            public void onClickFloating() {
                if (floatingWindow != null) {
                    floatingWindow.showByUser(!floatingWindow.isRequestingShowByUser());
                }
            }
        });
    }

    private void initAudioModeView() {
        audioModeView.setOnChangeVideoModeListener(new PLVLCLiveAudioModeView.OnChangeVideoModeListener() {
            @Override
            public void onClickPlayVideo() {
                livePlayerPresenter.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
            }
        });
    }

    private void initLoadingView() {
        loadingView.bindVideoView(videoView);
    }

    private void initSwitchView() {
        playerSwitchAnchor.setOnSwitchListener(new PLVSwitchViewAnchorLayout.IPLVSwitchViewAnchorLayoutListener() {
            @Override
            protected void onSwitchElsewhereBefore() {
                View childOfAnchor;
                try {
                    childOfAnchor = playerSwitchAnchor.getSwitchView();
                } catch (IllegalAccessException e) {
                    PLVCommonLog.exception(e);
                    return;
                }
                PLVCommonLog.d(TAG, "onSwitchElsewhereBefore-> childOfAnchor= " + childOfAnchor);
                //主屏幕的switch view anchor内的switch view可能是播放器，PPT，连麦item。只有当他是播放器时，
                //才处理播放器的switch逻辑
                if (childOfAnchor == flLivePlayerSwitchView) {
                    videoView.removeView(playerView);
                    videoView.removeView(screenshotIV);
                    videoView.removeView(audioModeView);
                    videoView.removeView(coverImageView);
                    videoView.removeView(logoView);
                    videoView.removeView(loadingView);
                    videoView.removeView(noStreamView);
                    videoView.removeView(stopStreamView);

                    flLivePlayerSwitchView.addView(playerView);
                    flLivePlayerSwitchView.addView(screenshotIV);
                    flLivePlayerSwitchView.addView(audioModeView);
                    flLivePlayerSwitchView.addView(coverImageView);
                    flLivePlayerSwitchView.addView(logoView);
                    flLivePlayerSwitchView.addView(loadingView);
                    flLivePlayerSwitchView.addView(noStreamView);
                    flLivePlayerSwitchView.addView(stopStreamView);
                }
            }

            @Override
            protected void onSwitchBackAfter() {
                View childOfAnchor;
                try {
                    childOfAnchor = playerSwitchAnchor.getSwitchView();
                } catch (IllegalAccessException e) {
                    PLVCommonLog.exception(e);
                    return;
                }
                PLVCommonLog.d(TAG, "onSwitchBackAfter-> childOfAnchor= " + childOfAnchor);
                //主屏幕的switch view anchor内的switch view可能是播放器，PPT，连麦item。只有当他是播放器时，
                //才处理播放器的switch逻辑
                if (childOfAnchor == flLivePlayerSwitchView) {
                    flLivePlayerSwitchView.removeAllViews();
                    videoView.addView(playerView, 0);
                    videoView.addView(screenshotIV);
                    videoView.addView(audioModeView);
                    videoView.addView(coverImageView);
                    videoView.addView(logoView);
                    videoView.addView(loadingView);
                    videoView.addView(noStreamView);
                    videoView.addView(stopStreamView);
                }
            }
        });
    }

    private void initNetworkTipsLayout() {
        networkTipsView.setOnClickChangeNormalLatencyListener(new PLVLCNetworkTipsView.OnClickChangeNormalLatencyListener() {
            @Override
            public void onClickChangeNormalLatency() {
                PLVLCLiveMediaLayout.this.isLowLatency = false;
                livePlayerPresenter.startPlay(false);
                if (onViewActionListener != null) {
                    onViewActionListener.onWatchLowLatency(false);
                }
                mediaController.notifyLowLatencyUpdate(false);
            }
        });
    }

    private void initChatLandscapeLayout() {
        chatLandscapeLayout.setOnRoomStatusListener(new PLVLCChatLandscapeLayout.OnRoomStatusListener() {
            @Override
            public void onStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus) {
                mediaController.notifyChatroomStatusChanged(isCloseRoomStatus, isFocusModeStatus);
                if (isCloseRoomStatus || isFocusModeStatus) {
                    landscapeMessageSender.hideMessageSender();
                }
            }
        });
    }

    private void initLayoutWH() {
        //调整播放器布局的宽高
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams vlp = getLayoutParams();
                vlp.width = -1;
                vlp.height = ScreenUtils.isPortrait() ? (int) (getWidth() / RATIO_WH) : -1;
                setLayoutParams(vlp);
            }
        });
    }

    private void initFloatingPlayer() {
        floatingWindow = PLVDependManager.getInstance().get(PLVLCFloatingWindow.class);
        floatingWindow.bindContentView(playerSwitchAnchor);

        PLVFloatingPlayerManager.getInstance().getFloatingViewShowState()
                .observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean isShowingBoolean) {
                        final boolean isShowing = isShowingBoolean != null && isShowingBoolean;
                        livePlayerFloatingPlayingPlaceholderTv.setVisibility(isShowing ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void observeLinkMicStatus(IPLVLivePlayerContract.ILivePlayerPresenter presenter) {
        presenter.getData().getLinkMicState().observe((LifecycleOwner) getContext(), new Observer<Pair<Boolean, Boolean>>() {
            @Override
            public void onChanged(@Nullable Pair<Boolean /*openLinkMic*/, Boolean /*audioLinkMic*/> pair) {
                if (pair == null || pair.first == null) {
                    return;
                }
                updateWhenLinkMicOpenStatusChanged(pair.first);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的common方法">
    @Override
    public void init(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        floatingWindow.setLiveRoomData(liveRoomDataManager);

        observeLiveRoomData();

        livePlayerPresenter = new PLVLivePlayerPresenter(liveRoomDataManager);
        livePlayerPresenter.registerView(livePlayerView);
        livePlayerPresenter.init();
        observeLinkMicStatus(livePlayerPresenter);

        mediaController.setLivePlayerPresenter(livePlayerPresenter);
    }

    @Override
    public void startPlay() {
        post(new Runnable() {
            @Override
            public void run() {
                livePlayerPresenter.startPlay(isLowLatency);
                if (onViewActionListener != null) {
                    onViewActionListener.onWatchLowLatency(isLowLatency);
                }
                networkTipsView.setIsLowLatency(isLowLatency);
            }
        });
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
    public void stop() {
        livePlayerPresenter.stop();
    }

    @Override
    public boolean isPlaying() {
        return livePlayerPresenter.isPlaying();
    }

    @Override
    public void setVolume(int volume) {
        livePlayerPresenter.setVolume(volume);
    }

    @Override
    public int getVolume() {
        return livePlayerPresenter.getVolume();
    }

    @Override
    public void sendDanmaku(CharSequence message) {
        danmuController.sendDanmaku(message);
    }

    @Override
    public void updateOnClickCloseFloatingView() {
        mediaController.show();
        mediaController.updateOnClickCloseFloatingView();
    }

    @Override
    public PLVSwitchViewAnchorLayout getPlayerSwitchView() {
        return playerSwitchAnchor;
    }

    @Override
    public PLVLCChatLandscapeLayout getChatLandscapeLayout() {
        return chatLandscapeLayout;
    }

    @Override
    public PLVPlayerLogoView getLogoView() {
        return logoView;
    }

    @Override
    public ImageView getCardEnterView() {
        return mediaController.getLandscapeController().getCardEnterView();
    }

    @Override
    public TextView getCardEnterCdView() {
        return mediaController.getLandscapeController().getCardEnterCdView();
    }

    @Override
    public PLVTriangleIndicateTextView getCardEnterTipsView() {
        return mediaController.getLandscapeController().getCardEnterTipsView();
    }

    @Override
    public void setOnViewActionListener(IPLVLCMediaLayout.OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void addOnPlayerStateListener(IPLVOnDataChangedListener<PLVPlayerState> listener) {
        livePlayerPresenter.getData().getPlayerState().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnPPTShowStateListener(IPLVOnDataChangedListener<Boolean> listener) {
        livePlayerPresenter.getData().getPPTShowState().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public boolean hideController() {
        boolean isShowing = mediaController.isShowing();
        mediaController.hide();
        return isShowing;
    }

    @Override
    public void showController() {
        mediaController.show();
    }

    @Override
    public boolean onBackPressed() {
        if (ScreenUtils.isLandscape()) {
            PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        if (livePlayerPresenter != null) {
            livePlayerPresenter.destroy();
        }

        if (mediaController != null) {
            mediaController.clean();
        }

        if (danmuWrapper != null) {
            danmuWrapper.release();
        }

        if (danmuController != null) {
            danmuController.release();
        }

        if (landscapeMessageSender != null) {
            landscapeMessageSender.dismiss();
        }

        stopLiveCountDown();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的live方法">
    @Override
    public void setLandscapeControllerView(@NonNull IPLVLCLiveLandscapePlayerController landscapeControllerView) {
        mediaController.setLandscapeController(landscapeControllerView);
        final View danmuSwitchView = landscapeControllerView.getDanmuSwitchView();
        danmuSwitchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                danmuWrapper.dispatchDanmuSwitchOnClicked(v);
                mediaController.dispatchDanmuSwitchOnClicked(v);
                if (SYNC_LANDSCAPE_CHATROOM_LAYOUT_VISIBILITY_WITH_DANMU) {
                    final boolean showChatLayout = !danmuSwitchView.isSelected();
                    chatLandscapeLayout.toggle(showChatLayout);
                }
            }
        });
        danmuWrapper.setDanmuSwitchLandView(danmuSwitchView);
    }

    @Override
    public IPLVLCLiveLandscapePlayerController getLandscapeControllerView() {
        return mediaController.getLandscapeController();
    }

    @Override
    public void updateViewerCount(long viewerCount) {
        mediaController.updateViewerCount(viewerCount);
    }

    @Override
    public void updatePPTStatusChange(PLVPPTStatus plvpptStatus) {
        mediaController.updatePPTStatusChange(plvpptStatus);
    }

    @Override
    public void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth) {
        isJoinRTC = true;
        landscapeMarginRightForLinkMicLayout = linkMicLayoutLandscapeWidth;

        mediaController.updateWhenJoinRtc(liveRoomDataManager.isSupportRTC());

        if (liveRoomDataManager.isSupportRTC()) {
            //如果支持RTC，连麦时停止播放器播放，使用rtc视频流+rtc音频流
            stop();
        } else {
            //如果不支持RTC，连麦时静音播放器，使用播放器的cdn视频流+rtc音频流
            livePlayerPresenter.setPlayerVolume(0);
        }
        //禁用播放器手势
        livePlayerPresenter.setNeedGestureDetector(false);

        mediaController.show();

        if (PLVScreenUtils.isPortrait(getContext())) {
            setPortrait();
        } else {
            setLandscape();
        }
    }

    @Override
    public void updateWhenLinkMicOpenStatusChanged(boolean isOpen) {
        mediaController.updateWhenLinkMicOpenOrClose(isOpen);
    }

    @Override
    public void updateWhenLeaveRTC() {
        isJoinRTC = false;
        landscapeMarginRightForLinkMicLayout = 0;

        mediaController.updateWhenLeaveRtc();

        if (liveRoomDataManager.isSupportRTC()) {
            startPlay();
        }
        //恢复播放器手势
        livePlayerPresenter.setNeedGestureDetector(true);
        //恢复播放器音量
        livePlayerPresenter.setPlayerVolume(100);

        if (PLVScreenUtils.isPortrait(getContext())) {
            setPortrait();
        } else {
            setLandscape();
        }
    }

    @Override
    public void updateWhenRequestJoinLinkMic(boolean requestJoin) {
        mediaController.updateWhenRequestJoinLinkMic(requestJoin);
    }

    @Override
    public void updateWhenJoinLinkMic() {
        isJoinLinkMic = true;
        videoView.setIsLinkMic(true);
        mediaController.updateWhenJoinLinkMic(liveRoomDataManager.isSupportRTC());
        networkTipsView.setIsLinkMic(true);
    }

    @Override
    public void updateWhenLeaveLinkMic() {
        isJoinLinkMic = false;
        videoView.setIsLinkMic(false);
        mediaController.updateWhenLeaveLinkMic();
        networkTipsView.setIsLinkMic(true);
    }

    @Override
    public void acceptNetworkQuality(int quality) {
        networkTipsView.acceptNetworkQuality(quality);
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
    public void addOnSeiDataListener(IPLVOnDataChangedListener<Long> listener) {
        livePlayerPresenter.getData().getSeiData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void setOnRTCPlayEventListener(IPolyvLiveListenerEvent.OnRTCPlayEventListener listener) {
        videoView.setOnRTCPlayEventListener(listener);
    }

    @Override
    public void setShowLandscapeRTCLayout() {
        isShowLandscapeRTCLayout = true;
        if (isLandscape) {
            //如果此时就是横屏，那么显示RTC layout
            showLandscapeRTCLayout(true);
        } else {
            //如果此时是竖屏，那么不做操作，等到旋转到横屏的时候再预留右边距
            PLVCommonLog.d(TAG, "PLVLCLiveMediaLayout.setShowLandscapeRTCLayout-->isLandscape=false. We'll wait for portrait to show landscape rtc layout");
        }
    }

    @Override
    public void setHideLandscapeRTCLayout() {
        isShowLandscapeRTCLayout = false;
        if (isLandscape) {
            //如果此时是横屏，那么隐藏RTC layout
            showLandscapeRTCLayout(false);
        } else {
            //如果此时是竖屏，右边距已经是0了，那么不做操作。
            PLVCommonLog.d(TAG, "PLVLCLiveMediaLayout.setHideLandscapeRTCLayout-->isLandscape=false. We do noting when it is portrait");
        }
    }

    @Override
    public void setLandscapeRewardEffectVisibility(boolean isShow) {
        isRewardEffectShow = isShow;
        if(!isShow){
            svgaHelper.clear();
            rewardSvgaView.setVisibility(INVISIBLE);
        } else {
            rewardSvgaView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onTurnPageLayoutChange(boolean toShow) {
        mediaController.setTurnPageLayoutStatus(toShow);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的playback方法，空实现">
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

    }

    @Override
    public void setSpeed(float speed) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public void setPPTView(IPolyvPPTView pptView) {

    }

    @Override
    public void addOnPlayInfoVOListener(IPLVOnDataChangedListener<PLVPlayInfoVO> listener) {

    }

    @Override
    public void addOnSeekCompleteListener(IPLVOnDataChangedListener<Integer> listener) {

    }

    @Override
    public void updatePlayBackVideVid(String vid) {

    }

    @Override
    public void updatePlayBackVideVidAndPlay(String vid) {

    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public void setChatPlaybackEnabled(boolean isChatPlaybackEnabled) {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式的view实现">
    private IPLVLivePlayerContract.ILivePlayerView livePlayerView = new PLVAbsLivePlayerView() {
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
            return noStreamView;
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
        public IPLVMarqueeView getMarqueeView(){
            return marqueeView;
        }

        @Override
        public IPLVWatermarkView getWatermarkView() {
            return watermarkView;
        }

        @Override
        public void onSubVideoViewLoadImage(String imageUrl, ImageView imageView) {
            PLVImageLoader.getInstance().loadImage(subVideoView.getContext(), imageUrl, imageView);
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            imageView.setLayoutParams(lp);
        }

        @Override
        public void onSubVideoViewClick(boolean mainPlayerIsPlaying) {
            super.onSubVideoViewClick(mainPlayerIsPlaying);

            if (!mainPlayerIsPlaying) {
                mediaController.updateWhenSubVideoViewClick(mainPlayerIsPlaying);
                if (mediaController.isShowing()) {
                    mediaController.hide();
                } else {
                    mediaController.show();
                }
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
        public void onSubVideoViewVisiblityChanged(boolean isOpenAdHead, boolean isShow) {
            if (isOpenAdHead) {
                if (!isShow) {
                    llAuxiliaryCountDown.setVisibility(GONE);
                }
            } else {
                llAuxiliaryCountDown.setVisibility(GONE);
            }
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            PLVPlayErrorMessageUtils.showOnPlayError(playErrorView, error, liveRoomDataManager.getConfig().isLive());
            PLVCommonLog.e(TAG, tips);
        }

        @Override
        public void onLoadSlow(int loadedTime, boolean isBufferEvent) {
            super.onLoadSlow(loadedTime, isBufferEvent);
            PLVPlayErrorMessageUtils.showOnLoadSlow(playErrorView, liveRoomDataManager.getConfig().isLive());
        }

        @Override
        public void onNoLiveAtPresent() {
            super.onNoLiveAtPresent();
            ToastUtils.showShort(R.string.plv_player_toast_no_live);
        }

        @Override
        public void onLiveEnd() {
            super.onLiveEnd();
            Log.i(TAG, "onLiveEnd: ");
            startLiveTimeCountDown(liveStartTime);
        }

        @Override
        public void onPrepared(int mediaPlayMode) {
            super.onPrepared(mediaPlayMode);
            hideScreenShotView();
            stopLiveCountDown();
            mediaController.updateWhenVideoViewPrepared();
            mediaController.show();
        }

        @Override
        public void onRestartPlay() {
            super.onRestartPlay();
            showScreenShotView();
        }

        @Override
        public boolean onLightChanged(int changeValue, boolean isEnd) {
            lightTipsView.setLightPercent(changeValue, isEnd);
            return true;
        }

        @Override
        public boolean onVolumeChanged(int changeValue, boolean isEnd) {
            volumeTipsView.setVolumePercent(changeValue, isEnd);
            return true;
        }

        @Override
        public void onServerDanmuOpen(boolean isServerDanmuOpen) {
            super.onServerDanmuOpen(isServerDanmuOpen);
            danmuWrapper.setOnServerDanmuOpen(isServerDanmuOpen);
        }

        @Override
        public void onShowPPTView(int visible) {
            super.onShowPPTView(visible);
            mediaController.setServerEnablePPT(visible == View.VISIBLE);
        }

        @Override
        public boolean onNetworkRecover() {
            //如果加入了连麦，那么断网重连后，不让播放器内部自动拉流播放
            return isJoinRTC;
        }

        @Override
        public void onOnlyAudio(boolean isOnlyAudio) {
            super.onOnlyAudio(isOnlyAudio);
            mediaController.updateWhenOnlyAudio(isOnlyAudio);
        }

        @Override
        public void onLowLatencyNetworkQuality(int networkQuality) {
            networkTipsView.acceptNetworkQuality(networkQuality);
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

    // <editor-fold defaultstate="collapsed" desc="播放器 - 横屏RTC布局显示、隐藏---通过控制播放器右边距">
    private void showLandscapeRTCLayout(boolean show) {
        MarginLayoutParams switchAnchorLp = (MarginLayoutParams) playerSwitchAnchor.getLayoutParams();
        switchAnchorLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        switchAnchorLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        switchAnchorLp.rightMargin = show ? landscapeMarginRightForLinkMicLayout : 0;
        playerSwitchAnchor.setLayoutParams(switchAnchorLp);

        MarginLayoutParams networkTipsLp = (MarginLayoutParams) networkTipsView.getLayoutParams();
        networkTipsLp.rightMargin = show ? landscapeMarginRightForLinkMicLayout : 0;
        networkTipsView.setLayoutParams(networkTipsLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 直播倒计时开始、停止">
    private void startLiveTimeCountDown(String startTime) {
        this.liveStartTime = startTime;
        //2019/08/01 12:22:00

        if (TextUtils.isEmpty(startTime)) {
            timeCountDownTv.setVisibility(View.GONE);
            return;
        }

        //解析时间
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        long startTimeMillis = TimeUtils.string2Millis(startTime, dateFormat);
        long timeSpanMillis = startTimeMillis - System.currentTimeMillis();

        //初始化计时器
        startTimeCountDown = new CountDownTimer(timeSpanMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeText = "倒计时：" + TimeUtils.toCountDownTime(millisUntilFinished);
                timeCountDownTv.setText(timeText);
                timeCountDownTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                timeCountDownTv.setVisibility(View.GONE);
            }
        };
        startTimeCountDown.start();
    }

    private void stopLiveCountDown() {
        if (startTimeCountDown != null) {
            startTimeCountDown.cancel();
        }
        if (timeCountDownTv != null) {
            timeCountDownTv.setVisibility(GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 封面图更新显示">

    /**
     * 更新封面图显示，仅限三分屏场景，已开启仅音频模式{@link #isOnlyAudio=true}下使用
     */
    private void updateCoverImage(boolean isOnlyAudio, String coverImage){
        if(!isOnlyAudio){
            coverImageView.setVisibility(INVISIBLE);
            return;
        }

        if(TextUtils.isEmpty(coverImage)){
            coverImage = DEFAULT_COVER_IMAGE;
        }

        if (coverImage.startsWith("//")) {
            coverImage = "https:" + coverImage;
        }

        coverImageView.setVisibility(VISIBLE);
        PLVImageLoader.getInstance().loadImage(coverImage, coverImageView);

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscape();
            isLandscape = true;
        } else {
            setPortrait();
            isLandscape = false;
        }
    }

    private void setLandscape() {
        //videoLayout root
        MarginLayoutParams vlp = (MarginLayoutParams) getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(vlp);
        //switch anchor
        if (isJoinRTC && isClickShowSubTab && isShowLandscapeRTCLayout) {
            //如果加入连麦，并且右下角点击的是显示连麦，那么给连麦布局预留右边距
            showLandscapeRTCLayout(true);
        } else {
            //否则不预留右边距
            showLandscapeRTCLayout(false);
        }
        //横屏时显示播放器区域的打赏动画
        rewardSvgaView.setVisibility(VISIBLE);
    }

    private void setPortrait() {
        //videoLayout root
        MarginLayoutParams vlp = (MarginLayoutParams) getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //获取高度不要用videoLayout.getWidth()来计算，因为此时宽度并不是全屏的，还有右边的margin占了。
        int portraitWidth = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        vlp.height = (int) (portraitWidth / RATIO_WH);
        setLayoutParams(vlp);

        //switch anchor
        MarginLayoutParams switchAnchorLp = (MarginLayoutParams) playerSwitchAnchor.getLayoutParams();
        switchAnchorLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //获取高度不要用videoLayout.getWidth()来计算，因为此时宽度并不是全屏的，还有右边的margin占了。
        switchAnchorLp.height = (int) (portraitWidth / RATIO_WH);
        switchAnchorLp.rightMargin = 0;
        playerSwitchAnchor.setLayoutParams(switchAnchorLp);

        // network tips
        MarginLayoutParams networkTipsLp = (MarginLayoutParams) networkTipsView.getLayoutParams();
        networkTipsLp.rightMargin = 0;
        networkTipsView.setLayoutParams(networkTipsLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情信息、功能开关数据">
    private void observeLiveRoomData() {
        //监听 直播间数据管理器对象中的直播详情数据变化
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> liveClassDetailVO) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (liveClassDetailVO == null || !liveClassDetailVO.isSuccess()) {
                    return;
                }
                PolyvLiveClassDetailVO liveClassDetail = liveClassDetailVO.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }
                //从接口获取到的当前是否正在直播状态，如果当前不在直播并且设置了倒计时则显示倒计时
                if (!liveClassDetail.getData().isLiveStatus()) {
                    String startTime = liveClassDetail.getData().getStartTime();
                    //startTime为空表示没设置直播开始时间
                    if (!StringUtils.isEmpty(startTime)) {
                        startLiveTimeCountDown(startTime);
                    }
                }
                //设置视频名称
                mediaController.setVideoName(liveClassDetail.getData().getName());
                //更新封面图
                coverImage = liveClassDetail.getData().getSplashImg();
                updateCoverImage(isOnlyAudio, coverImage);
            }
        });

        //监听 直播间数据管理器对象中的功能开关数据
        liveRoomDataManager.getFunctionSwitchVO().observe(((LifecycleOwner) getContext()), new Observer<PLVStatefulData<PolyvChatFunctionSwitchVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvChatFunctionSwitchVO> chatFunctionSwitchStateData) {
                liveRoomDataManager.getFunctionSwitchVO().removeObserver(this);
                if (chatFunctionSwitchStateData == null || !chatFunctionSwitchStateData.isSuccess()) {
                    return;
                }
                PolyvChatFunctionSwitchVO functionSwitchVO = chatFunctionSwitchStateData.getData();
                if (functionSwitchVO == null || functionSwitchVO.getData() == null) {
                    return;
                }
                List<PolyvChatFunctionSwitchVO.DataBean> dataBeanList = functionSwitchVO.getData();
                if (dataBeanList == null) {
                    return;
                }
                for (PolyvChatFunctionSwitchVO.DataBean dataBean : dataBeanList) {
                    boolean isSwitchEnabled = dataBean.isEnabled();
                    switch (dataBean.getType()) {
                        //送花/点赞开关
                        case PolyvChatFunctionSwitchVO.TYPE_SEND_FLOWERS_ENABLED:
                            mediaController.setOnLikesSwitchEnabled(isSwitchEnabled);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        //监听 直播间是否是仅音频模式
        liveRoomDataManager.getIsOnlyAudioEnabled().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean onlyAudio) {
                if(onlyAudio == null){
                    onlyAudio = false;
                }
                isOnlyAudio = onlyAudio;
                //更新封面图
                updateCoverImage(isOnlyAudio, coverImage);
            }
        });

        //订阅积分打赏开关
        liveRoomDataManager.getPointRewardEnableData().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<Boolean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<Boolean> booleanPLVStatefulData) {
                liveRoomDataManager.getPointRewardEnableData().removeObserver(this);
                if(mediaController != null){
                    mediaController.updateRewardView(booleanPLVStatefulData.getData());
                }

            }
        });

        //订阅积分打赏事件
        liveRoomDataManager.getRewardEventData().observe((LifecycleOwner) getContext(), new Observer<PLVRewardEvent>() {
            @Override
            public void onChanged(@Nullable PLVRewardEvent event) {
                if(event == null || !isLandscape || !isRewardEffectShow){
                    return;
                }
                //添加到svga
                svgaHelper.addEvent(event);
            }
        });
    }
    // </editor-fold>
}
