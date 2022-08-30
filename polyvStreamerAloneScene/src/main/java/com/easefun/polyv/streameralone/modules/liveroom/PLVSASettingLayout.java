package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.kv.PLVAutoSaveKV;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 开播设置布局
 */
public class PLVSASettingLayout extends FrameLayout implements IPLVSASettingLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 设置配置布局左右间距
    private final static int SETTING_CONFIG_LAYOUT_HORIZON_MARGIN_PORT = ConvertUtils.dp2px(24);
    private final static int SETTING_CONFIG_LAYOUT_HORIZON_MARGIN_LAND = ConvertUtils.dp2px(132);
    // 开始直播按钮左右间距
    private final static int START_LIVE_BUTTON_HORIZON_MARGIN_PORT = ConvertUtils.dp2px(24);
    private final static int START_LIVE_BUTTON_HORIZON_MARGIN_LAND = ConvertUtils.dp2px(144);
    // 标题框最大显示行数
    private final static int LIVE_TITLE_MAX_LINES_PORT = Integer.MAX_VALUE;
    private final static int LIVE_TITLE_MAX_LINES_LAND = 2;

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
    private View plvsaSettingLiveTitleSplitView;
    private ImageView plvsaSettingCameraOrientIv;
    private ImageView plvsaSettingMirrorIv;
    private ImageView plvsaSettingBitrateIv;
    private ImageView plvsaSettingScreenOrientationIv;
    private TextView plvsaSettingCameraOrientTv;
    private TextView plvsaSettingMirrorTv;
    private TextView plvsaSettingBitrateTv;
    private TextView plvsaSettingScreenOrientationTv;
    private LinearLayout plvsaSettingBtnLl;
    private PLVRoundRectLayout plvsaSettingBeautyLayout;
    private ImageView plvsaSettingBeautyIv;
    private Button plvsaSettingStartLiveBtn;
    private PLVOrientationSensibleLinearLayout settingPushResolutionRatioLl;
    private ImageView settingPushResolutionRatioIv;
    private TextView settingPushResolutionRatioTv;

    private String liveTitle;

    private PLVAutoSaveKV<Map<String, PLVLinkMicConstant.PushResolutionRatio>> landscapeChannelPushRatioMapKV = new PLVAutoSaveKV<Map<String, PLVLinkMicConstant.PushResolutionRatio>>("plvsa_setting_push_resolution_ratio_land_key") {};

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;

    //listener
    private OnViewActionListener onViewActionListener;

    private boolean isSettingFinished = false;
    private boolean isBeautyLayoutShowing = false;

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
        plvsaSettingLiveTitleSplitView = findViewById(R.id.plvsa_setting_live_title_split_view);
        plvsaSettingCameraOrientIv = findViewById(R.id.plvsa_setting_camera_orient_iv);
        plvsaSettingMirrorIv = findViewById(R.id.plvsa_setting_mirror_iv);
        plvsaSettingBitrateIv = findViewById(R.id.plvsa_setting_bitrate_iv);
        plvsaSettingScreenOrientationIv = findViewById(R.id.plvsa_setting_screen_orientation_iv);
        plvsaSettingCameraOrientTv = findViewById(R.id.plvsa_setting_camera_orient_tv);
        plvsaSettingMirrorTv = findViewById(R.id.plvsa_setting_mirror_tv);
        plvsaSettingBitrateTv = findViewById(R.id.plvsa_setting_bitrate_tv);
        plvsaSettingScreenOrientationTv = findViewById(R.id.plvsa_setting_screen_orientation_tv);
        plvsaSettingBtnLl = findViewById(R.id.plvsa_setting_btn_ll);
        plvsaSettingBeautyLayout = findViewById(R.id.plvsa_setting_beauty_layout);
        plvsaSettingBeautyIv = findViewById(R.id.plvsa_setting_beauty_iv);
        plvsaSettingStartLiveBtn = findViewById(R.id.plvsa_setting_start_live_btn);
        settingPushResolutionRatioLl = findViewById(R.id.plvsa_setting_push_resolution_ratio_ll);
        settingPushResolutionRatioIv = findViewById(R.id.plvsa_setting_push_resolution_ratio_iv);
        settingPushResolutionRatioTv = findViewById(R.id.plvsa_setting_push_resolution_ratio_tv);

        plvsaSettingClosePageIv.setOnClickListener(this);
        plvsaSettingBeautyLayout.setOnClickListener(this);
        plvsaSettingStartLiveBtn.setOnClickListener(this);
        plvsaSettingCameraOrientIv.setOnClickListener(this);
        plvsaSettingCameraOrientTv.setOnClickListener(this);
        plvsaSettingMirrorIv.setOnClickListener(this);
        plvsaSettingMirrorTv.setOnClickListener(this);
        plvsaSettingBitrateIv.setOnClickListener(this);
        plvsaSettingBitrateTv.setOnClickListener(this);
        plvsaSettingScreenOrientationIv.setOnClickListener(this);
        plvsaSettingScreenOrientationTv.setOnClickListener(this);
        settingPushResolutionRatioLl.setOnClickListener(this);

        initTitleInputLayout();
        initBitrateLayout();
        initTitleTextOnClickListener();
        initBeginCountDownWindow();

        observeBeautyModuleInitResult();
        observeBeautyLayoutStatus();
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
                plvsaSettingBitrateTv.setText(PLVStreamerConfig.Bitrate.getText(bitrate));
                updateBitrateIcon(bitrate);
                PLVLiveLocalActionHelper.getInstance().updateBitrate(bitrate);
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
                if(isGuest()){
                    return;
                }
                ViewGroup.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                removeView(titleInputLayout);
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
                isSettingFinished = true;
                updateVisibility();
                liveRoomDataManager.getConfig().setupChannelName(liveTitle);
                liveRoomDataManager.requestUpdateChannelName();
                if (onViewActionListener != null) {
                    if (isGuest()) {
                        onViewActionListener.onEnterLiveAction();
                    } else {
                        onViewActionListener.onStartLiveAction();
                    }
                }
            }

            @Override
            public void onCountDownCanceled() {
                // 倒计时取消，重新显示设置布局
                isSettingFinished = false;
                updateVisibility();
            }
        });
    }

    private void observeBeautyModuleInitResult() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        if (beautyUiState == null) {
                            return;
                        }
                        final boolean isBeautySupport = beautyUiState.isBeautySupport;
                        final boolean isInitSuccess = beautyUiState.isBeautyModuleInitSuccess;
                        final boolean showBeautyLayout = isBeautySupport && isInitSuccess;
                        plvsaSettingBeautyLayout.setVisibility(showBeautyLayout ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void observeBeautyLayoutStatus() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSASettingLayout.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }

    private void initStartLiveBtnText() {
        plvsaSettingStartLiveBtn.setText(isGuest() ? getContext().getString(R.string.plvsa_setting_enter_live) : getContext().getString(R.string.plvsa_setting_start_live));
    }

    private void initPushResolutionRatioLayout() {
        final boolean userAllowChangeRatio = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RATIO);
        final boolean channelAllowChangeRatio = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RESOLUTION_RATIO);
        settingPushResolutionRatioLl.setShowOnLandscape(userAllowChangeRatio && channelAllowChangeRatio);
    }

    /**
     * 判断当前用户类型是否是嘉宾
     */
    private boolean isGuest() {
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        return PLVSocketUserConstant.USERTYPE_GUEST.equals(userType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSASettingLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        this.liveTitle = liveRoomDataManager.getConfig().getChannelName();
        plvsaSettingLiveTitleTv.setText(liveTitle);
        titleInputLayout.initTitle(liveTitle);
        initStartLiveBtnText();
        initPushResolutionRatioLayout();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;

        if (onViewActionListener != null) {
            Pair<Integer, Integer> bitrateInfo = onViewActionListener.getBitrateInfo();
            if (bitrateInfo != null) {
                plvsaSettingBitrateTv.setText(PLVStreamerConfig.Bitrate.getText(bitrateInfo.second));
                updateBitrateIcon(bitrateInfo.second);
                PLVLiveLocalActionHelper.getInstance().updateBitrate(bitrateInfo.second);
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

    @Override
    public void liveStart() {
        if (onViewActionListener != null) {
            int currentNetworkQuality = onViewActionListener.getCurrentNetworkQuality();
            if (currentNetworkQuality == PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
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
        //标记默认的布局方向
        PLVLiveLocalActionHelper.getInstance().updateOrientation(PLVScreenUtils.isPortrait(getContext()));
        //检查权限并开始直播
        checkPermissionToStartCountDown();
    }

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 MVP - View">

    private void initStreamerMvpView() {
        if (onViewActionListener != null) {
            streamerPresenter = onViewActionListener.getStreamerPresenter();
        }
        if (streamerPresenter != null) {
            streamerPresenter.registerView(streamerView);
            observePushResolutionRatio();
            streamerPresenter.getData().getCurBitrate().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer bitrate) {
                    if (bitrate == null || getContext() == null || !switchBitrateByUser) {
                        return;
                    }
                    switchBitrateByUser = false;
                    String toastText = "已切换为" + PLVStreamerConfig.Bitrate.getText(bitrate);
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

    private void observePushResolutionRatio() {
        if (streamerPresenter == null) {
            return;
        }
        streamerPresenter.getData().getPushResolutionRatio()
                .observe((LifecycleOwner) getContext(), new Observer<PLVLinkMicConstant.PushResolutionRatio>() {
                    @Override
                    public void onChanged(@Nullable PLVLinkMicConstant.PushResolutionRatio resolutionRatio) {
                        if (resolutionRatio == null) {
                            return;
                        }
                        if (ScreenUtils.isLandscape()) {
                            saveLandscapePushResolutionRatio(resolutionRatio);
                        }
                        switch (resolutionRatio) {
                            case RATIO_16_9:
                                settingPushResolutionRatioIv.setImageResource(R.drawable.plvsa_live_room_setting_ratio_16_9);
                                break;
                            case RATIO_4_3:
                                settingPushResolutionRatioIv.setImageResource(R.drawable.plvsa_live_room_setting_ratio_4_3);
                                break;
                            default:
                        }
                    }
                });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updatePushResolutionRatioOnOrientationChanged(newConfig);

        MarginLayoutParams settingConfigLayoutParam = (MarginLayoutParams) plvsaSettingConfigLy.getLayoutParams();
        MarginLayoutParams settingButtonLayoutParam = (MarginLayoutParams) plvsaSettingBtnLl.getLayoutParams();
        int liveTitleMaxLines;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            settingConfigLayoutParam.width = ConvertUtils.dp2px(450);
            settingButtonLayoutParam.width = ConvertUtils.dp2px(450);
            settingConfigLayoutParam.leftMargin = settingConfigLayoutParam.rightMargin = SETTING_CONFIG_LAYOUT_HORIZON_MARGIN_LAND;
            settingButtonLayoutParam.leftMargin = settingButtonLayoutParam.rightMargin = START_LIVE_BUTTON_HORIZON_MARGIN_LAND;
            liveTitleMaxLines = LIVE_TITLE_MAX_LINES_LAND;
        } else {
            settingConfigLayoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            settingButtonLayoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT;
            settingConfigLayoutParam.leftMargin = settingConfigLayoutParam.rightMargin = SETTING_CONFIG_LAYOUT_HORIZON_MARGIN_PORT;
            settingButtonLayoutParam.leftMargin = settingButtonLayoutParam.rightMargin = START_LIVE_BUTTON_HORIZON_MARGIN_PORT;
            liveTitleMaxLines = LIVE_TITLE_MAX_LINES_PORT;
        }

        plvsaSettingConfigLy.setLayoutParams(settingConfigLayoutParam);
        plvsaSettingBtnLl.setLayoutParams(settingButtonLayoutParam);
        plvsaSettingLiveTitleTv.setMaxLines(liveTitleMaxLines);
    }

    private void updatePushResolutionRatioOnOrientationChanged(Configuration newConfig) {
        final boolean userAllowChangeRatio = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RATIO);
        final boolean channelAllowChangeRatio = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RESOLUTION_RATIO);
        if (!userAllowChangeRatio || !channelAllowChangeRatio) {
            return;
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏推流只支持 16:9
            if (streamerPresenter != null) {
                streamerPresenter.setPushResolutionRatio(PLVLinkMicConstant.PushResolutionRatio.RATIO_16_9);
            }
        } else {
            // 横屏自动恢复上次选择的画面比例
            final PLVLinkMicConstant.PushResolutionRatio lastLandscapePushResolutionRatio = readLandscapePushResolutionRatio();
            if (streamerPresenter != null && lastLandscapePushResolutionRatio != null && streamerPresenter.getData().getPushResolutionRatio().getValue() != lastLandscapePushResolutionRatio) {
                streamerPresenter.setPushResolutionRatio(lastLandscapePushResolutionRatio);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        if (!PLVDebounceClicker.tryClick(this)) {
            return;
        }
        int id = v.getId();
        if (id == R.id.plvsa_setting_close_page_iv) {
            ((Activity) getContext()).onBackPressed();
        } else if (id == R.id.plvsa_setting_start_live_btn) {
            liveStart();
        } else if (id == R.id.plvsa_setting_camera_orient_iv
                || id == R.id.plvsa_setting_camera_orient_tv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (streamerPresenter != null) {
                    streamerPresenter.setCameraDirection(!isFrontCamera);
                    PLVLiveLocalActionHelper.getInstance().updateCameraDirection(!isFrontCamera);
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
        } else if (id == plvsaSettingScreenOrientationIv.getId()
                || id == plvsaSettingScreenOrientationTv.getId()) {
            changeScreenOrientation();
        } else if (id == plvsaSettingBeautyLayout.getId()) {
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == settingPushResolutionRatioLl.getId()) {
            changePushResolutionRatio();
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
                isSettingFinished = true;
                updateVisibility();
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
            case PLVStreamerConfig.Bitrate.BITRATE_STANDARD:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_sd);
                break;
            case PLVStreamerConfig.Bitrate.BITRATE_HIGH:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_hd);
                break;
            case PLVStreamerConfig.Bitrate.BITRATE_SUPER:
                plvsaSettingBitrateIv.setImageResource(R.drawable.plvsa_bitrate_icon_uhd);
                break;
            default:
        }
    }

    private void changeScreenOrientation() {
        if (PLVScreenUtils.isPortrait(getContext())) {
            PLVScreenUtils.enterLandscape((Activity) getContext());
            ScreenUtils.setLandscape((Activity) getContext());
            PLVLiveLocalActionHelper.getInstance().updateOrientation(false);
        } else {
            PLVScreenUtils.enterPortrait((Activity) getContext());
            ScreenUtils.setPortrait((Activity) getContext());
            PLVLiveLocalActionHelper.getInstance().updateOrientation(true);
        }
    }

    private void saveLandscapePushResolutionRatio(@Nullable PLVLinkMicConstant.PushResolutionRatio resolutionRatio) {
        final Map<String, PLVLinkMicConstant.PushResolutionRatio> channelRatioMap = getOrDefault(landscapeChannelPushRatioMapKV.get(), new HashMap<String, PLVLinkMicConstant.PushResolutionRatio>());
        channelRatioMap.put(liveRoomDataManager.getConfig().getChannelId(), resolutionRatio);
        landscapeChannelPushRatioMapKV.set(channelRatioMap);
    }

    @Nullable
    private PLVLinkMicConstant.PushResolutionRatio readLandscapePushResolutionRatio() {
        final String channelId = liveRoomDataManager.getConfig().getChannelId();
        final Map<String, PLVLinkMicConstant.PushResolutionRatio> channelRatioMap = getOrDefault(landscapeChannelPushRatioMapKV.get(), new HashMap<String, PLVLinkMicConstant.PushResolutionRatio>());
        final PLVLinkMicConstant.PushResolutionRatio localRatio = channelRatioMap.get(channelId);
        if (localRatio != null) {
            return localRatio;
        }
        return PLVChannelFeatureManager.onChannel(channelId).get(PLVChannelFeature.STREAMER_ALONE_DEFAULT_PUSH_RESOLUTION_RATIO);
    }

    private void changePushResolutionRatio() {
        if (streamerPresenter == null) {
            return;
        }
        final boolean userAllowChangeRatio = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RATIO);
        final boolean channelAllowChangeRatio = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RESOLUTION_RATIO);
        if (!userAllowChangeRatio || !channelAllowChangeRatio) {
            return;
        }

        final PLVLinkMicConstant.PushResolutionRatio currentRatio = streamerPresenter.getData().getPushResolutionRatio().getValue();
        if (currentRatio == null) {
            streamerPresenter.setPushResolutionRatio(PLVLinkMicConstant.PushResolutionRatio.RATIO_16_9);
        } else {
            streamerPresenter.setPushResolutionRatio(currentRatio.next());
        }
    }

    private void updateVisibility() {
        // 美颜布局显示时，不显示设置布局
        if (isBeautyLayoutShowing) {
            setVisibility(View.GONE);
            return;
        }
        // 设置结束后隐藏
        if (isSettingFinished) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
    }

    // </editor-fold>
}
