package com.easefun.polyv.livestreamer.modules.chatroom;

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
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSMessageAdapter;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.holder.PLVLSMessageViewHolder;
import com.easefun.polyv.livestreamer.modules.chatroom.widget.PLVLSChatMsgInputWindow;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室布局
 */
public class PLVLSChatroomLayout extends FrameLayout implements IPLVLSChatroomLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //socket登录管理器
    private IPLVSocketLoginManager socketLoginManager;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    //聊天信息列表适配器
    private PLVLSMessageAdapter chatMessageAdapter;
    //聊天图片查看fragment
    private PLVChatImageViewerFragment chatImageViewerFragment;

    //view
    private FrameLayout plvlsChatroomChatMsgLy;
    private PLVSimpleSwipeRefreshLayout plvlsChatroomSwipeLoadView;
    private PLVMessageRecyclerView plvlsChatroomChatMsgRv;
    private TextView plvlsChatroomUnreadMsgTv;
    private ImageView plvlsChatroomControlIv;
    private ImageView plvlsChatroomToolbarMicControlIv;
    private ImageView plvlsChatroomToolbarCameraControlIv;
    private ImageView plvlsChatroomToolbarFrontCameraControlIv;
    private TextView plvlsChatroomToolbarOpenInputWindowTv;

    //listener
    private OnViewActionListener onViewActionListener;

    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSChatroomLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLSChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_chatroom_layout, this);

        //findView
        plvlsChatroomChatMsgLy = findViewById(R.id.plvls_chatroom_chat_msg_ly);
        plvlsChatroomSwipeLoadView = findViewById(R.id.plvls_chatroom_swipe_load_view);
        plvlsChatroomChatMsgRv = findViewById(R.id.plvls_chatroom_chat_msg_rv);
        plvlsChatroomUnreadMsgTv = findViewById(R.id.plvls_chatroom_unread_msg_tv);
        plvlsChatroomControlIv = findViewById(R.id.plvls_chatroom_control_iv);
        plvlsChatroomToolbarMicControlIv = findViewById(R.id.plvls_chatroom_toolbar_mic_control_iv);
        plvlsChatroomToolbarCameraControlIv = findViewById(R.id.plvls_chatroom_toolbar_camera_control_iv);
        plvlsChatroomToolbarFrontCameraControlIv = findViewById(R.id.plvls_chatroom_toolbar_front_camera_control_iv);
        plvlsChatroomToolbarOpenInputWindowTv = findViewById(R.id.plvls_chatroom_toolbar_open_input_window_tv);

        //初始化按钮点击事件
        plvlsChatroomControlIv.setOnClickListener(this);
        plvlsChatroomToolbarMicControlIv.setOnClickListener(this);
        plvlsChatroomToolbarCameraControlIv.setOnClickListener(this);
        plvlsChatroomToolbarFrontCameraControlIv.setOnClickListener(this);
        plvlsChatroomToolbarOpenInputWindowTv.setOnClickListener(this);

        //初始化聊天室列表
        PLVMessageRecyclerView.setLayoutManager(plvlsChatroomChatMsgRv).setStackFromEnd(true);
        plvlsChatroomChatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVLSMessageAdapter();
        chatMessageAdapter.setOnViewActionListener(new PLVLSMessageAdapter.OnViewActionListener() {
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
                Intent intent = new Intent(getContext(), PLVLSChatMsgInputWindow.class);
                intent.putExtra(PLVLSChatMsgInputWindow.ANSWER_USER_NAME, chatQuoteVO.getNick());
                intent.putExtra(PLVLSChatMsgInputWindow.ANSWER_USER_CONTENT, chatQuoteVO.getContent());
                if (chatQuoteVO.getImage() != null) {
                    intent.putExtra(PLVLSChatMsgInputWindow.ANSWER_USER_IMG_URL, chatQuoteVO.getImage().getUrl());
                    intent.putExtra(PLVLSChatMsgInputWindow.ANSWER_USER_IMG_WIDTH, chatQuoteVO.getImage().getWidth());
                    intent.putExtra(PLVLSChatMsgInputWindow.ANSWER_USER_IMG_HEIGHT, chatQuoteVO.getImage().getHeight());
                }
                PLVLSChatMsgInputWindow.show(((Activity) getContext()), intent, new PLVLSChatMsgInputWindow.MessageSendListener() {
                    @Override
                    public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                        sendChatMessage(imgEvent);
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLSChatroomLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        this.chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        this.chatroomPresenter.registerView(chatroomView);
        this.chatroomPresenter.init();
        //请求一次历史记录
        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));

        initSocketLoginManager();
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void addOnOnlineCountListener(IPLVOnDataChangedListener<Integer> listener) {
        chatroomPresenter.getData().getOnlineCountData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void setOpenMicViewStatus(boolean isOpen) {
        plvlsChatroomToolbarMicControlIv.setSelected(!isOpen);
    }

    @Override
    public void setOpenCameraViewStatus(boolean isOpen) {
        plvlsChatroomToolbarCameraControlIv.setSelected(!isOpen);
        plvlsChatroomToolbarFrontCameraControlIv.setSelected(plvlsChatroomToolbarCameraControlIv.isSelected());
    }

    @Override
    public void setFrontCameraViewStatus(boolean isFront) {
        plvlsChatroomToolbarFrontCameraControlIv.setTag(isFront ? null : "back");
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
        PLVMyProgressManager.removeModuleListener(PLVLSMessageViewHolder.LOADIMG_MOUDLE_TAG);
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
                //发送失败
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second)
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
                //发送失败
                PLVToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second)
                        .build()
                        .show();
                return false;
            }
        }
    }

    private void sendChatMessage(PolyvSendLocalImgEvent imgEvent) {
        chatroomPresenter.sendChatImage(imgEvent);
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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PLVToast.Builder.context(getContext())
                            .setText(closeRoomEvent.getValue().isClosed() ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open)
                            .build()
                            .show();
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
        socketLoginManager.login();
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
                showExitDialog(R.string.plv_chat_toast_been_kicked);
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
        if (id == R.id.plvls_chatroom_control_iv) {
            v.setSelected(!v.isSelected());
            plvlsChatroomChatMsgLy.setVisibility(v.isSelected() ? View.INVISIBLE : View.VISIBLE);
        } else if (id == R.id.plvls_chatroom_toolbar_mic_control_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onMicControl(!v.isSelected());
            }
        } else if (id == R.id.plvls_chatroom_toolbar_camera_control_iv) {
            if (onViewActionListener != null) {
                onViewActionListener.onCameraControl(!v.isSelected());
            }
        } else if (id == R.id.plvls_chatroom_toolbar_front_camera_control_iv) {
            if (!v.isSelected()) {
                if (onViewActionListener != null) {
                    onViewActionListener.onFrontCameraControl(v.getTag() != null);
                }
            }
        } else if (id == R.id.plvls_chatroom_toolbar_open_input_window_tv) {
            PLVLSChatMsgInputWindow.show(((Activity) getContext()), PLVLSChatMsgInputWindow.class, new PLVLSChatMsgInputWindow.MessageSendListener() {
                @Override
                public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                    sendChatMessage(imgEvent);
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
    }
    // </editor-fold>
}
