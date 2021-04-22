package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
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
    public static final int MARGIN_TYPE_LEFT = 0;
    public static final int MARGIN_TYPE_RIGHT = 1;

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
        radius = typedArray.getDimension(R.styleable.PLVTriangleIndicateTextView_rectRadius, 0);

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childHeightSize = (int) (heightSize - triangleHeight);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, heightMode);

        super.onMeasure(widthMeasureSpec, childHeightMeasureSpec);

        int height = (int) (getMeasuredHeight() + triangleHeight);
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (paint == null || path == null) {
            initBackground();
        }
        canvas.drawPath(path, paint);
        if (trianglePosition == POSITION_TOP) {
            canvas.translate(0, triangleHeight);
        }
        super.onDraw(canvas);
    }

    private void initBackground() {
        paint = new Paint();
        paint.setColor(indicateColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Point first, second, third;
        if (trianglePosition == POSITION_TOP) {
            int leftBottomX;
            if (triangleMarginType == MARGIN_TYPE_LEFT) {
                leftBottomX = (int) triangleMargin;
            } else {
                // marginType == right
                leftBottomX = (int) (getMeasuredWidth() - triangleMargin - triangleWidth);
            }
            // leftBottom
            first = new Point(leftBottomX, (int) triangleHeight);
            // rightBottom
            second = new Point((int) (leftBottomX + triangleWidth), (int) triangleHeight);
            // top
            third = new Point((int) (leftBottomX + triangleWidth / 2), 0);
        } else {
            // position == bottom
            int leftTopX;
            if (triangleMarginType == MARGIN_TYPE_LEFT) {
                leftTopX = (int) triangleMargin;
            } else {
                // marginType == right
                leftTopX = (int) (getMeasuredWidth() - triangleMargin - triangleWidth);
            }
            // leftTop
            first = new Point(leftTopX, (int) (getMeasuredHeight() - triangleHeight));
            // rightTop
            second = new Point((int) (leftTopX + triangleWidth), (int) (getMeasuredHeight() - triangleHeight));
            // bottom
            third = new Point((int) (leftTopX + triangleWidth / 2), getMeasuredHeight());
        }

        RectF roundRect;
        if (trianglePosition == POSITION_TOP) {
            roundRect = new RectF(0, (int) triangleHeight, getMeasuredWidth(), getMeasuredHeight());
        } else {
            // triangle position == bottom
            roundRect = new RectF(0, 0, getMeasuredWidth(), (int) (getMeasuredHeight() - triangleHeight));
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
