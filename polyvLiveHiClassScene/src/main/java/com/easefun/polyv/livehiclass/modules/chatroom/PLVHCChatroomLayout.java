package com.easefun.polyv.livehiclass.modules.chatroom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
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
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVOutsideTouchableLayout;
import com.easefun.polyv.livecommon.ui.widget.PLVSimpleSwipeRefreshLayout;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCMessageAdapter;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.holder.PLVHCMessageViewHolder;
import com.easefun.polyv.livehiclass.modules.chatroom.utils.PLVHCChatroomUtils;
import com.easefun.polyv.livehiclass.modules.chatroom.widget.PLVHCChatMsgInputWindow;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.livescenes.chatroom.IPLVChatroomManager;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageHelper;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室布局
 */
public class PLVHCChatroomLayout extends FrameLayout implements IPLVHCChatroomLayout, View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //socket登录管理器
    private IPLVSocketLoginManager socketLoginManager;
    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    //聊天信息列表适配器
    private PLVHCMessageAdapter chatMessageAdapter;
    //聊天图片查看fragment
    private PLVChatImageViewerFragment chatImageViewerFragment;

    //status
    private boolean isChatroomEnable;
    private boolean isCloseRoom;

    private boolean isTeacherType;

    //view
    private PLVSimpleSwipeRefreshLayout plvhcChatroomSwipeLoadView;
    private PLVMessageRecyclerView plvhcChatroomChatMsgRv;
    private TextView plvhcChatroomMoreMsgTv;
    private ImageView plvhcChatroomSelectEmojiIv;
    private ImageView plvhcChatroomSelectImgIv;
    private EditText plvhcChatroomCallInputEt;
    private ViewGroup plvhcChatroomEmojiSmallLy;
    private RecyclerView plvhcChatroomEmojiRv;
    private ImageView plvhcChatroomDeleteMsgIv;
    private TextView plvhcChatroomSendMsgTv;
    private ImageView plvhcChatroomCloseRoomIv;

    //container
    private PLVOutsideTouchableLayout container;
    //listener
    private OnViewActionListener onViewActionListener;
    //handler
    private Handler handler = new Handler(Looper.getMainLooper());
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCChatroomLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVHCChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVHCChatroomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvhc_chatroom_layout, this);

        //findView
        plvhcChatroomSwipeLoadView = findViewById(R.id.plvhc_chatroom_swipe_load_view);
        plvhcChatroomChatMsgRv = findViewById(R.id.plvhc_chatroom_chat_msg_rv);
        plvhcChatroomMoreMsgTv = findViewById(R.id.plvhc_chatroom_more_msg_tv);
        plvhcChatroomSelectEmojiIv = findViewById(R.id.plvhc_chatroom_select_emoji_iv);
        plvhcChatroomSelectImgIv = findViewById(R.id.plvhc_chatroom_select_img_iv);
        plvhcChatroomCallInputEt = findViewById(R.id.plvhc_chatroom_call_input_et);
        plvhcChatroomEmojiSmallLy = findViewById(R.id.plvhc_chatroom_emoji_samll_ly);
        plvhcChatroomEmojiRv = findViewById(R.id.plvhc_chatroom_emoji_rv);
        plvhcChatroomDeleteMsgIv = findViewById(R.id.plvhc_chatroom_delete_msg_iv);
        plvhcChatroomSendMsgTv = findViewById(R.id.plvhc_chatroom_send_msg_tv);
        plvhcChatroomCloseRoomIv = findViewById(R.id.plvhc_chatroom_close_room_iv);

        //初始化按钮点击事件
        plvhcChatroomSelectEmojiIv.setOnClickListener(this);
        plvhcChatroomSelectImgIv.setOnClickListener(this);
        plvhcChatroomCallInputEt.setOnClickListener(this);
        plvhcChatroomDeleteMsgIv.setOnClickListener(this);
        plvhcChatroomSendMsgTv.setOnClickListener(this);
        plvhcChatroomCloseRoomIv.setOnClickListener(this);
        plvhcChatroomCallInputEt.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.callOnClick();
                }
                return true;
            }
        });
        plvhcChatroomChatMsgRv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeEmojiSmallLayout();
                }
                return false;
            }
        });

        //初始化聊天室列表
        PLVMessageRecyclerView.setLayoutManager(plvhcChatroomChatMsgRv).setStackFromEnd(false);
        plvhcChatroomChatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(12), 0));
        chatMessageAdapter = new PLVHCMessageAdapter();
        chatMessageAdapter.setOnViewActionListener(new PLVHCMessageAdapter.OnViewActionListener() {
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
                if (!checkChatroomEnable()) {
                    return;
                }
                Intent intent = new Intent(getContext(), PLVHCChatMsgInputWindow.class);
                intent.putExtra(PLVHCChatMsgInputWindow.ANSWER_USER_NAME, chatQuoteVO.getNick());
                intent.putExtra(PLVHCChatMsgInputWindow.ANSWER_USER_CONTENT, chatQuoteVO.getContent());
                if (chatQuoteVO.getImage() != null) {
                    intent.putExtra(PLVHCChatMsgInputWindow.ANSWER_USER_IMG_URL, chatQuoteVO.getImage().getUrl());
                    intent.putExtra(PLVHCChatMsgInputWindow.ANSWER_USER_IMG_WIDTH, chatQuoteVO.getImage().getWidth());
                    intent.putExtra(PLVHCChatMsgInputWindow.ANSWER_USER_IMG_HEIGHT, chatQuoteVO.getImage().getHeight());
                }
                PLVHCChatMsgInputWindow.setLastInputText(new SpannableStringBuilder(plvhcChatroomCallInputEt.getText()));
                PLVHCChatMsgInputWindow.show(((Activity) getContext()), intent, new PLVHCChatMsgInputWindow.MessageSendListener() {
                    @Override
                    public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                        sendChatMessage(imgEvent);
                    }

                    @Override
                    public boolean onSendQuoteMsg(String message) {
                        return sendQuoteMessage(message, chatQuoteVO, quoteId);
                    }

                    @Override
                    public void onFinish(SpannableStringBuilder lastInputText) {
                        if (lastInputText != null) {
                            plvhcChatroomCallInputEt.setText(lastInputText);
                            plvhcChatroomCallInputEt.setSelection(plvhcChatroomCallInputEt.getText().length());
                        }
                    }

                    @Override
                    public boolean onSendMsg(String message) {
                        return sendChatMessage(message);
                    }
                });
            }
        });
        plvhcChatroomChatMsgRv.setAdapter(chatMessageAdapter);
        plvhcChatroomChatMsgRv.addUnreadView(plvhcChatroomMoreMsgTv);
        plvhcChatroomChatMsgRv.addOnUnreadCountChangeListener(new PLVMessageRecyclerView.OnUnreadCountChangeListener() {
            @Override
            public void onChange(int currentUnreadCount) {
                plvhcChatroomMoreMsgTv.setText(currentUnreadCount + "条新信息");
            }
        });

        //初始化下拉加载历史记录控件
        plvhcChatroomSwipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        plvhcChatroomSwipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
            }
        });

        //初始化表情列表
        PLVHCChatroomUtils.initEmojiList(plvhcChatroomEmojiRv, plvhcChatroomCallInputEt);

        //默认不可用状态
        disableChatroom();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVHCChatroomLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;
        String userType = liveRoomDataManager.getConfig().getUser().getViewerType();
        isTeacherType = PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
        plvhcChatroomCloseRoomIv.setVisibility(isTeacherType ? View.VISIBLE : View.GONE);

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
    public IPLVChatroomContract.IChatroomPresenter getChatroomPresenter() {
        return chatroomPresenter;
    }

    @Override
    public void onLessonPreparing(long serverTime, long lessonStartTime) {
        disableChatroom();
    }

    @Override
    public void onLessonStarted() {
        enableChatroom();
    }

    @Override
    public void onLessonEnd(long inClassTime) {
        disableChatroom();
    }

    @Override
    public void onJoinDiscuss(String groupId) {
        chatroomPresenter.onJoinDiscuss(groupId);
        removeChatMessageToList(null, true);
        //请求一次历史记录
        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
    }

    @Override
    public void onLeaveDiscuss() {
        chatroomPresenter.onLeaveDiscuss();
        removeChatMessageToList(null, true);
        //请求一次历史记录
        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
    }

    @Override
    public void handleImgSelectResult(Intent data) {
        final Uri selectedUri = data.getData();
        if (selectedUri != null) {
            String picturePath = PLVUriPathHelper.getPrivatePath(getContext(), selectedUri);
            sendImg(picturePath);
        } else {
            PLVHCToast.Builder.context(getContext())
                    .setText(R.string.plv_chat_cannot_retrieve_selected_image)
                    .build()
                    .show();
        }
    }

    @Override
    public void show(int viewWidth, int viewHeight, int[] viewLocation) {
        if (container == null) {
            container = ((Activity) getContext()).findViewById(R.id.plvhc_live_room_popup_container);
            container.addOnDismissListener(new PLVOutsideTouchableLayout.OnOutsideDismissListener(this) {
                @Override
                public void onDismiss() {
                    hide();
                }
            });
        }

        int height = viewHeight - ConvertUtils.dp2px(16);
        int width = (int) (height * 0.93);

        FrameLayout.LayoutParams lp = new LayoutParams(width, height);
        lp.rightMargin = ConvertUtils.dp2px(66);
        lp.bottomMargin = ConvertUtils.dp2px(8);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        setLayoutParams(lp);

        container.removeAllViews();
        container.addView(this);

        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(true);
        }
    }

    @Override
    public void hide() {
        if (container != null) {
            container.removeAllViews();
        }
        if (onViewActionListener != null) {
            onViewActionListener.onVisibilityChanged(false);
        }
        closeEmojiSmallLayout();
    }

    @Override
    public boolean isShown() {
        return super.isShown();
    }

    @Override
    public boolean onBackPressed() {
        if (chatImageViewerFragment != null && chatImageViewerFragment.isVisible()) {
            chatImageViewerFragment.hide();
            return true;
        } else if (isShown()) {
            hide();
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
        PLVMyProgressManager.removeModuleListener(PLVHCMessageViewHolder.LOADIMG_MOUDLE_TAG);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 发送聊天信息">
    private boolean sendChatMessage(String message) {
        if (message.trim().length() == 0) {
            PLVHCToast.Builder.context(getContext())
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
                PLVHCToast.Builder.context(getContext())
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
            PLVHCToast.Builder.context(getContext())
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
                PLVHCToast.Builder.context(getContext())
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
                        plvhcChatroomChatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
                    } else {
                        plvhcChatroomChatMsgRv.scrollToBottomOrShowMore(chatMessageDataList.size());
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onUnreadMsgCountChanged(chatMessageDataList.size());
                    }
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        boolean result = chatMessageAdapter.addDataListChangedAtFirst(chatMessageDataList);
        if (result) {
            if (isScrollEnd) {
                plvhcChatroomChatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            } else {
                plvhcChatroomChatMsgRv.scrollToPosition(0);
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
        public void onCloseRoomStatusChanged(boolean isClose) {
            super.onCloseRoomStatusChanged(isClose);
            if (isCloseRoom != isClose) {
                isCloseRoom = isClose;
                final List<PLVBaseViewData> dataList = new ArrayList<>();
                PLVCloseRoomEvent closeRoomEvent = new PLVCloseRoomEvent();
                PLVCloseRoomEvent.ValueBean valueBean = new PLVCloseRoomEvent.ValueBean();
                valueBean.setClosed(isClose);
                valueBean.setRoomId(PLVSocketWrapper.getInstance().getLoginRoomId());
                closeRoomEvent.setValue(valueBean);
                dataList.add(new PLVBaseViewData<>(closeRoomEvent, PLVChatMessageItemType.ITEMTYPE_TIPS_MSG, null));
                //添加信息至列表
                addChatMessageToList(dataList, false);

                plvhcChatroomCloseRoomIv.setSelected(isClose);
            }
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
            plvhcChatroomSwipeLoadView.setRefreshing(false);
            plvhcChatroomSwipeLoadView.setEnabled(true);
            if (chatMessageDataList.size() > 0) {
                addChatHistoryToList(chatMessageDataList, requestSuccessTime == 1);
            }
            if (isNoMoreHistory) {
                plvhcChatroomSwipeLoadView.setEnabled(false);
            }
        }

        @Override
        public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            plvhcChatroomSwipeLoadView.setRefreshing(false);
            plvhcChatroomSwipeLoadView.setEnabled(true);
            if (viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                PLVHCToast.Builder.context(getContext())
                        .setText(getContext().getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg)
                        .build()
                        .show();
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 选择图片发送">
    private void requestSelectImg() {
        ArrayList<String> permissions = new ArrayList<>(1);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PLVFastPermission.getInstance()
                .start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        selectImg();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        if (deniedForeverP.size() > 0) {
                            showRequestPermissionDialog(getContext().getString(R.string.plv_chat_send_img_error_tip_permission_denied));
                        } else {
                            PLVHCToast.Builder.context(getContext())
                                    .setText(R.string.plv_chat_send_img_error_tip_permission_cancel)
                                    .build()
                                    .show();
                        }
                    }
                });
    }

    private void selectImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        ((Activity) getContext()).startActivityForResult(Intent.createChooser(intent, getContext().getString(R.string.plv_chat_chooser_sel_img)), REQUEST_CODE_SELECT_IMG);
    }

    private void showRequestPermissionDialog(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.plv_common_dialog_tip)
                .setMessage(message)
                .setPositiveButton(R.string.plv_common_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(getContext());
                    }
                })
                .setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCancelable(false).show();
    }

    private void sendImg(String picturePath) {
        PolyvSendLocalImgEvent sendLocalImgEvent = new PolyvSendLocalImgEvent();
        sendLocalImgEvent.setImageFilePath(picturePath);
        int[] pictureWh = PLVSendChatImageHelper.getPictureWh(picturePath);
        sendLocalImgEvent.setWidth(pictureWh[0]);
        sendLocalImgEvent.setHeight(pictureWh[1]);

        sendChatMessage(sendLocalImgEvent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 打开信息输入窗口">
    private void openInputWindow(boolean isFirstShowEmojiLayout) {
        Intent intent = new Intent(getContext(), PLVHCChatMsgInputWindow.class);
        intent.putExtra(PLVHCChatMsgInputWindow.IS_FIRST_SHOW_EMOJI_LAYOUT, isFirstShowEmojiLayout);
        PLVHCChatMsgInputWindow.setLastInputText(new SpannableStringBuilder(plvhcChatroomCallInputEt.getText()));
        PLVHCChatMsgInputWindow.show(((Activity) getContext()), intent, new PLVHCChatMsgInputWindow.MessageSendListener() {
            @Override
            public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                sendChatMessage(imgEvent);
            }

            @Override
            public boolean onSendQuoteMsg(String message) {
                return false;
            }

            @Override
            public void onFinish(SpannableStringBuilder lastInputText) {
                if (lastInputText != null) {
                    plvhcChatroomCallInputEt.setText(lastInputText);
                    plvhcChatroomCallInputEt.setSelection(plvhcChatroomCallInputEt.getText().length());
                }
            }

            @Override
            public boolean onSendMsg(String message) {
                return sendChatMessage(message);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 可用状态处理">
    private void disableChatroom() {
        isChatroomEnable = false;
        plvhcChatroomCallInputEt.setHint("请耐心等待上课");
    }

    private void enableChatroom() {
        isChatroomEnable = true;
        plvhcChatroomCallInputEt.setHint(Html.fromHtml("有话要说&#046;&#046;&#046;"));
    }

    private boolean checkChatroomEnable() {
        if (!isChatroomEnable) {
            PLVHCToast.Builder.context(getContext())
                    .setText("上课前不能聊天")
                    .build()
                    .show();
            return false;
        } else if (!isTeacherType && isCloseRoom) {
            PLVHCToast.Builder.context(getContext())
                    .setText("老师已开启全体禁言")
                    .build()
                    .show();
            return false;
        }
        return true;
    }
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
                PLVHCToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_reconnecting)
                        .build()
                        .show();
            } else {
                PLVHCToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_logging)
                        .build()
                        .show();
            }
        }

        @Override
        public void handleLoginSuccess(boolean isReconnect) {
            super.handleLoginSuccess(isReconnect);
            if (isReconnect) {
                PLVHCToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_reconnect_success)
                        .build()
                        .show();
            } else {
                PLVHCToast.Builder.context(getContext())
                        .setText(R.string.plv_chat_toast_login_success)
                        .build()
                        .show();
            }
        }

        @Override
        public void handleLoginFailed(@NonNull Throwable throwable) {
            super.handleLoginFailed(throwable);
            PLVHCToast.Builder.context(getContext())
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

    // <editor-fold defaultstate="collapsed" desc="内部方法">
    private void closeEmojiSmallLayout() {
        plvhcChatroomEmojiSmallLy.setVisibility(View.GONE);
        plvhcChatroomSelectEmojiIv.setSelected(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.plvhc_chatroom_select_emoji_iv) {
            if (!checkChatroomEnable()) {
                return;
            }
            v.setSelected(!v.isSelected());
            plvhcChatroomEmojiSmallLy.setVisibility(v.isSelected() ? View.VISIBLE : View.GONE);
        } else if (id == R.id.plvhc_chatroom_select_img_iv) {
            if (!checkChatroomEnable()) {
                return;
            }
            requestSelectImg();
            closeEmojiSmallLayout();
        } else if (id == R.id.plvhc_chatroom_call_input_et) {
            if (!checkChatroomEnable()) {
                return;
            }
            openInputWindow(false);
            closeEmojiSmallLayout();
        } else if (id == R.id.plvhc_chatroom_delete_msg_iv) {
            PLVHCChatroomUtils.deleteEmoText(plvhcChatroomCallInputEt);
        } else if (id == R.id.plvhc_chatroom_send_msg_tv) {
            boolean result = sendChatMessage(plvhcChatroomCallInputEt.getText().toString());
            if (result) {
                plvhcChatroomCallInputEt.setText("");
                closeEmojiSmallLayout();
            }
        } else if (id == R.id.plvhc_chatroom_close_room_iv) {
            if (!checkChatroomEnable()) {
                return;
            }
            v.setEnabled(false);
            chatroomPresenter.toggleRoom(!v.isSelected(), new IPLVChatroomManager.RequestApiListener<String>() {
                @Override
                public void onSuccess(String s) {
                    v.setEnabled(true);
                }

                @Override
                public void onFailed(Throwable t) {
                    v.setEnabled(true);
                }
            });
        }
    }
    // </editor-fold>
}
