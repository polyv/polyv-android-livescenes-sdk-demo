package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles;

import android.content.Context;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerTitleView;
import com.plv.foundationsdk.log.PLVCommonLog;


/**
 * 空指示器标题，用于只需要指示器而不需要title的需求
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVDummyPagerTitleView extends View implements IPLVPagerTitleView {
    private static final String TAG = "PLVDummyPagerTitleView";

    public PLVDummyPagerTitleView(Context context) {
        super(context);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        PLVCommonLog.d(TAG,"onSelected index:"+index+ " totalCount:"+totalCount);
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        PLVCommonLog.d(TAG,"onDeselected index:"+index+ " totalCount:"+totalCount);
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
    }
}
