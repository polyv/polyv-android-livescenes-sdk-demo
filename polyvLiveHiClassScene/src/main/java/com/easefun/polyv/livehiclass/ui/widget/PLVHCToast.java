package com.easefun.polyv.livehiclass.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.utils.PLVToast;

/**
 * @author suhongtao
 */
public class PLVHCToast {

    public static class Builder {

        public static PLVToast.Builder context(@NonNull Context context) {
            return PLVToast.Builder.context(context)
                    .setTextColor(Color.parseColor("#CCFFFFFF"))
                    .setBackgroundColor(Color.parseColor("#2D3452"));
        }

    }

}
