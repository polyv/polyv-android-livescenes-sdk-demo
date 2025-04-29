package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.streameralone.R;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.access.PLVLocalFeature;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSAMoreScreenShareFloatMessageLayout extends FrameLayout {

    private static final int MENU_SIZE_PORT_HEIGHT = ConvertUtils.dp2px(216);
    private static final int MENU_SIZE_LAND_WIDTH = ConvertUtils.dp2px(375);
    private static final int BACKGROUND_ID_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int BACKGROUND_ID_LAND = R.drawable.plvsa_more_ly_shape_land;

    private ConstraintLayout moreScreenShareFloatMessageLayoutRoot;
    private TextView moreScreenShareFloatMessageTv;
    private Switch moreScreenShareFloatMessageSwitch;
    private TextView moreScreenShareFloatMessageHintTv;

    private PLVMenuDrawer menuDrawer;

    private IPLVLiveRoomDataManager liveRoomDataManager;

    public PLVSAMoreScreenShareFloatMessageLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVSAMoreScreenShareFloatMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVSAMoreScreenShareFloatMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_more_screen_share_float_message_layout, this);

        findView();
    }

    private void findView() {
        moreScreenShareFloatMessageLayoutRoot = findViewById(R.id.plvsa_more_screen_share_float_message_layout_root);
        moreScreenShareFloatMessageTv = findViewById(R.id.plvsa_more_screen_share_float_message_tv);
        moreScreenShareFloatMessageSwitch = findViewById(R.id.plvsa_more_screen_share_float_message_switch);
        moreScreenShareFloatMessageHintTv = findViewById(R.id.plvsa_more_screen_share_float_message_hint_tv);

        moreScreenShareFloatMessageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PLVLocalFeature.setStreamerScreenShareFloatWindowV2(isChecked);
            }
        });
    }

    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        moreScreenShareFloatMessageSwitch.setChecked(PLVLocalFeature.isStreamerScreenShareFloatWindowV2());
    }

    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? Position.BOTTOM : Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize(PLVScreenUtils.isPortrait(getContext()) ? MENU_SIZE_PORT_HEIGHT : MENU_SIZE_LAND_WIDTH);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }

                    ViewGroup popupContainer = ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
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
        } else {
            menuDrawer.attachToContainer();
        }

        updateViewWithOrientation();
        menuDrawer.openMenu();
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    private void updateViewWithOrientation() {
        if (PLVScreenUtils.isPortrait(getContext())) {
            moreScreenShareFloatMessageLayoutRoot.setBackgroundResource(BACKGROUND_ID_PORT);
        } else {
            moreScreenShareFloatMessageLayoutRoot.setBackgroundResource(BACKGROUND_ID_LAND);
        }
    }
}
