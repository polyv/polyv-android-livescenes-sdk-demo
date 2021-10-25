package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PLVNoInterceptTouchRecyclerView extends RecyclerView {
    public PLVNoInterceptTouchRecyclerView(Context context) {
        super(context);
    }

    public PLVNoInterceptTouchRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }

    public boolean onSuperTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }
}
