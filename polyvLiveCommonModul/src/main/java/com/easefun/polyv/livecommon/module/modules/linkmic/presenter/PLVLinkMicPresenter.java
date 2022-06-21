package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowModeGetter;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMsgHandler;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMuteCacheList;
import com.easefun.polyv.livescenes.linkmic.IPolyvLinkMicManager;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicEventListener;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicListener;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicManagerFactory;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.linkmic.log.IPLVLinkMicTraceLogSender;
import com.plv.linkmic.log.PLVLinkMicTraceLogSender;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.vo.PLVLinkMicEngineParam;
import com.plv.livescenes.log.linkmic.PLVLinkMicELog;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
    @Nullable
    private PLVLinkMicMsgHandler linkMicMsgHandler;
    @Nullable
    private IPLVRTCInvokeStrategy rtcInvokeStrategy;
    //账号数据
    private IPLVLiveRoomDataManager liveRoomDataManager;

    /**** 连麦状态数据 ****/
    //连麦初始化状态
    private int linkMicInitState = LINK_MIC_UNINITIATED;
    private String myLinkMicId = "";
    private boolean isAudioLinkMic;
    //socket消息更新连麦状态的时间
    private long socketRefreshOpenStatusData = -1;
    private boolean isTeacherOpenLinkMic;
    //是否已经初始化过第一画面用户
    private boolean hasInitFirstScreenUser = false;
    //纯视频频道类型连麦时，是否已经初始化完成连麦布局讲师的位置
    private boolean hasInitFirstTeacherLocation = false;
    //纯视频频道类型连麦时，主屏的讲师连麦Id
    private String mainTeacherLinkMicId;
    //连麦列表
    private List<PLVLinkMicItemDataBean> linkMicList = new LinkedList<>();
    //mute事件缓存列表。用于解决，Mute事件到达时，连麦列表中还不存在该成员，导致mute事件遗漏的情况。
    private PLVLinkMicMuteCacheList muteCacheList = new PLVLinkMicMuteCacheList();
    //音视频模式，禁止的类型，用于全体静音等
    private String avConnectMode = "";
    // 是否rtc观看
    private boolean isWatchRtc;
    // 是否正在mute全体
    private boolean isMuteAllAudio;
    private boolean isMuteAllVideo;

    /**** Disposable ****/
    private Disposable getLinkMicListDelay;
    private Disposable getLinkMicListTimer;
    private Disposable linkJoinTimer;
    @Nullable
    private List<Runnable> actionAfterLinkMicEngineCreated;

    /**** Listener ****/
    //rtc事件监听器
    private PolyvLinkMicEventListener eventListener = new PolyvLinkMicEventListenerImpl();
    //socket事件监听器
    private PolyvLinkMicSocketEventListener socketEventListener = new PolyvLinkMicSocketEventListener();
    //socket监听器
    private PLVSocketMessageObserver.OnMessageListener messageListener;
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
        actionAfterLinkMicEngineCreated = new ArrayList<>();
        linkMicManager = PolyvLinkMicManagerFactory.createNewLinkMicManager();

        final PLVLinkMicEngineParam param = new PLVLinkMicEngineParam()
                .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                .setViewerId(liveRoomDataManager.getConfig().getUser().getViewerId())
                .setViewerType(liveRoomDataManager.getConfig().getUser().getViewerType())
                .setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        linkMicManager.initEngine(param, new PolyvLinkMicListener() {
            @Override
            public void onLinkMicEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "连麦初始化成功");
                linkMicInitState = LINK_MIC_INITIATED;
                linkMicManager.addEventHandler(eventListener);
                if (actionAfterLinkMicEngineCreated != null) {
                    for (Runnable action : actionAfterLinkMicEngineCreated) {
                        action.run();
                    }
                    actionAfterLinkMicEngineCreated = null;
                }
            }

            @Override
            public void onLinkMicError(int errorCode, Throwable throwable) {
                linkMicInitState = LINK_MIC_UNINITIATED;
                if (linkMicView != null) {
                    linkMicView.onLinkMicError(errorCode, throwable);
                }
            }
        });
        myLinkMicId = linkMicManager.getLinkMicUid();
        if (TextUtils.isEmpty(myLinkMicId)) {
            if (linkMicView != null) {
                linkMicView.onLinkMicError(-1, new Throwable("获取到空的linkMicId"));
            }
            return;
        }
        //连麦socket事件监听
        linkMicMsgHandler = new PLVLinkMicMsgHandler(myLinkMicId);
        linkMicMsgHandler.addLinkMicMsgListener(socketEventListener);
        //初始化RTC调用策略实现
        isWatchRtc = PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch();
        initRTCInvokeStrategy();
        //Socket事件监听
        messageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if(event == null){
                    return;
                }
                switch (event){
                    case PLVEventConstant.MESSAGE_EVENT_RELOGIN:{
                        //当收到重登录的消息的时候，我们需要将判断一下是否是连麦状态，是的话就要断开连麦
                        if(isJoinLinkMic()){
                            leaveLinkMic();
                        }
                        break;
                    }
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(messageListener);
    }

    private void initRTCInvokeStrategy() {
        if (PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch() && isWatchRtc) {
            //RTC 无延迟观看
            rtcInvokeStrategy = new PLVRTCWatchEnabledStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new PLVRTCWatchEnabledStrategy.OnJoinRTCChannelWatchListener() {
                        @Override
                        public void onJoinRTCChannelWatch() {
                            if (linkMicView != null) {
                                linkMicView.onJoinRtcChannel();
                            }
                            stopJoinTimeoutCount();
                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVRxTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    requestLinkMicListFromServer();
                                }
                            });
                        }
                    },
                    new IPLVRTCInvokeStrategy.OnJoinLinkMicListener() {

                        @Override
                        public void onJoinLinkMic(PLVLinkMicJoinSuccess data) {
                            PLVLinkMicItemDataBean selfDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(data);
                            //已经存在则不要重复添加
                            boolean selfExist = false;
                            for (PLVLinkMicItemDataBean bean : linkMicList) {
                                if (selfDataBean.getLinkMicId().equals(bean.getLinkMicId())) {
                                    selfExist = true;
                                    break;
                                }
                            }
                            if (!selfExist) {
                                //添加自己
                                if (linkMicList.isEmpty()) {
                                    linkMicList.add(selfDataBean);
                                } else {
                                    linkMicList.add(1, selfDataBean);//添加自己
                                }
                            }

                            if (linkMicView != null) {
                                linkMicView.onChangeListShowMode(PLVLinkMicListShowModeGetter.getJoinedMicShowMode(isAudioLinkMic));
                                linkMicView.onJoinLinkMic();
                                linkMicView.updateAllLinkMicList();
                            }

                            loadLinkMicConnectMode(avConnectMode);
                        }
                    });
            rtcInvokeStrategy.setOnLeaveLinkMicListener(new IPLVRTCInvokeStrategy.OnLeaveLinkMicListener() {
                @Override
                public void onLeaveLinkMic() {
                    Iterator<PLVLinkMicItemDataBean> it = linkMicList.iterator();//移除自己
                    while (it.hasNext()) {
                        PLVLinkMicItemDataBean dataBean = it.next();
                        if (dataBean.getLinkMicId().equals(myLinkMicId)) {
                            if (linkMicView != null) {
                                linkMicView.onUsersLeave(Collections.singletonList(myLinkMicId));
                            }
                            it.remove();
                            break;
                        }
                    }

                    if (linkMicView != null) {
                        linkMicView.onChangeListShowMode(PLVLinkMicListShowModeGetter.getLeavedMicShowMode());
                        linkMicView.onLeaveLinkMic();
                    }
                }
            });
        } else {
            //非RTC无延迟观看
            rtcInvokeStrategy = new PLVRTCWatchDisabledStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new IPLVRTCInvokeStrategy.OnJoinLinkMicListener() {
                        @Override
                        public void onJoinLinkMic(PLVLinkMicJoinSuccess data) {
                            stopJoinTimeoutCount();
                            if (!linkMicList.isEmpty()) {
                                //正常情况下不会走这里的逻辑，走到这里的可能是：退出了RTC频道，但是还收到了RTC的用户加入事件。为了安全，还是做一个检查和清空。
                                PLVCommonLog.w(TAG, "非无延迟观看，加入连麦时，连麦列表不为空！手动清空连麦列表，连麦列表为：\n" + linkMicList.toString());
                                cleanLinkMicListData();
                            }
                            //如果是普通连麦观众，则rtc上麦就表示加入了连麦列表
                            linkMicList.add(0, PLVLinkMicDataMapper.map2LinkMicItemData(data));//添加自己
                            //如果是参与者，则是自动加入rtc频道的，要等讲师同意，才能加入连麦列表
                            if (linkMicView != null) {
                                linkMicView.onJoinRtcChannel();
                                linkMicView.onJoinLinkMic();
                            }

                            loadLinkMicConnectMode(avConnectMode);

                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVRxTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    requestLinkMicListFromServer();
                                }
                            });
                        }
                    });
            rtcInvokeStrategy.setOnLeaveLinkMicListener(new IPLVRTCInvokeStrategy.OnLeaveLinkMicListener() {
                @Override
                public void onLeaveLinkMic() {
                    if (linkMicView != null) {
                        linkMicView.onLeaveLinkMic();
                    }
                }
            });
        }

        rtcInvokeStrategy.setOnBeforeJoinChannelListener(new IPLVRTCInvokeStrategy.OnBeforeJoinChannelListener() {
            @Override
            public void onBeforeJoinChannel(PLVLinkMicListShowMode linkMicListShowMode) {
                startJoinTimeoutCount(new Runnable() {
                    @Override
                    public void run() {
                        if (linkMicView != null) {
                            linkMicView.onJoinChannelTimeout();
                        }
                    }
                });
                if (linkMicView != null) {
                    linkMicView.onPrepareLinkMicList(myLinkMicId, linkMicListShowMode, linkMicList);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API-presenter接口实现">
    @Override
    public void destroy() {
        //判断是否处于连麦状态，是连麦状态下才会发送joinLeave
        if(isJoinLinkMic()){
            //发送joinLeave
            linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        }
        leaveChannel();
        dispose(getLinkMicListDelay);
        dispose(getLinkMicListTimer);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(messageListener);
        linkMicList.clear();
        muteCacheList.clear();
        myLinkMicId = "";
        this.linkMicView = null;
        linkMicInitState = LINK_MIC_UNINITIATED;
        linkMicManager.destroy();
        if (linkMicMsgHandler != null) {
            linkMicMsgHandler.destroy();
        }
        PolyvLinkMicConfig.getInstance().clear();
    }

    @Override
    public void requestJoinLinkMic() {
        requestPermissionAndJoin();
    }

    @Override
    public void cancelRequestJoinLinkMic() {
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
    }

    @Override
    public void leaveLinkMic() {
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setLeaveLinkMic();
        }
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
    public void muteAudio(String linkMicId, boolean mute) {
        if (myLinkMicId != null && myLinkMicId.equals(linkMicId)) {
            muteAudio(mute);
        } else {
            linkMicManager.muteRemoteAudio(linkMicId, mute);
        }
    }

    @Override
    public void muteVideo(String linkMicId, boolean mute) {
        if (myLinkMicId != null && myLinkMicId.equals(linkMicId)) {
            muteVideo(mute);
        } else {
            linkMicManager.muteRemoteVideo(linkMicId, mute);
        }
    }

    @Override
    public void muteAllAudio(boolean mute) {
        isMuteAllAudio = mute;
        linkMicManager.muteAllRemoteAudio(mute);
    }

    @Override
    public void muteAllVideo(boolean mute) {
        isMuteAllVideo = mute;
        linkMicManager.muteAllRemoteVideo(mute);
    }

    @Override
    public void switchCamera() {
        linkMicManager.switchCamera();
    }

    @Override
    public void setPushPictureResolutionType(int type) {
        linkMicManager.setPushPictureResolutionType(type);
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
    public String getMainTeacherLinkMicId() {
        return mainTeacherLinkMicId;
    }

    @Override
    public void setupRenderView(SurfaceView renderView, String linkMicId) {
        if (linkMicManager.getLinkMicUid().equals(linkMicId)) {
            if (liveRoomDataManager.isOnlyAudio()) {
                linkMicManager.setupLocalVideo(renderView, PLVStreamerConfig.RenderMode.RENDER_MODE_NONE);
            }
            linkMicManager.setupLocalVideo(renderView, linkMicId);
        } else {
            linkMicManager.setupRemoteVideo(renderView, linkMicId);
            if (isMuteAllAudio) {
                linkMicManager.muteRemoteAudio(linkMicId, true);
            }
            if (isMuteAllVideo) {
                linkMicManager.muteRemoteVideo(linkMicId, true);
            }
        }
    }

    @Override
    public void releaseRenderView(SurfaceView renderView) {
        linkMicManager.releaseRenderView(renderView);
    }

    @Override
    public boolean isJoinLinkMic() {
        if (rtcInvokeStrategy != null) {
            return rtcInvokeStrategy.isJoinLinkMic();
        } else {
            return false;
        }
    }

    @Override
    public boolean isJoinChannel() {
        if (rtcInvokeStrategy != null) {
            return rtcInvokeStrategy.isJoinChannel();
        } else {
            return false;
        }
    }

    @Override
    public void setIsAudioLinkMic(boolean isAudioLinkMic) {
        long interval = (System.currentTimeMillis() - socketRefreshOpenStatusData) / 1000;
        //服务端轮询到的连麦类型，距离上一次socket获取的类型的时间差不足10秒，就不通过轮询更新了
        if (interval < 10) {
            return;
        }
        this.isAudioLinkMic = isAudioLinkMic;//通过服务端轮询接口获取连麦类型
    }

    @Override
    public boolean getIsAudioLinkMic() {
        return isAudioLinkMic;
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        this.isTeacherOpenLinkMic = isTeacherOpenLinkMic;
        if (isJoinLinkMic() && !isTeacherOpenLinkMic) {
            leaveLinkMic();
        }
    }

    @Override
    public boolean isTeacherOpenLinkMic() {
        return isTeacherOpenLinkMic;
    }

    @Override
    public boolean isAloneChannelTypeSupportRTC() {
        return liveRoomDataManager.getConfig().isAloneChannelType() && liveRoomDataManager.isSupportRTC();
    }

    @Override
    public void setLiveStart() {
        pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
            @Override
            public void run() {
                if (rtcInvokeStrategy != null) {
                    rtcInvokeStrategy.setLiveStart();
                }
            }
        });
    }


    @Override
    public void setLiveEnd() {
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setLiveEnd();
        }
    }

    @Override
    public void setWatchRtc(boolean watchRtc) {
        if (isWatchRtc == watchRtc) {
            return;
        }
        setLiveEnd();
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.destroy();
        }
        isWatchRtc = watchRtc;
        initRTCInvokeStrategy();
        setLiveStart();
    }

    @Override
    public int getRTCListSize() {
        return linkMicList.size();
    }

    @Override
    public void resetRequestPermissionList(ArrayList<String> permissions) {
        linkMicManager.resetRequestPermissionList(permissions);
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
        String sessionId = liveRoomDataManager.getSessionId();
        //rtc观看的时候，由于还没有onPrepared，所以还拿不到sessionId。
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        linkMicManager.getLinkStatus(sessionId, new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
            @Override
            public void onSuccess(PLVLinkMicJoinStatus data) {
                PLVCommonLog.d(TAG, "PLVLinkMicPresenter.requestLinkMicListFromServer.onSuccess->\n" + PLVGsonUtil.toJson(data));
                if (data.getJoinList().isEmpty()) {
                    return;
                }
                List<String> newJoinUserList = new ArrayList<>();
                //老师连麦ID
                String teacherLinkMicId = "";
                //嘉宾连麦ID
                String guestLinkMicId = "";

                //嘉宾可能被挂断连麦后，还一直留在连麦列表里。不过我们可以通过他的voice字段是否=1来区分他是否有上麦。没有上麦，就从data中删掉。
                Iterator<PLVJoinInfoEvent> joinInfoEventIterator = data.getJoinList().iterator();
                while (joinInfoEventIterator.hasNext()) {
                    PLVJoinInfoEvent plvJoinInfoEvent = joinInfoEventIterator.next();
                    if (PLVSocketUserConstant.USERTYPE_GUEST.equals(plvJoinInfoEvent.getUserType()) && !plvJoinInfoEvent.getClassStatus().isVoice()) {
                        joinInfoEventIterator.remove();
                    }
                }


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
                        //从连麦列表中添加用户
                        linkMicList.add(itemDataBean);
                        muteCacheList.updateUserMuteCacheWhenJoinList(itemDataBean);
                        newJoinUserList.add(plvJoinInfoEvent.getUserId());
                    }

                    String userType = plvJoinInfoEvent.getUserType();
                    if (userType != null) {
                        switch (userType) {
                            case PLVSocketUserConstant.USERTYPE_TEACHER:
                                teacherLinkMicId = plvJoinInfoEvent.getUserId();
                                break;
                            case PLVSocketUserConstant.USERTYPE_GUEST:
                                if (TextUtils.isEmpty(guestLinkMicId)) {
                                    guestLinkMicId = plvJoinInfoEvent.getUserId();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (TextUtils.isEmpty(teacherLinkMicId)) {
                    PLVCommonLog.d(TAG, "该频道内不存在讲师");
                }
                if (TextUtils.isEmpty(guestLinkMicId)) {
                    PLVCommonLog.d(TAG, "该频道内不存在嘉宾");
                }

                //2. 设置初始化的第一画面用户ID
                String firstScreenLinkMicId = data.getMaster();
                //      如果初始化的第一画面是空，就以主讲作为第一画面
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = teacherLinkMicId;
                }
                //      如果主讲不存在，则以嘉宾作为第一画面
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = guestLinkMicId;
                }
                //      如果嘉宾也不存在，则以连麦列表中第一个观众作为第一画面
                if (TextUtils.isEmpty(firstScreenLinkMicId)) {
                    firstScreenLinkMicId = data.getJoinList().get(0).getUserId();
                }
                PLVCommonLog.d(TAG, "第一画面:" + firstScreenLinkMicId);

                if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
                    rtcInvokeStrategy.setFirstScreenLinkMicId(firstScreenLinkMicId, isMuteAllVideo);
                    if (linkMicView != null) {
                        //位置传递-1表示不需要对新旧位置的View渲染更新，只记录第一画面的id即可。
                        linkMicView.updateFirstScreenChanged(firstScreenLinkMicId, -1, -1);
                    }

                    if (linkMicList.size() > 0
                            && !linkMicList.get(0).getLinkMicId().equals(firstScreenLinkMicId)) {
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
                }

                //3. 将新添加的用户回调出去
                if (!newJoinUserList.isEmpty()) {
                    if (linkMicView != null) {
                        linkMicView.onUsersJoin(newJoinUserList);
                    }
                }

                //4. 纯视频频道类型，如果列表列表添加了讲师，则调整讲师的位置
                if (liveRoomDataManager.getConfig().isAloneChannelType()
                        && teacherLinkMicId != null
                        && newJoinUserList.contains(teacherLinkMicId)) {
                    mainTeacherLinkMicId = teacherLinkMicId;
                    if (linkMicView != null) {
                        for (int i = 0; i < linkMicList.size(); i++) {
                            PLVLinkMicItemDataBean plvLinkMicItemDataBean = linkMicList.get(i);
                            if (teacherLinkMicId.equals(plvLinkMicItemDataBean.getLinkMicId())) {
                                final String finalFirstScreenLinkMicId = firstScreenLinkMicId;
                                final String finalTeacherLinkMicId = teacherLinkMicId;
                                linkMicView.onAdjustTeacherLocation(teacherLinkMicId, i, liveRoomDataManager.isSupportRTC(), new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!finalFirstScreenLinkMicId.equals(finalTeacherLinkMicId)) {
                                            //如果第一画面不是讲师，那么要手动进行一个第一画面的切换
                                            socketEventListener.onSwitchFirstScreen(finalFirstScreenLinkMicId);
                                        }
                                    }
                                });
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
        //是否打开本地视频
        linkMicManager.enableLocalVideo(!isAudioLinkMic);
        //加入频道
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setJoinLinkMic();
        }
        if (linkMicView != null) {
            linkMicView.onTeacherAllowJoin();
        }
    }

    private void handleTeacherCloseLinkMic() {
        if (isTeacherOpenLinkMic) {
            isTeacherOpenLinkMic = false;
            if (linkMicView != null) {
                linkMicView.onTeacherCloseLinkMic();
            }
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
    void leaveChannel() {
        if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
            dispose(getLinkMicListTimer);
            cleanLinkMicListData();
            muteCacheList.clear();
            if (linkMicView != null) {
                linkMicView.onLeaveRtcChannel();
            }
        }
    }

    private void cleanLinkMicListData() {
        PLVCommonLog.d(TAG, "cleanLinkMicListData() called \n" + Log.getStackTraceString(new Throwable()));
        linkMicList.clear();
    }

    /**
     * 加载连麦音视频模式
     */
    private void loadLinkMicConnectMode(String mode) {
        if (TextUtils.isEmpty(mode)) {
            //默认上麦后，摄像头关闭，麦克风打开
            muteVideo(true);
            muteAudio(false);
            return;
        }
        if ("audio".equals(mode)) {
            muteAudio(true);
            //默认关闭摄像头
            muteVideo(true);
        } else if ("video".equals(mode)) {
            muteVideo(true);
            //默认开启了音频
            muteAudio(false);
        }

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    /**
     * 将依赖linkMicManager初始化后执行的操作临时挂起。
     * 如果linkMicManager已经初始化了，就直接执行。
     *
     * @param action 将操作封装并挂起
     */
    void pendingActionInCaseLinkMicEngineInitializing(final Runnable action) {
        switch (linkMicInitState) {
            case LINK_MIC_UNINITIATED:
                if (actionAfterLinkMicEngineCreated != null) {
                    actionAfterLinkMicEngineCreated.add(action);
                } else {
                    action.run();
                }
                break;
            case LINK_MIC_INITIATED:
                actionAfterLinkMicEngineCreated = null;
                action.run();
                break;
            default:
                break;
        }
    }

    @Nullable
    IPLVLinkMicContract.IPLVLinkMicView getLinkMicView() {
        return linkMicView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - rtc事件接收器">
    private class PolyvLinkMicEventListenerImpl extends PolyvLinkMicEventListener {
        @Override
        public void onJoinChannelSuccess(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onJoinChannelSuccess, uid=" + uid);
            stopJoinTimeoutCount();
            //连麦限制，全体静音响应
            loadLinkMicConnectMode(avConnectMode);
        }

        @Override
        public void onLeaveChannel() {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onLeaveChannel");
            leaveChannel();
        }

        @Override
        public void onUserJoined(String uid) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserJoined, uid=" + uid);
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
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserOffline, uid=" + uid);
            Iterator<PLVLinkMicItemDataBean> it = linkMicList.iterator();//移除
            while (it.hasNext()) {
                PLVLinkMicItemDataBean dataBean = it.next();
                if (dataBean.getLinkMicId().equals(uid)) {
                    if (linkMicView != null) {
                        linkMicView.onUsersLeave(Collections.singletonList(uid));
                    }
                    it.remove();
                    break;
                }
            }
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute) {
            PLVCommonLog.d(TAG, "PolyvLinkMicEventListenerImpl.onUserMuteAudio,uid=" + uid + " mute=" + mute);
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
            muteCacheList.addOrUpdateAudioMuteCacheList(uid, mute);
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
            muteCacheList.addOrUpdateVideoMuteCacheList(uid, mute);
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

        @Override
        public void onNetworkQuality(int quality) {
            super.onNetworkQuality(quality);
            if (linkMicView != null) {
                linkMicView.onNetQuality(quality);
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
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setLeaveLinkMic();
            }
        }

        @Override
        public void onTeacherOpenLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherOpenLinkMic");
            isTeacherOpenLinkMic = true;
            if (linkMicView != null) {
                linkMicView.onTeacherOpenLinkMic();
            }
            /***
             * ///暂时保留该代码
             * if (liveRoomDataManager.getConfig().isParticipant() && !isJoinLinkMic) {
             *     linkMicManager.enableLocalVideo(!isAudioLinkMic);
             *     //参与者加入时，将角色调整为观众角色
             *     linkMicManager.switchRoleToAudience();
             *     handleTeacherAllowJoin(isAudioLinkMic);
             * }
             */
        }

        @Override
        public void onTeacherCloseLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherCloseLinkMic");
            handleTeacherCloseLinkMic();
            if (rtcInvokeStrategy != null) {
                //只有在连麦中才能在断开连麦时发送joinLeave消息
                if(isJoinLinkMic()){
                    rtcInvokeStrategy.setLeaveLinkMic();
                }
            }
        }

        @Override
        public void onTeacherMuteMedia(boolean isMute, boolean isAudio) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherMuteMedia");

            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
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
            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
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
                muteCacheList.updateUserMuteCacheWhenJoinList(dataBean);
                if (dataBean.isTeacher()) {
                    // 添加讲师
                    linkMicList.add(0, dataBean);
                } else if (dataBean.getLinkMicId().equals(myLinkMicId)) {
                    // 添加自己
                    PLVCommonLog.d(TAG, "onUserJoinSuccess-> 收到自己的joinSuccess事件");
                } else {
                    // 添加观众
                    linkMicList.add(dataBean);
                }
                if (linkMicView != null) {
                    linkMicView.onUsersJoin(Collections.singletonList(dataBean.getLinkMicId()));
                }
            }
        }

        @Override
        public void onTeacherSendCup(String linkMicId, int cupNum) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherSendCup");
            if (rtcInvokeStrategy == null || !rtcInvokeStrategy.isJoinChannel()) {
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
            socketRefreshOpenStatusData = System.currentTimeMillis();
            isAudioLinkMic = isAudio;//通过socket消息更新连麦类型
        }

        @Override
        public void onSwitchFirstScreen(final String linkMicId) {
            final IPLVLinkMicContract.IPLVLinkMicView view = linkMicView;
            if (view == null || TextUtils.isEmpty(linkMicId)) {
                return;
            }
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setFirstScreenLinkMicId(linkMicId, isMuteAllVideo);
            }
            //将[linkMicId]切换到连麦列表的第一画面
            if (linkMicList.isEmpty()) {
                return;
            }
            final int oldFirstScreenPos = 0;
            //遍历找到target
            PLVLinkMicItemDataBean itemToBeFirst = linkMicList.get(oldFirstScreenPos);
            for (PLVLinkMicItemDataBean itemDataBean : linkMicList) {
                if (itemDataBean.getLinkMicId().equals(linkMicId)) {
                    itemToBeFirst = itemDataBean;
                }
            }
            final int indexOfTarget = linkMicList.indexOf(itemToBeFirst);

            if (liveRoomDataManager.getConfig().isAloneChannelType()) {
                //如果切换的第一画面的位置正好在media的位置，说明第一画面已经在主屏，则不再处理
                if (indexOfTarget == view.getMediaViewIndexInLinkMicList()) {
                    return;
                }
                //如果切换的第一画面为讲师
                if (mainTeacherLinkMicId != null && mainTeacherLinkMicId.equals(linkMicId)) {
                    //1. 如果Media在连麦列表，则将media切换到主屏幕即可
                    if (view.isMediaShowInLinkMicList()) {
                        view.performClickInLinkMicListItem(view.getMediaViewIndexInLinkMicList());
                    }
                } else {
                    //1. 切换的第一画面不为讲师，将第一画面和主屏幕的位置进行切换
                    view.performClickInLinkMicListItem(indexOfTarget);
                }
            } else {
                //如果target已经在第一画面，则不再做处理
                if (indexOfTarget == oldFirstScreenPos) {
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
                PLVLinkMicItemDataBean oldFirst = linkMicList.get(oldFirstScreenPos);
                linkMicList.remove(oldFirst);
                linkMicList.remove(itemToBeFirst);
                linkMicList.add(0, itemToBeFirst);
                linkMicList.add(indexOfTarget, oldFirst);
                view.onSwitchFirstScreen(linkMicId);

                //3. 将原先在连麦列表的PPT恢复到原先的位置
                if (pptNeedToGoBackToLinkMicList) {
                    view.performClickInLinkMicListItem(pptIndexInLinkMicList);
                }
            }

            view.updateFirstScreenChanged(linkMicId, oldFirstScreenPos, indexOfTarget);
        }

        @Override
        public void onSwitchPPTViewLocation(boolean toMainScreen) {
            if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
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
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setLiveEnd();
            }
        }

        @Override
        public void onLinkMicConnectMode(String avConnectMode) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onLinkMicConnectMode " + avConnectMode);
            //socket消息，早于连麦，缓存下来后更新刚进来时的连麦状态
            PLVLinkMicPresenter.this.avConnectMode = avConnectMode;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="请求权限">
    //请求权限，成功获取权限的话就申请连麦，否则不申请
    private void requestPermissionAndJoin() {

        Activity activity = ActivityUtils.getTopActivity();
        ArrayList<String> permissions = new ArrayList<>(Arrays.asList(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        ));
        PLVFastPermission.getInstance()
                .start(activity, permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        pendingActionInCaseLinkMicEngineInitializing(new Runnable() {
                            @Override
                            public void run() {
                                linkMicManager.sendJoinRequestMsg();
                            }
                        });
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        IPLVLinkMicTraceLogSender iplvLinkMicTraceLogSender = new PLVLinkMicTraceLogSender();
                        iplvLinkMicTraceLogSender.setLogModuleClass(PLVLinkMicELog.class);
                        iplvLinkMicTraceLogSender.submitTraceLog(PLVLinkMicELog.LinkMicTraceLogEvent.PERMISSION_DENIED," deniedPermissions: "+deniedPermissions+" deniedForeverP: "+deniedForeverP);
                        if (deniedForeverP == null) {
                            linkMicView.onLeaveLinkMic();
                        } else {
                            showRequestPermissionDialog();
                        }
                    }
                });
    }

    private void showRequestPermissionDialog() {
        new AlertDialog.Builder(ActivityUtils.getTopActivity()).setTitle("提示")
                .setMessage("通话所需的相机权限和麦克风权限被拒绝，请到应用设置的权限管理中恢复")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(ActivityUtils.getTopActivity());
                        linkMicView.onLeaveLinkMic();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ActivityUtils.getTopActivity(), "权限不足，申请发言失败", Toast.LENGTH_SHORT).show();
                        linkMicView.onLeaveLinkMic();
                    }
                }).setCancelable(false).show();
    }
    // </editor-fold>
}
