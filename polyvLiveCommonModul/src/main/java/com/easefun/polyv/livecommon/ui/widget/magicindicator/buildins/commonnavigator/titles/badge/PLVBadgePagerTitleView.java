package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.badge;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVMeasurablePagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerTitleView;


/**
 * 支持显示角标的title，角标布局可自定义
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/7/18.
 */
public class PLVBadgePagerTitleView extends FrameLayout implements IPLVMeasurablePagerTitleView {
    private IPLVPagerTitleView mInnerPagerTitleView;
    private View mBadgeView;
    private boolean mAutoCancelBadge = true;

    private PLVBadgeRule mXBadgeRule;
    private PLVBadgeRule mYBadgeRule;

    public PLVBadgePagerTitleView(Context context) {
        super(context);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        if (mInnerPagerTitleView != null) {
            mInnerPagerTitleView.onSelected(index, totalCount);
        }
        if (mAutoCancelBadge) {
            setBadgeView(null);
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        if (mInnerPagerTitleView != null) {
            mInnerPagerTitleView.onDeselected(index, totalCount);
        }
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        if (mInnerPagerTitleView != null) {
            mInnerPagerTitleView.onLeave(index, totalCount, leavePercent, leftToRight);
        }
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        if (mInnerPagerTitleView != null) {
            mInnerPagerTitleView.onEnter(index, totalCount, enterPercent, leftToRight);
        }
    }

    public IPLVPagerTitleView getInnerPagerTitleView() {
        return mInnerPagerTitleView;
    }

    public void setInnerPagerTitleView(IPLVPagerTitleView innerPagerTitleView) {
        if (mInnerPagerTitleView == innerPagerTitleView) {
            return;
        }
        mInnerPagerTitleView = innerPagerTitleView;
        removeAllViews();
        if (mInnerPagerTitleView instanceof View) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView((View) mInnerPagerTitleView, lp);
        }
        if (mBadgeView != null) {
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(mBadgeView, lp);
        }
    }

    public View getBadgeView() {
        return mBadgeView;
    }

    public void setBadgeView(View badgeView) {
        if (mBadgeView == badgeView) {
            return;
        }
        mBadgeView = badgeView;
        removeAllViews();
        if (mInnerPagerTitleView instanceof View) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView((View) mInnerPagerTitleView, lp);
        }
        if (mBadgeView != null) {
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(mBadgeView, lp);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mInnerPagerTitleView instanceof View && mBadgeView != null) {
            int[] position = new int[14];   // 14种角标定位方式
            View v = (View) mInnerPagerTitleView;
            position[0] = v.getLeft();
            position[1] = v.getTop();
            position[2] = v.getRight();
            position[3] = v.getBottom();
            if (mInnerPagerTitleView instanceof IPLVMeasurablePagerTitleView) {
                IPLVMeasurablePagerTitleView view = (IPLVMeasurablePagerTitleView) mInnerPagerTitleView;
                position[4] = view.getContentLeft();
                position[5] = view.getContentTop();
                position[6] = view.getContentRight();
                position[7] = view.getContentBottom();
            } else {
                for (int i = 4; i < 8; i++) {
                    position[i] = position[i - 4];
                }
            }
            position[8] = v.getWidth() / 2;
            position[9] = v.getHeight() / 2;
            position[10] = position[4] / 2;
            position[11] = position[5] / 2;
            position[12] = position[6] + (position[2] - position[6]) / 2;
            position[13] = position[7] + (position[3] - position[7]) / 2;

            // 根据设置的BadgeRule调整角标的位置
            if (mXBadgeRule != null) {
                int x = position[mXBadgeRule.getAnchor().ordinal()];
                int offset = mXBadgeRule.getOffset();
                int newLeft = x + offset;
                mBadgeView.offsetLeftAndRight(newLeft - mBadgeView.getLeft());
            }
            if (mYBadgeRule != null) {
                int y = position[mYBadgeRule.getAnchor().ordinal()];
                int offset = mYBadgeRule.getOffset();
                int newTop = y + offset;
                mBadgeView.offsetTopAndBottom(newTop - mBadgeView.getTop());
            }
        }
    }

    @Override
    public int getContentLeft() {
        if (mInnerPagerTitleView instanceof IPLVMeasurablePagerTitleView) {
            return getLeft() + ((IPLVMeasurablePagerTitleView) mInnerPagerTitleView).getContentLeft();
        }
        return getLeft();
    }

    @Override
    public int getContentTop() {
        if (mInnerPagerTitleView instanceof IPLVMeasurablePagerTitleView) {
            return ((IPLVMeasurablePagerTitleView) mInnerPagerTitleView).getContentTop();
        }
        return getTop();
    }

    @Override
    public int getContentRight() {
        if (mInnerPagerTitleView instanceof IPLVMeasurablePagerTitleView) {
            return getLeft() + ((IPLVMeasurablePagerTitleView) mInnerPagerTitleView).getContentRight();
        }
        return getRight();
    }

    @Override
    public int getContentBottom() {
        if (mInnerPagerTitleView instanceof IPLVMeasurablePagerTitleView) {
            return ((IPLVMeasurablePagerTitleView) mInnerPagerTitleView).getContentBottom();
        }
        return getBottom();
    }

    public PLVBadgeRule getXBadgeRule() {
        return mXBadgeRule;
    }

    public void setXBadgeRule(PLVBadgeRule badgeRule) {
        if (badgeRule != null) {
            PLVBadgeAnchor anchor = badgeRule.getAnchor();
            if (anchor != PLVBadgeAnchor.LEFT
                    && anchor != PLVBadgeAnchor.RIGHT
                    && anchor != PLVBadgeAnchor.CONTENT_LEFT
                    && anchor != PLVBadgeAnchor.CONTENT_RIGHT
                    && anchor != PLVBadgeAnchor.CENTER_X
                    && anchor != PLVBadgeAnchor.LEFT_EDGE_CENTER_X
                    && anchor != PLVBadgeAnchor.RIGHT_EDGE_CENTER_X) {
                throw new IllegalArgumentException("x badge rule is wrong.");
            }
        }
        mXBadgeRule = badgeRule;
    }

    public PLVBadgeRule getYBadgeRule() {
        return mYBadgeRule;
    }

    public void setYBadgeRule(PLVBadgeRule badgeRule) {
        if (badgeRule != null) {
            PLVBadgeAnchor anchor = badgeRule.getAnchor();
            if (anchor != PLVBadgeAnchor.TOP
                    && anchor != PLVBadgeAnchor.BOTTOM
                    && anchor != PLVBadgeAnchor.CONTENT_TOP
                    && anchor != PLVBadgeAnchor.CONTENT_BOTTOM
                    && anchor != PLVBadgeAnchor.CENTER_Y
                    && anchor != PLVBadgeAnchor.TOP_EDGE_CENTER_Y
                    && anchor != PLVBadgeAnchor.BOTTOM_EDGE_CENTER_Y) {
                throw new IllegalArgumentException("y badge rule is wrong.");
            }
        }
        mYBadgeRule = badgeRule;
    }

    public boolean isAutoCancelBadge() {
        return mAutoCancelBadge;
    }

    public void setAutoCancelBadge(boolean autoCancelBadge) {
        mAutoCancelBadge = autoCancelBadge;
    }
}
