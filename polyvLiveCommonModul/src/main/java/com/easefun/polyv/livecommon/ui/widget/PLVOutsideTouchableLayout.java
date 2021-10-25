package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * PLVOutsideTouchableLayout，适用于只有1个子view，点击子view之外的地方隐藏子view的情况
 */
public class PLVOutsideTouchableLayout extends FrameLayout {
    private List<OnOutsideDismissListener> onDismissListeners = new ArrayList<>();

    public PLVOutsideTouchableLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVOutsideTouchableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVOutsideTouchableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 0) {
            setClickable(true);
        } else {
            setClickable(false);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() > 0) {
            View view = getChildAt(0);
            int[] location = new int[2];
            view.getLocationInWindow(location);
            float x = ev.getX();
            float y = ev.getY();
            if (x < location[0]
                    || (x > location[0] && x < location[0] + view.getWidth()
                    && (y < location[1] || y > location[1] + view.getHeight()))
                    || x > location[0] + view.getWidth()) {
                removeAllViews();
                for (OnOutsideDismissListener listener : onDismissListeners) {
                    if (listener.view == view) {
                        listener.onDismiss();
                    }
                }
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void addOnDismissListener(OnOutsideDismissListener listener) {
        if (!onDismissListeners.contains(listener)) {
            onDismissListeners.add(listener);
        }
    }

    public static abstract class OnOutsideDismissListener {
        private View view;

        public OnOutsideDismissListener(View view) {
            this.view = view;
        }

        public abstract void onDismiss();
    }
}
