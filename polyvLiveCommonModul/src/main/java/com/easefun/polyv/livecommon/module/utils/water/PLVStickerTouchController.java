package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;

public class PLVStickerTouchController implements View.OnTouchListener {
    private static final float MIN_SCALE = 0.4f;
    private static final float MAX_SCALE = 2f;
    private static int BOTTOM_MARGIN = 0;
    private static boolean CAN_DELETE = true;

    private final ScaleGestureDetector mScaleDetector;
    private final View mDeleteArea;
    private float mLastX, mLastY;
    private float mScaleFactor = 1f;
    private View mView;
    // 标记是否正在缩放
    private boolean mIsScaling = false;

    private long downTime = 0;
    private float downX = 0, downY = 0;
    private static final int CLICK_TIMEOUT = 200;
    private static final int CLICK_DISTANCE = 10;

    public static void setBottomMargin(int bottomMargin) {
        BOTTOM_MARGIN = bottomMargin;
    }

    public static void setCanDelete(boolean canDelete) {
        CAN_DELETE = canDelete;
    }

    public PLVStickerTouchController(Context context, View deleteArea) {
        mDeleteArea = deleteArea;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isEditMode = ((IPLVToggleView) v).isEditMode();
        if (!isEditMode) {
            return false;
        }
        this.mView = v;
        mScaleDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        Rect extraPadding = ((IPLVToggleView) v).getExtraPadding();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                v.bringToFront();
                // 设置默认的枢轴点为中心
                v.setPivotX(v.getWidth() / 2f);
                v.setPivotY(v.getHeight() / 2f);
                mLastX = event.getRawX();
                mLastY = event.getRawY();
                mDeleteArea.setVisibility(View.VISIBLE);
                ((IPLVToggleView) v).toggleBorder(true);

                downTime = System.currentTimeMillis();
                downX = event.getX();
                downY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mIsScaling = true;
                mDeleteArea.setVisibility(View.GONE);
                break;

            case MotionEvent.ACTION_MOVE:
                // 如果不是缩放状态且只有单指操作，则执行平移
                if (!mIsScaling && pointerCount == 1) {
                    float dx = event.getRawX() - mLastX;
                    float dy = event.getRawY() - mLastY;

                    float newX = v.getX() + dx;
                    float newY = v.getY() + dy;

                    ViewGroup parent = null;
                    if (v.getParent() instanceof ViewGroup) {
                        parent = (ViewGroup) v.getParent();
                    }

                    if (parent != null) {
                        int paddingLeft = parent.getPaddingLeft() + extraPadding.left;
                        int paddingRight = parent.getPaddingRight() + extraPadding.right;
                        int paddingTop = parent.getPaddingTop() + extraPadding.top;
                        int paddingBottom = parent.getPaddingBottom() + extraPadding.bottom;

                        int parentWidth = parent.getWidth();
                        int parentHeight = parent.getHeight();

                        int viewWidth = v.getWidth();
                        int viewHeight = v.getHeight();

                        // 计算允许的X范围
                        float minX = paddingLeft;
                        float maxX = parentWidth - paddingRight - viewWidth;
                        newX = Math.max(newX, minX);
                        newX = Math.min(newX, maxX);

                        // 计算允许的Y范围
                        float minY = paddingTop;
                        float maxY = parentHeight - paddingBottom - viewHeight - BOTTOM_MARGIN;
                        newY = Math.max(newY, minY);
                        newY = Math.min(newY, maxY);
                    }

                    v.setX(newX);
                    v.setY(newY);

                    mLastX = event.getRawX();
                    mLastY = event.getRawY();

                    // 检查手指是否在 mDeleteArea 区域内
                    int[] deletePos = new int[2];
                    mDeleteArea.getLocationOnScreen(deletePos);
                    Rect deleteRect = new Rect(
                            deletePos[0],
                            deletePos[1],
                            deletePos[0] + mDeleteArea.getWidth(),
                            deletePos[1] + mDeleteArea.getHeight()
                    );
                    float fingerX = event.getRawX();
                    float fingerY = event.getRawY();
                    if (deleteRect.contains((int) fingerX, (int) fingerY)) {
                        mDeleteArea.setSelected(true);
                    } else {
                        mDeleteArea.setSelected(false);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                mIsScaling = false;
                // 松手时，如果 mDeleteArea 被选中则删除贴图
                if (mDeleteArea.isSelected() && CAN_DELETE) {
                    ((IPLVToggleView) v).removeFromParent();
                }
                mDeleteArea.setVisibility(View.GONE);
                mDeleteArea.setSelected(false);

                long upTime = System.currentTimeMillis();
                float upX = event.getX();
                float upY = event.getY();
                if (upTime - downTime < CLICK_TIMEOUT &&
                        Math.abs(upX - downX) < CLICK_DISTANCE &&
                        Math.abs(upY - downY) < CLICK_DISTANCE) {
                    // 单击事件
                    ((IPLVToggleView) v).onClick(upX, upY);
                }
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private int mWidth;
        private int mHeight;
        private Rect extraPadding;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // 在缩放开始时设置枢轴点为当前 View 的中心
            if (mView != null) {
                mView.setPivotX(mView.getWidth() / 2f);
                mView.setPivotY(mView.getHeight() / 2f);
                if (mWidth == 0 || mHeight == 0) {
                    mWidth = mView.getWidth();
                    mHeight = mView.getHeight();
                }
            }
            mIsScaling = true;
            mDeleteArea.setVisibility(View.GONE);
            extraPadding = ((IPLVToggleView) mView).getExtraPadding();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            int parentWidth = ((View) mView.getParent()).getWidth() - extraPadding.left - extraPadding.right;
            int parentHeight = ((View) mView.getParent()).getHeight() - extraPadding.top - extraPadding.bottom;
            float max_scale = Math.min(parentWidth / (float) mWidth, parentHeight / (float) mHeight);
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, max_scale));
            if (mView != null) {
                View canScaleView = ((IPLVToggleView) mView).getCanScaleView();
                if (canScaleView instanceof ImageView) {
                    ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
                    layoutParams.width = (int) (mWidth * mScaleFactor);
                    layoutParams.height = (int) (mHeight * mScaleFactor);
                    mView.setLayoutParams(layoutParams);
                } else if (canScaleView instanceof TextView) {
                    // 只缩放文字大小
//                    scaleTextView((TextView) canScaleView);
                } else if (canScaleView instanceof ViewGroup) {
//                    // 遍历内容区的子View，分别缩放TextView和ImageView
//                    ViewGroup vg = (ViewGroup) canScaleView;
//                    for (int i = 0; i < vg.getChildCount(); i++) {
//                        View child = vg.getChildAt(i);
//                        if (child.getVisibility() != View.VISIBLE) {
//                            continue;
//                        }
//                        if (child instanceof TextView) {
//                            scaleTextView((TextView) child);
//                        } else if (child instanceof ImageView) {
//                            ImageView imageView = (ImageView) child;
//                            int originalWidth = imageView.getTag(R.id.plv_sticker_icon_original_width) instanceof Integer
//                                    ? (Integer) imageView.getTag(R.id.plv_sticker_icon_original_width)
//                                    : imageView.getWidth();
//                            int originalHeight = imageView.getTag(R.id.plv_sticker_icon_original_height) instanceof Integer
//                                    ? (Integer) imageView.getTag(R.id.plv_sticker_icon_original_height)
//                                    : imageView.getHeight();
//                            if (imageView.getTag(R.id.plv_sticker_icon_original_width) == null) {
//                                imageView.setTag(R.id.plv_sticker_icon_original_width, originalWidth);
//                            }
//                            if (imageView.getTag(R.id.plv_sticker_icon_original_height) == null) {
//                                imageView.setTag(R.id.plv_sticker_icon_original_height, originalHeight);
//                            }
//                            ViewGroup.LayoutParams iconParams = imageView.getLayoutParams();
//                            iconParams.width = (int) (originalWidth * mScaleFactor);
//                            iconParams.height = (int) (originalHeight * mScaleFactor);
//                            imageView.setLayoutParams(iconParams);
//                        }
//                    }
                }
            }
            return true;
        }

        private void scaleTextView(TextView textView) {
            float originalTextSize = textView.getTag(R.id.plv_sticker_text_original_size) instanceof Float
                    ? (Float) textView.getTag(R.id.plv_sticker_text_original_size)
                    : textView.getTextSize();
            if (textView.getTag(R.id.plv_sticker_text_original_size) == null) {
                textView.setTag(R.id.plv_sticker_text_original_size, originalTextSize);
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize * mScaleFactor);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mView != null && mView.getParent() instanceof View) {
                Rect extraPadding = ((IPLVToggleView) mView).getExtraPadding();
                View parent = (View) mView.getParent();
                int paddingLeft = parent.getPaddingLeft() + extraPadding.left;
                int paddingRight = parent.getPaddingRight() + extraPadding.right;
                int paddingTop = parent.getPaddingTop() + extraPadding.top;
                int paddingBottom = parent.getPaddingBottom() + extraPadding.bottom;

                // 获取当前 mView 的坐标 (setX、setY 是相对于父控件的)
                float currentX = mView.getX();
                float currentY = mView.getY();
                int viewWidth = mView.getWidth();
                int viewHeight = mView.getHeight();
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                float newX = currentX;
                float newY = currentY;

                // 计算允许的X范围
                float minX = paddingLeft;
                float maxX = parentWidth - paddingRight - viewWidth;
                newX = Math.max(newX, minX);
                newX = Math.min(newX, maxX);

                // 计算允许的Y范围
                float minY = paddingTop;
                float maxY = parentHeight - paddingBottom - viewHeight - BOTTOM_MARGIN;
                newY = Math.max(newY, minY);
                newY = Math.min(newY, maxY);

                mView.setX(newX);
                mView.setY(newY);
            }
        }
    }
}