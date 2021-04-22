package com.easefun.polyv.livecommon.module.modules.socket;

import android.support.annotation.NonNull;

import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfig;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.net.model.PLVSocketLoginVO;
import com.plv.socket.socketio.PLVSocketIOClient;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;

/**
 * socket登录管理器，聊天、连麦、PPT、互动等功能都依赖于socket通信，实现 IPLVSocketLoginManager 接口
 */
public class PLVSocketLoginManager implements IPLVSocketLoginManager {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVSocketLoginManager";
    private IPLVLiveRoomDataManager liveRoomDataManager;
    private OnSocketEventListener onSocketEventListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSocketLoginManager(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSocketLoginManager定义的方法">
    @Override
    public void init() {
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                PLVCommonLog.d(TAG, "socket onStatus: " + status);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    acceptConnectStatusChange(status);
                }
            }
        });
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                PLVCommonLog.d(TAG, "socket receiveMessage: " + message + ", event: " + event + ", listenEvent: " + listenEvent);
                if (getConfig().getChannelId().equals(PolyvSocketWrapper.getInstance().getLoginVO().getChannelId())) {
                    acceptSocketMessage(new PLVSocketMessage(listenEvent, message, event));
                }
            }
        });
    }

    @Override
    public void setOnSocketEventListener(OnSocketEventListener listener) {
        this.onSocketEventListener = listener;
    }

    @Override
    public void setAllowChildRoom(boolean allow) {
        PolyvSocketWrapper.getInstance().setAllowChildRoom(allow);
    }

    @Override
    public void login() {
        //设置socket登录配置
        PLVSocketIOClient.getInstance().setSocketUserId(getConfig().getUser().getViewerId())//用户id
                .setNickName(getConfig().getUser().getViewerName())//用户昵称
                .setAvatarUrl(getConfig().getUser().getViewerAvatar())//用户头像
                .setUserType(getConfig().getUser().getViewerType())//用户类型
                .setActor(getConfig().getUser().getActor())//用户头衔
                .setChannelId(getConfig().getChannelId());//频道号
        //登录socket
        PolyvSocketWrapper.getInstance().login(PLVSocketLoginVO.createFromUserClient());
    }

    @Override
    public void disconnect() {
        PolyvSocketWrapper.getInstance().disconnect();
    }

    @Override
    public void destroy() {
        onSocketEventListener = null;
        PolyvSocketWrapper.getInstance().destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket - 事件处理">
    private void acceptConnectStatusChange(PLVSocketStatus status) {
        switch (status.getStatus()) {
            case PLVSocketStatus.STATUS_LOGINFAIL:
                if (onSocketEventListener != null) {
                    onSocketEventListener.handleLoginFailed(status.getThrowable());
                }
                break;
            case PLVSocketStatus.STATUS_LOGINING:
                if (onSocketEventListener != null) {
                    onSocketEventListener.handleLoginIng(false);
                }
                break;
            case PLVSocketStatus.STATUS_LOGINSUCCESS:
                if (onSocketEventListener != null) {
                    onSocketEventListener.handleLoginSuccess(false);
                }
                break;
            case PLVSocketStatus.STATUS_RECONNECTING:
                if (onSocketEventListener != null) {
                    onSocketEventListener.handleLoginIng(true);
                }
                break;
            case PLVSocketStatus.STATUS_RECONNECTSUCCESS:
                if (onSocketEventListener != null) {
                    onSocketEventListener.handleLoginSuccess(true);
                }
                break;
            default:
                break;
        }
    }

    private void acceptSocketMessage(PLVSocketMessage socketMessage) {
        String event = socketMessage.getEvent();
        String message = socketMessage.getMessage();
        String listenEvent = socketMessage.getListenEvent();

        if (PLVEventConstant.MESSAGE_EVENT.equals(listenEvent)) {
            switch (event) {
                //用户被踢事件
                case PLVEventConstant.MESSAGE_EVENT_KICK:
                    PLVKickEvent kickEvent = PLVEventHelper.toMessageEventModel(message, PLVKickEvent.class);
                    if (kickEvent != null) {
                        boolean isOwn = PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(kickEvent.getUser().getUserId());
                        if (onSocketEventListener != null) {
                            onSocketEventListener.onKickEvent(kickEvent, isOwn);
                        }
                    }
                    break;
                //用户登录被拒事件，被踢后，再次登录聊天室会回调(用户被踢后不能再正常登录socket，可以在管理员后台取消踢出后恢复)
                case PLVEventConstant.MESSAGE_EVENT_LOGIN_REFUSE:
                    PLVLoginRefuseEvent loginRefuseEvent = PLVEventHelper.toMessageEventModel(message, PLVLoginRefuseEvent.class);
                    if (loginRefuseEvent != null) {
                        //收到该事件处理后需要退出登录，否则sdk内部每次重连都会触发该回调
                        disconnect();
                        if (onSocketEventListener != null) {
                            onSocketEventListener.onLoginRefuseEvent(loginRefuseEvent);
                        }
                    }
                    break;
                //用户重新登录事件
                case PLVEventConstant.MESSAGE_EVENT_RELOGIN:
                    PLVReloginEvent reloginEvent = PLVEventHelper.toMessageEventModel(message, PLVReloginEvent.class);
                    if (reloginEvent != null &&
                            PolyvSocketWrapper.getInstance().getLoginVO().getUserId()
                                    .equals(reloginEvent.getUser().getUserId())) {
                        if (onSocketEventListener != null) {
                            onSocketEventListener.onReloginEvent(reloginEvent);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private PLVLiveChannelConfig getConfig() {
        return liveRoomDataManager.getConfig();
    }
    // </editor-fold>
}
