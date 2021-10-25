package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import org.jetbrains.annotations.NotNull;

/**
 * @author suhongtao
 */
public class PLVDragScaleLayout extends FrameLayout implements View.OnTouchListener {

    public static final int CORNER_RESPONSE_SIZE = ConvertUtils.dp2px(26);
    public static final int TOP_RESPONSE_SIZE = ConvertUtils.dp2px(26);

    public static final int MIN_WIDTH = ConvertUtils.dp2px(120);
    public static final int MIN_HEIGHT = ConvertUtils.dp2px(52);

    /**
     * Center目前也包括了View左侧边、右侧边及底边部分范围
     *
     * @see #parseTouchPosition(View, int, int)
     */
    public static final int POSITION_CENTER = 0;
    public static final int POSITION_TOP_RIGHT = 1;
    public static final int POSITION_TOP_LEFT = 2;
    public static final int POSITION_BOTTOM_LEFT = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_TOP = 5;

    protected int minX;
    protected int minY;
    protected int maxX;
    protected int maxY;

    protected int left;
    protected int right;
    protected int top;
    protected int bottom;

    protected int touchPosition = POSITION_CENTER;
    protected float lastRawX;
    protected float lastRawY;

    public PLVDragScaleLayout(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PLVDragScaleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVDragScaleLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOnTouchListener(this);
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = right - left;
        int height = bottom - top;
        if (width == 0 && height == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int widthMeasureSpec2 = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int heightMeasureSpec2 = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchPosition = parseTouchPosition(this, (int) event.getX(), (int) event.getY());
            left = getLeft();
            right = getRight();
            top = getTop();
            bottom = getBottom();
            lastRawX = event.getRawX();
            lastRawY = event.getRawY();
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (touchPosition) {
            case POSITION_TOP:
            case POSITION_TOP_LEFT:
            case POSITION_TOP_RIGHT:
            case POSITION_BOTTOM_LEFT:
            case POSITION_BOTTOM_RIGHT:
                return true;
            case POSITION_CENTER:
            default:

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int dx = (int) (event.getRawX() - lastRawX);
            int dy = (int) (event.getRawY() - lastRawY);
            lastRawX = event.getRawX();
            lastRawY = event.getRawY();

            switch (touchPosition) {
                case POSITION_TOP:
                    processDrag(dx, dy);
                    break;
                case POSITION_TOP_LEFT:
                case POSITION_TOP_RIGHT:
                case POSITION_BOTTOM_LEFT:
                case POSITION_BOTTOM_RIGHT:
                    processScale(dx, dy);
                    break;
                case POSITION_CENTER:
                default:
                    break;
            }
            updateLayoutParam();
        }
        return true;
    }

    public void setScaleRange(int minX, int maxX, int minY, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    private void processDrag(int dx, int dy) {
        int maxConsumeDx = dx;
        if (left + dx < minX) {
            maxConsumeDx = minX - left;
        }
        if (right + dx > maxX) {
            maxConsumeDx = maxX - right;
        }
        scaleLeft(maxConsumeDx);
        scaleRight(maxConsumeDx);

        int maxConsumeDy = dy;
        if (top + dy < minY) {
            maxConsumeDy = minY - top;
        }
        if (bottom + dy > maxY) {
            maxConsumeDy = maxY - bottom;
        }
        scaleTop(maxConsumeDy);
        scaleBottom(maxConsumeDy);
    }

    private void processScale(int dx, int dy) {
        switch (touchPosition) {
            case POSITION_TOP_RIGHT:
                scaleTop(dy);
                scaleRight(dx);
                break;
            case POSITION_TOP_LEFT:
                scaleTop(dy);
                scaleLeft(dx);
                break;
            case POSITION_BOTTOM_LEFT:
                scaleBottom(dy);
                scaleLeft(dx);
                break;
            case POSITION_BOTTOM_RIGHT:
                scaleBottom(dy);
                scaleRight(dx);
                break;
            default:
        }
    }

    private void scaleTop(int size) {
        top += size;
        // 防止超过顶部
        top = Math.max(top, minY);
        // 防止高度过小
        top = Math.min(top, bottom - MIN_HEIGHT);
    }

    private void scaleBottom(int size) {
        bottom += size;
        // 防止超过底部
        bottom = Math.min(bottom, maxY);
        // 防止高度过小
        bottom = Math.max(bottom, top + MIN_HEIGHT);
    }

    private void scaleLeft(int size) {
        left += size;
        // 防止超出左侧
        left = Math.max(left, minX);
        // 防止宽度过小
        left = Math.min(left, right - MIN_WIDTH);
    }

    private void scaleRight(int size) {
        right += size;
        // 防止超过右侧
        right = Math.min(right, maxX);
        // 防止宽度过小
        right = Math.max(right, left + MIN_WIDTH);
    }

    private void updateLayoutParam() {
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.topMargin = top;
        lp.leftMargin = left;
        lp.height = bottom - top;
        lp.width = right - left;
        setLayoutParams(lp);
    }

    private static int parseTouchPosition(View v, int x, int y) {
        int viewTop = v.getTop();
        int viewBottom = v.getBottom();
        int viewLeft = v.getLeft();
        int viewRight = v.getRight();
        int viewWidth = viewRight - viewLeft;
        int viewHeight = viewBottom - viewTop;

        if (x < CORNER_RESPONSE_SIZE && y < CORNER_RESPONSE_SIZE) {
            return POSITION_TOP_LEFT;
        }
        if (viewWidth - x < CORNER_RESPONSE_SIZE && y < CORNER_RESPONSE_SIZE) {
            return POSITION_TOP_RIGHT;
        }
        if (x < CORNER_RESPONSE_SIZE && viewHeight - y < CORNER_RESPONSE_SIZE) {
            return POSITION_BOTTOM_LEFT;
        }
        if (viewWidth - x < CORNER_RESPONSE_SIZE && viewHeight - y < CORNER_RESPONSE_SIZE) {
            return POSITION_BOTTOM_RIGHT;
        }
        if (x >= CORNER_RESPONSE_SIZE
                && viewWidth - x >= CORNER_RESPONSE_SIZE
                && y < TOP_RESPONSE_SIZE) {
            // x不在左右边上，y在顶部边上
            return POSITION_TOP;
        }
        return POSITION_CENTER;
    }
}
