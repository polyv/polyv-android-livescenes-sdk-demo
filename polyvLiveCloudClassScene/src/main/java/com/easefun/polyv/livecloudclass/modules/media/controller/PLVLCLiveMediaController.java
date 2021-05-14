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
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecloudclass.modules.liveroom.IPLVLiveLandscapePlayerController;
import com.easefun.polyv.livecloudclass.modules.media.widget.PLVLCLiveMoreLayout;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;

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
    //更多按钮
    private ImageView morePortIv;
    //顶部渐变view
    private ImageView gradientBarTopPortView;
    //重新打开悬浮窗提示
    private TextView tvReopenFloatingViewTip;

    //横屏皮肤
    private IPLVLiveLandscapePlayerController landscapeController;
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

    //已经显示过"可从此处重新打开浮窗"的提示了
    private boolean hasShowReopenFloatingViewTip = false;
    //延迟隐藏"可从此处重新打开浮窗"
    private Disposable reopenFloatingDelay;

    //服务端的PPT开关
    private boolean isServerEnablePPT;

    //view动作监听器
    private OnViewActionListener onViewActionListener;

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
        backPortIv.setOnClickListener(this);
        videoNamePortTv = findViewById(R.id.video_name_port_tv);
        videoViewerCountPortTv = findViewById(R.id.video_viewer_count_port_tv);
        videoPausePortIv = findViewById(R.id.video_pause_port_iv);
        videoPausePortIv.setOnClickListener(this);
        videoRefreshPortIv = findViewById(R.id.video_refresh_port_iv);
        videoRefreshPortIv.setOnClickListener(this);
        videoPptSwitchPortIv = findViewById(R.id.video_ppt_switch_port_iv);
        videoPptSwitchPortIv.setOnClickListener(this);
        videoScreenSwitchPortIv = findViewById(R.id.video_screen_switch_port_iv);
        videoScreenSwitchPortIv.setOnClickListener(this);
        morePortIv = findViewById(R.id.more_port_iv);
        morePortIv.setOnClickListener(this);
        gradientBarTopPortView = findViewById(R.id.gradient_bar_top_port_view);
        tvReopenFloatingViewTip = findViewById(R.id.plvlc_live_player_controller_tv_reopen_floating_view);

        //more layout
        initMoreLayout();

        //choose right orientation
        if (ScreenUtils.isPortrait()) {
            videoControllerPortLy.setVisibility(View.VISIBLE);
        } else {
            videoControllerPortLy.setVisibility(View.INVISIBLE);
        }
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
            onViewActionListener.onShow(false);
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
            onViewActionListener.onShow(true);
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
    public void setLandscapeController(@NonNull IPLVLiveLandscapePlayerController landscapeController) {
        this.landscapeController = landscapeController;
        //横屏控制栏布局
        videoControllerLandLy = landscapeController.getLandRoot();
        backLandIv = landscapeController.getBackView();
        backLandIv.setOnClickListener(this);
        videoNameLandTv = landscapeController.getNameView();
        videoViewerCountLandTv = landscapeController.getViewerCountView();
        videoPauseLandIv = landscapeController.getPauseView();
        videoPauseLandIv.setOnClickListener(this);
        videoRefreshLandIv = landscapeController.getRefreshView();
        videoRefreshLandIv.setOnClickListener(this);
        videoPptSwitchLandIv = landscapeController.getSwitchView();
        videoPptSwitchLandIv.setOnClickListener(this);
        startSendMessageLandIv = landscapeController.getMessageSender();
        startSendMessageLandIv.setOnClickListener(this);
        danmuSwitchLandIv = landscapeController.getDanmuSwitchView();
        danmuSwitchLandIv.setOnClickListener(this);
        bulletinLandIv = landscapeController.getBulletinView();
        bulletinLandIv.setOnClickListener(this);
        likesLandIv = landscapeController.getLikesView();
        likesLandIv.setOnButtonClickListener(this);
        moreLandIv = landscapeController.getMoreView();
        moreLandIv.setOnClickListener(this);

        //choose right orientation
        if (ScreenUtils.isPortrait()) {
            videoControllerLandLy.setVisibility(View.GONE);
        } else {
            videoControllerLandLy.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setOnLikesSwitchEnabled(boolean isSwitchEnabled) {
        likesLandIv.setVisibility(isSwitchEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setServerEnablePPT(boolean enable) {
        this.isServerEnablePPT = enable;
        videoPptSwitchPortIv.setVisibility(enable ? View.VISIBLE : View.GONE);
        videoPptSwitchLandIv.setVisibility(enable ? View.VISIBLE : View.GONE);
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
    public void updateWhenSubVideoViewClick() {
        videoPausePortIv.setVisibility(GONE);
        videoPauseLandIv.setVisibility(GONE);
        videoRefreshPortIv.setVisibility(GONE);
        videoRefreshLandIv.setVisibility(GONE);
        videoPptSwitchPortIv.setVisibility(GONE);
        videoPptSwitchLandIv.setVisibility(GONE);
        morePortIv.setVisibility(View.GONE);
        moreLandIv.setVisibility(View.GONE);
        //由于控件隐藏，因此需要调整信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32 + 74);//74=(40+40+40+14+14)/2
        mlp.rightMargin = ConvertUtils.dp2px(38 + 74);//74=(40+40+40+14+14)/2
        startSendMessageLandIv.setLayoutParams(mlp);
    }

    @Override
    public void updateWhenVideoViewPrepared() {
        videoPausePortIv.setVisibility(VISIBLE);
        videoPauseLandIv.setVisibility(VISIBLE);
        videoRefreshPortIv.setVisibility(VISIBLE);
        videoRefreshLandIv.setVisibility(VISIBLE);
        if (isServerEnablePPT) {
            videoPptSwitchPortIv.setVisibility(VISIBLE);
            videoPptSwitchLandIv.setVisibility(VISIBLE);
        }
        morePortIv.setVisibility(View.VISIBLE);
        moreLandIv.setVisibility(View.VISIBLE);
        //还原信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32);
        mlp.rightMargin = ConvertUtils.dp2px(38);
        startSendMessageLandIv.setLayoutParams(mlp);
    }

    @Override
    public void updateViewerCount(long viewerCount) {
        videoViewerCountPortTv.setVisibility(View.VISIBLE);
        videoViewerCountLandTv.setVisibility(View.VISIBLE);

        String viewerCountText = StringUtils.toWString(viewerCount);

        videoViewerCountPortTv.setText(viewerCountText + "次播放");
        videoViewerCountLandTv.setText(viewerCountText + "次播放");
    }

    @Override
    public void updateWhenJoinLinkMic(boolean isHideRefreshButton) {
        moreLayout.hide();

        videoPausePortIv.setVisibility(GONE);
        videoPauseLandIv.setVisibility(GONE);
        if (isHideRefreshButton) {
            videoRefreshPortIv.setVisibility(GONE);
            videoRefreshLandIv.setVisibility(GONE);
        }
        morePortIv.setVisibility(View.GONE);
        moreLandIv.setVisibility(View.GONE);
        //由于控件隐藏，因此需要调整信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32 + 47);//47=(40+40+14)/2
        mlp.rightMargin = ConvertUtils.dp2px(38 + 47);//47=(40+40+14)/2
        startSendMessageLandIv.setLayoutParams(mlp);
    }

    @Override
    public void updateWhenLeaveLinkMic() {
        videoPausePortIv.setVisibility(VISIBLE);
        videoPauseLandIv.setVisibility(VISIBLE);
        videoRefreshPortIv.setVisibility(VISIBLE);
        videoRefreshLandIv.setVisibility(VISIBLE);
        morePortIv.setVisibility(View.VISIBLE);
        moreLandIv.setVisibility(View.VISIBLE);
        //还原信息发送控件的宽度
        MarginLayoutParams mlp = (MarginLayoutParams) startSendMessageLandIv.getLayoutParams();
        mlp.leftMargin = ConvertUtils.dp2px(32);
        mlp.rightMargin = ConvertUtils.dp2px(38);
        startSendMessageLandIv.setLayoutParams(mlp);
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
    public void clean() {
        if (moreLayout != null) {
            moreLayout.hide();
        }

        dispose(bitPopupWindowTimer);
        dispose(reopenFloatingDelay);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 重写View定义的方法">
    @Override
    public void setVisibility(int visibility) {
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
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="事件监听- 监听播放信息变化">
    private void observePlayInfoVO() {
        livePlayerPresenter.getData().getPlayInfoVO().observe((LifecycleOwner) getContext(), new Observer<PLVPlayInfoVO>() {
            @Override
            public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                if (playInfoVO == null) {
                    return;
                }
                if (playInfoVO.isPlaying()) {
                    videoPausePortIv.setSelected(true);
                    videoPauseLandIv.setSelected(true);

                } else {
                    videoPausePortIv.setSelected(false);
                    videoPauseLandIv.setSelected(false);

                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.video_pause_port_iv || id == R.id.video_pause_land_iv) {
            boolean isPlaying = livePlayerPresenter.isPlaying();
            if (isPlaying) {
                livePlayerPresenter.pause();
            } else {
                livePlayerPresenter.restartPlay();
            }
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
        }
    }
    // </editor-fold>

}
