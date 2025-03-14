package com.easefun.polyv.livecommon.ui.widget;


import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerConfig;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 播放设置弹窗
 */
public class PLVPlaySettingPopupWindow {
    private PopupWindow popupWindow;
    private View view;
    private TextView playSettingTv;
    private View line1Tv;
    private TextView exitPageTv;
    private TextView goHomeTv;
    private ViewGroup widgetRoundLy;
    private ViewGroup contentLy;
    private PLVOrientationSensibleLinearLayout orientLy;
    private boolean isUseBlackStyle;

    public PLVPlaySettingPopupWindow(View anchorView) {
        popupWindow = new PopupWindow(anchorView.getContext());
        this.view = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plv_play_setting_popup_window, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        widgetRoundLy = this.view.findViewById(R.id.plv_widget_round_ly);
        contentLy = this.view.findViewById(R.id.plv_play_setting_content_ly);
        orientLy = this.view.findViewById(R.id.plv_play_setting_orient_ly);
        playSettingTv = this.view.findViewById(R.id.plv_play_setting_tv);
        line1Tv = this.view.findViewById(R.id.plv_line_1);
        exitPageTv = this.view.findViewById(R.id.plv_play_setting_exit_page_tv);
        goHomeTv = this.view.findViewById(R.id.plv_play_setting_go_home_tv);

        Switch exitPageSw = this.view.findViewById(R.id.plv_play_setting_exit_page_sw);
        final Switch goHomeSw = this.view.findViewById(R.id.plv_play_setting_go_home_sw);
        exitPageSw.setChecked(PLVFloatingPlayerConfig.isAutoFloatingWhenExitPage());
        goHomeSw.setChecked(PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome());
        if (!PLVFloatingPlayerConfig.isAutoFloatingWhenExitPage()) {
            ((View) goHomeSw.getParent()).setVisibility(View.GONE);
        }
        exitPageSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PLVFloatingPlayerConfig.setIsAutoFloatingWhenExitPage(isChecked);
                ((View) goHomeSw.getParent()).setVisibility(PLVFloatingPlayerConfig.isAutoFloatingWhenExitPage() ? View.VISIBLE : View.GONE);
            }
        });
        goHomeSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PLVFloatingPlayerConfig.setIsAutoFloatingWhenGoHome(isChecked);
            }
        });

        orientLy.setOnLandscape(new Runnable() {
            @Override
            public void run() {
                onLandscape();
            }
        });
        orientLy.setOnPortrait(new Runnable() {
            @Override
            public void run() {
                onPortrait();
            }
        });
        if (isUseBlackStyle) {
            setUseBlackStyle();
        }
    }

    public void onPortrait() {
        if (!isUseBlackStyle) {
            return;
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ConvertUtils.dp2px(266);
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        widgetRoundLy.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams contentLyParams = (FrameLayout.LayoutParams) contentLy.getLayoutParams();
        contentLyParams.height = ConvertUtils.dp2px(266);
        contentLyParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        contentLyParams.gravity = Gravity.BOTTOM;
        contentLy.setLayoutParams(contentLyParams);
    }

    public void onLandscape() {
        if (!isUseBlackStyle) {
            return;
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) widgetRoundLy.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.gravity = Gravity.RIGHT;
        widgetRoundLy.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams contentLyParams = (FrameLayout.LayoutParams) contentLy.getLayoutParams();
        contentLyParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        contentLyParams.width = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        contentLyParams.gravity = Gravity.RIGHT;
        contentLy.setLayoutParams(contentLyParams);
    }

    public void setUseBlackStyle() {
        this.isUseBlackStyle = true;
        contentLy.setBackground(null);
        playSettingTv.setTextColor(PLVFormatUtils.parseColor("#FFFFFF"));
        line1Tv.setVisibility(View.GONE);
        exitPageTv.setTextColor(PLVFormatUtils.parseColor("#FFFFFF"));
        goHomeTv.setTextColor(PLVFormatUtils.parseColor("#FFFFFF"));
        widgetRoundLy.setVisibility(View.VISIBLE);
        PLVBlurUtils.initBlurView((PLVBlurView) this.view.findViewById(R.id.blur_ly));
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
