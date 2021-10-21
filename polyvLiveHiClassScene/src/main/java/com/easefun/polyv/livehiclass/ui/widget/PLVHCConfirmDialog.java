package com.easefun.polyv.livehiclass.ui.widget;

import android.content.Context;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livehiclass.R;

/**
 * @author suhongtao
 */
public class PLVHCConfirmDialog extends PLVConfirmDialog {

    public PLVHCConfirmDialog(Context context) {
        super(context);
    }

    @Override
    protected int layoutId() {
        return R.layout.plvhc_widget_confirm_dialog;
    }

    @Override
    protected float dialogWidthInDp() {
        return 260F;
    }

    @Override
    protected int confirmTitleId() {
        return R.id.plvhc_confirm_title;
    }

    @Override
    protected int confirmContentId() {
        return R.id.plvhc_confirm_content;
    }

    @Override
    protected int leftConfirmTextViewId() {
        return R.id.plvhc_left_confirm_btn;
    }

    @Override
    protected int rightConfirmTextViewId() {
        return R.id.plvhc_right_confirm_btn;
    }

    @Override
    protected int splitViewId() {
        return R.id.plvhc_split_view;
    }

    @Override
    protected boolean hasSplitView() {
        return true;
    }

}
