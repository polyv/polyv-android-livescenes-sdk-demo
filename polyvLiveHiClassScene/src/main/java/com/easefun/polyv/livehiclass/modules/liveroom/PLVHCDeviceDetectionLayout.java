package com.easefun.polyv.livehiclass.modules.liveroom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.media.PLVAudioRecordVolumeHelper;
import com.easefun.polyv.livecommon.module.utils.media.PLVCameraListener;
import com.easefun.polyv.livecommon.module.utils.media.PLVCameraTextureView;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOnFocusDialog;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCVolumeView;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVScreenUtils;

import java.util.ArrayList;

/**
 * 设备检测布局
 */
public class PLVHCDeviceDetectionLayout extends FrameLayout implements IPLVHCDeviceDetectionLayout, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private PLVCameraTextureView plvhcCameraView;
    private ImageView plvhcBackIv;
    private ImageView plvhcSettingMicIv;
    private Switch plvhcSettingMicSw;
    private PLVHCVolumeView plvhcSettingMicVolumeView;
    private ImageView plvhcSettingCameraIv;
    private Switch plvhcSettingCameraSw;
    private ViewGroup plvhcSettingCameraOrientSw;
    private ImageView plvhcSettingCameraOrientIv;
    private TextView plvhcSettingCameraOrientTv;
    private TextView plvhcSettingEnterTv;
    private TextView plvhcWidgetFrontTv;
    private TextView plvhcWidgetBackTv;
    //dialog
    private PLVHCConfirmDialog permissionConfirmDialog;

    private PLVAudioRecordVolumeHelper audioRecordVolumeHelper;
    private Throwable startAudioRecordThrowable;
    private Throwable openCameraThrowable;
    private boolean isCheckStartCamera;
    private boolean isCheckStartAudio;

    private long lastClickCameraSwitchViewTime;

    //runnable
    private Runnable enterClassTask;
    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCDeviceDetectionLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCDeviceDetectionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCDeviceDetectionLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_live_room_device_detection_layout, this, true);

        plvhcCameraView = findViewById(R.id.plvhc_camera_view);
        plvhcBackIv = findViewById(R.id.plvhc_back_iv);
        plvhcSettingMicIv = findViewById(R.id.plvhc_setting_mic_iv);
        plvhcSettingMicSw = findViewById(R.id.plvhc_setting_mic_sw);
        plvhcSettingMicVolumeView = findViewById(R.id.plvhc_setting_mic_volume_view);
        plvhcSettingCameraIv = findViewById(R.id.plvhc_setting_camera_iv);
        plvhcSettingCameraSw = findViewById(R.id.plvhc_setting_camera_sw);
        plvhcSettingCameraOrientSw = findViewById(R.id.plvhc_setting_camera_orient_sw);
        plvhcSettingCameraOrientIv = findViewById(R.id.plvhc_setting_camera_orient_iv);
        plvhcSettingCameraOrientTv = findViewById(R.id.plvhc_setting_camera_orient_tv);
        plvhcSettingEnterTv = findViewById(R.id.plvhc_setting_enter_tv);
        plvhcWidgetFrontTv = findViewById(R.id.plvhc_widget_front_tv);
        plvhcWidgetBackTv = findViewById(R.id.plvhc_widget_back_tv);

        plvhcBackIv.setOnClickListener(this);
        plvhcSettingMicSw.setOnCheckedChangeListener(this);
        plvhcSettingCameraSw.setOnCheckedChangeListener(this);
        plvhcSettingMicVolumeView.setVisibility(View.GONE);
        plvhcSettingCameraOrientIv.setVisibility(View.GONE);
        plvhcSettingCameraOrientTv.setVisibility(View.GONE);
        plvhcSettingCameraOrientSw.setVisibility(View.GONE);
        plvhcWidgetFrontTv.setSelected(true);
        plvhcWidgetFrontTv.setOnClickListener(this);
        plvhcWidgetBackTv.setOnClickListener(this);
        plvhcSettingEnterTv.setOnClickListener(this);

        permissionConfirmDialog = new PLVHCConfirmDialog(getContext());
        permissionConfirmDialog.setOnWindowFocusChangedListener(new PLVOnFocusDialog.OnWindowFocusChangeListener() {
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                if (hasFocus && permissionConfirmDialog.isShowing() && checkSelMediaPermission()) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            permissionConfirmDialog.hide();// need post hide
                            if (enterClassTask != null) {
                                enterClassTask.run();
                                enterClassTask = null;
                            }
                        }
                    });
                }
            }
        });

        // 进入横屏模式
        PLVScreenUtils.enterLandscape((Activity) getContext());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化cameraView">
    private void initCameraView() {
        plvhcCameraView.setCameraOpenListener(new PLVCameraListener() {
            @Override
            public void onOpenSuccess() {
                isCheckStartCamera = true;
                openCameraThrowable = null;
                plvhcSettingCameraSw.setChecked(true);
                plvhcSettingCameraOrientIv.setVisibility(View.VISIBLE);
                plvhcSettingCameraOrientTv.setVisibility(View.VISIBLE);
                plvhcSettingCameraOrientSw.setVisibility(View.VISIBLE);
                plvhcCameraView.setScaleX(-1);
            }

            @Override
            public void onOpenFail(Throwable t, int error) {
                openCameraThrowable = t;
                PLVHCToast.Builder.context(getContext())
                        .setText("摄像头打开失败：" + t.getMessage())
                        .build()
                        .show();
            }

            @Override
            public void onCameraChange() {
            }
        });
        plvhcCameraView.startCamera();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化AudioRecordVolumeHelper">
    private void initAudioRecordVolumeHelper() {
        audioRecordVolumeHelper = new PLVAudioRecordVolumeHelper();
        audioRecordVolumeHelper.setOnGetVolumeListener(new PLVAudioRecordVolumeHelper.OnAudioRecordListener() {
            @Override
            public void onStartSuccess() {
                isCheckStartAudio = true;
                startAudioRecordThrowable = null;
                plvhcSettingMicSw.setChecked(true);
                plvhcSettingMicVolumeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartFail(Throwable t) {
                startAudioRecordThrowable = t;
                PLVHCToast.Builder.context(getContext())
                        .setText("麦克风打开失败：" + t.getMessage())
                        .build()
                        .show();
            }

            @Override
            public void onVolume(int volumeValue) {
                plvhcSettingMicVolumeView.setProgress((int) (volumeValue * plvhcSettingMicVolumeView.getMax() / 100.f));
            }
        });
        audioRecordVolumeHelper.start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    @Override
    public void acceptLayoutVisibility(boolean isShow, Runnable enterClassTask) {
        setVisibility(isShow ? View.VISIBLE : View.GONE);
        if (isShow) {
            initCameraView();
            initAudioRecordVolumeHelper();
        } else {
            destroyResource();
            this.enterClassTask = enterClassTask;
        }
        checkMediaPermission();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public boolean isShown() {
        return super.isShown();
    }

    @Override
    public void destroy() {
        destroyResource();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && isShown()) {
            acceptCheckPermission();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private boolean checkSelMediaPermission() {
        return checkSelCameraPermission() && checkSelAudioPermission();
    }

    private boolean checkSelCameraPermission() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkSelAudioPermission() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void acceptCheckPermission() {
        if (checkSelCameraPermission()) {
            if (!isCheckStartCamera) {
                isCheckStartCamera = true;
                if (plvhcCameraView != null) {
                    plvhcCameraView.startCamera();
                }
            }
        }
        if (checkSelAudioPermission()) {
            if (!isCheckStartAudio) {
                isCheckStartAudio = true;
                if (audioRecordVolumeHelper != null) {
                    audioRecordVolumeHelper.start();
                }
            }
        }
    }

    private void checkMediaPermission() {
        ArrayList<String> permissions = new ArrayList<>(2);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);

        PLVFastPermission.getInstance().start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                if (isShown()) {
                    acceptCheckPermission();
                } else {
                    permissionConfirmDialog.hide();
                    if (enterClassTask != null) {
                        enterClassTask.run();
                        enterClassTask = null;
                    }
                }
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, final ArrayList<String> deniedPermissions, final ArrayList<String> deniedForeverP) {
                if (isShown()) {
                    acceptOnPartialGrantedWithShow(deniedForeverP);
                } else {
                    if (deniedPermissions.size() > 0 || deniedForeverP.size() > 0) {
                        String permissionName = "";
                        if (deniedPermissions.size() > 0) {
                            permissionName = deniedPermissions.size() > 1 ? "摄像头和麦克风"
                                    : (Manifest.permission.CAMERA.equals(deniedPermissions.get(0)) ? "摄像头" : "麦克风");
                        } else {
                            permissionName = deniedForeverP.size() > 1 ? "摄像头和麦克风"
                                    : (Manifest.permission.CAMERA.equals(deniedForeverP.get(0)) ? "摄像头" : "麦克风");
                        }
                        String content = deniedPermissions.size() > 0
                                ? "请允许媒体权限后再上课"
                                : "请前往应用设置中开启" + permissionName + "权限";
                        String rightButtonText = deniedPermissions.size() > 0 ? "确定" : "设置";
                        permissionConfirmDialog.setTitle("媒体权限申请")
                                .setContent(content)
                                .setCancelable(false)
                                .setLeftButtonText("退出")
                                .setLeftBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        permissionConfirmDialog.hide();
                                        ((Activity) getContext()).finish();
                                    }
                                })
                                .setRightButtonText(rightButtonText)
                                .setRightBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (deniedPermissions.size() > 0) {
                                            permissionConfirmDialog.hide();
                                            checkMediaPermission();
                                        } else {
                                            PLVFastPermission.getInstance().jump2Settings(getContext());
                                        }
                                    }
                                })
                                .show();
                    }
                }
            }
        });
    }

    private void acceptOnPartialGrantedWithShow(ArrayList<String> deniedForeverP) {
        acceptCheckPermission();
        if (deniedForeverP.size() > 0) {
            final PLVHCConfirmDialog permissionConfirmDialog = new PLVHCConfirmDialog(getContext());
            for (String deniedPermission : deniedForeverP) {
                if (Manifest.permission.CAMERA.equals(deniedPermission)) {
                    permissionConfirmDialog.setTitle("摄像头权限申请")
                            .setContent("请前往“设置-隐私-摄像头”开启权限")
                            .setLeftButtonText("取消")
                            .setRightButtonText("设置")
                            .setRightBtnListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    permissionConfirmDialog.hide();
                                    PLVFastPermission.getInstance().jump2Settings(getContext());
                                }
                            });
                    break;
                }
                if (Manifest.permission.RECORD_AUDIO.equals(deniedPermission)) {
                    permissionConfirmDialog.setTitle("麦克风权限申请")
                            .setContent("请前往“设置-隐私-麦克风”开启权限")
                            .setLeftButtonText("取消")
                            .setRightButtonText("设置")
                            .setRightBtnListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    permissionConfirmDialog.hide();
                                    PLVFastPermission.getInstance().jump2Settings(getContext());
                                }
                            });
                    break;
                }
            }
            permissionConfirmDialog.show();
        }
    }

    private void destroyResource() {
        if (plvhcCameraView != null) {
            plvhcCameraView.release();
            plvhcCameraView = null;
        }
        if (audioRecordVolumeHelper != null) {
            audioRecordVolumeHelper.stop();
            audioRecordVolumeHelper = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvhc_back_iv) {
            ((Activity) getContext()).finish();
        } else if (id == R.id.plvhc_widget_front_tv
                || id == R.id.plvhc_widget_back_tv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                final boolean isFront = id == R.id.plvhc_widget_front_tv;
                boolean result = plvhcCameraView.switchCamera(isFront, new Runnable() {
                    @Override
                    public void run() {
                        plvhcCameraView.setScaleX(isFront ? -1 : 1);
                    }
                });
                if (result) {
                    plvhcWidgetFrontTv.setSelected(isFront);
                    plvhcWidgetBackTv.setSelected(!isFront);
                }
                lastClickCameraSwitchViewTime = currentTime;
            }
        } else if (id == R.id.plvhc_setting_enter_tv) {
            if (!checkSelMediaPermission()) {
                new PLVHCConfirmDialog(getContext())
                        .setTitle("温馨提示")
                        .setContent("请允许媒体权限后再请进教室")
                        .setLeftButtonText("取消")
                        .setRightButtonText("确定")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                checkMediaPermission();
                            }
                        })
                        .show();
                return;
            }
            destroyResource();
            setVisibility(View.GONE);
            if (onViewActionListener != null) {
                onViewActionListener.onEnterClassAction(plvhcSettingMicSw.isChecked(),
                        plvhcSettingCameraSw.isChecked(),
                        plvhcWidgetFrontTv.isSelected());
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) {
            return;
        }
        int id = buttonView.getId();
        if (id == R.id.plvhc_setting_mic_sw) {
            if (startAudioRecordThrowable != null) {
                PLVHCToast.Builder.context(getContext())
                        .setText("麦克风打开失败：" + startAudioRecordThrowable.getMessage())
                        .build()
                        .show();
                buttonView.setChecked(!buttonView.isChecked());
                return;
            }
            if (isChecked) {
                audioRecordVolumeHelper.start();
            } else {
                audioRecordVolumeHelper.stop();
            }
            plvhcSettingMicVolumeView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            PLVHCToast.Builder.context(getContext())
                    .setDrawable(isChecked ? R.drawable.plvhc_member_mic : R.drawable.plvhc_member_mic_sel)
                    .setText((isChecked ? "已开启" : "已关闭") + "麦克风")
                    .build()
                    .show();
        } else if (id == R.id.plvhc_setting_camera_sw) {
            if (openCameraThrowable != null) {
                PLVHCToast.Builder.context(getContext())
                        .setText("摄像头打开失败：" + openCameraThrowable.getMessage())
                        .build()
                        .show();
                buttonView.setChecked(!buttonView.isChecked());
                return;
            }
            if (isChecked) {
                plvhcCameraView.startPreview();
                plvhcCameraView.setVisibility(View.VISIBLE);
            } else {
                plvhcCameraView.stopPreview();
                plvhcCameraView.setVisibility(View.INVISIBLE);
            }
            plvhcSettingCameraOrientIv.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            plvhcSettingCameraOrientTv.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            plvhcSettingCameraOrientSw.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            PLVHCToast.Builder.context(getContext())
                    .setDrawable(isChecked ? R.drawable.plvhc_member_camera : R.drawable.plvhc_member_camera_sel)
                    .setText((isChecked ? "已开启" : "已关闭") + "摄像头")
                    .build()
                    .show();
        }
    }
    // </editor-fold>
}
