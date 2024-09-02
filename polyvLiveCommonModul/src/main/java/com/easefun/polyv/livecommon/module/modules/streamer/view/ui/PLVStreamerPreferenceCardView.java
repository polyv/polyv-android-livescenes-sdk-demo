package com.easefun.polyv.livecommon.module.modules.streamer.view.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVStreamerPreferenceCardView extends ConstraintLayout {

    private final Drawable strokeDrawable = getResources().getDrawable(R.drawable.plv_push_downgrade_preference_check_stroke);
    private final Drawable checkIconDrawable = getResources().getDrawable(R.drawable.plv_push_downgrade_preference_check_icon);
    private final float checkIconWidth = ConvertUtils.dp2px(28);
    private final float checkIconHeight = ConvertUtils.dp2px(32);

    private final float radius = ConvertUtils.dp2px(8);
    private final RectF viewSizeRect = new RectF();
    private final Paint backgroundPaint = new Paint();

    public PLVStreamerPreferenceCardView(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVStreamerPreferenceCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVStreamerPreferenceCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(PLVFormatUtils.parseColor("#0AF0F1F5"));
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (viewSizeRect.width() != getMeasuredWidth() || viewSizeRect.height() != getMeasuredHeight()) {
            viewSizeRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        if (isSelected()) {
            drawCheckIcon(canvas);
            drawStroke(canvas);
        }
        super.onDraw(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRoundRect(viewSizeRect, radius, radius, backgroundPaint);
    }

    private void drawCheckIcon(Canvas canvas) {
        checkIconDrawable.setBounds((int) (viewSizeRect.width() - checkIconWidth), (int) (viewSizeRect.height() - checkIconHeight), (int) viewSizeRect.width(), (int) viewSizeRect.height());
        checkIconDrawable.draw(canvas);
    }

    private void drawStroke(Canvas canvas) {
        strokeDrawable.setBounds(0, 0, (int) viewSizeRect.width(), (int) viewSizeRect.height());
        strokeDrawable.draw(canvas);
    }

}
