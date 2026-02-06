package com.easefun.polyv.streameralone.modules.streamer.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.rx.PLVTimer;
import com.plv.foundationsdk.utils.PLVTimeUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVSALinkMicDurationLayout extends FrameLayout {

    private TextView linkmicDurationTv;

    @Nullable
    private PLVLinkMicItemDataBean linkMicItemDataBean = null;

    @Nullable
    private Disposable updateDurationTimer = null;

    public PLVSALinkMicDurationLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVSALinkMicDurationLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVSALinkMicDurationLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_linkmic_duration_layout, this);

        linkmicDurationTv = findViewById(R.id.plvsa_linkmic_duration_tv);

        stop();
    }

    public void start(PLVLinkMicItemDataBean itemDataBean) {
        this.linkMicItemDataBean = itemDataBean;
        startTimer();
        setVisibility(VISIBLE);
    }

    public void stop() {
        setVisibility(GONE);
        stopTimer();
        linkMicItemDataBean = null;
    }

    private void startTimer() {
        updateDurationTimer = PLVTimer.timer(1000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                updateDuration();
            }
        });
    }

    private void stopTimer() {
        if (updateDurationTimer != null) {
            updateDurationTimer.dispose();
        }
        updateDurationTimer = null;
    }

    private void updateDuration() {
        if (linkMicItemDataBean == null) {
            return;
        }
        long timeDiff = System.currentTimeMillis() - linkMicItemDataBean.getLinkMicStartTimestamp();
        String timeText = PLVTimeUtils.generateTime(timeDiff, true);
        linkmicDurationTv.setText(timeText);
    }

}
