package com.plv.livecommon.ui.widget.magicindicator;

import android.support.v4.view.ViewPager;

/**
 * 简化和ViewPager绑定
 * Created by hackware on 2016/8/17.
 */

public class PLVViewPagerHelper {
    public static void bind(final PLVMagicIndicator magicIndicator, ViewPager viewPager) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                magicIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magicIndicator.onPageScrollStateChanged(state);
            }
        });
    }
}
