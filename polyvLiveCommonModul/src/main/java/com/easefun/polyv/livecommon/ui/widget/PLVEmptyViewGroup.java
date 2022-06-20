package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Hoshiiro
 */
public class PLVEmptyViewGroup extends ViewGroup {

    public PLVEmptyViewGroup(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        setVisibility(GONE);
    }

    public PLVEmptyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        setVisibility(GONE);
    }

    public PLVEmptyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        setVisibility(GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return true;
    }
}
