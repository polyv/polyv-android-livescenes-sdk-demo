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
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVEmptyMediaController;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.PLVLivePlayerPresenter;
import com.easefun.polyv.livecommon.module.modules.player.live.view.PLVAbsLivePlayerView;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.PLVVideoSizeUtils;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.constant.PLVECFitMode;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveAudioModeView;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * date: 2020-04-29
 * author: hwj
 * description:直播播放器布局
 */
public class PLVECLiveVideoLayout extends FrameLayout implements IPLVECVideoLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    //直播播放器横屏视频、音频模式的播放器区域位置
    private Rect videoViewRect;
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //主播放器渲染视图view
    private PolyvLiveVideoView videoView;
    //子播放器渲染视图view
    private PolyvAuxiliaryVideoview subVideoView;
    //音频模式view
    private IPolyvLiveAudioModeView audioModeView;
    //控制器
    private IPolyvMediaController mediaController;
    //播放器缓冲显示的view
    private View loadingView;
    //当前没有直播显示的view
    private View nostreamView;
    //浮窗关闭按钮
    private ImageView closeFloatingView;
    //播放器presenter
    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;

    //主播放器父控件的父控件
    private ViewGroup videoViewParentParent;
    //主播放器父控件在父控件中的位置索引
    private int videoViewParentIndexInParent;
    //主播放器父控件是否已经从VideoLayout中分离出来
    private boolean isVideoViewParentDetachVideoLayout;
    //播放器及其布局在VideoLayout中设置的适配方式
    private int fitMode = PLVECFitMode.FIT_NONE;

    //Listener
    private IPLVECVideoLayout.OnViewActionListener onViewActionListener;
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
        videoView = findViewById(R.id.plvec_live_video_view);
        subVideoView = findViewById(R.id.sub_video_view);
        audioModeView = findViewById(R.id.audio_mode_ly);
        loadingView = findViewById(R.id.loading_pb);
        nostreamView = findViewById(R.id.nostream_ly);
        closeFloatingView = findViewById(R.id.close_floating_iv);
        closeFloatingView.setOnClickListener(this);
        mediaController = new PLVEmptyMediaController();
        videoViewParentParent = (ViewGroup) videoView.getParent().getParent();
        videoViewParentIndexInParent = videoViewParentParent.indexOfChild((View) videoView.getParent());

        videoView.setSubVideoView(subVideoView);
        videoView.setAudioModeView(audioModeView);
        videoView.setPlayerBufferingIndicator(loadingView);
        videoView.setNoStreamIndicator(nostreamView);
        videoView.setMediaController(mediaController);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的common方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        livePlayerPresenter = new PLVLivePlayerPresenter(liveRoomDataManager);
        livePlayerPresenter.registerView(livePlayerView);
        livePlayerPresenter.init();
    }

    @Override
    public void startPlay() {
        livePlayerPresenter.startPlay();
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
    public boolean isPlaying() {
        return livePlayerPresenter.isPlaying();
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
    public View detachVideoViewParent() {
        //调整播放器及其布局的适配方式
        PLVVideoSizeUtils.fitVideoRect(true, videoView.getParent(), videoViewRect);
        videoView.setAspectRatio(PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT);
        subVideoView.setAspectRatio(PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT);
        //调整手势配置
        videoView.setNeedGestureDetector(false);
        subVideoView.setNeedGestureDetector(false);
        //调整背景色
        videoView.setBackgroundColor(Color.parseColor("#6F6F6F"));
        //调整背景色同时也是为了覆盖主播放器暂无直播时的盖图
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
        fitVideoRatioAndRect();
        //还原手势配置
        videoView.setNeedGestureDetector(true);
        subVideoView.setNeedGestureDetector(true);
        //还原背景色
        videoView.setBackgroundColor(Color.TRANSPARENT);
        subVideoView.setBackgroundColor(Color.TRANSPARENT);

        closeFloatingView.setVisibility(View.GONE);
        isVideoViewParentDetachVideoLayout = false;
    }

    @Override
    public void destroy() {
        if (audioModeView != null) {
            audioModeView.onHide();
        }
        if (livePlayerPresenter != null) {
            livePlayerPresenter.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的live方法">
    public void setVideoViewRect(Rect videoViewRect) {
        this.videoViewRect = videoViewRect;
        if (!isVideoViewParentDetachVideoLayout) {
            fitVideoRatioAndRect();
        }
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API- 实现IPLVECVideoLayout定义的playback方法，空实现">

    @Override
    public int getDuration() {
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
    public LiveData<PLVPlayInfoVO> getPlayInfoVO() {
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
        public void onSubVideoViewPlay(boolean isFirst) {
            super.onSubVideoViewPlay(isFirst);
            fitMode = PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_SUB_VIDEOVIEW;
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRatioAndRect(subVideoView, videoView.getParent(), videoViewRect);//传主播放器viewParent
            }
        }

        @Override
        public void onPlayError(PolyvPlayError error, String tips) {
            super.onPlayError(error, tips);
            ToastUtils.showLong(tips);
            fitMode = PLVECFitMode.FIT_VIDEO_RECT_FALSE;
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            }
        }

        @Override
        public void onNoLiveAtPresent() {
            super.onNoLiveAtPresent();
            fitMode = PLVECFitMode.FIT_VIDEO_RECT_FALSE;
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            }
            ToastUtils.showShort(R.string.plv_player_toast_no_live);
        }

        @Override
        public void onLiveStop() {
            super.onLiveStop();
            fitMode = PLVECFitMode.FIT_VIDEO_RECT_FALSE;
            if (!isVideoViewParentDetachVideoLayout) {
                PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
            }
        }

        @Override
        public void onLiveEnd() {
            super.onLiveEnd();
            ToastUtils.showShort(R.string.plv_player_toast_live_end);
        }

        @Override
        public void onPrepared(int mediaPlayMode) {
            super.onPrepared(mediaPlayMode);
            if (mediaPlayMode == PolyvMediaPlayMode.MODE_VIDEO) {
                fitMode = PLVECFitMode.FIT_VIDEO_RATIO_AND_RECT_VIDEOVIEW;
                if (!isVideoViewParentDetachVideoLayout) {
                    PLVVideoSizeUtils.fitVideoRatioAndRect(videoView, videoView.getParent(), videoViewRect);
                }
            } else if (mediaPlayMode == PolyvMediaPlayMode.MODE_AUDIO) {
                fitMode = PLVECFitMode.FIT_VIDEO_RECT_FALSE;
                if (!isVideoViewParentDetachVideoLayout) {
                    PLVVideoSizeUtils.fitVideoRect(false, videoView.getParent(), videoViewRect);
                }
            }
        }

        @Override
        public void onLinesChanged(int linesPos) {
            super.onLinesChanged(linesPos);
        }
    };
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
        }
    }
    // </editor-fold>
}
