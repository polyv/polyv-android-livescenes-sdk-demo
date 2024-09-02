package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVFormatUtils.parseColor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSAExternalAudioInputPreferenceLayout extends FrameLayout {

    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.RIGHT;
    private static final int MENU_DRAWER_SIZE_PORT = ConvertUtils.dp2px(384);
    private static final int MENU_DRAWER_SIZE_LAND = ConvertUtils.dp2px(300);

    private boolean hasAlertEnableExternalInput = false;

    private final Lazy<AbsExternalAudioInputLayout> portLayout = new Lazy<AbsExternalAudioInputLayout>() {
        @Override
        public AbsExternalAudioInputLayout onLazyInit() {
            return new ExternalAudioInputLayoutPort(getContext());
        }
    };
    private final Lazy<AbsExternalAudioInputLayout> landLayout = new Lazy<AbsExternalAudioInputLayout>() {
        @Override
        public AbsExternalAudioInputLayout onLazyInit() {
            return new ExternalAudioInputLayoutLand(getContext());
        }
    };

    private PLVMenuDrawer menuDrawer;

    private OnViewActionListener onViewActionListener;

    public PLVSAExternalAudioInputPreferenceLayout(@NonNull Context context) {
        super(context);
    }

    public PLVSAExternalAudioInputPreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVSAExternalAudioInputPreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    isPortrait() ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(getLayoutImpl());
            menuDrawer.setMenuSize(isPortrait() ? MENU_DRAWER_SIZE_PORT : MENU_DRAWER_SIZE_LAND);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
            menuDrawer.openMenu();
        } else {
            menuDrawer.setMenuView(getLayoutImpl());
            menuDrawer.setPosition(isPortrait() ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND);
            menuDrawer.setMenuSize(isPortrait() ? MENU_DRAWER_SIZE_PORT : MENU_DRAWER_SIZE_LAND);
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
        getLayoutImpl().updateCurrentExternalAudioInputEnable(null);
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public PLVSAExternalAudioInputPreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    private boolean isPortrait() {
        return PLVScreenUtils.isPortrait(getContext());
    }

    private AbsExternalAudioInputLayout getLayoutImpl() {
        return isPortrait() ? portLayout.get() : landLayout.get();
    }

    private abstract class AbsExternalAudioInputLayout extends FrameLayout {

        protected View externalAudioInputEnableCardView;
        protected View externalAudioInputDisableCardView;

        public AbsExternalAudioInputLayout(@NonNull Context context) {
            super(context);
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
                                close();
                            }
                        }
                    };
                    if (hasAlertEnableExternalInput) {
                        enableExternalInputRunnable.run();
                    } else {
                        PLVSAConfirmDialog.Builder.context(getContext())
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
                        close();
                    }
                }
            });
        }

        protected abstract void updateCurrentExternalAudioInputEnable(@Nullable Boolean enable);

    }

    private class ExternalAudioInputLayoutPort extends AbsExternalAudioInputLayout {

        public ExternalAudioInputLayoutPort(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_external_audio_input_preference_layout_port, this);
            externalAudioInputEnableCardView = findViewById(R.id.plvsa_external_audio_input_enable_card_view);
            externalAudioInputDisableCardView = findViewById(R.id.plvsa_external_audio_input_disable_card_view);
            setOnClickListener();
        }

        @Override
        protected void updateCurrentExternalAudioInputEnable(@Nullable Boolean enable) {
            if (enable == null) {
                if (onViewActionListener != null) {
                    enable = onViewActionListener.currentIsEnableExternalAudioInput();
                }
            }
            externalAudioInputEnableCardView.setSelected(enable);
            externalAudioInputDisableCardView.setSelected(!enable);
        }
    }

    private class ExternalAudioInputLayoutLand extends AbsExternalAudioInputLayout {

        private TextView externalAudioInputEnableTv;
        private PLVRoundColorView externalAudioInputEnableIndicateView;
        private TextView externalAudioInputDisableTv;
        private PLVRoundColorView externalAudioInputDisableIndicateView;

        public ExternalAudioInputLayoutLand(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_external_audio_input_preference_layout_land, this);
            externalAudioInputEnableCardView = findViewById(R.id.plvsa_external_audio_input_enable_card_view);
            externalAudioInputDisableCardView = findViewById(R.id.plvsa_external_audio_input_disable_card_view);
            externalAudioInputEnableTv = findViewById(R.id.plvsa_external_audio_input_enable_tv);
            externalAudioInputEnableIndicateView = findViewById(R.id.plvsa_external_audio_input_enable_indicate_view);
            externalAudioInputDisableTv = findViewById(R.id.plvsa_external_audio_input_disable_tv);
            externalAudioInputDisableIndicateView = findViewById(R.id.plvsa_external_audio_input_disable_indicate_view);

            setOnClickListener();
        }

        @Override
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
    }

    public interface OnViewActionListener {

        boolean currentIsEnableExternalAudioInput();

        void onEnableExternalAudioInputChanged(boolean enable);

    }

}
