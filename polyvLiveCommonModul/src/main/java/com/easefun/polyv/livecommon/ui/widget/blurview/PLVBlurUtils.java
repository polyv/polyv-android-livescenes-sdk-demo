package com.easefun.polyv.livecommon.ui.widget.blurview;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.Window;

public class PLVBlurUtils {

    public static void initBlurView(PLVBlurView blurView) {
        blurView.setupWith((ViewGroup) ((Activity) blurView.getContext()).findViewById(Window.ID_ANDROID_CONTENT))
                .setFrameClearDrawable(null)
                .setBlurAlgorithm(new SupportRenderScriptBlur(blurView.getContext()))
                .setBlurRadius(1)
                .setHasFixedTransformationMatrix(false);
    }
}
