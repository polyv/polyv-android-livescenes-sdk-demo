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

import com.easefun.polyv.livecommon.module.modules.streamer.view.ui.PLVDowngradePreferenceCardView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSAPushDowngradePreferenceLayout extends FrameLayout {

    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.RIGHT;
    private static final int MENU_DRAWER_SIZE_PORT = ConvertUtils.dp2px(350);
    private static final int MENU_DRAWER_SIZE_LAND = ConvertUtils.dp2px(300);

    private final Lazy<AbsPushDowngradeLayout> portLayout = new Lazy<AbsPushDowngradeLayout>() {
        @Override
        public AbsPushDowngradeLayout onLazyInit() {
            return new PushDowngradeLayoutPort(getContext());
        }
    };
    private final Lazy<AbsPushDowngradeLayout> landLayout = new Lazy<AbsPushDowngradeLayout>() {
        @Override
        public AbsPushDowngradeLayout onLazyInit() {
            return new PushDowngradeLayoutLand(getContext());
        }
    };

    private PLVMenuDrawer menuDrawer;

    private OnViewActionListener onViewActionListener;

    public PLVSAPushDowngradePreferenceLayout(@NonNull Context context) {
        super(context);
    }

    public PLVSAPushDowngradePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PLVSAPushDowngradePreferenceLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
        getLayoutImpl().updateCurrentPreference();
    }

    public PLVSAPushDowngradePreferenceLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    private boolean isPortrait() {
        return PLVScreenUtils.isPortrait(getContext());
    }

    private AbsPushDowngradeLayout getLayoutImpl() {
        return isPortrait() ? portLayout.get() : landLayout.get();
    }

    private abstract class AbsPushDowngradeLayout extends FrameLayout {

        protected PLVDowngradePreferenceCardView pushDowngradePreferenceQualityCardView;
        protected PLVDowngradePreferenceCardView pushDowngradePreferenceFluencyCardView;

        public AbsPushDowngradeLayout(@NonNull Context context) {
            super(context);
        }

        protected final void setOnClickListener() {
            pushDowngradePreferenceQualityCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onDowngradePreferenceChanged(PLVPushDowngradePreference.PREFER_BETTER_QUALITY);
                        updateCurrentPreference();
                    }
                }
            });
            pushDowngradePreferenceFluencyCardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onDowngradePreferenceChanged(PLVPushDowngradePreference.PREFER_BETTER_FLUENCY);
                        updateCurrentPreference();
                    }
                }
            });
        }

        protected final void updateCurrentPreference() {
            if (onViewActionListener == null) {
                return;
            }
            pushDowngradePreferenceQualityCardView.setSelected(onViewActionListener.getCurrentDowngradePreference() == PLVPushDowngradePreference.PREFER_BETTER_QUALITY);
            pushDowngradePreferenceFluencyCardView.setSelected(onViewActionListener.getCurrentDowngradePreference() == PLVPushDowngradePreference.PREFER_BETTER_FLUENCY);
        }

    }

    private class PushDowngradeLayoutPort extends AbsPushDowngradeLayout {

        public PushDowngradeLayoutPort(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_push_downgrade_preference_layout_port, this);
            pushDowngradePreferenceQualityCardView = findViewById(R.id.plvsa_push_downgrade_preference_quality_card_view);
            pushDowngradePreferenceFluencyCardView = findViewById(R.id.plvsa_push_downgrade_preference_fluency_card_view);
            setOnClickListener();
        }

    }

    private class PushDowngradeLayoutLand extends AbsPushDowngradeLayout {

        public PushDowngradeLayoutLand(@NonNull Context context) {
            super(context);
        }

        {
            LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_push_downgrade_preference_layout_land, this);
            pushDowngradePreferenceQualityCardView = findViewById(R.id.plvsa_push_downgrade_preference_quality_card_view);
            pushDowngradePreferenceFluencyCardView = findViewById(R.id.plvsa_push_downgrade_preference_fluency_card_view);
            setOnClickListener();
        }

    }

    public interface OnViewActionListener {

        @Nullable
        PLVPushDowngradePreference getCurrentDowngradePreference();

        void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference);

    }

}
