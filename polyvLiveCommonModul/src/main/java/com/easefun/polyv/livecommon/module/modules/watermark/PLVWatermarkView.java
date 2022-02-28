package com.easefun.polyv.livecommon.module.modules.watermark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;

/**
 * author: fangfengrui
 * date: 2021/12/27
 */
public class PLVWatermarkView extends FrameLayout implements IPLVWatermarkView {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVWatermarkTextVO plvWatermarkTextVO = new PLVWatermarkTextVO();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVWatermarkView(Context context) {
        super(context);
        initView(context);
    }

    public PLVWatermarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PLVWatermarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public void initView(Context context) {
        PLVWatermarkDrawable plvWatermarkDrawable = new PLVWatermarkDrawable();
        plvWatermarkDrawable.plvWatermarkTextVO = plvWatermarkTextVO;
        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setBackground(plvWatermarkDrawable);
        addView(layout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外调用api">
    @Override
    public void setPLVWatermarkVO(PLVWatermarkTextVO plvWatermarkVO) {
        this.plvWatermarkTextVO = plvWatermarkVO;
    }

    @Override
    public void showWatermark() {
        initView(this.getContext());
    }

    @Override
    public void removeWatermark() {
        super.removeAllViews();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">
    private class PLVWatermarkDrawable extends Drawable {
        /**
         * 文字画笔
         */
        private Paint paint;
        /**
         * 描边用画笔
         */
        private Paint paintFilter;
        private PLVWatermarkTextVO plvWatermarkTextVO;
        private static final int CANVAS_COLOR_TRANSPARENT = 0x00000000;

        private PLVWatermarkDrawable() {
            paint = new Paint();
            paintFilter = new Paint();
        }

        //初始化画笔
        private void initPaint() {
            int textSize = PLVUIUtil.dip2px(getContext(), plvWatermarkTextVO.getFontSize());
            paint.setColor(Color.BLACK);
            paint.setTextSize(textSize);
            paint.setAlpha(100 - Integer.parseInt(plvWatermarkTextVO.getFontAlpha()));
            paintFilter.setColor(Color.WHITE);
            paintFilter.setStyle(Paint.Style.STROKE);
            paintFilter.setTextSize(textSize);
            paintFilter.setAlpha(100 - Integer.parseInt(plvWatermarkTextVO.getFontAlpha()));

            paint.setDither(true);
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paintFilter.setDither(true);
            paintFilter.setAntiAlias(true);
            paintFilter.setFilterBitmap(true);
            paintFilter.setStrokeWidth(1);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int textSize = PLVUIUtil.dip2px(getContext(), plvWatermarkTextVO.getFontSize());
            int width = getWidth();
            int height = getHeight();
            initPaint();
            //对角线
            int diagonal = (int) Math.sqrt(width * width + height * height);

            float textWidth = paint.measureText(plvWatermarkTextVO.getContent());

            canvas.drawColor(CANVAS_COLOR_TRANSPARENT);

            // 以对角线的长度来做高度，这样可以保证竖屏和横屏整个屏幕都能布满水印
            for (int positionY = -diagonal / 12; positionY <= diagonal; positionY += textSize * 4) {
                for (float positionX = 3; positionX < width; positionX += (textWidth + textSize * 2)) {
                    canvas.rotate(-15, positionX, positionY);
                    canvas.drawText(plvWatermarkTextVO.getContent(), positionX, positionY, paint);
                    canvas.drawText(plvWatermarkTextVO.getContent(), positionX, positionY, paintFilter);
                    canvas.rotate(15, positionX, positionY);
                }
            }

            canvas.save();
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

    }
    // </editor-fold>
}
