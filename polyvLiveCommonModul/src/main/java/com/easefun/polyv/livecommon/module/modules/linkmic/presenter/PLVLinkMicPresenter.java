package com.easefun.polyv.livecommon.module.modules.linkmic.presenter;

import static com.plv.foundationsdk.component.livedata.PLVLiveDataExt.mutableLiveData;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowModeGetter;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMsgHandler;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicMuteCacheList;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.usecase.PLVLinkMicRequestQueueOrderUseCase;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.PLVMultiRoomTransmitRepo;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.vo.PLVMultiRoomTransmitVO;
import com.easefun.polyv.livescenes.linkmic.listener.PolyvLinkMicEventListener;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.rx.PLVTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.log.IPLVLinkMicTraceLogSender;
import com.plv.linkmic.log.PLVLinkMicTraceLogSender;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVJoinLeaveEvent;
import com.plv.linkmic.model.PLVJoinRequestSEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVLinkMicMedia;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.listener.PLVLinkMicListener;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.manager.PLVLinkMicManagerFactory;
import com.plv.livescenes.linkmic.vo.PLVLinkMicEngineParam;
import com.plv.livescenes.log.linkmic.PLVLinkMicELog;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.event.PLVEventConstant;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.event.linkmic.PLVTeacherSetPermissionEvent;
import com.plv.socket.impl.PLVSocketMessageObserver;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import kotlin.jvm.functions.Function1;

/**
 * date: 2020/7/16
 * author: hwj
 * description: 连麦Presenter
 */
public class PLVLinkMicPresenter implements IPLVLinkMicContract.IPLVLinkMicPresenter, PLVViewerLinkMicState.OnStateActionListener {
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

    /**
     * uc
     */
    private PLVLinkMicRequestQueueOrderUseCase linkMicRequestQueueOrderUseCase;

    /**** Model ****/
    private IPLVLinkMicManager linkMicManager;
    private final PLVMultiRoomTransmitRepo multiRoomTransmitRepo = PLVDependManager.getInstance().get(PLVMultiRoomTransmitRepo.class);

    @Nullable
    private PLVLinkMicMsgHandler linkMicMsgHandler;
    @Nullable
    private IPLVRTCInvokeStrategy rtcInvokeStrategy;
    //账号数据
    private IPLVLiveRoomDataManager liveRoomDataManager;

    /**** 连麦状态数据 ****/
    //连麦初始化状态
    private int linkMicInitState = LINK_MIC_UNINITIATED;
    private volatile PLVViewerLinkMicState viewerLinkMicState = PLVViewerLinkMicState.initState().setOnStateActionListener(this);
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
    // 连麦是否打开摄像头
    private boolean enableLocalVideo = false;
    // 连麦是否打开麦克风
    private boolean enableLocalAudio = true;
    private boolean isEcommerceLinkMicItemSort = false;
    /**
     * 申请连麦排队序号，从0开始
     * value < 0 表明数据不可用
     */
    private final MutableLiveData<Integer> linkMicRequestQueueOrder = mutableLiveData(-1);
    // 转播数据
    @Nullable
    private PLVMultiRoomTransmitVO transmitVO;

    /**** Disposable ****/
    private Disposable getLinkMicListDelay;
    private Disposable getLinkMicListTimer;
    private Disposable linkJoinTimer;
    @NonNull
    private final List<Runnable> actionAfterLinkMicEngineCreated = new LinkedList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

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
        PLVLinkMicConfig.getInstance().init(viewerId, false);
        //view
        this.linkMicView = view;

        if (!initLinkMicManager()) {
            return;
        }

        linkMicRequestQueueOrderUseCase = new PLVLinkMicRequestQueueOrderUseCase(myLinkMicId, linkMicRequestQueueOrder);
        //连麦socket事件监听
        linkMicMsgHandler = new PLVLinkMicMsgHandler(myLinkMicId);
        linkMicMsgHandler.addLinkMicMsgListener(socketEventListener);
        //初始化RTC调用策略实现
        isWatchRtc = PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch() || PLVLinkMicConfig.getInstance().isLowLatencyMixRtcWatch();
        initRTCInvokeStrategy();
        //Socket事件监听
        messageListener = new PLVSocketMessageObserver.OnMessageListener() {
            @Override
            public void onMessage(String listenEvent, String event, String message) {
                if (event == null) {
                    return;
                }
                switch (event) {
                    case PLVEventConstant.MESSAGE_EVENT_RELOGIN: {
                        //当收到重登录的消息的时候，我们需要将判断一下是否是连麦状态，是的话就要断开连麦
                        if (isJoinLinkMic()) {
                            leaveLinkMic();
                        }
                        break;
                    }
                }
            }
        };
        PLVSocketWrapper.getInstance().getSocketObserver().addOnMessageListener(messageListener);

        observeTransmitChangeEvent();
    }

    /**
     * 初始化连麦管理器
     *
     * @return true -> success
     */
    private boolean initLinkMicManager() {
        destroyLinkMicManager();
        linkMicManager = PLVLinkMicManagerFactory.createNewLinkMicManager();

        final PLVLinkMicEngineParam param = new PLVLinkMicEngineParam()
                .setChannelId(getCurrentLinkMicChannelId())
                .setViewerId(liveRoomDataManager.getConfig().getUser().getViewerId())
                .setViewerType(liveRoomDataManager.getConfig().getUser().getViewerType())
                .setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        linkMicManager.initEngine(param, new PLVLinkMicListener() {
            @Override
            public void onLinkMicEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "连麦初始化成功");// no need i18n
                linkMicInitState = LINK_MIC_INITIATED;
                linkMicManager.addEventHandler(eventListener);

                for (Runnable action : actionAfterLinkMicEngineCreated) {
                    action.run();
                }
                actionAfterLinkMicEngineCreated.clear();
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
                linkMicView.onLinkMicError(-1, new Throwable(PLVAppUtils.getString(R.string.plv_linkmic_id_empty)));
            }
            return false;
        }
        return true;
    }

    private void initRTCInvokeStrategy() {
        if (PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch() && isWatchRtc) {
            //RTC 无延迟观看
            rtcInvokeStrategy = new PLVRTCPureStreamWatchStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new PLVRTCPureStreamWatchStrategy.OnJoinRTCChannelWatchListener() {
                        @Override
                        public void onJoinRTCChannelWatch() {
                            if (linkMicView != null) {
                                linkMicView.onStartRtcWatch();
                                linkMicView.onStartPureRtcWatch();
                            }
                            stopJoinTimeoutCount();
                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
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
                                    sort(linkMicList);
                                } else {
                                    linkMicList.add(1, selfDataBean);//添加自己
                                    sort(linkMicList);
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
                            it.remove();
                            if (linkMicView != null) {
                                linkMicView.onUsersLeave(Collections.singletonList(myLinkMicId));
                            }
                            break;
                        }
                    }

                    if (linkMicView != null) {
                        linkMicView.onChangeListShowMode(PLVLinkMicListShowModeGetter.getLeavedMicShowMode());
                        linkMicView.onLeaveLinkMic();
                    }
                }
            });
        } else if (PLVLinkMicConfig.getInstance().isLowLatencyMixRtcWatch() && isWatchRtc) {
            // rtc混流观看
            rtcInvokeStrategy = new PLVRTCMixStreamWatchStrategy(
                    this, linkMicManager, liveRoomDataManager,
                    new IPLVRTCInvokeStrategy.OnJoinLinkMicListener() {
                        @Override
                        public void onJoinLinkMic(PLVLinkMicJoinSuccess data) {
                            stopJoinTimeoutCount();
                            if (!linkMicList.isEmpty()) {
                                PLVCommonLog.w(TAG, "rtc混流观看，加入连麦时，连麦列表不为空！手动清空连麦列表，连麦列表为：\n" + linkMicList);
                                cleanLinkMicListData();
                            }
                            //如果是普通连麦观众，则rtc上麦就表示加入了连麦列表
                            linkMicList.add(0, PLVLinkMicDataMapper.map2LinkMicItemData(data));

                            if (linkMicView != null) {
                                linkMicView.onStartPureRtcWatch();
                                linkMicView.onJoinLinkMic();
                            }

                            loadLinkMicConnectMode(avConnectMode);

                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    requestLinkMicListFromServer();
                                }
                            });
                        }
                    },
                    new PLVRTCMixStreamWatchStrategy.OnRtcWatchListener() {
                        @Override
                        public void onStartRtcWatch() {
                            if (linkMicView != null) {
                                linkMicView.onStartRtcWatch();
                            }
                        }

                        @Override
                        public void onStopRtcWatch() {
                            if (linkMicView != null) {
                                linkMicView.onStopRtcWatch();
                            }
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
                                PLVCommonLog.w(TAG, "非无延迟观看，加入连麦时，连麦列表不为空！手动清空连麦列表，连麦列表为：\n" + linkMicList.toString());// no need i18n
                                cleanLinkMicListData();
                            }
                            //如果是普通连麦观众，则rtc上麦就表示加入了连麦列表
                            linkMicList.add(0, PLVLinkMicDataMapper.map2LinkMicItemData(data));//添加自己
                            //如果是参与者，则是自动加入rtc频道的，要等讲师同意，才能加入连麦列表
                            if (linkMicView != null) {
                                linkMicView.onStartRtcWatch();
                                linkMicView.onStartPureRtcWatch();
                                linkMicView.onJoinLinkMic();
                            }

                            loadLinkMicConnectMode(avConnectMode);

                            dispose(getLinkMicListTimer);
                            getLinkMicListTimer = PLVTimer.timer(INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
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

    // <editor-fold defaultstate="collapsed" desc="初始化">

    private void observeTransmitChangeEvent() {
        Disposable disposable = multiRoomTransmitRepo.transmitObservable
                .retry()
                .subscribe(new Consumer<PLVMultiRoomTransmitVO>() {
                    @Override
                    public void accept(PLVMultiRoomTransmitVO multiRoomTransmitVO) throws Exception {
                        final boolean isWatchMainRoomLastTime = transmitVO != null && transmitVO.isWatchMainRoom();
                        transmitVO = multiRoomTransmitVO;
                        if (isWatchMainRoomLastTime != multiRoomTransmitVO.isWatchMainRoom()) {
                            // 重新创建连麦
                            setLiveEnd();
                            if (rtcInvokeStrategy != null) {
                                rtcInvokeStrategy.destroy();
                            }
                            destroyLinkMicManager();
                            initLinkMicManager();
                            initRTCInvokeStrategy();
                            setLiveStart();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
        compositeDisposable.add(disposable);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API-presenter接口实现">
    @Override
    public void destroy() {
        //判断是否处于连麦状态，是连麦状态下才会发送joinLeave
        if (isJoinLinkMic()) {
            //发送joinLeave
            linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        }
        leaveChannel();
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.destroy();
        }
        compositeDisposable.dispose();
        dispose(getLinkMicListDelay);
        dispose(getLinkMicListTimer);
        PLVSocketWrapper.getInstance().getSocketObserver().removeOnMessageListener(messageListener);
        linkMicList.clear();
        muteCacheList.clear();
        myLinkMicId = "";
        this.linkMicView = null;
        destroyLinkMicManager();
        if (linkMicMsgHandler != null) {
            linkMicMsgHandler.destroy();
        }
    }

    @Override
    public void requestJoinLinkMic() {
        viewerLinkMicState.onRequestJoinLinkMic();
    }

    @Override
    public void cancelRequestJoinLinkMic() {
        viewerLinkMicState.onCancelLinkMic();
    }

    @Override
    public void answerLinkMicInvitation(boolean accept, boolean isTimeout, boolean openCamera, boolean openMicrophone) {
        enableLocalVideo = openCamera;
        enableLocalAudio = openMicrophone;
        if (accept) {
            viewerLinkMicState.onAcceptInviteLinkMic();
        } else {
            viewerLinkMicState.onCancelLinkMic();
            if (!isTimeout) {
                linkMicManager.sendJoinAnswerMsg(false);
            }
        }
    }

    @Override
    public void getJoinAnswerTimeLeft(PLVSugarUtil.Consumer<Integer> callback) {
        linkMicManager.getJoinAnswerTimeLeft(callback);
    }

    @Override
    public void setEcommerceLinkMicItemSort(boolean isEcommerceLinkMicItemSort) {
        this.isEcommerceLinkMicItemSort = isEcommerceLinkMicItemSort;
    }

    @Override
    public void leaveLinkMic() {
        viewerLinkMicState.onCancelLinkMic();
        if (rtcInvokeStrategy != null) {
            rtcInvokeStrategy.setLeaveLinkMic();
        }
    }

    @Override
    public void muteAudio(boolean mute) {
        this.enableLocalAudio = !mute;

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
        this.enableLocalVideo = !mute;

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
    public boolean isEnableLocalAudio() {
        return enableLocalAudio;
    }

    @Override
    public boolean isEnableLocalVideo() {
        return enableLocalVideo;
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
    public TextureView createTextureRenderView(Context context) {
        return linkMicManager.createTextureRenderView(context);
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
    public void setupRenderView(View renderView, String linkMicId) {
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
    public void releaseRenderView(View renderView) {
        linkMicManager.releaseRenderView(renderView);
    }

    @Override
    public void setupMixStreamView(View renderView) {
        linkMicManager.setupMixStreamVideo(renderView);
    }

    @Override
    public void releaseMixStreamView(View renderView) {
        linkMicManager.stopMixStreamVideo(renderView);
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
        if (linkMicView != null) {
            linkMicView.onUpdateLinkMicType(isAudioLinkMic);
        }
    }

    @Override
    public boolean getIsAudioLinkMic() {
        return isAudioLinkMic;
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        this.isTeacherOpenLinkMic = isTeacherOpenLinkMic;
        if (isJoinLinkMic() && !isTeacherOpenLinkMic && !isNewLinkMicStrategy()) {
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
        viewerLinkMicState.onTeacherNotAllowToJoin();
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
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                setLiveStart();
            }
        });
    }

    @Override
    public int getRTCListSize() {
        return linkMicList.size();
    }

    @Override
    public void resetRequestPermissionList(ArrayList<String> permissions) {
        linkMicManager.resetRequestPermissionList(permissions);
    }

    @Override
    public LiveData<Integer> getLinkMicRequestQueueOrder() {
        return linkMicRequestQueueOrder;
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
                        sort(linkMicList);
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
                    PLVCommonLog.d(TAG, "该频道内不存在讲师");// no need i18n
                }
                if (TextUtils.isEmpty(guestLinkMicId)) {
                    PLVCommonLog.d(TAG, "该频道内不存在嘉宾");// no need i18n
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
                PLVCommonLog.d(TAG, "第一画面:" + firstScreenLinkMicId);// no need i18n

                if (rtcInvokeStrategy != null && rtcInvokeStrategy.isJoinChannel()) {
                    rtcInvokeStrategy.setFirstScreenLinkMicId(firstScreenLinkMicId, isMuteAllVideo);
                    //位置传递-1表示不需要对新旧位置的View渲染更新，只记录第一画面的id即可。
                    dispatchOnFirstScreenChanged(firstScreenLinkMicId, -1, -1);

                    if (linkMicList.size() > 0
                            && !linkMicList.get(0).getLinkMicId().equals(firstScreenLinkMicId)) {
                        //找出第一画面，并插入到连麦列表顶部
                        PLVLinkMicItemDataBean firstScreenDataBean = null;
                        for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                            plvLinkMicItemDataBean.setFirstScreen(false);
                            if (plvLinkMicItemDataBean.getLinkMicId().equals(firstScreenLinkMicId)) {
                                firstScreenDataBean = plvLinkMicItemDataBean;
                                linkMicList.remove(plvLinkMicItemDataBean);
                                break;
                            }
                        }
                        if (firstScreenDataBean != null) {
                            firstScreenDataBean.setFirstScreen(true);
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

                // 回调连麦状态
                for (PLVJoinInfoEvent joinInfoEvent : data.getJoinList()) {
                    if (myLinkMicId.equals(joinInfoEvent.getUserId())) {
                        callbackMyClassStatus(joinInfoEvent.getClassStatus());
                        break;
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

            private void callbackMyClassStatus(PLVJoinInfoEvent.ClassStatus classStatus) {
                // 画笔状态
                final boolean localHasPaintPermission = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.LIVE_LINKMIC_VIEWER_GRANTED_PAINT);
                final boolean remoteHasPaintPermission = classStatus.isPaint();
                if (localHasPaintPermission != remoteHasPaintPermission) {
                    if (remoteHasPaintPermission) {
                        PLVUserAbilityManager.myAbility().addRole(PLVUserRole.LIVE_LINKMIC_VIEWER_GRANTED_PAINT);
                    } else {
                        PLVUserAbilityManager.myAbility().removeRole(PLVUserRole.LIVE_LINKMIC_VIEWER_GRANTED_PAINT);
                    }
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket事件处理">
    private void handleTeacherAllowJoin() {
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
        linkJoinTimer = PLVTimer.delay(TIME_OUT_JOIN_CHANNEL, new Consumer<Long>() {
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

    // <editor-fold defaultstate="collapsed" desc="连麦状态回调">

    @Override
    public void sendJoinRequest(PLVViewerLinkMicState state) {
        requestPermissionAndJoin();
    }

    @Override
    public void sendJoinLeave(PLVViewerLinkMicState state) {
        linkMicManager.sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
    }

    @Override
    public void sendAcceptJoinInvite(PLVViewerLinkMicState state) {
        linkMicManager.sendJoinAnswerMsg(true);
    }

    @Override
    public void onStateChanged(PLVViewerLinkMicState oldState, PLVViewerLinkMicState newState) {
        viewerLinkMicState = newState;
        if (linkMicView != null) {
            linkMicView.onLinkMicStateChanged(oldState, newState);
        }
    }

    @Override
    public void checkLinkMicLimited(@NonNull final PLVViewerLinkMicState.OnCheckLinkMicLimitedCallback onCheckLinkMicLimitedCallback) {
        final int linkMicLimit = getNullableOrDefault(new PLVSugarUtil.Supplier<Integer>() {
            @Override
            public Integer get() {
                return liveRoomDataManager.getClassDetailVO().getValue().getData().getData().getLinkMicLimit();
            }
        }, 0);

        linkMicManager.getLinkStatus(liveRoomDataManager.getSessionId(), new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
            @Override
            public void onSuccess(PLVLinkMicJoinStatus data) {
                final int linkMicConnectedCount = PLVSequenceWrapper.wrap(data.getJoinList())
                        .filter(new Function1<PLVJoinInfoEvent, Boolean>() {
                            @Override
                            public Boolean invoke(PLVJoinInfoEvent joinInfoEvent) {
                                return PLVSocketUserConstant.USERTYPE_TEACHER.equals(joinInfoEvent.getUserType())
                                        || joinInfoEvent.getClassStatus().isVoice();
                            }
                        })
                        .toMutableList().size();
                if (linkMicConnectedCount >= linkMicLimit + 1/*讲师*/) {
                    onCheckLinkMicLimitedCallback.onLimited(viewerLinkMicState);
                    if (linkMicView != null) {
                        linkMicView.onLinkMicMemberReachLimit();
                    }
                    return;
                }

                onCheckLinkMicLimitedCallback.onSuccess(viewerLinkMicState);
            }

            @Override
            public void onFail(PLVLinkMicHttpRequestException throwable) {
                PLVCommonLog.exception(throwable);
                onCheckLinkMicLimitedCallback.onLimited(viewerLinkMicState);
            }
        });
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
                linkMicView.onStopPureRtcWatch();
                linkMicView.onStopRtcWatch();
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
        if ("audio".equals(mode)) {
            muteAudio(!enableLocalAudio);
            muteVideo(true);
        } else {
            muteVideo(!enableLocalVideo);
            muteAudio(!enableLocalAudio);
        }

    }

    private void dispatchOnFirstScreenChanged(String firstScreenLinkMicId, int oldPos, int newPos) {
        if (linkMicView != null) {
            linkMicView.updateFirstScreenChanged(firstScreenLinkMicId, oldPos, newPos);
        }
        updateLinkMicBitrateOnFirstScreenChanged(firstScreenLinkMicId);
    }

    private void updateLinkMicBitrateOnFirstScreenChanged(String firstScreenLinkMicId) {
        final boolean isMyFirstScreen = myLinkMicId != null && myLinkMicId.equals(firstScreenLinkMicId);
        if (isMyFirstScreen && isJoinLinkMic()) {
            linkMicManager.setBitrate(
                    PLVChannelFeatureManager.onChannel(getCurrentLinkMicChannelId())
                            .getOrDefault(PLVChannelFeature.LIVE_LINK_MIC_FIRST_SCREEN_PUSH_BITRATE, PLVLinkMicConstant.Bitrate.BITRATE_STANDARD)
            );
        } else {
            linkMicManager.setBitrate(PLVLinkMicConstant.Bitrate.BITRATE_STANDARD);
        }
    }

    private boolean isNewLinkMicStrategy() {
        if (liveRoomDataManager == null) {
            return false;
        }
        return PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
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
                actionAfterLinkMicEngineCreated.add(action);
                break;
            case LINK_MIC_INITIATED:
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

    private void destroyLinkMicManager() {
        linkMicInitState = LINK_MIC_UNINITIATED;
        if (linkMicManager != null) {
            linkMicManager.destroy();
        }
    }

    private String getCurrentLinkMicChannelId() {
        if (transmitVO == null || !transmitVO.isWatchMainRoom()) {
            if (liveRoomDataManager != null) {
                return liveRoomDataManager.getConfig().getChannelId();
            } else {
                return null;
            }
        }
        return transmitVO.mainRoomChannelId;
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
            getLinkMicListDelay = PLVTimer.delay(DELAY_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
                @Override
                public void accept(Long o) throws Exception {
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
        public void onNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
            super.onNetworkQuality(quality);
            if (linkMicView != null) {
                linkMicView.onNetQuality(quality);
            }
        }

        @Override
        public void onVideoSizeChanged(String uid, int width, int height) {
            if (linkMicView != null) {
                linkMicView.onVideoSizeChanged(uid, width, height);
            }
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - socket事件接收器">
    private class PolyvLinkMicSocketEventListener implements PLVLinkMicMsgHandler.OnLinkMicDataListener {

        @Override
        public void onUserJoinRequest(PLVJoinRequestSEvent joinRequestEvent) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onUserJoinRequest");
            linkMicRequestQueueOrderUseCase.onUserJoinRequest(joinRequestEvent);
        }

        @Override
        public void onUserJoinLeave(PLVJoinLeaveEvent joinLeaveEvent) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onUserJoinLeave");
            linkMicRequestQueueOrderUseCase.onUserJoinLeave(joinLeaveEvent);
        }

        @Override
        public void onTeacherAllowMeToJoin(PLVJoinResponseSEvent joinResponseEvent) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherAllowMeToJoin");
            viewerLinkMicState.onTeacherAllowToJoin();
            handleTeacherAllowJoin();
        }

        @Override
        public void onTeacherInviteToJoin(PLVJoinResponseSEvent joinResponseEvent) {
            if (joinResponseEvent == null || joinResponseEvent.getUser() == null || joinResponseEvent.getUser().getUserId() == null) {
                return;
            }
            final String socketUserId = nullable(new PLVSugarUtil.Supplier<String>() {
                @Override
                public String get() {
                    return PLVSocketWrapper.getInstance().getLoginVO().getUserId();
                }
            });
            if (!joinResponseEvent.getUser().getUserId().equals(socketUserId)
                    && !joinResponseEvent.getUser().getUserId().equals(myLinkMicId)) {
                return;
            }

            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherInviteToJoin");
            viewerLinkMicState.onTeacherInviteToJoin();
        }

        @Override
        public void onTeacherHangupMe() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherHangupMe");
            viewerLinkMicState.onTeacherNotAllowToJoin();
            if (rtcInvokeStrategy != null) {
                rtcInvokeStrategy.setLeaveLinkMic();
            }
            if (linkMicView != null) {
                linkMicView.onTeacherHangupMe();
            }
        }

        @Override
        public void onTeacherOpenLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherOpenLinkMic");
            isTeacherOpenLinkMic = true;
            if (linkMicView != null) {
                linkMicView.onTeacherOpenLinkMic();
            }
        }

        @Override
        public void onTeacherCloseLinkMic() {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onTeacherCloseLinkMic");
            if (isNewLinkMicStrategy()) {
                viewerLinkMicState.onTeacherNotAllowRaiseHand();
                handleTeacherCloseLinkMic();
            } else {
                viewerLinkMicState.onTeacherNotAllowToJoin();
                handleTeacherCloseLinkMic();
                if (rtcInvokeStrategy != null) {
                    //只有在连麦中才能在断开连麦时发送joinLeave消息
                    if (isJoinLinkMic()) {
                        rtcInvokeStrategy.setLeaveLinkMic();
                    }
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
                        muteAudio(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteAudio(myLinkMicId, isMute, i);
                        }
                    } else {
                        plvLinkMicItemDataBean.setMuteVideo(isMute);
                        muteVideo(isMute);
                        if (linkMicView != null) {
                            linkMicView.onUserMuteVideo(myLinkMicId, isMute, i);
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onUserJoinSuccess(PLVLinkMicItemDataBean dataBean, PLVLinkMicJoinSuccess joinSuccessEvent) {
            PLVCommonLog.d(TAG, "PolyvLinkMicSocketEventListener.onUserJoinSuccess");
            linkMicRequestQueueOrderUseCase.onUserJoinSuccess(joinSuccessEvent);
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
                    PLVCommonLog.d(TAG, "onUserJoinSuccess-> 收到自己的joinSuccess事件");// no need i18n
                } else {
                    // 添加观众
                    linkMicList.add(dataBean);
                }
                sort(linkMicList);
                if (linkMicView != null) {
                    linkMicView.onUsersJoin(Collections.singletonList(dataBean.getLinkMicId()));
                }
            }
        }

        @Override
        public void onTeacherSetPermission(PLVTeacherSetPermissionEvent setPermissionEvent) {
            if (setPermissionEvent == null || setPermissionEvent.getUserId() == null) {
                return;
            }
            final String socketUserId = nullable(new PLVSugarUtil.Supplier<String>() {
                @Override
                public String get() {
                    return PLVSocketWrapper.getInstance().getLoginVO().getUserId();
                }
            });
            final boolean isSetMyPermission = setPermissionEvent.getUserId().equals(socketUserId)
                    || setPermissionEvent.getUserId().equals(myLinkMicId);
            if (isSetMyPermission) {
                // 画笔权限
                if (PLVTeacherSetPermissionEvent.TYPE_PAINT.equals(setPermissionEvent.getType())) {
                    if (PLVTeacherSetPermissionEvent.STATUS_GRANT.equals(setPermissionEvent.getStatus())) {
                        PLVUserAbilityManager.myAbility().addRole(PLVUserRole.LIVE_LINKMIC_VIEWER_GRANTED_PAINT);
                    } else {
                        PLVUserAbilityManager.myAbility().removeRole(PLVUserRole.LIVE_LINKMIC_VIEWER_GRANTED_PAINT);
                    }
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
            if (linkMicView != null) {
                linkMicView.onUpdateLinkMicType(isAudioLinkMic);
            }
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
                oldFirst.setFirstScreen(false);
                itemToBeFirst.setFirstScreen(true);
                linkMicList.remove(oldFirst);
                linkMicList.remove(itemToBeFirst);
                linkMicList.add(0, itemToBeFirst);
                linkMicList.add(indexOfTarget, oldFirst);
                sort(linkMicList);
                view.onSwitchFirstScreen(linkMicId);

                //3. 将原先在连麦列表的PPT恢复到原先的位置
                if (pptNeedToGoBackToLinkMicList) {
                    view.performClickInLinkMicListItem(pptIndexInLinkMicList);
                }
            }

            dispatchOnFirstScreenChanged(linkMicId, oldFirstScreenPos, indexOfTarget);
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
            viewerLinkMicState.onTeacherNotAllowToJoin();
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
        new AlertDialog.Builder(ActivityUtils.getTopActivity()).setTitle(R.string.plv_common_dialog_tip)
                .setMessage(R.string.plv_linkmic_error_tip_permission_denied)
                .setPositiveButton(R.string.plv_common_dialog_confirm_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(ActivityUtils.getTopActivity());
                        linkMicView.onLeaveLinkMic();
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ActivityUtils.getTopActivity(), R.string.plv_linkmic_error_tip_permission_cancel, Toast.LENGTH_SHORT).show();
                        linkMicView.onLeaveLinkMic();
                    }
                }).setCancelable(false).show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 排序成员列表">
    private void sort(List<PLVLinkMicItemDataBean> linkMicList) {
        if (isEcommerceLinkMicItemSort) {
            SortLinkMicListUtils.sort(linkMicList);
        }
    }

    public static class SortLinkMicListUtils {
        //按第一画面>讲师>自己>嘉宾>管理员>助教>非虚拟用户>虚拟用户类型进行排序
        private static final String FIRST = "第一画面";// no need i18n
        private static final String SELF = "自己";// no need i18n
        private static final String REAL = "非虚拟";// no need i18n
        private static final String REAL_LINK_MIC_RTC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_RTC_JOIN;
        private static final String REAL_LINK_MIC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_JOIN;
        private static final String REAL_LINK_MIC_JOINING = REAL + PLVLinkMicItemDataBean.STATUS_JOINING;
        private static final String REAL_LINK_MIC_WAIT = REAL + PLVLinkMicItemDataBean.STATUS_WAIT;
        private static final List<String> SORT_INDEX = Arrays.asList(
                FIRST,
                PLVSocketUserConstant.USERTYPE_TEACHER,
                SELF,
                PLVSocketUserConstant.USERTYPE_GUEST,
                PLVSocketUserConstant.USERTYPE_MANAGER,
                PLVSocketUserConstant.USERTYPE_VIEWER,
                PLVSocketUserConstant.USERTYPE_ASSISTANT,
                REAL_LINK_MIC_WAIT,
                REAL_LINK_MIC_JOINING,
                REAL_LINK_MIC_JOIN,
                REAL_LINK_MIC_RTC_JOIN,
                REAL,
                PLVSocketUserConstant.USERTYPE_DUMMY
        );

        private static String getSortType(PLVLinkMicItemDataBean linkMicItemDataBean) {
            String type = linkMicItemDataBean.getUserType();
            String myUserId = PLVSocketWrapper.getInstance().getLoginVO().getUserId();
            if (linkMicItemDataBean.isFirstScreen()) {
                type = FIRST;
                return type;
            }
            if (myUserId.equals(linkMicItemDataBean.getUserId())) {
                type = SELF;
                return type;
            }
            if (!PLVSocketUserConstant.USERTYPE_MANAGER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_TEACHER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_GUEST.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_VIEWER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_DUMMY.equals(type)) {
                if (linkMicItemDataBean.isRtcJoinStatus()) {
                    type = REAL_LINK_MIC_RTC_JOIN;
                    return type;
                } else if (linkMicItemDataBean.isJoinStatus()) {
                    type = REAL_LINK_MIC_JOIN;
                    return type;
                } else if (linkMicItemDataBean.isJoiningStatus()) {
                    type = REAL_LINK_MIC_JOINING;
                    return type;
                } else if (linkMicItemDataBean.isWaitStatus()) {
                    type = REAL_LINK_MIC_WAIT;
                    return type;
                }
                type = REAL;
            }
            return type;
        }

        public static List<PLVLinkMicItemDataBean> sort(List<PLVLinkMicItemDataBean> linkMicList) {
            Collections.sort(linkMicList, new Comparator<PLVLinkMicItemDataBean>() {
                @Override
                public int compare(PLVLinkMicItemDataBean o1, PLVLinkMicItemDataBean o2) {
                    int io1 = SORT_INDEX.indexOf(getSortType(o1));
                    int io2 = SORT_INDEX.indexOf(getSortType(o2));
                    return io1 - io2;
                }
            });
            return linkMicList;
        }
    }
    // </editor-fold>
}
