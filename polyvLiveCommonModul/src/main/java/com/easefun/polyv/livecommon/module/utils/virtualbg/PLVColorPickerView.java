package com.easefun.polyv.livecommon.module.utils.virtualbg;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

public class PLVColorPickerView extends RelativeLayout {
    private View mStickerContainer;
    private View mIndicatorWrapper;
    private ImageView mColorPreview;
    private OnColorPickerListener mListener;

    private int mContainerWidth, mContainerHeight;
    private float mAspectRatio = 16f / 9f; // 默认摄像头比例
    private float[] pickedColor;

    public interface OnColorPickerListener {
        /**
         * @param xPercent X 坐标占比 (0.0~1.0)
         * @param yPercent Y 坐标占比 (0.0~1.0)
         */
        void onPositionChanged(float xPercent, float yPercent);

        void onPickedColor(float[] rgb);
    }

    public PLVColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_color_picker_layout, this);
        mStickerContainer = findViewById(R.id.plv_sticker_container);
        mIndicatorWrapper = findViewById(R.id.plv_color_indicator_wrapper);
        mColorPreview = findViewById(R.id.plv_color_preview_view);

        // 设置触摸逻辑
        mStickerContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateIndicatorPosition(event.getX(), event.getY() - ConvertUtils.dp2px(42));
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (mListener != null && pickedColor != null) {
                        mListener.onPickedColor(pickedColor);
                    }
                    hide();
                    mColorPreview.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_OVER);
                }
                return true;
            }
        });

        updateLayout(mAspectRatio);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout(mAspectRatio);
    }

    // 同步你提供的 updateContainerLayout 逻辑
    public void updateLayout(float aspectRatio) {
        if (aspectRatio <= 0) {
            return;
        }
        this.mAspectRatio = aspectRatio;
        post(() -> {
            int parentWidth = getWidth();
            int parentHeight = getHeight();
            if (parentWidth <= 0 || parentHeight <= 0) return;

            float currentRatio = getContext().getResources().getConfiguration().orientation == 1 ? 1.0f / mAspectRatio : mAspectRatio;

            // 你的核心计算逻辑
            if (parentWidth * 1.0f / parentHeight > currentRatio) {
                mContainerHeight = parentHeight;
                mContainerWidth = (int) (mContainerHeight * currentRatio);
            } else {
                mContainerWidth = parentWidth;
                mContainerHeight = (int) (mContainerWidth / currentRatio);
            }

            LayoutParams params = (LayoutParams) mStickerContainer.getLayoutParams();
            params.width = mContainerWidth;
            params.height = mContainerHeight;
            mStickerContainer.setLayoutParams(params);

            // 默认让指示器在中心
            updateIndicatorCenter();
        });
    }

    private void updateIndicatorPosition(float x, float y) {
        // 边界控制
        x = Math.max(1, Math.min(x, mContainerWidth));
        y = Math.max(1, Math.min(y, mContainerHeight));

        // 让准星（indicator 的中心偏下位置）对准手指
        mIndicatorWrapper.setX(x - mIndicatorWrapper.getWidth() / 2f);
        mIndicatorWrapper.setY(y - mIndicatorWrapper.getHeight() + ConvertUtils.dp2px(12));

        if (mListener != null && mContainerWidth > 0 && mContainerHeight > 0) {
            float xPercent = x / mContainerWidth;
            float yPercent = y / mContainerHeight;
            mListener.onPositionChanged(xPercent, yPercent);
        }
    }

    private void updateIndicatorCenter() {
        float x = mContainerWidth / 2f;
        float y = mContainerHeight / 2f;
        x = Math.max(1, Math.min(x, mContainerWidth));
        y = Math.max(1, Math.min(y, mContainerHeight));
        // 让准星（indicator 的中心偏下位置）对准手指
        mIndicatorWrapper.setX(x - mIndicatorWrapper.getWidth() / 2f);
        mIndicatorWrapper.setY(y - mIndicatorWrapper.getHeight());
    }

    public void show() {
        setVisibility(VISIBLE);
        updateLayout(this.mAspectRatio);
    }

    public void hide() {
        setVisibility(INVISIBLE);
    }

    public void updatePickedColor(float[] rgb) {
        this.pickedColor = rgb;
        // 更新上方预览图的颜色（利用染色滤镜）
        int color = Color.rgb((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255));
        mColorPreview.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
    }

    public void setOnColorPickerListener(OnColorPickerListener listener) {
        this.mListener = listener;
    }
}
