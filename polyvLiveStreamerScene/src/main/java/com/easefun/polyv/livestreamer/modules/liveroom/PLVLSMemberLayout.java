package com.easefun.polyv.livestreamer.modules.liveroom;

import static com.plv.foundationsdk.utils.PLVAppUtils.getString;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVMemberItemDataBean;
import com.easefun.polyv.livecommon.module.modules.streamer.model.PLVStreamerControlLinkMicAction;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.PLVSipLinkMicViewModel;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingInListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicCallingOutListState;
import com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo.PLVSipLinkMicUiState;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.span.PLVCenterVerticalAbsoluteSizeSpan;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.PLVConfirmDialog;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundColorView;
import com.easefun.polyv.livecommon.ui.widget.tabview.PLVTabLinearLayout;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.liveroom.adapter.PLVLSMemberAdapter;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSConfirmDialog;
import com.easefun.polyv.livestreamer.ui.widget.PLVLSMemberTabIndicateTextView;
import com.plv.business.model.ppt.PLVPPTAuthentic;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.proxy.PLVDynamicProxy;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.livescenes.access.PLVUserRole;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 成员列表布局
 */
public class PLVLSMemberLayout extends FrameLayout implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="成员变量">

    private static final int MEMBER_LAYOUT_MENU_WIDTH = ConvertUtils.dp2px(425);

    private final PLVSipLinkMicViewModel sipLinkMicViewModel = PLVDependManager.getInstance().get(PLVSipLinkMicViewModel.class);

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //推流和连麦presenter
    private IPLVStreamerContract.IStreamerPresenter streamerPresenter;

    //布局弹层
    private PLVMenuDrawer menuDrawer;

    //view
    private PLVBlurView blurLy;
    private PLVTabLinearLayout memberListTabLayout;
    private PLVLSMemberTabIndicateTextView memberListTv;
    private ConstraintLayout memberSipLinkmicListLayout;
    private PLVLSMemberTabIndicateTextView memberSipLinkmicListTv;
    private PLVRoundColorView memberSipLinkmicListRedPointView;
    private TextView memberListLinkMicDownAllTv;
    private TextView memberListLinkMicMuteAllAudioTv;
    private View memberSplitView;
    private FrameLayout memberListLayout;
    private RecyclerView memberListRv;
    private RecyclerView memberSearchListRv;
    private EditText memberSearchEt;
    private PLVLSSipLinkMicMemberLayout memberSipLinkmicLayout;
    private TextView memberListLinkmicConfigTv;

    private final PLVLSLinkMicTypeSettingLayout linkMicTypeSettingLayout = new PLVLSLinkMicTypeSettingLayout(getContext());

    private final MemberListOperationDispatcher memberListOperationDispatcher = new MemberListOperationDispatcher();

    //adapter
    private PLVLSMemberAdapter memberAdapter;
    private PLVLSMemberAdapter memberSearchAdapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable lastRunnable;

    //disposable
    private Disposable updateBlurViewDisposable;
    private Disposable searchUserDisposable;

    //推流状态
    private boolean isStartedStatus;

    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSMemberLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSMemberLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        // init memberListRv
        memberAdapter = new PLVLSMemberAdapter(liveRoomDataManager);
        memberListRv.setAdapter(memberAdapter);
        PLVLSMemberAdapter.OnViewActionListener listener = new PLVLSMemberAdapter.OnViewActionListener() {
            @Override
            public void onMicControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onMicControl(position, isMute);
                }
            }

            @Override
            public void onCameraControl(int position, boolean isMute) {
                if (onViewActionListener != null) {
                    onViewActionListener.onCameraControl(position, isMute);
                }
            }

            @Override
            public void onFrontCameraControl(int position, boolean isFront) {
                if (onViewActionListener != null) {
                    onViewActionListener.onFrontCameraControl(position, isFront);
                }
            }

            @Override
            public void onControlUserLinkMic(int position, PLVStreamerControlLinkMicAction action) {
                if (onViewActionListener != null) {
                    onViewActionListener.onControlUserLinkMic(position, action);
                }
            }

            @Override
            public void onGrantSpeakerPermission(int position, String userId, boolean isGrant) {
                if(onViewActionListener != null){
                    onViewActionListener.onGrantSpeakerPermission(position, userId, isGrant);
                }
            }

            @Override
            public int getPosition(PLVMemberItemDataBean memberItemDataBean) {
                if (onViewActionListener != null) {
                    return onViewActionListener.getPosition(memberItemDataBean);
                }
                return -1;
            }
        };
        memberAdapter.setOnViewActionListener(listener);

        //init memberSearchListRv
        memberSearchAdapter = new PLVLSMemberAdapter(liveRoomDataManager);
        memberSearchAdapter.setMemberListShow(false);
        memberSearchListRv.setAdapter(memberSearchAdapter);
        memberSearchAdapter.setOnViewActionListener(listener);

        if (PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
            memberListLinkMicDownAllTv.setVisibility(INVISIBLE);
            memberListLinkMicMuteAllAudioTv.setVisibility(INVISIBLE);
        }
        updateMemberListLinkMicShowType(liveRoomDataManager.isOnlyAudio());
        updateLinkMicStrategy(liveRoomDataManager);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_live_room_member_layout, this);

        findView();

        initTabLayout();

        memberListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // ignore IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
                }
            }
        });
        memberListRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(0, ConvertUtils.dp2px(8)));

        memberSearchListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    // ignore IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder
                }
            }
        });
        memberSearchListRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(0, ConvertUtils.dp2px(8)));

        PLVBlurUtils.initBlurView(blurLy);

        observeSipLinkMicListUpdate();
    }

    private void findView() {
        blurLy = findViewById(R.id.blur_ly);
        memberListTabLayout = findViewById(R.id.plvls_member_list_tab_layout);
        memberListTv = findViewById(R.id.plvls_member_list_tv);
        memberSipLinkmicListLayout = findViewById(R.id.plvls_member_sip_linkmic_list_layout);
        memberSipLinkmicListTv = findViewById(R.id.plvls_member_sip_linkmic_list_tv);
        memberSipLinkmicListRedPointView = findViewById(R.id.plvls_member_sip_linkmic_list_red_point_view);
        memberSipLinkmicListLayout.setVisibility(GONE);
        memberListLinkMicDownAllTv = findViewById(R.id.plvls_member_list_link_mic_down_all_tv);
        memberListLinkMicMuteAllAudioTv = findViewById(R.id.plvls_member_list_link_mic_mute_all_audio_tv);
        memberSplitView = findViewById(R.id.plvls_member_split_view);
        memberListLayout = findViewById(R.id.plvls_member_list_layout);
        memberListRv = findViewById(R.id.plvls_member_list_rv);
        memberSipLinkmicLayout = findViewById(R.id.plvls_member_sip_linkmic_layout);
        memberListLinkmicConfigTv = findViewById(R.id.plvls_member_list_linkmic_config_tv);
        memberSearchListRv = findViewById(R.id.plvls_member_search_list_rv);
        memberSearchEt = findViewById(R.id.plvls_member_search_et);

        memberListLinkMicDownAllTv.setOnClickListener(this);
        memberListLinkMicMuteAllAudioTv.setOnClickListener(this);
        memberListTv.setOnClickListener(this);
        memberSipLinkmicListLayout.setOnClickListener(this);
        memberListLinkmicConfigTv.setOnClickListener(this);

        memberSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideSoftInput(memberSearchEt);
                    return true;
                }
                return false;
            }
        });
        memberSearchEt.addTextChangedListener(new TextWatcher() {

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
                    memberSearchListRv.setVisibility(View.INVISIBLE);
                    memberListRv.setVisibility(View.VISIBLE);
                } else {
                    searchUser(editable);
                }
            }
        });
    }

    private void initTabLayout() {
        memberListTv.setDrawBottomLineOnSelected(true);
        memberSipLinkmicListTv.setDrawBottomLineOnSelected(true);

        memberListOperationDispatcher.selectView(memberListTv);
    }

    private void observeSipLinkMicListUpdate() {
        sipLinkMicViewModel.getUiStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicUiState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicUiState sipLinkMicUiState) {
                if (sipLinkMicUiState == null) {
                    return;
                }
                memberSipLinkmicListLayout.setVisibility(sipLinkMicUiState.sipEnable ? VISIBLE : GONE);
            }
        });
        sipLinkMicViewModel.getCallingInListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingInListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingInListState sipLinkMicCallingInListState) {
                if (sipLinkMicCallingInListState == null || sipLinkMicCallingInListState.callingInViewerList.isEmpty()) {
                    return;
                }
                if (!memberSipLinkmicListLayout.isSelected()) {
                    memberSipLinkmicListRedPointView.setVisibility(VISIBLE);
                }
            }
        });
        sipLinkMicViewModel.getCallingOutListStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVSipLinkMicCallingOutListState>() {
            @Override
            public void onChanged(@Nullable PLVSipLinkMicCallingOutListState sipLinkMicCallingOutListState) {
                if (sipLinkMicCallingOutListState == null || sipLinkMicCallingOutListState.callingOutViewerList.isEmpty()) {
                    return;
                }
                if (!memberSipLinkmicListLayout.isSelected()) {
                    memberSipLinkmicListRedPointView.setVisibility(VISIBLE);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 布局控制、监听设置等">
    public void open() {
        if (menuDrawer == null) {
            //初始化menuDrawer
            menuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
            );
            menuDrawer.setMenuView(this);
            menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
            menuDrawer.setMenuSize(MEMBER_LAYOUT_MENU_WIDTH);
            menuDrawer.setDrawOverlay(false);
            menuDrawer.setDropShadowEnabled(false);
            menuDrawer.openMenu();
            menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
                @Override
                public void onDrawerStateChange(int oldState, int newState) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                    }
                    if (newState == PLVMenuDrawer.STATE_CLOSED) {
                        memberSearchEt.setText("");
                        KeyboardUtils.hideSoftInput(memberSearchEt);
                        menuDrawer.detachToContainer();
                        stopUpdateBlurViewTimer();
                    } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                        startUpdateBlurViewTimer();
                    }
                }

                @Override
                public void onDrawerSlide(float openRatio, int offsetPixels) {
                    if (onDrawerStateChangeListener != null) {
                        onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                    }
                }
            });

            memberAdapter.setIsFirstOpenMemberLayout();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isOpen() {
        return menuDrawer != null && menuDrawer.isMenuVisible();
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return PLVDynamicProxy.forClass(IPLVStreamerContract.IStreamerView.class)
                .proxyAll(
                        streamerView,
                        linkMicTypeSettingLayout.getStreamerView()
                );
    }

    public void setOnlineCount(int onlineCount) {
        final PLVSpannableStringBuilder sb = new PLVSpannableStringBuilder(getString(R.string.plvls_live_member_count_hint_text));
        if (memberAdapter.getItemCount() != 0) {
            sb.appendExclude(
                    "(" + Math.max(onlineCount, memberAdapter.getItemCount()) + ")",
                    new PLVCenterVerticalAbsoluteSizeSpan(ConvertUtils.sp2px(12))
            );
        }
        memberListTv.setText(sb);
    }

    public void setStreamerStatus(boolean isStartedStatus) {
        this.isStartedStatus = isStartedStatus;
        memberAdapter.setStreamerStatus(isStartedStatus);
        memberSearchAdapter.setStreamerStatus(isStartedStatus);
    }

    public void updateLinkMicMediaType(boolean isVideoLinkMicType, boolean isOpenLinkMic) {
        memberAdapter.updateLinkMicMediaType(isVideoLinkMicType, isOpenLinkMic);
        memberSearchAdapter.updateLinkMicMediaType(isVideoLinkMicType, isOpenLinkMic);
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

    public void destroy() {
        close();
        stopUpdateBlurViewTimer();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="成员列表 - MVP模式的view层实现">
    private PLVAbsStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            streamerPresenter = presenter;
        }

        @Override
        public void onUsersLeave(List<PLVLinkMicItemDataBean> leaveUsers) {
            // 用户退出时，收回主讲权限
            for (PLVLinkMicItemDataBean leaveUser : leaveUsers) {
                if (leaveUser.isHasSpeaker()) {
                    onViewActionListener.onGrantSpeakerPermission(-1, leaveUser.getUserId(), false);
                }
            }
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
            memberAdapter.updateVolumeChanged();
            memberSearchAdapter.updateVolumeChanged();
        }

        @Override
        public void onLocalUserMicVolumeChanged(int volume) {
            memberAdapter.updateVolumeChanged();
            memberSearchAdapter.updateVolumeChanged();
        }

        @Override
        public void onRemoteUserVolumeChanged(List<PLVMemberItemDataBean> linkMicList) {
            super.onRemoteUserVolumeChanged(linkMicList);
            memberAdapter.updateVolumeChanged();
            memberSearchAdapter.updateVolumeChanged();
        }

        @Override
        public void onUpdateMemberListData(List<PLVMemberItemDataBean> dataBeanList) {
            super.onUpdateMemberListData(dataBeanList);
            memberAdapter.update(dataBeanList);
        }

        @Override
        public void onUpdateMemberSearchListData(List<PLVMemberItemDataBean> dataBeanList) {
            if (TextUtils.isEmpty(memberSearchEt.getText().toString())) {
                return;
            }
            memberSearchListRv.setVisibility(View.VISIBLE);
            memberListRv.setVisibility(View.INVISIBLE);
            memberSearchAdapter.update(dataBeanList);
        }

        @Override
        public void onCameraDirection(boolean front, int pos, String uid) {
            memberAdapter.updateCameraDirection(pos);
            memberSearchAdapter.updateCameraDirection(uid);
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
            PLVLSConfirmDialog.Builder.context(getContext())
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
            if(type.equals(PLVPPTAuthentic.PermissionType.TEACHER)) {
                memberAdapter.updatePermissionChange();
                memberSearchAdapter.updatePermissionChange();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="连麦类型控制">

    /**
     * 更新成员列表显示类型
     * @param isOnlyAudio 只显示音频部分
     */
    private void updateMemberListLinkMicShowType(boolean isOnlyAudio){
        if(memberAdapter != null){
            memberAdapter.setOnlyShowAudioUI(isOnlyAudio);
        }
        if (memberSearchAdapter != null) {
            memberSearchAdapter.setOnlyShowAudioUI(isOnlyAudio);
        }
    }

    private void updateLinkMicStrategy(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean canControlLinkMic = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN);
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        memberListLinkmicConfigTv.setVisibility(canControlLinkMic && isNewLinkMicStrategy ? View.VISIBLE : View.GONE);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="定时更新模糊背景view">
    private void startUpdateBlurViewTimer() {
        stopUpdateBlurViewTimer();
        updateBlurViewDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        blurLy.invalidate();
                    }
                });
    }

    private void stopUpdateBlurViewTimer() {
        if (updateBlurViewDisposable != null) {
            updateBlurViewDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="工具方法">
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
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == memberListLinkMicDownAllTv.getId()) {
            memberListOperationDispatcher.onClickCloseAllLinkMic();
        } else if (id == memberListLinkMicMuteAllAudioTv.getId()) {
            memberListOperationDispatcher.onClickMuteAllAudio();
        } else if (id == memberListTv.getId()) {
            memberListOperationDispatcher.selectView(memberListTv);
        } else if (id == memberSipLinkmicListLayout.getId()) {
            memberListOperationDispatcher.selectView(memberSipLinkmicListLayout);
        } else if (id == memberListLinkmicConfigTv.getId()) {
            linkMicTypeSettingLayout.show();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener extends PLVLSMemberAdapter.OnViewActionListener {
        /**
         * 下麦全体连麦用户
         */
        void closeAllUserLinkMic();

        /**
         * 全体连麦用户禁用/开启声音
         *
         * @param isMute true：禁用，false：开启
         */
        void muteAllUserAudio(boolean isMute);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 不同列表选项分发">

    private abstract static class AbsMemberListOperation {

        protected abstract void onClickCloseAllLinkMic();

        protected abstract void onClickMuteAllAudio();

        protected abstract void onSelectTab();

    }

    private class MemberListOperationDispatcher extends AbsMemberListOperation {

        private final Map<Integer, AbsMemberListOperation> dispatcherMap = new HashMap<Integer, AbsMemberListOperation>(4) {{
            put(R.id.plvls_member_list_tv, new MemberListOperationImpl());
            put(R.id.plvls_member_sip_linkmic_list_layout, new SipLinkMicListOperationImpl());
        }};

        private int currentSelectedViewId;

        public void selectView(View view) {
            currentSelectedViewId = view.getId();
            memberListTabLayout.setSelectedChild(view);
            memberSipLinkmicListTv.setSelected(view == memberSipLinkmicListLayout);
            hideLayout();
            onSelectTab();
        }

        protected void onClickCloseAllLinkMic() {
            if (dispatcherMap.get(currentSelectedViewId) != null) {
                dispatcherMap.get(currentSelectedViewId).onClickCloseAllLinkMic();
            }
        }

        protected void onClickMuteAllAudio() {
            if (dispatcherMap.get(currentSelectedViewId) != null) {
                dispatcherMap.get(currentSelectedViewId).onClickMuteAllAudio();
            }
        }

        protected void onSelectTab() {
            if (dispatcherMap.get(currentSelectedViewId) != null) {
                dispatcherMap.get(currentSelectedViewId).onSelectTab();
            }
        }

        private void hideLayout() {
            memberListLayout.setVisibility(View.GONE);
            memberSipLinkmicLayout.setVisibility(View.GONE);
        }
    }

    private class MemberListOperationImpl extends AbsMemberListOperation {

        private boolean isCurrentMuteAllAudio = false;

        @Override
        protected void onClickCloseAllLinkMic() {
            if (isStartedStatus) {
                PLVLSConfirmDialog.Builder.context(getContext())
                        .setTitleVisibility(View.GONE)
                        .setContent(R.string.plv_linkmic_dialog_hang_all_off_confirm_ask)
                        .setRightButtonText(R.string.plv_common_dialog_confirm)
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                if (onViewActionListener != null) {
                                    onViewActionListener.closeAllUserLinkMic();
                                }
                                PLVToast.Builder.context(getContext())
                                        .setText(R.string.plv_linkmic_toast_hang_all_off)
                                        .build()
                                        .show();
                            }
                        })
                        .show();
            } else {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_streamer_toast_please_click_class_first)
                        .build()
                        .show();
            }
        }

        @Override
        protected void onClickMuteAllAudio() {
            if (isStartedStatus) {
                final boolean currentIsMuteAll = memberListLinkMicMuteAllAudioTv.isSelected();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (onViewActionListener != null) {
                            onViewActionListener.muteAllUserAudio(!currentIsMuteAll);
                            memberListLinkMicMuteAllAudioTv.setSelected(!currentIsMuteAll);
                            memberListLinkMicMuteAllAudioTv.setText(!currentIsMuteAll ? R.string.plv_linkmic_unmute_all_audio : R.string.plv_linkmic_mute_all_audio);
                        }
                    }
                };
                if (!currentIsMuteAll) {
                    PLVLSConfirmDialog.Builder.context(getContext())
                            .setTitleVisibility(View.GONE)
                            .setContent(R.string.plv_linkmic_dialog_mute_all_audio_confirm_ask)
                            .setRightButtonText(R.string.plv_common_dialog_confirm)
                            .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, View v) {
                                    dialog.dismiss();
                                    runnable.run();
                                    PLVToast.Builder.context(getContext())
                                            .setText(R.string.plv_linkmic_toast_mute_all_audio)
                                            .build()
                                            .show();
                                }
                            })
                            .show();
                } else {
                    runnable.run();
                }
            } else {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_streamer_toast_please_click_class_first)
                        .build()
                        .show();
            }
        }

        @Override
        protected void onSelectTab() {
            updateMuteAllAudioText();
            memberListLayout.setVisibility(VISIBLE);
        }

        private void updateMuteAllAudioText() {
            if (PLVUserAbilityManager.myAbility().hasRole(PLVUserRole.STREAMER_TEACHER)) {
                memberListLinkMicDownAllTv.setVisibility(VISIBLE);
                memberListLinkMicMuteAllAudioTv.setVisibility(VISIBLE);
            }
            memberListLinkMicMuteAllAudioTv.setText(isCurrentMuteAllAudio ? R.string.plv_linkmic_unmute_all_audio : R.string.plv_linkmic_mute_all_audio);
        }
    }

    private class SipLinkMicListOperationImpl extends AbsMemberListOperation {
        @Override
        protected void onClickCloseAllLinkMic() {
            // Not Support
        }

        @Override
        protected void onClickMuteAllAudio() {
            // Not Support
        }

        @Override
        protected void onSelectTab() {
            memberListLinkMicDownAllTv.setVisibility(GONE);
            memberListLinkMicMuteAllAudioTv.setVisibility(GONE);
            memberSipLinkmicLayout.setVisibility(VISIBLE);
            memberSipLinkmicListRedPointView.setVisibility(GONE);
        }
    }

    // </editor-fold>
}
