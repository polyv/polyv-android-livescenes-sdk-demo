package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.PLVLinkMicEventHandler;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;
import com.plv.livescenes.log.chat.PLVChatroomELog;
import com.plv.livescenes.model.PLVListUsersVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVBanIpEvent;
import com.plv.socket.event.chat.PLVSetNickEvent;
import com.plv.socket.event.chat.PLVUnshieldEvent;
import com.plv.socket.event.linkmic.PLVTeacherSetPermissionEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.event.seminar.PLVJoinDiscussEvent;
import com.plv.socket.event.seminar.PLVSetLeaderEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVClassStatusBean;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;

import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.DELAY_TO_GET_USER_LIST;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.INTERVAL_TO_GET_USER_LIST;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.TIME_OUT_JOIN_CHANNEL;

/**
 * 成员列表
 */
public class PLVMultiRoleMemberList {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVMultiRoleMemberList";
    public static final int MEMBER_LENGTH_MORE = 500;
    public static final int MEMBER_LENGTH_LESS = 30;
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //连麦列表
    @Nullable
    private PLVMultiRoleLinkMicList linkMicList;
    //成员列表数据
    private List<PLVMemberItemDataBean> memberList = new LinkedList<>();
    //连麦管理器
    @Nullable
    private IPLVLinkMicManager linkMicManager;

    //request params
    private final int memberPage = 1;
    private int memberLength;
    private boolean isRequestedData;

    //举手数量
    private int raiseHandCount;
    //举手任务列表
    private Map<String, Runnable> raiseHandMap = new HashMap<>();
    //恢复举手状态的任务列表
    private Map<String, PLVLinkMicItemDataBean> raiseHandRecoverMap = new HashMap<>();
    private String myLinkMicId;
    //是否是讲师
    private boolean isTeacherType;
    //是否要添加自己到成员列表中
    private boolean isAddMyMemberItem;
    //分组Id
    private String groupId;
    //组长用户Id
    private String groupLeaderId;
    private boolean isNoGroupIdCalled;

    //disposable
    private Disposable listUsersDisposable;
    private Disposable listUserTimerDisposable;
    //listener
    private OnMemberListListener onMemberListListener;
    private PLVSocketIOObservable.OnConnectStatusListener onConnectStatusListener;
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
    private PLVLinkMicEventListener linkMicEventListener;
    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVMultiRoleMemberList(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        this.isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        this.isAddMyMemberItem = !isTeacherType;
        if (isTeacherType) {
            memberLength = MEMBER_LENGTH_MORE;
        } else {
            memberLength = MEMBER_LENGTH_LESS;
        }
        observeSocketEvent();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void setLinkMicList(@Nullable PLVMultiRoleLinkMicList linkMicList) {
        this.linkMicList = linkMicList;
        if (linkMicList == null) {
            return;
        }
        linkMicList.addOnLinkMicListListener(new PLVMultiRoleLinkMicList.AbsOnLinkMicListListener() {
            @Override
            public boolean onUpdateLinkMicItemInfo(@NonNull PLVSocketUserBean socketUserBean, @NonNull PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isGroupLeader) {
                return updateMemberItemInfo(socketUserBean, linkMicItemDataBean, isJoinList, isGroupLeader);
            }

            @Override
            public PLVLinkMicItemDataBean onGetSavedLinkMicItem(String linkMicId) {
                Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(linkMicId);
                return item == null ? null : item.second.getLinkMicItemDataBean();
            }

            @Override
            public void syncLinkMicItem(PLVLinkMicItemDataBean linkMicItemDataBean, String userId) {
                Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(userId);
                if (item != null) {
                    PLVClassStatusBean classStatusBean = item.second.getSocketUserBean().getClassStatus();
                    if (classStatusBean != null) {
                        linkMicItemDataBean.setHasPaint(classStatusBean.hasPaint());
                        linkMicItemDataBean.setCupNum(classStatusBean.getCup());
                    }
                    item.second.setLinkMicItemDataBean(linkMicItemDataBean);//保持linkMicList和memberList的linkMicItem一致
                }
            }

            @Override
            public void onLinkMicItemInfoChanged() {
                callOnMemberListChangedWithSort();
            }

            @Override
            public void onLinkMicItemIdleChanged(String linkMicId) {
                updateMemberItemIdleStatus(linkMicId);
            }

            @Override
            public void onLinkMicItemRemove(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {
                updateMemberItemIdleStatus(linkMicItemDataBean.getLinkMicId());
            }

            @Override
            public List<String> onUpdateLinkMicItemStatus(List<PLVJoinInfoEvent> joinList, List<PLVLinkMicJoinStatus.WaitListBean> waitList) {
                return updateMemberItemStatus(joinList, waitList);
            }
        });
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setLeaderId(String leaderId) {
        if (leaderId != null) {
            checkCallLeaderChanged(leaderId, "");
        }
    }

    public boolean isLeader() {
        return isMySocketUserId(groupLeaderId);
    }

    public void requestData() {
        isRequestedData = true;
        requestMemberListApi();
    }

    public Disposable requestMemberListLess(Consumer<List<PLVSocketUserBean>> onNext) {
        return requestMemberListApiLessInner(onNext);
    }

    public PLVMemberItemDataBean getItemWithLinkMicListPos(int linkMicListPos) {
        Pair<Integer, PLVMemberItemDataBean> itemPair = getItemPairWithLinkMicListPos(linkMicListPos);
        if (itemPair != null) {
            return itemPair.second;
        }
        return null;
    }

    public int getItemPos(int linkMicListPos) {
        Pair<Integer, PLVMemberItemDataBean> itemPair = getItemPairWithLinkMicListPos(linkMicListPos);
        if (itemPair != null) {
            return itemPair.first;
        }
        return -1;
    }

    public Pair<Integer, PLVMemberItemDataBean> getItemPairWithLinkMicListPos(int linkMicListPos) {
        if (linkMicList == null || linkMicList.getItem(linkMicListPos) == null) {
            return null;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = linkMicList.getItem(linkMicListPos);
        return getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
    }

    public PLVMemberItemDataBean getItem(int memberListPos) {
        if (memberListPos < 0 || memberListPos >= memberList.size()) {
            return null;
        }
        return memberList.get(memberListPos);
    }

    public Pair<Integer, PLVMemberItemDataBean> getMemberItemWithLinkMicId(String linkMicId) {
        return getMemberItemWithLinkMicIdInner(linkMicId);
    }

    public void updateUserMuteVideo(final String linkMicId, final boolean isMute, int streamType) {
        acceptUserMuteVideo(linkMicId, isMute, streamType);
    }

    public void updateUserMuteAudio(final String linkMicId, final boolean isMute, int streamType) {
        acceptUserMuteAudio(linkMicId, isMute, streamType);
    }

    public void updateUserJoining(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (linkMicItemDataBean != null) {
            linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_JOINING);
            startJoiningTimeoutCount(linkMicItemDataBean);
            callOnMemberListChangedWithSort();
        }
    }

    public void setMyLinkMicId(String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
    }

    public void setOnMemberListListener(OnMemberListListener listener) {
        this.onMemberListListener = listener;
    }

    public List<PLVMemberItemDataBean> getData() {
        return memberList;
    }

    public void observeRTCEvent(IPLVLinkMicManager linkMicManager) {
        this.linkMicManager = linkMicManager;
        observeRTCEventInner();
    }

    public void destroy() {
        isRequestedData = false;
        memberList.clear();
        raiseHandMap.clear();
        raiseHandRecoverMap.clear();
        dispose(listUsersDisposable);
        dispose(listUserTimerDisposable);
        handler.removeCallbacksAndMessages(null);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnConnectStatusListener(onConnectStatusListener);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        if (linkMicManager != null) {
            linkMicManager.removeEventHandler(linkMicEventListener);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="成员列表API请求">
    private void requestMemberListApi() {
        dispose(listUsersDisposable);
        dispose(listUserTimerDisposable);
        if (linkMicList != null) {
            linkMicList.disposeRequestData();
        }
        final String roomId = getRoomIdCombineDiscuss();
        listUsersDisposable = PLVChatApiRequestHelper.getListUsers(roomId, memberPage, memberLength, liveRoomDataManager.getSessionId())
                .retryWhen(new PLVRxBaseRetryFunction(Integer.MAX_VALUE, 3000))
                .compose(new PLVRxBaseTransformer<PLVListUsersVO, PLVListUsersVO>())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO PLVListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(PLVListUsersVO.getCount());
                        updateMemberListWithListUsers(PLVListUsersVO.getUserlist());
                        //请求连麦列表api
                        if (linkMicList != null) {
                            linkMicList.requestData();
                        }
                        //定时请求在线列表api
                        requestMemberListApiTimer(roomId);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PLVChatroomELog.class, PLVChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }

    private void requestMemberListApiTimer(final String roomId) {
        dispose(listUserTimerDisposable);
        listUserTimerDisposable = Observable.interval(DELAY_TO_GET_USER_LIST, INTERVAL_TO_GET_USER_LIST, TimeUnit.MILLISECONDS, Schedulers.io())
                .flatMap(new Function<Long, Observable<PLVListUsersVO>>() {
                    @Override
                    public Observable<PLVListUsersVO> apply(Long aLong) throws Exception {
                        return PLVChatApiRequestHelper.getListUsers(roomId, memberPage, memberLength, liveRoomDataManager.getSessionId()).retry(1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO PLVListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(PLVListUsersVO.getCount());
                        updateMemberListWithListUsers(PLVListUsersVO.getUserlist());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PLVChatroomELog.class, PLVChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }

    private Disposable requestMemberListApiLessInner(final Consumer<List<PLVSocketUserBean>> onNext) {
        return PLVChatApiRequestHelper.getListUsers(getRoomIdCombineDiscuss(), 1, MEMBER_LENGTH_LESS, liveRoomDataManager.getSessionId())
                .map(new Function<PLVListUsersVO, List<PLVSocketUserBean>>() {
                    @Override
                    public List<PLVSocketUserBean> apply(PLVListUsersVO PLVListUsersVO) throws Exception {
                        return PLVListUsersVO.getUserlist();
                    }
                })
                .compose(new PLVRxBaseTransformer<List<PLVSocketUserBean>, List<PLVSocketUserBean>>())
                .subscribe(new Consumer<List<PLVSocketUserBean>>() {
                    @Override
                    public void accept(List<PLVSocketUserBean> userBeans) throws Exception {
                        if (onNext != null) {
                            onNext.accept(userBeans);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PLVChatroomELog.class, PLVChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="加入中状态倒计时">
    private void startJoiningTimeoutCount(final PLVLinkMicItemDataBean linkMicItemDataBean) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                linkMicItemDataBean.setStatusMethodCallListener(null);
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_WAIT);
                callOnMemberListChangedWithSort();
            }
        };
        handler.postDelayed(runnable, TIME_OUT_JOIN_CHANNEL);
        linkMicItemDataBean.setStatusMethodCallListener(new Runnable() {
            @Override
            public void run() {
                if (!linkMicItemDataBean.isJoiningStatus()) {
                    handler.removeCallbacks(runnable);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新列表信息">
    private void updateMemberItemIdleStatus(String linkMicId) {
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(linkMicId);
        if (item != null && !item.second.getLinkMicItemDataBean().isIdleStatus()) {
            item.second.getLinkMicItemDataBean().setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
            callOnMemberListChangedWithSort();
        }
    }

    private List<String> updateMemberItemStatus(List<PLVJoinInfoEvent> joinList, List<PLVLinkMicJoinStatus.WaitListBean> waitList) {
        List<String> changeStatusUidList = new ArrayList<>();
        for (PLVMemberItemDataBean plvMemberItemDataBean : memberList) {
            PLVLinkMicItemDataBean linkMicItemDataBean = plvMemberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean == null
                    || linkMicItemDataBean.isIdleStatus()
                    || isMyLinkMicId(linkMicItemDataBean.getLinkMicId())
                    || TextUtils.isEmpty(linkMicItemDataBean.getLinkMicId())) {
                continue;
            }
            String linkMicId = linkMicItemDataBean.getLinkMicId();
            boolean isExitLinkMicList = false;
            for (PLVJoinInfoEvent joinInfoEvent : joinList) {
                if (linkMicId != null && linkMicId.equals(joinInfoEvent.getUserId())) {
                    isExitLinkMicList = true;
                    break;
                }
            }
            if (!isExitLinkMicList) {
                for (PLVLinkMicJoinStatus.WaitListBean waitListBean : waitList) {
                    if (linkMicId != null && linkMicId.equals(waitListBean.getUserId())) {
                        isExitLinkMicList = true;
                        break;
                    }
                }
            }
            if (!isExitLinkMicList && !linkMicItemDataBean.isJoiningStatus()) {
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
                changeStatusUidList.add(linkMicItemDataBean.getLinkMicId());
            }
        }
        return changeStatusUidList;
    }

    private void updateMemberListWithListUsers(List<PLVSocketUserBean> socketUserBeanList) {
        List<PLVMemberItemDataBean> tempMemberList = new LinkedList<>();
        //自己的classStatus
        PLVClassStatusBean myClassStatusBean = null;
        for (int i = 0; i < socketUserBeanList.size(); i++) {
            PLVSocketUserBean socketUserBean = socketUserBeanList.get(i);
            String userId = socketUserBean.getUserId();
            if (isMySocketUserId(userId)) {
                myClassStatusBean = socketUserBean.getClassStatus();
                socketUserBeanList.remove(socketUserBean);
                i--;
                continue;//排除在线列表中的自己，使用本地的数据添加，socket未登陆成功时获取不到
            }
            if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(socketUserBean.getUserType())) {
                continue;//过滤讲师
            }
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            memberItemDataBean.setSocketUserBean(socketUserBean);
            //给新成员列表数据添加旧成员列表中保存的一些状态信息
            Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(userId);
            if (item != null) {
                PLVLinkMicItemDataBean linkMicItemDataBean = item.second.getLinkMicItemDataBean();
                if (linkMicItemDataBean != null) {
                    boolean oldLinkMicItemHasPaint = linkMicItemDataBean.isHasPaint();
                    int oldLinkMicItemCupNum = linkMicItemDataBean.getCupNum();
                    memberItemDataBean.setLinkMicItemDataBean(linkMicItemDataBean);
                    //判断是否需要通知连麦列表更新奖杯数及画笔权限
                    PLVClassStatusBean classStatusBean = socketUserBean.getClassStatus();
                    checkUpdateHasPermission(classStatusBean, linkMicItemDataBean, oldLinkMicItemHasPaint, oldLinkMicItemCupNum);
                } else {
                    //添加基础的连麦bean
                    memberItemDataBean.addBaseLinkMicBean(socketUserBean);
                }
            } else {
                //添加基础的连麦bean
                memberItemDataBean.addBaseLinkMicBean(socketUserBean);
            }
            PLVClassStatusBean classStatusBean = socketUserBean.getClassStatus();
            if (classStatusBean != null && classStatusBean.isGroupLeader()) {
                checkCallLeaderChanged(socketUserBean.getUserId(), socketUserBean.getNick());
            }
            tempMemberList.add(memberItemDataBean);
        }
        memberList = tempMemberList;
        //添加自己的信息
        addMyMemberItem(myClassStatusBean);
        //更新列表
        callOnMemberListChangedWithSort();
        //处理分组里面没有组长的情况
        if (!TextUtils.isEmpty(groupId) && groupLeaderId == null && !isNoGroupIdCalled) {
            isNoGroupIdCalled = true;
            callOnUserHasGroupLeader(false, "", null);
        }
    }

    private void addMyMemberItem(@Nullable PLVClassStatusBean classStatusBean) {
        if (isAddMyMemberItem) {
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            PLVSocketUserBean mySocketUserBean = PLVSocketWrapper.getInstance().getLoginVO().createSocketUserBean();
            if (classStatusBean != null) {
                mySocketUserBean.setClassStatus(classStatusBean);
            }
            memberItemDataBean.setSocketUserBean(mySocketUserBean);
            PLVLinkMicItemDataBean myLinkMicItem = linkMicList == null ? null : linkMicList.getMyLinkMicItemBean();
            if (myLinkMicItem != null) {
                boolean oldLinkMicItemHasPaint = myLinkMicItem.isHasPaint();
                int oldLinkMicItemCupNum = myLinkMicItem.getCupNum();
                memberItemDataBean.setLinkMicItemDataBean(myLinkMicItem);
                checkUpdateHasPermission(classStatusBean, myLinkMicItem, oldLinkMicItemHasPaint, oldLinkMicItemCupNum);
            } else {
                //添加基础的连麦bean
                memberItemDataBean.addBaseLinkMicBean(memberItemDataBean.getSocketUserBean());
            }
            if (classStatusBean != null && classStatusBean.isGroupLeader()) {
                checkCallLeaderChanged(mySocketUserBean.getUserId(), mySocketUserBean.getNick());
            }
            memberList.add(0, memberItemDataBean);
        }
    }

    private void checkUpdateHasPermission(PLVClassStatusBean classStatusBean, PLVLinkMicItemDataBean linkMicItemDataBean, boolean oldLinkMicItemHasPaint, int oldLinkMicItemCupNum) {
        if (classStatusBean != null) {
            @Nullable
            Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
            if (linkMicItem == null) {
                return;
            }
            int linkMicItemPos = linkMicItem.first;
            if (classStatusBean.hasPaint() != oldLinkMicItemHasPaint) {
                callOnUserHasPaint(isMyLinkMicId(linkMicItem.second.getLinkMicId()), linkMicItemDataBean.isHasPaint(), linkMicItemPos, -1);
            }
            if (classStatusBean.getCup() != oldLinkMicItemCupNum) {
                callOnUserGetCup(linkMicItem.second.getNick(), false, linkMicItemPos, -1);
            }
        }
    }

    private boolean updateMemberItemInfo(@NonNull PLVSocketUserBean socketUserBean, @NonNull PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isGroupLeader) {
        return updateMemberItemInfo(socketUserBean, linkMicItemDataBean, isJoinList, false, isGroupLeader);
    }

    private boolean updateMemberItemInfo(@NonNull PLVSocketUserBean socketUserBean, @NonNull PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isUpdateJoiningStatus, boolean isGroupLeader) {
        if (isGroupLeader) {
            checkCallLeaderChanged(linkMicItemDataBean.getLinkMicId(), linkMicItemDataBean.getNick());
        }
        if (isMyLinkMicId(linkMicItemDataBean.getLinkMicId()) || isMySocketUserId(linkMicItemDataBean.getLinkMicId())) {
            return false;//过滤自己
        }
        boolean hasChangedMemberList = false;
        //获取数据是否在成员列表中
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithUserId(socketUserBean.getUserId());
        if (memberItem == null || memberItem.second.getLinkMicItemDataBean() == null) {
            //如果不在成员列表中或列表中成员的连麦信息为空，则添加或补充信息
            PLVMemberItemDataBean memberItemDataBean;
            if (memberItem == null) {
                memberItemDataBean = new PLVMemberItemDataBean();
                memberItemDataBean.setSocketUserBean(socketUserBean);
                if (!PLVSocketUserConstant.USERTYPE_TEACHER.equals(socketUserBean.getUserType())) {
                    memberList.add(memberItemDataBean);//过滤讲师
                }
            } else {
                memberItemDataBean = memberItem.second;
            }
            memberItemDataBean.setLinkMicItemDataBean(linkMicItemDataBean);
            if (linkMicList != null) {
                linkMicList.updateLinkMicItemInfoWithRtcJoinList(linkMicItemDataBean, linkMicItemDataBean.getLinkMicId());
            }
            hasChangedMemberList = true;
        } else {
            memberItem.second.updateBaseLinkMicBean(linkMicItemDataBean);
            PLVLinkMicItemDataBean linkMicItemDataBeanInMemberList = memberItem.second.getLinkMicItemDataBean();
            boolean isJoiningStatus = linkMicItemDataBeanInMemberList.isJoiningStatus();
            boolean isJoinStatus = linkMicItemDataBeanInMemberList.isJoinStatus();
            boolean isWaitStatus = linkMicItemDataBeanInMemberList.isWaitStatus();
            if (isJoinList) {
                hasChangedMemberList = linkMicList != null && linkMicList.updateLinkMicItemInfoWithRtcJoinList(linkMicItemDataBeanInMemberList, linkMicItemDataBeanInMemberList.getLinkMicId());
                if (hasChangedMemberList) {
                    return true;
                }
                boolean isRtcJoinStatus = linkMicItemDataBeanInMemberList.isRtcJoinStatus();
                //更新为加入中状态
                if (!isRtcJoinStatus && !isJoinStatus && !isJoiningStatus) {
                    linkMicItemDataBeanInMemberList.setStatus(PLVLinkMicItemDataBean.STATUS_JOIN);
                    hasChangedMemberList = true;
                }
            } else {
                //更新为等待状态
                if ((!isJoiningStatus || isUpdateJoiningStatus)
                        && !isWaitStatus) {
                    linkMicItemDataBeanInMemberList.setStatus(PLVLinkMicItemDataBean.STATUS_WAIT);
                    hasChangedMemberList = true;
                }
            }
        }
        return hasChangedMemberList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="举手计时">
    private void startRaiseHandTimeoutCount(@NonNull final PLVLinkMicItemDataBean linkMicItemDataBean, final PLVClassStatusBean classStatusBean, final String userId, long timeMillis) {
        final Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                --raiseHandCount;
                raiseHandMap.remove(userId);

                linkMicItemDataBean.setRaiseHand(false);
                PLVLinkMicItemDataBean putLinkMicItem = raiseHandRecoverMap.remove(userId);
                if (putLinkMicItem != null) {
                    putLinkMicItem.setRaiseHand(false);
                }
                if (classStatusBean != null) {
                    classStatusBean.setRaiseHand(linkMicItemDataBean.isRaiseHand() ? PLVClassStatusBean.STATUS_ON : PLVClassStatusBean.STATUS_OFF);
                }

                /*发送举手的途中用户退出的情况为null*/
                @Nullable
                Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithUserId(userId);
                int memberItemPos = memberItem == null ? -1 : memberItem.first;
                @Nullable
                Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
                int linkMicItemPos = linkMicItem == null ? -1 : linkMicItem.first;
                callOnUserRaiseHand(Math.max(0, raiseHandCount), linkMicItemDataBean.isRaiseHand(), linkMicItemPos, memberItemPos);
            }
        };
        if (raiseHandMap.containsKey(userId)) {
            handler.removeCallbacks(raiseHandMap.get(userId));
        }
        raiseHandMap.put(userId, runnableTask);
        handler.postDelayed(runnableTask, timeMillis);
    }

    private void checkSetRaiseHand(@NonNull final PLVLinkMicItemDataBean linkMicItemDataBean, final PLVClassStatusBean classStatusBean, final String userId) {
        if (classStatusBean != null && classStatusBean.isRaiseHand()) {
            for (String raiseHandUserId : raiseHandMap.keySet()) {
                if (userId != null && userId.equals(raiseHandUserId)) {
                    raiseHandRecoverMap.put(userId, linkMicItemDataBean);
                    linkMicItemDataBean.setRaiseHand(true);
                    break;
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="回调方法">
    private void callOnMemberListChangedWithSort() {
        if (onMemberListListener != null) {
            onMemberListListener.onMemberListChanged(PLVStreamerPresenter.SortMemberListUtils.sort(memberList));
        }
    }

    private void callOnMemberItemChanged(int pos) {
        if (onMemberListListener != null) {
            onMemberListListener.onMemberItemChanged(pos);
        }
    }

    private void callOnMemberItemRemove(int pos) {
        if (onMemberListListener != null) {
            onMemberListListener.onMemberItemRemove(pos);
        }
    }

    private void callOnMemberItemInsert(int pos) {
        if (onMemberListListener != null) {
            onMemberListListener.onMemberItemInsert(pos);
        }
    }

    private void callOnUserRaiseHand(int raiseHandCount, boolean isRaiseHand, int linkMicListPos, int memberListPos) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserRaiseHand(raiseHandCount, isRaiseHand, linkMicListPos, memberListPos);
        }
    }

    private void callOnUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserGetCup(userNick, isByEvent, linkMicListPos, memberListPos);
        }
    }

    private void callOnUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserHasPaint(isMyself, isHasPaint, linkMicListPos, memberListPos);
        }
    }

    private void callOnUserHasGroupLeader(boolean isHasGroupLeader, String nick, String leaderId) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserHasGroupLeader(isHasGroupLeader, nick, leaderId);
        }
    }

    private void callOnLeaveDiscuss() {
        if (onMemberListListener != null) {
            onMemberListListener.onLeaveDiscuss();
        }
    }

    private void callOnUserMuteVideo(final String uid, final boolean mute, int linkMicListPos, int memberListPos) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserMuteVideo(uid, mute, linkMicListPos, memberListPos);
        }
    }

    private void callOnUserMuteAudio(final String uid, final boolean mute, int linkMicListPos, int memberListPos) {
        if (onMemberListListener != null) {
            onMemberListListener.onUserMuteAudio(uid, mute, linkMicListPos, memberListPos);
        }
    }

    private void callOnRemoteUserVolumeChanged() {
        if (onMemberListListener != null) {
            onMemberListListener.onRemoteUserVolumeChanged();
        }
    }

    private void callOnLocalUserVolumeChanged(int volume) {
        if (onMemberListListener != null) {
            onMemberListListener.onLocalUserVolumeChanged(volume);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取item方法">
    private Pair<Integer, PLVMemberItemDataBean> getMemberItemWithLinkMicIdInner(String linkMicId) {
        for (int i = 0; i < memberList.size(); i++) {
            PLVMemberItemDataBean memberItemDataBean = memberList.get(i);
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean != null) {
                String linkMicIdForIndex = linkMicItemDataBean.getLinkMicId();
                if (linkMicId != null && linkMicId.equals(linkMicIdForIndex)) {
                    return new Pair<>(i, memberItemDataBean);
                }
            } else {
                PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
                if (socketUserBean != null && linkMicId != null && linkMicId.equals(socketUserBean.getUserId())) {
                    return new Pair<>(i, memberItemDataBean);
                }
            }
        }
        return null;
    }

    private Pair<Integer, PLVMemberItemDataBean> getMemberItemWithUserId(String userId) {
        for (int i = 0; i < memberList.size(); i++) {
            PLVMemberItemDataBean memberItemDataBean = memberList.get(i);
            PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
            if (socketUserBean != null) {
                String userIdForIndex = socketUserBean.getUserId();
                if (userId != null && userId.equals(userIdForIndex)) {
                    return new Pair<>(i, memberItemDataBean);
                }
            }
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="分组讨论处理">
    private void checkCallLeaderChanged(String leaderUserId, @NonNull String nick) {
        if (leaderUserId != null && !leaderUserId.equals(groupLeaderId) && !TextUtils.isEmpty(groupId)) {
            boolean isHasGroupLeader = isMySocketUserId(leaderUserId);
            groupLeaderId = leaderUserId;
            if (!isTeacherType) {
                memberLength = isHasGroupLeader ? MEMBER_LENGTH_MORE : MEMBER_LENGTH_LESS;
            }
            if (TextUtils.isEmpty(nick) && isMySocketUserId(groupLeaderId)) {
                nick = liveRoomDataManager.getConfig().getUser().getViewerName();
            }
            callOnUserHasGroupLeader(isHasGroupLeader, nick, groupLeaderId);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private boolean isMyLinkMicId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(myLinkMicId);
    }

    private String getRoomIdCombineDiscuss() {
        if (!TextUtils.isEmpty(groupId)) {
            return groupId;
        }
        String loginRoomId = PLVSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = liveRoomDataManager.getConfig().getChannelId();//socket未登陆时，使用频道号
        }
        return loginRoomId;
    }

    private boolean isMySocketUserId(String userId) {
        return userId != null && userId.equals(liveRoomDataManager.getConfig().getUser().getViewerId());
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听socket事件">
    private void observeSocketEvent() {
        onConnectStatusListener = new PLVSocketIOObservable.OnConnectStatusListener() {
            @Override
            public void onStatus(PLVSocketStatus status) {
                //重连成功时，刷新在线列表，以更新重连期间的人员变动情况
                if (PLVSocketStatus.STATUS_RECONNECTSUCCESS == status.getStatus()) {
                    if (isRequestedData) {
                        requestMemberListApi();
                    }
                }
            }
        };
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                switch (event) {
                    //sliceId事件
                    case PLVOnSliceIDEvent.EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceIDEvent.class);
                        acceptOnSliceIDEvent(onSliceIDEvent);
                        break;
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
                    //用户请求连麦事件
                    case PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT:
                        PLVJoinRequestSEvent joinRequestSEvent = PLVGsonUtil.fromJson(PLVJoinRequestSEvent.class, message);
                        acceptJoinRequestSEvent(joinRequestSEvent);
                        break;
                    //讲师设置权限事件
                    case PLVEventConstant.LinkMic.TEACHER_SET_PERMISSION:
                        PLVTeacherSetPermissionEvent teacherSetPermissionEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVTeacherSetPermissionEvent.class);
                        acceptTeacherSetPermissionEvent(teacherSetPermissionEvent);
                        break;
                    //设置组长事件
                    case PLVEventConstant.Seminar.EVENT_SET_LEADER:
                        PLVSetLeaderEvent setLeaderEvent = PLVGsonUtil.fromJson(PLVSetLeaderEvent.class, message);
                        acceptSetLeaderEvent(setLeaderEvent);
                        break;
                    //加入讨论事件
                    case PLVEventConstant.Seminar.EVENT_JOIN_DISCUSS:
                        PLVJoinDiscussEvent joinDiscussEvent = PLVGsonUtil.fromJson(PLVJoinDiscussEvent.class, message);
                        acceptJoinDiscussEvent(joinDiscussEvent);
                        break;
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(onConnectStatusListener);
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(onMessageListener,
                PLVEventConstant.LinkMic.JOIN_REQUEST_EVENT,
                PLVEventConstant.LinkMic.JOIN_RESPONSE_EVENT,
                PLVEventConstant.LinkMic.JOIN_SUCCESS_EVENT,
                PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT,
                PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT,
                PLVEventConstant.Class.SE_SWITCH_MESSAGE,
                PLVEventConstant.Seminar.SEMINAR_EVENT,
                Socket.EVENT_MESSAGE);
    }

    private void acceptOnSliceIDEvent(PLVOnSliceIDEvent onSliceIDEvent) {
        if (onSliceIDEvent != null && !TextUtils.isEmpty(groupId)) {
            if (!onSliceIDEvent.isInDiscuss()
                    || (onSliceIDEvent.isInDiscuss() && TextUtils.isEmpty(onSliceIDEvent.getGroupId()))) {
                callOnLeaveDiscuss();//断网中途，讲师停止讨论
            } else if (groupId.equals(onSliceIDEvent.getGroupId())) {
                String leaderId = onSliceIDEvent.getLeader();
                checkCallLeaderChanged(leaderId, "");
            }
        }
    }

    private void acceptBanIpEvent(PLVBanIpEvent banIpEvent) {
        if (banIpEvent != null) {
            List<PLVSocketUserBean> shieldUsers = banIpEvent.getUserIds();
            if (shieldUsers == null) {
                return;
            }
            for (PLVSocketUserBean socketUserBean : shieldUsers) {
                final Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(true);
                    callOnMemberItemChanged(item.first);
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
                final Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(socketUserBean.getUserId());
                if (item != null) {
                    PLVSocketUserBean socketUserBeanForItem = item.second.getSocketUserBean();
                    socketUserBeanForItem.setBanned(false);
                    callOnMemberItemChanged(item.first);
                }
            }
        }
    }

    private void acceptSetNickEvent(PLVSetNickEvent setNickEvent) {
        if (setNickEvent != null && PLVSetNickEvent.STATUS_SUCCESS.equals(setNickEvent.getStatus())) {
            final Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(setNickEvent.getUserId());
            if (item != null) {
                PLVSocketUserBean socketUserBean = item.second.getSocketUserBean();
                socketUserBean.setNick(setNickEvent.getNick());
                callOnMemberItemChanged(item.first);
            }
        }
    }

    private void acceptKickEvent(PLVKickEvent kickEvent) {
        if (kickEvent != null && kickEvent.getUser() != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(kickEvent.getUser().getUserId());
            if (item != null) {
                memberList.remove(item.second);
                callOnMemberItemRemove(item.first);
            }
        }
    }

    private void acceptLoginEvent(PLVLoginEvent loginEvent) {
        if (loginEvent != null && loginEvent.getUser() != null) {
            PLVSocketUserBean socketUserBean = loginEvent.getUser();
            socketUserBean.setClassStatus(loginEvent.getClassStatus());
            if (PLVSocketUserConstant.USERSOURCE_CHATROOM.equals(socketUserBean.getUserSource())) {
                return;//过滤"userSource":"chatroom"的用户
            }
            Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(socketUserBean.getUserId());
            if (item != null || isMySocketUserId(socketUserBean.getUserId())) {//过滤存在的用户和自己
                return;
            }
            if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(socketUserBean.getUserType())) {
                return;//过滤讲师
            }
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            memberItemDataBean.setSocketUserBean(socketUserBean);
            //添加基础的连麦bean
            memberItemDataBean.addBaseLinkMicBean(socketUserBean);
            checkSetRaiseHand(memberItemDataBean.getLinkMicItemDataBean(), socketUserBean.getClassStatus(), socketUserBean.getUserId());
            memberList.add(memberItemDataBean);
            PLVStreamerPresenter.SortMemberListUtils.sort(memberList);
            final Pair<Integer, PLVMemberItemDataBean> newItem = getMemberItemWithUserId(socketUserBean.getUserId());
            if (newItem != null) {
                callOnMemberItemInsert(newItem.first);
            }
        }
    }

    private void acceptLogoutEvent(PLVLogoutEvent logoutEvent) {
        if (logoutEvent != null) {
            final Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(logoutEvent.getUserId());
            if (item != null) {
                memberList.remove(item.second);
                callOnMemberItemRemove(item.first);
            }
        }
    }

    private boolean acceptJoinRequestSEvent(PLVJoinRequestSEvent joinRequestSEvent) {
        if (joinRequestSEvent != null && joinRequestSEvent.getUser() != null) {
            PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinRequestSEvent.getUser());
            final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinRequestSEvent.getUser());
            boolean hasChanged = updateMemberItemInfo(socketUserBean, linkMicItemDataBean, false, true, false);
            //更新成员列表数据
            if (hasChanged) {
                callOnMemberListChangedWithSort();
            }
            return hasChanged;
        }
        return false;
    }

    private void acceptTeacherSetPermissionEvent(PLVTeacherSetPermissionEvent teacherSetPermissionEvent) {
        if (teacherSetPermissionEvent != null) {
            String type = teacherSetPermissionEvent.getType();
            String status = teacherSetPermissionEvent.getStatus();
            String userId = teacherSetPermissionEvent.getUserId();
            Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithUserId(userId);
            if (memberItem == null) {
                return;
            }
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItem.second.getLinkMicItemDataBean();
            if (linkMicItemDataBean == null) {
                return;
            }
            @Nullable
            Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());//要通过成员列表拿连麦uid
            int linkMicItemPos = linkMicItem == null ? -1 : linkMicItem.first;
            @Nullable
            PLVClassStatusBean classStatusBean = memberItem.second.getSocketUserBean().getClassStatus();
            if (PLVTeacherSetPermissionEvent.TYPE_CUP.equals(type)) {
                //all receive
                linkMicItemDataBean.setCupNum(linkMicItemDataBean.getCupNum() + 1);
                if (classStatusBean != null) {
                    classStatusBean.setCup(linkMicItemDataBean.getCupNum());
                }
                callOnUserGetCup(linkMicItemDataBean.getNick(), true, linkMicItemPos, memberItem.first);
            } else if (PLVTeacherSetPermissionEvent.TYPE_RAISE_HAND.equals(type)) {
                if (PLVTeacherSetPermissionEvent.STATUS_ZERO.equals(status)) {
                    return;
                }
                raiseHandCount = Math.max(0, raiseHandCount);
                if (!raiseHandMap.containsKey(userId)) {
                    raiseHandCount++;//本地保存
                }
                //teacher receive
                if (PLVTeacherSetPermissionEvent.STATUS_ONE.equals(status)) {
                    linkMicItemDataBean.setRaiseHand(true);
                }
                if (classStatusBean != null) {
                    classStatusBean.setRaiseHand(linkMicItemDataBean.isRaiseHand() ? PLVClassStatusBean.STATUS_ON : PLVClassStatusBean.STATUS_OFF);
                }
                callOnUserRaiseHand(raiseHandCount, linkMicItemDataBean.isRaiseHand(), linkMicItemPos, memberItem.first);
                if (linkMicItemDataBean.isRaiseHand()) {
                    startRaiseHandTimeoutCount(linkMicItemDataBean, classStatusBean, userId, teacherSetPermissionEvent.getRaiseHandTime());
                }
            } else if (PLVTeacherSetPermissionEvent.TYPE_PAINT.equals(type)) {
                //all receive
                if (PLVTeacherSetPermissionEvent.STATUS_ONE.equals(status)) {
                    linkMicItemDataBean.setHasPaint(true);
                } else if (PLVTeacherSetPermissionEvent.STATUS_ZERO.equals(status)) {
                    linkMicItemDataBean.setHasPaint(false);
                }
                if (classStatusBean != null) {
                    classStatusBean.setPaint(linkMicItemDataBean.isHasPaint() ? PLVClassStatusBean.STATUS_ON : PLVClassStatusBean.STATUS_OFF);
                }
                callOnUserHasPaint(isMyLinkMicId(linkMicItemDataBean.getLinkMicId()), linkMicItemDataBean.isHasPaint(), linkMicItemPos, memberItem.first);
            }
        }
    }

    private void acceptSetLeaderEvent(PLVSetLeaderEvent setLeaderEvent) {
        if (setLeaderEvent != null) {
            String userId = setLeaderEvent.getUserId();
            checkCallLeaderChanged(userId, setLeaderEvent.getNick());
        }
    }

    private void acceptJoinDiscussEvent(final PLVJoinDiscussEvent joinDiscussEvent) {
        if (joinDiscussEvent == null) {
            return;
        }
        if (groupId != null && !groupId.equals(joinDiscussEvent.getGroupId())) {
            groupId = null;//当改变分组后，之前的memberList对象需要置空groupId
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听rtc事件">
    private void observeRTCEventInner() {
        linkMicEventListener = new PLVLinkMicEventListener() {
            @Override
            public void onUserMuteVideo(String uid, boolean mute, int streamType) {
                super.onUserMuteVideo(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteVideo: " + uid + "*" + mute + "*" + streamType);
                acceptUserMuteVideo(uid, mute, streamType);
            }

            @Override
            public void onUserMuteAudio(final String uid, final boolean mute, int streamType) {
                super.onUserMuteAudio(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteAudio: " + uid + "*" + mute + "*" + streamType);
                acceptUserMuteAudio(uid, mute, streamType);
            }

            @Override
            public void onRemoteAudioVolumeIndication(PLVAudioVolumeInfo[] speakers) {
                super.onRemoteAudioVolumeIndication(speakers);
                acceptRemoteAudioVolumeIndication(speakers);
            }

            @Override
            public void onLocalAudioVolumeIndication(final PLVAudioVolumeInfo speaker) {
                super.onLocalAudioVolumeIndication(speaker);
                acceptLocalAudioVolumeIndication(speaker);
            }
        };
        if (linkMicManager != null) {
            linkMicManager.addEventHandler(linkMicEventListener);
        }
    }

    private void acceptUserMuteVideo(final String linkMicId, final boolean isMute, int streamType) {
        Pair<Integer, PLVLinkMicItemDataBean> item = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(linkMicId);
        if (item == null || !item.second.includeStreamType(streamType)) {
            return;
        }
        item.second.setMuteVideo(isMute);
        final int linkMicListPos = item.first;
        @Nullable
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicId);
        final int memberListPos = memberItem == null ? -1 : memberItem.first;
        callOnUserMuteVideo(linkMicId, isMute, linkMicListPos, memberListPos);
    }

    private void acceptUserMuteAudio(final String linkMicId, final boolean isMute, int streamType) {
        Pair<Integer, PLVLinkMicItemDataBean> item = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(linkMicId);
        if (item == null || !item.second.includeStreamType(streamType)) {
            return;
        }
        item.second.setMuteAudio(isMute);
        final int linkMicListPos = item.first;
        @Nullable
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicId);
        final int memberListPos = memberItem == null ? -1 : memberItem.first;
        callOnUserMuteAudio(linkMicId, isMute, linkMicListPos, memberListPos);
    }

    private void acceptRemoteAudioVolumeIndication(PLVLinkMicEventHandler.PLVAudioVolumeInfo[] speakers) {
        for (PLVMemberItemDataBean memberItemDataBean : memberList) {
            @Nullable PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean == null) {
                continue;
            }
            String linkMicId = linkMicItemDataBean.getLinkMicId();
            if (isMyLinkMicId(linkMicId)) {
                continue;
            }
            boolean hitInVolumeInfoList = false;
            for (PLVLinkMicEventHandler.PLVAudioVolumeInfo audioVolumeInfo : speakers) {
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
        callOnRemoteUserVolumeChanged();
    }

    private void acceptLocalAudioVolumeIndication(PLVLinkMicEventHandler.PLVAudioVolumeInfo speaker) {
        Pair<Integer, PLVLinkMicItemDataBean> item = linkMicList == null ? null : linkMicList.getLinkMicItemWithLinkMicId(speaker.getUid());//由于自己可能不存在成员列表，所以使用连麦列表获取
        if (item != null) {
            item.second.setCurVolume(speaker.getVolume());
        }
        callOnLocalUserVolumeChanged(speaker.getVolume());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 成员列表监听器">
    public interface OnMemberListListener {
        /**
         * 更新成员列表
         */
        void onMemberListChanged(List<PLVMemberItemDataBean> dataBeans);

        /**
         * 更新成员列表item
         */
        void onMemberItemChanged(int pos);

        /**
         * 移除成员列表item
         */
        void onMemberItemRemove(int pos);

        /**
         * 添加成员列表item
         */
        void onMemberItemInsert(int pos);

        /**
         * 响应本地用户麦克风音量变化
         */
        void onLocalUserVolumeChanged(int volume);

        /**
         * 响应远端用户麦克风音量变化
         */
        void onRemoteUserVolumeChanged();

        /**
         * 响应用户开关视频
         *
         * @param uid            用户id
         * @param mute           true表示关闭视频，false表示开启视频
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserMuteVideo(final String uid, final boolean mute, int linkMicListPos, int memberListPos);

        /**
         * 响应用户开关音频
         *
         * @param uid            用户id
         * @param mute           true表示关闭音频，false表示开启音频
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserMuteAudio(final String uid, final boolean mute, int linkMicListPos, int memberListPos);

        /**
         * 响应用户举手
         *
         * @param raiseHandCount 举手的数量
         * @param isRaiseHand    true：举手，false：结束举手
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand, int linkMicListPos, int memberListPos);

        /**
         * 用户获取到奖杯
         *
         * @param isByEvent      是否是通过事件获取到的奖杯
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos);

        /**
         * 响应用户被授权画笔
         *
         * @param isMyself       是否是自己
         * @param isHasPaint     true：被授权，false：被取消授权
         * @param linkMicListPos 连麦列表中的位置，连麦列表中没有对应数据时为-1
         * @param memberListPos  成员列表中的位置，成员列表中没有对应数据时为-1
         */
        void onUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos);

        /**
         * 响应用户被授权组长
         *
         * @param isHasGroupLeader true：自己当前被授权，false：自己当前没有被授权
         * @param nick             被授权用户的昵称
         * @param leaderId         组长Id，为null表示分组里没有组长
         */
        void onUserHasGroupLeader(boolean isHasGroupLeader, String nick, @Nullable String leaderId);

        /**
         * 离开讨论
         */
        void onLeaveDiscuss();
    }
    // </editor-fold>
}
