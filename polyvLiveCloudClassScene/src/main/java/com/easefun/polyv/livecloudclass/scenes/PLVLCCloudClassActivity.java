package com.easefun.polyv.livecloudclass.scenes;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotEmpty;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.IPLVLCLinkMicLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.PLVLCLinkMicControlBar;
import com.easefun.polyv.livecloudclass.modules.media.IPLVLCMediaLayout;
import com.easefun.polyv.livecloudclass.modules.media.controller.PLVLCLiveLandscapeChannelController;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindow;
import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindowModule;
import com.easefun.polyv.livecloudclass.modules.pagemenu.IPLVLCLivePageMenuLayout;
import com.easefun.polyv.livecloudclass.modules.pagemenu.commodity.PLVLCCommodityDetailActivity;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCFloatingPPTLayout;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCPPTView;
import com.easefun.polyv.livecloudclass.modules.ppt.enums.PLVLCMarkToolEnums;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.cast.manager.PLVCastBusinessManager;
import com.easefun.polyv.livecommon.module.modules.di.PLVCommonModule;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.popover.IPLVPopoverLayout;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.utils.PLVDialogFactory;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVScreenshotHelper;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVToTopView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.IPLVMenuDrawerContainer;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.document.model.PLVPPTPaintStatus;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.livescenes.video.subtitle.vo.PLVLiveSubtitleTranslation;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowLotteryEvent;
import com.plv.socket.event.interact.PLVShowProductDetailEvent;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * date: 2020/10/12
 * author: HWilliamgo
 * 云课堂场景下定义的 直播模式、回放模式 的 共用界面。
 * 直播支持的功能有：播放器、页面菜单、悬浮PPT(三分屏频道独有)、连麦、互动应用。
 * 回放支持的功能有：播放器、页面菜单、悬浮PPT(三分屏频道独有)。
 */
public class PLVLCCloudClassActivity extends PLVBaseActivity implements IPLVMenuDrawerContainer {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCCloudClassActivity.class.getSimpleName();
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 观看者昵称
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//观看者头像地址
    private static final String EXTRA_VID = "vid";//回放视频Id
    private static final String EXTRA_TEMP_STORE_FILE_ID = "temp_store_file_id";//暂存视频id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//回放列表类型
    private static final String EXTRA_MATERIAL_LIBRARY_ENABLED = "material_library_enabled";//素材库回放
    private static final String EXTRA_IS_LIVE = "is_live";//是否是直播
    private static final String EXTRA_CHANNEL_TYPE = "channel_type";//频道类型

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // 播放器布局
    private IPLVLCMediaLayout mediaLayout;
    // 直播页面菜单布局
    private IPLVLCLivePageMenuLayout livePageMenuLayout;
    // 悬浮PPT布局
    private IPLVLCFloatingPPTLayout floatingPPTLayout;
    // 连麦布局
    @Nullable
    private IPLVLCLinkMicLayout linkMicLayout;
    // 横屏聊天室布局
    private PLVLCChatLandscapeLayout chatLandscapeLayout;
    // 评论上墙布局
    private PLVToTopView toTopView;

    //弹窗布局
    private IPLVPopoverLayout popoverLayout;

    // 悬浮PPT布局 和 播放器布局 的切换器
    private PLVViewSwitcher pptViewSwitcher = new PLVViewSwitcher();

    private PLVPlayerLogoView plvPlayerLogoView;

    private PLVScreenshotHelper screenshotHelper = new PLVScreenshotHelper();

    private PLVLCFloatingWindow floatingWindow;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动直播页
     *
     * @param activity    上下文Activity
     * @param channelId   频道号
     * @param channelType 频道类型
     * @param viewerId    观众ID
     * @param viewerName  观众昵称
     * @param langType    观看页语言
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchLive(@NonNull Activity activity,
                                             @NonNull String channelId,
                                             @NonNull PLVLiveChannelType channelType,
                                             @NonNull String viewerId,
                                             @NonNull String viewerName,
                                             @NonNull String viewerAvatar,
                                             String langType) {
        if (activity == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_live_error_activity_is_null));
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_live_error_channel_id_is_empty));
        }
        if (channelType == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_live_error_channel_type_is_null));
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_live_error_viewer_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_live_error_viewer_name_is_empty));
        }

        PLVLanguageUtil.checkOverrideLanguage(channelId, langType);
        Intent intent = new Intent(activity, PLVLCCloudClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_IS_LIVE, true);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }

    /**
     * 启动回放页面
     * 如果没有输入vid的情况下会加载该频道的往期视频列表，如果输入vid的话就直接播放相应vid的视频，
     * 这样的话就不会加载往期视频列表
     *
     * @param activity      上下文Activity
     * @param channelId     频道号
     * @param channelType   频道类型
     * @param vid           视频ID
     * @param viewerId      观众ID
     * @param viewerName    观众昵称
     * @param videoListType 回放视频类型 {@link PLVPlaybackListType}
     * @param langType      观看页语言
     * @param materialLibraryEnabled 素材库回放
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchPlayback(@NonNull Activity activity,
                                                 @NonNull String channelId,
                                                 @NonNull PLVLiveChannelType channelType,
                                                 @Nullable String vid,
                                                 @Nullable String tempStoreFileId,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String viewerAvatar,
                                                 PLVPlaybackListType videoListType,
                                                 String langType,
                                                 boolean materialLibraryEnabled) {
        if (activity == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_playback_error_activity_is_null));
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_playback_error_channel_id_is_empty));
        }
        if (channelType == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_playback_error_channel_type_is_null));
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_playback_error_viewer_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvlc_login_playback_error_viewer_name_is_empty));
        }

        PLVLanguageUtil.checkOverrideLanguage(channelId, langType);
        Intent intent = new Intent(activity, PLVLCCloudClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_TEMP_STORE_FILE_ID, tempStoreFileId);
        intent.putExtra(EXTRA_VIDEO_LIST_TYPE, videoListType);
        intent.putExtra(EXTRA_MATERIAL_LIBRARY_ENABLED, materialLibraryEnabled);
        intent.putExtra(EXTRA_IS_LIVE, false);
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
        setContentView(R.layout.plvlc_cloudclass_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initPptTurnPageLandLayout();

        observeMediaLayout();
        observeLinkMicLayout();
        observePageMenuLayout();
        observePPTView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        floatingWindow.hideWhenOnlyShowByGoHome();
    }

    @Override
    protected void onStop() {
        super.onStop();
        floatingWindow.showByGoHomeWhenEnabled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVCastBusinessManager.getInstance().resetState();
        PLVFloatingPlayerManager.getInstance().runOnFloatingWindowClosed(new Runnable() {
            @Override
            public void run() {
                PLVLanguageUtil.detachLanguageActivity();
                PLVFloatingPlayerManager.getInstance().clear();
                if (mediaLayout != null) {
                    mediaLayout.destroy();
                }
                if (linkMicLayout != null) {
                    linkMicLayout.destroy();
                }
                if (livePageMenuLayout != null) {
                    livePageMenuLayout.destroy();
                }
                if (popoverLayout != null) {
                    popoverLayout.destroy();
                }
                if (floatingPPTLayout != null) {
                    floatingPPTLayout.destroy();
                }
                if (popoverLayout != null) {
                    popoverLayout.destroy();
                }
                if (liveRoomDataManager != null) {
                    liveRoomDataManager.destroy();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (popoverLayout != null && popoverLayout.onBackPress()) {
            return;
        } else if (mediaLayout != null && mediaLayout.onBackPressed()) {
            return;
        } else if (livePageMenuLayout != null && livePageMenuLayout.onBackPressed()) {
            return;
        }
        floatingWindow.showByExitPageWhenEnabled(new PLVLCFloatingWindow.Callback() {
            @Override
            public void run(@Nullable final Runnable callback) {
                //弹出退出直播间的确认框
                PLVDialogFactory.createConfirmDialog(
                        PLVLCCloudClassActivity.this,
                        getResources().getString(
                                liveRoomDataManager.getConfig().isLive()
                                        ? R.string.plv_live_room_dialog_exit_confirm_ask
                                        : R.string.plv_playback_room_dialog_exit_confirm_ask
                        ),
                        getResources().getString(R.string.plv_common_dialog_exit),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (callback != null) {
                                    callback.run();
                                }
                                dialog.dismiss();
                                PLVLCCloudClassActivity.super.onBackPressed();
                            }
                        }
                ).show();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mediaLayout != null && mediaLayout.onKeyDown(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (popoverLayout != null) {
            popoverLayout.onActivityResult(requestCode, resultCode, data);
        }
        if (screenshotHelper != null) {
            screenshotHelper.onActivityResult(requestCode, resultCode, data);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 依赖注入">

    private void injectDependency() {
        PLVDependManager.getInstance()
                .switchStore(this)
                .addModule(PLVCommonModule.instance)
                .addModule(PLVLCFloatingWindowModule.instance);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面参数">
    private void initParams() {
        // 获取输入数据
        final Intent intent = getIntent();
        final boolean isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true);
        final PLVLiveChannelType channelType = (PLVLiveChannelType) intent.getSerializableExtra(EXTRA_CHANNEL_TYPE);
        final String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        final String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        final String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        final String viewerAvatar = intent.getStringExtra(EXTRA_VIEWER_AVATAR);
        final String vid = firstNotEmpty(intent.getStringExtra(EXTRA_VID), intent.getStringExtra(EXTRA_TEMP_STORE_FILE_ID));
        final PLVPlaybackListType videoListType = (PLVPlaybackListType) intent.getSerializableExtra(EXTRA_VIDEO_LIST_TYPE);
        final boolean materialLibraryEnabled = intent.getBooleanExtra(EXTRA_MATERIAL_LIBRARY_ENABLED, false);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setIsLive(isLive);
        PLVLiveChannelConfigFiller.setChannelType(channelType);
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, viewerAvatar,
                PLVLinkMicConfig.getInstance().getLiveChannelTypeNew() == PLVLiveChannelType.PPT
                        ? PLVSocketUserConstant.USERTYPE_SLICE : PLVSocketUserConstant.USERTYPE_STUDENT);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);

        PLVFloatingPlayerManager.getInstance().saveIntent(intent);
        // 根据不同模式，设置对应参数
        if (isLive) {
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_live");
        } else { // 回放模式
            PLVLiveChannelConfigFiller.setupVid(vid != null ? vid : "");
            PLVLiveChannelConfigFiller.setupVideoListType(videoListType != null ? videoListType : PLVPlaybackListType.PLAYBACK);
            PLVLiveChannelConfigFiller.setMaterialLibraryEnabled(materialLibraryEnabled);

            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_" + (vid == null ? "playback" : vid));
        }

        initPlaybackParam(vid, channelId, viewerId, viewerName, viewerAvatar, channelType, videoListType);
    }

    private void initPlaybackParam(
            final String vid,
            final String channelId,
            final String viewerId,
            final String viewerName,
            final String viewerAvatar,
            final PLVLiveChannelType channelType,
            final PLVPlaybackListType playbackListType
    ) {
        PLVDependManager.getInstance().get(PLVPlaybackCacheConfig.class)
                .setApplicationContext(getApplicationContext())
                .setDatabaseNameByViewerId(viewerId)
                .setDownloadRootDirectory(new File(PLVPlaybackCacheConfig.defaultPlaybackCacheDownloadDirectory(this)));
        PLVDependManager.getInstance().get(PLVPlaybackCacheVideoConfig.class)
                .setVid(vid)
                .setVideoPoolIdByVid(vid)
                .setChannelId(channelId)
                .setViewerId(viewerId)
                .setViewerName(viewerName)
                .setViewerAvatar(viewerAvatar)
                .setChannelType(channelType)
                .setPlaybackListType(playbackListType);
    }


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 直播间数据管理器">
    private void initLiveRoomManager() {
        // 使用PLVLiveChannelConfigFiller配置好直播参数后，用其创建直播间数据管理器实例
        liveRoomDataManager = new PLVLiveRoomDataManager(PLVLiveChannelConfigFiller.generateNewChannelConfig());
        // 进行网络请求，获取上报观看热度
        liveRoomDataManager.requestPageViewer();

        // 进行网络请求，获取直播详情数据
        liveRoomDataManager.requestChannelDetail();

        // 进行网络请求，获取功能开关数据
        liveRoomDataManager.requestChannelSwitch();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面UI">
    private void initView() {
        // 播放器ViewStub
        ViewStub videoLyViewStub = findViewById(R.id.plvlc_video_viewstub);

        // 悬浮PPT布局
        floatingPPTLayout = findViewById(R.id.plvlc_ppt_floating_ppt_layout);
        floatingPPTLayout.init(liveRoomDataManager);

        if (liveRoomDataManager.getConfig().isLive()) {
            // 横屏频道控制器
            ViewStub landscapeChannelControllerViewStub = findViewById(R.id.plvlc_ppt_landscape_channel_controller);
            PLVLCLiveLandscapeChannelController liveLandscapeChannelController = (PLVLCLiveLandscapeChannelController) landscapeChannelControllerViewStub.inflate();

            // 播放器布局
            videoLyViewStub.setLayoutResource(R.layout.plvlc_live_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setLandscapeControllerView(liveLandscapeChannelController);
            mediaLayout.setFloatPPTView(floatingPPTLayout.getPPTView());
            mediaLayout.startPlay();

            // 连麦控制条
            ViewStub linkmicControllerViewStub = findViewById(R.id.plvlc_ppt_linkmic_controller);
            PLVLCLinkMicControlBar linkMicControlBar = (PLVLCLinkMicControlBar) linkmicControllerViewStub.inflate();

            // 连麦布局
            ViewStub linkmicLayoutViewStub = findViewById(R.id.plvlc_linkmic_viewstub);
            linkMicLayout = (IPLVLCLinkMicLayout) linkmicLayoutViewStub.inflate();
            linkMicLayout.init(liveRoomDataManager, linkMicControlBar);
            linkMicLayout.hideAll();
        } else {
            // 播放器布局
            videoLyViewStub.setLayoutResource(R.layout.plvlc_playback_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setPPTView(floatingPPTLayout.getPPTView().getPlaybackPPTViewToBindInPlayer());
            mediaLayout.setFloatPPTView(floatingPPTLayout.getPPTView());
            mediaLayout.startPlay();
        }

        // 弹窗布局(包括积分打赏、互动应用)
        ViewStub floatViewStub = findViewById(R.id.plvlc_popover_layout);
        popoverLayout = (IPLVPopoverLayout) floatViewStub.inflate();
        popoverLayout.init(PLVLiveScene.CLOUDCLASS, liveRoomDataManager);
        popoverLayout.setOnPointRewardListener(new OnPointRewardListener() {
            @Override
            public void pointRewardEnable(boolean enable) {
                liveRoomDataManager.getPointRewardEnableData().postValue(PLVStatefulData.success(enable));
            }
        });
        popoverLayout.setOnOpenInsideWebViewListener(new PLVInteractLayout2.OnOpenInsideWebViewListener() {
            boolean needShowControllerOnClosed = false;

            @Override
            public PLVInteractLayout2.OpenUrlParam onOpenWithParam(boolean isLandscape) {
                if (isLandscape) {
                    needShowControllerOnClosed = !needShowControllerOnClosed ? mediaLayout.hideController() : needShowControllerOnClosed;
                }
                return new PLVInteractLayout2.OpenUrlParam(((View) mediaLayout).getHeight() + ConvertUtils.dp2px(48), (ViewGroup) findViewById(R.id.plvlc_popup_container));
            }

            @Override
            public void onClosed() {
                if (needShowControllerOnClosed) {
                    mediaLayout.showController();
                    needShowControllerOnClosed = false;
                }
            }
        });

        popoverLayout.setOnClickProductListener(new PLVInteractLayout2.OnClickProductListener() {

            @Override
            public void onClickProduct(String link) {
                if (!TextUtils.isEmpty(link)) {
                    PLVLCCommodityDetailActivity.start(PLVLCCloudClassActivity.this, link);
                }
            }
        });

        // 页面菜单布局
        livePageMenuLayout = findViewById(R.id.plvlc_live_page_menu_layout);
        livePageMenuLayout.init(liveRoomDataManager);

        // 卡片推送配置
        livePageMenuLayout.getCardPushManager().registerView(mediaLayout.getCardEnterView(), mediaLayout.getCardEnterCdView(), mediaLayout.getCardEnterTipsView());
        livePageMenuLayout.getCardPushManager().setOnCardEnterClickListener(new PLVCardPushManager.OnCardEnterClickListener() {
            @Override
            public void onClick(PLVShowPushCardEvent event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showCardPush(event);
                }
            }
        });


        //无条件抽奖挂件配置
        livePageMenuLayout.getLotteryManager().registerView(mediaLayout.getLotteryEnterView(), mediaLayout.getLotteryEnterCdView(), mediaLayout.getLotteryEnterTipsView());
        livePageMenuLayout.getLotteryManager().setLotteryEnterClickListener(new PLVLotteryManager.OnLotteryEnterClickListener() {
            @Override
            public void onClick(PLVShowLotteryEvent event) {
                //发送显示无条件抽奖
                popoverLayout.getInteractLayout().showLottery(event);
            }
        });

        // 有条件抽奖挂件配置
        PLVWelfareLotteryManager welfareLotteryManager = PLVDependManager.getInstance().get(PLVWelfareLotteryManager.class);
        welfareLotteryManager.onCleared();
        welfareLotteryManager.registerView(mediaLayout.getWelfareLotteryEnterView(), mediaLayout.getWelfareLotteryEnterCdView(), mediaLayout.getLotteryEnterTipsView());

        // 初始化横屏聊天室布局
        chatLandscapeLayout = mediaLayout.getChatLandscapeLayout();
        chatLandscapeLayout.init(livePageMenuLayout.getChatCommonMessageList());
        // 初始化评论上墙View
        toTopView = mediaLayout.getToTopView();

        // 注册 悬浮PPT布局 和 播放器布局 的切换器
        pptViewSwitcher.registerSwitchView(floatingPPTLayout.getPPTSwitchView(), mediaLayout.getPlayerSwitchView());

        // 初始化 屏幕方向
        if (ScreenUtils.isPortrait()) {
            PLVScreenUtils.enterPortrait(this);
        } else {
            PLVScreenUtils.enterLandscape(this);
        }
        PLVScreenUtils.setStatusBarDark(this);
        plvPlayerLogoView = mediaLayout.getLogoView();
        floatingWindow = PLVDependManager.getInstance().get(PLVLCFloatingWindow.class);
    }

    private void startPlaybackOnHasRecordFile() {
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                if (statefulData == null || !statefulData.isSuccess() || statefulData.getData() == null || statefulData.getData().getData() == null) {
                    return;
                }
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                final PLVLiveClassDetailVO liveClassDetailVO = statefulData.getData();
                final boolean hasRecordFile = liveClassDetailVO.getData().isPlaybackEnabled() && liveClassDetailVO.getData().getRecordFileSimpleModel() != null;
                if (hasRecordFile) {
                    mediaLayout.startPlay();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 播放器">
    private void observeMediaLayout() {
        //设置view事件监听器
        mediaLayout.setOnViewActionListener(new IPLVLCMediaLayout.OnViewActionListener() {
            @Override
            public void onClickShowOrHideSubTab(boolean toShow) {
                if (liveRoomDataManager.getConfig().isLive()) {
                    if (linkMicLayout == null) {
                        return;
                    }
                    if (linkMicLayout.isJoinChannel()) {
                        if (toShow) {
                            linkMicLayout.showAll();
                        } else {
                            linkMicLayout.hideLinkMicList();
                        }
                    } else {
                        if (toShow) {
                            floatingPPTLayout.show();
                        } else {
                            floatingPPTLayout.hide();
                        }
                    }
                } else {
                    if (toShow) {
                        floatingPPTLayout.show();
                    } else {
                        floatingPPTLayout.hide();
                    }
                }
            }

            @Override
            public void onShowMediaController(boolean show) {
                if (liveRoomDataManager.getConfig().isLive()) {
                    if (linkMicLayout == null) {
                        return;
                    }
                    if (show) {
                        linkMicLayout.showControlBar();
                    } else {
                        linkMicLayout.hideControlBar();
                    }
                }
            }

            @Override
            public Pair<Boolean, Integer> onSendChatMessageAction(String message, @Nullable PLVChatQuoteVO chatQuoteVO) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                localMessage.setQuote(chatQuoteVO);
                if (chatQuoteVO != null && chatQuoteVO.getMessageId() != null) {
                    return livePageMenuLayout.getChatroomPresenter().sendQuoteMessage(localMessage, chatQuoteVO.getMessageId());
                } else {
                    return livePageMenuLayout.getChatroomPresenter().sendChatMessage(localMessage);
                }
            }

            @Nullable
            @Override
            public PLVChatQuoteVO getChatQuoteContent() {
                return livePageMenuLayout.getChatQuoteContent();
            }

            @Override
            public void onCloseChatQuote() {
                livePageMenuLayout.onCloseChatQuote();
            }

            @Override
            public void onSubtitleUpdate(List<PLVLiveSubtitleTranslation> subtitles) {
                livePageMenuLayout.onSubtitleUpdate(subtitles);
            }

            @Override
            public void onShowBulletinAction() {
                if (liveRoomDataManager.getConfig().isLive() && popoverLayout != null) {
                    popoverLayout.getInteractLayout().showBulletin();
                }
            }

            @Override
            public void onShowRewardAction() {
                if (liveRoomDataManager.getConfig().isLive() && popoverLayout != null) {
                    popoverLayout.getRewardView().showPointRewardDialog(true);
                }
            }

            @Override
            public void onSendLikesAction() {
                livePageMenuLayout.getChatroomPresenter().sendLikeMessage();
            }

            @Override
            public void onPPTTurnPage(String type) {
                if (floatingPPTLayout != null && floatingPPTLayout.getPPTView() != null) {
                    floatingPPTLayout.getPPTView().turnPagePPT(type);
                }
            }

            @Override
            public void onWatchLowLatency(boolean watchLowLatency) {
                floatingPPTLayout.setIsLowLatencyWatch(watchLowLatency);
                if (linkMicLayout != null) {
                    linkMicLayout.setWatchLowLatency(watchLowLatency);
                }
            }

            @Override
            public void onRtcPauseResume(boolean toPause) {
                if (linkMicLayout == null) {
                    return;
                }
                if (toPause) {
                    linkMicLayout.pause();
                } else {
                    linkMicLayout.resume();
                }
            }

            @Override
            public boolean isRtcPausing() {
                return linkMicLayout.isPausing();
            }

            @Override
            public void onPaintModeChanged(boolean isInPaintMode) {
                if (floatingPPTLayout == null || floatingPPTLayout.getPPTView() == null) {
                    return;
                }
                if (isInPaintMode && linkMicLayout != null && linkMicLayout.isMediaShowInLinkMicList()) {
                    linkMicLayout.switchMediaToMainScreen();
                }
                floatingPPTLayout.getPPTView().notifyPaintModeStatus(isInPaintMode);
            }

            @Override
            public void onPaintMarkToolChanged(PLVLCMarkToolEnums.MarkTool markTool) {
                if (floatingPPTLayout == null || floatingPPTLayout.getPPTView() == null) {
                    return;
                }
                floatingPPTLayout.getPPTView().notifyPaintMarkToolChanged(markTool);
            }

            @Override
            public void onPaintMarkToolColorChanged(PLVLCMarkToolEnums.Color color) {
                if (floatingPPTLayout == null || floatingPPTLayout.getPPTView() == null) {
                    return;
                }
                floatingPPTLayout.getPPTView().notifyPaintMarkToolColorChanged(color);
            }

            @Override
            public void onPaintUndo() {
                if (floatingPPTLayout == null || floatingPPTLayout.getPPTView() == null) {
                    return;
                }
                floatingPPTLayout.getPPTView().notifyUndoLastPaint();
            }

            @Override
            public void onFinishChangeTextContent(String content) {
                if (floatingPPTLayout == null || floatingPPTLayout.getPPTView() == null) {
                    return;
                }
                floatingPPTLayout.getPPTView().notifyPaintUpdateTextContent(content);
            }

            @Override
            public void onShowJobDetail(PLVShowJobDetailEvent param) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().onShowJobDetail(param);
                }
            }

            @Override
            public void onShowLandscapeMemberList() {
                if (PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                        .isFeatureSupport(PLVChannelFeature.LIVE_SHOW_VIEWER_LIST)) {
                    if (chatLandscapeLayout != null) {
                        chatLandscapeLayout.showLandscapeMemberList();
                    }
                }
            }
        });

        //当前页面 监听 播放器数据中的PPT是否显示状态
        mediaLayout.addOnPPTShowStateListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isPptVisible) {
                if (isPptVisible == null) {
                    return;
                }
                floatingPPTLayout.setServerEnablePPT(isPptVisible);
            }
        });
        //监听播放状态
        mediaLayout.addOnPlayerStateListener(new IPLVOnDataChangedListener<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState playerState) {
                if (playerState == null) {
                    return;
                }
                if (liveRoomDataManager.getConfig().isLive()) {
                    //处理直播播放状态
                    switch (playerState) {
                        case PREPARED:
                            floatingPPTLayout.show();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.LIVE);
                            if (linkMicLayout != null) {
                                linkMicLayout.showAll();
                            }
                            floatingWindow.updateWhenPlayerPrepared(true);
                            break;
                        case LIVE_STOP:
                            if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                                //对于三分屏频道，直播结束，将PPT和播放器各自切回到各自的位置
                                if (!floatingPPTLayout.isPPTInFloatingLayout()) {
                                    pptViewSwitcher.switchView();
                                }
                            }
                            floatingPPTLayout.hide();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.STOP);
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            break;
                        case NO_LIVE:
                        case LIVE_END:
                            if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                                //对于三分屏频道，直播结束，将PPT和播放器各自切回到各自的位置
                                if (!floatingPPTLayout.isPPTInFloatingLayout()) {
                                    pptViewSwitcher.switchView();
                                }
                            }
                            floatingPPTLayout.hide();
                            livePageMenuLayout.updateLiveStatus(PLVLiveStateEnum.END);
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            floatingWindow.updateWhenPlayerPrepared(false);
                            break;
                        default:
                            break;
                    }
                } else {
                    //处理回放播放状态
                    switch (playerState) {
                        case PREPARED:
                            floatingPPTLayout.show();
                            livePageMenuLayout.onPlaybackVideoPrepared(mediaLayout.getSessionId(), liveRoomDataManager.getConfig().getChannelId(), mediaLayout.getFileId());
                            livePageMenuLayout.onAISummaryPrepared(mediaLayout.getPlayDataId(), mediaLayout.getPlayDataType());
                            floatingWindow.updateWhenPlayerPrepared(true);
                            break;
                        case IDLE:
                            floatingPPTLayout.hide();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        if (liveRoomDataManager.getConfig().isLive()) {
            //监听直播回调：

            //当前页面 监听 播放器数据中的连麦的->是否开启状态，连麦类型
            mediaLayout.addOnLinkMicStateListener(new IPLVOnDataChangedListener<Pair<Boolean, Boolean>>() {
                @Override
                public void onChanged(@Nullable Pair<Boolean/*连麦是否打开*/, Boolean/*是否是音频连麦*/> linkMicState) {
                    if (linkMicState == null) {
                        return;
                    }
                    boolean isLinkMicOpen = linkMicState.first;
                    boolean isAudio = linkMicState.second;
                    if (!isLinkMicOpen) {
                        floatingWindow.updateWhenJoinRequestLinkMic(false);
                    }
                    if (linkMicLayout == null) {
                        return;
                    }
                    linkMicLayout.setIsAudio(isAudio);
                    linkMicLayout.setIsTeacherOpenLinkMic(isLinkMicOpen);
                }
            });
            //当前页面 监听 播放器数据中的sei数据
            mediaLayout.addOnSeiDataListener(new IPLVOnDataChangedListener<Long>() {
                @Override
                public void onChanged(@Nullable Long aLong) {
                    if (aLong == null) {
                        return;
                    }
                    floatingPPTLayout.getPPTView().sendSEIData(aLong);
                }
            });
            mediaLayout.setOnRTCPlayEventListener(new IPolyvLiveListenerEvent.OnRTCPlayEventListener() {
                @Override
                public void onRTCLiveStart() {
                    if (linkMicLayout != null) {
                        linkMicLayout.setLiveStart();
                    }
                }

                @Override
                public void onRTCLiveEnd() {
                    if (linkMicLayout != null) {
                        linkMicLayout.setLiveEnd();
                    }
                }
            });
        } else {
            //监听回放回调：

            mediaLayout.addOnPlayInfoVOListener(new IPLVOnDataChangedListener<PLVPlayInfoVO>() {
                @Override
                public void onChanged(@Nullable PLVPlayInfoVO plvPlayInfoVO) {
                    if (plvPlayInfoVO == null) {
                        return;
                    }
                    //将当前回放视频的进度保存到PPT中
                    floatingPPTLayout.getPPTView().setPlaybackCurrentPosition(plvPlayInfoVO.getPosition());

                    //页面菜单更新当前进度，通知往期、章节页面更新
                    if (livePageMenuLayout.getPreviousPresenter() != null) {
                        livePageMenuLayout.getPreviousPresenter().updatePlaybackCurrentPosition(plvPlayInfoVO);
                    }
                }
            });

            mediaLayout.addOnSeekCompleteListener(new IPLVOnDataChangedListener<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    if (integer == null) {
                        return;
                    }
                    livePageMenuLayout.onPlaybackVideoSeekComplete(integer);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 页面菜单">
    private void observePageMenuLayout() {
        //设置view事件监听器
        livePageMenuLayout.setOnViewActionListener(new IPLVLCLivePageMenuLayout.OnViewActionListener() {
            @Override
            public void onShowBulletinAction() {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showBulletin();
                }
            }

            @Override
            public void onSendDanmuAction(CharSequence message) {
                mediaLayout.sendDanmaku(message);
            }

            @Override
            public void onChangeVideoVidAction(String vid, String fileId) {
                mediaLayout.updatePlayBackVideVidAndPlay(vid, fileId);
            }

            @Override
            public void onSeekToAction(int progress) {
                mediaLayout.seekTo(progress * 1000, mediaLayout.getDuration());
            }

            @Override
            public int getVideoCurrentPosition() {
                return mediaLayout.getVideoCurrentPosition();
            }

            @Override
            public void onChatTabPrepared(boolean isChatPlaybackEnabled, boolean isDisplayEnabled) {
                if (chatLandscapeLayout != null) {
                    chatLandscapeLayout.setIsDisplayEnabled(isDisplayEnabled);
                    chatLandscapeLayout.setIsChatPlaybackLayout(isChatPlaybackEnabled);
                    chatLandscapeLayout.setIsLiveType(liveRoomDataManager.getConfig().isLive());
                    livePageMenuLayout.getChatPlaybackManager().addOnCallDataListener(chatLandscapeLayout.getChatPlaybackDataListener());
                    livePageMenuLayout.getChatroomPresenter().registerView(chatLandscapeLayout.getChatroomView());
                }
                if (mediaLayout != null) {
                    mediaLayout.setChatPlaybackEnabled(isChatPlaybackEnabled, liveRoomDataManager.getConfig().isLive());
                    mediaLayout.setChatIsDisplayEnabled(isDisplayEnabled);
                }
                if (toTopView != null) {
                    toTopView.setIsChatPlaybackLayout(isChatPlaybackEnabled);
                    toTopView.setIsLiveType(liveRoomDataManager.getConfig().isLive());
                    livePageMenuLayout.getChatPlaybackManager().addOnCallDataListener(toTopView.getChatPlaybackDataListener());
                    livePageMenuLayout.getChatroomPresenter().registerView(toTopView.getChatroomView());
                }
            }

            @Override
            public void onShowRewardAction() {
                if (popoverLayout != null) {
                    popoverLayout.getRewardView().showPointRewardDialog(true);
                }
            }

            @Override
            public void onShowEffectAction(boolean isShow) {
                //控制横屏时的打赏特效显示
                if (mediaLayout != null) {
                    mediaLayout.setLandscapeRewardEffectVisibility(isShow);
                }
            }

            @Override
            public void onShowQuestionnaire() {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showQuestionnaire();
                }
            }

            @Override
            public void onClickChatMoreDynamicFunction(String event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().onCallDynamicFunction(event);
                }
            }

            @Override
            public void onReplyMessage(PLVChatQuoteVO chatQuoteVO) {
                if (mediaLayout != null) {
                    mediaLayout.notifyOnReplyMessage(chatQuoteVO);
                }
            }

            @Override
            public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().receiveRedPaper(redPaperEvent);
                }
            }

            @Override
            public void onShowJobDetail(PLVShowJobDetailEvent param) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().onShowJobDetail(param);
                }
            }

            @Override
            public void onShowProductDetail(PLVShowProductDetailEvent param) {
                if (popoverLayout != null) {
                    popoverLayout.getProductDetailLayout().showProductDetail(param.getProductId());
                }
            }

            @Override
            public void onShowOpenLink() {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().onShowOpenLink();
                }
            }

            @Override
            public void onScreenshot() {
                screenshotHelper.startScreenCapture(PLVLCCloudClassActivity.this);
            }

            @Override
            public void onSetSubtitleTranslateLanguage(String language) {
                mediaLayout.updateSubtitleTranslateLanguage(language);
            }
        });
        //当前页面 监听 聊天室数据中的观看热度变化
        livePageMenuLayout.addOnViewerCountListener(new IPLVOnDataChangedListener<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                mediaLayout.updateViewerCount(l);
            }
        });

        // 当前页面 监听 聊天室在线人数变化
        livePageMenuLayout.addOnViewOnlineCountData(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                mediaLayout.updateViewOnlineCount(integer);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - PPT">
    private void observePPTView() {
        //设置悬浮窗点击监听器
        floatingPPTLayout.setOnFloatingViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pptViewSwitcher.switchView();
                initPptTurnPageLandLayout();
            }
        });
        //设置关闭悬浮窗的点击监听器
        floatingPPTLayout.setOnClickCloseListener(new IPLVLCFloatingPPTLayout.IPLVOnClickCloseFloatingView() {
            @Override
            public void onClickCloseFloatingView() {
                mediaLayout.updateOnClickCloseFloatingView();
            }
        });
        if (liveRoomDataManager.getConfig().isLive()) {
            //设置直播PPT事件监听器
            floatingPPTLayout.getPPTView().initLivePPT(new IPLVLCPPTView.OnPLVLCLivePPTViewListener() {

                @Override
                public void onLiveSwitchPPTViewLocation(boolean toMainScreen) {
                    if (!liveRoomDataManager.getConfig().isPPTChannelType()) {
                        return;
                    }

                    //ppt翻页控件显示隐藏处理
                    mediaLayout.onTurnPageLayoutChange(toMainScreen);

                    if (linkMicLayout == null || !linkMicLayout.isJoinChannel()) {
                        if (toMainScreen) {
                            if (!pptViewSwitcher.isViewSwitched()) {
                                pptViewSwitcher.switchView();
                            }
                        } else {
                            if (pptViewSwitcher.isViewSwitched()) {
                                pptViewSwitcher.switchView();
                            }
                        }
                    }
                }

                @Override
                public void onLiveChangeToLandscape(boolean toLandscape) {
                    if (toLandscape) {
                        PLVOrientationManager.getInstance().setLandscape(PLVLCCloudClassActivity.this);
                    } else {
                        PLVOrientationManager.getInstance().setPortrait(PLVLCCloudClassActivity.this);
                    }
                }

                @Override
                public void onLiveStartOrPauseVideoView(boolean toStart) {
                    if (toStart) {
                        mediaLayout.startPlay();
                    } else {
                        mediaLayout.stop();
                    }
                }

                @Override
                public void onLiveRestartVideoView() {
                    mediaLayout.startPlay();
                }

                @Override
                public void onLiveBackTopActivity() {
                    if (ScreenUtils.isLandscape()) {
                        PLVOrientationManager.getInstance().setPortrait(PLVLCCloudClassActivity.this);
                    } else {
                        finish();
                    }
                }

                @Override
                public void onLivePPTStatusChange(PLVPPTStatus plvpptStatus) {
                    //更新PPT状态
                    if (mediaLayout != null) {
                        mediaLayout.updatePPTStatusChange(plvpptStatus);
                    }
                }

                @Override
                public void onPaintEditText(PLVPPTPaintStatus paintStatus) {
                    if (mediaLayout != null) {
                        mediaLayout.onPaintEditText(paintStatus);
                    }
                }
            });
        } else {
            //设置回放PPT事件监听
            floatingPPTLayout.getPPTView().initPlaybackPPT(new IPLVLCPPTView.OnPLVLCPlaybackPPTViewListener() {
                @Override
                public void onPlaybackSwitchPPTViewLocation(boolean toMainScreen) {
                    if (toMainScreen) {
                        if (!pptViewSwitcher.isViewSwitched()) {
                            pptViewSwitcher.switchView();
                        }
                    } else {
                        if (pptViewSwitcher.isViewSwitched()) {
                            pptViewSwitcher.switchView();
                        }
                    }
                }
            });
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 连麦">
    private void observeLinkMicLayout() {
        if (!liveRoomDataManager.getConfig().isLive() || linkMicLayout == null) {
            return;
        }
        //连麦 View 和主屏幕的切换器
        final PLVViewSwitcher linkMicItemSwitcher = new PLVViewSwitcher();
        linkMicLayout.setLogoView(plvPlayerLogoView);
        //设置连麦布局监听器
        linkMicLayout.setOnPLVLinkMicLayoutListener(new IPLVLCLinkMicLayout.OnPLVLinkMicLayoutListener() {

            @Nullable
            @Override
            public ViewGroup onRequireMixStreamVideoContainer() {
                if (mediaLayout == null) {
                    return null;
                }
                return mediaLayout.getRtcMixStreamContainer();
            }

            @Override
            public void onStartRtcWatch() {
                //更新PPT的延迟时间为0
                floatingPPTLayout.getPPTView().notifyStartRtcWatch();
                //更新播放器布局
                mediaLayout.updateWhenStartRtcWatch(linkMicLayout.getLandscapeWidth());
            }

            @Override
            public void onStopRtcWatch() {
                //重置PPT延迟时间
                floatingPPTLayout.getPPTView().notifyStopRtcWatch();
                //更新播放器布局
                mediaLayout.updateWhenLeaveRtcWatch();
            }

            @Override
            public void onStartPureRtcWatch() {
                if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                    //对于三分屏频道，如果PPT此时还在悬浮窗，则将PPT从悬浮窗切到主屏幕，将播放器从主屏幕切到悬浮窗
                    if (floatingPPTLayout.isPPTInFloatingLayout()) {
                        pptViewSwitcher.switchView();
                        linkMicLayout.notifySwitchedPptToMainScreenOnJoinChannel();
                    }
                }
                //隐藏悬浮窗
                floatingPPTLayout.hide();
            }

            @Override
            public void onStopPureRtcWatch() {
                //显示悬浮窗
                floatingPPTLayout.show();
            }

            @Override
            public void onChannelLinkMicOpenStatusChanged(boolean isOpen) {
                mediaLayout.updateWhenLinkMicOpenStatusChanged(isOpen);
                if (!isOpen) {
                    floatingWindow.updateWhenJoinRequestLinkMic(false);
                }
            }

            @Override
            public void onRequestJoinLinkMic() {
                mediaLayout.updateWhenRequestJoinLinkMic(true);
                floatingWindow.updateWhenJoinRequestLinkMic(true);
            }

            @Override
            public void onCancelRequestJoinLinkMic() {
                mediaLayout.updateWhenRequestJoinLinkMic(false);
                floatingWindow.updateWhenJoinRequestLinkMic(false);
            }

            @Override
            public void onJoinLinkMic() {
                mediaLayout.updateWhenJoinLinkMic();
                floatingWindow.updateWhenJoinLinkMic(true);
            }

            @Override
            public void onLeaveLinkMic() {
                mediaLayout.updateWhenLeaveLinkMic();
                floatingWindow.updateWhenJoinLinkMic(false);
            }

            @Override
            public void onShowLandscapeRTCLayout(boolean show) {
                if (show) {
                    mediaLayout.setShowLandscapeRTCLayout();
                } else {
                    mediaLayout.setHideLandscapeRTCLayout();
                }
            }

            @Override
            public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
                mediaLayout.acceptNetworkQuality(quality);
            }

            @Override
            public void onChangeTeacherLocation(PLVViewSwitcher viewSwitcher, PLVSwitchViewAnchorLayout switchView) {
                viewSwitcher.registerSwitchView(switchView, mediaLayout.getPlayerSwitchView());
                viewSwitcher.switchView();
                mediaLayout.getPlayerSwitchView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaLayout != null && mediaLayout.getPlayerSwitchView() != null) {
                            //兼容 constraint-layout 升级到 2.0.0+ 出现的无延迟黑屏问题
                            mediaLayout.getPlayerSwitchView().requestLayout();
                        }
                    }
                });
            }

            @Override
            public void onClickSwitchWithMediaOnce(PLVSwitchViewAnchorLayout switchView) {
                linkMicItemSwitcher.registerSwitchView(switchView, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();

                checkMediaViewPosition();
            }

            @Override
            public void onClickSwitchWithMediaTwice(PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
                //先将PPT从连麦列表切到主屏幕
                linkMicItemSwitcher.registerSwitchView(switchViewHasMedia, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();

                //再将要切到主屏幕的item和PPT交换位置
                linkMicItemSwitcher.registerSwitchView(switchViewGoMainScreen, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();

                checkMediaViewPosition();
            }

            @Override
            public void onRTCPrepared() {
                mediaLayout.notifyRTCPrepared();
            }

            @Override
            public boolean isInPaintMode() {
                if (mediaLayout != null) {
                    return mediaLayout.isInPaintMode();
                }
                return false;
            }

            private void checkMediaViewPosition() {
                postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (linkMicLayout == null || mediaLayout == null) {
                            return;
                        }
                        mediaLayout.notifyMediaLayoutPosition(linkMicLayout.isMediaShowInLinkMicList());
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转处理">
    @Override
    protected boolean enableRotationObserver() {
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(PLVLanguageUtil.setToConfiguration(newConfig, this));
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PLVScreenUtils.enterLandscape(this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        } else {
            PLVScreenUtils.enterPortrait(this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化ppt翻页控件">
    private void initPptTurnPageLandLayout() {
        if (floatingPPTLayout.isPPTInFloatingLayout()) {
            mediaLayout.onTurnPageLayoutChange(false);
        } else {
            mediaLayout.onTurnPageLayoutChange(true);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVMenuDrawerContainer">

    @Override
    @NotNull
    public ViewGroup menuDrawerContainer() {
        return findViewById(R.id.plvlc_popup_container);
    }

    // </editor-fold>
}
