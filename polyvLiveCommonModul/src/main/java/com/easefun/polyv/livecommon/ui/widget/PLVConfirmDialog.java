package com.easefun.polyv.livecommon.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;
import com.plv.foundationsdk.utils.PLVAppUtils;

import java.lang.ref.WeakReference;

/**
 * date: 2020-05-21
 * author: hwj
 */
public class PLVConfirmDialog {

    private final WeakReference<Context> contextWeakReference;

    private PLVOnFocusDialog dialog;
    private TextView plvConfirmTitle;
    private TextView plvConfirmContent;
    private TextView plvLeftConfirmBtn;
    private TextView plvRightConfirmBtn;
    @Nullable
    private View plvSplitView;

    public PLVConfirmDialog(Context context) {
        contextWeakReference = new WeakReference<>(context);
        View root = LayoutInflater.from(context).inflate(layoutId(), null, false);

        RelativeLayout dialogWrapper = new RelativeLayout(context);
        RelativeLayout.LayoutParams rootLp = new RelativeLayout.LayoutParams(DpOrPxUtils.dip2px(context, dialogWidthInDp()), RelativeLayout.LayoutParams.WRAP_CONTENT);
        rootLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        dialogWrapper.addView(root, rootLp);

        dialog = new PLVOnFocusDialog(context);
        dialog.setContentView(dialogWrapper);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initView(root);
    }

    private void initView(View root) {
        plvConfirmTitle = (TextView) root.findViewById(confirmTitleId());
        plvConfirmContent = (TextView) root.findViewById(confirmContentId());
        plvLeftConfirmBtn = (TextView) root.findViewById(leftConfirmTextViewId());
        plvRightConfirmBtn = (TextView) root.findViewById(rightConfirmTextViewId());
        if (hasSplitView()) {
            plvSplitView = root.findViewById(splitViewId());
        }

        plvLeftConfirmBtn.setText(R.string.plv_common_dialog_click_wrong);
        plvLeftConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="自定义布局 - 继承修改">

    @LayoutRes
    protected int layoutId() {
        return R.layout.plv_confirm_window_layout;
    }

    protected float dialogWidthInDp() {
        return 228F;
    }

    @IdRes
    protected int confirmTitleId() {
        return R.id.plv_confirm_title;
    }

    @IdRes
    protected int confirmContentId() {
        return R.id.plv_confirm_content;
    }

    @IdRes
    protected int leftConfirmTextViewId() {
        return R.id.plv_left_confirm_btn;
    }

    @IdRes
    protected int rightConfirmTextViewId() {
        return R.id.plv_right_confirm_btn;
    }

    @IdRes
    protected int splitViewId() {
        return R.id.plv_split_view;
    }

    protected boolean hasSplitView() {
        return true;
    }

    // </editor-fold>

    public PLVConfirmDialog setTitle(String title) {
        plvConfirmTitle.setText(title);
        return this;
    }

    public PLVConfirmDialog setTitle(@StringRes int resId) {
        return setTitle(dialog.getContext().getString(resId));
    }

    public PLVConfirmDialog setTitleVisibility(int visibility) {
        plvConfirmTitle.setVisibility(visibility);
        return this;
    }

    public PLVConfirmDialog setContent(String content) {
        plvConfirmContent.setText(content);
        return this;
    }

    public PLVConfirmDialog setContent(@StringRes int resId) {
        return setContent(dialog.getContext().getString(resId));
    }

    public PLVConfirmDialog setContentVisibility(int visibility) {
        plvConfirmContent.setVisibility(visibility);
        return this;
    }

    public PLVConfirmDialog setLeftButtonText(String leftText) {
        plvLeftConfirmBtn.setText(leftText);
        return this;
    }

    public PLVConfirmDialog setLeftButtonText(@StringRes int resId) {
        return setLeftButtonText(dialog.getContext().getString(resId));
    }

    public PLVConfirmDialog setRightButtonText(String rightText) {
        plvRightConfirmBtn.setText(rightText);
        return this;
    }

    public PLVConfirmDialog setRightButtonText(@StringRes int resId) {
        return setRightButtonText(dialog.getContext().getString(resId));
    }

    public PLVConfirmDialog setLeftBtnListener(final View.OnClickListener leftBtnListener) {
        plvLeftConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftBtnListener != null) {
                    if (leftBtnListener instanceof OnClickListener) {
                        ((OnClickListener) leftBtnListener).onClick(dialog, v);
                    } else {
                        leftBtnListener.onClick(v);
                    }
                }
            }
        });
        return this;
    }

    public PLVConfirmDialog setRightBtnListener(final View.OnClickListener rightBtnListener) {
        plvRightConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightBtnListener != null) {
                    if (rightBtnListener instanceof OnClickListener) {
                        ((OnClickListener) rightBtnListener).onClick(dialog, v);
                    } else {
                        rightBtnListener.onClick(v);
                    }
                }
            }
        });
        return this;
    }

    public PLVConfirmDialog setIsNeedLeftBtn(boolean isNeedLeftBtn) {
        if (!isNeedLeftBtn) {
            if (plvSplitView != null) {
                plvSplitView.setVisibility(View.GONE);
            }
            plvLeftConfirmBtn.setVisibility(View.GONE);
        } else {
            if (plvSplitView != null) {
                plvSplitView.setVisibility(View.VISIBLE);
            }
            plvLeftConfirmBtn.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public PLVConfirmDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public void setOnWindowFocusChangedListener(PLVOnFocusDialog.OnWindowFocusChangeListener listener) {
        dialog.setOnWindowFocusChangedListener(listener);
    }

    /**
     * window是否正在显示
     */
    public boolean isShowing() {
        return dialog.isShowing();
    }

    /**
     * 显示window
     */
    public void show() {
        if (contextWeakReference != null
                && contextWeakReference.get() != null
                && contextWeakReference.get() instanceof Activity
                && !((Activity) contextWeakReference.get()).isFinishing()) {
            dialog.show();
        }
    }

    /**
     * 隐藏window
     */
    public void hide() {
        dialog.dismiss();
    }

    public static abstract class OnClickListener implements View.OnClickListener {
        public abstract void onClick(DialogInterface dialog, View v);

        @Override
        public void onClick(View v) {
        }
    }

    public static class Builder {

        protected final Param param = new Param();

        protected Builder(@NonNull Context context) {
            param.context = context;
        }

        public static Builder context(@NonNull Context context) {
            return new Builder(context);
        }

        public Builder setTitle(String title) {
            param.title = title;
            return this;
        }

        public Builder setTitle(int titleResId) {
            param.title = param.context.getString(titleResId);
            return this;
        }

        public Builder setContent(String content) {
            param.content = content;
            return this;
        }

        public Builder setContent(int contentResId) {
            param.content = param.context.getString(contentResId);
            return this;
        }

        public Builder setLeftButtonText(String leftBtnText) {
            param.leftBtnText = leftBtnText;
            return this;
        }

        public Builder setLeftButtonText(int leftBtnTextResId) {
            param.leftBtnText = param.context.getString(leftBtnTextResId);
            return this;
        }

        public Builder setRightButtonText(String rightBtnText) {
            param.rightBtnText = rightBtnText;
            return this;
        }

        public Builder setRightButtonText(int rightBtnTextResId) {
            param.rightBtnText = param.context.getString(rightBtnTextResId);
            return this;
        }

        public Builder setTitleVisibility(int visibility) {
            param.titleVisibility = visibility;
            return this;
        }

        public Builder setContentVisibility(int visibility) {
            param.contentVisibility = visibility;
            return this;
        }

        public Builder setLeftBtnListener(OnClickListener listener) {
            param.leftBtnListener = listener;
            return this;
        }

        public Builder setRightBtnListener(OnClickListener listener) {
            param.rightBtnListener = listener;
            return this;
        }

        public Builder setIsNeedLeftBtn(boolean show) {
            param.showLeftButton = show;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            param.cancelable = cancelable;
            return this;
        }

        public PLVConfirmDialog build() {
            return param.initTo(new PLVConfirmDialog(param.context));
        }

        public void show() {
            build().show();
        }

        protected static class Param {
            public Context context;
            public String title;
            public String content;
            public int titleVisibility = View.VISIBLE;
            public int contentVisibility = View.VISIBLE;
            public String leftBtnText = PLVAppUtils.getString(R.string.plv_common_dialog_click_wrong);
            public String rightBtnText;
            public OnClickListener leftBtnListener = hideDialogOnClickListener;
            public OnClickListener rightBtnListener;
            public boolean showLeftButton = true;
            public boolean cancelable = true;

            public PLVConfirmDialog initTo(PLVConfirmDialog dialog) {
                dialog.setTitle(title);
                dialog.setContent(content);
                dialog.setTitleVisibility(titleVisibility);
                dialog.setContentVisibility(contentVisibility);
                dialog.setLeftButtonText(leftBtnText);
                dialog.setRightButtonText(rightBtnText);
                dialog.setLeftBtnListener(leftBtnListener);
                dialog.setRightBtnListener(rightBtnListener);
                dialog.setIsNeedLeftBtn(showLeftButton);
                dialog.setCancelable(cancelable);
                return dialog;
            }

            private static final OnClickListener hideDialogOnClickListener = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, View v) {
                    dialog.dismiss();
                }
            };
        }
    }
}
