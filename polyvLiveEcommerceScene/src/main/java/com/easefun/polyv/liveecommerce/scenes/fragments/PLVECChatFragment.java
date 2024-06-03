package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.log.PLVTrackLogHelper;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.layout.PLVECChatOverLengthMessageLayout;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天fragment
 */
public class PLVECChatFragment extends PLVBaseFragment {
    // <editor-folder defaultstate="collapsed" desc="变量">
    //未读信息提醒view
    private TextView unreadMsgTv;
    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;
    //聊天区域
    private PLVMessageRecyclerView chatMsgRv;
    private PLVECChatMessageAdapter chatMessageAdapter;
    private PLVECChatMessageAdapter.OnViewActionListener messageAdapterListener;
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //聊天室presenter
    protected IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_chat_fragment, null);
        initView();
        return view;
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化View">
    private void initView() {
        chatMsgRv = findViewById(R.id.chat_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setAllowReplyMessage(
                PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                        .isFeatureSupport(PLVChannelFeature.LIVE_CHATROOM_VIEWER_QUOTE_REPLY)
        );
        chatMessageAdapter.setOnViewActionListener(new PLVECChatMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(View view, String imgUrl) {
                if (messageAdapterListener != null) {
                    messageAdapterListener.onChatImgClick(view, imgUrl);
                }
            }

            @Override
            public void callOnReplyMessage(PLVChatQuoteVO chatQuoteVO) {
                if (messageAdapterListener != null) {
                    messageAdapterListener.callOnReplyMessage(chatQuoteVO);
                }
            }

            @Override
            public void onShowOverLengthMessage(PLVECChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean) {
                if (messageAdapterListener != null) {
                    messageAdapterListener.onShowOverLengthMessage(chatMessageDataBean);
                }
            }

            @Override
            public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
                if (messageAdapterListener != null) {
                    messageAdapterListener.onReceiveRedPaper(redPaperEvent);
                }
            }
        });
        // 追踪红包曝光事件
        PLVTrackLogHelper.trackReadRedpack(chatMsgRv, chatMessageAdapter.getDataList(), liveRoomDataManager);
        //未读信息view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);
        chatMsgRv.addUnreadView(unreadMsgTv);
        //下拉控件
        swipeLoadView = findViewById(R.id.swipe_load_view);
        swipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (chatroomPresenter != null) {
                    chatroomPresenter.requestChatHistory(0);
                }
            }
        });
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="对外API">
    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }

    public void setOnMessageAdapterListener(PLVECChatMessageAdapter.OnViewActionListener listener) {
        messageAdapterListener = listener;
    }

    public void changeDisplayType(final int displayDataType) {
        runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                chatMessageAdapter.changeDisplayType(displayDataType);
            }
        });
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 添加信息至列表">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean result = chatMessageAdapter.addDataListChanged(chatMessageDataList);
                if (result) {
                    if (isScrollEnd) {
                        chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        chatMsgRv.scrollToBottomOrShowMore(chatMessageDataList.size());
                    }
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean result = chatMessageAdapter.addDataListChangedAtFirst(chatMessageDataList);
                if (result) {
                    if (isScrollEnd) {
                        chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        chatMsgRv.scrollToPosition(0);
                    }
                }
            }
        });
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRemoveAll) {
                    chatMessageAdapter.removeAllDataChanged();
                } else {
                    chatMessageAdapter.removeDataChanged(id);
                }
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {
            super.setPresenter(presenter);
            chatroomPresenter = presenter;
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);//聊天列表里的文本信息textSize
        }

        @Override
        public void onRemoveMessageEvent(@Nullable final String id, final boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    removeChatMessageToList(id, isRemoveAll);
                }
            });
        }

        @Override
        public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {
            //自定义礼物消息已移除，统一通过onRewardEvent实现打赏
        }

        @Override
        public void onLocalSpeakMessage(@Nullable final PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (localMessage != null) {
                        //添加信息至列表
                        List<PLVBaseViewData> dataList = new ArrayList<>();
                        dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
                        addChatMessageToList(dataList, true);
                    }
                }
            });
        }

        @Override
        public void onSpeakImgDataList(final List<PLVBaseViewData> chatMessageDataList) {
            super.onSpeakImgDataList(chatMessageDataList);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    //添加信息至列表
                    addChatMessageToList(chatMessageDataList, false);
                }
            });
        }

        @Override
        public void onHistoryDataList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final int requestSuccessTime, final boolean isNoMoreHistory, int viewIndex) {
            super.onHistoryDataList(chatMessageDataList, requestSuccessTime, isNoMoreHistory, viewIndex);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (swipeLoadView != null) {
                        swipeLoadView.setRefreshing(false);
                        swipeLoadView.setEnabled(true);
                    }
                    if (!chatMessageDataList.isEmpty()) {
                        addChatHistoryToList(chatMessageDataList, requestSuccessTime == 1);
                    }
                    if (isNoMoreHistory) {
                        ToastUtils.showShort(R.string.plv_chat_toast_history_all_loaded);
                        if (swipeLoadView != null) {
                            swipeLoadView.setEnabled(false);
                        }
                    }
                }
            });
        }

        @Override
        public void onHistoryRequestFailed(final String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (swipeLoadView != null) {
                        swipeLoadView.setRefreshing(false);
                        swipeLoadView.setEnabled(true);
                    }
                    ToastUtils.showShort(getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
                }
            });
        }

        @Override
        public void onLoadEmotionMessage(@Nullable final PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (emotionEvent != null) {
                        //添加信息至列表
                        List<PLVBaseViewData> dataList = new ArrayList<>();
                        dataList.add(new PLVBaseViewData<>(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
                        addChatMessageToList(dataList, emotionEvent.isLocal());
                    }
                }
            });
        }

        @Override
        public void onCheckMessageMaxLength(int maxLength) {
            if (chatMessageAdapter != null) {
                chatMessageAdapter.removeDataChanged(maxLength);
            }
        }
    };
    // </editor-fold>
}
