package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.streamer.listener.PLVSStreamerEventListener;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVBanIpEvent;
import com.plv.socket.event.chat.PLVSetNickEvent;
import com.plv.socket.event.chat.PLVUnshieldEvent;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinLeaveSEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

/**
 * 推流和连麦的信息处理器
 */
public class PLVStreamerMsgHandler {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVStreamerMsgHandler";

    private PLVStreamerPresenter streamerPresenter;

    private PLVSocketIOObservable.OnConnectStatusListener onConnectStatusListener;
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;

    private PLVSStreamerEventListener linkMicEventHandler;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStreamerMsgHandler(PLVStreamerPresenter streamerPresenter) {
        this.streamerPresenter = streamerPresenter;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void run() {
        observeSocketData();
    }

    public void destroy() {
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnConnectStatusListener(onConnectStatusListener);
        PolyvSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);

        streamerPresenter.streamerManager.removeEventHandler(linkMicEventHandler);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket - 数据监听及处理">
    private void observeSocketData() {
        onConnectStatusListener = new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                //重连成功时，刷新在线列表，以更新重连期间的人员变动情况
                if (PLVSocketStatus.STATUS_RECONNECTSUCCESS == status.getStatus()) {
                    streamerPresenter.requestMemberList();
                }
            }
        };
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                switch (event) {
                    //禁言事件
                    case PLVBanIpEvent.EVENT:
                        PLVBanIpEvent banIpEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVBanIpEvent.class);
                        acceptBanIpEvent(banIpEvent);
                        break;
                    //解除禁言事件
                    case PLVUnshieldEvent.EVENT:
                        PLVUnshieldEvent unshieldEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVUnshieldEvent.class);
                        acceptUnshieldEvent(unshieldEvent);
                        break;
                    //设置昵称事件
                    case PLVSetNickEvent.EVENT:
                        PLVSetNickEvent setNickEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVSetNickEvent.class);
                        acceptSetNickEvent(setNickEvent);
                        break;
                    //踢出用户事件
                    case PLVKickEvent.EVENT:
                        PLVKickEvent kickEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVKickEvent.class);
                        acceptKickEvent(kickEvent);
                        break;
                    //用户登录事件
                    case PLVLoginEvent.EVENT:
                        PLVLoginEvent loginEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVLoginEvent.class);
                        acceptLoginEvent(loginEvent);
                        break;
                    //用户登出事件
                    case PLVLogoutEvent.EVENT:
                        PLVLogoutEvent logoutEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVLogoutEvent.class);
                        acceptLogoutEvent(logoutEvent);
                        break;
                    //sessionId事件
                    case PLVOnSliceIDEvent.EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceIDEvent.class);
                        acceptOnSliceIDEvent(onSliceIDEvent);
                        break;
                    //用户请求连麦事件
                    case PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT:
                        PLVJoinRequestSEvent joinRequestSEvent = PLVGsonUtil.fromJson(PLVJoinRequestSEvent.class, message);
                        acceptJoinRequestSEvent(joinRequestSEvent);
                        break;
                    //用户离开连麦事件
                    case PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT:
                        PLVJoinLeaveSEvent joinLeaveSEvent = PLVGsonUtil.fromJson(PLVJoinLeaveSEvent.class, message);
                        acceptJoinLeaveSEvent(joinLeaveSEvent);
                        break;
                    //嘉宾同意/拒绝连麦事件
                    case PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT:
                        PLVJoinAnswerSEvent joinAnswerSEvent = PLVGsonUtil.fromJson(PLVJoinAnswerSEvent.class, message);
                        acceptJoinAnswerSEvent(joinAnswerSEvent);
                        break;
                }
            }
        };
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(onConnectStatusListener);
        PolyvSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                Socket.EVENT_MESSAGE);
    }

    private void acceptBanIpEvent(PLVBanIpEvent banIpEvent) {
        if (banIpEvent != null) {
            List<PLVSocketUserBean> shieldUsers = banIpEvent.getUserIds();
            if (shieldUsers == null) {
                return;
            }
            for (PLVSocketUserBean socketUserBean : shieldUsers) {
                final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(true);
                    streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onUpdateSocketUserData(item.first);
                        }
                    });
                }
            }
        }
    }

    private void acceptUnshieldEvent(PLVUnshieldEvent unshieldEvent) {
        if (unshieldEvent != null) {
            List<PLVSocketUserBean> unShieldUsers = unshieldEvent.getUserIds();
            if (unShieldUsers == null) {
                return;
            }
            for (PLVSocketUserBean socketUserBean : unShieldUsers) {
                final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(false);
                    streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onUpdateSocketUserData(item.first);
                        }
                    });
                }
            }
        }
    }

    private void acceptSetNickEvent(PLVSetNickEvent setNickEvent) {
        if (setNickEvent != null && PLVSetNickEvent.STATUS_SUCCESS.equals(setNickEvent.getStatus())) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(setNickEvent.getUserId());
            if (item != null) {
                PLVSocketUserBean socketUserBean = item.second.getSocketUserBean();
                socketUserBean.setNick(setNickEvent.getNick());
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUpdateSocketUserData(item.first);
                    }
                });
            }
        }
    }

    private void acceptKickEvent(PLVKickEvent kickEvent) {
        if (kickEvent != null && kickEvent.getUser() != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(kickEvent.getUser().getUserId());
            if (item != null) {
                streamerPresenter.memberList.remove(item.second);
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoveMemberListData(item.first);
                    }
                });
            }
        }
    }

    private void acceptLoginEvent(PLVLoginEvent loginEvent) {
        if (loginEvent != null && loginEvent.getUser() != null) {
            if (PLVSocketUserConstant.USERSOURCE_CHATROOM.equals(loginEvent.getUser().getUserSource())) {
                return;//过滤"userSource":"chatroom"的用户
            }
            Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(loginEvent.getUser().getUserId());
            if (item != null) {
                return;
            }
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            memberItemDataBean.setSocketUserBean(loginEvent.getUser());
            streamerPresenter.memberList.add(memberItemDataBean);
            PLVStreamerPresenter.SortMemberListUtils.sort(streamerPresenter.memberList);
            final Pair<Integer, PLVMemberItemDataBean> newItem = streamerPresenter.getMemberItemWithUserId(loginEvent.getUser().getUserId());
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onAddMemberListData(newItem.first);
                }
            });
        }
    }

    private void acceptLogoutEvent(PLVLogoutEvent logoutEvent) {
        if (logoutEvent != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithUserId(logoutEvent.getUserId());
            if (item != null) {
                streamerPresenter.memberList.remove(item.second);
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoveMemberListData(item.first);
                    }
                });
            }
        }
    }

    private void acceptOnSliceIDEvent(PLVOnSliceIDEvent onSliceIDEvent) {
        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
            streamerPresenter.liveRoomDataManager.setSessionId(onSliceIDEvent.getData().getSessionId());
        }
    }

    private void acceptJoinRequestSEvent(PLVJoinRequestSEvent joinRequestSEvent) {
        if (joinRequestSEvent != null && joinRequestSEvent.getUser() != null) {
            PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinRequestSEvent.getUser());
            final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinRequestSEvent.getUser());
            boolean hasChanged = streamerPresenter.updateMemberListItemInfo(socketUserBean, linkMicItemDataBean, false, true);
            //更新成员列表数据
            if (hasChanged) {
                streamerPresenter.callUpdateSortMemberList();
                streamerPresenter.getData().postUserRequestData(linkMicItemDataBean.getLinkMicId());
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUserRequest(linkMicItemDataBean.getLinkMicId());
                    }
                });
            }
        }
    }

    private void acceptJoinLeaveSEvent(PLVJoinLeaveSEvent joinLeaveSEvent) {
        if (joinLeaveSEvent != null && joinLeaveSEvent.getUser() != null) {
            updateMemberListWithLeave(joinLeaveSEvent.getUser().getUserId());
        }
    }

    private void acceptJoinAnswerSEvent(PLVJoinAnswerSEvent joinAnswerSEvent) {
        if (joinAnswerSEvent != null) {
            String linkMicUid = joinAnswerSEvent.getUserId();
            if (joinAnswerSEvent.isRefuse()) {
                updateMemberListWithLeave(linkMicUid);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦 - 数据监听及处理">
    void observeLinkMicData() {
        linkMicEventHandler = new PLVSStreamerEventListener() {
            @Override
            public void onJoinChannelSuccess(String uid) {
                super.onJoinChannelSuccess(uid);
                PLVCommonLog.d(TAG, "onJoinChannelSuccess: " + uid);
            }

            @Override
            public void onLeaveChannel() {
                super.onLeaveChannel();
                PLVCommonLog.d(TAG, "onLeaveChannel");
            }

            @Override
            public void onUserOffline(String uid) {
                super.onUserOffline(uid);
                PLVCommonLog.d(TAG, "onUserOffline: " + uid);
                updateMemberListWithLeave(uid);
            }

            @Override
            public void onUserJoined(String uid) {
                super.onUserJoined(uid);
                PLVCommonLog.d(TAG, "onUserJoined: " + uid);
                updateMemberListWithJoin(uid);
            }

            @Override
            public void onUserMuteVideo(String uid, boolean mute) {
                super.onUserMuteVideo(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteVideo: " + uid + "*" + mute);
                streamerPresenter.callUserMuteVideo(uid, mute);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : streamerPresenter.rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteVideoInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute));
                    }
                }
            }

            @Override
            public void onUserMuteAudio(final String uid, final boolean mute) {
                super.onUserMuteAudio(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteAudio: " + uid + "*" + mute);
                streamerPresenter.callUserMuteAudio(uid, mute);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : streamerPresenter.rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteAudioInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute));
                    }
                }
            }

            @Override
            public void onRemoteAudioVolumeIndication(PLVAudioVolumeInfo[] speakers) {
                super.onRemoteAudioVolumeIndication(speakers);
                for (PLVMemberItemDataBean memberItemDataBean : streamerPresenter.memberList) {
                    @Nullable PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
                    if (linkMicItemDataBean == null) {
                        continue;
                    }
                    String linkMicId = linkMicItemDataBean.getLinkMicId();
                    if (linkMicId == null || linkMicId.equals(streamerPresenter.streamerManager.getLinkMicUid())) {
                        continue;
                    }
                    boolean hitInVolumeInfoList = false;
                    for (PLVAudioVolumeInfo audioVolumeInfo : speakers) {
                        if (linkMicId.equals(audioVolumeInfo.getUid())) {
                            hitInVolumeInfoList = true;
                            //如果总音量不为0，那么设置当前音量，以PLVLinkMicItemDataBean.MAX_VOLUME作为最大值
                            linkMicItemDataBean.setCurVolume(audioVolumeInfo.getVolume());
                            break;
                        }
                    }
                    if (!hitInVolumeInfoList) {
                        linkMicItemDataBean.setCurVolume(0);
                    }
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onRemoteUserVolumeChanged(streamerPresenter.memberList);
                    }
                });
            }

            @Override
            public void onLocalAudioVolumeIndication(PLVAudioVolumeInfo speaker) {
                super.onLocalAudioVolumeIndication(speaker);
                Pair<Integer, PLVLinkMicItemDataBean> item = streamerPresenter.getLinkMicItemWithLinkMicId(speaker.getUid());
                if (item != null) {
                    item.second.setCurVolume(speaker.getVolume());
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLocalUserMicVolumeChanged();
                    }
                });
            }
        };
        streamerPresenter.streamerManager.addEventHandler(linkMicEventHandler);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="列表数据处理">
    private void updateMemberListWithLeave(final String linkMicUid) {
        streamerPresenter.rtcJoinMap.remove(linkMicUid);
        Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithLinkMicId(linkMicUid);
        if (item != null) {
            item.second.getLinkMicItemDataBean().setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
            streamerPresenter.callUpdateSortMemberList();
        }
        Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = streamerPresenter.getLinkMicItemWithLinkMicId(linkMicUid);
        if (linkMicItem != null) {
            streamerPresenter.streamerList.remove(linkMicItem.second);
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    //更新推流和连麦列表
                    view.onUsersLeave(Collections.singletonList(linkMicUid));
                }
            });
        }
    }

    private void updateMemberListWithJoin(final String linkMicUid) {
        if (!streamerPresenter.rtcJoinMap.containsKey(linkMicUid)) {
            PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
            linkMicItemDataBean.setLinkMicId(linkMicUid);
            streamerPresenter.rtcJoinMap.put(linkMicUid, linkMicItemDataBean);
        }
        Pair<Integer, PLVMemberItemDataBean> item = streamerPresenter.getMemberItemWithLinkMicId(linkMicUid);
        if (item != null) {
            boolean result = streamerPresenter.updateMemberListLinkMicStatusWithRtcJoinList(item.second, linkMicUid);
            if (result) {
                streamerPresenter.callUpdateSortMemberList();
            }
        }
    }
    // </editor-fold>
}
