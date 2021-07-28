package com.easefun.polyv.livecommon.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;

/**
 * @author suhongtao
 */
public class PLVTriangleIndicateLayout extends FrameLayout {

    private AttributeSet mAttrs = null;

    public static final int POSITION_TOP = 0;
    public static final int POSITION_BOTTOM = 1;
    public static final int POSITION_LEFT = 2;
    public static final int POSITION_RIGHT = 3;
    public static final int MARGIN_TYPE_LEFT = 0;
    public static final int MARGIN_TYPE_RIGHT = 1;
    public static final int MARGIN_TYPE_TOP = 2;
    public static final int MARGIN_TYPE_BOTTOM = 3;

    private static final int DEFAULT_TRIANGLE_WIDTH = 12; // dp
    private static final int DEFAULT_TRIANGLE_HEIGHT = 8; // dp
    private static final int DEFAULT_TRIANGLE_MARGIN = 0; // dp
    private static final int DEFAULT_TRIANGLE_COLOR = Color.WHITE;
    private static final int DEFAULT_TRIANGLE_POSITION = POSITION_TOP;
    private static final int DEFAULT_TRIANGLE_MARGIN_TYPE = MARGIN_TYPE_LEFT;

    private float triangleWidth;
    private float triangleHeight;
    private float triangleMargin;
    private int trianglePosition;
    private int triangleMarginType;
    private int indicateColor;
    private float radius;

    private Paint paint;
    private Path path;
    private ShapeDrawable shapeDrawable;

    public PLVTriangleIndicateLayout(Context context) {
        super(context);
        init();
    }

    public PLVTriangleIndicateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAttrs = attrs;
        init();
    }

    public PLVTriangleIndicateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAttrs = attrs;
        init();
    }

    private void init() {
        TypedArray typedArray = getContext().obtainStyledAttributes(mAttrs, R.styleable.PLVTriangleIndicateLayout);

        triangleWidth = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleWidth, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_WIDTH));
        triangleHeight = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleHeight, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_HEIGHT));
        triangleMargin = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleMargin, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_MARGIN));
        trianglePosition = typedArray.getInteger(R.styleable.PLVTriangleIndicateTextView_trianglePosition, DEFAULT_TRIANGLE_POSITION);
        triangleMarginType = typedArray.getInteger(R.styleable.PLVTriangleIndicateTextView_triangleMarginType, DEFAULT_TRIANGLE_MARGIN_TYPE);
        indicateColor = typedArray.getColor(R.styleable.PLVTriangleIndicateTextView_indicateColor, DEFAULT_TRIANGLE_COLOR);
        radius = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_rectRadius, 0);

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (trianglePosition == POSITION_TOP || trianglePosition == POSITION_BOTTOM) {
            // 三角指示在上下
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int childHeightSize = (int) (heightSize - triangleHeight);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, heightMode);

            super.onMeasure(widthMeasureSpec, childHeightMeasureSpec);

            int height = (int) (getMeasuredHeight() + triangleHeight);
            setMeasuredDimension(getMeasuredWidth(), height);
        } else {
            // 三角指示在左右
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);

            int childWidthSize = (int) (widthSize - triangleHeight);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, widthMode);

            super.onMeasure(childWidthMeasureSpec, heightMeasureSpec);

            int width = (int) (getMeasuredWidth() + triangleHeight);
            setMeasuredDimension(width, getMeasuredHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
        initBackground();
        setBackground(shapeDrawable);
    }

    /**
     * @see FrameLayout#onLayout(boolean, int, int, int, int)
     */
    @SuppressLint("NewApi")
    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        int parentLeft = getPaddingLeft();
        if (trianglePosition == POSITION_LEFT) {
            parentLeft += triangleHeight;
        }
        int parentRight = right - left - getPaddingRight();
        if (trianglePosition == POSITION_RIGHT) {
            parentRight -= triangleHeight;
        }

        int parentTop = getPaddingTop();
        if (trianglePosition == POSITION_TOP) {
            parentTop += triangleHeight;
        }
        int parentBottom = bottom - top - getPaddingBottom();
        if (trianglePosition == POSITION_BOTTOM) {
            parentBottom -= triangleHeight;
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.START;
                }

                final int layoutDirection = getLayoutDirection();
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    private void initBackground() {
        Point first = new Point();
        Point second = new Point();
        Point third = new Point();
        if (trianglePosition == POSITION_TOP) {
            int leftBottomX = 0;
            if (triangleMarginType == MARGIN_TYPE_LEFT) {
                leftBottomX = (int) triangleMargin;
            } else if (triangleMarginType == MARGIN_TYPE_RIGHT) {
                leftBottomX = (int) (getMeasuredWidth() - triangleMargin - triangleWidth);
            }
            // leftBottom
            first = new Point(leftBottomX, (int) triangleHeight);
            // rightBottom
            second = new Point((int) (leftBottomX + triangleWidth), (int) triangleHeight);
            // top
            third = new Point((int) (leftBottomX + triangleWidth / 2), 0);
        } else if (trianglePosition == POSITION_BOTTOM) {
            int leftTopX = 0;
            if (triangleMarginType == MARGIN_TYPE_LEFT) {
                leftTopX = (int) triangleMargin;
            } else if (triangleMarginType == MARGIN_TYPE_RIGHT) {
                leftTopX = (int) (getMeasuredWidth() - triangleMargin - triangleWidth);
            }
            // leftTop
            first = new Point(leftTopX, (int) (getMeasuredHeight() - triangleHeight));
            // rightTop
            second = new Point((int) (leftTopX + triangleWidth), (int) (getMeasuredHeight() - triangleHeight));
            // bottom
            third = new Point((int) (leftTopX + triangleWidth / 2), getMeasuredHeight());
        } else if (trianglePosition == POSITION_LEFT) {
            int topY = 0;
            if (triangleMarginType == MARGIN_TYPE_TOP) {
                topY = (int) triangleMargin;
            } else if (triangleMarginType == MARGIN_TYPE_BOTTOM) {
                topY = (int) (getMeasuredHeight() - triangleMargin - triangleWidth);
            }
            // left
            first = new Point(0, (int) (topY + triangleWidth / 2));
            // top
            second = new Point((int) triangleHeight, topY);
            // bottom
            third = new Point((int) triangleHeight, (int) (topY + triangleWidth));
        } else if (trianglePosition == POSITION_RIGHT) {
            int topY = 0;
            if (triangleMarginType == MARGIN_TYPE_TOP) {
                topY = (int) triangleMargin;
            } else if (triangleMarginType == MARGIN_TYPE_BOTTOM) {
                topY = (int) (getMeasuredHeight() - triangleMargin - triangleWidth);
            }
            // top
            first = new Point((int) (getMeasuredWidth() - triangleHeight), topY);
            // right
            second = new Point(getMeasuredWidth(), (int) (topY + triangleWidth / 2));
            // bottom
            third = new Point((int) (getMeasuredWidth() - triangleHeight), (int) (topY + triangleWidth));
        }

        RectF roundRect;
        if (trianglePosition == POSITION_TOP) {
            roundRect = new RectF(0, triangleHeight, getMeasuredWidth(), getMeasuredHeight());
        } else if (trianglePosition == POSITION_BOTTOM) {
            roundRect = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight() - triangleHeight);
        } else if (trianglePosition == POSITION_LEFT) {
            roundRect = new RectF(triangleHeight, 0, getMeasuredWidth(), getMeasuredHeight());
        } else if (trianglePosition == POSITION_RIGHT) {
            roundRect = new RectF(0, 0, getMeasuredWidth() - triangleHeight, getMeasuredHeight());
        } else {
            roundRect = new RectF();
        }

        path = new Path();
        path.setFillType(Path.FillType.WINDING);
        path.addRoundRect(roundRect, radius, radius, Path.Direction.CCW);
        path.moveTo(first.x, first.y);
        path.lineTo(second.x, second.y);
        path.lineTo(third.x, third.y);
        path.close();

        shapeDrawable = new ShapeDrawable();
        PathShape pathShape = new PathShape(path, getMeasuredWidth(), getMeasuredHeight());
        shapeDrawable.setShape(pathShape);
        paint = shapeDrawable.getPaint();
        paint.setColor(indicateColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

}
