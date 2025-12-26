package com.easefun.polyv.livecommon.module.modules.chatroom.view.widget;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.style.DynamicDrawableSpan;

import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVChatQuoteHintMyselfDrawableSpan extends DynamicDrawableSpan {

    private final GradientDrawable drawable = new GradientDrawable();

    public PLVChatQuoteHintMyselfDrawableSpan() {
        super(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? DynamicDrawableSpan.ALIGN_CENTER : DynamicDrawableSpan.ALIGN_BASELINE);
        drawable.setColor(PLVFormatUtils.parseColor("#3F76FC"));
        drawable.setCornerRadius(ConvertUtils.dp2px(1));
        drawable.setBounds(0, 0, ConvertUtils.dp2px(2), ConvertUtils.dp2px(12));
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

}
