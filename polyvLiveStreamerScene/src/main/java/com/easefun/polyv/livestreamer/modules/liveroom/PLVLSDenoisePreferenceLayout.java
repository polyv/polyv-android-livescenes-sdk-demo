package com.easefun.polyv.livestreamer.modules.liveroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerPreferenceCardView;
import com.easefun.polyv.livestreamer.R;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;

/**
 * @author Hoshiiro
 */
public class PLVLSDenoisePreferenceLayout extends FrameLayout {
    protected PLVStreamerPreferenceCardView denoiseAdaptiveCardView;
    protected PLVStreamerPreferenceCardView denoiseBalanceCardView;
    protected PLVStreamerPreferenceCardView denoiseDefaultCardView;

    private OnViewActionListener onViewActionListener;

    public PLVLSDenoisePreferenceLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSDenoisePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSDenoisePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_denoise_preference_layout_land, this);
        denoiseAdaptiveCardView = findViewById(R.id.plvls_denoise_adaptive_card_view);
        denoiseBalanceCardView = findViewById(R.id.plvls_denoise_balance_card_view);
        denoiseDefaultCardView = findViewById(R.id.plvls_denoise_default_card_view);
        setOnClickListener();
    }

    protected final void setOnClickListener() {
        denoiseAdaptiveCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.ADAPTIVE);
                    updateCurrentDenoiseType(PLVLinkMicDenoiseType.ADAPTIVE);
                }
            }
        });
        denoiseBalanceCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.BALANCE);
                    updateCurrentDenoiseType(PLVLinkMicDenoiseType.BALANCE);
                }
            }
        });
        denoiseDefaultCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.DEFAULT);
                    updateCurrentDenoiseType(PLVLinkMicDenoiseType.DEFAULT);
                }
            }
        });
    }

    protected final void updateCurrentDenoiseType(@Nullable PLVLinkMicDenoiseType denoiseType) {
        if (denoiseType == null) {
            if (onViewActionListener != null) {
                denoiseType = onViewActionListener.getCurrentDenoiseType();
            }
        }
        if (denoiseType == null) {
            return;
        }
        denoiseAdaptiveCardView.setSelected(denoiseType == PLVLinkMicDenoiseType.ADAPTIVE);
        denoiseBalanceCardView.setSelected(denoiseType == PLVLinkMicDenoiseType.BALANCE);
        denoiseDefaultCardView.setSelected(denoiseType == PLVLinkMicDenoiseType.DEFAULT);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            updateCurrentDenoiseType(null);
        }
    }

    public PLVLSDenoisePreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public interface OnViewActionListener {

        @Nullable
        PLVLinkMicDenoiseType getCurrentDenoiseType();

        void onDenoiseChanged(@NonNull PLVLinkMicDenoiseType denoiseType);

    }

}
