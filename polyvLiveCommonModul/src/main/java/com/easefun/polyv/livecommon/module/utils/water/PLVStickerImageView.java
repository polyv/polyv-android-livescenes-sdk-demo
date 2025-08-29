package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

public class PLVStickerImageView extends ImageView implements IPLVToggleView {
    private PLVRoundedBorderDrawable mBorderDrawable;
    private Drawable mDrawable;
    private boolean mBorderVisible = true;
    private int mMaxSize;
    private boolean mUseNormalDrawable = false;
    private Drawable finishDrawable;
    private Rect finishRect = new Rect();
    private OnToggleBorderListener mOnToggleBorderListener;

    public PLVStickerImageView(Context context, int maxSizeDp) {
        super(context);
        init(maxSizeDp);
        finishDrawable = ContextCompat.getDrawable(context, R.drawable.plv_sticker_finish_ic);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isBorderVisible() && finishDrawable != null) {
            int iconSize = Math.min(ConvertUtils.dp2px(24), Math.min(getHeight() / 3, getWidth() / 3));
            int padding = ConvertUtils.dp2px(1);
            int left = getWidth() - iconSize - padding;
            int top = padding;
            int right = getWidth() - padding;
            int bottom = top + iconSize;
            finishRect.set(left, top, right, bottom);
            finishDrawable.setBounds(finishRect);
            finishDrawable.draw(canvas);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mOnToggleBorderListener != null && mOnToggleBorderListener.isSettingFinished()) {
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取父控件
                ViewGroup parent = (ViewGroup) getParent();
                if (parent == null) {
                    return;
                }
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                // 获取当前View的布局参数和尺寸
                LayoutParams params = getLayoutParams();
                if (params == null) {
                    return;
                }
                int viewWidth = params.width;
                int viewHeight = params.height;

                // 计算当前宽高比（假设宽高不为0）
                float ratio = (viewHeight != 0) ? (float) viewWidth / viewHeight : 1f;
                boolean needResize = false;

                // 如果View宽度超出父控件，则将宽度设置为父控件宽度，同时等比计算高度
                if (viewWidth > parentWidth) {
                    viewWidth = parentWidth;
                    viewHeight = (int) (viewWidth / ratio);
                    needResize = true;
                }
                // 如果View高度超出父控件，则将高度设置为父控件高度，同时等比计算宽度
                if (viewHeight > parentHeight) {
                    viewHeight = parentHeight;
                    viewWidth = (int) (viewHeight * ratio);
                    needResize = true;
                }
                if (needResize) {
                    params.width = viewWidth;
                    params.height = viewHeight;
                    setLayoutParams(params);
                }

                setX((parentWidth - viewWidth) / 2f);
                setY((parentHeight - viewHeight) / 2f);
            }
        }, 100);
    }

    private void init(int maxSizeDp) {
        mMaxSize = PLVDisplayUtils.dpToPx(getContext(), maxSizeDp);
        setScaleType(ScaleType.CENTER_CROP);
    }

    public void setImageWithBorder(String path) {
        Glide.with(getContext())
                .load(path)
                .into(new SimpleTarget<Drawable>(mMaxSize, mMaxSize) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        Bitmap bitmap = null;
                        if (resource instanceof BitmapDrawable) {
                            bitmap = ((BitmapDrawable) resource).getBitmap();
                        } else if (resource instanceof GifDrawable) {
                            bitmap = ((GifDrawable) resource).getFirstFrame();
                        }
                        if (bitmap == null) {
                            return;
                        }
                        adjustImageSize(bitmap);
                        if (mUseNormalDrawable) {
                            mDrawable = resource;
                        } else {
                            mBorderDrawable = new PLVRoundedBorderDrawable(
                                    bitmap,
                                    PLVDisplayUtils.dpToPx(getContext(), 2),
                                    Color.WHITE,
                                    PLVDisplayUtils.dpToPx(getContext(), 1),
                                    0
                            );
                        }
                        setImageDrawable(mBorderDrawable != null ? mBorderDrawable : mDrawable);
                    }
                });
    }

    private void adjustImageSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / height;

        int targetWidth, targetHeight;
        if (width > height) {
            targetWidth = Math.min(width, mMaxSize);
            targetHeight = (int) (targetWidth / ratio);
        } else {
            targetHeight = Math.min(height, mMaxSize);
            targetWidth = (int) (targetHeight * ratio);
        }

        LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new LayoutParams(targetWidth, targetHeight);
        } else {
            params.width = targetWidth;
            params.height = targetHeight;
        }
        setLayoutParams(params);
    }

    public void toggleBorder(boolean show) {
        mBorderVisible = show;
        if (mBorderDrawable != null) {
            mBorderDrawable.setShowBorder(show);
        }
        if (mOnToggleBorderListener != null) {
            mOnToggleBorderListener.onToggleBorder(show, this);
        }
        invalidate();
    }

    public boolean isBorderVisible() {
        return mBorderVisible;
    }

    @Override
    public void onClick(float upX, float upY) {
        if (finishRect.contains((int) upX, (int) upY)) {
            toggleBorder(false);
            if (mOnToggleBorderListener != null) {
                mOnToggleBorderListener.onExitEditMode();
            }
        }
    }

    @Override
    public Rect getExtraPadding() {
        return new Rect(0, 0, 0, 0);
    }

    public void removeFromParent() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mOnToggleBorderListener != null) {
            mOnToggleBorderListener.onRemoveFromParent();
        }
    }

    @Override
    public View getCanScaleView() {
        return this;
    }

    public boolean isEditMode() {
        return mOnToggleBorderListener != null && mOnToggleBorderListener.isEditMode();
    }

    public void setOnToggleBorderListener(OnToggleBorderListener onToggleBorderListener) {
        mOnToggleBorderListener = onToggleBorderListener;
    }

    public interface OnToggleBorderListener {
        void onToggleBorder(boolean show, View view);

        void onExitEditMode();

        void onRemoveFromParent();

        boolean isEditMode();

        boolean isSettingFinished();
    }
}
