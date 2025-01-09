package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.easefun.polyv.livecommon.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVClipOutMaskView extends View {

    private final List<ClipOutParam> clipOutParams = new ArrayList<>();
    private final List<Path> clipOutPaths = new ArrayList<>();
    private int maskColor = Color.TRANSPARENT;

    public PLVClipOutMaskView(Context context) {
        this(context, null);
    }

    public PLVClipOutMaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVClipOutMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVClipOutMaskView, defStyleAttr, 0);
        int maskColor = typedArray.getColor(R.styleable.PLVClipOutMaskView_plv_mask_color, Color.TRANSPARENT);
        boolean consumeClick = typedArray.getBoolean(R.styleable.PLVClipOutMaskView_plv_mask_consume_click, true);
        typedArray.recycle();

        this.setMaskColor(maskColor);
        if (consumeClick) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public void clipOut(View anchor) {
        final ClipOutParam param = new ClipOutParam();
        param.anchor = anchor;
        clipOutParams.add(param);
        clipOutPaths.clear();
    }

    public void clipOutRoundRect(View anchor, @Px float radius) {
        final ClipOutParam param = new ClipOutParam();
        param.anchor = anchor;
        param.radiusTopLeft = radius;
        param.radiusTopRight = radius;
        param.radiusBottomLeft = radius;
        param.radiusBottomRight = radius;
        clipOutParams.add(param);
        clipOutPaths.clear();
    }

    public void clipOutRoundRect(View anchor, @Px float radiusTopLeft, float radiusTopRight, float radiusBottomLeft, float radiusBottomRight) {
        final ClipOutParam param = new ClipOutParam();
        param.anchor = anchor;
        param.radiusTopLeft = radiusTopLeft;
        param.radiusTopRight = radiusTopRight;
        param.radiusBottomLeft = radiusBottomLeft;
        param.radiusBottomRight = radiusBottomRight;
        clipOutParams.add(param);
        clipOutPaths.clear();
    }

    public void clearClipOutParams() {
        clipOutParams.clear();
        clipOutPaths.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] location = new int[2];
        for (ClipOutParam clipOutParam : clipOutParams) {
            clipOutParam.anchor.getLocationOnScreen(location);
            final int anchorX = location[0];
            final int anchorY = location[1];
            final int anchorWidth = clipOutParam.anchor.getWidth();
            final int anchorHeight = clipOutParam.anchor.getHeight();
            final RectF anchorRect = new RectF(anchorX, anchorY, anchorX + anchorWidth, anchorY + anchorHeight);
            if (anchorRect.contains(event.getRawX(), event.getRawY())) {
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void invalidate() {
        this.clipOutPaths.clear();
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        prepareClipOutPath();
        for (Path path : clipOutPaths) {
            canvas.clipPath(path, Region.Op.DIFFERENCE);
        }
        canvas.drawColor(this.maskColor);
    }

    private void prepareClipOutPath() {
        if (!clipOutPaths.isEmpty() || clipOutParams.isEmpty()) {
            return;
        }
        int[] location = new int[2];
        for (ClipOutParam clipOutParam : clipOutParams) {
            clipOutParam.anchor.getLocationOnScreen(location);
            final int anchorX = location[0];
            final int anchorY = location[1];
            final int anchorWidth = clipOutParam.anchor.getWidth();
            final int anchorHeight = clipOutParam.anchor.getHeight();
            this.getLocationOnScreen(location);
            final int maskX = location[0];
            final int maskY = location[1];
            final int offsetX = anchorX - maskX;
            final int offsetY = anchorY - maskY;
            final float radiusTopLeft = clipOutParam.radiusTopLeft;
            final float radiusTopRight = clipOutParam.radiusTopRight;
            final float radiusBottomLeft = clipOutParam.radiusBottomLeft;
            final float radiusBottomRight = clipOutParam.radiusBottomRight;

            final Path path = new Path();
            path.addRoundRect(
                    new RectF(offsetX, offsetY, offsetX + anchorWidth, offsetY + anchorHeight),
                    new float[]{radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, radiusBottomRight, radiusBottomRight, radiusBottomLeft, radiusBottomLeft},
                    Path.Direction.CCW
            );
            clipOutPaths.add(path);
        }
    }

    public static class ClipOutParam {
        View anchor;
        @Px
        float radiusTopLeft = 0F;
        @Px
        float radiusTopRight = 0F;
        @Px
        float radiusBottomLeft = 0F;
        @Px
        float radiusBottomRight = 0F;
    }

}
