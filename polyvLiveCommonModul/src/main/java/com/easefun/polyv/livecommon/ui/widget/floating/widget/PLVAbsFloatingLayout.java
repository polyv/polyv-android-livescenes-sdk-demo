package com.easefun.polyv.livecommon.ui.widget.floating.widget;

import static com.plv.foundationsdk.utils.PLVSugarUtil.clamp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
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

    //悬浮窗填充布局
    protected View contentView;

    //悬浮窗坐标
    protected int floatingLocationX;
    protected int floatingLocationY;

    //悬浮窗宽高
    protected int floatWindowWidth;
    protected int floatWindowHeight;

    protected boolean enableDrag = true;
    protected boolean enableDragX = true;
    protected boolean enableDragY = true;
    protected boolean consumeTouchEventOnMove = true;
    protected PLVFloatingEnums.ShowType showType = PLVFloatingEnums.ShowType.SHOW_ONLY_BACKGROUND;
    protected PLVFloatingEnums.AutoEdgeType autoEdgeType = PLVFloatingEnums.AutoEdgeType.AUTO_MOVE_TO_RIGHT;

    // 悬浮窗显示状态
    protected boolean isShowing = false;

    // 判断悬浮窗口是否移动
    private boolean isMove;

    // 缩放相关
    protected boolean enableScale = false;
    protected float minScale = 0.5f;
    protected float maxScale = 2.0f;
    protected float currentScale = 1.0f;
    protected int originalWidth;
    protected int originalHeight;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isScaling = false;
    private boolean isJustFinishScale = false;
    // 收起相关
    protected boolean enableCollapse = false;
    protected View collapseView;
    protected View collapsedLeftView;
    protected View collapsedRightView;
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
        collapseView = LayoutInflater.from(getContext()).inflate(R.layout.plv_widget_floating_window_collapsed, null, false);
        collapsedLeftView = collapseView.findViewById(R.id.plv_float_window_collapsed_left_iv);
        collapsedRightView = collapseView.findViewById(R.id.plv_float_window_collapsed_right_iv);
        collapsedLeftView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getAlpha() > 0) {
                    restoreCollapse();
                }
            }
        });
        collapsedRightView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getAlpha() > 0) {
                    restoreCollapse();
                }
            }
        });
        addView(collapseView);
        // 初始化缩放手势检测器
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                if (!enableScale) {
                    return false;
                }
                isScaling = true;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (!enableScale || !isScaling || isCollapseState()) {
                    return false;
                }

                float scaleFactor = detector.getScaleFactor();
                float newScale = currentScale * scaleFactor;

                // 使用内部缩放方法
                internalUpdateScale(newScale);

                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
                isJustFinishScale = true;
            }
        });
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

    private boolean checkCollapse() {
        if (contentView == null) {
            return false;
        }
        if (enableCollapse) {
            float translationX = getTranslationX();
            if (Math.abs(translationX) > (float) floatWindowWidth / 3) {
                contentView.setZ(-99);
                collapseView.setZ(99);
                if (translationX < 0) {
                    collapsedLeftView.setAlpha(1.0f);
                } else if (translationX > 0) {
                    collapsedRightView.setAlpha(1.0f);
                }
                floatingLocationX = 0;
                animate()
                        .translationX(translationX < 0 ? -floatWindowWidth : floatWindowWidth)
                        .setDuration(100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                contentView.setVisibility(View.INVISIBLE);
                                setTranslationX(0);
                            }
                        })
                        .start();
                return true;
            }
        }
        return false;
    }

    private void restoreCollapse() {
        if (contentView == null) {
            return;
        }
        contentView.setZ(99);
        collapseView.setZ(-99);
        contentView.setVisibility(View.VISIBLE);
        boolean isLeft = collapsedLeftView.getAlpha() > 0;
        boolean isRight = collapsedRightView.getAlpha() > 0;
        if (isLeft) {
            floatingLocationX = 0;
        }
        if (isRight) {
            floatingLocationX = ScreenUtils.getScreenWidth() - floatWindowWidth;
        }
        collapsedLeftView.setAlpha(0.0f);
        collapsedRightView.setAlpha(0.0f);
    }

    private boolean isCollapseState() {
        return enableCollapse && (collapsedLeftView.getAlpha() > 0 || collapsedRightView.getAlpha() > 0);
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
    public void setEnableDragX(boolean enableDrag) {
        this.enableDragX = enableDrag;
    }

    @Override
    public void setEnableDragY(boolean enableDrag) {
        this.enableDragY = enableDrag;
    }

    @Override
    public void setConsumeTouchEventOnMove(boolean consumeTouchEventOnMove) {
        this.consumeTouchEventOnMove = consumeTouchEventOnMove;
    }

    @Override
    public void setEnableScale(boolean enableScale) {
        this.enableScale = enableScale;
    }

    @Override
    public void setScaleRange(float minScale, float maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    @Override
    public float getCurrentScale() {
        return currentScale;
    }

    @Override
    public void resetScale() {
        if (originalWidth > 0 && originalHeight > 0) {
            internalUpdateScale(1.0f);
        }
    }

    @Override
    public void setScale(float scale) {
        internalUpdateScale(scale);
    }

    @Override
    public void setEnableCollapse(boolean enableCollapse) {
        this.enableCollapse = enableCollapse;
    }

    @Override
    public void updateFloatSize(int width, int height) {
        // 当外部直接调用updateFloatSize时，重置缩放基准
        if (!isScaling) {
            originalWidth = width;
            originalHeight = height;
            currentScale = 1.0f;
        }

        floatWindowWidth = width;
        floatWindowHeight = height;

        // 确保不超出屏幕
        int maxWidth = ScreenUtils.getScreenWidth();
        int maxHeight = ScreenUtils.getScreenHeight();
        floatWindowWidth = Math.min(floatWindowWidth, maxWidth);
        floatWindowHeight = Math.min(floatWindowHeight, maxHeight);

        // 更新位置确保不超出屏幕
        floatingLocationX = fitInsideScreenX(floatingLocationX);
        floatingLocationY = fitInsideScreenY(floatingLocationY);

        // 通知子类更新实际显示
        onFloatSizeChanged(floatWindowWidth, floatWindowHeight);
    }

    @Override
    public void updateFloatLocation(int x, int y) {
        x = fitInsideScreenX(x);
        y = fitInsideScreenY(y);
        floatingLocationX = x;
        floatingLocationY = y;
        // 子类需要重写来处理实际的位置更新
        onFloatLocationChanged(x, y);
    }

    @Override
    public Point getFloatLocation() {
        return new Point(floatingLocationX, floatingLocationY);
    }

    /**
     * 悬浮窗位置变化回调，子类需要重写来更新实际显示
     */
    protected void onFloatLocationChanged(int x, int y) {
        // 默认空实现，子类重写
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="touch事件处理">

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 先让缩放手势检测器处理事件
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                isMove = false;
                isJustFinishScale = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isScaling) { // 只有在非缩放状态下才处理拖拽
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    if (enableDrag) {
                        int newX = enableDragX ? fitInsideScreenX(floatingLocationX + movedX) : floatingLocationX;
                        int newY = enableDragY ? fitInsideScreenY(floatingLocationY + movedY) : floatingLocationY;
                        if (isCollapseState()) {
                            newX = collapsedLeftView.getAlpha() > 0 ? floatingLocationX : ScreenUtils.getScreenWidth() - ConvertUtils.dp2px(24);
                        }
                        updateFloatLocation(newX, newY);
                    }
                    if (Math.abs(movedX) >= 5 || Math.abs(movedY) >= 5) {
                        isMove = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isScaling && isMove) {
                    isMove = false;
                    boolean result = !isJustFinishScale && checkCollapse();
                    if (!result) {
                        autoMoveToEdge();
                    }
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
        if (enableCollapse && (collapsedLeftView.getAlpha() <= 0 && collapsedRightView.getAlpha() <= 0)) {
            if (x < 0) {
                setTranslationX(x);
            } else if (x > ScreenUtils.getScreenWidth() - floatWindowWidth) {
                setTranslationX(x - (ScreenUtils.getScreenWidth() - floatWindowWidth));
            } else {
                setTranslationX(0);
            }
            return x;
        }
        return clamp(x, 0, ScreenUtils.getScreenWidth() - floatWindowWidth);
    }

    protected int fitInsideScreenY(int y) {
        return clamp(y, 0, ScreenUtils.getScreenHeight() - floatWindowHeight);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="缩放功能">

    /**
     * 内部缩放方法，不重置基准尺寸
     */
    private void internalUpdateScale(float newScale) {
        if (originalWidth == 0 || originalHeight == 0) {
            return;
        }

        // 限制缩放范围
        newScale = clamp(newScale, minScale, maxScale);

        // 计算新的宽高
        int newWidth = (int) (originalWidth * newScale);
        int newHeight = (int) (originalHeight * newScale);

        // 确保不超过屏幕
        int maxWidth = ScreenUtils.getScreenWidth();
        int maxHeight = ScreenUtils.getScreenHeight();
        newWidth = Math.min(newWidth, maxWidth);
        newHeight = Math.min(newHeight, maxHeight);

        // 更新当前缩放比例
        currentScale = (float) newWidth / originalWidth;

        // 直接更新WindowManager参数，不调用updateFloatSize
        floatWindowWidth = newWidth;
        floatWindowHeight = newHeight;

        // 更新位置确保不超出屏幕
        floatingLocationX = fitInsideScreenX(floatingLocationX);
        floatingLocationY = fitInsideScreenY(floatingLocationY);

        // 通知子类更新实际显示
        onFloatSizeChanged(newWidth, newHeight);
    }

    /**
     * 悬浮窗尺寸变化回调，子类需要重写来更新实际显示
     */
    protected void onFloatSizeChanged(int width, int height) {
        // 默认空实现，子类重写
    }

    // </editor-fold>

}
