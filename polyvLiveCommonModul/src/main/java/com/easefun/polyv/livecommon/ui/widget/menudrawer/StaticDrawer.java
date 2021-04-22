package com.easefun.polyv.livecommon.ui.widget.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class StaticDrawer extends PLVMenuDrawer {

    public StaticDrawer(Context context) {
        super(context);
    }

    public StaticDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StaticDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        super.addView(mMenuContainer, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        super.addView(mContentContainer, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mIsStatic = true;
    }

    @Override
    protected void drawOverlay(Canvas canvas) {
        // NO-OP
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        // NO-OP
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        switch (getPosition()) {
            case LEFT:
                mMenuContainer.layout(0, 0, mMenuSize, height);
                mContentContainer.layout(mMenuSize, 0, width, height);
                break;

            case RIGHT:
                mMenuContainer.layout(width - mMenuSize, 0, width, height);
                mContentContainer.layout(0, 0, width - mMenuSize, height);
                break;

            case TOP:
                mMenuContainer.layout(0, 0, width, mMenuSize);
                mContentContainer.layout(0, mMenuSize, width, height);
                break;

            case BOTTOM:
                mMenuContainer.layout(0, height - mMenuSize, width, height);
                mContentContainer.layout(0, 0, width, height - mMenuSize);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == View.MeasureSpec.UNSPECIFIED || heightMode == View.MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = View.MeasureSpec.getSize(widthMeasureSpec);
        final int height = View.MeasureSpec.getSize(heightMeasureSpec);

        switch (getPosition()) {
            case LEFT:
            case RIGHT: {
                final int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

                final int menuWidth = mMenuSize;
                final int menuWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(menuWidth, View.MeasureSpec.EXACTLY);

                final int contentWidth = width - menuWidth;
                final int contentWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY);

                mContentContainer.measure(contentWidthMeasureSpec, childHeightMeasureSpec);
                mMenuContainer.measure(menuWidthMeasureSpec, childHeightMeasureSpec);
                break;
            }

            case TOP:
            case BOTTOM: {
                final int childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);

                final int menuHeight = mMenuSize;
                final int menuHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(menuHeight, View.MeasureSpec.EXACTLY);

                final int contentHeight = height - menuHeight;
                final int contentHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(contentHeight, View.MeasureSpec.EXACTLY);

                mContentContainer.measure(childWidthMeasureSpec, contentHeightMeasureSpec);
                mMenuContainer.measure(childWidthMeasureSpec, menuHeightMeasureSpec);
                break;
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void toggleMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public void openMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public void closeMenu(boolean animate) {
        // NO-OP
    }

    @Override
    public boolean isMenuVisible() {
        return true;
    }

    @Override
    public void setMenuSize(int size) {
        mMenuSize = size;
        requestLayout();
        invalidate();
    }

    @Override
    public void setDragAreaMenuBottom(int bottom) {
        // NO-OP
    }

    @Override
    public void setOffsetMenuEnabled(boolean offsetMenu) {
        // NO-OP
    }

    @Override
    public boolean getOffsetMenuEnabled() {
        return false;
    }

    @Override
    public void peekDrawer() {
        // NO-OP
    }

    @Override
    public void peekDrawer(long delay) {
        // NO-OP
    }

    @Override
    public void peekDrawer(long startDelay, long delay) {
        // NO-OP
    }

    @Override
    public void setHardwareLayerEnabled(boolean enabled) {
        // NO-OP
    }

    @Override
    public int getTouchMode() {
        return TOUCH_MODE_NONE;
    }

    @Override
    public void setTouchMode(int mode) {
        // NO-OP
    }

    @Override
    public void setTouchBezelSize(int size) {
        // NO-OP
    }

    @Override
    public int getTouchBezelSize() {
        return 0;
    }

    @Override
    public void setSlideDrawable(int drawableRes) {
        // NO-OP
    }

    @Override
    public void setSlideDrawable(Drawable drawable) {
        // NO-OP
    }

    @Override
    public void setupUpIndicator(Activity activity) {
        // NO-OP
    }

    @Override
    public void setDrawerIndicatorEnabled(boolean enabled) {
        // NO-OP
    }

    @Override
    public boolean isDrawerIndicatorEnabled() {
        return false;
    }
}
