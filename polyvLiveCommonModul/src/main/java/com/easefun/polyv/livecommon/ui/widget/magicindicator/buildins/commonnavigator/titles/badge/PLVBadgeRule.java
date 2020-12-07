package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.badge;

/**
 * 角标的定位规则
 * Created by hackware on 2016/7/19.
 */
public class PLVBadgeRule {
    private PLVBadgeAnchor mAnchor;
    private int mOffset;

    public PLVBadgeRule(PLVBadgeAnchor anchor, int offset) {
        mAnchor = anchor;
        mOffset = offset;
    }

    public PLVBadgeAnchor getAnchor() {
        return mAnchor;
    }

    public void setAnchor(PLVBadgeAnchor anchor) {
        mAnchor = anchor;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        mOffset = offset;
    }
}
