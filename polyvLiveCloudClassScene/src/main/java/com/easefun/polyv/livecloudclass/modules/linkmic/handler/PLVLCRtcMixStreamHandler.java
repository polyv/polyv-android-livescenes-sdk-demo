package com.easefun.polyv.livecloudclass.modules.linkmic.handler;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

/**
 * @author Hoshiiro
 */
public class PLVLCRtcMixStreamHandler {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter;
    private final OnHandlerCallbackListener onHandlerCallbackListener;

    private View mixStreamRenderView;

    private boolean requireToPlayManually = true;
    private boolean rtcWatch = false;
    private boolean pureRtcWatch = false;

    private boolean isPlaying = false;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLCRtcMixStreamHandler(
            @NonNull IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter,
            @NonNull OnHandlerCallbackListener onHandlerCallbackListener
    ) {
        this.linkMicPresenter = linkMicPresenter;
        this.onHandlerCallbackListener = onHandlerCallbackListener;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void start() {
        requireToPlayManually = true;
        updatePlayPause();
    }

    public void stop() {
        requireToPlayManually = false;
        updatePlayPause();
    }

    public void onStartRtcWatch() {
        rtcWatch = true;
        updatePlayPause();
    }

    public void onStopRtcWatch() {
        rtcWatch = false;
        updatePlayPause();
    }

    public void onStartPureRtcWatch() {
        pureRtcWatch = true;
        updatePlayPause();
    }

    public void onStopPureRtcWatch() {
        pureRtcWatch = false;
        updatePlayPause();
    }

    public boolean isPlayRtcAsMixStream() {
        return !pureRtcWatch && rtcWatch && PLVLinkMicConfig.getInstance().isLowLatencyMixRtcWatch();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理">

    private void updatePlayPause() {
        final boolean needPlayMixStream = isPlayRtcAsMixStream() && requireToPlayManually;
        if (needPlayMixStream && !isPlaying) {
            startPlay();
        } else if (!needPlayMixStream && isPlaying) {
            stopPlay();
        }
    }

    private void startPlay() {
        ViewGroup parent = onHandlerCallbackListener.onRequireMixStreamContainer();
        if (parent == null || isPlaying) {
            return;
        }
        isPlaying = true;
        mixStreamRenderView = linkMicPresenter.createRenderView(parent.getContext());
        parent.addView(mixStreamRenderView, MATCH_PARENT, MATCH_PARENT);
        linkMicPresenter.setupMixStreamView(mixStreamRenderView);
    }

    private void stopPlay() {
        ViewGroup parent = onHandlerCallbackListener.onRequireMixStreamContainer();
        if (parent == null || !isPlaying) {
            return;
        }
        linkMicPresenter.releaseMixStreamView(mixStreamRenderView);
        parent.removeAllViews();
        mixStreamRenderView = null;
        isPlaying = false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义">

    public interface OnHandlerCallbackListener {
        @Nullable
        ViewGroup onRequireMixStreamContainer();
    }

    // </editor-fold>

}
