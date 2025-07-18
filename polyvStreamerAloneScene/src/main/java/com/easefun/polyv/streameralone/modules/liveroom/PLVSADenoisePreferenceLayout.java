package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVStreamerPreferenceCardView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSADenoisePreferenceLayout extends FrameLayout {

    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.RIGHT;
    private static final int MENU_DRAWER_SIZE_PORT = ConvertUtils.dp2px(420);
    private static final int MENU_DRAWER_SIZE_LAND = ConvertUtils.dp2px(300);

    private final Lazy<AbsDenoiseLayout> portLayout = new Lazy<AbsDenoiseLayout>() {
        @Override
        public AbsDenoiseLayout onLazyInit() {
            return new DenoiseLayoutPort(getContext());
        }
    };
    private final Lazy<AbsDenoiseLayout> landLayout = new Lazy<AbsDenoiseLayout>() {
        @Override
        public AbsDenoiseLayout onLazyInit() {
            return new DenoiseLayoutLand(getContext());
        }
    };

    private PLVMenuDrawer menuDrawer;

    private OnViewActionListener onViewActionListener;

    public PLVSADenoisePreferenceLayout(@NonNull Context context) {
        super(context);
    }

    public PLVSADenoisePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVSADenoisePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        getLayoutImpl().updateCurrentDenoiseType(null);
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public PLVSADenoisePreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    private boolean isPortrait() {
        return PLVScreenUtils.isPortrait(getContext());
    }

    private AbsDenoiseLayout getLayoutImpl() {
        return isPortrait() ? portLayout.get() : landLayout.get();
    }

    private abstract class AbsDenoiseLayout extends FrameLayout {

        protected PLVStreamerPreferenceCardView denoiseAdaptiveCardView;
        protected PLVStreamerPreferenceCardView denoiseBalanceCardView;
        protected PLVStreamerPreferenceCardView denoiseDefaultCardView;

        public AbsDenoiseLayout(@NonNull Context context) {
            super(context);
        }

        protected final void setOnClickListener() {
            denoiseAdaptiveCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.ADAPTIVE);
                        updateCurrentDenoiseType(PLVLinkMicDenoiseType.ADAPTIVE);
                        close();
                    }
                }
            });
            denoiseBalanceCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.BALANCE);
                        updateCurrentDenoiseType(PLVLinkMicDenoiseType.BALANCE);
                        close();
                    }
                }
            });
            denoiseDefaultCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onDenoiseChanged(PLVLinkMicDenoiseType.DEFAULT);
                        updateCurrentDenoiseType(PLVLinkMicDenoiseType.DEFAULT);
                        close();
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

    }

    private class DenoiseLayoutPort extends AbsDenoiseLayout {

        public DenoiseLayoutPort(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_denoise_preference_layout_port, this);
            denoiseAdaptiveCardView = findViewById(R.id.plvsa_denoise_adaptive_card_view);
            denoiseBalanceCardView = findViewById(R.id.plvsa_denoise_balance_card_view);
            denoiseDefaultCardView = findViewById(R.id.plvsa_denoise_default_card_view);
            setOnClickListener();
        }

    }

    private class DenoiseLayoutLand extends AbsDenoiseLayout {

        public DenoiseLayoutLand(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_denoise_preference_layout_land, this);
            denoiseAdaptiveCardView = findViewById(R.id.plvsa_denoise_adaptive_card_view);
            denoiseBalanceCardView = findViewById(R.id.plvsa_denoise_balance_card_view);
            denoiseDefaultCardView = findViewById(R.id.plvsa_denoise_default_card_view);
            setOnClickListener();
        }

    }

    public interface OnViewActionListener {

        @Nullable
        PLVLinkMicDenoiseType getCurrentDenoiseType();

        void onDenoiseChanged(@NonNull PLVLinkMicDenoiseType denoiseType);

    }

}
