package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleEventProcessor;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicList;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleMemberList;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.presenter.data.PLVMultiRoleLinkMicData;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.hiclass.IPLVHiClassManager;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.livescenes.hiclass.PLVHiClassManager;
import com.plv.livescenes.hiclass.PLVHiClassManagerFactory;
import com.plv.livescenes.hiclass.api.PLVHCApiManager;
import com.plv.livescenes.hiclass.vo.PLVHCStudentLessonListVO;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;
import com.plv.livescenes.linkmic.listener.PLVLinkMicListener;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.manager.PLVLinkMicManagerFactory;
import com.plv.livescenes.linkmic.vo.PLVLinkMicEngineParam;
import com.plv.livescenes.net.IPLVDataRequestListener;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.socket.event.linkmic.PLVJoinResponseAckResult;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.user.PLVClassStatusBean;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.socket.client.Ack;

import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.JOIN_CHANNEL_ED;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.JOIN_CHANNEL_ING;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.JOIN_CHANNEL_UN;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.LINK_MIC_INITIATED;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.LINK_MIC_INITIATING;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.LINK_MIC_UNINITIATED;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

/**
 * mvp-多角色连麦presenter层实现，实现 IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter 接口
 */
public class PLVMultiRoleLinkMicPresenter implements IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVMultiRoleLinkMicPresenter";
    private static final long JOIN_DISCUSS_COUNTDOWN_TIME = 3000;
    private static final long CAN_SWITCH_CAMERA_AFTER_INIT = 1200;
    /**** Model ****/
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //互动学堂课堂管理
    private IPLVHiClassManager hiClassManager;
    //连麦管理器
    private IPLVLinkMicManager linkMicManager;
    //连麦列表
    private PLVMultiRoleLinkMicList linkMicList;
    //成员列表
    private PLVMultiRoleMemberList memberList;
    //信息处理器
    private PLVMultiRoleEventProcessor eventProcessor;
    //自己的连麦Id
    private String myLinkMicId = "";
    private PLVMultiRoleLinkMicData linkMicData;

    //连麦初始化状态
    private int linkMicInitState = LINK_MIC_UNINITIATED;
    //初始化成功后需要执行的任务
    private List<Runnable> initiatedTasks = new ArrayList<>();

    //初始化连麦引擎成功的时间
    private long initEngineSuccessTimestamp;
    //媒体状态
    private boolean curEnableLocalAudio = true;
    private boolean curEnableLocalVideo = true;
    private boolean curCameraFront = true;
    //摄像头切换到后置的任务
    private Runnable switchCameraTask;
    //加入频道状态
    private int joinChannelStatus = JOIN_CHANNEL_UN;
    //是否处于上台状态
    private boolean isInClassStatus = false;
    //通过LOGIN事件已响应自动上麦的用户Id列表
    private List<String> autoLinkResponseList = new ArrayList<>();
    //是否是讲师类型
    private boolean isTeacherType;
    //是否前置摄像头预览镜像
    private boolean isFrontPreviewMirror = false;
    //当前连麦清晰度
    private int currentBitrate = PLVLinkMicConstant.Bitrate.BITRATE_STANDARD;
    //分组讨论的分组Id
    private String groupId;
    //分组Id对应的组名
    private String groupName;
    //是否是切换分组
    private boolean isGroupChanged;
    //是否是切换组长
    private boolean isLeaderChanged;
    //是否触发了加入讨论
    private boolean isCallJoinDiscuss;

    /**** View ****/
    //连麦mvp模式的view
    private List<IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView> iMultiRoleLinkMicViews;

    //disposable
    private Disposable getMemberListLessDisposable;
    private Disposable getGroupNameDisposable;
    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public PLVMultiRoleLinkMicPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        this.isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        //init linkMicConfig
        String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        PLVLinkMicConfig.getInstance().init(viewerId, false);
        //create linkMicManager
        linkMicManager = PLVLinkMicManagerFactory.createNewLinkMicManager();
        //create linkMicData
        linkMicData = new PLVMultiRoleLinkMicData();
        //初始化连麦列表
        initLinkMicList();
        //初始化成员列表
        initMemberList(linkMicList);
        //初始化信息处理器
        initEventProcessor();
        //初始化互动学堂堂管理器
        initHiClassManager(userType);
    }

    private void initHiClassManager(String userType) {
        hiClassManager = PLVHiClassManagerFactory.createHiClassManager(liveRoomDataManager.getHiClassDataBean(), userType);
        hiClassManager.setOnHiClassListener(new OnHiClassListenerImpl());
    }

    private void initMemberList(PLVMultiRoleLinkMicList linkMicList) {
        memberList = new PLVMultiRoleMemberList(liveRoomDataManager);
        memberList.setLinkMicList(linkMicList);
        memberList.setOnMemberListListener(new OnMemberListListenerImpl());
    }

    private void initLinkMicList() {
        linkMicList = new PLVMultiRoleLinkMicList(liveRoomDataManager);
        linkMicList.addOnLinkMicListListener(new OnLinkMicListListenerImpl());
    }

    private void initEventProcessor() {
        eventProcessor = new PLVMultiRoleEventProcessor(liveRoomDataManager);
        eventProcessor.setOnEventProcessorListener(new OnEventProcessorListenerImpl());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter定义的方法">
    @Override
    public void registerView(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView v) {
        if (iMultiRoleLinkMicViews == null) {
            iMultiRoleLinkMicViews = new ArrayList<>();
        }
        if (!iMultiRoleLinkMicViews.contains(v)) {
            iMultiRoleLinkMicViews.add(v);
        }
        v.setPresenter(this);
    }

    @Override
    public void unregisterView(IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView v) {
        if (iMultiRoleLinkMicViews != null) {
            iMultiRoleLinkMicViews.remove(v);
        }
    }

    @Override
    public void init() {
        if (!checkSelMediaPermission()) {
            return;
        }
        linkMicInitState = LINK_MIC_INITIATING;
        final PLVLinkMicEngineParam param = new PLVLinkMicEngineParam()
                .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                .setGroupId(groupId)
                .setViewerId(liveRoomDataManager.getConfig().getUser().getViewerId())
                .setViewerType(liveRoomDataManager.getConfig().getUser().getViewerType())
                .setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        linkMicManager.initEngine(param, new PLVLinkMicListener() {
            @Override
            public void onLinkMicEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "连麦初始化成功");
                initEngineSuccessTimestamp = System.currentTimeMillis();
                linkMicInitState = LINK_MIC_INITIATED;
                observeRTCEvent(linkMicManager);
                for (Runnable runnable : initiatedTasks) {
                    runnable.run();
                }
                initiatedTasks.clear();
                muteVideo(!curEnableLocalVideo);
                muteAudio(!curEnableLocalAudio);
                if (isTeacherType) {
                    linkMicList.addMyItemToLinkMicList(curEnableLocalVideo, curEnableLocalAudio);
                }
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onLinkMicEngineCreatedSuccess();
                    }
                });
            }

            @Override
            public void onLinkMicError(final int errorCode, final Throwable throwable) {
                PLVCommonLog.e(TAG, "连麦模块错误：errorCode=" + errorCode);
                PLVCommonLog.exception(throwable);
                if (linkMicInitState != LINK_MIC_INITIATED) {
                    linkMicInitState = LINK_MIC_UNINITIATED;
                }
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onLinkMicError(errorCode, throwable);
                    }
                });
            }
        });
        myLinkMicId = linkMicManager.getLinkMicUid();
        setMyLinkMicId(myLinkMicId);
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                view.onInitLinkMicList(myLinkMicId, linkMicList.getData());
            }
        });
    }

    @Override
    public void joinChannel() {
        if (joinChannelStatus != JOIN_CHANNEL_UN) {
            return;
        }
        joinChannelStatus = JOIN_CHANNEL_ING;
        Runnable joinChannelTask = new Runnable() {
            @Override
            public void run() {
                if (PLVLinkMicConfig.getInstance().isPureRtcWatchEnabled()) {
                    Activity topActivity = ActivityUtils.getTopActivity();
                    if (topActivity != null) {
                        topActivity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                    }
                }
                linkMicManager.joinChannel();
            }
        };
        acceptInitiatedTask(joinChannelTask);
    }

    @Override
    public void leaveChannel() {
        linkMicManager.leaveChannel();
    }

    @Override
    public void startLesson(final IPLVDataRequestListener<String> listener) {
        hiClassManager.changeLessonStatus(listener, PLVHiClassDataBean.STATUS_IN_CLASS);
    }

    @Override
    public void stopLesson(IPLVDataRequestListener<String> listener) {
        hiClassManager.changeLessonStatus(listener, PLVHiClassDataBean.STATUS_OVER_CLASS);
    }

    @Override
    public void switchRoleToAudience() {
        linkMicManager.switchRoleToAudience();
    }

    @Override
    public void switchRoleToBroadcaster() {
        linkMicManager.switchRoleToBroadcaster();
    }

    @Override
    public boolean muteAudio(boolean mute) {
        curEnableLocalAudio = !mute;
        if (LINK_MIC_INITIATED != linkMicInitState) {
            return false;
        }
        linkMicManager.muteLocalAudio(mute);
        linkMicData.postEnableAudio(!mute);
        memberList.updateUserMuteAudio(myLinkMicId, mute, PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
        return true;
    }

    @Override
    public boolean muteVideo(boolean mute) {
        curEnableLocalVideo = !mute;
        if (LINK_MIC_INITIATED != linkMicInitState) {
            return false;
        }
        linkMicManager.muteLocalVideo(mute);
        linkMicData.postEnableVideo(!mute);
        memberList.updateUserMuteVideo(myLinkMicId, mute, PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
        return true;
    }

    @Override
    public void switchCamera() {
        switchCamera(!curCameraFront);
    }

    @Override
    public void switchCamera(final boolean front) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (LINK_MIC_INITIATED != linkMicInitState) {
                    return;
                }
                if (front) {
                    linkMicManager.setLocalPreviewMirror(isFrontPreviewMirror);
                } else {
                    linkMicManager.setLocalPreviewMirror(false);
                }
                if (curCameraFront == front) {
                    return;
                }
                curCameraFront = front;
                linkMicManager.switchCamera();
                linkMicData.postIsFrontCamera(curCameraFront);
            }
        };
        acceptSwitchCameraTask(runnable);
    }

    @Override
    public void setPushPictureResolutionType(int type) {
        if (linkMicManager == null) {
            return;
        }
        linkMicManager.setPushPictureResolutionType(type);
    }

    @Override
    public void requestMemberList() {
        memberList.requestData();
    }

    @Override
    public View createRenderView(Context context) {
        return linkMicManager.createTextureRenderView(context);
    }

    @Override
    public void releaseRenderView(View renderView) {
        linkMicManager.releaseRenderView(renderView);
    }

    @Override
    public void setupRenderView(View renderView, String linkMicId) {
        if (isMyLinkMicId(linkMicId)) {
            linkMicManager.setupLocalVideo(renderView, linkMicId);
        } else {
            linkMicManager.setupRemoteVideo(renderView, linkMicId);
        }
    }

    @Override
    public void setupRenderView(View renderView, String linkMicId, int streamType) {
        if (isMyLinkMicId(linkMicId)) {
            linkMicManager.setupLocalVideo(renderView, linkMicId);
        } else {
            linkMicManager.setupRemoteVideo(renderView, linkMicId, streamType);
        }
    }

    @Override
    public void sendCupEvent(int linkMicListPos, final Ack ack) {
        PLVMemberItemDataBean memberItemDataBean = memberList.getItemWithLinkMicListPos(linkMicListPos);
        if (memberItemDataBean == null) {
            return;
        }
        PLVLinkMicEventSender.getInstance().sendCupEvent(
                memberItemDataBean.getSocketUserBean(),
                liveRoomDataManager.getSessionId(),
                new IPLVLinkMicEventSender.PLVSMainCallAck() {
                    @Override
                    public void onCall(Object... args) {
                        if (ack != null) {
                            ack.call(args);
                        }
                    }
                }
        );
    }

    @Override
    public void setPaintPermission(int memberListPos, boolean isHasPermission, final Ack ack) {
        PLVMemberItemDataBean memberItemDataBean = memberList.getItem(memberListPos);
        if (memberItemDataBean == null) {
            return;
        }
        PLVLinkMicEventSender.getInstance().setPaintPermission(
                memberItemDataBean.getSocketUserBean(),
                liveRoomDataManager.getSessionId(),
                isHasPermission, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                    @Override
                    public void onCall(Object... args) {
                        if (ack != null) {
                            ack.call(args);
                        }
                    }
                }
        );
    }

    @Override
    public void setPaintPermissionInLinkMicList(int linkMicListPos, boolean isHasPermission, Ack ack) {
        setPaintPermission(memberList.getItemPos(linkMicListPos), isHasPermission, ack);
    }

    @Override
    public void setMediaPermission(int memberListPos, final boolean isVideoType, final boolean isMute) {
        setMediaPermission(memberListPos, isVideoType, isMute, null);
    }

    @Override
    public void setMediaPermission(int memberListPos, final boolean isVideoType, final boolean isMute, final Ack ack) {
        PLVMemberItemDataBean memberItemDataBean = memberList.getItem(memberListPos);
        if (memberItemDataBean == null) {
            return;
        }
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        String sessionId = liveRoomDataManager.getSessionId();
        PLVLinkMicEventSender.getInstance().setMediaPermission(socketUserBean, sessionId, isVideoType, isMute, new IPLVLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                if (linkMicItemDataBean != null) {
                    if (isVideoType) {
                        memberList.updateUserMuteVideo(linkMicItemDataBean.getLinkMicId(), isMute, linkMicItemDataBean.getStreamType());
                    } else {
                        memberList.updateUserMuteAudio(linkMicItemDataBean.getLinkMicId(), isMute, linkMicItemDataBean.getStreamType());
                    }
                    if (ack != null) {
                        ack.call(args);
                    }
                }
            }
        });
    }

    @Override
    public void setMediaPermissionInLinkMicList(int linkMicListPos, boolean isVideoType, boolean isMute, Ack ack) {
        setMediaPermission(memberList.getItemPos(linkMicListPos), isVideoType, isMute, ack);
    }

    @Override
    public void sendRaiseHandEvent(int raiseHandTime) {
        PLVLinkMicEventSender.getInstance().sendRaiseHandEvent(raiseHandTime, liveRoomDataManager.getSessionId(), null);
    }

    @Override
    public void controlUserLinkMic(int memberListPos, boolean isAllowJoin) {
        final PLVMemberItemDataBean memberItemDataBean = memberList.getItem(memberListPos);
        if (memberItemDataBean == null) {
            return;
        }
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        if (isAllowJoin) {
            //互动学堂的学生需要确认上麦
            PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, true, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    if (args != null && args.length != 0 && args[0] != null) {
                        PLVJoinResponseAckResult joinResponseAckResult = PLVGsonUtil.fromJson(PLVJoinResponseAckResult.class, args[0].toString());
                        if (joinResponseAckResult != null && !joinResponseAckResult.isStatus()) {
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                                    view.onReachTheInteractNumLimit();
                                }
                            });
                            return;
                        }
                    }
                    memberList.updateUserJoining(linkMicItemDataBean);
                }
            });
        } else {
            if (linkMicItemDataBean != null) {
                PLVLinkMicEventSender.getInstance().closeUserLinkMic(linkMicItemDataBean.getLinkMicId(), null);
            }
        }
    }

    @Override
    public void closeAllUserLinkMic() {
        PLVLinkMicEventSender.getInstance().closeAllUserLinkMic(liveRoomDataManager.getSessionId(), null);
    }

    @Override
    public void muteAllUserAudio(final boolean isMute) {
        for (int i = 0; i < memberList.getData().size(); i++) {
            @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberList.getData().get(i).getLinkMicItemDataBean();
            if (linkMicItemDataBean != null && linkMicItemDataBean.isRtcJoinStatus()) {
                if (!isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
                    setMediaPermission(i, false, isMute);
                }
            }
        }
    }

    @Override
    public void answerLinkMicInvitation() {
        acceptResponseJoin(true);
    }

    @Override
    public boolean isTeacherType() {
        return isTeacherType;
    }

    @Override
    public boolean isMyLinkMicId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(myLinkMicId);
    }

    @Override
    public boolean isInClassStatus() {
        return isInClassStatus;
    }

    @Override
    public int getLessonStatus() {
        return hiClassManager.getLessonStatus();
    }

    @Override
    public int getLimitLinkNumber() {
        return hiClassManager.getLimitLinkNumber();
    }

    @Override
    public boolean isJoinDiscuss() {
        return !TextUtils.isEmpty(groupId);
    }

    @NonNull
    @Override
    public PLVMultiRoleLinkMicData getData() {
        return linkMicData;
    }

    @Override
    public void destroy() {
        linkMicInitState = LINK_MIC_UNINITIATED;
        joinChannelStatus = JOIN_CHANNEL_UN;
        isInClassStatus = false;
        initiatedTasks.clear();
        autoLinkResponseList.clear();
        if (iMultiRoleLinkMicViews != null) {
            iMultiRoleLinkMicViews.clear();
        }
        handler.removeCallbacksAndMessages(null);
        dispose(getMemberListLessDisposable);
        dispose(getGroupNameDisposable);
        eventProcessor.destroy();
        linkMicList.destroy();
        memberList.destroy();
        hiClassManager.destroy();
        linkMicManager.destroy();
        PLVLinkMicConfig.getInstance().clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦 - 自动连麦处理">
    private void acceptUserLogin(PLVLoginEvent loginEvent) {
        if (isTeacherType && loginEvent != null && loginEvent.getUser() != null) {
            PLVSocketUserBean socketUserBean = loginEvent.getUser();
            if (isMySocketUserId(socketUserBean.getUserId())) {
                return;
            }
            if (hiClassManager.isAutoConnectEnabledWithTimeRange()) {
                if (!PLVSocketUserConstant.USERTYPE_SCSTUDENT.equals(socketUserBean.getUserType())) {
                    return;//互动学堂只上麦SCStudent的用户
                }
                //自动上麦
                PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, false, null);
                autoLinkResponseList.add(socketUserBean.getUserId());
            } else {
                PLVClassStatusBean classStatusBean = loginEvent.getClassStatus();
                if (classStatusBean != null && classStatusBean.isVoice()) {
                    //存在连麦权限，重新邀请上麦
                    PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, false, null);
                }
            }
        }
    }

    private void acceptOnSliceStart() {
        //讲师或学员收到onSliceStart消息后，再次发送LOGIN消息，小班特殊需求在未开始上课前聊天室不可用，包括发言、学员在线列。若收到sliceId，则聊天室可正常使用。
        PLVSocketWrapper.getInstance().sendLoginEvent(new IPLVLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                if (isTeacherType) {
                    requestMemberListLess();//请求在线列表，如果需要自动上麦在讲师之前登录的用户
                }
            }
        });
    }

    private void requestMemberListLess() {
        dispose(getMemberListLessDisposable);
        getMemberListLessDisposable = memberList.requestMemberListLess(new Consumer<List<PLVSocketUserBean>>() {
            @Override
            public void accept(List<PLVSocketUserBean> userBeans) throws Exception {
                if (hiClassManager.isAutoConnectEnabledWithTimeRange()) {
                    for (PLVSocketUserBean socketUserBean : userBeans) {
                        if (autoLinkResponseList.contains(socketUserBean.getUserId()) || isMyLinkMicId(socketUserBean.getUserId())) {
                            continue;
                        }
                        if (!PLVSocketUserConstant.USERTYPE_SCSTUDENT.equals(socketUserBean.getUserType())) {
                            continue;//互动学堂只上麦SCStudent的用户
                        }
                        //自动上麦
                        PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, false, null);
                    }
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="分组讨论 - 逻辑处理">
    private void acceptJoinDiscuss(final String groupId, boolean isInClass, final String leaderId, final PLVSwitchRoomEvent switchRoomEvent) {
        if (this.groupId != null
                && this.groupId.equals(groupId)) {
            if (isMyLinkMicId(leaderId)) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        //需要回调自己获得组长，因为重连后讲师会回收画笔权限
                        view.onUserHasGroupLeader(true, liveRoomDataManager.getConfig().getUser().getViewerName(), false, true, groupName, leaderId);
                    }
                });
            }
            return;
        }
        isGroupChanged = isJoinDiscuss();
        this.groupId = groupId;
        if (!isGroupChanged) {
            linkMicList.removeMyItemToLinkMicList();
            switchRoleToAudience();
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onWillJoinDiscuss(JOIN_DISCUSS_COUNTDOWN_TIME);
                }
            });
        }
        isLeaderChanged = false;
        isCallJoinDiscuss = false;
        isInClassStatus = isInClass;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dispose(getGroupNameDisposable);
                getGroupNameDisposable = PLVHCApiManager.getInstance().getGroupName(groupId).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(final String groupName) throws Exception {
                        callJoinDiscuss(groupName, leaderId, switchRoomEvent);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        callJoinDiscuss("", leaderId, switchRoomEvent);
                        PLVCommonLog.exception(throwable);
                    }
                });
            }
        }, isGroupChanged ? 0 : JOIN_DISCUSS_COUNTDOWN_TIME);
    }

    private void callJoinDiscuss(final String groupName, String leaderId, final PLVSwitchRoomEvent switchRoomEvent) {
        //1.先回调到上层releaseRenderView后，再创建新的linkMicManager
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                view.onJoinDiscuss(groupId, groupName, switchRoomEvent);
            }
        });
        this.groupName = groupName;
        isCallJoinDiscuss = true;
        handleDiscussChanged(leaderId);
    }

    private void acceptLeaveDiscuss(final PLVSwitchRoomEvent switchRoomEvent) {
        if (groupId == null) {
            return;
        }
        groupId = null;
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                view.onLeaveDiscuss(switchRoomEvent);
            }
        });
        isInClassStatus = false;
        handleDiscussChanged(null);
    }

    private void handleDiscussChanged(String leaderId) {
        linkMicInitState = LINK_MIC_UNINITIATED;
        joinChannelStatus = JOIN_CHANNEL_UN;
        initiatedTasks.clear();
        autoLinkResponseList.clear();
        handler.removeCallbacksAndMessages(null);
        dispose(getMemberListLessDisposable);
        dispose(getGroupNameDisposable);
        //1.销毁memberList、linkMicList
        memberList.destroy();
        linkMicList.destroy();
        //初始化连麦列表
        initLinkMicList();
        //初始化成员列表
        initMemberList(linkMicList);
        memberList.setGroupId(groupId);
        memberList.setLeaderId(leaderId);
        //2.销毁linkMicManager
        linkMicManager.destroy();
        //create linkMicManager
        linkMicManager = PLVLinkMicManagerFactory.createNewLinkMicManager();
        init();
        joinChannel();//加入频道
        requestMemberList();//请求成员列表
        //由于linkMicManager初始化为前置摄像头镜像，所以这里要保存之前的状态
        boolean isCameraFront = curCameraFront;
        curCameraFront = true;
        switchCamera(isCameraFront);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void acceptSwitchCameraTask(Runnable runnable) {
        if (linkMicList.getLinkMicItemWithLinkMicId(myLinkMicId) != null) {
            switchCameraTask = null;
            runnable.run();
        } else {
            switchCameraTask = runnable;
        }
    }

    private void acceptInitiatedTask(Runnable runnable) {
        switch (linkMicInitState) {
            //未初始化
            case LINK_MIC_UNINITIATED:
                PLVCommonLog.d(TAG, "连麦开始初始化");
                initiatedTasks.add(runnable);
                init();
                break;
            //初始化中
            case LINK_MIC_INITIATING:
                PLVCommonLog.d(TAG, "连麦初始化中");
                initiatedTasks.add(runnable);
                return;
            //已经初始化
            case LINK_MIC_INITIATED:
                runnable.run();
                break;
            default:
                break;
        }
    }

    private String getRoomIdCombineDiscuss() {
        return isJoinDiscuss() ? groupId : liveRoomDataManager.getConfig().getChannelId();
    }

    private boolean isMySocketUserId(String userId) {
        return userId != null && userId.equals(liveRoomDataManager.getConfig().getUser().getViewerId());
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void setMyLinkMicId(String linkMicId) {
        memberList.setMyLinkMicId(linkMicId);
        linkMicList.setMyLinkMicId(linkMicId);
        eventProcessor.setMyLinkMicId(linkMicId);
    }

    private void observeRTCEvent(IPLVLinkMicManager linkMicManager) {
        memberList.observeRTCEvent(linkMicManager);
        linkMicList.observeRTCEvent(linkMicManager);
        eventProcessor.observeRTCEvent(linkMicManager);
        registerOnRejoinRoomListener();
    }

    private boolean checkSelMediaPermission() {
        return ActivityUtils.getTopActivity() != null
                && ActivityCompat.checkSelfPermission(ActivityUtils.getTopActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ActivityUtils.getTopActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void acceptResponseJoin(boolean isNeedSendAnswerEvent) {
        if (isInClassStatus) {
            return;
        }
        if (isNeedSendAnswerEvent) {
            PLVLinkMicEventSender.getInstance().sendJoinAnswerEvent();
        }
        isInClassStatus = true;
        switchRoleToBroadcaster();//切换rtc身份
        joinChannel();
        linkMicList.addMyItemToLinkMicList(curEnableLocalVideo, curEnableLocalAudio);
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                view.onTeacherControlMyLinkMic(true);
            }
        });
    }

    private void onMyMicSiteChanged(boolean zoomIn) {
        final Integer serverRtcMaxResolution = nullable(new PLVSugarUtil.Supplier<Integer>() {
            @Override
            public Integer get() {
                return liveRoomDataManager.getHiClassDataBean().getValue().getRtcMaxResolution();
            }
        });
        if (serverRtcMaxResolution == null || serverRtcMaxResolution == 0) {
            return;
        }

        int targetBitrate;
        if (zoomIn) {
            targetBitrate = PLVLinkMicConstant.Bitrate.mapFromServerResolution(serverRtcMaxResolution);
        } else {
            targetBitrate = PLVLinkMicConstant.Bitrate.BITRATE_STANDARD;
        }
        if (targetBitrate != currentBitrate) {
            linkMicManager.setBitrate(targetBitrate);
            currentBitrate = targetBitrate;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - socket、rtc事件接收器">
    private class OnEventProcessorListenerImpl implements PLVMultiRoleEventProcessor.OnEventProcessorListener {
        @Override
        public void onAcceptMyJoinLeave(boolean isByTeacherControl) {
            if (isByTeacherControl && isInClassStatus) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onTeacherControlMyLinkMic(false);
                    }
                });
            }
            isInClassStatus = false;
            switchRoleToAudience();//切换rtc身份
        }

        @Override
        public void onJoinChannelSuccess() {
            joinChannelStatus = JOIN_CHANNEL_ED;
            if (!isInClassStatus) {
                switchRoleToAudience();//切换rtc身份
            } else {
                if (isJoinDiscuss()) {
                    if (isCallJoinDiscuss) {
                        switchRoleToBroadcaster();//切换rtc身份
                        linkMicList.addMyItemToLinkMicList(curEnableLocalVideo, curEnableLocalAudio);
                    }
                } else {
                    switchRoleToBroadcaster();//切换rtc身份
                }
            }
        }

        @Override
        public void onLeaveChannel() {
            joinChannelStatus = JOIN_CHANNEL_UN;
            isInClassStatus = false;
        }

        @Override
        public void onTeacherMuteMyMedia(final boolean isVideoType, final boolean isMute) {
            if (isVideoType) {
                muteVideo(isMute);
            } else {
                muteAudio(isMute);
            }
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onTeacherMuteMyMedia(isVideoType, isMute);
                }
            });
        }

        @Override
        public void onResponseJoin(final boolean isNeedAnswer) {
            Runnable acceptJoinResponseRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isInClassStatus) {
                        return;
                    }
                    if (isNeedAnswer) {
                        final boolean[] hasHandleNeedAnswer = {false};
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                                if (!hasHandleNeedAnswer[0]) {
                                    hasHandleNeedAnswer[0] = view.onUserNeedAnswerLinkMic();
                                }
                            }
                        });
                        if (!hasHandleNeedAnswer[0]) {
                            acceptResponseJoin(true);
                        }
                    } else {
                        acceptResponseJoin(false);
                    }
                }
            };
            acceptInitiatedTask(acceptJoinResponseRunnable);
        }

        @Override
        public void onUserLogin(PLVLoginEvent loginEvent) {
            acceptUserLogin(loginEvent);
        }

        @Override
        public void onSliceStart(PLVOnSliceStartEvent onSliceStartEvent) {
            acceptOnSliceStart();
        }

        @Override
        public void onNetworkQuality(final int quality) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onNetworkQuality(quality);
                }
            });
        }

        @Override
        public void onUpstreamNetworkStatus(final PLVNetworkStatusVO networkStatusVO) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUpstreamNetworkStatus(networkStatusVO);
                }
            });
        }

        @Override
        public void onRemoteNetworkStatus(final PLVNetworkStatusVO networkStatusVO) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onRemoteNetworkStatus(networkStatusVO);
                }
            });
        }

        @Override
        public void onTeacherInfo(final String nick) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onTeacherInfo(nick);
                }
            });
        }

        @Override
        public void onResponseJoinForDiscuss() {
            if (isJoinDiscuss() && !isInClassStatus) {
                isInClassStatus = true;
                if (isCallJoinDiscuss) {
                    switchRoleToBroadcaster();
                    linkMicList.addMyItemToLinkMicList(curEnableLocalVideo, curEnableLocalAudio);
                }
            }
        }

        @Override
        public void onJoinDiscuss(final String groupId, boolean isInClass, @Nullable String leaderId, PLVSwitchRoomEvent switchRoomEvent) {
            acceptJoinDiscuss(groupId, isInClass, leaderId, switchRoomEvent);
        }

        @Override
        public void onLeaveDiscuss(PLVSwitchRoomEvent switchRoomEvent) {
            acceptLeaveDiscuss(switchRoomEvent);
        }

        @Override
        public void onTeacherJoinDiscuss(final boolean isJoin) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onTeacherJoinDiscuss(isJoin);
                }
            });
        }

        @Override
        public void onTeacherSendBroadcast(final String content) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onTeacherSendBroadcast(content);
                }
            });
        }

        @Override
        public void onLeaderRequestHelp() {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLeaderRequestHelp();
                }
            });
        }

        @Override
        public void onLeaderCancelHelp() {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLeaderCancelHelp();
                }
            });
        }

        @Override
        public void onUpdateLinkMicZoom(final PLVUpdateMicSiteEvent updateMicSiteEvent) {
            if (myLinkMicId != null && myLinkMicId.equals(updateMicSiteEvent.getLinkMicIdFromEventId())) {
                onMyMicSiteChanged(true);
            }
            if (PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.HI_CLASS_ZOOM_NEED_REACT_UPDATE_EVENT)) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onUpdateLinkMicZoom(updateMicSiteEvent);
                    }
                });
            }
        }

        @Override
        public void onRemoveLinkMicZoom(final PLVRemoveMicSiteEvent removeMicSiteEvent) {
            if (myLinkMicId != null && myLinkMicId.equals(removeMicSiteEvent.getLinkMicIdFromEventId())) {
                onMyMicSiteChanged(false);
            }
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onRemoveLinkMicZoom(removeMicSiteEvent);
                }
            });
        }

        @Override
        public void onChangeLinkMicZoom(@Nullable final Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap) {
            final boolean isMyselfZoomIn = myLinkMicId != null && updateMicSiteEventMap != null && updateMicSiteEventMap.containsKey(myLinkMicId);
            onMyMicSiteChanged(isMyselfZoomIn);
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onChangeLinkMicZoom(updateMicSiteEventMap);
                }
            });
        }
    }

    private void registerOnRejoinRoomListener() {
        if (linkMicManager == null) {
            return;
        }
        linkMicManager.addEventHandler(new PLVLinkMicEventListener() {
            @Override
            public void onRejoinChannelSuccess(String channel, String uid) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onRejoinRoomSuccess();
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 连麦列表事件接收器">
    private class OnLinkMicListListenerImpl extends PLVMultiRoleLinkMicList.AbsOnLinkMicListListener {
        @Override
        public void onLinkMicListChanged(final List<PLVLinkMicItemDataBean> dataBeanList) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLinkMicListChanged(dataBeanList);
                }
            });
        }

        @Override
        public void onLinkMicItemRemove(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUsersLeave(linkMicItemDataBean, position);
                }
            });
        }

        @Override
        public void onLinkMicUserExisted(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserExisted(linkMicItemDataBean, position);
                }
            });
        }

        @Override
        public void onTeacherScreenStream(final PLVLinkMicItemDataBean linkMicItemDataBean, final boolean isOpen) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onTeacherScreenStream(linkMicItemDataBean, isOpen);
                }
            });
        }

        @Override
        public void onGetLinkMicListStatus(String sessionId, PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus> callback) {
            linkMicManager.getLinkStatus(sessionId, callback);
        }

        @Override
        public void onLinkMicItemInsert(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUsersJoin(linkMicItemDataBean, position);
                }
            });
            if (isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
                if (switchCameraTask != null) {
                    final long delayMillis = Math.max(0, CAN_SWITCH_CAMERA_AFTER_INIT - (System.currentTimeMillis() - initEngineSuccessTimestamp));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (switchCameraTask != null) {
                                switchCameraTask.run();//添加自己item后再切换摄像头
                                switchCameraTask = null;
                            }
                        }
                    }, delayMillis);//需要添加一些延迟，避免直接切换不成功
                }
            }
        }

        @Override
        public void onUserGetCup(final String userNick, final boolean isByEvent, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserGetCup(userNick, isByEvent, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserHasPaint(final boolean isMyself, final boolean isHasPaint, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserHasPaint(isMyself, isHasPaint, linkMicListPos, memberListPos);
                }
            });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 成员列表事件接收器">
    private class OnMemberListListenerImpl implements PLVMultiRoleMemberList.OnMemberListListener {
        @Override
        public void onMemberListChanged(final List<PLVMemberItemDataBean> dataBeans) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onMemberListChanged(dataBeans);
                }
            });
        }

        @Override
        public void onMemberItemChanged(final int pos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onMemberItemChanged(pos);
                }
            });
        }

        @Override
        public void onMemberItemRemove(final int pos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onMemberItemRemove(pos);
                }
            });
        }

        @Override
        public void onMemberItemInsert(final int pos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onMemberItemInsert(pos);
                }
            });
        }

        @Override
        public void onLocalUserVolumeChanged(final int volume) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLocalUserVolumeChanged(volume);
                }
            });
        }

        @Override
        public void onRemoteUserVolumeChanged() {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onRemoteUserVolumeChanged();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final String uid, final boolean mute, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserMuteVideo(uid, mute, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserMuteAudio(final String uid, final boolean mute, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserMuteAudio(uid, mute, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserRaiseHand(final int raiseHandCount, final boolean isRaiseHand, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserRaiseHand(raiseHandCount, isRaiseHand, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserGetCup(final String userNick, final boolean isByEvent, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserGetCup(userNick, isByEvent, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserHasPaint(final boolean isMyself, final boolean isHasPaint, final int linkMicListPos, final int memberListPos) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserHasPaint(isMyself, isHasPaint, linkMicListPos, memberListPos);
                }
            });
        }

        @Override
        public void onUserHasGroupLeader(final boolean isHasGroupLeader, final String nick, @Nullable final String leaderId) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onUserHasGroupLeader(isHasGroupLeader, nick, isGroupChanged, isLeaderChanged, groupName, leaderId);
                }
            });
            isLeaderChanged = true;
            linkMicList.notifyLeaderChanged(leaderId);
        }

        @Override
        public void onLeaveDiscuss() {
            acceptLeaveDiscuss(null);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 互动学堂课堂管理事件接收器">
    private class OnHiClassListenerImpl implements PLVHiClassManager.OnHiClassListener {
        @Override
        public void onLessonPreparing(final long serverTime, final long lessonStartTime) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLessonPreparing(serverTime, lessonStartTime);
                }
            });
        }

        @Override
        public void onLessonStarted(boolean isFirstStart) {
            autoLinkResponseList.clear();
            if (isTeacherType) {
                isInClassStatus = true;
                switchRoleToBroadcaster();//切换rtc身份
            }
            if (isFirstStart) {
                joinChannel();//加入频道
                requestMemberList();//请求成员列表
            }
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLessonStarted();
                }
            });
        }

        @Override
        public void onLessonEnd(final long inClassTime, final boolean isFromApi, @Nullable final PLVHCStudentLessonListVO.DataVO dataVO) {
            isInClassStatus = false;
            switchRoleToAudience();//切换rtc身份
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLessonEnd(inClassTime, isFromApi, dataVO);
                }
            });
            if (!isTeacherType) {
                //下课后收回画笔权限
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                        view.onUserHasPaint(true, false, -1, -1);
                    }
                });
            }
        }

        @Override
        public void onLessonLateTooLong(final long willAutoStopLessonTimeMs) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onLessonLateTooLong(willAutoStopLessonTimeMs);
                }
            });
        }

        @Override
        public void onRepeatLogin(final String desc) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view) {
                    view.onRepeatLogin(desc);
                }
            });
        }

        @Override
        public void onLimitLinkNumber(int limitLinkNumber) {
            linkMicData.postLimitLinkNumber(limitLinkNumber);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view回调">
    private void callbackToView(ViewRunnable runnable) {
        if (iMultiRoleLinkMicViews != null) {
            for (IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view : iMultiRoleLinkMicViews) {
                if (view != null && runnable != null) {
                    runnable.run(view);
                }
            }
        }
    }

    private interface ViewRunnable {
        void run(@NonNull IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicView view);
    }
    // </editor-fold>
}
