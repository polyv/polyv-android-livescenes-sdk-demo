package com.easefun.polyv.liveecommerce.modules.player.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateLayout;
import com.easefun.polyv.liveecommerce.R;
import com.plv.business.api.common.player.PLVPlayerConstant;

import java.util.concurrent.TimeUnit;

/**
 * @author suhongtao
 */

public class PLVECNetworkTipsView extends FrameLayout implements View.OnClickListener {

    private PLVRoundRectGradientTextView livePlayerNetworkNotGoodTipsTv;
    private PLVTriangleIndicateLayout livePlayerNetworkChangeLatencyTipsLayout;
    private TextView livePlayerNetworkChangeLatencyTv;
    private ImageView livePlayerNetworkCloseBadTipsIv;

    private OnViewActionListener onViewActionListener;

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
                livePlayerNetworkChangeLatencyTipsLayout.setVisibility(GONE);
            } else if (msg.what == HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS) {
                livePlayerNetworkNotGoodTipsTv.setVisibility(GONE);
            }
        }

    };

    public PLVECNetworkTipsView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECNetworkTipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECNetworkTipsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_live_player_network_tips_layout, this);

        livePlayerNetworkNotGoodTipsTv = findViewById(R.id.plvec_live_player_network_not_good_tips_tv);
        livePlayerNetworkChangeLatencyTipsLayout = findViewById(R.id.plvec_live_player_network_change_latency_tips_layout);
        livePlayerNetworkChangeLatencyTv = findViewById(R.id.plvec_live_player_network_change_latency_tv);
        livePlayerNetworkCloseBadTipsIv = findViewById(R.id.plvec_live_player_network_close_bad_tips_iv);

        livePlayerNetworkChangeLatencyTv.setOnClickListener(this);
        livePlayerNetworkCloseBadTipsIv.setOnClickListener(this);

        initChangeLatencyText();

        hide();
    }

    private void initChangeLatencyText() {
        final String hintText = "您的网络状态糟糕，可尝试在 更多>模式 ";
        final String clickableText = "切换到正常延迟";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(hintText);
        builder.append(clickableText);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")), 0, hintText.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickChangeNormalLatency();
                }
                reset();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#6DA7FF"));
                ds.setUnderlineText(false);
            }
        }, hintText.length(), hintText.length() + clickableText.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        livePlayerNetworkChangeLatencyTv.setText(builder);
        livePlayerNetworkChangeLatencyTv.setMovementMethod(LinkMovementMethod.getInstance());
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

    public void reset() {
        lastQuality = -1;
        qualityCount = 0;
        hasShowNotGoodTips = false;
        hasShowChangeLatencyTips = false;
        hide();
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
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
        if (hasShowNotGoodTips) {
            return;
        }
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS);
        livePlayerNetworkNotGoodTipsTv.setVisibility(View.VISIBLE);
        mainHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS, TimeUnit.SECONDS.toMillis(3));
        hasShowNotGoodTips = true;
    }

    private void tryShowChangeLatencyTips() {
        if (hasShowChangeLatencyTips || !isLowLatency()) {
            return;
        }
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS);
        livePlayerNetworkChangeLatencyTipsLayout.setVisibility(View.VISIBLE);
        mainHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS, TimeUnit.SECONDS.toMillis(3));
        hasShowChangeLatencyTips = true;
    }

    private void hide() {
        livePlayerNetworkNotGoodTipsTv.setVisibility(View.GONE);
        livePlayerNetworkChangeLatencyTipsLayout.setVisibility(View.GONE);
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_CHANGE_LATENCY_TIPS);
        mainHandler.removeMessages(HANDLER_MESSAGE_HIDE_NET_NOT_GOOD_TIPS);
    }

    private boolean isLowLatency() {
        if (onViewActionListener != null) {
            return onViewActionListener.isCurrentLowLatency();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if (viewId == livePlayerNetworkCloseBadTipsIv.getId()) {
            hide();
        }
    }

    public interface OnViewActionListener {
        void onClickChangeNormalLatency();

        boolean isCurrentLowLatency();
    }

}
