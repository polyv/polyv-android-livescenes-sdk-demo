package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.PLVClipOutMaskView;
import com.easefun.polyv.livestreamer.R;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSNetworkDisconnectMaskLayout extends FrameLayout {

    private PLVClipOutMaskView clipOutMaskView;

    private boolean isLive = false;
    private boolean isNetworkDisconnect = false;

    public PLVLSNetworkDisconnectMaskLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSNetworkDisconnectMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSNetworkDisconnectMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_network_disconnect_mask_layout, this);
        clipOutMaskView = findViewById(R.id.plvls_clip_out_mask_view);
    }

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            presenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isLive) {
                    PLVLSNetworkDisconnectMaskLayout.this.isLive = isLive != null && isLive;
                    update();
                }
            });
        }

        @Override
        public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
            isNetworkDisconnect = quality == PLVLinkMicConstant.NetworkQuality.DISCONNECT || !PLVNetworkUtils.isConnected(getContext());
            update();
        }

    };

    private void update() {
        final boolean currentVisible = getVisibility() == VISIBLE;
        final boolean needVisible = isLive && isNetworkDisconnect;
        if (currentVisible == needVisible) {
            return;
        }
        if (!needVisible) {
            setVisibility(GONE);
            return;
        }
        setupClipOutMaskView();
        setVisibility(VISIBLE);
    }

    private void setupClipOutMaskView() {
        clipOutMaskView.clearClipOutParams();
        final Activity activity = (Activity) getContext();
        final View closeIcon = activity.findViewById(R.id.plvls_status_bar_class_control_tv);
        if (closeIcon != null && closeIcon.isShown()) {
            clipOutMaskView.clipOutRoundRect(closeIcon, ConvertUtils.dp2px(30));
        }
        final View networkStatusBar = activity.findViewById(R.id.plvls_status_bar_net_quality_view);
        if (networkStatusBar != null && networkStatusBar.isShown()) {
            clipOutMaskView.clipOutRoundRect(networkStatusBar, ConvertUtils.dp2px(10));
        }
    }

}
