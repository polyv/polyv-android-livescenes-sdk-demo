package com.plv.streameralone.scenes.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.plv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.plv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.plv.livecommon.module.modules.streamer.contract.IPLVStreamerContract;
import com.plv.livecommon.module.utils.PLVToast;
import com.plv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.plv.livecommon.ui.widget.PLVConfirmDialog;
import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livecommon.ui.window.PLVBaseFragment;
import com.plv.livescenes.streamer.linkmic.IPLVSLinkMicEventSender;
import com.plv.livescenes.streamer.linkmic.PLVSLinkMicEventSender;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.streameralone.R;
import com.plv.streameralone.modules.chatroom.IPLVSAChatroomLayout;
import com.plv.streameralone.modules.chatroom.PLVSAChatroomLayout;
import com.plv.streameralone.modules.chatroom.widget.PLVSAGreetingView;
import com.plv.streameralone.modules.chatroom.widget.PLVSARewardGiftAnimView;
import com.plv.streameralone.modules.liveroom.PLVSAMemberLayout;
import com.plv.streameralone.modules.liveroom.PLVSAMoreLayout;
import com.plv.streameralone.modules.statusbar.PLVSAStatusBarLayout;
import com.plv.streameralone.ui.widget.PLVSAConfirmDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机开播纯视频主页fragment
 */
public class PLVSAStreamerHomeFragment extends PLVBaseFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">

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
    //view
    private TextView plvsaToolBarCallInputTv;
    private ImageView plvsaToolBarMoreIv;
    private ImageView plvsaToolBarMemberIv;
    private ImageView plvsaToolBarLinkmicIv;
    private View plvsaToolBarMemberLinkmicRequestTipsView;

    // 聊天室消息列表观察者
    private RecyclerView.AdapterDataObserver chatMessageDataObserver;

    //listener
    private OnViewActionListener onViewActionListener;
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
        plvsaStatusBarLayout = (PLVSAStatusBarLayout) findViewById(R.id.plvsa_status_bar_layout);
        plvsaChatroomRewardLy = findViewById(R.id.plvsa_chatroom_reward_ly);
        plvsaChatroomGreetingLy = findViewById(R.id.plvsa_chatroom_greet_ly);
        plvsaChatroomLayout = (PLVSAChatroomLayout) findViewById(R.id.plvsa_chatroom_layout);
        plvsaToolBarCallInputTv = (TextView) findViewById(R.id.plvsa_tool_bar_call_input_tv);
        plvsaToolBarMoreIv = (ImageView) findViewById(R.id.plvsa_tool_bar_more_iv);
        plvsaToolBarMemberIv = (ImageView) findViewById(R.id.plvsa_tool_bar_member_iv);
        plvsaToolBarLinkmicIv = (ImageView) findViewById(R.id.plvsa_tool_bar_linkmic_iv);
        plvsaToolBarMemberLinkmicRequestTipsView = findViewById(R.id.plvsa_tool_bar_member_linkmic_request_tips_view);

        plvsaToolBarCallInputTv.setOnClickListener(this);
        plvsaToolBarMoreIv.setOnClickListener(this);
        plvsaToolBarMemberIv.setOnClickListener(this);
        plvsaToolBarLinkmicIv.setOnClickListener(this);

        initMoreLayout();
        initMemberLayout();
    }

    private void initMoreLayout() {
        moreLayout = new PLVSAMoreLayout(view.getContext());
    }

    private void initMemberLayout() {
        memberLayout = new PLVSAMemberLayout(view.getContext());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //after onViewCreated init
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        //初始化状态栏布局
        plvsaStatusBarLayout.init(liveRoomDataManager);
        //初始化聊天室布局
        plvsaChatroomLayout.init(liveRoomDataManager);

        observeChatroomLayout();
        observeStatusBarLayout();
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

    public void openMemberLayoutAndHideUserRequestTips() {
        memberLayout.open();
        hideUserRequestTips();
    }

    public IPLVStreamerContract.IStreamerView getMoreLayoutStreamerView() {
        return moreLayout == null ? null : moreLayout.getStreamerView();
    }

    public IPLVStreamerContract.IStreamerView getMemberLayoutStreamerView() {
        return memberLayout == null ? null : memberLayout.getStreamerView();
    }

    public IPLVStreamerContract.IStreamerView getStatusBarLayoutStreamerView() {
        return plvsaStatusBarLayout == null ? null : plvsaStatusBarLayout.getStreamerView();
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
    }
    // </editor-fold>

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
        } else if (id == R.id.plvsa_tool_bar_linkmic_iv) {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    boolean isOpenLinkMic = PLVSLinkMicEventSender.getInstance().openLinkMic(true, !v.isSelected(), new IPLVSLinkMicEventSender.PLVSMainCallAck() {
                        @Override
                        public void onCall(Object... args) {
                            v.setSelected(!v.isSelected());
                            PLVToast.Builder.context(v.getContext())
                                    .setText(v.isSelected() ? R.string.plv_linkmic_video_type_open_success_tip : R.string.plv_linkmic_hang_all_off_tip)
                                    .build()
                                    .show();
                        }
                    });
                    if (!isOpenLinkMic) {
                        PLVToast.Builder.context(v.getContext())
                                .setText(R.string.plv_linkmic_error_tip_have_not_opened)
                                .build()
                                .show();
                    }
                }
            };
            if (v.isSelected()) {
                new PLVSAConfirmDialog(v.getContext())
                        .setTitle("确定关闭视频连麦吗？")
                        .setContent("关闭后将挂断进行中的所有连麦")
                        .setLeftButtonText(R.string.plv_common_dialog_cancel)
                        .setRightButtonText("确定")
                        .setRightBtnListener(new PLVConfirmDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, View v) {
                                dialog.dismiss();
                                runnable.run();
                            }
                        })
                        .show();
                return;
            }
            runnable.run();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        /**
         * onActivityCreated call
         */
        void onViewCreated();

        void onStopLive();

        void onClickToOpenMemberLayout();

        boolean showCleanUpLayout();
    }
    // </editor-fold>
}
