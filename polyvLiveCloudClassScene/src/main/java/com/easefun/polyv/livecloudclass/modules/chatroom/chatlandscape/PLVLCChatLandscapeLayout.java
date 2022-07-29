package com.easefun.polyv.livecloudclass.modules.chatroom.chatlandscape;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

/**
 * 横屏的聊天布局
 */
public class PLVLCChatLandscapeLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //布局占比
    public static final float RATIO_W = 0.27f;
    public static final float RATIO_H = 0.49f;
    //聊天信息列表
    private PLVLCChatCommonMessageList chatCommonMessageList;
    //未读信息提醒view
    private TextView unreadMsgTv;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;

    //聊天回放管理器
    private IPLVChatPlaybackManager chatPlaybackManager;

    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;

    //是否打开
    private boolean toShow = true;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper());

    //是否聊天回放布局
    private boolean isChatPlaybackLayout;

    private OnRoomStatusListener onRoomStatusListener;
    //聊天室开关状态
    private boolean isCloseRoomStatus;
    //专注模式状态
    private boolean isFocusModeStatus;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCChatLandscapeLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCChatLandscapeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCChatLandscapeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(PLVLCChatCommonMessageList chatCommonMessageList) {
        this.chatCommonMessageList = chatCommonMessageList;
        if (chatCommonMessageList == null) {
            return;
        }
        chatCommonMessageList.addUnreadView(unreadMsgTv);
        chatCommonMessageList.addOnUnreadCountChangeListener(new PLVMessageRecyclerView.OnUnreadCountChangeListener() {
            @Override
            public void onChange(int currentUnreadCount) {
                unreadMsgTv.setText(currentUnreadCount + "条新消息");
            }
        });
        chatCommonMessageList.attachToParent(swipeLoadView, true);//聊天信息列表附加到下拉控件中
    }

    public void setIsChatPlaybackLayout(boolean isChatPlaybackLayout) {
        this.isChatPlaybackLayout = isChatPlaybackLayout;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        if (ScreenUtils.isPortrait()) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_chat_landscape_layout, this, true);

        //未读信息view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);

        //下拉控件
        swipeLoadView = findViewById(R.id.swipe_load_view);
        swipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isChatPlaybackLayout) {
                    if (chatPlaybackManager != null) {
                        chatPlaybackManager.loadPrevious();
                    }
                } else if (chatroomPresenter != null) {
                    chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
                }
            }
        });

        initLayoutWH();
    }

    private void initLayoutWH() {
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams vlp = getLayoutParams();
                int landscapeWith = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                int landscapeHeight = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
                vlp.width = (int) (landscapeWith * RATIO_W);
                vlp.height = (int) (landscapeHeight * RATIO_H);
                setLayoutParams(vlp);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天回放">
    private IPLVChatPlaybackCallDataListener chatPlaybackDataListener = new PLVChatPlaybackCallDataExListener() {

        @Override
        public void onLoadPreviousEnabled(boolean enabled, boolean isByClearData) {
            if (swipeLoadView != null) {
                swipeLoadView.setEnabled(enabled);
            }
            if (!enabled && !isByClearData) {
                ToastUtils.showShort(R.string.plv_chat_toast_history_all_loaded);
            }
        }

        @Override
        public void onHasNotAddedData() {
            if (unreadMsgTv != null) {
                if (unreadMsgTv.getVisibility() != View.VISIBLE) {
                    unreadMsgTv.setText("有新消息，点击查看");
                    unreadMsgTv.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onLoadPreviousFinish() {
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
            }
        }

        @Override
        public void onDataInserted(int startPosition, int count, List<PLVChatPlaybackData> insertDataList, boolean inHead, int time) {
            List<PLVBaseViewData> dataList = new ArrayList<>();
            for (PLVChatPlaybackData chatPlaybackData : insertDataList) {
                boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(chatPlaybackData.getUserType())
                        || PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatPlaybackData.getUserId());
                int itemType = chatPlaybackData.isImgMsg() ? PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG : PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK;
                // 可通过userType判断是否是特别身份
                dataList.add(new PLVBaseViewData<>(chatPlaybackData, itemType, isSpecialTypeOrMe ? new PLVSpecialTypeTag(chatPlaybackData.getUserId()) : null));
            }
            if (inHead) {
                addChatMessageToListHead(dataList);
            } else {
                boolean isScrollEnd = chatCommonMessageList != null && chatCommonMessageList.getItemCount() == 0;
                addChatMessageToList(dataList, isScrollEnd);
            }
        }

        @Override
        public void onDataRemoved(int startPosition, int count, List<PLVChatPlaybackData> removeDataList, boolean inHead) {
            removeChatMessageToList(startPosition, count);
        }

        @Override
        public void onDataCleared() {
            removeChatMessageToList(null, true);
        }

        @Override
        public void onData(List<PLVChatPlaybackData> dataList) {
        }

        @Override
        public void onManager(IPLVChatPlaybackManager manager) {
            chatPlaybackManager = manager;
        }
    };

    public IPLVChatPlaybackCallDataListener getChatPlaybackDataListener() {
        return chatPlaybackDataListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="专注模式">
    private void acceptFocusModeEvent(final PLVFocusModeEvent focusModeEvent) {
        isFocusModeStatus = focusModeEvent.isOpen();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (onRoomStatusListener != null) {
                    onRoomStatusListener.onStatusChanged(isCloseRoomStatus, isFocusModeStatus);
                }
                if (chatCommonMessageList != null && !chatCommonMessageList.isLandscapeLayout()) {
                    return;
                }
                ToastUtils.showLong(isFocusModeStatus ? R.string.plv_chat_toast_focus_mode_open : R.string.plv_chat_toast_focus_mode_close);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="房间开关">
    private void acceptCloseRoomEvent(PLVCloseRoomEvent closeRoomEvent) {
        isCloseRoomStatus = closeRoomEvent.getValue().isClosed();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (onRoomStatusListener != null) {
                    onRoomStatusListener.onStatusChanged(isCloseRoomStatus, isFocusModeStatus);
                }
                if (chatCommonMessageList == null || !chatCommonMessageList.isLandscapeLayout()) {
                    return;
                }
                ToastUtils.showLong(isCloseRoomStatus ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
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
            if (isChatPlaybackLayout) {
                return;
            }
            if (chatroomPresenter.getChatHistoryTime() == 0 && chatCommonMessageList != null && chatCommonMessageList.isLandscapeLayout()) {
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
            }
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(14);
        }

        @Override
        public void onCloseRoomEvent(@NonNull final PLVCloseRoomEvent closeRoomEvent) {
            super.onCloseRoomEvent(closeRoomEvent);
            if (isChatPlaybackLayout) {
                return;
            }
            acceptCloseRoomEvent(closeRoomEvent);
        }

        @Override
        public void onFocusModeEvent(@NonNull PLVFocusModeEvent focusModeEvent) {
            super.onFocusModeEvent(focusModeEvent);
            if (isChatPlaybackLayout) {
                return;
            }
            acceptFocusModeEvent(focusModeEvent);
        }

        @Override
        public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            if (isChatPlaybackLayout) {
                return;
            }
            removeChatMessageToList(id, isRemoveAll);
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            if (isChatPlaybackLayout) {
                return;
            }
            if (localMessage == null) {
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
            //添加信息至列表
            addChatMessageToList(dataList, true);
        }

        @Override
        public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {
            super.onLocalImageMessage(localImgEvent);
            if (isChatPlaybackLayout) {
                return;
            }
            List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_SEND_IMG, new PLVSpecialTypeTag(localImgEvent.getUserId())));
            //添加信息至列表
            addChatMessageToList(dataList, true);
        }

        @Override
        public void onSpeakImgDataList(List<PLVBaseViewData> chatMessageDataList) {
            super.onSpeakImgDataList(chatMessageDataList);
            if (isChatPlaybackLayout) {
                return;
            }
            //添加信息至列表
            addChatMessageToList(chatMessageDataList, false);
        }

        @Override
        public void onHistoryDataList(List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, int requestSuccessTime, boolean isNoMoreHistory, int viewIndex) {
            super.onHistoryDataList(chatMessageDataList, requestSuccessTime, isNoMoreHistory, viewIndex);
            if (isChatPlaybackLayout) {
                return;
            }
            swipeLoadView.setRefreshing(false);
            swipeLoadView.setEnabled(true);
            if (!chatMessageDataList.isEmpty()) {
                addChatHistoryToList(chatMessageDataList, requestSuccessTime == 1);
            }
            if (isNoMoreHistory) {
                ToastUtils.showShort(R.string.plv_chat_toast_history_all_loaded);
                swipeLoadView.setEnabled(false);
            }
        }

        @Override
        public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            if (isChatPlaybackLayout) {
                return;
            }
            swipeLoadView.setRefreshing(false);
            swipeLoadView.setEnabled(true);
            if (viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                ToastUtils.showShort(getContext().getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
            }
        }

        @Override
        public void onLoadEmotionMessage(@Nullable @org.jetbrains.annotations.Nullable PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            if (isChatPlaybackLayout) {
                return;
            }
            if (emotionEvent == null) {
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
            addChatMessageToList(dataList, emotionEvent.isLocal());
        }
    };

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 列表数据更新">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (chatCommonMessageList != null) {
                    chatCommonMessageList.addChatMessageToList(chatMessageDataList, isScrollEnd, true);
                }
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addChatHistoryToList(chatMessageDataList, isScrollEnd, true);
        }
    }

    private void addChatMessageToListHead(final List<PLVBaseViewData> chatMessageDataList) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addChatMessageToListHead(chatMessageDataList, false, true);
        }
    }

    private void removeChatMessageToList(int startPosition, int count) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.removeChatMessage(startPosition, count, true);
            if (!chatCommonMessageList.canScrollVertically(1)) {
                chatCommonMessageList.scrollToPosition(chatCommonMessageList.getItemCount() - 1);
            }
        }
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (chatCommonMessageList == null) {
                    return;
                }
                if (isRemoveAll) {
                    chatCommonMessageList.removeAllChatMessage(true);
                } else {
                    chatCommonMessageList.removeChatMessage(id, true);
                }
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="横屏聊天室布局 - 开关控制">
    public void toggle(boolean toShow) {
        this.toShow = toShow;
        setVisibility((toShow && ScreenUtils.isLandscape()) ? View.VISIBLE : View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (toShow) {
                setVisibility(View.VISIBLE);
            }
            if (chatCommonMessageList != null) {
                boolean result = chatCommonMessageList.attachToParent(swipeLoadView, true);
                if (result && chatroomPresenter != null) {
                    chatCommonMessageList.setMsgIndex(chatroomPresenter.getViewIndex(chatroomView));
                    if (!isChatPlaybackLayout) {
                        //处理播放页面初始竖屏，然后在竖屏聊天室没加载完成前切换到横屏的情况，这时需要加载历史记录
                        if (chatroomPresenter.getChatHistoryTime() == 0) {
                            chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
                        }
                    }
                }
            }
        } else {
            setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - 聊天室房间状态监听器">
    public void setOnRoomStatusListener(OnRoomStatusListener listener) {
        this.onRoomStatusListener = listener;
    }

    public interface OnRoomStatusListener {
        void onStatusChanged(boolean isCloseRoomStatus, boolean isFocusModeStatus);
    }
    // </editor-fold>
}
