package com.easefun.polyv.livecommon.ui.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;


//复制板弹窗
public class PLVCopyBoardPopupWindow {

    public static PopupWindow show(final View anchor, boolean isLeft, final String copyContent) {
        // 自定义的布局View
        final PopupWindow popupWindow = new PopupWindow();
        View view = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plv_copy_board_popup_layout, null, false);
        view.findViewById(R.id.long_press_copy_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(popupWindow, anchor.getContext(), copyContent, null);
            }
        });

        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(PLVScreenUtils.dip2px(anchor.getContext(), 96));
        popupWindow.setBackgroundDrawable(new ColorDrawable()); // 需要设置一个背景setOutsideTouchable(true)才会生效
        popupWindow.setFocusable(true); // 防止点击事件穿透
        popupWindow.setOutsideTouchable(true); // 设置点击外部时取消
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        popupWindow.showAtLocation(anchor, Gravity.TOP | Gravity.START,
                location[0] + (isLeft ? anchor.getWidth() / 2 : -anchor.getWidth() / 2), (int) (location[1] + anchor.getHeight() - ConvertUtils.dp2px(8)));
        return popupWindow;
    }

    public static PopupWindow showAndAnswer(final View anchor, boolean isLeft, @Nullable final String copyContent, final View.OnClickListener clickListener) {
        return showAndAnswer(anchor, isLeft, false, copyContent, clickListener);
    }

    public static PopupWindow showAndAnswer(final View anchor, boolean isLeft, boolean onlyShowCopyItem, @Nullable final String copyContent, final View.OnClickListener clickListener) {
        return showAndAnswer(anchor, isLeft, 0, onlyShowCopyItem, copyContent, R.drawable.plv_cp_ly_corner_bg, R.drawable.plv_inverted_triangle_layer_list, Color.parseColor("#F0F1F5"), clickListener);
    }

    public static PopupWindow showAndAnswer(final View anchor, boolean isLeft, int parentY, @Nullable final String copyContent, @DrawableRes int bgResId, @DrawableRes int triangleResId, @ColorInt int textColor, final View.OnClickListener clickListener) {
        return showAndAnswer(anchor, isLeft, parentY, false, copyContent, bgResId, triangleResId, textColor, clickListener);
    }

    public static PopupWindow showAndAnswer(final View anchor, boolean isLeft, int parentY, boolean onlyShowCopyItem, @Nullable final String copyContent, @DrawableRes int bgResId, @DrawableRes int triangleResId, @ColorInt int textColor, final View.OnClickListener clickListener) {
        // 自定义的布局View
        final PopupWindow popupWindow = new PopupWindow();
        View view = LayoutInflater.from(anchor.getContext()).inflate(R.layout.plv_copy_answer_board_popup_layout, null, false);
        View copyAnswerParentLy = view.findViewById(R.id.copy_answer_parent_ly);
        copyAnswerParentLy.setBackgroundResource(bgResId);
        View copyTv = view.findViewById(R.id.long_press_copy_tv);
        ((TextView) copyTv).setTextColor(textColor);
        copyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(popupWindow, anchor.getContext(), copyContent, clickListener);
            }
        });
        View splitView = view.findViewById(R.id.split_view);
        View answerTv = view.findViewById(R.id.long_press_answer_tv);
        ((TextView) answerTv).setTextColor(textColor);
        answerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                if (clickListener instanceof CopyBoardClickListener) {
                    ((CopyBoardClickListener) clickListener).onClickAnswerButton();
                } else if (clickListener != null) {
                    clickListener.onClick(v);
                }
            }
        });
        View invertedTriangleView = view.findViewById(R.id.inverted_triangle_view);
        invertedTriangleView.setBackgroundResource(triangleResId);
        if (copyContent == null) {
            copyTv.setVisibility(View.GONE);
            splitView.setVisibility(View.GONE);
        }
        if (onlyShowCopyItem) {
            answerTv.setVisibility(View.GONE);
            splitView.setVisibility(View.GONE);
        }

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = view.getMeasuredWidth();

        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(width);
        popupWindow.setBackgroundDrawable(new ColorDrawable()); // 需要设置一个背景setOutsideTouchable(true)才会生效
        popupWindow.setFocusable(true); // 防止点击事件穿透
        popupWindow.setOutsideTouchable(true); // 设置点击外部时取消
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        popupWindow.showAtLocation(anchor, Gravity.TOP | Gravity.START,
                location[0] + (isLeft ? anchor.getWidth() / 2 : -anchor.getWidth() / 2) - width / 2, Math.max(location[1], parentY) - ConvertUtils.dp2px(42) - ConvertUtils.dp2px(4));
        return popupWindow;
    }

    public static void copy(Context context, String copyContent) {
        copy(null, context, copyContent, null);
    }

    private static void copy(PopupWindow popupWindow, Context context, String copyContent, View.OnClickListener listener) {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyContent);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            boolean isShowToast = true;
            if (listener instanceof CopyBoardClickListener) {
                isShowToast = !((CopyBoardClickListener) listener).onClickCopyButton();
            }
            if (isShowToast) {
                ToastUtils.showLong("复制成功");
            }
        } catch (Exception e) {
            PLVCommonLog.exception(e);
        }
    }

    public static abstract class CopyBoardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }

        public abstract void onClickAnswerButton();

        public abstract boolean onClickCopyButton();
    }
}
