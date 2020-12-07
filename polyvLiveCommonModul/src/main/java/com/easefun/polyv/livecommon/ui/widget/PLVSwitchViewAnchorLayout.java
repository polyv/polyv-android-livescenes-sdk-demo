package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * date: 2020/8/13
 * author: HWilliamgo
 * description: 要切换的View的父布局，只能有一个直接的子View。使用[PLVViewSwitcher.registerSwitchVew]进行注册。
 */
public class PLVSwitchViewAnchorLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    //listener
    private IPLVSwitchViewAnchorLayoutListener onSwitchListener;
    //子View是否已经切换
    private boolean isViewSwitched = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSwitchViewAnchorLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSwitchViewAnchorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSwitchViewAnchorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setOnSwitchListener(IPLVSwitchViewAnchorLayoutListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
    }

    /**
     * 获取要切换的View。要切换的View必须在一个FrameLayout里，且FrameLayout只有
     * 他一个子View。
     *
     * @return 要切换的View
     */
    public View getSwitchView() throws IllegalAccessException {
        int childCount = getChildCount();
        if (childCount == 0) {
            throw new IllegalAccessException("child count must not be 0!");
        } else if (childCount > 1) {
            throw new IllegalAccessException("child count must exactly be 1");
        }
        return getChildAt(0);
    }

    /**
     * 子View是否已经切换
     *
     * @return true表示子View切换到别处了，false表示子View还在原地。
     */
    public boolean isViewSwitched() {
        return isViewSwitched;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="notify监听器">
    public void notifySwitchBackBefore() {
        if (onSwitchListener != null) {
            onSwitchListener.onSwitchBackBefore();
        }
    }

    public void notifySwitchBackAfter() {
        if (onSwitchListener != null) {
            onSwitchListener.onSwitchBackAfter();
        }
        isViewSwitched = false;
    }

    public void notifySwitchElsewhereBefore() {
        if (onSwitchListener != null) {
            onSwitchListener.onSwitchElsewhereBefore();
        }
    }

    public void notifySwitchElsewhereAfter() {
        if (onSwitchListener != null) {
            onSwitchListener.onSwitchElsewhereAfter();
        }
        isViewSwitched = true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器定义">
    public static class IPLVSwitchViewAnchorLayoutListener {
        /**
         * view切回来之前
         */
        protected void onSwitchBackBefore() {/**/}

        /**
         * view切回来之后
         */
        protected void onSwitchBackAfter() {/**/}

        /**
         * view切出去之前
         */
        protected void onSwitchElsewhereBefore() {/**/}

        /**
         * view切出去之后
         */
        protected void onSwitchElsewhereAfter() {/**/}
    }
    // </editor-fold>

}
