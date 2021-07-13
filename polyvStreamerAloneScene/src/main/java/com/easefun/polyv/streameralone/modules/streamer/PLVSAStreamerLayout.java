package com.easefun.polyv.streameralone.modules.streamer;

import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchRecyclerView;
import com.easefun.polyv.livescenes.streamer.IPLVSStreamerManager;
import com.easefun.polyv.livescenes.streamer.config.PLVSStreamerConfig;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.streamer.adapter.PLVSAStreamerAdapter;
import com.easefun.polyv.streameralone.modules.streamer.service.PLVSAForegroundService;
import com.easefun.polyv.streameralone.ui.widget.PLVSAConfirmDialog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * 推流和连麦布局
 */
public class PLVSAStreamerLayout extends FrameLayout implements IPLVSAStreamerLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //推流和连麦Presenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;
    //view
    private PLVNoInterceptTouchRecyclerView plvsaStreamerRv;
    //adapter
    private PLVSAStreamerAdapter streamerAdapter;
    //listener
    private OnViewActionListener onViewActionListener;
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

        plvsaStreamerRv = findViewById(R.id.plvsa_streamer_rv);

        //init RecyclerView
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

        //init adapter
        streamerAdapter = new PLVSAStreamerAdapter(plvsaStreamerRv, new PLVSAStreamerAdapter.OnStreamerAdapterCallback() {
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
        });

        //启动前台服务，防止在后台被杀
        PLVSAForegroundService.startService();
        //防止自动息屏、锁屏
        setKeepScreenOn(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSAStreamerLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        streamerPresenter = new PLVStreamerPresenter(liveRoomDataManager);
        streamerPresenter.registerView(streamerView);
        streamerPresenter.init();
        streamerPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);
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
    public void startLive() {
        streamerPresenter.startLiveStream();
    }

    @Override
    public void stopLive() {
        streamerPresenter.stopLiveStream();
    }

    @Override
    public IPLVStreamerContract.IStreamerPresenter getStreamerPresenter() {
        return streamerPresenter;
    }

    @Override
    public boolean onBackPressed() {
        return streamerAdapter.onBackPressed();
    }

    @Override
    public boolean onRvSuperTouchEvent(MotionEvent ev) {
        boolean returnResult = plvsaStreamerRv.onSuperTouchEvent(ev);
        streamerAdapter.checkClickItemView(ev);
        return returnResult;
    }

    @Override
    public void destroy() {
        streamerPresenter.destroy();
        PLVSAForegroundService.stopService();
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
            updateLinkMicListLayout();
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);
            streamerAdapter.updateAllItem();
            updateLinkMicListLayout();
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
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新连麦布局">
    private void updateLinkMicListLayout() {
        FrameLayout.LayoutParams lp = (LayoutParams) plvsaStreamerRv.getLayoutParams();
        if (streamerAdapter.getItemCount() <= 1) {
            int itemType = PLVSAStreamerAdapter.ITEM_TYPE_ONLY_TEACHER;
            if (streamerAdapter.getItemType() == itemType) {
                return;
            }
            lp.topMargin = 0;
            plvsaStreamerRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            streamerAdapter.setItemType(itemType);
        } else {
            int itemType = PLVSAStreamerAdapter.ITEM_TYPE_ONE_TO_ONE;
            if (streamerAdapter.getItemType() == itemType) {
                return;
            }
            lp.topMargin = ConvertUtils.dp2px(78);
            plvsaStreamerRv.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
            streamerAdapter.setItemType(itemType);
        }
        plvsaStreamerRv.setLayoutParams(lp);
        plvsaStreamerRv.setAdapter(streamerAdapter);
    }
    // </editor-fold>
}
