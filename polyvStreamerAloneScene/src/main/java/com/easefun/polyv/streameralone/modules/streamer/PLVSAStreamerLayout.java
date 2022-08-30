package com.easefun.polyv.streameralone.modules.streamer;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;
import static java.lang.Math.min;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVMultiModeRecyclerViewLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.PLVFloatPermissionUtils;
import com.easefun.polyv.livescenes.streamer.IPLVSStreamerManager;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.adapter.PLVSAStreamerAdapter;
import com.easefun.polyv.streameralone.scenes.PLVSAStreamerAloneActivity;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.screenshare.IPLVScreenShareListener;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.socket.client.Ack;

/**
 * 推流和连麦布局
 */
public class PLVSAStreamerLayout extends FrameLayout implements IPLVSAStreamerLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

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

    //退出直播间弹窗
    private PLVConfirmDialog leaveLiveConfirmDialog;
    //返回home时的悬浮窗
    public PLVSAStreamerFloatWindow floatWindow;

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

        //init RecyclerView
        PLVNoInterceptTouchRecyclerView plvsaStreamerRv = plvsaStreamerRvLayout.getRecyclerView();
        plvsaStreamerRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        //禁用RecyclerView默认动画
        plvsaStreamerRv.getItemAnimator().setAddDuration(0);
        plvsaStreamerRv.getItemAnimator().setChangeDuration(0);
        plvsaStreamerRv.getItemAnimator().setMoveDuration(0);
        plvsaStreamerRv.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = plvsaStreamerRv.getItemAnimator();
        if (rvAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
        }

        gridLayoutManager = new GridLayoutManager(getContext(), 1);
        plvsaStreamerRv.setLayoutManager(gridLayoutManager);

        //启动前台服务，防止在后台被杀
        PLVForegroundService.startForegroundService(PLVSAStreamerAloneActivity.class, "POLYV开播", R.drawable.plvsa_ic_launcher);
        //防止自动息屏、锁屏
        setKeepScreenOn(true);

        //悬浮窗
        floatWindow = new PLVSAStreamerFloatWindow(getContext());

        observeSpeakerPermissionChange();
    }

    private void observeSpeakerPermissionChange() {
        this.onSpeakerRoleChangedListener = new PLVUserAbilityManager.OnUserRoleChangedListener() {
            @Override
            public void onUserRoleAdded(PLVUserRole role) {
                if (role == PLVUserRole.STREAMER_GRANTED_SPEAKER_USER) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plvsa_streamer_grant_speaker_permission)
                            .show();
                }
            }

            @Override
            public void onUserRoleRemoved(PLVUserRole role) {
                if (role == PLVUserRole.STREAMER_GRANTED_SPEAKER_USER) {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plvsa_streamer_remove_speaker_permission)
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
            public SurfaceView createLinkMicRenderView() {
                return streamerPresenter.createRenderView(Utils.getApp());
            }

            @Override
            public void releaseLinkMicRenderView(SurfaceView renderView) {
                streamerPresenter.releaseRenderView(renderView);
            }

            @Override
            public void setupRenderView(SurfaceView surfaceView, String linkMicId) {
                streamerPresenter.setupRenderView(surfaceView, linkMicId);
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
            public void onControlUserLinkMic(int position, boolean isAllowJoin) {
                if (streamerPresenter != null) {
                    streamerPresenter.controlUserLinkMicInLinkMicList(position, isAllowJoin);
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
                            text = "已收回主讲权限";
                        } else if (isGuestTransferPermission) {
                            text = "已移交主讲权限";
                        } else {
                            text = "已授予主讲权限";
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

        streamerPresenter = new PLVStreamerPresenter(liveRoomDataManager);
        streamerPresenter.registerView(streamerView);
        streamerPresenter.init();
        streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);

        //初始化悬浮窗
        streamerPresenter.registerView(floatWindow.getStreamerView());
        observePushResolutionRatio();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
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
    public Pair<Integer, Integer> getBitrateInfo() {
        return new Pair<>(streamerPresenter.getMaxBitrate(), streamerPresenter.getBitrate());
    }

    @Override
    public int getNetworkQuality() {
        return streamerPresenter.getNetworkQuality();
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
        streamerPresenter.startLiveStream();
    }

    @Override
    public void stopLive() {
        streamerPresenter.stopLiveStream();
    }

    @Override
    public void enterLive() {
        isEnterLive = true;
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
                        updateLinkMicLayoutListOnChange();
                    }
                } else {
                    changePlaceholder();
                    updateLinkMicLayoutListOnChange();
                }
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
        streamerPresenter.destroy();
        PLVForegroundService.stopForegroundService();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            super.onStreamerEngineCreatedSuccess(linkMicUid, linkMicList);
            streamerPresenter.setMixLayoutType(PLVSStreamerConfig.MixStream.MIX_LAYOUT_TYPE_TILE);
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
            streamerAdapter.updateAllItem();
            updateLinkMicLayoutListOnChange();
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);
            streamerAdapter.updateAllItem();
            updateLinkMicLayoutListOnChange();
        }

        @Override
        public void onGuestRTCStatusChanged(int pos) {
            super.onGuestRTCStatusChanged(pos);
            streamerAdapter.updateGuestStatus(pos);
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
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PLVFastPermission.getInstance().jump2Settings(getContext());
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        dialog.show();
                    } else {
                        new PLVSAConfirmDialog(getContext())
                                .setTitle("直播异常")
                                .setContent(throwable.getMessage())
                                .setIsNeedLeftBtn(false)
                                .setCancelable(false)
                                .setRightButtonText("重新开播")
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
                    text = "主持人";
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
        public void onScreenShareChange(final int position, final boolean isShare, int extra) {
            super.onScreenShareChange(position, isShare, extra);
            String msg;
            streamerAdapter.updateUserScreenSharing(position, isShare);
            if(isShare){
                showFloatWindow();
            } else {
                hideFloatWindow();
            }
            if(extra == IPLVScreenShareListener.PLV_SCREEN_SHARE_OK){
                msg = isShare ? getContext().getString(R.string.plvsa_streamer_sharescreen_start_tip) : getContext().getString(R.string.plvsa_streamer_sharescreen_already_stop);
                if(!isShare && !PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_GRANT_PERMISSION_SHARE_SCREEN)){
                    msg = getContext().getString(R.string.plvsa_streamer_remove_speaker_and_stop_screenshare);
                }
            } else {
                msg = getContext().getString(R.string.plvsa_streamer_sharescreen_error) + extra;
            }
            PLVToast.Builder.context(getContext())
                    .setText(msg)
                    .build().show();

        }

        @Override
        public void onFirstScreenChange(String linkMicUserId, boolean isFirstScreen) {
            updateLinkMicLayoutListOnChange();
            streamerAdapter.updateAllItem();
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

            if (isPortrait) {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.topMargin = ConvertUtils.dp2px(78);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            } else {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if(streamerAdapter.getItemCount() <= 2){
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                } else if (streamerAdapter.getItemCount() <= 4){
                    lp.width = (int) (ScreenUtils.getScreenOrientatedHeight() * 1.5);
                } else {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                lp.topMargin = 0;
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                lp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
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
        final RecyclerView.ViewHolder viewHolder = streamerAdapter.createMainViewHolder(plvsaStreamerRvLayout.getMainContainer());
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
            }
        }
        gridLayoutManager.setSpanCount(1);
        gridLayoutManager.requestLayout();

        plvsaStreamerRvLayout.getRecyclerView().setAdapter(streamerAdapter);
    }

    /**
     * 更新占位模式布局，用于嘉宾登陆时，讲师占位
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
     * 切换到占位模式，适用于嘉宾登陆时讲师占位
     */
    private void changePlaceholder(){
        plvsaStreamerRvLayout.changeMode(PLVMultiModeRecyclerViewLayout.MODE_PLACEHOLDER);
        plvsaStreamerRvLayout.setMainViewVisibility(View.VISIBLE);
        streamerAdapter.setItemType(PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_MORE);
        streamerAdapter.setItemHide(-1);//不隐藏
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
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE);
        } else {
            streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);
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
                    .setRightButtonText("确认")
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
            if (floatWindow != null) {
                floatWindow.show();
            }
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
                                            if (floatWindow != null) {
                                                floatWindow.show();
                                            }
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
        if(floatWindow != null){
            floatWindow.close();
        }
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
                .setText(R.string.plvsa_streamer_notify_speaking_with_mute_audio)
                .longDuration()
                .show();
    }

    // </editor-fold>

}
