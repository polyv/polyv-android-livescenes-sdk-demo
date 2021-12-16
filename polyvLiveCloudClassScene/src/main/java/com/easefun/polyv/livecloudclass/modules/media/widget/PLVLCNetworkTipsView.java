package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.plv.business.api.common.player.PLVPlayerConstant;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

import java.util.concurrent.TimeUnit;

/**
 * @author suhongtao
 */

public class PLVLCNetworkTipsView extends FrameLayout implements View.OnClickListener {

    private PLVRoundRectGradientTextView plvlcLivePlayerNetworkNotGoodTipsTv;
    private TextView plvlcLivePlayerNetworkChangeLatencyTv;
    private ImageView plvlcLivePlayerNetworkCloseBadTipsIv;
    private PLVRoundRectLayout plvlcLivePlayerNetworkChangeLatencyTipsLayout;

    private OnClickChangeNormalLatencyListener onClickChangeNormalLatencyListener;

    private boolean isLinkMic = false;
    private boolean isLowLatency = PLVLinkMicConfig.getInstance().isLowLatencyWatchEnabled();

    private int lastQuality = -1;
    private int qualityCount = 0;
    private boolean hasShowNotGoodTips = false;
    private boolean hasShowChangeLatencyTips = false;

    private static final int HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS = 1;
    private static final int HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS = 2;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS) {
                plvlcLivePlayerNetworkChangeLatencyTipsLayout.setVisibility(GONE);
            } else if (msg.what == HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS) {
                plvlcLivePlayerNetworkNotGoodTipsTv.setVisibility(GONE);
            }
        }

    };

    public PLVLCNetworkTipsView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCNetworkTipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCNetworkTipsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_player_network_tips_layout, this);

        plvlcLivePlayerNetworkNotGoodTipsTv = findViewById(R.id.plvlc_live_player_network_not_good_tips_tv);
        plvlcLivePlayerNetworkChangeLatencyTv = findViewById(R.id.plvlc_live_player_network_change_latency_tv);
        plvlcLivePlayerNetworkCloseBadTipsIv = findViewById(R.id.plvlc_live_player_network_close_bad_tips_iv);
        plvlcLivePlayerNetworkChangeLatencyTipsLayout = findViewById(R.id.plvlc_live_player_network_change_latency_tips_layout);

        plvlcLivePlayerNetworkChangeLatencyTv.setOnClickListener(this);
        plvlcLivePlayerNetworkCloseBadTipsIv.setOnClickListener(this);

        hide();
    }

    public void acceptNetworkQuality(int quality) {
        if (lastQuality != quality) {
            lastQuality = quality;
            qualityCount = 1;
            return;
        }
        qualityCount++;
        if (qualityCount >= 3) {
            tryShow();
        }
    }

    public void setIsLinkMic(boolean isLinkMic) {
        this.isLinkMic = isLinkMic;
    }

    public void setIsLowLatency(boolean isLowLatency) {
        if (this.isLowLatency != isLowLatency) {
            reset();
        }
        this.isLowLatency = isLowLatency;
    }

    public void reset() {
        lastQuality = -1;
        qualityCount = 0;
        hasShowNotGoodTips = false;
        hasShowChangeLatencyTips = false;
        hide();
    }

    public void setOnClickChangeNormalLatencyListener(OnClickChangeNormalLatencyListener onClickChangeNormalLatencyListener) {
        this.onClickChangeNormalLatencyListener = onClickChangeNormalLatencyListener;
    }

    private void tryShow() {
        if (PLVPlayerConstant.NetQuality.isNoConnection(lastQuality)) {
            tryShowNetNotGoodTips();
        } else if (PLVPlayerConstant.NetQuality.isNetPoor(lastQuality)) {
            tryShowChangeLatencyTips();
        } else if (PLVPlayerConstant.NetQuality.isNetMiddleOrWorse(lastQuality)) {
            tryShowNetNotGoodTips();
        }
    }

    private void tryShowNetNotGoodTips() {
        if (hasShowNotGoodTips || isLinkMic) {
            return;
        }
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS);
        plvlcLivePlayerNetworkNotGoodTipsTv.setVisibility(View.VISIBLE);
        mainHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS, TimeUnit.SECONDS.toMillis(3));
        hasShowNotGoodTips = true;
    }

    private void tryShowChangeLatencyTips() {
        if (hasShowChangeLatencyTips || isLinkMic || !isLowLatency) {
            return;
        }
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS);
        plvlcLivePlayerNetworkChangeLatencyTipsLayout.setVisibility(View.VISIBLE);
        mainHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS, TimeUnit.SECONDS.toMillis(3));
        hasShowChangeLatencyTips = true;
    }

    private void hide() {
        plvlcLivePlayerNetworkNotGoodTipsTv.setVisibility(View.GONE);
        plvlcLivePlayerNetworkChangeLatencyTipsLayout.setVisibility(View.GONE);
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS);
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == plvlcLivePlayerNetworkCloseBadTipsIv.getId()) {
            hide();
        } else if (v.getId() == plvlcLivePlayerNetworkChangeLatencyTv.getId()) {
            if (onClickChangeNormalLatencyListener != null) {
                onClickChangeNormalLatencyListener.onClickChangeNormalLatency();
            }
        }
    }

    public interface OnClickChangeNormalLatencyListener {
        void onClickChangeNormalLatency();
    }

}
