package com.plv.livedemo.hiclass.fragments.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.plv.livecommon.ui.widget.PLVSimpleViewPager;

import org.jetbrains.annotations.NotNull;

/**
 * @author suhongtao
 */
public class PLVHCNoTouchViewPager extends PLVSimpleViewPager {
    public PLVHCNoTouchViewPager(@NonNull @NotNull Context context) {
        super(context);
    }

    public PLVHCNoTouchViewPager(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
