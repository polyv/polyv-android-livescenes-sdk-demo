package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.easefun.polyv.livecommon.R;

/**
 * @author Hoshiiro
 */
public class PLVMaxHeightScrollView extends ScrollView {

    private int maxHeight = Integer.MAX_VALUE;

    public PLVMaxHeightScrollView(@NonNull Context context) {
        super(context);
    }

    public PLVMaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(attrs);
    }

    public PLVMaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PLVMaxHeightScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttrs(attrs);
    }

    private void parseAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PLVMaxHeightScrollView);
        maxHeight = typedArray.getDimensionPixelSize(R.styleable.PLVMaxHeightScrollView_plv_max_height, maxHeight);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredHeight() > maxHeight) {
            setMeasuredDimension(getMeasuredWidth(), MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
        }
    }
}
