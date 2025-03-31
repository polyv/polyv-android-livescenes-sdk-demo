package com.easefun.polyv.streameralone.modules.liveroom;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.google.android.flexbox.FlexboxLayout;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

public class PLVSASettingMoreLayout {
    private PopupWindow popupWindow;
    private View view;
    private PLVRoundRectLayout settingMoreLy;
    private ViewGroup widgetRoundLy;
    private FlexboxLayout flexboxLayout;

    public PLVSASettingMoreLayout(View anchorView) {
        popupWindow = new PopupWindow(anchorView.getContext());
        this.view = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plvsa_live_room_setting_more_layout, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        PLVBlurUtils.initBlurView((PLVBlurView) this.view.findViewById(R.id.blur_ly));
        settingMoreLy = this.view.findViewById(R.id.plv_setting_more_ly);
        widgetRoundLy = this.view.findViewById(R.id.plv_widget_round_ly);
        settingMoreLy.setOnOrientationChangedListener(new PLVRoundRectLayout.OnOrientationChangedListener() {
            @Override
            public void onChanged(boolean isPortrait) {
                if (isPortrait) {
                    onPortrait();
                } else {
                    onLandscape();
                }
            }
        });
        flexboxLayout = this.view.findViewById(R.id.plvsa_setting_action_scroll_container);
    }

    public void addItem(View itemView) {
        if (itemView.getParent() != null) {
            ((ViewGroup) itemView.getParent()).removeView(itemView);
        }
        flexboxLayout.addView(itemView);
    }

    public View removePreviousItem() {
        if (flexboxLayout.getChildCount() > 0) {
            View view = flexboxLayout.getChildAt(0);
            flexboxLayout.removeView(view);
            return view;
        }
        return null;
    }

    public boolean hasItem() {
        return flexboxLayout.getChildCount() > 0;
    }

    public void onPortrait() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ConvertUtils.dp2px(202);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        widgetRoundLy.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams contentLyParams = (FrameLayout.LayoutParams) settingMoreLy.getLayoutParams();
        contentLyParams.height = ConvertUtils.dp2px(202);
        contentLyParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        contentLyParams.gravity = Gravity.BOTTOM;
        settingMoreLy.setLayoutParams(contentLyParams);
    }

    public void onLandscape() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.gravity = Gravity.RIGHT;
        widgetRoundLy.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams contentLyParams = (FrameLayout.LayoutParams) settingMoreLy.getLayoutParams();
        contentLyParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        contentLyParams.width = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        contentLyParams.gravity = Gravity.RIGHT;
        settingMoreLy.setLayoutParams(contentLyParams);
    }

    public void show() {
        if (ScreenUtils.isPortrait()) {
            onPortrait();
        } else {
            onLandscape();
        }
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }
}
