package com.easefun.polyv.livecommon.module.modules.marquee.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import java.util.HashMap;

public class PLVMarqueeRollAnimation extends PLVMarqueeAnimation {
    private static final String TAG = "PLVMarqueeRollAnimation";

    // <editor-fold desc="变量">
    protected int duration;
    protected int interval;

    @Nullable
    protected RelativeLayout.LayoutParams layoutParams;
    @Nullable
    protected View mainView;

    @Nullable
    protected ObjectAnimator mainAnimator;

    @Nullable
    private Runnable manualRollRunnable;
    private long manualRollStartTimeMs;
    private long manualRollPausedElapsedMs;
    private float manualRollStartX;
    private float manualRollEndX;
    // </editor-fold>

    // <editor-fold desc="对外API - 参数设置">
    @Override
    public void setViews(HashMap<Integer, View> viewMap) {
        mainView = viewMap.get(VIEW_MAIN);
        if (mainView == null) {
            return;
        }
        layoutParams = (RelativeLayout.LayoutParams) mainView.getLayoutParams();
        mainView.setAlpha(1F);
        mainView.setVisibility(View.GONE);
    }

    @Override
    public void setParams(HashMap<Integer, Integer> paramMap) {
        if (paramMap == null) {
            return;
        }
        this.viewWidth = paramMap.containsKey(PARAM_VIEW_WIDTH) ? paramMap.get(PARAM_VIEW_WIDTH) : 0;
        this.viewHeight = paramMap.containsKey(PARAM_VIEW_HEIGHT) ? paramMap.get(PARAM_VIEW_HEIGHT) : 0;
        this.screenWidth = paramMap.containsKey(PARAM_SCREEN_WIDTH) ? paramMap.get(PARAM_SCREEN_WIDTH) : 0;
        this.screenHeight = paramMap.containsKey(PARAM_SCREEN_HEIGHT) ? paramMap.get(PARAM_SCREEN_HEIGHT) : 0;
        this.duration = paramMap.containsKey(PARAM_DURATION) ? paramMap.get(PARAM_DURATION) : 0;
        this.interval = paramMap.containsKey(PARAM_INTERVAL) ? paramMap.get(PARAM_INTERVAL) : 0;
        if (paramMap.containsKey(PARAM_ISALWAYS_SHOW_WHEN_RUN)) {
            this.isAlwaysShowWhenRun = paramMap.get(PARAM_ISALWAYS_SHOW_WHEN_RUN) == 1;
        }
        if (paramMap.containsKey(PARAM_HIDE_WHEN_PAUSE)) {
            this.isHiddenWhenPause = paramMap.get(PARAM_HIDE_WHEN_PAUSE) == 1;
        }
    }
    // </editor-fold>

    // <editor-fold desc="对外API - 生命周期控制">
    @Override
    public void start() {
        if (mainView == null) {
            return;
        }
        if (shouldUseManualFrameAnimation(mainView)) {
            if (animationStatus == PAUSE) {
                startManualRoll(true);
            } else {
                startManualRoll(false);
            }
            return;
        }
        if (animationStatus == PAUSE && mainAnimator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mainAnimator.resume();
            } else {
                mainAnimator.start();
            }
            animationStatus = STARTED;
            if (mainAnimator.isRunning()) {
                mainView.setVisibility(View.VISIBLE);
            }
        } else {
            setAnimation();
            if (mainAnimator != null) {
                mainAnimator.start();
            }
        }
    }

    @Override
    public void pause() {
        if (mainView == null) {
            return;
        }
        animationStatus = PAUSE;
        if (manualRollRunnable != null) {
            manualRollPausedElapsedMs = Math.max(0, SystemClock.uptimeMillis() - manualRollStartTimeMs);
            mainView.removeCallbacks(manualRollRunnable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mainAnimator != null) {
            mainAnimator.pause();
        } else {
            stop();
        }
        if (isHiddenWhenPause) {
            mainView.setVisibility(View.GONE);
        }
    }

    @Override
    public void stop() {
        if (mainView == null) {
            return;
        }
        animationStatus = STOP;
        stopManualRoll();
        if (mainAnimator != null) {
            mainAnimator.cancel();
            mainAnimator.end();
        }
    }

    @Override
    public void destroy() {
        stopManualRoll();
        if (mainAnimator != null) {
            mainAnimator.removeAllListeners();
        }
        mainAnimator = null;
    }

    @Override
    public void onParentSizeChanged(final View parentView) {
        if (mainView == null) {
            return;
        }
        if (!isParentSizeChanged(parentView)) {
            return;
        }
        final int oldScreenWidth = screenWidth;
        final int oldScreenHeight = screenHeight;
        mainView.clearAnimation();
        mainView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (!updateParentSizeIfChanged(parentView, oldScreenWidth, oldScreenHeight)) {
                    return;
                }
                if (animationStatus == STARTED) {
                    stop();
                    start();
                } else if (animationStatus == PAUSE) {
                    stop();
                    setAnimation();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="功能模块 - 设置动画样式和位置">

    /**
     * 设置动画
     */
    protected void setAnimation() {
        if (animationStatus == STARTED) {
            return;
        }
        if (mainView == null) {
            return;
        }
        if (shouldUseManualFrameAnimation(mainView)) {
            startManualRoll(false);
            return;
        }
        animationStatus = STARTED;
        final float startX = isAlwaysShowWhenRun && screenWidth / 2 > viewWidth ? screenWidth - viewWidth : screenWidth;
        final float endX = isAlwaysShowWhenRun && screenWidth > viewWidth ? 0 : -viewWidth;
        mainAnimator = ObjectAnimator.ofFloat(mainView, "translationX", startX, endX);

        mainAnimator.setDuration(duration);
        mainAnimator.setInterpolator(new LinearInterpolator());
        mainAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mainView.setVisibility(View.VISIBLE);
                setActiveRect();
                mainView.setLayoutParams(layoutParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mainView.setVisibility(View.GONE);
                if (animationStatus == STARTED) {
                    mainAnimator.setStartDelay(isAlwaysShowWhenRun ? 0 : interval);
                    animation.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /**
     * 设置 滚动动画的位置
     */
    protected void setActiveRect() {
        if (layoutParams == null) {
            return;
        }
        layoutParams.topMargin = (int) (Math.random() * (screenHeight - Math.min(screenHeight, viewHeight)));
    }

    private void startManualRoll(boolean resume) {
        if (mainView == null) {
            return;
        }
        if (manualRollRunnable != null) {
            mainView.removeCallbacks(manualRollRunnable);
        }
        animationStatus = STARTED;
        manualRollStartX = isAlwaysShowWhenRun && screenWidth / 2 > viewWidth ? screenWidth - viewWidth : screenWidth;
        manualRollEndX = isAlwaysShowWhenRun && screenWidth > viewWidth ? 0 : -viewWidth;
        if (resume) {
            manualRollStartTimeMs = SystemClock.uptimeMillis() - manualRollPausedElapsedMs;
        } else {
            startManualRollCycle();
        }
        ensureManualRollRunnable();
        mainView.postOnAnimation(manualRollRunnable);
    }

    private void startManualRollCycle() {
        if (mainView == null) {
            return;
        }
        manualRollPausedElapsedMs = 0;
        manualRollStartTimeMs = SystemClock.uptimeMillis();
        mainView.setAlpha(1F);
        mainView.setTranslationX(manualRollStartX);
        mainView.setVisibility(View.VISIBLE);
        setActiveRect();
        mainView.setLayoutParams(layoutParams);
    }

    private void ensureManualRollRunnable() {
        if (manualRollRunnable != null) {
            return;
        }
        manualRollRunnable = new Runnable() {
            @Override
            public void run() {
                if (mainView == null || animationStatus != STARTED) {
                    return;
                }
                final long now = SystemClock.uptimeMillis();
                final long safeDuration = Math.max(1, duration);
                final long elapsed = Math.max(0, now - manualRollStartTimeMs);
                if (elapsed <= safeDuration) {
                    float fraction = Math.min(1F, elapsed * 1F / safeDuration);
                    mainView.setVisibility(View.VISIBLE);
                    mainView.setTranslationX(manualRollStartX + (manualRollEndX - manualRollStartX) * fraction);
                } else {
                    final long delayElapsed = elapsed - safeDuration;
                    if (isAlwaysShowWhenRun || delayElapsed >= Math.max(0, interval)) {
                        startManualRollCycle();
                    } else {
                        mainView.setVisibility(View.GONE);
                    }
                }
                mainView.postOnAnimation(this);
            }
        };
    }

    private void stopManualRoll() {
        if (mainView != null && manualRollRunnable != null) {
            mainView.removeCallbacks(manualRollRunnable);
        }
        manualRollPausedElapsedMs = 0;
    }
    // </editor-fold>
}
