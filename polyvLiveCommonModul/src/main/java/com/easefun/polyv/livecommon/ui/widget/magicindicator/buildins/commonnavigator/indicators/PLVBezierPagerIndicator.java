package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;


import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVFragmentContainerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVArgbEvaluatorHolder;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.model.PLVPositionData;

import java.util.Arrays;
import java.util.List;

/**
 * 贝塞尔曲线ViewPager指示器，带颜色渐变
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVBezierPagerIndicator extends View implements IPLVPagerIndicator {
    private List<PLVPositionData> mPositionDataList;

    private float mLeftCircleRadius;
    private float mLeftCircleX;
    private float mRightCircleRadius;
    private float mRightCircleX;

    private float mYOffset;
    private float mMaxCircleRadius;
    private float mMinCircleRadius;

    private Paint mPaint;
    private Path mPath = new Path();

    private List<Integer> mColors;
    private Interpolator mStartInterpolator = new AccelerateInterpolator();
    private Interpolator mEndInterpolator = new DecelerateInterpolator();

    public PLVBezierPagerIndicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mMaxCircleRadius = PLVUIUtil.dip2px(context, 3.5);
        mMinCircleRadius = PLVUIUtil.dip2px(context, 2);
        mYOffset = PLVUIUtil.dip2px(context, 1.5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mLeftCircleX, getHeight() - mYOffset - mMaxCircleRadius, mLeftCircleRadius, mPaint);
        canvas.drawCircle(mRightCircleX, getHeight() - mYOffset - mMaxCircleRadius, mRightCircleRadius, mPaint);
        drawBezierCurve(canvas);
    }

    /**
     * 绘制贝塞尔曲线
     *
     * @param canvas
     */
    private void drawBezierCurve(Canvas canvas) {
        mPath.reset();
        float y = getHeight() - mYOffset - mMaxCircleRadius;
        mPath.moveTo(mRightCircleX, y);
        mPath.lineTo(mRightCircleX, y - mRightCircleRadius);
        mPath.quadTo(mRightCircleX + (mLeftCircleX - mRightCircleX) / 2.0f, y, mLeftCircleX, y - mLeftCircleRadius);
        mPath.lineTo(mLeftCircleX, y + mLeftCircleRadius);
        mPath.quadTo(mRightCircleX + (mLeftCircleX - mRightCircleX) / 2.0f, y, mRightCircleX, y + mRightCircleRadius);
        mPath.close();  // 闭合
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
            return;
        }

        // 计算颜色
        if (mColors != null && !mColors.isEmpty()) {
            int currentColor = mColors.get(Math.abs(position) % mColors.size());
            int nextColor = mColors.get(Math.abs(position + 1) % mColors.size());
            int color = PLVArgbEvaluatorHolder.eval(positionOffset, currentColor, nextColor);
            mPaint.setColor(color);
        }

        // 计算锚点位置
        PLVPositionData current = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position);
        PLVPositionData next = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position + 1);

        float leftX = current.getLeft() + ((float) (current.getRight() - current.getLeft()) / 2);
        float rightX = next.getLeft() + ((float) (next.getRight() - next.getLeft()) / 2);

        mLeftCircleX = leftX + (rightX - leftX) * mStartInterpolator.getInterpolation(positionOffset);
        mRightCircleX = leftX + (rightX - leftX) * mEndInterpolator.getInterpolation(positionOffset);
        mLeftCircleRadius = mMaxCircleRadius + (mMinCircleRadius - mMaxCircleRadius) * mEndInterpolator.getInterpolation(positionOffset);
        mRightCircleRadius = mMinCircleRadius + (mMaxCircleRadius - mMinCircleRadius) * mStartInterpolator.getInterpolation(positionOffset);

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPositionDataProvide(List<PLVPositionData> dataList) {
        mPositionDataList = dataList;
    }

    public float getMaxCircleRadius() {
        return mMaxCircleRadius;
    }

    public void setMaxCircleRadius(float maxCircleRadius) {
        mMaxCircleRadius = maxCircleRadius;
    }

    public float getMinCircleRadius() {
        return mMinCircleRadius;
    }

    public void setMinCircleRadius(float minCircleRadius) {
        mMinCircleRadius = minCircleRadius;
    }

    public float getYOffset() {
        return mYOffset;
    }

    public void setYOffset(float yOffset) {
        mYOffset = yOffset;
    }

    public void setColors(Integer... colors) {
        mColors = Arrays.asList(colors);
    }

    public void setStartInterpolator(Interpolator startInterpolator) {
        mStartInterpolator = startInterpolator;
        if (mStartInterpolator == null) {
            mStartInterpolator = new AccelerateInterpolator();
        }
    }

    public void setEndInterpolator(Interpolator endInterpolator) {
        mEndInterpolator = endInterpolator;
        if (mEndInterpolator == null) {
            mEndInterpolator = new DecelerateInterpolator();
        }
    }
}
