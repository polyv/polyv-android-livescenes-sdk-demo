package com.easefun.polyv.streameralone.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.streameralone.R;

/**
 * @author suhongtao
 */
public class PLVSAConfirmDialog extends PLVConfirmDialog {

    public static class Builder extends PLVConfirmDialog.Builder {

        protected Builder(@NonNull Context context) {
            super(context);
        }

        public static Builder context(@NonNull Context context) {
            return new Builder(context);
        }

        @Override
        public PLVConfirmDialog build() {
            return param.initTo(new PLVSAConfirmDialog(param.context));
        }

    }

    public PLVSAConfirmDialog(Context context) {
        super(context);
    }

    @Override
    protected int layoutId() {
        return R.layout.plvsa_widget_confirm_dialog;
    }

    @Override
    protected float dialogWidthInDp() {
        return 260F;
    }

    @Override
    protected int confirmTitleId() {
        return R.id.plvsa_confirm_title;
    }

    @Override
    protected int confirmContentId() {
        return R.id.plvsa_confirm_content;
    }

    @Override
    protected int leftConfirmTextViewId() {
        return R.id.plvsa_left_confirm_btn;
    }

    @Override
    protected int rightConfirmTextViewId() {
        return R.id.plvsa_right_confirm_btn;
    }

    @Override
    protected int splitViewId() {
        return R.id.plvsa_split_view;
    }

    @Override
    protected boolean hasSplitView() {
        return true;
    }

}
