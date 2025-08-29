package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVImageSelectorUtil;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVVirtualBackgroundLayout;
import com.easefun.polyv.livecommon.module.utils.water.PLVImagePickerUtil;
import com.easefun.polyv.livecommon.module.utils.water.PLVPhotoContainer;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.google.android.flexbox.FlexboxLayout;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.kv.PLVAutoSaveKV;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.image.segmenter.api.IPLVImageSegmenterManager;
import com.plv.image.segmenter.api.PLVImageSegmenterManager;
import com.plv.image.segmenter.api.enums.PLVImageSegmenterInitCode;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVPushStreamTemplateJsonBean;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

/**
 * 开播设置布局
 */
public class PLVSASettingLayout extends FrameLayout implements IPLVSASettingLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVSASettingLayout.class.getSimpleName();

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private String channelId;
    //data
    private boolean isFrontCamera;
    private boolean isMirrorMode;
    // 直播标题输入布局
    private PLVSASettingTitleInputLayout titleInputLayout;
    //清晰度设置布局
    private PLVSABitrateLayout bitrateLayout;
    //混流布局设置布局
    private PLVSAMixLayout mixLayout;
    private PLVSAStickerLayout stickerLayout;
    //推流开始倒计时布局
    private IPLVSACountDownWindow countDownWindow;
    // 降噪配置布局
    private final PLVSADenoisePreferenceLayout denoisePreferenceLayout = new PLVSADenoisePreferenceLayout(getContext());
    // 外接设备布局
    private final PLVSAExternalAudioInputPreferenceLayout externalAudioInputPreferenceLayout = new PLVSAExternalAudioInputPreferenceLayout(getContext());

    // 推流Presenter
    @Nullable
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //view
    private ImageView plvsaSettingClosePageIv;
    private ConstraintLayout plvsaSettingConfigLy;
    private TextView plvsaSettingLiveTitleTv;
    private View plvsaSettingLiveTitleSplitView;
    private FlexboxLayout settingActionScrollContainer;
    private ImageView plvsaSettingCameraOrientIv;
    private ImageView plvsaSettingMirrorIv;
    private ImageView plvsaSettingBitrateIv;
    private TextView plvsaSettingCameraOrientTv;
    private TextView plvsaSettingMirrorTv;
    private TextView plvsaSettingBitrateTv;
    private LinearLayout settingMixLayout;
    private LinearLayout settingScreenOrientationLayout;
    private LinearLayout plvsaSettingBtnLl;
    private LinearLayout plvsaSettingBeautyLayout;
    private Button plvsaSettingStartLiveBtn;
    private PLVOrientationSensibleLinearLayout settingPushResolutionRatioLl;
    private ImageView settingPushResolutionRatioIv;
    private TextView settingPushResolutionRatioTv;
    private LinearLayout settingDenoiseLayout;
    private LinearLayout settingExternalAudioInputLayout;
    private LinearLayout settingLiveReplaySwitchLayout;
    private LinearLayout settingMoreLayout;
    private LinearLayout settingWaterLayout;
    private ViewGroup settingLayout;
    private LinearLayout settingVirtualBgLayout;

    private PLVSASettingMoreLayout settingMorePopupLayout;
    private PLVPhotoContainer waterLayout;
    private PLVVirtualBackgroundLayout virtualBackgroundLayout;

    private String liveTitle;

    private PLVAutoSaveKV<Map<String, PLVLinkMicConstant.PushResolutionRatio>> landscapeChannelPushRatioMapKV = new PLVAutoSaveKV<Map<String, PLVLinkMicConstant.PushResolutionRatio>>("plvsa_setting_push_resolution_ratio_land_key") {
    };

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;

    //listener
    private OnViewActionListener onViewActionListener;
    private IPLVImageSegmenterManager.InitCallback imageSegmenterInitCallback = null;

    private boolean isSettingFinished = false;
    private boolean isBeautyLayoutShowing = false;

    private long lastClickCameraSwitchViewTime;

    private Map<Integer, Integer> bitrateMapIcon = new HashMap<Integer, Integer>() {{
        put(PLVStreamerConfig.Bitrate.BITRATE_STANDARD, R.drawable.plvsa_bitrate_icon_sd);
        put(PLVStreamerConfig.Bitrate.BITRATE_HIGH, R.drawable.plvsa_bitrate_icon_hd);
        put(PLVStreamerConfig.Bitrate.BITRATE_SUPER, R.drawable.plvsa_bitrate_icon_fhd);
        put(PLVStreamerConfig.Bitrate.BITRATE_SUPER_HIGH, R.drawable.plvsa_bitrate_icon_uhd);
    }};
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PLVVirtualBackgroundLayout.destroy();
        PLVSAStickerLayout.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_setting_layout, this, true);

        plvsaSettingClosePageIv = findViewById(R.id.plvsa_setting_close_page_iv);
        plvsaSettingConfigLy = findViewById(R.id.plvsa_setting_config_ly);
        plvsaSettingLiveTitleTv = findViewById(R.id.plvsa_setting_live_title_tv);
        plvsaSettingLiveTitleSplitView = findViewById(R.id.plvsa_setting_live_title_split_view);
        settingActionScrollContainer = findViewById(R.id.plvsa_setting_action_scroll_container);
        plvsaSettingCameraOrientIv = findViewById(R.id.plvsa_setting_camera_orient_iv);
        plvsaSettingMirrorIv = findViewById(R.id.plvsa_setting_mirror_iv);
        plvsaSettingBitrateIv = findViewById(R.id.plvsa_setting_bitrate_iv);
        plvsaSettingCameraOrientTv = findViewById(R.id.plvsa_setting_camera_orient_tv);
        plvsaSettingMirrorTv = findViewById(R.id.plvsa_setting_mirror_tv);
        plvsaSettingBitrateTv = findViewById(R.id.plvsa_setting_bitrate_tv);
        settingMixLayout = findViewById(R.id.plvsa_setting_mix_layout);
        settingScreenOrientationLayout = findViewById(R.id.plvsa_setting_screen_orientation_layout);
        plvsaSettingBtnLl = findViewById(R.id.plvsa_setting_btn_ll);
        plvsaSettingBeautyLayout = findViewById(R.id.plvsa_setting_beauty_layout);
        plvsaSettingStartLiveBtn = findViewById(R.id.plvsa_setting_start_live_btn);
        settingPushResolutionRatioLl = findViewById(R.id.plvsa_setting_push_resolution_ratio_ll);
        settingPushResolutionRatioIv = findViewById(R.id.plvsa_setting_push_resolution_ratio_iv);
        settingPushResolutionRatioTv = findViewById(R.id.plvsa_setting_push_resolution_ratio_tv);
        settingDenoiseLayout = findViewById(R.id.plvsa_setting_denoise_layout);
        settingExternalAudioInputLayout = findViewById(R.id.plvsa_setting_external_audio_input_layout);
        settingLiveReplaySwitchLayout = findViewById(R.id.plvsa_setting_live_replay_switch_layout);
        settingMoreLayout = findViewById(R.id.plvsa_setting_more_layout);
        settingMorePopupLayout = new PLVSASettingMoreLayout(this);
        settingWaterLayout = findViewById(R.id.plvsa_setting_live_water_layout);
        settingLayout = findViewById(R.id.plvsa_setting_ly);
        settingVirtualBgLayout = findViewById(R.id.plvsa_setting_virtual_background_layout);

        plvsaSettingClosePageIv.setOnClickListener(this);
        plvsaSettingBeautyLayout.setOnClickListener(this);
        plvsaSettingStartLiveBtn.setOnClickListener(this);
        plvsaSettingCameraOrientIv.setOnClickListener(this);
        plvsaSettingCameraOrientTv.setOnClickListener(this);
        plvsaSettingMirrorIv.setOnClickListener(this);
        plvsaSettingMirrorTv.setOnClickListener(this);
        plvsaSettingBitrateIv.setOnClickListener(this);
        plvsaSettingBitrateTv.setOnClickListener(this);
        settingMixLayout.setOnClickListener(this);
        settingScreenOrientationLayout.setOnClickListener(this);
        settingPushResolutionRatioLl.setOnClickListener(this);
        settingDenoiseLayout.setOnClickListener(this);
        settingExternalAudioInputLayout.setOnClickListener(this);
        settingLiveReplaySwitchLayout.setOnClickListener(this);
        settingMoreLayout.setOnClickListener(this);
        settingWaterLayout.setOnClickListener(this);
        settingVirtualBgLayout.setOnClickListener(this);

        initWaterLayout();
        initTitleInputLayout();
        initBitrateLayout();
        initMixLayout();
        initStickerLayout();
        initTitleTextOnClickListener();
        initBeginCountDownWindow();
        initDenoiseLayout();
        initExternalAudioInputLayout();

        observeBeautyModuleInitResult();
        observeBeautyLayoutStatus();
    }

    private void initWaterLayout() {
        waterLayout = findViewById(R.id.plvsa_water_layout);
        waterLayout.setOnViewActionListener(new PLVPhotoContainer.OnViewActionListener() {
            @Override
            public void onEditMode(boolean isEditMode) {
                if (isEditMode) {
                    waterLayout.bringToFront();
                    settingLayout.setAlpha(0.5f);
                    bringToFront();
                } else {
                    settingLayout.bringToFront();
                    settingLayout.setAlpha(1f);
                    for (int i = 0; i < ((ViewGroup) getParent()).getChildCount(); i++) {
                        View child = ((ViewGroup) getParent()).getChildAt(i);
                        if (child != null && !child.equals(PLVSASettingLayout.this)) {
                            child.bringToFront();
                        }
                    }
                    if (isSettingFinished && streamerPresenter != null) {
                        streamerPresenter.setWatermark(waterLayout.captureView(), 0, 0, 1);
                    }
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onEditMode(isEditMode);
                }
            }
        });
    }

    private void initVirtualBgLayout() {
        virtualBackgroundLayout = PLVVirtualBackgroundLayout.init(this, new PLVVirtualBackgroundLayout.OnViewActionListener() {
            @Override
            public void onConfirmDeleteBg(final int position) {
                new PLVSAConfirmDialog(getContext())
                        .setTitleVisibility(View.GONE)
                        .setContent(R.string.plv_streamer_bg_delete_confirm)
                        .setIsNeedLeftBtn(true)
                        .setCancelable(true)
                        .setLeftButtonText(R.string.plv_common_dialog_cancel)
                        .setRightButtonText(R.string.plv_common_dialog_confirm_2)
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                virtualBackgroundLayout.confirmDeleteBg(position);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }

            @Override
            public void onSelectedBg(Bitmap bitmap) {
                if (streamerPresenter != null) {
                    streamerPresenter.setVirtualBackground(bitmap, false);
                }
            }

            @Override
            public void onCancelBgAndBlur() {
                if (streamerPresenter != null) {
                    streamerPresenter.setVirtualBackground(null, false);
                }
            }

            @Override
            public void onSelectedBlur() {
                if (streamerPresenter != null) {
                    streamerPresenter.setVirtualBackground(null, true);
                }
            }
        });
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
                    plvsaSettingLiveTitleTv.setText(R.string.plv_live_input_live_title_hint);
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
                plvsaSettingBitrateTv.setText(PLVStreamerConfig.QualityLevel.getTextCombineTemplate(bitrate, channelId));
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

    private void initMixLayout() {
        mixLayout = new PLVSAMixLayout(getContext());
        mixLayout.setOnViewActionListener(new PLVSAMixLayout.OnViewActionListener() {
            @Override
            public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
                return onViewActionListener != null ? onViewActionListener.getMixLayoutType() : PLVStreamerConfig.MixLayoutType.TILE;
            }

            @Override
            public void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix) {
                mixLayout.close();
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeMixLayoutType(mix);
                }
            }
        });
    }

    private void initStickerLayout() {
        stickerLayout = PLVSAStickerLayout.init(getContext(), new PLVSAStickerLayout.OnViewActionListener() {
            @Override
            public void onClick(boolean isClickText) {
                stickerLayout.close();
                if (isClickText) {
                    waterLayout.previewTextSticker();
                } else {
                    PLVImagePickerUtil.openGallery((Activity) getContext());
                }
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
                if (isGuest()) {
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
                waterLayout.setSettingFinished(true);
                updateVisibility();
                // 进入直播间时再设置水印
                if (streamerPresenter != null) {
                    streamerPresenter.setWatermark(waterLayout.captureView(), 0, 0, 1);
                }
                liveRoomDataManager.getConfig().setupChannelName(liveTitle);
                liveRoomDataManager.requestUpdateChannelName(new IPLVLiveRoomDataManager.IUpdateChannelNameListener() {
                    @Override
                    public void onAfter() {
                        if (onViewActionListener != null) {
                            if (isGuest()) {
                                onViewActionListener.onEnterLiveAction();
                            } else {
                                onViewActionListener.onStartLiveAction();
                            }
                        }
                    }
                });

            }

            @Override
            public void onCountDownCanceled() {
                // 倒计时取消，重新显示设置布局
                isSettingFinished = false;
                waterLayout.setSettingFinished(false);
                updateVisibility();
            }
        });
    }

    private void initDenoiseLayout() {
        denoisePreferenceLayout.setOnViewActionListener(new PLVSADenoisePreferenceLayout.OnViewActionListener() {
            @Nullable
            @Override
            public PLVLinkMicDenoiseType getCurrentDenoiseType() {
                if (streamerPresenter != null) {
                    return streamerPresenter.getData().getDenoiseType().getValue();
                } else {
                    return null;
                }
            }

            @Override
            public void onDenoiseChanged(@NonNull PLVLinkMicDenoiseType denoiseType) {
                if (streamerPresenter != null) {
                    streamerPresenter.setDenoiseType(denoiseType);
                }
            }
        });
    }

    private void initExternalAudioInputLayout() {
        externalAudioInputPreferenceLayout.setOnViewActionListener(new PLVSAExternalAudioInputPreferenceLayout.OnViewActionListener() {
            @Override
            public boolean currentIsEnableExternalAudioInput() {
                if (streamerPresenter != null) {
                    return streamerPresenter.getData().getUseExternalAudioInput().getValue();
                } else {
                    return false;
                }
            }

            @Override
            public void onEnableExternalAudioInputChanged(boolean enable) {
                if (streamerPresenter != null) {
                    streamerPresenter.setIsUseExternalAudioInput(enable);
                }
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

    private void observeScrollContainer() {
        checkShowMoreAction();
        settingActionScrollContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkShowMoreAction();
                    }
                }, 500); // 需要加一定的延迟，必现view移除后留下空位/添加时重叠在一起
            }
        });
    }

    private void checkShowMoreAction() {
        // 1、确保定义时其他可动态调整状态的item默认为隐藏，2、确保可能放到更多里的item的状态在初始化之后不可再调整
        int showMoreMaxItemCount = 10;
        int childVisibleCount = 0;
        for (int i = 0; i < settingActionScrollContainer.getChildCount(); i++) {
            View child = settingActionScrollContainer.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE && child != settingMoreLayout) {
                childVisibleCount++;
            }
        }
        // 大于等于10个，移除最后的item添加到更多弹窗布局中
        if (childVisibleCount >= showMoreMaxItemCount) {
            settingMoreLayout.setVisibility(View.VISIBLE);
            List<View> addItemViews = new ArrayList<>();
            int index = settingActionScrollContainer.getChildCount() - 2; // 跳过更多按钮
            while (childVisibleCount >= showMoreMaxItemCount) {
                View child = settingActionScrollContainer.getChildAt(index);
                if (child.getVisibility() != View.VISIBLE) {
                    index--;
                    continue;
                }
                settingActionScrollContainer.removeView(child);
                addItemViews.add(0, child);
                index--;
                childVisibleCount--;
            }
            for (View addItemView : addItemViews) {
                settingMorePopupLayout.addItem(addItemView);
            }
        } else if (settingMorePopupLayout.hasItem() && childVisibleCount < showMoreMaxItemCount - 1) { // 小于9个时，检查是否要从更多弹窗布局中移除item回来
            while (childVisibleCount < showMoreMaxItemCount - 1) {
                View child = settingMorePopupLayout.removePreviousItem();
                if (child != null) {
                    settingActionScrollContainer.addView(child, settingActionScrollContainer.getChildCount() - 1);
                    childVisibleCount++;
                } else {
                    break;
                }
            }
            if (!settingMorePopupLayout.hasItem()) {
                settingMoreLayout.setVisibility(View.GONE);
            }
        }
    }

    private void initStartLiveBtnText() {
        plvsaSettingStartLiveBtn.setText(isGuest() ? getContext().getString(R.string.plvsa_setting_enter_live) : getContext().getString(R.string.plv_streamer_start_live));
    }

    private void initButtonVisibility(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean userAllowChangeRatio = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RATIO);
        final boolean channelAllowChangeRatio = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RESOLUTION_RATIO);
        settingPushResolutionRatioLl.setShowOnLandscape(userAllowChangeRatio && channelAllowChangeRatio);

        final boolean userAllowChangeLiveReplay = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_REPLAY_OPEN);
        settingLiveReplaySwitchLayout.setVisibility(userAllowChangeLiveReplay ? View.VISIBLE : View.GONE);

        final boolean showOrientationButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_SETTING_SHOW_ORIENTATION_BUTTON, true);
        settingScreenOrientationLayout.setVisibility(showOrientationButton ? View.VISIBLE : View.GONE);

        final boolean showMixLayoutButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_SETTING_SHOW_MIX_LAYOUT_BUTTON, true);
        if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_MIX_LAYOUT) && showMixLayoutButton) {
            settingMixLayout.setVisibility(View.VISIBLE);
        } else {
            settingMixLayout.setVisibility(View.GONE);
        }

        boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
        settingWaterLayout.setVisibility(showWatermarkButton ? View.VISIBLE : View.GONE);

        // 监听图片分割初始化回调
        PLVImageSegmenterManager.getInstance().addInitCallback(new WeakReference<>(imageSegmenterInitCallback = new IPLVImageSegmenterManager.InitCallback() {
            @Override
            public void onFinishInit(PLVImageSegmenterInitCode code) {
                PLVCommonLog.i(TAG, "onImageSegmenterFinishInit, code: " + code);
                if (code == PLVImageSegmenterInitCode.SUCCESS) {
                    settingVirtualBgLayout.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void initOrientation(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean isDefaultLandscape = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_ALONE_DEFAULT_LANDSCAPE_RESOLUTION, false);
        if (PLVScreenUtils.isLandscape(getContext()) != isDefaultLandscape) {
            changeScreenOrientation();
        }
    }

    /**
     * 判断当前用户类型是否是嘉宾
     */
    private boolean isGuest() {
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        return PLVSocketUserConstant.USERTYPE_GUEST.equals(userType);
    }

    private void initBitrateMapIcon() {
        PLVPushStreamTemplateJsonBean pushStreamTemplateJsonBean = PLVStreamerConfig.getPushStreamTemplate(channelId);
        if (pushStreamTemplateJsonBean != null && pushStreamTemplateJsonBean.isEnabled()) {
            bitrateMapIcon.clear();
            int i = 0;
            for (PLVPushStreamTemplateJsonBean.VideoParamsBean videoParamsBean : pushStreamTemplateJsonBean.getVideoParams()) {
                i++;
                bitrateMapIcon.put(i, getQualityIcon(videoParamsBean.getQualityLevel()));
            }
        }
    }

    private int getQualityIcon(String qualityLevel) {
        if (PLVLinkMicConstant.QualityLevel.isHSD(qualityLevel)) {
            return R.drawable.plvsa_bitrate_icon_hd;
        } else if (PLVLinkMicConstant.QualityLevel.isSHD(qualityLevel)) {
            return R.drawable.plvsa_bitrate_icon_fhd;
        } else if (PLVLinkMicConstant.QualityLevel.isFHD(qualityLevel)) {
            return R.drawable.plvsa_bitrate_icon_uhd;
        } else {
            return R.drawable.plvsa_bitrate_icon_sd;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSASettingLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        this.channelId = liveRoomDataManager.getConfig().getChannelId();

        this.liveTitle = liveRoomDataManager.getConfig().getChannelName();
        plvsaSettingLiveTitleTv.setText(liveTitle);
        titleInputLayout.initTitle(liveTitle);
        bitrateLayout.init(liveRoomDataManager);
        mixLayout.init(liveRoomDataManager);
        initButtonVisibility(liveRoomDataManager);
        initBitrateMapIcon();
        initStartLiveBtnText();

        updatePushResolutionRatioOnOrientationChanged(PLVScreenUtils.isLandscape(getContext()));
        initOrientation(liveRoomDataManager);
        observeLiveRoomData();
        observeScrollContainer();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;

        if (onViewActionListener != null) {
            Pair<Integer, Integer> bitrateInfo = onViewActionListener.getBitrateInfo();
            if (bitrateInfo != null) {
                plvsaSettingBitrateTv.setText(PLVStreamerConfig.QualityLevel.getTextCombineTemplate(bitrateInfo.second, channelId));
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
                .setRightButtonText(R.string.plv_common_dialog_confirm_2)
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
        return bitrateLayout.onBackPressed() || mixLayout.onBackPressed();
    }

    @Override
    public void liveStart() {
        if (onViewActionListener != null) {
            PLVLinkMicConstant.NetworkQuality currentNetworkQuality = onViewActionListener.getCurrentNetworkQuality();
            if (currentNetworkQuality == PLVLinkMicConstant.NetworkQuality.DISCONNECT) {
                //如果断网，则不直播，显示弹窗。
                showAlertDialogNoNetwork();
                return;
            }
        }
        if (liveTitle.length() == 0) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_live_title_non_null)
                    .build()
                    .show();
            return;
        }
        //标记默认的布局方向
        PLVLiveLocalActionHelper.getInstance().updateOrientation(PLVScreenUtils.isPortrait(getContext()));
        //检查权限并开始直播
        checkPermissionToStartCountDown();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PLVImagePickerUtil.handleActivityResult(getContext(), requestCode, resultCode, data, new PLVImagePickerUtil.ImagePickerCallback() {

            @Override
            public void onImagesSelected(ArrayList<String> imagePaths) {
                for (String imagePath : imagePaths) {
                    waterLayout.addImage(imagePath);
                }
            }
        });
        PLVImageSelectorUtil.handleActivityResult(getContext(), requestCode, resultCode, data, new PLVImageSelectorUtil.ImagePickerCallback() {

            @Override
            public void onImagesSelected(ArrayList<String> imagePaths) {
                for (String imagePath : imagePaths) {
                    if (virtualBackgroundLayout != null) {
                        virtualBackgroundLayout.addImage(imagePath);
                    }
                }
            }
        });
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
                    String toastText = PLVAppUtils.formatString(R.string.plv_player_change_definition_2, PLVStreamerConfig.QualityLevel.getTextCombineTemplate(bitrate, channelId));
                    PLVToast.Builder.context(getContext())
                            .setText(toastText)
                            .build().show();
                }
            });
        }
    }

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        private boolean isShare;
        private boolean hasLinkMicUser;

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            initVirtualBgLayout();
        }

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
        public void onCameraDirection(boolean front, int pos, String uid) {
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

        @Override
        public void onScreenShareChange(int position, boolean isShare, int extra, String userId, boolean isMyself) {
            this.isShare = isShare;
            boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                    .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
            if (waterLayout != null) {
                waterLayout.setVisibility((showWatermarkButton && !isShare && !hasLinkMicUser) ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onHasLinkMicUser(boolean hasHasLinkMicUser) {
            this.hasLinkMicUser = hasHasLinkMicUser;
            boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                    .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
            if (waterLayout != null) {
                waterLayout.setVisibility((showWatermarkButton && !isShare && !hasHasLinkMicUser) ? View.VISIBLE : View.GONE);
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
                                waterLayout.setAspectRatio(16.0f / 9.0f);
                                break;
                            case RATIO_4_3:
                                settingPushResolutionRatioIv.setImageResource(R.drawable.plvsa_live_room_setting_ratio_4_3);
                                waterLayout.setAspectRatio(4.0f / 3.0f);
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
        final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        updatePushResolutionRatioOnOrientationChanged(isLandscape);
        if (virtualBackgroundLayout != null) {
            virtualBackgroundLayout.onOrientationChanged(!isLandscape);
        }
    }

    private void updatePushResolutionRatioOnOrientationChanged(boolean isLandscape) {
        if (liveRoomDataManager == null) {
            return;
        }
        final boolean userAllowChangeRatio = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RATIO);
        final boolean channelAllowChangeRatio = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_ALONE_ALLOW_CHANGE_PUSH_RESOLUTION_RATIO);
        if (!userAllowChangeRatio || !channelAllowChangeRatio) {
            return;
        }

        if (!isLandscape) {
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
    @SuppressLint("CheckResult")
    @Override
    public void onClick(View v) {
        if (!PLVDebounceClicker.tryClick(this)) {
            return;
        }
        settingMorePopupLayout.dismiss();
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
        } else if (id == settingMixLayout.getId()) {
            mixLayout.open();
        } else if (id == settingScreenOrientationLayout.getId()) {
            changeScreenOrientation();
        } else if (id == plvsaSettingBeautyLayout.getId()) {
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == settingPushResolutionRatioLl.getId()) {
            changePushResolutionRatio();
        } else if (id == settingDenoiseLayout.getId()) {
            denoisePreferenceLayout.open();
        } else if (id == settingExternalAudioInputLayout.getId()) {
            externalAudioInputPreferenceLayout.open();
        } else if (id == settingLiveReplaySwitchLayout.getId()) {
            settingLiveReplaySwitchLayout.setSelected(!settingLiveReplaySwitchLayout.isSelected());
            settingLiveReplaySwitchLayout.setEnabled(false);
            PLVChatApiRequestHelper.getInstance().updatePlaybackSetting(liveRoomDataManager.getConfig().getChannelId(), !settingLiveReplaySwitchLayout.isSelected())
                    .subscribe(new Consumer<ResponseBody>() {
                        @Override
                        public void accept(ResponseBody responseBody) throws Exception {
                            settingLiveReplaySwitchLayout.setEnabled(true);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            settingLiveReplaySwitchLayout.setEnabled(true);
                            PLVCommonLog.exception(throwable);
                        }
                    });
        } else if (id == settingMoreLayout.getId()) {
            settingMorePopupLayout.show();
        } else if (id == settingWaterLayout.getId()) {
            stickerLayout.open();
        } else if (id == settingVirtualBgLayout.getId()) {
            if (virtualBackgroundLayout != null) {
                virtualBackgroundLayout.show();
            }
        }
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="数据监听">
    private void observeLiveRoomData() {
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (statefulData != null && statefulData.getData() != null && statefulData.getData().getData() != null) {
                    boolean playbackEnabled = statefulData.getData().getData().isPlaybackEnabled();
                    settingLiveReplaySwitchLayout.setSelected(!playbackEnabled);
                }
            }
        });
    }
    // </editor-folder>

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
                        confirmDialog.setTitle(R.string.plv_linkmic_camera_permission_apply)
                                .setContent(R.string.plv_linkmic_camera_permission_apply_hint)
                                .setLeftButtonText(R.string.plv_common_dialog_cancel)
                                .setLeftBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        confirmDialog.hide();
                                    }
                                })
                                .setRightButtonText(R.string.plv_common_dialog_setting)
                                .setRightBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PLVFastPermission.getInstance().jump2Settings(getContext());
                                    }
                                });
                        break;
                    }
                    if (Manifest.permission.RECORD_AUDIO.equals(deniedPermission)) {
                        confirmDialog.setTitle(R.string.plv_linkmic_microphone_permission_apply)
                                .setContent(R.string.plv_linkmic_microphone_permission_apply_hint)
                                .setLeftButtonText(R.string.plv_common_dialog_cancel)
                                .setLeftBtnListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        confirmDialog.hide();
                                    }
                                })
                                .setRightButtonText(R.string.plv_common_dialog_setting)
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
        Integer iconId = bitrateMapIcon.get(selectedBitrate);
        if (iconId != null) {
            plvsaSettingBitrateIv.setImageResource(iconId);
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
            settingLayout.setVisibility(View.GONE);
            return;
        }
        // 设置结束后隐藏
        if (isSettingFinished) {
            settingLayout.setVisibility(View.GONE);
            return;
        }
        settingLayout.setVisibility(View.VISIBLE);
    }

    // </editor-fold>
}
