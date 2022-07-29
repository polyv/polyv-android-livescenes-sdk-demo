package com.easefun.polyv.streameralone.modules.chatroom;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.socket.IPLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.modules.socket.PLVAbsOnSocketEventListener;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVSimpleSwipeRefreshLayout;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAMessageAdapter;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.holder.PLVSAMessageViewHolder;
import com.easefun.polyv.streameralone.modules.chatroom.widget.PLVSAChatMsgInputWindow;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室布局
 */
public class PLVSAChatroomLayout extends FrameLayout implements IPLVSAChatroomLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //socket登录管理器
    private IPLVSocketLoginManager socketLoginManager;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    //聊天信息列表适配器
    private PLVSAMessageAdapter chatMessageAdapter;
    //聊天图片查看fragment
    private PLVChatImageViewerFragment chatImageViewerFragment;

    //view
    private PLVSimpleSwipeRefreshLayout plvlsChatroomSwipeLoadView;
    private PLVMessageRecyclerView plvlsChatroomChatMsgRv;
    private TextView plvlsChatroomUnreadMsgTv;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAChatroomLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVSAChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVSAChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initLayoutSize();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvsa_chatroom_layout, this);

        //findView
        plvlsChatroomSwipeLoadView = findViewById(R.id.plvls_chatroom_swipe_load_view);
        plvlsChatroomChatMsgRv = findViewById(R.id.plvls_chatroom_chat_msg_rv);
        plvlsChatroomUnreadMsgTv = findViewById(R.id.plvls_chatroom_unread_msg_tv);

        //初始化聊天室列表
        PLVMessageRecyclerView.setLayoutManager(plvlsChatroomChatMsgRv).setStackFromEnd(true);
        plvlsChatroomChatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVSAMessageAdapter();
        chatMessageAdapter.setOnViewActionListener(new PLVSAMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg) {
                if (isQuoteImg) {
                    chatImageViewerFragment = PLVChatImageViewerFragment.show((AppCompatActivity) getContext(), chatMessageAdapter.getDataList().get(position), Window.ID_ANDROID_CONTENT);
                } else {
                    chatImageViewerFragment = PLVChatImageViewerFragment.show((AppCompatActivity) getContext(), chatMessageAdapter.getDataList(), chatMessageAdapter.getDataList().get(position), Window.ID_ANDROID_CONTENT);
                }
            }

            @Override
            public void onShowAnswerWindow(final PLVChatQuoteVO chatQuoteVO, final String quoteId) {
                Intent intent = new Intent(getContext(), PLVSAChatMsgInputWindow.class);
                intent.putExtra(PLVSAChatMsgInputWindow.ANSWER_USER_NAME, chatQuoteVO.getNick());
                intent.putExtra(PLVSAChatMsgInputWindow.ANSWER_USER_CONTENT, chatQuoteVO.getContent());
                if (chatQuoteVO.getImage() != null) {
                    intent.putExtra(PLVSAChatMsgInputWindow.ANSWER_USER_IMG_URL, chatQuoteVO.getImage().getUrl());
                    intent.putExtra(PLVSAChatMsgInputWindow.ANSWER_USER_IMG_WIDTH, chatQuoteVO.getImage().getWidth());
                    intent.putExtra(PLVSAChatMsgInputWindow.ANSWER_USER_IMG_HEIGHT, chatQuoteVO.getImage().getHeight());
                }
                PLVSAChatMsgInputWindow.show(((Activity) getContext()), intent, new PLVSAChatMsgInputWindow.MessageSendListener() {
                    @Override
                    public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                        sendChatMessage(imgEvent);
                    }

                    @Override
                    public boolean onSendEmotion(String emotionId) {
                        Pair<Boolean, Integer> pair = sendChatEmotion(new PLVChatEmotionEvent(emotionId));
                        return pair.first;
                    }

                    @Override
                    public boolean onSendQuoteMsg(String message) {
                        return sendQuoteMessage(message, chatQuoteVO, quoteId);
                    }

                    @Override
                    public boolean onSendMsg(String message) {
                        return sendChatMessage(message);
                    }
                });
            }
        });
        plvlsChatroomChatMsgRv.setAdapter(chatMessageAdapter);
        plvlsChatroomChatMsgRv.addUnreadView(plvlsChatroomUnreadMsgTv);
        plvlsChatroomChatMsgRv.addOnUnreadCountChangeListener(new PLVMessageRecyclerView.OnUnreadCountChangeListener() {
            @Override
            public void onChange(int currentUnreadCount) {
                plvlsChatroomUnreadMsgTv.setText(currentUnreadCount + "条新信息");
            }
        });

        //初始化下拉加载历史记录控件
        plvlsChatroomSwipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        plvlsChatroomSwipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
            }
        });
    }

    private void initLayoutSize() {
        post(new Runnable() {
            @Override
            public void run() {
                int portraitHeight = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                int portraitWidth = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                //聊天室布局在竖屏下的高度
                int chatroomLayoutHeight = (int) (portraitHeight * 0.25f);
                //聊天室布局在竖屏下的宽度
                int chatroomLayoutWidth = (int) (portraitWidth * 0.65f);
                //调整聊天室布局的大小
                ViewGroup.LayoutParams vlp = getLayoutParams();
                vlp.width = chatroomLayoutWidth;
                vlp.height = chatroomLayoutHeight;
                setLayoutParams(vlp);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVSAChatroomLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        this.chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        this.chatroomPresenter.registerView(chatroomView);
        this.chatroomPresenter.init();

        initSocketLoginManager();
    }

    @Override
    public void loginAndLoadHistory() {
        socketLoginManager.login();
        //请求一次历史记录
        chatroomPresenter.setHistoryContainRewardEvent(true);
        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
    }

    @Override
    public void callInputWindow() {
        PLVSAChatMsgInputWindow.show(((Activity) getContext()), PLVSAChatMsgInputWindow.class, new PLVSAChatMsgInputWindow.MessageSendListener() {
            @Override
            public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                sendChatMessage(imgEvent);
            }

            @Override
            public boolean onSendEmotion(String emotionId) {
                Pair<Boolean, Integer> pair = sendChatEmotion(new PLVChatEmotionEvent(emotionId));
                return pair.first;
            }

            @Override
            public boolean onSendQuoteMsg(String message) {
                return false;
            }

            @Override
            public boolean onSendMsg(String message) {
                return sendChatMessage(message);
            }
        });
    }

    @Override
    public void addChatMessageToChatList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        addChatMessageToList(chatMessageDataList, isScrollEnd);
    }

    @Override
    public void addOnOnlineCountListener(IPLVOnDataChangedListener<Integer> listener) {
        chatroomPresenter.getData().getOnlineCountData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnLoginEventListener(IPLVOnDataChangedListener<PLVLoginEvent> listener) {
        chatroomPresenter.getData().getLoginEventData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnRewardEventListener(IPLVOnDataChangedListener<PLVRewardEvent> listener) {
        chatroomPresenter.getData().getRewardEvent().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addObserverToChatMessageAdapter(RecyclerView.AdapterDataObserver adapterDataObserver) {
        chatMessageAdapter.registerAdapterDataObserver(adapterDataObserver);
    }

    @Override
    public void removeObserverFromChatMessageAdapter(RecyclerView.AdapterDataObserver adapterDataObserver) {
        chatMessageAdapter.unregisterAdapterDataObserver(adapterDataObserver);
    }

    @Override
    public int getChatMessageListSize() {
        return chatMessageAdapter.getDataList().size();
    }

    @Override
    public boolean onBackPressed() {
        if (chatImageViewerFragment != null && chatImageViewerFragment.isVisible()) {
            chatImageViewerFragment.hide();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        destroySocketLoginManager();
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
        }
        //移除聊天室加载图片进度的监听器，避免内存泄漏
        PLVMyProgressManager.removeModuleListener(PLVSAMessageViewHolder.LOADIMG_MOUDLE_TAG);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 发送聊天信息">
    private boolean sendChatMessage(String message) {
        if (message.trim().length() == 0) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_chat_toast_send_text_empty)
                    .build()
                    .show();
            return false;
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
            Pair<Boolean, Integer> sendResult = chatroomPresenter.sendChatMessage(localMessage);
            if (sendResult.first) {
                return true;
            } else {
                String errorTips = getContext().getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second;
                if (PolyvLocalMessage.SENDVALUE_NOONLINE == sendResult.second) {
                    errorTips = "当前网络不可用，请检查网络设置";
                }
                //发送失败
                PLVToast.Builder.context(getContext())
                        .setText(errorTips)
                        .build()
                        .show();
                return false;
            }
        }
    }

    private boolean sendQuoteMessage(String message, PLVChatQuoteVO chatQuoteVO, String quoteId) {
        if (chatQuoteVO.getImage() == null
                && message.trim().length() == 0) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plv_chat_toast_send_text_empty)
                    .build()
                    .show();
            return false;
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
            localMessage.setQuote(chatQuoteVO);
            Pair<Boolean, Integer> sendResult = chatroomPresenter.sendQuoteMessage(localMessage, quoteId);
            if (sendResult.first) {
                return true;
            } else {
                String errorTips = getContext().getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second;
                if (PolyvLocalMessage.SENDVALUE_NOONLINE == sendResult.second) {
                    errorTips = "当前网络不可用，请检查网络设置";
                }
                //发送失败
                PLVToast.Builder.context(getContext())
                        .setText(errorTips)
                        .build()
                        .show();
                return false;
            }
        }
    }

    private void sendChatMessage(PolyvSendLocalImgEvent imgEvent) {
        chatroomPresenter.sendChatImage(imgEvent);
    }

    private Pair<Boolean, Integer> sendChatEmotion(PLVChatEmotionEvent emotionEvent){
        return chatroomPresenter.sendChatEmotionImage(emotionEvent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 列表数据更新">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean result = chatMessageAdapter.addDataListChangedAtLast(chatMessageDataList);
                if (result) {
                    if (isScrollEnd) {
                        plvlsChatroomChatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        plvlsChatroomChatMsgRv.scrollToBottomOrShowMore(chatMessageDataList.size());
                    }
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        boolean result = chatMessageAdapter.addDataListChangedAtFirst(chatMessageDataList);
        if (result) {
            if (isScrollEnd) {
                plvlsChatroomChatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            } else {
                plvlsChatroomChatMsgRv.scrollToPosition(0);
            }
        }
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
    private PLVAbsChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);
        }

        @Override
        public void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent) {
            super.onImgEvent(chatImgEvent);
        }

        @Override
        public void onLikesEvent(@NonNull PLVLikesEvent likesEvent) {
            super.onLikesEvent(likesEvent);
        }

        @Override
        public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {
            super.onLoginEvent(loginEvent);
        }

        @Override
        public void onLogoutEvent(@NonNull PLVLogoutEvent logoutEvent) {
            super.onLogoutEvent(logoutEvent);
        }

        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
        }

        @Override
        public void onRemoveBulletinEvent() {
            super.onRemoveBulletinEvent();
        }

        @Override
        public void onCloseRoomEvent(@NonNull final PLVCloseRoomEvent closeRoomEvent) {
            super.onCloseRoomEvent(closeRoomEvent);
        }

        @Override
        public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            removeChatMessageToList(id, isRemoveAll);
        }

        @Override
        public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {
            super.onCustomGiftEvent(userBean, customGiftBean);
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            if (localMessage == null) {
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
            //添加信息至列表
            addChatMessageToList(dataList, true);
        }

        @Override
        public void onLoadEmotionMessage(@Nullable PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            if(emotionEvent == null){
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
            //添加信息至列表
            addChatMessageToList(dataList, emotionEvent.isLocal());
        }

        @Override
        public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {
            super.onLocalImageMessage(localImgEvent);
            if (localImgEvent == null) {
                return;
            }
            List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_SEND_IMG, new PLVSpecialTypeTag(localImgEvent.getUserId())));
            //添加信息至列表
            addChatMessageToList(dataList, true);
        }

        @Override
        public void onSendProhibitedWord(@NonNull final String prohibitedMessage, @NonNull final String hintMsg, @NonNull final String status) {
            super.onSendProhibitedWord(prohibitedMessage, hintMsg, status);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    chatMessageAdapter.notifyProhibitedChanged(prohibitedMessage, hintMsg, status);
                }
            });
        }

        @Override
        public void onSpeakImgDataList(List<PLVBaseViewData> chatMessageDataList) {
            super.onSpeakImgDataList(chatMessageDataList);
            //添加信息至列表
            addChatMessageToList(chatMessageDataList, false);
        }

        @Override
        public void onHistoryDataList(List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory, int viewIndex) {
            super.onHistoryDataList(chatMessageDataList, requestSuccessTime, isNoMoreHistory, viewIndex);
            plvlsChatroomSwipeLoadView.setRefreshing(false);
            plvlsChatroomSwipeLoadView.setEnabled(true);
            if (chatMessageDataList.size() > 0) {
                addChatHistoryToList(chatMessageDataList, requestSuccessTime == 1);
            }
            if (isNoMoreHistory) {
                plvlsChatroomSwipeLoadView.setEnabled(false);
            }
        }

        @Override
        public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            plvlsChatroomSwipeLoadView.setRefreshing(false);
            plvlsChatroomSwipeLoadView.setEnabled(true);
            if (viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg)
                        .build()
                        .show();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="socket - 初始化、登录、销毁">
    private void initSocketLoginManager() {
        socketLoginManager = new PLVSocketLoginManager(liveRoomDataManager);
        socketLoginManager.init();
        socketLoginManager.setOnSocketEventListener(onSocketEventListener);
    }

    private void destroySocketLoginManager() {
        if (socketLoginManager != null) {
            socketLoginManager.destroy();
        }
    }

    private IPLVSocketLoginManager.OnSocketEventListener onSocketEventListener = new PLVAbsOnSocketEventListener() {
        @Override
        public void handleLoginIng(boolean isReconnect) {
            super.handleLoginIng(isReconnect);
            if (isReconnect) {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_reconnecting)
                        .build()
                        .show();
            }
        }

        @Override
        public void handleLoginSuccess(boolean isReconnect) {
            super.handleLoginSuccess(isReconnect);
            if (isReconnect) {
                PLVToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_reconnect_success)
                        .build()
                        .show();
            }
            initChatroomEmotion();
        }

        @Override
        public void handleLoginFailed(@NonNull Throwable throwable) {
            super.handleLoginFailed(throwable);
            PLVToast.Builder.context(getContext())
                    .setText(getResources().getString(R.string.plv_chat_toast_login_failed) + ":" + throwable.getMessage())
                    .build()
                    .show();
        }

        @Override
        public void onKickEvent(@NonNull PLVKickEvent kickEvent, boolean isOwn) {
            super.onKickEvent(kickEvent, isOwn);
            if (isOwn) {
                PLVToast.Builder.context(PLVAppUtils.getApp())
                        .shortDuration()
                        .setText(R.string.plv_chat_toast_kicked_streamer)
                        .build()
                        .show();
                ((Activity)getContext()).finish();
            }
        }

        @Override
        public void onLoginRefuseEvent(@NonNull PLVLoginRefuseEvent loginRefuseEvent) {
            super.onLoginRefuseEvent(loginRefuseEvent);
            showExitDialog(R.string.plv_chat_toast_been_kicked);
        }

        @Override
        public void onReloginEvent(@NonNull PLVReloginEvent reloginEvent) {
            super.onReloginEvent(reloginEvent);
            showExitDialog(R.string.plv_chat_toast_account_login_elsewhere);
        }
    };

    private void showExitDialog(int messageId) {
        new AlertDialog.Builder(getContext())
                .setTitle("温馨提示")
                .setMessage(messageId)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) getContext()).finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 表情">
    private void initChatroomEmotion(){
        if(chatroomPresenter == null){
            return;
        }
        //加载个性表情
        chatroomPresenter.getChatEmotionImages();
    }
    // </editor-fold >

}
