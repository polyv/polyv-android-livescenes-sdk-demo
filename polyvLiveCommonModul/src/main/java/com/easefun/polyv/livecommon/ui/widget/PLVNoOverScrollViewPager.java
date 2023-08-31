package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

public class PLVNoOverScrollViewPager extends PLVSimpleViewPager {
    private boolean isNoOverScroll = true;

    public PLVNoOverScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public PLVNoOverScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNoOverScroll(boolean isNoOverScroll) {
        this.isNoOverScroll = isNoOverScroll;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (isNoOverScroll) {
            return true;
        }
        return super.canScrollHorizontally(direction);
    }
}
