package com.easefun.polyv.livecommon.ui.widget.imageview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 一个简单的、可监听可见性改变的ImageView
 */
public class PLVSimpleImageView extends ImageView {
    private IPLVVisibilityChangedListener visibilityChangedListener;

    public PLVSimpleImageView(Context context) {
        super(context);
    }

    public PLVSimpleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVSimpleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVisibilityChangedListener(IPLVVisibilityChangedListener listener) {
        this.visibilityChangedListener = listener;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibilityChangedListener != null) {
            visibilityChangedListener.onChanged(visibility);
        }
    }
}
