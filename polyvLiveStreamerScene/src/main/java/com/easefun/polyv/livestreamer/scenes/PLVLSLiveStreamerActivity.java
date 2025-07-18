package com.easefun.polyv.livestreamer.scenes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.helper.PLVBeautyInitHelper;
import com.easefun.polyv.livecommon.module.modules.di.PLVCommonModule;
import com.easefun.polyv.livecommon.module.modules.interact.IPLVStreamerInteractLayout;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVLiveLocalActionHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVToTopView;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.beauty.IPLVLSBeautyLayout;
import com.easefun.polyv.livestreamer.modules.beauty.PLVLSBeautyLayout;
import com.easefun.polyv.livestreamer.modules.chatroom.IPLVLSChatroomLayout;
import com.easefun.polyv.livestreamer.modules.document.IPLVLSDocumentLayout;
import com.easefun.polyv.livestreamer.modules.document.PLVLSDocumentLayout;
import com.easefun.polyv.livestreamer.modules.document.widget.PLVLSDocumentControllerExpandMenu;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSNetworkDisconnectMaskLayout;
import com.easefun.polyv.livestreamer.modules.liveroom.PLVLSPushDowngradeAlertToastLayout;
import com.easefun.polyv.livestreamer.modules.statusbar.IPLVLSStatusBarLayout;
import com.easefun.polyv.livestreamer.modules.streamer.IPLVLSStreamerLayout;
import com.easefun.polyv.livestreamer.modules.streamer.di.PLVLSStreamerModule;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * 手机开播三分屏场景界面。
 * 支持的功能有：推流和连麦、文档、聊天室
 */
public class PLVLSLiveStreamerActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final String TAG = PLVLSLiveStreamerActivity.class.getSimpleName();

    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";             // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";               // 开播者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";           // 开播者昵称
    private static final String EXTRA_AVATAR_URL = "avatarUrl";             // 开播者头像url
    private static final String EXTRA_ACTOR = "actor";                      // 开播者头衔
    private static final String EXTRA_USERTYPE = "usertype";                // 开播者角色
    private static final String EXTRA_COLIN_MIC_TYPE = "colinMicType";      // 嘉宾连麦类型
    private static final String EXTRA_IS_OPEN_MIC = "isOpenMic";            // 麦克风开关
    private static final String EXTRA_IS_OPEN_CAMERA = "isOpenCamera";      // 摄像头开关
    private static final String EXTRA_IS_FRONT_CAMERA = "isFrontCamera";    // 摄像头方向

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // 状态栏布局
    private IPLVLSStatusBarLayout plvlsStatusBarLy;
    // 文档布局
    private IPLVLSDocumentLayout plvlsDocumentLy;
    // 推流和连麦布局
    private IPLVLSStreamerLayout plvlsStreamerLy;
    // 聊天室布局
    private IPLVLSChatroomLayout plvlsChatroomLy;
    // 评论上墙布局
    private PLVToTopView toTopView;
    // 美颜布局
    private IPLVLSBeautyLayout beautyLayout;
    // 推流降级提示布局
    private PLVLSPushDowngradeAlertToastLayout pushDowngradeAlertToastLy;
    // 互动布局
    private IPLVStreamerInteractLayout interactLayout;
    private PLVLSNetworkDisconnectMaskLayout networkDisconnectMaskLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动手机开播三分屏页
     *
     * @param activity      上下文Activity
     * @param channelId     频道号
     * @param viewerId      开播者ID
     * @param viewerName    开播者昵称
     * @param avatarUrl     开播者头像url
     * @param actor         开播者头衔
     * @param isOpenMic     是否打开麦克风
     * @param isOpenCamera  是否打开相机
     * @param isFrontCamera 是否使用前置摄像头
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchStreamer(@NonNull Activity activity,
                                                 @NonNull String channelId,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String avatarUrl,
                                                 @NonNull String actor,
                                                 @NonNull String usertype,
                                                 @NonNull String colinMicType,
                                                 boolean isOpenMic,
                                                 boolean isOpenCamera,
                                                 boolean isFrontCamera) {
        if (activity == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_activity_is_null));
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_channel_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_viewer_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_viewer_name_is_empty));
        }
        if (TextUtils.isEmpty(avatarUrl)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_avatar_url_is_empty));
        }
        if (TextUtils.isEmpty(actor)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_actor_is_empty));
        }
        if (TextUtils.isEmpty(usertype)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_user_type_is_empty));
        }
        if (TextUtils.isEmpty(colinMicType)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvls_login_streamer_error_colin_mic_type_is_empty));
        }

        PLVLanguageUtil.checkOverrideLanguage(channelId, PLVLanguageUtil.LANGUAGE_FOLLOW_SYSTEM);
        Intent intent = new Intent(activity, PLVLSLiveStreamerActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_ACTOR, actor);
        intent.putExtra(EXTRA_USERTYPE, usertype);
        intent.putExtra(EXTRA_COLIN_MIC_TYPE, colinMicType);
        intent.putExtra(EXTRA_IS_OPEN_MIC, isOpenMic);
        intent.putExtra(EXTRA_IS_OPEN_CAMERA, isOpenCamera);
        intent.putExtra(EXTRA_IS_FRONT_CAMERA, isFrontCamera);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(PLVLanguageUtil.attachLanguageActivity(newBase, this));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependency();
        setContentView(R.layout.plvls_live_streamer_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initBeautyModule();
        initPushDowngradeAlertLayout();

        checkStreamRecover();

        observeStatusBarLayout();
        observeStreamerLayout();
        observeChatroomLayout();
        observeDocumentLayout();
        observeNetworkDisconnectLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVLanguageUtil.detachLanguageActivity();
        PLVBeautyInitHelper.getInstance().destroy();
        if (plvlsStatusBarLy != null) {
            plvlsStatusBarLy.destroy();
        }
        if (plvlsStreamerLy != null) {
            plvlsStreamerLy.destroy();
        }
        if (plvlsChatroomLy != null) {
            plvlsChatroomLy.destroy();
        }
        if (plvlsDocumentLy != null) {
            plvlsDocumentLy.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && requestCode == PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT
                && data != null) {
            // 选择上传PPT文档
            if (plvlsDocumentLy != null) {
                plvlsDocumentLy.onSelectUploadDocument(data);
            }
        }
        if (plvlsStatusBarLy != null) {
            plvlsStatusBarLy.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (plvlsStatusBarLy != null && plvlsStatusBarLy.onBackPressed()) {
            return;
        } else if (plvlsChatroomLy != null && plvlsChatroomLy.onBackPressed()) {
            return;
        } else if (plvlsDocumentLy != null && plvlsDocumentLy.onBackPressed()) {
            return;
        } else if (beautyLayout != null && beautyLayout.onBackPressed()) {
            return;
        }

        // 弹出退出直播间的确认框
        PLVLSConfirmDialog.Builder.context(this)
                .setTitleVisibility(View.GONE)
                .setContent(R.string.plv_live_room_dialog_steamer_exit_confirm_ask)
                .setRightButtonText(R.string.plv_common_dialog_confirm)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        PLVLSLiveStreamerActivity.super.onBackPressed();
                    }
                })
                .show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(PLVLanguageUtil.setToConfiguration(newConfig, this));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 依赖注入">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVCommonModule.instance)
                .addModule(PLVLSStreamerModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面参数">
    private void initParams() {
        // 获取输入数据
        Intent intent = getIntent();
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        String avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL);
        String actor = intent.getStringExtra(EXTRA_ACTOR);
        String role = intent.getStringExtra(EXTRA_USERTYPE);
        String colinMicType = intent.getStringExtra(EXTRA_COLIN_MIC_TYPE);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, avatarUrl, role, actor);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);
        PLVLiveChannelConfigFiller.setColinMicType(colinMicType);
        PLVLiveChannelConfigFiller.setChannelType(PLVLiveChannelType.PPT);

        PLVLiveLocalActionHelper.getInstance().enterChannel(channelId);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 直播间数据管理器">
    private void initLiveRoomManager() {
        // 使用PLVLiveChannelConfigFiller配置好直播参数后，用其创建直播间数据管理器实例
        liveRoomDataManager = new PLVLiveRoomDataManager(PLVLiveChannelConfigFiller.generateNewChannelConfig());

        // 进行网络请求，获取直播详情数据
        liveRoomDataManager.requestChannelDetail();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面UI">
    private void initView() {
        plvlsStatusBarLy = findViewById(R.id.plvls_status_bar_ly);
        plvlsDocumentLy = findViewById(R.id.plvls_document_ly);
        plvlsStreamerLy = findViewById(R.id.plvls_streamer_ly);
        plvlsChatroomLy = findViewById(R.id.plvls_chatroom_ly);
        toTopView = findViewById(R.id.plvls_chatroom_to_top_view);
        pushDowngradeAlertToastLy = findViewById(R.id.plvls_push_downgrade_alert_toast_ly);
        interactLayout = findViewById(R.id.plvsa_interact_layout);
        networkDisconnectMaskLayout = findViewById(R.id.plvls_network_disconnect_mask_layout);

        interactLayout.init(liveRoomDataManager);

        // 初始化推流和连麦布局
        plvlsStreamerLy.init(liveRoomDataManager);
        // 初始化推流的媒体配置
        boolean isOpenMic = getIntent().getBooleanExtra(EXTRA_IS_OPEN_MIC, true);
        boolean isOpenCamera = getIntent().getBooleanExtra(EXTRA_IS_OPEN_CAMERA, true);
        boolean isFrontCamera = getIntent().getBooleanExtra(EXTRA_IS_FRONT_CAMERA, true);
        plvlsStreamerLy.enableRecordingAudioVolume(isOpenMic);
        plvlsStreamerLy.enableLocalVideo(isOpenCamera);
        plvlsStreamerLy.setCameraDirection(isFrontCamera);

        if(PLVSStreamerInnerDataTransfer.getInstance().isOnlyAudio()){
            //如果是音频开播，主动关闭摄像头，其优先级大于传进来的isOpenCamera
            plvlsStreamerLy.enableLocalVideo(false);
        }

        // 初始化状态栏布局
        plvlsStatusBarLy.init(liveRoomDataManager);

        // 注册成员列表中的streamerView，并请求成员列表接口
        plvlsStreamerLy.getStreamerPresenter().registerView(plvlsStatusBarLy.getStreamerView());
        plvlsStreamerLy.getStreamerPresenter().requestMemberList();

        // 初始化聊天室布局
        plvlsChatroomLy.init(liveRoomDataManager);

        // 初始化文档布局
        plvlsDocumentLy.init(liveRoomDataManager);
        plvlsStreamerLy.getStreamerPresenter().registerView(plvlsDocumentLy.getDocumentLayoutStreamerView());

        // 进入横屏模式
        PLVScreenUtils.enterLandscape(this);

        // 初始化美颜布局
        beautyLayout = new PLVLSBeautyLayout(this);

        // 初始化评论上墙布局
        toTopView.setIsLiveType(true);
        if (PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER)) {
            toTopView.setCancelTopStyle();
        }
         plvlsChatroomLy.getChatroomPresenter().registerView(toTopView.getChatroomView());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 美颜模块">

    private void initBeautyModule() {
        if (!PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_BEAUTY_ENABLE)) {
            return;
        }
        PLVBeautyInitHelper.getInstance().init(this, new PLVSugarUtil.Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) {
                PLVCommonLog.i(TAG, "initBeauty success: " + success);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 状态栏">
    private void observeStatusBarLayout() {
        //监听状态栏布局的UI交互事件
        plvlsStatusBarLy.setOnViewActionListener(new IPLVLSStatusBarLayout.OnViewActionListener() {
            @Override
            public void onClassControl(boolean isStart) {
                if (isStart) {
                    plvlsStreamerLy.startClass();
                } else {
                    plvlsStreamerLy.stopClass();
                }
            }

            @Override
            public PLVLinkMicConstant.NetworkQuality getCurrentNetworkQuality() {
                return plvlsStreamerLy.getNetworkQuality();
            }

            @Nullable
            @Override
            public PLVPushDowngradePreference getCurrentDowngradePreference() {
                return plvlsStreamerLy.getStreamerPresenter().getPushDowngradePreference();
            }

            @Override
            public void onDowngradePreferenceChanged(@NonNull PLVPushDowngradePreference preference) {
                plvlsStreamerLy.setPushDowngradePreference(preference);
            }

            @Override
            public void onShowSignInAction() {
                interactLayout.showSignIn();
            }

            @Override
            public boolean isStreamerStartSuccess() {
                return plvlsStreamerLy.isStreamerStartSuccess();
            }

            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return plvlsStreamerLy.getBitrateInfo();
            }

            @Override
            public void onBitrateClick(int bitrate) {
                plvlsStreamerLy.setBitrate(bitrate);
            }

            @Override
            public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
                return plvlsStreamerLy.getMixLayoutType();
            }

            @Override
            public void onChangeMixLayoutType(PLVStreamerConfig.MixLayoutType mix) {
                plvlsStreamerLy.setMixLayoutType(mix);
            }

            @Override
            public boolean isCurrentLocalVideoEnable() {
                final Boolean res = plvlsStreamerLy.getStreamerPresenter().getData().getEnableVideo().getValue();
                return res != null && res;
            }

            @Override
            public void onMicControl(int position, boolean isMute) {
                if (position == 0) {
                    plvlsStreamerLy.enableRecordingAudioVolume(!isMute);
                } else {
                    plvlsStreamerLy.muteUserMedia(position, false, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (position == 0) {
                    plvlsStreamerLy.enableLocalVideo(!isMute);
                } else {
                    plvlsStreamerLy.muteUserMedia(position, true, isMute);
                }
            }

            @Override
            public void onFrontCameraControl(int position, boolean isFront) {
                if (position == 0) {
                    plvlsStreamerLy.setCameraDirection(isFront);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
                plvlsStreamerLy.controlUserLinkMic(position, action);
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, final boolean isGrant) {
                plvlsStreamerLy.getStreamerPresenter().setUserPermissionSpeaker(userId, isGrant, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                    @Override
                    public void onCall(Object... args) {
                        final boolean isGuestTransferPermission = !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
                        final String text;
                        if (!isGrant) {
                            text = PLVAppUtils.getString(R.string.plv_streamer_remove_speaker_permission_2);
                        } else if (isGuestTransferPermission) {
                            text = PLVAppUtils.getString(R.string.plv_streamer_change_speaker_permission);
                        } else {
                            text = PLVAppUtils.getString(R.string.plv_streamer_grant_speaker_permission_2);
                        }
                        PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                                .setText(text)
                                .show();
                    }
                });
            }

            @Override
            public int getPosition(PLVMemberItemDataBean memberItemDataBean) {
                return plvlsStreamerLy.getPositionByMemberItem(memberItemDataBean);
            }

            @Override
            public void closeAllUserLinkMic() {
                plvlsStreamerLy.closeAllUserLinkMic();
            }

            @Override
            public void muteAllUserAudio(boolean isMute) {
                plvlsStreamerLy.muteAllUserAudio(isMute);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 推流和连麦">
    private void observeStreamerLayout() {
        //监听推流状态变化
        plvlsStreamerLy.addOnStreamerStatusListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isStartedStatus) {
                if (isStartedStatus == null) {
                    return;
                }
                if (plvlsDocumentLy != null) {
                    plvlsDocumentLy.setStreamerStatus(isStartedStatus);
                }
                plvlsStatusBarLy.setStreamerStatus(isStartedStatus);
            }
        });
        //监听推流网络变化
        plvlsStreamerLy.addOnNetworkQualityListener(new IPLVOnDataChangedListener<PLVLinkMicConstant.NetworkQuality>() {
            @Override
            public void onChanged(@Nullable PLVLinkMicConstant.NetworkQuality quality) {
                if (quality == null) {
                    return;
                }
                plvlsStatusBarLy.updateNetworkQuality(quality);
            }
        });
        plvlsStreamerLy.addOnNetworkStatusListener(new IPLVOnDataChangedListener<PLVNetworkStatusVO>() {
            @Override
            public void onChanged(@Nullable PLVNetworkStatusVO networkStatusVO) {
                if (networkStatusVO == null) {
                    return;
                }
                plvlsStatusBarLy.updateNetworkStatus(networkStatusVO);
            }
        });
        //监听推流的累计时间
        plvlsStreamerLy.addOnStreamerTimeListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvlsStatusBarLy.updateStreamerTime(integer);
            }
        });
        //监听因断网延迟20s断流的状态
        plvlsStreamerLy.addOnShowNetBrokenListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsStatusBarLy.showAlertDialogNoNetwork();
            }
        });
        //监听用户连麦请求的变化
        plvlsStreamerLy.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) {
                    return;
                }
                plvlsStatusBarLy.updateUserRequestStatus(s);
            }
        });
        //添加推流的媒体状态监听器
        plvlsStreamerLy.addOnEnableAudioListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setOpenMicViewStatus(aBoolean);
                PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                        .setText(PLVAppUtils.getString(aBoolean ? R.string.plv_linkmic_microphone_unmute_2 : R.string.plv_linkmic_microphone_mute_2))
                        .build()
                        .show();
            }
        });
        plvlsStreamerLy.addOnEnableVideoListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setOpenCameraViewStatus(aBoolean);
                PLVToast.Builder.context(PLVLSLiveStreamerActivity.this)
                        .setText(PLVAppUtils.getString(aBoolean ? R.string.plv_linkmic_camera_unmute_2 : R.string.plv_linkmic_camera_mute_2))
                        .build()
                        .show();
            }
        });
        plvlsStreamerLy.addOnIsFrontCameraListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                plvlsChatroomLy.setFrontCameraViewStatus(aBoolean);
            }
        });
    }

    private void initPushDowngradeAlertLayout() {
        plvlsStreamerLy.getStreamerPresenter().registerView(pushDowngradeAlertToastLy.streamerView);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 聊天室">
    private void observeChatroomLayout() {
        //监听聊天室布局的UI交互事件
        plvlsChatroomLy.setOnViewActionListener(new IPLVLSChatroomLayout.OnViewActionListener() {
            @Override
            public boolean onMicControl(boolean isMute) {
                return plvlsStreamerLy.enableRecordingAudioVolume(!isMute);
            }

            @Override
            public boolean onCameraControl(boolean isMute) {
                PLVLiveLocalActionHelper.getInstance().updateCameraEnable(!isMute);
                return plvlsStreamerLy.enableLocalVideo(!isMute);
            }

            @Override
            public boolean onFrontCameraControl(boolean isFront) {
                PLVLiveLocalActionHelper.getInstance().updateCameraDirection(isFront);
                return plvlsStreamerLy.setCameraDirection(isFront);
            }

            @Override
            public void onKickByServer() {
                boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
                if (plvlsStreamerLy != null) {
                    if (!isGuest) {
                        plvlsStreamerLy.stopClass();
                    }
                    plvlsStreamerLy.destroy();
                }
            }

            @Override
            public boolean isStreamerStartSuccess() {
                return plvlsStreamerLy.isStreamerStartSuccess();
            }
        });
        //监听聊天室的在线人数变化
        plvlsChatroomLy.addOnOnlineCountListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvlsStatusBarLy.setOnlineCount(integer);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 文档">
    private void observeDocumentLayout() {
        // 监听标注工具展开折叠
        plvlsDocumentLy.setMarkToolOnFoldExpandListener(new PLVLSDocumentControllerExpandMenu.OnFoldExpandListener() {
            @Override
            public void onFoldExpand(boolean isExpand) {
                plvlsChatroomLy.notifyDocumentMarkToolExpand(isExpand);
            }
        });

        // 监听文档布局切换全屏
        plvlsDocumentLy.setOnSwitchFullScreenListener(new PLVLSDocumentLayout.OnSwitchFullScreenListener() {
            @Override
            public void onSwitchFullScreen(boolean toFullScreen) {
                plvlsChatroomLy.notifyDocumentLayoutFullScreen(toFullScreen);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 网络断开遮罩">

    private void observeNetworkDisconnectLayout() {
        plvlsStreamerLy.getStreamerPresenter().registerView(networkDisconnectMaskLayout.streamerView);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置直播恢复">

    private void checkStreamRecover() {
        boolean isTeacher = PLVSocketUserConstant.USERTYPE_TEACHER.equals(PLVLiveChannelConfigFiller.generateNewChannelConfig().getUser().getViewerType());

        if(liveRoomDataManager.isNeedStreamRecover() && isTeacher){
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.plv_streamer_recover_hint)
                    .setPositiveButton(R.string.plv_streamer_stop_stream, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            liveRoomDataManager.setNeedStreamRecover(false);
                            plvlsStreamerLy.getStreamerPresenter().setRecoverStream(false);
                            plvlsStreamerLy.stopClass();
                        }
                    })
                    .setNegativeButton(R.string.plv_streamer_recover_stream, null)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (plvlsStreamerLy.getNetworkQuality() == PLVLinkMicConstant.NetworkQuality.DISCONNECT) {
                        plvlsStatusBarLy.showAlertDialogNoNetwork();
                        return;
                    }

                    liveRoomDataManager.setNeedStreamRecover(true);
                    plvlsStreamerLy.getStreamerPresenter().setRecoverStream(true);
                    PLVLiveLocalActionHelper.Action action = PLVLiveLocalActionHelper.getInstance().getChannelAction(liveRoomDataManager.getConfig().getChannelId());
                    plvlsStatusBarLy.switchPptType(action.pptType);
                    plvlsStreamerLy.setCameraDirection(action.isFrontCamera);
                    plvlsStreamerLy.enableLocalVideo(action.isEnableCamera);
                    plvlsStreamerLy.startClass();
                    dialog.dismiss();
                }
            });
        }
    }

    // </editor-fold >
}
