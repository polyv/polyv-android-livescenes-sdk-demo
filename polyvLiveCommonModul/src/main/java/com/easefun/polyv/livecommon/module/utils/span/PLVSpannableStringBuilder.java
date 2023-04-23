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

    @Override
    public PLVSpannableStringBuilder append(char text) {
        super.append(text);
        return this;
    }

    @Override
    public PLVSpannableStringBuilder append(CharSequence text) {
        super.append(text);
        return this;
    }

    @Override
    public PLVSpannableStringBuilder append(CharSequence text, int start, int end) {
        super.append(text, start, end);
        return this;
    }

    // 避免api21要求
    @Override
    public PLVSpannableStringBuilder append(CharSequence text, Object what, int flags) {
        int start = length();
        append(text);
        setSpan(what, start, length(), flags);
        return this;
    }

    public PLVSpannableStringBuilder appendExclude(CharSequence text, Object what) {
        return append(text, what, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
