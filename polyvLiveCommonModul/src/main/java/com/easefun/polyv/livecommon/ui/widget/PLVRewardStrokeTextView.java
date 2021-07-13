package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * date: 2019-12-06
 * author: hwj
 * description: 字体描边
 */
public class PLVRewardStrokeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private TextPaint strokePaint;

    private Context context;

    public PLVRewardStrokeTextView(Context context) {
        this(context, null);
    }

    public PLVRewardStrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVRewardStrokeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Helvetica-Bold-Oblique_22454.ttf"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //自定义的字体： Helvetica-Bold-Oblique_22454.ttf 。用wrap_content会导致宽度不够，在这里补足10像素，发现刚好。
//        int width = getMeasuredWidth() + 10;
//        setMeasuredDimension(width, getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (strokePaint == null) {
            strokePaint = new TextPaint();
        }
        //复制原来TextViewg画笔中的一些参数
        TextPaint paint = getPaint();
        strokePaint.set(paint);

        //自定义描边效果
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(4);

        String text = getText().toString();

        //在文本底层画出带描边的文本
        canvas.drawText(text, getPaddingLeft(),
                getBaseline(), strokePaint);
        super.onDraw(canvas);
    }
}