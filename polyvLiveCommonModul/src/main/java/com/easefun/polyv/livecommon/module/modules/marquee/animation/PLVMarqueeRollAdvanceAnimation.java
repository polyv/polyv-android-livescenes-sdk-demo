package com.easefun.polyv.livecommon.module.modules.marquee.animation;

import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;

import com.plv.foundationsdk.rx.PLVRxTimer;

import java.util.HashMap;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 跑马灯 滚动 增强的动画类
 */
public class PLVMarqueeRollAdvanceAnimation extends PLVMarqueeRollAnimation {

    // <editor-fold desc="变量">
    @Nullable
    private View secondView;

    private Disposable viewPositionChangeDisposable;
    private volatile boolean isStarted = false;

    // </editor-fold>

    // <editor-fold desc="对外API - 参数设置">
    @Override
    public void setViews(HashMap<Integer, View> viewMap) {
        super.setViews(viewMap);
        secondView = viewMap.get(VIEW_SECOND);
        if (secondView == null) {
            return;
        }
        secondView.setAlpha(0);
    }
    // </editor-fold>

    // <editor-fold desc="对外API - 生命周期控制">
    @Override
    public void start() {
        super.start();
        isStarted = true;
        if (secondView != null) {
            secondView.setAlpha(1);
        }
        if (viewPositionChangeDisposable != null) {
            viewPositionChangeDisposable.dispose();
        }
        viewPositionChangeDisposable = PLVRxTimer.timer((int) seconds(5).toMillis(),
                new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (isStarted) {
                            setSecondActiveRect();
                        }
                    }
                });
    }

    @Override
    public void pause() {
        super.pause();
        isStarted = false;
        if (secondView != null) {
            secondView.setAlpha(0);
        }
    }

    @Override
    public void stop() {
        super.stop();
        isStarted = false;
        if (secondView != null) {
            secondView.setAlpha(0);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        isStarted = false;
        if (secondView != null) {
            secondView.setAlpha(0);
        }
        if (viewPositionChangeDisposable != null) {
            viewPositionChangeDisposable.dispose();
            viewPositionChangeDisposable = null;
        }
    }

    @Override
    public void onParentSizeChanged(final View parentView) {
        super.onParentSizeChanged(parentView);
        if (secondView == null) {
            return;
        }
        secondView.clearAnimation();
        secondView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = parentView.getWidth();
                screenHeight = parentView.getHeight();
                secondView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setSecondActiveRect();
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="功能模块 - 设置位置">

    // 设置活动区域
    protected void setSecondActiveRect() {
        if (secondView == null) {
            return;
        }
        MarginLayoutParams lp = (MarginLayoutParams) secondView.getLayoutParams();
        lp.topMargin = (int) (Math.random() * (screenHeight - Math.min(screenHeight, secondView.getHeight())));
        lp.leftMargin = (int) (Math.random() * (screenWidth - Math.min(screenWidth, secondView.getWidth())));
        secondView.setLayoutParams(lp);
    }

    // </editor-fold>

}
