package com.easefun.polyv.livecommon.ui.widget.blurview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/** Marks offscreen captures so no BlurView recursively draws the view hierarchy. */
final class BlurCanvas extends Canvas {
    BlurCanvas(Bitmap bitmap) {
        super(bitmap);
    }
}
