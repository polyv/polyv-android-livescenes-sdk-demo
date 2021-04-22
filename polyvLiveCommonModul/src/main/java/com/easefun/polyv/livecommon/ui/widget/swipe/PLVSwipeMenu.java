package com.easefun.polyv.livecommon.ui.widget.swipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class PLVSwipeMenu extends ViewGroup {
    private int downX, moveX, moved;
    private Scroller scroller;
    private boolean haveShowRight;
    private float lastX, lastY;
    private boolean isRequestEv;
    private boolean enabledSwipe;
    private OnShowRightChangedListener onShowRightChangedListener;
    public static PLVSwipeMenu swipeMenu;

    public interface OnShowRightChangedListener{
        void onChanged(boolean haveShowRight);
    }

    public PLVSwipeMenu(Context context) {
        this(context, null);
    }

    public PLVSwipeMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSwipeMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
    }

    public void setOnShowRightChangedListener(OnShowRightChangedListener l) {
        this.onShowRightChangedListener = l;
    }

    public void enabledSwipe(boolean enabledSwipe) {
        this.enabledSwipe = enabledSwipe;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (swipeMenu != null && swipeMenu == this) {
            swipeMenu.closeMenus();
            swipeMenu = null;
        }
    }

    //缓慢滚动到指定位置
    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        //1000ms内滑动destX，效果就是慢慢滑动
        scroller.startScroll(scrollX, 0, delta, 0, 100);
        invalidate();
    }

    public void closeMenus() {
        smoothScrollTo(0, 0);
        haveShowRight = false;
        if (onShowRightChangedListener != null) {
            onShowRightChangedListener.onChanged(haveShowRight);
        }
    }

    public static void closeMenu() {
        if (swipeMenu != null) {
            swipeMenu.closeMenus();
            swipeMenu = null;
        }
    }

    public void openMenus() {
        haveShowRight = true;
        swipeMenu = this;
        smoothScrollTo(getChildAt(1).getMeasuredWidth(), 0);
        if (onShowRightChangedListener != null) {
            onShowRightChangedListener.onChanged(haveShowRight);
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        closeMenu();
        if (!enabledSwipe) {
            return super.onTouchEvent(ev);
        }
        if (!scroller.isFinished()) {
            return false;
        }
        float currentX = ev.getX();
        float currentY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getRawX();
                lastX = currentX;
                lastY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = currentX - lastX;
                float dy = currentY - lastY;
                if (dx < 0 && Math.abs(dx) > Math.abs(dy) && !isRequestEv) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isRequestEv = true;
                }
                if (isRequestEv) {
                    moveX = (int) ev.getRawX();
                    moved = moveX - downX;
                    if (haveShowRight) {
                        moved -= getChildAt(1).getMeasuredWidth();
                    }
                    scrollTo(-moved, 0);
                    if (getScrollX() <= 0) {
                        scrollTo(0, 0);
                    } else if (getScrollX() >= getChildAt(1).getMeasuredWidth()) {
                        scrollTo(getChildAt(1).getMeasuredWidth(), 0);
                    }
                }
                lastX = currentX;
                lastY = currentY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isRequestEv) {
                    if (getScrollX() >= getChildAt(1).getMeasuredWidth() / 4) {
                        haveShowRight = true;
                        swipeMenu = this;
                        smoothScrollTo(getChildAt(1).getMeasuredWidth(), 0);
                        if (onShowRightChangedListener != null) {
                            onShowRightChangedListener.onChanged(haveShowRight);
                        }
                    } else {
                        haveShowRight = false;
                        smoothScrollTo(0, 0);
                        if (onShowRightChangedListener != null) {
                            onShowRightChangedListener.onChanged(haveShowRight);
                        }
                    }
                    getParent().requestDisallowInterceptTouchEvent(false);
                    isRequestEv = false;
                }
                lastX = 0;
                lastY = 0;
                break;

        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        View child = getChildAt(0);
        int margin =
                ((MarginLayoutParams) child.getLayoutParams()).topMargin +
                        ((MarginLayoutParams) child.getLayoutParams()).bottomMargin;
        setMeasuredDimension(width, getChildAt(0).getMeasuredHeight() + margin);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (i == 0) {
                child.layout(l, t, r, b);
            } else if (i == 1) {
                child.layout(r, t, r + child.getMeasuredWidth(), b);
            }
        }
    }
}
