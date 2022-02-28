package com.easefun.polyv.livehiclass.modules.linkmic.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livehiclass.R;

/**
 * 讲师屏幕共享流布局
 */
public class PLVHCTeacherScreenStreamLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private ViewGroup container;
    private View renderView;
    @Nullable
    private String id;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCTeacherScreenStreamLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCTeacherScreenStreamLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCTeacherScreenStreamLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setBackgroundColor(Color.BLACK);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void show(String id, View renderView) {
        if (id == null || this.id != null) {
            return;
        }
        this.renderView = renderView;
        this.id = id;
        this.renderView.setVisibility(View.VISIBLE);
        addView(this.renderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_teacher_screen_stream_container);
        }
        if (container != null) {
            container.removeAllViews();
            container.addView(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void hide(String id, CallParamRunnable<View> runnable) {
        if (this.id == null || !this.id.equals(id)) {
            return;
        }
        if (renderView != null) {
            renderView.setVisibility(View.INVISIBLE);
            if (renderView.getParent() != null) {
                ((ViewGroup) renderView.getParent()).removeView(renderView);
            }
        }
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        if (runnable != null && renderView != null) {
            runnable.run(renderView);
        }
        renderView = null;
        this.id = null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写View的方法">
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (renderView != null) {
            renderView.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">
    public interface CallParamRunnable<T> {
        void run(T t);
    }
    // </editor-fold>
}
