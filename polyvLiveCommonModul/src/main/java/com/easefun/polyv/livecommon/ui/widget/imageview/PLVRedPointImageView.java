package com.easefun.polyv.livecommon.ui.widget.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.easefun.polyv.livecommon.R;

/**
 * @author Hoshiiro
 */
public class PLVRedPointImageView extends AppCompatImageView {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final int POS_LEFT_TOP = 0;
    private static final int POS_RIGHT_TOP = 1;
    private static final int POS_RIGHT_BOTTOM = 2;
    private static final int POS_LEFT_BOTTOM = 3;

    private int redPointPos = POS_LEFT_TOP;
    @Px
    private int redPointMarginHorizontal = 0;
    @Px
    private int redPointMarginVertical = 0;
    @Px
    private int redPointRadius = 0;
    @ColorInt
    private int redPointColor = Color.TRANSPARENT;
    private boolean drawRedPoint = false;

    private final RectF redPointRect = new RectF();
    private final Paint redPointPaint = new Paint();

    private int lastMeasureWidth;
    private int lastMeasureHeight;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVRedPointImageView(Context context) {
        super(context);
        init(null);
    }

    public PLVRedPointImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVRedPointImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void init(@Nullable AttributeSet attrs) {
        parseAttrs(attrs);
        updateRedPointPaint();

        redPointPaint.setAntiAlias(true);
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVRedPointImageView);

        redPointPos = typedArray.getInt(R.styleable.PLVRedPointImageView_plvRedPointPos, redPointPos);
        redPointMarginHorizontal = typedArray.getDimensionPixelSize(R.styleable.PLVRedPointImageView_plvRedPointMarginHorizontal, redPointMarginHorizontal);
        redPointMarginVertical = typedArray.getDimensionPixelSize(R.styleable.PLVRedPointImageView_plvRedPointMarginVertical, redPointMarginVertical);
        redPointRadius = typedArray.getDimensionPixelSize(R.styleable.PLVRedPointImageView_plvRedPointRadius, redPointRadius);
        redPointColor = typedArray.getColor(R.styleable.PLVRedPointImageView_plvRedPointColor, redPointColor);
        drawRedPoint = typedArray.getBoolean(R.styleable.PLVRedPointImageView_plvDrawRedPoint, drawRedPoint);

        typedArray.recycle();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="测量绘制">

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (lastMeasureWidth != getMeasuredWidth() || lastMeasureHeight != getMeasuredHeight()) {
            lastMeasureWidth = getMeasuredWidth();
            lastMeasureHeight = getMeasuredHeight();
            updateRedPointRect();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawRedPoint) {
            canvas.drawOval(redPointRect, redPointPaint);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    public void setRedPointColor(@ColorInt int redPointColor) {
        this.redPointColor = redPointColor;
        updateRedPointPaint();
        invalidate();
    }

    public void setDrawRedPoint(boolean drawRedPoint) {
        this.drawRedPoint = drawRedPoint;
        invalidate();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理方法">

    private void updateRedPointRect() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        int centerX = 0;
        int centerY = 0;

        switch (redPointPos) {
            case POS_LEFT_TOP:
                centerX = redPointMarginHorizontal + redPointRadius;
                centerY = redPointMarginVertical + redPointRadius;
                break;
            case POS_RIGHT_TOP:
                centerX = width - redPointMarginHorizontal - redPointRadius;
                centerY = redPointMarginVertical + redPointRadius;
                break;
            case POS_RIGHT_BOTTOM:
                centerX = width - redPointMarginHorizontal - redPointRadius;
                centerY = height - redPointMarginVertical - redPointRadius;
                break;
            case POS_LEFT_BOTTOM:
                centerX = redPointMarginHorizontal + redPointRadius;
                centerY = height - redPointMarginVertical - redPointRadius;
                break;
            default:
        }

        redPointRect.set(centerX - redPointRadius, centerY - redPointRadius, centerX + redPointRadius, centerY + redPointRadius);
    }

    private void updateRedPointPaint() {
        redPointPaint.setColor(redPointColor);
    }

    // </editor-fold>
}
