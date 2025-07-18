package com.easefun.polyv.livecommon.module.modules.reward.view.effect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * date: 2019-12-06
 * author: hwj
 * description: 字体描边
 */
public class PLVPointRewardStrokeTextView extends androidx.appcompat.widget.AppCompatTextView {

    private TextPaint strokePaint;

    private Context context;

    public PLVPointRewardStrokeTextView(Context context) {
        this(context, null);
    }

    public PLVPointRewardStrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPointRewardStrokeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (strokePaint == null) {
            strokePaint = new TextPaint();
        }
        //复制原来TextViewg画笔中的一些参数
        TextPaint paint = getPaint();
        strokePaint.setTextSize(paint.getTextSize());
        strokePaint.setTypeface(getTypeface());
        strokePaint.setFlags(paint.getFlags());
        strokePaint.setAlpha(paint.getAlpha());

        //自定义描边效果
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(ConvertUtils.dp2px(3));

        String text = getText().toString();

        //在文本底层画出带描边的文本
        canvas.drawText(text, getPaddingLeft(),
                getBaseline(), strokePaint);
        super.onDraw(canvas);
    }
}