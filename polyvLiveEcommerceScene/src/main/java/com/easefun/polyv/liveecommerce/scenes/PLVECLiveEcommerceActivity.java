package com.easefun.polyv.liveecommerce.scenes;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static com.plv.foundationsdk.utils.PLVSugarUtil.transformList;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.di.PLVCommonModule;
import com.easefun.polyv.livecommon.module.modules.interact.PLVInteractLayout2;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.config.PLVPlaybackCacheConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config.PLVPlaybackCacheVideoConfig;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.popover.IPLVPopoverLayout;
import com.easefun.polyv.livecommon.module.modules.reward.OnPointRewardListener;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchViewPager;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityDetailActivity;
import com.easefun.polyv.liveecommerce.modules.linkmic.IPLVECLinkMicLayout;
import com.easefun.polyv.liveecommerce.modules.linkmic.PLVECLinkMicControlBar;
import com.easefun.polyv.liveecommerce.modules.player.IPLVECVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECLiveVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECPlaybackVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindowModule;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECCommonHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECEmptyFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveDetailFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECPalybackHomeFragment;
import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.video.api.IPolyvLiveListenerEvent;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowLotteryEvent;
import com.plv.socket.event.interact.PLVShowPushCardEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.user.PLVSocketUserConstant;

import java.io.File;
import java.util.List;

/**
 * 直播带货场景下定义的 直播模式、回放模式 的 共用界面。
 * 直播支持的功能有：播放器、聊天室、商品、打赏、互动应用。
 * 回放支持的功能有：播放器。
 */
public class PLVECLiveEcommerceActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVECLiveEcommerceActivity.class.getSimpleName();
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 观看者昵称
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//观看者头像地址
    private static final String EXTRA_VID = "vid";//视频Id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//回放列表类型
    private static final String EXTRA_MATERIAL_LIBRARY_ENABLED = "material_library_enabled";//素材库回放
    private static final String EXTRA_IS_LIVE = "is_live";//是否是直播
    private static final String EXTRA_CHANNEL_TYPE = "channel_type";//频道类型

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // 播放器布局
    private IPLVECVideoLayout videoLayout;
    // 连麦布局
    @Nullable
    private IPLVECLinkMicLayout linkMicLayout;
    // logoView
    private PLVPlayerLogoView plvPlayerLogoView;
    // 布局 - 直播页面 - 上层的 viewpager 布局，包含的三个fragment
    private PLVNoInterceptTouchViewPager viewPager;
    // 位于左边的 直播间详情信息页 fragment
    private PLVECLiveDetailFragment liveDetailFragment;
    // 位于中间的 主页 fragment
    private PLVECCommonHomeFragment commonHomeFragment;
    // 位于右边的 空白页 fragment （该fragment用于清空左右信息，只看底层的视频）
    private PLVECEmptyFragment emptyFragment;
    //弹窗Layout
    private IPLVPopoverLayout popoverLayout;

    // 悬浮小窗
    private PLVECFloatingWindow floatingWindow;
    //是否用户手动关闭了浮窗
    private boolean isUserCloseFloatingWindow;
    //布局的手势操作
    protected GestureDetector gestureScanner;

    private ImageView closeIm;

    //当前频道是否支持横置全屏
    private boolean isCanFullScreen;

    //当前是否是直播状态
    private boolean isLive;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动直播带货直播页
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
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_live_error_activity_is_null));
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_live_error_channel_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_live_error_viewer_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_live_error_viewer_name_is_empty));
        }
        PLVLanguageUtil.checkOverrideLanguage(channelId, langType);
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
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
     * 启动直播带货回放页
     *
     * 如果没有输入vid的情况下会加载该频道的往期视频列表，如果输入vid的话就直接播放相应vid的视频，
     * 这样的话就不会加载往期视频列表
     * 若是想关闭不输入vid播放往期视频列表这个功能的话可以放开下面
     * PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_vid_is_empty))的注释
     *
     * @param activity      上下文Activity
     * @param channelId     频道号
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
                                                 @NonNull String vid,
                                                 @NonNull String viewerId,
                                                 @NonNull String viewerName,
                                                 @NonNull String viewerAvatar,
                                                 PLVPlaybackListType videoListType,
                                                 String langType,
                                                 boolean materialLibraryEnabled) {
        if (activity == null) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_activity_is_null));
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_channel_id_is_empty));
        }
//        if (TextUtils.isEmpty(vid)) {
//            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_vid_is_empty));
//        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_viewer_id_is_empty));
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error(PLVAppUtils.getString(R.string.plvec_login_playback_error_viewer_name_is_empty));
        }
        PLVLanguageUtil.checkOverrideLanguage(channelId, langType);
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
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
        setContentView(R.layout.plvec_live_ecommerce_page_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initOnClick();
        initFloatingWindowSetting();

        observeVideoLayout();
        observeLinkMicLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUserCloseFloatingWindow) {
            videoLayout.setPlayerVolume(100);
        }
        isUserCloseFloatingWindow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PLVFloatingPlayerManager.getInstance().runOnFloatingWindowClosed(new Runnable() {
            @Override
            public void run() {
                PLVLanguageUtil.detachLanguageActivity();
                PLVFloatingPlayerManager.getInstance().clear();
                if(popoverLayout != null){
                    popoverLayout.destroy();
                }
                if (videoLayout != null) {
                    videoLayout.destroy();
                }
                if (linkMicLayout != null) {
                    linkMicLayout.destroy();
                }
                if (liveRoomDataManager != null) {
                    liveRoomDataManager.destroy();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(popoverLayout != null && popoverLayout.onBackPress()){
            return;
        }
        if(PLVScreenUtils.isLandscape(this)){
            PLVOrientationManager.getInstance().setPortrait(this);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (popoverLayout != null) {
            popoverLayout.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected boolean enableRotationObserver() {
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(PLVLanguageUtil.setToConfiguration(newConfig, this));
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            closeIm.setVisibility(View.GONE);
            PLVScreenUtils.enterLandscape(this);
            floatingWindow.setLanderScreen(true);
        } else {
            PLVScreenUtils.exitFullScreen(this);
            closeIm.setVisibility(View.VISIBLE);
            floatingWindow.setLanderScreen(false);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="手势事件拦截">

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (videoLayout != null) {
            if (commonHomeFragment != null && !commonHomeFragment.isInterceptViewAction(ev)) {
                videoLayout.dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewPager != null) {
            viewPager.onSuperTouchEvent(event);
        }
        if (linkMicLayout != null) {
            linkMicLayout.onRvSuperTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 依赖注入">

    private void injectDependency() {
        PLVDependManager.getInstance().switchStore(this)
                .addModule(PLVCommonModule.instance)
                .addModule(PLVECFloatingWindowModule.instance);
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
        final String vid = intent.getStringExtra(EXTRA_VID);
        final PLVPlaybackListType videoListType = (PLVPlaybackListType) intent.getSerializableExtra(EXTRA_VIDEO_LIST_TYPE);
        final boolean materialLibraryEnabled = intent.getBooleanExtra(EXTRA_MATERIAL_LIBRARY_ENABLED, false);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setIsLive(isLive);
        PLVLiveChannelConfigFiller.setChannelType(channelType);
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, viewerAvatar,
                PolyvLinkMicConfig.getInstance().getLiveChannelType() == PolyvLiveChannelType.PPT
                        ? PLVSocketUserConstant.USERTYPE_SLICE : PLVSocketUserConstant.USERTYPE_STUDENT);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);

        PLVFloatingPlayerManager.getInstance().saveIntent(intent);
        // 根据不同模式，设置对应参数
        if (isLive) {
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_live");
        } else { // 回放模式
            PLVLiveChannelConfigFiller.setupVid(vid);
            PLVLiveChannelConfigFiller.setupVideoListType(videoListType != null ? videoListType : PLVPlaybackListType.PLAYBACK);
            PLVLiveChannelConfigFiller.setMaterialLibraryEnabled(materialLibraryEnabled);
            PLVFloatingPlayerManager.getInstance().setTag(channelId + "_" + (vid == null ? "playback" : vid));
        }

        initPlaybackParam(vid, channelId, viewerId, viewerName, viewerAvatar, PLVLiveChannelType.ALONE, videoListType);

        PLVOrientationManager.getInstance().lockOrientation();
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
        // 页面关闭按钮
        closeIm = findViewById(R.id.close_page_iv);
        findViewById(R.id.close_page_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 页面上层ViewPage
        viewPager = findViewById(R.id.watch_info_vp);
        initEmptyFragment();
        initLiveDetailFragment();
        initCommonHomeFragment();
        // 添加fragment到页面上层ViewPage中
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                viewPager,
                1,
                liveDetailFragment,
                commonHomeFragment,
                emptyFragment
        );

        if (liveRoomDataManager.getConfig().isLive()) {
            // 页面底层播放器
            videoLayout = new PLVECLiveVideoLayout(this);
            plvPlayerLogoView = videoLayout.getLogoView();

            // 连麦控制条
            ViewStub linkmicControllerViewStub = findViewById(R.id.plvec_ppt_linkmic_controller);
            PLVECLinkMicControlBar linkMicControlBar = (PLVECLinkMicControlBar) linkmicControllerViewStub.inflate();

            // 连麦布局
            ViewStub linkmicLayoutViewStub = findViewById(R.id.plvec_linkmic_viewstub);
            linkMicLayout = (IPLVECLinkMicLayout) linkmicLayoutViewStub.inflate();
            linkMicLayout.init(liveRoomDataManager, linkMicControlBar);
            linkMicLayout.hideAll();
        } else {
            // 页面底层播放器
            videoLayout = new PLVECPlaybackVideoLayout(this);
        }
        // 播放器容器
        FrameLayout videoContainer = findViewById(R.id.plvec_fl_video_container);
        // 添加播放器到容器中
        videoContainer.addView((View) videoLayout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        // 初始化播放器、播放
        videoLayout.init(liveRoomDataManager);
        //当前activity 可以手势操作暂停和播放
        initGesture();

        videoLayout.startPlay();

        final String vid = liveRoomDataManager.getConfig().getVid();
        final boolean isPlayback = !liveRoomDataManager.getConfig().isLive();
        if (isPlayback && TextUtils.isEmpty(vid)) {
            observePreviousPage();
        }
    }

    private void initOnClick(){
        closeIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //设置布局可单次点击或者多次点击
    private void initGesture() {
        gestureScanner = new GestureDetector(PLVECLiveEcommerceActivity.this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                PLVCommonLog.d(TAG,"onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                PLVCommonLog.d(TAG,"onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                PLVCommonLog.d(TAG,"onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (videoLayout.isSubVideoViewShow()) {
                    if (!videoLayout.getSubVideoViewHerf().isEmpty()) {
                        PLVWebUtils.openWebLink(videoLayout.getSubVideoViewHerf(), PLVECLiveEcommerceActivity.this);
                    }
                } else {
                    if (!videoLayout.isSubVideoViewShow()) {
                        if (!(linkMicLayout != null && linkMicLayout.isJoinChannel()) && !videoLayout.isPlaying()) {
                            videoResume();
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (videoLayout.isSubVideoViewShow()) {
                    return true;
                }
                if (linkMicLayout == null || (!linkMicLayout.isJoinChannel() && !linkMicLayout.isPlayRtcAsMixStream())) {
                    if (videoLayout.isPlaying()) {
                        videoPause();
                    } else {
                        videoResume();
                    }
                } else {
                    if (linkMicLayout.isPausing()) {
                        linkMicLayout.resume();
                    } else {
                        linkMicLayout.pause();
                    }
                }
                videoLayout.updatePlayCenterView();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (linkMicLayout != null && linkMicLayout.isJoinChannel()) {
                    return false;
                }
                return gestureScanner.onTouchEvent(event);
            }
        });
    }

    private void initEmptyFragment() {
        emptyFragment = new PLVECEmptyFragment();
    }

    private void initLiveDetailFragment() {
        liveDetailFragment = new PLVECLiveDetailFragment();
        // 设置liveDetailFragment的view事件监听器
        liveDetailFragment.setOnViewActionListener(liveDetailViewActionListener);
    }

    private void initCommonHomeFragment() {
        if (liveRoomDataManager.getConfig().isLive()) {
            // 页面上层的主页fragment
            commonHomeFragment = new PLVECLiveHomeFragment();
            // 设置view交互事件监听器
            commonHomeFragment.setOnViewActionListener(liveHomeViewActionListener);
        } else {
            // 页面上层的主页fragment
            commonHomeFragment = new PLVECPalybackHomeFragment();
            // 设置view交互事件监听器
            commonHomeFragment.setOnViewActionListener(playbackHomeViewActionListener);
        }
        // 设置LiveRoomDataManager
        commonHomeFragment.init(liveRoomDataManager);
        // 卡片推送
        commonHomeFragment.getCardPushManager().setOnCardEnterClickListener(new PLVCardPushManager.OnCardEnterClickListener() {
            @Override
            public void onClick(PLVShowPushCardEvent event) {
                if (popoverLayout != null) {
                    popoverLayout.getInteractLayout().showCardPush(event);
                }
            }
        });
        //抽奖挂件
        commonHomeFragment.getLotteryManager().setLotteryEnterClickListener(new PLVLotteryManager.OnLotteryEnterClickListener() {
            @Override
            public void onClick(PLVShowLotteryEvent event) {
                if (popoverLayout != null) {
                    //发送显示无条件抽奖
                    popoverLayout.getInteractLayout().showLottery(event);
                }

            }
        });
    }

    /**
     * 单个回放视频 - 暂存列表最新视频
     */
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
                    videoLayout.startPlay();
                }
            }
        });
    }

    /**
     * 列表回放
     */
    private void observePreviousPage() {
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable final PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                final List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenus = nullable(new PLVSugarUtil.Supplier<List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean>>() {
                    @Override
                    public List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> get() {
                        return statefulData.getData().getData().getChannelMenus();
                    }
                });
                final List<String> channelMenuTypes = transformList(channelMenus, new PLVSugarUtil.Function<PLVLiveClassDetailVO.DataBean.ChannelMenusBean, String>() {
                    @Override
                    public String apply(PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
                        return channelMenusBean.getMenuType();
                    }
                });
                boolean hasPreviousPage = channelMenuTypes != null && channelMenuTypes.contains(PLVLiveClassDetailVO.MENUTYPE_PREVIOUS);
                if (!hasPreviousPage) {
                    hasPreviousPage = liveRoomDataManager.getConfig().getVideoListType() == PLVPlaybackListType.VOD
                            && !liveRoomDataManager.getConfig().isLive()
                            && liveRoomDataManager.getConfig().getVid().isEmpty();
                }
                if (commonHomeFragment != null) {
                    commonHomeFragment.onHasPreviousPage(hasPreviousPage);
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化弹窗Layout - 积分打赏、互动应用">
    private void setupPopoverLayout(){
        if (popoverLayout == null) {
            ViewStub floatViewStub = findViewById(R.id.plvec_popover_layout);
            popoverLayout = (IPLVPopoverLayout) floatViewStub.inflate();
            popoverLayout.init(PLVLiveScene.ECOMMERCE, liveRoomDataManager);
            popoverLayout.setOnPointRewardListener(new OnPointRewardListener() {
                @Override
                public void pointRewardEnable(boolean enable) {
                    liveRoomDataManager.getPointRewardEnableData().postValue(PLVStatefulData.success(enable));
                }
            });
            popoverLayout.setOnOpenInsideWebViewListener(new PLVInteractLayout2.OnOpenInsideWebViewListener() {
                @Override
                public PLVInteractLayout2.OpenUrlParam onOpenWithParam(boolean isLandscape) {
                    ViewGroup containerView = findViewById(R.id.plvec_popup_container);
                    return new PLVInteractLayout2.OpenUrlParam((int) (containerView.getHeight() * 0.3f), containerView);
                }

                @Override
                public void onClosed() {
                }
            });
            popoverLayout.setOnClickProductListener(new PLVInteractLayout2.OnClickProductListener() {
                @Override
                public void onClickProduct(String link) {
                    if (!TextUtils.isEmpty(link)) {
                        PLVECCommodityDetailActivity.start(PLVECLiveEcommerceActivity.this, link);
                    }
                }
            });
            popoverLayout.getInteractLayout().updateOrientationLock(true);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化 - 悬浮小窗配置">
    private void initFloatingWindowSetting() {
        floatingWindow = PLVDependManager.getInstance().get(PLVECFloatingWindow.class);
        floatingWindow.bindContentView(videoLayout.getPlayerSwitchAnchorLayout());
        floatingWindow.setLiveRoomData(liveRoomDataManager);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听业务数据">
    private void observerDataToLiveDetailFragment() {
        // 当前页面 监听 直播间数据管理器对象中的直播详情数据变化
        liveRoomDataManager.getClassDetailVO().observe(this, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> liveClassDetailVO) {
                if (liveClassDetailVO != null && liveClassDetailVO.isSuccess()) {
                    liveDetailFragment.setClassDetailVO(liveClassDetailVO.getData());
                }
            }
        });
        // 当前页面 监听 聊天室数据对象中的公告数据变化
        commonHomeFragment.runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                commonHomeFragment.getBulletinVO().observe(PLVECLiveEcommerceActivity.this, new Observer<PolyvBulletinVO>() {
                    @Override
                    public void onChanged(@Nullable PolyvBulletinVO bulletinVO) {
                        liveDetailFragment.setBulletinVO(bulletinVO);
                    }
                });
            }
        });
    }

    private void observerDataToLiveHomeFragment() {
        // 当前页面 监听 播放器数据对象中的播放状态变化
        videoLayout.getPlayerState().observe(this, new Observer<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState state) {
                commonHomeFragment.setPlayerState(state);
            }
        });
    }

    private void observerDataToPlaybackHomeFragment() {
        // 当前页面 监听 播放器数据对象中的播放状态变化
        videoLayout.getPlayerState().observe(this, new Observer<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState state) {
                commonHomeFragment.setPlayerState(state);
                if (PLVPlayerState.PREPARED.equals(state)) {
                    commonHomeFragment.onPlaybackVideoPrepared(videoLayout.getSessionId(), liveRoomDataManager.getConfig().getChannelId(), videoLayout.getFileId());
                }
            }
        });

        // 当前页面 监听 播放器数据的seek完成状态变化
        videoLayout.addOnSeekCompleteListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                commonHomeFragment.onPlaybackVideoSeekComplete(integer);
            }
        });

        // 当前页面 监听 回放播放器数据对象中的播放信息变化
        if (videoLayout.getPlaybackPlayInfoVO() != null) {
            videoLayout.getPlaybackPlayInfoVO().observe(this, new Observer<PLVPlayInfoVO>() {
                @Override
                public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                    commonHomeFragment.setPlaybackPlayInfo(playInfoVO);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 播放器、连麦">
    private void observeVideoLayout() {
        videoLayout.setOnViewActionListener(new IPLVECVideoLayout.OnViewActionListener() {
            @Override
            public void onCloseFloatingAction() {
                //主动关闭浮窗时，播放器静音
                videoLayout.setPlayerVolume(0);
                isUserCloseFloatingWindow = true;
            }

            @Override
            public void onShowMoreLayoutAction() {
                if (commonHomeFragment != null) {
                    commonHomeFragment.showMorePopupWindow();
                }
            }

            @Override
            public void acceptOnLowLatencyChange(boolean isLowLatency) {
                if (commonHomeFragment != null) {
                    commonHomeFragment.acceptOnLowLatencyChange(isLowLatency);
                }
                if (linkMicLayout != null) {
                    linkMicLayout.setWatchLowLatency(isLowLatency);
                }
            }

            @Override
            public void acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality networkQuality) {
                if (commonHomeFragment != null) {
                    commonHomeFragment.acceptNetworkQuality(networkQuality);
                }
            }

            @Override
            public void acceptVideoSize(boolean isCanFullScreen) {
                PLVECLiveEcommerceActivity.this.isCanFullScreen = isCanFullScreen;
                //根据视频的宽高来决定是否可以全屏
                if (isCanFullScreen) {
                    PLVOrientationManager.getInstance().unlockOrientation();
                } else {
                    PLVOrientationManager.getInstance().lockOrientation();;
                }
                if (!isCanFullScreen && PLVScreenUtils.isLandscape(PLVECLiveEcommerceActivity.this)) {
                    PLVOrientationManager.getInstance().unlockOrientation();
                }
            }

            @Override
            public boolean isPlayRtcAsMixStream() {
                return linkMicLayout != null && linkMicLayout.isPlayRtcAsMixStream();
            }

            @Override
            public boolean isRtcMixStreamPlaying() {
                return linkMicLayout != null && linkMicLayout.isPlayRtcAsMixStream() && !linkMicLayout.isPausing();
            }
        });
        //监听播放状态
        videoLayout.addOnPlayerStateListener(new IPLVOnDataChangedListener<PLVPlayerState>() {
            @Override
            public void onChanged(@Nullable PLVPlayerState playerState) {
                if (playerState == null) {
                    return;
                }
                if (liveRoomDataManager.getConfig().isLive()) {
                    //处理直播播放状态
                    switch (playerState) {
                        case PREPARED:
                            if (linkMicLayout != null) {
                                linkMicLayout.showAll();
                            }
                            isLive = true;
                            break;
                        case LIVE_STOP:
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            //如果是可以旋转的频道，停止直播时，禁止旋转
                            if (isCanFullScreen && PLVScreenUtils.isPortrait(PLVECLiveEcommerceActivity.this)) {
                                isCanFullScreen = false;
                                PLVOrientationManager.getInstance().lockOrientation();
                            }
                            isLive = false;
                            break;
                        case NO_LIVE:
                        case LIVE_END:
                            if (linkMicLayout != null) {
                                linkMicLayout.setLiveEnd();
                                linkMicLayout.hideAll();
                            }
                            //如果是可以旋转的频道，停止直播时，禁止旋转
                            if (isCanFullScreen && PLVScreenUtils.isPortrait(PLVECLiveEcommerceActivity.this)) {
                                isCanFullScreen = false;
                                PLVOrientationManager.getInstance().lockOrientation();
                            }
                            isLive = false;
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
            videoLayout.addOnLinkMicStateListener(new IPLVOnDataChangedListener<Pair<Boolean, Boolean>>() {
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
                    linkMicLayout.setIsAudio(isAudio);
                    linkMicLayout.setIsTeacherOpenLinkMic(isLinkMicOpen);
                }
            });
            videoLayout.setOnRTCPlayEventListener(new IPolyvLiveListenerEvent.OnRTCPlayEventListener() {
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
        }
    }

    private void observeLinkMicLayout() {
        if (!liveRoomDataManager.getConfig().isLive() || linkMicLayout == null) {
            return;
        }
        linkMicLayout.setLogoView(plvPlayerLogoView);
        //设置连麦布局监听器
        linkMicLayout.setOnPLVLinkMicLayoutListener(new IPLVECLinkMicLayout.OnPLVLinkMicLayoutListener() {

            @Nullable
            @Override
            public ViewGroup onRequireMixStreamVideoContainer() {
                if (videoLayout == null) {
                    return null;
                }
                return videoLayout.getRtcMixStreamContainer();
            }

            @Override
            public void onJoinRtcChannel() {
                //更新播放器布局
                videoLayout.updateWhenJoinRTC(linkMicLayout.getLandscapeWidth());
                if (commonHomeFragment != null) {
                    commonHomeFragment.setJoinRTCChannel(true);
                }
            }

            @Override
            public void onLeaveRtcChannel() {
                //更新播放器布局
                videoLayout.updateWhenLeaveRTC();
                if (PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing()) {
                    PLVFloatingPlayerManager.getInstance().hide();
                    floatingWindow.setRequestShowByUser(false);
                }
                floatingWindow.bindContentView(videoLayout.getPlayerSwitchAnchorLayout());
                if (commonHomeFragment != null) {
                    commonHomeFragment.setJoinRTCChannel(false);
                }
            }

            @Override
            public void onChannelLinkMicOpenStatusChanged(boolean isOpen) {
            }

            @Override
            public void onRequestJoinLinkMic() {
            }

            @Override
            public void onCancelRequestJoinLinkMic() {
            }

            @Override
            public void onJoinLinkMic() {
                if (isCanFullScreen) {
                    //在连麦期间不允许自动旋转屏幕
                    PLVOrientationManager.getInstance().lockOrientation();
                }
                videoLayout.updateWhenJoinLinkMic();
                if (commonHomeFragment != null) {
                    commonHomeFragment.setJoinLinkMic(true);
                }
            }

            @Override
            public void onLeaveLinkMic() {
                if (isCanFullScreen) {
                    PLVOrientationManager.getInstance().unlockOrientation();
                }
                videoLayout.updateWhenLeaveLinkMic();
                if (commonHomeFragment != null) {
                    commonHomeFragment.setJoinLinkMic(false);
                }
            }

            @Override
            public void onShowLandscapeRTCLayout(boolean show) {
            }

            @Override
            public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
                if (commonHomeFragment != null) {
                    commonHomeFragment.acceptNetworkQuality(quality);
                }
            }

            @Override
            public void onChangeTeacherLocation(PLVViewSwitcher viewSwitcher, PLVSwitchViewAnchorLayout switchView) {
            }

            @Override
            public void onClickSwitchWithMediaOnce(PLVSwitchViewAnchorLayout switchView) {
            }

            @Override
            public void onClickSwitchWithMediaTwice(PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
            }

            @Override
            public void onRTCPrepared() {
                videoLayout.notifyRTCPrepared();
            }

            @Override
            public boolean isInPaintMode() {
                return false;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="匿名内部类 - view交互事件监听器">
    private void videoResume() {
        videoLayout.resume();
    }

    private void videoPause() {
        videoLayout.pause();
    }

    private PLVECLiveDetailFragment.OnViewActionListener liveDetailViewActionListener = new PLVECLiveDetailFragment.OnViewActionListener() {
        @Override
        public void onViewCreated() {
            observerDataToLiveDetailFragment();
        }
    };

    private PLVECLiveHomeFragment.OnViewActionListener liveHomeViewActionListener = new PLVECLiveHomeFragment.OnViewActionListener() {
        @Override
        public void onChangeMediaPlayModeClick(View view, int mediaPlayMode) {
            videoLayout.changeMediaPlayMode(mediaPlayMode);
        }

        @Override
        public void onChangeLinesClick(View view, int linesPos) {
            videoLayout.changeLines(linesPos);
        }

        @Override
        public Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view) {
            return new Pair<>(videoLayout.getBitrateVO(), videoLayout.getBitratePos());
        }

        @Override
        public void onDefinitionChangeClick(View view, int definitionPos) {
            videoLayout.changeBitRate(definitionPos);
        }

        @Override
        public int onGetMediaPlayModeAction() {
            return videoLayout.getMediaPlayMode();
        }

        @Override
        public int onGetLinesCountAction() {
            return videoLayout.getLinesCount();
        }

        @Override
        public int onGetLinesPosAction() {
            return videoLayout.getLinesPos();
        }

        @Override
        public int onGetDefinitionAction() {
            return videoLayout.getBitratePos();
        }

        @Override
        public void onSetVideoViewRectAction(Rect videoViewRect) {
            videoLayout.setVideoViewRect(videoViewRect);
        }

        @Override
        public void onShowRewardAction() {
            if(popoverLayout != null){
                popoverLayout.getRewardView().showPointRewardDialog(true);
            }
        }

        @Override
        public void onShowQuestionnaire() {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().showQuestionnaire();
            }
        }

        @Override
        public boolean isCurrentLowLatencyMode() {
            return videoLayout.isCurrentLowLatencyMode();
        }

        @Override
        public void switchLowLatencyMode(boolean isLowLatency) {
            videoLayout.switchLowLatencyMode(isLowLatency);
        }

        @Override
        public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().receiveRedPaper(redPaperEvent);
            }
        }

        @Override
        public void onClickDynamicFunction(String event) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onCallDynamicFunction(event);
            }
        }

        @Override
        public void onShowJobDetail(PLVShowJobDetailEvent param) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onShowJobDetail(param);
            }
        }

        @Override
        public void onShowOpenLink() {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onShowOpenLink();
            }
        }

        @Override
        public void onViewCreated() {
            observerDataToLiveHomeFragment();
            setupPopoverLayout();
        }
    };

    private PLVECPalybackHomeFragment.OnViewActionListener playbackHomeViewActionListener = new PLVECPalybackHomeFragment.OnViewActionListener() {
        @Override
        public boolean onPauseOrResumeClick(View view) {
            if (!videoLayout.isSubVideoViewShow()) {
                if (videoLayout.isPlaying()) {
                    videoPause();
                    return false;
                } else {
                    videoResume();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onChangeSpeedClick(View view, float speed) {
            videoLayout.setSpeed(speed);
        }

        @Override
        public void onSeekToAction(int progress, int max) {
            videoLayout.seekTo(progress, max);
        }

        @Override
        public int onGetDurationAction() {
            return videoLayout.getDuration();
        }

        @Override
        public int getVideoCurrentPosition() {
            return videoLayout.getVideoCurrentPosition();
        }

        @Override
        public void onSetVideoViewRectAction(Rect videoViewRect) {
            videoLayout.setVideoViewRect(videoViewRect);
        }

        @Override
        public float onGetSpeedAction() {
            return videoLayout.getSpeed();
        }

        @Override
        public void onChangePlaybackVidAndPlay(String vid) {
            videoLayout.changePlaybackVidAndPlay(vid);
        }

        @Override
        public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().receiveRedPaper(redPaperEvent);
            }
        }

        @Override
        public void onClickDynamicFunction(String event) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onCallDynamicFunction(event);
            }
        }

        @Override
        public void onShowJobDetail(PLVShowJobDetailEvent param) {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onShowJobDetail(param);
            }
        }

        @Override
        public void onShowOpenLink() {
            if (popoverLayout != null) {
                popoverLayout.getInteractLayout().onShowOpenLink();
            }
        }

        @Override
        public void onViewCreated() {
            observerDataToPlaybackHomeFragment();
            setupPopoverLayout();
        }
    };
    // </editor-fold>

}
