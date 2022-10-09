package com.easefun.polyv.liveecommerce.scenes.fragments;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.IPLVPointRewardEventProducer;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectQueue;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectWidget;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVRewardSVGAHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatImgScanPopupView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatInputWindow;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECGreetingView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECLikeIconView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityAdapter;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityDetailActivity;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPopupView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPushLayout;
import com.easefun.polyv.liveecommerce.modules.player.widget.PLVECNetworkTipsView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.custom.PolyvCustomEvent;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.plv.livescenes.model.commodity.saas.PLVCommodityVO2;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatEmotionEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.commodity.PLVProductControlEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.commodity.PLVProductMoveEvent;
import com.plv.socket.event.commodity.PLVProductRemoveEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 直播首页：主持人信息、聊天室、点赞、更多、商品、打赏
 */
public class PLVECLiveHomeFragment extends PLVECCommonHomeFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //观看信息布局
    private PLVECWatchInfoView watchInfoLy;
    //公告布局
    private PLVECBulletinView bulletinLy;
    //欢迎语
    private PLVECGreetingView greetLy;
    //聊天区域
    private PLVMessageRecyclerView chatMsgRv;
    private PLVECChatMessageAdapter chatMessageAdapter;
    private TextView sendMsgTv;
    private PLVECChatImgScanPopupView chatImgScanPopupView;
    //未读信息提醒view
    private TextView unreadMsgTv;
    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;
    //点赞区域
    private PLVECLikeIconView likeBt;
    private TextView likeCountTv;
    //更多
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;
    private int currentLinesPos;
    private int currentDefinitionPos;
    private Rect videoViewRect;
    //商品
    private ImageView commodityIv;
    private PLVECCommodityPopupView commodityPopupView;
    private boolean isOpenCommodityMenu;
    private PLVECCommodityPushLayout commodityPushLayout;
    private String lastJumpBuyCommodityLink;
    //打赏
    private ImageView rewardIv;

    //是否打开了积分打赏
    private boolean isOpenPointReward = false;
    //积分打赏事件队列
    private IPLVPointRewardEventProducer pointRewardEventProducer;
    //积分打赏动画item
    private PLVPointRewardEffectWidget polyvPointRewardEffectWidget;
    //积分打赏svg动画
    private SVGAImageView rewardSvgImage;
    private SVGAParser svgaParser;
    private PLVRewardSVGAHelper svgaHelper;

    // 网络较差提示
    private PLVECNetworkTipsView networkTipsView;
    //监听器
    private OnViewActionListener onViewActionListener;

    //聊天室开关状态
    private boolean isCloseRoomStatus;
    //专注模式状态
    private boolean isFocusModeStatus;
    //聊天输入窗口
    private PLVECChatInputWindow chatInputWindow;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_live_page_home_fragment, null);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
        calculateLiveVideoViewRect();
        startLikeAnimationTask(5000);

        //订阅是否打开积分打赏
        observeRewardData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isOpenPointReward) {
            destroyPointRewardEffectQueue();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        bulletinLy = findViewById(R.id.bulletin_ly);
        greetLy = findViewById(R.id.greet_ly);
        chatMsgRv = findViewById(R.id.chat_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setOnViewActionListener(onChatMsgViewActionListener);
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
                chatroomPresenter.requestChatHistory(0);
            }
        });
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        likeBt = findViewById(R.id.like_bt);
        likeBt.setOnButtonClickListener(this);
        likeCountTv = findViewById(R.id.like_count_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);
        commodityIv = findViewById(R.id.commodity_iv);
        commodityIv.setOnClickListener(this);
        commodityPushLayout = findViewById(R.id.commodity_push_ly);
        rewardIv = findViewById(R.id.reward_iv);
        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
        rewardIv.setOnClickListener(this);
        morePopupView = new PLVECMorePopupView();
        commodityPopupView = new PLVECCommodityPopupView();
        chatImgScanPopupView = new PLVECChatImgScanPopupView();

        //打赏动画特效
        polyvPointRewardEffectWidget = findViewById(R.id.plvec_point_reward_effect);
        rewardSvgImage = findViewById(R.id.plvec_reward_svg);
        svgaParser = new SVGAParser(getContext());
        svgaHelper = new PLVRewardSVGAHelper();
        svgaHelper.init(rewardSvgImage, svgaParser);

        networkTipsView = findViewById(R.id.plvec_live_network_tips_layout);

        //卡片推送
        cardPushManager.registerView((ImageView) findViewById(R.id.card_enter_view), (TextView) findViewById(R.id.card_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.card_enter_tips_view));

        initNetworkTipsLayout();
    }

    private void initNetworkTipsLayout() {
        networkTipsView.setOnViewActionListener(new PLVECNetworkTipsView.OnViewActionListener() {
            @Override
            public void onClickChangeNormalLatency() {
                if (onViewActionListener != null) {
                    onViewActionListener.switchLowLatencyMode(false);
                }
            }

            @Override
            public boolean isCurrentLowLatency() {
                if (onViewActionListener != null) {
                    return onViewActionListener.isCurrentLowLatencyMode();
                }
                return false;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    @Override
    protected void registerChatroomView() {
        chatroomPresenter.registerView(chatroomView);
    }

    @Override
    protected void updateWatchInfo(String coverImage, String publisher) {
        watchInfoLy.updateWatchInfo(coverImage, publisher);
        watchInfoLy.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateWatchCount(long watchCount) {
        watchInfoLy.updateWatchCount(watchCount);
    }

    @Override
    protected void updateLikesInfo(String likesString) {
        likeCountTv.setText(likesString);
    }

    @Override
    protected void acceptOpenCommodity() {
        isOpenCommodityMenu = true;
        commodityIv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void acceptCommodityVO(PLVCommodityVO2 commodityVO, boolean isAddOrSet) {
        if (isAddOrSet) {
            commodityPopupView.addCommodityVO(commodityVO);
        } else {
            commodityPopupView.setCommodityVO(commodityVO);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //设置播放状态
    @Override
    public void setPlayerState(PLVPlayerState state) {
        if (state == PLVPlayerState.PREPARED) {
            morePopupView.updatePlayStateView(View.VISIBLE);

            if (onViewActionListener != null) {
                currentLinesPos = onViewActionListener.onGetLinesPosAction();
                currentDefinitionPos = onViewActionListener.onGetDefinitionAction();
                morePopupView.updatePlayMode(onViewActionListener.onGetMediaPlayModeAction());
            }
            morePopupView.updateLinesView(new int[]{onViewActionListener == null ? 1 : onViewActionListener.onGetLinesCountAction(), currentLinesPos});
            morePopupView.updateDefinitionView(onViewActionListener == null ?
                    new Pair<List<PolyvDefinitionVO>, Integer>(null, 0) :
                    onViewActionListener.onShowDefinitionClick(view));
        } else if (state == PLVPlayerState.NO_LIVE || state == PLVPlayerState.LIVE_END) {
            morePopupView.hideAll();
            morePopupView.updatePlayStateView(View.GONE);
        }
    }

    @Override
    public void jumpBuyCommodity() {
        if (TextUtils.isEmpty(lastJumpBuyCommodityLink)) {
            return;
        }
        commodityPushLayout.hide();
        commodityPopupView.hide();
        //默认用当前应用的一个webView页面打开后端填写的链接，另外也可以根据后端填写的信息自行调整需要的操作
        PLVECCommodityDetailActivity.start(getContext(), lastJumpBuyCommodityLink);
    }

    @Override
    public void setOnViewActionListener(PLVECCommonHomeFragment.OnViewActionListener listener) {
        this.onViewActionListener = (OnViewActionListener) listener;
    }

    @Override
    public void acceptOnLowLatencyChange(boolean isLowLatency) {
        morePopupView.updateLatencyMode(isLowLatency);
    }

    @Override
    public void acceptNetworkQuality(int quality) {
        networkTipsView.acceptNetworkQuality(quality);
    }

    @Override
    public void showMorePopupWindow() {
        if (moreIv != null) {
            moreIv.performClick();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 打赏动画控制">
    /**
     * 初始化积分打赏动画特效item
     */
    private void initPointRewardEffectQueue(){
        if(pointRewardEventProducer == null) {
            pointRewardEventProducer = new PLVPointRewardEffectQueue();
            polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);
        }
    }

    /**
     * 销毁积分打赏动效队列
     */
    private void destroyPointRewardEffectQueue(){
        if (pointRewardEventProducer != null) {
            pointRewardEventProducer.destroy();
        }
        svgaHelper.clear();
    }

    private void acceptPointRewardMessage(PLVRewardEvent rewardEvent){
        if (pointRewardEventProducer != null) {
            //横屏不处理积分打赏事件
            if (ScreenUtils.isPortrait()) {
                //添加到队列后，自动加载动画特效
//                pointRewardEventProducer.addEvent(rewardEvent);
                //添加到svga
//                svgaHelper.addEvent(rewardEvent);
            }
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 公告控制">
    private void acceptBulletinMessage(final PolyvBulletinVO bulletinVO) {
        bulletinLy.acceptBulletinMessage(bulletinVO);
    }

    private void removeBulletin() {
        bulletinLy.removeBulletin();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 欢迎语控制">
    private void acceptLoginMessage(PLVLoginEvent loginEvent) {
        //显示欢迎语
        greetLy.acceptGreetingMessage(loginEvent);
    }
    // </editor-fold>

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

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 专注模式">
    private void acceptFocusModeEvent(final PLVFocusModeEvent focusModeEvent) {
        isFocusModeStatus = focusModeEvent.isOpen();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                updateViewByRoomStatusChanged(isFocusModeStatus);
                if (isFocusModeStatus) {
                    chatMessageAdapter.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FOCUS_MODE);
                } else {
                    chatMessageAdapter.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FULL);
                }
                ToastUtils.showLong(isFocusModeStatus ? R.string.plv_chat_toast_focus_mode_open : R.string.plv_chat_toast_focus_mode_close);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 房间开关">
    private void acceptCloseRoomEvent(PLVCloseRoomEvent closeRoomEvent) {
        isCloseRoomStatus = closeRoomEvent.getValue().isClosed();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                updateViewByRoomStatusChanged(isCloseRoomStatus);
                ToastUtils.showLong(isCloseRoomStatus ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
            }
        });
    }

    private void updateViewByRoomStatusChanged(boolean isDisabled) {
        boolean isEnabled = !isCloseRoomStatus && !isFocusModeStatus;
        if (isDisabled) {
            if (chatInputWindow != null) {
                chatInputWindow.requestClose();
            }
        }
        sendMsgTv.setText(isCloseRoomStatus ? "聊天室已关闭" : (isFocusModeStatus ? "当前为专注模式.." : "跟大家聊点什么吧~"));
        sendMsgTv.setOnClickListener(isEnabled ? this : null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void onSpeakEvent(@NonNull PLVSpeakEvent speakEvent) {
            super.onSpeakEvent(speakEvent);
        }

        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);//聊天列表里的文本信息textSize
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
            acceptLoginMessage(loginEvent);
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
        public void onLogoutEvent(@NonNull PLVLogoutEvent logoutEvent) {
            super.onLogoutEvent(logoutEvent);
        }

        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
            acceptBulletinMessage(bulletinVO);
        }

        @Override
        public void onRewardEvent(@NonNull PLVRewardEvent rewardEvent) {
            super.onRewardEvent(rewardEvent);
            acceptPointRewardMessage(rewardEvent);
        }

        @Override
        public void onRemoveBulletinEvent() {
            super.onRemoveBulletinEvent();
            removeBulletin();
        }

        @Override
        public void onProductControlEvent(@NonNull PLVProductControlEvent productControlEvent) {
            super.onProductControlEvent(productControlEvent);
            acceptProductControlEvent(productControlEvent);
        }

        @Override
        public void onProductRemoveEvent(@NonNull PLVProductRemoveEvent productRemoveEvent) {
            super.onProductRemoveEvent(productRemoveEvent);
            acceptProductRemoveEvent(productRemoveEvent);
        }

        @Override
        public void onProductMoveEvent(@NonNull PLVProductMoveEvent productMoveEvent) {
            super.onProductMoveEvent(productMoveEvent);
            acceptProductMoveEvent(productMoveEvent);
        }

        @Override
        public void onProductMenuSwitchEvent(@NonNull PLVProductMenuSwitchEvent productMenuSwitchEvent) {
            super.onProductMenuSwitchEvent(productMenuSwitchEvent);

            /** ///暂时保留，主要是商品库开关
             *   if (productMenuSwitchEvent.getContent() != null) {
             *  boolean isEnabled = productMenuSwitchEvent.getContent().isEnabled();
             *   }
             */
        }

        @Override
        public void onCloseRoomEvent(@NonNull final PLVCloseRoomEvent closeRoomEvent) {
            super.onCloseRoomEvent(closeRoomEvent);
            acceptCloseRoomEvent(closeRoomEvent);
        }

        @Override
        public void onFocusModeEvent(@NonNull PLVFocusModeEvent focusModeEvent) {
            super.onFocusModeEvent(focusModeEvent);
            acceptFocusModeEvent(focusModeEvent);
        }

        @Override
        public void onRemoveMessageEvent(@Nullable String id, boolean isRemoveAll) {
            super.onRemoveMessageEvent(id, isRemoveAll);
            removeChatMessageToList(id, isRemoveAll);
        }

        @Override
        public void onCustomGiftEvent(@NonNull PolyvCustomEvent.UserBean userBean, @NonNull PLVCustomGiftBean customGiftBean) {
            //自定义礼物消息已移除，统一通过onRewardEvent实现打赏
        }

        @Override
        public void onLocalSpeakMessage(@Nullable PolyvLocalMessage localMessage) {
            super.onLocalSpeakMessage(localMessage);
            if (localMessage != null) {
                //添加信息至列表
                List<PLVBaseViewData> dataList = new ArrayList<>();
                dataList.add(new PLVBaseViewData<>(localMessage, PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK, new PLVSpecialTypeTag(localMessage.getUserId())));
                addChatMessageToList(dataList, true);
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
            ToastUtils.showShort(getString(R.string.plv_chat_toast_history_load_failed) + ": " + errorMsg);
        }

        @Override
        public void onLoadEmotionMessage(@Nullable PLVChatEmotionEvent emotionEvent) {
            super.onLoadEmotionMessage(emotionEvent);
            if (emotionEvent != null) {
                //添加信息至列表
                List<PLVBaseViewData> dataList = new ArrayList<>();
                dataList.add(new PLVBaseViewData<>(emotionEvent, PLVChatMessageItemType.ITEMTYPE_EMOTION, emotionEvent.isSpecialTypeOrMe() ? new PLVSpecialTypeTag(emotionEvent.getUserId()) : null));
                addChatMessageToList(dataList, emotionEvent.isLocal());
            }
        }

        @Override
        public void onNewsPushStartMessage(@NonNull PLVNewsPushStartEvent newsPushStartEvent) {
            super.onNewsPushStartMessage(newsPushStartEvent);
            cardPushManager.acceptNewsPushStartMessage(chatroomPresenter, newsPushStartEvent);
        }

        @Override
        public void onNewsPushCancelMessage() {
            super.onNewsPushCancelMessage();
            cardPushManager.acceptNewsPushCancelMessage();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 图片信息点击事件处理">
    PLVECChatMessageAdapter.OnViewActionListener onChatMsgViewActionListener = new PLVECChatMessageAdapter.OnViewActionListener() {
        @Override
        public void onChatImgClick(View view, String imgUrl) {
            chatImgScanPopupView.showImgScanLayout(view, imgUrl);
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 输入窗口显示，信息发送">
    private void showInputWindow() {
        PLVECChatInputWindow.show(getActivity(), PLVECChatInputWindow.class, new PLVECChatInputWindow.MessageSendListener() {
            @Override
            public void onInputContext(PLVECChatInputWindow inputWindow) {
                chatInputWindow = inputWindow;
            }

            @Override
            public boolean onSendMsg(String message) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                Pair<Boolean, Integer> sendResult = chatroomPresenter.sendChatMessage(localMessage);
                if (!sendResult.first) {
                    //发送失败
                    ToastUtils.showShort(getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second);
                    return false;
                }
                return true;
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 点赞数据处理，定时显示飘心任务">
    private void acceptLikesMessage(final int likesCount) {
        handler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                startAddLoveIconTask(200, Math.min(5, likesCount));
            }
        });
    }

    private void startLikeAnimationTask(long ts) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int randomLikeCount = new Random().nextInt(5) + 1;
                startAddLoveIconTask(200, randomLikeCount);
                startLikeAnimationTask((new Random().nextInt(6) + 5) * 1000L);
            }
        }, ts);
    }

    private void startAddLoveIconTask(final long ts, final int count) {
        if (count >= 1) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    likeBt.addLoveIcon(1);
                    startAddLoveIconTask(ts, count - 1);
                }
            }, ts);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="商品 - 布局显示、事件处理、推送显示、商品链接跳转等">
    private void showCommodityLayout(View v) {
        //清空旧数据
        commodityPopupView.setCommodityVO(null);
        //每次弹出都调用一次接口获取商品信息
        liveRoomDataManager.requestProductList();
        commodityPopupView.showCommodityLayout(v, new PLVECCommodityAdapter.OnViewActionListener() {
            @Override
            public void onBuyCommodityClick(View view, PLVProductContentBean contentsBean) {
                acceptBuyCommodityClick(contentsBean);
            }

            @Override
            public void onLoadMoreData(int rank) {
                liveRoomDataManager.requestProductList(rank);
            }
        });
    }

    private void acceptProductControlEvent(final PLVProductControlEvent productControlEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                final PLVProductContentBean contentBean = productControlEvent.getContent();
                if (productControlEvent.getContent() == null) {
                    return;
                }
                if (productControlEvent.isPush()) {//商品推送
                    commodityPushLayout.setViewActionListener(new PLVECCommodityPushLayout.ViewActionListener() {
                        @Override
                        public void onEnterClick() {
                            acceptBuyCommodityClick(contentBean);
                        }
                    });
                    commodityPushLayout.updateView(contentBean);
                    commodityPushLayout.show();
                } else if (productControlEvent.isNewly()) {//新增
                    commodityPopupView.add(contentBean, true);
                } else if (productControlEvent.isRedact()) {//编辑
                    commodityPopupView.update(contentBean);
                } else if (productControlEvent.isPutOnShelves()) {//上架
                    commodityPopupView.add(contentBean, false);
                }
            }
        });
    }

    private void acceptProductRemoveEvent(final PLVProductRemoveEvent productRemoveEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (productRemoveEvent.getContent() != null) {
                    commodityPopupView.delete(productRemoveEvent.getContent().getProductId());//删除/下架
                    if (commodityPushLayout.isShown() && commodityPushLayout.getProductId() == productRemoveEvent.getContent().getProductId()) {
                        commodityPushLayout.hide();
                    }
                }
            }
        });
    }

    private void acceptProductMoveEvent(final PLVProductMoveEvent productMoveEvent) {
        if (!isOpenCommodityMenu) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                commodityPopupView.move(productMoveEvent);//移动
            }
        });
    }

    private void acceptBuyCommodityClick(PLVProductContentBean contentBean) {
        final String link = contentBean.getLinkByType();
        if (TextUtils.isEmpty(link)) {
            ToastUtils.showShort(R.string.plv_commodity_toast_empty_link);
            return;
        }
        lastJumpBuyCommodityLink = link;
        jumpBuyCommodity();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更多弹窗 - 布局显示及交互处理">
    private void showMorePopupWindow(View v) {
        boolean isCurrentVideoMode = onViewActionListener == null || onViewActionListener.onGetMediaPlayModeAction() == PolyvMediaPlayMode.MODE_VIDEO;
        morePopupView.showLiveMoreLayout(v, isCurrentVideoMode, new PLVECMorePopupView.OnLiveMoreClickListener() {
            @Override
            public boolean onPlayModeClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeMediaPlayModeClick(view, view.isSelected() ? PolyvMediaPlayMode.MODE_VIDEO : PolyvMediaPlayMode.MODE_AUDIO);
                    return true;
                }
                return false;
            }

            @Override
            public int[] onShowLinesClick(View view) {
                return new int[]{onViewActionListener == null ? 1 : onViewActionListener.onGetLinesCountAction(), currentLinesPos};
            }

            @Override
            public void onLinesChangeClick(View view, int linesPos) {
                if (currentLinesPos != linesPos) {
                    currentLinesPos = linesPos;
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeLinesClick(view, linesPos);
                    }
                }
            }

            @Override
            public Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view) {
                return onViewActionListener == null ? new Pair<List<PolyvDefinitionVO>, Integer>(null, 0)
                        : onViewActionListener.onShowDefinitionClick(view);
            }

            @Override
            public void onDefinitionChangeClick(View view, int definitionPos) {
                if (currentDefinitionPos != definitionPos) {
                    currentDefinitionPos = definitionPos;
                    if (onViewActionListener != null) {
                        onViewActionListener.onDefinitionChangeClick(view, definitionPos);
                    }
                }
            }

            @Override
            public boolean isCurrentLowLatencyMode() {
                if (onViewActionListener != null) {
                    return onViewActionListener.isCurrentLowLatencyMode();
                }
                return false;
            }

            @Override
            public void switchLowLatencyMode(boolean isLowLatency) {
                if (onViewActionListener != null) {
                    onViewActionListener.switchLowLatencyMode(isLowLatency);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放器 - 计算直播播放器横屏视频、音频模式的播放器区域位置">
    private void calculateLiveVideoViewRect() {
        watchInfoLy.post(new Runnable() {
            @Override
            public void run() {
                acceptVideoViewRectParams(watchInfoLy.getBottom(), 0);
            }
        });
        greetLy.post(new Runnable() {
            @Override
            public void run() {
                acceptVideoViewRectParams(0, greetLy.getTop());
            }
        });
    }

    private void acceptVideoViewRectParams(int top, int bottom) {
        if (videoViewRect == null) {
            videoViewRect = new Rect(0, top, 0, bottom);
        } else {
            videoViewRect = new Rect(0, Math.max(videoViewRect.top, top), 0, Math.max(videoViewRect.bottom, bottom));
            if (onViewActionListener != null) {
                onViewActionListener.onSetVideoViewRectAction(videoViewRect);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 是否打开积分打赏">
    private void observeRewardData(){
        liveRoomDataManager.getPointRewardEnableData().observe(this, new Observer<PLVStatefulData<Boolean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<Boolean> booleanPLVStatefulData) {
                liveRoomDataManager.getPointRewardEnableData().removeObserver(this);
                if( booleanPLVStatefulData != null && booleanPLVStatefulData.getData() != null){
                    isOpenPointReward = booleanPLVStatefulData.getData();
                    if(isOpenPointReward) {
                        initPointRewardEffectQueue();
                    }
                    if (rewardIv != null) {
                        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
                    }
                }
            }
        });
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send_msg_tv) {
            showInputWindow();
        } else if (id == R.id.more_iv) {
            showMorePopupWindow(v);
        } else if (id == R.id.like_bt) {
            chatroomPresenter.sendLikeMessage();
            acceptLikesMessage(1);
        } else if (id == R.id.commodity_iv) {
            showCommodityLayout(v);
        } else if (id == R.id.reward_iv) {
            if(isOpenPointReward){
                //回调显示积分打赏弹窗
                if(onViewActionListener != null){
                    onViewActionListener.onShowRewardAction();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //切换播放模式
        void onChangeMediaPlayModeClick(View view, int mediaPlayMode);

        //切换线路
        void onChangeLinesClick(View view, int linesPos);

        //[清晰度信息，清晰度索引]
        Pair<List<PolyvDefinitionVO>, Integer> onShowDefinitionClick(View view);

        //切换清晰度
        void onDefinitionChangeClick(View view, int definitionPos);

        //获取播放模式
        int onGetMediaPlayModeAction();

        //获取线路数
        int onGetLinesCountAction();

        //获取线路索引
        int onGetLinesPosAction();

        //获取清晰度索引
        int onGetDefinitionAction();

        //设置播放器的位置
        void onSetVideoViewRectAction(Rect videoViewRect);

        //显示积分选择弹窗
        void onShowRewardAction();

        /**
         * 当前是否无延迟模式
         *
         * @return 是否无延迟模式
         */
        boolean isCurrentLowLatencyMode();

        /**
         * 切换无延迟模式
         *
         * @param isLowLatency 是否无延迟模式
         */
        void switchLowLatencyMode(boolean isLowLatency);
    }
    // </editor-fold>
}
