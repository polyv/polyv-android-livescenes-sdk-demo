package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVMicphoneStatus;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicEventListener;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinLeaveSEvent;
import com.plv.socket.event.linkmic.PLVOpenMicrophoneEvent;
import com.plv.socket.event.ppt.PLVFinishClassEvent;
import com.plv.socket.event.ppt.PLVOnSliceIDEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVClassStatusBean;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.socket.client.Socket;

import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.DELAY_TO_GET_LINK_MIC_LIST;
import static com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model.PLVMultiRoleLinkMicConstant.INTERVAL_TO_GET_LINK_MIC_LIST;

/**
 * 连麦列表
 */
public class PLVMultiRoleLinkMicList {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVMultiRoleLinkMicList";
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //连麦管理器
    @Nullable
    private IPLVLinkMicManager linkMicManager;
    //连麦列表数据
    private List<PLVLinkMicItemDataBean> linkMicList = new LinkedList<>();
    //加入rtc的列表数据
    private Map<String, PLVLinkMicItemDataBean> rtcJoinMap = new HashMap<>();
    //记录的讲师/组长屏幕流状态
    private Map<String, Boolean> teacherScreenStreamMap = new HashMap<>();

    //我的连麦item
    @Nullable
    private PLVLinkMicItemDataBean myLinkMicItemBean;
    //从onSliceId事件保存的信息
    private PLVClassStatusBean myClassStatusBeanOnSliceId;
    private String myLinkMicId;
    private boolean isTeacherType;
    //分组的组长Id
    private String groupLeaderId;

    //disposable
    private Disposable linkMicListTimerDisposable;
    private Disposable linkMicListOnceDisposable;
    //listener
    private List<OnLinkMicListListener> onLinkMicListListeners = new ArrayList<>();
    private PLVSocketMessageObserver.OnMessageListener onMessageListener;
    private PLVLinkMicEventListener linkMicEventListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVMultiRoleLinkMicList(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        this.isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        observeSocketEvent();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public List<PLVLinkMicItemDataBean> getData() {
        return linkMicList;
    }

    public Map<String, PLVLinkMicItemDataBean> getRtcJoinMap() {
        return rtcJoinMap;
    }

    public void notifyLeaderChanged(String groupLeaderId) {
        this.groupLeaderId = groupLeaderId;
        sortLinkMicList(groupLeaderId);
    }

    public void requestData() {
        requestLinkMicListApi();
    }

    public PLVLinkMicItemDataBean getItem(int linkMicListPos) {
        if (linkMicListPos < 0 || linkMicListPos >= linkMicList.size()) {
            return null;
        }
        return linkMicList.get(linkMicListPos);
    }

    public void disposeRequestData() {
        dispose(linkMicListTimerDisposable);
    }

    public void observeRTCEvent(IPLVLinkMicManager linkMicManager) {
        this.linkMicManager = linkMicManager;
        observeRTCEventInner();
    }

    public void addMyItemToLinkMicList(boolean curEnableLocalVideo, boolean curEnableLocalAudio) {
        addMyItemToLinkMicListInner(curEnableLocalVideo, curEnableLocalAudio);
    }

    public void removeMyItemToLinkMicList() {
        removeMyItemToLinkMicListInner();
    }

    public boolean updateLinkMicItemInfoWithRtcJoinList(PLVLinkMicItemDataBean linkMicItemDataBean, final String linkMicUid) {
        return updateLinkMicItemInfoWithRtcJoinListInner(linkMicItemDataBean, linkMicUid);
    }

    public Pair<Integer, PLVLinkMicItemDataBean> getLinkMicItemWithLinkMicId(String linkMicId) {
        return getLinkMicItemWithLinkMicIdInner(linkMicId);
    }

    public void addOnLinkMicListListener(OnLinkMicListListener listListener) {
        if (listListener != null && !onLinkMicListListeners.contains(listListener)) {
            onLinkMicListListeners.add(listListener);
        }
    }

    public void setMyLinkMicId(String myLinkMicId) {
        this.myLinkMicId = myLinkMicId;
        createMyLinkMicItem(myLinkMicId);
    }

    @Nullable
    public PLVLinkMicItemDataBean getMyLinkMicItemBean() {
        return myLinkMicItemBean;
    }

    public void destroy() {
        linkMicList.clear();
        rtcJoinMap.clear();
        cleanTeacherScreenStream();
        onLinkMicListListeners.clear();
        dispose(linkMicListTimerDisposable);
        dispose(linkMicListOnceDisposable);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(onMessageListener);
        if (linkMicManager != null) {
            linkMicManager.removeEventHandler(linkMicEventListener);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦列表API请求">
    private void requestLinkMicListApi() {
        dispose(linkMicListTimerDisposable);
        linkMicListTimerDisposable = PLVRxTimer.timer(DELAY_TO_GET_LINK_MIC_LIST, INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                acceptGetLinkMicListStatus();
            }
        });
    }

    private void requestLinkMicListApiOnce() {
        dispose(linkMicListOnceDisposable);
        linkMicListOnceDisposable = PLVRxTimer.delay(DELAY_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                acceptGetLinkMicListStatus();
            }
        });
    }

    private void acceptGetLinkMicListStatus() {
        callbackToListener(new ListenerRunnable() {
            @Override
            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                linkMicListListener.onGetLinkMicListStatus(liveRoomDataManager.getSessionId(), new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
                    @Override
                    public void onSuccess(PLVLinkMicJoinStatus data) {
                        PLVCommonLog.d(TAG, "requestLinkMicListFromServer.onSuccess->\n" + data.toString());
                        updateLinkMicListWithJoinStatus(data);
                    }

                    @Override
                    public void onFail(PLVLinkMicHttpRequestException throwable) {
                        super.onFail(throwable);
                        PLVCommonLog.exception(throwable);
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="增删自己的连麦item">
    private void addMyItemToLinkMicListInner(boolean curEnableLocalVideo, boolean curEnableLocalAudio) {
        Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(myLinkMicId);
        if (linkMicItem == null && myLinkMicItemBean != null) {
            myLinkMicItemBean.setMuteVideo(!curEnableLocalVideo);
            myLinkMicItemBean.setMuteAudio(!curEnableLocalAudio);
            myLinkMicItemBean.setStatus(PLVLinkMicItemDataBean.STATUS_RTC_JOIN);
            int addIndex = isTeacherType ? 0 : linkMicList.size();
            if (isLeaderId(myLinkMicId)) {
                addIndex = 0;
                for (int i = 0; i < linkMicList.size(); i++) {
                    if (linkMicList.get(i).isTeacher()) {
                        addIndex = i + 1;
                    }
                }
            }
            linkMicList.add(addIndex, myLinkMicItemBean);
            if (myClassStatusBeanOnSliceId != null) {
                myLinkMicItemBean.setHasPaint(myClassStatusBeanOnSliceId.hasPaint());
                myLinkMicItemBean.setCupNum(myClassStatusBeanOnSliceId.getCup());
            }
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    String userId = liveRoomDataManager.getConfig().getUser().getViewerId();
                    linkMicListListener.syncLinkMicItem(myLinkMicItemBean, userId);
                }
            });
            final int finalAddIndex = addIndex;
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    //更新连麦列表
                    linkMicListListener.onLinkMicItemInsert(myLinkMicItemBean, finalAddIndex);
                }
            });
            //连麦状态改变，更新成员列表
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    linkMicListListener.onLinkMicItemInfoChanged();
                }
            });
        }
    }

    private void removeMyItemToLinkMicListInner() {
        final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(myLinkMicId);
        if (linkMicItem != null) {
            linkMicList.remove(linkMicItem.second);
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    //更新连麦列表
                    linkMicListListener.onLinkMicItemRemove(linkMicItem.second, linkMicItem.first);
                }
            });
        }
    }

    private void createMyLinkMicItem(String myLinkMicId) {
        if (myLinkMicItemBean == null && myLinkMicId != null) {
            myLinkMicItemBean = new PLVLinkMicItemDataBean();
            myLinkMicItemBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
            myLinkMicItemBean.setLinkMicId(myLinkMicId);
            myLinkMicItemBean.setActor(liveRoomDataManager.getConfig().getUser().getActor());
            myLinkMicItemBean.setNick(liveRoomDataManager.getConfig().getUser().getViewerName());
            myLinkMicItemBean.setUserType(liveRoomDataManager.getConfig().getUser().getViewerType());
            myLinkMicItemBean.setPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新列表信息">
    private void updateLinkMicListWithJoinStatus(PLVLinkMicJoinStatus data) {
        final List<PLVJoinInfoEvent> joinList = data.getJoinList();
        final List<PLVLinkMicJoinStatus.WaitListBean> waitList = data.getWaitList();

        //嘉宾可能被挂断连麦后，还一直留在连麦列表里。不过我们可以通过他的voice字段是否=1来区分他是否有上麦。
        Iterator<PLVJoinInfoEvent> joinInfoEventIterator = joinList.iterator();
        while (joinInfoEventIterator.hasNext()) {
            PLVJoinInfoEvent plvJoinInfoEvent = joinInfoEventIterator.next();
            if (PLVSocketUserConstant.USERTYPE_GUEST.equals(plvJoinInfoEvent.getUserType()) && !plvJoinInfoEvent.getClassStatus().isVoice()) {
                //没有上麦，就从joinList中移除，添加到waitList。
                joinInfoEventIterator.remove();
                waitList.add(PLVLinkMicDataMapper.map2WaitListBean(plvJoinInfoEvent));
                PLVCommonLog.d(TAG, String.format(Locale.US, "guest user [%s] lies in joinList but not join at all, so we move him to waitList manually.", plvJoinInfoEvent.toString()));
            }
        }

        final boolean[] hasChangedLinkMicList = new boolean[1];
        //更新成员列表中的连麦状态相关数据
        callbackToListener(new ListenerRunnable() {
            @Override
            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                List<String> linkMicUidList = linkMicListListener.onUpdateLinkMicItemStatus(joinList, waitList);
                if (linkMicUidList != null && linkMicList.size() > 0) {
                    hasChangedLinkMicList[0] = true;
                    for (String linkMicUid : linkMicUidList) {
                        rtcJoinMap.remove(linkMicUid);
                    }
                }
            }
        });
        //遍历加入状态的连麦列表
        for (PLVJoinInfoEvent joinInfoEvent : joinList) {
            final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinInfoEvent);
            final PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinInfoEvent);
            final boolean isGroupLeader = joinInfoEvent.getClassStatus() != null && joinInfoEvent.getClassStatus().isGroupLeader();
            //补充或更新成员列表中的数据信息
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    boolean result = linkMicListListener.onUpdateLinkMicItemInfo(socketUserBean, linkMicItemDataBean, true, isGroupLeader);
                    if (result) {
                        hasChangedLinkMicList[0] = true;
                    }
                }
            });
        }
        //移除本地连麦列表不在服务器列表中的用户数据
        removeLinkMicItemNoExistServer(joinList);
        //按照服务器的索引排序连麦列表
        sortLinkMicList(joinList);
        //遍历等待状态的连麦列表
        for (PLVLinkMicJoinStatus.WaitListBean waitListBean : waitList) {
            final PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(waitListBean);
            final PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(waitListBean);
            //补充或更新成员列表中的数据信息
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    boolean result = linkMicListListener.onUpdateLinkMicItemInfo(socketUserBean, linkMicItemDataBean, false, false);
                    if (result) {
                        hasChangedLinkMicList[0] = true;
                    }
                }
            });
        }
        //更新成员列表数据
        if (hasChangedLinkMicList[0]) {
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    linkMicListListener.onLinkMicItemInfoChanged();
                }
            });
        }
    }

    private void removeLinkMicItemNoExistServer(List<PLVJoinInfoEvent> joinList) {
        Iterator<PLVLinkMicItemDataBean> linkMicItemDataBeanIterator = linkMicList.iterator();
        int i = 0;
        while (linkMicItemDataBeanIterator.hasNext()) {
            final PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItemDataBeanIterator.next();
            String linkMicId = linkMicItemDataBean.getLinkMicId();
            boolean isExistServerList = false;
            for (PLVJoinInfoEvent joinInfoEvent : joinList) {
                if (linkMicId != null && linkMicId.equals(joinInfoEvent.getUserId())) {
                    isExistServerList = true;
                    break;
                }
            }
            if (!isExistServerList && !isMyLinkMicId(linkMicId)) {
                //这里注意linkMicList的data每remove一个就要调一次onLinkMicItemRemove方法通知视图更新
                linkMicItemDataBeanIterator.remove();
                final int finalI = i;
                callbackToListener(new ListenerRunnable() {
                    @Override
                    public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                        linkMicListListener.onLinkMicItemRemove(linkMicItemDataBean, finalI);
                    }
                });
                i--;
            }
            i++;
        }
        for (Map.Entry<String, Boolean> teacherScreenStreamEntry : teacherScreenStreamMap.entrySet()) {
            boolean value = teacherScreenStreamEntry.getValue();
            if (!value) {
                continue;
            }
            String key = teacherScreenStreamEntry.getKey();
            boolean isExistServerList = false;
            for (PLVJoinInfoEvent joinInfoEvent : joinList) {
                if (key != null && key.equals(joinInfoEvent.getUserId())) {
                    isExistServerList = true;
                    break;
                }
            }
            if (!isExistServerList && !isMyLinkMicId(key)) {
                callOnTeacherScreenStream(key, false);
            }
        }
    }

    private void cleanTeacherScreenStream() {
        for (Map.Entry<String, Boolean> teacherScreenStreamEntry : teacherScreenStreamMap.entrySet()) {
            boolean value = teacherScreenStreamEntry.getValue();
            String key = teacherScreenStreamEntry.getKey();
            if (!value || isMyLinkMicId(key)) {
                continue;
            }
            callOnTeacherScreenStream(key, false);
        }
        teacherScreenStreamMap.clear();
    }

    private void sortLinkMicList(String groupLeaderId) {
        if (groupLeaderId == null) {
            return;
        }
        int position = -1;
        int teacherPosition = -1;
        int leaderPosition = -1;
        for (PLVLinkMicItemDataBean linkMicItemDataBean : linkMicList) {
            position++;
            if (linkMicItemDataBean.isTeacher()) {
                teacherPosition = position;
            }
            if (linkMicItemDataBean.getLinkMicId() != null
                    && linkMicItemDataBean.getLinkMicId().equals(groupLeaderId)) {
                leaderPosition = position;
            }
        }
        if (leaderPosition != -1 && leaderPosition != teacherPosition && leaderPosition - teacherPosition != 1) {
            PLVLinkMicItemDataBean linkMicItemDataBean = linkMicList.remove(leaderPosition);
            if (leaderPosition > teacherPosition) {
                linkMicList.add(teacherPosition + 1, linkMicItemDataBean);
            } else {
                linkMicList.add(teacherPosition, linkMicItemDataBean);
            }
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    linkMicListListener.onLinkMicListChanged(linkMicList);
                }
            });
        }
    }

    private void sortLinkMicList(List<PLVJoinInfoEvent> joinList) {
        boolean isNeedSort = false;
        PLVLinkMicItemDataBean[] sortLinkMicArr = new PLVLinkMicItemDataBean[linkMicList.size()];
        List<PLVJoinInfoEvent> copyJoinList = new ArrayList<>(joinList);
        Iterator<PLVJoinInfoEvent> joinListIterator = copyJoinList.iterator();
        int joinListPosition = -1;
        int groupLeaderPosition = -1;
        while (joinListIterator.hasNext()) {
            joinListPosition++;
            PLVJoinInfoEvent joinInfoEvent = joinListIterator.next();
            String linkMicId = joinInfoEvent.getUserId();
            if (linkMicId != null && linkMicId.equals(groupLeaderId)) {
                groupLeaderPosition = joinListPosition;
            }
            boolean isExistLocalList = false;
            for (PLVLinkMicItemDataBean linkMicItem : linkMicList) {
                if (linkMicId != null && linkMicId.equals(linkMicItem.getLinkMicId())) {
                    isExistLocalList = true;
                    break;
                }
            }
            if (!isExistLocalList || PLVSocketUserConstant.USERTYPE_TEACHER.equals(joinInfoEvent.getUserType())) {
                joinListIterator.remove();
                joinListPosition--;
            }
        }
        if (groupLeaderPosition != -1) {
            PLVJoinInfoEvent joinInfoEvent = copyJoinList.remove(groupLeaderPosition);
            copyJoinList.add(0, joinInfoEvent);
        }
        List<PLVLinkMicItemDataBean> sortLinkMicList = new ArrayList<>();
        int linkMicItemSortIndex = -1;
        for (PLVLinkMicItemDataBean linkMicItem : linkMicList) {
            if (linkMicItem.isTeacher() || linkMicItem.getLinkMicId() == null) {
                sortLinkMicList.add(0, linkMicItem);
                continue;
            }
            linkMicItemSortIndex++;
            String linkMicItemId = linkMicItem.getLinkMicId();
            int sortIndex = linkMicItemSortIndex;
            int joinInfoIndex = -1;
            for (PLVJoinInfoEvent joinInfoEvent : copyJoinList) {
                joinInfoIndex++;
                String joinInfoId = joinInfoEvent.getUserId();
                if (linkMicItemId.equals(joinInfoId)) {
                    if (linkMicItemSortIndex != joinInfoIndex) {
                        sortIndex = joinInfoIndex;
                        isNeedSort = true;
                    }
                }
            }
            for (int i = sortIndex; i < sortLinkMicArr.length; i++) {
                if (sortLinkMicArr[i] == null) {
                    sortLinkMicArr[i] = linkMicItem;
                    break;
                }
            }
        }
        if (isNeedSort) {
            sortLinkMicList.addAll(Arrays.asList(sortLinkMicArr).subList(0, sortLinkMicArr.length - sortLinkMicList.size()));
            linkMicList.clear();
            linkMicList.addAll(sortLinkMicList);
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    linkMicListListener.onLinkMicListChanged(linkMicList);
                }
            });
        }
    }

    private boolean updateLinkMicItemInfoWithRtcJoinListInner(final PLVLinkMicItemDataBean linkMicItemDataBean, final String linkMicUid) {
        if (linkMicItemDataBean == null) {
            return false;
        }
        boolean hasChangedLinkMicListItem = false;
        for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
            String uid = linkMicItemDataBeanEntry.getKey();
            PLVLinkMicItemDataBean linkMicItemBean = linkMicItemDataBeanEntry.getValue();
            if (linkMicUid != null && linkMicUid.equals(uid)) {
                if (!linkMicItemDataBean.isRtcJoinStatus()) {
                    linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_RTC_JOIN);
                    updateLinkMicItemMediaStatus(linkMicItemBean, linkMicItemDataBean);
                    hasChangedLinkMicListItem = true;
                }
                final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
                if (linkMicItem == null) {
                    int addIndex = linkMicItemDataBean.isTeacher() ? 0 : linkMicList.size();
                    if (isLeaderId(linkMicItemDataBean.getLinkMicId())) {
                        addIndex = 0;
                        for (int i = 0; i < linkMicList.size(); i++) {
                            if (linkMicList.get(i).isTeacher()) {
                                addIndex = i + 1;
                            }
                        }
                    }
                    linkMicList.add(addIndex, linkMicItemDataBean);
                    if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX == linkMicItemBean.getStreamType()) {
                        if (isTeacherLinkMicId(linkMicUid) || isLeaderId(linkMicUid)) {
                            linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_CAMERA);
                            callOnTeacherScreenStream(linkMicUid, true);
                        } else {
                            linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN);
                        }
                    } else if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN == linkMicItemBean.getStreamType()
                            && (isTeacherLinkMicId(linkMicUid) || isLeaderId(linkMicUid))) {
                        callOnTeacherScreenStream(linkMicUid, true);
                        return false;
                    } else {
                        linkMicItemDataBean.setStreamType(linkMicItemBean.getStreamType());
                    }
                    updateLinkMicItemMediaStatus(linkMicItemBean, linkMicItemDataBean);
                    final int finalAddIndex = addIndex;
                    callbackToListener(new ListenerRunnable() {
                        @Override
                        public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                            linkMicListListener.onLinkMicItemInsert(linkMicItemDataBean, finalAddIndex);
                        }
                    });
                }
                break;
            }
        }
        return hasChangedLinkMicListItem;
    }

    private void updateLinkMicItemMediaStatus(PLVLinkMicItemDataBean rtcJoinLinkMicItem, PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (rtcJoinLinkMicItem == null || linkMicItemDataBean == null) {
            return;
        }
        PLVLinkMicItemDataBean.MuteMedia videoMuteMedia;
        PLVLinkMicItemDataBean.MuteMedia audioMuteMedia;
        if ((videoMuteMedia = rtcJoinLinkMicItem.getMuteVideoInRtcJoinList(linkMicItemDataBean.getStreamType())) != null) {
            //如果之前有保存过连麦用户媒体的状态，则使用
            linkMicItemDataBean.setMuteVideo(videoMuteMedia.isMute());
        } else {
            if (!linkMicItemDataBean.isGuest()) {//嘉宾可以在音频模式下使用摄像头
                //根据音视频连麦类型，设置连麦成员的muteVideo状态
                linkMicItemDataBean.setMuteVideo(!PLVLinkMicEventSender.getInstance().isVideoLinkMicType());
            } else {
                linkMicItemDataBean.setMuteVideo(false);
            }
        }
        if ((audioMuteMedia = rtcJoinLinkMicItem.getMuteAudioInRtcJoinList(linkMicItemDataBean.getStreamType())) != null) {
            //如果之前有保存过连麦用户媒体的状态，则使用
            linkMicItemDataBean.setMuteAudio(audioMuteMedia.isMute());
        } else {
            //连麦的用户muteAudio默认为false
            linkMicItemDataBean.setMuteAudio(false);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private Pair<Integer, PLVLinkMicItemDataBean> getLinkMicItemWithLinkMicIdInner(String linkMicId) {
        for (int i = 0; i < linkMicList.size(); i++) {
            PLVLinkMicItemDataBean linkMicItemDataBean = linkMicList.get(i);
            String linkMicIdForIndex = linkMicItemDataBean.getLinkMicId();
            if (linkMicId != null && linkMicId.equals(linkMicIdForIndex)) {
                return new Pair<>(i, linkMicItemDataBean);
            }
        }
        return null;
    }

    private boolean isTeacherLinkMicId(final String linkMicUid) {
        final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
        if (linkMicItem != null) {
            PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItem.second;
            return linkMicItemDataBean.isTeacher();
        } else {
            final boolean[] isTeacherLinkMicId = {false};
            callbackToListener(new ListenerRunnable() {
                @Override
                public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                    PLVLinkMicItemDataBean linkMicItemDataBean = linkMicListListener.onGetSavedLinkMicItem(linkMicUid);
                    if (linkMicItemDataBean != null && !isTeacherLinkMicId[0]) {
                        isTeacherLinkMicId[0] = linkMicItemDataBean.isTeacher();
                    }
                }
            });
            return isTeacherLinkMicId[0];
        }
    }

    private boolean isLeaderId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(groupLeaderId);
    }

    private boolean isMyLinkMicId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(myLinkMicId);
    }

    private void callOnTeacherScreenStream(final String linkMicId, final boolean isOpen) {
        if (teacherScreenStreamMap.containsKey(linkMicId)) {
            boolean oldState = teacherScreenStreamMap.get(linkMicId);
            if (oldState == isOpen) {
                return;
            }
        }
        teacherScreenStreamMap.put(linkMicId, isOpen);
        callbackToListener(new ListenerRunnable() {
            @Override
            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
                linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN);
                linkMicItemDataBean.setLinkMicId(linkMicId);
                linkMicListListener.onTeacherScreenStream(linkMicItemDataBean, isOpen);
            }
        });
    }

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听socket事件">
    private void observeSocketEvent() {
        onMessageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                switch (event) {
                    //sliceId事件
                    case PLVOnSliceIDEvent.EVENT:
                        PLVOnSliceIDEvent onSliceIDEvent = PLVEventHelper.toEventModel(listenEvent, event, message, PLVOnSliceIDEvent.class);
                        acceptOnSliceIDEvent(onSliceIDEvent);
                        break;
                    //用户离开连麦事件
                    case PLVEventConstant.LinkMic.JOIN_LEAVE_EVENT:
                        PLVJoinLeaveSEvent joinLeaveSEvent = PLVGsonUtil.fromJson(PLVJoinLeaveSEvent.class, message);
                        acceptJoinLeaveSEvent(joinLeaveSEvent);
                        break;
                    //嘉宾/互动学堂的学生 同意/拒绝连麦事件
                    case PLVEventConstant.LinkMic.JOIN_ANSWER_EVENT:
                        PLVJoinAnswerSEvent joinAnswerSEvent = PLVGsonUtil.fromJson(PLVJoinAnswerSEvent.class, message);
                        acceptJoinAnswerSEvent(joinAnswerSEvent);
                        break;
                    //①讲师开启/关闭连麦；②讲师将某个人下麦了
                    case PLVEventConstant.LinkMic.EVENT_OPEN_MICROPHONE:
                        PLVMicphoneStatus micPhoneStatus = PLVGsonUtil.fromJson(PLVMicphoneStatus.class, message);
                        acceptMicphoneStatusEvent(micPhoneStatus);
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
                PLVEventConstant.Seminar.SEMINAR_EVENT,
                Socket.EVENT_MESSAGE);
    }

    private void acceptOnSliceIDEvent(PLVOnSliceIDEvent onSliceIDEvent) {
        if (onSliceIDEvent != null && onSliceIDEvent.getData() != null) {
            PLVClassStatusBean classStatusBean = onSliceIDEvent.getClassStatus();
            if (classStatusBean != null) {
                myClassStatusBeanOnSliceId = classStatusBean;
                @Nullable final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(myLinkMicId);
                if (linkMicItem == null) {
                    return;
                }
                boolean oldLinkMicItemHasPaint = linkMicItem.second.isHasPaint();
                int oldLinkMicItemCupNum = linkMicItem.second.getCupNum();
                final int linkMicItemPos = linkMicItem.first;
                if (classStatusBean.hasPaint() != oldLinkMicItemHasPaint) {
                    linkMicItem.second.setHasPaint(classStatusBean.hasPaint());
                    callbackToListener(new ListenerRunnable() {
                        @Override
                        public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                            //因为自己不出现在成员列表，故不用获取成员列表索引更新
                            linkMicListListener.onUserHasPaint(true, linkMicItem.second.isHasPaint(), linkMicItemPos, -1);
                        }
                    });
                }
                if (classStatusBean.getCup() != oldLinkMicItemCupNum) {
                    linkMicItem.second.setCupNum(classStatusBean.getCup());
                    callbackToListener(new ListenerRunnable() {
                        @Override
                        public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                            linkMicListListener.onUserGetCup(linkMicItem.second.getNick(), false, linkMicItemPos, -1);
                        }
                    });
                }
            }
            if (classStatusBean == null || !classStatusBean.isVoice()) {
                if (!isTeacherType) {
                    acceptUserJoinLeave(myLinkMicId);
                }
            }
        }
    }

    private void acceptJoinLeaveSEvent(PLVJoinLeaveSEvent joinLeaveSEvent) {
        if (joinLeaveSEvent != null && joinLeaveSEvent.getUser() != null) {
            acceptUserJoinLeave(joinLeaveSEvent.getUser().getUserId());
        }
    }

    private void acceptJoinAnswerSEvent(PLVJoinAnswerSEvent joinAnswerSEvent) {
        if (joinAnswerSEvent != null) {
            final String linkMicUid = joinAnswerSEvent.getUserId();
            if (joinAnswerSEvent.isRefuse() || !joinAnswerSEvent.isResult()) {
                callbackToListener(new ListenerRunnable() {
                    @Override
                    public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                        linkMicListListener.onLinkMicItemIdleChanged(linkMicUid);
                    }
                });
                acceptUserJoinLeave(linkMicUid);
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
                    acceptUserJoinLeave(userId);
                }
            }
        }
    }

    private void acceptFinishClassEvent(PLVFinishClassEvent finishClassEvent) {
        if (!isTeacherType) {
            acceptUserJoinLeave(myLinkMicId);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听rtc事件">
    private void observeRTCEventInner() {
        linkMicEventListener = new PLVLinkMicEventListener() {
            @Override
            public void onUserOffline(String uid) {
                super.onUserOffline(uid);
                PLVCommonLog.d(TAG, "onUserOffline: " + uid);
            }

            @Override
            public void onUserJoined(String uid) {
                super.onUserJoined(uid);
                PLVCommonLog.d(TAG, "onUserJoined: " + uid);
            }

            @Override
            public void onUserMuteVideo(String uid, boolean mute, int streamType) {
                super.onUserMuteVideo(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteVideo: " + uid + "*" + mute + "*" + streamType);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteVideoInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute, streamType));
                    }
                }
            }

            @Override
            public void onUserMuteAudio(final String uid, final boolean mute, int streamType) {
                super.onUserMuteAudio(uid, mute);
                PLVCommonLog.d(TAG, "onUserMuteAudio: " + uid + "*" + mute + "*" + streamType);
                for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
                    if (uid != null && uid.equals(linkMicItemDataBeanEntry.getKey())) {
                        linkMicItemDataBeanEntry.getValue().setMuteAudioInRtcJoinList(new PLVLinkMicItemDataBean.MuteMedia(mute, streamType));
                    }
                }
            }

            @Override
            public void onRemoteStreamOpen(String uid, @PLVLinkMicConstant.RenderStreamTypeAnnotation int streamType) {
                super.onRemoteStreamOpen(uid, streamType);
                PLVCommonLog.d(TAG, "onRemoteStreamOpen: " + uid + "*" + streamType);
                acceptUserJoinChannel(uid, streamType);
            }

            @Override
            public void onRemoteStreamClose(String uid, @PLVLinkMicConstant.RenderStreamTypeAnnotation int streamType) {
                super.onRemoteStreamClose(uid, streamType);
                PLVCommonLog.d(TAG, "onRemoteStreamClose: " + uid + "*" + streamType);
                acceptUserJoinLeave(uid, streamType);
            }
        };
        if (linkMicManager != null) {
            linkMicManager.addEventHandler(linkMicEventListener);
        }
    }

    private void acceptUserJoinLeave(String linkMicUid) {
        acceptUserJoinLeave(linkMicUid, PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
    }

    private void acceptUserJoinLeave(final String linkMicUid, final int streamType) {
        Runnable userJoinLeaveTask = new Runnable() {
            @Override
            public void run() {
                final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
                if (linkMicItem != null
                        && (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX == streamType
                        || linkMicItem.second.getStreamType() == streamType)) {
                    linkMicList.remove(linkMicItem.second);
                    callbackToListener(new ListenerRunnable() {
                        @Override
                        public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                            linkMicListListener.onLinkMicItemRemove(linkMicItem.second, linkMicItem.first);
                        }
                    });
                }
            }
        };
        if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX == streamType) {
            rtcJoinMap.remove(linkMicUid);
            userJoinLeaveTask.run();
            if (teacherScreenStreamMap.containsKey(linkMicUid)) {
                callOnTeacherScreenStream(linkMicUid, false);
            }
        } else {
            PLVLinkMicItemDataBean linkMicItemDataBean = rtcJoinMap.get(linkMicUid);
            if (linkMicItemDataBean != null && linkMicItemDataBean.includeStreamType(streamType)) {
                if (linkMicItemDataBean.equalStreamType(streamType)) {
                    rtcJoinMap.remove(linkMicUid);
                } else {
                    if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN == streamType) {
                        linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_CAMERA);
                    } else {
                        linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN);
                    }
                }
                if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN == streamType
                        && teacherScreenStreamMap.containsKey(linkMicUid)) {
                    callOnTeacherScreenStream(linkMicUid, false);
                } else {
                    userJoinLeaveTask.run();
                }
            }
        }
    }

    private void acceptUserJoinChannel(final String linkMicUid, final int streamType) {
        requestLinkMicListApiOnce();
        PLVLinkMicItemDataBean linkMicItemDataBean = rtcJoinMap.get(linkMicUid);
        if (linkMicItemDataBean == null) {
            linkMicItemDataBean = new PLVLinkMicItemDataBean();
            linkMicItemDataBean.setLinkMicId(linkMicUid);
            linkMicItemDataBean.setStreamType(streamType);
            rtcJoinMap.put(linkMicUid, linkMicItemDataBean);
        } else {
            if (!linkMicItemDataBean.equalStreamType(streamType)) {
                linkMicItemDataBean.setStreamType(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
            }
        }
        final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
        if (linkMicItem != null) {
            final PLVLinkMicItemDataBean linkMicItemBean = linkMicItem.second;
            final int position = linkMicItem.first;
            if (linkMicItemBean.equalStreamType(streamType)) {
                callbackToListener(new ListenerRunnable() {
                    @Override
                    public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                        linkMicListListener.onLinkMicUserExisted(linkMicItemBean, position);
                    }
                });
            } else {
                if (PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN == streamType) {
                    if (isTeacherLinkMicId(linkMicUid) || isLeaderId(linkMicUid)) {
                        callOnTeacherScreenStream(linkMicUid, true);
                    } else {
                        callbackToListener(new ListenerRunnable() {
                            @Override
                            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                                linkMicList.remove(position);
                                linkMicListListener.onLinkMicItemRemove(linkMicItemBean, position);
                                linkMicItemBean.setStreamType(streamType);
                                linkMicList.add(position, linkMicItemBean);
                                linkMicListListener.onLinkMicItemInsert(linkMicItemBean, position);
                            }
                        });
                    }
                }
            }
        }
        callbackToListener(new ListenerRunnable() {
            @Override
            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                PLVLinkMicItemDataBean linkMicItemDataBean = linkMicListListener.onGetSavedLinkMicItem(linkMicUid);
                if (linkMicItemDataBean != null) {
                    boolean result = updateLinkMicItemInfoWithRtcJoinListInner(linkMicItemDataBean, linkMicUid);
                    if (result) {
                        callbackToListener(new ListenerRunnable() {
                            @Override
                            public void run(@NonNull OnLinkMicListListener linkMicListListener) {
                                linkMicListListener.onLinkMicItemInfoChanged();
                            }
                        });
                    }
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view回调">
    private void callbackToListener(ListenerRunnable runnable) {
        if (onLinkMicListListeners != null) {
            for (OnLinkMicListListener listListener : onLinkMicListListeners) {
                if (listListener != null && runnable != null) {
                    runnable.run(listListener);
                }
            }
        }
    }

    private interface ListenerRunnable {
        void run(@NonNull OnLinkMicListListener linkMicListListener);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 监听器">
    public interface OnLinkMicListListener {
        /**
         * 更新连麦列表
         *
         * @param dataBeanList 连麦列表。在实现中，要该列表去渲染
         */
        void onLinkMicListChanged(List<PLVLinkMicItemDataBean> dataBeanList);

        /**
         * 移除连麦列表item
         */
        void onLinkMicItemRemove(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

        /**
         * 响应用户已存在连麦频道
         *
         * @param position 连麦列表中的位置
         */
        void onLinkMicUserExisted(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

        /**
         * 响应讲师的屏幕共享流
         *
         * @param isOpen true：打开，false：关闭
         */
        void onTeacherScreenStream(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isOpen);

        /**
         * 通过joinList和waitList更新连麦列表item状态
         */
        List<String> onUpdateLinkMicItemStatus(List<PLVJoinInfoEvent> joinList, List<PLVLinkMicJoinStatus.WaitListBean> waitList);

        /**
         * 更新连麦列表item信息
         */
        boolean onUpdateLinkMicItemInfo(@NonNull PLVSocketUserBean socketUserBean, @NonNull PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isGroupLeader);

        /**
         * 获取保存过的连麦item
         */
        PLVLinkMicItemDataBean onGetSavedLinkMicItem(String linkMicId);

        /**
         * 同步连麦item
         */
        void syncLinkMicItem(PLVLinkMicItemDataBean linkMicItemDataBean, String userId);

        /**
         * 连麦item信息发生改变
         */
        void onLinkMicItemInfoChanged();

        /**
         * 改变连麦item为idle状态
         */
        void onLinkMicItemIdleChanged(String linkMicId);

        /**
         * 获取连麦列表和状态
         *
         * @param sessionId 场次id
         * @param callback  回调
         */
        void onGetLinkMicListStatus(String sessionId, PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus> callback);

        /**
         * 添加连麦列表item
         *
         * @param position 连麦列表中的位置
         */
        void onLinkMicItemInsert(PLVLinkMicItemDataBean linkMicItemDataBean, int position);

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
    }

    public static abstract class AbsOnLinkMicListListener implements OnLinkMicListListener {
        @Override
        public void onLinkMicListChanged(List<PLVLinkMicItemDataBean> dataBeanList) {

        }

        @Override
        public void onLinkMicItemRemove(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

        }

        @Override
        public void onLinkMicUserExisted(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

        }

        @Override
        public void onTeacherScreenStream(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isOpen) {

        }

        @Override
        public List<String> onUpdateLinkMicItemStatus(List<PLVJoinInfoEvent> joinList, List<PLVLinkMicJoinStatus.WaitListBean> waitList) {
            return null;
        }

        @Override
        public boolean onUpdateLinkMicItemInfo(@NonNull PLVSocketUserBean socketUserBean, @NonNull PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isGroupLeader) {
            return false;
        }

        @Override
        public PLVLinkMicItemDataBean onGetSavedLinkMicItem(String linkMicId) {
            return null;
        }

        @Override
        public void syncLinkMicItem(PLVLinkMicItemDataBean linkMicItemDataBean, String userId) {

        }

        @Override
        public void onLinkMicItemInfoChanged() {

        }

        @Override
        public void onLinkMicItemIdleChanged(String linkMicId) {

        }

        @Override
        public void onGetLinkMicListStatus(String sessionId, PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus> callback) {

        }

        @Override
        public void onLinkMicItemInsert(PLVLinkMicItemDataBean linkMicItemDataBean, int position) {

        }

        @Override
        public void onUserGetCup(String userNick, boolean isByEvent, int linkMicListPos, int memberListPos) {

        }

        @Override
        public void onUserHasPaint(boolean isMyself, boolean isHasPaint, int linkMicListPos, int memberListPos) {

        }
    }
    // </editor-fold>
}
