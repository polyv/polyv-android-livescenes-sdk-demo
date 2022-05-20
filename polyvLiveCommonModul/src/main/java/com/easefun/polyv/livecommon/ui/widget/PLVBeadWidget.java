package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * date: 2019-12-04
 * author: hwj
 * description: 珠子自定义布局，可以用来作为ViewPager底部的指示器
 */
public class PLVBeadWidget extends View {
    private int beadCount;
    private int beadRadius;
    private int beadMargin;

    private Paint paintForSelected;
    private Paint paintForUnSelected;

    private int curSelectedIndex;

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVBeadWidget(Context context) {
        this(context, null);
    }

    public PLVBeadWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVBeadWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    // </editor-fold>

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PolyvBeadWidget);
        beadMargin = (int) ta.getDimension(R.styleable.PolyvBeadWidget_bead_margin, 30);
        beadRadius = (int) ta.getDimension(R.styleable.PolyvBeadWidget_bead_radius, 10);
        int colorForSelectedBead = ta.getColor(R.styleable.PolyvBeadWidget_selected_bead_color, Color.BLACK);
        int colorForUnselectedBead = ta.getColor(R.styleable.PolyvBeadWidget_unselected_bead_color, Color.BLUE);
        ta.recycle();


        paintForSelected = new Paint();
        paintForSelected.setColor(colorForSelectedBead);
        paintForSelected.setAntiAlias(true);

        paintForUnSelected = new Paint();
        paintForUnSelected.setColor(colorForUnselectedBead);
        paintForSelected.setAntiAlias(true);
    }

    /**
     * 设置珠子数量，一般用ViewPager的item count来设置
     *
     * @param beadCount 珠子数量
     */
    public void setBeadCount(int beadCount) {
        this.beadCount = beadCount;
        invalidate();
    }

    /**
     * 设置珠子半径
     *
     * @param beadRadius 半径 ，dp
     */
    public void setBeadRadius(int beadRadius) {
        this.beadRadius = ConvertUtils.dp2px(beadRadius);
    }


    /**
     * 设置珠子之间的边距
     *
     * @param beadMargin 边距 ， dp
     */
    public void setBeadMargin(int beadMargin) {
        this.beadMargin = ConvertUtils.dp2px(beadMargin);
    }

    /**
     * 设置当前选中的珠子的下标
     *
     * @param index 下标
     */
    public void setCurrentSelectedIndex(int index) {
        curSelectedIndex = index;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //下一个要画的圆的x坐标
        float nextBeadCX = beadRadius + paintForUnSelected.getStrokeWidth();
        //下一个要画的圆的y坐标
        float nextBeadCY = beadRadius + paintForUnSelected.getStrokeWidth();

        if (curSelectedIndex == 0) {
            canvas.drawCircle(nextBeadCX, nextBeadCY, beadRadius, paintForSelected);
        } else {
            canvas.drawCircle(nextBeadCX, nextBeadCY, beadRadius, paintForUnSelected);
        }


        for (int i = 1; i < beadRadius; i++) {
            //半径+画笔宽度+偏移+画笔宽度+半径
            nextBeadCX += beadRadius + paintForUnSelected.getStrokeWidth() +
                    beadMargin + paintForUnSelected.getStrokeWidth() + beadRadius;

            if (i == curSelectedIndex) {
                canvas.drawCircle(nextBeadCX, nextBeadCY, beadRadius, paintForSelected);
            } else {
                canvas.drawCircle(nextBeadCX, nextBeadCY, beadRadius, paintForUnSelected);
            }
        }
    }

    //计算自身宽度
    private int getSelfWidth() {
        int width = 0;

        for (int i = 0; i < beadCount; i++) {
            width += beadRadius * 2 + paintForUnSelected.getStrokeWidth() * 2;

            //如果不是最后一个bead，累加距离
            if (i != beadCount - 1) {
                width += beadMargin;
            }
        }
        return width;
    }

    //计算自身高度
    private int getSelfHeight() {
        return (int) (2 * beadRadius + 2 * paintForUnSelected.getStrokeWidth());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getSelfWidth(), getSelfHeight());
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getSelfWidth(), height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, getSelfHeight());
        }
    }
}
