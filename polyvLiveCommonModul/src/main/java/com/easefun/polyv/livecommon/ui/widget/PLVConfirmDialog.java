package com.easefun.polyv.livecommon.ui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.expandmenu.utils.DpOrPxUtils;

/**
 * date: 2020-05-21
 * author: hwj
 */
public class PLVConfirmDialog {

    private Dialog dialog;
    private TextView plvConfirmTitle;
    private TextView plvConfirmContent;
    private TextView plvLeftConfirmBtn;
    private TextView plvRightConfirmBtn;
    private View plvSplitView;

    public PLVConfirmDialog(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.plv_confirm_window_layout, null, false);

        RelativeLayout dialogWrapper = new RelativeLayout(context);
        RelativeLayout.LayoutParams rootLp = new RelativeLayout.LayoutParams(DpOrPxUtils.dip2px(context, 228), RelativeLayout.LayoutParams.WRAP_CONTENT);
        rootLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        dialogWrapper.addView(root, rootLp);

        dialog = new AlertDialog.Builder(context)
                .setView(dialogWrapper)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initView(root);
    }

    private void initView(View root) {
        plvConfirmTitle = (TextView) root.findViewById(R.id.plv_confirm_title);
        plvConfirmContent = (TextView) root.findViewById(R.id.plv_confirm_content);
        plvLeftConfirmBtn = (TextView) root.findViewById(R.id.plv_left_confirm_btn);
        plvRightConfirmBtn = (TextView) root.findViewById(R.id.plv_right_confirm_btn);
        plvSplitView = root.findViewById(R.id.plv_split_view);

        plvLeftConfirmBtn.setText(R.string.plv_common_dialog_click_wrong);
        plvLeftConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public PLVConfirmDialog setTitle(String title) {
        plvConfirmTitle.setText(title);
        return this;
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
        plvConfirmContent.setText(dialog.getContext().getString(resId));
        return this;
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
        plvLeftConfirmBtn.setText(dialog.getContext().getString(resId));
        return this;
    }

    public PLVConfirmDialog setRightButtonText(String rightText) {
        plvRightConfirmBtn.setText(rightText);
        return this;
    }

    public PLVConfirmDialog setRightButtonText(@StringRes int resId) {
        plvRightConfirmBtn.setText(dialog.getContext().getString(resId));
        return this;
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

    public PLVConfirmDialog setIsNeedLeftBtn(boolean isNeedRightBtn) {
        if (!isNeedRightBtn) {
            plvSplitView.setVisibility(View.GONE);
            plvLeftConfirmBtn.setVisibility(View.GONE);
        } else {
            plvSplitView.setVisibility(View.VISIBLE);
            plvLeftConfirmBtn.setVisibility(View.VISIBLE);
        }
        return this;
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
        dialog.show();
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
}
