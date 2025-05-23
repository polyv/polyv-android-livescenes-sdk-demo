package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCPlaybackMoreLayout;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.imageview.PLVSimpleImageView;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.config.PLVLivePlaybackSeekBarStrategy;
import com.plv.livescenes.playback.subtitle.vo.PLVPlaybackSubtitleVO;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 回放播放器控制栏布局，实现 IPLVLCPlaybackMediaController 接口
 */
public class PLVLCPlaybackMediaController extends FrameLayout implements IPLVLCPlaybackMediaController, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLCPlaybackMediaController";
    private static final int SHOW_TIME = 5000;//控制栏显示时间

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    /**** 横屏View **/
    private ImageView ivPlayPauseLand;
    private TextView tvCurrentTimeLand;
    private TextView tvTotalTimeLand;
    private ImageView ivSubviewShowLand;
    private SeekBar sbPlayProgressLand;
    private ImageView btnMoreLand;
    private ImageView floatingLandIv;
    private ImageView ivBackLand;
    private TextView tvVideoNameLand;
    private PLVLCLikeIconView ivLikesLand;
    private TextView tvStartSendMessageLand;
    private ImageView ivDanmuSwitchLand;
    private RelativeLayout rlRootLand;
    private PLVSimpleImageView controllerCommodityLandIv;
    private PLVSimpleImageView cardEnterLandView;
    private TextView cardEnterCdLandTv;
    private PLVTriangleIndicateTextView cardEnterTipsLandView;

    private PLVSimpleImageView lotteryEnterLandView;
    private TextView lotteryEnterCdLandTv;
    private PLVTriangleIndicateTextView lotteryEnterTipsLandView;

    private PLVSimpleImageView welfareLotteryEnterLandView;
    private TextView welfareLotteryEnterCdLandTv;
    private PLVTriangleIndicateTextView welfareLotteryEnterTipsLandView;

    //观看热度
    private TextView videoViewerCountLandTv;
    /**** 竖屏View **/
    private ImageView ivPlayPausePort;
    private TextView tvCurrentTimePort;
    private TextView tvTotalTimePort;
    private SeekBar sbPlayProgressPort;
    private ImageView iVFullScreenPort;
    private ImageView ivSubviewShowPort;
    private ImageView btnMorePort;
    // 小窗按钮
    private ImageView floatingIv;
    private ImageView ivBackPort;
    private ImageView ivTopGradientPort;
    private TextView tvVideoNamePort;
    private RelativeLayout rlRootPort;
    //观看热度
    private TextView videoViewerCountPortTv;
    /**** 更多布局 **/
    private PLVLCPlaybackMoreLayout moreLayout;
    //重新打开悬浮窗提示
    private TextView tvReopenFloatingViewTip;

    /**** status **/
    // 进度条是否处于拖动的状态
    private boolean isPbDragging;
    //已经显示过 可从此处重新打开浮窗 的提示了
    private boolean hasShowReopenFloatingViewTip = false;

    //延迟隐藏 可从此处重新打开浮窗
    private Disposable reopenFloatingDelay;

    //延迟隐藏“控制栏”
    private Disposable hideMediaControllerDisable;

    //player presenter
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playerPresenter;

    private IPLVLiveRoomDataManager liveRoomDataManager;

    //服务端的PPT开关
    private boolean isServerEnablePPT;
    // 服务端的点赞开关
    private boolean isLikesSwitchEnabled = true;
    // 聊天室tab是否可见
    private boolean isChatDisplayEnabled = true;

    //Listener
    private OnViewActionListener onViewActionListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCPlaybackMediaController(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCPlaybackMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCPlaybackMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        //create and add view
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_playback_controller_layout, this, true);

        //find all view
        //land layout
        ivPlayPauseLand = findViewById(R.id.plvlc_playback_controller_land_iv_playpause);
        tvCurrentTimeLand = findViewById(R.id.plvlc_playback_controller_land_tv_currenttime);
        tvTotalTimeLand = findViewById(R.id.plvlc_playback_controller_land_tv_totaltime);
        ivSubviewShowLand = findViewById(R.id.plvlc_playback_controller_land_iv_subview_show_land);
        sbPlayProgressLand = findViewById(R.id.plvlc_playback_controller_land_sb_playprogress);
        btnMoreLand = findViewById(R.id.plvlc_playback_controller_land_bt_more);
        floatingLandIv = findViewById(R.id.plvlc_playback_control_floating_land_iv);
        ivBackLand = findViewById(R.id.plvlc_playback_controller_land_iv_back);
        tvVideoNameLand = findViewById(R.id.plvlc_playback_controller_land_tv_video_name);
        ivLikesLand = findViewById(R.id.plvlc_playback_controller_land_iv_likes);
        tvStartSendMessageLand = findViewById(R.id.plvlc_playback_controller_land_tv_start_send_message);
        ivDanmuSwitchLand = findViewById(R.id.plvlc_playback_controller_land_iv_danmu_switch);
        rlRootLand = findViewById(R.id.plvlc_playback_controller_land_rl_root);
        controllerCommodityLandIv = findViewById(R.id.plvlc_controller_commodity_land_iv);
        cardEnterLandView = findViewById(R.id.plvlc_card_enter_land_view);
        cardEnterCdLandTv = findViewById(R.id.plvlc_card_enter_cd_land_tv);
        cardEnterTipsLandView = findViewById(R.id.plvlc_card_enter_tips_land_view);

        lotteryEnterLandView = findViewById(R.id.plvlc_playback_lottery_enter_land_view);
        lotteryEnterCdLandTv = findViewById(R.id.plvlc_playback_lottery_enter_cd_land_tv);
        lotteryEnterTipsLandView = findViewById(R.id.plvlc_playback_lottery_enter_tips_land_view);

        welfareLotteryEnterLandView = findViewById(R.id.plvlc_playback_welfare_lottery_enter_land_view);
        welfareLotteryEnterCdLandTv = findViewById(R.id.plvlc_playback_welfare_lottery_enter_cd_land_tv);
        welfareLotteryEnterTipsLandView = findViewById(R.id.plvlc_playback_welfare_lottery_enter_tips_land_view);

        videoViewerCountLandTv = findViewById(R.id.plvlc_playback_count_land_tv);

        //port layout
        ivPlayPausePort = findViewById(R.id.plvlc_playback_controller_port_iv_play_pause);
        tvCurrentTimePort = findViewById(R.id.plvlc_playback_controller_port_tv_currenttime);
        tvTotalTimePort = findViewById(R.id.plvlc_playback_controller_port_tv_totaltime);
        sbPlayProgressPort = findViewById(R.id.plvlc_playback_controller_port_sb_playprogress);
        iVFullScreenPort = findViewById(R.id.plvlc_playback_controller_port_iv_full_screen);
        ivSubviewShowPort = findViewById(R.id.plvlc_playback_controller_port_iv_subview_show);
        btnMorePort = findViewById(R.id.plvlc_playback_controller_port_btn_controller_more);
        floatingIv = findViewById(R.id.plvlc_playback_control_floating_iv);
        ivBackPort = findViewById(R.id.plvlc_playback_controller_port_iv_back);
        ivTopGradientPort = findViewById(R.id.plvlc_playback_controller_port_iv_top_gradient);
        tvVideoNamePort = findViewById(R.id.plvlc_playback_controller_port_tv_video_name);
        rlRootPort = findViewById(R.id.plvlc_playback_controller_port_rl_root);
        videoViewerCountPortTv = findViewById(R.id.plvlc_playback_viewer_count_port_tv);

        tvReopenFloatingViewTip = findViewById(R.id.plvlc_playback_player_controller_tv_reopen_floating_view);

        ivBackLand.setOnClickListener(this);
        ivPlayPauseLand.setOnClickListener(this);
        ivPlayPausePort.setOnClickListener(this);
        iVFullScreenPort.setOnClickListener(this);
        sbPlayProgressLand.setOnSeekBarChangeListener(playProgressChangeListener);
        sbPlayProgressPort.setOnSeekBarChangeListener(playProgressChangeListener);
        ivSubviewShowLand.setOnClickListener(this);
        ivSubviewShowPort.setOnClickListener(this);
        btnMoreLand.setOnClickListener(this);
        btnMorePort.setOnClickListener(this);
        floatingLandIv.setOnClickListener(this);
        floatingIv.setOnClickListener(this);
        ivBackPort.setOnClickListener(this);
        ivLikesLand.setOnButtonClickListener(this);
        tvStartSendMessageLand.setOnClickListener(this);
        controllerCommodityLandIv.setOnClickListener(this);

        //more layout
        initMoreLayout();

        //choose right orientation
        if (PLVScreenUtils.isLandscape(getContext())) {
            setLandscapeController();
        } else {
            setPortraitController();
        }

        observeCommodityStatus();
    }

    private void initMoreLayout() {
        moreLayout = new PLVLCPlaybackMoreLayout(this);
        moreLayout.setOnSpeedSelectedListener(new PLVLCPlaybackMoreLayout.OnSpeedSelectedListener() {
            @Override
            public void onSpeedSelected(Float speed, int pos) {
                playerPresenter.setSpeed(speed);
            }
        });
        moreLayout.setOnSubtitleActionListener(new PLVLCPlaybackMoreLayout.OnSubtitleActionListener() {
            @Override
            public List<PLVPlaybackSubtitleVO> getAllSubtitleSettings() {
                return playerPresenter.getAllSubtitleSettings();
            }

            @Override
            public List<PLVPlaybackSubtitleVO> getShowSubtitles() {
                return playerPresenter.getShowSubtitles();
            }

            @Override
            public void setShowSubtitles(List<PLVPlaybackSubtitleVO> subtitles) {
                playerPresenter.setShowSubtitles(subtitles);
            }
        });
    }

    private void observeCommodityStatus() {
        commodityViewModel.getCommodityUiStateLiveData()
                .observe((LifecycleOwner) getContext(), new Observer<PLVCommodityUiState>() {
                    boolean needShowControllerOnClosed = false;

                    @Override
                    public void onChanged(@Nullable PLVCommodityUiState uiState) {
                        if (uiState == null) {
                            return;
                        }
                        controllerCommodityLandIv.setVisibility(uiState.hasProductView ? View.VISIBLE : View.GONE);
                        if (uiState.showProductViewOnLandscape) {
                            if (!needShowControllerOnClosed) {
                                needShowControllerOnClosed = isShowing();
                                hide();
                            }
                        } else {
                            if (needShowControllerOnClosed) {
                                show();
                                needShowControllerOnClosed = false;
                            }
                        }
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCLiveMediaController父接口IPolyvMediaController定义的方法">
    @Override
    public void onPrepared(PolyvPlaybackVideoView videoView) {
        updateTotalTimeView();
        updateMoreLayout();
        updateVideoName();
    }

    @Override
    public void onLongBuffering(String tip) {

    }

    @Override
    public void hide() {
        setVisibility(GONE);
    }

    @Override
    public boolean isShowing() {
        return isShown();
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
        resetHideControllerBarDelay();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCPlaybackMediaController定义的方法">
    @Override
    public void setPlaybackPlayerPresenter(IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playerPresenter) {
        this.playerPresenter = playerPresenter;
        observePlayInfoVO();
    }

    @Override
    public void initData(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String channelId = liveRoomDataManager.getConfig().getChannelId();
        boolean enableSpeedControl = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_SPEED_CONTROL_ENABLE);
        boolean enablePlayButton = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_PLAY_BUTTON_ENABLE);
        PLVLivePlaybackSeekBarStrategy seekBarStrategy = PLVChannelFeatureManager.onChannel(channelId)
                .getOrDefault(PLVChannelFeature.LIVE_PLAYBACK_SEEK_BAR_STRATEGY, PLVLivePlaybackSeekBarStrategy.ALLOW_SEEK);

        ivPlayPausePort.setVisibility(enablePlayButton ? View.VISIBLE : View.GONE);
        ivPlayPauseLand.setVisibility(enablePlayButton ? View.VISIBLE : View.GONE);
        moreLayout.setEnableSpeedControl(enableSpeedControl);
        sbPlayProgressPort.setVisibility(seekBarStrategy != PLVLivePlaybackSeekBarStrategy.INVISIBLE ? View.VISIBLE : View.GONE);
        sbPlayProgressLand.setVisibility(seekBarStrategy != PLVLivePlaybackSeekBarStrategy.INVISIBLE ? View.VISIBLE : View.GONE);
    }

    @Override
    public View getLandscapeDanmuSwitchView() {
        return ivDanmuSwitchLand;
    }

    @Override
    public ImageView getCardEnterView() {
        return cardEnterLandView;
    }

    @Override
    public TextView getCardEnterCdView() {
        return cardEnterCdLandTv;
    }

    @Override
    public PLVTriangleIndicateTextView getCardEnterTipsView() {
        return cardEnterTipsLandView;
    }

    @Override
    public ImageView getLotteryEnterView() {
        return lotteryEnterLandView;
    }

    @Override
    public TextView getLotteryEnterCdView() {
        return lotteryEnterCdLandTv;
    }

    @Override
    public PLVTriangleIndicateTextView getLotteryEnterTipsView() {
        return lotteryEnterTipsLandView;
    }

    @Override
    public ImageView getWelfareLotteryEnterView() {
        return welfareLotteryEnterLandView;
    }

    @Override
    public TextView getWelfareLotteryEnterCdView() {
        return welfareLotteryEnterCdLandTv;
    }

    @Override
    public PLVTriangleIndicateTextView getWelfareLotteryEnterTipsView() {
        return welfareLotteryEnterTipsLandView;
    }

    @Override
    public void setOnLikesSwitchEnabled(boolean isSwitchEnabled) {
        this.isLikesSwitchEnabled = isSwitchEnabled;
        ivLikesLand.setVisibility(isLikesSwitchEnabled && isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setServerEnablePPT(boolean enable) {
        this.isServerEnablePPT = enable;
        ivSubviewShowPort.setVisibility(enable ? View.VISIBLE : View.GONE);
        ivSubviewShowLand.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    @Override
    public void playOrPause() {
        boolean enablePlayButton = true;
        if (liveRoomDataManager != null) {
            String channelId = liveRoomDataManager.getConfig().getChannelId();
            enablePlayButton = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_PLAY_BUTTON_ENABLE);
        }
        if (!enablePlayButton) {
            return;
        }

        if (playerPresenter.isPlaying()) {
            playerPresenter.pause();
            ivPlayPausePort.setSelected(false);
            ivPlayPauseLand.setSelected(false);
        } else {
            playerPresenter.resume();
            ivPlayPausePort.setSelected(true);
            ivPlayPauseLand.setSelected(true);
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void updateOnClickCloseFloatingView() {
        ivSubviewShowPort.performClick();
        if (!hasShowReopenFloatingViewTip) {
            hasShowReopenFloatingViewTip = true;
            tvReopenFloatingViewTip.setVisibility(VISIBLE);
            dispose(reopenFloatingDelay);
            reopenFloatingDelay = PLVRxTimer.delay(3000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    tvReopenFloatingViewTip.setVisibility(GONE);
                }
            });
        }
    }

    @Override
    public void dispatchDanmuSwitchOnClicked(View v) {
        this.onClick(v);
    }

    @Override
    public void setChatPlaybackEnabled(boolean isChatPlaybackEnabled, boolean isLiveType) {
        if (tvStartSendMessageLand != null) {
            if (isChatPlaybackEnabled || !isLiveType) {
                tvStartSendMessageLand.setText(PLVAppUtils.getString(R.string.plv_chat_input_tips_chatroom_close));
                tvStartSendMessageLand.setEnabled(false);
            } else {
                tvStartSendMessageLand.setText(PLVAppUtils.getString(R.string.plv_chat_input_tips_chat));
                tvStartSendMessageLand.setEnabled(true);
            }
        }
    }

    @Override
    public void setChatIsDisplayEnabled(boolean isDisplayEnabled) {
        this.isChatDisplayEnabled = isDisplayEnabled;
        if (tvStartSendMessageLand != null) {
            tvStartSendMessageLand.setVisibility(isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
        }
        if (ivLikesLand != null) {
            ivLikesLand.setVisibility(isLikesSwitchEnabled && isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void notifyChatroomStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus) {
        if (tvStartSendMessageLand != null) {
            String text = PLVAppUtils.getString(isCloseRoomStatus ? R.string.plv_chat_input_tips_chatroom_close_2 : (isFocusModeStatus ? R.string.plv_chat_input_tips_focus : R.string.plv_chat_input_tips_chat));
            tvStartSendMessageLand.setText(text);
            tvStartSendMessageLand.setOnClickListener((!isCloseRoomStatus && !isFocusModeStatus) ? this : null);
        }
    }

    public void updateViewerCount(long viewerCount) {
        videoViewerCountPortTv.setVisibility(View.VISIBLE);
        videoViewerCountLandTv.setVisibility(View.VISIBLE);

        String viewerCountText = StringUtils.toKString(viewerCount);
        String text = PLVAppUtils.formatString(R.string.plv_player_viewer_count, viewerCountText);

        videoViewerCountPortTv.setText(text);
        videoViewerCountLandTv.setText(text);
    }

    @Override
    public void updateViewerOnlineCount(int onlineCount) {
        videoViewerCountPortTv.setVisibility(View.VISIBLE);
        videoViewerCountLandTv.setVisibility(View.VISIBLE);

        String viewerCountText = StringUtils.toKString(onlineCount);
        String text = PLVAppUtils.formatString(R.string.plv_player_viewer_online_count, viewerCountText);

        videoViewerCountPortTv.setText(text);
        videoViewerCountLandTv.setText(text);
    }

    @Override
    public void clean() {
        if (moreLayout != null) {
            moreLayout.hide();
        }

        dispose(reopenFloatingDelay);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制栏 - 视频准备完成后更新view">
    private void updateMoreLayout() {
        moreLayout.updateViewWithPlayInfo(playerPresenter.getSpeed());
    }

    private void updateTotalTimeView() {
        String totalTime = "/" + PLVTimeUtils.generateTime(playerPresenter.getDuration(), true);
        tvTotalTimePort.setText(totalTime);
        tvTotalTimeLand.setText(totalTime);
    }

    private void updateVideoName() {
        tvVideoNamePort.setText(playerPresenter.getVideoName());
        tvVideoNameLand.setText(playerPresenter.getVideoName());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制栏 - 进度条拖动事件处理">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            show();
            isPbDragging = true;
            int seekPosition = (int) ((long) playerPresenter.getDuration() * progress / seekBar.getMax());
            tvCurrentTimePort.setText(PLVTimeUtils.generateTime(seekPosition, true));
            tvCurrentTimeLand.setText(PLVTimeUtils.generateTime(seekPosition, true));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPbDragging = false;
            playerPresenter.seekTo(seekBar.getProgress(), seekBar.getMax());
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeController();
        } else {
            setPortraitController();
        }
    }

    private void setLandscapeController() {
        post(new Runnable() {
            @Override
            public void run() {
                rlRootPort.setVisibility(GONE);
                rlRootLand.setVisibility(VISIBLE);
            }
        });
    }

    private void setPortraitController() {
        post(new Runnable() {
            @Override
            public void run() {
                rlRootPort.setVisibility(VISIBLE);
                rlRootLand.setVisibility(GONE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    //开启/重置 延迟3s隐藏控制栏
    private void resetHideControllerBarDelay(){
        dispose(hideMediaControllerDisable);
        hideMediaControllerDisable = PLVRxTimer.delay(3000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                hide();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="事件监听 - 监听播放信息变化">
    private void observePlayInfoVO() {
        //监听播放信息变化
        playerPresenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                if (playInfoVO == null) {
                    return;
                }
                int position = playInfoVO.getPosition();
                int totalTime = playInfoVO.getTotalTime();
                int bufPercent = playInfoVO.getBufPercent();
                boolean isPlaying = playInfoVO.isPlaying();
                //在拖动进度条的时候，这里不更新
                if (!isPbDragging) {
                    tvCurrentTimePort.setText(PLVTimeUtils.generateTime(position, true));
                    tvCurrentTimeLand.setText(PLVTimeUtils.generateTime(position, true));
                    if (totalTime > 0) {
                        sbPlayProgressPort.setProgress((int) ((long) sbPlayProgressPort.getMax() * position / totalTime));
                        sbPlayProgressLand.setProgress((int) ((long) sbPlayProgressLand.getMax() * position / totalTime));
                    } else {
                        sbPlayProgressPort.setProgress(0);
                        sbPlayProgressLand.setProgress(0);
                    }
                }
                sbPlayProgressPort.setSecondaryProgress(sbPlayProgressPort.getMax() * bufPercent / 100);
                sbPlayProgressLand.setSecondaryProgress(sbPlayProgressPort.getMax() * bufPercent / 100);
                if (isPlaying) {
                    ivPlayPausePort.setSelected(true);
                    ivPlayPauseLand.setSelected(true);
                } else {
                    ivPlayPausePort.setSelected(false);
                    ivPlayPauseLand.setSelected(false);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvlc_playback_controller_port_iv_back) {
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.plvlc_playback_controller_land_iv_back) {
            //点击横屏返回
            PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
            resetHideControllerBarDelay();
        } else if (id == R.id.plvlc_playback_controller_land_iv_playpause || id == R.id.plvlc_playback_controller_port_iv_play_pause) {
            //点击播放/暂停
            playOrPause();
            //重置一下隐藏控制栏时间
            resetHideControllerBarDelay();
        } else if (id == R.id.plvlc_playback_controller_port_iv_full_screen) {
            //点击竖屏全屏
            PLVOrientationManager.getInstance().setLandscape((Activity) getContext());
            resetHideControllerBarDelay();
        } else if (id == R.id.plvlc_playback_controller_land_iv_subview_show_land || id == R.id.plvlc_playback_controller_port_iv_subview_show) {
            //重置一下隐藏控制栏时间
            resetHideControllerBarDelay();
            //selected == true时就是隐藏状态，false时是显示状态
            boolean isNotShowState = ivSubviewShowPort.isSelected();
            boolean isShowState = !isNotShowState;
            ivSubviewShowPort.setSelected(isShowState);
            ivSubviewShowLand.setSelected(isShowState);
            if (onViewActionListener != null) {
                onViewActionListener.onClickShowOrHideSubTab(isNotShowState);
            }
        } else if (id == R.id.plvlc_playback_controller_land_bt_more || id == R.id.plvlc_playback_controller_port_btn_controller_more) {
            hide();
            if (ScreenUtils.isPortrait()) {
                moreLayout.showWhenPortrait(getHeight());
            } else {
                moreLayout.showWhenLandscape();
            }
        } else if (id == R.id.plvlc_playback_controller_land_iv_likes) {
            show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivLikesLand.addLoveIcon(1);
                }
            }, 200);
            if (onViewActionListener != null) {
                onViewActionListener.onSendLikesAction();
            }
        } else if (id == R.id.plvlc_playback_controller_land_tv_start_send_message) {
            hide();
            if (onViewActionListener != null) {
                onViewActionListener.onStartSendMessageAction();
            }
        } else if (id == controllerCommodityLandIv.getId()) {
            commodityViewModel.showProductLayoutOnLandscape();
        } else if (id == floatingIv.getId()
                || id == floatingLandIv.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickFloating();
            }
        }
    }
    // </editor-fold>
}
