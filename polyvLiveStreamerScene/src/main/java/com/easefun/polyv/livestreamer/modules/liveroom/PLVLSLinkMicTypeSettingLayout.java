package com.easefun.polyv.livestreamer.modules.liveroom;

import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLSLinkMicTypeSettingLayout extends FrameLayout implements View.OnClickListener {

    private static final int TEXT_COLOR_SELECTED = PLVFormatUtils.parseColor("#4399FF");
    private static final int TEXT_COLOR_NOT_SELECTED = PLVFormatUtils.parseColor("#F0F1F5");
    private static final int TEXT_COLOR_NOT_AVAILABLE = PLVFormatUtils.parseColor("#99F0F1F5");

    private PLVMenuDrawer menuDrawer;

    private PLVBlurView blurLy;
    private ImageView linkmicTypeSettingBackIv;
    private TextView linkmicTypeSettingTitleTv;
    private View linkmicTypeSettingSeparatorLine;
    private TextView linkmicTypeSettingHintTitleTv;
    private TextView linkmicTypeSettingAudioTv;
    private PLVRoundColorView linkmicTypeSettingAudioSelectedIndicateView;
    private TextView linkmicTypeSettingVideoTv;
    private TextView linkmicTypeSettingVideoUnavailableTv;
    private PLVRoundColorView linkmicTypeSettingVideoSelectedIndicateView;
    private TextView linkmicTypeSettingHintSwitchTv;
    private TextView linkmicTypeSettingHintDefaultSettingTv;

    @Nullable
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter = null;

    private boolean currentIsVideoLinkMic = false;

    public PLVLSLinkMicTypeSettingLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PLVLSLinkMicTypeSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PLVLSLinkMicTypeSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_linkmic_type_setting_layout, this);
        findView();

        PLVBlurUtils.initBlurView(blurLy);
    }

    private void findView() {
        blurLy = findViewById(R.id.blur_ly);
        linkmicTypeSettingBackIv = findViewById(R.id.plvls_linkmic_type_setting_back_iv);
        linkmicTypeSettingTitleTv = findViewById(R.id.plvls_linkmic_type_setting_title_tv);
        linkmicTypeSettingSeparatorLine = findViewById(R.id.plvls_linkmic_type_setting_separator_line);
        linkmicTypeSettingHintTitleTv = findViewById(R.id.plvls_linkmic_type_setting_hint_title_tv);
        linkmicTypeSettingAudioTv = findViewById(R.id.plvls_linkmic_type_setting_audio_tv);
        linkmicTypeSettingAudioSelectedIndicateView = findViewById(R.id.plvls_linkmic_type_setting_audio_selected_indicate_view);
        linkmicTypeSettingVideoTv = findViewById(R.id.plvls_linkmic_type_setting_video_tv);
        linkmicTypeSettingVideoUnavailableTv = findViewById(R.id.plvls_linkmic_type_setting_video_unavailable_tv);
        linkmicTypeSettingVideoSelectedIndicateView = findViewById(R.id.plvls_linkmic_type_setting_video_selected_indicate_view);
        linkmicTypeSettingHintSwitchTv = findViewById(R.id.plvls_linkmic_type_setting_hint_switch_tv);
        linkmicTypeSettingHintDefaultSettingTv = findViewById(R.id.plvls_linkmic_type_setting_hint_default_setting_tv);

        linkmicTypeSettingBackIv.setOnClickListener(this);
        linkmicTypeSettingAudioTv.setOnClickListener(this);
        linkmicTypeSettingVideoTv.setOnClickListener(this);
    }

    public void show() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize(ConvertUtils.dp2px(356));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
        } else {
            menuDrawer.attachToContainer();
        }
        menuDrawer.openMenu();
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
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
            PLVLSConfirmDialog.Builder.context(getContext())
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
        if (id == linkmicTypeSettingBackIv.getId()) {
            close();
        } else if (id == linkmicTypeSettingAudioTv.getId()) {
            switchLinkMicType(false);
        } else if (id == linkmicTypeSettingVideoTv.getId()) {
            switchLinkMicType(true);
        }
    }
}
