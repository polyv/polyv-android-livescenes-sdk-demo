package com.easefun.polyv.livecommon.ui.widget.gif;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 简单的GifSpanTextView
 */
public class SimpleGifSpanTextView extends GifSpanTextView {

    public SimpleGifSpanTextView(Context context) {
        super(context);
    }

    public SimpleGifSpanTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleGifSpanTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        if (wMode != MeasureSpec.EXACTLY && getParent() instanceof ViewGroup) {
            int pmWidth = ((ViewGroup) getParent()).getMeasuredWidth();
            int pWidth = ((ViewGroup) getParent()).getWidth();
            if (pmWidth != 0 && wSize <= pmWidth && getMeasuredWidth() < wSize
                    && (pWidth != pmWidth || getMeasuredWidth() == getWidth())) {
                setMeasuredDimension(wSize, getMeasuredHeight());
            }
        }
    }
}
