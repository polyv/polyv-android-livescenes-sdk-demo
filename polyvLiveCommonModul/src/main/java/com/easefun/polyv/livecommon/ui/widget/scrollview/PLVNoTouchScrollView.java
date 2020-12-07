package com.easefun.polyv.livecommon.ui.widget.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class PLVNoTouchScrollView extends ScrollView {
    public PLVNoTouchScrollView(Context context) {
        super(context);
    }

    public PLVNoTouchScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVNoTouchScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
