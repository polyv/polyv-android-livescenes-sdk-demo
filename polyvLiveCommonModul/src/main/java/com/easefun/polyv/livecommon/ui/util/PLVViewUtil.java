package com.easefun.polyv.livecommon.ui.util;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.widget.GridLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.lang.ref.WeakReference;

/**
 * @author Hoshiiro
 */
public class PLVViewUtil {

    private static final int TAG_VIEW_UTIL_SHOW_DURATION = R.id.plv_view_util_show_view_for_duration_tag;
    private static final Point POINT = new Point();
    private static final Rect RECT = new Rect(0, 0, 0, 0);
    private static final int[] LOCATION = new int[2];

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

    public static boolean isViewVisible(View view) {
        try {
            ((Activity) view.getContext()).getWindowManager().getDefaultDisplay().getSize(POINT);
            final int screenWidth = POINT.x;
            final int screenHeight = POINT.y;
            RECT.set(0, 0, screenWidth, screenHeight);
            view.getLocationInWindow(LOCATION);
            return view.getLocalVisibleRect(RECT);
        } catch (Exception e) {
            PLVCommonLog.exception(e);
        }
        return false;
    }

    public static void setGridLayoutItemVisible(View view, final boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) view.getLayoutParams();
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, visible ? 1 : 0, 1F);
            view.setLayoutParams(lp);
        }
    }

}
