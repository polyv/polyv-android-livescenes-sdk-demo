package com.easefun.polyv.livecommon.module.utils.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import android.text.style.ReplacementSpan;

/**
 * @author Hoshiiro
 */
public class PLVCenterVerticalAbsoluteSizeSpan extends ReplacementSpan {

    @Px
    private final int textSize;

    public PLVCenterVerticalAbsoluteSizeSpan(@Px int textSize) {
        this.textSize = textSize;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return (int) getReplacementPaint(paint).measureText(text, start, end);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        final Paint replacementPaint = getReplacementPaint(paint);
        Paint.FontMetrics fontMetrics = replacementPaint.getFontMetrics();
        final int replaceY = (int) ((top + bottom) / 2F - (fontMetrics.ascent + fontMetrics.descent) / 2F);
        canvas.drawText(text, start, end, x, replaceY, replacementPaint);
    }

    private Paint getReplacementPaint(Paint src) {
        Paint paint = new Paint(src);
        paint.setTextSize(textSize);
        return paint;
    }

}
