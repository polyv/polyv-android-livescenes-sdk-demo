package com.easefun.polyv.livehiclass.scenes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.utils.document.PLVFileChooseUtils;
import com.easefun.polyv.livecommon.module.utils.result.PLVLaunchResult;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.IPLVHCChatroomLayout;
import com.easefun.polyv.livehiclass.modules.document.IPLVHCDocumentLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.IPLVHCLinkMicLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.zoom.IPLVHCLinkMicZoomLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.IPLVHCDeviceDetectionLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCExitConfirmDialog;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCGuideLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCStudentClassCountDownLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCStudentCupGainLayout;
import com.easefun.polyv.livehiclass.modules.liveroom.event.PLVHCOnLessonStatusEvent;
import com.easefun.polyv.livehiclass.modules.statusbar.IPLVHCStatusBarLayout;
import com.easefun.polyv.livehiclass.modules.toolbar.IPLVHCToolBarLayout;
import com.easefun.polyv.livehiclass.modules.toolbar.enums.PLVHCMarkToolEnums;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.hiclass.PLVHiClassGlobalConfig;
import com.plv.livescenes.net.IPLVDataRequestListener;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * 互动学堂场景下定义的 讲师、学生 的 共用界面。
 * 支持的功能有：连麦、聊天室、文档、课堂管理
 */
public class PLVHCLiveHiClassActivity extends PLVBaseActivity {

    // <editor-fold defaultstate="collapsed" desc="变量">
    // 参数 - 定义进入页面所需参数
    private static final String EXTRA_CHANNEL_ID = "channelId";   // 频道号
    private static final String EXTRA_VIEWER_ID = "viewerId";   // 上课者Id
    private static final String EXTRA_VIEWER_NAME = "viewerName";   // 上课者昵称
    private static final String EXTRA_AVATAR_URL = "avatarUrl"; // 上课者头像url
    private static final String EXTRA_USER_TYPE = "userType";   // 用户类型
    private static final String EXTRA_SESSION_ID = "sessionId"; // 场次Id
    private static final String EXTRA_TOKEN = "token"; // token
    private static final String EXTRA_LESSON_ID = "lessonId"; // 课节Id
    private static final String EXTRA_COURSE_CODE = "courseCode"; // 课程号，课程登录时需要传
    private static final String EXTRA_IS_OPEN_MIC = "isOpenMic";    // 麦克风开关
    private static final String EXTRA_IS_OPEN_CAMERA = "isOpenCamera";  // 摄像头开关
    private static final String EXTRA_IS_FRONT_CAMERA = "isFrontCamera";    // 摄像头方向
    private static final String EXTRA_IS_SHOW_DEVICE_DETECTION_LAYOUT = "isShowDeviceDetectionLayout";//是否显示设备检测布局

    // 直播间数据管理器，每个业务初始化所需的参数
    private IPLVLiveRoomDataManager liveRoomDataManager;

    // View
    // 状态栏布局
    private IPLVHCStatusBarLayout plvhcStatusBarLy;
    // 连麦布局
    private IPLVHCLinkMicLayout plvhcLinkmicLy;
    // 文档布局
    private IPLVHCDocumentLayout plvhcDocumentLy;
    // 连麦摄像头放大布局
    private IPLVHCLinkMicZoomLayout plvhcLinkMicZoomLayout;
    // 工具栏布局
    private IPLVHCToolBarLayout plvhcToolBarLy;
    // 高亮引导布局
    @Nullable
    private PLVHCGuideLayout plvhcLiveRoomGuideLayout;
    // 奖杯动效布局
    private PLVHCStudentCupGainLayout plvhcStudentCupGainLayout;
    // 学生观看倒计时布局
    @Nullable
    private PLVHCStudentClassCountDownLayout plvhcStudentClassCountdownLayout;
    // 设备检测布局
    private IPLVHCDeviceDetectionLayout plvhcDeviceDetectionLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="启动Activity的方法">

    /**
     * 启动互动学堂上课页
     *
     * @param activity      上下文Activity
     * @param channelId     频道号
     * @param courseCode    课程号，课程登录时需要传
     * @param lessonId      课节Id
     * @param token         token
     * @param sessionId     场次Id
     * @param userType      用户类型
     * @param viewerId      上课者Id
     * @param viewerName    上课者昵称
     * @param avatarUrl     上课者头像url
     * @return PLVLaunchResult.isSuccess=true表示启动成功，PLVLaunchResult.isSuccess=false表示启动失败
     */
    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static PLVLaunchResult launchHiClass(@NonNull Activity activity,
                                                @NonNull String channelId,
                                                String courseCode,
                                                long lessonId,
                                                @NonNull String token,
                                                @NonNull String sessionId,
                                                @NonNull String userType,
                                                @NonNull String viewerId,
                                                @NonNull String viewerName,
                                                @NonNull String avatarUrl,
                                                boolean isShowDeviceDetectionLayout) {
        if (activity == null) {
            return PLVLaunchResult.error("activity 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(channelId)) {
            return PLVLaunchResult.error("channelId 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(token)) {
            return PLVLaunchResult.error("token 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(sessionId)) {
            return PLVLaunchResult.error("sessionId 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(userType)) {
            return PLVLaunchResult.error("userType 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(viewerId)) {
            return PLVLaunchResult.error("viewerId 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(viewerName)) {
            return PLVLaunchResult.error("viewerName 为空，启动互动学堂上课页失败！");
        }
        if (TextUtils.isEmpty(avatarUrl)) {
            return PLVLaunchResult.error("avatarUrl 为空，启动互动学堂上课页失败！");
        }

        Intent intent = new Intent(activity, PLVHCLiveHiClassActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_COURSE_CODE, courseCode);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        intent.putExtra(EXTRA_TOKEN, token);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        intent.putExtra(EXTRA_USER_TYPE, userType);
        intent.putExtra(EXTRA_VIEWER_ID, viewerId);
        intent.putExtra(EXTRA_VIEWER_NAME, viewerName);
        intent.putExtra(EXTRA_AVATAR_URL, avatarUrl);
        intent.putExtra(EXTRA_IS_SHOW_DEVICE_DETECTION_LAYOUT, isShowDeviceDetectionLayout);
        activity.startActivity(intent);
        return PLVLaunchResult.success();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvhc_live_hi_class_activity);
        findView();// 先findView，避免无法执行到view的销毁方法
        initDeviceDetectionLayout(new Runnable() {
            @Override
            public void run() {
                initParams();
                initLiveRoomManager();
                initView();

                observeToolBarLayout();
                observeLinkMicLayout();
                observeDocumentLayout();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (plvhcDeviceDetectionLayout != null) {
            plvhcDeviceDetectionLayout.destroy();
        }
        if (plvhcLinkMicZoomLayout != null) {
            plvhcLinkMicZoomLayout.destroy();
        }
        if (plvhcStudentCupGainLayout != null) {
            plvhcStudentCupGainLayout.destroy();
        }
        if (plvhcLinkmicLy != null) {
            plvhcLinkmicLy.destroy();
        }
        if (plvhcStatusBarLy != null) {
            plvhcStatusBarLy.destroy();
        }
        // last destroy socket
        if (plvhcToolBarLy != null) {
            plvhcToolBarLy.destroy();
        }
        if (liveRoomDataManager != null) {
            liveRoomDataManager.destroy();
        }
        if (plvhcStudentClassCountdownLayout != null) {
            plvhcStudentClassCountdownLayout.removeFromParent();
            plvhcStudentClassCountdownLayout = null;
        }
        PLVHiClassGlobalConfig.clear();
        PLVUserAbilityManager.myAbility().clearRole();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && data != null) {
            switch (requestCode) {
                case IPLVHCChatroomLayout.REQUEST_CODE_SELECT_IMG:
                    if (plvhcToolBarLy != null) {
                        plvhcToolBarLy.handleImgSelectResult(data);
                    }
                    break;
                case PLVFileChooseUtils.REQUEST_CODE_CHOOSE_UPLOAD_DOCUMENT:
                    if (plvhcDocumentLy != null) {
                        plvhcDocumentLy.onSelectUploadDocument(data);
                    }
                    break;
                default:
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (plvhcDeviceDetectionLayout != null && plvhcDeviceDetectionLayout.isShown()) {
            super.onBackPressed();
            return;
        } else if (plvhcToolBarLy != null && plvhcToolBarLy.onBackPressed()) {
            return;
        } else if (plvhcLinkmicLy != null && !plvhcLinkmicLy.isLessonStarted()) {
            super.onBackPressed();
            return;
        }

        // 弹出退出直播间的确认框
        new PLVHCExitConfirmDialog(this)
                .setOnPositiveListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PLVHCLiveHiClassActivity.super.onBackPressed();
                    }
                })
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="findView">
    private void findView() {
        plvhcStatusBarLy = findViewById(R.id.plvhc_status_bar_ly);
        plvhcLinkmicLy = findViewById(R.id.plvhc_linkmic_ly);
        plvhcDocumentLy = findViewById(R.id.plvhc_document_ly);
        plvhcLinkMicZoomLayout = findViewById(R.id.plvhc_linkmic_zoom_container_layout);
        plvhcToolBarLy = findViewById(R.id.plvhc_tool_bar_ly);
        plvhcLiveRoomGuideLayout = findViewById(R.id.plvhc_live_room_guide_layout);
        plvhcStudentCupGainLayout = findViewById(R.id.plvhc_student_cup_gain_layout);
        plvhcStudentClassCountdownLayout = findViewById(R.id.plvhc_student_class_countdown_layout);
        plvhcDeviceDetectionLayout = findViewById(R.id.plvhc_device_detection_layout);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 设备检测布局">
    private void initDeviceDetectionLayout(final Runnable enterClassTask) {
        plvhcDeviceDetectionLayout.acceptLayoutVisibility(getIntent().getBooleanExtra(EXTRA_IS_SHOW_DEVICE_DETECTION_LAYOUT, true), enterClassTask);
        plvhcDeviceDetectionLayout.setOnViewActionListener(new IPLVHCDeviceDetectionLayout.OnViewActionListener() {
            @Override
            public void onEnterClassAction(boolean isOpenMic, boolean isOpenCamera, boolean isFrontCamera) {
                Intent intent = getIntent();
                intent.putExtra(EXTRA_IS_OPEN_MIC, isOpenMic);
                intent.putExtra(EXTRA_IS_OPEN_CAMERA, isOpenCamera);
                intent.putExtra(EXTRA_IS_FRONT_CAMERA, isFrontCamera);
                if (enterClassTask != null) {
                    enterClassTask.run();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面参数">
    private void initParams() {
        // 获取输入数据
        Intent intent = getIntent();
        final String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);
        final String viewerId = intent.getStringExtra(EXTRA_VIEWER_ID);
        final String viewerName = intent.getStringExtra(EXTRA_VIEWER_NAME);
        final String avatar = intent.getStringExtra(EXTRA_AVATAR_URL);
        final String userType = intent.getStringExtra(EXTRA_USER_TYPE);

        final String token = intent.getStringExtra(EXTRA_TOKEN);
        final long lessonId = intent.getLongExtra(EXTRA_LESSON_ID, 0);
        final String courseCode = intent.getStringExtra(EXTRA_COURSE_CODE);

        final boolean isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);

        // 设置Config数据
        PLVLiveChannelConfigFiller.setupUser(viewerId, viewerName, avatar, userType);
        PLVLiveChannelConfigFiller.setupChannelId(channelId);
        PLVLiveChannelConfigFiller.setHiClassConfig(token, lessonId, courseCode);
        // 配置互动学堂信息，页面销毁时需调用PLVHiClassGlobalConfig.clear方法清除
        PLVHiClassGlobalConfig.setupConfig(token, userType, isTeacherType, courseCode, lessonId, channelId);
        PLVUserAbilityManager.myAbility().addRole(isTeacherType ? PLVUserRole.HI_CLASS_TEACHER : PLVUserRole.HI_CLASS_NORMAL_STUDENT);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 直播间数据管理器">
    private void initLiveRoomManager() {
        // 使用PLVLiveChannelConfigFiller配置好直播参数后，用其创建直播间数据管理器实例
        liveRoomDataManager = new PLVLiveRoomDataManager(PLVLiveChannelConfigFiller.generateNewChannelConfig());
        // 设置场次Id
        String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        liveRoomDataManager.setSessionId(sessionId);
        // 进行网络请求，获取详情课节数据
        liveRoomDataManager.requestLessonDetail();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 页面UI">
    private void initView() {
        // 先初始化连麦布局
        plvhcLinkmicLy.init(liveRoomDataManager);
        // 初始化状态栏布局
        plvhcStatusBarLy.init(liveRoomDataManager);
        // 初始化工具栏布局
        plvhcToolBarLy.init(liveRoomDataManager);

        // 初始化连麦的媒体配置
        boolean isOpenMic = getIntent().getBooleanExtra(EXTRA_IS_OPEN_MIC, true);
        boolean isOpenCamera = getIntent().getBooleanExtra(EXTRA_IS_OPEN_CAMERA, true);
        boolean isFrontCamera = getIntent().getBooleanExtra(EXTRA_IS_FRONT_CAMERA, true);
        plvhcLinkmicLy.muteAudio(!isOpenMic);
        plvhcLinkmicLy.muteVideo(!isOpenCamera);
        plvhcLinkmicLy.switchCamera(isFrontCamera);
        plvhcToolBarLy.initDefaultMediaStatus(!isOpenMic, !isOpenCamera, isFrontCamera);

        // 初始化文档布局
        plvhcDocumentLy.init(liveRoomDataManager);
        // 初始化学生观看倒计时布局
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        final boolean isStudent = PLVSocketUserConstant.USERTYPE_SCSTUDENT.equals(userType);
        if (isStudent && plvhcStudentClassCountdownLayout != null) {
            plvhcStudentClassCountdownLayout.setLiveRoomDataManager(liveRoomDataManager);
            plvhcStudentClassCountdownLayout.setVisibility(View.VISIBLE);
        } else if (plvhcStudentClassCountdownLayout != null) {
            plvhcStudentClassCountdownLayout.removeFromParent();
            plvhcStudentClassCountdownLayout = null;
        }

        // 注册linkMicView
        plvhcLinkmicLy.getLinkMicPresenter().registerView(plvhcToolBarLy.getSettingLayoutLinkMicView());
        plvhcLinkmicLy.getLinkMicPresenter().registerView(plvhcToolBarLy.getMemberLayoutLinkMicView());

        // 进入横屏模式
        PLVScreenUtils.enterLandscape(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 工具栏">
    private void observeToolBarLayout() {
        plvhcToolBarLy.setOnViewActionListener(new IPLVHCToolBarLayout.OnViewActionListener() {
            @Override
            public void onSendRaiseHandEvent(int raiseHandTime) {
                plvhcLinkmicLy.sendRaiseHandEvent(raiseHandTime);
            }

            @Override
            public void onFullScreenControl(boolean isFullScreen) {
                plvhcLinkmicLy.setVisibility(isFullScreen ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onInitClassImageView(View classImageView) {
                if (plvhcLiveRoomGuideLayout != null && plvhcLiveRoomGuideLayout.getStartClassView() == null) {
                    plvhcLiveRoomGuideLayout.setStartClassView(classImageView);
                    final int result = plvhcLiveRoomGuideLayout.showIfNeeded();
                    if (result == PLVHCGuideLayout.SHOW_SUCCESS) {
                        plvhcLiveRoomGuideLayout = null;
                    }
                }
            }

            @Override
            public void onStartLesson(IPLVDataRequestListener<String> listener) {
                plvhcLinkmicLy.startLesson(listener);
            }

            @Override
            public void onStopLesson(IPLVDataRequestListener<String> listener) {
                plvhcLinkmicLy.stopLesson(listener);
            }

            @Override
            public void onRequestChangeDocumentMarkTool(PLVHCMarkToolEnums.MarkTool newMarkTool) {
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.changeMarkTool(newMarkTool);
                }
            }

            @Override
            public void onRequestChangeDocumentColor(PLVHCMarkToolEnums.Color newColor) {
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.changeColor(newColor);
                }
            }

            @Override
            public void onRequestUndo() {
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.operateUndo();
                }
            }

            @Override
            public void onRequestDelete() {
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.operateDelete();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 连麦">
    private void observeLinkMicLayout() {
        plvhcLinkmicLy.setOnViewActionListener(new IPLVHCLinkMicLayout.OnViewActionListener() {
            @Override
            public void onLayoutSizeChanged() {
                plvhcToolBarLy.adjustLayout();
            }

            @Override
            public void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand) {
                plvhcToolBarLy.acceptUserRaiseHand(raiseHandCount, isRaiseHand);
            }

            @Override
            public void onHasPaintToMe(boolean isHasPaint) {
                plvhcToolBarLy.acceptHasPaintToMe(isHasPaint);
                plvhcDocumentLy.acceptHasPaintToMe(isHasPaint);
            }

            @Override
            public void onSetupLinkMicRenderView(View renderView, String linkMicId, int streamType) {
                if (plvhcLiveRoomGuideLayout != null && plvhcLiveRoomGuideLayout.getLinkMicView() == null) {
                    plvhcLiveRoomGuideLayout.setLinkMicView(renderView);
                    final int result = plvhcLiveRoomGuideLayout.showIfNeeded();
                    if (result == PLVHCGuideLayout.SHOW_SUCCESS) {
                        plvhcLiveRoomGuideLayout = null;
                    }
                }
            }

            @Override
            public void onGetCup(String userName) {
                plvhcStudentCupGainLayout.show(userName);
            }

            @Override
            public void onNetworkQuality(int networkQuality) {
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.acceptNetworkQuality(networkQuality);
                }
            }

            @Override
            public void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.acceptUpstreamNetworkStatus(networkStatusVO);
                }
            }

            @Override
            public void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.acceptRemoteNetworkStatus(networkStatusVO);
                }
            }

            @Override
            public void onLessonPreparing(long serverTime, long lessonStartTime) {
                plvhcToolBarLy.onLessonPreparing(serverTime, lessonStartTime);
            }

            @Override
            public void onLessonStarted() {
                plvhcToolBarLy.onLessonStarted();
                if (plvhcStudentClassCountdownLayout != null) {
                    plvhcStudentClassCountdownLayout.startClass();
                    plvhcStudentClassCountdownLayout = null;
                }
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.onLessonStart();
                }
                PLVHCOnLessonStatusEvent.Bus.post(new PLVHCOnLessonStatusEvent(true));
            }

            @Override
            public void onLessonEnd(long inClassTime, boolean isTeacherType, boolean hasNextClass) {
                PLVHCOnLessonStatusEvent.Bus.post(new PLVHCOnLessonStatusEvent(false, isTeacherType, hasNextClass));
                plvhcToolBarLy.onLessonEnd(inClassTime);
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.onLessonEnd();
                }
            }

            @Override
            public void onUserHasGroupLeader(boolean isHasGroupLeader) {
                plvhcToolBarLy.onUserHasGroupLeader(isHasGroupLeader);
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.onUserHasGroupLeader(isHasGroupLeader);
                }
            }

            @Override
            public void onJoinDiscuss(String groupId, String groupName, PLVSwitchRoomEvent switchRoomEvent) {
                plvhcToolBarLy.onJoinDiscuss(groupId);
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.onJoinDiscuss(groupId, groupName);
                }
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.onJoinDiscuss(switchRoomEvent);
                }
            }

            @Override
            public void onLeaveDiscuss(PLVSwitchRoomEvent switchRoomEvent) {
                plvhcToolBarLy.onLeaveDiscuss();
                if (plvhcStatusBarLy != null) {
                    plvhcStatusBarLy.onLeaveDiscuss();
                }
                if (plvhcDocumentLy != null) {
                    plvhcDocumentLy.onLeaveDiscuss(switchRoomEvent);
                }
            }

            @Override
            public void onLeaderRequestHelp() {
                plvhcToolBarLy.onLeaderRequestHelp();
            }

            @Override
            public void onLeaderCancelHelp() {
                plvhcToolBarLy.onLeaderCancelHelp();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 文档">

    private void observeDocumentLayout() {
        plvhcDocumentLy.setOnViewActionListener(new IPLVHCDocumentLayout.OnViewActionListener() {
            @Override
            public void onChangeMarkToolOperationButtonState(boolean showUndoButton, boolean showDeleteButton) {
                if (plvhcToolBarLy != null) {
                    plvhcToolBarLy.changeMarkToolState(showUndoButton, showDeleteButton);
                }
            }
        });
    }

    // </editor-fold>
}
