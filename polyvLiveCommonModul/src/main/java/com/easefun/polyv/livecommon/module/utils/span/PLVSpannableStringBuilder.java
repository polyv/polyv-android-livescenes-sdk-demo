package com.easefun.polyv.livecommon.module.utils.span;

import android.text.SpannableStringBuilder;

/**
 * @author Hoshiiro
 */
public class PLVSpannableStringBuilder extends SpannableStringBuilder {

    public PLVSpannableStringBuilder() {
    }

    public PLVSpannableStringBuilder(CharSequence text) {
        super(text);
    }

    public PLVSpannableStringBuilder(CharSequence text, int start, int end) {
        super(text, start, end);
    }

    // 避免api21要求
    @Override
    public SpannableStringBuilder append(CharSequence text, Object what, int flags) {
        int start = length();
        append(text);
        setSpan(what, start, length(), flags);
        return this;
    }

    public SpannableStringBuilder appendExclude(CharSequence text, Object what) {
        return append(text, what, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
