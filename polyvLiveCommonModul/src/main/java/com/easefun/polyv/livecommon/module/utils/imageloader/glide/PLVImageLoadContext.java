package com.easefun.polyv.livecommon.module.utils.imageloader.glide;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/** Checks the Activity behind a themed/wrapped context before starting a UI image request. */
final class PLVImageLoadContext {
    private PLVImageLoadContext() { }

    static boolean canLoad(Context context) {
        while (context != null) {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                return !activity.isFinishing() && !activity.isDestroyed();
            }
            if (!(context instanceof ContextWrapper)) {
                return true;
            }
            Context base = ((ContextWrapper) context).getBaseContext();
            if (base == context) {
                return false;
            }
            context = base;
        }
        return false;
    }
}
