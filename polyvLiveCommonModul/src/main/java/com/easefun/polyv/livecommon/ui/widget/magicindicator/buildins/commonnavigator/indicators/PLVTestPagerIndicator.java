package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVFragmentContainerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.model.PLVPositionData;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;


/**
 * 用于测试的指示器，可用来检测自定义的IMeasurablePagerTitleView是否正确测量内容区域
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVTestPagerIndicator extends View implements IPLVPagerIndicator {
    private static final String TAG = "PLVTestPagerIndicator";
    private Paint mPaint;
    private int mOutRectColor;
    private int mInnerRectColor;
    private RectF mOutRect = new RectF();
    private RectF mInnerRect = new RectF();

    private List<PLVPositionData> mPositionDataList;

    public PLVTestPagerIndicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mOutRectColor = Color.RED;
        mInnerRectColor = Color.GREEN;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mOutRectColor);
        canvas.drawRect(mOutRect, mPaint);
        mPaint.setColor(mInnerRectColor);
        canvas.drawRect(mInnerRect, mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPositionDataList == null || mPositionDataList.isEmpty()) {
            return;
        }

        // 计算锚点位置
        PLVPositionData current = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position);
        PLVPositionData next = PLVFragmentContainerHelper.getImitativePositionData(mPositionDataList, position + 1);

        mOutRect.left = current.getLeft() + (next.getLeft() - current.getLeft()) * positionOffset;
        mOutRect.top = current.getTop() + (next.getTop() - current.getTop()) * positionOffset;
        mOutRect.right = current.getRight() + (next.getRight() - current.getRight()) * positionOffset;
        mOutRect.bottom = current.getBottom() + (next.getBottom() - current.getBottom()) * positionOffset;

        mInnerRect.left = current.getContentLeft() + (next.getContentLeft() - current.getContentLeft()) * positionOffset;
        mInnerRect.top = current.getContentTop() + (next.getContentTop() - current.getContentTop()) * positionOffset;
        mInnerRect.right = current.getContentRight() + (next.getContentRight() - current.getContentRight()) * positionOffset;
        mInnerRect.bottom = current.getContentBottom() + (next.getContentBottom() - current.getContentBottom()) * positionOffset;

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

    public int getOutRectColor() {
        return mOutRectColor;
    }

    public void setOutRectColor(int outRectColor) {
        mOutRectColor = outRectColor;
    }

    public int getInnerRectColor() {
        return mInnerRectColor;
    }

    public void setInnerRectColor(int innerRectColor) {
        mInnerRectColor = innerRectColor;
    }
}
