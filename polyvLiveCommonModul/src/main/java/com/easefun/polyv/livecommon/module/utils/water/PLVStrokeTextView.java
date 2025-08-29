package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

public class PLVStrokeTextView extends TextView {
    private final TextView outBackGroundText; // 外描边
    private final TextView backGroundText;    // 内描边
    private int outStrokeColor = Color.TRANSPARENT;
    private float outStrokeWidth = 0f;
    private int outOffsetY = 0;
    private int strokeColor = Color.TRANSPARENT;
    private float strokeWidth = 0f;
    private boolean hasDoubleStroke = false;
    private boolean hasStroke = false;
    private boolean firstSetPadding = true;
    private int style = 0;

    public PLVStrokeTextView(Context context) {
        this(context, null);
    }

    public PLVStrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVStrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        outBackGroundText = new TextView(context, attrs, defStyleAttr);
        backGroundText = new TextView(context, attrs, defStyleAttr);
    }

    public void setStyle(int style) {
        this.style = style;
        setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf"));
        setBackground(null);
        switch (style) {
            case 1:
                setDoubleStroke(0xFFBDCCFB, 22f, Color.WHITE, 14f, 4);
                setTextColor(0xFF3F76FC);
                break;
            case 2:
                setDoubleStroke(Color.WHITE, 22f, 0xFFF2344F, 14f, 0);
                setTextColor(Color.WHITE);
                break;
            case 3:
                setStroke(Color.TRANSPARENT, 0f);
                setTextColor(Color.BLACK);
                setBackgroundResource(R.drawable.plv_sticker_text_bg_1);
                break;
            case 4:
                setStroke(Color.TRANSPARENT, 0f);
                setTextColor(Color.BLACK);
                setBackgroundResource(R.drawable.plv_sticker_text_bg_2);
                break;
            case 5:
                setStroke(Color.BLACK, 6f);
                setTextColor(Color.WHITE);
                setShadowLayer(1, 4, 4, 0xFFD070A0);
                setBackgroundResource(R.drawable.plv_sticker_text_bg_3);
                break;
            case 6:
                setStroke(Color.TRANSPARENT, 0f);
                setTextColor(Color.BLACK);
                setBackgroundResource(R.drawable.plv_sticker_text_bg_4);
                break;
            case 7:
                setStroke(0xFF3F76FC, 6f);
                setTextColor(Color.WHITE);
                break;
            case 8:
                setStroke(Color.WHITE, 10f);
                setTextColor(0xFFEA444F);
                break;
        }
    }

    public void setStroke(int color, float width) {
        this.strokeColor = color;
        this.strokeWidth = width;
        this.hasStroke = width > 0f;
        this.hasDoubleStroke = false;
        invalidate();
    }

    @Override
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        if (hasDoubleStroke) {
            outBackGroundText.setShadowLayer(radius, dx, dy, color);
        } else if (hasStroke) {
            backGroundText.setShadowLayer(radius, dx, dy, color);
        } else {
            super.setShadowLayer(radius, dx, dy, color);
        }
    }

    public void setDoubleStroke(int outColor, float outWidth, int inColor, float inWidth, int outOffsetY) {
        this.outStrokeColor = outColor;
        this.outStrokeWidth = outWidth;
        this.strokeColor = inColor;
        this.strokeWidth = inWidth;
        this.outOffsetY = outOffsetY;
        this.hasDoubleStroke = true;
        invalidate();
    }

    private void setupPadding() {
        int extra = (int) (hasDoubleStroke ? outStrokeWidth : hasStroke ? strokeWidth : 0);
        boolean isMaxTextSize = getTextSize() >= ConvertUtils.sp2px(16);
        int left = extra;
        int right = extra;
        int top = extra / 2;
        int bottom = extra / 2;
        setGravity(Gravity.CENTER);
        if (style == 3) {
            left = left + ConvertUtils.dp2px(14);
            right = right + ConvertUtils.dp2px(4);
            bottom = bottom + ConvertUtils.dp2px(isMaxTextSize ? 1 : 0);
        } else if (style == 4) {
            setGravity(Gravity.LEFT | Gravity.TOP);
            left = left + ConvertUtils.dp2px(4);
            right = right + ConvertUtils.dp2px(14);
            if (isMaxTextSize) {
                bottom = bottom + ConvertUtils.dp2px(6);
            } else {
                top = top + ConvertUtils.dp2px(1);
            }
        } else if (style == 5) {
            left = left + ConvertUtils.dp2px(6);
            right = right + ConvertUtils.dp2px(4);
            bottom = bottom + ConvertUtils.dp2px(isMaxTextSize ? 2 : 1);
        } else if (style == 6) {
            left = left + ConvertUtils.dp2px(4);
            right = right + ConvertUtils.dp2px(4);
            bottom = bottom + ConvertUtils.dp2px(isMaxTextSize ? 6 : 5);
        }
        setPadding(left, top, right, bottom);
        backGroundText.setPadding(firstSetPadding ? 1 : 0, top, firstSetPadding ? 1 : 0, 0);
        outBackGroundText.setPadding(firstSetPadding ? 1 : 0, top + outOffsetY, firstSetPadding ? 1 : 0, 0);
        firstSetPadding = false;
    }

    private void drawStrokeText(TextView textView, int strokeColor, float strokeWidth, float maxStrokeWidth, int offsetY, Canvas canvas) {
        int left = 1;
        int right = 1;
        if (style == 5) {
            left = ConvertUtils.dp2px(6);
            right = ConvertUtils.dp2px(4);
        }
        textView.getPaint().setStrokeWidth(strokeWidth);
        textView.getPaint().setStyle(Paint.Style.STROKE);
        textView.setTextColor(strokeColor);
        textView.setGravity(getGravity());
        textView.setPadding(left, (int) ((maxStrokeWidth / 2) + offsetY), right, 0);
        textView.setTextSize(getTextSize() / getResources().getDisplayMetrics().scaledDensity);
        textView.setTypeface(getTypeface());
        textView.draw(canvas);
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        if (backGroundText != null) backGroundText.setTextSize(unit, size);
        if (outBackGroundText != null) outBackGroundText.setTextSize(unit, size);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        if (backGroundText != null) backGroundText.setLayoutParams(params);
        if (outBackGroundText != null) outBackGroundText.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setupPadding();
        boolean hasPostInvalidate = false;
        CharSequence txt = getText();
        if (backGroundText.getText() == null || !backGroundText.getText().equals(txt)) {
            backGroundText.setText(txt);
            hasPostInvalidate = true;
        }
        if (outBackGroundText.getText() == null || !outBackGroundText.getText().equals(txt)) {
            outBackGroundText.setText(txt);
            hasPostInvalidate = true;
        }
        if (hasPostInvalidate) {
            invalidate();
        }
        backGroundText.measure(widthMeasureSpec, heightMeasureSpec);
        outBackGroundText.measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        backGroundText.layout(left, top, right, bottom);
        outBackGroundText.layout(left, top, right, bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (hasDoubleStroke) {
            // 外描边
            drawStrokeText(outBackGroundText, outStrokeColor, outStrokeWidth, outStrokeWidth, outOffsetY, canvas);
            // 内描边
            drawStrokeText(backGroundText, strokeColor, strokeWidth, outStrokeWidth, 0, canvas);
            // 填充
            super.onDraw(canvas);
        } else if (hasStroke) {
            // 内描边
            drawStrokeText(backGroundText, strokeColor, strokeWidth, strokeWidth, 0, canvas);
            super.onDraw(canvas);
        } else {
            super.onDraw(canvas);
        }
    }
} 