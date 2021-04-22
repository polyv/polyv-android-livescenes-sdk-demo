package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;

/**
 * @author suhongtao
 */
public class PLVMultipleSelectionWindow {

    private Context context;

    private PopupWindow popupWindow;
    private View rootView;
    private View plvMultipleSelectionMaskView;
    private LinearLayout plvMultipleSelectionLl;
    private View plvMultipleSelectionSeparatorLine;
    private TextView plvMultipleSelectionCancelTv;

    private OnCancelListener onCancelListener;

    private int selectionSeparatorLineColor = Color.WHITE;
    private int selectionSeparatorLineHeightInPx = 0;

    public PLVMultipleSelectionWindow(Context context) {
        this.context = context;
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.plv_multiple_selection_window_layout, null);
        findView();
        plvMultipleSelectionCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (onCancelListener != null) {
                    onCancelListener.onCancel();
                }
            }
        });

        popupWindow = new PopupWindow();
        popupWindow.setContentView(rootView);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void findView() {
        plvMultipleSelectionMaskView = (View) rootView.findViewById(R.id.plv_multiple_selection_mask_view);
        plvMultipleSelectionLl = (LinearLayout) rootView.findViewById(R.id.plv_multiple_selection_ll);
        plvMultipleSelectionSeparatorLine = (View) rootView.findViewById(R.id.plv_multiple_selection_separator_line);
        plvMultipleSelectionCancelTv = (TextView) rootView.findViewById(R.id.plv_multiple_selection_cancel_tv);
    }

    public PLVMultipleSelectionWindow addSelectionItem(final SelectionItem selectionItem) {
        if (selectionSeparatorLineHeightInPx > 0 && plvMultipleSelectionLl.getChildCount() > 0) {
            View separator = new View(context);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, selectionSeparatorLineHeightInPx);
            separator.setLayoutParams(lp);
            separator.setBackgroundColor(selectionSeparatorLineColor);
            plvMultipleSelectionLl.addView(separator);
        }

        AppCompatTextView textView = new AppCompatTextView(context);
        int viewHeight = selectionItem.viewHeightInPx;
        if (viewHeight < ViewGroup.LayoutParams.WRAP_CONTENT) {
            viewHeight = DpOrPxUtils.dip2px(context, 48);
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight);
        textView.setLayoutParams(lp);
        textView.setText(selectionItem.text);
        textView.setTextSize(selectionItem.textSizeInSp);
        textView.setTextColor(selectionItem.textColor);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(selectionItem.backgroundColor);
        textView.setOnClickListener(selectionItem.onClickListener);

        plvMultipleSelectionLl.addView(textView);

        return this;
    }

    public PLVMultipleSelectionWindow removeAllItems() {
        plvMultipleSelectionLl.removeAllViews();
        return this;
    }

    public void show(View parent) {
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void hide() {
        popupWindow.dismiss();
    }

    public PLVMultipleSelectionWindow setMaskColor(@ColorInt int maskColor) {
        plvMultipleSelectionMaskView.setBackgroundColor(maskColor);
        return this;
    }

    public PLVMultipleSelectionWindow cancelable(boolean cancelable) {
        if (cancelable) {
            plvMultipleSelectionSeparatorLine.setVisibility(View.VISIBLE);
            plvMultipleSelectionCancelTv.setVisibility(View.VISIBLE);
        } else {
            plvMultipleSelectionSeparatorLine.setVisibility(View.GONE);
            plvMultipleSelectionCancelTv.setVisibility(View.GONE);
        }
        return this;
    }

    public PLVMultipleSelectionWindow setCancelButtonText(CharSequence text) {
        plvMultipleSelectionCancelTv.setText(text);
        return this;
    }

    public PLVMultipleSelectionWindow setCancelButtonTextColor(@ColorInt int color) {
        plvMultipleSelectionCancelTv.setTextColor(color);
        return this;
    }

    public PLVMultipleSelectionWindow setCancelButtonTextSizeInSp(int textSizeInSp) {
        plvMultipleSelectionCancelTv.setTextSize(textSizeInSp);
        return this;
    }

    public PLVMultipleSelectionWindow setSelectionSeparatorLineColor(@ColorInt int selectionSeparatorLineColor) {
        this.selectionSeparatorLineColor = selectionSeparatorLineColor;
        return this;
    }

    public PLVMultipleSelectionWindow setSelectionSeparatorLineHeightInPx(int selectionSeparatorLineHeightInPx) {
        this.selectionSeparatorLineHeightInPx = selectionSeparatorLineHeightInPx;
        return this;
    }

    public PLVMultipleSelectionWindow setCancelSeparateLineColor(@ColorInt int color) {
        plvMultipleSelectionSeparatorLine.setBackgroundColor(color);
        return this;
    }

    public PLVMultipleSelectionWindow setCancelSeparateLineHeightInPx(int heightInPx) {
        ViewGroup.LayoutParams lp = plvMultipleSelectionSeparatorLine.getLayoutParams();
        lp.height = heightInPx;
        plvMultipleSelectionSeparatorLine.setLayoutParams(lp);
        return this;
    }

    public PLVMultipleSelectionWindow onCancel(@Nullable OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    public static class SelectionItem {

        public static final int DEFAULT_TEXT_SIZE_SP = 18;
        public static final int DEFAULT_TEXT_COLOR = Color.parseColor("#ADADC0");
        public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#2B2C35");

        private CharSequence text;
        private int textSizeInSp;
        private int textColor;
        private int viewHeightInPx = -3;
        private int backgroundColor;
        private View.OnClickListener onClickListener;

        public SelectionItem(CharSequence text) {
            this(text, DEFAULT_TEXT_SIZE_SP);
        }

        public SelectionItem(CharSequence text, int textSizeInSp) {
            this(text, textSizeInSp, DEFAULT_TEXT_COLOR);
        }

        public SelectionItem(CharSequence text, int textSizeInSp, @ColorInt int textColor) {
            this(text, textSizeInSp, textColor, DEFAULT_BACKGROUND_COLOR);
        }

        public SelectionItem(CharSequence text, int textSizeInSp, @ColorInt int textColor, @ColorInt int backgroundColor) {
            setText(text);
            setTextSizeInSp(textSizeInSp);
            setTextColor(textColor);
            setBackgroundColor(backgroundColor);
        }

        public SelectionItem setText(CharSequence text) {
            this.text = text;
            return this;
        }

        public SelectionItem setTextSizeInSp(int textSizeInSp) {
            this.textSizeInSp = textSizeInSp;
            return this;
        }

        public SelectionItem setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public SelectionItem setViewHeightInPx(int viewHeightInPx) {
            this.viewHeightInPx = viewHeightInPx;
            return this;
        }

        public SelectionItem setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public SelectionItem onClick(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

    }

    public interface OnCancelListener {
        void onCancel();
    }

}
