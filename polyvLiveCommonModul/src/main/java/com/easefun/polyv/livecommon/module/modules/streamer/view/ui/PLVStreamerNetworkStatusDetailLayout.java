package com.easefun.polyv.livecommon.module.modules.streamer.view.ui;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.content.Context;
import android.os.Build;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.linkmic.model.PLVNetworkStatusVO;

import java.util.Locale;

/**
 * @author Hoshiiro
 */
public class PLVStreamerNetworkStatusDetailLayout extends FrameLayout {

    private TextView streamerNetworkDelayTv;
    private TextView streamerNetworkPacketLossTv;

    @Nullable
    private PLVNetworkStatusVO lastNetworkStatus = null;

    public PLVStreamerNetworkStatusDetailLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVStreamerNetworkStatusDetailLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVStreamerNetworkStatusDetailLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(layoutId(), this);

        streamerNetworkDelayTv = findViewById(R.id.plv_streamer_network_delay_tv);
        streamerNetworkPacketLossTv = findViewById(R.id.plv_streamer_network_packet_loss_tv);
    }

    @LayoutRes
    protected int layoutId() {
        return R.layout.plv_streamer_network_status_detail_layout;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateNetworkStatusText();
    }

    public void onNetworkStatus(@Nullable PLVNetworkStatusVO networkStatus) {
        lastNetworkStatus = networkStatus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isAttachedToWindow()) {
                updateNetworkStatusText();
            }
        } else {
            updateNetworkStatusText();
        }
    }

    private void updateNetworkStatusText() {
        if (lastNetworkStatus == null) {
            return;
        }
        final String delay = getOrDefault(lastNetworkStatus.getUpDelayMs(), 0).toString();
        final String upPacketLoss = String.format(Locale.getDefault(), "%.1f", getOrDefault(lastNetworkStatus.getUpPackageLost(), 0F));
        final String downPacketLoss = String.format(Locale.getDefault(), "%.1f", getOrDefault(lastNetworkStatus.getDownPackageLost(), 0F));

        streamerNetworkDelayTv.setText(PLVAppUtils.formatString(R.string.plv_streamer_network_delay_2, delay));
        streamerNetworkPacketLossTv.setText(PLVAppUtils.formatString(R.string.plv_streamer_network_packet_loss_2, upPacketLoss, downPacketLoss));
    }

}
