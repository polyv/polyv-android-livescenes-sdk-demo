package com.easefun.polyv.livecloudclass.modules.media.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.player.IPolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayType;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.PLVViewLocationSensor;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.livescenes.video.PolyvLiveVideoView;
import com.plv.foundationsdk.utils.PLVScreenUtils;

import java.util.Locale;

/**
 * 视频加载时显示的布局
 */
public class PLVLCVideoLoadingLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final int DP_TEXT_SIZE_SMALL = 10;
    private static final int DP_TEXT_SIZE_BIG = 12;
    private static final int DP_LOADING_VIEW_WIDTH_SMALL = 28;
    private static final int DP_LOADING_VIEW_WIDTH_BIG = 36;


    private ProgressBar loadingProgress;
    private TextView loadingSpeed;

    private IPolyvBaseVideoView videoView;

    private PLVViewLocationSensor viewLocationSensor;


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (videoView instanceof PolyvLiveVideoView ||
                        (videoView instanceof PolyvPlaybackVideoView && ((PolyvPlaybackVideoView) videoView).getPlayType() == PolyvPlayType.ONLINE_PLAY)) {
                    long tcpSpeed = videoView.getTcpSpeed();
                    if (tcpSpeed >= 0) {
                        loadingSpeed.setVisibility(View.VISIBLE);
                        loadingSpeed.setText(formatedSpeed(tcpSpeed, 1000));

                        handler.sendEmptyMessageDelayed(1, 500);
                    }
                }

            }
        }
    };
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCVideoLoadingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCVideoLoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCVideoLoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.plvlc_player_video_loading_layout, this);
        initView();
        initLocationSensor();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        loadingSpeed = (TextView) findViewById(R.id.loading_speed);
    }

    private void initLocationSensor() {
        viewLocationSensor = new PLVViewLocationSensor(this, new PLVViewLocationSensor.OnViewLocationSensorListener() {
            @Override
            public void onLandscapeSmall() {
                loadingSpeed.setTextSize(DP_TEXT_SIZE_SMALL);
                ViewGroup.LayoutParams lpOfLoading = loadingProgress.getLayoutParams();
                lpOfLoading.width = PLVScreenUtils.dip2px(DP_LOADING_VIEW_WIDTH_SMALL);
            }

            @Override
            public void onLandscapeBig() {
                loadingSpeed.setTextSize(DP_TEXT_SIZE_BIG);
                ViewGroup.LayoutParams lpOfLoading = loadingProgress.getLayoutParams();
                lpOfLoading.width = PLVScreenUtils.dip2px(DP_LOADING_VIEW_WIDTH_BIG);
            }

            @Override
            public void onPortraitSmall() {
                loadingSpeed.setTextSize(DP_TEXT_SIZE_SMALL);
                ViewGroup.LayoutParams lpOfLoading = loadingProgress.getLayoutParams();
                lpOfLoading.width = PLVScreenUtils.dip2px(DP_LOADING_VIEW_WIDTH_SMALL);
            }

            @Override
            public void onPortraitBig() {
                loadingSpeed.setTextSize(DP_TEXT_SIZE_BIG);
                ViewGroup.LayoutParams lpOfLoading = loadingProgress.getLayoutParams();
                lpOfLoading.width = PLVScreenUtils.dip2px(DP_LOADING_VIEW_WIDTH_BIG);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void bindVideoView(IPolyvBaseVideoView videoView) {
        this.videoView = videoView;
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="格式化速度">
    private static String formatedSpeed(long bytes, long elapsedMilli) {
        if (elapsedMilli <= 0) {
            return "0 B/S";
        }

        if (bytes <= 0) {
            return "0 B/S";
        }

        float bytesPerSec = ((float) bytes) * 1000.f / elapsedMilli;
        if (bytesPerSec >= 1000 * 1000) {
            return String.format(Locale.US, "%.2f MB/S", bytesPerSec / 1000 / 1000);
        } else if (bytesPerSec >= 1000) {
            return String.format(Locale.US, "%.2f KB/S", bytesPerSec / 1000);
        } else {
            return String.format(Locale.US, "%d B/S", (long) bytesPerSec);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制显示隐藏">
    private void acceptVisibilityChange(int visibility) {
        handler.removeCallbacksAndMessages(null);
        if (visibility == View.VISIBLE) {
            handler.sendEmptyMessage(1);
        } else {
            loadingSpeed.setVisibility(View.GONE);
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="方法重写 - View">
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        acceptVisibilityChange(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        acceptVisibilityChange(getVisibility());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewLocationSensor.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewLocationSensor.onSizeChanged(w, h, oldw, oldh);
    }
    // </editor-fold>
}
