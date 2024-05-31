package com.easefun.polyv.streameralone.scenes.fragments;

import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.PLVBeautyViewModel;
import com.easefun.polyv.livecommon.module.modules.beauty.viewmodel.vo.PLVBeautyUiState;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.easefun.polyv.livecommon.module.modules.streamer.view.PLVAbsStreamerView;
import com.easefun.polyv.livecommon.module.utils.PLVDebounceClicker;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.IPLVSAChatroomLayout;
import com.easefun.polyv.streameralone.modules.chatroom.PLVSAChatroomLayout;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSAGreetingView;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSARewardGiftAnimView;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSACommodityControlLayout;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSAMemberLayout;
import com.easefun.polyv.streameralone.modules.liveroom.PLVSAMoreLayout;
import com.easefun.polyv.streameralone.modules.statusbar.PLVSAStatusBarLayout;
import com.easefun.polyv.streameralone.modules.streamer.widget.PLVSALinkMicControlButton;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.component.proxy.PLVDynamicProxy;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.access.PLVUserAbility;
import com.plv.livescenes.access.PLVUserAbilityManager;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机开播纯视频主页fragment
 */
public class PLVSAStreamerHomeFragment extends PLVBaseFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

    // 布局间距
    private static final int LAYOUT_VERTICAL_PADDING = ConvertUtils.dp2px(8);
    private static final int LAYOUT_HORIZON_PADDING_PORT = ConvertUtils.dp2px(8);
    private static final int LAYOUT_HORIZON_PADDING_LAND = ConvertUtils.dp2px(16);

    /**
     * 当聊天室消息数量达到此值时，显示清屏指引
     */
    private static final int CHAT_MESSAGE_SIZE_TO_SHOW_CLEAN_UP_HINT = 10;

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //更多布局
    private PLVSAMoreLayout moreLayout;
    //成员列表布局
    private PLVSAMemberLayout memberLayout;
    //状态栏布局
    private PLVSAStatusBarLayout plvsaStatusBarLayout;
    //打赏布局
    private PLVSARewardGiftAnimView plvsaChatroomRewardLy;
    //欢迎语布局
    private PLVSAGreetingView plvsaChatroomGreetingLy;
    //聊天室布局
    private IPLVSAChatroomLayout plvsaChatroomLayout;
    // 商品库管理布局
    private PLVSACommodityControlLayout commodityControlLayout;
    //view
    private ConstraintLayout homeFragmentLayout;
    private TextView plvsaToolBarCallInputTv;
    private ImageView plvsaToolBarMoreIv;
    private ImageView plvsaToolBarMemberIv;
    private PLVSALinkMicControlButton plvsaToolBarLinkmicIv;
    private ImageView plvsaToolBarLinkmicTypeIv;
    private View plvsaToolBarMemberLinkmicRequestTipsView;
    private TextView plvsaToolBarLinkmicTypeTip;
    private ImageView toolBarCommodityControlIv;

    // 聊天室消息列表观察者
    private RecyclerView.AdapterDataObserver chatMessageDataObserver;

    //listener
    private OnViewActionListener onViewActionListener;

    private boolean isBeautyLayoutShowing = false;
    private boolean isVideoLinkMic = false;
    private boolean isOpenLinkMic = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvsa_streamer_home_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        homeFragmentLayout = findViewById(R.id.plvsa_home_fragment_layout);
        plvsaStatusBarLayout = (PLVSAStatusBarLayout) findViewById(R.id.plvsa_status_bar_layout);
        plvsaChatroomRewardLy = findViewById(R.id.plvsa_chatroom_reward_ly);
        plvsaChatroomGreetingLy = findViewById(R.id.plvsa_chatroom_greet_ly);
        plvsaChatroomLayout = (PLVSAChatroomLayout) findViewById(R.id.plvsa_chatroom_layout);
        plvsaToolBarCallInputTv = (TextView) findViewById(R.id.plvsa_tool_bar_call_input_tv);
        plvsaToolBarMoreIv = (ImageView) findViewById(R.id.plvsa_tool_bar_more_iv);
        plvsaToolBarMemberIv = (ImageView) findViewById(R.id.plvsa_tool_bar_member_iv);
        plvsaToolBarLinkmicIv = findViewById(R.id.plvsa_tool_bar_linkmic_iv);
        plvsaToolBarLinkmicTypeIv = (ImageView) findViewById(R.id.plvsa_tool_bar_linkmic_type_iv);
        plvsaToolBarMemberLinkmicRequestTipsView = findViewById(R.id.plvsa_tool_bar_member_linkmic_request_tips_view);
        plvsaToolBarLinkmicTypeTip = findViewById(R.id.plvsa_tool_bar_linkmic_type_tip);
        toolBarCommodityControlIv = findViewById(R.id.plvsa_tool_bar_commodity_control_iv);

        plvsaToolBarCallInputTv.setOnClickListener(this);
        plvsaToolBarMoreIv.setOnClickListener(this);
        plvsaToolBarMemberIv.setOnClickListener(this);
        plvsaToolBarLinkmicTypeIv.setOnClickListener(this);
        toolBarCommodityControlIv.setOnClickListener(this);

        initLinkMicControlWindow();

        moreLayout = new PLVSAMoreLayout(view.getContext());
        memberLayout = new PLVSAMemberLayout(view.getContext());
        commodityControlLayout = new PLVSACommodityControlLayout(view.getContext());

        observeBeautyLayoutStatus();

        updateViewWithOrientation();
    }

    private void initLinkMicControlWindow() {
        plvsaToolBarLinkmicIv.setOnViewActionListener(new PLVSALinkMicControlButton.OnViewActionListener() {
            @Override
            public void onLinkMicOpenStateChanged(boolean isVideoLinkMicType, boolean isOpenLinkMic) {
                PLVSAStreamerHomeFragment.this.isVideoLinkMic = isVideoLinkMicType;
                PLVSAStreamerHomeFragment.this.isOpenLinkMic = isOpenLinkMic;
                if (memberLayout != null) {
                    memberLayout.notifyLinkMicTypeChange(isVideoLinkMicType, isOpenLinkMic);
                }
                if (onViewActionListener != null) {
                    onViewActionListener.onLinkMicMediaTypeUpdate(isVideoLinkMicType, isOpenLinkMic);
                }
            }
        });
    }

    private void initAutoOpenLinkMic(IPLVLiveRoomDataManager liveRoomDataManager) {
        if (PLVUserAbilityManager.myAbility().notHasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN)) {
            return;
        }
        final String channelId = liveRoomDataManager.getConfig().getChannelId();
        final boolean isAutoOpenLinkMic = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.STREAMER_DEFAULT_OPEN_LINKMIC_ENABLE);
        final String autoOpenLinkMicType = PLVChannelFeatureManager.onChannel(channelId).get(PLVChannelFeature.STREAMER_DEFAULT_OPEN_LINKMIC_TYPE);
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        plvsaToolBarLinkmicIv.performAutoOpenLinkMic(isAutoOpenLinkMic, "video".equals(autoOpenLinkMicType), isNewLinkMicStrategy);
    }

    private void observeBeautyLayoutStatus() {
        if (getActivity() == null) {
            return;
        }
        PLVDependManager.getInstance().get(PLVBeautyViewModel.class)
                .getUiState()
                .observe(getActivity(), new Observer<PLVBeautyUiState>() {
                    @Override
                    public void onChanged(@Nullable PLVBeautyUiState beautyUiState) {
                        PLVSAStreamerHomeFragment.this.isBeautyLayoutShowing = beautyUiState != null && beautyUiState.isBeautyMenuShowing;
                        updateVisibility();
                    }
                });
    }

    private void observeCommodityControlSwitch(@NonNull IPLVLiveRoomDataManager liveRoomDataManager) {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        liveRoomDataManager.getClassDetailVO()
                .observe((LifecycleOwner) context, new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                    @Override
                    public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> data) {
                        if (data != null && data.isSuccess() && data.getData() != null && data.getData().getData() != null) {
                            final boolean isCommodityControlSwitchOn = data.getData().getData().isMobileAnchorProductEnabled();
                            final boolean hasControlPermission = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_COMMODITY_ALLOW_CONTROL);
                            toolBarCommodityControlIv.setVisibility(isCommodityControlSwitchOn && hasControlPermission ? View.VISIBLE : View.GONE);
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Streamer - Mvp View">

    private final IPLVStreamerContract.IStreamerView streamerView = new PLVAbsStreamerView() {

        @Override
        public void setPresenter(@NonNull IPLVStreamerContract.IStreamerPresenter presenter) {
            if (homeFragmentLayout == null) {
                return;
            }
            presenter.getData().getStreamerStatus().observe((LifecycleOwner) homeFragmentLayout.getContext(), new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable Boolean isLive) {
                    if (Boolean.TRUE.equals(isLive)) {
                        if (liveRoomDataManager != null) {
                            initAutoOpenLinkMic(liveRoomDataManager);
                        }
                    }
                }
            });
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //after onViewCreated init
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        //初始化状态栏布局
        plvsaStatusBarLayout.init(liveRoomDataManager);
        //初始化聊天室布局
        plvsaChatroomLayout.init(liveRoomDataManager);
        //初始化成员布局
        memberLayout.init(liveRoomDataManager);
        //初始化更多布局
        moreLayout.init(liveRoomDataManager);
        commodityControlLayout.init(liveRoomDataManager);

        observeChatroomLayout();
        observeStatusBarLayout();
        observeCommodityControlSwitch(liveRoomDataManager);
        updateGuestLayout();
        updateLinkMicStrategy(liveRoomDataManager);
    }

    public void chatroomLogin(){
        plvsaChatroomLayout.loginAndLoadHistory();
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public void updateChannelName() {
        if (plvsaStatusBarLayout != null && liveRoomDataManager != null) {
            plvsaStatusBarLayout.updateChannelName(liveRoomDataManager.getConfig().getChannelName());
        }
    }

    public void updateUserRequestStatus() {
        showUserRequestTips();
    }

    public void updateLinkMicLayoutTypeVisibility(boolean isShow) {
        if(isShow && !loadStatus()){
            plvsaToolBarLinkmicTypeTip.setVisibility(View.VISIBLE);
            saveStatus();
        } else {
            plvsaToolBarLinkmicTypeTip.setVisibility(View.INVISIBLE);
        }
        plvsaToolBarLinkmicTypeIv.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void openMemberLayoutAndHideUserRequestTips() {
        memberLayout.open();
        hideUserRequestTips();
    }

    public void closeMemberLayout() {
        memberLayout.closeAndHideWindow();
    }

    public IPLVStreamerContract.IStreamerView getStreamerView() {
        return PLVDynamicProxy.forClass(IPLVStreamerContract.IStreamerView.class)
                .proxyAll(
                        streamerView,
                        nullable(new PLVSugarUtil.Supplier<IPLVStreamerContract.IStreamerView>() {
                            @Override
                            public IPLVStreamerContract.IStreamerView get() {
                                return moreLayout.getStreamerView();
                            }
                        }),
                        nullable(new PLVSugarUtil.Supplier<IPLVStreamerContract.IStreamerView>() {
                            @Override
                            public IPLVStreamerContract.IStreamerView get() {
                                return memberLayout.getStreamerView();
                            }
                        }),
                        nullable(new PLVSugarUtil.Supplier<IPLVStreamerContract.IStreamerView>() {
                            @Override
                            public IPLVStreamerContract.IStreamerView get() {
                                return plvsaStatusBarLayout.getStreamerView();
                            }
                        }),
                        plvsaToolBarLinkmicIv.streamerView
                );
    }

    public PLVSAMoreLayout getMoreLayout() {
        return moreLayout;
    }

    public boolean onBackPressed() {
        return plvsaChatroomLayout.onBackPressed()
                || moreLayout.onBackPressed()
                || memberLayout.onBackPressed()
                || plvsaStatusBarLayout.onBackPressed();
    }

    public void destroy() {
        if (plvsaChatroomLayout != null) {
            plvsaChatroomLayout.destroy();
        }
        if (plvsaStatusBarLayout != null) {
            plvsaStatusBarLayout.destroy();
        }
        if (moreLayout != null) {
            moreLayout.destroy();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转">

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateViewWithOrientation();
    }

    private void updateViewWithOrientation() {
        if (getContext() == null) {
            return;
        }
        // padding - left right top bottom
        int pl, pr, pt, pb;
        if (PLVScreenUtils.isPortrait(getContext())) {
            pl = pr = LAYOUT_HORIZON_PADDING_PORT;
        } else {
            pl = pr = LAYOUT_HORIZON_PADDING_LAND;
        }
        pt = pb = LAYOUT_VERTICAL_PADDING;
        view.setPadding(pl, pt, pr, pb);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更新布局">

    /**
     * 更新嘉宾的布局
     */
    private void updateGuestLayout() {
        if (isGuest()) {
            moreLayout.updateCloseRoomLayout(true);
            updateLinkMicLayoutTypeVisibility(false);
            final boolean isAutoLinkMic = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId()).isFeatureSupport(PLVChannelFeature.STREAMER_GUEST_AUTO_LINKMIC_ENABLE);
            plvsaToolBarLinkmicIv.setVisibility(isAutoLinkMic ? View.GONE : View.VISIBLE);
        }
    }

    private void updateVisibility() {
        // 美颜布局显示时，不显示主页布局
        if (isBeautyLayoutShowing) {
            homeFragmentLayout.setVisibility(View.GONE);
            return;
        }
        homeFragmentLayout.setVisibility(View.VISIBLE);
    }

    private void updateLinkMicStrategy(IPLVLiveRoomDataManager liveRoomDataManager) {
        final boolean canControlLinkMic = PLVUserAbilityManager.myAbility().hasAbility(PLVUserAbility.STREAMER_ALLOW_CONTROL_LINK_MIC_OPEN);
        if (!canControlLinkMic) {
            return;
        }
        final boolean isNewLinkMicStrategy = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_NEW_LINKMIC_STRATEGY);
        if (!isNewLinkMicStrategy) {
            plvsaToolBarLinkmicIv.setVisibility(View.VISIBLE);
        } else {
            plvsaToolBarLinkmicIv.setVisibility(View.GONE);
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 聊天室">

    private void observeChatroomLayout() {
        //监听聊天室的在线人数变化
        plvsaChatroomLayout.addOnOnlineCountListener(new IPLVOnDataChangedListener<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer == null) {
                    return;
                }
                plvsaStatusBarLayout.setOnlineCount(integer);
                memberLayout.updateOnlineCount(integer);
            }
        });
        //监听聊天室用户的登录事件
        plvsaChatroomLayout.addOnLoginEventListener(new IPLVOnDataChangedListener<PLVLoginEvent>() {
            @Override
            public void onChanged(@Nullable PLVLoginEvent loginEvent) {
                if (loginEvent == null) {
                    return;
                }
                plvsaChatroomGreetingLy.acceptGreetingMessage(loginEvent);
            }
        });
        //监听聊天室打赏事件
        plvsaChatroomLayout.addOnRewardEventListener(new IPLVOnDataChangedListener<PLVRewardEvent>() {
            @Override
            public void onChanged(@Nullable PLVRewardEvent rewardEvent) {
                if (rewardEvent == null) {
                    return;
                }
                plvsaChatroomRewardLy.acceptRewardGiftMessage(rewardEvent);
                addRewardEventToChatList(rewardEvent);
            }
        });
        plvsaChatroomLayout.addObserverToChatMessageAdapter(chatMessageDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                checkIfNeedShowCleanUpLayout();
            }
        });
        plvsaChatroomLayout.setOnViewActionListener(new PLVSAChatroomLayout.OnViewActionListener() {
            @Override
            public void onKickByServer() {
                if (onViewActionListener != null) {
                    onViewActionListener.onKickByServer();
                }
            }
        });
    }

    private void checkIfNeedShowCleanUpLayout() {
        if (plvsaChatroomLayout != null) {
            int messageListSize = plvsaChatroomLayout.getChatMessageListSize();
            if (messageListSize >= CHAT_MESSAGE_SIZE_TO_SHOW_CLEAN_UP_HINT
                    && onViewActionListener != null) {
                boolean success = onViewActionListener.showCleanUpLayout();
                if (success) {
                    plvsaChatroomLayout.removeObserverFromChatMessageAdapter(chatMessageDataObserver);
                }
            }
        }
    }

    private void addRewardEventToChatList(PLVRewardEvent rewardEvent) {
        PLVCustomGiftEvent customGiftEvent = PLVCustomGiftEvent.generateCustomGiftEvent(rewardEvent);
        List<PLVBaseViewData> dataList = new ArrayList<>();
        dataList.add(new PLVBaseViewData<>(customGiftEvent, PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT));
        plvsaChatroomLayout.addChatMessageToChatList(dataList, false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置布局回调 - 状态栏">

    private void observeStatusBarLayout() {
        //监听状态栏回调
        plvsaStatusBarLayout.setOnStopLiveListener(new PLVSAStatusBarLayout.OnStopLiveListener() {
            @Override
            public void onStopLive() {
                if (onViewActionListener != null) {
                    onViewActionListener.onStopLive();
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="用户请求连麦的提示处理">
    private void showUserRequestTips() {
        if (memberLayout != null && memberLayout.isOpen()) {
            return;
        }
        plvsaToolBarMemberLinkmicRequestTipsView.setVisibility(View.VISIBLE);
    }

    private void hideUserRequestTips() {
        plvsaToolBarMemberLinkmicRequestTipsView.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvsa_tool_bar_call_input_tv) {
            plvsaChatroomLayout.callInputWindow();
        } else if (id == R.id.plvsa_tool_bar_more_iv) {
            moreLayout.open();
        } else if (id == R.id.plvsa_tool_bar_member_iv) {
            openMemberLayoutAndHideUserRequestTips();
            if (onViewActionListener != null) {
                onViewActionListener.onClickToOpenMemberLayout();
            }
        } else if (id == R.id.plvsa_tool_bar_linkmic_type_iv) {
            plvsaToolBarLinkmicTypeTip.setVisibility(View.INVISIBLE);
            if (PLVDebounceClicker.tryClick(this, 800)) {
                v.setSelected(!v.isSelected());
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeLinkMicLayoutType();
                }
            }
        } else if (id == toolBarCommodityControlIv.getId()) {
            commodityControlLayout.show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private boolean isGuest() {
        if (liveRoomDataManager != null) {
            if (PLVSocketUserConstant.USERTYPE_GUEST.equals(liveRoomDataManager.getConfig().getUser().getViewerType())) {
                return true;
            }
        }
        return false;
    }

    private void saveStatus() {
        SPUtils.getInstance().put("plv_key_linkmic_type_tips_is_showed", true);
    }

    private boolean loadStatus() {
        return SPUtils.getInstance().getBoolean("plv_key_linkmic_type_tips_is_showed", false);
    }
    // </editor-fold >


    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * onActivityCreated call
         */
        void onViewCreated();

        void onStopLive();

        void onClickToOpenMemberLayout();

        boolean showCleanUpLayout();

        /**
         * 改变连麦布局类型
         */
        void onChangeLinkMicLayoutType();

        void onLinkMicMediaTypeUpdate(boolean isVideoLinkMicType, boolean isOpen);

        void onKickByServer();
    }
    // </editor-fold>

}
