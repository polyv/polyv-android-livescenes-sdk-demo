package com.easefun.polyv.livecommon.ui.util;

import android.view.View;

/**
 * @author Hoshiiro
 */
public class PLVViewUtil {

    public static void showViewForDuration(final View view, final long durationInMillis) {
        final int tagKey = "PLVViewUtil.showViewForDuration".hashCode();
        if (view == null) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setTag(tagKey, System.currentTimeMillis() + durationInMillis - 100);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Object timestamp = view.getTag(tagKey);
                final boolean shouldHide = !(timestamp instanceof Long) || ((Long) timestamp) <= System.currentTimeMillis();
                if (shouldHide) {
                    view.setVisibility(View.GONE);
                }
            }
        }, durationInMillis);
    }

}
