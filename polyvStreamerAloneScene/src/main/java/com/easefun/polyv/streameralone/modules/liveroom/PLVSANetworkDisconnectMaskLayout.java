package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.PLVClipOutMaskView;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSANetworkDisconnectMaskLayout extends FrameLayout {

    private PLVClipOutMaskView clipOutMaskView;

    public PLVSANetworkDisconnectMaskLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVSANetworkDisconnectMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVSANetworkDisconnectMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_network_disconnect_mask_layout, this);
        clipOutMaskView = findViewById(R.id.plvsa_clip_out_mask_view);
    }

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
            final boolean isNetworkDisconnected = quality == PLVLinkMicConstant.NetworkQuality.DISCONNECT || !PLVNetworkUtils.isConnected(getContext());
            onNetworkChanged(isNetworkDisconnected);
        }
    };

    private void onNetworkChanged(boolean isNetworkDisconnected) {
        final boolean currentVisible = getVisibility() == VISIBLE;
        if (currentVisible == isNetworkDisconnected) {
            return;
        }
        if (!isNetworkDisconnected) {
            setVisibility(GONE);
            return;
        }
        setupClipOutMaskView();
        setVisibility(VISIBLE);
    }

    private void setupClipOutMaskView() {
        clipOutMaskView.clearClipOutParams();
        final Activity activity = (Activity) getContext();
        final View closeIcon = activity.findViewById(R.id.plvsa_status_bar_close_iv);
        if (closeIcon != null && closeIcon.isShown()) {
            clipOutMaskView.clipOutRoundRect(closeIcon, ConvertUtils.dp2px(32));
        }
        final View emptyFragmentCloseIcon = activity.findViewById(R.id.plvsa_empty_close_iv);
        if (emptyFragmentCloseIcon != null && emptyFragmentCloseIcon.isShown()) {
            clipOutMaskView.clipOutRoundRect(emptyFragmentCloseIcon, ConvertUtils.dp2px(32));
        }
        final View networkStatusBar = activity.findViewById(R.id.plvsa_status_bar_network_status_layout);
        if (networkStatusBar != null && networkStatusBar.isShown()) {
            clipOutMaskView.clipOutRoundRect(networkStatusBar, ConvertUtils.dp2px(10));
        }
    }

}
