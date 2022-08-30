package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 能收到方向回调的LinearLayout
 */
public class PLVOrientationSensibleLinearLayout extends LinearLayout {
    private Runnable onPortrait;
    private Runnable onLandscape;

    private boolean showOnPortrait = true;
    private boolean showOnLandscape = true;

    public PLVOrientationSensibleLinearLayout(Context context) {
        super(context);
        initView(null);
    }

    public PLVOrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public PLVOrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(@Nullable AttributeSet attributeSet) {
        parseAttrs(attributeSet);
        post(new Runnable() {
            @Override
            public void run() {
                updateVisibleStatus();
            }
        });
    }

    private void parseAttrs(@Nullable AttributeSet attributeSet) {
        if (attributeSet == null) {
            return;
        }
        final TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PLVOrientationSensibleLinearLayout);
        showOnPortrait = typedArray.getBoolean(R.styleable.PLVOrientationSensibleLinearLayout_plv_show_on_portrait, showOnPortrait);
        showOnLandscape = typedArray.getBoolean(R.styleable.PLVOrientationSensibleLinearLayout_plv_show_on_landscape, showOnLandscape);
        typedArray.recycle();
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
        updateVisibleStatus();
    }

    public void setOnPortrait(Runnable onPortrait) {
        this.onPortrait = onPortrait;
    }

    public void setOnLandscape(Runnable onLandscape) {
        this.onLandscape = onLandscape;
    }

    public PLVOrientationSensibleLinearLayout setShowOnLandscape(boolean showOnLandscape) {
        this.showOnLandscape = showOnLandscape;
        return this;
    }

    public PLVOrientationSensibleLinearLayout setShowOnPortrait(boolean showOnPortrait) {
        this.showOnPortrait = showOnPortrait;
        return this;
    }

    private void updateVisibleStatus() {
        final boolean isLandscape = ScreenUtils.isLandscape();
        final boolean needShow = isLandscape && showOnLandscape || !isLandscape && showOnPortrait;
        setVisibility(needShow ? VISIBLE : GONE);
    }

}
