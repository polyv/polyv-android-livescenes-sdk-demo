package com.easefun.polyv.livecloudclass.modules.media;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
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
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.media.controller.IPLVLCLiveLandscapePlayerController;
import com.easefun.polyv.livecloudclass.modules.media.controller.IPLVLCPlaybackMediaController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCDanmuController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCLandscapeMessageSender;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuFragment;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuWrapper;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCLandscapeMessageSendPanel;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLightTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCProgressTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVideoLoadingLayout;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVolumeTipsView;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chapter.viewmodel.PLVPlaybackChapterViewModel;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.marquee.PLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayErrorMessageUtils;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackPlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.view.PLVAbsPlaybackPlayerView;
import com.easefun.polyv.livecommon.module.modules.watermark.IPLVWatermarkView;
import com.easefun.polyv.livecommon.module.modules.watermark.PLVWatermarkView;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVPlaceHolderView;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerRetryLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * 云课堂场景下的回放播放器布局，实现 IPLVLCMediaLayout 接口
 */
public class PLVLCPlaybackMediaLayout extends FrameLayout implements IPLVLCMediaLayout, LifecycleObserver {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCPlaybackMediaLayout.class.getSimpleName();
    private static final float RATIO_WH = 16f / 9;//播放器竖屏宽高使用16:9比例
    private static final int MAX_RETRY_COUNT = 3;//断网重连重试次数
    private static final boolean AUTO_PAUSE_WHEN_ENTER_BACKGROUND = false;//进入后台自动暂停

    /**
     * 横屏聊天布局可见性与弹幕开关同步
     * true -> 当弹幕关闭时，也隐藏横屏聊天布局
     */
    private static final boolean SYNC_LANDSCAPE_CHATROOM_LAYOUT_VISIBILITY_WITH_DANMU = true;

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //播放器渲染视图view
    private PolyvPlaybackVideoView videoView;
    private View playerView;
    //controller
    private IPLVLCPlaybackMediaController mediaController;
    //播放失败时显示的view
    private PLVPlaceHolderView noStreamView;
    //可自定义提示的播放失败/加载缓慢显示的占位View
    private PLVPlaceHolderView playErrorView;
    //Switch View
    private FrameLayout flPlayerSwitchViewParent;
    private PLVSwitchViewAnchorLayout switchAnchorPlayer;
    //子播放器渲染视图view
    private PolyvAuxiliaryVideoview subVideoView;
    //倒计时
    private LinearLayout llAuxiliaryCountDown;
    private TextView tvCountDown;
    // Logo
    private PLVPlayerLogoView logoView;
    //载入状态指示器
    private PLVLCVideoLoadingLayout loadingLayout;
    private PLVPlayerRetryLayout playerRetryLayout;
    // tips view
    private PLVLCLightTipsView lightTipsView;
    private PLVLCVolumeTipsView volumeTipsView;
    private PLVLCProgressTipsView progressTipsView;
    private PLVRoundRectLayout playbackAutoContinueSeekTimeHintLayout;
    private TextView playbackAutoContinueSeekTimeTv;

    //横屏聊天区
    private PLVLCChatLandscapeLayout chatLandscapeLayout;

    //弹幕
    private IPLVLCDanmuController danmuController;
    //弹幕包装器
    private PLVLCDanmuWrapper danmuWrapper;
    //信息发送输入框弹窗
    private IPLVLCLandscapeMessageSender landscapeMessageSender;

    //跑马灯
    private PLVMarqueeView marqueeView = null;

    //水印
    private PLVWatermarkView watermarkView = null;

    //播放器presenter
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playbackPlayerPresenter;
    private final PLVPlaybackChapterViewModel playbackChapterViewModel = PLVDependManager.getInstance().get(PLVPlaybackChapterViewModel.class);
    //listener
    private IPLVLCMediaLayout.OnViewActionListener onViewActionListener;

    private boolean pausingOnEnterBackground = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCPlaybackMediaLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPlaybackMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPlaybackMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(this);
        }

        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (pausingOnEnterBackground) {
            resume();
        }
        pausingOnEnterBackground = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        if (AUTO_PAUSE_WHEN_ENTER_BACKGROUND && isPlaying()) {
            pausingOnEnterBackground = true;
            pause();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_player_layout, this, true);
        videoView = findViewById(R.id.plvlc_playback_video_view);
        subVideoView = findViewById(R.id.sub_video_view);
        playerView = videoView.findViewById(PolyvBaseVideoView.IJK_VIDEO_ID);
        mediaController = findViewById(R.id.plvlc_playback_media_controller);
        noStreamView = findViewById(R.id.no_stream_ly);
        playErrorView = findViewById(R.id.play_error_ly);
        logoView = findViewById(R.id.playback_logo_view);
        loadingLayout = findViewById(R.id.plvlc_playback_loading_layout);
        playerRetryLayout = findViewById(R.id.plvlc_playback_player_retry_layout);
        lightTipsView = findViewById(R.id.plvlc_playback_tipsview_light);
        volumeTipsView = findViewById(R.id.plvlc_playback_tipsview_volume);
        progressTipsView = findViewById(R.id.plvlc_playback_tipsview_progress);
        chatLandscapeLayout = findViewById(R.id.plvlc_chat_landscape_ly);

        flPlayerSwitchViewParent = findViewById(R.id.plvlc_playback_fl_player_switch_view_parent);
        switchAnchorPlayer = findViewById(R.id.plvlc_playback_switch_anchor_player);

        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);
        watermarkView = findViewById(R.id.polyv_watermark_view);
        playbackAutoContinueSeekTimeHintLayout = findViewById(R.id.plvlc_playback_auto_continue_seek_time_hint_layout);
        playbackAutoContinueSeekTimeTv = findViewById(R.id.plvlc_playback_auto_continue_seek_time_tv);

        initVideoView();
        initPlayErrorView();
        initDanmuView();
        initMediaController();
        initLoadingView();
        initRetryView();
        initSwitchView();
        initChatLandscapeLayout();
        initLayoutWH();

        observePlaybackChapterSeekEvent();
    }

    private void initVideoView() {
        //设置允许断网重连
        videoView.enableRetry(true);
        videoView.setMaxRetryCount(MAX_RETRY_COUNT);
        //设置noStreamView
        noStreamView.setPlaceHolderImg(R.drawable.plv_bg_player_error_ic);
        noStreamView.setPlaceHolderText(getResources().getString(R.string.plv_player_video_playback_no_stream));

        videoView.setSubVideoView(subVideoView);
        videoView.setMediaController(mediaController);
        videoView.setNoStreamIndicator(noStreamView);
        videoView.setPlayerBufferingIndicator(loadingLayout);
        //设置跑马灯
        marqueeView = ((Activity) getContext()).findViewById(R.id.polyv_marquee_view);
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

    private void initDanmuView() {
        danmuController = new PLVLCDanmuFragment();
        FragmentTransaction fragmentTransaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.plvlc_danmu_ly, (Fragment) danmuController, "danmuFragment").commitAllowingStateLoss();

        danmuWrapper = new PLVLCDanmuWrapper(this);
        danmuWrapper.setDanmuController(danmuController);
        final View danmuSwitchView = mediaController.getLandscapeDanmuSwitchView();
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
        mediaController.setOnViewActionListener(new IPLVLCPlaybackMediaController.OnViewActionListener() {
            @Override
            public void onStartSendMessageAction() {
                landscapeMessageSender.openMessageSender();
            }

            @Override
            public void onClickShowOrHideSubTab(boolean toShow) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickShowOrHideSubTab(toShow);
                }
            }

            @Override
            public void onSendLikesAction() {
                if (onViewActionListener != null) {
                    onViewActionListener.onSendLikesAction();
                }
            }
        });
    }

    private void initLoadingView() {
        loadingLayout.bindVideoView(videoView);
    }

    private void initRetryView() {
        playerRetryLayout.setOnClickPlayerRetryListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playbackPlayerPresenter != null){
                    playbackPlayerPresenter.startPlay();
                }
            }
        });
    }

    private void initSwitchView() {
        switchAnchorPlayer.setOnSwitchListener(new PLVSwitchViewAnchorLayout.IPLVSwitchViewAnchorLayoutListener() {
            @Override
            protected void onSwitchElsewhereBefore() {
                super.onSwitchElsewhereBefore();
                View childOfAnchor = switchAnchorPlayer.getChildAt(0);
                if (childOfAnchor == flPlayerSwitchViewParent) {
                    videoView.removeView(playerView);
                    videoView.removeView(logoView);

                    flPlayerSwitchViewParent.addView(playerView);
                    flPlayerSwitchViewParent.addView(logoView);
                }
            }

            @Override
            protected void onSwitchBackAfter() {
                super.onSwitchBackAfter();
                View childOfAnchor = switchAnchorPlayer.getChildAt(0);
                if (childOfAnchor == flPlayerSwitchViewParent) {
                    flPlayerSwitchViewParent.removeAllViews();
                    videoView.addView(playerView, 0);
                    videoView.addView(logoView);
                }
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

    private void observePlaybackChapterSeekEvent() {
        playbackChapterViewModel.getSeekToChapterLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<Event<Integer>>() {
                    @Override
                    public void onChanged(@Nullable Event<Integer> seekEvent) {
                        final Integer seekPosition = Event.unwrap(seekEvent);
                        if (seekPosition == null) {
                            return;
                        }
                        seekTo(seekPosition * 1000, getDuration());
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的common方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        observeLiveRoomData();

        playbackPlayerPresenter = new PLVPlaybackPlayerPresenter(liveRoomDataManager);
        playbackPlayerPresenter.registerView(playbackPlayerView);
        playbackPlayerPresenter.init();
        mediaController.setPlaybackPlayerPresenter(playbackPlayerPresenter);
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
    public void stop() {
        playbackPlayerPresenter.stop();
    }

    @Override
    public boolean isPlaying() {
        return playbackPlayerPresenter.isPlaying();
    }

    @Override
    public void setVolume(int volume) {
        playbackPlayerPresenter.setVolume(volume);
    }

    @Override
    public int getVolume() {
        return playbackPlayerPresenter.getVolume();
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
        return switchAnchorPlayer;
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
        return mediaController.getCardEnterView();
    }

    @Override
    public TextView getCardEnterCdView() {
        return mediaController.getCardEnterCdView();
    }

    @Override
    public PLVTriangleIndicateTextView getCardEnterTipsView() {
        return mediaController.getCardEnterTipsView();
    }

    @Override
    public void setOnViewActionListener(IPLVLCMediaLayout.OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void addOnPlayerStateListener(IPLVOnDataChangedListener<PLVPlayerState> listener) {
        playbackPlayerPresenter.getData().getPlayerState().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnPPTShowStateListener(IPLVOnDataChangedListener<Boolean> listener) {
        playbackPlayerPresenter.getData().getPPTShowState().observe((LifecycleOwner) getContext(), listener);
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
        if (mediaController.onBackPressed()) {
            return true;
        }
        if (ScreenUtils.isLandscape()) {
            PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        if (playbackPlayerPresenter != null) {
            playbackPlayerPresenter.destroy();
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
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的live方法，空实现">
    @Override
    public void setLandscapeControllerView(@NonNull IPLVLCLiveLandscapePlayerController landscapeControllerView) {

    }

    @Override
    public IPLVLCLiveLandscapePlayerController getLandscapeControllerView() {
        return null;
    }

    @Override
    public void updateViewerCount(long viewerCount) {

    }

    @Override
    public void updatePPTStatusChange(PLVPPTStatus plvpptStatus) {

    }

    @Override
    public void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth) {

    }

    @Override
    public void updateWhenLeaveRTC() {

    }

    @Override
    public void updateWhenLinkMicOpenStatusChanged(boolean isOpen) {

    }

    @Override
    public void updateWhenRequestJoinLinkMic(boolean requestJoin) {

    }

    @Override
    public void notifyRTCPrepared() {

    }

    @Override
    public void updateWhenJoinLinkMic() {

    }

    @Override
    public void updateWhenLeaveLinkMic() {

    }

    @Override
    public void acceptNetworkQuality(int quality) {

    }

    @Override
    public void addOnLinkMicStateListener(IPLVOnDataChangedListener<Pair<Boolean, Boolean>> listener) {

    }

    @Override
    public void addOnSeiDataListener(IPLVOnDataChangedListener<Long> listener) {

    }

    @Override
    public void setOnRTCPlayEventListener(IPolyvLiveListenerEvent.OnRTCPlayEventListener listener) {

    }

    @Override
    public void setShowLandscapeRTCLayout() {

    }

    @Override
    public void setHideLandscapeRTCLayout() {

    }

    @Override
    public void setLandscapeRewardEffectVisibility(boolean isShow) {

    }

    @Override
    public void onTurnPageLayoutChange(boolean toShow) {

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的playback方法">
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
    public void setPPTView(IPolyvPPTView pptView) {
        playbackPlayerPresenter.bindPPTView(pptView);
    }

    @Override
    public void addOnPlayInfoVOListener(IPLVOnDataChangedListener<PLVPlayInfoVO> listener) {
        playbackPlayerPresenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnSeekCompleteListener(IPLVOnDataChangedListener<Integer> listener) {
        playbackPlayerPresenter.getData().getSeekCompleteVO().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void updatePlayBackVideVid(String vid) {
        playbackPlayerPresenter.setPlayerVid(vid);
    }

    @Override
    public void updatePlayBackVideVidAndPlay(String vid) {
        playbackPlayerPresenter.setPlayerVidAndPlay(vid);
    }

    @Override
    public String getSessionId() {
        return playbackPlayerPresenter.getSessionId();
    }

    @Override
    public void setChatPlaybackEnabled(boolean isChatPlaybackEnabled) {
        mediaController.setChatPlaybackEnabled(isChatPlaybackEnabled);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - MVP模式的view实现">
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
        public View getNoStreamIndicator() {
            return noStreamView;
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
        public View getRetryLayout(){
            return null;
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
        public void onPrepared() {
            super.onPrepared();
            PLVCommonLog.d(TAG, "PLVLCPlaybackMediaLayout.onPreparing");
            mediaController.show();
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
        public boolean onProgressChanged(int seekTime, int totalTime, boolean isEnd, boolean isRightSwipe) {
            progressTipsView.setProgressPercent(seekTime, totalTime, isEnd, isRightSwipe);
            return true;
        }

        @Override
        public void onAutoContinuePlaySeeked(int seekTo) {
            playbackAutoContinueSeekTimeTv.setText(PLVTimeUtils.generateTime(seekTo));
            PLVViewUtil.showViewForDuration(playbackAutoContinueSeekTimeHintLayout, seconds(3).toMillis());
        }

        @Override
        public void onDoubleClick() {
            super.onDoubleClick();
            mediaController.playOrPause();
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
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscape();
        } else {
            setPortrait();
        }
    }

    private void setLandscape() {
        //videoLayout root
        MarginLayoutParams vlp = (MarginLayoutParams) getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(vlp);

        final MarginLayoutParams seekTimeHintLayoutLp = (MarginLayoutParams) playbackAutoContinueSeekTimeHintLayout.getLayoutParams();
        seekTimeHintLayoutLp.bottomMargin = ConvertUtils.dp2px(92);
        playbackAutoContinueSeekTimeHintLayout.setLayoutParams(seekTimeHintLayoutLp);
    }

    private void setPortrait() {
        //videoLayout root
        MarginLayoutParams vlp = (MarginLayoutParams) getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //获取高度不要用videoLayout.getWidth()来计算，因为此时宽度并不是全屏的，还有右边的margin占了。
        int portraitWidth = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        vlp.height = (int) (portraitWidth / RATIO_WH);
        setLayoutParams(vlp);

        final MarginLayoutParams seekTimeHintLayoutLp = (MarginLayoutParams) playbackAutoContinueSeekTimeHintLayout.getLayoutParams();
        seekTimeHintLayoutLp.bottomMargin = ConvertUtils.dp2px(44);
        playbackAutoContinueSeekTimeHintLayout.setLayoutParams(seekTimeHintLayoutLp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情信息、功能开关数据">
    private void observeLiveRoomData() {
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
    }
    // </editor-fold>
}
