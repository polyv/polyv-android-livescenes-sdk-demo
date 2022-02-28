package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.auxiliary.IPolyvAuxiliaryVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.marquee.IPLVMarqueeView;
import com.easefun.polyv.livecommon.module.modules.marquee.PLVMarqueeCommonController;
import com.easefun.polyv.livecommon.module.modules.marquee.model.PLVMarqueeModel;
import com.easefun.polyv.livecommon.module.modules.player.playback.contract.IPLVPlaybackPlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlaybackPlayerData;
import com.easefun.polyv.livecommon.module.modules.watermark.IPLVWatermarkView;
import com.easefun.polyv.livecommon.module.modules.watermark.PLVWatermarkCommonController;
import com.easefun.polyv.livecommon.module.modules.watermark.PLVWatermarkTextVO;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerRetryLayout;
import com.easefun.polyv.livescenes.model.PolyvPlaybackVO;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livescenes.playback.video.api.IPolyvPlaybackListenerEvent;
import com.easefun.polyv.mediasdk.player.IMediaPlayer;
import com.plv.business.api.common.player.listener.IPLVVideoViewListenerEvent;
import com.plv.business.model.video.PLVWatermarkVO;
import com.plv.foundationsdk.config.PLVPlayOption;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVControlUtils;
import com.plv.livescenes.marquee.PLVMarqueeSDKController;

import java.lang.ref.WeakReference;



/**
 * mvp-回放播放器presenter层实现，实现 IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter 接口
 */
public class PLVPlaybackPlayerPresenter implements IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVPlaybackPlayerPresen";
    private static final int WHAT_PLAY_PROGRESS = 1;
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVPlaybackPlayerData playbackPlayerData;
    private WeakReference<IPLVPlaybackPlayerContract.IPlaybackPlayerView> vWeakReference;
    //当前子播放器片头广告或者暖场的超链接
    private String subVideoViewHerf = "";
    //设置是否启动片头广告
    private boolean isAllowOpenAdHead = false;
    private PolyvPlaybackVideoView videoView;
    private PolyvAuxiliaryVideoview subVideoView;
    //显示的logo
    private PLVPlayerLogoView logoView;
    //设置是否允许跑马灯运行
    private boolean isAllowMarqueeRunning = true;
    private boolean isAllowWatermarkShow = true;
    //手势滑动进度
    private int fastForwardPos;

    private IPolyvVideoViewListenerEvent.OnGestureClickListener onSubGestureClickListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVPlaybackPlayerPresenter(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        playbackPlayerData = new PLVPlaybackPlayerData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVPlaybackPlayerContract.IPlaybackPlayerPresenter定义的方法">
    @Override
    public void registerView(@NonNull IPLVPlaybackPlayerContract.IPlaybackPlayerView v) {
        this.vWeakReference = new WeakReference<>(v);
        v.setPresenter(this);
    }

    @Override
    public void unregisterView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
        }
    }

    @Override
    public void init() {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        if (view == null) {
            return;
        }
        //init data
        videoView = view.getPlaybackVideoView();
        subVideoView = view.getSubVideoView();
        initVideoViewListener();
        initSubVideoViewListener();
    }

    @Override
    public void setAllowOpenAdHead(boolean isAllowOpenAdHead) {
        this.isAllowOpenAdHead = isAllowOpenAdHead;
    }

    @Override
    public void startPlay() {
        PolyvPlaybackVideoParams playbackVideoParams = new PolyvPlaybackVideoParams(
                getConfig().getVid(),
                getConfig().getChannelId(),
                getConfig().getAccount().getUserId(),
                getConfig().getUser().getViewerId()
        );
        playbackVideoParams.buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.HEAD_AD, isAllowOpenAdHead)
                .buildOptions(PolyvBaseVideoParams.PARAMS2, getConfig().getUser().getViewerName())
                .buildOptions(PolyvPlaybackVideoParams.ENABLE_ACCURATE_SEEK, true)
                .buildOptions(PolyvPlaybackVideoParams.VIDEO_LISTTYPE, liveRoomDataManager.getConfig().getVideoListType());
        if (videoView != null) {
            videoView.playByMode(playbackVideoParams, PLVPlayOption.PLAYMODE_VOD);
        }
        startPlayProgressTimer();
    }

    @Override
    public void pause() {
        if (videoView != null) {
            videoView.pause();
            updatePlayInfo();
        }
    }

    @Override
    public void resume() {
        if (videoView != null) {
            videoView.start();
            updatePlayInfo();
        }
        removeWatermark();
        showWatermarkView(true);
    }

    @Override
    public void stop() {
        if (videoView != null) {
            videoView.stopPlay();
        }
    }

    @Override
    public int getDuration() {
        if (videoView != null) {
            return videoView.getDuration();
        }

        return 0;
    }

    @Override
    public void seekTo(int duration) {
        if (videoView != null) {
            videoView.seekTo(duration);
        }
    }

    @Override
    public void seekTo(int progress, int max) {
        if (videoView != null && videoView.isInPlaybackStateEx()) {
            int seekPosition = (int) ((long) videoView.getDuration() * progress / max);
            if (!videoView.isCompletedState()) {
                videoView.seekTo(seekPosition);
            } else if (seekPosition < videoView.getDuration()) {
                videoView.seekTo(seekPosition);
                videoView.start();
            }
        }
    }

    @Override
    public boolean isPlaying() {
        if (videoView != null) {
            return videoView.isPlaying();
        }
        return false;
    }

    @Override
    public boolean isInPlaybackState() {
        if (videoView != null) {
            return videoView.isInPlaybackState();
        }
        return false;
    }

    @Override
    public boolean isSubVideoViewShow() {
        if (subVideoView != null) {
            return subVideoView.isShow();
        }
        return false;
    }

    @Override
    public void setSpeed(float speed) {
        if (videoView != null) {
            videoView.setSpeed(speed);
        }
    }

    @Override
    public float getSpeed() {
        if (videoView != null) {
            return videoView.getSpeed();
        }
        return 0;
    }

    @Override
    public void setVolume(int volume) {
        if (videoView != null) {
            videoView.setVolume(volume);
        }
    }

    @Override
    public int getVolume() {
        return videoView == null ? 0 : videoView.getVolume();
    }

    @Override
    public void setPlayerVolume(int volume) {
        if (videoView != null) {
            videoView.setPlayerVolume(volume);
        }
        if (subVideoView != null) {
            subVideoView.setPlayerVolume(volume);
        }
    }

    @Override
    public void setPlayerVid(String vid) {
        liveRoomDataManager.setConfigVid(vid);
    }

    @Override
    public void setPlayerVidAndPlay(String vid) {
        liveRoomDataManager.setConfigVid(vid);
        startPlay();
    }

    @Override
    public void bindPPTView(IPolyvPPTView pptView) {
        if (videoView != null) {
            videoView.bindPPTView(pptView);
        }
    }

    @Override
    public String getSubVideoViewHerf() {
        if (subVideoView.isShow()) {
            return subVideoViewHerf;
        } else {
            return "";
        }

    }

    @NonNull
    @Override
    public PLVPlaybackPlayerData getData() {
        return playbackPlayerData;
    }

    @Override
    public String getVideoName() {
        if (videoView != null) {
            PolyvPlaybackVO.DataBean dataBean = videoView.getModleVO();
            if (dataBean != null) {
                return dataBean.getTitle();
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        stopMarqueeView();
        removeWatermark();
        unregisterView();
        if (logoView != null) {
            logoView.removeAllViews();
            logoView = null;
        }

        if (subVideoView != null) {
            subVideoView.destroy();
            subVideoView = null;
        }

        if (videoView != null) {
            videoView.destroy();
            videoView = null;
        }

        stopPlayProgressTimer();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - logo 显示的控制">
    private void setLogoVisibility(int visible) {
        if (logoView != null) {
            logoView.setVisibility(visible);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 重试Layout显示的控制">
    private void setRetryLayoutVisibility(int visible){
        if(getView() != null && getView().getRetryLayout() != null){
            getView().getRetryLayout().setVisibility(visible);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="播放器 - 初始化subVideo, videoView的监听器配置">
    private void initSubVideoViewListener() {
        if (subVideoView != null) {
            subVideoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
                @Override
                public void onPlay(boolean isFirst) {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewPlay(isFirst);
                    }
                }
            });
            onSubGestureClickListener = new IPolyvVideoViewListenerEvent.OnGestureClickListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    if (!TextUtils.isEmpty(subVideoViewHerf)) {
                        PLVWebUtils.openWebLink(subVideoViewHerf, subVideoView.getContext());
                    }
                }
            };
            subVideoView.setOnGestureClickListener(onSubGestureClickListener);
            subVideoView.setOnSubVideoViewLoadImage(new IPolyvAuxiliaryVideoViewListenerEvent.IPolyvOnSubVideoViewLoadImage() {
                @Override
                public void onLoad(String imageUrl, final ImageView imageView, final String coverHref) {
                    if (!TextUtils.isEmpty(coverHref)) {
                        PLVImageLoader.getInstance().loadImage(subVideoView.getContext(), imageUrl, imageView);
                    }
                    subVideoViewHerf = coverHref;
                    if (!TextUtils.isEmpty(coverHref)) {
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PLVWebUtils.openWebLink(coverHref, subVideoView.getContext());
                            }
                        });
                    }
                }
            });
            subVideoView.setOnSubVideoViewCountdownListener(new IPolyvAuxiliaryVideoViewListenerEvent.IPolyvOnSubVideoViewCountdownListener() {
                @Override
                public void onCountdown(int totalTime, int remainTime, int adStage) {
                    boolean isOpenAdHead = subVideoView != null && subVideoView.isOpenHeadAd();
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null)
                        view.onSubVideoViewCountDown(isOpenAdHead, totalTime, remainTime, adStage);
                }

                @Override
                public void onVisibilityChange(boolean isShow) {
                    boolean isOpenAdHead = subVideoView != null && subVideoView.isOpenHeadAd();
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewVisiblityChanged(isOpenAdHead, isShow);
                    }
                }
            });
        }
    }

    // <editor-fold defaultstate="collapsed" desc="播放器 - 初始化videoView的监听器配置">
    private void initVideoViewListener() {
        if (videoView != null) {
            videoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    playbackPlayerData.postPrepared();
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onPrepared();
                    }
                    setLogoVisibility(View.VISIBLE);
                    setRetryLayoutVisibility(View.GONE);
                    setAllowMarqueeViewRunning(true);
                }

                @Override
                public void onPreparing() {
                    PLVCommonLog.d(TAG, "onPreparing");
                }
            });
            videoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
                @Override
                public void onError(int what, int extra) {
                    PLVCommonLog.d(TAG, "onError:" + what);
                }

                @Override
                public void onError(PolyvPlayError error) {
                    String tips;
                    switch (error.playStage) {
                        case PolyvPlayError.PLAY_STAGE_HEADAD:
                            tips = "片头广告";
                            break;
                        case PolyvPlayError.PLAY_STAGE_TAILAD:
                            tips = "片尾广告";
                            break;
                        case PolyvPlayError.PLAY_STAGE_TEASER:
                            tips = "暖场视频";
                            break;
                        default:
                            if (error.isMainStage()) {
                                tips = "主视频";
                            } else {
                                tips = "";
                            }
                            break;
                    }
                    tips += "播放异常\n" +
                            error.errorDescribe +
                            "(" + error.errorCode + "-" + error.playStage + ")\n" +
                            error.playPath;

                    setLogoVisibility(View.GONE);
                    setRetryLayoutVisibility(View.VISIBLE);
                    stopMarqueeView();
                    removeWatermark();
                }
            });
            videoView.setOnCompletionListener(new IPolyvVideoViewListenerEvent.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onCompletion();
                    }
                    stopMarqueeView();
                    removeWatermark();
                }
            });
            videoView.setOnInfoListener(new IPolyvVideoViewListenerEvent.OnInfoListener() {
                @Override
                public void onInfo(int what, int extra) {
                    if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                        if (view != null) {
                            view.onBufferStart();
                        }
                    } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                        if (view != null) {
                            view.onBufferEnd();
                        }
                    }
                }
            });
            videoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
                @Override
                public void onPlay(boolean isFirst) {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onVideoPlay(isFirst);
                    }
                    setAllowMarqueeViewRunning(true);
                    removeWatermark();
                    showWatermarkView(true);
                }
            });
            videoView.setOnVideoPauseListener(new IPolyvVideoViewListenerEvent.OnVideoPauseListener() {
                @Override
                public void onPause() {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onVideoPause();
                    }
                    setAllowMarqueeViewRunning(false);
                }
            });
            videoView.setOnGestureLeftDownListener(new IPolyvVideoViewListenerEvent.OnGestureLeftDownListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    int brightness = videoView.getBrightness((Activity) videoView.getContext()) - 8;
                    brightness = Math.max(0, brightness);
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        boolean result = view.onLightChanged(brightness, end);
                        if (start && result) {
                            videoView.setBrightness((Activity) videoView.getContext(), brightness);
                        }
                    }
                }
            });
            videoView.setOnGestureLeftUpListener(new IPolyvVideoViewListenerEvent.OnGestureLeftUpListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    int brightness = videoView.getBrightness((Activity) videoView.getContext()) + 8;
                    brightness = Math.min(100, brightness);
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        boolean result = view.onLightChanged(brightness, end);
                        if (start && result) {
                            videoView.setBrightness((Activity) videoView.getContext(), brightness);
                        }
                    }
                }
            });
            videoView.setOnGestureRightDownListener(new IPolyvVideoViewListenerEvent.OnGestureRightDownListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    int volume = videoView.getVolume() - PLVControlUtils.getVolumeValidProgress(videoView.getContext(), 8);
                    volume = Math.max(0, volume);
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        boolean result = view.onVolumeChanged(volume, end);
                        if (start && result) {
                            videoView.setVolume(volume);
                        }
                    }
                }
            });
            videoView.setOnGestureRightUpListener(new IPolyvVideoViewListenerEvent.OnGestureRightUpListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    int volume = videoView.getVolume() + PLVControlUtils.getVolumeValidProgress(videoView.getContext(), 8);
                    volume = Math.min(100, volume);
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        boolean result = view.onVolumeChanged(volume, end);
                        if (start && result) {
                            videoView.setVolume(volume);
                        }
                    }
                }
            });
            videoView.setOnGestureDoubleClickListener(new IPolyvVideoViewListenerEvent.OnGestureDoubleClickListener() {
                @Override
                public void callback() {
                    if (videoView.isInPlaybackStateEx()) {
                        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                        if (view != null) {
                            view.onDoubleClick();
                        }
                    }
                }
            });
            videoView.setOnGestureSwipeLeftListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeLeftListener() {
                @Override
                public void callback(boolean start, boolean end, int times) {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (videoView.isInPlaybackStateEx()) {
                        if (fastForwardPos == 0) {
                            fastForwardPos = videoView.getCurrentPosition();
                        }
                        fastForwardPos -= 1000 * times;
                        if (fastForwardPos <= 0) {
                            fastForwardPos = -1;
                        }
                        if (end) {
                            fastForwardPos = Math.max(0, fastForwardPos);
                            if (view != null) {
                                boolean result = view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, false);
                                if (result) {
                                    videoView.seekTo(fastForwardPos);
                                    if (videoView.isCompletedState()) {
                                        videoView.start();
                                    }
                                }
                            }
                            fastForwardPos = 0;
                            return;
                        }
                        if (view != null) {
                            view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, false);
                        }
                    } else if (end) {
                        fastForwardPos = 0;
                        if (view != null) {
                            view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, false);
                        }
                    }
                }
            });
            videoView.setOnGestureSwipeRightListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeRightListener() {
                @Override
                public void callback(boolean start, boolean end, int times) {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (videoView.isInPlaybackStateEx()) {
                        if (fastForwardPos == 0) {
                            fastForwardPos = videoView.getCurrentPosition();
                        }
                        fastForwardPos += 1000 * times;
                        if (fastForwardPos > videoView.getDuration()) {
                            fastForwardPos = videoView.getDuration();
                        }
                        if (end) {
                            if (view != null) {
                                boolean result = view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, true);
                                if (result) {
                                    if (!videoView.isCompletedState()) {
                                        videoView.seekTo(fastForwardPos);
                                    } else if (fastForwardPos < videoView.getDuration()) {
                                        videoView.seekTo(fastForwardPos);
                                        videoView.start();
                                    }
                                }
                            }
                            fastForwardPos = 0;
                        }
                        if (view != null) {
                            view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, true);
                        }
                    } else if (end) {
                        fastForwardPos = 0;
                        if (view != null) {
                            view.onProgressChanged(fastForwardPos, videoView.getDuration(), end, true);
                        }
                    }
                }


            });
            videoView.setOnGetMarqueeVoListener(new IPolyvVideoViewListenerEvent.OnGetMarqueeVoListener() {
                @Override
                public void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo) {
                    if (!isMarqueeExisted()) {
                        return;
                    }
                    PLVMarqueeCommonController.getInstance().updateMarqueeView(marqueeVo,
                            getConfig().getUser().getViewerName(),
                            new PLVMarqueeCommonController.IPLVMarqueeControllerCallback() {
                                @Override
                                public void onMarqueeModel(@PLVMarqueeSDKController.MARQUEE_CONTROLLER_TIP int controllerTip,
                                                           PLVMarqueeModel marqueeModel) {
                                    switch (controllerTip) {
                                        case PLVMarqueeSDKController.ALLOW_PLAY_MARQUEE:
                                            isAllowMarqueeRunning = true;
                                            stopMarqueeView();
                                            setMarqueeViewModel(marqueeModel);
                                            break;
                                        case PLVMarqueeSDKController.NOT_ALLOW_PLAY_MARQUEE:
                                            isAllowMarqueeRunning = false;
                                            stopMarqueeView();
                                            break;
                                        case PLVMarqueeSDKController.MARQUEE_SIGN_ERROR:
                                        case PLVMarqueeSDKController.NOT_ALLOW_PLAY_VIDEO:
                                            isAllowMarqueeRunning = false;
                                            final Activity activity = (Activity) videoView.getContext();
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String msg = PLVMarqueeCommonController.getInstance().getErrorMessage();
                                                    Toast.makeText(
                                                            activity,
                                                            "".equals(msg) ? "跑马灯验证失败" : msg,
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                    activity.finish();
                                                }
                                            });
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            });
                }
            });
            videoView.setOnGetWatermarkVOListener(new IPLVVideoViewListenerEvent.OnGetWatermarkVoListener() {
                @Override
                public void onGetWatermarkVO(final PLVWatermarkVO waterMarkVO) {
                    PLVWatermarkCommonController.getInstance().updateWatermarkView(waterMarkVO,
                            getConfig().getUser().getViewerName());
                    if (!isWatermarkExisted()) {
                        return;
                    }
                    if ("N".equals(waterMarkVO.watermarkRestrict)) {
                        removeWatermark();
                    } else {
                        setWatermarkTextVO(waterMarkVO);
                    }
                }
            });
            videoView.setOnDanmuServerOpenListener(new IPolyvVideoViewListenerEvent.OnDanmuServerOpenListener() {
                @Override
                public void onDanmuServerOpenListener(boolean isServerDanmuOpen) {
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onServerDanmuOpen(isServerDanmuOpen);
                    }
                }
            });
            videoView.setOnPPTShowListener(new IPolyvVideoViewListenerEvent.OnPPTShowListener() {
                @Override
                public void showPPTView(int visible) {
                    playbackPlayerData.postPPTShowState(visible == View.VISIBLE);
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        view.onShowPPTView(visible);
                    }
                }
            });
            videoView.setOnGetLogoListener(new IPolyvVideoViewListenerEvent.OnGetLogoListener() {
                @Override
                public void onLogo(String logoImage, int logoAlpha, int logoPosition, String logoHref) {
                    if (TextUtils.isEmpty(logoImage)) {
                        return;
                    }
                    IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
                    if (view != null) {
                        logoView = view.getLogo();
                        if (logoView != null) {
                            logoView.removeAllLogo();
                            logoView.addLogo(new PLVPlayerLogoView.LogoParam().setWidth(0.14F).setHeight(0.25F)
                                    .setAlpha(logoAlpha).setOffsetX(0.03F).setOffsetY(0.06F).setPos(logoPosition).setResUrl(logoImage));
                        }
                    }
                }
            });
            videoView.setOnRetryListener(new IPolyvPlaybackListenerEvent.OnRetryListener() {
                @Override
                public boolean onRetryFailed() {
                    if(getView() != null && getView().getRetryLayout() != null){
                        ((PLVPlayerRetryLayout)getView().getRetryLayout()).onRetryFailed("重试失败");
                    }
                    //false表示使用sdk内部逻辑重试，true表示拦截重试逻辑，开发者自己处理
                    return false;
                }
            });
        }
    }


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 定时获取播放信息任务">
    private Handler selfHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PLAY_PROGRESS) {
                startPlayProgressTimer();
            }
        }
    };

    private void startPlayProgressTimer() {
        stopPlayProgressTimer();
        if (videoView != null) {
            int position = updatePlayInfo();

            selfHandler.sendEmptyMessageDelayed(WHAT_PLAY_PROGRESS, 1000 - (position % 1000));
        } else {
            selfHandler.sendEmptyMessageDelayed(WHAT_PLAY_PROGRESS, 1000);
        }
    }

    private void stopPlayProgressTimer() {
        selfHandler.removeMessages(WHAT_PLAY_PROGRESS);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 获取播放信息">
    private int updatePlayInfo() {
        // 单位：毫秒
        int position = videoView.getCurrentPosition();
        int totalTime = videoView.getDuration() / 1000 * 1000;
        if (videoView.isCompletedState() || position > totalTime) {
            position = totalTime;
        }
        int bufPercent = videoView.getBufferPercentage();
        PLVPlayInfoVO.Builder builder = new PLVPlayInfoVO.Builder()
                .position(position).totalTime(totalTime)
                .bufPercent(bufPercent).isPlaying(videoView.isPlaying());
        if (subVideoView != null) {
            builder.isSubViewPlaying(subVideoView.isPlaying());
        }
        playbackPlayerData.postPlayInfoVO(builder.build());

        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        if (view != null) {
            view.updatePlayInfo(builder.build());
        }
        return position;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 跑马灯显示控制">
    private boolean isMarqueeExisted() {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVMarqueeView marqueeView = view.getMarqueeView();
        return marqueeView != null;
    }

    private void setMarqueeViewModel(PLVMarqueeModel marqueeViewModel) {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVMarqueeView marqueeView = view.getMarqueeView();
        if (marqueeView!=null) {
            marqueeView.setPLVMarqueeModel(marqueeViewModel);
        }
    }

    private void setAllowMarqueeViewRunning(boolean allow) {
        if (!isAllowMarqueeRunning) {
            return;
        }
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVMarqueeView marqueeView = view.getMarqueeView();
        if (marqueeView != null) {
            if (allow) {
                marqueeView.start();
            } else {
                marqueeView.pause();
            }
        }
    }

    private void stopMarqueeView() {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVMarqueeView marqueeView = view.getMarqueeView();
        if (marqueeView!=null) {
            marqueeView.stop();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 水印显示控制">
    private boolean isWatermarkExisted() {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVWatermarkView watermarkView = view.getWatermarkView();
        return watermarkView != null;
    }

    private void setWatermarkTextVO(PLVWatermarkVO plvWatermarkVO) {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVWatermarkView watermarkView = view.getWatermarkView();
        PLVWatermarkTextVO plvWatermarkTextVO = new PLVWatermarkTextVO();
        switch(plvWatermarkVO.watermarkType){
            case "fixed":
                plvWatermarkTextVO.setContent(plvWatermarkVO.watermarkContent)
                        .setFontSize(plvWatermarkVO.watermarkFontSize)
                        .setFontAlpha(plvWatermarkVO.watermarkOpacity);
                break;
            case "nickname":
                plvWatermarkTextVO.setContent(getConfig().getUser().getViewerName())
                        .setFontSize(plvWatermarkVO.watermarkFontSize)
                        .setFontAlpha(plvWatermarkVO.watermarkOpacity);
                break;
            default:
                PLVCommonLog.d(TAG,"设置水印失败，默认为空");
                break;
        }

        if (watermarkView != null) {
            watermarkView.setPLVWatermarkVO(plvWatermarkTextVO);
        }
    }

    private void showWatermarkView(boolean allow) {
        if (!isAllowWatermarkShow) {
            return;
        }
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        IPLVWatermarkView watermarkView = view.getWatermarkView();
        if (watermarkView != null) {
            if (allow) {
                watermarkView.showWatermark();
            } else {
                watermarkView.removeWatermark();
            }
        }
    }

    private void removeWatermark() {
        IPLVPlaybackPlayerContract.IPlaybackPlayerView view = getView();
        if(view != null){
            IPLVWatermarkView watermarkView = view.getWatermarkView();
            if (watermarkView != null) {
                watermarkView.removeWatermark();
            }
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private IPLVPlaybackPlayerContract.IPlaybackPlayerView getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    private PLVLiveChannelConfig getConfig() {
        return liveRoomDataManager.getConfig();
    }
    // </editor-fold>
}
