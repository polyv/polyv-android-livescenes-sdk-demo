package com.plv.streameralone.modules.liveroom;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.plv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.plv.livecommon.module.utils.PLVToast;
import com.plv.livecommon.ui.widget.PLVConfirmDialog;
import com.plv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.plv.livescenes.streamer.config.PLVSStreamerConfig;
import com.plv.streameralone.R;
import com.plv.streameralone.ui.widget.PLVSAConfirmDialog;

import java.util.ArrayList;

/**
 * 开播设置布局
 */
public class PLVSASettingLayout extends FrameLayout implements IPLVSASettingLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //data
    private boolean isFrontCamera;
    private boolean isMirrorMode;
    // 直播标题输入布局
    private PLVSASettingTitleInputLayout titleInputLayout;
    //清晰度设置布局
    private PLVSABitrateLayout bitrateLayout;
    //推流开始倒计时布局
    private IPLVSACountDownWindow countDownWindow;

    // 推流Presenter
    @Nullable
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //view
    private ImageView plvsaSettingClosePageIv;
    private ConstraintLayout plvsaSettingConfigLy;
    private TextView plvsaSettingLiveTitleTv;
    private ImageView plvsaSettingCameraOrientIv;
    private ImageView plvsaSettingMirrorIv;
    private ImageView plvsaSettingBitrateIv;
    private TextView plvsaSettingCameraOrientTv;
    private TextView plvsaSettingMirrorTv;
    private TextView plvsaSettingBitrateTv;
    private Button plvsaSettingStartLiveBtn;

    private String liveTitle;

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;

    //listener
    private OnViewActionListener onViewActionListener;

    private long lastClickCameraSwitchViewTime;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSASettingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSASettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSASettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_layout, this, true);

        plvsaSettingClosePageIv = findViewById(R.id.plvsa_setting_close_page_iv);
        plvsaSettingConfigLy = findViewById(R.id.plvsa_setting_config_ly);
        plvsaSettingLiveTitleTv = findViewById(R.id.plvsa_setting_live_title_tv);
        plvsaSettingCameraOrientIv = findViewById(R.id.plvsa_setting_camera_orient_iv);
        plvsaSettingMirrorIv = findViewById(R.id.plvsa_setting_mirror_iv);
        plvsaSettingBitrateIv = findViewById(R.id.plvsa_setting_bitrate_iv);
        plvsaSettingCameraOrientTv = findViewById(R.id.plvsa_setting_camera_orient_tv);
        plvsaSettingMirrorTv = findViewById(R.id.plvsa_setting_mirror_tv);
        plvsaSettingBitrateTv = findViewById(R.id.plvsa_setting_bitrate_tv);
        plvsaSettingStartLiveBtn = findViewById(R.id.plvsa_setting_start_live_btn);

        plvsaSettingClosePageIv.setOnClickListener(this);
        plvsaSettingStartLiveBtn.setOnClickListener(this);
        plvsaSettingCameraOrientIv.setOnClickListener(this);
        plvsaSettingCameraOrientTv.setOnClickListener(this);
        plvsaSettingMirrorIv.setOnClickListener(this);
        plvsaSettingMirrorTv.setOnClickListener(this);
        plvsaSettingBitrateIv.setOnClickListener(this);
        plvsaSettingBitrateTv.setOnClickListener(this);

        initTitleInputLayout();
        initBitrateLayout();
        initTitleTextOnClickListener();
        initBeginCountDownWindow();
    }

    /**
     * 初始化直播标题输入布局
     */
    private void initTitleInputLayout() {
        titleInputLayout = new PLVSASettingTitleInputLayout(getContext());
        titleInputLayout.setOnTitleChangeListener(new PLVSASettingTitleInputLayout.OnTitleChangeListener() {
            @Override
            public void onChange(String newTitle) {
                liveTitle = newTitle;
                if (liveTitle.length() > 0) {
                    plvsaSettingLiveTitleTv.setText(liveTitle);
                    plvsaSettingLiveTitleTv.setTextColor(Color.parseColor("#F0F1F5"));
                } else {
                    plvsaSettingLiveTitleTv.setText("点击输入直播标题");
                    plvsaSettingLiveTitleTv.setTextColor(Color.parseColor("#99FFFFFF"));
                }
            }
        });
        titleInputLayout.setOnAttachDetachListener(new PLVSASettingTitleInputLayout.OnAttachDetachListener() {
            @Override
            public void onAttach(View v) {
                // 显示直播标题输入布局时，隐藏设置选项和开始直播按钮
                plvsaSettingConfigLy.setVisibility(View.GONE);
                plvsaSettingStartLiveBtn.setVisibility(View.GONE);
            }

            @Override
            public void onDetach(View v) {
                plvsaSettingConfigLy.setVisibility(View.VISIBLE);
                plvsaSettingStartLiveBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initBitrateLayout() {
        bitrateLayout = new PLVSABitrateLayout(getContext());
        bitrateLayout.setOnViewActionListener(new PLVSABitrateLayout.OnViewActionListener() {
            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return onViewActionListener != null ? onViewActionListener.getBitrateInfo() : null;
            }

            @Override
            public void onBitrateClick(int bitrate) {
                bitrateLayout.close();
                switchBitrateByUser = true;
                if (onViewActionListener != null) {
                    onViewActionListener.onBitrateClick(bitrate);
                }
                plvsaSettingBitrateTv.setText(PLVSStreamerConfig.Bitrate.getText(bitrate));
                updateBitrateIcon(bitrate);
            }
        });
        bitrateLayout.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == PLVMenuDrawer.STATE_OPEN) {
                    // 显示清晰度选择布局时，隐藏设置选项和开始直播按钮
                    plvsaSettingConfigLy.setVisibility(View.GONE);
                    plvsaSettingStartLiveBtn.setVisibility(View.GONE);
                } else if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    plvsaSettingConfigLy.setVisibility(View.VISIBLE);
                    plvsaSettingStartLiveBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });
    }

    /**
     * 初始化直播标题点击事件
     * 点击直播标题时，显示直播标题输入布局
     */
    private void initTitleTextOnClickListener() {
        plvsaSettingLiveTitleTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addView(titleInputLayout, layoutParams);
            }
        });
    }

    private void initBeginCountDownWindow() {
        countDownWindow = new PLVSABeginCountDownWindow(this);
        //初始化倒计时监听器
        countDownWindow.setOnCountDownListener(new IPLVSACountDownWindow.OnCountDownListener() {
            @Override
            public void onCountDownFinished() {
                // 倒计时结束，隐藏设置布局，更新直播标题，开始直播
                setVisibility(View.GONE);
                liveRoomDataManager.getConfig().setupChannelName(liveTitle);
                liveRoomDataManager.requestUpdateChannelName();
                if (onViewActionListener != null) {
                    onViewActionListener.onStartLiveAction();
                }
            }

            @Override
            public void onCountDownCanceled() {
                // 倒计时取消，重新显示设置布局
                setVisibility(View.VISIBLE);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSASettingLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        this.liveTitle = liveRoomDataManager.getConfig().getChannelName();
        plvsaSettingLiveTitleTv.setText(liveTitle);
        titleInputLayout.initTitle(liveTitle);
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;

        if (onViewActionListener != null) {
            Pair<Integer, Integer> bitrateInfo = onViewActionListener.getBitrateInfo();
            if (bitrateInfo != null) {
                plvsaSettingBitrateTv.setText(PLVSStreamerConfig.Bitrate.getText(bitrateInfo.second));
                updateBitrateIcon(bitrateInfo.second);
            }
        }

        initStreamerMvpView();
    }

    @Override
    public void setFrontCameraStatus(boolean isFront) {
        this.isFrontCamera = isFront;
    }

    @Override
    public void setMirrorModeStatus(boolean isMirrorMode) {
        this.isMirrorMode = isMirrorMode;
        plvsaSettingMirrorIv.setSelected(isMirrorMode);
    }

    @Override
    public void showAlertDialogNoNetwork() {
        new PLVSAConfirmDialog(getContext())
                .setTitleVisibility(View.GONE)
                .setContent(R.string.plv_streamer_dialog_no_network)
                .setIsNeedLeftBtn(false)
                .setCancelable(false)
                .setRightButtonText("确定")
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onBackPressed() {
        return bitrateLayout.onBackPressed();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 MVP - View">

    private void initStreamerMvpView() {
        if (onViewActionListener != null) {
            streamerPresenter = onViewActionListener.getStreamerPresenter();
        }
        if (streamerPresenter != null) {
            streamerPresenter.registerView(streamerView);
            streamerPresenter.getData().getCurBitrate().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer bitrate) {
                    if (bitrate == null || getContext() == null || !switchBitrateByUser) {
                        return;
                    }
                    switchBitrateByUser = false;
                    String toastText = "已切换为" + PLVSStreamerConfig.Bitrate.getText(bitrate);
                    PLVToast.Builder.context(getContext())
                            .setText(toastText)
                            .build().show();
                }
            });
        }
    }

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void onStreamerError(int errorCode, Throwable throwable) {
            plvsaSettingCameraOrientIv.setEnabled(false);
            plvsaSettingCameraOrientTv.setEnabled(false);
            plvsaSettingCameraOrientTv.setAlpha(0.6F);
            plvsaSettingMirrorIv.setEnabled(false);
            plvsaSettingMirrorTv.setEnabled(false);
            plvsaSettingMirrorTv.setAlpha(0.6F);
        }

        @Override
        public void onCameraDirection(boolean front, int pos) {
            if (front) {
                plvsaSettingMirrorIv.setEnabled(true);
                plvsaSettingMirrorTv.setEnabled(true);
                plvsaSettingMirrorTv.setAlpha(1F);
            } else {
                plvsaSettingMirrorIv.setEnabled(false);
                plvsaSettingMirrorTv.setEnabled(false);
                plvsaSettingMirrorTv.setAlpha(0.6F);
            }
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.plvsa_setting_close_page_iv) {
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.plvsa_setting_start_live_btn) {
            if (onViewActionListener != null) {
                int currentNetworkQuality = onViewActionListener.getCurrentNetworkQuality();
                if (currentNetworkQuality == PLVSStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                    //如果断网，则不直播，显示弹窗。
                    showAlertDialogNoNetwork();
                    return;
                }
            }
            if (liveTitle.length() == 0) {
                PLVToast.Builder.context(getContext())
                        .setText("直播标题不能为空")
                        .build()
                        .show();
                return;
            }
            checkPermissionToStartCountDown();
        } else if (id == R.id.plvsa_setting_camera_orient_iv
                || id == R.id.plvsa_setting_camera_orient_tv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (streamerPresenter != null) {
                    streamerPresenter.setCameraDirection(!isFrontCamera);
                }
                lastClickCameraSwitchViewTime = currentTime;
            }
        } else if (id == R.id.plvsa_setting_mirror_iv
                || id == R.id.plvsa_setting_mirror_tv) {
            if (onViewActionListener != null) {
                onViewActionListener.setMirrorMode(!isMirrorMode);
            }
        } else if (id == R.id.plvsa_setting_bitrate_iv
                || id == R.id.plvsa_setting_bitrate_tv) {
            bitrateLayout.open();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    /**
     * 开播前检查权限
     */
    private void checkPermissionToStartCountDown() {
        ArrayList<String> permissions = new ArrayList<>(2);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);

        PLVFastPermission.getInstance().start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                // 检查权限通过，隐藏设置布局，开始倒计时
                setVisibility(View.GONE);
                countDownWindow.startCountDown();
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                final PLVConfirmDialog confirmDialog = new PLVSAConfirmDialog(getContext());
                for (String deniedPermission : deniedPermissions) {
                    if (Manifest.permission.CAMERA.equals(deniedPermission)) {
                        confirmDialog.setTitle("摄像头权限申请")
                                .setContent("请前往“设置-隐私-摄像头”开启权限")
                                .setLeftButtonText("取消")
                                .setLeftBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        confirmDialog.hide();
                                    }
                                })
                                .setRightButtonText("设置")
                                .setRightBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PLVFastPermission.getInstance().jump2Settings(getContext());
                                    }
                                });
                        break;
                    }
                    if (Manifest.permission.RECORD_AUDIO.equals(deniedPermission)) {
                        confirmDialog.setTitle("麦克风权限申请")
                                .setContent("请前往“设置-隐私-麦克风”开启权限")
                                .setLeftButtonText("取消")
                                .setLeftBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        confirmDialog.hide();
                                    }
                                })
                                .setRightButtonText("设置")
                                .setRightBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PLVFastPermission.getInstance().jump2Settings(getContext());
                                    }
                                });
                        break;
                    }
                }
                confirmDialog.show();
            }
        });
    }

    private void updateBitrateIcon(int selectedBitrate) {
        switch (selectedBitrate) {
            case PLVSStreamerConfig.Bitrate.BITRATE_STANDARD:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_sd);
                break;
            case PLVSStreamerConfig.Bitrate.BITRATE_HIGH:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_hd);
                break;
            case PLVSStreamerConfig.Bitrate.BITRATE_SUPER:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_uhd);
                break;
            default:
        }
    }

    // </editor-fold>
}
