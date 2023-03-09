package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import android.text.TextUtils;

import com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.model.PLVJoinLeaveEvent;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.linkmic.model.PLVLinkMicSwitchViewEvent;
import com.plv.linkmic.model.PLVMicphoneStatus;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.chat.PLVSendCupEvent;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.event.linkmic.PLVTeacherSetPermissionEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

/**
 * date: 2020/7/20
 * author: hwj
 * description:连麦消息处理器
 * 接收server发送的连麦相关消息，并转换为对应的业务相关事件并回调出去。
 */
public class PLVLinkMicMsgHandler {

    // <editor-fold defaultstate="collapsed" desc="变量">
    //static
    private static final String TAG = PLVLinkMicMsgHandler.class.getSimpleName();
    private static final String LINK_MIC_TYPE_AUDIO = "audio";
    private static final String LINK_MIC_STATE_OPEN = "open";

    //账号数据
    private String linkMicId;

    //Listener
    private PLVSocketMessageObserver.OnMessageListener onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
        @Override
        public void onMessage(String listenEvent, String event, String message) {
            processLinkMicSocketMessage(message, event);
        }
    };
    private List<OnLinkMicDataListener> onLinkMicDataListeners = new ArrayList<>();
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLinkMicMsgHandler(String linkMicId) {
        this.linkMicId = linkMicId;
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                Socket.EVENT_MESSAGE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void addLinkMicMsgListener(OnLinkMicDataListener onLinkMicDataListener) {
        onLinkMicDataListeners.add(onLinkMicDataListener);
    }

    public void destroy() {
        onLinkMicDataListeners.clear();
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听、处理socket事件">
    private void processLinkMicSocketMessage(String message, String event) {
        if (TextUtils.isEmpty(event)) {
            return;
        }
        PLVCommonLog.d(TAG, message);
        switch (event) {
            //①讲师开启/关闭连麦；②讲师将某个人下麦了
            case PLVEventConstant.LinkMic.EVENT_OPEN_MICROPHONE:
                PLVMicphoneStatus micPhoneStatus = PLVGsonUtil.fromJson(PLVMicphoneStatus.class, message);
                if (micPhoneStatus != null) {
                    String type = micPhoneStatus.getType();
                    String linkMicState = micPhoneStatus.getStatus();
                    String userId = micPhoneStatus.getUserId();
                    //当userId字段为空时，表示讲师开启或关闭连麦。否则表示讲师让某个观众下麦。
                    boolean isTeacherOpenOrCloseLinkMic = TextUtils.isEmpty(userId);
                    boolean isAudioLinkMic = LINK_MIC_TYPE_AUDIO.equals(type);
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onUpdateLinkMicType(isAudioLinkMic);
                    }


                    if (LINK_MIC_STATE_OPEN.equals(linkMicState) && isTeacherOpenOrCloseLinkMic) {
                        //讲师打开连麦
                        for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                            onLinkMicDataListener.onTeacherOpenLinkMic();
                        }
                    } else {
                        if (isTeacherOpenOrCloseLinkMic) {
                            //讲师关闭连麦
                            for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                                onLinkMicDataListener.onTeacherCloseLinkMic();
                            }
                        } else {
                            //讲师挂断学员

                            boolean isTeacherHangUpMe = linkMicId.equals(userId);
                            //讲师挂断我
                            if (isTeacherHangUpMe) {
                                for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                                    onLinkMicDataListener.onTeacherHangupMe();
                                }
                            }
                        }
                    }
                }
                break;

            //服务端发回来的join request
            case PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT:
                PLVJoinRequestSEvent joinRequestSEvent = PLVGsonUtil.fromJson(PLVJoinRequestSEvent.class, message);
                if (joinRequestSEvent != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onUserJoinRequest(joinRequestSEvent);
                    }
                }
                break;
            //收到其他用户加入rtc频道后发送的joinSuccess
            case PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT:
                PLVLinkMicJoinSuccess joinSuccess = PLVGsonUtil.fromJson(PLVLinkMicJoinSuccess.class, message);
                if (joinSuccess != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onUserJoinSuccess(PLVLinkMicDataMapper.map2LinkMicItemData(joinSuccess), joinSuccess);
                    }
                }
                break;
            //讲师允许加入连麦
            case PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT:
                PLVJoinResponseSEvent joinResponseEvent = PLVGsonUtil.fromJson(PLVJoinResponseSEvent.class, message);
                if (joinResponseEvent == null) {
                    return;
                }
                if (!joinResponseEvent.isNeedAnswer()) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onTeacherAllowMeToJoin(joinResponseEvent);
                    }
                } else {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onTeacherInviteToJoin(joinResponseEvent);
                    }
                }
                break;
            //离开连麦
            case PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT:
                PLVJoinLeaveEvent joinLeaveEvent = PLVGsonUtil.fromJson(PLVJoinLeaveEvent.class, message);
                if (joinLeaveEvent != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onUserJoinLeave(joinLeaveEvent);
                    }
                }
                break;
            //讲师禁用观众视频或麦克风
            case PLVEventConstant.LinkMic.EVENT_MUTE_USER_MICRO:
                PLVLinkMicMedia micMedia = PLVGsonUtil.fromJson(PLVLinkMicMedia.class, message);
                if (micMedia != null) {
                    boolean isMute = micMedia.isMute();
                    boolean isAudio = LINK_MIC_TYPE_AUDIO.equals(micMedia.getType());
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onTeacherMuteMedia(isMute, isAudio);
                        if (TextUtils.isEmpty(micMedia.getSocketId())) {
                            //如果soicketid为空，则认为是全体静音，而不是针对某个人的操作
                            onLinkMicDataListener.onLinkMicConnectMode(isMute ? micMedia.getType() : "");
                        }
                    }
                }
                break;
            // 讲师设置相关权限
            case PLVEventConstant.LinkMic.TEACHER_SET_PERMISSION:
                final PLVTeacherSetPermissionEvent teacherSetPermissionEvent = PLVGsonUtil.fromJson(PLVTeacherSetPermissionEvent.class, message);
                if (teacherSetPermissionEvent != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onTeacherSetPermission(teacherSetPermissionEvent);
                    }
                }
                break;
            case PLVEventConstant.Class.EVENT_SEND_CUP:
                PLVSendCupEvent sendCupEvent = PLVGsonUtil.fromJson(PLVSendCupEvent.class, message);
                if (sendCupEvent != null && sendCupEvent.getOwner() != null && sendCupEvent.getOwner().getUserId() != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onTeacherSendCup(sendCupEvent.getOwner().getUserId(), sendCupEvent.getOwner().getNum());
                    }
                }
                break;
            case PLVEventConstant.Class.SE_SWITCH_MESSAGE:
                PLVLinkMicSwitchViewEvent switchViewEvent = PLVGsonUtil.fromJson(PLVLinkMicSwitchViewEvent.class, message);
                if (switchViewEvent != null) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onSwitchFirstScreen(switchViewEvent.getUserId());
                    }
                }
                break;

            case PLVEventConstant.Class.SE_SWITCH_PPT_MESSAGE:
                //PPT和主屏幕切换位置
                PolyvPPTAuthentic pptAuthentic = PLVGsonUtil.fromJson(PolyvPPTAuthentic.class, message);
                if (pptAuthentic == null) {
                    return;
                }
                String status = pptAuthentic.getStatus();
                if (PolyvPPTAuthentic.PermissionStatus.OK.equals(status)) {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onSwitchPPTViewLocation(false);
                    }
                } else {
                    for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                        onLinkMicDataListener.onSwitchPPTViewLocation(true);
                    }
                }
                break;
            //下课事件
            case PLVEventConstant.Class.FINISH_CLASS:
                for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                    onLinkMicDataListener.onFinishClass();
                }
                break;
            case PLVEventConstant.Ppt.ON_SLICE_ID_EVENT:
                PLVOnSliceIDEvent sliceIDEvent = PLVGsonUtil.fromJson(PLVOnSliceIDEvent.class, message);
                if(sliceIDEvent == null || sliceIDEvent.getData() == null){
                    break;
                }
                for (OnLinkMicDataListener onLinkMicDataListener : onLinkMicDataListeners) {
                    onLinkMicDataListener.onLinkMicConnectMode(sliceIDEvent.getData().getAvConnectMode());
                }
                break;
            default:
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义 - OnLinkMicDataListener">
    public interface OnLinkMicDataListener {
        /**
         * 频道内有用户申请连麦
         */
        void onUserJoinRequest(PLVJoinRequestSEvent joinRequestEvent);

        /**
         * 频道内有用户离开连麦
         */
        void onUserJoinLeave(PLVJoinLeaveEvent joinLeaveEvent);

        /**
         * 讲师允许我上麦
         */
        void onTeacherAllowMeToJoin(PLVJoinResponseSEvent joinResponseEvent);

        /**
         * 僵尸邀请我上麦
         */
        void onTeacherInviteToJoin(PLVJoinResponseSEvent joinResponseSEvent);

        /**
         * 讲师将我下麦
         */
        void onTeacherHangupMe();

        /**
         * 讲师开启连麦
         * 如果是普通连麦观众，则通过请求连麦，老师允许，来加入rtc频道
         * 如果是参与者，则应该直接在回调中调用rtc的加入连麦
         */
        void onTeacherOpenLinkMic();

        /**
         * 讲师关闭连麦
         * 所有观众离开rtc频道
         */
        void onTeacherCloseLinkMic();

        /**
         * 讲师禁用观众视频或麦克风
         *
         * @param isMute  是否禁用
         * @param isAudio true是音频，false是视频
         */
        void onTeacherMuteMedia(boolean isMute, boolean isAudio);

        /**
         * 用户加入连麦成功
         *
         * @param dataBean 用户列表数据
         */
        void onUserJoinSuccess(PLVLinkMicItemDataBean dataBean, PLVLinkMicJoinSuccess joinSuccessEvent);

        /**
         * 讲师设置权限
         *
         * @param setPermissionEvent 事件
         */
        void onTeacherSetPermission(PLVTeacherSetPermissionEvent setPermissionEvent);

        /**
         * 讲师发送奖杯
         */
        void onTeacherSendCup(String linkMicId, int cupNum);

        /**
         * 更新连麦类型
         *
         * @param isAudio true表示是音频连麦，false表示是视频连麦
         */
        void onUpdateLinkMicType(boolean isAudio);

        /**
         * 切换第一画面
         *
         * @param linkMicId 要切换到第一画面的ID
         */
        void onSwitchFirstScreen(String linkMicId);

        /**
         * 切换PPT View的位置
         *
         * @param toMainScreen true表示切换到主屏幕，false表示切回到悬浮窗
         */
        void onSwitchPPTViewLocation(boolean toMainScreen);

        /**
         * 讲师下课
         */
        void onFinishClass();

        /**
         * 连麦音视频模式，可实现如全体静音等
         */
        void onLinkMicConnectMode(String avConnectMode);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="接口定义 - SimpleOnLinkMicDataListener">
    public abstract static class SimpleOnLinkMicDataListener implements OnLinkMicDataListener {
        @Override
        public void onUserJoinRequest(PLVJoinRequestSEvent joinRequestEvent) {

        }

        @Override
        public void onUserJoinLeave(PLVJoinLeaveEvent joinLeaveEvent) {

        }

        @Override
        public void onTeacherAllowMeToJoin(PLVJoinResponseSEvent joinResponseEvent) {

        }

        @Override
        public void onTeacherInviteToJoin(PLVJoinResponseSEvent joinResponseSEvent) {

        }

        @Override
        public void onTeacherHangupMe() {

        }

        @Override
        public void onTeacherOpenLinkMic() {

        }

        @Override
        public void onTeacherCloseLinkMic() {

        }

        @Override
        public void onTeacherMuteMedia(boolean isMute, boolean isAudio) {

        }

        @Override
        public void onUserJoinSuccess(PLVLinkMicItemDataBean dataBean, PLVLinkMicJoinSuccess joinSuccess) {

        }

        @Override
        public void onTeacherSetPermission(PLVTeacherSetPermissionEvent setPermissionEvent) {

        }

        @Override
        public void onTeacherSendCup(String linkMicId, int cupNum) {

        }

        @Override
        public void onUpdateLinkMicType(boolean isAudio) {

        }

        @Override
        public void onSwitchFirstScreen(String linkMicId) {

        }

        @Override
        public void onSwitchPPTViewLocation(boolean toMainScreen) {

        }

        @Override
        public void onFinishClass() {

        }

        @Override
        public void onLinkMicConnectMode(String avConnectMode) {

        }
    }
    // </editor-fold>
}
