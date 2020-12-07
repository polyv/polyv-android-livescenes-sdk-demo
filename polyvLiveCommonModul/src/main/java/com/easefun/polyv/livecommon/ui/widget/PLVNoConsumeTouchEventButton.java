package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * date: 2020/8/18
 * author: HWilliamgo
 * description: 不消费touch event，但是依然可以接收点击事件的按钮
 */
public class PLVNoConsumeTouchEventButton extends android.support.v7.widget.AppCompatButton {

    // <editor-fold defaultstate="collapsed" desc="属性">
    private GestureDetector gestureDetector;
    private OnClickListener onClickListener;

    private View shareTouchEventView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVNoConsumeTouchEventButton(Context context) {
        this(context, null);
    }

    public PLVNoConsumeTouchEventButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVNoConsumeTouchEventButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!isEnabled()) {
                    return false;
                }
                if (onClickListener != null) {
                    onClickListener.onClick(PLVNoConsumeTouchEventButton.this);
                }
                return true;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setShareTouchEventView(View shareTouchEventView) {
        this.shareTouchEventView = shareTouchEventView;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        this.onClickListener.onClick(this);
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写不拦截的点击事件">
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (shareTouchEventView != null) {
            shareTouchEventView.onTouchEvent(event);
        }
        return true;
    }
    // </editor-fold>
}
