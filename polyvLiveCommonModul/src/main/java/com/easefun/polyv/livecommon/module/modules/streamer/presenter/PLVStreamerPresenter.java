package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.catchingNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.SurfaceView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.data.PLVStreamerData;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSStreamerOnLiveStatusChangeListener;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.linkmic.screenshare.IPLVScreenShareListener;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.vo.PLVLinkMicEngineParam;
import com.plv.livescenes.log.chat.PLVChatroomELog;
import com.plv.livescenes.model.PLVListUsersVO;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livescenes.streamer.IPLVStreamerManager;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.livescenes.streamer.linkmic.IPLVLinkMicEventSender;
import com.plv.livescenes.streamer.linkmic.PLVLinkMicEventSender;
import com.plv.livescenes.streamer.listener.IPLVOnGetSessionIdInnerListener;
import com.plv.livescenes.streamer.listener.IPLVStreamerOnLiveStreamingStartListener;
import com.plv.livescenes.streamer.listener.IPLVStreamerOnLiveTimingListener;
import com.plv.livescenes.streamer.listener.IPLVStreamerOnServerTimeoutDueToNetBrokenListener;
import com.plv.livescenes.streamer.listener.PLVStreamerEventListener;
import com.plv.livescenes.streamer.listener.PLVStreamerListener;
import com.plv.livescenes.streamer.manager.PLVStreamerManagerFactory;
import com.plv.livescenes.streamer.mix.PLVRTCMixUser;
import com.plv.livescenes.streamer.transfer.PLVStreamerInnerDataTransfer;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import org.jetbrains.annotations.NotNull;

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
import io.socket.client.Ack;

/**
 * mvp-推流和连麦presenter层实现，实现 IPLVStreamerContract.IStreamerPresenter 接口
 */
public class PLVStreamerPresenter implements IPLVStreamerContract.IStreamerPresenter {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVStreamerPresenter";

    /**** 推流引擎推流状态 ****/
    //开始推流
    public static final int STREAMER_STATUS_START = 1;
    //推流成功
    public static final int STREAMER_STATUS_START_SUCCESS = 2;
    //停止推流
    public static final int STREAMER_STATUS_STOP = 3;

    /**** 推流引擎初始化状态 ****/
    //未初始化
    private static final int STREAMER_MIC_UNINITIATED = 1;
    //初始化中
    private static final int STREAMER_MIC_INITIATING = 2;
    //已经初始化
    private static final int STREAMER_MIC_INITIATED = 3;

    /**** 时间 ****/
    //加入频道超时
    private static final int TIME_OUT_JOIN_CHANNEL = 20 * 1000;
    //断网提示时间
    private static final int TIME_OUT_TO_SHOW_NET_BROKEN = 20;
    //每20s轮询在线列表
    private static final int INTERVAL_TO_GET_USER_LIST = 20 * 1000;
    //每10s轮询连麦列表
    private static final int INTERVAL_TO_GET_LINK_MIC_LIST = 10 * 1000;
    //每10s轮询直播状态
    private static final int INTERVAL_TO_POLL_LIVE_STATUS = 10 * 1000;

    /**** 错误码 ****/
    private static final int ERROR_GUEST_LINK_TIMEOUT = 1;

    /**** 成员列表默认请求数据大小 ****/
    private static final int DEFAULT_MEMBER_PAGE = 1;
    private static final int DEFAULT_MEMBER_LENGTH = 500;

    /**** View ****/
    //推流和连麦mvp模式的view
    private List<IPLVStreamerContract.IStreamerView> iStreamerViews;
    /**** Model ****/
    //推流和连麦的管理器
    private final IPLVStreamerManager streamerManager;
    //推流和连麦的socket信息处理器
    private final PLVStreamerMsgHandler streamerMsgHandler;
    private final PLVBeautyRepo beautyRepo = PLVDependManager.getInstance().get(PLVBeautyRepo.class);

    /**** 状态 ****/
    //推流引擎初始化状态
    private int streamerInitState = STREAMER_MIC_UNINITIATED;
    //推流状态
    private int streamerStatus = STREAMER_STATUS_STOP;

    /**** 本地数据 ****/
    //直播间数据管理器
    private final IPLVLiveRoomDataManager liveRoomDataManager;
    //当前用户的成员数据
    @Nullable
    private PLVSocketUserBean currentSocketUserBean;
    //用户类型，现在这里可能的用户类型是：讲师、嘉宾
    private final String userType;
    //推流和连麦数据
    private final PLVStreamerData streamerData;
    //当前拥有主讲权限的用户
    private PLVSocketUserBean currentSpeakerPermissionUser;
    @Nullable
    private String lastFirstScreenUserId;

    /**** 推流参数 ****/
    @PLVStreamerConfig.BitrateType
    private int curBitrate = loadBitrate();
    private boolean curCameraFront = true;
    private boolean curEnableRecordingAudioVolume = true;
    private boolean curEnableLocalVideo = true;
    private boolean isFrontMirror = true;
    private int pushPictureResolution = PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE;
    private PLVLinkMicConstant.PushResolutionRatio pushResolutionRatio = PLVLinkMicConstant.PushResolutionRatio.RATIO_16_9;
    //开始推流是否需要恢复上一场的直播流，继续推流
    private boolean isRecoverStream = false;
    private float localCameraZoomFactor = 1F;
    @Nullable
    private Boolean isNetworkConnect = null;

    /**** 容器 ****/
    //推流和连麦列表
    final List<PLVLinkMicItemDataBean> streamerList = new LinkedList<>();
    //成员列表
    List<PLVMemberItemDataBean> memberList = new LinkedList<>();
    //rtc回调在连麦中的列表
    final Map<String, PLVLinkMicItemDataBean> rtcJoinMap = new HashMap<>();

    /**** 任务 ****/
    //断网提示倒计时
    private final TimerToShowNetBroken timerToShowNetBroken = new TimerToShowNetBroken(TIME_OUT_TO_SHOW_NET_BROKEN);
    //加入频道任务
    private Runnable joinChannelRunnable = null;
    //disposable
    private Disposable listUsersDisposable;
    private Disposable listUserTimerDisposable;
    private Disposable linkMicListTimerDisposable;
    private Observer<Boolean> beautySwitchStateObserver;

    //handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStreamerPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        streamerData = new PLVStreamerData();

        String viewerId = liveRoomDataManager.getConfig().getUser().getViewerId();
        userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        PLVLinkMicConfig.getInstance().init(viewerId, true);//需先初始化，再创建manager
        streamerManager = PLVStreamerManagerFactory.createNewStreamerManager();
        if (liveRoomDataManager.isOnlyAudio()) {
            //音频开播模式下，不请求相机权限
            ArrayList permissions = new ArrayList<String>();
            permissions.add(Manifest.permission.RECORD_AUDIO);
            streamerManager.resetRequestPermissionList(permissions);
        }

        streamerMsgHandler = new PLVStreamerMsgHandler(this);
        streamerMsgHandler.run();

        observeBeautySwitchState();
    }

    private void observeBeautySwitchState() {
        if (beautySwitchStateObserver != null) {
            beautyRepo.getBeautySwitchLiveData().removeObserver(beautySwitchStateObserver);
        }
        beautyRepo.getBeautySwitchLiveData().observeForever(beautySwitchStateObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean switchOnBoolean) {
                if (switchOnBoolean == null) {
                    return;
                }
                streamerManager.switchBeauty(switchOnBoolean);
            }
        });
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
        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            //如果当前用户是嘉宾，那么要轮询知道当前的直播状态
            pollLiveStatus();
            //嘉宾要等直播中的时候才加入频道，创建引擎后不要自动加入频道
            streamerManager.disableAutoJoinChannel();
        }

        final PLVLinkMicEngineParam param = new PLVLinkMicEngineParam()
                .setChannelId(liveRoomDataManager.getConfig().getChannelId())
                .setViewerId(liveRoomDataManager.getConfig().getUser().getViewerId())
                .setViewerType(liveRoomDataManager.getConfig().getUser().getViewerType())
                .setNickName(liveRoomDataManager.getConfig().getUser().getViewerName());
        streamerManager.initEngine(param, new PLVStreamerListener() {
            @Override
            public void onStreamerEngineCreatedSuccess() {
                PLVCommonLog.d(TAG, "推流和连麦初始化成功");
                streamerInitState = STREAMER_MIC_INITIATED;

                streamerManager.setOnlyAudio(liveRoomDataManager.isOnlyAudio());

                PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
                linkMicItemDataBean.setMuteAudio(!curEnableRecordingAudioVolume);
                linkMicItemDataBean.setMuteVideo(!curEnableLocalVideo);
                if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                    //嘉宾刚创建好引擎的时候是还没有加入频道的
                    linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
                } else {
                    linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_RTC_JOIN);
                }
                linkMicItemDataBean.setLinkMicId(streamerManager.getLinkMicUid());
                linkMicItemDataBean.setActor(liveRoomDataManager.getConfig().getUser().getActor());
                linkMicItemDataBean.setNick(liveRoomDataManager.getConfig().getUser().getViewerName());
                linkMicItemDataBean.setUserId(liveRoomDataManager.getConfig().getUser().getViewerId());
                linkMicItemDataBean.setUserType(liveRoomDataManager.getConfig().getUser().getViewerType());
                // 讲师进来时，默认给自己第一画面
                if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
                    linkMicItemDataBean.setFirstScreen(true);
                    lastFirstScreenUserId = streamerManager.getLinkMicUid();
                }
                streamerList.add(0, linkMicItemDataBean);
                Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(linkMicItemDataBean.getLinkMicId());
                if (item != null && item.second.getLinkMicItemDataBean() == null) {
                    item.second.setLinkMicItemDataBean(linkMicItemDataBean);
                }
                updateLinkMicCount();

                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onStreamerEngineCreatedSuccess(streamerManager.getLinkMicUid(), streamerList);
                    }
                });

                setBitrate(curBitrate);
                setCameraDirection(curCameraFront);
                setPushPictureResolutionType(pushPictureResolution);
                setPushResolutionRatio(pushResolutionRatio);
                enableLocalVideo(curEnableLocalVideo);
                enableRecordingAudioVolume(curEnableRecordingAudioVolume);
                setFrontCameraMirror(isFrontMirror);

                initStreamerListener();

                if (streamerStatus == STREAMER_STATUS_START) {
                    streamerManager.startLiveStream();
                }
                if (joinChannelRunnable != null) {
                    joinChannelRunnable.run();
                }
            }

            @Override
            public void onStreamerError(final int errorCode, final Throwable throwable) {
                PLVCommonLog.e(TAG, "推流和连麦模块错误：errorCode=" + errorCode);
                PLVCommonLog.exception(throwable);
                if (streamerInitState != STREAMER_MIC_INITIATED) {
                    streamerInitState = STREAMER_MIC_UNINITIATED;
                }

                if (streamerStatus != STREAMER_STATUS_STOP) {
                    stopLiveStream();
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onStreamerError(errorCode, throwable);
                        }
                    });
                }
            }
        });

        //屏幕共享监听
        streamerManager.setShareScreenListener(new IPLVScreenShareListener() {
            @Override
            public void onScreenShare(final boolean isShare) {
                //发送消息通知
                if (currentSocketUserBean != null) {
                    PLVLinkMicEventSender.getInstance().sendScreenShareEvent(currentSocketUserBean, liveRoomDataManager.getSessionId(), isShare, null);
                }

                streamerData.postEnableShareScreen(isShare);
                Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(streamerManager.getLinkMicUid());
                Pair<Integer, PLVLinkMicItemDataBean> streamerItem = getLinkMicItemWithLinkMicId(streamerManager.getLinkMicUid());
                if (memberItem != null) {
                    memberItem.second.getLinkMicItemDataBean().setScreenShare(isShare);
                }
                if (streamerItem != null) {
                    streamerItem.second.setScreenShare(isShare);
                }
                final int pos = streamerItem == null ? 0 : streamerItem.first;

                callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onScreenShareChange(pos, isShare, IPLVScreenShareListener.PLV_SCREEN_SHARE_OK);
                    }
                });
                updateMixLayoutWhenScreenShare(isShare, streamerItem.second.getLinkMicId());
            }

            @Override
            public void onScreenShareError(final int errorCode) {
                Pair<Integer, PLVLinkMicItemDataBean> streamerItem = getLinkMicItemWithLinkMicId(streamerManager.getLinkMicUid());
                final int pos = streamerItem.first;
                streamerItem.second.setScreenShare(false);
                callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onScreenShareChange(pos, false, errorCode);
                    }
                });
                updateMixLayoutWhenScreenShare(false, streamerItem.second.getLinkMicId());
                streamerData.postEnableShareScreen(false);

            }
        });

    }

    @Override
    public int getNetworkQuality() {
        return streamerManager.getCurrentNetQuality();
    }

    @Override
    public void setBitrate(int bitrate) {
        curBitrate = Math.min(bitrate, getMaxBitrate());
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setBitrate(curBitrate);
        streamerData.postCurBitrate(curBitrate);
        saveBitrate();
    }

    @Override
    public int getBitrate() {
        return Math.min(curBitrate, getMaxBitrate());
    }

    @Override
    public int getMaxBitrate() {
        return PLVStreamerInnerDataTransfer.getInstance().getSupportedMaxBitrate();
    }

    @Override
    public boolean isRecoverStream() {
        return isRecoverStream;
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
    public boolean enableTorch(boolean enable) {
        if (!isInitStreamerManager()) {
            return false;
        }
        return streamerManager.enableTorch(enable);
    }

    @Override
    public boolean setCameraDirection(final boolean front) {
        curCameraFront = front;
        if (!isInitStreamerManager()) {
            return false;
        }
        if (curCameraFront) {
            streamerManager.setLocalPreviewMirror(isFrontMirror);
            streamerManager.setLocalPushMirror(isFrontMirror);
        } else {
            streamerManager.setLocalPreviewMirror(false);
            streamerManager.setLocalPushMirror(false);
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
    public void setFrontCameraMirror(final boolean enable) {
        if (!isInitStreamerManager()) {
            return;
        }
        if (curCameraFront) {
            this.isFrontMirror = enable;
            streamerData.postIsFrontMirrorMode(isFrontMirror);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    streamerManager.setLocalPreviewMirror(enable);
                    streamerManager.setLocalPushMirror(enable);
                }
            });
        }
    }

    @Override
    public void zoomLocalCamera(float scaleFactor) {
        if (!isInitStreamerManager()) {
            return;
        }
        localCameraZoomFactor += (scaleFactor - 1F) * 3.5F;
        localCameraZoomFactor = Math.max(1, Math.min(10, localCameraZoomFactor));
        streamerManager.setCameraZoomRatio(localCameraZoomFactor);
    }

    @Override
    public void setPushPictureResolutionType(int type) {
        pushPictureResolution = type;
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setPushPictureResolutionType(pushPictureResolution);
    }

    @Override
    public void setPushResolutionRatio(PLVLinkMicConstant.PushResolutionRatio resolutionRatio) {
        pushResolutionRatio = resolutionRatio;
        if (!isInitStreamerManager()) {
            return;
        }
        streamerData.postPushResolutionRatio(pushResolutionRatio);
        streamerManager.setPushResolutionRatio(pushResolutionRatio);
    }

    @Override
    public void setMixLayoutType(int mixLayoutType) {
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setMixLayoutType(mixLayoutType);
    }

    @Override
    public void setRecoverStream(boolean recoverStream) {
        isRecoverStream = recoverStream;
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
            if (liveRoomDataManager.isOnlyAudio()) {
                streamerManager.setupLocalVideo(renderView, PLVStreamerConfig.RenderMode.RENDER_MODE_NONE);
                return;
            }
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
                streamerManager.startLiveStream(isRecoverStream);
                break;
            default:
                break;
        }
    }

    @Override
    public void stopLiveStream() {
        isRecoverStream = false;
        streamerStatus = STREAMER_STATUS_STOP;
        streamerManager.stopLiveStream();
        streamerData.postStreamerStatus(false);
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onStatesToStreamEnded();
            }
        });
        //停止推流后需要回收权限
        if (currentSpeakerPermissionUser != null && currentSocketUserBean != null &&
                !currentSpeakerPermissionUser.getUserId().equals(currentSocketUserBean.getUserId())) {
            setUserPermissionSpeaker(currentSpeakerPermissionUser.getUserId(), false, null);

        }
        //关闭连麦
        PLVLinkMicEventSender.getInstance().closeLinkMic();
        PLVLinkMicEventSender.getInstance().emitFinishClassEvent(streamerManager.getLinkMicUid());
    }


    @Override
    public void exitShareScreen() {
        if (streamerManager.isScreenSharing()) {
            streamerManager.exitScreenCapture();
        }
    }

    @Override
    public void requestShareScreen(Activity activity) {
        if (!streamerManager.isScreenSharing()) {
            streamerManager.requestScreenCapture(activity);
        }
    }

    @Override
    public boolean isScreenSharing() {
        return streamerManager.isScreenSharing();
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
            if (rtcJoinMap.size() >= PLVStreamerInnerDataTransfer.getInstance().getInteractNumLimit()) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onReachTheInteractNumLimit();
                    }
                });
                return;
            }
            PLVLinkMicEventSender.getInstance().responseUserLinkMic(socketUserBean, new IPLVLinkMicEventSender.PLVSMainCallAck() {
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
                PLVLinkMicEventSender.getInstance().closeUserLinkMic(linkMicItemDataBean.getLinkMicId(), null);
            }
        }
    }

    @Override
    public void controlUserLinkMicInLinkMicList(int position, boolean isAllowJoin) {
        if (position < 0 || position >= streamerList.size()) {
            return;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = streamerList.get(position);
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
        if (item != null) {
            controlUserLinkMic(item.first, isAllowJoin);
        }
    }

    @Override
    public void muteUserMedia(int position, final boolean isVideoType, final boolean isMute) {
        if (position < 0 || position >= memberList.size()) {
            return;
        }
        PLVMemberItemDataBean memberItemDataBean = memberList.get(position);
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        String sessionId = liveRoomDataManager.getSessionId();
        PLVLinkMicEventSender.getInstance().muteUserMedia(socketUserBean, sessionId, isVideoType, isMute, new IPLVLinkMicEventSender.PLVSMainCallAck() {
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
    public void muteUserMediaInLinkMicList(int position, boolean isVideoType, boolean isMute) {
        if (position < 0 || position >= streamerList.size()) {
            return;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = streamerList.get(position);
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
        if (item != null) {
            muteUserMedia(item.first, isVideoType, isMute);
        }
    }

    @Override
    public void closeAllUserLinkMic() {
        PLVLinkMicEventSender.getInstance().closeAllUserLinkMic(liveRoomDataManager.getSessionId(), null);
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

    @Override
    public void guestTryJoinLinkMic() {
        boolean isGuestAutoLinkMic = liveRoomDataManager.getConfig().isAutoLinkToGuest();
        if (isGuestAutoLinkMic) {
            //自动上麦
            PLVLinkMicEventSender.getInstance().guestAutoLinkMic(3, new IPLVLinkMicEventSender.IPLVGuestAutoLinkMicListener() {
                @Override
                public void onAutoLinkMic() {
                    if (streamerInitState == STREAMER_MIC_INITIATED) {
                        streamerManager.joinChannel();
                    } else {
                        joinChannelRunnable = new Runnable() {
                            @Override
                            public void run() {
                                streamerManager.joinChannel();
                            }
                        };
                    }
                    requestLinkMicListApiTimer();
                }

                @Override
                public void onTimeout() {
                    final String msg = "嘉宾上麦超时！";
                    PLVCommonLog.e(TAG, msg);
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull @NotNull IPLVStreamerContract.IStreamerView view) {
                            view.onStreamerError(ERROR_GUEST_LINK_TIMEOUT, new Exception(msg));
                        }
                    });
                }

                @Override
                public void onHangupByTeacher() {
                    streamerManager.switchRoleToAudience();
                    callUpdateGuestStatus(false);
                }

                @Override
                public void onInviteByTeacher() {
                    streamerManager.switchRoleToBroadcaster();
                    callUpdateGuestStatus(true);
                }
            });
        } else {
            //手动上麦
            PLVCommonLog.d(TAG, "暂不支持手动上麦的嘉宾");
        }
    }

    @Override
    public void setUserPermissionSpeaker(final String userId, final boolean isSetPermission, final Ack ack) {
        if(!PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER) &&
                !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_GRANTED_SPEAKER_USER)){
            //只有讲师/被授予主讲权限的嘉宾可以控制主讲权限
            return;
        }
        if(currentSocketUserBean == null){
            return;
        }

        final String newPermissionUserId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return getMemberItemWithUserId(userId).second.getLinkMicItemDataBean().getUserId();
            }
        });
        if (newPermissionUserId == null && isSetPermission) {
            return;
        }

        final String sessionId = liveRoomDataManager.getSessionId();
        if (currentSpeakerPermissionUser == null) {
            currentSpeakerPermissionUser = new PLVSocketUserBean();
            currentSpeakerPermissionUser.setUserId(currentSocketUserBean.getUserId());
        }

        if (!isSetPermission) {
            currentSpeakerPermissionUser.setUserId(newPermissionUserId);
        }
        //1、新授权时取消之前的主讲权限
        PLVLinkMicEventSender.getInstance().setSpeakerPermission(currentSpeakerPermissionUser, sessionId, false, new Ack() {
            @Override
            public void call(Object... objects) {
                //2、重新赋值权限用户
                if (isSetPermission) {
                    //3、新授予用户主讲权限
                    currentSpeakerPermissionUser.setUserId(newPermissionUserId);
                    PLVLinkMicEventSender.getInstance().setSpeakerPermission(currentSpeakerPermissionUser, sessionId, true, ack);
                } else {
                    //3、收回权限时，把第一画面重新交给频道主讲
                    currentSpeakerPermissionUser.setUserId(findChannelTeacherUserId());
                    if (currentSpeakerPermissionUser.getUserId() != null) {
                        PLVLinkMicEventSender.getInstance().setSpeakerPermission(currentSpeakerPermissionUser, sessionId, true, ack);
                    } else if (ack != null) {
                        ack.call(objects);
                    }
                }
                //4、切换用户到第一画面
                PLVLinkMicEventSender.getInstance().setSwitchFirstView(currentSpeakerPermissionUser, null);
            }
        });

    }

    @Override
    public void setDocumentAndStreamerViewPosition(boolean documentInMainScreen) {
        final boolean isStreaming = getOrDefault(streamerData.getStreamerStatus().getValue(), false);
        if (!isStreaming || PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_DOCUMENT_ALLOW_SWITCH_WITH_FIRST_SCREEN_TO_ALL_USER)) {
            return;
        }
        PLVLinkMicEventSender.getInstance().setDocumentStreamerViewPosition(documentInMainScreen, liveRoomDataManager.getSessionId());
    }

    @NonNull
    @Override
    public PLVStreamerData getData() {
        return streamerData;
    }

    @Override
    public void destroy() {
        if (currentSocketUserBean != null && currentSocketUserBean.getUserId() != null && !currentSocketUserBean.isTeacher()) {
            setUserPermissionSpeaker(currentSocketUserBean.getUserId(), false, null);
        }
        streamerInitState = STREAMER_MIC_UNINITIATED;
        streamerStatus = STREAMER_STATUS_STOP;
        isRecoverStream = false;

        handler.removeCallbacksAndMessages(null);
        streamerMsgHandler.destroy();

        dispose(listUsersDisposable);
        dispose(listUserTimerDisposable);
        dispose(linkMicListTimerDisposable);
        if (beautySwitchStateObserver != null) {
            beautyRepo.getBeautySwitchLiveData().removeObserver(beautySwitchStateObserver);
            beautySwitchStateObserver = null;
        }

        streamerList.clear();

        timerToShowNetBroken.destroy();
        if (iStreamerViews != null) {
            iStreamerViews.clear();
        }

        //关闭连麦开关
        if (!PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
            PLVLinkMicEventSender.getInstance().closeLinkMic();
        }

        streamerManager.destroy();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - 初始化监听器">
    private void initStreamerListener() {
        //观察连麦的数据
        streamerMsgHandler.observeLinkMicData();
        //添加事件处理器
        streamerManager.addEventHandler(new PLVStreamerEventListener() {
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
                if (quality == PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
                    timerToShowNetBroken.invokeTimerWhenNoConnection();
                    updateNetworkForGuestAutoLinkMic(false);
                } else {
                    if (timerToShowNetBroken.hasShownDuringOneNetBroken) {
                        updateMixLayoutUsers();
                    }
                    timerToShowNetBroken.resetWhenHasConnection();
                    updateNetworkForGuestAutoLinkMic(true);
                }
            }

            @Override
            public void onRemoteStreamOpen(final String uid, int streamType) {
                final PLVLinkMicItemDataBean itemDataBean = nullable(new PLVSugarUtil.Supplier<PLVLinkMicItemDataBean>() {
                    @Override
                    public PLVLinkMicItemDataBean get() {
                        return getLinkMicItemWithLinkMicId(uid).second;
                    }
                });
                if (itemDataBean == null) {
                    return;
                }
                if (streamType == PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN) {
                    itemDataBean.setScreenShare(true);
                    updateMixLayoutWhenScreenShare(true, uid);
                }
            }

            @Override
            public void onRemoteStreamClose(final String uid, int streamType) {
                final PLVLinkMicItemDataBean itemDataBean = nullable(new PLVSugarUtil.Supplier<PLVLinkMicItemDataBean>() {
                    @Override
                    public PLVLinkMicItemDataBean get() {
                        return getLinkMicItemWithLinkMicId(uid).second;
                    }
                });
                if (itemDataBean == null) {
                    return;
                }
                if (streamType == PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN) {
                    itemDataBean.setScreenShare(false);
                    updateMixLayoutWhenScreenShare(false, uid);
                }
            }
        });

        //推流开始
        streamerManager.addOnLiveStreamingStartListener(new IPLVStreamerOnLiveStreamingStartListener() {
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
        streamerManager.setOnLiveTimingListener(new IPLVStreamerOnLiveTimingListener() {
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
        streamerManager.addStreamerServerTimeoutListener(new IPLVStreamerOnServerTimeoutDueToNetBrokenListener() {
            @Override
            public void onServerTimeoutDueToNetBroken() {
                //将状态改变为：已经下课
                if (streamerStatus != STREAMER_STATUS_STOP) {
                    stopLiveStream();
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onStreamerError(-1, new Throwable("timeout"));
                        }
                    });
                }
            }
        });

        //推流sessionId
        streamerManager.addGetSessionIdFromServerListener(new IPLVOnGetSessionIdInnerListener() {
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
        String loginRoomId = PLVSocketWrapper.getInstance().getLoginRoomId();//分房间开启，在获取到后为分房间id，其他情况为频道号
        if (TextUtils.isEmpty(loginRoomId)) {
            loginRoomId = liveRoomDataManager.getConfig().getChannelId();//socket未登陆时，使用频道号
        }
        final String roomId = loginRoomId;
        listUsersDisposable = PLVChatApiRequestHelper.getListUsers(roomId, DEFAULT_MEMBER_PAGE, DEFAULT_MEMBER_LENGTH)
                .retryWhen(new PLVRxBaseRetryFunction(Integer.MAX_VALUE, 3000))
                .compose(new PLVRxBaseTransformer<PLVListUsersVO, PLVListUsersVO>())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO plvsListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
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
                        PLVELogSender.send(PLVChatroomELog.class, PLVChatroomELog.Event.GET_LISTUSERS_FAIL, throwable);
                    }
                });
    }

    private void requestListUsersApiTimer(final String roomId) {
        dispose(listUserTimerDisposable);
        listUserTimerDisposable = Observable.interval(INTERVAL_TO_GET_USER_LIST, INTERVAL_TO_GET_USER_LIST, TimeUnit.MILLISECONDS, Schedulers.io())
                .flatMap(new Function<Long, Observable<PLVListUsersVO>>() {
                    @Override
                    public Observable<PLVListUsersVO> apply(@NonNull Long aLong) throws Exception {
                        return PLVChatApiRequestHelper.getListUsers(roomId, DEFAULT_MEMBER_PAGE, DEFAULT_MEMBER_LENGTH).retry(1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO plvsListUsersVO) throws Exception {
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
                        generateMemberListWithListUsers(plvsListUsersVO.getUserlist(), false);
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

    private void generateMemberListWithListUsers(List<PLVSocketUserBean> socketUserBeanList, boolean isResetJoiningStatus) {
        List<PLVMemberItemDataBean> tempMemberList = new LinkedList<>();
        for (int i = 0; i < socketUserBeanList.size(); i++) {
            PLVSocketUserBean socketUserBean = socketUserBeanList.get(i);
            String userId = socketUserBean.getUserId();
            if (userId != null && userId.equals(PLVSocketWrapper.getInstance().getLoginVO().getUserId())) {
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
                currentSocketUserBean = PLVSocketWrapper.getInstance().getLoginVO().createSocketUserBean();
                memberItemDataBean.setSocketUserBean(currentSocketUserBean);
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
                if (TextUtils.isEmpty(liveRoomDataManager.getSessionId())) {
                    return;
                }
                streamerManager.getLinkStatus(liveRoomDataManager.getSessionId(), new PLVLinkMicDataRepository.IPLVLinkMicDataRepoListener<PLVLinkMicJoinStatus>() {
                    @Override
                    public void onSuccess(PLVLinkMicJoinStatus data) {
                        PLVCommonLog.d(TAG, "PLVStreamerPresenter.requestLinkMicListFromServer.onSuccess->\n" + PLVGsonUtil.toJson(data));
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
            //更新连麦列表中的权限状态
            updateUserPermissionStatus(linkMicItemDataBean, joinInfoEvent);
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
        final PLVLinkMicItemDataBean linkMicItemDataBean = item.getLinkMicItemDataBean();
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
                    if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                        SortGuestLinkMicListUtils.sort(streamerList);
                    }
                    updateMixLayoutUsers();
                    updateLinkMicCount();
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            //更新推流和连麦列表
                            view.onUsersJoin(Collections.singletonList(linkMicItemDataBean));
                        }
                    });
                }
                break;
            }
        }
        return hasChangedMemberList;
    }

    /**
     * 更新混流用户状态，需要和以下的调用保持同步：
     * view.onUserLeave
     * view.onUsersJoin
     * view.onUserMuteVideo
     */
    void updateMixLayoutUsers() {
        List<PLVRTCMixUser> mixUserList = new ArrayList<>();
        for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : streamerList) {
            PLVRTCMixUser mixUser = new PLVRTCMixUser();
            mixUser.setScreenShare(plvLinkMicItemDataBean.isScreenShare());
            mixUser.setUserId(plvLinkMicItemDataBean.getLinkMicId());
            mixUser.setMuteVideo(plvLinkMicItemDataBean.isMuteVideo());
            mixUserList.add(mixUser);
        }
        streamerManager.updateMixLayoutUsers(mixUserList);
    }

    /**
     * 更新屏幕共享时，混流用户状态，需要和以下的调用保持同步：
     * view.onScreenShareChange
     */
    void updateMixLayoutWhenScreenShare(boolean isShare, String linkmicId) {
        List<PLVRTCMixUser> mixUserList = new ArrayList<>();
        for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : streamerList) {
            PLVRTCMixUser mixUser = new PLVRTCMixUser();
            if (plvLinkMicItemDataBean.getLinkMicId().equals(linkmicId)) {
                mixUser.setScreenShare(isShare);
            } else {
                mixUser.setScreenShare(plvLinkMicItemDataBean.isScreenShare());
            }
            //屏幕共享时，当前用户的混流摄像头不能为mute，否则CDN混流无画面
            mixUser.setMuteVideo(plvLinkMicItemDataBean.isMuteVideo());
            mixUser.setUserId(plvLinkMicItemDataBean.getLinkMicId());
            mixUserList.add(mixUser);
        }
        streamerManager.updateMixLayoutUsers(mixUserList);
    }

    void updateLinkMicCount() {
        streamerData.postLinkMicCount(streamerList.size());
    }

    private void updateUserPermissionStatus(PLVLinkMicItemDataBean linkMicItemDataBean, PLVJoinInfoEvent joinInfoEvent) {
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
        if (memberItem != null) {
            final boolean speaker = joinInfoEvent.getClassStatus().isSpeaker();
            if (memberItem.second.getLinkMicItemDataBean().isHasSpeaker() != speaker) {
                memberItem.second.getLinkMicItemDataBean().setHasSpeaker(speaker);
                final PLVSocketUserBean socketUser = memberItem.second.getSocketUserBean();
                callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onSetPermissionChange(PLVPPTAuthentic.PermissionType.TEACHER, speaker, true, socketUser);
                    }
                });
            }
            if (speaker) {
                // 主讲权限视为第一画面
                onFirstScreenChange(linkMicItemDataBean.getLinkMicId(), true);
            }
        }
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
        final List<PLVLinkMicItemDataBean> willRemoveStreamerList = new ArrayList<>();
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
                willRemoveStreamerList.add(linkMicItemDataBean);
            }
        }
        if (!willRemoveStreamerList.isEmpty()) {
            updateMixLayoutUsers();
            updateLinkMicCount();
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
                linkMicItemDataBean.setMuteVideo(!PLVLinkMicEventSender.getInstance().isVideoLinkMicType());
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

    // <editor-fold defaultstate="collapsed" desc="轮询直播状态">
    private void pollLiveStatus() {
        streamerManager.listenLiveStatusChange(0, INTERVAL_TO_POLL_LIVE_STATUS, new IPLVSStreamerOnLiveStatusChangeListener() {
            @Override
            public void onLiveStatusChange(final boolean isLive) {
                if (isLive) {
                    //直播中
                    guestTryJoinLinkMic();
                } else {
                    //直播结束

                    dispose(linkMicListTimerDisposable);
                    streamerManager.leaveChannel(true);

                    PLVLinkMicItemDataBean guestDataBean = null;
                    String myLinkMicID = streamerManager.getLinkMicUid();
                    for (int i = 0; i < streamerList.size(); i++) {
                        PLVLinkMicItemDataBean dataBean = streamerList.get(i);
                        if (myLinkMicID.equals(dataBean.getLinkMicId())) {
                            guestDataBean = dataBean;
                            break;
                        }
                    }
                    rtcJoinMap.clear();
                    streamerList.clear();
                    if (guestDataBean != null) {
                        streamerList.add(guestDataBean);
                    }
                    updateLinkMicCount();
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull @NotNull IPLVStreamerContract.IStreamerView view) {
                            view.onUsersLeave(streamerList);
                        }
                    });
                    callUpdateGuestStatus(false);
                }
                streamerData.postStreamerStatus(isLive);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull @NotNull IPLVStreamerContract.IStreamerView view) {
                        view.onStreamLiveStatusChanged(isLive);
                    }
                });
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据存储">
    private void saveBitrate() {
        if (curBitrate < PLVStreamerConfig.Bitrate.BITRATE_STANDARD
                || curBitrate > PLVStreamerConfig.Bitrate.BITRATE_SUPER) {
            // invalid bitrate data
            return;
        }
        SPUtils.getInstance().put("plv_key_bitrate", curBitrate);
    }

    private int loadBitrate() {
        int bitrate = SPUtils.getInstance().getInt("plv_key_bitrate", PLVStreamerConfig.Bitrate.BITRATE_SUPER);
        if (bitrate < PLVStreamerConfig.Bitrate.BITRATE_STANDARD) {
            bitrate = PLVStreamerConfig.Bitrate.BITRATE_STANDARD;
        }
        if (bitrate > PLVStreamerConfig.Bitrate.BITRATE_SUPER) {
            bitrate = PLVStreamerConfig.Bitrate.BITRATE_SUPER;
        }
        return bitrate;
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

    @Nullable
    private String findChannelTeacherUserId() {
        for (PLVMemberItemDataBean memberItemDataBean : memberList) {
            if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(memberItemDataBean.getSocketUserBean().getUserType())) {
                return memberItemDataBean.getSocketUserBean().getUserId();
            }
        }
        return null;
    }

    Pair<Integer, PLVLinkMicItemDataBean> getLinkMicItemWithLinkMicId(@Nullable String linkMicId) {
        for (int i = 0; i < streamerList.size(); i++) {
            final PLVLinkMicItemDataBean itemDataBean = streamerList.get(i);
            if (linkMicId != null && linkMicId.equals(itemDataBean.getLinkMicId())) {
                return new Pair<>(i, itemDataBean);
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
        updateMixLayoutUsers();
    }

    void callUpdateGuestStatus(boolean joinRTC) {
        String myLinkMicID = streamerManager.getLinkMicUid();
        int myIndex = 0;
        for (int i = 0; i < streamerList.size(); i++) {
            PLVLinkMicItemDataBean dataBean = streamerList.get(i);
            if (myLinkMicID.equals(dataBean.getLinkMicId())) {
                dataBean.setStatus(joinRTC ? PLVLinkMicItemDataBean.STATUS_RTC_JOIN : PLVLinkMicItemDataBean.STATUS_IDLE);
                myIndex = i;
                break;
            }
        }
        final int finalMyIndex = myIndex;
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull @NotNull IPLVStreamerContract.IStreamerView view) {
                view.onGuestRTCStatusChanged(finalMyIndex);
            }
        });
    }

    void callUpdateGuestMediaStatus(boolean isMute, boolean isAudio) {
        String myLinkMicID = streamerManager.getLinkMicUid();
        if (isAudio) {
            enableRecordingAudioVolume(!isMute);
        } else {
            enableLocalVideo(!isMute);
            streamerManager.enableLocalCamera(!isMute);
        }
    }

    void onFirstScreenChange(final String firstScreenUserId, final boolean isFirstScreen) {
        if (lastFirstScreenUserId != null && lastFirstScreenUserId.equals(firstScreenUserId)) {
            return;
        }
        PLVCommonLog.i(TAG, "onFirstScreenChange: " + firstScreenUserId + ", isFirstScreen: " + isFirstScreen);

        catchingNull(new Runnable() {
            @Override
            public void run() {
                getLinkMicItemWithLinkMicId(lastFirstScreenUserId).second.setFirstScreen(false);
            }
        });
        catchingNull(new Runnable() {
            @Override
            public void run() {
                getLinkMicItemWithLinkMicId(firstScreenUserId).second.setFirstScreen(isFirstScreen);
            }
        });

        lastFirstScreenUserId = firstScreenUserId;

        SortGuestLinkMicListUtils.sort(streamerList);
        updateMixLayoutUsers();
        callbackToView(new PLVStreamerPresenter.ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onFirstScreenChange(firstScreenUserId, isFirstScreen);
            }
        });
    }

    void onCurrentSpeakerChanged(final String type, final boolean isGranted, final boolean isCurrentUser, final PLVSocketUserBean user) {
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onSetPermissionChange(type, isGranted, isCurrentUser, user);
            }
        });

        if (type != null
                && type.equals(PLVPPTAuthentic.TYPE_SPEAKER)
                && isGranted) {
            if (currentSpeakerPermissionUser == null) {
                currentSpeakerPermissionUser = new PLVSocketUserBean();
                if (user != null) {
                    currentSpeakerPermissionUser.setUserId(user.getUserId());
                }
            } else {
                // 主讲权限转移时，由于频道内只有一个主讲，撤回上一个主讲的权限
                if (currentSpeakerPermissionUser.getUserId() != null
                        && user != null
                        && user.getUserId() != null
                        && !currentSpeakerPermissionUser.getUserId().equals(user.getUserId())) {
                    // revoke old speaker permission
                    final String oldSpeakerUserId = currentSpeakerPermissionUser.getUserId();
                    catchingNull(new Runnable() {
                        @Override
                        public void run() {
                            getMemberItemWithLinkMicId(oldSpeakerUserId).second.getLinkMicItemDataBean().setHasSpeaker(false);
                        }
                    });
                    catchingNull(new Runnable() {
                        @Override
                        public void run() {
                            getLinkMicItemWithLinkMicId(oldSpeakerUserId).second.setHasSpeaker(false);
                        }
                    });
                    final boolean isRevokeMySpeaker = isMyLinkMicId(oldSpeakerUserId);
                    if (isRevokeMySpeaker) {
                        PLVUserAbilityManager.myAbility().removeRole(PLVUserRole.STREAMER_GRANTED_SPEAKER_USER);
                    }
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onSetPermissionChange(type, false, isRevokeMySpeaker, currentSpeakerPermissionUser);
                        }
                    });
                    currentSpeakerPermissionUser.setUserId(user.getUserId());
                }
            }
        }
    }

    IPLVLiveRoomDataManager getLiveRoomDataManager() {
        return liveRoomDataManager;
    }

    IPLVStreamerManager getStreamerManager() {
        return streamerManager;
    }

    /**
     * 从断网中恢复链接，嘉宾需要重新上麦
     */
    private void updateNetworkForGuestAutoLinkMic(boolean isNetworkConnect) {
        final boolean lastNetworkConnect = getOrDefault(this.isNetworkConnect, isNetworkConnect);
        this.isNetworkConnect = isNetworkConnect;

        if (!lastNetworkConnect && this.isNetworkConnect) {
            final boolean isGuest = PLVSocketUserConstant.USERTYPE_GUEST.equals(userType);
            final boolean isGuestAutoLinkMic = liveRoomDataManager.getConfig().isAutoLinkToGuest();
            final boolean isLive = getOrDefault(streamerData.getStreamerStatus().getValue(), false);
            if (isGuest && isGuestAutoLinkMic && isLive) {
                guestTryJoinLinkMic();
            }
        }
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
        private final int secondsToShow;

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

                        //如果还没有推流成功，说明在推流前已经断网了，这时直接变成下课
                        if (!streamerManager.isLiveStreaming()) {
                            stopLiveStream();
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                    view.onStreamerError(-1, new Throwable("network disconnect"));
                                }
                            });
                        } else {
                            //call
                            streamerData.postShowNetBroken();
                            callbackToView(new ViewRunnable() {
                                @Override
                                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                    view.onShowNetBroken();
                                }
                            });
                        }
                    } else {
                        //还未到时间，检查是否连上网络了,如果在指定时间内网络恢复，则取消定时器。
                        int netQuality = streamerManager.getCurrentNetQuality();
                        if (netQuality != PLVStreamerConfig.NetQuality.NET_QUALITY_NO_CONNECTION) {
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
                REAL_LINK_MIC_WAIT,
                REAL_LINK_MIC_JOINING,
                REAL_LINK_MIC_JOIN,
                REAL_LINK_MIC_RTC_JOIN,
                REAL,
                PLVSocketUserConstant.USERTYPE_DUMMY
        );

        private static String getSortType(PLVMemberItemDataBean item) {
            PLVSocketUserBean data = item.getSocketUserBean();
            String type = data.getUserType();
            String myUserId = PLVSocketWrapper.getInstance().getLoginVO().getUserId();
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

    // <editor-fold defaultstate="collapsed" desc="内部类 - 排序嘉宾连麦列表">
    public static class SortGuestLinkMicListUtils {
        // 第一画面
        private static final String FIRST_SCREEN_USER_TYPE = "SortGuestLinkMicListUtils-firstScreenUserType";
        //除了讲师和嘉宾，其他类型都放在最后面，不论他是什么用户类型
        private static final String OTHER_TYPE = "SortGuestLinkMicListUtils-other";
        private static final List<String> SORT_INDEX = Arrays.asList(
                FIRST_SCREEN_USER_TYPE,
                PLVSocketUserConstant.USERTYPE_TEACHER,
                PLVSocketUserConstant.USERTYPE_GUEST,
                OTHER_TYPE
        );

        private static String getSortType(PLVLinkMicItemDataBean itemDataBean) {
            if (itemDataBean.isFirstScreen()) {
                return FIRST_SCREEN_USER_TYPE;
            }
            String type = itemDataBean.getUserType();
            if (!SORT_INDEX.contains(type)) {
                return OTHER_TYPE;
            }
            return type;
        }

        public static List<PLVLinkMicItemDataBean> sort(List<PLVLinkMicItemDataBean> input) {
            Collections.sort(input, new Comparator<PLVLinkMicItemDataBean>() {
                @Override
                public int compare(PLVLinkMicItemDataBean o1, PLVLinkMicItemDataBean o2) {
                    int io1 = SORT_INDEX.indexOf(getSortType(o1));
                    int io2 = SORT_INDEX.indexOf(getSortType(o2));
                    if (io1 != io2) {
                        return io1 - io2;
                    }
                    try {
                        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(o1.getUserType()) && PLVSocketUserConstant.USERTYPE_GUEST.equals(o2.getUserType())) {
                            return Integer.parseInt(o1.getUserId()) - Integer.parseInt(o2.getUserId());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    return 0;
                }
            });
            return input;
        }
    }
// </editor-fold>
}
