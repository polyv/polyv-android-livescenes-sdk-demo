package com.easefun.polyv.livecommon.ui.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

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
                popupWindow.dismiss();
                try {
                    //获取剪贴板管理器
                    ClipboardManager cm = (ClipboardManager) anchor.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", copyContent);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    ToastUtils.showLong("复制成功");
                } catch (Exception e) {
                    PLVCommonLog.exception(e);
                }
            }
        });

        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(PLVScreenUtils.dip2px(anchor.getContext(), 96));
        popupWindow.setBackgroundDrawable(new ColorDrawable()); // 需要设置一个背景setOutsideTouchable(true)才会生效
        popupWindow.setFocusable(true); // 防止点击事件穿透
        popupWindow.setOutsideTouchable(true); // 设置点击外部时取消
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchor, Gravity.TOP | Gravity.START,
                location[0] + (isLeft ? anchor.getWidth() / 2 : -anchor.getWidth() / 2), (int) (location[1] + anchor.getHeight() - ConvertUtils.dp2px(8)));
        return popupWindow;
    }
}
