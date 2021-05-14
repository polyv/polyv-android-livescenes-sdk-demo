package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.SurfaceView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.data.PLVStreamerData;
import com.easefun.polyv.livescenes.chatroom.PolyvChatApiRequestHelper;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.linkmic.manager.PolyvLinkMicConfig;
import com.easefun.polyv.livescenes.log.chat.PolyvChatroomELog;
import com.easefun.polyv.livescenes.model.PLVSListUsersVO;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livescenes.streamer.IPLVSStreamerManager;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livescenes.streamer.linkmic.IPLVSLinkMicEventSender;
import com.easefun.polyv.livescenes.streamer.linkmic.PLVSLinkMicEventSender;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSOnGetSessionIdInnerListener;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSStreamerOnLiveStreamingStartListener;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSStreamerOnLiveTimingListener;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSStreamerOnServerTimeoutDueToNetBrokenListener;
import com.easefun.polyv.livescenes.streamer.listener.PLVSStreamerEventListener;
import com.easefun.polyv.livescenes.streamer.listener.PLVSStreamerListener;
import com.easefun.polyv.livescenes.streamer.manager.PLVSStreamerManagerFactory;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * mvp-推流和连麦presenter层实现，实现 IPLVStreamerContract.IStreamerPresenter 接口
 */
public class PLVStreamerPresenter implements IPLVStreamerContract.IStreamerPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVStreamerPresenter";
    //未初始化
    private static final int STREAMER_MIC_UNINITIATED = 1;
    //初始化中
    private static final int STREAMER_MIC_INITIATING = 2;
    //已经初始化
    private static final int STREAMER_MIC_INITIATED = 3;
    //开始推流
    public static final int STREAMER_STATUS_START = 1;
    //推流成功
    public static final int STREAMER_STATUS_START_SUCCESS = 2;
    //停止推流
    public static final int STREAMER_STATUS_STOP = 3;
    //加入频道超时
    private static final int TIME_OUT_JOIN_CHANNEL = 20 * 1000;
    //每20s轮询在线列表
    private static final int INTERVAL_TO_GET_USER_LIST = 20 * 1000;
    //每10s轮询连麦列表
    private static final int INTERVAL_TO_GET_LINK_MIC_LIST = 10 * 1000;

    //推流和连麦初始化状态
    private int streamerInitState = STREAMER_MIC_UNINITIATED;

    //直播间数据管理器
    IPLVLiveRoomDataManager liveRoomDataManager;
    //推流和连麦数据
    private PLVStreamerData streamerData;
    //推流和连麦mvp模式的view
    private List<IPLVStreamerContract.IStreamerView> iStreamerViews;
    //推流和连麦的管理器
    IPLVSStreamerManager streamerManager;

    private TimerToShowNetBroken timerToShowNetBroken = new TimerToShowNetBroken(20);

    //推流参数
    @PLVSStreamerConfig.BitrateType
    private int curBitrate = loadBitrate();
    private boolean curCameraFront = true;
    private boolean curEnableRecordingAudioVolume = true;
    private boolean curEnableLocalVideo = true;

    //推流状态
    private int streamerStatus = STREAMER_STATUS_STOP;

    //推流和连麦列表
    List<PLVLinkMicItemDataBean> streamerList = new LinkedList<>();
    //成员列表
    List<PLVMemberItemDataBean> memberList = new LinkedList<>();
    //rtc回调在连麦中的列表
    Map<String, PLVLinkMicItemDataBean> rtcJoinMap = new HashMap<>();

    //推流和连麦的socket信息处理器
    private PLVStreamerMsgHandler streamerMsgHandler;

    private int memberPage = 1;
    private int memberLength = 500;
    //disposable
    private Disposable listUsersDisposable;
    private Disposable listUserTimerDisposable;
    private Disposable linkMicListTimerDisposable;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStreamerPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        streamerData = new PLVStreamerData();

        String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        PolyvLinkMicConfig.getInstance().init(viewerId, true);//需先初始化，再创建manager
        streamerManager = PLVSStreamerManagerFactory.createNewStreamerManager();

        streamerMsgHandler = new PLVStreamerMsgHandler(this);
        streamerMsgHandler.run();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVStreamerContract.IStreamerPresenter定义的方法">
    @Override
    public void registerView(@NonNull IPLVStreamerContract.IStreamerView v) {
        if (iStreamerViews == null) {
            iStreamerViews = new ArrayList<>();
        }
        if (!iStreamerViews.contains(v)) {
            iStreamerViews.add(v);
        }
        v.setPresenter(this);
    }

    @Override
    public void unregisterView(IPLVStreamerContract.IStreamerView v) {
        if (iStreamerViews != null) {
            iStreamerViews.remove(v);
        }
    }

    @Override
    public void init() {
        streamerInitState = STREAMER_MIC_INITIATING;
        streamerManager.initEngine(new PLVSStreamerListener() {
            @Override
            public void onStreamerEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "推流和连麦初始化成功");
                streamerInitState = STREAMER_MIC_INITIATED;

                PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
                linkMicItemDataBean.setMuteAudio(!curEnableRecordingAudioVolume);
                linkMicItemDataBean.setMuteVideo(!curEnableLocalVideo);
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_RTC_JOIN);
                linkMicItemDataBean.setLinkMicId(streamerManager.getLinkMicUid());
                linkMicItemDataBean.setActor(liveRoomDataManager.getConfig().getUser().getActor());
                linkMicItemDataBean.setNick(liveRoomDataManager.getConfig().getUser().getViewerName());
                linkMicItemDataBean.setUserType(liveRoomDataManager.getConfig().getUser().getViewerType());
                streamerList.add(0, linkMicItemDataBean);
                Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(linkMicItemDataBean.getLinkMicId());
                if (item != null && item.second.getLinkMicItemDataBean() == null) {
                    item.second.setLinkMicItemDataBean(linkMicItemDataBean);
                }

                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onStreamerEngineCreatedSuccess(streamerManager.getLinkMicUid(), streamerList);
                    }
                });

                setBitrate(curBitrate);
                setCameraDirection(curCameraFront);
                enableLocalVideo(curEnableLocalVideo);
                enableRecordingAudioVolume(curEnableRecordingAudioVolume);

                initStreamerListener();

                if (streamerStatus == STREAMER_STATUS_START) {
                    streamerManager.startLiveStream();
                }
            }

            @Override
            public void onStreamerError(final int errorCode, final Throwable throwable) {
                PLVCommonLog.e(TAG, "推流和连麦模块错误：errorCode=" + errorCode);
                PLVCommonLog.exception(throwable);
                if (streamerInitState != STREAMER_MIC_INITIATED) {
                    streamerInitState = STREAMER_MIC_UNINITIATED;
                }

                stopLiveStream();
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onStatesToStreamEnded();
                    }
                });

                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onStreamerError(errorCode, throwable);
                    }
                });
            }
        });
    }

    @Override
    public int getNetworkQuality() {
        return streamerManager.getCurrentNetQuality();
    }

    @Override
    public void setBitrate(int bitrate) {
        curBitrate = Math.min(bitrate, streamerManager.getMaxSupportedBitrate());
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setBitrate(bitrate);
        saveBitrate();
    }

    @Override
    public int getBitrate() {
        return Math.min(curBitrate, streamerManager.getMaxSupportedBitrate());
    }

    @Override
    public int getMaxBitrate() {
        return streamerManager.getMaxSupportedBitrate();
    }

    @Override
    public boolean enableRecordingAudioVolume(final boolean enable) {
        curEnableRecordingAudioVolume = enable;
        if (!isInitStreamerManager()) {
            return false;
        }
        streamerManager.enableLocalMicrophone(enable);
        streamerData.postEnableAudio(enable);
        callUserMuteAudio(streamerManager.getLinkMicUid(), !enable);
        return true;
    }

    @Override
    public boolean enableLocalVideo(final boolean enable) {
        curEnableLocalVideo = enable;
        if (!isInitStreamerManager()) {
            return false;
        }
        streamerManager.enableLocalCamera(enable);
        streamerData.postEnableVideo(enable);
        callUserMuteVideo(streamerManager.getLinkMicUid(), !enable);
        return true;
    }

    @Override
    public boolean setCameraDirection(final boolean front) {
        curCameraFront = front;
        if (!isInitStreamerManager()) {
            return false;
        }
        streamerManager.switchCamera(front);
        streamerData.postIsFrontCamera(front);
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(streamerManager.getLinkMicUid());
                if (item == null) {
                    return;
                }
                item.second.setFrontCamera(front);
                view.onCameraDirection(front, item.first);
            }
        });
        return true;
    }

    @Override
    public SurfaceView createRenderView(Context context) {
        return streamerManager.createRendererView(context);
    }

    @Override
    public void releaseRenderView(SurfaceView renderView) {
        streamerManager.releaseRenderView(renderView);
    }

    @Override
    public void setupRenderView(SurfaceView renderView, String linkMicId) {
        if (isMyLinkMicId(linkMicId)) {
            streamerManager.setupLocalVideo(renderView);
        } else {
            streamerManager.setupRemoteVideo(renderView, linkMicId);
        }
    }

    @Override
    public void startLiveStream() {
        streamerStatus = STREAMER_STATUS_START;
        switch (streamerInitState) {
            //未初始化
            case STREAMER_MIC_UNINITIATED:
                PLVCommonLog.d(TAG, "推流和连麦开始初始化");
                init();
                break;
            //初始化中
            case STREAMER_MIC_INITIATING:
                PLVCommonLog.d(TAG, "推流和连麦初始化中");
                return;
            //已经初始化
            case STREAMER_MIC_INITIATED:
                streamerManager.startLiveStream();
                break;
            default:
                break;
        }
    }

    @Override
    public void stopLiveStream() {
        streamerStatus = STREAMER_STATUS_STOP;
        streamerManager.stopLiveStream();
        streamerData.postStreamerStatus(false);
        PLVSLinkMicEventSender.getInstance().closeLinkMic();
    }

    @Override
    public void controlUserLinkMic(int position, boolean isAllowJoin) {
        if (position < 0 || position >= memberList.size()) {
            return;
        }
        final PLVMemberItemDataBean memberItemDataBean = memberList.get(position);
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        if (isAllowJoin) {
            if (rtcJoinMap.size() >= PLVSStreamerInnerDataTransfer.getInstance().getInteractNumLimit()) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onReachTheInteractNumLimit();
                    }
                });
                return;
            }
            PLVSLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, new IPLVSLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    if (linkMicItemDataBean != null) {
                        linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_JOINING);
                        startJoinTimeoutCount(linkMicItemDataBean);
                        callUpdateSortMemberList();
                    }
                }
            });
        } else {
            if (linkMicItemDataBean != null) {
                PLVSLinkMicEventSender.getInstance().closeUserLinkMic(linkMicItemDataBean.getLinkMicId(), null);
            }
        }
    }

    @Override
    public void muteUserMedia(int position, final boolean isVideoType, final boolean isMute) {
        if (!PLVSLinkMicEventSender.getInstance().isVideoLinkMicType() && isVideoType && !isMute) {
            return;//音频连麦模式，不操作用户打开视频
        }
        if (position < 0 || position >= memberList.size()) {
            return;
        }
        PLVMemberItemDataBean memberItemDataBean = memberList.get(position);
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        String sessionId = liveRoomDataManager.getSessionId();
        PLVSLinkMicEventSender.getInstance().muteUserMedia(socketUserBean, sessionId, isVideoType, isMute, new IPLVSLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                if (linkMicItemDataBean != null) {
                    if (isVideoType) {
                        linkMicItemDataBean.setMuteVideo(isMute);
                        callUserMuteVideo(linkMicItemDataBean.getLinkMicId(), isMute);
                    } else {
                        linkMicItemDataBean.setMuteAudio(isMute);
                        callUserMuteAudio(linkMicItemDataBean.getLinkMicId(), isMute);
                    }
                }
            }
        });
    }

    @Override
    public void closeAllUserLinkMic() {
        for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
            String linkMicId = linkMicItemDataBeanEntry.getKey();
            if (!isMyLinkMicId(linkMicId)) {
                PLVSLinkMicEventSender.getInstance().closeUserLinkMic(linkMicId, null);
            }
        }
    }

    @Override
    public void muteAllUserAudio(final boolean isMute) {
        for (int i = 0; i < memberList.size(); i++) {
            @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberList.get(i).getLinkMicItemDataBean();
            if (linkMicItemDataBean != null && linkMicItemDataBean.isRtcJoinStatus()) {
                if (!isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
                    muteUserMedia(i, false, isMute);
                }
            }
        }
    }

    @Override
    public void requestMemberList() {
        requestListUsersApi();
    }

    @Override
    public int getStreamerStatus() {
        return streamerStatus;
    }

    @NonNull
    @Override
    public PLVStreamerData getData() {
        return streamerData;
    }

    @Override
    public void destroy() {
        streamerInitState = STREAMER_MIC_UNINITIATED;
        streamerStatus = STREAMER_STATUS_STOP;

        handler.removeCallbacksAndMessages(null);
        streamerMsgHandler.destroy();

        dispose(listUsersDisposable);
        dispose(listUserTimerDisposable);
        dispose(linkMicListTimerDisposable);

        streamerList.clear();

        if (timerToShowNetBroken != null) {
            timerToShowNetBroken.destroy();
        }
        if (iStreamerViews != null) {
            iStreamerViews.clear();
        }

        //关闭连麦开关
        PLVSLinkMicEventSender.getInstance().closeLinkMic();

        streamerManager.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - 初始化监听器">
    private void initStreamerListener() {
        //观察连麦的数据
        streamerMsgHandler.observeLinkMicData();
        //添加事件处理器
        streamerManager.addEventHandler(new PLVSStreamerEventListener() {
            //推流网络状态变化
            @Override
            public void onNetworkQuality(final int quality) {
                streamerData.postNetworkQuality(quality);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onNetworkQuality(quality);
                    }
                });
                if (quality == PLVSStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                    timerToShowNetBroken.invokeTimerWhenNoConnection();
                } else {
                    timerToShowNetBroken.resetWhenHasConnection();
                }
            }
        });

        //推流开始
        streamerManager.addOnLiveStreamingStartListener(new IPLVSStreamerOnLiveStreamingStartListener() {
            @Override
            public void onLiveStreamingStart() {
                streamerStatus = STREAMER_STATUS_START_SUCCESS;
                streamerData.postStreamerStatus(true);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onStatesToStreamStarted();
                    }
                });
            }
        });

        //推流计时
        streamerManager.setOnLiveTimingListener(new IPLVSStreamerOnLiveTimingListener() {
            @Override
            public void onTimePastEachSeconds(final int duration) {
                streamerData.postStreamerTime(duration);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUpdateStreamerTime(duration);
                    }
                });
            }
        });

        //推流超时
        streamerManager.addStreamerServerTimeoutListener(new IPLVSStreamerOnServerTimeoutDueToNetBrokenListener() {
            @Override
            public void onServerTimeoutDueToNetBroken() {
                //将状态改变为：已经下课
                if (streamerStatus != STREAMER_STATUS_STOP) {
                    stopLiveStream();
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onStatesToStreamEnded();
                        }
                    });
                }
            }
        });

        //推流sessionId
        streamerManager.addGetSessionIdFromServerListener(new IPLVSOnGetSessionIdInnerListener() {
            @Override
            public void onGetSessionId(String sessionId, String channelId, String streamId, boolean isCamClosed) {
                liveRoomDataManager.setSessionId(sessionId);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="成员列表 - 数据处理">
    private void requestListUsersApi() {
        dispose(listUsersDisposable);
        dispose(listUserTimerDisposable);
        dispose(linkMicListTimerDisposable);
        String loginRoomId = PolyvSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = liveRoomDataManager.getConfig().getChannelId();//socket未登陆时，使用频道号
        }
        final String roomId = loginRoomId;
        listUsersDisposable = PolyvChatApiRequestHelper.getListUsers(roomId, memberPage, memberLength)
                .retryWhen(new PLVRxBaseRetryFunction(Integer.MAX_VALUE, 3000))
                .subscribe(new Consumer<PLVSListUsersVO>() {
                    @Override
                    public void accept(PLVSListUsersVO plvsListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PolyvChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
                        generateMemberListWithListUsers(plvsListUsersVO.getUserlist(), true);
                        //请求连麦列表api
                        requestLinkMicListApiTimer();
                        //定时请求在线列表api
                        requestListUsersApiTimer(roomId);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }

    private void requestListUsersApiTimer(final String roomId) {
        dispose(listUserTimerDisposable);
        listUserTimerDisposable = Observable.interval(INTERVAL_TO_GET_USER_LIST, INTERVAL_TO_GET_USER_LIST, TimeUnit.MILLISECONDS, Schedulers.io())
                .flatMap(new Function<Long, Observable<PLVSListUsersVO>>() {
                    @Override
                    public Observable<PLVSListUsersVO> apply(Long aLong) throws Exception {
                        return PolyvChatApiRequestHelper.getListUsers(roomId, memberPage, memberLength).retry(1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVSListUsersVO>() {
                    @Override
                    public void accept(PLVSListUsersVO plvsListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PolyvChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
                        generateMemberListWithListUsers(plvsListUsersVO.getUserlist(), false);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                        //发送错误日志，便于排查问题
                        PLVELogSender.send(PolyvChatroomELog.class, PolyvChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }

    private void generateMemberListWithListUsers(List<PLVSocketUserBean> socketUserBeanList, boolean isResetJoiningStatus) {
        List<PLVMemberItemDataBean> tempMemberList = new LinkedList<>();
        for (int i = 0; i < socketUserBeanList.size(); i++) {
            PLVSocketUserBean socketUserBean = socketUserBeanList.get(i);
            String userId = socketUserBean.getUserId();
            if (userId != null && userId.equals(PolyvSocketWrapper.getInstance().getLoginVO().getUserId())) {
                socketUserBeanList.remove(socketUserBean);
                i--;
                continue;//排除在线列表中的讲师，使用本地的数据添加
            }
            PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
            memberItemDataBean.setSocketUserBean(socketUserBean);
            //给新成员列表数据添加旧成员列表中保存的一些状态信息
            Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(userId);
            if (item != null) {
                PLVLinkMicItemDataBean linkMicItemDataBean = item.second.getLinkMicItemDataBean();
                if (linkMicItemDataBean != null) {
                    if (linkMicItemDataBean.isJoiningStatus() && isResetJoiningStatus) {
                        //重连成功后，重置之前成员列表成员连麦中的状态
                        linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
                    }
                    memberItemDataBean.setLinkMicItemDataBean(linkMicItemDataBean);
                }
            }
            tempMemberList.add(memberItemDataBean);
        }
        memberList = tempMemberList;
        //添加讲师的信息
        Runnable addTeacherTask = new Runnable() {
            @Override
            public void run() {
                PLVMemberItemDataBean memberItemDataBean = new PLVMemberItemDataBean();
                memberItemDataBean.setFrontCamera(curCameraFront);
                Pair<Integer, PLVLinkMicItemDataBean> item = getLinkMicItemWithLinkMicId(streamerManager.getLinkMicUid());
                if (item != null) {
                    memberItemDataBean.setLinkMicItemDataBean(item.second);
                }
                memberItemDataBean.setSocketUserBean(PolyvSocketWrapper.getInstance().getLoginVO().createSocketUserBean());
                memberList.add(0, memberItemDataBean);
            }
        };
        addTeacherTask.run();

        callUpdateSortMemberList();
    }

    private void requestLinkMicListApiTimer() {
        dispose(linkMicListTimerDisposable);
        linkMicListTimerDisposable = PLVRxTimer.timer(1000, INTERVAL_TO_GET_LINK_MIC_LIST, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                streamerManager.getLinkStatus(liveRoomDataManager.getSessionId(), new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
                    @Override
                    public void onSuccess(PLVLinkMicJoinStatus data) {
                        PLVCommonLog.d(TAG, "PLVStreamerPresenter.requestLinkMicListFromServer.onSuccess->\n" + data.toString());
                        acceptLinkMicJoinStatus(data);
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

    private void acceptLinkMicJoinStatus(PLVLinkMicJoinStatus data) {
        List<PLVJoinInfoEvent> joinList = data.getJoinList();
        List<PLVLinkMicJoinStatus.WaitListBean> waitList = data.getWaitList();

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

        boolean hasChangedMemberList;
        //更新成员列表中的连麦状态相关数据
        hasChangedMemberList = updateMemberListLinkMicStatus(joinList, waitList);
        //遍历加入状态的连麦列表
        for (PLVJoinInfoEvent joinInfoEvent : joinList) {
            PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(joinInfoEvent);
            PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(joinInfoEvent);
            if (isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
                continue;//过滤自己
            }
            //补充或更新成员列表中的数据信息
            boolean result = updateMemberListItemInfo(socketUserBean, linkMicItemDataBean, true);
            if (result) {
                hasChangedMemberList = true;
            }
        }
        //移除本地推流和连麦列表不在服务器列表中的用户数据
        removeLinkMicDataNoExistServer(joinList);
        //遍历等待状态的连麦列表
        for (PLVLinkMicJoinStatus.WaitListBean waitListBean : waitList) {
            PLVLinkMicItemDataBean linkMicItemDataBean = PLVLinkMicDataMapper.map2LinkMicItemData(waitListBean);
            PLVSocketUserBean socketUserBean = PLVLinkMicDataMapper.map2SocketUserBean(waitListBean);
            if (isMyLinkMicId(socketUserBean.getUserId())) {
                continue;//过滤自己
            }
            //补充或更新成员列表中的数据信息
            boolean result = updateMemberListItemInfo(socketUserBean, linkMicItemDataBean, false);
            if (result) {
                hasChangedMemberList = true;
            }
        }
        //更新成员列表数据
        if (hasChangedMemberList) {
            callUpdateSortMemberList();
        }
    }

    boolean updateMemberListItemInfo(PLVSocketUserBean socketUserBean, PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList) {
        return updateMemberListItemInfo(socketUserBean, linkMicItemDataBean, isJoinList, false);
    }

    boolean updateMemberListItemInfo(PLVSocketUserBean socketUserBean, PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoinList, boolean isUpdateJoiningStatus) {
        boolean hasChangedMemberList = false;
        //获取数据是否在成员列表中
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithUserId(socketUserBean.getUserId());
        if (memberItem == null || memberItem.second.getLinkMicItemDataBean() == null) {
            //如果不在成员列表中或列表中成员的连麦信息为空，则添加或补充信息
            PLVMemberItemDataBean memberItemDataBean;
            if (memberItem == null) {
                memberItemDataBean = new PLVMemberItemDataBean();
                memberItemDataBean.setSocketUserBean(socketUserBean);
                memberList.add(memberItemDataBean);
            } else {
                memberItemDataBean = memberItem.second;
            }
            memberItemDataBean.setLinkMicItemDataBean(linkMicItemDataBean);
            updateMemberListLinkMicStatusWithRtcJoinList(memberItemDataBean, linkMicItemDataBean.getLinkMicId());
            hasChangedMemberList = true;
        } else {
            PLVLinkMicItemDataBean linkMicItemDataBeanInMemberList = memberItem.second.getLinkMicItemDataBean();
            boolean isJoiningStatus = linkMicItemDataBeanInMemberList.isJoiningStatus();
            boolean isJoinStatus = linkMicItemDataBeanInMemberList.isJoinStatus();
            boolean isWaitStatus = linkMicItemDataBeanInMemberList.isWaitStatus();
            if (isJoinList) {
                hasChangedMemberList = updateMemberListLinkMicStatusWithRtcJoinList(memberItem.second, linkMicItemDataBeanInMemberList.getLinkMicId());
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

    boolean updateMemberListLinkMicStatusWithRtcJoinList(PLVMemberItemDataBean item, final String linkMicUid) {
        boolean hasChangedMemberList = false;
        PLVLinkMicItemDataBean linkMicItemDataBean = item.getLinkMicItemDataBean();
        if (linkMicItemDataBean == null) {
            return false;
        }
        for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
            String uid = linkMicItemDataBeanEntry.getKey();
            if (linkMicUid != null && linkMicUid.equals(uid)) {
                if (!linkMicItemDataBean.isRtcJoinStatus()) {
                    linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_RTC_JOIN);
                    updateLinkMicMediaStatus(linkMicItemDataBeanEntry.getValue(), linkMicItemDataBean);
                    hasChangedMemberList = true;
                }
                Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
                if (linkMicItem == null) {
                    streamerList.add(linkMicItemDataBean);
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            //更新推流和连麦列表
                            view.onUsersJoin(Collections.singletonList(linkMicUid));
                        }
                    });
                }
                break;
            }
        }
        return hasChangedMemberList;
    }

    private boolean updateMemberListLinkMicStatus(List<PLVJoinInfoEvent> joinList, List<PLVLinkMicJoinStatus.WaitListBean> waitList) {
        boolean hasChanged = false;
        for (PLVMemberItemDataBean plvMemberItemDataBean : memberList) {
            PLVLinkMicItemDataBean linkMicItemDataBean = plvMemberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean == null
                    || linkMicItemDataBean.isIdleStatus()
                    || isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
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
            if (!isExitLinkMicList) {
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
                rtcJoinMap.remove(linkMicItemDataBean.getLinkMicId());
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    private void removeLinkMicDataNoExistServer(List<PLVJoinInfoEvent> joinList) {
        final List<String> willRemoveStreamerList = new ArrayList<>();
        Iterator<PLVLinkMicItemDataBean> linkMicItemDataBeanIterator = streamerList.iterator();
        while (linkMicItemDataBeanIterator.hasNext()) {
            PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItemDataBeanIterator.next();
            String linkMicId = linkMicItemDataBean.getLinkMicId();
            boolean isExistServerList = false;
            for (PLVJoinInfoEvent joinInfoEvent : joinList) {
                if (linkMicId != null && linkMicId.equals(joinInfoEvent.getUserId())) {
                    isExistServerList = true;
                    break;
                }
            }
            if (!isExistServerList && !isMyLinkMicId(linkMicId)) {
                linkMicItemDataBeanIterator.remove();
                willRemoveStreamerList.add(linkMicId);
            }
        }
        if (!willRemoveStreamerList.isEmpty()) {
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onUsersLeave(willRemoveStreamerList);
                }
            });
        }
    }

    void updateLinkMicMediaStatus(PLVLinkMicItemDataBean rtcJoinLinkMicItem, PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (rtcJoinLinkMicItem == null || linkMicItemDataBean == null) {
            return;
        }
        if (rtcJoinLinkMicItem.getMuteVideoInRtcJoinList() != null) {
            //如果之前有保存过连麦用户媒体的状态，则使用
            linkMicItemDataBean.setMuteVideo(rtcJoinLinkMicItem.isMuteVideo());
        } else {
            if (!linkMicItemDataBean.isGuest()) {//嘉宾可以在音频模式下使用摄像头
                //根据音视频连麦类型，设置连麦成员的muteVideo状态
                linkMicItemDataBean.setMuteVideo(!PLVSLinkMicEventSender.getInstance().isVideoLinkMicType());
            } else {
                linkMicItemDataBean.setMuteVideo(false);
            }
        }
        if (rtcJoinLinkMicItem.getMuteAudioInRtcJoinList() != null) {
            //如果之前有保存过连麦用户媒体的状态，则使用
            linkMicItemDataBean.setMuteAudio(rtcJoinLinkMicItem.isMuteAudio());
        } else {
            //连麦的用户muteAudio默认为false
            linkMicItemDataBean.setMuteAudio(false);
        }
    }

    private void startJoinTimeoutCount(final PLVLinkMicItemDataBean linkMicItemDataBean) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                linkMicItemDataBean.setStatusMethodCallListener(null);
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_WAIT);
                callUpdateSortMemberList();
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

    // <editor-fold defaultstate="collapsed" desc="数据存储">
    private void saveBitrate() {
        SPUtils.getInstance().put("plv_key_bitrate", curBitrate);
    }

    private int loadBitrate() {
        return SPUtils.getInstance().getInt("plv_key_bitrate", PLVSStreamerConfig.Bitrate.BITRATE_HIGH);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private boolean isInitStreamerManager() {
        return streamerInitState == STREAMER_MIC_INITIATED;
    }

    private boolean isMyLinkMicId(String linkMicId) {
        return linkMicId != null && linkMicId.equals(streamerManager.getLinkMicUid());
    }

    Pair<Integer, PLVLinkMicItemDataBean> getLinkMicItemWithLinkMicId(String linkMicId) {
        for (int i = 0; i < streamerList.size(); i++) {
            PLVLinkMicItemDataBean linkMicItemDataBean = streamerList.get(i);
            String linkMicIdForIndex = linkMicItemDataBean.getLinkMicId();
            if (linkMicId != null && linkMicId.equals(linkMicIdForIndex)) {
                return new Pair<>(i, linkMicItemDataBean);
            }
        }
        return null;
    }

    Pair<Integer, PLVMemberItemDataBean> getMemberItemWithLinkMicId(String linkMicId) {
        for (int i = 0; i < memberList.size(); i++) {
            PLVMemberItemDataBean memberItemDataBean = memberList.get(i);
            PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
            if (linkMicItemDataBean != null) {
                String linkMicIdForIndex = linkMicItemDataBean.getLinkMicId();
                if (linkMicId != null && linkMicId.equals(linkMicIdForIndex)) {
                    return new Pair<>(i, memberItemDataBean);
                }
            }
        }
        return null;
    }

    Pair<Integer, PLVMemberItemDataBean> getMemberItemWithUserId(String userId) {
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

    void callUpdateSortMemberList() {
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onUpdateMemberListData(SortMemberListUtils.sort(memberList));
            }
        });
    }

    void callUserMuteAudio(final String linkMicId, final boolean isMute) {
        Pair<Integer, PLVLinkMicItemDataBean> item = getLinkMicItemWithLinkMicId(linkMicId);
        if (item == null) {
            return;
        }
        item.second.setMuteAudio(isMute);
        final int streamerListPos = item.first;
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicId);
        if (memberItem == null) {
            return;
        }
        final int memberListPos = memberItem.first;
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onUserMuteAudio(linkMicId, isMute, streamerListPos, memberListPos);
            }
        });
    }

    void callUserMuteVideo(final String linkMicId, final boolean isMute) {
        Pair<Integer, PLVLinkMicItemDataBean> item = getLinkMicItemWithLinkMicId(linkMicId);
        if (item == null) {
            return;
        }
        item.second.setMuteVideo(isMute);
        final int streamerListPos = item.first;
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicId);
        if (memberItem == null) {
            return;
        }
        final int memberListPos = memberItem.first;
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onUserMuteVideo(linkMicId, isMute, streamerListPos, memberListPos);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view回调">
    void callbackToView(ViewRunnable runnable) {
        if (iStreamerViews != null) {
            for (IPLVStreamerContract.IStreamerView view : iStreamerViews) {
                if (view != null && runnable != null) {
                    runnable.run(view);
                }
            }
        }
    }

    interface ViewRunnable {
        void run(@NonNull IPLVStreamerContract.IStreamerView view);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 因断网延迟20s断流">
    public class TimerToShowNetBroken {
        // <editor-fold defaultstate="collapsed" desc="变量">
        //定时器
        private Disposable timerDisposable;

        //过多少秒显示提示
        private int secondsToShow;

        //在一次断网中已经显示过一次了，后续再收到断网事件，都不再显示弹窗了，除非又连上过网络。
        private boolean hasShownDuringOneNetBroken = false;
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="构造器">
        TimerToShowNetBroken(int secondsToShow) {
            this.secondsToShow = secondsToShow;
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="触发">
        //当没有网络的时候尝试触发定时器
        void invokeTimerWhenNoConnection() {
            //如果没有上课，不触发
            if (streamerStatus == STREAMER_STATUS_STOP) {
                return;
            }
            //如果正在倒计时，不触发
            if (isOngoing()) {
                return;
            }
            if (hasShownDuringOneNetBroken) {
                return;
            }
            dispose(timerDisposable);
            timerDisposable = PLVRxTimer.timer(1000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    if (aLong >= secondsToShow) {
                        //到达指定的时间
                        dispose(timerDisposable);
                        hasShownDuringOneNetBroken = true;

                        //call
                        streamerData.postShowNetBroken();
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                view.onShowNetBroken();
                            }
                        });
                        //如果还没有推流成功，说明在推流前已经断网了，这时直接变成下课
                        if (!streamerManager.isLiveStreaming()) {
                            stopLiveStream();
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                    view.onStatesToStreamEnded();
                                }
                            });
                        }
                    } else {
                        //还未到时间，检查是否连上网络了,如果在指定时间内网络恢复，则取消定时器。
                        int netQuality = streamerManager.getCurrentNetQuality();
                        if (netQuality != PLVSStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                            dispose(timerDisposable);
                        }
                    }
                }
            });
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="重置">
        //当有网络的时候，重置状态和flag
        void resetWhenHasConnection() {
            hasShownDuringOneNetBroken = false;
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="销毁">
        void destroy() {
            hasShownDuringOneNetBroken = false;
            dispose(timerDisposable);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="状态获取与变更">
        private void dispose(Disposable disposable) {
            if (disposable != null) {
                disposable.dispose();
            }
        }

        private boolean isOngoing() {
            if (timerDisposable != null) {
                return !timerDisposable.isDisposed();
            }
            return false;
        }
        // </editor-fold>
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 排序成员列表">
    public static class SortMemberListUtils {
        //按自己>管理员>讲师>助教>非虚拟用户>虚拟用户类型进行排序
        private static final String SELF = "自己";
        private static final String REAL = "非虚拟";
        private static final String REAL_LINK_MIC_RTC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_RTC_JOIN;
        private static final String REAL_LINK_MIC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_JOIN;
        private static final String REAL_LINK_MIC_JOINING = REAL + PLVLinkMicItemDataBean.STATUS_JOINING;
        private static final String REAL_LINK_MIC_WAIT = REAL + PLVLinkMicItemDataBean.STATUS_WAIT;
        private static final List<String> SORT_INDEX = Arrays.asList(
                SELF,
                PLVSocketUserConstant.USERTYPE_MANAGER,
                PLVSocketUserConstant.USERTYPE_TEACHER,
                PLVSocketUserConstant.USERTYPE_GUEST,
                PLVSocketUserConstant.USERTYPE_VIEWER,
                PLVSocketUserConstant.USERTYPE_ASSISTANT,
                REAL_LINK_MIC_RTC_JOIN,
                REAL_LINK_MIC_JOIN,
                REAL_LINK_MIC_JOINING,
                REAL_LINK_MIC_WAIT,
                REAL,
                PLVSocketUserConstant.USERTYPE_DUMMY
        );

        private static String getSortType(PLVMemberItemDataBean item) {
            PLVSocketUserBean data = item.getSocketUserBean();
            String type = data.getUserType();
            String myUserId = PolyvSocketWrapper.getInstance().getLoginVO().getUserId();
            if (myUserId.equals(data.getUserId())) {
                type = SELF;
                return type;
            }
            if (!PLVSocketUserConstant.USERTYPE_MANAGER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_TEACHER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_GUEST.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_VIEWER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_DUMMY.equals(type)) {
                @Nullable
                PLVLinkMicItemDataBean linkMicItemDataBean = item.getLinkMicItemDataBean();
                if (linkMicItemDataBean != null) {
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
                }
                type = REAL;
            }
            return type;
        }

        public static List<PLVMemberItemDataBean> sort(List<PLVMemberItemDataBean> memberList) {
            Collections.sort(memberList, new Comparator<PLVMemberItemDataBean>() {
                @Override
                public int compare(PLVMemberItemDataBean o1, PLVMemberItemDataBean o2) {
                    int io1 = SORT_INDEX.indexOf(getSortType(o1));
                    int io2 = SORT_INDEX.indexOf(getSortType(o2));
                    return io1 - io2;
                }
            });
            return memberList;
        }
    }
    // </editor-fold>
}
