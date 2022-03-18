package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.model.PLVMicphoneStatus;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVOTeacherInfoEvent;
import com.plv.socket.event.linkmic.PLVJoinLeaveSEvent;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.event.linkmic.PLVOpenMicrophoneEvent;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVTeacherSetPermissionEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.ppt.PLVFinishClassEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.event.seminar.PLVDiscussAckResult;
import com.plv.socket.event.seminar.PLVHostSendToAllGroupEvent;
import com.plv.socket.event.seminar.PLVJoinDiscussEvent;
import com.plv.socket.event.seminar.PLVJoinSuccessEvent;
import com.plv.socket.event.seminar.PLVLeaveDiscussEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVClassStatusBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.Map;

import io.socket.client.Socket;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

/**
 * socket、rtc事件处理器
 */
public class PLVMultiRoleEventProcessor {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVMultiRoleEventProcessor";
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //连麦管理器
    @Nullable
    private IPLVLinkMicManager linkMicManager;

    private String myLinkMicId;
    private boolean isTeacherType;

    private boolean sendJoinDiscussMsgFlag;
    private boolean isInClassStatusInDiscuss;
    private String groupLeaderId;
    private String groupId;

    //listener
    private OnEventProcessorListener onEventProcessorListener;
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
    private PLVLinkMicEventListener linkMicEventListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVMultiRoleEventProcessor(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        this.isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        observeSocketEvent();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setOnEventProcessorListener(OnEventProcessorListener listener) {
        this.onEventProcessorListener = listener;
    }

    public void setMyLinkMicId(String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
    }

    public void observeRTCEvent(IPLVLinkMicManager linkMicManager) {
        this.linkMicManager = linkMicManager;
        observeRTCEventInner();
    }

    public void destroy() {
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        if (linkMicManager != null) {
            linkMicManager.removeEventHandler(linkMicEventListener);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private boolean isMyLinkMicId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(myLinkMicId);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听socket事件">
    private void observeSocketEvent() {
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                switch (event) {
                    //用户登录事件
                    case PLVLoginEvent.EVENT:
                        PLVLoginEvent loginEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVLoginEvent.class);
                        acceptLoginEvent(loginEvent);
                        break;
                    //sliceId事件
                    case PLVOnSliceIDEvent.EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceIDEvent.class);
                        acceptOnSliceIDEvent(onSliceIDEvent);
                        break;
                    //讲师信息事件
                    case PLVEventConstant.Class.O_TEACHER_INFO:
                        PLVOTeacherInfoEvent oTeacherInfoEvent = PLVEventHelper.toMessageEventModel(message, PLVOTeacherInfoEvent.class);
                        acceptOTeacherInfoEvent(oTeacherInfoEvent);
                        break;
                    //讲师设置权限事件
                    case PLVEventConstant.LinkMic.TEACHER_SET_PERMISSION:
                        PLVTeacherSetPermissionEvent teacherSetPermissionEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVTeacherSetPermissionEvent.class);
                        acceptTeacherSetPermissionEvent(teacherSetPermissionEvent);
                        break;
                    //讲师允许加入连麦事件
                    case PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT:
                        PLVJoinResponseSEvent joinResponseSEvent = PLVGsonUtil.fromJson(PLVJoinResponseSEvent.class, message);
                        acceptJoinResponseSEvent(joinResponseSEvent);
                        break;
                    //用户离开连麦事件
                    case PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT:
                        PLVJoinLeaveSEvent joinLeaveSEvent = PLVGsonUtil.fromJson(PLVJoinLeaveSEvent.class, message);
                        acceptJoinLeaveSEvent(joinLeaveSEvent);
                        break;
                    //①讲师开启/关闭连麦；②讲师将某个人下麦了
                    case PLVEventConstant.LinkMic.EVENT_OPEN_MICROPHONE:
                        PLVMicphoneStatus micPhoneStatus = PLVGsonUtil.fromJson(PLVMicphoneStatus.class, message);
                        acceptMicphoneStatusEvent(micPhoneStatus);
                        break;
                    //上课开始事件
                    case PLVEventConstant.Ppt.ON_SLICE_START_EVENT:
                        PLVOnSliceStartEvent onSliceStartEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceStartEvent.class);
                        acceptOnSliceStartEvent(onSliceStartEvent);
                        break;
                    //下课事件
                    case PLVEventConstant.Class.FINISH_CLASS:
                        PLVFinishClassEvent finishClassEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVFinishClassEvent.class);
                        acceptFinishClassEvent(finishClassEvent);
                        break;
                    //加入讨论
                    case PLVEventConstant.Seminar.EVENT_JOIN_DISCUSS:
                        PLVJoinDiscussEvent joinDiscussEvent = PLVGsonUtil.fromJson(PLVJoinDiscussEvent.class, message);
                        acceptJoinDiscussEvent(joinDiscussEvent);
                        break;
                    //离开讨论
                    case PLVEventConstant.Seminar.EVENT_LEAVE_DISCUSS:
                        PLVLeaveDiscussEvent leaveDiscussEvent = PLVGsonUtil.fromJson(PLVLeaveDiscussEvent.class, message);
                        acceptLeaveDiscussEvent(leaveDiscussEvent);
                        break;
                    //老师加入讨论
                    case PLVEventConstant.Seminar.EVENT_HOST_JOIN:
                        acceptHostJoinEvent();
                        break;
                    //老师离开讨论
                    case PLVEventConstant.Seminar.EVENT_HOST_LEAVE:
                        acceptHostLeaveEvent();
                        break;
                    //老师发出广播通知
                    case PLVEventConstant.Seminar.EVENT_HOST_SEND_TO_ALL_GROUP:
                        PLVHostSendToAllGroupEvent hostSendToAllGroupEvent = PLVGsonUtil.fromJson(PLVHostSendToAllGroupEvent.class, message);
                        acceptHostSendToAllGroupEvent(hostSendToAllGroupEvent);
                        break;
                    //组长请求帮助
                    case PLVEventConstant.Seminar.EVENT_GROUP_REQUEST_HELP:
                        acceptRequestHelp();
                        break;
                    //组长取消帮助(讲师进入分组后会触发)
                    case PLVEventConstant.Seminar.EVENT_CANCEL_HELP:
                        acceptCancelHelp();
                        break;
                }
                // 更新摄像头放大位置
                acceptUpdateLinkMicZoom(listenEvent, event, message);
                // 移除放大区域的摄像头画面
                acceptRemoveLinkMicZoom(listenEvent, event, message);
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                PLVEventConstant.Seminar.SEMINAR_EVENT,
                Socket.EVENT_MESSAGE,
                PLVEventConstant.LinkMic.EVENT_CHANGE_MIC_SITE);
    }

    private void acceptLoginEvent(PLVLoginEvent loginEvent) {
        if (onEventProcessorListener != null) {
            onEventProcessorListener.onUserLogin(loginEvent);
        }
    }

    private void acceptOnSliceIDEvent(PLVOnSliceIDEvent onSliceIDEvent) {
        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
            PLVClassStatusBean classStatusBean = onSliceIDEvent.getClassStatus();
            if (classStatusBean == null || !classStatusBean.isVoice()) {
                if (!isTeacherType) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onAcceptMyJoinLeave(false);
                    }
                }
            }
            if (sendJoinDiscussMsgFlag && groupId != null && groupId.equals(onSliceIDEvent.getGroupId())) {
                groupLeaderId = onSliceIDEvent.getLeader();
            }

            final Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap = onSliceIDEvent.getData().getParsedMicSite();
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onChangeLinkMicZoom(updateMicSiteEventMap);
            }
        }
    }

    private void acceptOTeacherInfoEvent(PLVOTeacherInfoEvent oTeacherInfoEvent) {
        if (oTeacherInfoEvent != null && oTeacherInfoEvent.getData() != null) {
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onTeacherInfo(oTeacherInfoEvent.getData().getNick());
            }
        }
    }

    private void acceptTeacherSetPermissionEvent(PLVTeacherSetPermissionEvent teacherSetPermissionEvent) {
        if (teacherSetPermissionEvent != null) {
            String type = teacherSetPermissionEvent.getType();
            String status = teacherSetPermissionEvent.getStatus();
            String userId = teacherSetPermissionEvent.getUserId();
            if (userId != null && userId.equals(liveRoomDataManager.getConfig().getUser().getViewerId())) {
                final boolean isZeroStatus = PLVTeacherSetPermissionEvent.STATUS_ZERO.equals(status);
                if (PLVTeacherSetPermissionEvent.TYPE_VIDEO.equals(type)) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onTeacherMuteMyMedia(true, isZeroStatus);
                    }
                } else if (PLVTeacherSetPermissionEvent.TYPE_AUDIO.equals(type)) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onTeacherMuteMyMedia(false, isZeroStatus);
                    }
                } else if (PLVTeacherSetPermissionEvent.TYPE_VOICE.equals(type)) {
                    if (!isZeroStatus && groupId != null && groupId.equals(teacherSetPermissionEvent.getRoomId())) {
                        if (sendJoinDiscussMsgFlag) {
                            isInClassStatusInDiscuss = true;
                        }
                        if (onEventProcessorListener != null) {
                            onEventProcessorListener.onResponseJoinForDiscuss();
                        }
                    }
                }
            }
        }
    }

    private void acceptJoinResponseSEvent(PLVJoinResponseSEvent joinResponseSEvent) {
        if (joinResponseSEvent != null) {
            if (!TextUtils.isEmpty(groupId) && !groupId.equals(joinResponseSEvent.getRoomId())) {
                return;
            }
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onResponseJoin(joinResponseSEvent.isNeedAnswer());
            }
        }
    }

    private void acceptJoinLeaveSEvent(PLVJoinLeaveSEvent joinLeaveSEvent) {
        if (joinLeaveSEvent != null && joinLeaveSEvent.getUser() != null) {
            if (isMyLinkMicId(joinLeaveSEvent.getUser().getUserId())) {
                //全体下台的方式挂断只有该回调和teacherSetPermission回调
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onAcceptMyJoinLeave(true);
                }
            }
        }
    }

    private void acceptMicphoneStatusEvent(PLVMicphoneStatus micPhoneStatus) {
        if (micPhoneStatus != null) {
            String linkMicState = micPhoneStatus.getStatus();
            String userId = micPhoneStatus.getUserId();
            //当userId字段为空时，表示讲师开启或关闭连麦。否则表示讲师让某个观众下麦。
            boolean isTeacherOpenOrCloseLinkMic = TextUtils.isEmpty(userId);
            if (!isTeacherOpenOrCloseLinkMic
                    && isMyLinkMicId(userId)) {
                //讲师挂断我
                if (PLVOpenMicrophoneEvent.STATUS_CLOSE.equals(linkMicState)) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onAcceptMyJoinLeave(true);
                    }
                }
            }
        }
    }

    private void acceptOnSliceStartEvent(PLVOnSliceStartEvent onSliceStartEvent) {
        if (onSliceStartEvent != null) {
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onSliceStart(onSliceStartEvent);
            }
        }
    }

    private void acceptFinishClassEvent(PLVFinishClassEvent finishClassEvent) {
        if (!isTeacherType) {
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onAcceptMyJoinLeave(false);
            }
        }
    }

    private void acceptJoinDiscussEvent(final PLVJoinDiscussEvent joinDiscussEvent) {
        if (joinDiscussEvent == null) {
            return;
        }
        groupId = joinDiscussEvent.getGroupId();
        sendJoinDiscussMsgFlag = true;
        isInClassStatusInDiscuss = false;
        groupLeaderId = null;
        PLVSocketWrapper.getInstance().emit(PLVEventConstant.Seminar.SEMINAR_EVENT, PLVGsonUtil.toJson(new PLVJoinDiscussEvent()), new IPLVLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                sendJoinDiscussMsgFlag = false;
                if (args != null && args.length != 0 && args[0] != null) {
                    PLVDiscussAckResult simpleAckResult = PLVGsonUtil.fromJson(PLVDiscussAckResult.class, args[0].toString());
                    if (simpleAckResult != null && simpleAckResult.isSuccess()) {
                        PLVSwitchRoomEvent switchRoomEvent = PLVSwitchRoomEvent.fromDataBean(simpleAckResult.getData());
                        if (onEventProcessorListener != null) {
                            onEventProcessorListener.onJoinDiscuss(joinDiscussEvent.getGroupId(), isInClassStatusInDiscuss, groupLeaderId, switchRoomEvent);
                        }
                        PLVSocketWrapper.getInstance().emit(PLVEventConstant.Seminar.EVENT_JOIN_SUCCESS, PLVGsonUtil.toJson(new PLVJoinSuccessEvent()), null);
                    }
                }
            }
        });
    }

    private void acceptLeaveDiscussEvent(PLVLeaveDiscussEvent leaveDiscussEvent) {
        groupId = null;
        PLVSocketWrapper.getInstance().emit(PLVEventConstant.Seminar.SEMINAR_EVENT, PLVGsonUtil.toJson(new PLVLeaveDiscussEvent()), new IPLVLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                if (args != null && args.length != 0 && args[0] != null) {
                    final PLVDiscussAckResult simpleAckResult = PLVGsonUtil.fromJson(PLVDiscussAckResult.class, args[0].toString());
                    if (simpleAckResult != null && simpleAckResult.isSuccess()) {
                        PLVSwitchRoomEvent switchRoomEvent = PLVSwitchRoomEvent.fromDataBean(simpleAckResult.getData());
                        if (onEventProcessorListener != null) {
                            onEventProcessorListener.onLeaveDiscuss(switchRoomEvent);
                        }
                    }

                    final Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap = nullable(new PLVSugarUtil.Supplier<Map<String, PLVUpdateMicSiteEvent>>() {
                        @Override
                        public Map<String, PLVUpdateMicSiteEvent> get() {
                            return simpleAckResult.getData().getRoomsStatus().getParsedMicSite();
                        }
                    });
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onChangeLinkMicZoom(updateMicSiteEventMap);
                    }
                }
            }
        });
    }

    private void acceptHostJoinEvent() {
        if (onEventProcessorListener != null) {
            onEventProcessorListener.onTeacherJoinDiscuss(true);
        }
    }

    private void acceptHostLeaveEvent() {
        if (onEventProcessorListener != null) {
            onEventProcessorListener.onTeacherJoinDiscuss(false);
        }
    }

    private void acceptHostSendToAllGroupEvent(PLVHostSendToAllGroupEvent hostSendToAllGroupEvent) {
        if (hostSendToAllGroupEvent != null) {
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onTeacherSendBroadcast(hostSendToAllGroupEvent.getContent());
            }
        }
    }

    private void acceptRequestHelp() {
        if (onEventProcessorListener != null) {
            onEventProcessorListener.onLeaderRequestHelp();
        }
    }

    private void acceptCancelHelp() {
        if (onEventProcessorListener != null) {
            onEventProcessorListener.onLeaderCancelHelp();
        }
    }

    private void acceptUpdateLinkMicZoom(String listenEvent, String event, String message) {
        if (PLVUpdateMicSiteEvent.SOCKET_EVENT_TYPE.equals(listenEvent)
                && PLVUpdateMicSiteEvent.EVENT_NAME.equals(event)
                && onEventProcessorListener != null) {
            PLVUpdateMicSiteEvent updateMicSiteEvent = PLVUpdateMicSiteEvent.fromJson(message);
            onEventProcessorListener.onUpdateLinkMicZoom(updateMicSiteEvent);
        }
    }

    private void acceptRemoveLinkMicZoom(String listenEvent, String event, String message) {
        if (PLVRemoveMicSiteEvent.SOCKET_EVENT_TYPE.equals(listenEvent)
                && PLVRemoveMicSiteEvent.EVENT_NAME.equals(event)
                && onEventProcessorListener != null) {
            PLVRemoveMicSiteEvent removeMicSiteEvent = PLVRemoveMicSiteEvent.fromJson(message);
            onEventProcessorListener.onRemoveLinkMicZoom(removeMicSiteEvent);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听rtc事件">
    private void observeRTCEventInner() {
        linkMicEventListener = new PLVLinkMicEventListener() {
            @Override
            public void onJoinChannelSuccess(String uid) {
                PLVCommonLog.d(TAG, "onJoinChannelSuccess, uid=" + uid);
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onJoinChannelSuccess();
                }
            }

            @Override
            public void onLeaveChannel() {
                super.onLeaveChannel();
                PLVCommonLog.d(TAG, "onLeaveChannel");
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onLeaveChannel();
                }
            }

            @Override
            public void onNetworkQuality(final int quality) {
                super.onNetworkQuality(quality);
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onNetworkQuality(quality);
                }
            }

            @Override
            public void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
                super.onUpstreamNetworkStatus(networkStatusVO);
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onUpstreamNetworkStatus(networkStatusVO);
                }
            }

            @Override
            public void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
                super.onRemoteNetworkStatus(networkStatusVO);
                if (onEventProcessorListener != null) {
                    onEventProcessorListener.onRemoteNetworkStatus(networkStatusVO);
                }
            }
        };
        if (linkMicManager != null) {
            linkMicManager.addEventHandler(linkMicEventListener);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器">
    public interface OnEventProcessorListener {

        /**
         * 离开连麦/下台的处理
         */
        void onAcceptMyJoinLeave(boolean isByTeacherControl);

        /**
         * 成功加入连麦频道回调
         */
        void onJoinChannelSuccess();

        /**
         * 已离开连麦频道回调
         */
        void onLeaveChannel();

        /**
         * 讲师控制我的媒体状态
         */
        void onTeacherMuteMyMedia(boolean isVideoType, boolean isMute);

        /**
         * 响应加入连麦
         */
        void onResponseJoin(boolean isNeedAnswer);

        /**
         * 用户登录
         */
        void onUserLogin(PLVLoginEvent loginEvent);

        /**
         * 收到onSliceStart事件
         */
        void onSliceStart(PLVOnSliceStartEvent onSliceStartEvent);

        /**
         * 连麦网络变化
         *
         * @param quality 网络状态常量
         */
        void onNetworkQuality(int quality);

        /**
         * 上行流量网络状态
         *
         * @param networkStatusVO
         */
        void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO);

        /**
         * 远端连麦用户网络状态
         *
         * @param networkStatusVO
         */
        void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO);

        /**
         * 讲师昵称
         */
        void onTeacherInfo(String nick);

        /**
         * 分组讨论的响应加入连麦处理
         */
        void onResponseJoinForDiscuss();

        /**
         * 加入讨论
         *
         * @param groupId         分组Id
         * @param isInClass       是否上台
         * @param leaderId        组长Id
         * @param switchRoomEvent 切换房间事件
         */
        void onJoinDiscuss(String groupId, boolean isInClass, @Nullable String leaderId, PLVSwitchRoomEvent switchRoomEvent);

        /**
         * 离开讨论
         *
         * @param switchRoomEvent 切换房间事件
         */
        void onLeaveDiscuss(PLVSwitchRoomEvent switchRoomEvent);

        /**
         * 讲师加入分组讨论
         *
         * @param isJoin true：加入，false：离开
         */
        void onTeacherJoinDiscuss(boolean isJoin);

        /**
         * 讲师发送广播通知
         *
         * @param content 通知内容
         */
        void onTeacherSendBroadcast(String content);

        /**
         * 组长请求帮助
         */
        void onLeaderRequestHelp();

        /**
         * 组长取消帮助
         */
        void onLeaderCancelHelp();

        /**
         * 更新摄像头放大位置
         */
        void onUpdateLinkMicZoom(PLVUpdateMicSiteEvent updateMicSiteEvent);

        /**
         * 移除放大区域的摄像头画面
         */
        void onRemoveLinkMicZoom(PLVRemoveMicSiteEvent removeMicSiteEvent);

        /**
         * 更新所有摄像头放大画面位置
         *
         * @param updateMicSiteEventMap Key:连麦id，Value:事件
         */
        void onChangeLinkMicZoom(@Nullable Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap);

    }
    // </editor-fold>
}
