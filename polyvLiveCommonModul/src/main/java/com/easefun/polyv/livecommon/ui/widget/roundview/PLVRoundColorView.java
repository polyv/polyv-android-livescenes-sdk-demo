package com.easefun.polyv.livecommon.ui.widget.roundview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.livecommon.R;

/**
 * @author suhongtao
 */
public class PLVRoundColorView extends View {

    private AttributeSet attributeSet;

    @Px
    private int mainRadius = 0;
    @ColorInt
    private int mainColor = Color.TRANSPARENT;
    @ColorInt
    private int backgroundColor = Color.TRANSPARENT;

    private int backgroundRadius;

    private final Paint mainPaint = new Paint();
    private final Paint backgroundPaint = new Paint();

    public PLVRoundColorView(Context context) {
        super(context);
        initView();
    }

    public PLVRoundColorView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        this.attributeSet = attrs;
        initView();
    }

    public PLVRoundColorView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attributeSet = attrs;
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        backgroundRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2F, getHeight() / 2F, backgroundRadius, backgroundPaint);
        canvas.drawCircle(getWidth() / 2F, getHeight() / 2F, mainRadius, mainPaint);
    }

    private void initView() {
        TypedArray ta = getContext().obtainStyledAttributes(attributeSet, R.styleable.PLVRoundColorView);

        mainRadius = ta.getDimensionPixelSize(R.styleable.PLVRoundColorView_plvMainRadius, 0);
        mainColor = ta.getColor(R.styleable.PLVRoundColorView_plvMainColor, Color.TRANSPARENT);
        backgroundColor = ta.getColor(R.styleable.PLVRoundColorView_plvBackgroundColor, Color.TRANSPARENT);

        ta.recycle();

        initPaint();
    }

    private void initPaint() {
        mainPaint.setColor(mainColor);
        mainPaint.setStyle(Paint.Style.FILL);
        mainPaint.setAntiAlias(true);

        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);
    }

    public void updateMainColor(@ColorInt int mainColor) {
        this.mainColor = mainColor;
        mainPaint.setColor(mainColor);
        invalidate();
    }

    public void updateBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundPaint.setColor(backgroundColor);
        invalidate();
    }

}
