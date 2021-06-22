package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PLVNoInterceptTouchViewPager extends PLVSimpleViewPager {
    private boolean dispatchTouchEvent;

    public PLVNoInterceptTouchViewPager(@NonNull Context context) {
        super(context);
    }

    public PLVNoInterceptTouchViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        dispatchTouchEvent = super.dispatchTouchEvent(ev);//inner view slide
        return dispatchTouchEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);//handle slide-touch-up no restore
        return dispatchTouchEvent && result;//inner view click
    }

    public boolean onSuperTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }
}
