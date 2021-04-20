package com.easefun.polyv.livecommon.module.utils;

import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * date: 2020/8/13
 * author: HWilliamgo
 * description: view位置切换器
 * <p>
 * 使用方式：
 * 当两个View，例如 ViewA 和 ViewB 要进行位置切换时，实例化一个[PLVViewSwitcher]，并调用[registerSwitchVew]
 * 为 ViewA 和 ViewB 进行注册，之后调用[switchView]方法即可进行位置切换。
 */
public class PLVViewSwitcher {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    private static final String TAG = PLVViewSwitcher.class.getSimpleName();
    private static final String SWITCH_VIEW = "switchView:";
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="实例变量">
    private PLVSwitchViewAnchorLayout switchViewA;
    private PLVSwitchViewAnchorLayout switchViewB;
    private boolean isViewSwitched = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">

    /**
     * 注册要切换位置的View
     *
     * @param switchViewA View A
     * @param switchViewB View B
     */
    public void registerSwitchVew(PLVSwitchViewAnchorLayout switchViewA, PLVSwitchViewAnchorLayout switchViewB) {
        this.switchViewA = switchViewA;
        this.switchViewB = switchViewB;
    }

    /**
     * 切换View位置
     */
    public void switchView() {
        if (switchViewA == null || switchViewB == null) {
            return;
        }
        PLVSwitchViewAnchorLayout viewACurParent;
        PLVSwitchViewAnchorLayout viewBCurParent;

        View viewA;
        View viewB;

        if (isViewSwitched) {
            switchViewA.notifySwitchBackBefore();
            switchViewB.notifySwitchBackBefore();

            try {
                viewA = switchViewB.getSwitchView();
                viewB = switchViewA.getSwitchView();
            } catch (IllegalAccessException e) {
                PLVCommonLog.e(TAG, SWITCH_VIEW +e.getMessage());
                return;
            }


            viewACurParent = switchViewB;
            viewBCurParent = switchViewA;

            try {
                exchangeView(viewACurParent, viewA, viewBCurParent, viewB);
            } catch (Exception e) {
                PLVCommonLog.e(TAG,SWITCH_VIEW+e.getMessage());
                return;
            }


            isViewSwitched = false;

            switchViewA.notifySwitchBackAfter();
            switchViewB.notifySwitchBackAfter();
            PLVCommonLog.d(TAG, viewA + " and " + viewB + " switch back to their origin parent");
        } else {
            switchViewA.notifySwitchElsewhereBefore();
            switchViewB.notifySwitchElsewhereBefore();

            try {
                viewA = switchViewA.getSwitchView();
                viewB = switchViewB.getSwitchView();
            } catch (IllegalAccessException e) {
                PLVCommonLog.e(TAG,SWITCH_VIEW+e.getMessage());
                return;
            }


            viewACurParent = switchViewA;
            viewBCurParent = switchViewB;

            try {
                exchangeView(viewACurParent, viewA, viewBCurParent, viewB);
            } catch (Exception e) {
                PLVCommonLog.e(TAG,SWITCH_VIEW+e.getMessage());
                return;
            }

            isViewSwitched = true;

            switchViewA.notifySwitchElsewhereAfter();
            switchViewB.notifySwitchElsewhereAfter();
            PLVCommonLog.d(TAG, viewA + " and " + viewB + " switch to new parent of each");
        }
    }

    /**
     * View位置是否切换过
     *
     * @return true表示 两个view互换了位置，false表示两个view没有互换位置
     */
    public boolean isViewSwitched() {
        return isViewSwitched;
    }
// </editor-fold>

    private void exchangeView(ViewGroup viewACurParent, View viewA, ViewGroup viewBCurParent, View viewB) {
        viewACurParent.removeView(viewA);
        viewBCurParent.removeView(viewB);

        viewACurParent.addView(viewB);
        viewBCurParent.addView(viewA);
    }
}
