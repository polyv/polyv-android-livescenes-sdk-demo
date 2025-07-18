package com.easefun.polyv.streameralone.modules.liveroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVStreamerPresenter;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.liveroom.adapter.PLVSAMemberAdapter;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.socket.client.Ack;

/**
 * 成员列表布局
 */
public class PLVSAMemberLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 竖屏时屏幕高度
    private static final int SCREEN_HEIGHT_IN_PORT = Math.max(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
    // 横屏时菜单横向尺寸
    private static final int MENU_SIZE_LAND = ConvertUtils.dp2px(400);
    // 清晰度弹层布局位置
    private static final Position MENU_DRAWER_POSITION_PORT = Position.BOTTOM;
    private static final Position MENU_DRAWER_POSITION_LAND = Position.END;
    // 布局背景
    private static final int LAYOUT_BACKGROUND_RES_PORT = R.drawable.plvsa_more_ly_shape;
    private static final int LAYOUT_BACKGROUND_RES_LAND = R.drawable.plvsa_more_ly_shape_land;

    private IPLVLiveRoomDataManager liveRoomDataManager;
    //view
    private LinearLayout plvsaMemberLayout;
    private TextView plvsaMemberOnlineCountTv;
    private RecyclerView plvsaMemberListRv;
    private RecyclerView plvsaMemberSearchListRv;
    private EditText plvsaMemberSearchEt;
    //adapter
    private PLVSAMemberAdapter memberAdapter;
    private PLVSAMemberAdapter memberSearchAdapter;

    //推流和连麦presenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //布局弹层
    private PLVMenuDrawer menuDrawer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable lastRunnable;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    //dispose
    private Disposable searchUserDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMemberLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_live_room_member_layout, this);

        plvsaMemberLayout = findViewById(R.id.plvsa_member_layout);
        plvsaMemberOnlineCountTv = findViewById(R.id.plvsa_member_online_count_tv);
        plvsaMemberListRv = findViewById(R.id.plvsa_member_list_rv);
        plvsaMemberSearchListRv = findViewById(R.id.plvsa_member_search_list_rv);
        plvsaMemberSearchEt = findViewById(R.id.plvsa_member_search_et);

        plvsaMemberSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideSoftInput(plvsaMemberSearchEt);
                    return true;
                }
                return false;
            }
        });
        plvsaMemberSearchEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (editable.length() <= 0) {
                    cancelPost();
                    plvsaMemberSearchListRv.setVisibility(View.INVISIBLE);
                    plvsaMemberListRv.setVisibility(View.VISIBLE);
                } else {
                    searchUser(editable);
                }
            }
        });
        final View rootView = ((Activity) getContext()).getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int oldHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int heightDiff = screenHeight - r.height();
                if (oldHeight == 0) {
                    oldHeight = heightDiff;
                    return;
                }
                if (heightDiff != oldHeight) {
                    memberAdapter.hideControlWindow();
                    memberSearchAdapter.hideControlWindow();
                }
                oldHeight = heightDiff;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        //init memberListRv
        plvsaMemberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        memberAdapter = new PLVSAMemberAdapter(liveRoomDataManager.getConfig().isAutoLinkToGuest());
        memberAdapter.setTeacherPermission(!isGuest());//嘉宾默认无权限，需要隐藏控制相关按钮
        PLVSAMemberAdapter.OnViewActionListener listener = new PLVSAMemberAdapter.OnViewActionListener() {
            @Override
            public void onMicControl(int position, boolean isMute) {
                if (streamerPresenter != null) {
                    streamerPresenter.muteUserMedia(position, false, isMute);
                    toastMuteMic(isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (streamerPresenter != null) {
                    streamerPresenter.muteUserMedia(position, true, isMute);
                    toastMuteCamera(isMute);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
                if (streamerPresenter != null) {
                    streamerPresenter.controlUserLinkMic(position, action);
                }
            }

            @Override
            public void onGrantUserSpeakerPermission(int position, final PLVSocketUserBean user, final boolean isGrant) {
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
            public int getPosition(PLVMemberItemDataBean memberItemDataBean) {
                if (streamerPresenter != null) {
                    return streamerPresenter.getPositionByMemberItem(memberItemDataBean);
                }
                return -1;
            }
        };
        memberAdapter.setOnViewActionListener(listener);
        plvsaMemberListRv.setAdapter(memberAdapter);

        //init memberSearchListRv
        plvsaMemberSearchListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        memberSearchAdapter = new PLVSAMemberAdapter(liveRoomDataManager.getConfig().isAutoLinkToGuest());
        memberSearchAdapter.setMemberListShow(false);
        memberSearchAdapter.setTeacherPermission(!isGuest());//嘉宾默认无权限，需要隐藏控制相关按钮
        memberSearchAdapter.setOnViewActionListener(listener);
        plvsaMemberSearchListRv.setAdapter(memberSearchAdapter);

        observeClassDetail();
    }

    private void observeClassDetail() {
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(),
                new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                    @Override
                    public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> data) {
                        if (data == null || data.getData() == null) {
                            return;
                        }
                        memberAdapter.setChannelAllowInviteLinkMic(data.getData().getData().isInviteAudioEnabled());
                        memberSearchAdapter.setChannelAllowInviteLinkMic(data.getData().getData().isInviteAudioEnabled());
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void open() {
        if (liveRoomDataManager == null) {
            return;
        }
        if (!PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_SHOW_FUNCTION_MEMBER)) {
            return;
        }
        if (menuDrawer == null) {
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    PLVScreenUtils.isPortrait(getContext()) ? MENU_DRAWER_POSITION_PORT : MENU_DRAWER_POSITION_LAND,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        plvsaMemberSearchEt.setText("");
                        KeyboardUtils.hideSoftInput(plvsaMemberSearchEt);
                        menuDrawer.detachToContainer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                    }

                    ViewGroup popupContainer = (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvsa_live_room_popup_container);
                    View maskView = ((Activity) getContext()).findViewById(R.id.plvsa_popup_container_mask);
                    if (popupContainer.getChildCount() > 0) {
                        maskView.setVisibility(View.VISIBLE);
                    } else {
                        maskView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });
        } else {
            menuDrawer.attachToContainer();
        }

        updateViewWithOrientation();
        menuDrawer.openMenu();
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public void closeAndHideWindow() {
        if (memberAdapter != null) {
            memberAdapter.hideControlWindow();
        }
        if (memberSearchAdapter != null) {
            memberSearchAdapter.hideControlWindow();
        }
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isOpen() {
        return menuDrawer != null && menuDrawer.isMenuVisible();
    }

    public void updateOnlineCount(int onlineCount) {
        plvsaMemberOnlineCountTv.setText(PLVAppUtils.formatString(R.string.plv_chat_online_count, onlineCount + ""));
    }

    public boolean hasUserRequestLinkMic() {
        return memberAdapter.hasUserRequestLinkMic();
    }

    public void notifyLinkMicTypeChange(boolean isVideoLinkMic, boolean isOpenLinkMic) {
        if (memberAdapter != null) {
            memberAdapter.setOpenLinkMic(isOpenLinkMic);
        }
        if (memberSearchAdapter != null) {
            memberSearchAdapter.setOpenLinkMic(isOpenLinkMic);
        }
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return streamerView;
    }

    public boolean onBackPressed() {
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="成员列表 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {
        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            super.setPresenter(presenter);
            streamerPresenter = presenter;

            presenter.getData().getStreamerStatus().observe((LifecycleOwner) getContext(), new IPLVOnDataChangedListener<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isStartedStatus) {
                    if (isStartedStatus == null) {
                        return;
                    }
                    memberAdapter.setStreamerStatus(isStartedStatus);
                    memberSearchAdapter.setStreamerStatus(isStartedStatus);
                }
            });
        }

        @Override
        public void onUserMuteVideo(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteVideo(uid, mute, streamerListPos, memberListPos);
            memberAdapter.updateUserMuteVideo(memberListPos);
            memberSearchAdapter.updateUserMuteVideo(uid);
        }

        @Override
        public void onUserMuteAudio(String uid, boolean mute, int streamerListPos, int memberListPos) {
            super.onUserMuteAudio(uid, mute, streamerListPos, memberListPos);
            memberAdapter.updateUserMuteAudio(memberListPos);
            memberSearchAdapter.updateUserMuteAudio(uid);
        }

        @Override
        public void onLocalUserMicVolumeChanged(int volume) {
            memberAdapter.updateVolumeChanged();
        }

        @Override
        public void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList) {
            super.onRemoteUserVolumeChanged(linkMicList);
            memberAdapter.updateVolumeChanged();
        }

        @Override
        public void onUsersJoin(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersJoin(dataBeanList);
            memberAdapter.updateUserJoin(dataBeanList);
            memberSearchAdapter.updateUserJoin(dataBeanList);
            toastUserLinkMicMsg(dataBeanList, true);
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> dataBeanList) {
            super.onUsersLeave(dataBeanList);

            // 用户退出时，收回主讲权限
            for (PLVLinkMicItemDataBean leaveUser : dataBeanList) {
                if (leaveUser.isHasSpeaker()) {
                    streamerPresenter.setUserPermissionSpeaker(leaveUser.getUserId(), false, null);
                }
            }

            memberAdapter.updateUserLeave(dataBeanList);
            memberSearchAdapter.updateUserLeave(dataBeanList);
            toastUserLinkMicMsg(dataBeanList, false);
        }

        @Override
        public void onUpdateMemberListData(List<PLVMemberItemDataBean> dataBeanList) {
            super.onUpdateMemberListData(dataBeanList);
            memberAdapter.update(dataBeanList);
        }

        @Override
        public void onUpdateMemberSearchListData(List<PLVMemberItemDataBean> dataBeanList) {
            if (TextUtils.isEmpty(plvsaMemberSearchEt.getText().toString())) {
                return;
            }
            plvsaMemberSearchListRv.setVisibility(View.VISIBLE);
            plvsaMemberListRv.setVisibility(View.INVISIBLE);
            memberSearchAdapter.update(dataBeanList);
        }

        @Override
        public void onCameraDirection(boolean front, int pos, String uid) {
            memberAdapter.updateCameraDirection(pos);
        }

        @Override
        public void onUpdateSocketUserData(int pos) {
            super.onUpdateSocketUserData(pos);
            memberAdapter.updateSocketUserData(pos);
        }

        @Override
        public void onAddMemberListData(int pos) {
            super.onAddMemberListData(pos);
            memberAdapter.insertUserData(pos);
        }

        @Override
        public void onRemoveMemberListData(int pos) {
            super.onRemoveMemberListData(pos);
            memberAdapter.removeUserData(pos);
        }

        @Override
        public void onReachTheInteractNumLimit() {
            super.onReachTheInteractNumLimit();
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
        public void onSetPermissionChange(String type, boolean isGranted, boolean isCurrentUser, PLVSocketUserBean user) {
            super.onSetPermissionChange(type, isGranted, isCurrentUser, user);
            if (PLVPPTAuthentic.PermissionType.TEACHER.equals(type)) {
                if (user != null && !user.isTeacher()) {
                    memberAdapter.setHasSpeakerUser(isGranted ? user : null);
                    memberSearchAdapter.setHasSpeakerUser(isGranted ? user : null);
                }
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewWithOrientation();
    }

    private void updateViewWithOrientation() {
        // 竖屏时菜单竖向尺寸
        final int menuSizePort = (int) (SCREEN_HEIGHT_IN_PORT * (memberAdapter.getItemCount() > 10 ? 0.75 : 0.5));

        int menuSize;
        Position menuPosition;
        int layoutBackgroundResId;

        if (PLVScreenUtils.isPortrait(getContext())) {
            menuSize = menuSizePort;
            menuPosition = MENU_DRAWER_POSITION_PORT;
            layoutBackgroundResId = LAYOUT_BACKGROUND_RES_PORT;
        } else {
            menuSize = MENU_SIZE_LAND;
            menuPosition = MENU_DRAWER_POSITION_LAND;
            layoutBackgroundResId = LAYOUT_BACKGROUND_RES_LAND;
        }

        if (menuDrawer != null) {
            menuDrawer.setMenuSize(menuSize);
            menuDrawer.setPosition(menuPosition);
        }
        plvsaMemberLayout.setBackgroundResource(layoutBackgroundResId);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void toastUserLinkMicMsg(List<PLVLinkMicItemDataBean> linkMicItemDataBeans, boolean isJoin) {
        if (streamerPresenter != null && streamerPresenter.getStreamerStatus() != PLVStreamerPresenter.STREAMER_STATUS_START_SUCCESS) {
            return;
        }
        if (linkMicItemDataBeans == null || linkMicItemDataBeans.isEmpty()) {
            return;
        }
        PLVLinkMicItemDataBean firstLinkMicItem = linkMicItemDataBeans.get(0);
        if (firstLinkMicItem == null) {
            return;
        }
        String text = PLVAppUtils.formatString(isJoin ? R.string.plv_linkmic_join_channel : R.string.plv_linkmic_leave_channel, firstLinkMicItem.getNick());
        PLVToast.Builder.context(getContext())
                .setText(text)
                .build()
                .show();
    }

    private void toastMuteMic(boolean isMute) {
        String text = PLVAppUtils.formatString(isMute ? R.string.plv_linkmic_microphone_mute : R.string.plv_linkmic_microphone_unmute);
        PLVToast.Builder.context(getContext())
                .setText(text)
                .build()
                .show();
    }

    private void toastMuteCamera(boolean isMute) {
        String text = PLVAppUtils.formatString(isMute ? R.string.plv_linkmic_camera_mute : R.string.plv_linkmic_camera_unmute);
        PLVToast.Builder.context(getContext())
                .setText(text)
                .build()
                .show();
    }

    private boolean isGuest() {
        if (liveRoomDataManager != null) {
            return PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType());
        }
        return false;
    }

    private void debounce(Runnable action, long delayMillis) {
        cancelPost();
        lastRunnable = action;
        handler.postDelayed(action, delayMillis);
    }

    private void cancelPost() {
        if (lastRunnable != null) {
            handler.removeCallbacks(lastRunnable);
        }
        if (searchUserDisposable != null) {
            searchUserDisposable.dispose();
        }
    }

    private void searchUser(final Editable editable) {
        debounce(new Runnable() {
            @Override
            public void run() {
                if (searchUserDisposable != null) {
                    searchUserDisposable.dispose();
                }
                if (TextUtils.isEmpty(editable.toString())) {
                    return;
                }
                if (streamerPresenter != null) {
                    searchUserDisposable = streamerPresenter.searchMemberList(editable.toString());
                }
            }
        }, 1000);
    }
    // </editor-fold>
}
