package com.easefun.polyv.livecommon.ui.util;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.view.View;

import com.easefun.polyv.livecommon.R;

import java.lang.ref.WeakReference;

/**
 * @author Hoshiiro
 */
public class PLVViewUtil {

    private static final int TAG_VIEW_UTIL_SHOW_DURATION = R.id.plv_view_util_show_view_for_duration_tag;

    public static void showViewForDuration(final View view, final long durationInMillis) {
        if (view == null) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setTag(TAG_VIEW_UTIL_SHOW_DURATION, System.currentTimeMillis() + durationInMillis - 100);
        final WeakReference<View> ref = new WeakReference<>(view);
        postToMainThread(durationInMillis, new Runnable() {
            @Override
            public void run() {
                final View view = ref.get();
                if (view == null) {
                    return;
                }
                final Object timestamp = view.getTag(TAG_VIEW_UTIL_SHOW_DURATION);
                final boolean shouldHide = !(timestamp instanceof Long) || ((Long) timestamp) <= System.currentTimeMillis();
                if (shouldHide) {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

}
