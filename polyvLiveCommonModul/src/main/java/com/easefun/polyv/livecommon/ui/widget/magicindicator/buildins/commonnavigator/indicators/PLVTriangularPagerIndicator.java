package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVFragmentContainerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.model.PLVPositionData;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;

/**
 * 带有小尖角的直线指示器
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVTriangularPagerIndicator extends View implements IPLVPagerIndicator {
    private static final String TAG = "PLVTriangularPagerIndic";
    private List<PLVPositionData> mPositionDataList;
    private Paint mPaint;
    private int mLineHeight;
    private int mLineColor;
    private int mTriangleHeight;
    private int mTriangleWidth;
    private boolean mReverse;
    private float mYOffset;

    private Path mPath = new Path();
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private float mAnchorX;

    public PLVTriangularPagerIndicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mLineHeight = PLVUIUtil.dip2px(context, 3);
        mTriangleWidth = PLVUIUtil.dip2px(context, 14);
        mTriangleHeight = PLVUIUtil.dip2px(context, 8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mLineColor);
        if (mReverse) {
            canvas.drawRect(0, getHeight() - mYOffset - mTriangleHeight, getWidth(), getHeight() - mYOffset - mTriangleHeight + mLineHeight, mPaint);
        } else {
            canvas.drawRect(0, getHeight() - mLineHeight - mYOffset, getWidth(), getHeight() - mYOffset, mPaint);
        }
        mPath.reset();
        if (mReverse) {
            mPath.moveTo(mAnchorX - ((float) (mTriangleWidth)) / 2, getHeight() - mYOffset - mTriangleHeight);
            mPath.lineTo(mAnchorX, getHeight() - mYOffset);
            mPath.lineTo(mAnchorX + ((float) (mTriangleWidth)) / 2, getHeight() - mYOffset - mTriangleHeight);
        } else {
            mPath.moveTo(mAnchorX - ((float) (mTriangleWidth)) / 2, getHeight() - mYOffset);
            mPath.lineTo(mAnchorX, getHeight() - mTriangleHeight - mYOffset);
            mPath.lineTo(mAnchorX + ((float) (mTriangleWidth)) / 2, getHeight() - mYOffset);
        }
        mPath.close();
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
            return;
        }

        // 计算锚点位置
        PLVPositionData current = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position);
        PLVPositionData next = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position + 1);

        float leftX = current.getLeft() + ((float) (current.getRight() - current.getLeft())) / 2;
        float rightX = next.getLeft() + ((float) (next.getRight() - next.getLeft())) / 2;

        mAnchorX = leftX + (rightX - leftX) * mStartInterpolator.getInterpolation(positionOffset);

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        PLVCommonLog.d(TAG,"onPageSelected:"+position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        PLVCommonLog.d(TAG,"onPageScrollStateChanged:"+state);
    }

    @Override
    public void onPositionDataProvide(List<PLVPositionData> dataList) {
        mPositionDataList = dataList;
    }

    public int getLineHeight() {
        return mLineHeight;
    }

    public void setLineHeight(int lineHeight) {
        mLineHeight = lineHeight;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public void setLineColor(int lineColor) {
        mLineColor = lineColor;
    }

    public int getTriangleHeight() {
        return mTriangleHeight;
    }

    public void setTriangleHeight(int triangleHeight) {
        mTriangleHeight = triangleHeight;
    }

    public int getTriangleWidth() {
        return mTriangleWidth;
    }

    public void setTriangleWidth(int triangleWidth) {
        mTriangleWidth = triangleWidth;
    }

    public Interpolator getStartInterpolator() {
        return mStartInterpolator;
    }

    public void setStartInterpolator(Interpolator startInterpolator) {
        mStartInterpolator = startInterpolator;
        if (mStartInterpolator == null) {
            mStartInterpolator = new LinearInterpolator();
        }
    }

    public boolean isReverse() {
        return mReverse;
    }

    public void setReverse(boolean reverse) {
        mReverse = reverse;
    }

    public float getYOffset() {
        return mYOffset;
    }

    public void setYOffset(float yOffset) {
        mYOffset = yOffset;
    }
}
