package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;


/**
 * 悬浮窗View抽象类
 */
public abstract class PLVAbsFloatingLayout extends FrameLayout implements IPLVFloatingLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private final String TAG = this.getClass().getSimpleName();

    protected Context context;

    private int x;
    private int y;

    //悬浮窗坐标
    protected int floatingLocationX;
    protected int floatingLocationY;

    //悬浮窗宽高
    protected int floatWindowWidth;
    protected int floatWindowHeight;

    protected boolean enableDrag = true;
    protected boolean consumeTouchEventOnMove = true;
    protected PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_BACKGROUND;
    protected PLVFloatingEnums.AutoEdgeType autoEdgeType = PLVFloatingEnums.AutoEdgeType.AUTO_MOVE_TO_RIGHT;

    // 悬浮窗显示状态
    protected boolean isShowing = false;

    // 判断悬浮窗口是否移动
    private boolean isMove;

    // </editor-fold>

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

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    protected void init() {
        // 初始化
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="贴边动画">

    private void autoMoveToEdge() {
        if (!enableDrag || autoEdgeType == PLVFloatingEnums.AutoEdgeType.NO_AUTO_MOVE) {
            return;
        }
        final int targetLeft;
        switch (autoEdgeType) {
            case AUTO_MOVE_TO_LEFT:
                targetLeft = 0;
                break;
            case AUTO_MOVE_TO_RIGHT:
                targetLeft = ScreenUtils.getScreenWidth() - floatWindowWidth;
                break;
            case AUTO_MOVE_TO_NEAREST_EDGE:
                targetLeft = floatingLocationX + floatWindowWidth / 2 < ScreenUtils.getScreenWidth() / 2 ? 0 : ScreenUtils.getScreenWidth() - floatWindowWidth;
                break;
            default:
                targetLeft = floatingLocationX;
        }
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(floatingLocationX, targetLeft);
        // 动画执行
        valueAnimator.setDuration(100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int left = (int) animation.getAnimatedValue();
                updateFloatLocation(left, floatingLocationY);
            }
        });
        valueAnimator.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口实现">

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setShowType(PLVFloatingEnums.ShowType showType) {
        this.showType = showType;
    }

    @Override
    public void setAutoMoveToEdge(PLVFloatingEnums.AutoEdgeType autoEdgeType) {
        this.autoEdgeType = autoEdgeType;
    }

    @Override
    public void setEnableDrag(boolean enableDrag) {
        this.enableDrag = enableDrag;
    }

    @Override
    public void setConsumeTouchEventOnMove(boolean consumeTouchEventOnMove) {
        this.consumeTouchEventOnMove = consumeTouchEventOnMove;
    }

    // </editor-fold>

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
                if (enableDrag) {
                    updateFloatLocation(fitInsideScreenX(floatingLocationX + movedX), fitInsideScreenY(floatingLocationY + movedY));
                }
                if (Math.abs(movedX) >= 5 || Math.abs(movedY) >= 5) {
                    isMove = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    isMove = false;
                    autoMoveToEdge();
                    if (consumeTouchEventOnMove) {
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    protected int fitInsideScreenX(int x) {
        return clamp(x, 0, ScreenUtils.getScreenWidth() - floatWindowWidth);
    }

    protected int fitInsideScreenY(int y) {
        return clamp(y, 0, ScreenUtils.getScreenHeight() - floatWindowHeight);
    }

    // </editor-fold>

}
