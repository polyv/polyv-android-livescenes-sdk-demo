package com.easefun.polyv.livecommon.module.utils.water;

import android.content.Context;
import android.util.TypedValue;

public class PLVDisplayUtils {

    public static int dpToPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}
