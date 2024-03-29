package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.PLVMarqueeTextView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import org.xml.sax.XMLReader;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PLVECBulletinView extends FrameLayout {
    private static final String TAG = "PLVECBulletinView";
    private PLVMarqueeTextView gonggaoTv;
    private TextView gonggaoIc;
    private int gonggaoLyWidth;
    private int gonggaoTvWidth;

    private OnVisibilityChangedListener onVisibilityChangedListener;

    private Disposable gonggaoCdDisposable;

    public PLVECBulletinView(@NonNull Context context) {
        this(context, null);
    }

    public PLVECBulletinView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECBulletinView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_chat_bulletin_layout, this, true);
        gonggaoTv = findViewById(R.id.bulletin_tv);
        gonggaoIc = findViewById(R.id.bulletin_ic);
        gonggaoTv.requestFocus();
        gonggaoTv.setSelected(true);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (onVisibilityChangedListener != null) {
            onVisibilityChangedListener.onChanged(visibility == View.VISIBLE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeBulletinCountDown();
        super.onDetachedFromWindow();
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener onVisibilityChangedListener) {
        this.onVisibilityChangedListener = onVisibilityChangedListener;
    }

    public void acceptBulletinMessage(final PolyvBulletinVO bulletinVO) {
        removeBulletinCountDown();
        post(new Runnable() {
            @Override
            public void run() {
                if (gonggaoLyWidth == 0) {
                    gonggaoLyWidth = getWidth();
                }
                if (gonggaoTvWidth == 0) {
                    gonggaoTvWidth = gonggaoTv.getWidth();
                }
                boolean isVisibleStatus = getVisibility() == View.VISIBLE;
                setVisibility(View.VISIBLE);
                clearAnimation();
                if (!isVisibleStatus) {
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 0f, 1f);
                    scaleAnimation.setDuration(200);
                    startAnimation(scaleAnimation);
                }
                gonggaoTv.setVisibility(View.INVISIBLE);
                gonggaoTv.setText(convertToHtml(bulletinVO.getContent()));
                gonggaoTv.setMovementMethod(LinkMovementMethod.getInstance());
                gonggaoTv.setOnGetRollDurationListener(new PLVMarqueeTextView.OnGetRollDurationListener() {
                    @Override
                    public void onFirstGetRollDuration(int rollDuration) {
                        bulletinCountDown(rollDuration + gonggaoTv.getScrollFirstDelay());
                    }
                });
                gonggaoTv.stopScroll();
                if (gonggaoTv.calculateScrollingLen() < gonggaoTvWidth) {
                    getLayoutParams().width = gonggaoTv.calculateScrollingLen() + gonggaoIc.getWidth() + ConvertUtils.dp2px(24 + 4);
                    requestLayout();
                    invalidate();
                    gonggaoTv.setVisibility(VISIBLE);
                    bulletinCountDown(gonggaoTv.getRndDuration());
                } else {
                    getLayoutParams().width = gonggaoLyWidth;
                    requestLayout();
                    invalidate();
                    gonggaoTv.startScroll();
                }
            }
        });
    }

    private CharSequence getClickableHtml(String html) {
        Spanned spannedHtml = Html.fromHtml(html);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        if (urls != null) {
            for (final URLSpan span : urls) {
                setLinkClickable(clickableHtmlBuilder, span);
            }
        }
        return clickableHtmlBuilder;
    }

    private void setLinkClickable(SpannableStringBuilder clickableHtmlBuilder, final URLSpan urlSpan) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);
        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(View view) {
                PLVCommonLog.d(TAG, "clickableSpan click: " + urlSpan.getURL());
            }
        };
        clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
    }

    private void bulletinCountDown(long ms) {
        gonggaoCdDisposable = Observable.timer(ms, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        removeBulletin();
                    }
                });
    }

    private void removeBulletinCountDown() {
        if (gonggaoCdDisposable != null) {
            gonggaoCdDisposable.dispose();
        }
    }

    public void removeBulletin() {
        removeBulletinCountDown();
        post(new Runnable() {
            @Override
            public void run() {
                gonggaoTv.setVisibility(View.INVISIBLE);
                gonggaoTv.stopScroll();
                boolean isVisibleStatus = getVisibility() == View.VISIBLE;
                setVisibility(View.INVISIBLE);
                clearAnimation();
                if (isVisibleStatus) {
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
                    scaleAnimation.setDuration(200);
                    startAnimation(scaleAnimation);
                }
            }
        });
    }

    private Spanned convertToHtml(String origin) {
        final Html.TagHandler tagHandler = new Html.TagHandler() {
            @Override
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                if ("img".equalsIgnoreCase(tag)) {
                    ImageSpan[] spans = output.getSpans(0, output.length(), ImageSpan.class);
                    if (spans != null) {
                        for (ImageSpan span : spans) {
                            output.replace(output.getSpanStart(span), output.getSpanEnd(span), getString(R.string.plvec_live_detail_bulletin_image_alt_text));
                            output.removeSpan(span);
                        }
                    }
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(origin, Html.FROM_HTML_MODE_COMPACT, null, tagHandler);
        } else {
            return Html.fromHtml(origin, null, tagHandler);
        }
    }

    public interface OnVisibilityChangedListener {
        void onChanged(boolean isVisible);
    }
}
