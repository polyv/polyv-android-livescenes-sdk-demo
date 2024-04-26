package com.easefun.polyv.livestreamer.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 下划线在View底部居中
 * 右上角小红点宽高6dp，横向padding至少8dp
 *
 * @author Hoshiiro
 */
public class PLVLSMemberTabIndicateTextView extends AppCompatTextView {

    private static final int COLOR_TEXT_SELECTED = PLVFormatUtils.parseColor("#F0F1F5");
    private static final int COLOR_TEXT_NOT_SELECTED = PLVFormatUtils.parseColor("#CFD1D6");
    private static final int COLOR_BOTTOM_LINE_INDICATOR_SOLID = PLVFormatUtils.parseColor("#F0F1F5");
    private static final int COLOR_RED_POINT_INDICATOR_SOLID = PLVFormatUtils.parseColor("#FF6363");
    private static final int BOTTOM_LINE_INDICATOR_MAX_WIDTH = ConvertUtils.dp2px(32);
    private static final int BOTTOM_LINE_INDICATOR_HEIGHT = ConvertUtils.dp2px(2);
    private static final int BOTTOM_LINE_INDICATOR_RADIUS = ConvertUtils.dp2px(1);
    private static final int RED_POINT_RADIUS = ConvertUtils.dp2px(3);

    private final RectF bottomLineRectF = new RectF();
    private final Paint bottomLinePaint = new Paint() {{
        setStyle(Style.FILL);
        setAntiAlias(true);
        setColor(COLOR_BOTTOM_LINE_INDICATOR_SOLID);
    }};
    private final Paint redPointPaint = new Paint() {{
        setStyle(Style.FILL);
        setAntiAlias(true);
        setColor(COLOR_RED_POINT_INDICATOR_SOLID);
    }};

    private boolean drawBottomLineOnSelected = false;
    private boolean drawRedPoint = false;

    public PLVLSMemberTabIndicateTextView(Context context) {
        super(context);
    }

    public PLVLSMemberTabIndicateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLSMemberTabIndicateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setTextColor(isSelected() ? COLOR_TEXT_SELECTED : COLOR_TEXT_NOT_SELECTED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBottomLineIndicator(canvas);
        drawRedPoint(canvas);
    }

    public void setDrawBottomLineOnSelected(boolean drawBottomLineOnSelected) {
        this.drawBottomLineOnSelected = drawBottomLineOnSelected;
        invalidate();
    }

    public void showRedPoint(boolean show) {
        this.drawRedPoint = show;
        invalidate();
    }

    private void drawBottomLineIndicator(Canvas canvas) {
        final boolean needDraw = isSelected() && drawBottomLineOnSelected;
        if (!needDraw) {
            return;
        }

        int bottomLineIndicatorWidth = getWidth() - ConvertUtils.dp2px(16);
        if (bottomLineIndicatorWidth > BOTTOM_LINE_INDICATOR_MAX_WIDTH) {
            bottomLineIndicatorWidth = BOTTOM_LINE_INDICATOR_MAX_WIDTH;
        }
        if (bottomLineIndicatorWidth <= 0) {
            return;
        }

        bottomLineRectF.left = (getWidth() - bottomLineIndicatorWidth) / 2F;
        bottomLineRectF.top = getHeight() - BOTTOM_LINE_INDICATOR_HEIGHT;
        bottomLineRectF.right = bottomLineRectF.left + bottomLineIndicatorWidth;
        bottomLineRectF.bottom = getHeight();
        final int radius = BOTTOM_LINE_INDICATOR_RADIUS;
        canvas.drawRoundRect(bottomLineRectF, radius, radius, bottomLinePaint);
    }

    private void drawRedPoint(Canvas canvas) {
        if (!drawRedPoint) {
            return;
        }

        final int centerX = getWidth() - ConvertUtils.dp2px(1) - RED_POINT_RADIUS;
        final int centerY = ConvertUtils.dp2px(6) + RED_POINT_RADIUS;

        canvas.drawCircle(centerX, centerY, RED_POINT_RADIUS, redPointPaint);
    }
}
