package com.easefun.polyv.livecloudclass.modules.media.controller;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLiveMoreLayout;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCPPTTurnPageLayout;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo.PLVCommodityUiState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 直播播放器控制栏布局，实现 IPLVLCLiveMediaController 接口
 */
public class PLVLCLiveMediaController extends FrameLayout implements IPLVLCLiveMediaController, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVLCLiveMediaController";
    //控制栏显示的时间
    private static final int SHOW_TIME = 5000;

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    //竖屏控制栏布局
    private ViewGroup videoControllerPortLy;
    //返回
    private ImageView backPortIv;
    //直播名称
    private TextView videoNamePortTv;
    //观看热度
    private TextView videoViewerCountPortTv;
    //播放/暂停
    private ImageView videoPausePortIv;
    //刷新
    private ImageView videoRefreshPortIv;
    //显示/隐藏 浮窗/连麦布局的按钮
    private ImageView videoPptSwitchPortIv;
    //点击切换到全屏按钮
    private ImageView videoScreenSwitchPortIv;
    private LinearLayout liveMoreControlLl;
    //更多按钮
    private ImageView morePortIv;
    //顶部渐变view
    private ImageView gradientBarTopPortView;
    //重新打开悬浮窗提示
    private TextView tvReopenFloatingViewTip;
    //ppt翻页
    private PLVLCPPTTurnPageLayout pptTurnPagePortLayout;

    private ImageView livePlayerControllerEnterPaintIv;

    //横屏皮肤
    private IPLVLCLiveLandscapePlayerController landscapeController;
    //横屏控制栏布局
    private ViewGroup videoControllerLandLy;
    //返回
    private ImageView backLandIv;
    //直播名称
    private TextView videoNameLandTv;
    //观看热度
    private TextView videoViewerCountLandTv;
    //播放/暂停
    private ImageView videoPauseLandIv;
    //刷新
    private ImageView videoRefreshLandIv;
    //显示/隐藏 浮窗/连麦布局
    private ImageView videoPptSwitchLandIv;
    private ImageView liveControlFloatingLandIv;
    //打开信息发送器的按钮
    private TextView startSendMessageLandIv;
    //横屏弹幕开关(后台开启弹幕时)
    private ImageView danmuSwitchLandIv;
    //打开公告
    private ImageView bulletinLandIv;
    //点赞
    private PLVLCLikeIconView likesLandIv;
    //更多按钮
    private ImageView moreLandIv;
    // 小窗按钮
    private ImageView liveControlFloatingIv;
    //ppt 翻页
    private PLVLCPPTTurnPageLayout pptTurnPageLandLayout;
    //打赏按钮
    private ImageView rewardView;
    //商品按钮
    private ImageView commodityView;

    private ImageView livePlayerControllerEnterPaintLandIv;
    private TextView liveWatchOnlineCountLandTv;

    //播放器presenter
    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;

    //码率索引
    private int currentBitratePos;
    private List<PolyvDefinitionVO> definitionVOS;
    //码率弹窗布局
    private PopupWindow bitRatePopupWindow;
    private Disposable bitPopupWindowTimer;

    //更多布局
    private PLVLCLiveMoreLayout moreLayout;

    //已经显示过 可从此处重新打开浮窗 的提示了
    private boolean hasShowReopenFloatingViewTip = false;
    //延迟隐藏 可从此处重新打开浮窗
    private Disposable reopenFloatingDelay;

    //服务端的PPT开关
    private boolean isServerEnablePPT;
    // 服务端的PPT翻页开关
    private boolean isServerEnablePptTurnPage = true;
    // 服务端的点赞开关
    private boolean isLikesSwitchEnabled = true;
    // 服务端的打赏开关
    private boolean isRewardSwitchEnabled = false;
    // 聊天室tab是否可见
    private boolean isChatDisplayEnabled = true;
    // 主播放器是否正在播放
    private boolean isMainVideoViewPlaying;
    // 是否无延迟观看
    private boolean isLowLatencyWatch;
    // 是否rtc频道观看
    private boolean isRtcChannelWatch;
    // 频道是否已经开启连麦
    private boolean isChannelOpenLinkMic;
    // 是否正在请求加入连麦
    private boolean isRequestingJoinLinkMic;
    // 是否连麦状态
    private boolean isLinkMic;
    // rtc观看时是否隐藏刷新按钮
    private boolean hideRefreshButtonInRtcChannel;
    // 是否正在画笔模式
    private boolean isInPaintMode;
    // 是否允许开启小窗模式
    private boolean isFloatWindowEnable = true;
    // 是否显示在线人数
    private boolean isShowOnlineCount = false;

    //view动作监听器
    private OnViewActionListener onViewActionListener;

    private PLVUserAbilityManager.OnUserAbilityChangedListener userAbilityChangedListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLiveMediaController(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLiveMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLiveMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_controller_layout, this);

        //竖屏控制栏布局
        videoControllerPortLy = findViewById(R.id.video_controller_port_ly);
        backPortIv = findViewById(R.id.back_port_iv);
        videoNamePortTv = findViewById(R.id.video_name_port_tv);
        videoViewerCountPortTv = findViewById(R.id.video_viewer_count_port_tv);
        videoPausePortIv = findViewById(R.id.video_pause_port_iv);
        videoRefreshPortIv = findViewById(R.id.video_refresh_port_iv);
        videoPptSwitchPortIv = findViewById(R.id.video_ppt_switch_port_iv);
        videoScreenSwitchPortIv = findViewById(R.id.video_screen_switch_port_iv);
        liveMoreControlLl = findViewById(R.id.plvlc_live_more_control_ll);
        morePortIv = findViewById(R.id.more_port_iv);
        liveControlFloatingIv = findViewById(R.id.plvlc_live_control_floating_iv);
        gradientBarTopPortView = findViewById(R.id.gradient_bar_top_port_view);
        tvReopenFloatingViewTip = findViewById(R.id.plvlc_live_player_controller_tv_reopen_floating_view);
        pptTurnPagePortLayout = findViewById(R.id.video_ppt_turn_page_layout);
        livePlayerControllerEnterPaintIv = findViewById(R.id.plvlc_live_player_controller_enter_paint_iv);

        backPortIv.setOnClickListener(this);
        videoPausePortIv.setOnClickListener(this);
        videoRefreshPortIv.setOnClickListener(this);
        videoPptSwitchPortIv.setOnClickListener(this);
        videoScreenSwitchPortIv.setOnClickListener(this);
        morePortIv.setOnClickListener(this);
        liveControlFloatingIv.setOnClickListener(this);
        livePlayerControllerEnterPaintIv.setOnClickListener(this);

        pptTurnPagePortLayout.setOnPPTTurnPageListener(new PLVLCPPTTurnPageLayout.OnPPTTurnPageListener() {
            @Override
            public void onPPTTurnPage(String type) {
                onViewActionListener.onPPTTurnPage(type);
            }
        });

        //more layout
        initMoreLayout();

        //choose right orientation
        if (ScreenUtils.isPortrait()) {
            videoControllerPortLy.setVisibility(View.VISIBLE);
        } else {
            videoControllerPortLy.setVisibility(View.INVISIBLE);
        }

        observeFloatingPlayerShowingState();
        observePaintAbility();
    }

    private void initMoreLayout() {
        moreLayout = new PLVLCLiveMoreLayout(this);
        moreLayout.setOnBitrateSelectedListener(new PLVLCLiveMoreLayout.OnBitrateSelectedListener() {
            @Override
            public void onBitrateSelected(PolyvDefinitionVO definitionVO, int pos) {
                livePlayerPresenter.changeBitRate(pos);
            }
        });
        moreLayout.setOnLinesSelectedListener(new PLVLCLiveMoreLayout.OnLinesSelectedListener() {
            @Override
            public void onLineSelected(int linesCount, int linePos) {
                livePlayerPresenter.changeLines(linePos);
            }
        });
        moreLayout.setOnOnlyAudioSwitchListener(new PLVLCLiveMoreLayout.OnOnlyAudioSwitchListener() {
            @Override
            public boolean onOnlyAudioSelect(boolean onlyAudio) {
                if (onlyAudio) {
                    livePlayerPresenter.changeMediaPlayMode(PolyvMediaPlayMode.MODE_AUDIO);
                } else {
                    livePlayerPresenter.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
                }
                return true;
            }
        });
        moreLayout.setOnChangeLowLatencyListener(new PLVLCLiveMoreLayout.OnChangeLowLatencyListener() {
            @Override
            public void accept(@NonNull Boolean isLowLatency) {
                isLowLatencyWatch = isLowLatency;
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeLowLatencyMode(isLowLatency);
                }
            }
        });
    }

    private void observeFloatingPlayerShowingState() {
        PLVFloatingPlayerManager.getInstance().getFloatingViewShowState()
                .observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(@Nullable Boolean showing) {
                        if (showing == null) {
                            return;
                        }
                        getLiveMediaDispatcher().updateViewProperties();
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
                        commodityView.setVisibility(uiState.hasProductView ? View.VISIBLE : View.GONE);
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

    private void observePaintAbility() {
        PLVUserAbilityManager.myAbility().addUserAbilityChangeListener(new WeakReference<>(userAbilityChangedListener = new PLVUserAbilityManager.OnUserAbilityChangedListener() {
            @Override
            public void onUserAbilitiesChanged(@NonNull List<PLVUserAbility> addedAbilities, @NonNull List<PLVUserAbility> removedAbilities) {
                boolean updatePaintModeEntrance = false;
                if (addedAbilities.contains(PLVUserAbility.LIVE_DOCUMENT_ALLOW_USE_PAINT_ON_LINKMIC)) {
                    updatePaintModeEntrance = true;
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_ppt_allow_use_paint)
                            .show();
                    show();
                }
                if (removedAbilities.contains(PLVUserAbility.LIVE_DOCUMENT_ALLOW_USE_PAINT_ON_LINKMIC)) {
                    updatePaintModeEntrance = true;
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_ppt_no_allow_use_paint)
                            .show();
                }
                if (updatePaintModeEntrance) {
                    updatePaintModeEntrance();
                }
            }
        }));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCLiveMediaController父接口IPolyvMediaController定义的方法">
    @Override
    public void onPrepared(PolyvLiveVideoView mp) {
        updateMoreLayout();
        updateBitrateVO();
    }

    @Override
    public void onLongBuffering(String tip) {
        showBitrateChangeView();
    }

    @Override
    public void hide() {
        setVisibility(View.GONE);
        if (onViewActionListener != null) {
            onViewActionListener.onShow(getVisibility() == View.VISIBLE);
        }
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
        if (onViewActionListener != null) {
            onViewActionListener.onShow(getVisibility() == View.VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCLiveMediaController定义的方法">
    @Override
    public void setLivePlayerPresenter(@NonNull IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter) {
        this.livePlayerPresenter = livePlayerPresenter;
        observePlayInfoVO();
    }

    @Override
    public void setLandscapeController(@NonNull IPLVLCLiveLandscapePlayerController landscapeController) {
        this.landscapeController = landscapeController;
        //横屏控制栏布局
        videoControllerLandLy = landscapeController.getLandRoot();
        backLandIv = landscapeController.getBackView();
        videoNameLandTv = landscapeController.getNameView();
        videoViewerCountLandTv = landscapeController.getViewerCountView();
        videoPauseLandIv = landscapeController.getPauseView();
        videoRefreshLandIv = landscapeController.getRefreshView();
        videoPptSwitchLandIv = landscapeController.getSwitchView();
        liveControlFloatingLandIv = landscapeController.getFloatingControlView();
        startSendMessageLandIv = landscapeController.getMessageSender();
        danmuSwitchLandIv = landscapeController.getDanmuSwitchView();
        bulletinLandIv = landscapeController.getBulletinView();
        likesLandIv = landscapeController.getLikesView();
        moreLandIv = landscapeController.getMoreView();
        rewardView = landscapeController.getRewardView();
        commodityView = landscapeController.getCommodityView();
        pptTurnPageLandLayout = landscapeController.getPPTTurnPageLayout();
        livePlayerControllerEnterPaintLandIv = landscapeController.getEnterPaintView();
        liveWatchOnlineCountLandTv = landscapeController.getLiveWatchOnlineCountTextView();

        backLandIv.setOnClickListener(this);
        videoPauseLandIv.setOnClickListener(this);
        videoRefreshLandIv.setOnClickListener(this);
        videoPptSwitchLandIv.setOnClickListener(this);
        liveControlFloatingLandIv.setOnClickListener(this);
        startSendMessageLandIv.setOnClickListener(this);
        bulletinLandIv.setOnClickListener(this);
        likesLandIv.setOnButtonClickListener(this);
        moreLandIv.setOnClickListener(this);
        rewardView.setOnClickListener(this);
        commodityView.setOnClickListener(this);
        livePlayerControllerEnterPaintLandIv.setOnClickListener(this);
        liveWatchOnlineCountLandTv.setOnClickListener(this);

        pptTurnPageLandLayout.setOnPPTTurnPageListener(new PLVLCPPTTurnPageLayout.OnPPTTurnPageListener() {
            @Override
            public void onPPTTurnPage(String type) {
                onViewActionListener.onPPTTurnPage(type);
            }
        });

        //choose right orientation
        if (ScreenUtils.isPortrait()) {
            videoControllerLandLy.setVisibility(View.GONE);
        } else {
            videoControllerLandLy.setVisibility(View.VISIBLE);
        }

        observeCommodityStatus();
    }

    @Override
    public IPLVLCLiveLandscapePlayerController getLandscapeController() {
        return landscapeController;
    }

    @Override
    public void setOnLikesSwitchEnabled(boolean isSwitchEnabled) {
        this.isLikesSwitchEnabled = isSwitchEnabled;
        likesLandIv.setVisibility(isLikesSwitchEnabled && isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setServerEnablePPT(boolean enable) {
        this.isServerEnablePPT = enable;
        videoPptSwitchPortIv.setVisibility(enable ? View.VISIBLE : View.GONE);
        videoPptSwitchLandIv.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setServerEnablePptTurnPage(boolean serverEnablePptTurnPage) {
        this.isServerEnablePptTurnPage = serverEnablePptTurnPage;
        if (!isServerEnablePptTurnPage) {
            if (pptTurnPageLandLayout != null) {
                pptTurnPageLandLayout.setVisibility(View.GONE);
            }
            if (pptTurnPagePortLayout != null) {
                pptTurnPagePortLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setTurnPageLayoutStatus(boolean isShow) {
        if (!isServerEnablePptTurnPage) {
            isShow = false;
        }
        pptTurnPageLandLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        pptTurnPagePortLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void setVideoName(String videoName) {
        videoNamePortTv.setText(videoName);
        videoNameLandTv.setText(videoName);
    }

    @Override
    public void updateWhenSubVideoViewClick(boolean mainVideoViewPlaying) {
        isMainVideoViewPlaying = mainVideoViewPlaying;
        videoPausePortIv.setVisibility(GONE);
        videoPauseLandIv.setVisibility(GONE);
        videoRefreshPortIv.setVisibility(GONE);
        videoRefreshLandIv.setVisibility(GONE);
        videoPptSwitchPortIv.setVisibility(GONE);
        videoPptSwitchLandIv.setVisibility(GONE);
        liveMoreControlLl.setVisibility(View.GONE);
        liveControlFloatingLandIv.setVisibility(GONE);
        //由于控件隐藏，因此需要调整信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32 + 74);//74=(40+40+40+14+14)/2
        mlp.rightMargin = ConvertUtils.dp2px(38 + 74);//74=(40+40+40+14+14)/2
        startSendMessageLandIv.setLayoutParams(mlp);

        getLiveMediaDispatcher().updateViewProperties();
    }

    @Override
    public void updateWhenVideoViewPrepared() {
        isMainVideoViewPlaying = true;
        videoPausePortIv.setVisibility(VISIBLE);
        videoPauseLandIv.setVisibility(VISIBLE);
        videoRefreshPortIv.setVisibility(VISIBLE);
        videoRefreshLandIv.setVisibility(VISIBLE);
        if (isServerEnablePPT) {
            videoPptSwitchPortIv.setVisibility(VISIBLE);
            videoPptSwitchLandIv.setVisibility(VISIBLE);
        }
        liveMoreControlLl.setVisibility(View.VISIBLE);
        liveControlFloatingLandIv.setVisibility(VISIBLE);
        //还原信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32);
        mlp.rightMargin = ConvertUtils.dp2px(38);
        startSendMessageLandIv.setLayoutParams(mlp);

        getLiveMediaDispatcher().updateViewProperties();
    }

    @Override
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
        if (liveWatchOnlineCountLandTv != null) {
            liveWatchOnlineCountLandTv.setVisibility(isShowOnlineCount ? View.VISIBLE : View.GONE);
        }
        String viewerCountText = StringUtils.toKString(onlineCount);
        String text = PLVAppUtils.formatString(R.string.plv_player_viewer_online_count, viewerCountText);
        liveWatchOnlineCountLandTv.setText(text);
    }

    @Override
    public void updateWhenJoinRtc(boolean isHideRefreshButton) {
        isRtcChannelWatch = true;
        hideRefreshButtonInRtcChannel = isHideRefreshButton;
        getLiveMediaDispatcher().updateViewProperties();
    }

    @Override
    public void updateWhenLeaveRtc() {
        isRtcChannelWatch = false;
        getLiveMediaDispatcher().updateViewProperties();
    }

    @Override
    public void updateWhenRequestJoinLinkMic(boolean isRequestJoinLinkMic) {
        isRequestingJoinLinkMic = isRequestJoinLinkMic;
        getLiveMediaDispatcher().updateViewProperties();
    }

    @Override
    public void updateWhenLinkMicOpenOrClose(boolean isOpenLinkMic) {
        if (isChannelOpenLinkMic == isOpenLinkMic) {
            return;
        }
        isChannelOpenLinkMic = isOpenLinkMic;
        if (!isOpenLinkMic) {
            isRequestingJoinLinkMic = false;
            getLiveMediaDispatcher().updateViewProperties();
        }
    }

    @Override
    public void updateWhenJoinLinkMic(boolean isHideRefreshButton) {
        isLinkMic = true;
        isRequestingJoinLinkMic = false;
        hideRefreshButtonInRtcChannel = isHideRefreshButton;
        getLiveMediaDispatcher().updateViewProperties();
        updatePaintModeEntrance();
        notifyPPTSwitchViewChanged(VISIBLE_FORCE_VISIBLE, true);
    }

    @Override
    public void updateWhenLeaveLinkMic() {
        isLinkMic = false;
        isRequestingJoinLinkMic = false;
        getLiveMediaDispatcher().updateViewProperties();
        updatePaintModeEntrance();
        notifyPPTSwitchViewChanged(VISIBLE_FOR_SERVER, false);
    }

    @Override
    public void updateWhenOnlyAudio(boolean isOnlyAudio) {
        moreLayout.updateWhenOnlyAudio(isOnlyAudio);
    }

    @Override
    public void updateOnClickCloseFloatingView() {
        videoPptSwitchPortIv.performClick();
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
    public void updatePPTStatusChange(PLVPPTStatus plvpptStatus) {
        pptTurnPagePortLayout.updatePageData(plvpptStatus);
        pptTurnPageLandLayout.updatePageData(plvpptStatus);
    }

    @Override
    public void showMoreLayout() {
        if (PLVScreenUtils.isPortrait(getContext())) {
            if (morePortIv != null) {
                morePortIv.performClick();
            }
        } else {
            if (moreLandIv != null) {
                moreLandIv.performClick();
            }
        }
    }

    @Override
    public void dispatchDanmuSwitchOnClicked(View v) {
        this.onClick(v);
    }

    @Override
    public void notifyLowLatencyUpdate(boolean isLowLatency) {
        this.isLowLatencyWatch = isLowLatency;
        moreLayout.updateViewWithLatency(isLowLatency);
    }

    @Override
    public void notifyChatroomStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus) {
        if (startSendMessageLandIv != null) {
            String text = PLVAppUtils.getString(isCloseRoomStatus ? R.string.plv_chat_input_tips_chatroom_close_2 : (isFocusModeStatus ? R.string.plv_chat_input_tips_focus : R.string.plv_chat_input_tips_chat));
            startSendMessageLandIv.setText(text);
            startSendMessageLandIv.setOnClickListener((!isCloseRoomStatus && !isFocusModeStatus) ? this : null);
        }
    }

    @Override
    public void setChatIsDisplayEnabled(boolean isDisplayEnabled) {
        this.isChatDisplayEnabled = isDisplayEnabled;
        if (startSendMessageLandIv != null) {
            startSendMessageLandIv.setVisibility(isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
        }
        if (likesLandIv != null) {
            likesLandIv.setVisibility(isLikesSwitchEnabled && isChatDisplayEnabled ? View.VISIBLE : View.INVISIBLE);
        }
        if (rewardView != null) {
            rewardView.setVisibility(isRewardSwitchEnabled && isChatDisplayEnabled ? VISIBLE : GONE);
        }
    }

    @Override
    public void clean() {
        if (moreLayout != null) {
            moreLayout.hide();
        }

        dispose(bitPopupWindowTimer);
        dispose(reopenFloatingDelay);
    }

    @Override
    public void updateRewardView(boolean enable) {
        this.isRewardSwitchEnabled = enable;
        if (rewardView != null) {
            rewardView.setVisibility(isRewardSwitchEnabled && isChatDisplayEnabled ? VISIBLE : GONE);
        }
    }

    @Override
    public void notifyPaintModeStatus(boolean isInPaintMode) {
        this.isInPaintMode = isInPaintMode;
        if (isInPaintMode) {
            hide();
        } else {
            show();
        }
    }

    @Override
    public void updateFloatWindowView(boolean isOpenFloatWindowEnable) {
        this.isFloatWindowEnable = isOpenFloatWindowEnable;
        if (liveControlFloatingLandIv != null && !isFloatWindowEnable) {
            liveControlFloatingLandIv.setVisibility(GONE);
        }
        if (liveControlFloatingIv != null && !isFloatWindowEnable) {
            liveControlFloatingIv.setVisibility(GONE);
        }

    }

    @Override
    public void setShowOnlineCount(boolean showOnlineCount) {
        this.isShowOnlineCount = showOnlineCount;
        if (liveWatchOnlineCountLandTv != null) {
            liveWatchOnlineCountLandTv.setVisibility(showOnlineCount ? View.VISIBLE : View.GONE);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 重写View定义的方法">
    @Override
    public void setVisibility(int visibility) {
        if (!canShow()) {
            visibility = View.GONE;
        }

        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            landscapeController.show();
        } else {
            landscapeController.hide();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制栏 - 视频准备完成后更新view">
    private void updateMoreLayout() {
        int playMode = livePlayerPresenter.getMediaPlayMode();
        Pair<List<PolyvDefinitionVO>, Integer> listIntegerPair = new Pair<>(livePlayerPresenter.getBitrateVO(), livePlayerPresenter.getBitratePos());
        int[] lines = new int[]{livePlayerPresenter.getLinesCount(), livePlayerPresenter.getLinesPos()};
        moreLayout.updateViewWithPlayInfo(playMode, listIntegerPair, lines);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制栏 - 显示/隐藏码率切换弹层">
    private void showBitrateChangeView() {
        if (definitionVOS == null
                || currentBitratePos >= definitionVOS.size() - 1
                || livePlayerPresenter.getMediaPlayMode() == PolyvMediaPlayMode.MODE_AUDIO) {
            return;
        }

        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        View showView = null;
        if (videoControllerPortLy.getVisibility() == View.VISIBLE) {
            showView = videoRefreshPortIv;
        } else if (videoControllerLandLy.getVisibility() == View.VISIBLE) {
            showView = videoRefreshLandIv;
        }
        if (showView == null) {
            return;
        }
        showView.getLocationOnScreen(location);
        if ((location[0] == location[1]) && location[0] == 0) {
            location[0] = ConvertUtils.dp2px(16);
            location[1] = ConvertUtils.dp2px(126);
        }

        if (bitRatePopupWindow == null) {
            createBitrateChangeWindow();
        }

        //在控件上方显示
        View child = bitRatePopupWindow.getContentView();
        TextView definition = (TextView) child.findViewById(R.id.live_bitrate_popup_definition_tv);

        PolyvDefinitionVO definitionVO = definitionVOS.get(Math.max(0, currentBitratePos + 1));//超清，高清，标清
        definition.setText(definitionVO.getDefinition());

        definition.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBitPopup();
                livePlayerPresenter.changeBitRate(currentBitratePos + 1);
            }
        });

        child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int popupHeight = child.getMeasuredHeight();
        bitRatePopupWindow.showAtLocation(showView, Gravity.NO_GRAVITY, (location[0] + 10), location[1] - popupHeight - 10);
        dispose(bitPopupWindowTimer);
        bitPopupWindowTimer = PLVRxTimer.delay(5000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                hideBitPopup();
            }
        });

    }

    private void hideBitPopup() {
        if (bitRatePopupWindow != null) {
            bitRatePopupWindow.dismiss();
        }
    }

    private void createBitrateChangeWindow() {
        View child = View.inflate(getContext(), R.layout.plvlc_live_controller_bitrate_popup_layout, null);
        bitRatePopupWindow = new PopupWindow(child, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        bitRatePopupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        bitRatePopupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        bitRatePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bitRatePopupWindow.setOutsideTouchable(true);
        bitRatePopupWindow.update();

        bitRatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dispose(bitPopupWindowTimer);
            }
        });
    }

    private void updateBitrateVO() {
        definitionVOS = livePlayerPresenter.getBitrateVO();
        currentBitratePos = livePlayerPresenter.getBitratePos();
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="控制流 - PPT切换按钮控制">
    private static final int VISIBLE_FOR_SERVER = 1;
    private static final int VISIBLE_FORCE_VISIBLE = 2;
    private void notifyPPTSwitchViewChanged(int visibleType, boolean useFullScreenStyle) {
        // 纯视频频道才使用全屏样式
        useFullScreenStyle = useFullScreenStyle && !isServerEnablePPT;
        if (visibleType == VISIBLE_FOR_SERVER) {
            setServerEnablePPT(isServerEnablePPT);
        } else {
            videoPptSwitchPortIv.setVisibility(View.VISIBLE);
            videoPptSwitchLandIv.setVisibility(View.VISIBLE);
        }
        // 全屏按钮resource
        int fullScreenResource = R.drawable.plvlc_controller_linkmic_sub_selector;
        // imageResource can‘t update state
        videoPptSwitchPortIv.setImageDrawable(useFullScreenStyle ? getResources().getDrawable(fullScreenResource) : getResources().getDrawable(R.drawable.plvlc_controller_ppt_sub_selector));
        videoPptSwitchLandIv.setImageDrawable(useFullScreenStyle ? getResources().getDrawable(fullScreenResource) : getResources().getDrawable(R.drawable.plvlc_controller_ppt_sub_selector));
        videoPptSwitchPortIv.setSelected(false);
        videoPptSwitchLandIv.setSelected(false);
        // 纯视频频道使用全屏样式的情况下，竖屏的按钮不显示
        if (useFullScreenStyle) {
            videoPptSwitchPortIv.setVisibility(View.GONE);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        PLVCommonLog.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
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
                videoControllerPortLy.setVisibility(View.GONE);
                videoControllerLandLy.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setPortraitController() {
        post(new Runnable() {
            @Override
            public void run() {
                videoControllerLandLy.setVisibility(View.GONE);
                videoControllerPortLy.setVisibility(View.VISIBLE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private boolean canShow() {
        return !isInPaintMode;
    }

    private void updatePaintModeEntrance() {
        final boolean showEntrance = isLinkMic && PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.LIVE_DOCUMENT_ALLOW_USE_PAINT_ON_LINKMIC);
        livePlayerControllerEnterPaintIv.setVisibility(showEntrance ? View.VISIBLE : View.GONE);
        livePlayerControllerEnterPaintLandIv.setVisibility(showEntrance ? View.VISIBLE : View.GONE);
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="事件监听 - 监听播放信息变化">
    private void observePlayInfoVO() {
        livePlayerPresenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                getLiveMediaDispatcher().acceptPlayInfo(playInfoVO);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_pause_port_iv || id == R.id.video_pause_land_iv) {
            getLiveMediaDispatcher().changePlayPause();
        } else if (id == R.id.video_screen_switch_port_iv) {
            PLVOrientationManager.getInstance().setLandscape((Activity) getContext());
        } else if (id == R.id.video_refresh_port_iv || id == R.id.video_refresh_land_iv) {
            livePlayerPresenter.restartPlay();
        } else if (id == R.id.back_port_iv) {
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.back_land_iv) {
            PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
        } else if (id == R.id.more_port_iv) {
            hide();
            moreLayout.showWhenPortrait(getHeight());
        } else if (id == R.id.more_land_iv) {
            hide();
            moreLayout.showWhenLandscape();
        } else if (id == R.id.start_send_message_land_tv) {
            hide();
            if (onViewActionListener != null) {
                onViewActionListener.onStartSendMessageAction();
            }
        } else if (id == R.id.video_ppt_switch_port_iv || id == R.id.video_ppt_switch_land_iv) {
            //selected == true时就是隐藏状态，false时是显示状态
            boolean isNotShowState = videoPptSwitchPortIv.isSelected();
            boolean isShowState = !isNotShowState;
            videoPptSwitchPortIv.setSelected(isShowState);
            videoPptSwitchLandIv.setSelected(isShowState);
            if (onViewActionListener != null) {
                onViewActionListener.onClickShowOrHideSubTab(isNotShowState);
            }
            if (isLinkMic) {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_player_layout_adjusted)
                        .show();
            }
        } else if (id == R.id.bulletin_land_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onShowBulletinAction();
            }
        } else if (id == R.id.likes_land_iv) {
            show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    likesLandIv.addLoveIcon(1);
                }
            }, 200);
            if (onViewActionListener != null) {
                onViewActionListener.onSendLikesAction();
            }
        } else if (id == R.id.plvlc_iv_show_point_reward){
            if (onViewActionListener != null) {
                onViewActionListener.onShowRewardView();
                hide();
            }
        } else if (id == liveControlFloatingIv.getId()
                || id == liveControlFloatingLandIv.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onClickFloating();
            }
        } else if (id == commodityView.getId()) {
            commodityViewModel.showProductLayoutOnLandscape();
        } else if (id == livePlayerControllerEnterPaintIv.getId() || id == livePlayerControllerEnterPaintLandIv.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onEnterPaintMode();
            }
        } else if (liveWatchOnlineCountLandTv != null && id == liveWatchOnlineCountLandTv.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onShowLandscapeMemberList();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 不同观看模式的分发">

    private LiveMediaDispatcher getLiveMediaDispatcher() {
        if (isLinkMic) {
            return new LinkMicMediaDispatcher();
        }
        if (isRtcChannelWatch) {
            return new RtcMediaDispatcher();
        }
        if (isLowLatencyWatch) {
            return new LowLatencyMediaDispatcher();
        }
        return new VideoViewDispatcher();
    }

    private interface LiveMediaDispatcher {

        void updateViewProperties();

        boolean isPlaying();

        void changePlayPause();

        void acceptPlayInfo(@Nullable PLVPlayInfoVO playInfoVO);

    }

    private class VideoViewDispatcher implements LiveMediaDispatcher {

        @Override
        public void updateViewProperties() {
            videoPausePortIv.setVisibility(VISIBLE);
            videoPauseLandIv.setVisibility(VISIBLE);
            videoRefreshPortIv.setVisibility(VISIBLE);
            videoRefreshLandIv.setVisibility(VISIBLE);
            liveControlFloatingIv.setVisibility(isRequestingJoinLinkMic || !isFloatWindowEnable ? View.GONE : View.VISIBLE);
            liveControlFloatingLandIv.setVisibility(isRequestingJoinLinkMic || !isFloatWindowEnable ? View.GONE : View.VISIBLE);
            final boolean showMoreButton = isMainVideoViewPlaying && !PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing();
            morePortIv.setVisibility(showMoreButton ? View.VISIBLE : View.GONE);
            moreLandIv.setVisibility(showMoreButton ? View.VISIBLE : View.GONE);
            //还原信息发送控件的宽度
            MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
            mlp.leftMargin = ConvertUtils.dp2px(32);
            mlp.rightMargin = ConvertUtils.dp2px(38);
            startSendMessageLandIv.setLayoutParams(mlp);
        }

        @Override
        public boolean isPlaying() {
            return livePlayerPresenter.isPlaying();
        }

        @Override
        public void changePlayPause() {
            if (isPlaying()) {
                livePlayerPresenter.pause();
            } else {
                livePlayerPresenter.restartPlay();
            }
        }

        @Override
        public void acceptPlayInfo(@Nullable PLVPlayInfoVO playInfoVO) {
            if (playInfoVO == null) {
                return;
            }
            videoPausePortIv.setSelected(playInfoVO.isPlaying());
            videoPauseLandIv.setSelected(playInfoVO.isPlaying());
        }
    }

    private class LowLatencyMediaDispatcher extends VideoViewDispatcher {

    }

    private class RtcMediaDispatcher implements LiveMediaDispatcher {
        @Override
        public void updateViewProperties() {
            videoPausePortIv.setVisibility(VISIBLE);
            videoPauseLandIv.setVisibility(VISIBLE);
            if (hideRefreshButtonInRtcChannel) {
                videoRefreshPortIv.setVisibility(GONE);
                videoRefreshLandIv.setVisibility(GONE);
            }
            liveControlFloatingIv.setVisibility(isRequestingJoinLinkMic || !isFloatWindowEnable ? View.GONE : View.VISIBLE);
            liveControlFloatingLandIv.setVisibility(isRequestingJoinLinkMic || !isFloatWindowEnable ? View.GONE : View.VISIBLE);
            final boolean showMoreButton = isMainVideoViewPlaying && !PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing();
            morePortIv.setVisibility(showMoreButton ? View.VISIBLE : View.GONE);
            moreLandIv.setVisibility(showMoreButton ? View.VISIBLE : View.GONE);
            //由于控件隐藏，因此需要调整信息发送控件的宽度
            MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
            mlp.leftMargin = ConvertUtils.dp2px(32 + 47);
            mlp.rightMargin = ConvertUtils.dp2px(38 + 47);
            startSendMessageLandIv.setLayoutParams(mlp);
        }

        @Override
        public boolean isPlaying() {
            if (onViewActionListener == null) {
                return false;
            }
            return !onViewActionListener.isRtcPausing();
        }

        @Override
        public void changePlayPause() {
            if (onViewActionListener == null) {
                return;
            }
            final boolean toPause = isPlaying();
            //更新播放器本身的状态
            if(toPause){
                livePlayerPresenter.pause();
            } else {
                livePlayerPresenter.resume();
            }
            onViewActionListener.onRtcPauseResume(toPause);
            acceptPlayInfo(null);
        }

        @Override
        public void acceptPlayInfo(@Nullable PLVPlayInfoVO playInfoVO) {
            final boolean isPlaying = isPlaying();
            videoPausePortIv.setSelected(isPlaying);
            videoPauseLandIv.setSelected(isPlaying);
        }
    }

    private class LinkMicMediaDispatcher extends RtcMediaDispatcher {
        @Override
        public void updateViewProperties() {
            moreLayout.hide();

            videoPausePortIv.setVisibility(GONE);
            videoPauseLandIv.setVisibility(GONE);
            if (hideRefreshButtonInRtcChannel) {
                videoRefreshPortIv.setVisibility(GONE);
                videoRefreshLandIv.setVisibility(GONE);
            }
            liveControlFloatingIv.setVisibility(View.GONE);
            liveControlFloatingLandIv.setVisibility(View.GONE);
            morePortIv.setVisibility(View.GONE);
            moreLandIv.setVisibility(View.GONE);
            //由于控件隐藏，因此需要调整信息发送控件的宽度
            MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
            mlp.leftMargin = ConvertUtils.dp2px(32 + 47);
            mlp.rightMargin = ConvertUtils.dp2px(38 + 47);
            startSendMessageLandIv.setLayoutParams(mlp);
        }
    }

    // </editor-fold>
}
