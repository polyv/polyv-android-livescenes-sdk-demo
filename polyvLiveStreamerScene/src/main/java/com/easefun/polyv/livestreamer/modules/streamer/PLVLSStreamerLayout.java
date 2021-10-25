package com.easefun.polyv.livestreamer.modules.streamer;

import android.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.livescenes.streamer.IPLVSStreamerManager;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.livescenes.streamer.transfer.PLVSStreamerInnerDataTransfer;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.streamer.adapter.PLVLSStreamerAdapter;
import com.easefun.polyv.livestreamer.scenes.PLVLSLiveStreamerActivity;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * 推流和连麦布局
 */
public class PLVLSStreamerLayout extends FrameLayout implements IPLVLSStreamerLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //推流和连麦presenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //view
    private RecyclerView plvlsStreamerRv;

    //适配器
    private PLVLSStreamerAdapter streamerAdapter;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSStreamerLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSStreamerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSStreamerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_streamer_layout, this);

        plvlsStreamerRv = findViewById(R.id.plvls_streamer_rv);

        //init RecyclerView
        plvlsStreamerRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        plvlsStreamerRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(8), 0));
        //禁用RecyclerView默认动画
        plvlsStreamerRv.getItemAnimator().setAddDuration(0);
        plvlsStreamerRv.getItemAnimator().setChangeDuration(0);
        plvlsStreamerRv.getItemAnimator().setMoveDuration(0);
        plvlsStreamerRv.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = plvlsStreamerRv.getItemAnimator();
        if (rvAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
        }

        //init adapter
        streamerAdapter = new PLVLSStreamerAdapter(plvlsStreamerRv, new PLVLSStreamerAdapter.OnStreamerAdapterCallback() {
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
        });
        streamerAdapter.setIsOnlyAudio(PLVSStreamerInnerDataTransfer.getInstance().isOnlyAudio());

        //启动前台服务，防止在后台被杀
        PLVForegroundService.startForegroundService(PLVLSLiveStreamerActivity.class, "POLYV开播", R.drawable.plvls_ic_launcher);
        //防止自动息屏、锁屏
        setKeepScreenOn(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLSStreamerLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        streamerPresenter = new PLVStreamerPresenter(liveRoomDataManager);
        streamerPresenter.registerView(streamerView);
        streamerPresenter.init();
        observeClassDetailData();
    }

    @Override
    public void startClass() {
        streamerPresenter.startLiveStream();
    }

    @Override
    public void stopClass() {
        streamerPresenter.stopLiveStream();
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
    public boolean enableRecordingAudioVolume(boolean enable) {
        return streamerPresenter.enableRecordingAudioVolume(enable);
    }

    @Override
    public boolean enableLocalVideo(boolean enable) {
        return streamerPresenter.enableLocalVideo(enable);
    }

    @Override
    public boolean setCameraDirection(boolean front) {
        return streamerPresenter.setCameraDirection(front);
    }

    @Override
    public void controlUserLinkMic(int position, boolean isAllowJoin) {
        streamerPresenter.controlUserLinkMic(position, isAllowJoin);
    }

    @Override
    public void muteUserMedia(int position, boolean isVideoType, boolean isMute) {
        streamerPresenter.muteUserMedia(position, isVideoType, isMute);
    }

    @Override
    public void closeAllUserLinkMic() {
        streamerPresenter.closeAllUserLinkMic();
    }

    @Override
    public void muteAllUserAudio(boolean isMute) {
        streamerPresenter.muteAllUserAudio(isMute);
    }

    @Override
    public void addOnStreamerStatusListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnNetworkQualityListener(IPLVOnDataChangedListener<Integer> listener) {
        streamerPresenter.getData().getNetworkQuality().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnStreamerTimeListener(IPLVOnDataChangedListener<Integer> listener) {
        streamerPresenter.getData().getStreamerTime().observe((LifecycleOwner) getContext(), listener);
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
    public void addOnEnableAudioListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getEnableAudio().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnEnableVideoListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getEnableVideo().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnIsFrontCameraListener(IPLVOnDataChangedListener<Boolean> listener) {
        streamerPresenter.getData().getIsFrontCamera().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public int getNetworkQuality() {
        return streamerPresenter.getNetworkQuality();
    }

    @Override
    public boolean isStreamerStartSuccess() {
        return streamerPresenter.getStreamerStatus() == PLVStreamerPresenter.STREAMER_STATUS_START_SUCCESS;
    }

    @Override
    public IPLVStreamerContract.IStreamerPresenter getStreamerPresenter() {
        return streamerPresenter;
    }

    @Override
    public void destroy() {
        streamerPresenter.destroy();
        PLVForegroundService.stopForegroundService();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="推流和连麦 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void onStreamerEngineCreatedSuccess(String linkMicUid, List<PLVLinkMicItemDataBean> linkMicList) {
            super.onStreamerEngineCreatedSuccess(linkMicUid, linkMicList);
            streamerPresenter.setMixLayoutType(PLVSStreamerConfig.MixStream.MIX_LAYOUT_TYPE_SPEAKER);
            streamerAdapter.setMyLinkMicId(linkMicUid);
            streamerAdapter.setDataList(linkMicList);
            plvlsStreamerRv.setAdapter(streamerAdapter);
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteVideo(uid, mute, streamerListPos, memberListPos);
            streamerAdapter.updateUserMuteVideo(streamerListPos);
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteAudio(uid, mute, streamerListPos, memberListPos);
            streamerAdapter.updateVolumeChanged();
        }

        @Override
        public void onLocalUserMicVolumeChanged() {
            super.onLocalUserMicVolumeChanged();
            streamerAdapter.updateVolumeChanged();
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
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);
            streamerAdapter.updateAllItem();
        }

        @Override
        public void onStreamerError(int errorCode, Throwable throwable) {
            super.onStreamerError(errorCode, throwable);
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
                PLVToast.Builder.context(getContext())
                        .setText(throwable.getMessage())
                        .build()
                        .show();
            }
        }

        @Override
        public void onGuestRTCStatusChanged(int pos) {
            streamerAdapter.updateGuestStatus(pos);
        }

        @Override
        public void onGuestMediaStatusChanged(int pos) {
            streamerAdapter.updateGuestStatus(pos);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅 - 直播间信息">
    private void observeClassDetailData(){
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> plvLiveClassDetailVOPLVStatefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if(liveRoomDataManager.isOnlyAudio()) {
                    //音频开播模式下，设置封面图
                    if (plvLiveClassDetailVOPLVStatefulData != null && plvLiveClassDetailVOPLVStatefulData.getData() != null
                            && plvLiveClassDetailVOPLVStatefulData.getData().getData() != null) {
                        updateTeacherCameraCoverImage(plvLiveClassDetailVOPLVStatefulData.getData().getData().getSplashImg());
                    }
                }
            }
        });
    }

    /**
     * 更新讲师的封面图
     */
    private void updateTeacherCameraCoverImage(String coverImage){
        if(streamerAdapter != null){
            streamerAdapter.updateCoverImage(coverImage);
        }

    }
    // </editor-fold >
}
