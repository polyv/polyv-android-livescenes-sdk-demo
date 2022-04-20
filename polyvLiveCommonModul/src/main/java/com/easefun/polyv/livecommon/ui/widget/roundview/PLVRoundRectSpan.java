package com.easefun.polyv.livecommon.ui.widget.roundview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.text.style.ReplacementSpan;

import com.plv.foundationsdk.annos.Dp;
import com.plv.foundationsdk.annos.Sp;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVRoundRectSpan extends ReplacementSpan {

    @Px
    private int marginLeft = 0;
    @Px
    private int marginRight = 0;
    @Px
    private int paddingLeft = 0;
    @Px
    private int paddingRight = 0;
    @Px
    private int radius = 0;
    @ColorInt
    private int backgroundColor = Color.TRANSPARENT;
    @ColorInt
    private int textColor = Color.BLACK;
    @Px
    private int textSize = ConvertUtils.sp2px(12);

    public PLVRoundRectSpan() {

    }

    // <editor-fold defaultstate="collapsed" desc="setter">

    public PLVRoundRectSpan marginLeft(@Dp int marginLeft) {
        this.marginLeft = ConvertUtils.dp2px(marginLeft);
        return this;
    }

    public PLVRoundRectSpan marginRight(@Dp int marginRight) {
        this.marginRight = ConvertUtils.dp2px(marginRight);
        return this;
    }

    public PLVRoundRectSpan paddingLeft(@Dp int paddingLeft) {
        this.paddingLeft = ConvertUtils.dp2px(paddingLeft);
        return this;
    }

    public PLVRoundRectSpan paddingRight(@Dp int paddingRight) {
        this.paddingRight = ConvertUtils.dp2px(paddingRight);
        return this;
    }

    public PLVRoundRectSpan radius(@Dp int radius) {
        this.radius = ConvertUtils.dp2px(radius);
        return this;
    }

    public PLVRoundRectSpan backgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public PLVRoundRectSpan textColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public PLVRoundRectSpan textSize(@Sp int textSize) {
        this.textSize = ConvertUtils.sp2px(textSize);
        return this;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Span方法重写">

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        final float originalTextSize = paint.getTextSize();
        paint.setTextSize(textSize);
        final int paintMeasureWidth = (int) paint.measureText(text, start, end);
        paint.setTextSize(originalTextSize);

        return paintMeasureWidth + marginLeft + marginRight + paddingLeft + paddingRight;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        final int originalColor = paint.getColor();
        final float originalTextSize = paint.getTextSize();
        final float oriFontMetricsHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        final float oriCenter = (bottom - top) / 2F;

        paint.setTextSize(textSize);

        final float newFontMetricsHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        final float scaleFactor = newFontMetricsHeight / oriFontMetricsHeight;
        final float newTop = oriCenter - (oriCenter - top) * scaleFactor;
        final float newBottom = oriCenter + (bottom - oriCenter) * scaleFactor;

        // draw background
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(new RectF(x + marginLeft, newTop, x + marginLeft + paint.measureText(text, start, end) + paddingLeft + paddingRight, newBottom), radius, radius, paint);

        // draw text
        paint.setColor(textColor);
        canvas.drawText(text, start, end, x + marginLeft + paddingLeft, oriCenter + (y - oriCenter) * scaleFactor, paint);

        // restore original color
        paint.setColor(originalColor);
        paint.setTextSize(originalTextSize);
    }

    // </editor-fold>
}
