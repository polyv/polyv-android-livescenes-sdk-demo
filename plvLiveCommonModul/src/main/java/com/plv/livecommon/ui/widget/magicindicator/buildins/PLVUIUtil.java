package com.plv.livecommon.ui.widget.magicindicator.buildins;

import android.content.Context;

/**
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public final class PLVUIUtil {

    public static int dip2px(Context context, double dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}