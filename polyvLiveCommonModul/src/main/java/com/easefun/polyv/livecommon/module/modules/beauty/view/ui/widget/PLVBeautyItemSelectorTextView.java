package com.easefun.polyv.livecommon.module.modules.beauty.view.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVBeautyItemSelectorTextView extends AppCompatTextView {

    private final int unselectedTextColor = PLVFormatUtils.parseColor("#99F0F1F5");
    private final int selectedTextColor = PLVFormatUtils.parseColor("#F0F1F5");
    private final Paint selectedIndicatePaint = new Paint() {{
        setColor(PLVFormatUtils.parseColor("#3399FF"));
        setAntiAlias(true);
    }};

    private final int indicatorRadius = ConvertUtils.dp2px(2);

    private boolean canDrawIndicator = true;

    public PLVBeautyItemSelectorTextView(Context context) {
        super(context);
    }

    public PLVBeautyItemSelectorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVBeautyItemSelectorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setTextColor(selected ? selectedTextColor : unselectedTextColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        canDrawIndicator = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
                && getMeasuredHeight() + indicatorRadius * 2 <= MeasureSpec.getSize(heightMeasureSpec);
        if (canDrawIndicator) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + indicatorRadius * 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    private void drawIndicator(Canvas canvas) {
        if (!canDrawIndicator || !isSelected()) {
            return;
        }
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() - indicatorRadius;
        canvas.drawCircle(centerX, centerY, indicatorRadius, selectedIndicatePaint);
    }
}
