package com.easefun.polyv.livecloudclass.modules.media;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeItem;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeUtils;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeView;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.liveroom.IPLVLiveLandscapePlayerController;
import com.easefun.polyv.livecloudclass.modules.media.controller.IPLVLCPlaybackMediaController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCDanmuController;
import com.easefun.polyv.livecloudclass.modules.media.danmu.IPLVLCLandscapeMessageSender;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuFragment;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCDanmuWrapper;
import com.easefun.polyv.livecloudclass.modules.media.danmu.PLVLCLandscapeMessageSendPanel;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLightTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCPlaceHolderView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCProgressTipsView;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVideoLoadingLayout;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCVolumeTipsView;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackPlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.view.PLVAbsPlaybackPlayerView;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * 云课堂场景下的回放播放器布局，实现 IPLVLCMediaLayout 接口
 */
public class PLVLCPlaybackMediaLayout extends FrameLayout implements IPLVLCMediaLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCPlaybackMediaLayout.class.getSimpleName();
    private static final float RATIO_WH = 16f / 9;//播放器竖屏宽高使用16:9比例
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //播放器渲染视图view
    private PolyvPlaybackVideoView videoView;
    private View playerView;
    //controller
    private IPLVLCPlaybackMediaController mediaController;
    //播放失败时显示的view
    private PLVLCPlaceHolderView noStreamView;
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
    // tips view
    private PLVLCLightTipsView lightTipsView;
    private PLVLCVolumeTipsView volumeTipsView;
    private PLVLCProgressTipsView progressTipsView;

    //横屏聊天区
    private PLVLCChatLandscapeLayout chatLandscapeLayout;

    //弹幕
    private IPLVLCDanmuController danmuController;
    //弹幕包装器
    private PLVLCDanmuWrapper danmuWrapper;
    //信息发送输入框弹窗
    private IPLVLCLandscapeMessageSender landscapeMessageSender;

    //跑马灯控件
    private PolyvMarqueeView marqueeView;
    private PolyvMarqueeItem marqueeItem;
    private PolyvMarqueeUtils marqueeUtils;

    //播放器presenter
    private IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter playbackPlayerPresenter;
    //listener
    private IPLVLCMediaLayout.OnViewActionListener onViewActionListener;
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
        initView();
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
        logoView = findViewById(R.id.playback_logo_view);
        loadingLayout = findViewById(R.id.plvlc_playback_loading_layout);
        lightTipsView = findViewById(R.id.plvlc_playback_tipsview_light);
        volumeTipsView = findViewById(R.id.plvlc_playback_tipsview_volume);
        progressTipsView = findViewById(R.id.plvlc_playback_tipsview_progress);
        chatLandscapeLayout = findViewById(R.id.plvlc_chat_landscape_ly);

        flPlayerSwitchViewParent = findViewById(R.id.plvlc_playback_fl_player_switch_view_parent);
        switchAnchorPlayer = findViewById(R.id.plvlc_playback_switch_anchor_player);

        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);

        initVideoView();
        initDanmuView();
        initMediaController();
        initLoadingView();
        initSwitchView();
        initLayoutWH();
    }

    private void initVideoView() {
        //设置noStreamView
        noStreamView.setPlaceHolderImg(R.drawable.plvlc_bg_player_no_stream);
        noStreamView.setPlaceHolderText(getResources().getString(R.string.plv_player_video_playback_no_stream));

        videoView.setSubVideoView(subVideoView);
        videoView.setMediaController(mediaController);
        videoView.setNoStreamIndicator(noStreamView);
        videoView.setPlayerBufferingIndicator(loadingLayout);
        //设置跑马灯
        videoView.post(new Runnable() {
            @Override
            public void run() {
                marqueeView = ((Activity) getContext()).findViewById(R.id.plvlc_marquee_view);//after videoLayout add, post find
                marqueeItem = new PolyvMarqueeItem();
                videoView.setMarqueeView(marqueeView, marqueeItem);
            }
        });
    }

    private void initDanmuView() {
        danmuController = new PLVLCDanmuFragment();
        FragmentTransaction fragmentTransaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.plvlc_danmu_ly, (Fragment) danmuController, "danmuFragment").commitAllowingStateLoss();

        danmuWrapper = new PLVLCDanmuWrapper(this);
        danmuWrapper.setDanmuController(danmuController);
        danmuWrapper.setDanmuSwitchLandView(mediaController.getLandscapeDanmuSwitchView());

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
    public void setLandscapeControllerView(@NonNull IPLVLiveLandscapePlayerController landscapeControllerView) {

    }

    @Override
    public void updateViewerCount(long viewerCount) {

    }

    @Override
    public void updateWhenJoinRTC(int linkMicLayoutLandscapeWidth) {

    }

    @Override
    public void updateWhenLeaveRTC() {

    }

    @Override
    public void notifyRTCPrepared() {

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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCMediaLayout定义的playback方法">
    @Override
    public int getDuration() {
        return playbackPlayerPresenter.getDuration();
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
        public View getBufferingIndicator() {
            return super.getBufferingIndicator();
        }

        @Override
        public PLVPlayerLogoView getLogo() {
            return logoView;
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
            ToastUtils.showLong(tips);
            PLVCommonLog.e(TAG, tips);
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
        public void onDoubleClick() {
            super.onDoubleClick();
            mediaController.playOrPause();
        }

        @Override
        public void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo, String viewerName) {
            super.onGetMarqueeVo(marqueeVo, viewerName);
            if (marqueeUtils == null) {
                marqueeUtils = new PolyvMarqueeUtils();
            }
            // 更新为后台设置的跑马灯类型
            marqueeUtils.updateMarquee((Activity) getContext(), marqueeVo, marqueeItem, viewerName);
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
    }

    private void setPortrait() {
        //videoLayout root
        MarginLayoutParams vlp = (MarginLayoutParams) getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //获取高度不要用videoLayout.getWidth()来计算，因为此时宽度并不是全屏的，还有右边的margin占了。
        int portraitWidth = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        vlp.height = (int) (portraitWidth / RATIO_WH);
        setLayoutParams(vlp);
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
