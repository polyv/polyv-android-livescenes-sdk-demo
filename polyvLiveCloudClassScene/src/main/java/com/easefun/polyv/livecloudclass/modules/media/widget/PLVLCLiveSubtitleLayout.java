package com.easefun.polyv.livecloudclass.modules.media.widget;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCLiveSubtitleLayout extends FrameLayout {

    private static final int MARGIN_BOTTOM_PORTRAIT = ConvertUtils.dp2px(40);
    private static final int MARGIN_BOTTOM_LANDSCAPE = ConvertUtils.dp2px(80);

    private PLVRoundRectConstraintLayout liveSubtitleContainer;
    private TextView liveSubtitleTv;

    public PLVLCLiveSubtitleLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLCLiveSubtitleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLCLiveSubtitleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_subtitle_layout, this);
        liveSubtitleContainer = findViewById(R.id.plvlc_live_subtitle_container);
        liveSubtitleTv = findViewById(R.id.plvlc_live_subtitle_tv);
    }

    public void initData(IPLVLivePlayerContract.ILivePlayerPresenter presenter) {
        presenter.getData().getRealTimeSubtitle().observe((LifecycleOwner) getContext(), new Observer<PLVLiveSubtitleVO>() {
            @Override
            public void onChanged(@Nullable PLVLiveSubtitleVO subtitleVO) {
                if (subtitleVO == null) {
                    liveSubtitleContainer.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(subtitleVO.getText())) {
                    liveSubtitleContainer.setVisibility(View.GONE);
                } else {
                    liveSubtitleContainer.setVisibility(View.VISIBLE);
                    liveSubtitleTv.setText(subtitleVO.getText());
                }
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MarginLayoutParams layoutParams = (MarginLayoutParams) liveSubtitleContainer.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.bottomMargin = MARGIN_BOTTOM_PORTRAIT;
        } else {
            layoutParams.bottomMargin = MARGIN_BOTTOM_LANDSCAPE;
        }
        liveSubtitleContainer.setLayoutParams(layoutParams);
    }
}

/**
 * 保留最后2行
 */
class PLVLCLiveSubtitleTextView extends AppCompatTextView {

    public PLVLCLiveSubtitleTextView(Context context) {
        super(context);
    }

    public PLVLCLiveSubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVLCLiveSubtitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        CharSequence text = getText();
        if (text == null || text.length() == 0) {
            super.onDraw(canvas);
            return;
        }
        Layout layout = getLayout();
        if (layout == null) {
            super.onDraw(canvas);
            return;
        }

        int lineCount = layout.getLineCount();
        if (lineCount <= 2) {
            super.onDraw(canvas);
            return;
        }

        int start = layout.getLineStart(lineCount - 2);
        int end = layout.getLineEnd(lineCount - 1);

        StaticLayout staticLayout = StaticLayout.Builder
                .obtain(text, start, end, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight())
                .build();

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        staticLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        if (layout != null && layout.getLineCount() > 2) {
            int lineHeight = getLineHeight();
            int newHeight = lineHeight * 2 + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(getMeasuredWidth(), newHeight);
        }
    }

}