package com.plv.livecommon.ui.widget.magicindicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;


import com.plv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.model.PLVPositionData;

import java.util.ArrayList;
import java.util.List;


/**
 * 使得MagicIndicator在FragmentContainer中使用
 * Created by hackware on 2016/9/4.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PLVFragmentContainerHelper {
    private List<PLVMagicIndicator> mMagicIndicators = new ArrayList<PLVMagicIndicator>();
    private ValueAnimator mScrollAnimator;
    private int mLastSelectedIndex;
    private int mDuration = 150;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            dispatchPageScrollStateChanged(PLVScrollState.SCROLL_STATE_IDLE);
            mScrollAnimator = null;
        }
    };

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float positionOffsetSum = (Float) animation.getAnimatedValue();
            int position = (int) positionOffsetSum;
            float positionOffset = positionOffsetSum - position;
            if (positionOffsetSum < 0) {
                position = position - 1;
                positionOffset = 1.0f + positionOffset;
            }
            dispatchPageScrolled(position, positionOffset, 0);
        }
    };

    public PLVFragmentContainerHelper() {
    }

    public PLVFragmentContainerHelper(PLVMagicIndicator magicIndicator) {
        mMagicIndicators.add(magicIndicator);
    }

    /**
     * IPagerIndicator支持弹性效果的辅助方法
     *
     * @param positionDataList
     * @param index
     * @return
     */
    public static PLVPositionData getImitativePositionData(List<PLVPositionData> positionDataList, int index) {
        if (index >= 0 && index <= positionDataList.size() - 1) { // 越界后，返回假的PositionData
            return positionDataList.get(index);
        } else {
            PLVPositionData result = new PLVPositionData();
            PLVPositionData referenceData;
            int offset;
            if (index < 0) {
                offset = index;
                referenceData = positionDataList.get(0);
            } else {
                offset = index - positionDataList.size() + 1;
                referenceData = positionDataList.get(positionDataList.size() - 1);
            }
            result.setLeft(referenceData.getLeft() + offset * referenceData.width());
            result.setTop(referenceData.getTop());
            result.setRight(referenceData.getRight() + offset * referenceData.width());
            result.setBottom(referenceData.getBottom());
            result.setContentLeft(referenceData.getContentLeft() + offset * referenceData.width());
            result.setContentTop(referenceData.getContentTop());
            result.setContentRight(referenceData.getContentRight() + offset * referenceData.width());
            result.setContentBottom(referenceData.getContentBottom());
            return result;
        }
    }

    public void handlePageSelected(int selectedIndex) {
        handlePageSelected(selectedIndex, true);
    }

    public void handlePageSelected(int selectedIndex, boolean smooth) {
        if (mLastSelectedIndex == selectedIndex) {
            return;
        }
        if (smooth) {
            if (mScrollAnimator == null || !mScrollAnimator.isRunning()) {
                dispatchPageScrollStateChanged(PLVScrollState.SCROLL_STATE_SETTLING);
            }
            dispatchPageSelected(selectedIndex);
            float currentPositionOffsetSum = mLastSelectedIndex;
            if (mScrollAnimator != null) {
                currentPositionOffsetSum = (Float) mScrollAnimator.getAnimatedValue();
                mScrollAnimator.cancel();
                mScrollAnimator = null;
            }
            mScrollAnimator = new ValueAnimator();
            mScrollAnimator.setFloatValues(currentPositionOffsetSum, selectedIndex);    // position = selectedIndex, positionOffset = 0.0f
            mScrollAnimator.addUpdateListener(mAnimatorUpdateListener);
            mScrollAnimator.addListener(mAnimatorListener);
            mScrollAnimator.setInterpolator(mInterpolator);
            mScrollAnimator.setDuration(mDuration);
            mScrollAnimator.start();
        } else {
            dispatchPageSelected(selectedIndex);
            if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
                dispatchPageScrolled(mLastSelectedIndex, 0.0f, 0);
            }
            dispatchPageScrollStateChanged(PLVScrollState.SCROLL_STATE_IDLE);
            dispatchPageScrolled(selectedIndex, 0.0f, 0);
        }
        mLastSelectedIndex = selectedIndex;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        } else {
            mInterpolator = interpolator;
        }
    }

    public void attachMagicIndicator(PLVMagicIndicator magicIndicator) {
        mMagicIndicators.add(magicIndicator);
    }

    private void dispatchPageSelected(int pageIndex) {
        for (PLVMagicIndicator magicIndicator : mMagicIndicators) {
            magicIndicator.onPageSelected(pageIndex);
        }
    }

    private void dispatchPageScrollStateChanged(int state) {
        for (PLVMagicIndicator magicIndicator : mMagicIndicators) {
            magicIndicator.onPageScrollStateChanged(state);
        }
    }

    private void dispatchPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        for (PLVMagicIndicator magicIndicator : mMagicIndicators) {
            magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }
}
