package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;

/**
 * date: 2019/10/12 0012
 *
 * @author hwj
 * description 网络状态View
 */
public class PLVLSNetworkQualityWidget extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private Group plvsGroupNetworkStatusCannotConnect;
    private ConstraintLayout plvsClNetworkStatus;
    private ImageView plvsIvNetworkStatusSignal;

    private boolean shouldShowNoNetworkHint = true;

    private int resNetGood = R.drawable.plv_network_signal_streamer_good;
    private int resNetMiddle = R.drawable.plv_network_signal_streamer_middle;
    private int resNetPoor = R.drawable.plv_network_signal_streamer_poor;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_status_bar_network_status_widget, this, true);

        plvsGroupNetworkStatusCannotConnect = findViewById(R.id.plvs_group_network_status_cannot_connect);
        plvsClNetworkStatus = findViewById(R.id.plvs_cl_network_status);
        plvsIvNetworkStatusSignal = findViewById(R.id.plvs_iv_network_status_signal);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 设置网路质量样式图片
     *
     * @param resNetGood   网络较好
     * @param resNetMiddle 网络一般
     * @param resNetPoor   网络较差
     */
    public void setNetQualityRes(int resNetGood, int resNetMiddle, int resNetPoor) {
        this.resNetGood = resNetGood;
        this.resNetMiddle = resNetMiddle;
        this.resNetPoor = resNetPoor;
        //让图片立刻替换并显示
        showHashNetwork(true);
        plvsIvNetworkStatusSignal.setImageResource(resNetGood);
    }

    /**
     * 设置是否应该显示没有网络的提示
     * true将会显示警告和文字，false就按照网络较差来显示
     *
     * @param shouldShowNoNetworkHint boolean
     */
    public void shouldShowNoNetworkHint(boolean shouldShowNoNetworkHint) {
        this.shouldShowNoNetworkHint = shouldShowNoNetworkHint;
        if (!shouldShowNoNetworkHint){
            plvsGroupNetworkStatusCannotConnect.setVisibility(GONE);
            plvsGroupNetworkStatusCannotConnect.updatePreLayout(plvsClNetworkStatus);
        }
    }

    /**
     * 设置网络质量
     *
     * @param netQuality see{@link PLVStreamerConfig.NetQuality}
     */
    public void setNetQuality(int netQuality) {
        if (!PLVNetworkUtils.isConnected(getContext())) {
            netQuality = PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION;
        }

        switch (netQuality) {
            case PLVStreamerConfig.NetQuality.NET_QUALITY_GOOD:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(resNetGood);
                break;
            case PLVStreamerConfig.NetQuality.NET_QUALITY_MIDDLE:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(resNetMiddle);
                break;
            case PLVStreamerConfig.NetQuality.NET_QUALITY_POOR:
                showHashNetwork(true);
                plvsIvNetworkStatusSignal.setImageResource(resNetPoor);
                break;
            case PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION:
                if (shouldShowNoNetworkHint) {
                    showHashNetwork(false);
                } else {
                    showHashNetwork(true);
                    plvsIvNetworkStatusSignal.setImageResource(resNetPoor);
                }
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置样式">
    private void showHashNetwork(boolean hasNetwork) {
        if (hasNetwork) {
            plvsIvNetworkStatusSignal.setVisibility(VISIBLE);
            if (shouldShowNoNetworkHint) {
                plvsGroupNetworkStatusCannotConnect.setVisibility(INVISIBLE);
            } else {
                plvsGroupNetworkStatusCannotConnect.setVisibility(GONE);
            }
            plvsGroupNetworkStatusCannotConnect.updatePreLayout(plvsClNetworkStatus);
            requestLayout();
        } else {
            plvsIvNetworkStatusSignal.setVisibility(INVISIBLE);

            plvsGroupNetworkStatusCannotConnect.setVisibility(VISIBLE);
            plvsGroupNetworkStatusCannotConnect.updatePreLayout(plvsClNetworkStatus);
            requestLayout();
        }
    }
    // </editor-fold>
}
