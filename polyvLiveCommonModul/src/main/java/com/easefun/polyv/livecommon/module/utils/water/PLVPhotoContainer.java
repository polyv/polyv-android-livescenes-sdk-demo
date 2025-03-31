package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

public class PLVPhotoContainer extends RelativeLayout {
    private static final int MAX_IMAGE_COUNT = 10;
    private View mMaskView;
    private FrameLayout mImageContainer;
    private TextView mDeleteTextView;
    private OnViewActionListener mOnViewActionListener;

    public PLVPhotoContainer(Context context) {
        this(context, null);
    }

    public PLVPhotoContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPhotoContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MarginLayoutParams containerParams = (MarginLayoutParams) mImageContainer.getLayoutParams();
        if (containerParams != null) {
            containerParams.bottomMargin = ScreenUtils.isLandscape() ? ConvertUtils.dp2px(56) : ConvertUtils.dp2px(170);
        }
    }

    private void init() {
        // 初始化蒙层View
        mMaskView = new View(getContext());
        mMaskView.setBackgroundColor(PLVFormatUtils.parseColor("#4D000000"));
        mMaskView.setVisibility(View.GONE);
        addView(mMaskView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // 初始化图片容器
        mImageContainer = new FrameLayout(getContext());
        LayoutParams containerParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        containerParams.leftMargin = ConvertUtils.dp2px(12);
        containerParams.rightMargin = ConvertUtils.dp2px(12);
        containerParams.topMargin = ConvertUtils.dp2px(78);
        containerParams.bottomMargin = ScreenUtils.isLandscape() ? ConvertUtils.dp2px(56) : ConvertUtils.dp2px(170);
        addView(mImageContainer, containerParams);

        // 初始化删除区域
        mDeleteTextView = new TextView(getContext());
        mDeleteTextView.setText("拖动到此处删除");
        mDeleteTextView.setTextSize(14);
        mDeleteTextView.setPadding(ConvertUtils.dp2px(20), ConvertUtils.dp2px(14), ConvertUtils.dp2px(20), ConvertUtils.dp2px(14));
        mDeleteTextView.setTextColor(Color.WHITE);
        mDeleteTextView.setBackgroundResource(R.drawable.plv_water_delete_tv_bg);
        mDeleteTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.plv_delete_icon), null, null, null);
        mDeleteTextView.setCompoundDrawablePadding(ConvertUtils.dp2px(4));
        mDeleteTextView.setGravity(Gravity.CENTER);
        mDeleteTextView.setVisibility(View.GONE);

        LayoutParams deleteParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        deleteParams.bottomMargin = ConvertUtils.dp2px(6);
        deleteParams.addRule(ALIGN_PARENT_BOTTOM);
        deleteParams.addRule(CENTER_HORIZONTAL);
        addView(mDeleteTextView, deleteParams);

        // 设置点击监听
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideAllBorders();
                }
                return false;
            }
        });
    }

    public void addImage(String path) {
        if (mImageContainer.getChildCount() >= MAX_IMAGE_COUNT) {
            ToastUtils.showShort("最多添加10张图片"); // no need i18n
            return;
        }
        PLVStickerImageView imageView = new PLVStickerImageView(getContext(), 160);
        imageView.setImageWithBorder(path);
        imageView.setOnToggleBorderListener(new PLVStickerImageView.OnToggleBorderListener() {
            @Override
            public void onToggleBorder(boolean show, View view) {
                if (show) {
                    mMaskView.setVisibility(View.VISIBLE);
                    if (mOnViewActionListener != null) {
                        mOnViewActionListener.onEditMode(true);
                    }
                    hideOtherBorders(view);
                }
            }

            @Override
            public void onRemoveFromParent() {
                if (!hasBorder()) {
                    mMaskView.setVisibility(View.GONE);
                    if (mOnViewActionListener != null) {
                        mOnViewActionListener.onEditMode(false);
                    }
                }
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;

        imageView.setLayoutParams(params);
        imageView.setOnTouchListener(new PLVStickerTouchController(getContext(), mDeleteTextView));
        mImageContainer.addView(imageView);

        mMaskView.setVisibility(View.VISIBLE);
        if (mOnViewActionListener != null) {
            mOnViewActionListener.onEditMode(true);
        }
    }

    public boolean hasImage() {
        return mImageContainer.getChildCount() > 0;
    }

    @Nullable
    public Bitmap captureView() {
        if (!hasImage()) {
            return null;
        }
        View rootView = this;
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }

    private void hideAllBorders() {
        for (int i = 0; i < mImageContainer.getChildCount(); i++) {
            View child = mImageContainer.getChildAt(i);
            if (child instanceof PLVStickerImageView) {
                ((PLVStickerImageView) child).toggleBorder(false);
            }
        }
        mMaskView.setVisibility(View.GONE);
        if (mOnViewActionListener != null) {
            mOnViewActionListener.onEditMode(false);
        }
    }

    private void hideOtherBorders(View view) {
        for (int i = 0; i < mImageContainer.getChildCount(); i++) {
            View child = mImageContainer.getChildAt(i);
            if (child instanceof PLVStickerImageView) {
                if (child != view) {
                    ((PLVStickerImageView) child).toggleBorder(false);
                }
            }
        }
    }

    private boolean hasBorder() {
        for (int i = 0; i < mImageContainer.getChildCount(); i++) {
            View child = mImageContainer.getChildAt(i);
            if (child instanceof PLVStickerImageView) {
                if (((PLVStickerImageView) child).isBorderVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setOnViewActionListener(OnViewActionListener onViewActionListener) {
        mOnViewActionListener = onViewActionListener;
    }

    public interface OnViewActionListener {
        void onEditMode(boolean isEditMode);
    }
}
