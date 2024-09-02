package com.easefun.polyv.livestreamer.modules.liveroom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerPreferenceCardView;
import com.easefun.polyv.livestreamer.R;
import com.plv.linkmic.model.PLVPushDowngradePreference;

/**
 * @author Hoshiiro
 */
public class PLVLSPushDowngradePreferenceLayout extends FrameLayout {

    private TextView pushDowngradeTitleTv;
    private View pushDowngradeTitleSeparator;
    private PLVStreamerPreferenceCardView pushDowngradePreferenceQualityCardView;
    private TextView pushDowngradePreferenceQualityTitleTv;
    private TextView pushDowngradePreferenceQualityDescTv;
    private PLVStreamerPreferenceCardView pushDowngradePreferenceFluencyCardView;
    private TextView pushDowngradePreferenceFluencyTitleTv;
    private TextView pushDowngradePreferenceFluencyDescTv;

    private OnViewActionListener onViewActionListener;

    public PLVLSPushDowngradePreferenceLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSPushDowngradePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSPushDowngradePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_push_downgrade_layout, this);

        pushDowngradeTitleTv = findViewById(R.id.plvls_push_downgrade_title_tv);
        pushDowngradeTitleSeparator = findViewById(R.id.plvls_push_downgrade_title_separator);
        pushDowngradePreferenceQualityCardView = findViewById(R.id.plvls_push_downgrade_preference_quality_card_view);
        pushDowngradePreferenceQualityTitleTv = findViewById(R.id.plvls_push_downgrade_preference_quality_title_tv);
        pushDowngradePreferenceQualityDescTv = findViewById(R.id.plvls_push_downgrade_preference_quality_desc_tv);
        pushDowngradePreferenceFluencyCardView = findViewById(R.id.plvls_push_downgrade_preference_fluency_card_view);
        pushDowngradePreferenceFluencyTitleTv = findViewById(R.id.plvls_push_downgrade_preference_fluency_title_tv);
        pushDowngradePreferenceFluencyDescTv = findViewById(R.id.plvls_push_downgrade_preference_fluency_desc_tv);

        setOnClickListener();
    }

    private void setOnClickListener() {
        pushDowngradePreferenceQualityCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDowngradePreferenceChanged(PLVPushDowngradePreference.PREFER_BETTER_QUALITY);
                }
                updateCurrentDowngradePreference();
            }
        });

        pushDowngradePreferenceFluencyCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDowngradePreferenceChanged(PLVPushDowngradePreference.PREFER_BETTER_FLUENCY);
                }
                updateCurrentDowngradePreference();
            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            updateCurrentDowngradePreference();
        }
    }

    public PLVLSPushDowngradePreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    private void updateCurrentDowngradePreference() {
        if (onViewActionListener != null) {
            PLVPushDowngradePreference preference = onViewActionListener.getCurrentDowngradePreference();
            pushDowngradePreferenceFluencyCardView.setSelected(preference == PLVPushDowngradePreference.PREFER_BETTER_FLUENCY);
            pushDowngradePreferenceQualityCardView.setSelected(preference == PLVPushDowngradePreference.PREFER_BETTER_QUALITY);
        }
    }

    public interface OnViewActionListener {

        @Nullable
        PLVPushDowngradePreference getCurrentDowngradePreference();

        void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference);

    }

}
