package com.easefun.polyv.livecommon.ui.widget.tabview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @author Hoshiiro
 */
public class PLVTabLinearLayout extends LinearLayout {

    private final PLVTabGroup tabGroup = new PLVTabGroup();

    public PLVTabLinearLayout(Context context) {
        super(context);
    }

    public PLVTabLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVTabLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PLVTabLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        tabGroup.setChildByParent(this);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        tabGroup.setChildByParent(this);
    }

    public void setSelectedChild(View view) {
        tabGroup.setSelectedChild(view);
    }
}
