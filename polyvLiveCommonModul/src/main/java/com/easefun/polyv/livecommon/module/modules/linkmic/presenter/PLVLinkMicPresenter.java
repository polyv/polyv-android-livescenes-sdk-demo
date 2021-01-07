package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.SurfaceView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMsgHandler;
import com.easefun.polyv.livescenes.linkmic.IPolyvLinkMicManager;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicEventListener;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicListener;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicManagerFactory;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * date: 2020/7/16
 * author: hwj
 * description: 连麦Presenter
 */
public class PLVLinkMicPresenter implements IPLVLinkMicContract.IPLVLinkMicPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLinkMicPresenter.class.getSimpleName();

    //未初始化
    private static final int LINK_MIC_UNINITIATED = 1;
    //初始化中
    private static final int LINK_MIC_INITIATING = 2;
    //已经初始化
    private static final int LINK_MIC_INITIATED = 3;


    //加入频道超时
    private static final int TIME_OUT_JOIN_CHANNEL = 20 * 1000;
    //延迟1秒请求连麦列表
    private static final int DELAY_TO_GET_LINK_MIC_LIST = 1000;
    //每20s轮询连麦列表
    private static final int INTERVAL_TO_GET_LINK_MIC_LIST = 20 * 1000;

    /**** View ****/
    @Nullable
    private IPLVLinkMicContract.IPLVLinkMicView linkMicView;

    /**** Model ****/
    private IPolyvLinkMicManager linkMicManager;
    private PLVLinkMicMsgHandler linkMicMsgHandler;
    //账号数据
    private IPLVLiveRoomDataManager liveRoomDataManager;

    /**** 连麦状态数据 ****/
    //连麦初始化状态
    private int linkMicInitState = LINK_MIC_UNINITIATED;
    private boolean isJoinLinkMic = false;
    private String myLinkMicId = "";
    private boolean isAudioLinkMic;
    private boolean isTeacherOpenLinkMic;
    //是否已经初始化过第一画面用户
    private boolean hasInitFirstScreenUser = false;
    //纯视频频道类型连麦时，是否已经初始化完成连麦布局讲师的位置
    private boolean hasInitFirstTeacherLocation = false;
    //纯视频频道类型连麦时，主屏的讲师连麦Id
    private String mainTeacherLinkMicId;
    //连麦列表
    private List<PLVLinkMicItemDataBean> linkMicList = new LinkedList<>();

    /**** Disposable ****/
    private Disposable getLinkMicListDelay;
    private Disposable getLinkMicListTimer;
    private Disposable linkJoinTimer;

    /**** Listener ****/
    //rtc事件监听器
    private PolyvLinkMicEventListener eventListener = new PolyvLinkMicEventListenerImpl();
    //连麦socket事件监听
    private PLVLinkMicMsgHandler.OnLinkMicDataListener onLinkMicDataListener = new PolyvLinkMicSocketEventListener();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLinkMicPresenter(final IPLVLiveRoomDataManager liveRoomDataManager, @Nullable final IPLVLinkMicContract.IPLVLinkMicView view) {
        this.liveRoomDataManager = liveRoomDataManager;
        //数据
        String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        PolyvLinkMicConfig.getInstance().init(viewerId, false);
        //view
        this.linkMicView = view;
        //model
        linkMicManager = PolyvLinkMicManagerFactory.createNewLinkMicManager();
        myLinkMicId = linkMicManager.getLinkMicUid();
        if (TextUtils.isEmpty(myLinkMicId)) {
            if (linkMicView != null) {
                linkMicView.onLinkMicError(-1, new Throwable("获取到空的linkMicId"));
            }
            return;
        }
        //构造连麦消息处理器
        linkMicMsgHandler = new PLVLinkMicMsgHandler(myLinkMicId, onLinkMicDataListener);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API-presenter接口实现">
    @Override
    public void destroy() {
        isJoinLinkMic = false;
        dispose(getLinkMicListDelay);
        dispose(getLinkMicListTimer);
        linkMicList.clear();
        myLinkMicId = "";
        this.linkMicView = null;
        linkMicInitState = LINK_MIC_UNINITIATED;
        linkMicManager.destroy();
        linkMicMsgHandler.destroy();
        PolyvLinkMicConfig.getInstance().clear();
    }

    @Override
    public void requestJoinLinkMic() {
        switch (linkMicInitState) {
            //未初始化
            case LINK_MIC_UNINITIATED:
                PLVCommonLog.d(TAG, "连麦开始初始化");
                linkMicInitState = LINK_MIC_INITIATING;
                int channelId = 0;
                try {
                    channelId = Integer.parseInt(liveRoomDataManager.getConfig().getChannelId());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    ToastUtils.showShort(Utils.getApp().getString(R.string.plv_linkmic_toast_invalid_channel_format) + liveRoomDataManager.getConfig().getChannelId());
                    return;
                }
                linkMicManager.initEngine(channelId, new PolyvLinkMicListener() {
                    @Override
                    public void onLinkMicEngineCreatedSuccess() {
                        PLVCommonLog.d(TAG, "连麦初始化成功");
                        linkMicInitState = LINK_MIC_INITIATED;
                        linkMicManager.addEventHandler(eventListener);
                        linkMicManager.sendJoinRequestMsg(myLinkMicId);
                    }

                    @Override
                    public void onLinkMicError(int errorCode, Throwable throwable) {
                        linkMicInitState = LINK_MIC_UNINITIATED;
                        if (linkMicView != null) {
                            linkMicView.onLinkMicError(errorCode, throwable);
                        }
                    }
                });
                break;
            //初始化中
            case LINK_MIC_INITIATING:
                PLVCommonLog.d(TAG, "连麦初始化中");
                return;
            //已经初始化
            case LINK_MIC_INITIATED:
                linkMicManager.sendJoinRequestMsg(myLinkMicId);
                break;
            default:
                break;
        }
    }

    @Override
    public void cancelRequestJoinLinkMic() {
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId(), myLinkMicId);
    }

    @Override
    public void leaveLinkMic() {
        linkMicManager.leaveChannel();
        leaveChannel();
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId(), myLinkMicId);
    }

    @Override
    public void muteAudio(boolean mute) {
        PLVLinkMicMedia linkMicMedia = new PLVLinkMicMedia();
        linkMicMedia.setType("audio");
        linkMicMedia.setMute(mute);

        linkMicManager.sendMuteEventMsg(linkMicMedia);
        linkMicManager.muteLocalAudio(mute);
        for (int i = 0; i < linkMicList.size(); i++) {
            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
            if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                plvLinkMicItemDataBean.setMuteAudio(mute);
                if (linkMicView != null) {
                    linkMicView.onUserMuteAudio(myLinkMicId, mute, i);
                }
                break;
            }
        }
    }

    @Override
    public void muteVideo(boolean mute) {
        PLVLinkMicMedia linkMicMedia = new PLVLinkMicMedia();
        linkMicMedia.setType("video");
        linkMicMedia.setMute(mute);
        linkMicManager.sendMuteEventMsg(linkMicMedia);
        linkMicManager.muteLocalVideo(mute);
        for (int i = 0; i < linkMicList.size(); i++) {
            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
            if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                plvLinkMicItemDataBean.setMuteVideo(mute);
                if (linkMicView != null) {
                    linkMicView.onUserMuteVideo(myLinkMicId, mute, i);
                }
                break;
            }
        }
    }

    @Override
    public void switchCamera() {
        linkMicManager.switchCamera();
    }

    @Override
    public SurfaceView createRenderView(Context context) {
        return linkMicManager.createRendererView(context);
    }

    @Override
    public String getLinkMicId() {
        return linkMicManager.getLinkMicUid();
    }

    @Override
    public void setupRenderView(SurfaceView renderView, String linkMicId) {
        if (linkMicManager.getLinkMicUid().equals(linkMicId)) {
            linkMicManager.setupLocalVideo(renderView, linkMicId);
        } else {
            linkMicManager.setupRemoteVideo(renderView, linkMicId);
        }
    }

    @Override
    public boolean isJoinLinkMic() {
        return isJoinLinkMic;
    }

    @Override
    public void setIsAudioLinkMic(boolean isAudioLinkMic) {
        this.isAudioLinkMic = isAudioLinkMic;//通过服务端轮询接口获取连麦类型
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        this.isTeacherOpenLinkMic = isTeacherOpenLinkMic;
    }

    @Override
    public boolean isTeacherOpenLinkMic() {
        return isTeacherOpenLinkMic;
    }

    @Override
    public boolean isAloneChannelTypeSupportRTC() {
        return liveRoomDataManager.getConfig().isAloneChannelType() && liveRoomDataManager.isSupportRTC();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="http请求">

    /**
     * 请求连麦列表，该请求在加入连麦后就定时轮询，保证本地连麦列表和server连麦列表的数据同步。
     * 请求成功后，做了以下操作：
     * 1. 遍历每一个server返回的连麦列表中的用户，将连麦列表中不存在的用户添加到列表中
     * 2. 设置初始化的第一画面用户ID
     * 3. 将新添加的用户回调出去
     * 4. 纯视频频道类型，如果列表列表添加了讲师，则调整讲师的位置
     * 5. 遍历本地的连麦列表用户，与服务端数据比对，如果出现了不在服务端连麦列表的用户，则踢出连麦列表。
     */
    private void requestLinkMicListFromServer() {
        linkMicManager.getLinkStatus(liveRoomDataManager.getSessionId(), new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
            @Override
            public void onSuccess(PLVLinkMicJoinStatus data) {
                PLVCommonLog.d(TAG, "PLVLinkMicPresenter.requestLinkMicListFromServer.onSuccess->\n" + data.toString());
                List<String> newJoinUserList = new ArrayList<>();

                //是否从连麦列表中添加了老师
                boolean isAddedTeacherLinkMicId = false;
                //老师连麦Id
                String teacherLinkMicId = "";

                //1. 遍历每一个server返回的连麦列表中的用户，将连麦列表中不存在的用户添加到列表中
                for (PLVJoinInfoEvent plvJoinInfoEvent : data.getJoinList()) {
                    //列表中是否存在该用户
                    boolean isThisUserExistInLinkMicList = false;
                    String userId = plvJoinInfoEvent.getUserId();
                    for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                        if (userId.equals(itemDataBean.getLinkMicId())) {
                            isThisUserExistInLinkMicList = true;
                            break;
                        }
                    }
                    //如果列表中不存在这个用户，将这个用户添加到连麦列表
                    if (!isThisUserExistInLinkMicList) {
                        PLVLinkMicItemDataBean itemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(plvJoinInfoEvent);
                        if (itemDataBean.isTeacher()) {
                            linkMicList.add(0, itemDataBean);//从连麦列表中添加老师
                            isAddedTeacherLinkMicId = true;
                            teacherLinkMicId = itemDataBean.getLinkMicId();
                        } else {
                            linkMicList.add(itemDataBean);//从连麦列表中添加观众
                        }
                        newJoinUserList.add(plvJoinInfoEvent.getUserId());
                    }
                }

                //2. 设置初始化的第一画面用户ID
                String firstScreenLinkMicId = data.getMaster();
                if (isJoinLinkMic && !hasInitFirstScreenUser && !TextUtils.isEmpty(firstScreenLinkMicId)) {
                    hasInitFirstScreenUser = true;
                    if (linkMicView != null) {
                        linkMicView.setFirstScreenLinkMicId(firstScreenLinkMicId);
                    }
                    //找出第一画面，并插入到连麦列表顶部
                    PLVLinkMicItemDataBean firstScreenDataBean = null;
                    for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                        if (plvLinkMicItemDataBean.getLinkMicId().equals(firstScreenLinkMicId)) {
                            firstScreenDataBean = plvLinkMicItemDataBean;
                            linkMicList.remove(plvLinkMicItemDataBean);
                            break;
                        }
                    }
                    if (firstScreenDataBean != null) {
                        linkMicList.add(0, firstScreenDataBean);
                    }
                }

                //3. 将新添加的用户回调出去
                if (!newJoinUserList.isEmpty()) {
                    if (linkMicView != null) {
                        linkMicView.onUsersJoin(newJoinUserList);
                    }
                }

                //4. 纯视频频道类型，如果列表列表添加了讲师，则调整讲师的位置
                if (liveRoomDataManager.getConfig().isAloneChannelType()
                        && isAddedTeacherLinkMicId
                        && !hasInitFirstTeacherLocation) {
                    hasInitFirstTeacherLocation = true;
                    mainTeacherLinkMicId = teacherLinkMicId;
                    if (linkMicView != null) {
                        for (int i = 0; i < linkMicList.size(); i++) {
                            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                            if (teacherLinkMicId.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                                linkMicView.onAdjustTeacherLocation(teacherLinkMicId, i, liveRoomDataManager.isSupportRTC());
                                break;
                            }
                        }
                    }
                }

                //5. 遍历本地的连麦列表用户，与服务端数据比对，如果出现了不在服务端连麦列表的用户，则踢出连麦列表。
                List<String> usersToRemove = new ArrayList<>();
                Iterator<PLVLinkMicItemDataBean> itemDataBeanIterator = linkMicList.iterator();
                while (itemDataBeanIterator.hasNext()) {
                    PLVLinkMicItemDataBean itemDataBean = itemDataBeanIterator.next();
                    //该本地列表的用户是否存在于服务端的连麦列表
                    boolean isLocalUserExistInServerList = false;
                    String linkMicId = itemDataBean.getLinkMicId();
                    for (PLVJoinInfoEvent plvJoinInfoEvent : data.getJoinList()) {
                        if (linkMicId.equals(plvJoinInfoEvent.getUserId())) {
                            isLocalUserExistInServerList = true;
                            break;
                        }
                    }
                    if (!isLocalUserExistInServerList) {
                        usersToRemove.add(itemDataBean.getLinkMicId());
                        itemDataBeanIterator.remove();
                    }
                }
                if (!usersToRemove.isEmpty()) {
                    if (linkMicView != null) {
                        linkMicView.onUsersLeave(usersToRemove);
                    }
                    if (usersToRemove.contains(myLinkMicId)) {
                        //如果我也不在服务端的连麦列表中(长时间断网，又连上后，服务端则认为该用户离线了)，那么自己主动下麦。
                        if (linkMicView != null) {
                            PLVCommonLog.d(TAG, "onNotInLinkMicList");
                            linkMicView.onNotInLinkMicList();
                        }
                    }
                }
            }

            @Override
            public void onFail(PLVLinkMicHttpRequestException throwable) {
                super.onFail(throwable);
                if (linkMicView != null) {
                    linkMicView.onLinkMicError(throwable.getErrorCode(), throwable);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket事件处理">
    private void handleTeacherAllowJoin(boolean isAudioLinkMic) {
        //在加入连麦前的回调
        if (linkMicView != null) {
            linkMicView.onBeforeJoinChannel(myLinkMicId, isAudioLinkMic, linkMicList);
        }
        //是否打开本地视频
        linkMicManager.enableLocalVideo(!isAudioLinkMic);
        //加入频道
        linkMicManager.joinChannel();
        if (linkMicView != null) {
            linkMicView.onTeacherAllowJoin();
        }
        //开始加入频道超时倒计时。
        startJoinTimeoutCount(new Runnable() {
            @Override
            public void run() {
                if (linkMicView != null) {
                    linkMicView.onAllowButJoinTimeout();
                }
            }
        });
    }

    private void handleTeacherCloseLinkMic() {
        if (isTeacherOpenLinkMic) {
            isTeacherOpenLinkMic = false;
            if (linkMicView != null) {
                linkMicView.onTeacherCloseLinkMic();
            }
            linkMicManager.leaveChannel();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="加入rtc超时">
    //开始加入频道超时倒计时
    private void startJoinTimeoutCount(final Runnable timeout) {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
        }
        linkJoinTimer = PLVRxTimer.delay(TIME_OUT_JOIN_CHANNEL, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                timeout.run();
            }
        });
    }

    private void stopJoinTimeoutCount() {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
            linkJoinTimer = null;
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦方法封装">
    //当主动离开频道的时候，可能因为断网的原因，收不到rtc引擎的onLeaveChannel()回调，因此要主动执行离开频道的逻辑
    private void leaveChannel() {
        if (isJoinLinkMic) {
            isJoinLinkMic = false;
            hasInitFirstScreenUser = false;
            hasInitFirstTeacherLocation = false;
            dispose(getLinkMicListTimer);
            linkMicList.clear();
            if (linkMicView != null) {
                linkMicView.onLeaveChannel();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - rtc事件接收器">
    private class PolyvLinkMicEventListenerImpl extends PolyvLinkMicEventListener {
        @Override
        public void onJoinChannelSuccess(String channel, String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onJoinChannelSuccess");
            isJoinLinkMic = true;
            stopJoinTimeoutCount();
            PLVLinkMicJoinSuccess joinSuccess = linkMicManager.sendJoinSuccessMsg(liveRoomDataManager.getSessionId(), uid);
            //如果是普通连麦观众，则rtc上麦就表示加入了连麦列表
            linkMicList.add(0, PLVLinkMicDataMapper.map2LinkMicItemData(joinSuccess));//添加自己
            //如果是参与者，则是自动加入rtc频道的，要等讲师同意，才能加入连麦列表
            if (linkMicView != null) {
                linkMicView.onJoinChannelSuccess();
            }

            dispose(getLinkMicListTimer);
            getLinkMicListTimer = PLVRxTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    requestLinkMicListFromServer();
                }
            });
        }

        @Override
        public void onLeaveChannel() {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onLeaveChannel");
            leaveChannel();
        }

        @Override
        public void onUserJoined(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserJoined");
            dispose(getLinkMicListDelay);
            getLinkMicListDelay = PLVRxTimer.delay(DELAY_TO_GET_LINK_MIC_LIST, new Consumer<Object>() {
                @Override
                public void accept(Object o) throws Exception {
                    requestLinkMicListFromServer();
                }
            });
        }

        @Override
        public void onUserOffline(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserOffline");
            Iterator<PLVLinkMicItemDataBean> it = linkMicList.iterator();//移除
            while (it.hasNext()) {
                PLVLinkMicItemDataBean dataBean = it.next();
                if (dataBean.getLinkMicId().equals(uid)) {
                    it.remove();
                    if (linkMicView != null) {
                        linkMicView.onUsersLeave(Collections.singletonList(uid));
                    }
                    break;
                }
            }
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserMuteAudio");
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (uid.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                    plvLinkMicItemDataBean.setMuteAudio(mute);
                    if (linkMicView != null) {
                        linkMicView.onUserMuteAudio(uid, mute, i);
                    }
                    break;
                }
            }
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserMuteVideo uid=" + uid);
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (uid.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                    plvLinkMicItemDataBean.setMuteVideo(mute);
                    if (linkMicView != null) {
                        linkMicView.onUserMuteVideo(uid, mute, i);
                    }
                    break;
                }
            }
        }

        @Override
        public void onLocalAudioVolumeIndication(PLVAudioVolumeInfo speaker) {
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                if (plvLinkMicItemDataBean.getLinkMicId().equals(speaker.getUid())) {
                    plvLinkMicItemDataBean.setCurVolume(speaker.getVolume());
                    break;
                }
            }
            if (linkMicView != null) {
                linkMicView.onLocalUserMicVolumeChanged();
            }
        }

        @Override
        public void onRemoteAudioVolumeIndication(PLVAudioVolumeInfo[] speakers) {
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                //自己的麦克风音量由[onLocalAudioVolumeIndication]回调控制
                if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                    continue;
                }

                boolean hitInVolumeInfoList = false;
                for (PLVAudioVolumeInfo speaker : speakers) {
                    if (plvLinkMicItemDataBean.getLinkMicId().equals(String.valueOf(speaker.getUid()))) {
                        hitInVolumeInfoList = true;
                        //如果总音量不为0，那么设置当前音量，以PLVLinkMicItemDataBean.MAX_VOLUME作为最大值
                        plvLinkMicItemDataBean.setCurVolume(speaker.getVolume());
                        break;
                    }
                }
                if (!hitInVolumeInfoList) {
                    plvLinkMicItemDataBean.setCurVolume(0);
                }
            }

            if (linkMicView != null) {
                linkMicView.onRemoteUserVolumeChanged(linkMicList);
            }
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - socket事件接收器">
    private class PolyvLinkMicSocketEventListener implements PLVLinkMicMsgHandler.OnLinkMicDataListener {
        @Override
        public void onTeacherReceiveJoinRequest() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherReceiveJoinRequest");
        }

        @Override
        public void onTeacherAllowMeToJoin() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherAllowMeToJoin");
            handleTeacherAllowJoin(isAudioLinkMic);
        }

        @Override
        public void onTeacherHangupMe() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherHangupMe");
            linkMicManager.leaveChannel();
        }

        @Override
        public void onTeacherOpenLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherOpenLinkMic");
            isTeacherOpenLinkMic = true;
            if (linkMicView != null) {
                linkMicView.onTeacherOpenLinkMic();
            }
//            if (liveRoomDataManager.getConfig().isParticipant() && !isJoinLinkMic) {
//                linkMicManager.enableLocalVideo(!isAudioLinkMic);
//                //参与者加入时，将角色调整为观众角色
//                linkMicManager.switchRoleToAudience();
//                handleTeacherAllowJoin(isAudioLinkMic);
//            }
        }

        @Override
        public void onTeacherCloseLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherCloseLinkMic");
            handleTeacherCloseLinkMic();
        }

        @Override
        public void onTeacherMuteMedia(boolean isMute, boolean isAudio) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherMuteMedia");
            if (!isJoinLinkMic) {
                return;
            }
            for (int i = 0; i < linkMicList.size(); i++) {
                PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                if (plvLinkMicItemDataBean.getLinkMicId().equals(myLinkMicId)) {
                    if (isAudio) {
                        plvLinkMicItemDataBean.setMuteAudio(isMute);
                        linkMicManager.muteLocalAudio(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteAudio(myLinkMicId, isMute, i);
                        }
                    } else {
                        plvLinkMicItemDataBean.setMuteVideo(isMute);
                        linkMicManager.muteLocalVideo(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteVideo(myLinkMicId, isMute, i);
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onUserJoinSuccess(PLVLinkMicItemDataBean dataBean) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onUserJoinSuccess");
            if (!isJoinLinkMic) {
                return;
            }
            boolean userExistInList = false;
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(dataBean.getLinkMicId())) {
                    userExistInList = true;
                    break;
                }
            }
            if (!userExistInList) {
                if (dataBean.isTeacher()) {
                    linkMicList.add(0, dataBean);//添加讲师
                } else {
                    linkMicList.add(dataBean);//添加观众
                }
                if (linkMicView != null) {
                    linkMicView.onUsersJoin(Collections.singletonList(dataBean.getLinkMicId()));
                }
            }
        }

        @Override
        public void onTeacherSendCup(String linkMicId, int cupNum) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherSendCup");
            if (!isJoinLinkMic) {
                return;
            }
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(linkMicId)) {
                    itemDataBean.setCupNum(cupNum);
                    break;
                }
            }
        }

        @Override
        public void onUpdateLinkMicType(boolean isAudio) {
            isAudioLinkMic = isAudio;//通过socket消息更新连麦类型
        }

        @Override
        public void onSwitchFirstScreen(final String linkMicId) {
            final IPLVLinkMicContract.IPLVLinkMicView view = linkMicView;
            if (view == null) {
                return;
            }
            //将[linkMicId]切换到连麦列表的第一画面
            if (linkMicList.isEmpty()) {
                return;
            }

            if (liveRoomDataManager.getConfig().isAloneChannelType()) {
                int firstScreenInLinkMicListIndex = -1;
                for (int i = 0; i < linkMicList.size(); i++) {
                    PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                    if (linkMicId != null && linkMicId.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                        firstScreenInLinkMicListIndex = i;
                        break;
                    }
                }
                //如果切换的第一画面的位置正好在media的位置，说明第一画面已经在主屏，则不再处理
                if (firstScreenInLinkMicListIndex != -1 && firstScreenInLinkMicListIndex == view.getMediaViewIndexInLinkMicList()) {
                    return;
                }
                //如果切换的第一画面为讲师
                if (mainTeacherLinkMicId != null && mainTeacherLinkMicId.equals(linkMicId)) {
                    //1. 如果Media在连麦列表，则将media切换到主屏幕即可
                    if (view.isMediaShowInLinkMicList()) {
                        view.performClickInLinkMicListItem(view.getMediaViewIndexInLinkMicList());
                    }
                } else if (firstScreenInLinkMicListIndex != -1) {//切换的第一画面不为讲师，并且在列表中能找到
                    //1. 将第一画面和主屏幕的位置进行切换
                    view.performClickInLinkMicListItem(firstScreenInLinkMicListIndex);
                }

                view.setFirstScreenLinkMicId(linkMicId);
            } else {
                PLVLinkMicItemDataBean itemToBeFirst = linkMicList.get(0);
                //遍历找到target
                for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                    if (itemDataBean.getLinkMicId().equals(linkMicId)) {
                        itemToBeFirst = itemDataBean;
                    }
                }
                //如果target已经在第一画面，则不再做处理
                final int indexOfTarget = linkMicList.indexOf(itemToBeFirst);
                if (indexOfTarget == 0) {
                    return;
                }

                //1. 如果PPT在连麦列表，则先将ppt切换到主屏幕
                boolean pptNeedToGoBackToLinkMicList = false;
                int pptIndexInLinkMicList = -1;
                if (view.isMediaShowInLinkMicList()) {
                    pptIndexInLinkMicList = view.getMediaViewIndexInLinkMicList();
                    pptNeedToGoBackToLinkMicList = true;
                    view.onSwitchPPTViewLocation(true);
                }

                //2. 将原先的第一画面和新的第一画面的位置进行切换
                PLVLinkMicItemDataBean oldFirst = linkMicList.get(0);
                linkMicList.remove(oldFirst);
                linkMicList.remove(itemToBeFirst);
                linkMicList.add(0, itemToBeFirst);
                linkMicList.add(indexOfTarget, oldFirst);
                view.onSwitchFirstScreen(linkMicId);

                //3. 将原先在连麦列表的PPT恢复到原先的位置
                if (pptNeedToGoBackToLinkMicList) {
                    view.performClickInLinkMicListItem(pptIndexInLinkMicList);
                }

                view.setFirstScreenLinkMicId(linkMicId);
            }
        }

        @Override
        public void onSwitchPPTViewLocation(boolean toMainScreen) {
            if (isJoinLinkMic) {
                //当加入连麦的时候，才回调该方法
                if (linkMicView != null) {
                    linkMicView.onSwitchPPTViewLocation(toMainScreen);
                }
            }
        }

        @Override
        public void onFinishClass() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onFinishClass");
            handleTeacherCloseLinkMic();
        }
    }
    // </editor-fold>
}
