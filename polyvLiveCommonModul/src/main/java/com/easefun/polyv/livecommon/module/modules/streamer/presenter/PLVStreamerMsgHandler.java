package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.config.PLVLiveChannelConfigFiller;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.streamer.listener.PLVSStreamerEventListener;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.livescenes.document.model.PLVPPTStatus;
import com.plv.livescenes.streamer.transfer.PLVStreamerInnerDataTransfer;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVBanIpEvent;
import com.plv.socket.event.chat.PLVSetNickEvent;
import com.plv.socket.event.chat.PLVUnshieldEvent;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinLeaveSEvent;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.ppt.PLVOnSliceStartEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

/**
 * 推流和连麦的信息处理器
 */
public class PLVStreamerMsgHandler {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVStreamerMsgHandler";
    private static final String LINK_MIC_TYPE_AUDIO = "audio";

    private final PLVStreamerPresenter streamerPresenter;

    private PLVSocketIOObservable.OnConnectStatusListener onConnectStatusListener;
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;

    private PLVSStreamerEventListener linkMicEventHandler;

    @Nullable
    private String lastFirstScreenUserId;
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

        streamerPresenter.getStreamerManager().removeEventHandler(linkMicEventHandler);
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
                    case PLVEventConstant.Ppt.ON_SLICE_START_EVENT:
                        PLVOnSliceStartEvent onSliceStartEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceStartEvent.class);
                        acceptOnSliceStartEvent(onSliceStartEvent);
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
                    case PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT:
                        PLVJoinResponseSEvent joinResponseSEvent = PLVGsonUtil.fromJson(PLVJoinResponseSEvent.class, message);
                        acceptJoinResponseEvent(joinResponseSEvent);
                        break;
                    case PLVEventConstant.LinkMic.TEACHER_SET_PERMISSION:
                        PLVPPTAuthentic authentic = PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message);
                        acceptTeacherSetPermissionEvent(authentic);
                        break;
                    //嘉宾被禁用观众视频或麦克风
                    case PLVEventConstant.LinkMic.EVENT_MUTE_USER_MICRO:
                        PLVLinkMicMedia micMedia = PLVGsonUtil.fromJson(PLVLinkMicMedia.class, message);
                        if (micMedia != null) {
                            boolean isMute = micMedia.isMute();
                            boolean isAudio = LINK_MIC_TYPE_AUDIO.equals(micMedia.getType());
                            streamerPresenter.callUpdateGuestMediaStatus(isMute, isAudio);
                        }
                        break;
                    // 第一画面切换
                    case PLVEventConstant.Class.SE_SWITCH_MESSAGE:
                        updateFirstScreen(PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message));
                        break;
                    // PPT白板和摄像头画面切换
                    case PLVEventConstant.Class.SE_SWITCH_PPT_MESSAGE:
                        updateDocumentStreamerViewPosition(PLVGsonUtil.fromJson(PLVPPTAuthentic.class, message));
                        break;
                    default:
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
            streamerPresenter.getLiveRoomDataManager().setSessionId(onSliceIDEvent.getData().getSessionId());
            //嘉宾响应全体静音
            if ("audio".equals(onSliceIDEvent.getData().getAvConnectMode())) {
                streamerPresenter.callUpdateGuestMediaStatus(true, true);
            }

            //更新data，响应直播恢复
            if(PLVLiveChannelConfigFiller.generateNewChannelConfig().isLiveStreamingWhenLogin()){
                //更新ppt状态
                PLVPPTStatus pptStatus = new PLVPPTStatus();
                PLVOnSliceIDEvent.DataBean data = onSliceIDEvent.getData();
                pptStatus.setAutoId(data.getAutoId());
                pptStatus.setStep(PLVFormatUtils.integerValueOf(data.getStep(), 0));
                pptStatus.setPageId(data.getPageId());

                PLVStreamerInnerDataTransfer.getInstance().setPPTStatusForOnSliceStartEvent(pptStatus);
            }

            // 同步主副屏切换
            final boolean documentInMainScreen = onSliceIDEvent.getPptAndVedioPosition() == 0;
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onDocumentStreamerViewChange(documentInMainScreen);
                }
            });
        }
    }

    private void acceptOnSliceStartEvent(PLVOnSliceStartEvent onSliceStartEvent) {
        if (onSliceStartEvent != null && onSliceStartEvent.getData() != null) {
            //嘉宾更新新的sessionId，防止使用上一场次的sessionId导致请求错误的连麦列表数据。
            if (streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST)) {
                String sessionId = onSliceStartEvent.getSessionId();
                streamerPresenter.getLiveRoomDataManager().setSessionId(sessionId);
            }
        }
    }

    private void acceptJoinRequestSEvent(PLVJoinRequestSEvent joinRequestSEvent) {
        if (joinRequestSEvent == null || joinRequestSEvent.getUser() == null) {
            return;
        }
        final PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinRequestSEvent.getUser());
        final Pair<Integer, PLVMemberItemDataBean> memberItemDataBean = streamerPresenter.getMemberItemWithUserId(joinRequestSEvent.getUser().getUserId());
        final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinRequestSEvent.getUser());

        if (memberItemDataBean != null
                && memberItemDataBean.second != null
                && memberItemDataBean.second.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION) {
            streamerPresenter.controlUserLinkMicInLinkMicList(memberItemDataBean.first, PLVStreamerControlLinkMicAction.acceptRequest());
        } else if (memberItemDataBean != null
                && memberItemDataBean.second != null
                && memberItemDataBean.second.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.IDLE) {
            memberItemDataBean.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP);
            streamerPresenter.callUpdateSortMemberList();
            streamerPresenter.getData().postUserRequestData(linkMicItemDataBean.getLinkMicId());
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onUserRequest(linkMicItemDataBean.getLinkMicId());
                }
            });
        } else {
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
            String userId = joinLeaveSEvent.getUser().getUserId();
            if (streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerId().equals(userId) &&
                    streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType().equals(PLVSocketUserConstant.USERTYPE_GUEST)) {
                //如果当前用户是嘉宾，则收到joinLeave的时候不要把自己移除了。
                PLVCommonLog.d(TAG, "guest receive joinLeave");
            } else {
                streamerPresenter.updateMemberListWithLeave(joinLeaveSEvent.getUser().getUserId(), false);
            }

        }
    }

    private void acceptJoinAnswerSEvent(final PLVJoinAnswerSEvent joinAnswerSEvent) {
        if (joinAnswerSEvent == null || joinAnswerSEvent.getUserId() == null) {
            return;
        }
        final String linkMicUid = joinAnswerSEvent.getUserId();
        final Pair<Integer, PLVMemberItemDataBean> currentMemberItemDataBean = streamerPresenter.getMemberItemWithUserId(linkMicUid);
        if (joinAnswerSEvent.isAccept()) {
            streamerPresenter.updateMemberListWithJoin(linkMicUid);
        } else {
            streamerPresenter.updateMemberListWithLeave(linkMicUid, false);
        }

        if (currentMemberItemDataBean != null && currentMemberItemDataBean.second != null) {
            streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onViewerJoinAnswer(joinAnswerSEvent, currentMemberItemDataBean.second);
                }
            });
        }
    }

    private void acceptJoinResponseEvent(PLVJoinResponseSEvent event) {
        if (event == null) {
            return;
        }
        if (event.isNeedAnswer()) {
            streamerPresenter.onInviteJoinLinkMic(event);
        } else {
            streamerPresenter.onResponseJoinLinkMic(event);
        }
    }

    private void acceptTeacherSetPermissionEvent(final PLVPPTAuthentic authentic) {
        if (authentic == null || authentic.getUserId() == null) {
            return;
        }
        //memberlist和streamerlist排序可能不同
        Pair<Integer, PLVMemberItemDataBean> memberItem = streamerPresenter.getMemberItemWithLinkMicId(authentic.getUserId());
        Pair<Integer, PLVLinkMicItemDataBean> streamerItem = streamerPresenter.getLinkMicItemWithLinkMicId(authentic.getUserId());
        final boolean isCurrentUser = authentic.getUserId().equals(streamerPresenter.getStreamerManager().getLinkMicUid());
        final PLVSocketUserBean bean = (memberItem != null && memberItem.second != null) ? memberItem.second.getSocketUserBean() : null;

        if (PLVPPTAuthentic.TYPE_SPEAKER.equals(authentic.getType())) {
            if (memberItem != null && memberItem.second != null && memberItem.second.getLinkMicItemDataBean() != null) {
                memberItem.second.getLinkMicItemDataBean().setHasSpeaker(!authentic.hasNoAthuentic());
            }
            if (streamerItem != null && streamerItem.second != null) {
                streamerItem.second.setHasSpeaker(!authentic.hasNoAthuentic());
            }
            streamerPresenter.onCurrentSpeakerChanged(authentic.getType(), !authentic.hasNoAthuentic(), isCurrentUser, bean);
        } else if (PLVPPTAuthentic.PermissionType.SCREEN_SHARE.equals(authentic.getType())) {
            if (memberItem != null && memberItem.second != null && memberItem.second.getLinkMicItemDataBean() != null) {
                memberItem.second.getLinkMicItemDataBean().setScreenShare(!authentic.hasNoAthuentic());
            }
            if (streamerItem != null && streamerItem.second != null) {
                streamerItem.second.setScreenShare(!authentic.hasNoAthuentic());
            }
        } else if (PLVPPTAuthentic.TYPE_VOICE.equals(authentic.getType())) {
            if (isCurrentUser) {
                if ("0".equals(authentic.getStatus())) {
                    streamerPresenter.getStreamerManager().switchRoleToAudience();
                    streamerPresenter.callUpdateGuestStatus(false);
                } else {
                    streamerPresenter.getStreamerManager().switchRoleToBroadcaster();
                    streamerPresenter.callUpdateGuestStatus(true);
                }
            }
        } else if (PLVPPTAuthentic.TYPE_SPECIAL_RAISE_HAND.equals(authentic.getType())) {
            final Pair<Integer, PLVMemberItemDataBean> memberItemDataBean = streamerPresenter.getMemberItemWithLinkMicId(authentic.getUserId());
            if ("1".equals(authentic.getStatus())) {
                if (memberItemDataBean != null && memberItemDataBean.second != null) {
                    if (memberItemDataBean.second.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION) {
                        streamerPresenter.controlUserLinkMicInLinkMicList(memberItemDataBean.first, PLVStreamerControlLinkMicAction.acceptRequest());
                    } else {
                        memberItemDataBean.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP);
                        streamerPresenter.callUpdateSortMemberList();
                        streamerPresenter.getData().postUserRequestData(authentic.getUserId());
                        streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                view.onUserRequest(authentic.getUserId());
                            }
                        });
                    }
                }
            } else {
                if (memberItemDataBean != null && memberItemDataBean.second != null && PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP.equals(memberItemDataBean.second.getLinkMicStatus())) {
                    memberItemDataBean.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.IDLE);
                    streamerPresenter.callUpdateSortMemberList();
                }
            }
        } else if (PLVPPTAuthentic.TYPE_IN_WAIT_VOICE.equals(authentic.getType())) {
            if ("0".equals(authentic.getStatus())) {
                final Pair<Integer, PLVMemberItemDataBean> memberItemDataBean = streamerPresenter.getMemberItemWithLinkMicId(authentic.getUserId());
                if (memberItemDataBean != null && memberItemDataBean.second != null) {
                    memberItemDataBean.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.IDLE);
                    streamerPresenter.callUpdateSortMemberList();
                }
            }
        }

    }

    private void updateFirstScreen(final PLVPPTAuthentic authentic) {
        if (authentic == null || authentic.getUserId() == null) {
            return;
        }
        streamerPresenter.onFirstScreenChange(authentic.getUserId(), !authentic.hasNoAthuentic());
    }

    private void updateDocumentStreamerViewPosition(final PLVPPTAuthentic authentic) {
        if (authentic == null) {
            return;
        }
        final boolean documentInMainScreen = "0".equals(authentic.getStatus());
        streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onDocumentStreamerViewChange(documentInMainScreen);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦 - 数据监听及处理">
    void observeLinkMicData() {
        linkMicEventHandler = new PLVSStreamerEventListener() {
            @Override
            public void onJoinChannelSuccess(String uid) {
                super.onJoinChannelSuccess(uid);
                PLVCommonLog.d(TAG, "onJoinChannelSuccess: " + uid);
                final boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(streamerPresenter.getLiveRoomDataManager().getConfig().getUser().getViewerType());
                final boolean isGuestAutoLinkMic = streamerPresenter.getLiveRoomDataManager().getConfig().isAutoLinkToGuest();
                if (isGuest) {
                    if (isGuestAutoLinkMic) {
                        streamerPresenter.getStreamerManager().switchRoleToBroadcaster();
                        streamerPresenter.callUpdateGuestStatus(true);
                    } else {
                        streamerPresenter.getStreamerManager().switchRoleToAudience();
                        streamerPresenter.callUpdateGuestStatus(false);
                    }
                }
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
                streamerPresenter.updateMemberListWithLeave(uid, true);
            }

            @Override
            public void onUserJoined(String uid) {
                super.onUserJoined(uid);
                PLVCommonLog.d(TAG, "onUserJoined: " + uid);
                streamerPresenter.updateMemberListWithJoin(uid);
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
                    if (linkMicId == null || linkMicId.equals(streamerPresenter.getStreamerManager().getLinkMicUid())) {
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
            public void onLocalAudioVolumeIndication(final PLVAudioVolumeInfo speaker) {
                super.onLocalAudioVolumeIndication(speaker);
                Pair<Integer, PLVLinkMicItemDataBean> item = streamerPresenter.getLinkMicItemWithLinkMicId(speaker.getUid());
                if (item != null) {
                    item.second.setCurVolume(speaker.getVolume());
                }
                streamerPresenter.callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLocalUserMicVolumeChanged(speaker.getVolume());
                    }
                });
            }
        };
        streamerPresenter.getStreamerManager().addEventHandler(linkMicEventHandler);
    }
    // </editor-fold>

}
