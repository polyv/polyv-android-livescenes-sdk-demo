package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;

/**
 * @author suhongtao
 */
public class PLVTriangleIndicateTextView extends AppCompatTextView {

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
    private int indicateEndColor = -1;
    private boolean triangleCenter;
    private float radius;

    private Paint paint;
    private Path path;

    public PLVTriangleIndicateTextView(Context context) {
        super(context);
        init();
    }

    public PLVTriangleIndicateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAttrs = attrs;
        init();
    }

    public PLVTriangleIndicateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAttrs = attrs;
        init();
    }

    private void init() {
        TypedArray typedArray = getContext().obtainStyledAttributes(mAttrs, R.styleable.PLVTriangleIndicateTextView);

        triangleWidth = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleWidth, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_WIDTH));
        triangleHeight = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleHeight, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_HEIGHT));
        triangleMargin = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_triangleMargin, DpOrPxUtils.dip2px(getContext(), DEFAULT_TRIANGLE_MARGIN));
        trianglePosition = typedArray.getInteger(R.styleable.PLVTriangleIndicateTextView_trianglePosition, DEFAULT_TRIANGLE_POSITION);
        triangleMarginType = typedArray.getInteger(R.styleable.PLVTriangleIndicateTextView_triangleMarginType, DEFAULT_TRIANGLE_MARGIN_TYPE);
        indicateColor = typedArray.getColor(R.styleable.PLVTriangleIndicateTextView_indicateColor, DEFAULT_TRIANGLE_COLOR);
        indicateEndColor = typedArray.getColor(R.styleable.PLVTriangleIndicateTextView_indicateEndColor, -1);
        triangleCenter = typedArray.getBoolean(R.styleable.PLVTriangleIndicateTextView_triangleCenter, false);
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
    protected void onDraw(Canvas canvas) {
        if (paint == null || path == null) {
            initBackground();
        }
        canvas.drawPath(path, paint);
        if (trianglePosition == POSITION_TOP) {
            canvas.translate(0, triangleHeight);
        } else if (trianglePosition == POSITION_LEFT) {
            canvas.translate(triangleHeight, 0);
        }
        super.onDraw(canvas);
    }

    public void setColor(int indicateColor, int indicateEndColor) {
        this.indicateColor = indicateColor;
        this.indicateEndColor = indicateEndColor;
        paint = null;
        path = null;
        invalidate();
    }

    public int getTrianglePosition() {
        return trianglePosition;
    }

    private void initBackground() {
        paint = new Paint();
        paint.setColor(indicateColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(indicateEndColor == -1);

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
            if (triangleCenter) {
                leftBottomX = (int) (getMeasuredWidth() / 2 - triangleWidth / 2);
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
            if (triangleCenter) {
                leftTopX = (int) (getMeasuredWidth() / 2 - triangleWidth / 2);
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
            if (triangleCenter) {
                topY = (int) (getMeasuredHeight() / 2 - triangleWidth / 2);
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
            if (triangleCenter) {
                topY = (int) (getMeasuredHeight() / 2 - triangleWidth / 2);
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

        if (indicateEndColor != -1) {
            LinearGradient linearGradient = new LinearGradient(0, roundRect.bottom, roundRect.right, 0, indicateColor, indicateEndColor, Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);
        }

        path = new Path();
        path.setFillType(Path.FillType.WINDING);
        path.addRoundRect(roundRect, radius, radius, Path.Direction.CCW);
        path.moveTo(first.x, first.y);
        path.lineTo(second.x, second.y);
        path.lineTo(third.x, third.y);
        path.close();
    }

}
