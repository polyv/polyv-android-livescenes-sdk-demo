package com.easefun.polyv.livecommon.module.modules.streamer.view.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVStreamerPreferenceCardView extends ConstraintLayout {

    private final Drawable checkIconDrawable = getResources().getDrawable(R.drawable.plv_streamer_preference_card_check_icon);

    private float checkIconWidth = ConvertUtils.dp2px(28);
    private float checkIconHeight = ConvertUtils.dp2px(32);
    private float radius = ConvertUtils.dp2px(8);

    private final RectF viewSizeRect = new RectF();
    private final Paint backgroundPaint = new Paint();
    private final Paint strokePaint = new Paint();

    public PLVStreamerPreferenceCardView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public PLVStreamerPreferenceCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PLVStreamerPreferenceCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        setWillNotDraw(false);
        initAttrs(attrs);

        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(PLVFormatUtils.parseColor("#0AF0F1F5"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        strokePaint.setAntiAlias(true);
        strokePaint.setColor(PLVFormatUtils.parseColor("#4399FF"));
        strokePaint.setStrokeWidth(ConvertUtils.dp2px(1));
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVStreamerPreferenceCardView);
        checkIconWidth = typedArray.getDimension(R.styleable.PLVStreamerPreferenceCardView_plv_check_icon_width, checkIconWidth);
        checkIconHeight = typedArray.getDimension(R.styleable.PLVStreamerPreferenceCardView_plv_check_icon_height, checkIconHeight);
        radius = typedArray.getDimension(R.styleable.PLVStreamerPreferenceCardView_plv_radius, radius);
        typedArray.recycle();
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
        viewSizeRect.inset(strokePaint.getStrokeWidth() / 2, strokePaint.getStrokeWidth() / 2);
        canvas.drawRoundRect(viewSizeRect, radius, radius, strokePaint);
        viewSizeRect.inset(-strokePaint.getStrokeWidth() / 2, -strokePaint.getStrokeWidth() / 2);
    }

}
