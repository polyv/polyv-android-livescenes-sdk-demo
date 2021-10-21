package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.model.PLVMicphoneStatus;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.event.linkmic.PLVOpenMicrophoneEvent;
import com.plv.socket.event.linkmic.PLVTeacherSetPermissionEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.ppt.PLVFinishClassEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVClassStatusBean;
import com.plv.socket.user.PLVSocketUserConstant;

import io.socket.client.Socket;

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
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                Socket.EVENT_MESSAGE);
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
        }
    }

    private void acceptTeacherSetPermissionEvent(PLVTeacherSetPermissionEvent teacherSetPermissionEvent) {
        if (teacherSetPermissionEvent != null) {
            String type = teacherSetPermissionEvent.getType();
            String status = teacherSetPermissionEvent.getStatus();
            String userId = teacherSetPermissionEvent.getUserId();
            if (userId != null && userId.equals(liveRoomDataManager.getConfig().getUser().getViewerId())) {
                final boolean isMute = PLVTeacherSetPermissionEvent.STATUS_ZERO.equals(status);
                if (PLVTeacherSetPermissionEvent.TYPE_VIDEO.equals(type)) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onTeacherMuteMyMedia(true, isMute);
                    }
                } else if (PLVTeacherSetPermissionEvent.TYPE_AUDIO.equals(type)) {
                    if (onEventProcessorListener != null) {
                        onEventProcessorListener.onTeacherMuteMyMedia(false, isMute);
                    }
                }
            }
        }
    }

    private void acceptJoinResponseSEvent(PLVJoinResponseSEvent joinResponseSEvent) {
        if (joinResponseSEvent != null) {
            if (onEventProcessorListener != null) {
                onEventProcessorListener.onResponseJoin(joinResponseSEvent.isNeedAnswer());
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
    }
    // </editor-fold>
}
