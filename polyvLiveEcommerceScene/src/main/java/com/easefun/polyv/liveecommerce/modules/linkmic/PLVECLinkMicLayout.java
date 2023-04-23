package com.easefun.polyv.liveecommerce.modules.linkmic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicListShowMode;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVLinkMicPresenter;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVViewerLinkMicState;
import com.easefun.polyv.livecommon.module.utils.PLVForegroundService;
import com.easefun.polyv.livecommon.module.utils.PLVNotchUtils;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVViewSwitcher;
import com.easefun.polyv.livecommon.ui.widget.PLVNoInterceptTouchRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVPlayerLogoView;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.linkmic.adapter.PLVLinkMicListAdapter;
import com.easefun.polyv.liveecommerce.modules.linkmic.widget.PLVECLinkMicInvitationLayout;
import com.easefun.polyv.liveecommerce.modules.linkmic.widget.PLVLinkMicRvLandscapeItemDecoration;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * date: 2020/7/16
 * author: hwj
 * description: 连麦布局实现
 */
public class PLVECLinkMicLayout extends FrameLayout implements IPLVLinkMicContract.IPLVLinkMicView, IPLVECLinkMicLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVECLinkMicLayout.class.getSimpleName();

    // 连麦权限请求失败错误码
    private static final int ERROR_PERMISSION_DENIED = 1060501;

    //尺寸
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_LEFT = 8;
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT = 34;
    private static final int DP_LAND_SPEAKING_USER_VIEW_MARGIN_RIGHT_TO_LINK_MIC_LIST = 24;

    //Presenter
    private IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter;

    //View
    private IPLVECLinkMicControlBar linkMicControlBar;
    private FrameLayout flMediaLinkMicRoot;
    private PLVNoInterceptTouchRecyclerView rvLinkMicList;
    //连麦列表布局管理
    private GridLayoutManager gridLayoutManager;
    private final PLVECLinkMicInvitationLayout linkMicInvitationLayout = new PLVECLinkMicInvitationLayout(getContext());
    //连麦列表适配器
    private PLVLinkMicListAdapter linkMicListAdapter;
    private PLVLinkMicRvLandscapeItemDecoration landscapeItemDecoration = new PLVLinkMicRvLandscapeItemDecoration();

    //纯视频频道连麦时，讲师的位置切换器
    private PLVViewSwitcher teacherLocationViewSwitcher;

    private final List<Runnable> onUserJoinPendingTask = new LinkedList<>();

    //Listener
    private OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener;

    //状态数据
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //media区是否显示在连麦列表
    private boolean isMediaShowInLinkMicList = false;
    //media在连麦列表中对应item的连麦id
    @Nullable
    private String mediaInLinkMicListLinkMicId;
    private PLVLiveChannelType liveChannelType;

    //横屏时的宽度
    private int landscapeWidth = 0;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVECLinkMicLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVECLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVECLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_linkmic_media_layout, this, true);
        flMediaLinkMicRoot = findViewById(R.id.plvec_linkmic_fl_media_linkmic_root);
        rvLinkMicList = findViewById(R.id.plvec_link_mic_rv_linkmic_list);

        //init RecyclerView
        rvLinkMicList.addItemDecoration(landscapeItemDecoration);
        //禁用RecyclerView默认动画
        rvLinkMicList.getItemAnimator().setAddDuration(0);
        rvLinkMicList.getItemAnimator().setChangeDuration(0);
        rvLinkMicList.getItemAnimator().setMoveDuration(0);
        rvLinkMicList.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = rvLinkMicList.getItemAnimator();
        if (rvAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
        }

        gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvLinkMicList.setLayoutManager(gridLayoutManager);

        //init adapter
        linkMicListAdapter = new PLVLinkMicListAdapter(rvLinkMicList, gridLayoutManager, new PLVLinkMicListAdapter.OnPLVLinkMicAdapterCallback() {
            @Override
            public SurfaceView createLinkMicRenderView() {
                return linkMicPresenter.createRenderView(Utils.getApp());
            }

            @Override
            public void setupRenderView(SurfaceView surfaceView, String linkMicId) {
                linkMicPresenter.setupRenderView(surfaceView, linkMicId);
            }

            @Override
            public void releaseRenderView(SurfaceView surfaceView) {
                linkMicPresenter.releaseRenderView(surfaceView);
            }

            @Override
            public void muteAudioVideo(String linkMicId, boolean mute) {
                linkMicPresenter.muteAudio(linkMicId, mute);
                linkMicPresenter.muteVideo(linkMicId, mute);
            }

            @Override
            public void muteAllAudioVideo(boolean mute) {
                linkMicPresenter.muteAllAudio(mute);
                linkMicPresenter.muteAllVideo(mute);
            }

            @Override
            public void onClickItemListener(int pos, @Nullable PLVSwitchViewAnchorLayout switchViewHasMedia, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
            }
        });

        linkMicInvitationLayout.setOnViewActionListener(new PLVECLinkMicInvitationLayout.OnViewActionListener() {
            @Override
            public void answerLinkMicInvitation(boolean accept, int cancelBy, boolean openCamera, boolean openMicrophone) {
                linkMicPresenter.answerLinkMicInvitation(accept, cancelBy == PLVECLinkMicInvitationLayout.CANCEL_BY_TIMEOUT, openCamera, openMicrophone);
            }

            @Override
            public void asyncGetAcceptInvitationLeftTimeInSecond(PLVSugarUtil.Consumer<Integer> callback) {
                linkMicPresenter.getJoinAnswerTimeLeft(callback);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化连麦控制条">
    private void initLinkMicControlBar(IPLVECLinkMicControlBar linkMicControlBar) {
        if (linkMicControlBar == null) {
            PLVCommonLog.exception(new Throwable("linkMicController == null"));
            return;
        }
        this.linkMicControlBar = linkMicControlBar;
        //监听连麦控制器的各种点击事件
        linkMicControlBar.setOnPLCLinkMicControlBarListener(new IPLVECLinkMicControlBar.OnPLCLinkMicControlBarListener() {
            @Override
            public void onClickRingUpLinkMic() {
                linkMicPresenter.requestJoinLinkMic();
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onRequestJoinLinkMic();
                }
            }

            @Override
            public void onClickRingOffLinkMic() {
                if (linkMicPresenter.isJoinLinkMic()) {
                    linkMicPresenter.leaveLinkMic();
                } else {
                    linkMicPresenter.cancelRequestJoinLinkMic();
                    if (onPLVLinkMicLayoutListener != null) {
                        onPLVLinkMicLayoutListener.onCancelRequestJoinLinkMic();
                    }
                }
            }

            @Override
            public void onClickCameraOpenOrClose(boolean toClose) {
                linkMicPresenter.muteVideo(toClose);
            }

            @Override
            public void onClickCameraFrontOfBack(boolean toFront) {
                linkMicPresenter.switchCamera();
            }

            @Override
            public void onClickMicroPhoneOpenOrClose(boolean toClose) {
                linkMicPresenter.muteAudio(toClose);
            }
        });
    }

    private void observeLinkMicQueueOrder() {
        linkMicPresenter.getLinkMicRequestQueueOrder().observe((LifecycleOwner) getContext(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer orderIndex) {
                if (orderIndex == null) {
                    return;
                }
                if (PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                        .isFeatureSupport(PLVChannelFeature.LIVE_LINK_MIC_SHOW_REQUEST_ORDER)) {
                    linkMicControlBar.updateLinkMicQueueOrder(orderIndex);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 外部直接调用的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, IPLVECLinkMicControlBar linkMicControlBar) {
        this.liveRoomDataManager = liveRoomDataManager;
        liveChannelType = liveRoomDataManager.getConfig().getChannelType();
        linkMicPresenter = new PLVLinkMicPresenter(liveRoomDataManager, this);
        linkMicPresenter.setEcommerceLinkMicItemSort(true);
        initLinkMicControlBar(linkMicControlBar);
        updatePushResolution(false);
        observeOnAudioState(liveRoomDataManager);
        observeLinkMicQueueOrder();
    }


    @Override
    public void destroy() {
        linkMicPresenter.destroy();
        linkMicInvitationLayout.destroy();
    }

    @Override
    public void showAll() {
        PLVCommonLog.d(TAG, "show");
        showLinkMicList();
        showControlBar();
    }

    @Override
    public void hideAll() {
        PLVCommonLog.d(TAG, "hide");
        hideLinkMicList();
        linkMicControlBar.hide();
    }

    @Override
    public void hideLinkMicList() {
        PLVCommonLog.d(TAG, "hideOnlyLinkMicList");
        linkMicListAdapter.hideAllRenderView();
        //在最后用post调用自身的隐藏，好让子View的一些布局操作能得到执行。
        post(new Runnable() {
            @Override
            public void run() {
                setVisibility(GONE);
            }
        });
    }

    @Override
    public void showLinkMicList() {
        setVisibility(VISIBLE);
        linkMicListAdapter.showAllRenderView();
        //可能在隐藏连麦列表的时候，连麦列表的控件发生了UI改变，但是由于连麦列表不可见，所以没有做出布局改变，
        // 那么在显示连麦列表的时候，主动更新整个连麦列表的UI。
        linkMicListAdapter.updateAllItem();
        updateLinkMicListLayout();
    }

    @Override
    public void hideControlBar() {
        PLVCommonLog.d(TAG, "hide");
        //横屏的情况下，才将连麦控制条隐藏
    }

    @Override
    public void showControlBar() {
        if (linkMicControlBar != null) {
            linkMicControlBar.show();
        }
    }

    @Override
    public void pause() {
        linkMicListAdapter.pauseAllRenderView();
    }

    @Override
    public void resume() {
        linkMicListAdapter.resumeAllRenderView();
    }

    @Override
    public boolean isPausing() {
        return linkMicListAdapter.isPausing();
    }

    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        linkMicControlBar.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
        linkMicPresenter.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
    }


    @Override
    public void setIsAudio(boolean isAudioLinkMic) {
        linkMicControlBar.setAudioState(isAudioLinkMic);
        linkMicPresenter.setIsAudioLinkMic(isAudioLinkMic);
    }

    @Override
    public boolean isJoinChannel() {
        return linkMicPresenter.isJoinChannel();
    }

    @Override
    public boolean isMediaShowInLinkMicList() {
        return isMediaShowInLinkMicList;
    }

    @Override
    public void switchMediaToMainScreen() {

    }

    @Override
    public void notifySwitchedPptToMainScreenOnJoinChannel() {
    }

    @Override
    public int getMediaViewIndexInLinkMicList() {
        return linkMicListAdapter.getMediaViewIndexInLinkMicList();
    }

    @Override
    public void performClickInLinkMicListItem(final int index) {
        rvLinkMicList.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder viewHolder = rvLinkMicList.findViewHolderForAdapterPosition(index);
                if (viewHolder != null) {//连续多次提交时，由于adapter的item的onclick里触发了notifyDataSetChanged，可能会导致后面获取到的viewHolder为null
                    viewHolder.itemView.performClick();
                }
            }
        });
    }

    @Override
    public void updateAllLinkMicList() {
        linkMicListAdapter.updateAllItem();

        updateLinkMicListLayout();
    }

    @Override
    public void setOnPLVLinkMicLayoutListener(OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener) {
        this.onPLVLinkMicLayoutListener = onPLVLinkMicLayoutListener;
    }


    @Override
    public int getLandscapeWidth() {
        return landscapeWidth;
    }

    @Override
    public void setLiveStart() {
        linkMicPresenter.setLiveStart();
    }

    @Override
    public void setLiveEnd() {
        linkMicPresenter.setLiveEnd();
    }

    @Override
    public void setWatchLowLatency(boolean watchLowLatency) {
        if (PLVLinkMicConfig.getInstance().isLowLatencyPureRtcWatch()) {
            linkMicPresenter.setWatchRtc(watchLowLatency);
        }
    }

    @Override
    public void setLogoView(PLVPlayerLogoView plvPlayerLogoView) {
        linkMicListAdapter.setPlvPlayerLogoView(plvPlayerLogoView);
    }

    @Override
    public boolean onRvSuperTouchEvent(MotionEvent ev) {
        boolean returnResult = rvLinkMicList.onSuperTouchEvent(ev);
        return returnResult;
    }

    @Override
    public void onRTCPrepared() {
        onPLVLinkMicLayoutListener.onRTCPrepared();
    }

    @Override
    public void updateFirstScreenChanged(String firstScreenLinkMicId, int oldPos, int newPos) {
        if (oldPos >= 0 && newPos >= 0 && oldPos != newPos) {
            List<PLVLinkMicItemDataBean> linkMicList = linkMicListAdapter.getDataList();
            //遍历找到target
            PLVLinkMicItemDataBean itemToBeFirst = linkMicList.get(newPos);
            //2. 将原先的第一画面和新的第一画面的位置进行切换
            PLVLinkMicItemDataBean oldFirst = linkMicList.get(oldPos);
            oldFirst.setFirstScreen(false);
            itemToBeFirst.setFirstScreen(true);
            linkMicList.remove(oldFirst);
            linkMicList.remove(itemToBeFirst);
            linkMicList.add(0, itemToBeFirst);
            linkMicList.add(newPos, oldFirst);
            PLVLinkMicPresenter.SortLinkMicListUtils.sort(linkMicList);
            onSwitchFirstScreen(firstScreenLinkMicId);
        }

        linkMicListAdapter.setFirstScreenLinkMicId(firstScreenLinkMicId);
        if (oldPos > 0) {
            linkMicListAdapter.updateUserMuteVideo(oldPos);
        }
        if (newPos > 0) {
            linkMicListAdapter.updateUserMuteVideo(newPos);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVLinkMicContract.IPLVLinkMicView实现">

    @Override
    public void onLinkMicError(int errorCode, Throwable throwable) {
        PLVCommonLog.exception(throwable);
        if (errorCode == ERROR_PERMISSION_DENIED) {
            new AlertDialog.Builder(getContext()).setTitle(R.string.plv_common_dialog_tip)
                    .setMessage(R.string.plv_linkmic_error_tip_permission_denied)
                    .setPositiveButton(R.string.plv_common_dialog_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVFastPermission.getInstance().jump2Settings(getContext());
                        }
                    })
                    .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), R.string.plv_linkmic_error_tip_permission_cancel, Toast.LENGTH_SHORT).show();
                        }
                    }).setCancelable(false).show();
        } else {
            ToastUtils.showShort(getResources().getString(R.string.plv_linkmic_toast_error) + errorCode);
        }
    }

    @Override
    public void onTeacherOpenLinkMic() {
        //教师打开连麦
        linkMicControlBar.setAudioState(linkMicPresenter.getIsAudioLinkMic());
        linkMicControlBar.setIsTeacherOpenLinkMic(true);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onChannelLinkMicOpenStatusChanged(true);
        }
    }

    @Override
    public void onTeacherCloseLinkMic() {
        //教师关闭连麦
        linkMicControlBar.setIsTeacherOpenLinkMic(false);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onChannelLinkMicOpenStatusChanged(false);
        }
    }

    @Override
    public void onTeacherAllowJoin() {
        PLVCommonLog.d(TAG, "onTeacherAllowJoin");
    }

    @Override
    public void onLinkMicStateChanged(PLVViewerLinkMicState oldState, PLVViewerLinkMicState newState) {
        linkMicInvitationLayout.setIsOnlyAudio(linkMicPresenter.getIsAudioLinkMic());
        linkMicInvitationLayout.onLinkMicStateChanged(oldState, newState);
    }

    @Override
    public void onJoinChannelTimeout() {
        ToastUtils.showShort("加入频道超时，请重试");
    }

    @Override
    public void onLinkMicMemberReachLimit() {
        PLVToast.Builder.context(getContext()).setText("连麦人数已达上限").show();
    }

    @Override
    public void onPrepareLinkMicList(String linkMicUid, PLVLinkMicListShowMode linkMicListShowMode, List<PLVLinkMicItemDataBean> linkMicList) {
        PLVCommonLog.d(TAG, "PLVLinkMicLayout.onBeforeJoinChannel");
        //初始化连麦适配器，准备添加连麦观众
        linkMicListAdapter.setDataList(linkMicList);
        linkMicListAdapter.setListShowMode(linkMicListShowMode);
        linkMicListAdapter.setMyLinkMicId(linkMicUid);

        rvLinkMicList.setAdapter(linkMicListAdapter);

        //如果是刘海屏，则有横屏右边距；如果不是刘海屏，则横屏右边距为0
        int marginRight = PLVNotchUtils.hasNotchInScreen((Activity) getContext()) ? PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT) : 0;
        landscapeWidth = linkMicListAdapter.getItemWidth() + PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_LEFT) + marginRight;
        //上麦后要根据当前的屏幕方向再做一次变换。
        onPortrait();

        linkMicControlBar.updateIsAudioWidth(linkMicPresenter.getIsAudioLinkMic());

        updateLinkMicListLayout();
    }

    @Override
    public void onJoinRtcChannel() {
        //显示连麦根布局
        flMediaLinkMicRoot.setKeepScreenOn(true);
        flMediaLinkMicRoot.setVisibility(VISIBLE);
        //更新连麦列表
        linkMicListAdapter.updateAllItem();
        updateLinkMicListLayout();
        //启动前台服务
        Activity activity = (Activity) getContext();
        PLVForegroundService.startForegroundService(activity.getClass(), "直播带货", R.drawable.ic_launcher);

        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onJoinRtcChannel();
        }
        isMediaShowInLinkMicList = false;
    }

    @Override
    public void onLeaveRtcChannel() {
        //将连麦列表和主屏幕区域的media分离，各自回到各自的位置
        //这里实际存在3种情况：
        //1. 纯视频且支持RTC：将隐藏在连麦列表的media区域切回主屏幕。
        //2. 纯视频且不支持RTC：将连麦列表的media区域的播放器切回到主屏幕。
        //3. 三分屏：将连麦列表的media区域的PPT切回到主屏幕。
        if (linkMicPresenter.isAloneChannelTypeSupportRTC()) {
            //如果是纯视频并且支持RTC的频道

            //把之前切换到连麦列表讲师位置的media(video)切回主屏
            if (teacherLocationViewSwitcher != null && teacherLocationViewSwitcher.isViewSwitched()) {
                teacherLocationViewSwitcher.switchView();
            } else {
                PLVCommonLog.exception(new Exception("teacherLocationViewSwitcher should not be null"));
            }
        } else {
            //如果是：1. 三分屏频道；2. 纯视频且不支持RTC的频道

            //如果media此时还在连麦列表，则将media从连麦列表切回到主屏幕
            if (isMediaShowInLinkMicList && linkMicListAdapter.getSwitchViewHasMedia() != null) {
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithMediaOnce(linkMicListAdapter.getSwitchViewHasMedia());
                }
            }
        }

        //连麦列表清空
        linkMicListAdapter.updateAllItem();
        linkMicListAdapter.releaseView();
        rvLinkMicList.removeAllViews();
        updateLinkMicListLayout();
        //隐藏连麦根布局
        flMediaLinkMicRoot.setVisibility(GONE);
        flMediaLinkMicRoot.setKeepScreenOn(false);
        //清空连麦场次状态数据
        isMediaShowInLinkMicList = false;
        teacherLocationViewSwitcher = null;
        setMediaInLinkMicListLinkMicId(null);
        //停止前台服务
        PLVForegroundService.stopForegroundService();
        //回调离开连麦
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onLeaveRtcChannel();
        }
    }

    @Override
    public void onChangeListShowMode(PLVLinkMicListShowMode linkMicListShowMode) {
        linkMicListAdapter.setListShowMode(linkMicListShowMode);
    }

    @Override
    public void onJoinLinkMic() {
        //我，加入频道成功
        PLVCommonLog.d(TAG, "onJoinLinkMic");
        ToastUtils.showShort("上麦成功");
        // 连麦时不允许小窗播放
        PLVDependManager.getInstance().get(PLVECFloatingWindow.class).showByUser(false);
        // 连麦成功不再暂停rtc观看
        resume();
        //更新连麦控制器
        //无延迟观看时，需要在上麦的时候再设置连麦类型
        linkMicControlBar.updateIsAudioWidth(linkMicPresenter.getIsAudioLinkMic());
        linkMicControlBar.setJoinLinkMicSuccess();
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onJoinLinkMic();
        }
    }

    @Override
    public void onLeaveLinkMic() {
        //我，离开频道

        //更新连麦控制器
        linkMicControlBar.setLeaveLinkMic();
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onLeaveLinkMic();
        }
    }

    @Override
    public void onUsersJoin(List<String> uids) {
        linkMicListAdapter.updateAllItem();

        Iterator<Runnable> pendingTaskIterator = onUserJoinPendingTask.iterator();
        while (pendingTaskIterator.hasNext()) {
            pendingTaskIterator.next().run();
            pendingTaskIterator.remove();
        }

        updateLinkMicListLayout();
    }

    @Override
    public void onUsersLeave(List<String> uids) {
        //如果media还在连麦列表，连麦列表的用户离开后
        if (isMediaShowInLinkMicList && getMediaViewIndexInLinkMicList() != -1) {
            for (String uid : uids) {
                if (mediaInLinkMicListLinkMicId != null && mediaInLinkMicListLinkMicId.equals(uid)) {
                    //如果用户离开的位置刚好在media的位置，则将media切回主屏
                    final int mediaIndex = getMediaViewIndexInLinkMicList();
                    RecyclerView.ViewHolder viewHolder = rvLinkMicList.findViewHolderForAdapterPosition(mediaIndex);
                    if (viewHolder != null) {
                        viewHolder.itemView.performClick();
                    }
                    break;
                }
            }
        }

        // 纯视频场景 讲师离开连麦 将连麦视图与播放器视图切换回原位
        if (liveChannelType == PLVLiveChannelType.ALONE) {
            final String mainTeacherLinkMicId = linkMicPresenter.getMainTeacherLinkMicId();
            if (mainTeacherLinkMicId != null && uids.contains(mainTeacherLinkMicId)) {
                if (teacherLocationViewSwitcher != null && teacherLocationViewSwitcher.isViewSwitched()) {
                    teacherLocationViewSwitcher.switchView();
                    linkMicListAdapter.setHasNotifyTeacherViewHolderBind(false);
                }
            }
        }

        linkMicListAdapter.updateAllItem();

        updateLinkMicListLayout();
    }

    @Override
    public void onTeacherHangupMe() {
        ToastUtils.showShort("主播已结束您的连麦");
    }

    @Override
    public void onNotInLinkMicList() {
        ToastUtils.showShort("连麦重连超时，请重新上麦");
        linkMicPresenter.leaveLinkMic();
    }


    @Override
    public void onUserMuteVideo(String uid, boolean mute, int pos) {
        //用户关闭视频
        linkMicListAdapter.updateUserMuteVideo(pos);
        if (uid.equals(linkMicPresenter.getLinkMicId())) {
            linkMicControlBar.setCameraOpenOrClose(!mute);
        }
    }

    @Override
    public void onUserMuteAudio(String uid, boolean mute, int pos) {
        //用户关闭音频
        linkMicListAdapter.updateVolumeChanged();
        if (uid.equals(linkMicPresenter.getLinkMicId())) {
            linkMicControlBar.setMicrophoneOpenOrClose(!mute);
        }
    }

    @Override
    public void onLocalUserMicVolumeChanged() {
        linkMicListAdapter.updateVolumeChanged();
    }

    @Override
    public void onRemoteUserVolumeChanged(List<PLVLinkMicItemDataBean> linkMicList) {
        linkMicListAdapter.updateVolumeChanged();
    }

    @Override
    public void onNetQuality(int quality) {
        linkMicListAdapter.updateNetQuality(quality);
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onNetworkQuality(quality);
        }
    }

    @Override
    public void onSwitchFirstScreen(String linkMicId) {
        linkMicListAdapter.updateAllItem();

        updateLinkMicListLayout();
    }

    @Override
    public void onAdjustTeacherLocation(final String linkMicId, final int teacherPos, boolean isNeedSwitchToMain, final Runnable onAdjustFinished) {
        if (!isNeedSwitchToMain) {
            onAdjustFinished.run();
        }
    }

    @Override
    public void onSwitchPPTViewLocation(boolean toMainScreen) {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="set、get">
    private void setMediaInLinkMicListLinkMicId(String linkMicId) {
        this.mediaInLinkMicListLinkMicId = linkMicId;
        if (linkMicListAdapter != null) {
            linkMicListAdapter.setMediaInLinkMicListLinkMicId(linkMicId);
        }
    }
    // </editor-fold>

    //转到竖屏
    @SuppressLint("RtlHardcoded")
    private void onPortrait() {
        updatePushResolution(false);
        //root
        FrameLayout.LayoutParams lpOfRoot = (FrameLayout.LayoutParams) getLayoutParams();
        lpOfRoot.width = LayoutParams.MATCH_PARENT;
        lpOfRoot.height = LayoutParams.MATCH_PARENT;
        setLayoutParams(lpOfRoot);

        //rvRoot
        LayoutParams lpOfRvRoot = (LayoutParams) flMediaLinkMicRoot.getLayoutParams();
        lpOfRvRoot.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.gravity = Gravity.LEFT;
        flMediaLinkMicRoot.setLayoutParams(lpOfRvRoot);

        //rv
        LayoutParams lpOfRv = (LayoutParams) rvLinkMicList.getLayoutParams();
        lpOfRv.leftMargin = 0;
        rvLinkMicList.setLayoutParams(lpOfRv);
        //移除横屏item间隙
        landscapeItemDecoration.setPortrait();
        //取消item显示圆角
        linkMicListAdapter.setShowRoundRect(false);

        //因为横屏时，连麦控制条是随着播放器皮肤一起显示和隐藏的，可能隐藏了，但是竖屏的时候连麦控制条要一直保持显示。
        //竖屏时，如果讲师打开了连麦，就显示连麦控制条
        if (linkMicPresenter.isTeacherOpenLinkMic()) {
            linkMicControlBar.show();
        }
    }

    private void updatePushResolution(boolean isLandscape) {
        if (linkMicPresenter != null) {
            linkMicPresenter.setPushPictureResolutionType(isLandscape ?
                    PLVLinkMicConstant.PushPictureResolution.RESOLUTION_LANDSCAPE :
                    PLVLinkMicConstant.PushPictureResolution.RESOLUTION_PORTRAIT);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView相关">
    /**
     * 更新平铺模式的连麦布局
     */
    private void updateLinkMicListLayout() {
        //重新计算连麦列表宽高布局参数
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rvLinkMicList.getLayoutParams();
        if (linkMicListAdapter.getItemCount() <= 1) {
            int itemType = PLVLinkMicListAdapter.ITEM_TYPE_ONLY_ONE;
            if (linkMicListAdapter.getItemType() == itemType) {
                return;
            }
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.topMargin = 0;

            linkMicListAdapter.setItemType(itemType);
            gridLayoutManager.setSpanCount(1);
            gridLayoutManager.requestLayout();
        } else {
            int itemType;
            if (linkMicListAdapter.getItemCount() <= 4){
                itemType = PLVLinkMicListAdapter.ITEM_TYPE_LESS_THAN_FOUR;
            } else {
                itemType = PLVLinkMicListAdapter.ITEM_TYPE_MORE_THAN_FOUR;
            }

            if (linkMicListAdapter.getItemType() == itemType) {
                return;
            }

            linkMicListAdapter.setItemType(itemType);

            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.topMargin = ConvertUtils.dp2px(78);
            //2-4人时，布局为2列，超过4人为为maxCount
            int maxCount = 3;
            int spanCount = linkMicListAdapter.getItemCount() <= 4 ? 2 : maxCount;

            gridLayoutManager.setSpanCount(spanCount);
            gridLayoutManager.requestLayout();
        }
        rvLinkMicList.setLayoutParams(lp);
        rvLinkMicList.setAdapter(linkMicListAdapter);//能否不调用这个
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据订阅 - 仅音频模式信息">

    private void observeOnAudioState(final IPLVLiveRoomDataManager liveRoomDataManager) {
        //监听 直播间是否是仅音频模式
        liveRoomDataManager.getIsOnlyAudioEnabled().observe((LifecycleOwner) getContext(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean onlyAudio) {
                if(onlyAudio == null){
                    onlyAudio = false;
                }
                ArrayList<String> permissions = new ArrayList<>();
                permissions.add(Manifest.permission.RECORD_AUDIO);
                if(!onlyAudio){
                    permissions.add(Manifest.permission.CAMERA);
                }
                linkMicPresenter.resetRequestPermissionList(permissions);
                if(linkMicListAdapter != null){
                    linkMicListAdapter.setOnlyAudio(onlyAudio);
                    linkMicListAdapter.updateTeacherCoverImage();
                }
            }
        });

        //封面图
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (polyvLiveClassDetailVOPLVStatefulData == null || !polyvLiveClassDetailVOPLVStatefulData.isSuccess()) {
                    return;
                }
                PLVLiveClassDetailVO liveClassDetail = polyvLiveClassDetailVOPLVStatefulData.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }

                String coverImage = liveClassDetail.getData().getSplashImg();
                if(linkMicListAdapter != null){
                    linkMicListAdapter.setCoverImage(coverImage);
                    linkMicListAdapter.updateTeacherCoverImage();
                }
            }
        });
    }
    // </editor-fold >
}
