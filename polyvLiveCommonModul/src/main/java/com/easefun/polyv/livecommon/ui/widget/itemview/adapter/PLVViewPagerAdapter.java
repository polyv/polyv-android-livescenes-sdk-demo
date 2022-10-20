package com.easefun.polyv.livecommon.ui.widget.itemview.adapter;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
    public int getItemPosition(@NonNull Object object) {
        final int index = fragments.indexOf(object);
        return index < 0 ? POSITION_NONE : index;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
