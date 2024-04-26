package com.easefun.polyv.livecloudclass.modules.linkmic.widget;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;
import static com.plv.foundationsdk.utils.PLVTimeUnit.millis;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVViewerLinkMicState;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import net.polyv.media.source.video.camera.PLVCameraVideoSource;
import net.polyv.media.source.video.camera.vo.CameraConfig;
import net.polyv.media.source.video.camera.vo.CameraFacing;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Hoshiiro
 */
public class PLVLCLinkMicInvitationLayout extends FrameLayout {

    public static final int CANCEL_BY_MANUAL = 1;
    public static final int CANCEL_BY_TIMEOUT = 2;
    public static final int CANCEL_BY_PERMISSION = 3;

    private static final boolean INVITATION_DEFAULT_OPEN_MICROPHONE = true;

    private PortLayout portLayout;
    private LandLayout landLayout;
    private PLVMenuDrawer menuDrawer;

    private final SoundPool bgmSoundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);
    private int bgmSoundId = 0;
    private int bgmStreamId = 0;

    private volatile long acceptInviteLinkMicLimitTs = 0;
    private final MutableLiveData<Boolean> isOpenCamera = mutableLiveData(false);
    private final MutableLiveData<Boolean> isOpenMicrophone = mutableLiveData(INVITATION_DEFAULT_OPEN_MICROPHONE);

    private OnViewActionListener onViewActionListener = null;

    @Nullable
    private Boolean isOnlyAudio = null;
    private boolean lastOrientationIsPortrait = ScreenUtils.isPortrait();

    private Disposable fetchAcceptInviteLinkMicLimitDisposable;
    private Disposable updateTimeLeftDisposable;

    public PLVLCLinkMicInvitationLayout(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        portLayout = new PortLayout(getContext());
        landLayout = new LandLayout(getContext());
        setListener(portLayout);
        setListener(landLayout);

        prepareBgm();

        observeOpenStateChanged();
    }

    private void setListener(ChildLayout childLayout) {
        childLayout.cameraSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenCamera.postValue(isChecked);
            }
        });

        childLayout.microphoneSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenMicrophone.postValue(isChecked);
            }
        });

        childLayout.cancelInvitationTextView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelInvitation(CANCEL_BY_MANUAL);
            }
        });

        childLayout.acceptInvitationTextView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptInvitation();
            }
        });
    }

    private void prepareBgm() {
        bgmSoundId = bgmSoundPool.load(getContext(), R.raw.plv_linkmic_invitation_bgm, 1);
    }

    private void observeOpenStateChanged() {
        isOpenCamera.observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean openBoolean) {
                final boolean open = getOrDefault(openBoolean, false);
                portLayout.cameraSwitch().setChecked(open);
                landLayout.cameraSwitch().setChecked(open);
                portLayout.cameraHintView().setVisibility(open ? GONE : VISIBLE);
                landLayout.cameraHintView().setVisibility(open ? GONE : VISIBLE);
                changeCamera(open);
            }
        });

        isOpenMicrophone.observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean openBoolean) {
                final boolean open = getOrDefault(openBoolean, false);
                portLayout.microphoneSwitch().setChecked(open);
                landLayout.microphoneSwitch().setChecked(open);
            }
        });
    }

    public PLVLCLinkMicInvitationLayout setOnViewActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
        return this;
    }

    public void onLinkMicStateChanged(PLVViewerLinkMicState oldState, PLVViewerLinkMicState newState) {
        if (newState instanceof PLVViewerLinkMicState.InvitingLinkMicState
                && !(oldState instanceof PLVViewerLinkMicState.InvitingLinkMicState)) {
            acceptInviteLinkMicLimitTs = System.currentTimeMillis() + seconds(30).toMillis();
            startFetchAcceptInviteLinkMicLimit();
            startUpdateTimeLeft();
            startPlayBgm();
            show();
        } else if (!(newState instanceof PLVViewerLinkMicState.InvitingLinkMicState)) {
            stop();
        }
    }

    public void updateDeviceOpenState(boolean openAudio, boolean openVideo) {
        isOpenMicrophone.postValue(openAudio);
        isOpenCamera.postValue(openVideo);
    }

    public void setIsOnlyAudio(boolean isOnlyAudio) {
        if (this.isOnlyAudio != null && this.isOnlyAudio == isOnlyAudio) {
            return;
        }
        this.isOnlyAudio = isOnlyAudio;
        stop();

        for (View view : portLayout.cameraViews()) {
            view.setVisibility(isOnlyAudio ? View.GONE : View.VISIBLE);
        }
        for (View view : landLayout.cameraViews()) {
            view.setVisibility(isOnlyAudio ? View.GONE : View.VISIBLE);
        }
        if (isOnlyAudio) {
            portLayout.cameraHintView().setVisibility(View.GONE);
            landLayout.cameraHintView().setVisibility(View.GONE);
            portLayout.onlyAudioHintView().setVisibility(View.VISIBLE);
            landLayout.onlyAudioHintView().setVisibility(View.VISIBLE);
        } else {
            portLayout.cameraHintView().setVisibility(getOrDefault(isOpenCamera.getValue(), false) ? View.GONE : View.VISIBLE);
            landLayout.cameraHintView().setVisibility(getOrDefault(isOpenCamera.getValue(), false) ? View.GONE : View.VISIBLE);
            portLayout.onlyAudioHintView().setVisibility(View.GONE);
            landLayout.onlyAudioHintView().setVisibility(View.GONE);
        }
        String format = PLVAppUtils.formatStringWithId(R.string.plv_linkmic_invitation, isOnlyAudio ? R.string.plv_linkmic_type_audio : R.string.plv_linkmic_type_video);
        portLayout.titleTextView().setText(format);
        landLayout.titleTextView().setText(format);
    }

    public void destroy() {
        stop();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final boolean isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait == lastOrientationIsPortrait) {
            return;
        }
        lastOrientationIsPortrait = isPortrait;

        final boolean isShowing = menuDrawer != null && menuDrawer.isMenuVisible();
        if (!isShowing) {
            return;
        }

        hide();
        show();
    }

    private void show() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvlc_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        clearCamera();
                        removeAllViews();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {

                }
            });
        } else {
            menuDrawer.attachToContainer();
        }
        menuDrawer.setMenuSize(ScreenUtils.getScreenOrientatedHeight());
        menuDrawer.openMenu();

        removeAllViews();
        final ChildLayout child = PLVScreenUtils.isPortrait(getContext()) ? portLayout : landLayout;
        addView((View) child, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setupCamera(child.renderView());
    }

    private void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    private void stop() {
        hide();
        stopPlayBgm();
        stopUpdateTimeLeft();
        stopFetchAcceptInviteLinkMicLimit();
    }

    private void startFetchAcceptInviteLinkMicLimit() {
        stopFetchAcceptInviteLinkMicLimit();
        fetchAcceptInviteLinkMicLimitDisposable = PLVRxTimer.timer(
                (int) seconds(9).toMillis(),
                new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (onViewActionListener == null) {
                            return;
                        }
                        final long ts1 = System.currentTimeMillis();
                        onViewActionListener.asyncGetAcceptInvitationLeftTimeInSecond(new PLVSugarUtil.Consumer<Integer>() {
                            @Override
                            public void accept(Integer timeLeft) {
                                final long ts2 = System.currentTimeMillis();
                                acceptInviteLinkMicLimitTs = ts1 + (ts2 - ts1) / 2 + timeLeft * 1000L;
                            }
                        });
                    }
                });
    }

    private void stopFetchAcceptInviteLinkMicLimit() {
        if (fetchAcceptInviteLinkMicLimitDisposable != null) {
            fetchAcceptInviteLinkMicLimitDisposable.dispose();
            fetchAcceptInviteLinkMicLimitDisposable = null;
        }
    }

    private void startUpdateTimeLeft() {
        stopUpdateTimeLeft();
        updateTimeLeftDisposable = PLVRxTimer.timer(
                (int) millis(500).getValue(),
                new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        final int timeLeftInSecond = (int) ((acceptInviteLinkMicLimitTs - System.currentTimeMillis()) / 1000);
                        if (timeLeftInSecond <= 0) {
                            stop();
                            cancelInvitation(CANCEL_BY_TIMEOUT);
                            return;
                        }
                        String format = PLVAppUtils.formatString(R.string.plv_linkmic_not_yet, timeLeftInSecond + "");
                        portLayout.cancelInvitationTextView().setText(format);
                        landLayout.cancelInvitationTextView().setText(format);
                    }
                });
    }

    private void stopUpdateTimeLeft() {
        if (updateTimeLeftDisposable != null) {
            updateTimeLeftDisposable.dispose();
            updateTimeLeftDisposable = null;
        }
    }

    private void startPlayBgm() {
        if (bgmStreamId != 0) {
            stopPlayBgm();
        }
        if (bgmSoundId != 0) {
            bgmStreamId = bgmSoundPool.play(bgmSoundId, 1, 1, 1, -1, 1);
        }
    }

    private void stopPlayBgm() {
        if (bgmStreamId != 0) {
            bgmSoundPool.stop(bgmStreamId);
        }
        bgmStreamId = 0;
    }

    private void setupCamera(View renderView) {
        PLVCameraVideoSource.INSTANCE.setup(
                new CameraConfig.Builder()
                        .context(getContext())
                        .size(1280, 720)
                        .facing(CameraFacing.FRONT)
                        .renderView(renderView)
                        .requestOpen(getOrDefault(isOpenCamera.getValue(), false))
                        .build()
        );
    }

    private void clearCamera() {
        PLVCameraVideoSource.INSTANCE.clear();
    }

    private void changeCamera(boolean open) {
        if (menuDrawer == null || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_CLOSED) {
            return;
        }
        if (open) {
            requirePermission(
                    listOf(Manifest.permission.CAMERA),
                    new PLVSugarUtil.Callback() {
                        @Override
                        public void onCallback() {
                            PLVCameraVideoSource.INSTANCE.openCamera();
                        }
                    },
                    new PLVSugarUtil.Callback() {
                        @Override
                        public void onCallback() {
                            hide();
                            cancelInvitation(CANCEL_BY_PERMISSION);
                            showPermissionDialog(PLVAppUtils.getString(R.string.plv_linkmic_camera_permission_apply_tips));
                        }
                    }
            );
        } else {
            PLVCameraVideoSource.INSTANCE.closeCamera();
        }
    }

    private void cancelInvitation(int cancelBy) {
        hide();
        if (onViewActionListener != null) {
            onViewActionListener.answerLinkMicInvitation(
                    false,
                    cancelBy,
                    getOrDefault(isOpenCamera.getValue(), false),
                    getOrDefault(isOpenMicrophone.getValue(), false)
            );
        }
    }

    private void acceptInvitation() {
        final boolean onlyAudio = getOrDefault(this.isOnlyAudio, false);
        final List<String> permissions = onlyAudio ? listOf(Manifest.permission.RECORD_AUDIO) : listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
        requirePermission(
                permissions,
                new PLVSugarUtil.Callback() {
                    @Override
                    public void onCallback() {
                        hide();
                        if (onViewActionListener != null) {
                            onViewActionListener.answerLinkMicInvitation(
                                    true,
                                    0,
                                    getOrDefault(isOpenCamera.getValue(), false),
                                    getOrDefault(isOpenMicrophone.getValue(), false)
                            );
                        }
                    }
                },
                new PLVSugarUtil.Callback() {
                    @Override
                    public void onCallback() {
                        hide();
                        cancelInvitation(CANCEL_BY_PERMISSION);
                        showPermissionDialog(PLVAppUtils.formatStringWithId(R.string.plv_linkmic_permission_apply_tips, onlyAudio ? R.string.plv_linkmic_permission_microphone : R.string.plv_linkmic_permission_camera_and_microphone));
                    }
                }
        );
    }

    private void requirePermission(final List<String> permissions, final PLVSugarUtil.Callback onGranted, final PLVSugarUtil.Callback onDenied) {
        if (PLVFastPermission.hasPermission(getContext(), permissions)) {
            onGranted.onCallback();
            return;
        }

        PLVFastPermission.getInstance().start(((Activity) getContext()), permissions, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                onGranted.onCallback();
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                onDenied.onCallback();
            }
        });
    }

    private void showPermissionDialog(String content) {
        new PLVConfirmDialog(getContext())
                .setTitle(R.string.plv_common_dialog_tip)
                .setContent(content)
                .setCancelable(true)
                .setLeftButtonText(R.string.plv_common_dialog_cancel)
                .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .setRightButtonText(R.string.plv_common_dialog_go_to_setting)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        PLVFastPermission.getInstance().jump2Settings(getContext());
                    }
                })
                .show();
    }

    private interface ChildLayout {
        TextView titleTextView();

        View renderView();

        View cameraHintView();

        View onlyAudioHintView();

        Switch cameraSwitch();

        List<View> cameraViews();

        Switch microphoneSwitch();

        TextView cancelInvitationTextView();

        TextView acceptInvitationTextView();
    }

    private static class PortLayout extends FrameLayout implements ChildLayout {

        private TextView linkmicInvitationTitleTv;
        private ConstraintLayout linkmicInvitationCameraLayout;
        private View linkmicInvitationCameraPreviewView;
        private ConstraintLayout linkmicInvitationCameraClosedHintLayout;
        private ConstraintLayout linkmicInvitationOnlyAudioHintLayout;
        private ImageView linkmicInvitationCameraIv;
        private TextView linkmicInvitationCameraTv;
        private Switch linkmicInvitationCameraSwitch;
        private ImageView linkmicInvitationMicrophoneIv;
        private TextView linkmicInvitationMicrophoneTv;
        private Switch linkmicInvitationMicrophoneSwitch;
        private ImageView linkmicInvitationPrivacyNotifyIv;
        private TextView linkmicInvitationPrivacyNotifyTv;
        private TextView linkmicInvitationCancelTv;
        private TextView linkmicInvitationAcceptTv;

        public PortLayout(@NonNull Context context) {
            super(context);
            LayoutInflater.from(getContext()).inflate(R.layout.plvlc_linkmic_invitation_port_layout, this);
            findView();
        }

        private void findView() {
            linkmicInvitationTitleTv = findViewById(R.id.plvlc_linkmic_invitation_title_tv);
            linkmicInvitationCameraLayout = findViewById(R.id.plvlc_linkmic_invitation_camera_layout);
            linkmicInvitationCameraPreviewView = findViewById(R.id.plvlc_linkmic_invitation_camera_preview_view);
            linkmicInvitationCameraClosedHintLayout = findViewById(R.id.plvlc_linkmic_invitation_camera_closed_hint_layout);
            linkmicInvitationOnlyAudioHintLayout = findViewById(R.id.plvlc_linkmic_invitation_only_audio_hint_layout);
            linkmicInvitationCameraIv = findViewById(R.id.plvlc_linkmic_invitation_camera_iv);
            linkmicInvitationCameraTv = findViewById(R.id.plvlc_linkmic_invitation_camera_tv);
            linkmicInvitationCameraSwitch = findViewById(R.id.plvlc_linkmic_invitation_camera_switch);
            linkmicInvitationMicrophoneIv = findViewById(R.id.plvlc_linkmic_invitation_microphone_iv);
            linkmicInvitationMicrophoneTv = findViewById(R.id.plvlc_linkmic_invitation_microphone_tv);
            linkmicInvitationMicrophoneSwitch = findViewById(R.id.plvlc_linkmic_invitation_microphone_switch);
            linkmicInvitationPrivacyNotifyIv = findViewById(R.id.plvlc_linkmic_invitation_privacy_notify_iv);
            linkmicInvitationPrivacyNotifyTv = findViewById(R.id.plvlc_linkmic_invitation_privacy_notify_tv);
            linkmicInvitationCancelTv = findViewById(R.id.plvlc_linkmic_invitation_cancel_tv);
            linkmicInvitationAcceptTv = findViewById(R.id.plvlc_linkmic_invitation_accept_tv);
        }

        @Override
        public TextView titleTextView() {
            return linkmicInvitationTitleTv;
        }

        @Override
        public View renderView() {
            return linkmicInvitationCameraPreviewView;
        }

        @Override
        public View cameraHintView() {
            return linkmicInvitationCameraClosedHintLayout;
        }

        @Override
        public View onlyAudioHintView() {
            return linkmicInvitationOnlyAudioHintLayout;
        }

        @Override
        public Switch cameraSwitch() {
            return linkmicInvitationCameraSwitch;
        }

        @Override
        public List<View> cameraViews() {
            return listOf(linkmicInvitationCameraIv,
                    linkmicInvitationCameraTv,
                    linkmicInvitationCameraSwitch);
        }

        @Override
        public Switch microphoneSwitch() {
            return linkmicInvitationMicrophoneSwitch;
        }

        @Override
        public TextView cancelInvitationTextView() {
            return linkmicInvitationCancelTv;
        }

        @Override
        public TextView acceptInvitationTextView() {
            return linkmicInvitationAcceptTv;
        }
    }

    private static class LandLayout extends FrameLayout implements ChildLayout {

        private TextView linkmicInvitationTitleTv;
        private ConstraintLayout linkmicInvitationCameraLayout;
        private View linkmicInvitationCameraPreviewView;
        private ConstraintLayout linkmicInvitationCameraClosedHintLayout;
        private ConstraintLayout linkmicInvitationOnlyAudioHintLayout;
        private ImageView linkmicInvitationCameraIv;
        private TextView linkmicInvitationCameraTv;
        private Switch linkmicInvitationCameraSwitch;
        private ImageView linkmicInvitationMicrophoneIv;
        private TextView linkmicInvitationMicrophoneTv;
        private Switch linkmicInvitationMicrophoneSwitch;
        private ImageView linkmicInvitationPrivacyNotifyIv;
        private TextView linkmicInvitationPrivacyNotifyTv;
        private TextView linkmicInvitationCancelTv;
        private TextView linkmicInvitationAcceptTv;

        public LandLayout(@NonNull Context context) {
            super(context);
            LayoutInflater.from(getContext()).inflate(R.layout.plvlc_linkmic_invitation_land_layout, this);
            findView();
        }

        private void findView() {
            linkmicInvitationTitleTv = findViewById(R.id.plvlc_linkmic_invitation_title_tv);
            linkmicInvitationCameraLayout = findViewById(R.id.plvlc_linkmic_invitation_camera_layout);
            linkmicInvitationCameraPreviewView = findViewById(R.id.plvlc_linkmic_invitation_camera_preview_view);
            linkmicInvitationCameraClosedHintLayout = findViewById(R.id.plvlc_linkmic_invitation_camera_closed_hint_layout);
            linkmicInvitationOnlyAudioHintLayout = findViewById(R.id.plvlc_linkmic_invitation_only_audio_hint_layout);
            linkmicInvitationCameraIv = findViewById(R.id.plvlc_linkmic_invitation_camera_iv);
            linkmicInvitationCameraTv = findViewById(R.id.plvlc_linkmic_invitation_camera_tv);
            linkmicInvitationCameraSwitch = findViewById(R.id.plvlc_linkmic_invitation_camera_switch);
            linkmicInvitationMicrophoneIv = findViewById(R.id.plvlc_linkmic_invitation_microphone_iv);
            linkmicInvitationMicrophoneTv = findViewById(R.id.plvlc_linkmic_invitation_microphone_tv);
            linkmicInvitationMicrophoneSwitch = findViewById(R.id.plvlc_linkmic_invitation_microphone_switch);
            linkmicInvitationPrivacyNotifyIv = findViewById(R.id.plvlc_linkmic_invitation_privacy_notify_iv);
            linkmicInvitationPrivacyNotifyTv = findViewById(R.id.plvlc_linkmic_invitation_privacy_notify_tv);
            linkmicInvitationCancelTv = findViewById(R.id.plvlc_linkmic_invitation_cancel_tv);
            linkmicInvitationAcceptTv = findViewById(R.id.plvlc_linkmic_invitation_accept_tv);
        }

        @Override
        public TextView titleTextView() {
            return linkmicInvitationTitleTv;
        }

        @Override
        public View renderView() {
            return linkmicInvitationCameraPreviewView;
        }

        @Override
        public View cameraHintView() {
            return linkmicInvitationCameraClosedHintLayout;
        }

        @Override
        public View onlyAudioHintView() {
            return linkmicInvitationOnlyAudioHintLayout;
        }

        @Override
        public Switch cameraSwitch() {
            return linkmicInvitationCameraSwitch;
        }

        @Override
        public List<View> cameraViews() {
            return listOf(linkmicInvitationCameraIv,
                    linkmicInvitationCameraTv,
                    linkmicInvitationCameraSwitch);
        }

        @Override
        public Switch microphoneSwitch() {
            return linkmicInvitationMicrophoneSwitch;
        }

        @Override
        public TextView cancelInvitationTextView() {
            return linkmicInvitationCancelTv;
        }

        @Override
        public TextView acceptInvitationTextView() {
            return linkmicInvitationAcceptTv;
        }
    }

    public interface OnViewActionListener {

        void answerLinkMicInvitation(boolean accept, int cancelBy, boolean openCamera, boolean openMicrophone);

        void asyncGetAcceptInvitationLeftTimeInSecond(PLVSugarUtil.Consumer<Integer> callback);

    }

}
