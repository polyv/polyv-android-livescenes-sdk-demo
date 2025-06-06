package com.easefun.polyv.livecloudclass.modules.chatroom;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;
import static com.plv.foundationsdk.utils.PLVTimeUnit.seconds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCEmotionPersonalListAdapter;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatmore.PLVLCChatFunctionListener;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatmore.PLVLCChatMoreLayout;
import com.easefun.polyv.livecloudclass.modules.chatroom.layout.PLVLCChatOverLengthMessageLayout;
import com.easefun.polyv.livecloudclass.modules.chatroom.layout.PLVLCChatReplyMessageLayout;
import com.easefun.polyv.livecloudclass.modules.chatroom.utils.PLVChatroomUtils;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCBulletinTextView;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCChatTipsLayout;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCGreetingTextView;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.entrance.PLVInteractEntranceLayout;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.welfarelottery.PLVWelfareLotteryManager;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.vo.PLVMultiRoomTransmitVO;
import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.viewmodel.PLVMultiRoomTransmitViewModel;
import com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.PLVRedpackViewModel;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.IPLVPointRewardEventProducer;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectQueue;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectWidget;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVRewardSVGAHelper;
import com.easefun.polyv.livecommon.module.utils.PLVStoragePermissionCompat;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.PLVImageUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVImagePreviewPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.window.PLVInputFragment;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVChatFunctionVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
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
    private String TAG = getClass().getSimpleName();

    private static final int REQUEST_SELECT_IMG = 0x01;//选择图片请求标志
    private static final int REQUEST_OPEN_CAMERA = 0x02;//打开相机请求标志

    private final PLVMultiRoomTransmitViewModel multiRoomTransmitViewModel = PLVDependManager.getInstance().get(PLVMultiRoomTransmitViewModel.class);
    private final PLVRedpackViewModel redpackViewModel = PLVDependManager.getInstance().get(PLVRedpackViewModel.class);
    private final PLVWelfareLotteryManager welfareLotteryManager = PLVDependManager.getInstance().get(PLVWelfareLotteryManager.class);

    //聊天信息列表
    private PLVLCChatCommonMessageList chatCommonMessageList;
    // 数据管理
    private IPLVLiveRoomDataManager liveRoomDataManager;
    //未读信息提醒view
    private TextView unreadMsgTv;
    //聊天背景
    private ImageView chatroomBgIv;

    @Nullable
    private PLVLCChatOverLengthMessageLayout chatOverLengthMessageLayout;

    //信息输入框
    private EditText inputEt;
    //信息输入框中记录的信息
    private Editable recordInputMessage;

    //表情布局开关
    private ImageView toggleEmojiIv;
    //更多布局开关
    private ImageView toggleMoreIv;

    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;

    private PLVLCChatMoreLayout chatMoreLayout;
    //是否选择只看讲师
    private boolean isSelectOnlyTeacher;

    //表情布局
    private ViewGroup emojiLy;
    private TextView sendMsgTv;
    private ImageView deleteMsgIv;
    //emoji表情列表
    private RecyclerView emojiRv;
    //emoji表情tab
    private ImageView tabEmojiIv;
    private RecyclerView emojiPersonalRv;
    //个性化表情tab
    private ImageView tabPersonalIv;
    //个性化表情预览弹窗
    private PLVImagePreviewPopupWindow emotionPreviewWindow;

    //点赞布局
    private ViewGroup likesLy;
    private PLVLCLikeIconView likesView;
    private TextView likesCountTv;
    private long likesCount;


    //打赏按钮
    @Nullable
    private ImageView rewardIv;
    //积分打赏事件队列
    private IPLVPointRewardEventProducer pointRewardEventProducer;
    //积分打赏动画item
    private PLVPointRewardEffectWidget polyvPointRewardEffectWidget;
    //积分打赏svg动画
    private SVGAImageView rewardSvgImage;
    private SVGAParser svgaParser;
    private PLVRewardSVGAHelper svgaHelper;
    //是否选择屏蔽特效
    private boolean isSelectCloseEffect = false;

    //卡片推送
    private ImageView cardEnterView;
    private TextView cardEnterCdTv;
    private PLVTriangleIndicateTextView cardEnterTipsView;
    private PLVCardPushManager cardPushManager;

    //无条件抽奖挂件
    private PLVLotteryManager lotteryManager;
    private ImageView lotteryEnterView;
    private TextView lotteryEnterCdTv;
    private PLVTriangleIndicateTextView lotteryEnterTipsView;

    //有条件抽奖挂件
    private ImageView welfareLotteryEnterView;
    private TextView welfareLotteryCdVTv;
    private PLVTriangleIndicateTextView welfareLotteryEnterTipsView;

    //欢迎语
    private PLVLCGreetingTextView greetingTv;
    private boolean isShowGreeting;//是否显示欢迎语

    //公告(管理员发言)
    private PLVLCBulletinTextView bulletinTv;

    private PLVLCChatReplyMessageLayout chatReplyLayout;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;

    //拍摄图片的保存地址
    private File takePictureFilePath;
    private Uri takePictureUri;

    //是否是直播类型
    private boolean isLiveType;
    //是否打开积分打赏按钮
    private boolean isOpenPointReward = false;

    //聊天回放管理器
    private IPLVChatPlaybackManager chatPlaybackManager;
    private Runnable playbackTipsRunnable;

    //功能开关数据
    private List<PolyvChatFunctionSwitchVO.DataBean> functionSwitchData;
    //表情图片数据
    private List<PLVEmotionImageVO.EmotionImage> emotionImages;

    //tipsView
    private PLVLCChatTipsLayout chatTipsLayout;
    //是否聊天回放布局(聊天回放布局不需要响应聊天室的实时聊天信息、房间开关、专注模式等)
    private boolean isChatPlaybackLayout;

    //聊天室开关状态
    private boolean isCloseRoomStatus;
    //专注模式状态
    private boolean isFocusModeStatus;

    // 转播
    @Nullable
    private PLVMultiRoomTransmitVO transmitVO = null;

    //互动入口布局
    private PLVInteractEntranceLayout interactEntranceLy;
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
                sendImg(PLVImageUtils.transformUriToFilePath(getContext(), selectedUri));
            } else {
                ToastUtils.showShort("cannot retrieve selected image");
            }
        } else if (requestCode == REQUEST_OPEN_CAMERA && resultCode == Activity.RESULT_OK) {//data->null
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    sendImg(PLVImageUtils.transformUriToFilePath(getContext(), takePictureUri));
                } else {
                    sendImg(takePictureFilePath.getAbsolutePath());
                }
            } catch (Exception e) {
                PLVCommonLog.exception(e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isOpenPointReward) {
            destroyPointRewardEffectQueue();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">
    public void init(PLVLCChatCommonMessageList chatCommonMessageList, IPLVLiveRoomDataManager liveRoomDataManager) {
        this.chatCommonMessageList = chatCommonMessageList;
        this.liveRoomDataManager = liveRoomDataManager;

        redpackViewModel.updateDelayRedpackStatus(liveRoomDataManager.getConfig().getChannelId());
    }

    public void setIsChatPlaybackLayout(boolean isChatPlaybackLayout) {
        this.isChatPlaybackLayout = isChatPlaybackLayout;
    }

    public void setCardPushManager(PLVCardPushManager cardPushManager) {
        this.cardPushManager = cardPushManager;
    }

    public void setLotteryManager(PLVLotteryManager lotteryManager) {
        this.lotteryManager = lotteryManager;
    }

    //设置是否是直播类型，如果不是直播类型，则隐藏公告(互动功能相关)按钮
    public void setIsLiveType(boolean isLiveType) {
        this.isLiveType = isLiveType;
    }

    /**
     * 设置是否开启积分打赏按钮
     */
    public void setOpenPointReward(boolean open) {
        isOpenPointReward = open;
        if (rewardIv != null) {
            rewardIv.setVisibility(open ? View.VISIBLE : View.GONE);
        }
        updateRewardEffectBtnVisibility(isOpenPointReward);
        initPointRewardEffectQueue();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        //未读信息view
        unreadMsgTv = findViewById(R.id.unread_msg_tv);
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addUnreadView(unreadMsgTv);
            chatCommonMessageList.addOnUnreadCountChangeListener(new PLVMessageRecyclerView.OnUnreadCountChangeListener() {
                @Override
                public void onChange(int currentUnreadCount) {
                    unreadMsgTv.setText(PLVAppUtils.formatString(R.string.plv_chat_view_new_msg_2, currentUnreadCount + ""));
                }
            });
            chatCommonMessageList.setOnViewActionListener(new PLVLCChatCommonMessageList.OnViewActionListener() {
                @Override
                public void onShowAloneOverLengthMessage(PLVLCChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean) {
                    if (chatOverLengthMessageLayout != null) {
                        chatOverLengthMessageLayout.show(chatMessageDataBean);
                    }
                }

                @Override
                public void onReplyMessage(PLVChatQuoteVO chatQuoteVO) {
                    if (chatReplyLayout != null) {
                        chatReplyLayout.setChatQuoteContent(chatQuoteVO);
                    }
                    if (onViewActionListener != null) {
                        onViewActionListener.onReplyMessage(chatQuoteVO);
                    }
                }

                @Override
                public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onReceiveRedPaper(redPaperEvent);
                    }
                }
            });
        }
        //聊天室背景
        chatroomBgIv = findViewById(R.id.plv_chat_room_bg_iv);

        //信息输入框
        inputEt = findViewById(R.id.input_et);
        inputEt.addTextChangedListener(inputTextWatcher);
        if (isChatPlaybackLayout || !isLiveType) {
            inputEt.setHint(PLVAppUtils.getString(R.string.plv_chat_input_tips_chatroom_close));
            inputEt.setEnabled(false);
        }

        //表情、更多按钮
        toggleEmojiIv = findViewById(R.id.toggle_emoji_iv);
        toggleEmojiIv.setOnClickListener(this);
        toggleMoreIv = findViewById(R.id.toggle_more_iv);
        if (isChatPlaybackLayout || !isLiveType) {
            toggleEmojiIv.setEnabled(false);
            toggleEmojiIv.setAlpha(0.5f);
        } else {
            toggleEmojiIv.setEnabled(true);
            toggleEmojiIv.setAlpha(1f);
        }
        toggleMoreIv.setVisibility(View.VISIBLE);
        toggleMoreIv.setOnClickListener(this);

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

        if (chatCommonMessageList != null) {
            //把聊天信息列表附加到下拉控件中
            boolean result = chatCommonMessageList.attachToParent(swipeLoadView, false);
            if (result) {
                if (chatroomPresenter != null) {
                    //设置信息索引，需在chatroomPresenter.registerView后设置
                    chatCommonMessageList.setMsgIndex(chatroomPresenter.getViewIndex(chatroomView));
                    if (!isChatPlaybackLayout) {
                        //附加成功后，加载历史记录
                        if (chatroomPresenter.getChatHistoryTime() == 0) {
                            chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));//加载一次历史记录
                        }
                    }
                }
            }
        }

        initChatMoreLayout();

        //表情布局
        emojiLy = findViewById(R.id.emoji_ly);
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        deleteMsgIv = findViewById(R.id.delete_msg_iv);
        deleteMsgIv.setOnClickListener(this);
        //emoji表情列表
        emojiRv = findViewById(R.id.emoji_rv);
        PLVChatroomUtils.initEmojiList(emojiRv, inputEt);
        //个性表情列表
        emojiPersonalRv = findViewById(R.id.emoji_personal_rv);
        //表情tab
        tabEmojiIv = findViewById(R.id.plvlc_emoji_tab_emoji_iv);
        tabEmojiIv.setSelected(true);
        tabEmojiIv.setOnClickListener(this);
        tabPersonalIv = findViewById(R.id.plvlc_emoji_tab_personal_iv);
        tabPersonalIv.setVisibility(View.VISIBLE);
        tabPersonalIv.setOnClickListener(this);
        //表情预览弹窗
        emotionPreviewWindow = new PLVImagePreviewPopupWindow(getContext());

        //点赞布局
        likesLy = findViewById(R.id.likes_ly);
        likesView = findViewById(R.id.likes_view);
        likesView.setOnButtonClickListener(this);
        likesCountTv = findViewById(R.id.likes_count_tv);
        if (likesCount != 0) {
            String likesString = StringUtils.toKString(likesCount);
            likesCountTv.setText(likesString);
        }

        //打赏
        rewardIv = findViewById(R.id.plvlc_iv_show_point_reward);
        rewardIv.setOnClickListener(this);
        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
        //打赏svga动画
        rewardSvgImage = findViewById(R.id.plvlc_reward_svg);
        svgaParser = new SVGAParser(getContext());
        svgaHelper = new PLVRewardSVGAHelper();
        svgaHelper.init(rewardSvgImage, svgaParser);

        //打赏横幅动画特效
        polyvPointRewardEffectWidget = findViewById(R.id.plvlc_point_reward_effect);
        polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);

        //欢迎语
        greetingTv = findViewById(R.id.greeting_tv);

        //公告(管理员发言)
        bulletinTv = findViewById(R.id.bulletin_tv);

        //聊天回放tipsView
        chatTipsLayout = findViewById(R.id.plvlc_chat_tips_layout);
        if (isChatPlaybackLayout) {
            chatTipsLayout.show(
                    new PLVLCChatTipsLayout.ShowTipsConfiguration()
                            .setContent(PLVAppUtils.getString(R.string.plv_chat_playback_tips))
                            .setContentGravity(Gravity.CENTER)
                            .setClosable(false)
                            .setAutoHideMillis(seconds(5).toMillis())
            );
        }

        //卡片推送
        cardEnterView = findViewById(R.id.card_enter_view);
        cardEnterCdTv = findViewById(R.id.card_enter_cd_tv);
        cardEnterTipsView = findViewById(R.id.card_enter_tips_view);
        if (cardPushManager != null) {
            cardPushManager.registerView(cardEnterView, cardEnterCdTv, cardEnterTipsView);
        }

        //无条件抽奖挂件
        lotteryEnterView = findViewById(R.id.plvlc_live_lottery_enter_view);
        lotteryEnterCdTv = findViewById(R.id.plvlc_live_lottery_enter_cd_tv);
        lotteryEnterTipsView = findViewById(R.id.plvlc_live_lottery_enter_tips_view);
        if (lotteryManager != null) {
            lotteryManager.registerView(lotteryEnterView, lotteryEnterCdTv, lotteryEnterTipsView);
        }

        // 有条件抽奖挂件
        welfareLotteryEnterView = findViewById(R.id.plvlc_live_welfare_lotter_view);
        welfareLotteryCdVTv = findViewById(R.id.plvlc_live_welfare_lottery_enter_cd_tv);
        welfareLotteryEnterTipsView = findViewById(R.id.plvlc_live_welfare_lottery_enter_tips_view);
        if (welfareLotteryManager != null) {
            welfareLotteryManager.registerView(welfareLotteryEnterView, welfareLotteryCdVTv, welfareLotteryEnterTipsView);
        }

        //互动入口
        interactEntranceLy = findViewById(R.id.plvlc_interact_entrance_ly);
        interactEntranceLy.changeLayoutStyle(true);
        interactEntranceLy.setOnViewActionListener(new PLVInteractEntranceLayout.OnViewActionListener() {
            @Override
            public void onShowQuestionnaire() {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowQuestionnaire();
                }
            }
        });

        chatReplyLayout = findViewById(R.id.plvlc_chat_reply_layout);

        if (getContext() != null && chatOverLengthMessageLayout == null) {
            chatOverLengthMessageLayout = new PLVLCChatOverLengthMessageLayout(getContext());
        }

        addPopupButton(toggleEmojiIv);
        addPopupLayout(emojiLy);

        addPopupButton(toggleMoreIv);
        addPopupLayout(chatMoreLayout);

        acceptFunctionSwitchData(functionSwitchData);
        acceptEmotionImageData(emotionImages);

        observeTransmitChangedEvent();
    }

    private void initChatMoreLayout() {
        chatMoreLayout = findViewById(R.id.plvlc_chat_more_layout);
        if (!isLiveType) {
            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_BULLETIN, false);
            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_ONLY_TEACHER, false);
            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SEND_IMAGE, false);
            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_OPEN_CAMERA, false);
            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT, false);
        }
        chatMoreLayout.setFunctionListener(new PLVLCChatFunctionListener() {
            @Override
            public void onFunctionCallback(String type, String data) {

                switch (type) {
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_ONLY_TEACHER:
                        if (isFocusModeStatus) {
                            ToastUtils.showShort(PLVAppUtils.getString(R.string.plv_chat_toast_focus));
                            return;
                        }
                        isSelectOnlyTeacher = !isSelectOnlyTeacher;
                        PLVChatFunctionVO onlyTeacherFunction = chatMoreLayout.getFunctionByType(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_ONLY_TEACHER);
                        if (onlyTeacherFunction != null) {
                            onlyTeacherFunction.setSelected(isSelectOnlyTeacher);
                            onlyTeacherFunction.setName(isSelectOnlyTeacher ? getString(R.string.plv_chat_view_all_message) : getString(R.string.plv_chat_view_special_message));
                            chatMoreLayout.updateFunctionStatus(onlyTeacherFunction);
                        }
                        if (chatCommonMessageList != null) {
                            chatCommonMessageList.changeDisplayType(isSelectOnlyTeacher ? PLVLCMessageAdapter.DISPLAY_DATA_TYPE_SPECIAL : PLVLCMessageAdapter.DISPLAY_DATA_TYPE_FULL);
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SEND_IMAGE:
                        requestSelectImg();
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_OPEN_CAMERA:
                        requestOpenCamera();
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_BULLETIN:
                        hideSoftInputAndPopupLayout();
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowBulletinAction();
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_LANGUAGE_SWITCH:
                        hideSoftInputAndPopupLayout();
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowLanguageAction();
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_PLAY_SETTING:
                        hideSoftInputAndPopupLayout();
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowPlaySettingAction();
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SCREENSHOT:
                        hideSoftInputAndPopupLayout();
                        if (onViewActionListener != null) {
                            onViewActionListener.onScreenshot();
                        }
                        break;
                    default:
                        hideSoftInputAndPopupLayout();
                        if (onViewActionListener != null) {
                            onViewActionListener.onClickDynamicFunction(data);
                        }
                        break;
                    case PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT:
                        hideSoftInputAndPopupLayout();
                        isSelectCloseEffect = !isSelectCloseEffect;
                        PLVChatFunctionVO effectFunction = chatMoreLayout.getFunctionByType(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT);
                        if (effectFunction != null) {
                            effectFunction.setSelected(isSelectCloseEffect);
                            effectFunction.setName(isSelectCloseEffect ? getString(R.string.plv_chat_view_show_effect) : getString(R.string.plv_chat_view_close_effect));
                            chatMoreLayout.updateFunctionStatus(effectFunction);
                        }
                        if (isSelectCloseEffect) {
                            polyvPointRewardEffectWidget.hideAndReleaseEffect();
                            svgaHelper.clear();
                            rewardSvgImage.setVisibility(View.INVISIBLE);
                        } else {
                            polyvPointRewardEffectWidget.showAndPrepareEffect();
                            rewardSvgImage.setVisibility(View.VISIBLE);
                        }
                        if (onViewActionListener != null) {
                            onViewActionListener.onShowEffectAction(!isSelectCloseEffect);
                        }
                        break;
                }
            }
        });

        if (liveRoomDataManager != null && getContext() != null) {
            liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
                @Override
                public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> plvLiveClassDetailVOPLVStatefulData) {
                    liveRoomDataManager.getClassDetailVO().removeObserver(this);
                    if (plvLiveClassDetailVOPLVStatefulData == null || !plvLiveClassDetailVOPLVStatefulData.isSuccess()) {
                        return;
                    }
                    PLVLiveClassDetailVO liveClassDetail = plvLiveClassDetailVOPLVStatefulData.getData();
                    if (liveClassDetail == null || liveClassDetail.getData() == null) {
                        return;
                    }
                    chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_PLAY_SETTING, liveClassDetail.getData().getGlobalRtcRecordSetting().isFenestrulePlayEnabled());
                    // 聊天室背景图
                    String chatBackgroundUrl = liveClassDetail.getData().getChatBackgroundImage();
                    if (!TextUtils.isEmpty(chatBackgroundUrl)) {
                        PLVImageLoader.getInstance()
                                .loadImage(getContext(), chatBackgroundUrl, chatroomBgIv, liveClassDetail.getData().getChatBackgroundImageOpacity());
                    }
                }
            });
        }
    }

    private void observeTransmitChangedEvent() {
        runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                multiRoomTransmitViewModel.getTransmitLiveData()
                        .observe((LifecycleOwner) getContext(), new Observer<PLVMultiRoomTransmitVO>() {

                            private PLVLCChatTipsLayout.ShowTipsConfiguration watchMainRoomTipsConfiguration = null;

                            @Override
                            public void onChanged(@Nullable PLVMultiRoomTransmitVO multiRoomTransmitVO) {
                                final boolean isWatchMainRoomLastTime = transmitVO != null && transmitVO.isWatchMainRoom();
                                transmitVO = multiRoomTransmitVO;
                                if (multiRoomTransmitVO == null || isWatchMainRoomLastTime == multiRoomTransmitVO.isWatchMainRoom()) {
                                    return;
                                }
                                if (multiRoomTransmitVO.isWatchMainRoom()) {
                                    if (chatTipsLayout != null) {
                                        chatTipsLayout.show(
                                                watchMainRoomTipsConfiguration = new PLVLCChatTipsLayout.ShowTipsConfiguration()
                                                        .setContent(PLVAppUtils.getString(R.string.plv_chat_linked_to_other_rooms))
                                                        .setClosable(true)
                                        );
                                    }
                                    PLVToast.Builder.context(getContext())
                                            .setText(PLVAppUtils.getString(R.string.plv_chat_switched_to_large_room))
                                            .show();
                                } else {
                                    if (chatTipsLayout != null) {
                                        if (chatTipsLayout.getTipsShowingConfiguration() == watchMainRoomTipsConfiguration) {
                                            chatTipsLayout.hide();
                                        }
                                    }
                                    PLVToast.Builder.context(getContext())
                                            .setText(PLVAppUtils.getString(R.string.plv_chat_switched_to_small_room))
                                            .show();
                                }
                            }
                        });
            }
        });

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

    // <editor-fold defaultstate="collapsed" desc="卡片推送">
    private void acceptNewsPushStartMessage(final PLVNewsPushStartEvent newsPushStartEvent) {
        if (cardPushManager != null) {
            cardPushManager.acceptNewsPushStartMessage(chatroomPresenter, newsPushStartEvent);
        }
    }

    private void acceptNewsPushCancelMessage() {
        if (cardPushManager != null) {
            cardPushManager.acceptNewsPushCancelMessage();
        }
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
                    unreadMsgTv.setText(PLVAppUtils.getString(R.string.plv_chat_view_new_msg_3));
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
                updateViewByRoomStatusChanged(isFocusModeStatus);
                if (chatCommonMessageList != null) {
                    if (isFocusModeStatus) {
                        chatCommonMessageList.changeDisplayType(PLVLCMessageAdapter.DISPLAY_DATA_TYPE_FOCUS_MODE);
                    } else {
                        chatCommonMessageList.changeDisplayType(isSelectOnlyTeacher ? PLVLCMessageAdapter.DISPLAY_DATA_TYPE_SPECIAL : PLVLCMessageAdapter.DISPLAY_DATA_TYPE_FULL);
                    }
                }
                if (chatCommonMessageList != null && chatCommonMessageList.isLandscapeLayout()) {
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
                updateViewByRoomStatusChanged(isCloseRoomStatus);
                if (chatCommonMessageList != null && chatCommonMessageList.isLandscapeLayout()) {
                    return;
                }
                ToastUtils.showLong(isCloseRoomStatus ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
            }
        });
    }

    private void updateViewByRoomStatusChanged(boolean isDisabled) {
        boolean isEnabled = !isCloseRoomStatus && !isFocusModeStatus;
        if (isDisabled) {
            hideSoftInputAndPopupLayout();
            if (recordInputMessage == null) {
                recordInputMessage = inputEt.getText();
            }
            inputEt.setText("");
        } else {
            if (recordInputMessage != null && isEnabled) {
                inputEt.setText(recordInputMessage);
                recordInputMessage = null;
            }
        }
        String hint = PLVAppUtils.getString(isCloseRoomStatus ? R.string.plv_chat_input_tips_chatroom_close_2 : (isFocusModeStatus ? R.string.plv_chat_input_tips_focus : R.string.plv_chat_input_tips_chat_2));
        inputEt.setHint(hint);
        inputEt.setEnabled(isEnabled);
        toggleEmojiIv.setEnabled(isEnabled);
        toggleEmojiIv.setAlpha(isEnabled ? 1f : 0.5f);
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
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            acceptSpeakEvent(speakEvent);
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(16);
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
        public void onLoginError(@Nullable PLVLoginEvent loginEvent, final String msg, final int errorCode) {
            super.onLoginError(loginEvent, msg, errorCode);
            postToMainThread(new Runnable() {
                @Override
                public void run() {
                    PLVToast.Builder.create()
                            .setText(format("{}({})", msg, errorCode))
                            .show();
                    final Activity activity = firstNotNull(getActivity(), ActivityUtils.getTopActivity());
                    if (activity != null) {
                        activity.finish();
                    }
                }
            });
        }

        @Override
        public void onRewardEvent(@NonNull PLVRewardEvent rewardEvent) {
            super.onRewardEvent(rewardEvent);
            acceptPointRewardMessage(rewardEvent);
        }

        @Override
        public void onCloseRoomEvent(@NonNull final PLVCloseRoomEvent closeRoomEvent) {
            super.onCloseRoomEvent(closeRoomEvent);
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            acceptCloseRoomEvent(closeRoomEvent);
        }

        @Override
        public void onFocusModeEvent(@NonNull PLVFocusModeEvent focusModeEvent) {
            super.onFocusModeEvent(focusModeEvent);
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            acceptFocusModeEvent(focusModeEvent);
        }

        @Override
        public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            removeChatMessageToList(id, isRemoveAll);
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            if (localMessage == null) {
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
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
        public void onLoadEmotionMessage(@Nullable PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            if (emotionEvent == null) {
                return;
            }
            final List<PLVBaseViewData> dataList = new ArrayList<>();
            dataList.add(new PLVBaseViewData(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
                //添加信息至列表
            addChatMessageToList(dataList, emotionEvent.isLocal());
        }

        @Override
        public void onNewsPushStartMessage(@NonNull PLVNewsPushStartEvent newsPushStartEvent) {
            super.onNewsPushStartMessage(newsPushStartEvent);
            acceptNewsPushStartMessage(newsPushStartEvent);
        }

        @Override
        public void onNewsPushCancelMessage() {
            super.onNewsPushCancelMessage();
            acceptNewsPushCancelMessage();
        }

        @Override
        public void onLocalImageMessage(@Nullable PolyvSendLocalImgEvent localImgEvent) {
            super.onLocalImageMessage(localImgEvent);
            if (isChatPlaybackLayout || !isLiveType) {
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
            if (isChatPlaybackLayout || !isLiveType) {
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
            if (isChatPlaybackLayout) {
                return;
            }
            if (swipeLoadView != null) {
                swipeLoadView.setRefreshing(false);
                swipeLoadView.setEnabled(true);
            }
            if (chatroomPresenter != null && viewIndex == chatroomPresenter.getViewIndex(chatroomView)) {
                ToastUtils.showShort(getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
            }
        }

        @Override
        public void onCheckMessageMaxLength(int maxLength) {
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            if (chatCommonMessageList != null) {
                chatCommonMessageList.removeChatMessage(maxLength, false);
            }
        }
    };

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 欢迎语、公告处理">
    private void acceptLoginEvent(final PLVLoginEvent loginEvent) {
        //非聊天回放布局并且非直播类型则不显示欢迎语
        if (!isChatPlaybackLayout && !isLiveType) {
            return;
        }
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

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 积分打赏动画特效">

    /**
     * 初始化积分打赏动画特效item
     */
    private void initPointRewardEffectQueue() {
        if (pointRewardEventProducer == null) {
            pointRewardEventProducer = new PLVPointRewardEffectQueue();
            if (polyvPointRewardEffectWidget != null) {
                polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);
            }
        }
    }

    /**
     * 销毁积分打赏动效队列
     */
    private void destroyPointRewardEffectQueue() {
        if (pointRewardEventProducer != null) {
            pointRewardEventProducer.destroy();
        }
    }

    private void acceptPointRewardMessage(final PLVRewardEvent rewardEvent) {
        if (pointRewardEventProducer != null) {
            //横屏 ｜ 屏蔽特效  不处理积分打赏事件
            if (ScreenUtils.isPortrait() && !isSelectCloseEffect) {
                //添加到队列后，自动加载动画特效
//                pointRewardEventProducer.addEvent(rewardEvent);
                //添加到svga
//                svgaHelper.addEvent(rewardEvent);
            }
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 列表数据更新">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (chatCommonMessageList != null) {
                    chatCommonMessageList.addChatMessageToList(chatMessageDataList, isScrollEnd, false);
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
            chatCommonMessageList.addChatHistoryToList(chatMessageDataList, isScrollEnd, false);
        }
    }

    private void addChatMessageToListHead(final List<PLVBaseViewData> chatMessageDataList) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.addChatMessageToListHead(chatMessageDataList, false, false);
        }
    }

    private void removeChatMessageToList(int startPosition, int count) {
        if (chatCommonMessageList != null) {
            chatCommonMessageList.removeChatMessage(startPosition, count, false);
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
                    chatCommonMessageList.removeAllChatMessage(false);
                } else {
                    chatCommonMessageList.removeChatMessage(id, false);
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

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 发送聊天信息">
    @Nullable
    public PLVChatQuoteVO getChatQuoteContent() {
        if (chatReplyLayout == null) {
            return null;
        }
        return chatReplyLayout.getChatQuoteContent();
    }

    public void onCloseChatQuote() {
        if (chatReplyLayout == null) {
            return;
        }
        chatReplyLayout.setChatQuoteContent(null);
    }

    private boolean sendChatMessage(String message) {
        if (message.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            return false;
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
            if (isChatPlaybackLayout || !isLiveType) {
                return false;
            }
            if (chatroomPresenter == null) {
                return false;
            }
            Pair<Boolean, Integer> sendResult;
            if (chatReplyLayout.getChatQuoteContent() == null || chatReplyLayout.getChatQuoteContent().getMessageId() == null) {
                sendResult = chatroomPresenter.sendChatMessage(localMessage);
            } else {
                localMessage.setQuote(chatReplyLayout.getChatQuoteContent());
                sendResult = chatroomPresenter.sendQuoteMessage(localMessage, chatReplyLayout.getChatQuoteContent().getMessageId());
            }
            if (sendResult.first) {
                //清空输入框内容并隐藏键盘/弹出的表情布局等
                inputEt.setText("");
                chatReplyLayout.setChatQuoteContent(null);
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
    private boolean checkCanSendImg() {
        if (isCloseRoomStatus) {
            ToastUtils.showShort(PLVAppUtils.getString(R.string.plv_chat_toast_chatroom_close_2));
            return false;
        } else if (isFocusModeStatus) {
            ToastUtils.showShort(PLVAppUtils.getString(R.string.plv_chat_toast_focus));
            return false;
        }
        return true;
    }

    private void requestSelectImg() {
        if (!checkCanSendImg()) {
            return;
        }
        PLVStoragePermissionCompat
                .start((Activity) getContext(), new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        selectImg();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        if (!deniedForeverP.isEmpty()) {
                            showRequestPermissionDialog(PLVAppUtils.getString(R.string.plv_chat_send_img_error_tip_permission_denied), true);
                        } else {
                            ToastUtils.showShort(R.string.plv_chat_send_img_error_tip_permission_cancel);
                        }
                    }
                });
    }

    private void selectImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, PLVAppUtils.getString(R.string.plv_chat_chooser_sel_img)), REQUEST_SELECT_IMG);
    }

    private void requestOpenCamera() {
        if (!checkCanSendImg()) {
            return;
        }
        PLVStoragePermissionCompat
                .start((Activity) getContext(), new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        requestCameraPermission();
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                        if (!deniedForeverP.isEmpty()) {
                            showRequestPermissionDialog(PLVAppUtils.getString(R.string.plv_chat_open_camera_error_tip_permission_denied), true);
                        } else {
                            ToastUtils.showShort(R.string.plv_chat_open_camera_error_tip_permission_cancel);
                        }
                    }
                });
    }

    private void requestCameraPermission() {
        ArrayList<String> permissions = new ArrayList<>(1);
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
                            showRequestPermissionDialog(PLVAppUtils.getString(R.string.plv_chat_open_camera_error_tip_permission_denied), false);
                        } else {
                            ToastUtils.showShort(PLVAppUtils.getString(R.string.plv_chat_open_camera_error_tip_permission_cancel));
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

    private void showRequestPermissionDialog(String message, final boolean isOnyStoragePermission) {
        new AlertDialog.Builder(getContext()).setTitle(PLVAppUtils.getString(R.string.plv_common_dialog_tip))
                .setMessage(message)
                .setPositiveButton(PLVAppUtils.getString(R.string.plv_common_dialog_confirm_2), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isOnyStoragePermission) {
                            PLVStoragePermissionCompat.jump2Settings(getContext());
                        } else {
                            PLVFastPermission.getInstance().jump2Settings(getContext());
                        }
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
        return isSelectOnlyTeacher;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 更多 - 更新消息状态">

    /**
     * 更新消息记录状态
     */
    public void updateInteractStatus(boolean isShow, boolean hasNew) {
        chatMoreLayout.updateFunctionNew(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_MESSAGE, isShow, hasNew);
    }

    public void updateChatMoreFunction(PLVWebviewUpdateAppStatusVO functionsVO) {
        if (chatMoreLayout != null) {
            chatMoreLayout.updateFunctionView(functionsVO);
        }
    }


    /**
     * 更新特效开关
     */
    public void updateRewardEffectBtnVisibility(boolean isShow) {
//        chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_EFFECT, isShow);
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 功能开关处理">
    public void acceptFunctionSwitchData(List<PolyvChatFunctionSwitchVO.DataBean> dataBeans) {
        this.functionSwitchData = dataBeans;
        if (view != null && dataBeans != null) {
            for (PolyvChatFunctionSwitchVO.DataBean dataBean : dataBeans) {
                boolean isSwitchEnabled = dataBean.isEnabled();
                switch (dataBean.getType()) {
                    //观众发送图片开关
                    case PolyvChatFunctionSwitchVO.TYPE_VIEWER_SEND_IMG_ENABLED:
                        if (isLiveType) {
                            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_SEND_IMAGE, isSwitchEnabled);
                            chatMoreLayout.updateFunctionShow(PLVLCChatMoreLayout.CHAT_FUNCTION_TYPE_OPEN_CAMERA, isSwitchEnabled);
                        }
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
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 表情图片处理">
    public void acceptEmotionImageData(List<PLVEmotionImageVO.EmotionImage> emotionImages) {
        this.emotionImages = emotionImages;
        //获取到图片表情，初始化
        if (view != null && emotionImages != null && !emotionImages.isEmpty()) {
            PLVChatroomUtils.initEmojiPersonalList(emojiPersonalRv, 5, emotionImages, new PLVLCEmotionPersonalListAdapter.OnViewActionListener() {
                @Override
                public void onEmotionViewClick(PLVEmotionImageVO.EmotionImage emotionImage) {
                    if (isChatPlaybackLayout || !isLiveType) {
                        return;
                    }
                    if (chatroomPresenter != null) {
                        Pair<Boolean, Integer> sendResult = chatroomPresenter.sendChatEmotionImage(new PLVChatEmotionEvent(emotionImage.getId()));

                        if (!sendResult.first) {
                            //发送失败
                            ToastUtils.showShort(getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second);
                        } else {
                            //发送成功
                            hideSoftInputAndPopupLayout();
                        }
                    }
                }

                @Override
                public void onEmotionViewLongClick(PLVEmotionImageVO.EmotionImage emotionImage, View view) {
                    emotionPreviewWindow.showInTopCenter(emotionImage.getUrl(), view);
                }
            });
        }
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
        String likesString = StringUtils.toKString(likesCount);
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

    // <editor-folder defaultstate="collapsed" desc="互动 - 互动入口数据处理">
    public void acceptInteractEntranceData(List<PLVCallAppEvent.ValueBean.DataBean> dataBeans) {
        if (interactEntranceLy != null) {
            interactEntranceLy.acceptInteractEntranceData(dataBeans);
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="切换表情TAB">
    private void changeEmojiTab(boolean isEmoji) {
        tabEmojiIv.setSelected(isEmoji);
        tabPersonalIv.setSelected(isEmoji);
        int selectColor = Color.parseColor("#FF2B2C35");
        int unSelectColor = Color.parseColor("#FF202127");
        tabEmojiIv.setBackgroundColor(isEmoji ? selectColor : unSelectColor);
        tabPersonalIv.setBackgroundColor(isEmoji ? unSelectColor : selectColor);
        //切换rv的表情库
        if (isEmoji) {
            //显示emoji表情库
            emojiRv.setVisibility(View.VISIBLE);
            sendMsgTv.setVisibility(View.VISIBLE);
            deleteMsgIv.setVisibility(View.VISIBLE);
            emojiPersonalRv.setVisibility(View.INVISIBLE);
        } else {
            //显示个性表情包
            emojiRv.setVisibility(View.INVISIBLE);
            sendMsgTv.setVisibility(View.INVISIBLE);
            deleteMsgIv.setVisibility(View.INVISIBLE);
            emojiPersonalRv.setVisibility(View.VISIBLE);
        }
    }
    // </editor-fold >

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
                if (!isChatPlaybackLayout) {
                    //处理播放页面初始竖屏，然后在竖屏聊天室没加载完成前切换到横屏的情况，之后等竖屏聊天室加载完成后再切换竖屏，这时需要加载历史记录
                    if (chatroomPresenter.getChatHistoryTime() == 0) {
                        chatroomPresenter.requestChatHistory(chatroomPresenter.getViewIndex(chatroomView));
                    }
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.toggle_emoji_iv) {
            togglePopupLayout(toggleEmojiIv, emojiLy);
        } else if (id == R.id.toggle_more_iv) {
            togglePopupLayout(toggleMoreIv, chatMoreLayout);
        } else if (id == R.id.delete_msg_iv) {
            PLVChatroomUtils.deleteEmoText(inputEt);
        } else if (id == R.id.send_msg_tv) {
            sendChatMessage(inputEt.getText().toString());
        } else if (id == R.id.likes_view) {
            if (chatroomPresenter != null) {
                chatroomPresenter.sendLikeMessage();
            }
            acceptLikesMessage(1);
        } else if (id == R.id.plvlc_emoji_tab_emoji_iv) {
            changeEmojiTab(true);
        } else if (id == R.id.plvlc_emoji_tab_personal_iv) {
            changeEmojiTab(false);
        } else if (id == R.id.plvlc_iv_show_point_reward) {
            if (onViewActionListener != null) {
                onViewActionListener.onShowRewardAction();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互监听器">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        /**
         * 显示公告
         */
        void onShowBulletinAction();

        /**
         * 显示语言切换弹窗
         */
        void onShowLanguageAction();

        /**
         * 显示播放设置弹窗
         */
        void onShowPlaySettingAction();

        /**
         * 截屏
         */
        void onScreenshot();

        /**
         * 显示积分打赏弹窗
         */
        void onShowRewardAction();

        /**
         * 显示特效
         */
        void onShowEffectAction(boolean isShow);

        /**
         * 显示问卷
         */
        void onShowQuestionnaire();

        /**
         * 点击了动态功能控件
         *
         * @param event 动态功能的event data
         */
        void onClickDynamicFunction(String event);

        void onReplyMessage(PLVChatQuoteVO chatQuoteVO);

        /**
         * 回调 拆开红包
         */
        void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent);
    }
    // </editor-fold>
}
