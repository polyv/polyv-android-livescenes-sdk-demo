package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 能收到方向回调的LinearLayout
 */
public class PLVOrientationSensibleLinearLayout extends LinearLayout {
    private Runnable onPortrait;
    private Runnable onLandscape;

    public PLVOrientationSensibleLinearLayout(Context context) {
        super(context);
    }

    public PLVOrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVOrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (onPortrait != null) {
                onPortrait.run();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (onLandscape != null) {
                onLandscape.run();
            }
        }
    }

    public void setOnPortrait(Runnable onPortrait) {
        this.onPortrait = onPortrait;
    }

    public void setOnLandscape(Runnable onLandscape) {
        this.onLandscape = onLandscape;
    }
}
