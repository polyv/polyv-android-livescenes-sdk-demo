package com.easefun.polyv.livecommon.module.modules.cast;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.cast.manager.IPLVCastUpdateListener;
import com.easefun.polyv.livecommon.module.modules.cast.manager.PLVCastBusinessManager;
import com.easefun.polyv.livecommon.module.modules.cast.widget.PLVCastBitratePopupWindow;
import com.easefun.polyv.livecommon.module.modules.cast.widget.PLVCastDeviceSearchWindow;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.plv.foundationsdk.component.exts.Nullables;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;

import java.util.List;

/**
 * 投屏状态管理Layout，负责监听管理投屏状态变化
 * 1、管理投屏码率切换，码率切换后，仅修改投屏播放器的码率；直到退出投屏后，同步当前码率到本地播放器
 * 2、管理投屏设备连接，保证连接设备一对一；且退出直播间不会自动关闭投屏，再次进入直播间恢复投屏状态
 * 3、拦截设备音量控制去操作投屏音量,搜索设备时静音，退出搜索后恢复
 * 4、仅支持云课堂场景纯视频投屏，无延迟场景下投屏为CDN有延迟
 * 5、直播投屏使用单独的viewlog，不与常规viewlog使用。开始投屏后关闭直播，退出投屏恢复直播
 * 6、投屏后将不允许连麦，连麦后投屏按钮功能自动隐藏
 */
public class PLVScreencastLayout extends FrameLayout implements IPLVScreencastLayout, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    //连接状态，设备名称
    private TextView tvStatus;
    private TextView tvDeviceName;
    //切换设备按钮
    private TextView tvSwitchDevice;
    private ImageView ivExit;
    private ImageView ivBack;
    private ImageView ivCastStop;
    private ImageView ivVideoFullscreen;
    private LinearLayout llDevice;
    //码率
    private TextView tvCurrentBitrate;

    //投屏搜索弹窗
    private PLVCastDeviceSearchWindow castDeviceSearchWindow;
    //投屏码率选择弹窗
    private PLVCastBitratePopupWindow bitratePopupWindow;

    private String channelId;
    private boolean isRtcWatch = false;
    private boolean isReconnect = false;
    private boolean stopLocalVideoByScreencast = false;
    private boolean stopByBitrateChanged = false;

    private IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter;
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // <editor-fold defaultstate="collapsed" desc="生命周期和初始化">
    public PLVScreencastLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVScreencastLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVScreencastLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_cast_status_layout, this);
        setFocusable(true);
        tvStatus = findViewById(R.id.tv_status);
        tvDeviceName = findViewById(R.id.tv_device_name);
        ivBack = findViewById(R.id.iv_cast_back);
        tvCurrentBitrate = findViewById(R.id.tv_current_bitrate);
        ivCastStop = findViewById(R.id.plv_iv_cast_stop);
        ivVideoFullscreen = findViewById(R.id.plv_iv_video_fullscreen);
        ivExit = findViewById(R.id.tv_exit);
        tvSwitchDevice = findViewById(R.id.tv_switch_device);
        llDevice = findViewById(R.id.ll_device);

        ivCastStop.setOnClickListener(this);
        tvCurrentBitrate.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivVideoFullscreen.setOnClickListener(this);
        ivExit.setOnClickListener(this);
        tvSwitchDevice.setOnClickListener(this);

        //初始化投屏搜索弹窗
        castDeviceSearchWindow = new PLVCastDeviceSearchWindow(getContext());
        castDeviceSearchWindow.setOnWindowDismissListener(new PLVCastDeviceSearchWindow.OnWindowDismissListener() {
            @Override
            public void onDismiss() {
                if (livePlayerPresenter != null) {
                    livePlayerPresenter.setPlayerVolume(100);
                }
            }
        });
        castDeviceSearchWindow.setOnCastDeviceItemClickListener(new PLVCastDeviceSearchWindow.OnCastDeviceItemClickListener() {
            @Override
            public void onClick(PLVMediaCastDevice pInfo) {
                PLVCommonLog.d(TAG, "CastDeviceItemClick: " + pInfo.getFriendlyName());
                // 断开上一次连接的设备
                PLVCastBusinessManager.getInstance().disConnect(PLVCastBusinessManager.getInstance().getSelectInfo());
                PLVCastBusinessManager.getInstance().clearInfos();

                PLVCastBusinessManager.getInstance().setExitScreencast(false);
                PLVCastBusinessManager.getInstance().stopBrowse();
                PLVCastBusinessManager.getInstance().connect(pInfo);
                if (livePlayerPresenter != null) {
                    PLVCastBusinessManager.getInstance().setCastBitratePos(livePlayerPresenter.getBitratePos());
                }
                castDeviceSearchWindow.dismiss();
            }
        });

        //初始化码率选择弹窗
        bitratePopupWindow = new PLVCastBitratePopupWindow(getContext());
        bitratePopupWindow.setBitrateChangeListener(new PLVCastBitratePopupWindow.IPlvCastBitrateChangeListener() {
            @Override
            public void onBitrateChange(int index, String bitrate) {
                PLVCommonLog.d(TAG, "change Bitrate " + index + "  " + bitrate);
                PLVMediaCastDevice selectInfo = PLVCastBusinessManager.getInstance().getSelectInfo();

                if (selectInfo != null && PLVCastBusinessManager.getInstance().getCastBitratePos() != index) {
                    stopByBitrateChanged = true;
                    String url = livePlayerPresenter.getBitrateVO().get(index).getUrl();
                    startCast(url, selectInfo, false);
                    PLVCastBusinessManager.getInstance().setCastBitratePos(index);
                }
                tvCurrentBitrate.setText(bitrate);
            }
        });


        PLVCastBusinessManager.getInstance().addPlvScreencastStateListener(castStateListener);

        post(new Runnable() {
            @Override
            public void run() {
                resetDeviceView(ConvertUtils.px2dp(ScreenUtils.getScreenWidth()));
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (castDeviceSearchWindow != null) {
            castDeviceSearchWindow.clearCastListener();
        }
        PLVCastBusinessManager.getInstance().removePlvScreencastStateListener(castStateListener);
        PLVCastBusinessManager.getInstance().leaveChannel();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetDeviceView(newConfig.screenWidthDp);
        ivVideoFullscreen.setSelected(newConfig.orientation != Configuration.ORIENTATION_PORTRAIT);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="对外接口">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, @NonNull IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter) {
        this.livePlayerPresenter = livePlayerPresenter;
        this.liveRoomDataManager = liveRoomDataManager;

        PLVCommonLog.d(TAG, "init screencast");

        PLVLiveChannelConfig channelConfig = PLVLiveChannelConfigFiller.generateNewChannelConfig();
        PLVCastBusinessManager.getInstance().enterChannel(channelConfig.getChannelId(),
                channelConfig.getAccount().getUserId(),
                liveRoomDataManager.getSessionId(),
                channelConfig.getUser().getViewerId(),
                channelConfig.getUser().getViewerName());
        this.channelId = channelConfig.getChannelId();
        observeLiveData();

        initShow();
    }

    @Override
    public void show(@Nullable PLVMediaCastDevice info) {
        //显示投屏状态UI
        setVisibility(VISIBLE);
        if (info != null) {
            tvDeviceName.setText(info.getFriendlyName());
        }
    }

    @Override
    public void hide() {
        setVisibility(GONE);
    }

    @Override
    public void browse() {
        if (castDeviceSearchWindow != null) {
            castDeviceSearchWindow.show();
            livePlayerPresenter.setPlayerVolume(0);
        }
    }

    @Override
    public void exitCast() {
        PLVCommonLog.d(TAG, "exitCast");

        syncBitrate();
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getCastStartData().postValue(false);
        }
        PLVCastBusinessManager.getInstance().setExitScreencast(true);
//        PlvCastBusinessManager.getInstance().stopBrowse();
        PLVCastBusinessManager.getInstance().stopPlay();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PLVCastBusinessManager.getInstance().disConnect(PLVCastBusinessManager.getInstance().getSelectInfo());
        PLVCastBusinessManager.getInstance().clearInfos();

        if (castDeviceSearchWindow != null) {
            castDeviceSearchWindow.clearCastSelectedInfo();
        }

        hide();
        recoverLocalVideo();
    }

    @Override
    public void setRtcWatching(boolean isRtcWatching) {
        this.isRtcWatch = isRtcWatching;
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (castDeviceSearchWindow.isShowing()) {
                castDeviceSearchWindow.dismiss();
                return true;
            }
            if (isShown() && ScreenUtils.isLandscape()) {
                ScreenUtils.setPortrait((Activity) getContext());
                return true;
            }
        } else if (isShown() && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            PLVCommonLog.d(TAG, "subVolume");
            PLVCastBusinessManager.getInstance().subVolume();
            return true;
        } else if (isShown() && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            PLVCommonLog.d(TAG, "addVolume");
            PLVCastBusinessManager.getInstance().addVolume();
            return true;
        }
        return false;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_exit) {
            exitCast();
        } else if (id == R.id.tv_switch_device) {
            browse();
        } else if (id == R.id.tv_current_bitrate) {
            if (bitratePopupWindow != null) {
                bitratePopupWindow.showBottomUpWithMask();
            }
        } else if (id == R.id.plv_iv_video_fullscreen) {
            if (ScreenUtils.isLandscape()) {
                ScreenUtils.setPortrait((Activity) getContext());
            } else {
                ScreenUtils.setLandscape((Activity) getContext());
            }
        } else if (id == R.id.plv_iv_cast_stop) {
            ivCastStop.setSelected(!ivCastStop.isSelected());
            if (ivCastStop.isSelected()) {
                PLVCastBusinessManager.getInstance().pause();
            } else {
                PLVCastBusinessManager.getInstance().resume();
            }
        } else if (id == R.id.iv_cast_back) {
            ((Activity) getContext()).onBackPressed();
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="私有方法">
    private void updateStatus(String updateMsg) {
        tvDeviceName.setText(updateMsg);
    }

    private void initShow() {
        //如果当前频道和正在投屏的频道相同，则恢复投屏状态
        if (PLVCastBusinessManager.getInstance().isSameChannelId() && PLVCastBusinessManager.getInstance().isCasting()) {
            show(PLVCastBusinessManager.getInstance().getSelectInfo());
            stopLocalVideo();
            if (livePlayerPresenter != null && liveRoomDataManager != null) {
                liveRoomDataManager.getCastStartData().postValue(true);
                if (isRtcWatch) {
                    livePlayerPresenter.stopRTCPlay();
                } else {
                    if (livePlayerPresenter.isPlaying()) {
                        livePlayerPresenter.stop();
                    }
                }
            }
        }
    }

    private void reconnect(PLVMediaCastDevice pInfo) {
        isReconnect = true;
        PLVCastBusinessManager.getInstance().connect(pInfo);

    }

    private void resetDeviceView(int widthDp) {
        ViewGroup.LayoutParams params = llDevice.getLayoutParams();
        params.width = (int) (ConvertUtils.dp2px(widthDp) * 0.54);
        params.height = (int) (109f / 387f * params.width);
        llDevice.setLayoutParams(params);
        llDevice.requestLayout();
    }

    private void syncBitrate() {
        //推出投屏后，让直播播放器同步投屏的码率
        if (livePlayerPresenter != null) {
            if (livePlayerPresenter.getBitratePos() == bitratePopupWindow.getCurBitrateIndex()) {
                if (isRtcWatch) {
                    livePlayerPresenter.startRTCPlay();
                } else {
                    livePlayerPresenter.restartPlay();
                }
            } else {
                livePlayerPresenter.changeBitRate(bitratePopupWindow.getCurBitrateIndex());
            }
        }
    }

    private void startCast(PLVMediaCastDevice info, boolean isSendLog) {
        if (livePlayerPresenter != null && livePlayerPresenter.getBitrateVO() != null) {
            startCast(livePlayerPresenter.getBitrateVO().get(livePlayerPresenter.getBitratePos()).getUrl(), info, isSendLog);
        }
    }

    private void startCast(String url, PLVMediaCastDevice info, boolean isSendLog) {
        PLVCastBusinessManager.getInstance().play(info, url, 0, isSendLog);
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 直播状态">
    private void observeLiveData() {
        livePlayerPresenter.getData().getPlayerState().observe((LifecycleOwner) getContext(), new Observer<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState plvPlayerState) {
                if (plvPlayerState == PLVPlayerState.PREPARED) {
                    List<PolyvDefinitionVO> bitrateVO = livePlayerPresenter.getBitrateVO();
                    if (bitrateVO == null) {
                        return;
                    }
                    String sessionId = liveRoomDataManager.getSessionId();
                    bitratePopupWindow.initBitRateView(bitrateVO, livePlayerPresenter.getBitratePos());
                    tvCurrentBitrate.setText(bitrateVO.get(livePlayerPresenter.getBitratePos()).getDefinition());
                    initShow();
                    if (isShown()) {
                        //恢复码率
                        int castIndex = PLVCastBusinessManager.getInstance().getCastBitratePos();
                        if (castIndex != -1 && livePlayerPresenter.getBitratePos() != castIndex) {
                            bitratePopupWindow.changeBitrate(castIndex);
                            tvCurrentBitrate.setText(bitrateVO.get(castIndex).getDefinition());
                        }
                    }
                } else if (plvPlayerState == PLVPlayerState.NO_LIVE ||
                        plvPlayerState == PLVPlayerState.LIVE_END) {
                    if (isShown()) {
                        //投屏正在显示，说明当前频道正在投屏
                        exitCast();
                    }
                }
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="投屏状态监听">
    private IPLVCastUpdateListener castStateListener = new IPLVCastUpdateListener() {
        @Override
        public void onStateUpdate(int state, Object object) {
            switch (state) {
                case PLVCastBusinessManager.STATE_CONNECTING:
                    show(null);
                    stopLocalVideo();
                    if (livePlayerPresenter != null && liveRoomDataManager != null) {
                        if (isRtcWatch) {
                            livePlayerPresenter.stopRTCPlay();
                        } else {
                            livePlayerPresenter.stop();
                        }
                    }
                    if (object instanceof String) {
                        updateStatus((String) object);
                    }
                    break;
                case PLVCastBusinessManager.STATE_CONNECT_SUCCESS:
                    PLVMediaCastDevice info = (PLVMediaCastDevice) object;
                    String url;
                    if (livePlayerPresenter.getBitrateVO() != null) {
                        url = livePlayerPresenter.getBitrateVO().get(livePlayerPresenter.getBitratePos()).getUrl();
                    } else {
                        url = Nullables.of(new PLVSugarUtil.Supplier<String>() {
                            @Override
                            public String get() {
                                return livePlayerPresenter.getChannelVO().getLines().get(0).getFlv();
                            }
                        }).getOrNull();
                    }
                    if (url == null) {
                        return;
                    }
                    if (liveRoomDataManager != null) {
                        liveRoomDataManager.getCastStartData().postValue(true);
                    }
                    if (isReconnect) {
                        isReconnect = false;
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    show(info);
                    stopLocalVideo();
                    startCast(url, info, true);
                    break;
                case PLVCastBusinessManager.STATE_PLAY:
                    show(null);
                    stopLocalVideo();
                    ivCastStop.setSelected(false);
                    break;
                case PLVCastBusinessManager.STATE_PAUSE:
                    ivCastStop.setSelected(true);
                    break;
                case PLVCastBusinessManager.STATE_STOP:
                case PLVCastBusinessManager.STATE_COMPLETION:
                    if (!stopByBitrateChanged) {
                        hide();
                        recoverLocalVideo();
                    }
                    stopByBitrateChanged = false;
                    break;
                case PLVCastBusinessManager.STATE_CONNECT_FAILURE:
                case PLVCastBusinessManager.STATE_PLAY_ERROR:
                    if (object instanceof String) {
                        PLVToast.Builder.context(getContext())
                                .setText((String) object)
                                .show();
                    }
                    break;

            }
        }
    };

    private void stopLocalVideo() {
        if (livePlayerPresenter != null) {
            if (isRtcWatch) {
                livePlayerPresenter.stopRTCPlay();
            } else {
                livePlayerPresenter.stop();
            }
            stopLocalVideoByScreencast = true;
        }
    }

    private void recoverLocalVideo() {
        if (!stopLocalVideoByScreencast) {
            return;
        }
        stopLocalVideoByScreencast = false;
        if (livePlayerPresenter != null) {
            if (isRtcWatch) {
                livePlayerPresenter.startRTCPlay();
            } else {
                livePlayerPresenter.restartPlay();
            }
        }
    }
    // </editor-fold >

}
