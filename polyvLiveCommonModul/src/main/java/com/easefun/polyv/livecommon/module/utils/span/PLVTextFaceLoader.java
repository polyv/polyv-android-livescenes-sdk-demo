package com.easefun.polyv.livecommon.module.utils.span;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本转表情
 */
public class PLVTextFaceLoader {

    public static CharSequence messageToSpan(CharSequence charSequence, int size, Context context) {
        return messageToSpan(charSequence, size, context, null);
    }

    public static CharSequence messageToSpan(CharSequence charSequence, int size, Context context, List<int[]> spanIndexs) {
        return messageToSpan(charSequence, new int[]{size}, context, spanIndexs)[0];
    }

    public static CharSequence[] messageToSpan(CharSequence charSequence, int[] sizes, Context context) {
        return messageToSpan(charSequence, sizes, context, null);
    }

    public static CharSequence[] messageToSpan(CharSequence charSequence, int[] sizes, Context context, List<int[]> spanIndexs) {
    /**
     * ///暂时保留该代码
     * if (sizes.length > 2) {
     *     throw new RuntimeException("sizes length is incorrect");
     * }
     * if (charSequence instanceof String) {
     *     charSequence = Html.fromHtml((String) charSequence);//html转义，send no transfer，eg: <> -> ""
     * }
     */
        SpannableStringBuilder[] spanArr = new SpannableStringBuilder[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            spanArr[i] = new SpannableStringBuilder(charSequence);
        }
        int start;
        int end;
        Pattern pattern = Pattern.compile("\\[[^\\[]{1,5}\\]");
        Matcher matcher = pattern.matcher(charSequence);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            String group = matcher.group();
            try {
                for (int i = 0; i < spanArr.length; i++) {
                    setSpan(context, group, spanArr[i], sizes[i], start, end);
                }
            } catch (Exception e) {
                continue;
            }
            if (spanIndexs != null) {
                spanIndexs.add(new int[]{start, end});
            }
        }
        return spanArr;
    }

    private static void setSpan(Context context, String group, SpannableStringBuilder span, int size, int start, int end) {
        Drawable drawable = context.getResources().getDrawable(PLVFaceManager.getInstance().getFaceId(group));
        ImageSpan imageSpan = new PLVRelativeImageSpan(drawable, PLVRelativeImageSpan.ALIGN_CENTER);
        drawable.setBounds(0, 0, (int) (size * 1.5), (int) (size * 1.5));
        span.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * 显示带本地表情图片的图文混排
     */
    public static void displayTextImage(CharSequence charSequence, TextView textView) {
        textView.setText(messageToSpan(charSequence, (int) textView.getTextSize(), textView.getContext()));
    }
}
