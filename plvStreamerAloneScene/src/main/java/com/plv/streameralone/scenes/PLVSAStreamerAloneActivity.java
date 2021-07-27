package com.plv.streameralone.scenes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.plv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.plv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.livecommon.module.data.PLVLiveRoomDataManager;
import com.plv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.plv.livecommon.module.utils.PLVViewInitUtils;
import com.plv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.plv.livecommon.module.utils.result.PLVLaunchResult;
import com.plv.livecommon.ui.widget.PLVConfirmDialog;
import com.plv.livecommon.ui.widget.PLVNoInterceptTouchViewPager;
import com.plv.livecommon.ui.window.PLVBaseActivity;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.streameralone.R;
import com.plv.streameralone.modules.liveroom.IPLVSASettingLayout;
import com.plv.streameralone.modules.liveroom.PLVSACleanUpLayout;
import com.plv.streameralone.modules.liveroom.PLVSALinkMicRequestTipsLayout;
import com.plv.streameralone.modules.streamer.IPLVSAStreamerLayout;
import com.plv.streameralone.modules.streamer.PLVSAStreamerFinishLayout;
import com.plv.streameralone.scenes.fragments.PLVSAEmptyFragment;
import com.plv.streameralone.scenes.fragments.PLVSAStreamerHomeFragment;
import com.plv.streameralone.ui.widget.PLVSAConfirmDialog;

/**
 * 手机开播纯视频场景界面
 * 支持的功能有：推流和连麦、聊天室
 */
public class PLVSAStreamerAloneActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="变量">
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId"; // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 开播者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 开播者昵称
    private static final String EXTRA_AVATAR_URL = "avatarUrl"; // 开播者头像url
    private static final String EXTRA_ACTOR = "actor";  // 开播者头衔
    private static final String EXTRA_CHANNEL_NAME = "channelName";//直播间名称

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // view
    // 推流和连麦布局
    private IPLVSAStreamerLayout streamerLayout;
    // 直播设置布局
    private IPLVSASettingLayout settingLayout;
    // 清屏指引布局
    @Nullable
    private PLVSACleanUpLayout cleanUpLayout;
    // 直播结束布局
    private PLVSAStreamerFinishLayout streamerFinishLayout;
    // 摄像头上层的viewpager布局
    private PLVNoInterceptTouchViewPager topLayerViewPager;
    // 有人申请连麦时 连麦提示条布局
    private PLVSALinkMicRequestTipsLayout linkMicRequestTipsLayout;
    // 主页fragment
    private PLVSAStreamerHomeFragment homeFragment;
    // 空白fragment
    private PLVSAEmptyFragment emptyFragment;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动手机开播纯视频页
     *
     * @param activity    上下文Activity
     * @param channelId   频道号
     * @param viewerId    开播者ID
     * @param viewerName  开播者昵称
     * @param avatarUrl   开播者头像url
     * @param actor       开播者头衔
     * @param channelName 直播间名称
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
                                                 @NonNull String channelName) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(avatarUrl)) {
            return PLVLaunchResult.error("avatarUrl 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(actor)) {
            return PLVLaunchResult.error("actor 为空，启动手机开播纯视频页失败！");
        }
        if (TextUtils.isEmpty(channelName)) {
            return PLVLaunchResult.error("channelName 为空，启动手机开播纯视频页失败！");
        }

        Intent intent = new Intent(activity, PLVSAStreamerAloneActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_ACTOR, actor);
        intent.putExtra(EXTRA_CHANNEL_NAME, channelName);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvsa_streamer_alone_activity);
        setStatusBarColor();

        initParams();
        initLiveRoomManager();
        initView();

        observeSettingLayout();
        observeViewPagerLayout();
        observeStreamerLayout();
        observeCleanUpLayout();
        observeLinkmicRequestLayout();
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (streamerLayout != null) {
            streamerLayout.destroy();
        }
        //last destroy socket
        if (homeFragment != null) {
            homeFragment.destroy();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (topLayerViewPager != null) {
            topLayerViewPager.onSuperTouchEvent(event);
        }
        if (streamerLayout != null) {
            streamerLayout.onRvSuperTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (settingLayout != null && settingLayout.onBackPressed()) {
            return;
        } else if (settingLayout != null && settingLayout.isShown()) {
            super.onBackPressed();
            return;
        } else if (homeFragment != null && homeFragment.onBackPressed()) {
            return;
        } else if (streamerLayout != null && streamerLayout.onBackPressed()) {
            return;
        } else if (streamerFinishLayout != null && streamerFinishLayout.isShown()) {
            super.onBackPressed();
            return;
        }

        // 弹出退出直播间的确认框
        new PLVSAConfirmDialog(this)
                .setTitleVisibility(View.GONE)
                .setContent(R.string.plv_live_room_dialog_steamer_exit_confirm_ask)
                .setRightButtonText(R.string.plv_common_dialog_confirm)
                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, View v) {
                        dialog.dismiss();
                        if (streamerLayout != null && streamerFinishLayout != null) {
                            streamerLayout.stopLive();
                            streamerFinishLayout.show();
                        } else {
                            PLVSAStreamerAloneActivity.super.onBackPressed();
                        }
                    }
                })
                .show();
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
        String channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, avatarUrl, PLVSocketUserConstant.USERTYPE_TEACHER, actor);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);
        PLVLiveChannelConfigFiller.setupChannelName(channelName);
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
        streamerLayout = findViewById(R.id.plvsa_streamer_layout);
        settingLayout = findViewById(R.id.plvsa_setting_layout);
        cleanUpLayout = findViewById(R.id.plvsa_clean_up_layout);
        streamerFinishLayout = findViewById(R.id.plvsa_streamer_finish_layout);
        topLayerViewPager = findViewById(R.id.plvsa_top_layer_view_pager);
        linkMicRequestTipsLayout = findViewById(R.id.plvsa_linkmic_request_layout);

        //初始化推流和连麦布局
        streamerLayout.init(liveRoomDataManager);

        //初始化直播设置布局
        settingLayout.init(liveRoomDataManager);

        // 初始化需要添加的fragment
        homeFragment = new PLVSAStreamerHomeFragment();
        emptyFragment = new PLVSAEmptyFragment();
        // 添加fragment到页面上层ViewPage中
        PLVViewInitUtils.initViewPager(
                getSupportFragmentManager(),
                topLayerViewPager,
                1,
                emptyFragment,
                homeFragment
        );
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 直播设置">
    private void observeSettingLayout() {
        settingLayout.setOnViewActionListener(new IPLVSASettingLayout.OnViewActionListener() {
            @Override
            public void onStartLiveAction() {
                getIntent().putExtra(EXTRA_CHANNEL_NAME, liveRoomDataManager.getConfig().getChannelName());
                homeFragment.updateChannelName();
                topLayerViewPager.setVisibility(View.VISIBLE);
                streamerLayout.startLive();
            }

            @Override
            public int getCurrentNetworkQuality() {
                return streamerLayout.getNetworkQuality();
            }

            @Override
            public void setCameraDirection(boolean front) {
                streamerLayout.setCameraDirection(front);
            }

            @Override
            public void setMirrorMode(boolean isMirror) {
                streamerLayout.setMirrorMode(isMirror);
            }

            @Override
            public Pair<Integer, Integer> getBitrateInfo() {
                return streamerLayout.getBitrateInfo();
            }

            @Override
            public void onBitrateClick(int bitrate) {
                streamerLayout.setBitrate(bitrate);
            }

            @Override
            public IPLVStreamerContract.IStreamerPresenter getStreamerPresenter() {
                if (streamerLayout == null) {
                    return null;
                }
                return streamerLayout.getStreamerPresenter();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 推流和连麦">
    private void observeStreamerLayout() {
        //设置view交互事件监听器
        streamerLayout.setOnViewActionListener(new IPLVSAStreamerLayout.OnViewActionListener() {
            @Override
            public void onRestartLiveAction() {
                recreate();
            }
        });
        //监听因断网延迟20s断流的状态
        streamerLayout.addOnShowNetBrokenListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.showAlertDialogNoNetwork();
            }
        });
        //添加推流的媒体状态监听器
        streamerLayout.addOnIsFrontCameraListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.setFrontCameraStatus(aBoolean);
            }
        });
        streamerLayout.addOnIsFrontMirrorModeListener(new IPLVOnDataChangedListener<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                settingLayout.setMirrorModeStatus(aBoolean);
            }
        });
        // 添加直播时长监听
        streamerLayout.addStreamerTimeListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                if (streamerFinishLayout != null) {
                    streamerFinishLayout.updateSecondsSinceStartTiming(integer);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 清屏指引布局">

    private void observeCleanUpLayout() {
        if (streamerLayout != null && cleanUpLayout != null) {
            streamerLayout.getStreamerPresenter().registerView(cleanUpLayout.getStreamerView());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 上层viewPager的页面">
    private void observeViewPagerLayout() {
        homeFragment.setOnViewActionListener(new PLVSAStreamerHomeFragment.OnViewActionListener() {
            @Override
            public void onViewCreated() {
                homeFragment.init(liveRoomDataManager);
                //注册streamerView
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getMoreLayoutStreamerView());
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getMemberLayoutStreamerView());
                streamerLayout.getStreamerPresenter().registerView(homeFragment.getStatusBarLayoutStreamerView());
                //请求成员列表接口
                streamerLayout.getStreamerPresenter().requestMemberList();

                //监听用户连麦请求的变化
                streamerLayout.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (s == null) {
                            return;
                        }
                        homeFragment.updateUserRequestStatus();
                    }
                });
            }

            @Override
            public void onStopLive() {
                if (streamerLayout != null && streamerFinishLayout != null) {
                    streamerLayout.stopLive();
                    streamerFinishLayout.show();
                } else {
                    finish();
                }
            }

            @Override
            public void onClickToOpenMemberLayout() {
                linkMicRequestTipsLayout.cancel();
            }

            @Override
            public boolean showCleanUpLayout() {
                boolean success = false;
                if (cleanUpLayout != null) {
                    success = cleanUpLayout.show();
                    if (success) {
                        cleanUpLayout = null;
                    }
                }
                return success;
            }
        });

        emptyFragment.setOnViewActionListener(new PLVSAEmptyFragment.OnViewActionListener() {
            @Override
            public void onViewCreated() {
                streamerLayout.addOnUserRequestListener(new IPLVOnDataChangedListener<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        if (s == null) {
                            return;
                        }
                        linkMicRequestTipsLayout.show();
                    }
                });
            }

            @Override
            public void onStopLive() {
                if (streamerLayout != null && streamerFinishLayout != null) {
                    streamerLayout.stopLive();
                    streamerFinishLayout.show();
                } else {
                    finish();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 连麦提示条布局">

    private void observeLinkmicRequestLayout() {
        linkMicRequestTipsLayout.setOnTipsClickListener(new PLVSALinkMicRequestTipsLayout.OnTipsClickListener() {
            @Override
            public void onClickBar() {
                linkMicRequestTipsLayout.cancel();
            }

            @Override
            public void onClickNavBtn() {
                linkMicRequestTipsLayout.cancel();
                // homeFragment index=1
                topLayerViewPager.setCurrentItem(1);
                homeFragment.openMemberLayoutAndHideUserRequestTips();
            }
        });
    }

    // </editor-fold>
}
