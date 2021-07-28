package com.easefun.polyv.livecommon.ui.widget.gif;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.R;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifTextView;

public class GifSpanTextView extends GifTextView {
    private static final String TAG = "GifSpanTextView";
    private static final String WEB_PATTERN = "((http[s]{0,1}://)?[a-zA-Z0-9\\.\\-]+\\." +
            "([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)|(www.[a-zA-Z0-9\\" +
            ".\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?)";

    private GifSpanChangeWatcher mGifSpanChangeWatcher;

    private WebLinkClickListener webLinkClickListener;

    public GifSpanTextView(Context context) {
        super(context);
        initGifSpanChangeWatcher();
    }

    public GifSpanTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGifSpanChangeWatcher();
    }

    public GifSpanTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initGifSpanChangeWatcher();
    }

    private void initGifSpanChangeWatcher() {
        mGifSpanChangeWatcher = new GifSpanChangeWatcher(this);
        addTextChangedListener(mGifSpanChangeWatcher);
    }

    public void setTextInner(CharSequence text, boolean specialType) {
        PLVCommonLog.d(TAG, "content :" + text);
        if (specialType) {
            setContentHttpPattern(text);
        } else {
            setText(text);
        }
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        PLVCommonLog.d(TAG, "set text :" + text);
        type = TextView.BufferType.EDITABLE;
        CharSequence oldText = getText();
        if (!TextUtils.isEmpty(text) && text.length() >= 50) {
            PLVCommonLog.d(TAG, "set text :" + text);
        }
        if (!TextUtils.isEmpty(oldText) && oldText instanceof Spannable) {
            Spannable sp = (Spannable) oldText;
            final GifImageSpan[] spans = sp.getSpans(0, sp.length(), GifImageSpan.class);
            for (GifImageSpan span : spans) {
                span.getDrawable().setCallback(null);
            }

            final GifSpanChangeWatcher[] watchers = sp.getSpans(0, sp.length(), GifSpanChangeWatcher.class);
            for (GifSpanChangeWatcher watcher : watchers) {
                sp.removeSpan(watcher);
            }
        }

        if (!TextUtils.isEmpty(text) && text instanceof Spannable) {
            Spannable sp = (Spannable) text;
            final GifImageSpan[] spans = sp.getSpans(0, sp.length(), GifImageSpan.class);
            final int count = spans.length;
            for (int i = 0; i < count; i++) {
                spans[i].getDrawable().setCallback(this);
            }

            final GifSpanChangeWatcher[] watchers = sp.getSpans(0, sp.length(), GifSpanChangeWatcher.class);
            final int count1 = watchers.length;
            for (int i = 0; i < count1; i++) {
                sp.removeSpan(watchers[i]);
            }

            if (mGifSpanChangeWatcher == null) {
                mGifSpanChangeWatcher = new GifSpanChangeWatcher(this);
                ;
            }

            sp.setSpan(mGifSpanChangeWatcher, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE | (100 << Spanned.SPAN_PRIORITY_SHIFT));
        }

        super.setText(text, type);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > 0 && TextViewLinesUtils.getTextViewLines(this, getMeasuredWidth()) > getMaxLines()
                && getEllipsize() == TextUtils.TruncateAt.END) {
            int lineEndIndex = getLayout().getLineEnd(getMaxLines() - 1);
            String text = getText().subSequence(0, lineEndIndex - 1) + "...";
            super.setText(text);
        }
    }

    private GifImageSpan getImageSpan(Drawable drawable) {
        GifImageSpan imageSpan = null;
        CharSequence text = getText();
        if (!TextUtils.isEmpty(text) && text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            GifImageSpan[] spans = spanned.getSpans(0, text.length(), GifImageSpan.class);
            if (spans != null && spans.length > 0) {
                for (GifImageSpan span : spans) {
                    if (drawable == span.getDrawable()) {
                        imageSpan = span;
                    }
                }
            }
        }

        return imageSpan;
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        GifImageSpan imageSpan = getImageSpan(drawable);
        if (imageSpan != null) {
            CharSequence text = getText();
            if (!TextUtils.isEmpty(text)) {
                if (text instanceof Editable) {
                    Editable editable = (Editable) text;
                    int start = editable.getSpanStart(imageSpan);
                    int end = editable.getSpanEnd(imageSpan);
                    int flags = editable.getSpanFlags(imageSpan);

                    editable.removeSpan(imageSpan);
                    editable.setSpan(imageSpan, start, end, flags);
                }
            }

        } else {
            super.invalidateDrawable(drawable);
        }
    }


    private void setContentHttpPattern(CharSequence string) {
        SpannableString sp = new SpannableString(string);
        String urlPattern = WEB_PATTERN;
        Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(string);
        int startPoint = 0;
        boolean hasFind = false;
        while (m.find(startPoint)) {
            int endPoint = m.end();
            final String hit = m.group();
            ClickableSpan clickSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    PLVCommonLog.d(TAG, "hit :" + hit);
                    if (webLinkClickListener != null) {
                        webLinkClickListener.webLinkOnClick(hit);
                    }
                }
            };
            sp.setSpan(clickSpan, endPoint - hit.length(), endPoint, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//用Span替换对应长度的url
            sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.link_color)), endPoint - hit.length(), endPoint, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            startPoint = endPoint;
            hasFind = true;
        }
        if (hasFind) {
            setText(sp);
            setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            setText(string);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        int startSelection = getSelectionStart();
        int endSelection = getSelectionEnd();
        if (startSelection != endSelection) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);
    }


    public void setWebLinkClickListener(WebLinkClickListener webLinkClickListener) {
        this.webLinkClickListener = webLinkClickListener;
    }

    public interface WebLinkClickListener {
        void webLinkOnClick(String url);
    }
}