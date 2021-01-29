package com.easefun.polyv.livecommon.ui.widget.blurview;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.plv.foundationsdk.log.PLVCommonLog;

class NoOpBlurAlgorithm implements BlurAlgorithm {
    private static final String TAG = "NoOpBlurAlgorithm";
    @Override
    public Bitmap blur(Bitmap bitmap, float blurRadius) {
        return bitmap;
    }

    @Override
    public void destroy() {
        PLVCommonLog.d(TAG,"destory");
    }

    @Override
    public boolean canModifyBitmap() {
        return true;
    }

    @NonNull
    @Override
    public Bitmap.Config getSupportedBitmapConfig() {
        return Bitmap.Config.ARGB_8888;
    }
}
