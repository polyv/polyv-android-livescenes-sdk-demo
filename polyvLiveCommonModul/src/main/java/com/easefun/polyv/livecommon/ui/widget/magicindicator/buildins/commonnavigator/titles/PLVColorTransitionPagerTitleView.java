package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles;

import android.content.Context;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVArgbEvaluatorHolder;
import com.plv.foundationsdk.log.PLVCommonLog;


/**
 * 两种颜色过渡的指示器标题
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVColorTransitionPagerTitleView extends PLVSimplePagerTitleView {
    private static final String TAG = "PLVColorTransitionPager";

    public PLVColorTransitionPagerTitleView(Context context) {
        super(context);
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        int color = PLVArgbEvaluatorHolder.eval(leavePercent, mSelectedColor, mNormalColor);
        setTextColor(color);
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        int color = PLVArgbEvaluatorHolder.eval(enterPercent, mNormalColor, mSelectedColor);
        setTextColor(color);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        PLVCommonLog.d(TAG,"onSelected index:"+index+ " totalCount:"+totalCount);
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        PLVCommonLog.d(TAG,"onDeselected index:"+index+ " totalCount:"+totalCount);
    }
}
