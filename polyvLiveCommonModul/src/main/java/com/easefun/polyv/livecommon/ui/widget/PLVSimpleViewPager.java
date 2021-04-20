package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.plv.foundationsdk.log.PLVCommonLog;

public class PLVSimpleViewPager extends ViewPager {
    private static final String TAG = "PLVSimpleViewPager";
    public PLVSimpleViewPager(@NonNull Context context) {
        super(context);
    }

    public PLVSimpleViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            PLVCommonLog.e(TAG,"onTouchEvent:"+ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            PLVCommonLog.e(TAG,"onInterceptTouchEvent:"+ex.getMessage());
        }
        return false;
    }
}
