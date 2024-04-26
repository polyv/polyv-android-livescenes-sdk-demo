package com.easefun.polyv.liveecommerce.scenes.fragments.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 空白tab布局
 */
public class PLVECBlackTabLayout extends FrameLayout {
    private Paint paint;
    private float r;
    private int childWidth;
    private int childPadding;
    private int position;
    private float positionOffset;
    private RectF rectFFront;
    private RectF rectFBack;

    public PLVECBlackTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECBlackTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECBlackTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setWillNotDraw(false);
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_widget_blank_tab_layout, this);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        r = ConvertUtils.dp2px(20);
        childWidth = ConvertUtils.dp2px(38);
        childPadding = ConvertUtils.dp2px(4);
        rectFFront = new RectF();
        rectFBack = new RectF();
    }

    public void bindViewPager(@NonNull ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                PLVECBlackTabLayout.this.position = position;
                PLVECBlackTabLayout.this.positionOffset = positionOffset;
                invalidate();
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float top = 0;
        float bottom = getHeight();
        if (positionOffset == 0) {
            float left = position * (childWidth + childPadding);
            float right = left + childWidth;
            rectFFront.set(left, top, right, bottom);
            canvas.drawRoundRect(rectFFront, r, r, paint);
        } else {
            float leftFront = position * (childWidth + childPadding) + positionOffset * childWidth;
            float rightFront = position * (childWidth + childPadding) + childWidth;
            rectFFront.set(leftFront, top, rightFront, bottom);
            canvas.drawRoundRect(rectFFront, r, r, paint);
            float leftBack = (position + 1) * (childWidth + childPadding);
            float rightBack = leftBack + positionOffset * childWidth;
            rectFBack.set(leftBack, top, rightBack, bottom);
            canvas.drawRoundRect(rectFBack, r, r, paint);
        }
    }
}
