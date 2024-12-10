package com.easefun.polyv.livecommon.ui.widget;


import static com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil.LANGUAGE_CHINESE_SIMPLIFIED;
import static com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil.LANGUAGE_CHINESE_TRADITIONAL;
import static com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil.LANGUAGE_EN;
import static com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil.LANGUAGE_JAPANESE;
import static com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil.LANGUAGE_KOREAN;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;

/**
 * 语言切换弹窗
 */
public class PLVLanguageSwitchPopupWindow {
    private String channelId;
    private PopupWindow popupWindow;
    private View view;

    public PLVLanguageSwitchPopupWindow(View anchorView) {
        popupWindow = new PopupWindow(anchorView.getContext());
        this.view = PLVViewInitUtils.initPopupWindow(anchorView, R.layout.plv_language_switch_popup_window, popupWindow, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView switchZhTv = this.view.findViewById(R.id.plv_language_switch_zh_tv);
        TextView switchEnTv = this.view.findViewById(R.id.plv_language_switch_en_tv);
        TextView switchTCTv = this.view.findViewById(R.id.plv_language_switch_tc_tv);
        TextView switchJPTv = this.view.findViewById(R.id.plv_language_switch_jp_tv);
        TextView switchKOTv = this.view.findViewById(R.id.plv_language_switch_ko_tv);
        TextView cancelTv = this.view.findViewById(R.id.plv_language_switch_cancel_tv);
        switchZhTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSwitch(LANGUAGE_CHINESE_SIMPLIFIED);
            }
        });
        switchTCTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                handleSwitch(LANGUAGE_CHINESE_TRADITIONAL);
            }
        });
        switchJPTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSwitch(LANGUAGE_JAPANESE);
            }
        });
        switchKOTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSwitch(LANGUAGE_KOREAN);
            }
        });
        switchEnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSwitch(LANGUAGE_EN);
            }
        });
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(String channelId) {
        this.channelId = channelId;
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    private void handleSwitch(final int languageType) {
        if (PLVLanguageUtil.isEqualsLanguage(languageType)) {
            dismiss();
            return;
        }
        new AlertDialog.Builder(view.getContext())
                .setMessage(R.string.plv_live_language_switch_hint)
                .setPositiveButton(R.string.plv_common_dialog_confirm_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        PLVLanguageUtil.recreateWithLanguage(channelId, languageType, (Activity) view.getContext());
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, null)
                .show();
    }
}
