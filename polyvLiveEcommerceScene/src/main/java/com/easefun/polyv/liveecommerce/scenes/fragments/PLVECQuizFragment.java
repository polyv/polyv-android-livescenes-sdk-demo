package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVBaseFragment;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.layout.PLVECChatOverLengthMessageLayout;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.List;

/**
 * 提问fragment
 */
public class PLVECQuizFragment extends PLVBaseFragment {
    // <editor-folder defaultstate="collapsed" desc="变量">
    private static final String DEFAULT_TIPS = "你已进入专属的提问频道，提问内容不会公开";
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
    //提示语
    private String tips = DEFAULT_TIPS;
    private boolean firstLoadHistory = true;
    private boolean requestHistoryAtRegister = false;
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_quiz_fragment, null);
        initView();
        return view;
    }
    // </editor-folder>

    // <editor-folder defaultstate="collapsed" desc="初始化View">
    private void initView() {
        chatMsgRv = findViewById(R.id.quiz_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setAllowReplyMessage(false);
        chatMessageAdapter.setOnViewActionListener(new PLVECChatMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(View view, String imgUrl) {
                if (messageAdapterListener != null) {
                    messageAdapterListener.onChatImgClick(view, imgUrl);
                }
            }

            @Override
            public void callOnReplyMessage(PLVChatQuoteVO chatQuoteVO) {
            }

            @Override
            public void onShowOverLengthMessage(PLVECChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean) {
            }

            @Override
            public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
            }
        });
        //下拉控件
        swipeLoadView = findViewById(R.id.swipe_load_view);
        swipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (chatroomPresenter != null) {
                    chatroomPresenter.requestQuizHistory();
                }
            }
        });
        //未读信息view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);
        chatMsgRv.addUnreadView(unreadMsgTv);
        // 拉取一次历史消息
        if (chatroomPresenter != null) {
            chatroomPresenter.requestQuizHistory();
        } else {
            requestHistoryAtRegister = true;
        }
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

    public void setTips(String tips) {
        if (TextUtils.isEmpty(tips)) {
            return;
        }
        this.tips = tips;
    }

    public void setOnMessageAdapterListener(PLVECChatMessageAdapter.OnViewActionListener listener) {
        messageAdapterListener = listener;
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {
            super.setPresenter(presenter);
            chatroomPresenter = presenter;
            if (requestHistoryAtRegister) {
                chatroomPresenter.requestQuizHistory();
            }
        }

        @Override
        public int getQuizEmojiSize() {
            return ConvertUtils.dp2px(16);//提问列表里的文本信息textSize
        }

        @Override
        public void onQuizHistoryDataList(@NonNull final List<PLVBaseViewData<PLVBaseEvent>> answerEvents, final boolean isNoMoreQuizHistory) {
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (chatMessageAdapter != null) {
                        boolean isScrollEnd = chatMessageAdapter.getItemCount() == 0;
                        chatMessageAdapter.addDataListChangedAtFirst(answerEvents);
                        chatMsgRv.scrollToPosition(isScrollEnd ? chatMessageAdapter.getItemCount() - 1 : 0);
                    }
                    if (firstLoadHistory && answerEvents.isEmpty()) {
                        addQuizTipsToList();
                    }
                    firstLoadHistory = false;
                    swipeLoadView.setRefreshing(false);
                    swipeLoadView.setEnabled(!isNoMoreQuizHistory);
                }
            });
        }

        @Override
        public void onQuizHistoryRequestFailed(Throwable throwable) {
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (firstLoadHistory) {
                        addQuizTipsToList();
                    }
                    firstLoadHistory = false;
                    swipeLoadView.setRefreshing(false);
                }
            });
        }

        @Override
        public void onLocalQuestionMessage(@Nullable final PolyvQuestionMessage questionMessage) {
            super.onLocalQuestionMessage(questionMessage);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    if (questionMessage == null) {
                        return;
                    }
                    final PLVBaseViewData viewData = new PLVBaseViewData<>(questionMessage, PLVChatMessageItemType.ITEMTYPE_SEND_QUIZ, new PLVSpecialTypeTag(questionMessage.getUserId()));
                    //添加信息至列表
                    addQuizMessageToList(viewData, true);
                }
            });
        }

        @Override
        public void onAnswerEvent(@NonNull final PLVTAnswerEvent answerEvent) {
            super.onAnswerEvent(answerEvent);
            runAfterOnActivityCreated(new Runnable() {
                @Override
                public void run() {
                    int itemType = answerEvent.isImgEvent() ? PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG : PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ;
                    //添加信息至列表
                    addQuizMessageToList(new PLVBaseViewData<>(answerEvent, itemType, new PLVSpecialTypeTag(answerEvent.getUserId())), false);
                }
            });
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 添加信息至列表">
    private void addQuizMessageToList(final PLVBaseViewData baseViewData, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatMessageAdapter == null) {
                    return;
                }
                boolean result = chatMessageAdapter.addDataChangedAtLast(baseViewData);
                if (result) {
                    if (isScrollEnd) {
                        chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        chatMsgRv.scrollToBottomOrShowMore(1);
                    }
                }
            }
        });
    }

    private void addQuizTipsToList() {
        PLVTAnswerEvent tAnswerEvent = new PLVTAnswerEvent();
        tAnswerEvent.setContent(tips);
        tAnswerEvent.setObjects(PLVTextFaceLoader.messageToSpan(tAnswerEvent.getContent(), ConvertUtils.dp2px(14), getContext()));
        PLVSocketUserBean userBean = new PLVSocketUserBean();
        userBean.setUserType(PLVSocketUserConstant.USERTYPE_TEACHER);
        userBean.setNick("讲师");
        userBean.setActor("讲师");
        userBean.setPic(PLVSocketUserConstant.TEACHER_AVATAR_URL);
        tAnswerEvent.setUser(userBean);

        addQuizMessageToList(new PLVBaseViewData<>(tAnswerEvent, PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ, new PLVSpecialTypeTag(null)), true);
    }
    // </editor-fold>
}
