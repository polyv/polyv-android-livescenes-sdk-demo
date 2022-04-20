package com.easefun.polyv.liveecommerce.modules.player.rtc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVLinkMicPresenter;
import com.easefun.polyv.livecommon.module.modules.linkmic.view.PLVAbsLinkMicView;
import com.easefun.polyv.liveecommerce.R;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

import java.util.List;

/**
 * 无延迟观看播放器容器布局
 *
 * @author suhongtao
 */
public class PLVECLiveRtcVideoLayout extends FrameLayout implements IPLVECLiveRtcVideoLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    @Nullable
    private IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter;

    private OnViewActionListener onViewActionListener;

    private String rtcLinkMicId;
    private SurfaceView rtcRenderView;
    private View.OnLayoutChangeListener onLayoutChangeListener;

    private ImageView pausingPlaceHolderIv;
    private boolean isPausing = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVECLiveRtcVideoLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVECLiveRtcVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVECLiveRtcVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        setupPausingPlaceholderImageView();
    }

    private void setupPausingPlaceholderImageView() {
        pausingPlaceHolderIv = new ImageView(getContext());
        pausingPlaceHolderIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        pausingPlaceHolderIv.setImageResource(R.drawable.plvec_player_rtc_pause_placeholder);
        addView(pausingPlaceHolderIv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pausingPlaceHolderIv.setVisibility(View.GONE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 接口实现">

    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (!PLVLinkMicConfig.getInstance().isPureRtcWatchEnabled()) {
            return;
        }
        initPresenter(liveRoomDataManager);
    }

    @Override
    public void setLiveStart() {
        if (linkMicPresenter == null) {
            return;
        }
        linkMicPresenter.setLiveStart();
        setKeepScreenOn(true);
    }

    @Override
    public void setLiveEnd() {
        if (linkMicPresenter == null) {
            return;
        }
        releaseRtcRenderView();
        linkMicPresenter.setLiveEnd();
        setKeepScreenOn(false);
    }

    @Override
    public void pause() {
        isPausing = true;
        if (linkMicPresenter == null) {
            return;
        }
        linkMicPresenter.muteAllVideo(true);
        linkMicPresenter.muteAllAudio(true);
        pausingPlaceHolderIv.bringToFront();
        pausingPlaceHolderIv.setVisibility(View.VISIBLE);
        if (onViewActionListener != null) {
            onViewActionListener.onUpdatePlayInfo();
        }
    }

    @Override
    public boolean isPausing() {
        return isPausing;
    }

    @Override
    public void resume() {
        isPausing = false;
        if (linkMicPresenter == null) {
            return;
        }
        linkMicPresenter.muteAllVideo(false);
        linkMicPresenter.muteAllAudio(false);
        pausingPlaceHolderIv.setVisibility(View.GONE);
        if (onViewActionListener != null) {
            onViewActionListener.onUpdatePlayInfo();
        }
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    @Override
    public void destroy() {
        releaseRtcRenderView();
        if (linkMicPresenter != null) {
            linkMicPresenter.destroy();
        }
        setKeepScreenOn(false);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MVP - View实现">

    private void initPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        linkMicPresenter = new PLVLinkMicPresenter(liveRoomDataManager, linkMicMvpView);
    }

    private final IPLVLinkMicContract.IPLVLinkMicView linkMicMvpView = new PLVAbsLinkMicView() {

        @Override
        public void onUsersJoin(List<String> uids) {
            if (uids.contains(rtcLinkMicId)) {
                resetRtcRenderView(rtcLinkMicId);
            }
        }

        @Override
        public void onSwitchFirstScreen(String linkMicId) {
            rtcLinkMicId = linkMicId;
            if (!isPausing) {
                resetRtcRenderView(rtcLinkMicId);
            }
        }

        @Override
        public void onRTCPrepared() {
            if (onViewActionListener != null) {
                onViewActionListener.onRtcPrepared();
            }
        }

        @Override
        public void updateFirstScreenChanged(String firstScreenLinkMicId, int oldPos, int newPos) {
            rtcLinkMicId = firstScreenLinkMicId;
            if (!isPausing) {
                updateRtcRenderView(rtcLinkMicId);
            }
        }

        @Override
        public void onNetQuality(int quality) {
            if (onViewActionListener != null) {
                onViewActionListener.acceptNetworkQuality(quality);
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法 - 无延迟播放器">

    /**
     * 重置rtc播放器，包括释放重建
     */
    private void resetRtcRenderView(final String linkMicId) {
        releaseRtcRenderView();
        setupRtcRenderView(linkMicId);
        addView(rtcRenderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 更新rtc播放器，仅重新配置
     */
    private void updateRtcRenderView(final String linkMicId) {
        if (rtcRenderView == null || linkMicPresenter == null) {
            return;
        }
        linkMicPresenter.setupRenderView(rtcRenderView, linkMicId);
        setOnLayoutChangeListener();
    }

    private void setupRtcRenderView(final String linkMicId) {
        if (linkMicPresenter == null) {
            return;
        }
        if (rtcRenderView != null) {
            releaseRtcRenderView();
        }
        rtcRenderView = linkMicPresenter.createRenderView(getContext());
        linkMicPresenter.setupRenderView(rtcRenderView, linkMicId);
        setOnLayoutChangeListener();
    }

    private void releaseRtcRenderView() {
        if (linkMicPresenter == null) {
            return;
        }
        if (rtcRenderView != null) {
            removeRenderViewFromParent();
            linkMicPresenter.releaseRenderView(rtcRenderView);
        }
        rtcRenderView = null;
    }

    private void removeRenderViewFromParent() {
        if (rtcRenderView != null && rtcRenderView.getParent() != null) {
            ((ViewGroup) rtcRenderView.getParent()).removeView(rtcRenderView);
        }
    }

    private void setOnLayoutChangeListener() {
        if (linkMicPresenter == null) {
            return;
        }
        if (onLayoutChangeListener == null) {
            onLayoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    final int width = right - left;
                    final int height = bottom - top;
                    final int oldWidth = oldRight - oldLeft;
                    final int oldHeight = oldBottom - oldTop;
                    if (width == oldWidth && height == oldHeight) {
                        return;
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onSizeChanged(right - left, bottom - top);
                    }
                }
            };
        }
        rtcRenderView.addOnLayoutChangeListener(onLayoutChangeListener);
    }

    // </editor-fold>

}
