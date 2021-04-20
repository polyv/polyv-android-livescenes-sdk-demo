package com.easefun.polyv.livecommon.module.modules.player.live.presenter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.auxiliary.IPolyvAuxiliaryVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveChannelVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVLivePlayerData;
import com.easefun.polyv.livecommon.module.modules.player.live.presenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.config.PLVPlayOption;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVControlUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * mvp-直播播放器presenter层实现，实现 IPLVLivePlayerContract.ILivePlayerPresenter 接口
 */
public class PLVLivePlayerPresenter implements IPLVLivePlayerContract.ILivePlayerPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLivePlayerPresenter.class.getSimpleName();
    private static final int WHAT_PLAY_PROGRESS = 1;
    //设置是否要开启片头广告
    private boolean isAllowOpenAdHead = false;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVLivePlayerData livePlayerData;
    private WeakReference<IPLVLivePlayerContract.ILivePlayerView> vWeakReference;
    //当前子播放器片头广告或者暖场的超链接
    private String subVideoViewHerf = "";

    private PolyvLiveVideoView videoView;
    private PolyvAuxiliaryVideoview subVideoView;
    //logo对象
    private PLVPlayerLogoView logoView;

    private IPolyvVideoViewListenerEvent.OnGestureClickListener onSubGestureClickListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLivePlayerPresenter(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        livePlayerData = new PLVLivePlayerData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLivePlayerContract.ILivePlayerPresenter定义的方法">
    @Override
    public void registerView(@NonNull IPLVLivePlayerContract.ILivePlayerView v) {
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
        IPLVLivePlayerContract.ILivePlayerView view = getView();
        if (view == null) {
            return;
        }
        //init data
        videoView = view.getLiveVideoView();
        subVideoView = view.getSubVideoView();
        initSubVideoViewListener();
        initVideoViewListener();
    }

    @Override
    public void setAllowOpenAdHead(boolean isAllowOpenAdHead) {
        this.isAllowOpenAdHead = isAllowOpenAdHead;
    }

    @Override
    public void startPlay() {
        PolyvLiveVideoParams liveVideoParams = new PolyvLiveVideoParams(
                getConfig().getChannelId(),
                getConfig().getAccount().getUserId(),
                getConfig().getUser().getViewerId()
        );
        liveVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.HEAD_AD, isAllowOpenAdHead)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.PARAMS2, getConfig().getUser().getViewerName());
        if (videoView != null) {
            videoView.playByMode(liveVideoParams, PLVPlayOption.PLAYMODE_LIVE);
        }
        startPlayProgressTimer();
    }

    @Override
    public void restartPlay() {
        IPLVLivePlayerContract.ILivePlayerView view = getView();
        if (view != null) {
            view.onRestartPlay();
        }
        startPlay();
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
    }

    @Override
    public void stop() {
        if (videoView != null) {
            videoView.stopPlay();
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
    public int getLinesCount() {
        return (videoView == null || videoView.getModleVO() == null || videoView.getModleVO().getLines() == null)
                ? 1 : videoView.getModleVO().getLines().size();
    }

    @Nullable
    @Override
    public List<PolyvDefinitionVO> getBitrateVO() {
        List<PolyvDefinitionVO> definitionVOS = null;
        if (videoView != null) {
            PolyvLiveChannelVO channelVO = videoView.getModleVO();
            if (channelVO != null) {
                List<PolyvLiveLinesVO> liveLines = channelVO.getLines();//线路数量
                if (liveLines != null) {
                    PolyvLiveLinesVO linesVO = liveLines.get(getLinesPos());//当前线路信息
                    if (linesVO != null && linesVO.getMultirateModel() != null) {//存在码率信息
                        if (channelVO.isMutilrateEnable()) {//多码率可用
                            definitionVOS = linesVO.getMultirateModel().getDefinitions();
                        } else {
                            definitionVOS = new ArrayList<>();
                            definitionVOS.add(new PolyvDefinitionVO(linesVO.getMultirateModel().getDefaultDefinition()
                                    , linesVO.getMultirateModel().getDefaultDefinitionUrl()));
                        }
                    }
                }
            }
        }
        return definitionVOS;
    }

    @Override
    public int getMediaPlayMode() {
        if (videoView != null) {
            return videoView.getMediaPlayMode();
        }
        return PolyvMediaPlayMode.MODE_VIDEO;
    }

    @Override
    public void changeMediaPlayMode(int mediaPlayMode) {
        if (videoView != null) {
            videoView.changeMediaPlayMode(mediaPlayMode);
        }
    }

    @Override
    public void changeLines(int linesPos) {
        if (videoView != null) {
            videoView.changeLines(linesPos);
        }
    }

    @Override
    public void changeBitRate(int bitRate) {
        if (videoView != null) {
            videoView.changeBitRate(bitRate);
        }
    }

    @Override
    public Bitmap screenshot() {
        return videoView == null ? null : videoView.screenshot();
    }

    @Override
    public PolyvLiveChannelVO getChannelVO() {
        return videoView == null ? null : videoView.getModleVO();
    }

    @Override
    public int getLinesPos() {
        return videoView == null ? 0 : videoView.getLinesPos();
    }

    @Override
    public int getBitratePos() {
        return videoView == null ? 0 : videoView.getBitratePos();
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
    public void setNeedGestureDetector(boolean need) {
        if (videoView != null) {
            videoView.setNeedGestureDetector(need);
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
    public PLVLivePlayerData getData() {
        return livePlayerData;
    }

    @Override
    public void destroy() {
        stopPlayProgressTimer();
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
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 初始化subVideo, videoView的监听器配置">
    private void initSubVideoViewListener() {
        if (subVideoView != null) {
            subVideoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
                @Override
                public void onPlay(boolean isFirst) {
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewPlay(isFirst);
                    }
                }
            });
            onSubGestureClickListener = new IPolyvVideoViewListenerEvent.OnGestureClickListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    boolean mainPlayerIsPlaying = videoView != null && videoView.isPlaying();
                    if (subVideoView != null && subVideoView.isPlaying()) {
                        mainPlayerIsPlaying = false;
                    }
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewClick(mainPlayerIsPlaying);
                    }
                    if (!TextUtils.isEmpty(subVideoViewHerf)) {
                        PLVWebUtils.openWebLink(subVideoViewHerf, subVideoView.getContext());
                    }
                }
            };
            subVideoView.setOnGestureClickListener(onSubGestureClickListener);
            subVideoView.setOnSubVideoViewLoadImage(new IPolyvAuxiliaryVideoViewListenerEvent.IPolyvOnSubVideoViewLoadImage() {
                @Override
                public void onLoad(String imageUrl, final ImageView imageView, final String coverHref) {
                    PLVImageLoader.getInstance().loadImage(subVideoView.getContext(), imageUrl, imageView);
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
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewCountDown(isOpenAdHead, totalTime, remainTime, adStage);
                        if (isOpenAdHead) {
                            setLogoVisibility(View.VISIBLE);
                        } else {
                            setLogoVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onVisibilityChange(boolean isShow) {
                    boolean isOpenAdHead = subVideoView != null && subVideoView.isOpenHeadAd();
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onSubVideoViewVisiblityChanged(isOpenAdHead, isShow);
                    }
                }
            });
        }
    }

    private void initVideoViewListener() {
        if (videoView != null) {
            videoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
                @Override
                public void onError(int what, int extra) {/**/}

                @Override
                public void onError(PolyvPlayError error) {
                    setDefaultViewStatus();
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

                    tips += "播放异常\n" + error.errorDescribe + " (errorCode:" + error.errorCode +
                            "-" + error.playStage + ")\n" + error.playPath;
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onPlayError(error, tips);
                    }
                    setLogoVisibility(View.GONE);
                }
            });
            videoView.setOnNoLiveAtPresentListener(new IPolyvLiveListenerEvent.OnNoLiveAtPresentListener() {
                @Override
                public void onNoLiveAtPresent() {
                    PLVCommonLog.d(TAG, "onNoLiveAtPresent");
                    videoView.removeRenderView();
                    livePlayerData.postNoLive();
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onNoLiveAtPresent();
                    }
                    setLogoVisibility(View.GONE);
                }

                @Override
                public void onLiveEnd() {
                    PLVCommonLog.d(TAG, "onLiveEnd");
                    livePlayerData.postLiveEnd();
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onLiveEnd();
                    }
                    setLogoVisibility(View.GONE);
                }

                @Override
                public void onLiveStop() {
                    PLVCommonLog.d(TAG, "onLiveStop");
                    livePlayerData.postLiveStop();
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onLiveStop();
                    }
                    setLogoVisibility(View.GONE);
                }
            });
            videoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    PLVCommonLog.d(TAG, "onPrepared");
                    livePlayerData.postPrepared();
                    liveRoomDataManager.setSessionId(videoView.getModleVO() != null ? videoView.getModleVO().getChannelSessionId() : null);
                    if (videoView.getMediaPlayMode() == PolyvMediaPlayMode.MODE_AUDIO) {
                        videoView.removeRenderView();//need clear&unregister
                        setLogoVisibility(View.GONE);
                    } else if (videoView.getMediaPlayMode() == PolyvMediaPlayMode.MODE_VIDEO) {
                        setLogoVisibility(View.VISIBLE);
                    }
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onPrepared(videoView.getMediaPlayMode());
                    }
                }

                @Override
                public void onPreparing() {
                    PLVCommonLog.d(TAG, "onPreparing");
                }
            });
            videoView.setOnLinesChangedListener(new IPolyvLiveListenerEvent.OnLinesChangedListener() {
                @Override
                public void onLinesChanged(final int linesPos) {
                    livePlayerData.postLinesChange(linesPos);
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onLinesChanged(linesPos);
                    }
                }
            });
            videoView.setOnGetMarqueeVoListener(new IPolyvVideoViewListenerEvent.OnGetMarqueeVoListener() {
                @Override
                public void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo) {
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onGetMarqueeVo(marqueeVo, getConfig().getUser().getViewerName());
                    }
                }
            });
            videoView.setOnGestureClickListener(new IPolyvVideoViewListenerEvent.OnGestureClickListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    //如果当前没有直播，才会将单击事件传递
                    if (!videoView.isOnline() && onSubGestureClickListener != null) {
                        onSubGestureClickListener.callback(start, end);
                    }
                }
            });
            videoView.setOnGestureLeftDownListener(new IPolyvVideoViewListenerEvent.OnGestureLeftDownListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    int brightness = videoView.getBrightness((Activity) videoView.getContext()) - 8;
                    brightness = Math.max(0, brightness);
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
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
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
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
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
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
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        boolean result = view.onVolumeChanged(volume, end);
                        if (start && result) {
                            videoView.setVolume(volume);
                        }
                    }
                }
            });
            videoView.setOnDanmuServerOpenListener(new IPolyvLiveListenerEvent.OnDanmuServerOpenListener() {
                @Override
                public void onDanmuServerOpenListener(boolean isServerDanmuOpen) {
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onServerDanmuOpen(isServerDanmuOpen);
                    }
                }
            });
            videoView.setMicroPhoneListener(new IPolyvLiveListenerEvent.MicroPhoneListener() {
                @Override
                public void showMicPhoneLine(int visible) {
                    PLVCommonLog.d(TAG, "showMicPhoneLine visible=" + visible);
                    livePlayerData.postLinkMicOpen(visible == View.VISIBLE, "audio".equals(videoView.getLinkMicType()));
                }
            });
            videoView.setOnPPTShowListener(new IPolyvVideoViewListenerEvent.OnPPTShowListener() {
                @Override
                public void showPPTView(int visible) {
                    livePlayerData.postPPTShowState(visible == View.VISIBLE);
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        view.onShowPPTView(visible);
                    }
                }
            });
            videoView.setOnSupportRTCListener(new IPolyvLiveListenerEvent.OnSupportRTCListener() {
                @Override
                public void onSupportRTC(boolean isSupportRTC) {
                    liveRoomDataManager.setSupportRTC(isSupportRTC);
                }
            });
            videoView.setOnSEIRefreshListener(new IPolyvVideoViewListenerEvent.OnSEIRefreshListener() {
                @Override
                public void onSEIRefresh(int seiType, byte[] seiData) {
                    long ts = PLVFormatUtils.parseLong(new String(seiData));
                    PLVCommonLog.v(TAG, "sei ts = " + ts);
                    livePlayerData.postSeiData(ts);
                }
            });
            videoView.setOnNetworkStateListener(new IPolyvVideoViewListenerEvent.OnNetworkStateListener() {
                @Override
                public boolean onNetworkRecover() {
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        return view.onNetworkRecover();
                    }
                    return false;
                }

                @Override
                public boolean onNetworkError() {
                    return false;
                }
            });
            videoView.setOnGetLogoListener(new IPolyvVideoViewListenerEvent.OnGetLogoListener() {
                @Override
                public void onLogo(String logoImage, int logoAlpha, int logoPosition, String logoHref) {
                    if (TextUtils.isEmpty(logoImage)) {
                        return;
                    }
                    PLVPlayerLogoView.LogoParam logoParam = new PLVPlayerLogoView.LogoParam().setWidth(0.14F).setHeight(0.25F)
                            .setAlpha(logoAlpha).setOffsetX(0.03F).setOffsetY(0.06F).setPos(logoPosition).setResUrl(logoImage);
                    IPLVLivePlayerContract.ILivePlayerView view = getView();
                    if (view != null) {
                        logoView = view.getLogo();
                        if (logoView != null) {
                            logoView.removeAllLogo();
                            logoView.addLogo(logoParam);
                            logoView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void setDefaultViewStatus() {
        videoView.removeRenderView();
        IPLVLivePlayerContract.ILivePlayerView view = getView();
        if (view != null && view.getBufferingIndicator() != null) {
            view.getBufferingIndicator().setVisibility(View.GONE);
        }
        if (view != null && view.getNoStreamIndicator() != null) {
            view.getNoStreamIndicator().setVisibility(View.VISIBLE);
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
        updatePlayInfo();
        selfHandler.sendEmptyMessageDelayed(WHAT_PLAY_PROGRESS, 1000);
    }

    private void stopPlayProgressTimer() {
        selfHandler.removeMessages(WHAT_PLAY_PROGRESS);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 获取播放信息">
    private void updatePlayInfo() {
        if (videoView != null) {
            PLVPlayInfoVO.Builder builder = new PLVPlayInfoVO.Builder();
            builder.isPlaying(videoView.isPlaying());
            if (subVideoView != null) {
                builder.isSubVideoViewPlaying(subVideoView.isPlaying());
            }
            livePlayerData.postPlayInfoVO(builder.build());
            IPLVLivePlayerContract.ILivePlayerView view = getView();
            if (view != null) {
                view.updatePlayInfo(builder.build());
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - logo显示控制">
    private void setLogoVisibility(int visible) {
        if (logoView != null) {
            logoView.setVisibility(visible);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private IPLVLivePlayerContract.ILivePlayerView getView() {
        return vWeakReference != null ? vWeakReference.get() : null;
    }

    private PLVLiveChannelConfig getConfig() {
        return liveRoomDataManager.getConfig();
    }
    // </editor-fold>
}
