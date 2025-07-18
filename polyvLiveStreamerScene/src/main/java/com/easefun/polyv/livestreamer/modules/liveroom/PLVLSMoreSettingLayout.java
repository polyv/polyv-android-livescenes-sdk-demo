package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;
import static com.plv.foundationsdk.utils.PLVSugarUtil.foreach;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVImageSelectorUtil;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVVirtualBackgroundLayout;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.image.segmenter.api.IPLVImageSegmenterManager;
import com.plv.image.segmenter.api.PLVImageSegmenterManager;
import com.plv.image.segmenter.api.enums.PLVImageSegmenterInitCode;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import kotlin.jvm.functions.Function1;

/**
 * 设置布局
 */
public class PLVLSMoreSettingLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLSMoreSettingLayout.class.getSimpleName();
    private PLVBlurView blurLy;
    private ConstraintLayout moreSettingSelectLayout;
    private TextView moreSettingTitleTv;
    private View moreSettingTitleSeparator;
    private LinearLayout moreSettingBeautyItemLayout;
    private LinearLayout moreSettingBitrateItemLayout;
    private LinearLayout moreSettingMixItemLayout;
    private LinearLayout moreSettingShareItemLayout;
    private LinearLayout morePushDowngradeItemLayout;
    private LinearLayout moreInteractSigninLl;
    private LinearLayout moreVirtualBgItemLayout;
    private LinearLayout moreDenoiseItemLayout;
    private LinearLayout moreExternalAudioInputItemLayout;
    private PLVLSBitrateLayout moreSettingBitrateLayout;
    private PLVLSMixLayout moreSettingMixLayout;
    private PLVLSPushDowngradePreferenceLayout morePushDowngradePreferenceLayout;
    // 降噪配置布局
    private PLVLSDenoisePreferenceLayout moreDenoisePreferenceLayout;
    // 外接设备布局
    private PLVLSExternalAudioInputPreferenceLayout moreExternalAudioInputPreferenceLayout;
    private View moreSettingExitSeparator;
    private TextView moreSettingExitTv;
    private Group moreSettingExitGroup;
    private TextView moreSettingsInteractTv;
    private GridLayout moreSettingsInteractLayout;

    // 分享布局
    private PLVLSShareLayout shareLayout;

    // 布局弹层
    private PLVMenuDrawer menuDrawer;
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;

    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    private PLVVirtualBackgroundLayout virtualBackgroundLayout;

    private OnViewActionListener onViewActionListener;
    private IPLVImageSegmenterManager.InitCallback imageSegmenterInitCallback = null;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private Disposable updateBlurViewDisposable;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSMoreSettingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSMoreSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSMoreSettingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_more_setting_layout, this);

        findView();

        initBitrateLayout();
        initMixLayout();
        initPushDowngradeLayout();
        initShareLayout();
        initVirtualBackgroundItemLayout();
        initDenoiseLayout();
        initExternalAudioInputLayout();
        observeBeautyModuleInitResult();

        PLVBlurUtils.initBlurView(blurLy);
    }

    private void findView() {
        blurLy = findViewById(R.id.blur_ly);
        moreSettingSelectLayout = findViewById(R.id.plvls_more_setting_select_layout);
        moreSettingTitleTv = findViewById(R.id.plvls_more_setting_title_tv);
        moreSettingTitleSeparator = findViewById(R.id.plvls_more_setting_title_separator);
        moreSettingBeautyItemLayout = findViewById(R.id.plvls_more_setting_beauty_item_layout);
        moreSettingBitrateItemLayout = findViewById(R.id.plvls_more_setting_bitrate_item_layout);
        moreSettingBitrateLayout = findViewById(R.id.plvls_more_setting_bitrate_layout);
        moreSettingMixItemLayout = findViewById(R.id.plvls_more_setting_mix_item_layout);
        moreSettingMixLayout = findViewById(R.id.plvls_more_setting_mix_layout);
        morePushDowngradeItemLayout = findViewById(R.id.plvls_more_push_downgrade_item_layout);
        morePushDowngradePreferenceLayout = findViewById(R.id.plvls_more_push_downgrade_preference_layout);
        moreSettingShareItemLayout = findViewById(R.id.plvls_more_setting_share_item_layout);
        moreSettingExitSeparator = findViewById(R.id.plvls_more_setting_exit_separator);
        moreSettingExitTv = findViewById(R.id.plvls_more_setting_exit_tv);
        moreSettingExitGroup = findViewById(R.id.plvls_more_setting_exit_group);
        moreInteractSigninLl = findViewById(R.id.plvls_more_interact_signin_layout);
        moreVirtualBgItemLayout = findViewById(R.id.plvls_more_setting_virtual_bg_layout);
        moreDenoiseItemLayout = findViewById(R.id.plvls_more_setting_denoise_layout);
        moreDenoisePreferenceLayout = findViewById(R.id.plvls_more_setting_denoise_preference_layout);
        moreExternalAudioInputItemLayout = findViewById(R.id.plvls_more_setting_external_audio_input_layout);
        moreExternalAudioInputPreferenceLayout = findViewById(R.id.plvls_more_setting_external_audio_input_preference_layout);
        moreSettingsInteractTv = findViewById(R.id.plvls_more_settings_interact_tv);
        moreSettingsInteractLayout = findViewById(R.id.plvls_more_settings_interact_layout);

        moreSettingExitTv.setOnClickListener(this);
        moreSettingBeautyItemLayout.setOnClickListener(this);
        moreSettingBitrateItemLayout.setOnClickListener(this);
        moreSettingMixItemLayout.setOnClickListener(this);
        morePushDowngradeItemLayout.setOnClickListener(this);
        moreSettingShareItemLayout.setOnClickListener(this);
        moreInteractSigninLl.setOnClickListener(this);
        moreVirtualBgItemLayout.setOnClickListener(this);
        moreDenoiseItemLayout.setOnClickListener(this);
        moreExternalAudioInputItemLayout.setOnClickListener(this);
    }

    private void initBitrateLayout() {
        moreSettingBitrateLayout.setOnViewActionListener(new PLVLSBitrateLayout.OnViewActionListener() {
            @Override
            public void onBitrateClick(int bitrate) {
                if (PLVLSMoreSettingLayout.this.onViewActionListener != null) {
                    PLVLSMoreSettingLayout.this.onViewActionListener.onBitrateClick(bitrate);
                }
            }
        });
    }

    private void initMixLayout() {
        if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_MIX_LAYOUT)) {
            moreSettingMixItemLayout.setVisibility(View.VISIBLE);
        } else {
            moreSettingMixItemLayout.setVisibility(View.GONE);
        }
        moreSettingMixLayout.setOnViewActionListener(new PLVLSMixLayout.OnViewActionListener() {
            @Override
            public void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix) {
                if (PLVLSMoreSettingLayout.this.onViewActionListener != null) {
                    PLVLSMoreSettingLayout.this.onViewActionListener.onChangeMixLayoutType(mix);
                }
            }
        });
    }

    private void initPushDowngradeLayout() {
        morePushDowngradePreferenceLayout.setOnViewActionListener(new PLVLSPushDowngradePreferenceLayout.OnViewActionListener() {
            @Nullable
            @Override
            public PLVPushDowngradePreference getCurrentDowngradePreference() {
                if (onViewActionListener != null) {
                    return onViewActionListener.getCurrentDowngradePreference();
                }
                return null;
            }

            @Override
            public void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference) {
                if (onViewActionListener != null) {
                    onViewActionListener.onDowngradePreferenceChanged(preference);
                }
            }
        });
    }

    private void initShareLayout() {
        shareLayout = new PLVLSShareLayout(getContext());
    }

    private void observeBeautyModuleInitResult() {
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe((LifecycleOwner) getContext(), new Observer<PLVBeautyUiState>() {
                    private Boolean lastShowBeautyLayout = null;

                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        if (beautyUiState == null) {
                            return;
                        }
                        final boolean isBeautySupport = beautyUiState.isBeautySupport;
                        final boolean isInitSuccess = beautyUiState.isBeautyModuleInitSuccess;
                        final boolean showBeautyLayout = isBeautySupport && isInitSuccess;
                        if (lastShowBeautyLayout != null && lastShowBeautyLayout == showBeautyLayout) {
                            return;
                        }
                        lastShowBeautyLayout = showBeautyLayout;

                        moreSettingBeautyItemLayout.setVisibility(showBeautyLayout ? View.VISIBLE : View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) moreSettingBeautyItemLayout.getLayoutParams();
                            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, showBeautyLayout ? 1 : 0, 1F);
                            moreSettingBeautyItemLayout.setLayoutParams(lp);
                        }
                    }
                });
    }

    private void observeLiveRoomStatus(){
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                    liveRoomDataManager.getClassDetailVO().removeObserver(this);
                    if(polyvLiveClassDetailVOPLVStatefulData.getData() != null){
                        boolean isOpen = polyvLiveClassDetailVOPLVStatefulData.getData().isOpenPushShare();
                        moreSettingShareItemLayout.setVisibility(isOpen == true ? VISIBLE : INVISIBLE);
                    }
                }
            });
        }
    }

    private void updateActionVisibility() {
        final PLVChannelFeatureManager channelFeatures = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId());
        final boolean showMixLayout = channelFeatures.isFeatureSupport(PLVChannelFeature.STREAMER_SETTING_SHOW_MIX_LAYOUT_BUTTON);
        final boolean showSignInLayout = channelFeatures.isFeatureSupport(PLVChannelFeature.STREAMER_SHOW_FUNCTION_INTERACT_SIGN_IN);
        if (!showMixLayout) {
            PLVViewUtil.setGridLayoutItemVisible(moreSettingMixItemLayout, false);
        }
        if (!showSignInLayout) {
            PLVViewUtil.setGridLayoutItemVisible(moreInteractSigninLl, false);
        }
        updateInteractMenuVisibility();
    }

    private void updateInteractMenuVisibility() {
        final boolean show = PLVSequenceWrapper.wrap(children(moreSettingsInteractLayout))
                .filter(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        return !(view instanceof Space);
                    }
                })
                .any(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        return view.getVisibility() == View.VISIBLE;
                    }
                });
        moreSettingsInteractTv.setVisibility(show ? View.VISIBLE : View.GONE);
        moreSettingsInteractLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initVirtualBackgroundLayout() {
        virtualBackgroundLayout = new PLVVirtualBackgroundLayout(this, new PLVVirtualBackgroundLayout.OnViewActionListener() {
            @Override
            public void onConfirmDeleteBg(final int position) {
                PLVLSConfirmDialog.Builder.context(getContext())
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
        virtualBackgroundLayout.setUseBlackStyle();
    }

    private void initVirtualBackgroundItemLayout() {
        initVirtualBackgroundItemLayout(false);
        // 监听图片分割初始化回调
        PLVImageSegmenterManager.getInstance().addInitCallback(new WeakReference<>(imageSegmenterInitCallback = new IPLVImageSegmenterManager.InitCallback() {
            @Override
            public void onFinishInit(PLVImageSegmenterInitCode code) {
                PLVCommonLog.i(TAG, "onImageSegmenterFinishInit, code: " + code);
                boolean isVisible = code == PLVImageSegmenterInitCode.SUCCESS;
                initVirtualBackgroundItemLayout(isVisible);
            }
        }));
    }

    private void initVirtualBackgroundItemLayout(boolean isVisible) {
        moreVirtualBgItemLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final GridLayout.LayoutParams lp = (GridLayout.LayoutParams) moreVirtualBgItemLayout.getLayoutParams();
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, isVisible ? 1 : 0, 1F);
            moreVirtualBgItemLayout.setLayoutParams(lp);
        }
    }

    private void initDenoiseLayout() {
        moreDenoisePreferenceLayout.setOnViewActionListener(new PLVLSDenoisePreferenceLayout.OnViewActionListener() {
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
        moreExternalAudioInputPreferenceLayout.setOnViewActionListener(new PLVLSExternalAudioInputPreferenceLayout.OnViewActionListener() {
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
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        if (shareLayout != null) {
            shareLayout.init(liveRoomDataManager);
        }
        if (moreSettingBitrateLayout != null) {
            moreSettingBitrateLayout.init(liveRoomDataManager);
        }
        if (moreSettingMixLayout != null) {
            moreSettingMixLayout.init(liveRoomDataManager);
        }
        observeLiveRoomStatus();
        updateActionVisibility();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="布局控制">
    public void open() {
        if (onViewActionListener != null && onViewActionListener.getBitrateInfo() != null) {
            moreSettingBitrateLayout.updateData(onViewActionListener.getBitrateInfo().first, onViewActionListener.getBitrateInfo().second);
        }
        if (onViewActionListener != null) {
            moreSettingMixLayout.updateData(onViewActionListener.getMixLayoutType());
        }
        showLayout(moreSettingSelectLayout);

        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
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
            menuDrawer.setMenuSize((int) (landscapeWidth * 0.44));
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public boolean onBackPressed() {
        if (shareLayout != null && shareLayout.onBackPressed()) {
            return true;
        }
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
        if (shareLayout != null) {
            shareLayout.destroy();
        }
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="streamerView">
    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            streamerPresenter = presenter;
        }

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            initVirtualBackgroundLayout();
        }
    };
    // </editor-folder>

    private void showLayout(final View viewToShow) {
        final List<View> views = PLVSugarUtil.<View>listOf(
                moreSettingSelectLayout,
                moreSettingBitrateLayout,
                moreSettingMixLayout,
                morePushDowngradePreferenceLayout,
                moreDenoisePreferenceLayout,
                moreExternalAudioInputPreferenceLayout
        );
        foreach(views, new PLVSugarUtil.Consumer<View>() {
            @Override
            public void accept(View view) {
                view.setVisibility(view == viewToShow ? VISIBLE : GONE);
            }
        });
        moreSettingExitGroup.setVisibility(viewToShow == moreSettingSelectLayout ? VISIBLE : GONE);
    }

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurLy.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == moreSettingExitTv.getId()) {
            close();
            ((Activity) getContext()).onBackPressed();
        } else if (id == moreSettingBeautyItemLayout.getId()) {
            close();
            final boolean isLocalVideoEnable = onViewActionListener != null && onViewActionListener.isCurrentLocalVideoEnable();
            if (!isLocalVideoEnable) {
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_beauty_need_open_camera))
                        .show();
                return;
            }
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == moreSettingBitrateItemLayout.getId()) {
            showLayout(moreSettingBitrateLayout);
        } else if (id == moreSettingMixItemLayout.getId()) {
            showLayout(moreSettingMixLayout);
        } else if (id == morePushDowngradeItemLayout.getId()) {
            showLayout(morePushDowngradePreferenceLayout);
        } else if (id == moreSettingShareItemLayout.getId()) {
            close();
            shareLayout.open();
        } else if (id == moreInteractSigninLl.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onShowSignInAction();
            }
        } else if (id == moreVirtualBgItemLayout.getId()) {
            if (virtualBackgroundLayout != null) {
                close();
                virtualBackgroundLayout.show();
            }
        } else if (id == moreDenoiseItemLayout.getId()) {
            showLayout(moreDenoisePreferenceLayout);
        } else if (id == moreExternalAudioInputItemLayout.getId()) {
            showLayout(moreExternalAudioInputPreferenceLayout);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        Pair<Integer, Integer> getBitrateInfo();

        void onBitrateClick(int bitrate);

        PLVStreamerConfig.MixLayoutType getMixLayoutType();

        void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix);

        boolean isCurrentLocalVideoEnable();

        @Nullable
        PLVPushDowngradePreference getCurrentDowngradePreference();

        void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference);

        /**
         * 显示签到
         */
        void onShowSignInAction();
    }
    // </editor-fold>
}
