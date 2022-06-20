package com.easefun.polyv.livecommon.ui.widget.itemview.adapter;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;

/**
 * viewPagerAdapter
 */
public class PLVViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<? extends Fragment> fragments;

    public PLVViewPagerAdapter(FragmentManager fm, List<? extends Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        // FIXME 不要做一只鸵鸟
        try {
            super.restoreState(state, loader);
        } catch (Exception e) {
            PLVCommonLog.exception(e);
            e.printStackTrace();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
