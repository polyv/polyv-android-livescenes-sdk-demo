package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

public class PLVStickerTouchController implements View.OnTouchListener {
    private static final float MIN_SCALE = 0.2f;
    private static final float MAX_SCALE = 2f;

    private final ScaleGestureDetector mScaleDetector;
    private final View mDeleteArea;
    private float mLastX, mLastY;
    private float mPosX, mPosY;
    private float mScaleFactor = 1f;
    private View mView;
    // 标记是否正在缩放
    private boolean mIsScaling = false;

    public PLVStickerTouchController(Context context, View deleteArea) {
        mDeleteArea = deleteArea;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isEditMode = ((PLVStickerImageView) v).isEditMode();
        if (!isEditMode) {
            return false;
        }
        this.mView = v;
        mScaleDetector.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                v.bringToFront();
                // 设置默认的枢轴点为中心
                v.setPivotX(v.getWidth() / 2f);
                v.setPivotY(v.getHeight() / 2f);
                mLastX = event.getRawX();
                mLastY = event.getRawY();
                mDeleteArea.setVisibility(View.VISIBLE);
                ((PLVStickerImageView) v).toggleBorder(true);
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
                        int paddingLeft = parent.getPaddingLeft();
                        int paddingRight = parent.getPaddingRight();
                        int paddingTop = parent.getPaddingTop();
                        int paddingBottom = parent.getPaddingBottom();

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
                        float maxY = parentHeight - paddingBottom - viewHeight;
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
                if (mDeleteArea.isSelected()) {
                    ((PLVStickerImageView) v).removeFromParent();
                }
                mDeleteArea.setVisibility(View.GONE);
                mDeleteArea.setSelected(false);
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private int mWidth;
        private int mHeight;

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
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));
            if (mView != null) {
                int parentWidth = ((View) mView.getParent()).getWidth();
                int parentHeight = ((View) mView.getParent()).getHeight();
                if ((int) (mWidth * mScaleFactor) > parentWidth || (int) (mHeight * mScaleFactor) > parentHeight) {
                    return true;
                }
                ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
                layoutParams.width = (int) (mWidth * mScaleFactor);
                layoutParams.height = (int) (mHeight * mScaleFactor);
                mView.setLayoutParams(layoutParams);
            }
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mView != null && mView.getParent() instanceof View) {
                View parent = (View) mView.getParent();
                // 获取当前 mView 的坐标 (setX、setY 是相对于父控件的)
                float currentX = mView.getX();
                float currentY = mView.getY();
                int viewWidth = mView.getWidth();
                int viewHeight = mView.getHeight();
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                // 计算需要调整的偏移量
                float newX = currentX;
                float newY = currentY;
                if (currentX < 0) {
                    newX = 0;
                } else if (currentX + viewWidth > parentWidth) {
                    newX = parentWidth - viewWidth;
                }
                if (currentY < 0) {
                    newY = 0;
                } else if (currentY + viewHeight > parentHeight) {
                    newY = parentHeight - viewHeight;
                }
                mView.setX(newX);
                mView.setY(newY);
            }
        }
    }
}