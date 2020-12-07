package com.easefun.polyv.livecloudclass.modules.linkmic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.linkmic.adapter.PLVLinkMicListAdapter;
import com.easefun.polyv.livecloudclass.modules.linkmic.service.PLVLCLinkMicForegroundService;
import com.easefun.polyv.livecloudclass.modules.linkmic.widget.PLVLinkMicRvLandscapeItemDecoration;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.contract.IPLVLinkMicContract;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.linkmic.presenter.PLVLinkMicPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVNotchUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * date: 2020/7/16
 * author: hwj
 * description: 连麦布局实现
 */
public class PLVLCLinkMicLayout extends FrameLayout implements IPLVLinkMicContract.IPLVLinkMicView, IPLVLCLinkMicLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVLCLinkMicLayout.class.getSimpleName();

    //尺寸
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_LEFT = 8;
    private static final int DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT = 34;
    private static final int DP_LAND_SPEAKING_USER_VIEW_MARGIN_RIGHT_TO_LINK_MIC_LIST = 24;

    //滑动提示状态：不可见，由于连麦列表数目还不够
    private static final int TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK = 0;
    //滑动提示状态：可见
    private static final int TRY_SCROLL_VIEW_STATE_VISIBLE = 1;
    //滑动提示状态：不可见，由于已经滑动了
    private static final int TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED = 2;

    //Presenter
    private IPLVLinkMicContract.IPLVLinkMicPresenter linkMicPresenter;

    //View
    private IPLVLCLinkMicControlBar linkMicControlBar;
    private FrameLayout flPPTLinkMicRoot;
    private RecyclerView rvLinkMicList;
    private LinearLayout llTryScrollTip;
    private LinearLayout llSpeakingUsers;
    private TextView tvSpeakingUsersText;
    //连麦列表适配器
    private PLVLinkMicListAdapter linkMicListAdapter;
    private PLVLinkMicRvLandscapeItemDecoration landscapeItemDecoration = new PLVLinkMicRvLandscapeItemDecoration();

    //Listener
    private OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener;
    private RecyclerView.OnScrollListener onScrollTryScrollTipListener;

    //状态数据
    private boolean isPPTShowInLinkMicList = false;
    //当前滑动提示状态
    @TryScrollViewStateType
    private int curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;

    //当前是否是横屏
    private boolean curIsLandscape = false;
    //横屏时的宽度
    private int landscapeWidth = 0;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLinkMicLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLinkMicLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_link_mic_layout_ppt, this, true);
        flPPTLinkMicRoot = findViewById(R.id.plvlc_linkmic_fl_ppt_linkmic_root);
        rvLinkMicList = findViewById(R.id.plvlc_link_mic_rv_linkmic_list);
        llTryScrollTip = findViewById(R.id.plvlc_link_mic_ll_try_scroll_tip);
        llSpeakingUsers = findViewById(R.id.plvlc_linkmic_ll_speaking_users);
        tvSpeakingUsersText = findViewById(R.id.plvlc_linkmic_tv_speaking_users_text);

        //init RecyclerView
        rvLinkMicList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rvLinkMicList.addItemDecoration(landscapeItemDecoration);
        //禁用RecyclerView默认动画
        rvLinkMicList.getItemAnimator().setAddDuration(0);
        rvLinkMicList.getItemAnimator().setChangeDuration(0);
        rvLinkMicList.getItemAnimator().setMoveDuration(0);
        rvLinkMicList.getItemAnimator().setRemoveDuration(0);
        RecyclerView.ItemAnimator rvAnimator = rvLinkMicList.getItemAnimator();
        if (rvAnimator != null) {
            if (rvAnimator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) rvAnimator).setSupportsChangeAnimations(false);
            }
        }

        //init adapter
        linkMicListAdapter = new PLVLinkMicListAdapter(rvLinkMicList, new PLVLinkMicListAdapter.OnPLVLinkMicAdapterCallback() {
            @Override
            public SurfaceView createLinkMicRenderView() {
                return linkMicPresenter.createRenderView(Utils.getApp());
            }

            @Override
            public void setupRenderView(SurfaceView surfaceView, String linkMicId) {
                linkMicPresenter.setupRenderView(surfaceView, linkMicId);
            }

            @Override
            public void onClickItemListener(int pos, @Nullable PLVSwitchViewAnchorLayout switchViewHasPPT, PLVSwitchViewAnchorLayout switchViewGoMainScreen) {
                if (onPLVLinkMicLayoutListener != null) {
                    if (switchViewHasPPT == null) {
                        //连麦列表没有PPT，那么切换一次就够了
                        onPLVLinkMicLayoutListener.onClickSwitchWithPPTOnce(switchViewGoMainScreen);
                    } else if (switchViewHasPPT == switchViewGoMainScreen) {
                        //连麦列表有PPT，但是刚好点击的就是PPT的item，因此还是切换一次就够了
                        onPLVLinkMicLayoutListener.onClickSwitchWithPPTOnce(switchViewGoMainScreen);
                    } else {
                        //连麦列表有PPT，且点击的不是PPT的item，那么就需要切两次
                        onPLVLinkMicLayoutListener.onClickSwitchWithPPTTwice(switchViewHasPPT, switchViewGoMainScreen);
                    }
                }
                if (switchViewHasPPT == switchViewGoMainScreen) {
                    //ppt被切回到主屏幕
                    isPPTShowInLinkMicList = false;
                } else {
                    //ppt切到连麦
                    isPPTShowInLinkMicList = true;
                }
            }
        });

        //init方向
        curIsLandscape = PLVScreenUtils.isLandscape(getContext());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化连麦控制条">
    private void initLinkMicControlBar(IPLVLCLinkMicControlBar linkMicControlBar) {
        if (linkMicControlBar == null) {
            PLVCommonLog.exception(new Throwable("linkMicController == null"));
            return;
        }
        this.linkMicControlBar = linkMicControlBar;
        //监听连麦控制器的各种点击事件
        linkMicControlBar.setOnPLCLinkMicControlBarListener(new IPLVLCLinkMicControlBar.OnPLCLinkMicControlBarListener() {
            @Override
            public void onClickRingUpLinkMic() {
                linkMicPresenter.requestJoinLinkMic();
            }

            @Override
            public void onClickRingOffLinkMic() {
                if (linkMicPresenter.isJoinLinkMic()) {
                    linkMicPresenter.leaveLinkMic();
                } else {
                    linkMicPresenter.cancelRequestJoinLinkMic();
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 外部直接调用的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager, IPLVLCLinkMicControlBar linkMicControlBar) {
        linkMicPresenter = new PLVLinkMicPresenter(liveRoomDataManager, this);
        initLinkMicControlBar(linkMicControlBar);
    }


    @Override
    public void destroy() {
        linkMicPresenter.destroy();
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
    }

    @Override
    public void hideControlBar() {
        PLVCommonLog.d(TAG, "hide");
        //横屏的情况下，才将连麦控制条隐藏
        if (linkMicControlBar != null && curIsLandscape) {
            linkMicControlBar.hide();
        }
    }

    @Override
    public void showControlBar() {
        if (linkMicControlBar != null) {
            linkMicControlBar.show();
        }
    }


    @Override
    public void setIsTeacherOpenLinkMic(boolean isTeacherOpenLinkMic) {
        linkMicControlBar.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
        linkMicPresenter.setIsTeacherOpenLinkMic(isTeacherOpenLinkMic);
    }


    @Override
    public void setIsAudio(boolean isAudioLinkMic) {
        linkMicPresenter.setIsAudioLinkMic(isAudioLinkMic);
    }

    @Override
    public boolean isJoinLinkMic() {
        return linkMicPresenter.isJoinLinkMic();
    }


    @Override
    public boolean isPPTShowInLinkMicList() {
        return isPPTShowInLinkMicList;
    }

    @Override
    public int getPPTViewIndexInLinkMicList() {
        return linkMicListAdapter.getPPTViewIndexInLinkMicList();
    }

    @Override
    public void performClickInLinkMicListItem(final int index) {
        rvLinkMicList.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder viewHolder = rvLinkMicList.findViewHolderForAdapterPosition(index);
                if (viewHolder != null) {
                    viewHolder.itemView.performClick();
                }
                updateAllLinkMicList();
            }
        });
    }

    @Override
    public void updateAllLinkMicList() {
        linkMicListAdapter.updateAllItem();
    }

    @Override
    public void setOnPLVLinkMicLayoutListener(OnPLVLinkMicLayoutListener onPLVLinkMicLayoutListener) {
        this.onPLVLinkMicLayoutListener = onPLVLinkMicLayoutListener;
    }


    @Override
    public int getLandscapeWidth() {
        return landscapeWidth;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPLVLinkMicContract.IPLVLinkMicView实现">

    @Override
    public void onLinkMicError(int errorCode, Throwable throwable) {
        PLVCommonLog.exception(throwable);
        if (errorCode == 1060501) {
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
        linkMicControlBar.setIsTeacherOpenLinkMic(true);
    }

    @Override
    public void onTeacherCloseLinkMic() {
        //教师关闭连麦
        linkMicControlBar.setIsTeacherOpenLinkMic(false);
    }

    @Override
    public void onTeacherAllowJoin() {
        PLVCommonLog.d(TAG, "onTeacherAllowJoin");
    }

    @Override
    public void onAllowButJoinTimeout() {
        linkMicControlBar.setLeaveLinkMic();
    }

    @Override
    public void onBeforeJoinChannel(String linkMicUid, boolean isAudio, List<PLVLinkMicItemDataBean> linkMicList) {
        PLVCommonLog.d(TAG, "PLVLinkMicLayout.onBeforeJoinChannel");
        //初始化连麦适配器，准备添加连麦观众
        linkMicListAdapter.setDataList(linkMicList);
        if (!linkMicListAdapter.hasObservers() && !linkMicListAdapter.hasStableIds()) {
            linkMicListAdapter.setHasStableIds(true);
        }
        linkMicListAdapter.setIsAudio(isAudio);
        linkMicListAdapter.setMyLinkMicId(linkMicUid);

        rvLinkMicList.setAdapter(linkMicListAdapter);

        //如果是刘海屏，则有横屏右边距；如果不是刘海屏，则横屏右边距为0
        int marginRight = PLVNotchUtils.hasNotchInScreen((Activity) getContext()) ? PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_RIGHT) : 0;
        landscapeWidth = linkMicListAdapter.getItemWidth() + PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_LEFT) + marginRight;
        //有可能横屏之后上麦的，所以上麦后要根据当前的屏幕方向再做一次变换。
        if (PLVScreenUtils.isPortrait(getContext())) {
            onPortrait();
        } else {
            onLandscape();
        }
        //设置正在发言View的右偏移
        MarginLayoutParams lpOfSpeakingUsers = (MarginLayoutParams) llSpeakingUsers.getLayoutParams();
        lpOfSpeakingUsers.rightMargin = landscapeWidth + PLVScreenUtils.dip2px(DP_LAND_SPEAKING_USER_VIEW_MARGIN_RIGHT_TO_LINK_MIC_LIST);
        llSpeakingUsers.setLayoutParams(lpOfSpeakingUsers);

        linkMicControlBar.setIsAudio(isAudio);
    }

    @Override
    public void onJoinChannelSuccess() {
        //我，加入频道成功
        PLVCommonLog.d(TAG, "onJoinChannelSuccess");
        ToastUtils.showShort("上麦成功");

        curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;
        //显示连麦根布局
        flPPTLinkMicRoot.setKeepScreenOn(true);
        flPPTLinkMicRoot.setVisibility(VISIBLE);
        //上麦后，摄像头关闭，麦克风打开
        linkMicPresenter.muteVideo(true);
        linkMicPresenter.muteAudio(false);
        //更新连麦列表
        linkMicListAdapter.updateAllItem();
        //启动前台服务
        Activity activity = (Activity) getContext();
        PLVLCLinkMicForegroundService.startForegroundService(activity.getClass());

        //更新连麦控制器
        linkMicControlBar.setJoinLinkMicSuccess();
        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onJoinChannelSuccess();
        }
        isPPTShowInLinkMicList = false;
    }

    @Override
    public void onLeaveChannel(boolean shouldStartPlay) {
        //我，离开频道

        //连麦列表清空了
        linkMicListAdapter.updateAllItem();
        linkMicListAdapter.releaseView();
        rvLinkMicList.removeAllViews();
        //隐藏连麦根布局
        flPPTLinkMicRoot.setVisibility(GONE);
        flPPTLinkMicRoot.setKeepScreenOn(false);
        //更新连麦控制器
        linkMicControlBar.setLeaveLinkMic();
        //隐藏向左滑动试试
        llTryScrollTip.setVisibility(GONE);

        //停止前台服务
        PLVLCLinkMicForegroundService.stopForegroundService();

        if (onPLVLinkMicLayoutListener != null) {
            onPLVLinkMicLayoutListener.onLeaveChannel(shouldStartPlay);
        }
        isPPTShowInLinkMicList = false;
    }

    @Override
    public void onUsersJoin(List<String> uids) {

        linkMicListAdapter.updateAllItem();

        if (curTryScrollViewState == TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK
                && linkMicListAdapter.getItemCount() > PLVLinkMicListAdapter.HORIZONTAL_VISIBLE_COUNT
                && getRvScrolledXOffset() == 0) {

            if (onScrollTryScrollTipListener == null) {
                onScrollTryScrollTipListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dx > 0) {
                            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED;
                            llTryScrollTip.setVisibility(GONE);
                            rvLinkMicList.removeOnScrollListener(this);
                        }
                    }
                };
            }

            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_VISIBLE;
            if (!curIsLandscape) {
                llTryScrollTip.setVisibility(VISIBLE);
            }
            rvLinkMicList.addOnScrollListener(onScrollTryScrollTipListener);
        }

    }

    @Override
    public void onUsersLeave(List<String> uids) {
        linkMicListAdapter.updateAllItem();

        if (curTryScrollViewState == TRY_SCROLL_VIEW_STATE_VISIBLE
                && linkMicListAdapter.getItemCount() <= PLVLinkMicListAdapter.HORIZONTAL_VISIBLE_COUNT) {
            curTryScrollViewState = TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK;
            llTryScrollTip.setVisibility(GONE);
            if (onScrollTryScrollTipListener != null) {
                rvLinkMicList.removeOnScrollListener(onScrollTryScrollTipListener);
            }
        }

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
        //横屏时才显示正在发言
        if (PLVScreenUtils.isLandscape(getContext())) {
            //筛选正在发言的用户
            PLVLinkMicItemDataBean curSpeakingUser = null;
            boolean moreThanOneUserSpeaking = false;
            for (PLVLinkMicItemDataBean plvLinkMicItemDataBean : linkMicList) {
                if (plvLinkMicItemDataBean.getLinkMicId().equals(linkMicPresenter.getLinkMicId())) {
                    continue;
                }
                if (plvLinkMicItemDataBean.getCurVolume() != 0) {
                    if (curSpeakingUser == null) {
                        curSpeakingUser = plvLinkMicItemDataBean;
                    } else {
                        moreThanOneUserSpeaking = true;
                        break;
                    }
                }
            }
            if (curSpeakingUser != null) {
                //设置正在发言的用户的昵称，包括字样："..." 和 "等"
                String userNick = curSpeakingUser.getNick();
                StringBuilder userNickSBuilder = new StringBuilder();
                if (userNick.length() > 8) {
                    userNickSBuilder.append(userNick, 0, 8);
                } else {
                    userNickSBuilder.append(userNick);
                }

                if (moreThanOneUserSpeaking) {
                    userNickSBuilder.append("...等");
                }
                tvSpeakingUsersText.setText(userNickSBuilder.toString());
                llSpeakingUsers.setVisibility(VISIBLE);
            } else {
                //当前没有用户在发言，则隐藏
                llSpeakingUsers.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onSwitchFirstScreen(String linkMicId) {
        linkMicListAdapter.updateAllItem();
    }

    @Override
    public void setFirstScreenLinkMicId(String linkMicId) {
        linkMicListAdapter.setFirstScreenLinkMicId(linkMicId);
    }

    @Override
    public void onSwitchPPTViewLocation(boolean toMainScreen) {
        PLVSwitchViewAnchorLayout firstScreenSwitchView = linkMicListAdapter.getFirstScreenSwitchView();
        //如果当前连麦列表为空，则返回
        if (firstScreenSwitchView == null) {
            return;
        }

        if (isPPTShowInLinkMicList && linkMicListAdapter.getSwitchViewHashPPT() != null) {
            //ppt此时在连麦列表

            if (linkMicListAdapter.getSwitchViewHashPPT() == firstScreenSwitchView) {
                //当ppt在第一画面

                if (!toMainScreen) {
                    return;
                }
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithPPTOnce(linkMicListAdapter.getSwitchViewHashPPT());
                    linkMicListAdapter.setSwitchViewHashPPT(null);
                }
            } else {
                //当ppt不在第一画面
                if (onPLVLinkMicLayoutListener != null) {
                    onPLVLinkMicLayoutListener.onClickSwitchWithPPTTwice(linkMicListAdapter.getSwitchViewHashPPT(), firstScreenSwitchView);
                    linkMicListAdapter.setSwitchViewHashPPT(firstScreenSwitchView);
                }
            }

        } else {
            //ppt此时在主屏幕

            //如果PPT要切换到主屏幕，返回。
            if (toMainScreen) {
                return;
            }
            //将ppt和连麦列表第一画面切换
            if (onPLVLinkMicLayoutListener != null) {
                onPLVLinkMicLayoutListener.onClickSwitchWithPPTOnce(firstScreenSwitchView);
                linkMicListAdapter.setSwitchViewHashPPT(firstScreenSwitchView);
            }
        }

        isPPTShowInLinkMicList = !toMainScreen;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="横竖屏切换">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //切到横屏
            PLVCommonLog.d(TAG, "onConfigurationChanged->landscape");
            if (!curIsLandscape) {
                onLandscape();
            }
            curIsLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //切到竖屏
            PLVCommonLog.d(TAG, "onConfigurationChanged->portrait");

            if (curIsLandscape) {
                onPortrait();
            }
            curIsLandscape = false;
        }
    }

    //转到横屏
    @SuppressLint("RtlHardcoded")
    private void onLandscape() {
        //root
        ConstraintLayout.LayoutParams lpOfRoot = (ConstraintLayout.LayoutParams) getLayoutParams();
        lpOfRoot.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        lpOfRoot.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        lpOfRoot.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lpOfRoot.width = LayoutParams.MATCH_PARENT;
        lpOfRoot.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        lpOfRoot.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        setLayoutParams(lpOfRoot);

        //rvRoot
        LayoutParams lpOfRvRoot = (LayoutParams) flPPTLinkMicRoot.getLayoutParams();
        lpOfRvRoot.width = landscapeWidth;
        lpOfRvRoot.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.gravity = Gravity.RIGHT;
        flPPTLinkMicRoot.setLayoutParams(lpOfRvRoot);

        //rv
        FrameLayout.LayoutParams lpOfRv = (LayoutParams) rvLinkMicList.getLayoutParams();
        lpOfRv.gravity = Gravity.LEFT;
        lpOfRv.leftMargin = PLVScreenUtils.dip2px(DP_LAND_LINK_MIC_LIST_MARGIN_LEFT);
        rvLinkMicList.setLayoutParams(lpOfRv);
        //设置vertical排列
        rvLinkMicList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        //设置横屏item间隙
        landscapeItemDecoration.setLandscape();
        //设置item显示圆角
        linkMicListAdapter.setShowRoundRect(true);

        //横屏隐藏滑动提示View
        llTryScrollTip.setVisibility(GONE);

        //正在发言
        if (isJoinLinkMic()) {
            llSpeakingUsers.setVisibility(VISIBLE);
        } else {
            llSpeakingUsers.setVisibility(GONE);
        }
    }

    //转到竖屏
    @SuppressLint("RtlHardcoded")
    private void onPortrait() {
        //root
        ConstraintLayout.LayoutParams lpOfRoot = (ConstraintLayout.LayoutParams) getLayoutParams();
        lpOfRoot.width = LayoutParams.MATCH_PARENT;
        lpOfRoot.height = LayoutParams.WRAP_CONTENT;
        lpOfRoot.topToBottom = R.id.plvlc_video_viewstub;
        lpOfRoot.topToTop = ConstraintLayout.LayoutParams.UNSET;
        lpOfRoot.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        setLayoutParams(lpOfRoot);

        //rvRoot
        LayoutParams lpOfRvRoot = (LayoutParams) flPPTLinkMicRoot.getLayoutParams();
        lpOfRvRoot.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lpOfRvRoot.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lpOfRvRoot.gravity = Gravity.LEFT;
        flPPTLinkMicRoot.setLayoutParams(lpOfRvRoot);

        //rv
        FrameLayout.LayoutParams lpOfRv = (LayoutParams) rvLinkMicList.getLayoutParams();
        lpOfRv.leftMargin = 0;
        rvLinkMicList.setLayoutParams(lpOfRv);
        //设置horizontal排列
        rvLinkMicList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //移除横屏item间隙
        landscapeItemDecoration.setPortrait();
        //设置horizontal排列
        rvLinkMicList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //取消item显示圆角
        linkMicListAdapter.setShowRoundRect(false);

        //正在发言
        llSpeakingUsers.setVisibility(GONE);

        //因为横屏时，连麦控制条是随着播放器皮肤一起显示和隐藏的，可能隐藏了，但是竖屏的时候连麦控制条要一直保持显示。
        //竖屏时，如果讲师打开了连麦，就显示连麦控制条
        if (linkMicPresenter.isTeacherOpenLinkMic()) {
            linkMicControlBar.show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="RecyclerView相关">
    private int getRvScrolledXOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvLinkMicList.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisiableChildView.getWidth();
        return (position) * itemWidth - firstVisiableChildView.getLeft();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 注解-TryScrollViewStateType">
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            TRY_SCROLL_VIEW_STATE_VISIBLE,
            TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_LACK,
            TRY_SCROLL_VIEW_STATE_INVISIBLE_BY_SCROLLED
    })
    public @interface TryScrollViewStateType {/**/
    }
// </editor-fold>

}
