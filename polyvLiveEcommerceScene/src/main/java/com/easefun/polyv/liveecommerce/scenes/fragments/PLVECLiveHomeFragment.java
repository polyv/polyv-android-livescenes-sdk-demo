package com.easefun.polyv.liveecommerce.scenes.fragments;

import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.firstNotNull;
import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.interact.entrance.PLVInteractEntranceLayout;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.IPLVPointRewardEventProducer;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectQueue;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVPointRewardEffectWidget;
import com.easefun.polyv.livecommon.module.modules.reward.view.effect.PLVRewardSVGAHelper;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.PLVNoOverScrollViewPager;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.PLVToTopView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.livecommon.ui.widget.textview.PLVDrawableListenerTextView;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.layout.PLVECChatOverLengthMessageLayout;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatImgScanPopupView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatInputWindow;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECGreetingView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECLikeIconView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECRedpackView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPopupLayout2;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECProductPushCardLayout;
import com.easefun.polyv.liveecommerce.modules.member.PLVECMemberListLayoutLand;
import com.easefun.polyv.liveecommerce.modules.member.PLVECMemberListLayoutPort;
import com.easefun.polyv.liveecommerce.modules.player.widget.PLVECNetworkTipsView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECBlackTabLayout;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.plv.foundationsdk.component.exts.Lazy;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVCloseRoomEvent;
import com.plv.socket.event.chat.PLVFocusModeEvent;
import com.plv.socket.event.chat.PLVLikesEvent;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.interact.PLVShowProductDetailEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.login.PLVLogoutEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.StringUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

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
    //viewpager
    private PLVNoOverScrollViewPager chatViewPager;
    private PLVECChatFragment chatFragment;
    private PLVECQuizFragment quizFragment;
    private PLVDrawableListenerTextView sendMsgTv;
    private TextView quizNewMsgTipsTv;
    private PLVECBlackTabLayout blackTabLayout;
    private PLVECChatImgScanPopupView chatImgScanPopupView;
    @Nullable
    private PLVECChatOverLengthMessageLayout chatOverLengthMessageLayout;
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
    private PLVECCommodityPopupLayout2 commodityPopupLayout;
    private PLVECProductPushCardLayout productPushCardLayout;
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

    private PLVECRedpackView chatroomRedPackWidgetView;

    //监听器
    private OnViewActionListener onViewActionListener;

    //聊天室开关状态
    private boolean isCloseRoomStatus;
    //专注模式状态
    private boolean isFocusModeStatus;
    //是否选中提问tab状态
    private boolean isSelectedQuizStatus;
    //聊天输入窗口
    private PLVECChatInputWindow chatInputWindow;

    private ImageView backIv;

    @Nullable
    private PLVChatQuoteVO chatQuoteVO = null;

    //互动入口
    private PLVInteractEntranceLayout interactEntranceView;
    //评论上墙布局
    private PLVToTopView toTopView;
    private PLVRoundRectGradientTextView liveWatchOnlineCountTv;
    // 成员列表布局
    private final Lazy<PLVECMemberListLayoutPort> memberListLayoutPort = new Lazy<PLVECMemberListLayoutPort>() {
        @Override
        public PLVECMemberListLayoutPort onLazyInit() {
            PLVECMemberListLayoutPort res = new PLVECMemberListLayoutPort(getContext());
            res.init(chatroomPresenter);
            return res;
        }
    };
    private final Lazy<PLVECMemberListLayoutLand> memberListLayoutLand = new Lazy<PLVECMemberListLayoutLand>() {
        @Override
        public PLVECMemberListLayoutLand onLazyInit() {
            PLVECMemberListLayoutLand res = new PLVECMemberListLayoutLand(getContext());
            res.init(chatroomPresenter);
            return res;
        }
    };
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
        if (isOpenPointReward) {
            destroyPointRewardEffectQueue();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int marginPortrait = getResources().getDimensionPixelSize(R.dimen.plvec_margin_common);
        int marginLandscape = getResources().getDimensionPixelSize(R.dimen.plvec_landscape_margin_common);
        RelativeLayout.LayoutParams toTopViewLayoutParams = null;
        if (toTopView != null) {
            toTopViewLayoutParams = (RelativeLayout.LayoutParams) toTopView.getLayoutParams();
        }
        RelativeLayout.LayoutParams sendMsgTvLayoutParams = (RelativeLayout.LayoutParams) sendMsgTv.getLayoutParams();
        RelativeLayout.LayoutParams chatViewPagerLayoutParams = (RelativeLayout.LayoutParams) chatViewPager.getLayoutParams();
        RelativeLayout.LayoutParams blackTabLayoutParams = (RelativeLayout.LayoutParams) blackTabLayout.getLayoutParams();
        RelativeLayout.LayoutParams effectWidgetLayoutParams = (RelativeLayout.LayoutParams) polyvPointRewardEffectWidget.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            view.setPadding(0, PLVUIUtil.dip2px(this.getContext(), 16), 0, PLVUIUtil.dip2px(this.getContext(), 16));
            chatViewPagerLayoutParams.leftMargin = marginLandscape;
            blackTabLayoutParams.leftMargin = marginLandscape;
            sendMsgTvLayoutParams.setMargins(marginLandscape, 0, 0, 0);
            sendMsgTv.setLayoutParams(sendMsgTvLayoutParams);
            backIv.setVisibility(View.VISIBLE);
            if (morePopupView != null) {
                morePopupView.onLandscape();
            }
            if (commodityPopupLayout != null) {
                commodityPopupLayout.setLandspace(true);
            }
            effectWidgetLayoutParams.removeRule(RelativeLayout.ABOVE);
            if (toTopViewLayoutParams != null) {
                toTopViewLayoutParams.setMargins(0, ConvertUtils.dp2px(16), 0, 0);
            }
        } else {
//            view.setPadding(0, PLVUIUtil.dip2px(this.getContext(), 30), 0, PLVUIUtil.dip2px(this.getContext(), 16));
            chatViewPagerLayoutParams.leftMargin = marginPortrait;
            blackTabLayoutParams.leftMargin = marginPortrait;
            sendMsgTvLayoutParams.setMargins(marginPortrait, 0, 0, 0);
            sendMsgTv.setLayoutParams(sendMsgTvLayoutParams);
            backIv.setVisibility(View.GONE);

            if (morePopupView != null) {
                morePopupView.onPortrait();
            }

            if (commodityPopupLayout != null) {
                commodityPopupLayout.setLandspace(false);
            }

            effectWidgetLayoutParams.addRule(RelativeLayout.ABOVE, R.id.greet_ly);
            if (toTopViewLayoutParams != null) {
                toTopViewLayoutParams.setMargins(0, ConvertUtils.dp2px(82), 0, 0);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        watchInfoLy = findViewById(R.id.watch_info_ly);
        bulletinLy = findViewById(R.id.bulletin_ly);
        bulletinLy.setOnVisibilityChangedListener(new PLVECBulletinView.OnVisibilityChangedListener() {
            @Override
            public void onChanged(boolean isVisible) {
                adjustInteractEntranceLyLocation(isVisible);
            }
        });
        greetLy = findViewById(R.id.greet_ly);
        //聊天区域
        chatViewPager = findViewById(R.id.chat_msg_vp);
        chatViewPager.setPageMargin(ConvertUtils.dp2px(20));
        chatViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                isSelectedQuizStatus = position == 1;
                sendMsgTv.setSelected(isSelectedQuizStatus);
                updateViewByRoomStatusChanged(false);
                quizNewMsgTipsTv.setVisibility(View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        chatFragment = new PLVECChatFragment();
        chatFragment.init(liveRoomDataManager);
        chatFragment.setOnMessageAdapterListener(onChatMsgViewActionListener);
        quizFragment = new PLVECQuizFragment();
        quizFragment.init(liveRoomDataManager);
        quizFragment.setOnMessageAdapterListener(onChatMsgViewActionListener);
        //信息发送框
        sendMsgTv = findViewById(R.id.send_msg_tv);
        sendMsgTv.setOnClickListener(this);
        sendMsgTv.setDrawableRightListener(new PLVDrawableListenerTextView.DrawableRightListener() {
            @Override
            public void onDrawableRightClick(View view) {
                isSelectedQuizStatus = !sendMsgTv.isSelected();
                sendMsgTv.setSelected(isSelectedQuizStatus);
                chatViewPager.setCurrentItem(isSelectedQuizStatus ? 1 : 0);
            }
        });
        quizNewMsgTipsTv = findViewById(R.id.quiz_new_msg_tips_tv);
        quizNewMsgTipsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectedQuizStatus = true;
                sendMsgTv.setSelected(true);
                chatViewPager.setCurrentItem(1);
            }
        });
        blackTabLayout = findViewById(R.id.black_tab_ly);
        blackTabLayout.bindViewPager(chatViewPager);
        likeBt = findViewById(R.id.like_bt);
        likeBt.setOnButtonClickListener(this);
        likeCountTv = findViewById(R.id.like_count_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);
        commodityIv = findViewById(R.id.commodity_iv);
        commodityIv.setOnClickListener(this);
        rewardIv = findViewById(R.id.reward_iv);
        rewardIv.setVisibility(isOpenPointReward ? View.VISIBLE : View.GONE);
        rewardIv.setOnClickListener(this);
        backIv = findViewById(R.id.plvec_controller_back_iv);
        backIv.setOnClickListener(this);
        morePopupView = new PLVECMorePopupView();
        morePopupView.initLiveMoreLayout(moreIv);
        chatImgScanPopupView = new PLVECChatImgScanPopupView();
        if (getContext() != null) {
            chatOverLengthMessageLayout = new PLVECChatOverLengthMessageLayout(getContext());
            commodityPopupLayout = new PLVECCommodityPopupLayout2(getContext());
            commodityPopupLayout.setOnViewActionListener(new PLVECCommodityPopupLayout2.OnViewActionListener() {
                @Override
                public void onShowJobDetail(PLVShowJobDetailEvent param) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowJobDetail(param);
                    }
                }

                @Override
                public void onShowProductDetail(PLVShowProductDetailEvent param) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowProductDetail(param);
                    }
                }

                @Override
                public void onShowOpenLink() {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowOpenLink();
                    }
                }
            });
        }

        //打赏动画特效
        polyvPointRewardEffectWidget = findViewById(R.id.plvec_point_reward_effect);
        rewardSvgImage = findViewById(R.id.plvec_reward_svg);
        svgaParser = new SVGAParser(getContext());
        svgaHelper = new PLVRewardSVGAHelper();
        svgaHelper.init(rewardSvgImage, svgaParser);

        networkTipsView = findViewById(R.id.plvec_live_network_tips_layout);

        productPushCardLayout = findViewById(R.id.plvec_product_push_card_layout);
        productPushCardLayout.init(liveRoomDataManager);

        interactEntranceView = findViewById(R.id.plvec_interact_entrance_ly);
        interactEntranceView.changeLayoutStyle(false);
        interactEntranceView.setOnViewActionListener(new PLVInteractEntranceLayout.OnViewActionListener() {
            @Override
            public void onShowQuestionnaire() {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowQuestionnaire();
                }
            }
        });

        //评论上墙
        toTopView = findViewById(R.id.plvec_chatroom_to_top_view);
        toTopView.setIsLiveType(true);

        liveWatchOnlineCountTv = findViewById(R.id.plvec_live_watch_online_count_tv);
        liveWatchOnlineCountTv.setOnClickListener(this);
        final boolean showOnlineCount = PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_SHOW_VIEWER_LIST);
        liveWatchOnlineCountTv.setVisibility(showOnlineCount ? View.VISIBLE : View.GONE);

        //卡片推送
        cardPushManager.registerView((ImageView) findViewById(R.id.card_enter_view), (TextView) findViewById(R.id.card_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.card_enter_tips_view));

        //无条件抽奖
        lotteryManager.registerView((ImageView) findViewById(R.id.plvec_live_lottery_enter_view),(TextView) findViewById(R.id.plvec_live_lottery_enter_cd_tv),(PLVTriangleIndicateTextView) findViewById(R.id.plvec_live_lottery_enter_tips_view));

        //有条件抽奖'
        welfareLotteryManager.onCleared();
        welfareLotteryManager.registerView((ImageView) findViewById(R.id.plvec_live_welfare_lottery_enter_view), (TextView) findViewById(R.id.plvec_live_welfare_lottery_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.plvec_live_welfare_lottery_enter_tips_view));

        chatroomRedPackWidgetView = findViewById(R.id.plvec_chatroom_red_pack_widget_view);
        chatroomRedPackWidgetView.initData(liveRoomDataManager);

        updateOnlineCount(0);
        initNetworkTipsLayout();
        adjustInteractEntranceLyLocation(bulletinLy.getVisibility() == View.VISIBLE);
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

    private void adjustInteractEntranceLyLocation(boolean isBulletinLyVisible) {
        if (interactEntranceView == null) {
            return;
        }
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) interactEntranceView.getLayoutParams();
        if (isBulletinLyVisible) {
            rlp.addRule(RelativeLayout.BELOW, R.id.bulletin_ly);
        } else {
            rlp.addRule(RelativeLayout.BELOW, R.id.watch_info_ly);
        }
        interactEntranceView.setLayoutParams(rlp);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">

    @Override
    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        super.init(liveRoomDataManager);
        runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                commodityPopupLayout.init(liveRoomDataManager);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    @Override
    protected void registerChatroomView() {
        chatroomPresenter.registerView(chatroomView);
        chatroomPresenter.registerView(chatFragment.getChatroomView());
        chatroomPresenter.registerView(quizFragment.getChatroomView());
        chatroomPresenter.registerView(toTopView.getChatroomView());
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
    protected void updateOnlineCount(long watchCount) {
        String viewerCountText = StringUtils.toKString(watchCount);
        String text = PLVAppUtils.formatString(R.string.plv_player_viewer_online_count, viewerCountText);
        liveWatchOnlineCountTv.setText(text);
    }

    @Override
    protected void updateLikesInfo(String likesString) {
        likeCountTv.setText(likesString);
    }

    @Override
    protected void acceptOpenCommodity() {
        commodityIv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void acceptOpenQuiz(@NonNull PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        chatViewPager.setNoOverScroll(true);
        quizFragment.setTips(channelMenusBean.getContent());
        Drawable rightDrawable = getContext() == null ? null : ContextCompat.getDrawable(getContext(), R.drawable.plvec_chatroom_quiz_sel);
        if (rightDrawable != null) {
            rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(), rightDrawable.getIntrinsicHeight());
        }
        sendMsgTv.setCompoundDrawables(sendMsgTv.getCompoundDrawables()[0], sendMsgTv.getCompoundDrawables()[1], rightDrawable, sendMsgTv.getCompoundDrawables()[3]);
        blackTabLayout.setVisibility(View.VISIBLE);
        PLVViewInitUtils.initViewPager(getChildFragmentManager(), chatViewPager, 0, chatFragment, quizFragment);
    }

    @Override
    protected void acceptCloseQuiz() {
        chatViewPager.setNoOverScroll(false);
        PLVViewInitUtils.initViewPager(getChildFragmentManager(), chatViewPager, 0, chatFragment);
    }

    @Override
    protected void acceptOpenFloatWindow(boolean isFloatEnable) {
        morePopupView.updateOpenFloat(isFloatEnable);
    }

    @Override
    protected void acceptInteractEntranceData(List<PLVCallAppEvent.ValueBean.DataBean> dataBeans) {
        if (interactEntranceView != null) {
            interactEntranceView.acceptInteractEntranceData(dataBeans);
        }
    }

    @Override
    protected void acceptInteractStatusData(PLVWebviewUpdateAppStatusVO webviewUpdateAppStatusVO) {
        super.acceptInteractStatusData(webviewUpdateAppStatusVO);
        if (morePopupView != null) {
            morePopupView.acceptInteractStatusData(webviewUpdateAppStatusVO);
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
        if (toTopView != null) {
            toTopView.setShowEnabled(state != PLVPlayerState.NO_LIVE && state != PLVPlayerState.LIVE_END);
        }
    }

    @Override
    public void setJoinRTCChannel(boolean isJoinRtcChannel) {
        morePopupView.updateJoinRTCChannel(isJoinRtcChannel);
    }

    @Override
    public void setJoinLinkMic(boolean isJoinLinkMic) {
        morePopupView.updateJoinLinkMic(isJoinLinkMic);
    }

    @Override
    public void setJoinRequestLinkMic(boolean isJoinRequestLinkMic) {
        morePopupView.updateJoinLinkMicRequest(isJoinRequestLinkMic);
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
    public void acceptNetworkQuality(PLVLinkMicConstant.NetworkQuality quality) {
        networkTipsView.acceptNetworkQuality(quality);
    }

    @Override
    public void showMorePopupWindow() {
        if (moreIv != null) {
            moreIv.performClick();
        }
    }

    @Override
    public boolean isInterceptViewAction(MotionEvent motionEvent) {
        if (backIv.getVisibility() == View.VISIBLE) {
            float x = backIv.getX() + backIv.getWidth();
            float y = backIv.getY() + backIv.getHeight();
            if (motionEvent.getX() >= backIv.getX() && motionEvent.getX() <= x
                    && motionEvent.getY() >= backIv.getY() && motionEvent.getY() <= y) {
                return true;
            }
        }
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 打赏动画控制">

    /**
     * 初始化积分打赏动画特效item
     */
    private void initPointRewardEffectQueue() {
        if (pointRewardEventProducer == null) {
            pointRewardEventProducer = new PLVPointRewardEffectQueue();
            polyvPointRewardEffectWidget.setEventProducer(pointRewardEventProducer);
            //开启横屏积分打赏效果
            polyvPointRewardEffectWidget.isOpenLandscapeEffect(true);
        }
    }

    /**
     * 销毁积分打赏动效队列
     */
    private void destroyPointRewardEffectQueue() {
        if (pointRewardEventProducer != null) {
            pointRewardEventProducer.destroy();
        }
        svgaHelper.clear();
    }

    private void acceptPointRewardMessage(PLVRewardEvent rewardEvent) {
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

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 专注模式">
    private void acceptFocusModeEvent(final PLVFocusModeEvent focusModeEvent) {
        isFocusModeStatus = focusModeEvent.isOpen();
        postToMainThread(new Runnable() {
            @Override
            public void run() {
                updateViewByRoomStatusChanged(isFocusModeStatus);
                if (isFocusModeStatus) {
                    chatFragment.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FOCUS_MODE);
                } else {
                    chatFragment.changeDisplayType(PLVECChatMessageAdapter.DISPLAY_DATA_TYPE_FULL);
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
        boolean isEnabled = enabledToSendMsg();
        if (isDisabled && !isSelectedQuizStatus) {
            if (chatInputWindow != null) {
                chatInputWindow.requestClose();
            }
        }
        sendMsgTv.setText(isSelectedQuizStatus ? R.string.plv_chat_input_tips_quiz
                : (isCloseRoomStatus ? R.string.plv_chat_input_tips_chatroom_close_2
                : (isFocusModeStatus ? R.string.plv_chat_input_tips_focus_2
                : R.string.plv_chat_input_tips_chat_6)));
        sendMsgTv.setOnClickListener(isEnabled ? this : null);
        if (chatInputWindow != null) {
            chatInputWindow.updateHintPair(sendMsgTv.getText().toString(), isEnabled);
        }
    }

    private boolean enabledToSendMsg() {
        return (!isCloseRoomStatus && !isFocusModeStatus) || isSelectedQuizStatus;
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
        public void onNewsPushStartMessage(@NonNull PLVNewsPushStartEvent newsPushStartEvent) {
            super.onNewsPushStartMessage(newsPushStartEvent);
            cardPushManager.acceptNewsPushStartMessage(chatroomPresenter, newsPushStartEvent);
        }

        @Override
        public void onNewsPushCancelMessage() {
            super.onNewsPushCancelMessage();
            cardPushManager.acceptNewsPushCancelMessage();
        }

        @Override
        public void onAnswerEvent(@NonNull PLVTAnswerEvent answerEvent) {
            super.onAnswerEvent(answerEvent);
            postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (chatViewPager != null && chatViewPager.getChildCount() > 1 && chatViewPager.getCurrentItem() == 0) {
                        if (quizNewMsgTipsTv != null) {
                            quizNewMsgTipsTv.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 图片信息点击事件处理">
    PLVECChatMessageAdapter.OnViewActionListener onChatMsgViewActionListener = new PLVECChatMessageAdapter.OnViewActionListener() {
        @Override
        public void onChatImgClick(View view, String imgUrl) {
            chatImgScanPopupView.showImgScanLayout(view, imgUrl);
        }

        @Override
        public void callOnReplyMessage(PLVChatQuoteVO chatQuoteVO) {
            PLVECLiveHomeFragment.this.chatQuoteVO = chatQuoteVO;
            showInputWindow();
        }

        @Override
        public void onShowOverLengthMessage(PLVECChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean) {
            if (chatOverLengthMessageLayout != null) {
                chatOverLengthMessageLayout.show(chatMessageDataBean);
            }
        }

        @Override
        public void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent) {
            if (onViewActionListener != null) {
                onViewActionListener.onReceiveRedPaper(redPaperEvent);
            }
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 输入窗口显示，信息发送">
    private void showInputWindow() {
        PLVECChatInputWindow.show(getActivity(), PLVECChatInputWindow.class, new PLVECChatInputWindow.MessageSendListener() {
            private void setViewBottomParam(View view, int bottom) {
                ViewGroup.MarginLayoutParams viewVp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                viewVp.bottomMargin = bottom;
                view.setLayoutParams(viewVp);
            }

            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
//                if (getContext() != null && PLVScreenUtils.isLandscape(getContext())) {
//                    return;
//                }
//                int bottom = keyboardHeightInPx - ConvertUtils.dp2px(16 + 32);
//                setViewBottomParam(sendMsgTv, bottom);
//                setViewBottomParam(moreIv, bottom);
            }

            @Override
            public void onSoftKeyboardClosed(boolean isFinished) {
//                setViewBottomParam(sendMsgTv, 0);
//                setViewBottomParam(moreIv, 0);
            }

            @Override
            public void onInputContext(PLVECChatInputWindow inputWindow) {
                chatInputWindow = inputWindow;
            }

            @Override
            public boolean hasQuiz() {
                return chatViewPager != null && chatViewPager.getChildCount() > 1;
            }

            @Override
            public boolean isSelectedQuiz() {
                return isSelectedQuizStatus;
            }

            @Override
            public void onQuizToggle(boolean isSelectedQuiz) {
                isSelectedQuizStatus = isSelectedQuiz;
                sendMsgTv.setSelected(isSelectedQuizStatus);
                chatViewPager.setCurrentItem(isSelectedQuizStatus ? 1 : 0);
            }

            @Override
            public Pair<String, Boolean> getHintPair() {
                return new Pair<>(sendMsgTv.getText().toString(), enabledToSendMsg());
            }

            @Override
            public PLVChatQuoteVO getChatQuoteContent() {
                return PLVECLiveHomeFragment.this.chatQuoteVO;
            }

            @Override
            public void onCloseQuote() {
                PLVECLiveHomeFragment.this.chatQuoteVO = null;
            }

            @Override
            public boolean onSendMsg(String message) {
                PolyvLocalMessage localMessage = new PolyvLocalMessage(message);
                Pair<Boolean, Integer> sendResult;
                if (isSelectedQuizStatus) {
                    int value = chatroomPresenter.sendQuestionMessage(new PolyvQuestionMessage(message));
                    sendResult = new Pair<>(value > 0, value);
                } else if (chatQuoteVO == null || chatQuoteVO.getMessageId() == null) {
                    sendResult = chatroomPresenter.sendChatMessage(localMessage);
                } else {
                    localMessage.setQuote(chatQuoteVO);
                    sendResult = chatroomPresenter.sendQuoteMessage(localMessage, chatQuoteVO.getMessageId());
                }
                if (!sendResult.first) {
                    //发送失败
                    ToastUtils.showShort(isSelectedQuizStatus ? getString(R.string.plv_chat_toast_send_quiz_failed) : getString(R.string.plv_chat_toast_send_msg_failed) + ": " + sendResult.second);
                    return false;
                } else {
                    if (!isSelectedQuizStatus) {
                        PLVECLiveHomeFragment.this.chatQuoteVO = null;
                    }
                    return true;
                }
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

    // <editor-fold defaultstate="collapsed" desc="商品 - 布局显示">
    private void showCommodityLayout() {
        if (commodityPopupLayout != null) {
            commodityPopupLayout.show();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="更多弹窗 - 布局显示及交互处理">
    private void showMorePopupWindow(View v) {
        boolean isCurrentVideoMode = onViewActionListener == null || onViewActionListener.onGetMediaPlayModeAction() == PolyvMediaPlayMode.MODE_VIDEO;
        morePopupView.showLiveMoreLayout(v, isCurrentVideoMode, liveRoomDataManager.getConfig().getChannelId(), new PLVECMorePopupView.OnLiveMoreClickListener() {
            @Override
            public boolean onPlayModeClick(View view) {
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeMediaPlayModeClick(view, view.isSelected() ? PolyvMediaPlayMode.MODE_VIDEO : PolyvMediaPlayMode.MODE_AUDIO);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onPlayModeClick(boolean viewSelected) {
                if (onViewActionListener != null) {
                    onViewActionListener.onChangeMediaPlayModeClick(null, viewSelected ? PolyvMediaPlayMode.MODE_VIDEO : PolyvMediaPlayMode.MODE_AUDIO);
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

            @Override
            public void onScreenshot() {
                screenshotHelper.startScreenCaptureToFragment(PLVECLiveHomeFragment.this);
            }

            @Override
            public void onClickDynamicFunction(String event) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickDynamicFunction(event);
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
    private void observeRewardData() {
        liveRoomDataManager.getPointRewardEnableData().observe(this, new Observer<PLVStatefulData<Boolean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<Boolean> booleanPLVStatefulData) {
                liveRoomDataManager.getPointRewardEnableData().removeObserver(this);
                if (booleanPLVStatefulData != null && booleanPLVStatefulData.getData() != null) {
                    isOpenPointReward = booleanPLVStatefulData.getData();
                    if (isOpenPointReward) {
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
            showCommodityLayout();
        } else if (id == R.id.reward_iv) {
            if (isOpenPointReward) {
                //回调显示积分打赏弹窗
                if (onViewActionListener != null) {
                    onViewActionListener.onShowRewardAction();
                }
            }
        } else if (id == R.id.plvec_controller_back_iv) {
            if(PLVScreenUtils.isLandscape(getContext())){
                PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
            }
        } else if (liveWatchOnlineCountTv != null && id == liveWatchOnlineCountTv.getId()) {
            if (liveRoomDataManager != null && PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                    .isFeatureSupport(PLVChannelFeature.LIVE_SHOW_VIEWER_LIST)) {
                if (ScreenUtils.isPortrait()) {
                    memberListLayoutPort.get().show();
                } else {
                    memberListLayoutLand.get().show();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //切换播放模式
        void onChangeMediaPlayModeClick(@Nullable View view, int mediaPlayMode);

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

        //显示问卷
        void onShowQuestionnaire();

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

        /**
         * 回调 拆开红包
         */
        void onReceiveRedPaper(PLVRedPaperEvent redPaperEvent);

        /**
         * 点击了动态功能控件
         *
         * @param event 动态功能的event data
         */
        void onClickDynamicFunction(String event);

        /**
         * 展示职位详情
         * @param param
         */
        void onShowJobDetail(PLVShowJobDetailEvent param);

        /**
         * 展示商品详情
         * @param param
         */
        void onShowProductDetail(PLVShowProductDetailEvent param);

        /**
         * 展示用于跳转微信复制的二维码
         */
        void onShowOpenLink();
    }
    // </editor-fold>
}
