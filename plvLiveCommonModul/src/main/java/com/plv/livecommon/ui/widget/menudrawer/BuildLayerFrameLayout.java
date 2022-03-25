package com.plv.livecommon.ui.widget.menudrawer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * FrameLayout which caches the hardware layer if available.
 * <p/>
 * If it's not posted twice the layer either wont be built on start, or it'll be built twice.
 */
class BuildLayerFrameLayout extends FrameLayout {

    private boolean mChanged;

    private boolean mHardwareLayersEnabled = true;

    private boolean mAttached;

    private boolean mFirst = true;

    public BuildLayerFrameLayout(Context context) {
        super(context);
        if (PLVMenuDrawer.USE_TRANSLATIONS) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public BuildLayerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (PLVMenuDrawer.USE_TRANSLATIONS) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public BuildLayerFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (PLVMenuDrawer.USE_TRANSLATIONS) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    void setHardwareLayersEnabled(boolean enabled) {
        mHardwareLayersEnabled = enabled;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (PLVMenuDrawer.USE_TRANSLATIONS && mHardwareLayersEnabled) {
            post(new Runnable() {
                @Override
                public void run() {
                    mChanged = true;
                    invalidate();
                }
            });
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mChanged && PLVMenuDrawer.USE_TRANSLATIONS) {
            post(new Runnable() {
                @Override
                public void run() {
                    if (mAttached) {
                        final int layerType = getLayerType();
                        // If it's already a hardware layer, it'll be built anyway.
                        if (layerType != LAYER_TYPE_HARDWARE || mFirst) {
                            mFirst = false;
                            setLayerType(LAYER_TYPE_HARDWARE, null);
                            buildLayer();
                            setLayerType(LAYER_TYPE_NONE, null);
                        }
                    }
                }
            });

            mChanged = false;
        }
    }
}
