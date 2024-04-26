package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVSAMoreLinkMicSettingLayout extends FrameLayout implements View.OnClickListener {

    private static final int MENU_SIZE_PORT_HEIGHT = ConvertUtils.dp2px(426);
    private static final int MENU_SIZE_LAND_WIDTH = ConvertUtils.dp2px(375);
    private static final int BACKGROUND_ID_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int BACKGROUND_ID_LAND = R.drawable.plvsa_more_ly_shape_land;
    private static final int TEXT_COLOR_SELECTED = PLVFormatUtils.parseColor("#4399FF");
    private static final int TEXT_COLOR_NOT_SELECTED = PLVFormatUtils.parseColor("#F0F1F5");
    private static final int TEXT_COLOR_NOT_AVAILABLE = PLVFormatUtils.parseColor("#99F0F1F5");

    private ConstraintLayout linkmicTypeSettingLayoutRoot;
    private TextView linkmicTypeSettingTitleTv;
    private TextView linkmicTypeSettingHintTitleTv;
    private TextView linkmicTypeSettingAudioTv;
    private PLVRoundColorView linkmicTypeSettingAudioSelectedIndicateView;
    private TextView linkmicTypeSettingVideoTv;
    private TextView linkmicTypeSettingVideoUnavailableTv;
    private PLVRoundColorView linkmicTypeSettingVideoSelectedIndicateView;
    private TextView linkmicTypeSettingHintSwitchTv;
    private TextView linkmicTypeSettingHintDefaultSettingTv;

    private PLVMenuDrawer menuDrawer;

    @Nullable
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter = null;

    private boolean currentIsVideoLinkMic = false;

    public PLVSAMoreLinkMicSettingLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVSAMoreLinkMicSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVSAMoreLinkMicSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_more_linkmic_type_setting_layout, this);

        findView();
    }

    private void findView() {
        linkmicTypeSettingLayoutRoot = findViewById(R.id.plvsa_linkmic_type_setting_layout_root);
        linkmicTypeSettingTitleTv = findViewById(R.id.plvsa_linkmic_type_setting_title_tv);
        linkmicTypeSettingHintTitleTv = findViewById(R.id.plvsa_linkmic_type_setting_hint_title_tv);
        linkmicTypeSettingAudioTv = findViewById(R.id.plvsa_linkmic_type_setting_audio_tv);
        linkmicTypeSettingAudioSelectedIndicateView = findViewById(R.id.plvsa_linkmic_type_setting_audio_selected_indicate_view);
        linkmicTypeSettingVideoTv = findViewById(R.id.plvsa_linkmic_type_setting_video_tv);
        linkmicTypeSettingVideoUnavailableTv = findViewById(R.id.plvsa_linkmic_type_setting_video_unavailable_tv);
        linkmicTypeSettingVideoSelectedIndicateView = findViewById(R.id.plvsa_linkmic_type_setting_video_selected_indicate_view);
        linkmicTypeSettingHintSwitchTv = findViewById(R.id.plvsa_linkmic_type_setting_hint_switch_tv);
        linkmicTypeSettingHintDefaultSettingTv = findViewById(R.id.plvsa_linkmic_type_setting_hint_default_setting_tv);

        linkmicTypeSettingAudioTv.setOnClickListener(this);
        linkmicTypeSettingVideoTv.setOnClickListener(this);
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

    public final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            streamerPresenter = presenter;
            observeVideoLinkMicType(presenter);
        }

        private void observeVideoLinkMicType(IPLVStreamerContract.IStreamerPresenter presenter) {
            presenter.getData().getVideoLinkMicType().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    final boolean isVideoLinkMic = aBoolean != null && aBoolean;
                    currentIsVideoLinkMic = isVideoLinkMic;

                    linkmicTypeSettingAudioTv.setTextColor(isVideoLinkMic ? TEXT_COLOR_NOT_SELECTED : TEXT_COLOR_SELECTED);
                    linkmicTypeSettingAudioSelectedIndicateView.setVisibility(isVideoLinkMic ? GONE : VISIBLE);
                    linkmicTypeSettingVideoTv.setTextColor(isVideoLinkMic ? TEXT_COLOR_SELECTED : TEXT_COLOR_NOT_SELECTED);
                    linkmicTypeSettingVideoSelectedIndicateView.setVisibility(isVideoLinkMic ? VISIBLE : GONE);
                }
            });
        }

    };

    private void updateViewWithOrientation() {
        if (PLVScreenUtils.isPortrait(getContext())) {
            linkmicTypeSettingLayoutRoot.setBackgroundResource(BACKGROUND_ID_PORT);
        } else {
            linkmicTypeSettingLayoutRoot.setBackgroundResource(BACKGROUND_ID_LAND);
        }
    }

    private void switchLinkMicType(final boolean isVideoLinkMic) {
        if (currentIsVideoLinkMic == isVideoLinkMic || streamerPresenter == null) {
            return;
        }

        final Runnable runSwitchLinkMic = new Runnable() {
            @Override
            public void run() {
                streamerPresenter.changeLinkMicType(isVideoLinkMic);
            }
        };

        int linkMicUserCount = streamerPresenter.countLinkMicUser(null);
        if (linkMicUserCount > 0) {
            PLVSAConfirmDialog.Builder.context(getContext())
                    .setTitle(R.string.plv_common_dialog_tip)
                    .setContent(R.string.plv_streamer_switch_linkmic_type_hint_viewer_connecting_text)
                    .setLeftButtonText(R.string.plv_common_dialog_cancel)
                    .setRightButtonText(R.string.plv_streamer_switch_linkmic_type_confirm_text)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            runSwitchLinkMic.run();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            runSwitchLinkMic.run();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == linkmicTypeSettingAudioTv.getId()) {
            switchLinkMicType(false);
        } else if (id == linkmicTypeSettingVideoTv.getId()) {
            switchLinkMicType(true);
        }
    }
}
