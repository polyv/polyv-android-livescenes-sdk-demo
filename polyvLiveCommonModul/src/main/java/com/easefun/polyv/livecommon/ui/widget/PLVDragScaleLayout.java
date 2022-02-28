package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author suhongtao
 */
public class PLVDragScaleLayout extends FrameLayout implements View.OnTouchListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVDragScaleLayout.class.getSimpleName();

    public static final int FLAG_CORNER_RIGHT_TOP = 0x0001;
    public static final int FLAG_CORNER_LEFT_TOP = 0x0002;
    public static final int FLAG_CORNER_LEFT_BOTTOM = 0x0004;
    public static final int FLAG_CORNER_RIGHT_BOTTOM = 0x0008;
    public static final int FLAG_CORNER_LEFT = FLAG_CORNER_LEFT_BOTTOM | FLAG_CORNER_LEFT_TOP;
    public static final int FLAG_CORNER_RIGHT = FLAG_CORNER_RIGHT_BOTTOM | FLAG_CORNER_RIGHT_TOP;
    public static final int FLAG_CORNER_TOP = FLAG_CORNER_LEFT_TOP | FLAG_CORNER_RIGHT_TOP;
    public static final int FLAG_CORNER_BOTTOM = FLAG_CORNER_LEFT_BOTTOM | FLAG_CORNER_RIGHT_BOTTOM;
    public static final int FLAG_CORNER_ALL = FLAG_CORNER_LEFT | FLAG_CORNER_RIGHT | FLAG_CORNER_TOP | FLAG_CORNER_BOTTOM;

    public static final int FLAG_EDGE_RIGHT = 0x0010;
    public static final int FLAG_EDGE_TOP = 0x0020;
    public static final int FLAG_EDGE_LEFT = 0x0040;
    public static final int FLAG_EDGE_BOTTOM = 0x0080;
    public static final int FLAG_EDGE_ALL = FLAG_EDGE_RIGHT | FLAG_EDGE_TOP | FLAG_EDGE_LEFT | FLAG_EDGE_BOTTOM;

    public static final int FLAG_MULTI_TOUCH = 0x0100;

    public static final int FLAG_CENTER = 0x1000;

    private int edgeResponseSize = ConvertUtils.dp2px(24);

    private int flagDragMode;
    private int flagScaleMode;

    private int minWidth = ConvertUtils.dp2px(144);
    private int minHeight = ConvertUtils.dp2px(81);
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;

    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    private int touchPosition;
    private float left;
    private float right;
    private float top;
    private float bottom;

    private ScaleGestureDetector multiTouchScaleGestureDetector;
    private SingleTouchScaleGestureDetector singleTouchScaleGestureDetector;
    private MultiTouchDragGestureDetector multiTouchDragGestureDetector;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVDragScaleLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVDragScaleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVDragScaleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView(@Nullable AttributeSet attrs) {
        parseAttrSet(attrs);

        initMultiTouchDragGestureDetector();
        initSingleTouchScaleGestureDetector();
        initMultiTouchScaleGestureDetector();

        setOnTouchListener(this);
        setClickable(true);
    }

    private void parseAttrSet(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVDragScaleLayout);
        int drag = typedArray.getInteger(R.styleable.PLVDragScaleLayout_plvDrag, 0);
        int scale = typedArray.getInteger(R.styleable.PLVDragScaleLayout_plvScale, 0);
        float minX = typedArray.getDimension(R.styleable.PLVDragScaleLayout_plvMinX, 0);
        float maxX = typedArray.getDimension(R.styleable.PLVDragScaleLayout_plvMaxX, 0);
        float minY = typedArray.getDimension(R.styleable.PLVDragScaleLayout_plvMinY, 0);
        float maxY = typedArray.getDimension(R.styleable.PLVDragScaleLayout_plvMaxY, 0);
        typedArray.recycle();

        setDragRange(minX, maxX, minY, maxY);
        setDragScaleMode(drag, scale);
    }

    private void initMultiTouchDragGestureDetector() {
        multiTouchDragGestureDetector = new MultiTouchDragGestureDetector.Builder()
                .view(this)
                .onDragListener(new MultiTouchDragGestureDetector.OnDragListener() {
                    @Override
                    public void onDrag(float dx, float dy, int pointCount) {
                        if (pointCount == 1 && (flagDragMode & touchPosition) != 0) {
                            processDrag(dx, dy);
                        } else if (pointCount > 1 && (flagDragMode & FLAG_MULTI_TOUCH) != 0) {
                            processDrag(dx, dy);
                        }
                    }
                })
                .build();
    }

    private void initSingleTouchScaleGestureDetector() {
        singleTouchScaleGestureDetector = new SingleTouchScaleGestureDetector.Builder()
                .view(this)
                .edgeResponseSize(edgeResponseSize)
                .onScaleListener(new SingleTouchScaleGestureDetector.OnScaleListener() {
                    @Override
                    public void onScale(float leftDx, float rightDx, float topDy, float bottomDy) {
                        if ((flagScaleMode & touchPosition) != 0) {
                            processScale(leftDx, rightDx, topDy, bottomDy);
                        }
                    }
                })
                .build();
    }

    private void initMultiTouchScaleGestureDetector() {
        multiTouchScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                final float factor = detector.getScaleFactor();
                final float width = right - left;
                final float height = bottom - top;
                final float dx = (width * factor - width) / 2;
                final float dy = (height * factor - height) / 2;
                processScale(-dx, dx, -dy, dy);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return (flagScaleMode & FLAG_MULTI_TOUCH) != 0;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View - 触摸事件重写">

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchPosition = parseTouchPosition(this, (int) event.getX(), (int) event.getY(), edgeResponseSize);
            left = getLeft();
            right = getRight();
            top = getTop();
            bottom = getBottom();
        }

        multiTouchScaleGestureDetector.onTouchEvent(event);
        singleTouchScaleGestureDetector.onTouchEvent(event);
        multiTouchDragGestureDetector.onTouchEvent(event);

        updateLayout();

        dispatchTouchEventToChildren(event);

        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API方法">

    public void setDragRange(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setMinSize(@Px int minWidth, @Px int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    public void setMaxSize(@Px int maxWidth, @Px int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setEdgeResponseSize(int edgeResponseSize) {
        this.edgeResponseSize = edgeResponseSize;
        singleTouchScaleGestureDetector.setEdgeResponseSize(edgeResponseSize);
    }

    public void setDragScaleMode(int dragFlags, int scaleFlags) {
        this.flagDragMode = dragFlags;
        this.flagScaleMode = scaleFlags;
        checkFlagsConflict();
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="拖拽缩放处理">

    protected void processDrag(float dx, float dy) {
        float maxConsumeDx = dx;
        if (left + dx < minX) {
            maxConsumeDx = minX - left;
        }
        if (right + dx > maxX) {
            maxConsumeDx = maxX - right;
        }
        scaleLeft(maxConsumeDx);
        scaleRight(maxConsumeDx);

        float maxConsumeDy = dy;
        if (top + dy < minY) {
            maxConsumeDy = minY - top;
        }
        if (bottom + dy > maxY) {
            maxConsumeDy = maxY - bottom;
        }
        scaleTop(maxConsumeDy);
        scaleBottom(maxConsumeDy);
    }

    protected void processScale(float left, float right, float top, float bottom) {
        scaleLeft(left);
        scaleRight(right);
        scaleTop(top);
        scaleBottom(bottom);
    }

    protected void scaleTop(float size) {
        top += size;
        // 防止超过顶部
        top = Math.max(top, minY);
        // 防止高度过小
        top = Math.min(top, bottom - minHeight);
        // 防止高度过大
        if (bottom - top > maxHeight) {
            top = bottom - maxHeight;
        }
    }

    protected void scaleBottom(float size) {
        bottom += size;
        // 防止超过底部
        bottom = Math.min(bottom, maxY);
        // 防止高度过小
        bottom = Math.max(bottom, top + minHeight);
        // 防止高度过大
        if (bottom - top > maxHeight) {
            bottom = top + maxHeight;
        }
    }

    protected void scaleLeft(float size) {
        left += size;
        // 防止超出左侧
        left = Math.max(left, minX);
        // 防止宽度过小
        left = Math.min(left, right - minWidth);
        // 防止宽度过大
        if (right - left > maxWidth) {
            left = right - maxWidth;
        }
    }

    protected void scaleRight(float size) {
        right += size;
        // 防止超过右侧
        right = Math.min(right, maxX);
        // 防止宽度过小
        right = Math.max(right, left + minWidth);
        // 防止宽度过大
        if (right - left > maxWidth) {
            right = left + maxWidth;
        }
    }

    protected void updateLayout() {
        updateLayoutParam((int) left, (int) top, (int) (right - left), (int) (bottom - top));
    }

    protected void updateLayoutParam(int leftMargin, int topMargin, int width, int height) {
        MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
        lp.leftMargin = leftMargin;
        lp.topMargin = topMargin;
        lp.width = width;
        lp.height = height;
        setLayoutParams(lp);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法 - 触摸点检测">

    public static int parseTouchPosition(View v, int x, int y, int edgeResponseSize) {
        int viewTop = v.getTop();
        int viewBottom = v.getBottom();
        int viewLeft = v.getLeft();
        int viewRight = v.getRight();
        int viewWidth = viewRight - viewLeft;
        int viewHeight = viewBottom - viewTop;

        // 优先检测4个角
        if (x < edgeResponseSize && y < edgeResponseSize) {
            return FLAG_CORNER_LEFT_TOP;
        }
        if (x > viewWidth - edgeResponseSize && y < edgeResponseSize) {
            return FLAG_CORNER_RIGHT_TOP;
        }
        if (x < edgeResponseSize && y > viewHeight - edgeResponseSize) {
            return FLAG_CORNER_LEFT_BOTTOM;
        }
        if (x > viewWidth - edgeResponseSize && y > viewHeight - edgeResponseSize) {
            return FLAG_CORNER_RIGHT_BOTTOM;
        }

        // 检测4个边缘
        if (x < edgeResponseSize) {
            return FLAG_EDGE_LEFT;
        }
        if (y < edgeResponseSize) {
            return FLAG_EDGE_TOP;
        }
        if (x > viewWidth - edgeResponseSize) {
            return FLAG_EDGE_RIGHT;
        }
        if (y > viewHeight - edgeResponseSize) {
            return FLAG_EDGE_BOTTOM;
        }

        return FLAG_CENTER;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void checkFlagsConflict() {
        final int conflictFlags = (flagDragMode & flagScaleMode | FLAG_MULTI_TOUCH) ^ FLAG_MULTI_TOUCH;
        if (conflictFlags != 0) {
            PLVCommonLog.e(TAG, "不能将同一flag同时应用在拖拽和缩放模式上");
            flagDragMode = flagDragMode - conflictFlags;
            flagScaleMode = flagScaleMode - conflictFlags;
        }
    }

    protected void dispatchTouchEventToChildren(MotionEvent ev) {
        final int action = ev.getAction();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                if (child.dispatchTouchEvent(ev)) {
                    break;
                }
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 拖拽缩放手势处理类">

    private static class MultiTouchDragGestureDetector {

        private static final float DRAG_THRESHOLD = 0.1F;

        private final View view;
        private final OnDragListener onDragListener;

        private final PointF lastCenterPoint = new PointF();
        private int lastPointerCount = 0;

        private MultiTouchDragGestureDetector(View view, OnDragListener onDragListener) {
            this.view = view;
            this.onDragListener = onDragListener;
        }

        public void onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastCenterPoint.set(getCenterPoint(event));
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (lastPointerCount != event.getPointerCount()) {
                    lastPointerCount = event.getPointerCount();
                    lastCenterPoint.set(getCenterPoint(event));
                    return;
                }
                PointF centerPoint = getCenterPoint(event);
                float dx = centerPoint.x - lastCenterPoint.x;
                float dy = centerPoint.y - lastCenterPoint.y;
                if (onDragListener != null) {
                    onDragListener.onDrag(dx, dy, event.getPointerCount());
                }
                lastCenterPoint.set(centerPoint);
            }
        }

        private PointF getCenterPoint(MotionEvent event) {
            float x = 0;
            float y = 0;
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                x += event.getX(i);
                y += event.getY(i);
            }
            float centerX = x / pointerCount;
            float centerY = y / pointerCount;
            if (view != null) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                centerX += location[0];
                centerY += location[1];
            }
            return new PointF(centerX, centerY);
        }

        public static class Builder {

            private View view;
            private OnDragListener onDragListener;

            public Builder view(View view) {
                this.view = view;
                return this;
            }

            public Builder onDragListener(OnDragListener onDragListener) {
                this.onDragListener = onDragListener;
                return this;
            }

            public MultiTouchDragGestureDetector build() {
                return new MultiTouchDragGestureDetector(view, onDragListener);
            }

        }

        interface OnDragListener {
            void onDrag(float dx, float dy, int pointCount);
        }

    }

    private static class SingleTouchScaleGestureDetector {

        private final View view;
        private final OnScaleListener onScaleListener;

        private int edgeResponseSize;

        private int touchPosition;
        private float lastX;
        private float lastY;

        private SingleTouchScaleGestureDetector(View view, OnScaleListener onScaleListener, int edgeResponseSize) {
            this.view = view;
            this.onScaleListener = onScaleListener;
            this.edgeResponseSize = edgeResponseSize;
        }

        public void onTouchEvent(MotionEvent event) {
            if (event.getPointerCount() > 1) {
                return;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchPosition = PLVDragScaleLayout.parseTouchPosition(view, (int) event.getX(), (int) event.getY(), edgeResponseSize);
                lastX = event.getRawX();
                lastY = event.getRawY();
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getRawX() - lastX;
                float dy = event.getRawY() - lastY;
                lastX = event.getRawX();
                lastY = event.getRawY();

                processScale(dx, dy);
            }
        }

        public void setEdgeResponseSize(int edgeResponseSize) {
            this.edgeResponseSize = edgeResponseSize;
        }

        private void processScale(float dx, float dy) {
            switch (touchPosition) {
                case PLVDragScaleLayout.FLAG_CORNER_RIGHT_TOP:
                    notifyOnScale(0, dx, dy, 0);
                    break;
                case PLVDragScaleLayout.FLAG_CORNER_LEFT_TOP:
                    notifyOnScale(dx, 0, dy, 0);
                    break;
                case PLVDragScaleLayout.FLAG_CORNER_LEFT_BOTTOM:
                    notifyOnScale(dx, 0, 0, dy);
                    break;
                case PLVDragScaleLayout.FLAG_CORNER_RIGHT_BOTTOM:
                    notifyOnScale(0, dx, 0, dy);
                    break;
                case PLVDragScaleLayout.FLAG_EDGE_LEFT:
                    notifyOnScale(dx, 0, 0, 0);
                    break;
                case PLVDragScaleLayout.FLAG_EDGE_RIGHT:
                    notifyOnScale(0, dx, 0, 0);
                    break;
                case PLVDragScaleLayout.FLAG_EDGE_TOP:
                    notifyOnScale(0, 0, dy, 0);
                    break;
                case PLVDragScaleLayout.FLAG_EDGE_BOTTOM:
                    notifyOnScale(0, 0, 0, dy);
                    break;
                default:
            }
        }

        private void notifyOnScale(float leftDx, float rightDx, float topDy, float bottomDy) {
            if (onScaleListener != null) {
                onScaleListener.onScale(leftDx, rightDx, topDy, bottomDy);
            }
        }

        public static class Builder {

            private View view;
            private OnScaleListener onScaleListener;
            private int edgeResponseSize;

            public Builder view(View view) {
                this.view = view;
                return this;
            }

            public Builder onScaleListener(OnScaleListener onScaleListener) {
                this.onScaleListener = onScaleListener;
                return this;
            }

            public Builder edgeResponseSize(int edgeResponseSize) {
                this.edgeResponseSize = edgeResponseSize;
                return this;
            }

            public SingleTouchScaleGestureDetector build() {
                return new SingleTouchScaleGestureDetector(view, onScaleListener, edgeResponseSize);
            }

        }

        interface OnScaleListener {
            void onScale(float leftDx, float rightDx, float topDy, float bottomDy);
        }

    }

    // </editor-fold>

}

