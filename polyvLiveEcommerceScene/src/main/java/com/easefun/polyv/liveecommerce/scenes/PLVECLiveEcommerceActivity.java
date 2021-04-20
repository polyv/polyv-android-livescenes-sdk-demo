package com.easefun.polyv.liveecommerce.scenes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.interact.IPLVInteractLayout;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.player.IPLVECVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECLiveVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.PLVECPlaybackVideoLayout;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindowBinder;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindowService;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECCommonHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECEmptyFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveDetailFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECLiveHomeFragment;
import com.easefun.polyv.liveecommerce.scenes.fragments.PLVECPalybackHomeFragment;
import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.livescenes.playback.video.PolyvPlaybackListType;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * 直播带货场景下定义的 直播模式、回放模式 的 共用界面。
 * 直播支持的功能有：播放器、聊天室、商品、打赏、互动应用。
 * 回放支持的功能有：播放器。
 */
public class PLVECLiveEcommerceActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVECLiveEcommerceActiv";
    private static final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 1;
    public static final String ACTION_FLOATING_WINDOW = "floatingWindow";
    public static final String EXTRA_IS_SHOW_FLOATING_WINDOW = "isShowFloatingWindow";
    public static final String EXTRA_IS_RESET_FLOATING_WINDOW_LOCATION = "isResetFloatingWindowLocation";
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 观看者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 观看者昵称
    private static final String EXTRA_VIEWER_AVATAR = "viewerAvatar";//观看者头像地址
    private static final String EXTRA_VID = "vid";//视频Id
    private static final String EXTRA_VIDEO_LIST_TYPE = "video_list_type";//回放列表类型
    private static final String EXTRA_IS_LIVE = "is_live";//是否是直播

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // 播放器布局
    private IPLVECVideoLayout videoLayout;
    // 布局 - 直播页面 - 上层的 viewpager 布局，包含的三个fragment
    private ViewPager viewPager;
    // 位于左边的 直播间详情信息页 fragment
    private PLVECLiveDetailFragment liveDetailFragment;
    // 位于中间的 主页 fragment
    private PLVECCommonHomeFragment commonHomeFragment;
    // 位于右边的 空白页 fragment （该fragment用于清空左右信息，只看底层的视频）
    private PLVECEmptyFragment emptyFragment;
    //互动应用View
    private IPLVInteractLayout interactLayout;

    //悬浮小窗服务连接
    private ServiceConnection floatingWindowConnection;
    //悬浮小窗binder
    private PLVECFloatingWindowBinder floatingWindowBinder;
    //悬浮小窗广播接收器
    private FloatingWindowReceiver floatingWindowReceiver;
    //是否用户手动关闭了浮窗
    private boolean isUserCloseFloatingWindow;
    //布局的手势操作
    protected GestureDetector gestureScanner;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动直播带货直播页
     *
     * @param activity   上下文Activity
     * @param channelId  频道号
     * @param viewerId   观众ID
     * @param viewerName 观众昵称
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchLive(@NonNull Activity activity, @NonNull String channelId, @NonNull String viewerId, @NonNull String viewerName,@NonNull String viewerAvatar) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动直播带货直播页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动直播带货直播页失败");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动直播带货直播页失败");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动直播带货直播页失败");
        }
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
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
     * @param activity      上下文Activity
     * @param channelId     频道号
     * @param vid           视频ID
     * @param viewerId      观众ID
     * @param viewerName    观众昵称
     * @param videoListType 回放视频类型，参考{@link PolyvPlaybackListType}
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchPlayback(@NonNull Activity activity, @NonNull String channelId, @NonNull String vid, @NonNull String viewerId, @NonNull String viewerName, @NonNull String viewerAvatar,int videoListType) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动直播带货回放页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动直播带货回放页失败");
        }
        if (TextUtils.isEmpty(vid)) {
            return PLVLaunchResult.error("vid 为空，启动直播带货回放页失败");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动直播带货回放页失败");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动直播带货回放页失败");
        }
        Intent intent = new Intent(activity, PLVECLiveEcommerceActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VID, vid);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_VIEWER_AVATAR, viewerAvatar);
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
        setContentView(R.layout.plvec_live_ecommerce_page_activity);
        initParams();
        initLiveRoomManager();
        initView();
        initFloatingWindowSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideFloatingWindowVideoView();
        if (isUserCloseFloatingWindow) {
            videoLayout.setPlayerVolume(100);
        }
        isUserCloseFloatingWindow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatingWindowBinder != null) {
            floatingWindowBinder.destroy();
        }
        if(interactLayout != null){
            interactLayout.destroy();
        }
        if (videoLayout != null) {
            videoLayout.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }

        PLVECFloatingWindowService.unbindService(this, floatingWindowConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(floatingWindowReceiver);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面参数">
    private void initParams() {
        // 获取输入数据
        Intent intent = getIntent();
        boolean isLive = intent.getBooleanExtra(EXTRA_IS_LIVE, true);
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        String viewerAvatar = intent.getStringExtra(EXTRA_VIEWER_AVATAR);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setIsLive(isLive);
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
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面UI">
    private void initView() {
        // 页面关闭按钮
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
        videoLayout.setOnViewActionListener(new IPLVECVideoLayout.OnViewActionListener() {
            @Override
            public void onCloseFloatingAction() {
                if (floatingWindowBinder != null) {
                    floatingWindowBinder.dismiss();
                }
                //主动关闭浮窗时，播放器静音
                videoLayout.setPlayerVolume(0);
                isUserCloseFloatingWindow = true;
            }

        });
        //当前activity 可以手势操作暂停和播放
        initGesture();
        videoLayout.startPlay();
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
                    if (!videoLayout.isSubVideoViewShow()) {
                        videoPause();
                    }
                    if (!videoLayout.getSubVideoViewHerf().isEmpty()) {
                        PLVWebUtils.openWebLink(videoLayout.getSubVideoViewHerf(), PLVECLiveEcommerceActivity.this);
                    }
                } else {
                    if (!videoLayout.isSubVideoViewShow()) {
                        videoResume();
                    }
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!videoLayout.isSubVideoViewShow()) {
                    if (videoLayout.isPlaying()) {
                        videoPause();
                    } else {
                        videoResume();
                    }
                }
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
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 互动应用">
    private void setupInteractLayout(){
        if(interactLayout == null) {
            // 互动应用布局
            ViewStub interactLayoutViewStub = findViewById(R.id.plvec_ppt_interact_layout);
            interactLayout = (IPLVInteractLayout) interactLayoutViewStub.inflate();
            interactLayout.init();
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="初始化 - 悬浮小窗配置">
    private void initFloatingWindowSetting() {
        floatingWindowConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                floatingWindowBinder = (PLVECFloatingWindowBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                floatingWindowBinder = null;
            }
        };
        //绑定悬浮小窗服务
        PLVECFloatingWindowService.bindService(this, floatingWindowConnection);

        floatingWindowReceiver = new FloatingWindowReceiver();
        //注册悬浮小窗广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(floatingWindowReceiver, new IntentFilter(ACTION_FLOATING_WINDOW));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="悬浮小窗 - 显示/隐藏控制">
    private boolean showFloatingWindowVideoView(boolean isResetLocation) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            return false;
        }
        if (floatingWindowBinder != null) {
            if (floatingWindowBinder.isShown()) {
                return true;
            }
            View videoViewParent = videoLayout.detachVideoViewParent();
            floatingWindowBinder.addView(videoViewParent, isResetLocation);
        } else {
            ToastUtils.showLong("悬浮小窗服务连接失败，无法使用悬浮小窗播放功能");
        }
        return true;
    }

    private void hideFloatingWindowVideoView() {
        if (floatingWindowBinder != null && floatingWindowBinder.isShown()) {
            View videoViewParent = floatingWindowBinder.removeView();
            videoLayout.attachVideoViewParent(videoViewParent);
            floatingWindowBinder.dismiss();
        }
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
        commonHomeFragment.getBulletinVO().observe(this, new Observer<PolyvBulletinVO>() {
            @Override
            public void onChanged(@Nullable PolyvBulletinVO bulletinVO) {
                liveDetailFragment.setBulletinVO(bulletinVO);
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
            }
        });

        // 当前页面 监听 回放播放器数据对象中的播放信息变化
        if (videoLayout.getPlaybackPlayInfoVO() != null)
            videoLayout.getPlaybackPlayInfoVO().observe(this, new Observer<PLVPlayInfoVO>() {
                @Override
                public void onChanged(@Nullable PLVPlayInfoVO playInfoVO) {
                    commonHomeFragment.setPlaybackPlayInfo(playInfoVO);
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
        public void onViewCreated() {
            observerDataToLiveHomeFragment();
            setupInteractLayout();
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
        public float onGetSpeedAction() {
            return videoLayout.getSpeed();
        }

        @Override
        public void onViewCreated() {
            observerDataToPlaybackHomeFragment();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 悬浮小窗广播接收器">
    public class FloatingWindowReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_FLOATING_WINDOW.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_IS_SHOW_FLOATING_WINDOW, true)) {
                    if (!isUserCloseFloatingWindow) {//如果没有手动关闭过浮窗才显示
                        showFloatingWindowVideoView(intent.getBooleanExtra(EXTRA_IS_RESET_FLOATING_WINDOW_LOCATION, true));
                        videoResume();
                        videoLayout.setFloatingWindow(true);
                    }
                } else {
                    hideFloatingWindowVideoView();
                    videoLayout.setFloatingWindow(false);
                }
            }
        }
    }
    // </editor-fold>
}
