package com.easefun.polyv.livehiclass.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 音量view
 */
public class PLVHCVolumeView extends View {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private Paint paint;
    private RectF rectF;
    private int max = 17;
    private int progress;
    private int itemInterval = ConvertUtils.dp2px(6);
    private int itemWidth = ConvertUtils.dp2px(2);
    private int itemDistance = itemInterval + itemWidth;
    private int itemHeight = ConvertUtils.dp2px(14);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCVolumeView(Context context) {
        this(context, null);
    }

    public PLVHCVolumeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCVolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setMax(int max) {
        if (this.max == max) {
            return;
        }
        this.max = max;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setProgress(int progress) {
        progress = Math.min(progress, max);
        if (this.progress == progress) {
            return;
        }
        this.progress = progress;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 重写View的方法">
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < max; i++) {
            draw(canvas, Color.parseColor("#767676"), i);
        }
        for (int i = 0; i < progress; i++) {
            draw(canvas, Color.parseColor("#00B16C"), i);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void draw(Canvas canvas, int color, int position) {
        paint.setColor(color);
        rectF.left = position * itemDistance;
        rectF.right = position * itemDistance + itemWidth;
        rectF.top = 0;
        rectF.bottom = itemHeight;
        canvas.drawRoundRect(rectF, itemWidth, itemWidth, paint);
    }
    // </editor-fold>
}
