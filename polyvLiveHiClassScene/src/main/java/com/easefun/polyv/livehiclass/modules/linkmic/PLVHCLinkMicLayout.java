package com.easefun.polyv.livehiclass.modules.linkmic;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.contract.IPLVMultiRoleLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.presenter.PLVMultiRoleLinkMicPresenter;
import com.easefun.polyv.livecommon.module.modules.multirolelinkmic.view.PLVAbsMultiRoleLinkMicView;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.linkmic.list.IPLVHCLinkMicItemLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.IPLVHCLinkMicItem;
import com.easefun.polyv.livehiclass.modules.linkmic.list.item.PLVHCLinkMicItemContainer;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCJoinDiscussCountDownWindow;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCLinkMicInvitationCountdownWindow;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCReceiveBroadcastDialog;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCSeminarCountdownWindow;
import com.easefun.polyv.livehiclass.modules.linkmic.widget.PLVHCTeacherScreenStreamLayout;
import com.easefun.polyv.livehiclass.modules.linkmic.zoom.PLVHCLinkMicZoomManager;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCClassStopHasNextDialog;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCClassStopNoNextDialog;
import com.easefun.polyv.livehiclass.modules.liveroom.PLVHCLinkMicUserControlDialog;
import com.easefun.polyv.livehiclass.scenes.PLVHCLiveHiClassActivity;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCConfirmDialog;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.linkmic.model.PLVNetworkStatusVO;
import com.plv.livescenes.document.event.PLVSwitchRoomEvent;
import com.plv.livescenes.hiclass.PLVHiClassConstant;
import com.plv.livescenes.hiclass.PLVHiClassDataBean;
import com.plv.livescenes.hiclass.vo.PLVHCStudentLessonListVO;
import com.plv.livescenes.net.IPLVDataRequestListener;
import com.plv.livescenes.streamer.IPLVStreamerManager;
import com.plv.socket.event.linkmic.PLVRemoveMicSiteEvent;
import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.socket.client.Ack;

/**
 * 连麦布局
 */
public class PLVHCLinkMicLayout extends FrameLayout implements IPLVHCLinkMicLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //连麦presenter
    private IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter linkMicPresenter;
    //view
    @Nullable
    private IPLVHCLinkMicItemLayout linkMicItemLayout;
    private PLVHCLinkMicUserControlDialog linkMicUserControlDialog;
    private PLVHCClassStopNoNextDialog classStopNoNextDialog;
    private PLVHCClassStopHasNextDialog classStopHasNextDialog;
    private PLVHCLinkMicInvitationCountdownWindow linkMicInvitationCountdownWindow;
    private PLVHCTeacherScreenStreamLayout teacherScreenStreamLayout;
    //dialog
    private PLVHCReceiveBroadcastDialog teacherSendBroadcastDialog;

    private String teacherNick;
    private boolean isHasPaintPermission;
    private boolean isGroupLeader;

    //run task after init linkMicItemLayout
    private List<Runnable> initiatedTasks = new ArrayList<>();
    //listener
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCLinkMicLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_linkmic_layout, this, true);

        classStopNoNextDialog = new PLVHCClassStopNoNextDialog(getContext());
        classStopHasNextDialog = new PLVHCClassStopHasNextDialog(getContext());
        teacherScreenStreamLayout = new PLVHCTeacherScreenStreamLayout(getContext());

        //启动前台服务，防止在后台被杀
        PLVForegroundService.startForegroundService(PLVHCLiveHiClassActivity.class, "POLYV互动学堂", R.drawable.plvhc_ic_launcher);
    }

    private void initLinkMicItemLayout(boolean isLittleLayout) {
        if (isLittleLayout) {
            ViewStub linkMicLyStub = findViewById(R.id.plvhc_linkmic_little_ly_stub);
            linkMicItemLayout = (IPLVHCLinkMicItemLayout) linkMicLyStub.inflate();
            initLayoutSize(true);
        } else {
            ViewStub linkMicLyStub = findViewById(R.id.plvhc_linkmic_large_ly_stub);
            linkMicItemLayout = (IPLVHCLinkMicItemLayout) linkMicLyStub.inflate();
            initLayoutSize(false);
        }
        if (linkMicItemLayout != null) {
            linkMicItemLayout.setOnViewActionListener(new IPLVHCLinkMicItemLayout.OnViewActionListener() {
                @Override
                public void onClickItemView(final int position, final IPLVHCLinkMicItem linkMicItem) {
                    final PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItem.getLinkMicItemDataBean();
                    if (linkMicItemDataBean == null || (!linkMicPresenter.isTeacherType() && !isGroupLeader)) {
                        return;
                    }
                    if (isGroupLeader && linkMicItemDataBean.isTeacher()) {
                        return;
                    }
                    final boolean isMyself = linkMicPresenter.isMyLinkMicId(linkMicItemDataBean.getLinkMicId());
                    if (linkMicUserControlDialog == null) {
                        linkMicUserControlDialog = new PLVHCLinkMicUserControlDialog(getContext());
                    }
                    linkMicUserControlDialog.setOnViewActionListener(new PLVHCLinkMicUserControlDialog.OnViewActionListener() {
                        @Override
                        public void onClickCamera(final boolean isWillOpen) {
                            controlUserCamera(isMyself, isWillOpen, position);
                        }

                        @Override
                        public void onClickMic(final boolean isWillOpen) {
                            controlUserMic(isMyself, isWillOpen, position);
                        }

                        @Override
                        public void onClickPaint(final boolean isHasPaint) {
                            controlUserPaint(isHasPaint, position);
                        }

                        @Override
                        public void onClickCup() {
                            controlUserCup(linkMicItemDataBean.getNick(), position);
                        }

                        @Override
                        public void onClickCameraOrient() {
                            linkMicPresenter.switchCamera();
                        }

                        @Override
                        public void onClickZoom() {
                            final boolean toZoom = !PLVHCLinkMicZoomManager.getInstance().isZoomIn(linkMicItemDataBean);
                            if (toZoom && !PLVHCLinkMicZoomManager.getInstance().canZoomInItem()) {
                                PLVHCToast.Builder.context(getContext())
                                        .setText("放大已达上限")
                                        .build().show();
                                return;
                            }
                            if (toZoom) {
                                PLVHCLinkMicItemContainer container = linkMicItem.findContainerParent();
                                if (container != null) {
                                    PLVHCLinkMicZoomManager.getInstance().zoom(container, true);
                                }
                            } else {
                                PLVHCLinkMicZoomManager.getInstance().zoomOut(linkMicItem.getLinkMicId());
                            }
                        }
                    });
                    linkMicUserControlDialog.bindViewData(linkMicItemDataBean, isMyself, isGroupLeader);
                    linkMicUserControlDialog.show();
                }
            });
            linkMicItemLayout.setOnRenderViewCallback(new IPLVHCLinkMicItem.OnRenderViewCallback() {
                @Override
                public View createLinkMicRenderView() {
                    return linkMicPresenter.createRenderView(getContext());
                }

                @Override
                public void releaseLinkMicRenderView(View renderView) {
                    linkMicPresenter.releaseRenderView(renderView);
                }

                @Override
                public void setupRenderView(View renderView, String linkMicId, int streamType) {
                    linkMicPresenter.setupRenderView(renderView, linkMicId, streamType);
                    if (onViewActionListener != null) {
                        onViewActionListener.onSetupLinkMicRenderView(renderView, linkMicId, streamType);
                    }
                }
            });
            //run task
            for (Runnable task : initiatedTasks) {
                task.run();
            }
            initiatedTasks.clear();
        }
    }

    private void initLayoutSize(final boolean isLittleLayout) {
        post(new Runnable() {
            @Override
            public void run() {
                int landscapeScreenWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                //连麦布局在横屏下的宽度
                int linkMicLayoutWidth = landscapeScreenWidth - ConvertUtils.dp2px(32);
                //连麦布局在横屏下的高度
                int linkMicLayoutHeight;
                if (isLittleLayout) {
                    linkMicLayoutHeight = linkMicLayoutWidth * 9 / (16 * 7);
                } else {
                    linkMicLayoutHeight = linkMicLayoutWidth * 9 / (16 * 5);
                }

                //调整连麦布局的高度
                ViewGroup.LayoutParams vlp = getLayoutParams();
                vlp.height = linkMicLayoutHeight;
                setLayoutParams(vlp);
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (onViewActionListener != null) {
                            onViewActionListener.onLayoutSizeChanged();
                        }
                    }
                });
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVHCLinkMicLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        observeLessonDataBean();

        linkMicPresenter = new PLVMultiRoleLinkMicPresenter(liveRoomDataManager);
        linkMicPresenter.registerView(linkMicView);
        linkMicPresenter.init();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void muteAudio(boolean mute) {
        linkMicPresenter.muteAudio(mute);
    }

    @Override
    public void muteVideo(boolean mute) {
        linkMicPresenter.muteVideo(mute);
    }

    @Override
    public void switchCamera(boolean front) {
        linkMicPresenter.switchCamera(front);
    }

    @Override
    public void startLesson(IPLVDataRequestListener<String> listener) {
        linkMicPresenter.startLesson(listener);
    }

    @Override
    public void stopLesson(IPLVDataRequestListener<String> listener) {
        PLVHCLinkMicZoomManager.getInstance().zoomOutAll();
        linkMicPresenter.stopLesson(listener);
    }

    @Override
    public void sendRaiseHandEvent(int raiseHandTime) {
        linkMicPresenter.sendRaiseHandEvent(raiseHandTime);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    @Override
    public boolean isLessonStarted() {
        return PLVHiClassConstant.LESSON_STARTED == linkMicPresenter.getLessonStatus();
    }

    @Override
    public IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter getLinkMicPresenter() {
        return linkMicPresenter;
    }

    @Override
    public void destroy() {
        if (linkMicPresenter != null) {
            linkMicPresenter.destroy();
        }
        if (linkMicItemLayout != null) {
            linkMicItemLayout.destroy();
        }
        PLVForegroundService.stopForegroundService();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦 - MVP模式的view层实现">
    private PLVAbsMultiRoleLinkMicView linkMicView = new PLVAbsMultiRoleLinkMicView() {

        @Override
        public void setPresenter(@NonNull final IPLVMultiRoleLinkMicContract.IMultiRoleLinkMicPresenter presenter) {
            presenter.getData().getLimitLinkNumber().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {
                    presenter.getData().getLimitLinkNumber().removeObserver(this);
                    if (integer == null) {
                        return;
                    }
                    initLinkMicItemLayout(integer <= 6);
                }
            });
        }

        @Override
        public void onLinkMicEngineCreatedSuccess() {
            super.onLinkMicEngineCreatedSuccess();
            post(new Runnable() {
                @Override
                public void run() {
                    if (linkMicPresenter == null) {
                        return;
                    }
                    // 互动学堂固定横屏推流
                    linkMicPresenter.setPushPictureResolutionType(PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE);
                }
            });
        }

        @Override
        public void onLinkMicError(int errorCode, Throwable throwable) {
            if (errorCode == IPLVStreamerManager.ERROR_PERMISSION_DENIED) {
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
                PLVHCToast.Builder.context(getContext())
                        .setText(throwable.getMessage())
                        .build()
                        .show();
            }
        }

        @Override
        public void onInitLinkMicList(String myLinkMicId, final List<PLVLinkMicItemDataBean> linkMicList) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.bindData(linkMicList, linkMicPresenter.isJoinDiscuss());
                    if (!linkMicPresenter.isTeacherType() && !linkMicPresenter.isJoinDiscuss()) {
                        PLVLinkMicItemDataBean placeLinkMicItem = new PLVLinkMicItemDataBean();
                        placeLinkMicItem.setNick(teacherNick);
                        linkMicItemLayout.setPlaceLinkMicItem(placeLinkMicItem, false);
                    }
                }
            });
        }

        @Override
        public void onUsersJoin(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserJoin(linkMicItemDataBean, position);
                    checkHideControlWindow();
                }
            });
            toastUserLinkMicMsg(linkMicItemDataBean, true);
        }

        @Override
        public void onUsersLeave(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    if (linkMicItemDataBean != null
                            && linkMicItemDataBean.getLinkMicId() != null
                            && PLVHCLinkMicZoomManager.getInstance().isZoomIn(linkMicItemDataBean.getLinkMicId())) {
                        PLVHCLinkMicZoomManager.getInstance().zoomOut(linkMicItemDataBean.getLinkMicId());
                    }

                    linkMicItemLayout.onUserLeave(linkMicItemDataBean, position);
                    checkHideControlWindow();
                }
            });
            toastUserLinkMicMsg(linkMicItemDataBean, false);
        }

        @Override
        public void onUserExisted(final PLVLinkMicItemDataBean linkMicItemDataBean, final int position) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserExisted(linkMicItemDataBean, position);
                }
            });
        }

        @Override
        public void onTeacherScreenStream(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isOpen) {
            if (isOpen) {
                View renderView = linkMicPresenter.createRenderView(getContext());
                linkMicPresenter.setupRenderView(renderView, linkMicItemDataBean.getLinkMicId(), PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_SCREEN);
                teacherScreenStreamLayout.show(linkMicItemDataBean.getLinkMicId(), renderView);
            } else {
                teacherScreenStreamLayout.hide(linkMicItemDataBean.getLinkMicId(), new PLVHCTeacherScreenStreamLayout.CallParamRunnable<View>() {
                    @Override
                    public void run(View renderView) {
                        linkMicPresenter.releaseRenderView(renderView);
                    }
                });
            }
        }

        @Override
        public void onLinkMicListChanged(final List<PLVLinkMicItemDataBean> dataBeanList) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updateListData(dataBeanList);
                }
            });
        }

        @Override
        public void onUserRaiseHand(int raiseHandCount, boolean isRaiseHand, final int linkMicListPos, int memberListPos) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserRaiseHand(linkMicListPos);
                }
            });
            if (onViewActionListener != null) {
                onViewActionListener.onUserRaiseHand(raiseHandCount, isRaiseHand);
            }
        }

        @Override
        public void onUserGetCup(String userNick, boolean isByEvent, final int linkMicListPos, int memberListPos) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserGetCup(linkMicListPos);
                }
            });
            if (isByEvent) {
                if (onViewActionListener != null) {
                    onViewActionListener.onGetCup(userNick);
                }
            }
        }

        @Override
        public void onUserHasPaint(boolean isMyself, boolean isHasPaint, final int linkMicListPos, int memberListPos) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserHasPaint(linkMicListPos);
                    checkUpdateControlWindow(linkMicListPos);
                }
            });
            if (isMyself) {
                if (isHasPaintPermission != isHasPaint) {
                    isHasPaintPermission = isHasPaint;
                    PLVHCToast.Builder.context(getContext())
                            .setDrawable(isHasPaint ? R.drawable.plvhc_member_list_paint : R.drawable.plvhc_member_list_paint_disable)
                            .setText("老师已" + (isHasPaint ? "授权" : "收回") + "你画笔权限")
                            .build()
                            .show();
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onHasPaintToMe(isHasPaint);
                }
            }
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, final int linkMicListPos, int memberListPos) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updateUserMuteVideo(linkMicListPos);
                    checkUpdateControlWindow(linkMicListPos);
                }
            });
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, final int linkMicListPos, int memberListPos) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updateUserMuteAudio(linkMicListPos);
                    checkUpdateControlWindow(linkMicListPos);
                }
            });
        }

        @Override
        public void onTeacherMuteMyMedia(boolean isVideoType, boolean isMute) {
            if (isVideoType) {
                PLVHCToast.Builder.context(getContext())
                        .setDrawable(!isMute ? R.drawable.plvhc_member_camera : R.drawable.plvhc_member_camera_sel)
                        .setText((!isMute ? "讲师已开启" : "讲师已关闭") + "你的摄像头")
                        .build()
                        .show();
            } else {
                PLVHCToast.Builder.context(getContext())
                        .setDrawable(!isMute ? R.drawable.plvhc_member_mic : R.drawable.plvhc_member_mic_sel)
                        .setText((!isMute ? "讲师已开启" : "讲师已关闭") + "你的麦克风")
                        .build()
                        .show();
            }
        }

        @Override
        public void onTeacherControlMyLinkMic(boolean isAllowJoin) {
            PLVHCToast.Builder.context(getContext())
                    .setDrawable(isAllowJoin ? R.drawable.plvhc_linkmic_join_status : 0)
                    .setText(isAllowJoin ? "你已上台" : "你已被老师请下台")
                    .build()
                    .show();
        }

        @Override
        public boolean onUserNeedAnswerLinkMic() {
            showInvitationCountdownWindow();
            return true;
        }

        @Override
        public void onLocalUserVolumeChanged(int volume) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updateVolumeChanged();
                }
            });
        }

        @Override
        public void onRemoteUserVolumeChanged() {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updateVolumeChanged();
                }
            });
        }

        @Override
        public void onReachTheInteractNumLimit() {
            new PLVConfirmDialog(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent(R.string.plv_linkmic_dialog_reach_the_interact_num_limit)
                    .setIsNeedLeftBtn(false)
                    .setRightButtonText(R.string.plv_common_dialog_alright)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        public void onRepeatLogin(String desc) {
            new PLVConfirmDialog(getContext())
                    .setTitleVisibility(View.GONE)
                    .setContent(desc)
                    .setIsNeedLeftBtn(false)
                    .setCancelable(false)
                    .setRightButtonText(R.string.plv_common_dialog_alright)
                    .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, View v) {
                            dialog.dismiss();
                            ((Activity) getContext()).finish();
                        }
                    })
                    .show();
        }

        @Override
        public void onRejoinRoomSuccess() {
            if (linkMicItemLayout != null) {
                linkMicItemLayout.notifyRejoinRoom();
            }
        }

        @Override
        public void onNetworkQuality(int quality) {
            if (onViewActionListener != null) {
                onViewActionListener.onNetworkQuality(quality);
            }
        }

        @Override
        public void onUpstreamNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
            if (onViewActionListener != null) {
                onViewActionListener.onUpstreamNetworkStatus(networkStatusVO);
            }
        }

        @Override
        public void onRemoteNetworkStatus(PLVNetworkStatusVO networkStatusVO) {
            if (onViewActionListener != null) {
                onViewActionListener.onRemoteNetworkStatus(networkStatusVO);
            }
        }

        @Override
        public void onTeacherInfo(String nick) {
            teacherNick = nick;
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.updatePlaceLinkMicItemNick(teacherNick);
                }
            });
        }

        @Override
        public void onLessonPreparing(long serverTime, long lessonStartTime) {
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    if (!linkMicPresenter.isTeacherType() && !linkMicPresenter.isJoinDiscuss()) {
                        PLVLinkMicItemDataBean placeLinkMicItem = new PLVLinkMicItemDataBean();
                        placeLinkMicItem.setNick(teacherNick);
                        linkMicItemLayout.setPlaceLinkMicItem(placeLinkMicItem, true);
                    }
                }
            });
            if (onViewActionListener != null) {
                onViewActionListener.onLessonPreparing(serverTime, lessonStartTime);
            }
        }

        @Override
        public void onLessonStarted() {
            classStopNoNextDialog.hide();
            classStopHasNextDialog.hide();
            if (onViewActionListener != null) {
                onViewActionListener.onLessonStarted();
            }
        }

        @Override
        public void onLessonEnd(long inClassTime, boolean isFromApi, @Nullable PLVHCStudentLessonListVO.DataVO dataVO) {
            if (!linkMicPresenter.isTeacherType() || !isFromApi) {
                if (dataVO != null) {
                    classStopHasNextDialog.setInClassTime(inClassTime)
                            .setClassTitle(dataVO.getName())
                            .setClassStartTime(dataVO.getStartTime())
                            .setOnPositiveListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ((Activity) getContext()).finish();
                                }
                            })
                            .show();
                } else {
                    String positiveText = linkMicPresenter.isTeacherType() ? "查看课节" : "确定";
                    classStopNoNextDialog.setInClassTime(inClassTime)
                            .setPositiveText(positiveText)
                            .setOnPositiveListener(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    ((Activity) getContext()).finish();
                                }
                            })
                            .show();
                }
            }
            if (onViewActionListener != null) {
                onViewActionListener.onLessonEnd(inClassTime, linkMicPresenter.isTeacherType(), dataVO != null);
            }
        }

        @Override
        public void onLessonLateTooLong(long willAutoStopLessonTimeMs) {
            int timeMinute = (int) (willAutoStopLessonTimeMs / 1000f / 60);
            PLVHCToast.Builder.context(getContext())
                    .setText("拖堂时间过长，" + timeMinute + "分钟后将强制下课")
                    .build()
                    .show();
        }

        @Override
        public void onUserHasGroupLeader(boolean isHasGroupLeader, String nick, boolean isGroupChanged, boolean isLeaderChanged, String groupName, @Nullable final String leaderId) {
            isGroupLeader = isHasGroupLeader;
            hideControlWindow();
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserHasLeader(leaderId);
                }
            });
            new PLVHCSeminarCountdownWindow(PLVHCLinkMicLayout.this).acceptOnUserHasGroupLeader(isHasGroupLeader, nick, isGroupChanged, isLeaderChanged, groupName);
            if (onViewActionListener != null) {
                onViewActionListener.onUserHasGroupLeader(isHasGroupLeader);
            }
        }

        @Override
        public void onWillJoinDiscuss(long countdownTimeMs) {
            // 加入分组前，清空摄像头放大区域
            PLVHCLinkMicZoomManager.getInstance().zoomOutAll();
            // 倒计时提示即将加入分组
            new PLVHCJoinDiscussCountDownWindow(PLVHCLinkMicLayout.this).acceptOnWillJoinDiscuss(countdownTimeMs);
        }

        @Override
        public void onJoinDiscuss(String groupId, String groupName, @Nullable PLVSwitchRoomEvent switchRoomEvent) {
            isGroupLeader = false;
            hideControlWindow();
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.clearData(true);
                }
            });
            if (onViewActionListener != null) {
                onViewActionListener.onJoinDiscuss(groupId, groupName, switchRoomEvent);
            }
        }

        @Override
        public void onLeaveDiscuss(@Nullable PLVSwitchRoomEvent switchRoomEvent) {
            isGroupLeader = false;
            hideControlWindow();
            new PLVHCSeminarCountdownWindow(PLVHCLinkMicLayout.this).acceptOnLeaveDiscuss();
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.clearData(false);
                }
            });
            if (onViewActionListener != null) {
                onViewActionListener.onLeaveDiscuss(switchRoomEvent);
            }
        }

        @Override
        public void onTeacherJoinDiscuss(boolean isJoin) {
            if (isJoin) {
                PLVHCToast.Builder.context(getContext())
                        .setText("老师已进入分组")
                        .build()
                        .show();
            }
        }

        @Override
        public void onTeacherSendBroadcast(String content) {
            if (teacherSendBroadcastDialog != null) {
                teacherSendBroadcastDialog.hide();
            }
            teacherSendBroadcastDialog = new PLVHCReceiveBroadcastDialog(getContext());
            teacherSendBroadcastDialog.setContent(content);
            teacherSendBroadcastDialog.show();
        }

        @Override
        public void onLeaderRequestHelp() {
            if (onViewActionListener != null) {
                onViewActionListener.onLeaderRequestHelp();
            }
        }

        @Override
        public void onLeaderCancelHelp() {
            if (onViewActionListener != null) {
                onViewActionListener.onLeaderCancelHelp();
            }
        }

        @Override
        public void onUpdateLinkMicZoom(final PLVUpdateMicSiteEvent updateMicSiteEvent) {
            if (updateMicSiteEvent == null || !updateMicSiteEvent.checkIsValid()) {
                return;
            }
            final String linkMicId = updateMicSiteEvent.getLinkMicIdFromEventId();
            if (PLVHCLinkMicZoomManager.getInstance().isZoomIn(linkMicId)) {
                PLVHCLinkMicZoomManager.getInstance().notifyUpdateMicSite(updateMicSiteEvent);
            } else {
                runTaskAfterInitItemLayout(new Runnable() {
                    @Override
                    public void run() {
                        linkMicItemLayout.onUserUpdateZoom(updateMicSiteEvent);
                    }
                });
            }
        }

        @Override
        public void onRemoveLinkMicZoom(final PLVRemoveMicSiteEvent removeMicSiteEvent) {
            if (removeMicSiteEvent == null || removeMicSiteEvent.getLinkMicIdFromEventId() == null) {
                return;
            }
            runTaskAfterInitItemLayout(new Runnable() {
                @Override
                public void run() {
                    linkMicItemLayout.onUserRemoveZoom(removeMicSiteEvent);
                }
            });
            PLVHCLinkMicZoomManager.getInstance().zoomOut(removeMicSiteEvent.getLinkMicIdFromEventId());
        }

        @Override
        public void onChangeLinkMicZoom(@Nullable final Map<String, PLVUpdateMicSiteEvent> updateMicSiteEventMap) {
            Map<String, PLVUpdateMicSiteEvent> changeMap = updateMicSiteEventMap;
            if (updateMicSiteEventMap == null || updateMicSiteEventMap.isEmpty()) {
                changeMap = Collections.emptyMap();
            }
            PLVHCLinkMicZoomManager.getInstance().zoomOutAllExcept(changeMap.keySet());
            for (final PLVUpdateMicSiteEvent updateMicSiteEvent : changeMap.values()) {
                onUpdateLinkMicZoom(updateMicSiteEvent);
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="显示上台邀请倒计时弹层">
    private void showInvitationCountdownWindow() {
        if (linkMicInvitationCountdownWindow == null) {
            linkMicInvitationCountdownWindow = new PLVHCLinkMicInvitationCountdownWindow(this);
            linkMicInvitationCountdownWindow.setOnAnswerListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkMicPresenter.answerLinkMicInvitation();
                }
            });
        }
        linkMicInvitationCountdownWindow.show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制成员媒体、权限方法">
    private void controlUserCamera(boolean isMyself, final boolean isWillOpen, int position) {
        if (isMyself) {
            linkMicPresenter.muteVideo(!isWillOpen);
            PLVHCToast.Builder.context(getContext())
                    .setDrawable(isWillOpen ? R.drawable.plvhc_member_camera : R.drawable.plvhc_member_camera_sel)
                    .setText((isWillOpen ? "已开启" : "已关闭") + "摄像头")
                    .build()
                    .show();
        } else {
            linkMicPresenter.setMediaPermissionInLinkMicList(position, true, !isWillOpen, new Ack() {
                @Override
                public void call(Object... args) {
                    PLVHCToast.Builder.context(getContext())
                            .setDrawable(!isWillOpen ? R.drawable.plvhc_member_camera_sel : R.drawable.plvhc_member_camera)
                            .setText(!isWillOpen ? "已关闭该学生摄像头" : "已开启该学生摄像头")
                            .build()
                            .show();
                }
            });
        }
    }

    private void controlUserMic(boolean isMyself, final boolean isWillOpen, int position) {
        if (isMyself) {
            linkMicPresenter.muteAudio(!isWillOpen);
            PLVHCToast.Builder.context(getContext())
                    .setDrawable(isWillOpen ? R.drawable.plvhc_member_mic : R.drawable.plvhc_member_mic_sel)
                    .setText((isWillOpen ? "已开启" : "已关闭") + "麦克风")
                    .build()
                    .show();
        } else {
            linkMicPresenter.setMediaPermissionInLinkMicList(position, false, !isWillOpen, new Ack() {
                @Override
                public void call(Object... args) {
                    PLVHCToast.Builder.context(getContext())
                            .setDrawable(!isWillOpen ? R.drawable.plvhc_member_mic_sel : R.drawable.plvhc_member_mic)
                            .setText(!isWillOpen ? "已关闭该学生麦克风" : "已开启该学生麦克风")
                            .build()
                            .show();
                }
            });
        }
    }

    private void controlUserPaint(final boolean isHasPaint, int position) {
        linkMicPresenter.setPaintPermissionInLinkMicList(position, isHasPaint, new Ack() {
            @Override
            public void call(Object... args) {
                PLVHCToast.Builder.context(getContext())
                        .setDrawable(isHasPaint ? R.drawable.plvhc_member_list_paint : R.drawable.plvhc_member_list_paint_disable)
                        .setText(isHasPaint ? "已授权该学生画笔" : "已收回该学生画笔")
                        .build()
                        .show();
            }
        });
    }

    private void controlUserCup(final String nick, int position) {
        linkMicPresenter.sendCupEvent(position, new Ack() {
            @Override
            public void call(Object... args) {
                PLVHCToast.Builder.context(getContext())
                        .setDrawable(R.drawable.plvhc_linkmic_cup_l)
                        .setText(nick + "被奖励一个奖杯")
                        .build()
                        .show();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新成员控制弹层">
    private void hideControlWindow() {
        if (linkMicUserControlDialog != null && linkMicUserControlDialog.isShowing()) {
            linkMicUserControlDialog.hide();
        }
    }

    private void checkHideControlWindow() {
        if (linkMicItemLayout == null) {
            return;
        }
        if (linkMicUserControlDialog != null && linkMicUserControlDialog.isShowing()) {
            for (int i = 0; i < linkMicItemLayout.getDataBeanList().size(); i++) {
                PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItemLayout.getDataBeanList().get(i);
                if (linkMicItemDataBean.getLinkMicId() != null && linkMicItemDataBean.getLinkMicId().equals(linkMicUserControlDialog.getLinkMicUid())) {
                    return;
                }
            }
            linkMicUserControlDialog.hide();
        }
    }

    private void checkUpdateControlWindow(int pos) {
        if (linkMicItemLayout == null) {
            return;
        }
        if (pos >= 0 && linkMicUserControlDialog != null && linkMicUserControlDialog.isShowing()) {
            PLVLinkMicItemDataBean linkMicItemDataBean = linkMicItemLayout.getDataBeanList().get(pos);
            if (linkMicItemDataBean.getLinkMicId() != null && linkMicItemDataBean.getLinkMicId().equals(linkMicUserControlDialog.getLinkMicUid())) {
                linkMicUserControlDialog.bindViewData(linkMicItemDataBean, linkMicPresenter.isMyLinkMicId(linkMicItemDataBean.getLinkMicId()), isGroupLeader);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void toastUserLinkMicMsg(PLVLinkMicItemDataBean linkMicItemDataBean, boolean isJoin) {
        if (linkMicPresenter == null || !linkMicPresenter.isTeacherType() || !isLessonStarted()) {
            return;
        }
        if (linkMicItemDataBean == null || linkMicPresenter.isMyLinkMicId(linkMicItemDataBean.getLinkMicId())) {
            return;
        }
        PLVHCToast.Builder.context(getContext())
                .setDrawable(isJoin ? R.drawable.plvhc_member_up : R.drawable.plvhc_member_down_all)
                .setText(linkMicItemDataBean.getNick() + "已" + (isJoin ? "上台" : "下台"))
                .build()
                .show();
    }

    private void runTaskAfterInitItemLayout(Runnable task) {
        if (linkMicItemLayout != null) {
            task.run();
        } else {
            initiatedTasks.add(task);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听课节详情数据">
    private void observeLessonDataBean() {
        liveRoomDataManager.getFulHiClassDataBean().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PLVHiClassDataBean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PLVHiClassDataBean> hiClassFulDataBean) {
                if (hiClassFulDataBean != null && hiClassFulDataBean.isError()) {
                    final String[] content = {""};
                    hiClassFulDataBean.ifError(new PLVStatefulData.ErrorHandler() {
                        @Override
                        public void error(String errorMsg, Throwable throwable) {
                            if (throwable instanceof UnknownHostException) {
                                content[0] = "当前网络不可用，请检查网络设置";
                            } else {
                                content[0] = errorMsg;
                            }
                        }
                    });
                    new PLVHCConfirmDialog(getContext())
                            .setTitle("获取课节信息失败")
                            .setContent(content[0])
                            .setCancelable(false)
                            .setLeftButtonText("退出")
                            .setLeftBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    ((Activity) getContext()).finish();
                                }
                            })
                            .setRightButtonText("重试")
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    liveRoomDataManager.requestLessonDetail();
                                }
                            })
                            .show();
                }
            }
        });
    }
    // </editor-fold>
}
