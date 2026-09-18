package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.player.IPLVPlayErrorView;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/** 播放错误提示条，支持文案内的切换线路和刷新操作。 */
public class PLVPlayErrorView extends FrameLayout implements IPLVPlayErrorView {
    private final TextView errorTv;
    private final View errorCard;
    private String errorText = "";
    private boolean showChangeLines;
    private boolean showRefresh;
    private OnClickListener changeLinesListener;
    private OnClickListener refreshListener;

    public PLVPlayErrorView(@NonNull Context context) {
        this(context, null);
    }

    public PLVPlayErrorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVPlayErrorView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.plv_player_play_error_view, this, true);
        errorCard = findViewById(R.id.plv_play_error_card);
        errorTv = findViewById(R.id.plv_play_error_text);
        errorTv.setHighlightColor(Color.TRANSPARENT);
        findViewById(R.id.plv_play_error_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewVisibility(GONE);
            }
        });
    }

    @Override
    public void setPlaceHolderImg(@DrawableRes int resId) {
        // 错误提示条不再显示占位图。
    }

    public void setCenterLayout() {
        LayoutParams params = (LayoutParams) errorCard.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.topMargin = 0;
        params.bottomMargin = ConvertUtils.dp2px(88);
        errorCard.setLayoutParams(params);
    }

    @Override
    public void setPlaceHolderText(String text) {
        errorText = text == null ? "" : text;
        updateErrorText();
    }

    @Override
    public void setPlaceHolderTextSize(float size) {
        errorTv.setTextSize(size);
    }

    @Override
    public void setChangeLinesViewVisibility(int visibility) {
        showChangeLines = visibility == VISIBLE;
        updateErrorText();
    }

    @Override
    public void setRefreshViewVisibility(int visibility) {
        showRefresh = visibility == VISIBLE;
        updateErrorText();
    }

    public void setOnChangeLinesViewClickListener(OnClickListener listener) {
        changeLinesListener = listener;
    }

    public void setOnRefreshViewClickListener(OnClickListener listener) {
        refreshListener = listener;
    }

    @Override
    public void setViewVisibility(int visibility) {
        setVisibility(visibility);
    }

    private void updateErrorText() {
        SpannableString text = new SpannableString(errorText);
        if (showChangeLines) {
            addAction(text, getResources().getString(R.string.plv_player_change_route), true);
        }
        if (showRefresh) {
            addAction(text, getResources().getString(R.string.plv_player_refresh), false);
        }
        errorTv.setText(text);
        boolean hasActions = text.getSpans(0, text.length(), ClickableSpan.class).length > 0;
        errorTv.setMovementMethod(hasActions ? LinkMovementMethod.getInstance() : null);
    }

    private void addAction(SpannableString text, String label, final boolean changeLines) {
        // 匹配完整的本地化操作文案，不依赖固定字数；两个操作各自绑定回调。
        int start = errorText.lastIndexOf(label);
        if (start < 0) {
            return;
        }
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                OnClickListener listener = changeLines ? changeLinesListener : refreshListener;
                if (listener != null) {
                    listener.onClick(widget);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(0xFF4C75F4);
                ds.setUnderlineText(false);
            }
        }, start, start + label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 小窗宽度不足时缩小提示条，让文案换行而不是被裁切。
        int width = ConvertUtils.dp2px(266);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = Math.min(width, Math.max(0, MeasureSpec.getSize(widthMeasureSpec)
                    - getPaddingLeft() - getPaddingRight() - ConvertUtils.dp2px(16)));
        }
        errorCard.getLayoutParams().width = width;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
