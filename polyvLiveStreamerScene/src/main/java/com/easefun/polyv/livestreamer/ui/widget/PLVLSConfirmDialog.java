package com.easefun.polyv.livestreamer.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livestreamer.R;

/**
 * @author Hoshiiro
 */
public class PLVLSConfirmDialog {

    public static class Builder extends PLVConfirmDialog.Builder {

        protected Builder(@NonNull Context context) {
            super(context);
        }

        public static Builder context(@NonNull Context context) {
            return new Builder(context);
        }

        @Override
        public PLVConfirmDialog build() {
            int lineCount = 0;
            if (param.title != null && param.titleVisibility == View.VISIBLE) {
                lineCount++;
            }
            if (param.content != null && param.contentVisibility == View.VISIBLE) {
                lineCount++;
            }
            final PLVConfirmDialog confirmDialog = lineCount > 1 ? new PLVLSDoubleLineConfirmDialog(param.context) : new PLVLSSingleLineConfirmDialog(param.context);
            return param.initTo(confirmDialog);
        }
    }

    private static class PLVLSSingleLineConfirmDialog extends PLVConfirmDialog {

        public PLVLSSingleLineConfirmDialog(Context context) {
            super(context);
        }

        @Override
        protected int layoutId() {
            return R.layout.plvls_single_line_confirm_window_layout;
        }

        @Override
        public PLVConfirmDialog setTitle(int resId) {
            return setContent(resId);
        }

        @Override
        public PLVConfirmDialog setTitle(String title) {
            return setContent(title);
        }

        @Override
        public PLVConfirmDialog setTitleVisibility(int visibility) {
            return this;
        }

        @Override
        public PLVConfirmDialog setContentVisibility(int visibility) {
            return this;
        }
    }

    private static class PLVLSDoubleLineConfirmDialog extends PLVConfirmDialog {

        public PLVLSDoubleLineConfirmDialog(Context context) {
            super(context);
        }

        @Override
        protected int layoutId() {
            return R.layout.plvls_double_line_confirm_window_layout;
        }

        @Override
        protected float dialogWidthInDp() {
            return 260F;
        }
    }

}
