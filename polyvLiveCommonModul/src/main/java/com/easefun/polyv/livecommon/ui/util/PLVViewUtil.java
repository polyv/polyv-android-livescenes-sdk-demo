package com.easefun.polyv.livecommon.ui.util;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.view.View;

import java.lang.ref.WeakReference;

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
        final WeakReference<View> ref = new WeakReference<>(view);
        postToMainThread(durationInMillis, new Runnable() {
            @Override
            public void run() {
                final View view = ref.get();
                if (view == null) {
                    return;
                }
                final Object timestamp = view.getTag(tagKey);
                final boolean shouldHide = !(timestamp instanceof Long) || ((Long) timestamp) <= System.currentTimeMillis();
                if (shouldHide) {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

}
