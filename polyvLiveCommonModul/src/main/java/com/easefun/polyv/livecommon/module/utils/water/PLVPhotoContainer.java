package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import androidx.annotation.Nullable;
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
    private static final int MAX_TEXT_COUNT = 10;
    private static final float DEFAULT_ASPECT_RATIO = 16.0f / 9.0f;
    
    private View mMaskView;
    private FrameLayout mStickerContainer;
    private TextView mDeleteTextView;
    private PLVStickerTextView mPreviewTextView;
    private PLVStickerTextSelectLayout mStickerTextSelectLayout;
    private boolean isEditMode = false;
    private boolean isSettingFinished = false;
    // 贴图容器比例相关
    private float mAspectRatio = DEFAULT_ASPECT_RATIO;
    private int mContainerWidth = 0;
    private int mContainerHeight = 0;
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

    private void init() {
        // 初始化蒙层View
        mMaskView = new View(getContext());
        mMaskView.setBackgroundColor(PLVFormatUtils.parseColor("#4D000000"));
        mMaskView.setVisibility(View.GONE);
        addView(mMaskView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // 初始化统一贴图容器
        mStickerContainer = new FrameLayout(getContext());
        LayoutParams stickerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        stickerParams.addRule(CENTER_IN_PARENT);
        addView(mStickerContainer, stickerParams);

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

        // 初始化文本贴图选择布局
        mStickerTextSelectLayout = new PLVStickerTextSelectLayout(getContext());
        mStickerTextSelectLayout.close();
        mStickerTextSelectLayout.setOnViewActionListener(new PLVStickerTextSelectLayout.OnViewActionListener() {
            @Override
            public void onShow(boolean isShow) {
                int containerBottomMargin = ConvertUtils.px2dp((getHeight() - mContainerHeight) / 2f);
                PLVStickerTouchController.setBottomMargin(isShow ? (ScreenUtils.isLandscape() ? ConvertUtils.dp2px(146 + 2) : ConvertUtils.dp2px(224 - containerBottomMargin + 2)) : 0);
                PLVStickerTouchController.setCanDelete(!isShow);
                if (!isShow) {
                    if (mPreviewTextView != null) {
                        mPreviewTextView.toggleBorder(false);
                    }
                    onEditMode(false);
                    mPreviewTextView = null;
                }
            }

            @Override
            public void cancel() {
                if (mPreviewTextView != null) {
                    if (mPreviewTextView.isHasSave()) {
                        mPreviewTextView.restoreStatus();
                        if (mPreviewTextView.getParent() == null) {
                            mStickerContainer.addView(mPreviewTextView);
                        }
                    } else {
                        mPreviewTextView.removeFromParent();
                    }
                }
            }

            @Override
            public void done() {
                if (mPreviewTextView != null) {
                    mPreviewTextView.saveStatus();
                }
            }

            @Override
            public void changeStyle(String text, int style) {
                if (mPreviewTextView != null) {
                    mPreviewTextView.setText(text, style);
                    if (mPreviewTextView.getParent() == null) {
                        mStickerContainer.addView(mPreviewTextView);
                    }
                } else {
                    addText(text, style);
                }
            }
        });
        LayoutParams stickerTextSelectParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        stickerTextSelectParams.addRule(ALIGN_PARENT_BOTTOM);
        addView(mStickerTextSelectLayout, stickerTextSelectParams);

        // 设置点击监听
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !mStickerTextSelectLayout.isShowing()) {
                    hideAllBorders();
                }
                return mStickerTextSelectLayout.isShowing();
            }
        });
    }

    private void updateContainerLayout() {
        if (mStickerContainer == null) {
            return;
        }

        int parentWidth = getWidth();
        int parentHeight = getHeight();

        if (parentWidth <= 0 || parentHeight <= 0) {
            return;
        }

        // 根据屏幕方向确定使用的比例
        float currentAspectRatio;
        if (ScreenUtils.isLandscape()) {
            // 横屏：保持传入的比例
            currentAspectRatio = mAspectRatio;
        } else {
            // 竖屏：转换为对应的竖屏比例（宽高比取倒数）
            currentAspectRatio = 1.0f / mAspectRatio;
        }

        // 计算容器实际尺寸
        if (ScreenUtils.isLandscape()) {
            // 横屏：以高度为基准计算宽度
            mContainerHeight = parentHeight;
            mContainerWidth = (int) (mContainerHeight * currentAspectRatio);

            // 如果计算出的宽度超过父容器宽度，则以宽度为基准重新计算
            if (mContainerWidth > parentWidth) {
                mContainerWidth = parentWidth;
                mContainerHeight = (int) (mContainerWidth / currentAspectRatio);
            }
        } else {
            // 竖屏：以宽度为基准计算高度
            mContainerWidth = parentWidth;
            mContainerHeight = (int) (mContainerWidth / currentAspectRatio);

            // 如果计算出的高度超过父容器高度，则以高度为基准重新计算
            if (mContainerHeight > parentHeight) {
                mContainerHeight = parentHeight;
                mContainerWidth = (int) (mContainerHeight * currentAspectRatio);
            }
        }

        // 更新容器布局参数
        LayoutParams params = (LayoutParams) mStickerContainer.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(mContainerWidth, mContainerHeight);
        } else {
            params.width = mContainerWidth;
            params.height = mContainerHeight;
        }

        // 居中显示
        params.addRule(CENTER_IN_PARENT);
        mStickerContainer.setLayoutParams(params);
        mStickerContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStickerContainer.requestLayout();
            }
        }, 300);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当容器大小改变时（如横竖屏切换），更新贴图容器布局
        updateContainerLayout();
    }

    public void setSettingFinished(boolean settingFinished) {
        isSettingFinished = settingFinished;
    }

    // 宽高比，例如16:9
    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            return;
        }
        this.mAspectRatio = aspectRatio;
        updateContainerLayout();
    }

    public void addImage(String path) {
        if (getImageCount() >= MAX_IMAGE_COUNT) {
            ToastUtils.showShort("最多添加10张图片"); // no need i18n
            return;
        }
        final PLVStickerImageView imageView = new PLVStickerImageView(getContext(), 160);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.bringToFront();
                imageView.toggleBorder(true);
            }
        });
        imageView.setImageWithBorder(path);
        imageView.setOnToggleBorderListener(new PLVStickerImageView.OnToggleBorderListener() {
            @Override
            public void onToggleBorder(boolean show, View view) {
                if (show) {
                    if (mPreviewTextView != null) {
                        mPreviewTextView.saveStatus();
                    }
                    if (mStickerTextSelectLayout.isShowing()) {
                        mStickerTextSelectLayout.close();
                    }
                    onEditMode(true);
                    hideOtherBorders(view);
                }
            }

            @Override
            public void onExitEditMode() {
                onEditMode(false);
            }

            @Override
            public void onRemoveFromParent() {
                if (!hasBorder()) {
                    onEditMode(false);
                }
            }

            @Override
            public boolean isEditMode() {
                return isEditMode;
            }

            @Override
            public boolean isSettingFinished() {
                return isSettingFinished;
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;

        imageView.setLayoutParams(params);
        imageView.setOnTouchListener(new PLVStickerTouchController(getContext(), mDeleteTextView));
        mStickerContainer.addView(imageView);
        onEditMode(true);
    }

    public void previewTextSticker() {
        if (getTextCount() >= MAX_TEXT_COUNT) {
            ToastUtils.showShort("最多添加10个文字贴图");
            return;
        }
        mStickerTextSelectLayout.open();
        addText(PLVStickerTextSelectLayout.stickerTextModels[0].text, PLVStickerTextSelectLayout.stickerTextModels[0].style);
    }

    public void addText(final String text, final int style) {
        if (getTextCount() >= MAX_TEXT_COUNT) {
            ToastUtils.showShort("最多添加10个文字贴图");
            return;
        }
        final PLVStickerTextView textView = new PLVStickerTextView(getContext());
        textView.setText(text, style);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.bringToFront();
                textView.toggleBorder(true);
            }
        });
        textView.setOnToggleBorderListener(new PLVStickerTextView.OnToggleBorderListener() {
            @Override
            public void onToggleBorder(boolean show, View view) {
                if (show) {
                    if (mPreviewTextView != null && mPreviewTextView != textView) {
                        mPreviewTextView.saveStatus();
                    }
                    if (mStickerTextSelectLayout.isShowing() && mPreviewTextView != textView) {
                        mStickerTextSelectLayout.close();
                    }
                    mPreviewTextView = textView;
                    onEditMode(true);
                    hideOtherBorders(view);
                }
            }

            @Override
            public void onRemoveFromParent() {
                if (!hasBorder() && !mStickerTextSelectLayout.isShowing()) {
                    onEditMode(false);
                }
                if (mPreviewTextView == textView && !textView.isHasSave()) {
                    mPreviewTextView = null;
                }
            }

            @Override
            public boolean isEditMode() {
                return isEditMode;
            }

            @Override
            public boolean isSettingFinished() {
                return isSettingFinished;
            }

            @Override
            public void onShowInputWindow(boolean isShow) {
                mStickerTextSelectLayout.setVisibility(isShow ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onShouldShowTextSelectLayout() {
                if (!mStickerTextSelectLayout.isShowing()) {
                    mStickerTextSelectLayout.open();
                    mStickerTextSelectLayout.setSelectedPosition(textView.getStyle() - 1);
                }
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);
        textView.setOnTouchListener(new PLVStickerTouchController(getContext(), mDeleteTextView));
        mStickerContainer.addView(textView);
        onEditMode(true);
        if (mStickerTextSelectLayout.isShowing()) {
            mPreviewTextView = textView;
        }
    }

    private void onEditMode(boolean isEditMode) {
        mMaskView.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        if (mOnViewActionListener != null) {
            mOnViewActionListener.onEditMode(isEditMode);
        }
        this.isEditMode = isEditMode;
    }

    public boolean hasImage() {
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            if (mStickerContainer.getChildAt(i) instanceof PLVStickerImageView) {
                return true;
            }
        }
        return false;
    }

    public boolean hasText() {
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            if (mStickerContainer.getChildAt(i) instanceof PLVStickerTextView) {
                return true;
            }
        }
        return false;
    }

    private int getImageCount() {
        int count = 0;
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            if (mStickerContainer.getChildAt(i) instanceof PLVStickerImageView) {
                count++;
            }
        }
        return count;
    }

    private int getTextCount() {
        int count = 0;
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            if (mStickerContainer.getChildAt(i) instanceof PLVStickerTextView) {
                count++;
            }
        }
        return count;
    }

    @Nullable
    public Bitmap captureView() {
        if (!hasImage() && !hasText()) {
            return null;
        }
        View rootView = mStickerContainer;
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }

    private void hideAllBorders() {
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            View child = mStickerContainer.getChildAt(i);
            if (child instanceof IPLVToggleView) {
                ((IPLVToggleView) child).toggleBorder(false);
            }
        }
        onEditMode(false);
    }

    private void hideOtherBorders(View view) {
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            View child = mStickerContainer.getChildAt(i);
            if (child != view && child instanceof IPLVToggleView) {
                ((IPLVToggleView) child).toggleBorder(false);
            }
        }
    }

    private boolean hasBorder() {
        for (int i = 0; i < mStickerContainer.getChildCount(); i++) {
            View child = mStickerContainer.getChildAt(i);
            if (child instanceof IPLVToggleView) {
                if (((IPLVToggleView) child).isBorderVisible()) {
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
