package com.easefun.polyv.livestreamer.modules.statusbar.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livestreamer.R;

/**
 * date: 2019/10/12 0012
 *
 * @author hwj
 * description 网络状态View
 */
public class PLVLSNetworkQualityWidget extends FrameLayout {


    private ImageView plvsIvNetworkStatusExclamationMark;
    private TextView plvsTvNetworkStatusCannotConnect;

    private Group plvsGroupNetworkStatusCannotConnect;
    private ConstraintLayout plvsClNetworkStatus;
    private ImageView plvsIvNetworkStatusSignal;

    private boolean hasNetwork = false;

    public PLVLSNetworkQualityWidget(@NonNull Context context) {
        this(context, null);

    }

    public PLVLSNetworkQualityWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSNetworkQualityWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_status_bar_network_status_widget, this, true);

        plvsIvNetworkStatusExclamationMark = findViewById(R.id.plvs_iv_network_status_exclamation_mark);
        plvsTvNetworkStatusCannotConnect = findViewById(R.id.plvs_tv_network_status_cannot_connect);
        plvsGroupNetworkStatusCannotConnect = findViewById(R.id.plvs_group_network_status_cannot_connect);
        plvsClNetworkStatus = findViewById(R.id.plvs_cl_network_status);
        plvsIvNetworkStatusSignal = findViewById(R.id.plvs_iv_network_status_signal);
    }

    public void setNetQuality(int netQuality) {
        switch (netQuality) {
            case PLVSStreamerConfig.NetQuality.NET_QUALITY_GOOD:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(R.drawable.plvls_network_signal_3);
                break;
            case PLVSStreamerConfig.NetQuality.NET_QUALITY_MIDDLE:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(R.drawable.plvls_network_signal_2);
                break;
            case PLVSStreamerConfig.NetQuality.NET_QUALITY_POOR:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(R.drawable.plvls_network_signal_1);
                break;
            case PLVSStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION:
                showHashNetwork(false);
                break;
        }
    }

    private void showHashNetwork(boolean hasNetwork) {
        if (hasNetwork) {
            plvsIvNetworkStatusSignal.setVisibility(VISIBLE);

            plvsGroupNetworkStatusCannotConnect.setVisibility(INVISIBLE);
            plvsGroupNetworkStatusCannotConnect.updatePreLayout(plvsClNetworkStatus);
            requestLayout();
        } else {
            plvsIvNetworkStatusSignal.setVisibility(INVISIBLE);

            plvsGroupNetworkStatusCannotConnect.setVisibility(VISIBLE);
            plvsGroupNetworkStatusCannotConnect.updatePreLayout(plvsClNetworkStatus);
            requestLayout();
        }
    }
}
