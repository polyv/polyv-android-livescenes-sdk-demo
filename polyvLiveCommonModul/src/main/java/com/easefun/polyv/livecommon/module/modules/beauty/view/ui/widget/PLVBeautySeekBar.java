package com.easefun.polyv.livecommon.module.modules.beauty.view.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVBeautySeekBar extends AppCompatSeekBar {

    private final int splitTrackMargin = ConvertUtils.dp2px(2);
    private final int indicatorRadius = ConvertUtils.dp2px(2);
    private final float shadowWidth = ConvertUtils.dp2px(0.5F);
    private final int shadowRadius = ConvertUtils.dp2px(3);
    private final Paint shadowPaint = new Paint() {{
        setColor(Color.TRANSPARENT);
        setAntiAlias(true);
        setShadowLayer(shadowWidth, 0, 0, PLVFormatUtils.parseColor("#66000000"));
        setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }};
    private final float[] progressShadowRadii = {shadowRadius, shadowRadius, 0, 0, 0, 0, shadowRadius, shadowRadius};
    private final float[] backgroundShadowRadii = {0, 0, shadowRadius, shadowRadius, shadowRadius, shadowRadius, 0, 0};

    private final Rect thumbRectWithSplitTrackMargin = new Rect();
    private final RectF shadowRect = new RectF();
    private final Path shadowPath = new Path();

    private int indicatorProgress = -1;
    private boolean drawShadow = true;

    public PLVBeautySeekBar(Context context) {
        super(context);
        initView(null);
    }

    public PLVBeautySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public PLVBeautySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(@Nullable AttributeSet attributeSet) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        parseAttrs(attributeSet);
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVBeautySeekBar);

        drawShadow = typedArray.getBoolean(R.styleable.PLVBeautySeekBar_plvDrawShadow, drawShadow);

        typedArray.recycle();
    }

    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        requestLayout();
    }

    public void setIndicatorProgress(int indicatorProgress) {
        this.indicatorProgress = indicatorProgress;
        invalidate();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int measuredHeight = getMeasuredHeight();
        if (drawShadow
                && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
                && measuredHeight + shadowWidth * 2 < MeasureSpec.getSize(heightMeasureSpec)) {
            setMeasuredDimension(getMeasuredWidth(), (int) (measuredHeight + shadowWidth * 2));
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (getThumb() == null) {
            super.onDraw(canvas);
            return;
        }

        final int saveCount = canvas.save();

        splitTrackAndNotDrawThumb(canvas);
        super.onDraw(canvas);

        canvas.restoreToCount(saveCount);

        drawTrackShadow(canvas);
        drawIndicator(canvas);
        drawThumb(canvas);
    }

    private void splitTrackAndNotDrawThumb(Canvas canvas) {
        canvas.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
        final Rect thumbRect = getThumb().getBounds();
        thumbRectWithSplitTrackMargin.set(thumbRect.left - splitTrackMargin, thumbRect.top, thumbRect.right + splitTrackMargin, thumbRect.bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutRect(thumbRectWithSplitTrackMargin);
        } else {
            canvas.clipRect(thumbRectWithSplitTrackMargin, Region.Op.DIFFERENCE);
        }
        canvas.translate(-(getPaddingLeft() - getThumbOffset()), -getPaddingTop());
    }

    private void drawTrackShadow(Canvas canvas) {
        if (!drawShadow) {
            return;
        }
        final Drawable d = getProgressDrawable();
        if (d == null) {
            return;
        }

        final int saveCount = canvas.save();

        canvas.translate(getPaddingLeft(), getPaddingTop());

        final Rect drawableBounds = d.getBounds();

        // left progress shadow
        shadowRect.set(drawableBounds.left - shadowWidth, drawableBounds.top - shadowWidth, thumbRectWithSplitTrackMargin.left - getThumbOffset() + shadowWidth, drawableBounds.bottom + shadowWidth);
        if (shadowRect.right > shadowRect.left) {
            shadowPath.reset();
            shadowPath.addRoundRect(shadowRect, progressShadowRadii, Path.Direction.CCW);
            canvas.drawPath(shadowPath, shadowPaint);
        }

        // right background shadow
        shadowRect.set(thumbRectWithSplitTrackMargin.right - getThumbOffset() - shadowWidth, drawableBounds.top - shadowWidth, drawableBounds.right + shadowWidth, drawableBounds.bottom + shadowWidth);
        if (shadowRect.right > shadowRect.left) {
            shadowPath.reset();
            shadowPath.addRoundRect(shadowRect, backgroundShadowRadii, Path.Direction.CCW);
            canvas.drawPath(shadowPath, shadowPaint);
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawIndicator(Canvas canvas) {
        final int min = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getMin() : 0;
        if (indicatorProgress < min || indicatorProgress > getMax()) {
            return;
        }
        final float centerX = (getWidth() - getPaddingLeft() - getPaddingRight()) * (indicatorProgress - min) * 1F / (getMax() - min);
        final float centerY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2F;

        final int saveCount = canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        final int savePaintColor = shadowPaint.getColor();
        final Xfermode saveXfermode = shadowPaint.getXfermode();
        shadowPaint.setColor(Color.WHITE);
        shadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        canvas.drawCircle(centerX, centerY, indicatorRadius, shadowPaint);

        shadowPaint.setColor(savePaintColor);
        shadowPaint.setXfermode(saveXfermode);
        canvas.restoreToCount(saveCount);
    }

    private void drawThumb(Canvas canvas) {
        final int saveCount = canvas.save();
        canvas.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());

        // thumb
        getThumb().draw(canvas);
        // shadow
        if (drawShadow) {
            final Rect thumbBounds = getThumb().getBounds();
            shadowRect.set(thumbBounds.left - shadowWidth, thumbBounds.top - shadowWidth, thumbBounds.right + shadowWidth, thumbBounds.bottom + shadowWidth);
            canvas.drawOval(shadowRect, shadowPaint);
        }

        canvas.restoreToCount(saveCount);
    }
}
