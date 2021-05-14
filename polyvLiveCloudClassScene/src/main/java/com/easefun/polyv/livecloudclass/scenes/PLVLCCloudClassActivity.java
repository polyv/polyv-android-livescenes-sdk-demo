package com.easefun.polyv.livecloudclass.scenes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape.PLVLCChatLandscapeLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.IPLVLCLinkMicLayout;
import com.easefun.polyv.livecloudclass.modules.linkmic.PLVLCLinkMicControlBar;
import com.easefun.polyv.livecloudclass.modules.liveroom.PLVLCLiveLandscapeChannelController;
import com.easefun.polyv.livecloudclass.modules.media.IPLVLCMediaLayout;
import com.easefun.polyv.livecloudclass.modules.pagemenu.IPLVLCLivePageMenuLayout;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCFloatingPPTLayout;
import com.easefun.polyv.livecloudclass.modules.ppt.IPLVLCPPTView;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.interact.IPLVInteractLayout;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.PLVDialogFactory;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * date: 2020/10/12
 * author: HWilliamgo
 * 云课堂场景下定义的 直播模式、回放模式 的 共用界面。
 * 直播支持的功能有：播放器、页面菜单、悬浮PPT(三分屏频道独有)、连麦、互动应用。
 * 回放支持的功能有：播放器、页面菜单、悬浮PPT(三分屏频道独有)。
 */
public class PLVLCCloudClassActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="变量">
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 观看者昵称
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//观看者头像地址
    private static final String EXTRA_VID = "vid";//回放视频Id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//回放列表类型
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
    // 互动应用布局
    @Nullable
    private IPLVInteractLayout interactLayout;

    // 悬浮PPT布局 和 播放器布局 的切换器
    private PLVViewSwitcher pptViewSwitcher = new PLVViewSwitcher();
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
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchLive(@NonNull Activity activity,
                                             @NonNull String channelId,
                                             @NonNull PolyvLiveChannelType channelType,
                                             @NonNull String viewerId,
                                             @NonNull String viewerName,
                                             @NonNull String viewerAvatar) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动云课堂直播页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动云课堂直播页失败！");
        }
        if (channelType == null) {
            return PLVLaunchResult.error("channelType 为空，启动云课堂直播页失败！");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动云课堂直播页失败！");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动云课堂直播页失败！");
        }

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
     *
     * @param activity      上下文Activity
     * @param channelId     频道号
     * @param channelType   频道类型
     * @param vid           视频ID
     * @param viewerId      观众ID
     * @param viewerName    观众昵称
     * @param videoListType 回放视频类型，参考{@link PolyvPlaybackListType}
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchPlayback(@NonNull Activity activity,
                                                 @NonNull String channelId,
                                                 @NonNull PolyvLiveChannelType channelType,
                                                 @NonNull String vid,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String viewerAvatar,
                                                 int videoListType) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动云课堂回放页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动云课堂回放页失败！");
        }
        if (channelType == null) {
            return PLVLaunchResult.error("channelType 为空，启动云课堂回放页失败！");
        }
        if (TextUtils.isEmpty(vid)) {
            return PLVLaunchResult.error("vid 为空，启动云课堂回放页失败！");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动云课堂回放页失败！");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动云课堂回放页失败！");
        }

        Intent intent = new Intent(activity, PLVLCCloudClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_VIDEO_LIST_TYPE, videoListType);
        intent.putExtra(EXTRA_IS_LIVE, false);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvlc_cloudclass_activity);
        initParams();
        initLiveRoomManager();
        initView();

        observeMediaLayout();
        observeLinkMicLayout();
        observePageMenuLayout();
        observePPTView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaLayout != null) {
            mediaLayout.destroy();
        }
        if (linkMicLayout != null) {
            linkMicLayout.destroy();
        }
        if (livePageMenuLayout != null) {
            livePageMenuLayout.destroy();
        }
        if (interactLayout != null) {
            interactLayout.destroy();
        }
        if (floatingPPTLayout != null) {
            floatingPPTLayout.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (interactLayout != null && interactLayout.onBackPress()) {
            return;
        } else if (mediaLayout != null && mediaLayout.onBackPressed()) {
            return;
        } else if (livePageMenuLayout != null && livePageMenuLayout.onBackPressed()) {
            return;
        }

        //弹出退出直播间的确认框
        PLVDialogFactory.createConfirmDialog(
                this,
                getResources().getString(
                        liveRoomDataManager.getConfig().isLive()
                                ? R.string.plv_live_room_dialog_exit_confirm_ask
                                : R.string.plv_playback_room_dialog_exit_confirm_ask
                ),
                getResources().getString(R.string.plv_common_dialog_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PLVLCCloudClassActivity.super.onBackPressed();
                    }
                }
        ).show();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面参数">
    private void initParams() {
        // 获取输入数据
        Intent intent = getIntent();
        boolean isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true);
        PolyvLiveChannelType channelType = (PolyvLiveChannelType) intent.getSerializableExtra(EXTRA_CHANNEL_TYPE);
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        String viewerAvatar = intent.getStringExtra(EXTRA_VIEWER_AVATAR);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setIsLive(isLive);
        PLVLiveChannelConfigFiller.setChannelType(channelType);
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, viewerAvatar,
                PolyvLinkMicConfig.getInstance().getLiveChannelType() == PolyvLiveChannelType.PPT
                        ? PLVSocketUserConstant.USERTYPE_SLICE : PLVSocketUserConstant.USERTYPE_STUDENT);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);

        // 根据不同模式，设置对应参数
        if (!isLive) { // 回放模式
            String vid = intent.getStringExtra(EXTRA_VID);
            int videoListType = intent.getIntExtra(EXTRA_VIDEO_LIST_TYPE, PolyvPlaybackListType.PLAYBACK);
            PLVLiveChannelConfigFiller.setupVid(vid);
            PLVLiveChannelConfigFiller.setupVideoListType(videoListType);
        }
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

        // 页面菜单布局
        livePageMenuLayout = findViewById(R.id.plvlc_live_page_menu_layout);
        livePageMenuLayout.init(liveRoomDataManager);

        // 悬浮PPT布局
        floatingPPTLayout = findViewById(R.id.plvlc_ppt_floating_ppt_layout);

        if (liveRoomDataManager.getConfig().isLive()) {
            // 横屏频道控制器
            ViewStub landscapeChannelControllerViewStub = findViewById(R.id.plvlc_ppt_landscape_channel_controller);
            PLVLCLiveLandscapeChannelController liveLandscapeChannelController = (PLVLCLiveLandscapeChannelController) landscapeChannelControllerViewStub.inflate();

            // 播放器布局
            videoLyViewStub.setLayoutResource(R.layout.plvlc_live_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setLandscapeControllerView(liveLandscapeChannelController);
            mediaLayout.startPlay();

            // 连麦控制条
            ViewStub linkmicControllerViewStub = findViewById(R.id.plvlc_ppt_linkmic_controller);
            PLVLCLinkMicControlBar linkMicControlBar = (PLVLCLinkMicControlBar) linkmicControllerViewStub.inflate();

            // 连麦布局
            ViewStub linkmicLayoutViewStub = findViewById(R.id.plvlc_linkmic_viewstub);
            linkMicLayout = (IPLVLCLinkMicLayout) linkmicLayoutViewStub.inflate();
            linkMicLayout.init(liveRoomDataManager, linkMicControlBar);
            linkMicLayout.hideAll();

            // 互动应用布局
            ViewStub interactLayoutViewStub = findViewById(R.id.plvlc_ppt_interact_layout);
            interactLayout = (IPLVInteractLayout) interactLayoutViewStub.inflate();
            interactLayout.init();
        } else {
            // 播放器布局
            videoLyViewStub.setLayoutResource(R.layout.plvlc_playback_media_layout_view_stub);
            mediaLayout = (IPLVLCMediaLayout) videoLyViewStub.inflate();
            mediaLayout.init(liveRoomDataManager);
            mediaLayout.setPPTView(floatingPPTLayout.getPPTView().getPlaybackPPTViewToBindInPlayer());
            mediaLayout.startPlay();
        }

        // 初始化横屏聊天室布局
        PLVLCChatLandscapeLayout chatLandscapeLayout = mediaLayout.getChatLandscapeLayout();
        chatLandscapeLayout.init(livePageMenuLayout.getChatCommonMessageList());
        livePageMenuLayout.getChatroomPresenter().registerView(chatLandscapeLayout.getChatroomView());

        // 注册 悬浮PPT布局 和 播放器布局 的切换器
        pptViewSwitcher.registerSwitchVew(floatingPPTLayout.getPPTSwitchView(), mediaLayout.getPlayerSwitchView());

        // 初始化 屏幕方向
        if (ScreenUtils.isPortrait()) {
            PLVScreenUtils.enterPortrait(this);
        } else {
            PLVScreenUtils.enterLandscape(this);
        }
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
            public Pair<Boolean, Integer> onSendChatMessageAction(String message) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                return livePageMenuLayout.getChatroomPresenter().sendChatMessage(localMessage);
            }

            @Override
            public void onShowBulletinAction() {
                if (liveRoomDataManager.getConfig().isLive() && interactLayout != null) {
                    interactLayout.showBulletin();
                }
            }

            @Override
            public void onSendLikesAction() {
                livePageMenuLayout.getChatroomPresenter().sendLikeMessage();
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
                            livePageMenuLayout.updateLiveStatusWithLive();
                            if (linkMicLayout != null) {
                                linkMicLayout.showAll();
                            }
                            break;
                        case LIVE_STOP:
                        case NO_LIVE:
                        case LIVE_END:
                            if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                                //对于三分屏频道，直播结束，将PPT和播放器各自切回到各自的位置
                                if (!floatingPPTLayout.isPPTInFloatingLayout()) {
                                    pptViewSwitcher.switchView();
                                }
                            }
                            floatingPPTLayout.hide();
                            livePageMenuLayout.updateLiveStatusWithNoLive();
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    //处理回放播放状态
                    switch (playerState) {
                        case PREPARED:
                            floatingPPTLayout.show();
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
                    if (linkMicLayout == null) {
                        return;
                    }
                    linkMicLayout.setIsTeacherOpenLinkMic(isLinkMicOpen);
                    linkMicLayout.setIsAudio(isAudio);
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
                if (interactLayout != null) {
                    interactLayout.showBulletin();
                }
            }

            @Override
            public void onSendDanmuAction(CharSequence message) {
                mediaLayout.sendDanmaku(message);
            }
        });
        //当前页面 监听 聊天室数据中的观看热度变化
        livePageMenuLayout.addOnViewerCountListener(new IPLVOnDataChangedListener<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                if (l == null) {
                    return;
                }
                mediaLayout.updateViewerCount(l);
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
            });
        } else {
            //设置回放PPT事件监听
            floatingPPTLayout.getPPTView().initPlaybackPPT(new IPLVLCPPTView.OnPLVLCPlaybackPPTViewListener() {
                @Override
                public void onPlaybackSwitchPPTViewLocation(boolean toMainScreen) {
                    pptViewSwitcher.switchView();
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
        //设置连麦布局监听器
        linkMicLayout.setOnPLVLinkMicLayoutListener(new IPLVLCLinkMicLayout.OnPLVLinkMicLayoutListener() {
            @Override
            public void onJoinChannelSuccess() {
                if (liveRoomDataManager.getConfig().isPPTChannelType()) {
                    //对于三分屏频道，如果PPT此时还在悬浮窗，则将PPT从悬浮窗切到主屏幕，将播放器从主屏幕切到悬浮窗
                    if (floatingPPTLayout.isPPTInFloatingLayout()) {
                        pptViewSwitcher.switchView();
                    }
                }
                //隐藏悬浮窗
                floatingPPTLayout.hide();
                //更新PPT的延迟时间为0
                floatingPPTLayout.getPPTView().removeDelayTime();
                //更新播放器布局
                mediaLayout.updateWhenJoinRTC(linkMicLayout.getLandscapeWidth());
            }

            @Override
            public void onLeaveChannel() {
                //显示悬浮窗
                floatingPPTLayout.show();
                //重置PPT延迟时间
                floatingPPTLayout.getPPTView().recoverDelayTime();
                //更新播放器布局
                mediaLayout.updateWhenLeaveRTC();
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
            public void onChangeTeacherLocation(PLVViewSwitcher viewSwitcher, PLVSwitchViewAnchorLayout switchView) {
                viewSwitcher.registerSwitchVew(switchView, mediaLayout.getPlayerSwitchView());
                viewSwitcher.switchView();
                mediaLayout.getPlayerSwitchView().post(new Runnable() {
                    @Override
                    public void run() {
                        if(mediaLayout != null && mediaLayout.getPlayerSwitchView() != null) {
                            //兼容 constraint-layout 升级到 2.0.0+ 出现的无延迟黑屏问题
                            mediaLayout.getPlayerSwitchView().requestLayout();
                        }
                    }
                });
            }

            @Override
            public void onClickSwitchWithMediaOnce(PLVSwitchViewAnchorLayout switchView) {
                linkMicItemSwitcher.registerSwitchVew(switchView, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();
            }

            @Override
            public void onClickSwitchWithMediaTwice(PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
                //先将PPT从连麦列表切到主屏幕
                linkMicItemSwitcher.registerSwitchVew(switchViewHasMedia, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();

                //再将要切到主屏幕的item和PPT交换位置
                linkMicItemSwitcher.registerSwitchVew(switchViewGoMainScreen, mediaLayout.getPlayerSwitchView());
                linkMicItemSwitcher.switchView();
            }

            @Override
            public void onRTCPrepared() {
                mediaLayout.notifyRTCPrepared();
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
        super.onConfigurationChanged(newConfig);
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

}
