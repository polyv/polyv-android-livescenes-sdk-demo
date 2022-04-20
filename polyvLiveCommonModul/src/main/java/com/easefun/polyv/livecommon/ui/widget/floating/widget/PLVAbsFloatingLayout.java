package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;


/**
 * 悬浮窗View抽象类
 */
public abstract class PLVAbsFloatingLayout extends FrameLayout implements IPLVFloatingLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private String TAG = this.getClass().getSimpleName();


    //仅前台显示
    public static int PLV_WINDOW_SHOW_ONLY_FOREGROUND = 22;
    //仅后台显示
    public static int PLV_WINDOW_SHOW_ONLY_BACKGROUND = 23;
    //总是显示
    public static int PLV_WINDOW_SHOW_EVERYWHERE = 24;

    protected Context context;

    private int x;
    private int y;
    //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
    private boolean isMove;

    //悬浮窗坐标
    protected int floatingLocationX;
    protected int floatingLocationY;

    //悬浮窗宽高
    protected int floatWindowWidth;
    protected int floatWindowHeight;

    //悬浮窗显示状态
    protected boolean isShow = false;

    //是否允许自动贴边
    protected boolean enableAutoMoveToEdge = true;
    //是否允许滑动
    protected boolean enableDrag;

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVAbsFloatingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVAbsFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVAbsFloatingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化">
    protected void init(){
        //做初始化
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="贴边动画">

    private void autoMoveToEdge() {
        if(!enableAutoMoveToEdge){
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(floatingLocationX, ScreenUtils.getScreenWidth());
        //动画执行时间
        valueAnimator.setDuration(100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (int) animation.getAnimatedValue();

                updateFloatLocation(x, floatingLocationY);
            }
        });
        valueAnimator.start();
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="接口实现">
    @Override
    public boolean isShow() {
        return isShow;
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="touch事件处理">

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                isMove = false;
                break;
            case MotionEvent.ACTION_MOVE:

                int nowX = (int) event.getRawX();
                int nowY = (int) event.getRawY();
                int movedX = nowX - x;
                int movedY = nowY - y;
                x = nowX;
                y = nowY;
                updateFloatLocation(floatingLocationX +movedX, floatingLocationY +movedY);
                if (Math.abs(movedX) >= 5 || Math.abs(movedY) >= 5) {
                    isMove = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    autoMoveToEdge();
                    isMove = false;
                } else {
                    //点击
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }
    // </editor-fold >

}
