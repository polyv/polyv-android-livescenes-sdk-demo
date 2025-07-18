package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVFormatUtils.parseColor;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;

/**
 * @author Hoshiiro
 */
public class PLVLSExternalAudioInputPreferenceLayout extends FrameLayout {

    protected View externalAudioInputEnableCardView;
    protected View externalAudioInputDisableCardView;
    private TextView externalAudioInputEnableTv;
    private PLVRoundColorView externalAudioInputEnableIndicateView;
    private TextView externalAudioInputDisableTv;
    private PLVRoundColorView externalAudioInputDisableIndicateView;

    private boolean hasAlertEnableExternalInput = false;

    private OnViewActionListener onViewActionListener;

    public PLVLSExternalAudioInputPreferenceLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSExternalAudioInputPreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSExternalAudioInputPreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_external_audio_input_preference_layout_land, this);
        externalAudioInputEnableCardView = findViewById(R.id.plvls_external_audio_input_enable_card_view);
        externalAudioInputDisableCardView = findViewById(R.id.plvls_external_audio_input_disable_card_view);
        externalAudioInputEnableTv = findViewById(R.id.plvls_external_audio_input_enable_tv);
        externalAudioInputEnableIndicateView = findViewById(R.id.plvls_external_audio_input_enable_indicate_view);
        externalAudioInputDisableTv = findViewById(R.id.plvls_external_audio_input_disable_tv);
        externalAudioInputDisableIndicateView = findViewById(R.id.plvls_external_audio_input_disable_indicate_view);

        setOnClickListener();
    }

    protected final void setOnClickListener() {
        externalAudioInputEnableCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Runnable enableExternalInputRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (onViewActionListener != null) {
                            onViewActionListener.onEnableExternalAudioInputChanged(true);
                            updateCurrentExternalAudioInputEnable(true);
                        }
                    }
                };
                if (hasAlertEnableExternalInput) {
                    enableExternalInputRunnable.run();
                } else {
                    PLVLSConfirmDialog.Builder.context(getContext())
                            .setTitle(R.string.plv_streamer_external_audio_input_enable_alert_title)
                            .setContent(R.string.plv_streamer_external_audio_input_enable_alert_desc)
                            .setLeftButtonText(R.string.plv_streamer_external_audio_input_enable_alert_cancel_text)
                            .setRightButtonText(R.string.plv_streamer_external_audio_input_enable_alert_confirm_text)
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    hasAlertEnableExternalInput = true;
                                    enableExternalInputRunnable.run();
                                }
                            })
                            .show();
                }
            }
        });
        externalAudioInputDisableCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onViewActionListener != null) {
                    onViewActionListener.onEnableExternalAudioInputChanged(false);
                    updateCurrentExternalAudioInputEnable(false);
                }
            }
        });
    }

    protected void updateCurrentExternalAudioInputEnable(@Nullable Boolean enable) {
        if (enable == null) {
            if (onViewActionListener != null) {
                enable = onViewActionListener.currentIsEnableExternalAudioInput();
            }
        }
        final boolean isEnable = Boolean.TRUE.equals(enable);
        externalAudioInputEnableTv.setTextColor(isEnable ? parseColor("#4399FF") : parseColor("#F0F1F5"));
        externalAudioInputDisableTv.setTextColor(isEnable ? parseColor("#F0F1F5") : parseColor("#4399FF"));
        externalAudioInputEnableIndicateView.setVisibility(isEnable ? View.VISIBLE : View.GONE);
        externalAudioInputDisableIndicateView.setVisibility(isEnable ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            updateCurrentExternalAudioInputEnable(null);
        }
    }

    public PLVLSExternalAudioInputPreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public interface OnViewActionListener {

        boolean currentIsEnableExternalAudioInput();

        void onEnableExternalAudioInputChanged(boolean enable);

    }

}
