package com.easefun.polyv.livecloudclass.modules.chatroom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecloudclass.modules.chatroom.utils.PLVChatroomUtils;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCBulletinTextView;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCGreetingTextView;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.PLVUriPathHelper;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVInputFragment;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 互动聊天tab页
 */
public class PLVLCChatFragment extends PLVInputFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final int REQUEST_SELECT_IMG = 0x01;//选择图片请求标志
    private static final int REQUEST_OPEN_CAMERA = 0x02;//打开相机请求标志
    //聊天信息列表
    private PLVLCChatCommonMessageList chatCommonMessageList;
    //未读信息提醒view
    private TextView unreadMsgTv;

    //信息输入框
    private EditText inputEt;

    //表情布局开关
    private ImageView toggleEmojiIv;
    //更多布局开关
    private ImageView toggleMoreIv;

    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;

    //更多布局
    private ViewGroup moreLy;
    //只看讲师/查看全部
    private ViewGroup changeMessageTypeLy;
    private TextView messageTypeTv;
    //发送图片
    private ViewGroup sendImgLy;
    //拍摄
    private ViewGroup openCameraLy;
    //公告
    private ViewGroup showBulletinLy;
    //占位view，用于保持布局间的间距
    private View moreEmptyView;

    //表情布局
    private ViewGroup emojiLy;
    private TextView sendMsgTv;
    private ImageView deleteMsgIv;
    //表情列表
    private RecyclerView emojiRv;

    //点赞布局
    private ViewGroup likesLy;
    private PLVLCLikeIconView likesView;
    private TextView likesCountTv;
    private long likesCount;

    //欢迎语
    private PLVLCGreetingTextView greetingTv;
    private boolean isShowGreeting;//是否显示欢迎语

    //公告(管理员发言)
    private PLVLCBulletinTextView bulletinTv;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;

    //拍摄图片的保存地址
    private File takePictureFilePath;
    private Uri takePictureUri;

    //是否是直播类型
    private boolean isLiveType;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvlc_chatroom_chat_portrait_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMG && resultCode == Activity.RESULT_OK) {
            final Uri selectedUri = data.getData();
            if (selectedUri != null) {
                String picturePath = PLVUriPathHelper.getPrivatePath(getContext(), selectedUri);
                sendImg(picturePath);
            } else {
                ToastUtils.showShort("cannot retrieve selected image");
            }
        } else if (requestCode == REQUEST_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {//data->null
            if (Build.VERSION.SDK_INT >= 29) {
                String picturePath = PLVUriPathHelper.getPrivatePath(getContext(), takePictureUri);
                sendImg(picturePath);
            } else {
                sendImg(takePictureFilePath.getAbsolutePath());
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(PLVLCChatCommonMessageList chatCommonMessageList) {
        this.chatCommonMessageList = chatCommonMessageList;
    }

    //设置是否是直播类型，如果不是直播类型，则隐藏公告(互动功能相关)按钮
    public void setIsLiveType(boolean isLiveType) {
        this.isLiveType = isLiveType;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        if (chatCommonMessageList == null || chatroomPresenter == null) {
            return;
        }
        //未读信息view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);
        chatCommonMessageList.addUnreadView(unreadMsgTv);
        chatCommonMessageList.addOnUnreadCountChangeListener(new PLVMessageRecyclerView.OnUnreadCountChangeListener() {
            @Override
            public void onChange(int currentUnreadCount) {
                unreadMsgTv.setText("有" + currentUnreadCount + "条新消息，点击查看");
            }
        });

        //信息输入框
        inputEt = findViewById(R.id.input_et);
        inputEt.addTextChangedListener(inputTextWatcher);

        //表情、更多按钮
        toggleEmojiIv = findViewById(R.id.toggle_emoji_iv);
        toggleEmojiIv.setOnClickListener(this);
        toggleMoreIv = findViewById(R.id.toggle_more_iv);
        toggleMoreIv.setVisibility(View.VISIBLE);
        toggleMoreIv.setOnClickListener(this);

        //下拉控件
        swipeLoadView = findViewById(R.id.swipe_load_view);
        swipeLoadView.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (chatroomPresenter == null) {
                    return;
                }
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
            }
        });

        //把聊天信息列表附加到下拉控件中
        boolean result = chatCommonMessageList.attachToParent(swipeLoadView, false);
        if (result) {
            //设置信息索引，需在chatroomPresenter.registerView后设置
            chatCommonMessageList.setMsgIndex(chatroomPresenter.getViewIndex(chatroomView));
            //附加成功后，加载历史记录
            if (chatroomPresenter.getChatHistoryTime() == 0) {
                chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));//加载一次历史记录
            }
        }

        //更多布局
        moreLy = findViewById(R.id.more_ly);
        changeMessageTypeLy = findViewById(R.id.change_message_type_ly);
        changeMessageTypeLy.setOnClickListener(this);
        messageTypeTv = findViewById(R.id.message_type_tv);
        sendImgLy = findViewById(R.id.send_img_ly);
        sendImgLy.setOnClickListener(this);
        openCameraLy = findViewById(R.id.open_camera_ly);
        openCameraLy.setOnClickListener(this);
        moreEmptyView = findViewById(R.id.more_empty_view);
        showBulletinLy = findViewById(R.id.show_bulletin_ly);
        showBulletinLy.setOnClickListener(this);
        if (!isLiveType) {
            showBulletinLy.setVisibility(View.INVISIBLE);
        }

        //表情布局
        emojiLy = findViewById(R.id.emoji_ly);
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        deleteMsgIv = findViewById(R.id.delete_msg_iv);
        deleteMsgIv.setOnClickListener(this);
        //表情列表
        emojiRv = findViewById(R.id.emoji_rv);
        PLVChatroomUtils.initEmojiList(emojiRv, inputEt);

        //点赞布局
        likesLy = findViewById(R.id.likes_ly);
        likesView = findViewById(R.id.likes_view);
        likesView.setOnButtonClickListener(this);
        likesCountTv = findViewById(R.id.likes_count_tv);
        if (likesCount != 0) {
            String likesString = StringUtils.toWString(likesCount);
            likesCountTv.setText(likesString);
        }

        //欢迎语
        greetingTv = findViewById(R.id.greeting_tv);

        //公告(管理员发言)
        bulletinTv = findViewById(R.id.bulletin_tv);

        addPopupButton(toggleEmojiIv);
        addPopupLayout(emojiLy);

        addPopupButton(toggleMoreIv);
        addPopupLayout(moreLy);

        //观察聊天室功能开关数据
        observeFunctionSwitchData();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVInputFragment定义的方法">
    @Override
    public int inputLayoutId() {
        return R.id.bottom_input_ly;
    }

    @Override
    public int inputViewId() {
        return R.id.input_et;
    }

    @Override
    public boolean onSendMsg(String message) {
        return sendChatMessage(message);
    }

    @Override
    public int attachContainerViewId() {
        return R.id.plvlc_chatroom_input_layout_container;
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
        public void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent) {
            super.onSpeakEvent(speakEvent);
            acceptSpeakEvent(speakEvent);
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(16);
        }

        @Override
        public void onImgEvent(@NonNull PLVChatImgEvent chatImgEvent) {
            super.onImgEvent(chatImgEvent);
        }

        @Override
        public void onLikesEvent(@NonNull PLVLikesEvent likesEvent) {
            super.onLikesEvent(likesEvent);
            acceptLikesMessage(likesEvent.getCount());
        }

        @Override
        public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {
            super.onLoginEvent(loginEvent);
            acceptLoginEvent(loginEvent);
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
            if (chatCommonMessageList != null && chatCommonMessageList.isLandscapeLayout()) {
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
            if (!isShowKeyBoard(new OnceHideKeyBoardListener() {
                @Override
                public void call() {
                    //添加信息至列表
                    addChatMessageToList(dataList, true);//如果键盘还没完全隐藏，则等待键盘隐藏后再添加到列表中，避免出现列表布局动画问题
                }
            })) {
                //添加信息至列表
                addChatMessageToList(dataList, true);
            }
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

        @Override
        public void onHistoryRequestFailed(String errorMsg, Throwable t, int viewIndex) {
            super.onHistoryRequestFailed(errorMsg, t, viewIndex);
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
                swipeLoadView.setEnabled(true);
            }
            if (chatroomPresenter != null && viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                ToastUtils.showShort(getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
            }
        }
    };

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 欢迎语、公告处理">
    private void acceptLoginEvent(final PLVLoginEvent loginEvent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //显示欢迎语
                if (greetingTv != null && isShowGreeting) {
                    greetingTv.acceptLoginEvent(loginEvent);
                }
            }
        });
    }

    private void acceptSpeakEvent(PLVSpeakEvent speakEvent) {
        //判断是不是管理员类型
        if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(speakEvent.getUser().getUserType())) {
            //开启跑马灯公告(管理员发言)
            if (bulletinTv != null) {
                bulletinTv.startMarquee((CharSequence) speakEvent.getObjects()[0]);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 列表数据更新">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (chatCommonMessageList != null) {
                    chatCommonMessageList.addChatMessageToList(chatMessageDataList, isScrollEnd, false);
                }
            }
        });
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addChatHistoryToList(chatMessageDataList, isScrollEnd, false);
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
                    chatCommonMessageList.removeAllChatMessage(false);
                } else {
                    chatCommonMessageList.removeChatMessage(id, false);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 发送聊天信息">
    private boolean sendChatMessage(String message) {
        if (message.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            return false;
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
            if (chatroomPresenter == null) {
                return false;
            }
            Pair<Boolean, Integer> sendResult = chatroomPresenter.sendChatMessage(localMessage);
            if (sendResult.first) {
                //清空输入框内容并隐藏键盘/弹出的表情布局等
                inputEt.setText("");
                hideSoftInputAndPopupLayout();
                return true;
            } else {
                //发送失败
                ToastUtils.showShort(getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second);
                return false;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 打开相机、选择图片及发送">
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
                        if (!deniedForeverP.isEmpty()) {
                            showRequestPermissionDialog("发送图片所需的存储权限被拒绝，请到应用设置的权限管理中恢复");
                        } else {
                            ToastUtils.showShort("请允许存储权限后再发送图片");
                        }
                    }
                });
    }

    private void selectImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_SELECT_IMG);
    }

    private void requestOpenCamera() {
        ArrayList<String> permissions = new ArrayList<>(2);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        PLVFastPermission.getInstance()
                .start((Activity) getContext(), permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        openCamera();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        if (!deniedForeverP.isEmpty()) {
                            showRequestPermissionDialog("拍摄所需的存储或相机权限被拒绝，请到应用设置的权限管理中恢复");
                        } else {
                            ToastUtils.showShort("请允许存储和相机权限后再拍摄");
                        }
                    }
                });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String picName = System.currentTimeMillis() + ".jpg";//同名会覆盖
        if (Build.VERSION.SDK_INT >= 29) {
            Environment.getExternalStorageState();
            // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, picName);
            takePictureUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            if (getContext() == null || getContext().getApplicationContext() == null) {
                return;
            }
            String savePath = PLVSDCardUtils.createPath(getContext(), "PLVChatImg");
            takePictureFilePath = new File(savePath, picName);
            takePictureUri = FileProvider.getUriForFile(
                    getContext(),
                    getContext().getApplicationContext().getPackageName() + ".plvfileprovider",
                    takePictureFilePath);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, takePictureUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_OPEN_CAMERA);
    }

    private void showRequestPermissionDialog(String message) {
        new AlertDialog.Builder(getContext()).setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PLVFastPermission.getInstance().jump2Settings(getContext());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCancelable(false).show();
    }

    private void sendImg(String picturePath) {
        PolyvSendLocalImgEvent sendLocalImgEvent = new PolyvSendLocalImgEvent();
        sendLocalImgEvent.setImageFilePath(picturePath);
        int[] pictureWh = PolyvSendChatImageHelper.getPictureWh(picturePath);
        sendLocalImgEvent.setWidth(pictureWh[0]);
        sendLocalImgEvent.setHeight(pictureWh[1]);

        if (chatroomPresenter != null) {
            chatroomPresenter.sendChatImage(sendLocalImgEvent);
        }
        hideSoftInputAndPopupLayout();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 当前是否是只看讲师">
    public boolean isDisplaySpecialType() {
        return changeMessageTypeLy != null && changeMessageTypeLy.isSelected();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="输入框 - 文本改变监听器">
    private TextWatcher inputTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() > 0) {
                sendMsgTv.setEnabled(true);
                sendMsgTv.setSelected(true);
            } else {
                sendMsgTv.setSelected(false);
                sendMsgTv.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点赞 - 数据设置、处理">
    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
        String likesString = StringUtils.toWString(likesCount);
        if (likesCountTv != null) {
            likesCountTv.setText(likesString);
        }
    }

    private void acceptLikesMessage(final int likesCount) {
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                startAddLoveIconTask(200, Math.min(5, likesCount));
            }
        });
    }

    private void startAddLoveIconTask(final long ts, final int count) {
        if (count >= 1) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (likesView != null) {
                        likesView.addLoveIcon(1);
                    }
                    startAddLoveIconTask(ts, count - 1);
                }
            }, ts);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (chatCommonMessageList == null) {
                return;
            }
            boolean result = chatCommonMessageList.attachToParent(swipeLoadView, false);
            if (result && chatroomPresenter != null) {
                chatCommonMessageList.setMsgIndex(chatroomPresenter.getViewIndex(chatroomView));
                //处理播放页面初始竖屏，然后在竖屏聊天室没加载完成前切换到横屏的情况，之后等竖屏聊天室加载完成后再切换竖屏，这时需要加载历史记录
                if (chatroomPresenter.getChatHistoryTime() == 0) {
                    chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 观察功能开关数据">
    private void observeFunctionSwitchData() {
        if (chatroomPresenter == null) {
            return;
        }
        chatroomPresenter.getData().getFunctionSwitchData().observe(this, new Observer<List<PolyvChatFunctionSwitchVO.DataBean>>() {
            @Override
            public void onChanged(@Nullable List<PolyvChatFunctionSwitchVO.DataBean> dataBeans) {
                if (chatroomPresenter != null) {
                    chatroomPresenter.getData().getFunctionSwitchData().removeObserver(this);
                }
                if (dataBeans == null) {
                    return;
                }
                for (PolyvChatFunctionSwitchVO.DataBean dataBean : dataBeans) {
                    boolean isSwitchEnabled = dataBean.isEnabled();
                    switch (dataBean.getType()) {
                        //观众发送图片开关
                        case PolyvChatFunctionSwitchVO.TYPE_VIEWER_SEND_IMG_ENABLED:
                            sendImgLy.setVisibility(isSwitchEnabled ? View.VISIBLE : View.GONE);
                            openCameraLy.setVisibility(isSwitchEnabled ? View.VISIBLE : View.GONE);
                            moreEmptyView.setVisibility(isSwitchEnabled ? View.GONE : View.VISIBLE);
                            break;
                        //欢迎语开关
                        case PolyvChatFunctionSwitchVO.TYPE_WELCOME:
                            isShowGreeting = isSwitchEnabled;
                            break;
                        //送花/点赞开关
                        case PolyvChatFunctionSwitchVO.TYPE_SEND_FLOWERS_ENABLED:
                            likesLy.setVisibility(isSwitchEnabled ? View.VISIBLE : View.GONE);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.toggle_emoji_iv) {
            togglePopupLayout(toggleEmojiIv, emojiLy);
        } else if (id == R.id.toggle_more_iv) {
            togglePopupLayout(toggleMoreIv, moreLy);
        } else if (id == R.id.delete_msg_iv) {
            PLVChatroomUtils.deleteEmoText(inputEt);
        } else if (id == R.id.send_msg_tv) {
            sendChatMessage(inputEt.getText().toString());
        } else if (id == R.id.change_message_type_ly) {
            changeMessageTypeLy.setSelected(!changeMessageTypeLy.isSelected());
            messageTypeTv.setText(changeMessageTypeLy.isSelected() ? R.string.plv_chat_view_all_message : R.string.plv_chat_view_special_message);
            if (chatCommonMessageList != null) {
                chatCommonMessageList.changeDisplayType(changeMessageTypeLy.isSelected());
            }
        } else if (id == R.id.show_bulletin_ly) {
            hideSoftInputAndPopupLayout();
            if (onViewActionListener != null) {
                onViewActionListener.onShowBulletinAction();
            }
        } else if (id == R.id.send_img_ly) {
            requestSelectImg();
        } else if (id == R.id.open_camera_ly) {
            requestOpenCamera();
        } else if (id == R.id.likes_view) {
            if (chatroomPresenter != null) {
                chatroomPresenter.sendLikeMessage();
            }
            acceptLikesMessage(1);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onShowBulletinAction();
    }
    // </editor-fold>
}
