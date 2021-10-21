package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * @author suhongtao
 */
public class PLVAlignTopFillWidthImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();

    public PLVAlignTopFillWidthImageView(Context context) {
        super(context);
    }

    public PLVAlignTopFillWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVAlignTopFillWidthImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int screenWidth = ScreenUtils.getScreenWidth();
        int drawableWidth = getDrawable().getIntrinsicWidth();
        float scale = ((float) screenWidth) / drawableWidth;

        matrix.setScale(scale, scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.concat(matrix);
        getDrawable().draw(canvas);
    }
}
