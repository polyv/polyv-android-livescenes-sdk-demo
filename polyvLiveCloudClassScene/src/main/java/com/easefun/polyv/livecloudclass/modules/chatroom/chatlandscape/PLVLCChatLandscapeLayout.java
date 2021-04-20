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
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

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

    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;

    //是否打开
    private boolean toShow = true;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
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
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
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

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void setPresenter(@NonNull IPLVChatroomContract.IChatroomPresenter presenter) {
            super.setPresenter(presenter);
            chatroomPresenter = presenter;
            if (chatroomPresenter.getChatHistoryTime() == 0 && chatCommonMessageList != null && chatCommonMessageList.isLandscapeLayout()) {
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
            }
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(14);
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
            if (chatCommonMessageList == null || !chatCommonMessageList.isLandscapeLayout()) {
                return;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showLong(closeRoomEvent.getValue().isClosed() ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
                }
            });
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
            dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag()));
            //添加信息至列表
            addChatMessageToList(dataList, true);
        }

        @Override
        public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {
            super.onLocalImageMessage(localImgEvent);
            List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localImgEvent, PLVChatMessageItemType.ITEMTYPE_SEND_IMG, new PLVSpecialTypeTag()));
            //添加信息至列表
            addChatMessageToList(dataList, true);
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
            swipeLoadView.setRefreshing(false);
            swipeLoadView.setEnabled(true);
            if (viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                ToastUtils.showShort(getContext().getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
            }
        }
    };

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 列表数据更新">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatCommonMessageList != null) {
                    chatCommonMessageList.addChatMessageToList(chatMessageDataList, isScrollEnd, true);
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addChatHistoryToList(chatMessageDataList, isScrollEnd, true);
        }
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        handler.post(new Runnable() {
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
        });
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
                    //处理播放页面初始竖屏，然后在竖屏聊天室没加载完成前切换到横屏的情况，这时需要加载历史记录
                    if (chatroomPresenter.getChatHistoryTime() == 0) {
                        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
                    }
                }
            }
        } else {
            setVisibility(View.GONE);
        }
    }
    // </editor-fold>
}
