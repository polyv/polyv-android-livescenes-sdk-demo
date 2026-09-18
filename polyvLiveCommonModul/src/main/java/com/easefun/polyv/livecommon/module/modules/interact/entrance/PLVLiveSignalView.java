package com.easefun.polyv.livecommon.module.modules.interact.entrance;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** 三条底部对齐的直播信号柱，周期 1.2 秒，延迟分别为 0、0.9、0.6 秒。 */
public class PLVLiveSignalView extends View {
    private static final long PERIOD_MS = 1200;
    private static final long[] DELAYS_MS = {0, 900, 600};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private boolean live;
    private long startTime;

    public PLVLiveSignalView(@NonNull Context context) {
        this(context, null);
    }

    public PLVLiveSignalView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLiveSignalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.WHITE);
    }

    public void setLive(boolean live) {
        this.live = live;
        updateAnimation();
    }

    private void updateAnimation() {
        if (live && isAttachedToWindow() && isShown() && getWindowVisibility() == VISIBLE) {
            if (animator != null) {
                return;
            }
            startTime = SystemClock.uptimeMillis();
            animator = ValueAnimator.ofFloat(0F, 1F);
            animator.setDuration(PERIOD_MS);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animator.start();
        } else {
            stopAnimation();
        }
    }

    private void stopAnimation() {
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float padding = 2 * density;
        float barWidth = density;
        float availableHeight = Math.max(0, getHeight() - 2 * padding);
        float step = Math.max(0, getWidth() - 2 * padding - barWidth) / 2;
        float bottom = getHeight() - padding;
        long elapsed = animator == null ? 0 : SystemClock.uptimeMillis() - startTime;
        for (int i = 0; i < DELAYS_MS.length; i++) {
            long barElapsed = Math.max(0, elapsed - DELAYS_MS[i]);
            double phase = (barElapsed % PERIOD_MS) / (double) PERIOD_MS;
            float fraction = (float) (0.3 + 0.6 * (1 - Math.cos(2 * Math.PI * phase)) / 2);
            float left = padding + i * step;
            canvas.drawRoundRect(left, bottom - availableHeight * fraction,
                    left + barWidth, bottom, density, density, paint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateAnimation();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateAnimation();
    }
}
