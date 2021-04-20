package com.easefun.polyv.liveecommerce.modules.player;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.graphics.Color;
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
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.PLVPlaybackPlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.view.PLVAbsPlaybackPlayerView;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.constant.PLVECFitMode;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

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
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
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

    //主播放器父控件的父控件
    private ViewGroup videoViewParentParent;
    //主播放器父控件在父控件中的位置索引
    private int videoViewParentIndexInParent;
    //主播放器父控件是否已经从VideoLayout中分离出来
    private boolean isVideoViewParentDetachVideoLayout;
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
        videoView = findViewById(R.id.plvec_playback_video_item);
        subVideoView = findViewById(R.id.sub_video_view);
        tvCountDown = findViewById(R.id.auxiliary_tv_count_down);
        llAuxiliaryCountDown = findViewById(R.id.polyv_auxiliary_controller_ll_tips);
        llAuxiliaryCountDown.setVisibility(GONE);
        closeFloatingView = findViewById(R.id.close_floating_iv);
        closeFloatingView.setOnClickListener(this);
        videoViewParentParent = (ViewGroup) videoView.getParent().getParent();
        videoViewParentIndexInParent = videoViewParentParent.indexOfChild((View) videoView.getParent());
        videoView.setSubVideoView(subVideoView);

        playCenterView = findViewById(R.id.play_center);
        hidePlayCenterView();
        playCenterView.setOnClickListener(this);

        logoView = findViewById(R.id.logo_view);

        initSubVideoViewChangeListener();
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
    public View detachVideoViewParent() {
        //调整播放器及其布局的适配方式
        videoView.setAspectRatio(PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT);
        subVideoView.setAspectRatio(PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT);
        //调整手势配置
        videoView.setNeedGestureDetector(false);
        subVideoView.setNeedGestureDetector(false);
        //调整背景色
        videoView.setBackgroundColor(Color.parseColor("#6F6F6F"));
        subVideoView.setBackgroundColor(Color.parseColor("#6F6F6F"));

        videoViewParentParent.removeView((View) videoView.getParent());
        closeFloatingView.setVisibility(View.VISIBLE);
        isVideoViewParentDetachVideoLayout = true;
        return (View) videoView.getParent();
    }

    @Override
    public void attachVideoViewParent(View view) {
        //添加view之后再还原配置
        videoViewParentParent.addView(view, videoViewParentIndexInParent);

        //还原播放器及其布局的适配方式
        if (fitMode == PLVECFitMode.FIT_VIDEO_RATIO_VIDEOVIEW) {
            PLVVideoSizeUtils.fitVideoRatio(videoView);
        }
        //调整手势配置
        videoView.setNeedGestureDetector(true);
        subVideoView.setNeedGestureDetector(true);
        //调整背景色
        videoView.setBackgroundColor(Color.TRANSPARENT);
        subVideoView.setBackgroundColor(Color.TRANSPARENT);

        closeFloatingView.setVisibility(View.GONE);
        isVideoViewParentDetachVideoLayout = false;
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
    public void setVideoViewRect(Rect videoViewRect) {
        PLVCommonLog.d(TAG, "直播带货回放场景 暂无调整视频区域布局");
    }

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
    public LiveData<PLVPlayInfoVO> getPlaybackPlayInfoVO() {
        return playbackPlayerPresenter.getData().getPlayInfoVO();
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
            fitMode = PLVECFitMode.FIT_VIDEO_RATIO_VIDEOVIEW;
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRatio(videoView);
            }
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            ToastUtils.showLong(tips);
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
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRatio(subVideoView);
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
