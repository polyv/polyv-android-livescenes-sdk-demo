package com.easefun.polyv.streameralone.modules.liveroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.children;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.virtualbg.PLVVirtualBackgroundLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.chatroom.IPolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.google.android.flexbox.FlexboxLayout;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.proxy.PLVDynamicProxy;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.image.segmenter.api.IPLVImageSegmenterManager;
import com.plv.image.segmenter.api.PLVImageSegmenterManager;
import com.plv.image.segmenter.api.enums.PLVImageSegmenterInitCode;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.linkmic.model.PLVPushStreamTemplateJsonBean;
import com.plv.linkmic.screenshare.vo.PLVCustomScreenShareData;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.chatroom.IPLVChatroomManager;
import com.plv.livescenes.feature.pointreward.IPLVPointRewardDataSource;
import com.plv.livescenes.feature.pointreward.PLVRewardDataSource;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.pointreward.PLVRewardSettingVO;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.Ack;
import kotlin.jvm.functions.Function1;
import okhttp3.ResponseBody;

/**
 * 更多布局
 */
public class PLVSAMoreLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVSAMoreLayout.class.getSimpleName();

    // 按钮表格每行显示数量
    private static final int GRID_COLUMN_COUNT_PORT = 5;
    private static final int GRID_COLUMN_COUNT_LAND = 3;
    // 弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 弹层布局尺寸 竖屏高度 横屏宽度
    private static final int MENU_DRAWER_SIZE_PORT = ConvertUtils.dp2px(270);
    private static final int MENU_DRAWER_SIZE_LAND = ConvertUtils.dp2px(240);
    // 更多布局高度
    private static final int MORE_LAYOUT_HEIGHT_PORT = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int MORE_LAYOUT_HEIGHT_LAND = ViewGroup.LayoutParams.MATCH_PARENT;
    // 更多布局位置
    private static final int MORE_LAYOUT_GRAVITY_PORT = Gravity.BOTTOM;
    private static final int MORE_LAYOUT_GRAVITY_LAND = Gravity.END;
    // 更多布局背景
    private static final int MORE_LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int MORE_LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_more_ly_shape_land;

    //view
    private ConstraintLayout plvsaMoreLayout;
    private ViewGroup plvsaMoreSettingsSv;
    private FlexboxLayout plvsaMoreSettingsLayout;
    private ImageView plvsaMoreCameraIv;
    private TextView plvsaMoreCameraTv;
    private ImageView plvsaMoreMicIv;
    private TextView plvsaMoreMicTv;
    private ImageView plvsaMoreCameraSwitchIv;
    private TextView plvsaMoreCameraSwitchTv;
    private ImageView plvsaMoreMirrorIv;
    private TextView plvsaMoreMirrorTv;
    private ImageView plvsaMoreFlashlightIv;
    private TextView plvsaMoreFlashlightTv;
    private ImageView plvsaMoreBitrateIv;
    private TextView plvsaMoreBitrateTv;
    private ImageView plvsaMoreMixIv;
    private TextView plvsaMoreMixTv;
    private View plvsaMoreMixLayout;
    private ImageView plvsaMoreCloseRoomIv;
    private TextView plvsaMoreCloseRoomTv;
    private View plvsaMoreCloseRoomLayout;
    private View plvsaMoreShareScreenLl;
    private ImageView plvsaMoreShareScreenIv;
    private TextView plvsaMoreShareScreenTv;
    private LinearLayout moreShareScreenFloatMessageLl;
    private ImageView moreShareScreenFloatMessageIv;
    private TextView moreShareScreenFloatMessageTv;
    private LinearLayout moreBeautyLl;
    private LinearLayout moreShareLl;
    private LinearLayout morePushDowngradePreferenceLl;
    private LinearLayout moreAllowViewerLinkmicLayout;
    private ImageView moreAllowViewerLinkmicIv;
    private TextView moreAllowViewerLinkmicTv;
    private LinearLayout moreLinkmicSettingLayout;
    private ImageView moreLinkmicSettingIv;
    private TextView moreLinkmicSettingTv;
    private LinearLayout moreHangUpViewerLinkmicLayout;
    private ImageView moreHangUpViewerLinkmicIv;
    private TextView moreHangUpViewerLinkmicTv;
    private LinearLayout moreInteractSigninLl;
    private LinearLayout moreGiftRewardLayout;
    private ImageView moreGiftRewardIv;
    private TextView moreGiftRewardTv;
    private LinearLayout moreGiftEffectLayout;
    private ImageView moreGiftEffectIv;
    private TextView moreGiftEffectTv;
    private ViewGroup moreWaterLayout;
    private ViewGroup moreVirtualBgLayout;
    private TextView moreSettingsInteractTv;
    private FlexboxLayout moreSettingsInteractLayout;

    //streamerPresenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //清晰度设置布局
    private PLVSABitrateLayout bitrateLayout;
    //混流设置布局
    private PLVSAMixLayout mixLayout;
    //分享布局
    private PLVSAShareLayout shareLayout;
    // 推流降级布局
    private final PLVSAPushDowngradePreferenceLayout pushDowngradePreferenceLayout = new PLVSAPushDowngradePreferenceLayout(getContext());
    // 连麦设置布局
    private final PLVSAMoreLinkMicSettingLayout linkMicSettingLayout = new PLVSAMoreLinkMicSettingLayout(getContext());
    // 屏幕分享桌面消息
    private final PLVSAMoreScreenShareFloatMessageLayout screenShareFloatMessageLayout = new PLVSAMoreScreenShareFloatMessageLayout(getContext());

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    private IPLVChatroomManager.RoomStatusListener roomStatusListener;

    // 标记位 当用户手动切换清晰度时为true 避免进入设置页时内部初始化清晰度弹出toast提示
    private boolean switchBitrateByUser = false;
    // 标记位
    private boolean attachedToWindow = false;

    private boolean isEnableVideo = true;

    private long lastClickCameraSwitchViewTime;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    private PLVRewardDataSource rewardDataSource = new PLVRewardDataSource();
    private String channelId;

    private IPLVImageSegmenterManager.InitCallback imageSegmenterInitCallback = null;

    /**
     * 限制每800ms点击一次
     **/
    private static final long QUICK_CLICK_LIMIT_TIME = 800;
    private long lastClickTime = 0;

    private Map<Integer, Integer> bitrateMapIcon = new HashMap<Integer, Integer>() {{
        put(PLVStreamerConfig.Bitrate.BITRATE_STANDARD, R.drawable.plvsa_bitrate_icon_sd);
        put(PLVStreamerConfig.Bitrate.BITRATE_HIGH, R.drawable.plvsa_bitrate_icon_hd);
        put(PLVStreamerConfig.Bitrate.BITRATE_SUPER, R.drawable.plvsa_bitrate_icon_fhd);
        put(PLVStreamerConfig.Bitrate.BITRATE_SUPER_HIGH, R.drawable.plvsa_bitrate_icon_uhd);
    }};
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMoreLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAMoreLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_more_layout, this);

        plvsaMoreLayout = (ConstraintLayout) findViewById(R.id.plvsa_more_layout);
        plvsaMoreSettingsSv = findViewById(R.id.plvsa_more_settings_sv);
        plvsaMoreSettingsLayout = (FlexboxLayout) findViewById(R.id.plvsa_more_settings_layout);
        plvsaMoreCameraIv = (ImageView) findViewById(R.id.plvsa_more_camera_iv);
        plvsaMoreCameraTv = (TextView) findViewById(R.id.plvsa_more_camera_tv);
        plvsaMoreMicIv = (ImageView) findViewById(R.id.plvsa_more_mic_iv);
        plvsaMoreMicTv = (TextView) findViewById(R.id.plvsa_more_mic_tv);
        plvsaMoreCameraSwitchIv = (ImageView) findViewById(R.id.plvsa_more_camera_switch_iv);
        plvsaMoreCameraSwitchTv = (TextView) findViewById(R.id.plvsa_more_camera_switch_tv);
        plvsaMoreMirrorIv = (ImageView) findViewById(R.id.plvsa_more_mirror_iv);
        plvsaMoreMirrorTv = (TextView) findViewById(R.id.plvsa_more_mirror_tv);
        plvsaMoreFlashlightIv = (ImageView) findViewById(R.id.plvsa_more_flashlight_iv);
        plvsaMoreFlashlightTv = (TextView) findViewById(R.id.plvsa_more_flashlight_tv);
        plvsaMoreBitrateIv = (ImageView) findViewById(R.id.plvsa_more_bitrate_iv);
        plvsaMoreBitrateTv = (TextView) findViewById(R.id.plvsa_more_bitrate_tv);
        plvsaMoreMixIv = findViewById(R.id.plvsa_more_mix_iv);
        plvsaMoreMixTv = findViewById(R.id.plvsa_more_mix_tv);
        plvsaMoreMixLayout = findViewById(R.id.plvsa_more_mix_layout);
        plvsaMoreCloseRoomIv = (ImageView) findViewById(R.id.plvsa_more_close_room_iv);
        plvsaMoreCloseRoomTv = (TextView) findViewById(R.id.plvsa_more_close_room_tv);
        plvsaMoreCloseRoomLayout = findViewById(R.id.plvsa_more_close_room_layout);
        plvsaMoreShareScreenLl = findViewById(R.id.plvsa_more_share_screen_ll);
        plvsaMoreShareScreenIv = findViewById(R.id.plvsa_more_share_screen_iv);
        plvsaMoreShareScreenTv = findViewById(R.id.plvsa_more_share_screen_tv);
        moreShareScreenFloatMessageLl = findViewById(R.id.plvsa_more_share_screen_float_message_ll);
        moreShareScreenFloatMessageIv = findViewById(R.id.plvsa_more_share_screen_float_message_iv);
        moreShareScreenFloatMessageTv = findViewById(R.id.plvsa_more_share_screen_float_message_tv);
        moreBeautyLl = findViewById(R.id.plvsa_more_beauty_ll);
        moreShareLl = findViewById(R.id.plvsa_more_share_layout);
        morePushDowngradePreferenceLl = findViewById(R.id.plvsa_more_push_downgrade_preference_ll);
        moreAllowViewerLinkmicLayout = findViewById(R.id.plvsa_more_allow_viewer_linkmic_layout);
        moreAllowViewerLinkmicIv = findViewById(R.id.plvsa_more_allow_viewer_linkmic_iv);
        moreAllowViewerLinkmicTv = findViewById(R.id.plvsa_more_allow_viewer_linkmic_tv);
        moreLinkmicSettingLayout = findViewById(R.id.plvsa_more_linkmic_setting_layout);
        moreLinkmicSettingIv = findViewById(R.id.plvsa_more_linkmic_setting_iv);
        moreLinkmicSettingTv = findViewById(R.id.plvsa_more_linkmic_setting_tv);
        moreHangUpViewerLinkmicLayout = findViewById(R.id.plvsa_more_hang_up_viewer_linkmic_layout);
        moreHangUpViewerLinkmicIv = findViewById(R.id.plvsa_more_hang_up_viewer_linkmic_iv);
        moreHangUpViewerLinkmicTv = findViewById(R.id.plvsa_more_hang_up_viewer_linkmic_tv);
        moreInteractSigninLl = findViewById(R.id.plvsa_more_interact_signin_layout);
        moreGiftRewardLayout = findViewById(R.id.plvsa_more_gift_reward_layout);
        moreGiftRewardIv = findViewById(R.id.plvsa_more_gift_reward_iv);
        moreGiftRewardTv = findViewById(R.id.plvsa_more_gift_reward_tv);
        moreGiftEffectLayout = findViewById(R.id.plvsa_more_gift_effect_layout);
        moreGiftEffectIv = findViewById(R.id.plvsa_more_gift_effect_iv);
        moreGiftEffectTv = findViewById(R.id.plvsa_more_gift_effect_tv);
        moreWaterLayout = findViewById(R.id.plvsa_setting_live_water_layout);
        moreVirtualBgLayout = findViewById(R.id.plvsa_setting_virtual_bg_layout);
        moreSettingsInteractTv = findViewById(R.id.plvsa_more_settings_interact_tv);
        moreSettingsInteractLayout = findViewById(R.id.plvsa_more_settings_interact_layout);

        plvsaMoreCameraIv.setOnClickListener(this);
        plvsaMoreCameraTv.setOnClickListener(this);
        plvsaMoreMicIv.setOnClickListener(this);
        plvsaMoreMicTv.setOnClickListener(this);
        plvsaMoreCameraSwitchIv.setOnClickListener(this);
        plvsaMoreCameraSwitchTv.setOnClickListener(this);
        plvsaMoreMirrorIv.setOnClickListener(this);
        plvsaMoreMirrorTv.setOnClickListener(this);
        plvsaMoreFlashlightIv.setOnClickListener(this);
        plvsaMoreFlashlightTv.setOnClickListener(this);
        plvsaMoreBitrateIv.setOnClickListener(this);
        plvsaMoreBitrateTv.setOnClickListener(this);
        plvsaMoreMixIv.setOnClickListener(this);
        plvsaMoreMixTv.setOnClickListener(this);
        plvsaMoreCloseRoomIv.setOnClickListener(this);
        plvsaMoreCloseRoomTv.setOnClickListener(this);
        plvsaMoreShareScreenLl.setOnClickListener(this);
        moreShareScreenFloatMessageLl.setOnClickListener(this);
        moreBeautyLl.setOnClickListener(this);
        moreShareLl.setOnClickListener(this);
        morePushDowngradePreferenceLl.setOnClickListener(this);
        moreAllowViewerLinkmicLayout.setOnClickListener(new PLVDebounceClicker.OnClickListener(this, 1000));
        moreLinkmicSettingLayout.setOnClickListener(this);
        moreHangUpViewerLinkmicLayout.setOnClickListener(this);
        moreInteractSigninLl.setOnClickListener(this);
        moreGiftRewardLayout.setOnClickListener(this);
        moreGiftEffectLayout.setOnClickListener(this);
        moreWaterLayout.setOnClickListener(this);
        moreVirtualBgLayout.setOnClickListener(this);

        plvsaMoreCloseRoomIv.setSelected(PolyvChatroomManager.getInstance().isCloseRoom());
        plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? R.string.plv_chat_cancel_close_room : R.string.plv_chat_confirm_close_room);

        if (!PLVLinkMicConfig.getInstance().isSupportScreenShare()){
            plvsaMoreShareScreenLl.setVisibility(View.GONE);
            moreShareScreenFloatMessageLl.setVisibility(View.GONE);
        }
        if (!PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_GIFT_REWARD_OPEN)) {
            moreGiftRewardLayout.setVisibility(View.GONE);
        }

        //init bitrateLayout
        bitrateLayout = new PLVSABitrateLayout(getContext());
        bitrateLayout.setOnViewActionListener(new PLVSABitrateLayout.OnViewActionListener() {
            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return streamerPresenter != null ? new Pair<>(streamerPresenter.getMaxBitrate(), streamerPresenter.getBitrate()) : null;
            }

            @Override
            public void onBitrateClick(int bitrate) {
                bitrateLayout.close();
                switchBitrateByUser = true;
                if (streamerPresenter != null) {
                    streamerPresenter.setBitrate(bitrate);
                }
            }
        });

        //init mixLayout
        mixLayout = new PLVSAMixLayout(getContext());
        mixLayout.setOnViewActionListener(new PLVSAMixLayout.OnViewActionListener() {
            @Override
            public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
                return streamerPresenter != null ? streamerPresenter.getMixLayoutType() : PLVStreamerConfig.MixLayoutType.TILE;
            }

            @Override
            public void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix) {
                mixLayout.close();
                if (streamerPresenter != null) {
                    streamerPresenter.setMixLayoutType(mix);
                }
            }
        });

        //init shareLayout
        shareLayout = new PLVSAShareLayout(getContext());

        //init virtualBgLayout
        PLVImageSegmenterManager.getInstance().addInitCallback(new WeakReference<>(imageSegmenterInitCallback = new IPLVImageSegmenterManager.InitCallback() {
            @Override
            public void onFinishInit(PLVImageSegmenterInitCode code) {
                PLVCommonLog.i(TAG, "onImageSegmenterFinishInit, code: " + code);
                if (code == PLVImageSegmenterInitCode.SUCCESS) {
                    moreVirtualBgLayout.setVisibility(View.VISIBLE);
                }
            }
        }));

        setupPushDowngradeLayout();
        observeBeautyModuleInitResult();
        observeChatroomStatus();
    }

    private void setupPushDowngradeLayout() {
        pushDowngradePreferenceLayout.setOnViewActionListener(new PLVSAPushDowngradePreferenceLayout.OnViewActionListener() {
            @Nullable
            @Override
            public PLVPushDowngradePreference getCurrentDowngradePreference() {
                return streamerPresenter.getPushDowngradePreference();
            }

            @Override
            public void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference) {
                if (streamerPresenter.getPushDowngradePreference() == preference) {
                    return;
                }
                streamerPresenter.setPushDowngradePreference(preference);
            }
        });
    }

    private void observeChatroomStatus() {
        PolyvChatroomManager.getInstance().addOnRoomStatusListener(roomStatusListener = new IPLVChatroomManager.RoomStatusListener() {
            @Override
            public void onStatus(boolean isClose) {
                plvsaMoreCloseRoomIv.setSelected(isClose);
                postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? R.string.plv_chat_cancel_close_room : R.string.plv_chat_confirm_close_room);
                    }
                });
            }
        });
    }

    private void observeLiveRoomStatus() {
        if (liveRoomDataManager != null) {
            liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                    liveRoomDataManager.getClassDetailVO().removeObserver(this);
                    if (polyvLiveClassDetailVOPLVStatefulData.getData() != null){
                        boolean isOpen = polyvLiveClassDetailVOPLVStatefulData.getData().isOpenPushShare();
                        moreShareLl.setVisibility(isOpen == true ? VISIBLE : INVISIBLE);
                    }
                }
            });
            liveRoomDataManager.getRewardSettingData().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PLVRewardSettingVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PLVRewardSettingVO> statefulData) {
                    liveRoomDataManager.getRewardSettingData().removeObserver(this);
                    if (statefulData == null || !statefulData.isSuccess() || statefulData.getData() == null) {
                        return;
                    }
                    boolean isOpen = statefulData.getData().getDonateGiftEnabled();
                    moreGiftRewardLayout.setSelected(isOpen ? false : true);
                }
            });
        }
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

                        moreBeautyLl.setVisibility(showBeautyLayout ? View.VISIBLE : View.GONE);
                    }
                });
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

    // <editor-folder defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
        if (shareLayout != null) {
            shareLayout.init(liveRoomDataManager);
        }
        if (bitrateLayout != null) {
            bitrateLayout.init(liveRoomDataManager);
        }
        if (mixLayout != null) {
            mixLayout.init(liveRoomDataManager);
        }
        boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
        if (moreWaterLayout != null) {
            moreWaterLayout.setVisibility(showWatermarkButton ? View.VISIBLE : View.GONE);
        }
        screenShareFloatMessageLayout.init(liveRoomDataManager);
        initBitrateMapIcon();
        observeLiveRoomStatus();
        updateLinkMicButton(liveRoomDataManager);
        updateInteractLayout(liveRoomDataManager);
        initMixLayoutButton();
    }

    private void updateLinkMicButton(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean canControlLinkMic = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN);
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        if (!canControlLinkMic || !isNewLinkMicStrategy) {
            moreAllowViewerLinkmicLayout.setVisibility(View.GONE);
            moreLinkmicSettingLayout.setVisibility(View.GONE);
            moreHangUpViewerLinkmicLayout.setVisibility(View.GONE);
        }
        updateInteractMenuVisibility();
    }

    private void updateInteractLayout(IPLVLiveRoomDataManager liveRoomDataManager) {
        final PLVChannelFeatureManager channelFeatures = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId());
        final boolean serverShowSignIn = channelFeatures.isFeatureSupport(PLVChannelFeature.STREAMER_SHOW_FUNCTION_INTERACT_SIGN_IN);
        final boolean serverShowReward = channelFeatures.isFeatureSupport(PLVChannelFeature.STREAMER_SHOW_FUNCTION_INTERACT_GIFT_REWARD);
        final boolean serverShowGiftEffect = channelFeatures.isFeatureSupport(PLVChannelFeature.STREAMER_SHOW_FUNCTION_INTERACT_GIFT_EFFECT);
        if (!serverShowSignIn) {
            moreInteractSigninLl.setVisibility(View.GONE);
        }
        if (!serverShowReward) {
            moreGiftRewardLayout.setVisibility(View.GONE);
        }
        if (!serverShowGiftEffect) {
            moreGiftEffectLayout.setVisibility(View.GONE);
        }
        updateInteractMenuVisibility();
    }

    private void updateInteractMenuVisibility() {
        final boolean show = PLVSequenceWrapper.wrap(children(moreSettingsInteractLayout))
                .any(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        return view.getVisibility() == View.VISIBLE;
                    }
                });
        moreSettingsInteractTv.setVisibility(show ? View.VISIBLE : View.GONE);
        moreSettingsInteractLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initMixLayoutButton() {
        final boolean showMixLayoutButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_SETTING_SHOW_MIX_LAYOUT_BUTTON, true);
        if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CHANGE_MIX_LAYOUT) && showMixLayoutButton) {
            plvsaMoreMixLayout.setVisibility(View.VISIBLE);
        } else {
            plvsaMoreMixLayout.setVisibility(View.GONE);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setMenuSize(PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_SIZE_PORT : MENU_DRAWER_SIZE_LAND);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
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
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
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

    public void updateCloseRoomLayout(boolean hide){
        plvsaMoreCloseRoomLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return PLVDynamicProxy.forClass(IPLVStreamerContract.IStreamerView.class)
                .proxyAll(streamerView, linkMicSettingLayout.streamerView);
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
        if (shareLayout != null) {
            shareLayout.destroy();
        }
        PolyvChatroomManager.getInstance().removeOnRoomStatusListener(roomStatusListener);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {
        private boolean isShare;
        private boolean hasLinkMicUser;

        @Override
        public void setPresenter(@NonNull final IPLVStreamerContract.IStreamerPresenter presenter) {
            super.setPresenter(presenter);
            streamerPresenter = presenter;

            //添加获取当前码率的监听器
            presenter.getData().getCurBitrate().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Integer>() {
                @Override
                public void onChanged(@Nullable Integer bitrate) {
                    if (bitrate == null || getContext() == null) {
                        return;
                    }
                    String bitrateText = PLVStreamerConfig.QualityLevel.getTextCombineTemplate(bitrate, channelId);
                    plvsaMoreBitrateTv.setText(bitrateText);
                    Integer iconId = bitrateMapIcon.get(bitrate);
                    if (iconId != null) {
                        plvsaMoreBitrateIv.setImageResource(iconId);
                    }

                    if (switchBitrateByUser) {
                        String toastText = PLVAppUtils.formatString(R.string.plv_player_change_definition_2, bitrateText);
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                    switchBitrateByUser = false;
                }
            });
            //添加推流的媒体状态监听器
            presenter.getData().getEnableAudio().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreMicIv.setSelected(!aBoolean);

                    if (attachedToWindow) {
                        String toastText = PLVAppUtils.getString(aBoolean ? R.string.plv_linkmic_microphone_unmute_2 : R.string.plv_linkmic_microphone_mute);
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                }
            });
            presenter.getData().getEnableVideo().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean enableVideo) {
                    if (enableVideo == null) {
                        return;
                    }
                    PLVSAMoreLayout.this.isEnableVideo = enableVideo;

                    plvsaMoreCameraIv.setSelected(!enableVideo);
                    plvsaMoreCameraSwitchIv.setEnabled(enableVideo);
                    plvsaMoreMirrorIv.setEnabled(enableVideo && !plvsaMoreCameraSwitchIv.isSelected());

                    if (attachedToWindow) {
                        String toastText = PLVAppUtils.getString(enableVideo ? R.string.plv_linkmic_camera_unmute_2 : R.string.plv_linkmic_camera_mute);
                        PLVToast.Builder.context(getContext())
                                .setText(toastText)
                                .build().show();
                    }
                }
            });
            presenter.getData().getIsFrontCamera().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreCameraSwitchIv.setSelected(!aBoolean);
                    plvsaMoreMirrorIv.setEnabled(aBoolean && !plvsaMoreCameraIv.isSelected());
                    plvsaMoreFlashlightIv.setEnabled(!aBoolean);
                    if (aBoolean) {
                        plvsaMoreFlashlightIv.setSelected(false);
                    }
                }
            });
            presenter.getData().getIsFrontMirrorMode().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean aBoolean) {
                    if (aBoolean == null) {
                        return;
                    }
                    plvsaMoreMirrorIv.setSelected(!aBoolean);
                }
            });
            presenter.getData().getIsStartShareScreen().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isStartShare) {
                    if (isStartShare == null) {
                        return;
                    }
                    isShare = isStartShare;
                    plvsaMoreShareScreenIv.setSelected(isStartShare);
                    plvsaMoreShareScreenTv.setText(isStartShare ? getContext().getString(R.string.plvsa_streamer_sharescreen_exit) : getContext().getString(R.string.plvsa_streamer_sharescreen_start));
                    //去掉摄像头可选项
                    plvsaMoreCameraIv.setClickable(!isStartShare);
                    plvsaMoreCameraTv.setClickable(!isStartShare);
                    plvsaMoreCameraSwitchIv.setClickable(!isStartShare);
                    plvsaMoreCameraSwitchTv.setClickable(!isStartShare);
                    plvsaMoreMirrorIv.setClickable(!isStartShare);
                    plvsaMoreMirrorTv.setClickable(!isStartShare);
                    //置灰
                    if(isStartShare) {
                        plvsaMoreCameraIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        plvsaMoreCameraSwitchIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        plvsaMoreMirrorIv.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    } else {
                        plvsaMoreCameraIv.clearColorFilter();
                        plvsaMoreCameraSwitchIv.clearColorFilter();
                        plvsaMoreMirrorIv.clearColorFilter();
                    }
                    boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                            .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
                    if (moreWaterLayout != null) {
                        moreWaterLayout.setVisibility((showWatermarkButton && !isShare && !hasLinkMicUser) ? View.VISIBLE : View.GONE);
                    }
                }
            });
            presenter.getData().getLinkMicCount().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    if (integer > 1) {
                        moreHangUpViewerLinkmicLayout.setEnabled(true);
                        moreHangUpViewerLinkmicIv.setAlpha(1f);
                    } else {
                        moreHangUpViewerLinkmicLayout.setEnabled(false);
                        moreHangUpViewerLinkmicIv.setAlpha(0.5f);
                    }
                }
            });
        }

        @Override
        public void onHasLinkMicUser(boolean hasHasLinkMicUser) {
            this.hasLinkMicUser = hasHasLinkMicUser;
            boolean showWatermarkButton = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                    .getOrDefault(PLVChannelFeature.STREAMER_WATERMARK_ENABLE, false);
            if (moreWaterLayout != null) {
                moreWaterLayout.setVisibility((showWatermarkButton && !isShare && !hasHasLinkMicUser) ? View.VISIBLE : View.GONE);
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private boolean quickClickLimit() {
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= QUICK_CLICK_LIMIT_TIME) {
            lastClickTime = curClickTime;
            return false;
        }
        return true;
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvsa_more_camera_iv
                || id == R.id.plvsa_more_camera_tv) {
            if (!quickClickLimit()) {
                if (streamerPresenter != null) {
                    streamerPresenter.enableLocalVideo(plvsaMoreCameraIv.isSelected());
                }
            }
        } else if (id == R.id.plvsa_more_mic_iv
                || id == R.id.plvsa_more_mic_tv) {
            if (streamerPresenter != null) {
                streamerPresenter.enableRecordingAudioVolume(plvsaMoreMicIv.isSelected());
            }
        } else if (id == R.id.plvsa_more_camera_switch_iv
                || id == R.id.plvsa_more_camera_switch_tv) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickCameraSwitchViewTime > 500) {
                if (streamerPresenter != null) {
                    streamerPresenter.setCameraDirection(plvsaMoreCameraSwitchIv.isSelected());
                }
                lastClickCameraSwitchViewTime = currentTime;
            }
        } else if (id == R.id.plvsa_more_mirror_iv
                || id == R.id.plvsa_more_mirror_tv) {
            if (streamerPresenter != null) {
                streamerPresenter.setFrontCameraMirror(plvsaMoreMirrorIv.isSelected());
            }
        } else if (id == R.id.plvsa_more_flashlight_iv
                || id == R.id.plvsa_more_flashlight_tv) {
            if (streamerPresenter != null) {
                boolean result = streamerPresenter.enableTorch(!plvsaMoreFlashlightIv.isSelected());
                if (result) {
                    plvsaMoreFlashlightIv.setSelected(!plvsaMoreFlashlightIv.isSelected());
                }
            }
        } else if (id == R.id.plvsa_more_bitrate_iv
                || id == R.id.plvsa_more_bitrate_tv) {
            bitrateLayout.open();
        } else if (id == R.id.plvsa_more_mix_iv
                || id == R.id.plvsa_mix_tv) {
            mixLayout.open();
        } else if (id == R.id.plvsa_more_close_room_iv
                || id == R.id.plvsa_more_close_room_tv) {
            PolyvChatroomManager.getInstance().toggleRoomByEvent(new IPolyvChatroomManager.RequestApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    plvsaMoreCloseRoomIv.setSelected(!plvsaMoreCloseRoomIv.isSelected());
                    postToMainThread(new Runnable() {
                        @Override
                        public void run() {
                            plvsaMoreCloseRoomTv.setText(plvsaMoreCloseRoomIv.isSelected() ? R.string.plv_chat_cancel_close_room : R.string.plv_chat_confirm_close_room);
                            String toastText = PLVAppUtils.getString(!plvsaMoreCloseRoomIv.isSelected() ? R.string.plv_chat_confirm_close_room_2 : R.string.plv_chat_cancel_close_room_2);
                            PLVToast.Builder.context(getContext())
                                    .setText(toastText)
                                    .build().show();
                        }
                    });
                }

                @Override
                public void onFailed(Throwable t) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_chat_network_bad)
                            .build()
                            .show();
                }
            });
        } else if (id == R.id.plvsa_more_share_screen_ll){
            close();
            if(!PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_GRANT_PERMISSION_SHARE_SCREEN)){
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plvsa_streamer_sharescreen_need_permission))
                        .build()
                        .show();
                return;
            }

            //开始屏幕共享
            if (streamerPresenter != null) {
                if (!plvsaMoreShareScreenIv.isSelected()) {
                    final PLVCustomScreenShareData customScreenShareData = new PLVCustomScreenShareData();
                    customScreenShareData.notificationIcon = R.drawable.plvsa_ic_launcher;
                    customScreenShareData.notificationText = "正在录制/投射您的屏幕";
                    streamerPresenter.requestShareScreen((Activity) getContext(), customScreenShareData);
                } else {
                    streamerPresenter.exitShareScreen();
                }
            }
        } else if (id == moreBeautyLl.getId()) {
            close();
            if (!isEnableVideo) {
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_beauty_need_open_camera))
                        .show();
                return;
            }
            PLVDependManager.getInstance().get(PLVBeautyViewModel.class).showBeautyMenu();
        } else if (id == moreShareLl.getId()) {
            close();
            shareLayout.open();
        } else if (id == morePushDowngradePreferenceLl.getId()) {
            pushDowngradePreferenceLayout.open();
        } else if (id == moreAllowViewerLinkmicLayout.getId()) {
            switchAllowViewerLinkMic(!moreAllowViewerLinkmicLayout.isActivated());
        } else if (id == moreLinkmicSettingLayout.getId()) {
            close();
            linkMicSettingLayout.open();
        } else if (id == moreShareScreenFloatMessageLl.getId()) {
            close();
            screenShareFloatMessageLayout.open();
        } else if (id == moreHangUpViewerLinkmicLayout.getId()) {
            closeAllViewerLinkMic();
        } else if (id == moreInteractSigninLl.getId()) {
            if (onViewActionListener != null) {
                onViewActionListener.onShowSignInAction();
            }
        } else if (id == moreGiftRewardLayout.getId()) {
            moreGiftRewardLayout.setSelected(!moreGiftRewardLayout.isSelected());
            moreGiftRewardLayout.setEnabled(false);
            PLVToast.Builder.context(getContext())
                    .setText(getContext().getString(R.string.plv_reward_gift_reward_switch_tips))
                    .show();
            rewardDataSource.updateGiftReward(channelId, !moreGiftRewardLayout.isSelected(), new IPLVPointRewardDataSource.IPointRewardListener<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody responseBody) {
                    moreGiftRewardLayout.setEnabled(true);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    moreGiftRewardLayout.setEnabled(true);
                    PLVCommonLog.exception(throwable);
                }
            });
        } else if (id == moreGiftEffectLayout.getId()) {
            moreGiftEffectLayout.setSelected(!moreGiftEffectLayout.isSelected());
            if (onViewActionListener != null) {
                onViewActionListener.onGiftEffectSwitch(!moreGiftEffectLayout.isSelected());
            }
        } else if (id == moreWaterLayout.getId()) {
            close();
            PLVSAStickerLayout.tryShow();
        } else if (id == moreVirtualBgLayout.getId()) {
            PLVVirtualBackgroundLayout.tryShow();
        }
    }

    private void switchAllowViewerLinkMic(final boolean toAllow) {
        if (streamerPresenter == null) {
            return;
        }
        final boolean isNetworkConnected = PLVNetworkUtils.isConnected(getContext());
        if (!isNetworkConnected) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_allow_viewer_linkmic_fail_toast)
                    .show();
            return;
        }

        boolean success;
        if (toAllow) {
            success = streamerPresenter.allowViewerRaiseHand(new Ack() {
                @Override
                public void call(Object... args) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_allow_viewer_linkmic_toast)
                            .show();
                    moreAllowViewerLinkmicLayout.setActivated(true);
                }
            });
        } else {
            success = streamerPresenter.disallowViewerRaiseHand(new Ack() {
                @Override
                public void call(Object... args) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_disallow_viewer_linkmic_toast)
                            .show();
                    moreAllowViewerLinkmicLayout.setActivated(false);
                }
            });
        }

        if (!success) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_streamer_allow_viewer_linkmic_fail_toast)
                    .show();
            PLVCommonLog.d(TAG, "switchAllowViewerLinkMic fail, toAllow:" + toAllow);
        }
    }

    private void closeAllViewerLinkMic() {
        if (streamerPresenter == null) {
            return;
        }
        PLVSAConfirmDialog.Builder.context(getContext())
                .setTitle(R.string.plv_common_dialog_tip)
                .setContent(R.string.plv_linkmic_dialog_hang_all_off_confirm_ask)
                .setLeftButtonText(R.string.plv_common_dialog_cancel)
                .setRightButtonText(R.string.plv_common_dialog_confirm)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        if (streamerPresenter != null) {
                            streamerPresenter.closeAllUserLinkMic();
                            PLVToast.Builder.context(getContext())
                                    .setText(R.string.plv_linkmic_toast_hang_all_off)
                                    .build()
                                    .show();
                        }
                    }
                })
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View父类方法重写">

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    private void updateViewWithOrientation() {
        Position menuPosition;
        FrameLayout.LayoutParams moreLayoutParam = (FrameLayout.LayoutParams) plvsaMoreLayout.getLayoutParams();
        ConstraintLayout.LayoutParams settingLayoutParam = (ConstraintLayout.LayoutParams) plvsaMoreSettingsSv.getLayoutParams();

        if (PLVScreenUtils.isPortrait(getContext())) {
            menuPosition = MENU_DRAWER_POSITION_PORT;
            moreLayoutParam.height = MORE_LAYOUT_HEIGHT_PORT;
            moreLayoutParam.gravity = MORE_LAYOUT_GRAVITY_PORT;
            plvsaMoreLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_PORT);
            settingLayoutParam.topMargin = ConvertUtils.dp2px(10);
        } else {
            menuPosition = MENU_DRAWER_POSITION_LAND;
            moreLayoutParam.height = MORE_LAYOUT_HEIGHT_LAND;
            moreLayoutParam.gravity = MORE_LAYOUT_GRAVITY_LAND;
            plvsaMoreLayout.setBackgroundResource(MORE_LAYOUT_BACKGROUND_RES_LAND);
            settingLayoutParam.topMargin = ConvertUtils.dp2px(plvsaMoreSettingsLayout.getChildCount() > 9 ? 48 : 0);
        }

        if (menuDrawer != null) {
            menuDrawer.setPosition(menuPosition);
        }
        plvsaMoreLayout.setLayoutParams(moreLayoutParam);
        plvsaMoreSettingsSv.setLayoutParams(settingLayoutParam);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * 显示签到
         */
        void onShowSignInAction();

        /**
         * 礼物特效开关切换
         */
        void onGiftEffectSwitch(boolean isOpen);
    }
    // </editor-fold>
}
