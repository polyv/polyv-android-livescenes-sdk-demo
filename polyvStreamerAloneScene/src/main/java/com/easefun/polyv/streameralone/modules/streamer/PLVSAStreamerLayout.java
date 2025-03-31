package com.easefun.polyv.streameralone.modules.streamer;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static java.lang.Math.min;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVMultiModeRecyclerViewLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.PLVFloatPermissionUtils;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.streamer.IPLVSStreamerManager;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.adapter.PLVSAStreamerAdapter;
import com.easefun.polyv.streameralone.modules.streamer.widget.screenshare.IPLVSAStreamerScreenShareFloatWindow;
import com.easefun.polyv.streameralone.modules.streamer.widget.screenshare.PLVSAStreamerScreenShareFloatWindowV1;
import com.easefun.polyv.streameralone.modules.streamer.widget.screenshare.PLVSAStreamerScreenShareFloatWindowV2;
import com.easefun.polyv.streameralone.scenes.PLVSAStreamerAloneActivity;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.screenshare.IPLVScreenShareListener;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.livescenes.streamer.IPLVStreamerManager;
import com.plv.livescenes.streamer.config.PLVStreamerConfig;
import com.plv.socket.event.linkmic.PLVJoinAnswerSEvent;
import com.plv.socket.event.linkmic.PLVJoinResponseSEvent;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ImageUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;

/**
 * 推流和连麦布局
 */
public class PLVSAStreamerLayout extends FrameLayout implements IPLVSAStreamerLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVSAStreamerLayout";

    // 本地麦克风音量大小达到该阈值时，提示当前正处于静音状态
    private static final int VOLUME_THRESHOLD_TO_NOTIFY_AUDIO_MUTED = 40;

    //直播间数管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //推流和连麦Presenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;
    //view
    private PLVMultiModeRecyclerViewLayout plvsaStreamerRvLayout;
    //one2more模式的主视图
    private View mainView;
    //占位View
    private View placeholderView;
    //adapter
    private PLVSAStreamerAdapter streamerAdapter;
    //listener
    private OnViewActionListener onViewActionListener;
    //是否进入直播间，用于嘉宾正式进入直播间的判断
    private boolean isEnterLive;
    //更新连麦Layout的runnable
    private Runnable updateLinkmicLayoutRunnable;
    //连麦列表布局管理
    private GridLayoutManager gridLayoutManager;
    // 邀请连麦布局
    private final PLVSALinkMicInvitationLayout linkMicInvitationLayout = new PLVSALinkMicInvitationLayout(getContext());

    //退出直播间弹窗
    private PLVConfirmDialog leaveLiveConfirmDialog;
    //返回home时的悬浮窗
    public final PLVSAStreamerScreenShareFloatWindowV1 floatWindow = new PLVSAStreamerScreenShareFloatWindowV1(getContext());
    public final PLVSAStreamerScreenShareFloatWindowV2 floatWindowV2 = new PLVSAStreamerScreenShareFloatWindowV2(getContext());

    // 连麦时显示的背景图
    private ImageView linkMicBgIv;

    private boolean isLocalAudioMuted = false;
    private long lastNotifyLocalAudioMutedTimestamp;
    //是否显示悬浮窗请求弹窗
    private boolean isShowWindowPermissionDialog = true;
    private PLVLinkMicConstant.PushResolutionRatio currentResolutionRatio;

    private PLVUserAbilityManager.OnUserRoleChangedListener onSpeakerRoleChangedListener;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAStreamerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAStreamerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAStreamerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_streamer_layout, this, true);
        setClickable(false);

        plvsaStreamerRvLayout = findViewById(R.id.plvsa_streamer_rv_layout);
        linkMicBgIv = findViewById(R.id.plvsa_streamer_bg_iv);

        //init RecyclerView
        PLVNoInterceptTouchRecyclerView plvsaStreamerRv = plvsaStreamerRvLayout.getRecyclerView();
        plvsaStreamerRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // ignore IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
                }
            }
        });
        //禁用RecyclerView默认动画
        plvsaStreamerRv.getItemAnimator().setAddDuration(0);
        plvsaStreamerRv.getItemAnimator().setChangeDuration(0);
        plvsaStreamerRv.getItemAnimator().setMoveDuration(0);
        plvsaStreamerRv.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = plvsaStreamerRv.getItemAnimator();
        if (rvAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
        }

        gridLayoutManager = new GridLayoutManager(getContext(), 1) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // ignore IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
                }
            }
        };
        plvsaStreamerRv.setLayoutManager(gridLayoutManager);

        //启动前台服务，防止在后台被杀
        PLVForegroundService.startForegroundService(PLVSAStreamerAloneActivity.class, PLVAppUtils.getString(R.string.plvsa_streamer_alone_scene_name), R.drawable.plvsa_ic_launcher);
        //防止自动息屏、锁屏
        setKeepScreenOn(true);

        initLinkMicInvitationLayout();
        observeSpeakerPermissionChange();
    }

    private void initLinkMicInvitationLayout() {
        linkMicInvitationLayout.setOnViewActionListener(new PLVSALinkMicInvitationLayout.OnViewActionListener() {

            @Override
            public void answerLinkMicInvitation(boolean accept, int cancelBy, boolean openCamera, boolean openMicrophone) {
                streamerPresenter.answerLinkMicInvitation(accept, cancelBy == PLVSALinkMicInvitationLayout.CANCEL_BY_TIMEOUT, openCamera, openMicrophone);
            }

            @Override
            public void asyncGetAcceptInvitationLeftTimeInSecond(PLVSugarUtil.Consumer<Integer> callback) {
                streamerPresenter.getJoinAnswerTimeLeft(callback);
            }

            @Override
            public void requestChangeCameraFocus(boolean requestFocus) {
                streamerPresenter.enableLocalVideoCapture(!requestFocus);
            }

        });
    }

    private void observeSpeakerPermissionChange() {
        this.onSpeakerRoleChangedListener = new PLVUserAbilityManager.OnUserRoleChangedListener() {
            @Override
            public void onUserRoleAdded(PLVUserRole role) {
                if (role == PLVUserRole.STREAMER_GRANTED_SPEAKER_USER) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_grant_speaker_permission)
                            .show();
                }
            }

            @Override
            public void onUserRoleRemoved(PLVUserRole role) {
                if (role == PLVUserRole.STREAMER_GRANTED_SPEAKER_USER) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_streamer_remove_speaker_permission)
                            .show();
                }
            }
        };
        PLVUserAbilityManager.myAbility().addUserRoleChangeListener(new WeakReference<>(onSpeakerRoleChangedListener));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSAStreamerLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        //嘉宾登录时，需要先跳过自动连麦，直到正式进入直播间后才允许自动连麦
        if (isGuest()) {
            liveRoomDataManager.getConfig().setSkipAutoLinkMic(true);
        }

        //init adapter
        streamerAdapter = new PLVSAStreamerAdapter(plvsaStreamerRvLayout.getRecyclerView(), liveRoomDataManager, new PLVSAStreamerAdapter.OnStreamerAdapterCallback() {
            @Override
            public View createLinkMicRenderView() {
                View renderView = streamerPresenter.createTextureRenderView(getContext());
                if (renderView == null) {
                    renderView = streamerPresenter.createRenderView(getContext());
                }
                return renderView;
            }

            @Override
            public void releaseLinkMicRenderView(View renderView) {
                streamerPresenter.releaseRenderView(renderView);
            }

            @Override
            public void setupRenderView(View renderView, String linkMicId) {
                streamerPresenter.setupRenderView(renderView, linkMicId);
            }

            @Override
            public void onMicControl(int position, boolean isMute) {
                if (streamerPresenter != null) {
                    streamerPresenter.muteUserMediaInLinkMicList(position, false, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (streamerPresenter != null) {
                    streamerPresenter.muteUserMediaInLinkMicList(position, true, isMute);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
                if (streamerPresenter != null) {
                    streamerPresenter.controlUserLinkMicInLinkMicList(position, action);
                }
            }

            @Override
            public void onGrantUserSpeakerPermission(int position, final PLVLinkMicItemDataBean user, final boolean isGrant) {
                if (streamerPresenter == null || user == null) {
                    return;
                }
                streamerPresenter.setUserPermissionSpeaker(user.getUserId(), isGrant, new Ack() {
                    @Override
                    public void call(Object... objects) {
                        final boolean isGuestTransferPermission = !PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER);
                        final String text;
                        if (!isGrant) {
                            text = PLVAppUtils.getString(R.string.plv_streamer_remove_speaker_permission_2);
                        } else if (isGuestTransferPermission) {
                            text = PLVAppUtils.getString(R.string.plv_streamer_change_speaker_permission);
                        } else {
                            text = PLVAppUtils.getString(R.string.plv_streamer_grant_speaker_permission_2);
                        }
                        PLVToast.Builder.context(getContext())
                                .setText(text)
                                .show();
                    }
                });
            }

            @Override
            public void onControlFullScreen(int position, PLVLinkMicItemDataBean itemDataBean, PLVSwitchViewAnchorLayout view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onFullscreenAction(itemDataBean, view);
                }
            }

            @Override
            public void onStreamerViewScale(PLVLinkMicItemDataBean itemDataBean, float scaleFactor) {
                scaleStreamerView(itemDataBean, scaleFactor);
            }
        });

        final boolean isDefaultBackCamera = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .getOrDefault(PLVChannelFeature.STREAMER_ALONE_DEFAULT_BACK_CAMERA, false);

        streamerPresenter = new PLVStreamerPresenter(liveRoomDataManager);
        streamerPresenter.registerView(streamerView);
        streamerPresenter.setCameraDirection(!isDefaultBackCamera);
        streamerPresenter.init();
        streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);

        //初始化悬浮窗
        streamerPresenter.registerView(floatWindow.getStreamerView());
        streamerPresenter.registerView(floatWindowV2.streamerView);

        linkMicInvitationLayout.setIsOnlyAudio(liveRoomDataManager.isOnlyAudio());

        observePushResolutionRatio();

        observeData();

        updateOnOrientationChanged(PLVScreenUtils.isLandscape(getContext()));
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return floatWindowV2.chatroomView;
    }

    @Override
    public boolean setCameraDirection(boolean front) {
        return streamerPresenter.setCameraDirection(front);
    }

    @Override
    public void setMirrorMode(boolean isMirror) {
        streamerPresenter.setFrontCameraMirror(isMirror);
    }

    @Override
    public void scaleStreamerView(PLVLinkMicItemDataBean itemDataBean, float scaleFactor) {
        if (streamerPresenter == null || itemDataBean == null) {
            return;
        }
        if (!isMyselfUserId(itemDataBean.getLinkMicId())) {
            return;
        }
        streamerPresenter.zoomLocalCamera(scaleFactor);
    }

    @Override
    public void changeLinkMicLayoutType() {
        if (plvsaStreamerRvLayout == null) {
            return;
        }
        if (plvsaStreamerRvLayout.getCurrentMode() == PLVMultiModeRecyclerViewLayout.MODE_TILED) {
            changeToOneToMore();
        } else {
            changeToTiled();
        }
    }

    @Override
    public void setBitrate(int bitrate) {
        streamerPresenter.setBitrate(bitrate);
    }

    @Override
    public void setMixLayoutType(PLVStreamerConfig.MixLayoutType mixLayout) {
        streamerPresenter.setMixLayoutType(mixLayout);
    }

    @Override
    public Pair<Integer, Integer> getBitrateInfo() {
        return new Pair<>(streamerPresenter.getMaxBitrate(), streamerPresenter.getBitrate());
    }

    @Override
    public PLVStreamerConfig.MixLayoutType getMixLayoutType() {
        return streamerPresenter.getMixLayoutType();
    }

    @Override
    public PLVLinkMicConstant.NetworkQuality getNetworkQuality() {
        return streamerPresenter.getNetworkQuality();
    }

    @Override
    public boolean isStreamerStartSuccess() {
        return streamerPresenter.getStreamerStatus() == PLVStreamerPresenter.STREAMER_STATUS_START_SUCCESS;
    }

    @Override
    public void addOnShowNetBrokenListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getShowNetBroken().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnUserRequestListener(IPLVOnDataChangedListener<String> listener) {
        streamerPresenter.getData().getUserRequestData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnIsFrontCameraListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getIsFrontCamera().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnIsFrontMirrorModeListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getIsFrontMirrorMode().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addStreamerTimeListener(IPLVOnDataChangedListener<Integer> listener) {
        streamerPresenter.getData().getStreamerTime().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addLinkMicCountListener(IPLVOnDataChangedListener<Integer> listener) {
        streamerPresenter.getData().getLinkMicCount().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void startLive() {
        streamerAdapter.updateEnterLive(true);
        streamerPresenter.startLiveStream();
    }

    @Override
    public void stopLive() {
        streamerPresenter.stopLiveStream();
    }

    @Override
    public void enterLive() {
        isEnterLive = true;
        streamerAdapter.updateEnterLive(true);
        //嘉宾进入直播间
        streamerPresenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isStartedStatus) {
                if(isStartedStatus == null){
                    return;
                }
                if(isStartedStatus){
                    liveRoomDataManager.getConfig().setSkipAutoLinkMic(false);
                    streamerPresenter.guestTryJoinLinkMic();
                    plvsaStreamerRvLayout.setMainViewVisibility(View.GONE);
                    if(plvsaStreamerRvLayout.getCurrentMode() == PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER){
                        plvsaStreamerRvLayout.changeMode(PLVMultiModeRecyclerViewLayout.MODE_TILED);
                        streamerAdapter.setItemType(PLVSAStreamerAdapter.ITEM_TYPE_ONLY_TEACHER);
                    }
                } else {
                    changePlaceholder();
                    linkMicInvitationLayout.close();
                }
                updateLinkMicLayoutListOnChange();
            }
        });
    }

    @Override
    public IPLVStreamerContract.IStreamerPresenter getStreamerPresenter() {
        return streamerPresenter;
    }

    @Override
    public boolean onBackPressed() {
        if(isGuest()){
            showLeaveLiveConfirmLayout();
            return true;
        }
        return streamerAdapter.onBackPressed();
    }

    @Override
    public boolean onRvSuperTouchEvent(MotionEvent ev) {
        boolean returnResult = plvsaStreamerRvLayout.getRecyclerView().onSuperTouchEvent(ev);
        streamerAdapter.checkScaleCamera(ev);
        streamerAdapter.checkClickItemView(ev);
        return returnResult;
    }

    @Override
    public void destroy() {
        floatWindow.close();
        floatWindow.destroy();
        floatWindowV2.close();
        floatWindowV2.destroy();
        linkMicInvitationLayout.destroy();
        streamerPresenter.destroy();
        PLVForegroundService.stopForegroundService();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            super.onStreamerEngineCreatedSuccess(linkMicUid, linkMicList);
            streamerAdapter.setMyLinkMicId(linkMicUid);
            streamerAdapter.setDataList(linkMicList);
            updateLinkMicListLayout();
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteVideo(uid, mute, streamerListPos, memberListPos);
            streamerAdapter.updateUserMuteVideo(streamerListPos);
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteAudio(uid, mute, streamerListPos, memberListPos);
            streamerAdapter.updateUserMuteAudio(streamerListPos);

            if (isMyselfUserId(uid)) {
                isLocalAudioMuted = mute;
                if (!mute) {
                    lastNotifyLocalAudioMutedTimestamp = 0;
                }
            }
        }

        @Override
        public void onLocalUserMicVolumeChanged(int volume) {
            streamerAdapter.updateVolumeChanged();
            tryNotifyLocalAudioMuted(volume);
        }

        @Override
        public void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList) {
            super.onRemoteUserVolumeChanged(linkMicList);
            streamerAdapter.updateVolumeChanged();
        }

        @Override
        public void onUsersJoin(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersJoin(dataBeanList);
            // 仅竖屏才展示背景图
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                linkMicBgIv.setVisibility(View.VISIBLE);
            }
            streamerAdapter.updateAllItem();
            updateLinkMicLayoutListOnChange();
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);
            if (plvsaStreamerRvLayout.getCurrentMode() != PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER) {
                // 嘉宾布局一直有背景画面
                linkMicBgIv.setVisibility(View.GONE);
            }
            streamerAdapter.updateAllItem();
            updateLinkMicLayoutListOnChange();
        }

        @Override
        public void onGuestRTCStatusChanged(int pos, boolean isJoinRTC) {
            streamerAdapter.updateGuestStatus(pos);
            if (isJoinRTC) {
                linkMicInvitationLayout.close();
            }
        }

        @Override
        public void onStreamerError(final int errorCode, final Throwable throwable) {
            super.onStreamerError(errorCode, throwable);
            post(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == IPLVSStreamerManager.ERROR_PERMISSION_DENIED) {
                        String tips = throwable.getMessage();
                        AlertDialog dialog = new AlertDialog.Builder(getContext())
                                .setMessage(tips)
                                .setPositiveButton(R.string.plv_common_dialog_setting, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PLVFastPermission.getInstance().jump2Settings(getContext());
                                    }
                                })
                                .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        dialog.show();
                    } else if (errorCode == IPLVStreamerManager.ERROR_SLICE_START_FAIL) {
                        new PLVSAConfirmDialog(getContext())
                                .setTitle(R.string.plv_streamer_exception)
                                .setContent(PLVAppUtils.formatString(R.string.plv_streamer_exception_hint, errorCode + ""))
                                .setIsNeedLeftBtn(true)
                                .setCancelable(false)
                                .setLeftButtonText(R.string.plv_streamer_continue)
                                .setRightButtonText(R.string.plv_streamer_restart)
                                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        dialog.dismiss();
                                        if (onViewActionListener != null) {
                                            onViewActionListener.onRestartLiveAction();
                                        }
                                    }
                                })
                                .show();
                    } else {
                        new PLVSAConfirmDialog(getContext())
                                .setTitle(R.string.plv_streamer_exception)
                                .setContent(throwable.getMessage())
                                .setIsNeedLeftBtn(false)
                                .setCancelable(false)
                                .setRightButtonText(R.string.plv_streamer_restart)
                                .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, View v) {
                                        dialog.dismiss();
                                        if (onViewActionListener != null) {
                                            onViewActionListener.onRestartLiveAction();
                                        }
                                    }
                                })
                                .show();
                    }
                }
            });
        }

        @Override
        public void onSetPermissionChange(String type, boolean isGranted, boolean isCurrentUser, PLVSocketUserBean user) {
            super.onSetPermissionChange(type, isGranted, isCurrentUser, user);
            streamerAdapter.updatePermissionChange();


            if(PLVPPTAuthentic.PermissionType.TEACHER.equals(type)){
                if(user != null && !user.isTeacher()){
                    streamerAdapter.setHasSpeakerUser(isGranted ? user : null);
                }
                if(isCurrentUser) {
                    if (user != null && user.isTeacher()) {
                        return;
                    }
                    if (!isGranted && streamerPresenter.isScreenSharing()) {
                        //失去权限，停止屏幕共享
                        streamerPresenter.exitShareScreen();
                    }
                }
            } else if(PLVPPTAuthentic.PermissionType.SCREEN_SHARE.equals(type) && !isCurrentUser && user != null){
                //非当前用户的屏幕共享，需要提示状态
                String text;
                if(user.isTeacher()){
                    text = PLVAppUtils.getString(R.string.plv_live_publisher_default);
                } else {
                    text = user.getNick()+"";
                }
                text = text + getContext().getString(isGranted ? R.string.plvsa_streamer_screenshare_start : R.string.plvsa_streamer_screenshare_stop);

                PLVToast.Builder.context(getContext())
                        .setText(text)
                        .show();

                if(isGranted) {
                    callAutoFullscreen(user.getUserId());
                }
            }
        }

        @Override
        public void onScreenShareChange(final int position, final boolean isShare, int extra, String userId, boolean isMyself) {
            super.onScreenShareChange(position, isShare, extra, userId, isMyself);
            streamerAdapter.updateUserScreenSharing(position, isShare);

            if (isMyself) {
                if (isShare) {
                    showFloatWindow();
                } else {
                    hideFloatWindow();
                }
                String msg;
                if (extra == IPLVScreenShareListener.PLV_SCREEN_SHARE_OK) {
                    msg = isShare ? getContext().getString(R.string.plvsa_streamer_sharescreen_start_tip) : getContext().getString(R.string.plvsa_streamer_sharescreen_already_stop);
                    if (!isShare && !PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_GRANT_PERMISSION_SHARE_SCREEN)) {
                        msg = getContext().getString(R.string.plvsa_streamer_remove_speaker_and_stop_screenshare);
                    }
                } else {
                    msg = getContext().getString(R.string.plvsa_streamer_sharescreen_error) + extra;
                }
                PLVToast.Builder.context(getContext())
                        .setText(msg)
                        .build().show();
            }
        }

        @Override
        public void onFirstScreenChange(String linkMicUserId, boolean isFirstScreen) {
            updateLinkMicLayoutListOnChange();
            streamerAdapter.updateAllItem();
        }

        @Override
        public void onTeacherInviteMeJoinLinkMic(PLVJoinResponseSEvent event) {
            if (isEnterLive) {
                linkMicInvitationLayout.setCurrentEnableVideo(streamerPresenter.isLocalVideoEnabled());
                linkMicInvitationLayout.setCurrentEnableAudio(streamerPresenter.isLocalAudioEnabled());
                linkMicInvitationLayout.open();
            } else {
                streamerPresenter.answerLinkMicInvitation(false, false, streamerPresenter.isLocalVideoEnabled(), streamerPresenter.isLocalAudioEnabled());
            }
        }

        @Override
        public void onViewerJoinAnswer(PLVJoinAnswerSEvent joinAnswerEvent, PLVMemberItemDataBean member) {
            if (joinAnswerEvent.isRefuse() && member != null && member.getSocketUserBean() != null) {
                PLVToast.Builder.context(getContext())
                        .setText(PLVAppUtils.formatString(R.string.plv_linkmic_no_answer, member.getSocketUserBean().getNick()))
                        .build().show();
            }
        }
    };

    private void observePushResolutionRatio() {
        if (streamerPresenter == null) {
            return;
        }
        streamerPresenter.getData().getPushResolutionRatio()
                .observe((LifecycleOwner) getContext(), new Observer<PLVLinkMicConstant.PushResolutionRatio>() {
                    @Override
                    public void onChanged(@Nullable PLVLinkMicConstant.PushResolutionRatio resolutionRatio) {
                        if (resolutionRatio == null) {
                            return;
                        }
                        currentResolutionRatio = resolutionRatio;
                        updateStreamerLayoutRatio();
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听数据">

    private void observeData() {
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable final PLVStatefulData<PolyvLiveClassDetailVO> statefulData) {
                String linkMicBg = nullable(new PLVSugarUtil.Supplier<String>() {
                    @Override
                    public String get() {
                        return statefulData.getData().getData().getPortraitChatBgImg();
                    }
                });

                final Integer linkMicBgOpacity = nullable(new PLVSugarUtil.Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        return statefulData.getData().getData().getPortraitChatBgImgOpacity();
                    }
                });
                if (TextUtils.isEmpty(linkMicBg)) {
                    return;
                }
                if (linkMicBg.startsWith("//")) {
                    linkMicBg = "https:" + linkMicBg;
                }
                final String finalLinkMicBg = linkMicBg;
                Disposable loadBgDisposable = Observable.just(1).map(new Function<Integer, BitmapDrawable>() {
                            @Override
                            public BitmapDrawable apply(Integer integer) throws Exception {
                                return (BitmapDrawable) PLVImageLoader.getInstance().getImageAsDrawable(getContext(), finalLinkMicBg);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<BitmapDrawable>() {
                            @Override
                            public void accept(BitmapDrawable bitmapDrawable) throws Exception {
                                if (bitmapDrawable == null) {
                                    return;
                                }
                                Bitmap bitmapBg = bitmapDrawable.getBitmap();
                                if (linkMicBgOpacity != null && linkMicBgOpacity > 0) {
                                    Bitmap blurBitmap = ImageUtils.fastBlur(bitmapBg, 0.8f, (float) linkMicBgOpacity / 2);
                                    linkMicBgIv.setImageBitmap(blurBitmap);

                                } else {
                                    linkMicBgIv.setImageBitmap(bitmapDrawable.getBitmap());
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                PLVCommonLog.e(TAG, "load link mic bg fail");
                            }
                        });
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="更新连麦布局">

    /**
     * 更新平铺模式的连麦布局
     */
    private void updateLinkMicListLayout() {
        if (plvsaStreamerRvLayout.getCurrentMode() != PLVMultiModeRecyclerViewLayout.MODE_TILED) {
            //不是平铺模式的布局不用更新
            return;
        }
        if (isGuest()) {
            if (!isEnterLive && streamerAdapter.getItemType() == PLVSAStreamerAdapter.ITEM_TYPE_ONLY_TEACHER) {
                //嘉宾还没进入直播间，初始化后不需要更新
                return;
            }
            if (isEnterLive && streamerAdapter.getItemCount() <= 1) {
                //进入直播间后，如果已经没人了，则跳过更新
                return;
            }
        }

        boolean isPortrait = PLVScreenUtils.isPortrait(getContext());

        //重新计算连麦列表宽高布局参数
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) plvsaStreamerRvLayout.getRecyclerView().getLayoutParams();
        if (streamerAdapter.getItemCount() <= 1) {
            int itemType = PLVSAStreamerAdapter.ITEM_TYPE_ONLY_TEACHER;
            if (streamerAdapter.getItemType() == itemType) {
                return;
            }
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.topMargin = 0;

            streamerAdapter.setItemType(itemType);
            gridLayoutManager.setSpanCount(1);
            gridLayoutManager.requestLayout();
        } else {
            int itemType;
            if (streamerAdapter.getItemCount() <= 4){
                itemType = PLVSAStreamerAdapter.ITEM_TYPE_LESS_THAN_FOUR;
                if(!isPortrait && streamerAdapter.getItemCount() <= 2){
                    itemType = PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_ONE;
                }
            } else {
                itemType = PLVSAStreamerAdapter.ITEM_TYPE_MORE_THAN_FOUR;
            }

            if (streamerAdapter.getItemType() == itemType) {
                return;
            }

            streamerAdapter.setItemType(itemType);

            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (streamerAdapter.getItemCount() <= 2) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else if (streamerAdapter.getItemCount() <= 4) {
                lp.width = (int) (ScreenUtils.getScreenOrientatedHeight() * 1.5);
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            lp.topMargin = 0;
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            //2-4人时，布局为2列，超过4人为为maxCount
            int maxCount = ScreenUtils.isPortrait() ? 3: 4;
            int spanCount = streamerAdapter.getItemCount() <= 4 ? 2 : maxCount;

            gridLayoutManager.setSpanCount(spanCount);
            gridLayoutManager.requestLayout();
        }
        plvsaStreamerRvLayout.getRecyclerView().setLayoutParams(lp);
        plvsaStreamerRvLayout.getRecyclerView().setAdapter(streamerAdapter);//能否不调用这个
    }

    /**
     * 更新一对多的主讲模式连麦布局
     */
    private void updateOneToMoreLinkMicLayout(){
        if(plvsaStreamerRvLayout.getCurrentMode() != PLVMultiModeRecyclerViewLayout.MODE_ONE_TO_MORE){
            //不是一对多的主讲模式的布局不用更新
            return;
        }

        streamerAdapter.setItemType(PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_MORE);

        boolean isPortrait = ScreenUtils.isPortrait();

        boolean isOnlyTeacher = streamerAdapter.getItemCount() <= 1;

        //更新MainView视图
        plvsaStreamerRvLayout.clearMainView();
        streamerAdapter.releaseMainViewHolder();
        final PLVSAStreamerAdapter.StreamerItemViewHolder viewHolder = streamerAdapter.createMainViewHolder(plvsaStreamerRvLayout.getMainContainer());
        viewHolder.changeRectLyToMatchParent();
        mainView = viewHolder.itemView;
        //将维护的viewHolder副本添加到主布局上，实现一对多主讲模式
        plvsaStreamerRvLayout.addViewToMain(mainView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //修改主画面尺寸
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) plvsaStreamerRvLayout.getMainContainer().getLayoutParams();
        if (params != null) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (isOnlyTeacher) {
                params.topMargin = 0;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (isPortrait) {
                    params.topMargin = ConvertUtils.dp2px(78);
                } else {
                    params.topMargin = 0;
                }
            }
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        streamerAdapter.updateMainViewHolder();

        //修改连麦列表尺寸
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) plvsaStreamerRvLayout.getRecyclerView().getLayoutParams();
        if(layoutParams != null){
            layoutParams.width = isOnlyTeacher ? 0 : ScreenUtils.getScreenOrientatedWidth() / 3;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            if(!isPortrait){
                layoutParams.topMargin = 0;
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                layoutParams.topMargin = ConvertUtils.dp2px(78);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
        }
        gridLayoutManager.setSpanCount(1);
        gridLayoutManager.requestLayout();

        plvsaStreamerRvLayout.getRecyclerView().setAdapter(streamerAdapter);
    }

    /**
     * 更新占位模式布局，用于嘉宾登录时，讲师占位
     */
    private void updatePlaceholderLayout(){
        if(plvsaStreamerRvLayout.getCurrentMode() != PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER){
            return;
        }

        if(placeholderView == null){
            placeholderView = LayoutInflater.from(getContext()).inflate(R.layout.plvsa_streamer_no_stream_placeholder, null, false);
            plvsaStreamerRvLayout.clearMainView();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) plvsaStreamerRvLayout.getMainContainer().getLayoutParams();
            if(params != null){
                params.topMargin = ConvertUtils.dp2px(78);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.width = ScreenUtils.getScreenOrientatedWidth() / 2;
                if(PLVScreenUtils.isPortrait(getContext())){
                    params.height = (int) (ScreenUtils.getScreenOrientatedWidth() / 2 * 1.5);
                } else {
                    params.height = (int) (ScreenUtils.getScreenOrientatedWidth() / 2 / 1.5);
                }

            }
            plvsaStreamerRvLayout.addViewToMain(placeholderView, ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT);
        }

        plvsaStreamerRvLayout.setMainViewVisibility(View.VISIBLE);

        //修改连麦列表尺寸
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) plvsaStreamerRvLayout.getRecyclerView().getLayoutParams();
        if (layoutParams != null) {
            layoutParams.topMargin = ConvertUtils.dp2px(78);
            layoutParams.width = ScreenUtils.getScreenOrientatedWidth() / 2;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
        }
        gridLayoutManager.setSpanCount(1);
        gridLayoutManager.requestLayout();
        plvsaStreamerRvLayout.getRecyclerView().setAdapter(streamerAdapter);
    }

    /**
     * 响应推流出去的画面比例，更新本地布局看到的视频画面比例
     */
    private void updateStreamerLayoutRatio() {
        final boolean isOnlyTeacher = streamerAdapter.getItemCount() <= 1;
        final ViewGroup.LayoutParams lp = plvsaStreamerRvLayout.getLayoutParams();
        if (currentResolutionRatio == null
                || currentResolutionRatio == PLVLinkMicConstant.PushResolutionRatio.RATIO_16_9
                || ScreenUtils.isPortrait()
                || !isOnlyTeacher) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            lp.width = min(ScreenUtils.getScreenOrientatedHeight() / 3 * 4, ScreenUtils.getScreenOrientatedWidth());
        }
        plvsaStreamerRvLayout.setLayoutParams(lp);
    }

    /**
     * 当人员变动时更新连麦布局。
     * 加了延迟防止过于频繁地刷新
     */
    private void updateLinkMicLayoutListOnChange() {
        if (updateLinkmicLayoutRunnable == null) {
            updateLinkmicLayoutRunnable = new Runnable() {
                @Override
                public void run() {
                    updateStreamerLayoutRatio();
                    updateLinkMicListLayout();
                    updateOneToMoreLinkMicLayout();
                    updatePlaceholderLayout();
                }
            };
        }
        removeCallbacks(updateLinkmicLayoutRunnable);
        postDelayed(updateLinkmicLayoutRunnable, 500);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦模式切换">

    /**
     * 主讲模式下，切换item到第一画面
     */
    private void exchangeItemToMain(int position){
        if(streamerAdapter.getItemType() == PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_MORE){
            int lastPosition = streamerAdapter.getHideItemIndex();
            streamerAdapter.setItemHide(position);
            streamerAdapter.updateMainViewHolder();
            streamerAdapter.notifyItemChanged(lastPosition);
            streamerAdapter.notifyItemChanged(position);
        }
    }

    /**
     * 切换为平铺模式
     */
    private void changeToTiled(){
        //把MainView释放
        plvsaStreamerRvLayout.clearMainView();
        streamerAdapter.releaseMainViewHolder();
        mainView = null;
        plvsaStreamerRvLayout.setMainViewVisibility(View.GONE);
        //切换模式
        plvsaStreamerRvLayout.changeMode(PLVMultiModeRecyclerViewLayout.MODE_TILED);
        //重置默认item不隐藏
        streamerAdapter.setItemHide(-1);
        updateLinkMicListLayout();
    }

    /**
     * 切换为一对多的主讲模式
     */
    private void changeToOneToMore(){
        //切换模式
        plvsaStreamerRvLayout.changeMode(PLVMultiModeRecyclerViewLayout.MODE_ONE_TO_MORE);
        plvsaStreamerRvLayout.setMainViewVisibility(View.VISIBLE);
        //讲师默认首位故此隐藏0
        streamerAdapter.setItemHide(0);
        updateOneToMoreLinkMicLayout();
    }

    /**
     * 切换到占位模式，适用于嘉宾登录时讲师占位
     */
    private void changePlaceholder(){
        plvsaStreamerRvLayout.changeMode(PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER);
        plvsaStreamerRvLayout.setMainViewVisibility(View.VISIBLE);
        streamerAdapter.setItemType(PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_MORE);
        streamerAdapter.setItemHide(-1);//不隐藏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            linkMicBgIv.setVisibility(View.VISIBLE);
        }
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="自动全屏">
    private void callAutoFullscreen(String userId){
        streamerAdapter.callUserFullscreen(userId, plvsaStreamerRvLayout.getRecyclerView());
    }

    public void clearFullscreenState(PLVLinkMicItemDataBean linkmicItem){
        streamerAdapter.clearFullscreenHolder(plvsaStreamerRvLayout.getRecyclerView(), linkmicItem);

    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        updateOnOrientationChanged(isLandscape);
    }

    private void updateOnOrientationChanged(boolean isLandscape) {
        if (isLandscape) {
            streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE);
        } else {
            streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);
        }
        if (!isLandscape && plvsaStreamerRvLayout.getCurrentMode() == PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER) {
            linkMicBgIv.setVisibility(View.VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="工具方法">
    private boolean isGuest(){
        if(liveRoomDataManager != null){
            return PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        }
        return false;
    }

    private void showLeaveLiveConfirmLayout() {
        if (leaveLiveConfirmDialog == null) {
            leaveLiveConfirmDialog = new PLVSAConfirmDialog(getContext())
                    .setTitleVisibility(GONE)
                    .setContent(getContext().getString(R.string.plv_live_room_dialog_exit_confirm_ask))
                    .setRightButtonText(R.string.plv_common_dialog_confirm)
                    .setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Activity) getContext()).finish();
                            leaveLiveConfirmDialog.hide();
                        }
                    });
        }
        leaveLiveConfirmDialog.show();
    }

    private void showFloatWindow() {
        if(PLVFloatPermissionUtils.checkPermission((Activity) getContext())){
            getCurrentScreenShareFloatWindow().show();
        } else {
            if(isShowWindowPermissionDialog) {
                new PLVSAConfirmDialog(getContext())
                        .setTitleVisibility(View.GONE)
                        .setContent(getContext().getString(R.string.plvsa_float_window_permission_need))
                        .setLeftButtonText(R.string.plv_common_dialog_cancel)
                        .setLeftBtnListener(new PLVConfirmDialog.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), getContext().getString(R.string.plvsa_float_window_permission_denied_and_not_show), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setRightButtonText(R.string.plv_common_dialog_confirm)
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                PLVFloatPermissionUtils.requestPermission((Activity) getContext(), new PLVFloatPermissionUtils.IPLVOverlayPermissionListener() {
                                    @Override
                                    public void onResult(boolean isGrant) {
                                        if(isGrant){
                                            getCurrentScreenShareFloatWindow().show();
                                        } else {
                                            Toast.makeText(getContext(), getContext().getString(R.string.plvsa_float_window_permission_denied_and_not_show), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .show();
                isShowWindowPermissionDialog = false;//只显示一次
            }
        }

    }

    private void hideFloatWindow(){
        getCurrentScreenShareFloatWindow().close();
    }

    @NonNull
    private IPLVSAStreamerScreenShareFloatWindow getCurrentScreenShareFloatWindow() {
        boolean isFloatWindowV2 = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.STREAMER_SCREEN_SHARE_FLOAT_WINDOW_V2);
        return isFloatWindowV2 ? floatWindowV2 : floatWindow;
    }

    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="内部处理逻辑">

    private boolean isMyselfUserId(String uid) {
        final String myUserId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return liveRoomDataManager.getConfig().getUser().getViewerId();
            }
        });
        return myUserId != null && myUserId.equals(uid);
    }

    /**
     * 静音麦克风时，检测用户是否正在说话，以提醒用户麦克风状态
     * <p>
     * 麦克风关闭时其他用户不会听到声音，但考虑到用户可能误触关闭了麦克风
     * 而引起输入异常的情况，不会完全关闭麦克风调用，这里进行音量检测提醒
     */
    private void tryNotifyLocalAudioMuted(int volume) {
        if (!isLocalAudioMuted) {
            return;
        }
        if (volume < VOLUME_THRESHOLD_TO_NOTIFY_AUDIO_MUTED) {
            return;
        }
        if (System.currentTimeMillis() - lastNotifyLocalAudioMutedTimestamp <= TimeUnit.MINUTES.toMillis(3)) {
            return;
        }
        lastNotifyLocalAudioMutedTimestamp = System.currentTimeMillis();

        PLVToast.Builder.context(getContext())
                .setText(R.string.plv_streamer_notify_speaking_with_mute_audio)
                .longDuration()
                .show();
    }

    // </editor-fold>

}
