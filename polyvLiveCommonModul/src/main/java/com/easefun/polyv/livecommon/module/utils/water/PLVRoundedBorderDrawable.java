package com.easefun.polyv.livecommon.module.utils.water;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public class PLVRoundedBorderDrawable extends Drawable {
    private final Paint mBorderPaint;
    private final Paint mImagePaint;
    private final RectF mBoundsRect = new RectF();
    private final RectF mBoundsRect2 = new RectF();
    private final float mCornerRadius;
    private final float mBorderWidth;
    private final float mBorderMargin;
    private Bitmap mBitmap;
    private boolean mShowBorder;

    public PLVRoundedBorderDrawable(Bitmap bitmap, float cornerRadius, int borderColor, float borderWidth, float borderMargin) {
        mBitmap = bitmap;
        mCornerRadius = cornerRadius;
        mBorderWidth = borderWidth;
        mBorderMargin = borderMargin;

        mImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mImagePaint.setFilterBitmap(true);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStrokeWidth(borderWidth);

        mShowBorder = true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBoundsRect.set(bounds);
        mBoundsRect2.set(
                bounds.left + mBorderMargin,
                bounds.top + mBorderMargin,
                bounds.right - mBorderMargin,
                bounds.bottom - mBorderMargin
        );
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // 绘制圆角图片
        canvas.save();
        Path path = new Path();
        path.addRoundRect(mBoundsRect2, mCornerRadius, mCornerRadius, Path.Direction.CW);
        canvas.clipPath(path);
        canvas.drawBitmap(mBitmap, null, mBoundsRect2, mImagePaint);
        canvas.restore();

        // 绘制边框
        if (mShowBorder) {
            RectF borderRect = new RectF(
                    mBoundsRect.left + mBorderWidth / 2,
                    mBoundsRect.top + mBorderWidth / 2,
                    mBoundsRect.right - mBorderWidth / 2,
                    mBoundsRect.bottom - mBorderWidth / 2
            );
            canvas.drawRoundRect(borderRect, mCornerRadius, mCornerRadius, mBorderPaint);
        }
    }

    public void setShowBorder(boolean show) {
        mShowBorder = show;
        invalidateSelf();
    }

    public boolean isShowBorder() {
        return mShowBorder;
    }

    // 其他必要方法
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mImagePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mImagePaint.setColorFilter(cf);
    }
}
