package com.easefun.polyv.livecommon.module.modules.streamer.presenter;

import static com.plv.foundationsdk.utils.PLVSugarUtil.catchingNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.getOrDefault;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.beauty.model.PLVBeautyRepo;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicDataMapper;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVStreamerMixBackground;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.data.PLVStreamerData;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.usecase.PLVStreamerLinkMicMsgHandler;
import com.easefun.polyv.livescenes.streamer.listener.IPLVSStreamerOnLiveStatusChangeListener;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.component.collection.PLVSequenceWrapper;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.kv.PLVAutoSaveKV;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.rx.PLVRxBaseRetryFunction;
import com.plv.foundationsdk.rx.PLVRxBaseTransformer;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.linkmic.model.PLVPushDowngradePreference;
import com.plv.linkmic.model.PLVPushStreamTemplateJsonBean;
import com.plv.linkmic.repository.PLVLinkMicDataRepository;
import com.plv.linkmic.repository.PLVLinkMicHttpRequestException;
import com.plv.linkmic.screenshare.IPLVScreenShareListener;
import com.plv.linkmic.screenshare.vo.PLVCustomScreenShareData;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.chatroom.PLVChatApiRequestHelper;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.linkmic.IPLVLinkMicManager;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.linkmic.vo.PLVLinkMicDenoiseType;
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
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.impl.PLVSocketManager;
import com.plv.socket.log.PLVELogSender;
import com.plv.socket.socketio.PLVSocketIOObservable;
import com.plv.socket.status.PLVSocketStatus;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import net.polyv.android.player.business.scene.common.player.IPLVMediaPlayer;

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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import kotlin.jvm.functions.Function1;

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
    public static final int MEMBER_MAX_LENGTH = 500;

    // 本地存储的各频道推流布局配置
    private static final PLVAutoSaveKV<Map<String, PLVStreamerConfig.MixLayoutType>> CHANNEL_MIX_LAYOUT_TYPE_KV = new PLVAutoSaveKV<Map<String, PLVStreamerConfig.MixLayoutType>>("plv_streamer_channel_mix_layout_type") {};

    /**** View ****/
    //推流和连麦mvp模式的view
    private List<IPLVStreamerContract.IStreamerView> iStreamerViews;
    /**** Model ****/
    //推流和连麦的管理器
    private final IPLVStreamerManager streamerManager;
    //推流和连麦的socket信息处理器
    private final PLVStreamerMsgHandler streamerMsgHandler;
    private final PLVBeautyRepo beautyRepo = PLVDependManager.getInstance().get(PLVBeautyRepo.class);
    private PLVForceHangUpHandler forceHangUpHandler = new PLVForceHangUpHandler(this);
    private PLVStreamerLinkMicMsgHandler streamerLinkMicMsgHandler;

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
    //频道类型
    private PLVLiveChannelType channelType;
    //推流和连麦数据
    private final PLVStreamerData streamerData;
    //当前拥有主讲权限的用户
    private PLVSocketUserBean currentSpeakerPermissionUser;
    @Nullable
    private String lastFirstScreenUserId;

    /**** 推流参数 ****/
    @PLVStreamerConfig.BitrateType
    private int curBitrate = 0;
    private boolean curCameraFront = true;
    private boolean curEnableRecordingAudioVolume = true;
    private boolean curEnableLocalVideo = true;
    private boolean isFrontMirror = true;
    private int pushPictureResolution = PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE;
    private PLVLinkMicConstant.PushResolutionRatio pushResolutionRatio = PLVLinkMicConstant.PushResolutionRatio.RATIO_16_9;
    private PLVStreamerConfig.MixLayoutType mixLayoutType = PLVStreamerConfig.MixLayoutType.TILE;
    //开始推流是否需要恢复上一场的直播流，继续推流
    private boolean isRecoverStream = false;
    private float localCameraZoomFactor = 1F;
    @Nullable
    private Boolean isNetworkConnect = null;
    private boolean isMyselfJoinRtc = false;
    private PLVAutoSaveKV<PLVLinkMicDenoiseType> denoiseTypeKV = new PLVAutoSaveKV<PLVLinkMicDenoiseType>("plv_streamer_denoise_type_new") {};
    private PLVAutoSaveKV<Boolean> isUseExternalAudioInputKV = new PLVAutoSaveKV<Boolean>("plv_streamer_is_use_external_audio_input") {};
    private Bitmap watermarkBitmap;
    private View myRenderView;
    private ViewGroup myRenderViewParent;
    private PLVStreamerMixBackground mixBackground = PLVStreamerMixBackground.DEFAULT;

    /**** 容器 ****/
    //推流和连麦列表
    private final List<PLVLinkMicItemDataBean> streamerList = new LinkedList<>();
    //成员列表
    final List<PLVMemberItemDataBean> memberList = Collections.synchronizedList(new LinkedList<PLVMemberItemDataBean>());
    //成员搜索列表
    final List<PLVMemberItemDataBean> memberSearchList = new LinkedList<>();
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
    final Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVStreamerPresenter(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        channelType = liveRoomDataManager.getConfig().getChannelType();
        streamerData = new PLVStreamerData();
        curBitrate = loadBitrate();
        streamerLinkMicMsgHandler = PLVStreamerLinkMicMsgHandler.create(liveRoomDataManager.getConfig().getChannelId());
        streamerLinkMicMsgHandler.setStreamerPresenter(this);
        initMixLayoutType();
        initNewLinkMicStrategyDefaultType();

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
                PLVCommonLog.d(TAG, "推流和连麦初始化成功");// no need i18n
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
                    linkMicItemDataBean.setLinkMicStartTimestamp(System.currentTimeMillis());
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
                setPushDowngradePreference(
                        PLVChannelFeatureManager.onChannel(param.getChannelId())
                                .getOrDefault(PLVChannelFeature.STREAMER_PUSH_QUALITY_PREFERENCE, PLVPushDowngradePreference.PREFER_BETTER_QUALITY)
                );
                setMixLayoutType(mixLayoutType);
                setDenoiseType(denoiseTypeKV.getOrDefault(PLVLinkMicDenoiseType.DEFAULT));
                setIsUseExternalAudioInput(isUseExternalAudioInputKV.getOrDefault(false));
                setMixBackground(mixBackground);
                streamerData.postLocalAudioCaptureVolume(streamerManager.getLocalAudioCaptureVolume());

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
                PLVCommonLog.e(TAG, "推流和连麦模块错误：errorCode=" + errorCode);// no need i18n
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
            public void onScreenShare(final boolean isShare, final boolean shareInScreenStream) {
                //发送消息通知
                if (currentSocketUserBean != null) {
                    PLVLinkMicEventSender.getInstance().sendScreenShareEvent(currentSocketUserBean, liveRoomDataManager.getSessionId(), isShare, null);
                }

                streamerData.postEnableShareScreen(isShare);
                Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(streamerManager.getLinkMicUid());
                Pair<Integer, PLVLinkMicItemDataBean> streamerItem = getLinkMicItemWithLinkMicId(streamerManager.getLinkMicUid());
                if (memberItem != null) {
                    memberItem.second.getLinkMicItemDataBean().setScreenShare(isShare);
                    memberItem.second.getLinkMicItemDataBean().setScreenShareInScreenStream(shareInScreenStream);
                }
                if (streamerItem != null) {
                    streamerItem.second.setScreenShare(isShare);
                    streamerItem.second.setScreenShareInScreenStream(shareInScreenStream);
                }
                final int pos = streamerItem == null ? 0 : streamerItem.first;

                callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onScreenShareChange(pos, isShare, IPLVScreenShareListener.PLV_SCREEN_SHARE_OK, streamerManager.getLinkMicUid(), true);
                    }
                });
                updateMixLayoutWhenScreenShare(isShare, streamerItem.second.getLinkMicId());
                // 屏幕共享不需要水印
                streamerManager.setWatermark(isShare ? null : watermarkBitmap, 0, 0, 1);
            }

            @Override
            public void onScreenShareError(final int errorCode) {
                Pair<Integer, PLVLinkMicItemDataBean> streamerItem = getLinkMicItemWithLinkMicId(streamerManager.getLinkMicUid());
                final int pos = streamerItem.first;
                streamerItem.second.setScreenShare(false);
                callbackToView(new PLVStreamerPresenter.ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onScreenShareChange(pos, false, errorCode, streamerManager.getLinkMicUid(), true);
                    }
                });
                updateMixLayoutWhenScreenShare(false, streamerItem.second.getLinkMicId());
                streamerData.postEnableShareScreen(false);

            }
        });

    }

    @Override
    public PLVLinkMicConstant.NetworkQuality getNetworkQuality() {
        return streamerManager.getNetworkQuality();
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
        return PLVStreamerInnerDataTransfer.getInstance().getSupportedMaxBitrateCombineTemplate();
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
    public void setLocalAudioCaptureVolume(int volume) {
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.adjustRecordingSignalVolume(volume);
        streamerData.postLocalAudioCaptureVolume(volume);
    }

    @Override
    public boolean isLocalAudioEnabled() {
        return curEnableRecordingAudioVolume;
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
    public boolean isLocalVideoEnabled() {
        return curEnableLocalVideo;
    }

    @Override
    public void enableLocalVideoCapture(boolean enable) {
        streamerManager.enableLocalCameraCapture(enable);
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
                // 同步状态到搜索列表
                for (PLVMemberItemDataBean memberItemDataBean : memberSearchList) {
                    if (memberItemDataBean.getLinkMicItemDataBean() != null && memberItemDataBean.getLinkMicItemDataBean().getLinkMicId() != null) {
                        if (memberItemDataBean.getLinkMicItemDataBean().getLinkMicId().equals(streamerManager.getLinkMicUid())) {
                            memberItemDataBean.setFrontCamera(front);
                            break;
                        }
                    }
                }
                view.onCameraDirection(front, item.first, streamerManager.getLinkMicUid());
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
    public void setMixLayoutType(PLVStreamerConfig.MixLayoutType mixLayoutType) {
        this.mixLayoutType = mixLayoutType;
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setMixLayoutType(mixLayoutType);
        saveMixLayoutType();
    }

    @Override
    public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
        return mixLayoutType;
    }

    @Override
    public void setMixBackground(PLVStreamerMixBackground mixBackground) {
        this.mixBackground = mixBackground;
        if (!isInitStreamerManager()) {
            return;
        }
        streamerManager.setMixBackgroundImageUrl(mixBackground.getUrl());
    }

    @Override
    public PLVStreamerMixBackground getMixBackground() {
        return mixBackground;
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
    public TextureView createTextureRenderView(Context context) {
        return streamerManager.createTextureRenderView(context);
    }

    @Override
    public void releaseRenderView(View renderView) {
        streamerManager.releaseRenderView(renderView);
        if (myRenderView == renderView) {
            tryDetachWaterFromRenderParent(true);
            myRenderView = null;
        }
    }

    @Override
    public void setupRenderView(View renderView, String linkMicId) {
        if (isMyLinkMicId(linkMicId)) {
            if (liveRoomDataManager.isOnlyAudio()) {
                streamerManager.setupLocalVideo(renderView, PLVStreamerConfig.RenderMode.RENDER_MODE_NONE);
                return;
            }
            int renderMode = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                    .getOrDefault(PLVChannelFeature.LIVE_LINKMIC_VIDEO_RENDER_MODE, PLVLinkMicConstant.RenderMode.RENDER_MODE_HIDDEN);
            streamerManager.setupLocalVideo(renderView, renderMode);
            myRenderView = renderView;
            tryAttachWaterToRenderParent();
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
                PLVCommonLog.d(TAG, "推流和连麦开始初始化");// no need i18n
                init();
                break;
            //初始化中
            case STREAMER_MIC_INITIATING:
                PLVCommonLog.d(TAG, "推流和连麦初始化中");// no need i18n
                return;
            //已经初始化
            case STREAMER_MIC_INITIATED:
                // socket连接成功后，再开始推流
                if (PLVSocketManager.getInstance().isOnlineStatus()) {
                    streamerManager.startLiveStream(isRecoverStream);
                } else {
                    PLVSocketWrapper.getInstance().getSocketObserver().addOnConnectStatusListener(new PLVSocketIOObservable.OnConnectStatusListener() {
                        @Override
                        public void onStatus(PLVSocketStatus status) {
                            if (status.getStatus() == PLVSocketStatus.STATUS_LOGINSUCCESS) {
                                PLVSocketWrapper.getInstance().getSocketObserver().removeOnConnectStatusListener(this);
                                streamerManager.startLiveStream(isRecoverStream);
                            }
                        }
                    });
                }
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
        liveRoomDataManager.getStreamerStatusLiveData().postValue(false);
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
            if (curCameraFront) {
                streamerManager.setLocalPushMirror(isFrontMirror);
            }
        }
    }

    @Override
    public void requestShareScreen(Activity activity, PLVCustomScreenShareData customScreenShareData) {
        if (!streamerManager.isScreenSharing()) {
            streamerManager.setLocalPushMirror(false);
            streamerManager.requestScreenCapture(activity, customScreenShareData);
        }
    }

    @Override
    public boolean isScreenSharing() {
        return streamerManager.isScreenSharing();
    }

    @Override
    public boolean openLinkMic(final boolean isVideoType, final boolean isOpen, final Ack ack) {
        final Ack wrapAck = new IPLVLinkMicEventSender.PLVSMainCallAck() {
            @Override
            public void onCall(Object... args) {
                if (ack != null) {
                    ack.call(args);
                }
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLinkMicOpenStateChanged(isVideoType, isOpen);
                    }
                });
                if (!isOpen) {
                    PLVSequenceWrapper.wrap(memberList)
                            .filter(new Function1<PLVMemberItemDataBean, Boolean>() {
                                @Override
                                public Boolean invoke(PLVMemberItemDataBean itemDataBean) {
                                    if (itemDataBean == null || itemDataBean.getSocketUserBean() == null) {
                                        return false;
                                    }
                                    final boolean isViewer = PLVSocketUserConstant.USERTYPE_STUDENT.equals(itemDataBean.getSocketUserBean().getUserType())
                                            || PLVSocketUserConstant.USERTYPE_SLICE.equals(itemDataBean.getSocketUserBean().getUserType());
                                    return itemDataBean.getLinkMicStatus() != PLVLinkMicItemDataBean.LinkMicStatus.IDLE && isViewer;
                                }
                            })
                            .forEach(new PLVSugarUtil.Consumer<PLVMemberItemDataBean>() {
                                @Override
                                public void accept(PLVMemberItemDataBean itemDataBean) {
                                    itemDataBean.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.IDLE);
                                }
                            });
                    callUpdateSortMemberList();
                }
            }
        };
        boolean result;
        if (isOpen) {
            result = streamerLinkMicMsgHandler.openChannelLinkMic(isVideoType, wrapAck);
        } else {
            result = streamerLinkMicMsgHandler.closeChannelLinkMic(wrapAck);
        }
        streamerData.postVideoLinkMicType(streamerLinkMicMsgHandler.isVideoLinkMic());
        return result;
    }

    @Override
    public boolean closeLinkMic(Ack ack) {
        return openLinkMic(PLVLinkMicEventSender.getInstance().isVideoLinkMicType(), false, ack);
    }

    @Override
    public boolean allowViewerRaiseHand(final Ack ack) {
        return streamerLinkMicMsgHandler.allowViewerRaiseHandLinkMic(new Ack() {
            @Override
            public void call(Object... args) {
                if (ack != null) {
                    ack.call(args);
                }
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLinkMicOpenStateChanged(streamerLinkMicMsgHandler.isVideoLinkMic(), streamerLinkMicMsgHandler.isOpenLinkMic());
                    }
                });
            }
        });
    }

    @Override
    public boolean disallowViewerRaiseHand(final Ack ack) {
        return streamerLinkMicMsgHandler.disallowViewerRaiseHandLinkMic(new Ack() {
            @Override
            public void call(Object... args) {
                if (ack != null) {
                    ack.call(args);
                }
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onLinkMicOpenStateChanged(streamerLinkMicMsgHandler.isVideoLinkMic(), streamerLinkMicMsgHandler.isOpenLinkMic());
                    }
                });
                PLVSequenceWrapper.wrap(memberList)
                        .filter(new Function1<PLVMemberItemDataBean, Boolean>() {
                            @Override
                            public Boolean invoke(PLVMemberItemDataBean itemDataBean) {
                                if (itemDataBean == null || itemDataBean.getSocketUserBean() == null) {
                                    return false;
                                }
                                final boolean isViewer = PLVSocketUserConstant.USERTYPE_STUDENT.equals(itemDataBean.getSocketUserBean().getUserType())
                                        || PLVSocketUserConstant.USERTYPE_SLICE.equals(itemDataBean.getSocketUserBean().getUserType());
                                return isViewer && itemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP;
                            }
                        })
                        .forEach(new PLVSugarUtil.Consumer<PLVMemberItemDataBean>() {
                            @Override
                            public void accept(PLVMemberItemDataBean itemDataBean) {
                                itemDataBean.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.IDLE);
                            }
                        });
                callUpdateSortMemberList();
            }
        });
    }

    @Override
    public boolean changeLinkMicType(boolean isVideoType) {
        closeAllUserLinkMic();

        boolean success = streamerLinkMicMsgHandler.changeLinkMicType(isVideoType);
        if (success) {
            streamerData.postVideoLinkMicType(streamerLinkMicMsgHandler.isVideoLinkMic());
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onLinkMicOpenStateChanged(streamerLinkMicMsgHandler.isVideoLinkMic(), streamerLinkMicMsgHandler.isOpenLinkMic());
                }
            });
        }
        return success;
    }

    @Override
    public void controlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
        if (position < 0 || position >= memberList.size()) {
            return;
        }
        final PLVMemberItemDataBean memberItemDataBean = memberList.get(position);
        PLVSocketUserBean socketUserBean = memberItemDataBean.getSocketUserBean();
        @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberItemDataBean.getLinkMicItemDataBean();
        if (action instanceof PLVStreamerControlLinkMicAction.AcceptRequestAction) {
            if (rtcJoinMap.size() >= PLVStreamerInnerDataTransfer.getInstance().getInteractNumLimit()) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onReachTheInteractNumLimit();
                    }
                });
                return;
            }
            streamerLinkMicMsgHandler.acceptRaiseHandLinkMic(socketUserBean, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    if (linkMicItemDataBean != null) {
                        linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_JOINING);
                        startJoinTimeoutCount(linkMicItemDataBean);
                        callUpdateSortMemberList();
                    }
                }
            });
        } else if (action instanceof PLVStreamerControlLinkMicAction.SendInvitationAction
                && memberItemDataBean.getLinkMicStatus() != PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION) {
            if (rtcJoinMap.size() >= PLVStreamerInnerDataTransfer.getInstance().getInteractNumLimit()) {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onReachTheInteractNumLimit();
                    }
                });
                return;
            }
            final boolean needAnswer = ((PLVStreamerControlLinkMicAction.SendInvitationAction) action).needAnswer
                    && memberItemDataBean.getLinkMicStatus() != PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP;
            streamerLinkMicMsgHandler.inviteLinkMic(socketUserBean, needAnswer, new IPLVLinkMicEventSender.PLVSMainCallAck() {
                @Override
                public void onCall(Object... args) {
                    memberItemDataBean.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION);
                    callUpdateSortMemberList();
                }
            });
        } else if (action instanceof PLVStreamerControlLinkMicAction.HangUpAction) {
            if (linkMicItemDataBean != null) {
                normalCloseUserLinkMic(linkMicItemDataBean.getLinkMicId(), false);
                forceHangUpHandler.put(linkMicItemDataBean.getLinkMicId(), linkMicItemDataBean);
            }
        }
    }

    @Override
    public void controlUserLinkMicInLinkMicList(int position, PLVStreamerControlLinkMicAction action) {
        if (position < 0 || position >= streamerList.size()) {
            return;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = streamerList.get(position);
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
        if (item != null) {
            controlUserLinkMic(item.first, action);
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
        streamerLinkMicMsgHandler.closeAllUserLinkMic(liveRoomDataManager.getSessionId(), null);
        for (Map.Entry<String, PLVLinkMicItemDataBean> linkMicItemDataBeanEntry : rtcJoinMap.entrySet()) {
            String linkMicId = linkMicItemDataBeanEntry.getKey();
            if (!isMyLinkMicId(linkMicId) && !PLVEventHelper.isSpecialType(linkMicItemDataBeanEntry.getValue().getUserType())) {
                forceHangUpHandler.put(linkMicId, linkMicItemDataBeanEntry.getValue());
            }
        }
    }

    @Override
    public void muteAllUserAudio(final boolean isMute) {
        synchronized (memberList) {
            for (int i = 0; i < memberList.size(); i++) {
                @Nullable final PLVLinkMicItemDataBean linkMicItemDataBean = memberList.get(i).getLinkMicItemDataBean();
                if (linkMicItemDataBean != null && linkMicItemDataBean.isRtcJoinStatus()) {
                    if (!isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
                        muteUserMedia(i, false, isMute);
                    }
                }
            }
        }
    }

    @Override
    public void requestMemberList() {
        requestListUsersApi();
    }

    @Override
    public Disposable searchMemberList(String nick) {
        return PLVChatApiRequestHelper.searchUsers(liveRoomDataManager.getConfig().getChannelId(), nick)
                .subscribe(new Consumer<List<PLVSocketUserBean>>() {

                    @Override
                    public void accept(List<PLVSocketUserBean> plvSocketUserBeans) throws Exception {
                        memberSearchList.clear();
                        for (PLVSocketUserBean plvSocketUserBean : plvSocketUserBeans) {
                            PLVMemberItemDataBean memberItemDataBean;
                            Pair<Integer, PLVMemberItemDataBean> dataBeanPair = getMemberItemWithUserId(plvSocketUserBean.getUserId());
                            if (dataBeanPair != null) {
                                memberItemDataBean = dataBeanPair.second;
                            } else {
                                memberItemDataBean = new PLVMemberItemDataBean();
                                memberItemDataBean.setSocketUserBean(plvSocketUserBean);
                            }
                            memberSearchList.add(memberItemDataBean);
                        }
                        SortMemberListUtils.sort(memberSearchList);
                        callbackToView(new ViewRunnable() {
                            @Override
                            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                                view.onUpdateMemberSearchListData(memberSearchList);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    @Override
    public int getPositionByMemberItem(PLVMemberItemDataBean memberItem) {
        if (memberItem == null || memberItem.getSocketUserBean() == null) {
            return -1;
        }
        Pair<Integer, PLVMemberItemDataBean> dataBeanPair = getMemberItemWithUserId(memberItem.getSocketUserBean().getUserId());
        if (dataBeanPair != null) {
            return dataBeanPair.first;
        } else {
            memberList.add(memberItem);
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onUpdateMemberListData(memberList);
                }
            });
            return memberList.size() - 1;
        }
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
                    final String msg = PLVAppUtils.getString(R.string.plv_linkmic_guest_join_timeout);
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
    }

    @Override
    public void guestSendJoinRequest() {
        final Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(streamerManager.getLinkMicUid());
        final boolean fastResponse = memberItem != null && memberItem.second != null && memberItem.second.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION;
        if (fastResponse) {
            PLVLinkMicEventSender.getInstance().sendJoinAnswerEvent();
        } else {
            if (memberItem != null && memberItem.second != null) {
                memberItem.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP);
            }
            PLVLinkMicEventSender.getInstance().sendJoinRequestMsg();
        }
    }

    @Override
    public void guestSendLeaveLinkMic() {
        PLVLinkMicEventSender.getInstance().sendJoinLeaveMsg(liveRoomDataManager.getSessionId());
        final Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(streamerManager.getLinkMicUid());
        if (memberItem != null && memberItem.second != null) {
            memberItem.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.IDLE);
        }
    }

    @Override
    public void setUserPermissionSpeaker(final String userId, final boolean isSetPermission, final Ack ack) {
        if (!PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER) &&
                !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_GRANTED_SPEAKER_USER)) {
            //只有讲师/被授予主讲权限的嘉宾可以控制主讲权限
            return;
        }
        if (currentSocketUserBean == null) {
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
                    // 切换用户到第一画面
                    PLVLinkMicEventSender.getInstance().setSwitchFirstView(currentSpeakerPermissionUser, null);
                } else {
                    //3、收回权限时，把第一画面重新交给频道主讲
                    currentSpeakerPermissionUser.setUserId(findChannelTeacherUserId());
                    if (currentSpeakerPermissionUser.getUserId() != null) {
                        PLVLinkMicEventSender.getInstance().setSpeakerPermission(currentSpeakerPermissionUser, sessionId, true, ack);
                    } else if (ack != null) {
                        ack.call(objects);
                    }
                    if (newPermissionUserId.equals(lastFirstScreenUserId)) {
                        // 切换主讲到第一画面
                        PLVLinkMicEventSender.getInstance().setSwitchFirstView(currentSpeakerPermissionUser, null);
                    }
                }
            }
        });

    }

    @Override
    public void setUserFirstView(String userId, boolean isSetFirstView, final Ack ack) {
        if (!PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER)) {
            //只有讲师可以控制第一画面
            return;
        }
        if (currentSocketUserBean == null) {
            return;
        }

        final PLVSocketUserBean firstViewUser = new PLVSocketUserBean();
        if (isSetFirstView) {
            firstViewUser.setUserId(userId);
        } else {
            firstViewUser.setUserId(currentSocketUserBean.getUserId());
        }

        PLVLinkMicEventSender.getInstance().setSwitchFirstView(firstViewUser, new Ack() {
            @Override
            public void call(Object... args) {
                if (ack != null) {
                    ack.call(args);
                }
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
    public void setPushDowngradePreference(@NonNull PLVPushDowngradePreference pushDowngradePreference) {
        streamerData.postDowngradePreference(pushDowngradePreference);
        streamerManager.setPushDowngradePreference(pushDowngradePreference);
    }

    @Nullable
    @Override
    public PLVPushDowngradePreference getPushDowngradePreference() {
        return streamerManager.getPushDowngradePreference();
    }

    @Override
    public void answerLinkMicInvitation(boolean accept, boolean isTimeout, boolean openCamera, boolean openMicrophone) {
        if (accept) {
            enableLocalVideo(openCamera);
            enableRecordingAudioVolume(openMicrophone);
        }
        PLVLinkMicEventSender.getInstance().sendJoinAnswerEvent(accept);
    }

    @Override
    public void getJoinAnswerTimeLeft(PLVSugarUtil.Consumer<Integer> callback) {
        PLVLinkMicEventSender.getInstance().getJoinAnswerTimeLeft(streamerManager.getLinkMicUid(), callback);
    }

    @Override
    public int countLinkMicUser(@Nullable final List<String> userTypes) {
        return PLVSequenceWrapper.wrap(rtcJoinMap.values())
                .filter(new Function1<PLVLinkMicItemDataBean, Boolean>() {
                    @Override
                    public Boolean invoke(PLVLinkMicItemDataBean itemDataBean) {
                        if (userTypes == null || userTypes.isEmpty()) {
                            return true;
                        }
                        return userTypes.contains(itemDataBean.getUserType());
                    }
                })
                .toMutableList().size();
    }

    @Override
    public void setDenoiseType(PLVLinkMicDenoiseType denoiseType) {
        streamerData.postDenoiseType(denoiseType);
        streamerManager.setDenoiseType(denoiseType);
        denoiseTypeKV.set(denoiseType);
    }

    @Override
    public void setIsUseExternalAudioInput(boolean isUseExternalAudioInput) {
        streamerData.postUseExternalAudioInput(isUseExternalAudioInput);
        streamerManager.setExternalAudioDeviceInput(isUseExternalAudioInput);
        isUseExternalAudioInputKV.set(isUseExternalAudioInput);
    }

    @Override
    public void setWatermark(Bitmap var1, float var3, float var4, float var5) {
        streamerManager.setWatermark(var1, var3, var4, var5);
        watermarkBitmap = var1;
        tryAttachWaterToRenderParent();
    }

    @Override
    public void setVirtualBackground(Bitmap bitmap, boolean onlyBlurBackground) {
        streamerManager.setVirtualBackground(bitmap, onlyBlurBackground);
    }

    @Override
    public IPLVMediaPlayer createMediaOverlay() {
        return streamerManager.createMediaOverlay();
    }

    @Override
    public void setMediaOverlayDisplayRect(@NonNull RectF displayRect) {
        streamerManager.setMediaOverlayDisplayRect(displayRect);
    }

    @Override
    public void setMediaOverlayVolume(int remoteVolume) {
        streamerManager.setMediaOverlayVolume(remoteVolume);
        streamerData.postMediaOverlayRemoteVolume(remoteVolume);
    }

    @Override
    public void removeMediaOverlay(IPLVMediaPlayer mediaOverlay) {
        streamerManager.removeMediaOverlay(mediaOverlay);
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
        forceHangUpHandler.destroy();

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
            closeLinkMic(null);
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
            public void onNetworkQuality(final PLVLinkMicConstant.NetworkQuality quality) {
                streamerData.postNetworkQuality(quality);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onNetworkQuality(quality);
                    }
                });
                if (quality == PLVLinkMicConstant.NetworkQuality.DISCONNECT) {
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
                final Pair<Integer, PLVLinkMicItemDataBean> pair = getLinkMicItemWithLinkMicId(uid);
                if (pair == null) {
                    return;
                }
                final Integer position = pair.first;
                final PLVLinkMicItemDataBean itemDataBean = pair.second;
                if (position == null || itemDataBean == null) {
                    return;
                }
                if (streamType == PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN) {
                    itemDataBean.setScreenShare(true);
                    itemDataBean.setScreenShareInScreenStream(true);
                    updateMixLayoutWhenScreenShare(true, uid);
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onScreenShareChange(position, true, IPLVScreenShareListener.PLV_SCREEN_SHARE_OK, uid, isMyLinkMicId(uid));
                        }
                    });
                }
            }

            @Override
            public void onRemoteStreamClose(final String uid, int streamType) {
                final Pair<Integer, PLVLinkMicItemDataBean> pair = getLinkMicItemWithLinkMicId(uid);
                if (pair == null) {
                    return;
                }
                final Integer position = pair.first;
                final PLVLinkMicItemDataBean itemDataBean = pair.second;
                if (position == null || itemDataBean == null) {
                    return;
                }
                if (streamType == PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN) {
                    itemDataBean.setScreenShare(false);
                    itemDataBean.setScreenShareInScreenStream(false);
                    updateMixLayoutWhenScreenShare(false, uid);
                    callbackToView(new ViewRunnable() {
                        @Override
                        public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                            view.onScreenShareChange(position, false, IPLVScreenShareListener.PLV_SCREEN_SHARE_OK, uid, isMyLinkMicId(uid));
                        }
                    });
                }
            }

            @Override
            public void onUpstreamNetworkStatus(final PLVNetworkStatusVO networkStatusVO) {
                streamerData.postNetworkStatus(networkStatusVO);
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUpstreamNetworkStatus(networkStatusVO);
                    }
                });
            }
        });

        //推流开始
        streamerManager.addOnLiveStreamingStartListener(new IPLVStreamerOnLiveStreamingStartListener() {
            @Override
            public void onLiveStreamingStart() {
                streamerStatus = STREAMER_STATUS_START_SUCCESS;
                streamerData.postStreamerStatus(true);
                liveRoomDataManager.getStreamerStatusLiveData().postValue( true);
                streamerLinkMicMsgHandler.changeLinkMicType(streamerLinkMicMsgHandler.isVideoLinkMic());
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
            loginRoomId = liveRoomDataManager.getConfig().getChannelId();//socket未登录时，使用频道号
        }
        final String roomId = loginRoomId;
        listUsersDisposable = PLVChatApiRequestHelper.getListUsers(roomId, DEFAULT_MEMBER_PAGE, DEFAULT_MEMBER_LENGTH)
                .retryWhen(new PLVRxBaseRetryFunction(Integer.MAX_VALUE, 3000))
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO plvsListUsersVO) throws Exception {
                        generateMemberListWithListUsers(plvsListUsersVO.getUserlist(), true);
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
                    }
                })
                .compose(new PLVRxBaseTransformer<PLVListUsersVO, PLVListUsersVO>())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO plvsListUsersVO) throws Exception {
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
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<PLVListUsersVO>() {
                    @Override
                    public void accept(PLVListUsersVO plvsListUsersVO) throws Exception {
                        generateMemberListWithListUsers(plvsListUsersVO.getUserlist(), false);
                        //更新聊天室在线人数
                        PLVChatroomManager.getInstance().setOnlineCount(plvsListUsersVO.getCount());
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
                memberItemDataBean.setLinkMicStatus(item.second.getLinkMicStatus());
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
        memberList.clear();
        memberList.addAll(tempMemberList);
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
            if (isUpdateJoiningStatus && memberItemDataBean.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.IDLE) {
                memberItemDataBean.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP);
            }
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
                if (isUpdateJoiningStatus && !isWaitStatus) {
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
                    linkMicItemDataBean.setLinkMicStartTimestamp(System.currentTimeMillis());
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
                    callOnUsersJoin(Collections.singletonList(linkMicItemDataBean));
                }
                break;
            }
        }
        return hasChangedMemberList;
    }

    void updateMemberListWithJoin(final String linkMicUid) {
        if (!rtcJoinMap.containsKey(linkMicUid)) {
            PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
            linkMicItemDataBean.setLinkMicId(linkMicUid);
            rtcJoinMap.put(linkMicUid, linkMicItemDataBean);
        }
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(linkMicUid);
        if (item != null && item.second != null) {
            boolean result = updateMemberListLinkMicStatusWithRtcJoinList(item.second, linkMicUid);
            if (result) {
                callUpdateSortMemberList();
            }
        }
    }

    void normalCloseUserLinkMic(final String linkMicUid, boolean isRTCCall) {
        streamerLinkMicMsgHandler.hangUpLinkMic(linkMicUid, null);
        updateMemberListWithLeave(linkMicUid, isRTCCall);
    }

    void updateMemberListWithLeave(final String linkMicUid, boolean isRTCCall) {
        if (isRTCCall) {
            rtcJoinMap.remove(linkMicUid);
            forceHangUpHandler.remove(linkMicUid);
        }
        Pair<Integer, PLVMemberItemDataBean> item = getMemberItemWithUserId(linkMicUid);
        if (lastFirstScreenUserId != null && lastFirstScreenUserId.equals(linkMicUid)) {
            onFirstScreenChange(findChannelTeacherUserId(), true);
        }
        if (item != null && item.second != null) {
            item.second.setLinkMicStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
            if (item.second.getLinkMicItemDataBean() != null) {
                item.second.getLinkMicItemDataBean().setFirstScreen(false);
            }
            callUpdateSortMemberList();
        }
        final Pair<Integer, PLVLinkMicItemDataBean> linkMicItem = getLinkMicItemWithLinkMicId(linkMicUid);
        if (linkMicItem != null) {
            streamerList.remove(linkMicItem.second);
            callOnUsersLeave(Collections.singletonList(linkMicItem.second));
            updateMixLayoutUsers();
            updateLinkMicCount();
        }
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
            mixUser.setScreenShare(plvLinkMicItemDataBean.isScreenShareInScreenStream());
            mixUser.setUserId(plvLinkMicItemDataBean.getLinkMicId());
            mixUser.setNickname(plvLinkMicItemDataBean.getNick());
            //屏幕共享时，当前用户的混流摄像头不能为mute，否则CDN混流无画面
            if (plvLinkMicItemDataBean.isScreenShare()) {
                mixUser.setMuteVideo(false);
            } else {
                mixUser.setMuteVideo(plvLinkMicItemDataBean.isMuteVideo());
            }
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
            mixUser.setScreenShare(plvLinkMicItemDataBean.isScreenShareInScreenStream());
            //屏幕共享时，当前用户的混流摄像头不能为mute，否则CDN混流无画面
            if (plvLinkMicItemDataBean.isScreenShare()) {
                mixUser.setMuteVideo(false);
            } else {
                mixUser.setMuteVideo(plvLinkMicItemDataBean.isMuteVideo());
            }
            mixUser.setUserId(plvLinkMicItemDataBean.getLinkMicId());
            mixUser.setNickname(plvLinkMicItemDataBean.getNick());
            mixUserList.add(mixUser);
        }
        streamerManager.updateMixLayoutUsers(mixUserList);
    }

    void updateLinkMicCount() {
        streamerData.postLinkMicCount(streamerList.size());
    }

    private void updateUserPermissionStatus(PLVLinkMicItemDataBean linkMicItemDataBean, PLVJoinInfoEvent joinInfoEvent) {
        Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(linkMicItemDataBean.getLinkMicId());
        if (memberItem != null && memberItem.second != null && memberItem.second.getLinkMicItemDataBean() != null) {
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
        synchronized (memberList) {
            for (PLVMemberItemDataBean plvMemberItemDataBean : memberList) {
                PLVLinkMicItemDataBean linkMicItemDataBean = plvMemberItemDataBean.getLinkMicItemDataBean();
                if (linkMicItemDataBean == null
                        || linkMicItemDataBean.getStatus() == PLVLinkMicItemDataBean.LinkMicStatus.IDLE
                        || linkMicItemDataBean.getStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP
                        || linkMicItemDataBean.getStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION
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
            callOnUsersLeave(willRemoveStreamerList);
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
            // 默认关闭摄像头
            linkMicItemDataBean.setMuteVideo(true);
        }
        if (rtcJoinLinkMicItem.getMuteAudioInRtcJoinList() != null) {
            //如果之前有保存过连麦用户媒体的状态，则使用
            linkMicItemDataBean.setMuteAudio(rtcJoinLinkMicItem.isMuteAudio());
        } else {
            // 默认静音
            linkMicItemDataBean.setMuteAudio(true);
        }
    }

    private void startJoinTimeoutCount(final PLVLinkMicItemDataBean linkMicItemDataBean) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                linkMicItemDataBean.setStatusMethodCallListener(null);
                linkMicItemDataBean.setStatus(PLVLinkMicItemDataBean.STATUS_IDLE);
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
                    callOnUsersLeave(streamerList);
                    callUpdateGuestStatus(false);
                }
                streamerData.postStreamerStatus(isLive);
                liveRoomDataManager.getStreamerStatusLiveData().postValue(isLive);
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
        PLVPushStreamTemplateJsonBean pushStreamTemplateJsonBean = PLVStreamerConfig.getPushStreamTemplate(liveRoomDataManager.getConfig().getChannelId());
        int maxSettingBitrate = PLVStreamerConfig.Bitrate.BITRATE_SUPER_HIGH;
        String key = "plv_key_bitrate";
        if (pushStreamTemplateJsonBean != null && pushStreamTemplateJsonBean.isEnabled()) {
            maxSettingBitrate = pushStreamTemplateJsonBean.getVideoParams().size();
            key = "plv_key_template_bitrate";
        }
        if (curBitrate < PLVStreamerConfig.Bitrate.BITRATE_STANDARD
                || curBitrate > maxSettingBitrate) {
            // invalid bitrate data
            return;
        }
        SPUtils.getInstance().put(key, curBitrate);
    }

    private int loadBitrate() {
        PLVPushStreamTemplateJsonBean pushStreamTemplateJsonBean = PLVStreamerConfig.getPushStreamTemplate(liveRoomDataManager.getConfig().getChannelId());
        int maxSettingBitrate = PLVStreamerConfig.Bitrate.BITRATE_SUPER_HIGH;
        int defaultSettingBitrate = PLVStreamerConfig.Bitrate.BITRATE_SUPER;
        String key = "plv_key_bitrate";
        if (pushStreamTemplateJsonBean != null && pushStreamTemplateJsonBean.isEnabled()) {
            maxSettingBitrate = pushStreamTemplateJsonBean.getVideoParams().size();
            String defaultQualityLevel = PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER) ? pushStreamTemplateJsonBean.getTeacherDefaultQualityLevel() : pushStreamTemplateJsonBean.getGuestDefaultQualityLevel();
            defaultSettingBitrate = PLVStreamerConfig.QualityLevel.getBitrateByLevel(defaultQualityLevel);
            key = "plv_key_template_bitrate";
        }
        int bitrate = SPUtils.getInstance().getInt(key, defaultSettingBitrate);
        if (bitrate < PLVStreamerConfig.Bitrate.BITRATE_STANDARD) {
            bitrate = PLVStreamerConfig.Bitrate.BITRATE_STANDARD;
        }
        if (bitrate > maxSettingBitrate) {
            bitrate = maxSettingBitrate;
        }
        return bitrate;
    }

    private void saveMixLayoutType() {
        final String channelId = liveRoomDataManager.getConfig().getChannelId();
        Map<String, PLVStreamerConfig.MixLayoutType> map = CHANNEL_MIX_LAYOUT_TYPE_KV.get();
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(channelId, mixLayoutType);
        CHANNEL_MIX_LAYOUT_TYPE_KV.set(map);
    }

    private void initMixLayoutType() {
        final String channelId = liveRoomDataManager.getConfig().getChannelId();
        // 从本地读取
        PLVStreamerConfig.MixLayoutType targetMixLayoutType = loadLocalMixLayoutType(channelId);
        if (targetMixLayoutType != null) {
            mixLayoutType = targetMixLayoutType;
            return;
        }
        // 从后台配置读取
        targetMixLayoutType = PLVChannelFeatureManager.onChannel(channelId).get(PLVChannelFeature.STREAMER_CHANNEL_DEFAULT_MIX_LAYOUT_TYPE);
        if (targetMixLayoutType != null) {
            mixLayoutType = targetMixLayoutType;
            saveMixLayoutType();
            return;
        }
        // 默认配置
        targetMixLayoutType = PLVStreamerConfig.MixLayoutType.getDefaultMixLayoutType(channelType);
        mixLayoutType = targetMixLayoutType;
        saveMixLayoutType();
    }

    @Nullable
    private PLVStreamerConfig.MixLayoutType loadLocalMixLayoutType(String channelId) {
        Map<String, PLVStreamerConfig.MixLayoutType> map = CHANNEL_MIX_LAYOUT_TYPE_KV.get();
        if (map == null || !map.containsKey(channelId)) {
            return null;
        }
        return map.get(channelId);
    }

    private void initNewLinkMicStrategyDefaultType() {
        if (liveRoomDataManager == null || streamerLinkMicMsgHandler == null) {
            return;
        }
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        if (!isNewLinkMicStrategy) {
            return;
        }
        final boolean isDefaultVideo = "video".equals(PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).get(PLVChannelFeature.LIVE_NEW_LINKMIC_DEFAULT_TYPE));
        changeLinkMicType(isDefaultVideo);
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

    private void callOnUsersJoin(final List<PLVLinkMicItemDataBean> dataBeanList) {
        callbackToView(new ViewRunnable() {

            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onUsersJoin(dataBeanList);
            }
        });
        tryCallHasLinkMicUser();
    }

    private void callOnUsersLeave(final List<PLVLinkMicItemDataBean> dataBeanList) {
        callbackToView(new ViewRunnable() {

            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onUsersLeave(dataBeanList);
            }
        });
        tryCallHasLinkMicUser();
    }

    private void tryCallHasLinkMicUser() {
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                view.onHasLinkMicUser(streamerList.size() > 1);
            }
        });
        if (streamerList.size() > 1) {
            tryAttachWaterToRenderParent();
        } else {
            tryDetachWaterFromRenderParent(false);
        }
    }

    private void tryAttachWaterToRenderParent() {
        if (watermarkBitmap != null && myRenderView != null && streamerList.size() > 1) {
            ViewGroup parent = (ViewGroup) myRenderView.getParent();
            if (parent != null) {
                boolean hasWaterMark = false;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    if (child instanceof ImageView && child.getTag() != null && child.getTag().equals(watermarkBitmap)) {
                        hasWaterMark = true;
                        break;
                    }
                }
                if (!hasWaterMark) {
                    ImageView imageView = new ImageView(myRenderView.getContext());
                    imageView.setImageBitmap(watermarkBitmap);
                    imageView.setTag(watermarkBitmap);
                    parent.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    myRenderViewParent = parent;
                }
            }
        }
    }

    private void tryDetachWaterFromRenderParent(boolean force) {
        if (watermarkBitmap != null && myRenderView != null && (force || streamerList.size() <= 1)) {
            ViewGroup parent = (ViewGroup) myRenderView.getParent(); // always null
            if (parent == null) {
                parent = myRenderViewParent;
            }
            if (parent != null) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    if (child instanceof ImageView && child.getTag() != null && child.getTag().equals(watermarkBitmap)) {
                        parent.removeView(child);
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    private String findChannelTeacherUserId() {
        synchronized (memberList) {
            for (PLVMemberItemDataBean memberItemDataBean : memberList) {
                if (PLVSocketUserConstant.USERTYPE_TEACHER.equals(memberItemDataBean.getSocketUserBean().getUserType())) {
                    return memberItemDataBean.getSocketUserBean().getUserId();
                }
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
        Pair<Integer, PLVMemberItemDataBean> pair = getMemberItemByOnlyLinkMicId(linkMicId);
        if (pair != null) {
            return pair;
        }
        return getMemberItemByOnlyUserId(linkMicId);
    }

    Pair<Integer, PLVMemberItemDataBean> getMemberItemWithUserId(String userId) {
        Pair<Integer, PLVMemberItemDataBean> pair = getMemberItemByOnlyUserId(userId);
        if (pair != null) {
            return pair;
        }
        return getMemberItemByOnlyLinkMicId(userId);
    }

    private Pair<Integer, PLVMemberItemDataBean> getMemberItemByOnlyLinkMicId(String linkMicId) {
        synchronized (memberList) {
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
        }
        return null;
    }

    private Pair<Integer, PLVMemberItemDataBean> getMemberItemByOnlyUserId(String userId) {
        synchronized (memberList) {
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
        }
        return null;
    }

    void callUpdateSortMemberList() {
        SortMemberListUtils.sort(memberList);
        for (PLVMemberItemDataBean memberItemDataBean : memberSearchList) {
            Pair<Integer, PLVMemberItemDataBean> dataBeanPair = getMemberItemWithUserId(memberItemDataBean.getSocketUserBean().getUserId());
            if (dataBeanPair != null) {
                memberItemDataBean.syncMemberItemBean(dataBeanPair.second);
            }
        }
        SortMemberListUtils.sort(memberSearchList);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callbackToView(new ViewRunnable() {
                    @Override
                    public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                        view.onUpdateMemberListData(memberList);
                        view.onUpdateMemberSearchListData(memberSearchList);
                    }
                });
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
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

    void callUpdateGuestStatus(final boolean joinRTC) {
        if (isMyselfJoinRtc == joinRTC) {
            return;
        }
        isMyselfJoinRtc = joinRTC;
        if (joinRTC) {
            PLVLinkMicEventSender.getInstance().sendShowHandUpToTeacher(false);
            PLVLinkMicEventSender.getInstance().sendJoinSuccessMsg(liveRoomDataManager.getSessionId(), new IPLVLinkMicManager.OnSendJoinSuccessMsgListener() {
                @Override
                public void onSendJoinSuccessMsg(PLVLinkMicJoinSuccess joinSuccess) {
                    callUpdateGuestStatusInternal(true);
                }
            });
        } else {
            callUpdateGuestStatusInternal(false);
        }
    }

    private void callUpdateGuestStatusInternal(final boolean joinRTC) {
        String myLinkMicID = streamerManager.getLinkMicUid();
        int myIndex = 0;
        for (int i = 0; i < streamerList.size(); i++) {
            PLVLinkMicItemDataBean dataBean = streamerList.get(i);
            if (myLinkMicID.equals(dataBean.getLinkMicId())) {
                dataBean.setStatus(joinRTC ? PLVLinkMicItemDataBean.STATUS_RTC_JOIN : PLVLinkMicItemDataBean.STATUS_IDLE);
                if (joinRTC) {
                    dataBean.setLinkMicStartTimestamp(System.currentTimeMillis());
                }
                myIndex = i;
                break;
            }
        }
        final int finalMyIndex = myIndex;
        callbackToView(new ViewRunnable() {
            @Override
            public void run(@NonNull @NotNull IPLVStreamerContract.IStreamerView view) {
                view.onGuestRTCStatusChanged(finalMyIndex, joinRTC);
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

    void onInviteJoinLinkMic(@NonNull final PLVJoinResponseSEvent event) {
        if (event.getUser() == null || !isMyLinkMicId(event.getUser().getUserId())) {
            return;
        }
        final Pair<Integer, PLVMemberItemDataBean> memberItem = getMemberItemWithLinkMicId(event.getUser().getUserId());
        final boolean fastResponseAccept = memberItem != null && memberItem.second != null && memberItem.second.getLinkMicStatus() == PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_HAND_UP;
        if (fastResponseAccept) {
            PLVLinkMicEventSender.getInstance().sendJoinAnswerEvent();
        } else {
            if (memberItem != null && memberItem.second != null) {
                memberItem.second.setLinkMicStatus(PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION);
            }
            callbackToView(new ViewRunnable() {
                @Override
                public void run(@NonNull IPLVStreamerContract.IStreamerView view) {
                    view.onTeacherInviteMeJoinLinkMic(event);
                }
            });
        }
    }

    void onResponseJoinLinkMic(@NonNull final PLVJoinResponseSEvent event) {
        if (event.getUser() == null || !isMyLinkMicId(event.getUser().getUserId()) || !"1".equals(event.getValue())) {
            return;
        }

        streamerManager.switchRoleToBroadcaster();
        callUpdateGuestStatus(true);
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
                        PLVLinkMicConstant.NetworkQuality netQuality = streamerManager.getNetworkQuality();
                        if (netQuality != PLVLinkMicConstant.NetworkQuality.DISCONNECT) {
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
        private static final String SELF = "自己";// no need i18n
        private static final String REAL = "非虚拟";// no need i18n
        private static final String REAL_LINK_MIC_RTC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_RTC_JOIN;
        private static final String REAL_LINK_MIC_JOIN = REAL + PLVLinkMicItemDataBean.STATUS_JOIN;
        private static final String REAL_LINK_MIC_JOINING = REAL + PLVLinkMicItemDataBean.STATUS_JOINING;
        private static final String REAL_LINK_MIC_WAIT = REAL + PLVLinkMicItemDataBean.STATUS_WAIT;
        private static final String REAL_LINK_MIC_WAIT_INVITATION = REAL + PLVLinkMicItemDataBean.LinkMicStatus.WAIT_ACCEPT_INVITATION;
        private static final List<String> SORT_INDEX = Arrays.asList(
                SELF,
                PLVSocketUserConstant.USERTYPE_MANAGER,
                PLVSocketUserConstant.USERTYPE_TEACHER,
                PLVSocketUserConstant.USERTYPE_GUEST,
                PLVSocketUserConstant.USERTYPE_VIEWER,
                PLVSocketUserConstant.USERTYPE_ASSISTANT,
                REAL_LINK_MIC_WAIT,
                REAL_LINK_MIC_WAIT_INVITATION,
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
                return SELF;
            }
            if (!PLVSocketUserConstant.USERTYPE_MANAGER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_TEACHER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_GUEST.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_VIEWER.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(type)
                    && !PLVSocketUserConstant.USERTYPE_DUMMY.equals(type)) {
                switch (item.getLinkMicStatus()) {
                    case WAIT_ACCEPT_HAND_UP:
                        return REAL_LINK_MIC_WAIT;
                    case WAIT_ACCEPT_INVITATION:
                        return REAL_LINK_MIC_WAIT_INVITATION;
                    case JOINING:
                        return REAL_LINK_MIC_JOINING;
                    case JOIN:
                        return REAL_LINK_MIC_JOIN;
                    case RTC_JOIN:
                        return REAL_LINK_MIC_RTC_JOIN;
                    default:
                        return REAL;
                }
            }
            return type;
        }

        public static List<PLVMemberItemDataBean> sort(List<PLVMemberItemDataBean> memberList) {
            try {
                Collections.sort(memberList, new Comparator<PLVMemberItemDataBean>() {
                    @Override
                    public int compare(PLVMemberItemDataBean o1, PLVMemberItemDataBean o2) {
                        int io1 = SORT_INDEX.indexOf(getSortType(o1));
                        int io2 = SORT_INDEX.indexOf(getSortType(o2));
                        return io1 - io2;
                    }
                });
            } catch (Exception e) {
                PLVCommonLog.exception(e);
            }
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
