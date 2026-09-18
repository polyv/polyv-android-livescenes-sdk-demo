package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** 直播首页的开播倒计时，同时向状态入口同步直播状态。 */
public class PLVECLiveCountDownLayout extends LinearLayout {
    private OnLiveStateChangedListener onLiveStateChangedListener;
    private TextView countDownTv;
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVLiveStateEnum liveState = PLVLiveStateEnum.WAITING;
    private PLVPlayerState playerState;
    private String liveStartTime;
    private CountDownTimer countDownTimer;

    private final Observer<PLVStatefulData<PolyvLiveClassDetailVO>> detailObserver =
            new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> data) {
                    if (data == null || !data.isSuccess() || data.getData() == null
                            || data.getData().getData() == null) {
                        return;
                    }
                    liveStartTime = data.getData().getData().getStartTime();
                    // 播放器已返回状态时，避免较晚返回的详情覆盖实时状态。
                    if (playerState == null || playerState == PLVPlayerState.IDLE
                            || playerState == PLVPlayerState.NO_LIVE
                            || (playerState == PLVPlayerState.LIVE_END
                            && (liveState == PLVLiveStateEnum.WAITING || liveState == PLVLiveStateEnum.UNSTART))) {
                        String watchStatus = data.getData().getData().getWatchStatus();
                        liveState = data.getData().getData().isLiveStatus() ? PLVLiveStateEnum.LIVE
                                : PLVLiveStateEnum.parse(TextUtils.isEmpty(watchStatus) ? "waiting" : watchStatus);
                    }
                    if (playerState != null && playerState != PLVPlayerState.IDLE) {
                        setPlayerState(playerState);
                    } else {
                        updateView();
                    }
                }
            };

    public PLVECLiveCountDownLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLiveCountDownLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLiveCountDownLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.plvec_widget_live_count_down_layout, this);
        countDownTv = findViewById(R.id.plvec_live_count_down_tv);
        setVisibility(GONE);
    }

    public void setOnLiveStateChangedListener(@Nullable OnLiveStateChangedListener listener) {
        onLiveStateChangedListener = listener;
        if (listener != null) {
            listener.onLiveStateChanged(liveState);
        }
    }

    public interface OnLiveStateChangedListener {
        void onLiveStateChanged(PLVLiveStateEnum state);
    }

    public void init(IPLVLiveRoomDataManager dataManager, LifecycleOwner lifecycleOwner) {
        destroy();
        liveRoomDataManager = dataManager;
        liveRoomDataManager.getClassDetailVO().observe(lifecycleOwner, detailObserver);
    }

    public void setPlayerState(@Nullable PLVPlayerState state) {
        if (state == null || state == PLVPlayerState.IDLE) {
            return;
        }
        playerState = state;
        if (state == PLVPlayerState.PREPARED) {
            liveState = PLVLiveStateEnum.LIVE;
        } else if (state == PLVPlayerState.LIVE_STOP) {
            liveState = PLVLiveStateEnum.STOP;
        } else if (state == PLVPlayerState.LIVE_END || state == PLVPlayerState.NO_LIVE) {
            // 未开播时播放器也可能返回结束，保留等待/未开始状态。
            if (liveState != PLVLiveStateEnum.WAITING && liveState != PLVLiveStateEnum.UNSTART) {
                liveState = PLVLiveStateEnum.END;
            }
        }
        updateView();
    }

    private void updateView() {
        if (onLiveStateChangedListener != null) {
            onLiveStateChangedListener.onLiveStateChanged(liveState);
        }
        stopCountDown();
        if (liveState == PLVLiveStateEnum.LIVE || liveState == PLVLiveStateEnum.STOP
                || liveState == PLVLiveStateEnum.PLAYBACK || liveState == PLVLiveStateEnum.PLAYBACK_CACHED
                || TextUtils.isEmpty(liveStartTime) || !isAttachedToWindow()) {
            return;
        }
        long startTimeMillis = TimeUtils.string2Millis(liveStartTime,
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()));
        long remainingMillis = startTimeMillis - System.currentTimeMillis();
        if (remainingMillis <= 0) {
            return;
        }
        countDownTimer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTv.setText(getResources().getString(R.string.plvec_live_start_count_down,
                        TimeUtils.toCountDownTime(millisUntilFinished)));
                countDownTv.setVisibility(VISIBLE);
                setVisibility(VISIBLE);
            }

            @Override
            public void onFinish() {
                countDownTv.setVisibility(GONE);
                setVisibility(GONE);
                countDownTimer = null;
            }
        }.start();
    }

    private void stopCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTv.setVisibility(GONE);
        setVisibility(GONE);
    }

    public void destroy() {
        stopCountDown();
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getClassDetailVO().removeObserver(detailObserver);
            liveRoomDataManager = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateView();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopCountDown();
        super.onDetachedFromWindow();
    }
}
