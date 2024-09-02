package com.easefun.polyv.liveecommerce.scenes.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.log.PLVTrackLogHelper;
import com.easefun.polyv.livecommon.module.modules.player.PLVPlayerState;
import com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data.PLVPlayInfoVO;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousView;
import com.easefun.polyv.livecommon.module.modules.previous.presenter.PLVPreviousPlaybackPresenter;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.PLVToTopView;
import com.easefun.polyv.livecommon.ui.widget.PLVTriangleIndicateTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.PLVUIUtil;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.PLVECChatMessageAdapter;
import com.easefun.polyv.liveecommerce.modules.chatroom.layout.PLVECChatOverLengthMessageLayout;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECBulletinView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatImgScanPopupView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECGreetingView;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECRedpackView;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPopupLayout2;
import com.easefun.polyv.liveecommerce.modules.commodity.PLVECCommodityPushLayout2;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.IPLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.PLVECPreviousDialogFragment;
import com.easefun.polyv.liveecommerce.modules.playback.fragments.previous.PLVECPreviousAdapter;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECMorePopupView;
import com.easefun.polyv.liveecommerce.scenes.fragments.widget.PLVECWatchInfoView;
import com.easefun.polyv.livescenes.model.bulletin.PolyvBulletinVO;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.foundationsdk.utils.PLVTimeUtils;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.config.PLVLivePlaybackSeekBarStrategy;
import com.plv.livescenes.model.PLVPlaybackListVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackGetDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.playback.chat.PLVChatPlaybackManager;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.commodity.PLVProductContentBean;
import com.plv.socket.event.interact.PLVNewsPushStartEvent;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.login.PLVLoginEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * 回放首页：主持人信息、播放控制、进度条、更多
 */
public class PLVECPalybackHomeFragment extends PLVECCommonHomeFragment implements View.OnClickListener {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //观看信息布局
    private PLVECWatchInfoView watchInfoLy;
    //公告布局
    private PLVECBulletinView bulletinLy;
    //播放控制
    private ImageView playControlIv;
    private TextView playTimeTv;
    private SeekBar playProgressSb;
    private TextView totalTimeTv;
    private boolean isPlaySbDragging;
    //更多
    private ImageView moreIv;
    private PLVECMorePopupView morePopupView;

    //欢迎语
    private PLVECGreetingView greetLy;
    //聊天区域
    private PLVMessageRecyclerView chatMsgRv;
    private PLVECChatMessageAdapter chatMessageAdapter;
    //未读信息提醒view
    private TextView unreadMsgTv;
    //下拉加载历史记录控件
    private SwipeRefreshLayout swipeLoadView;
    private PLVECChatImgScanPopupView chatImgScanPopupView;
    @Nullable
    private PLVECChatOverLengthMessageLayout chatOverLengthMessageLayout;
    //聊天回放tips
    private TextView chatPlaybackTipsTv;

    //商品
    private ImageView commodityIv;
    private PLVECCommodityPopupLayout2 commodityPopupLayout;
    private PLVECCommodityPushLayout2 commodityPushLayout2;

    //更多回放视频
    private ImageView moreVideoListIv;
    //监听器
    private OnViewActionListener onViewActionListener;
    //回放更多视频的弹窗
    private IPLVECPreviousDialogFragment previousPopupView;
    //回放视频列表
    private List<PLVPlaybackListVO.DataBean.ContentsBean> dataList;
    //当前回放视频的vid
    private String currentVid;

    //更多回放视频的presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPresenter;
    private PLVPreviousView plvPreviousView;

    private PLVECRedpackView chatroomRedPackWidgetView;

    //评论上墙布局
    private PLVToTopView toTopView;

    //聊天回放管理器
    private IPLVChatPlaybackManager chatPlaybackManager;
    private Runnable playbackTipsRunnable;

    private Boolean hasPreviousPage = null;
    private boolean hasInitPreviousView = false;
    private Rect videoViewRect;

    private ImageView backIv;

    //聊天回放是否打开
    protected boolean isChatPlaybackEnabled;
    //播放器准备完成的聊天回放任务
    private List<Triple<String, String, String>> needAddedChatPlaybackTask = new ArrayList<>();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plvec_playback_page_home_fragment, container, false);
        initView();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onViewActionListener != null) {
            onViewActionListener.onViewCreated();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatPlaybackManager != null) {
            chatPlaybackManager.destroy();
        }
        if (chatPlaybackTipsTv != null) {
            chatPlaybackTipsTv.removeCallbacks(playbackTipsRunnable);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams toTopViewLayoutParams = null;
        if (toTopView != null) {
            toTopViewLayoutParams = (RelativeLayout.LayoutParams) toTopView.getLayoutParams();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view.setPadding(0, PLVUIUtil.dip2px(this.getContext(), 16), 0, PLVUIUtil.dip2px(this.getContext(), 16));
            backIv.setVisibility(View.VISIBLE);
            if (morePopupView != null) {
                morePopupView.onLandscape();
            }
            if (commodityPopupLayout != null) {
                commodityPopupLayout.setLandspace(true);
            }
            if (toTopViewLayoutParams != null) {
                toTopViewLayoutParams.setMargins(0, ConvertUtils.dp2px(16), 0, 0);
            }
        } else {
            view.setPadding(0, PLVUIUtil.dip2px(this.getContext(), 30), 0, PLVUIUtil.dip2px(this.getContext(), 16));
            backIv.setVisibility(View.GONE);

            if (morePopupView != null) {
                morePopupView.onPortrait();
            }

            if (commodityPopupLayout != null) {
                commodityPopupLayout.setLandspace(false);
            }
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
        playControlIv = findViewById(R.id.play_control_iv);
        playControlIv.setOnClickListener(this);
        playTimeTv = findViewById(R.id.play_time_tv);
        playProgressSb = findViewById(R.id.play_progress_sb);
        playProgressSb.setOnSeekBarChangeListener(playProgressChangeListener);
        totalTimeTv = findViewById(R.id.total_time_tv);
        moreIv = findViewById(R.id.more_iv);
        moreIv.setOnClickListener(this);
        backIv = findViewById(R.id.plvec_controller_back_iv);
        backIv.setOnClickListener(this);
        //商品
        commodityIv = findViewById(R.id.playback_commodity_iv);
        commodityIv.setOnClickListener(this);
        moreVideoListIv = findViewById(R.id.more_video_list_iv);
        moreVideoListIv.setVisibility(View.GONE);
        morePopupView = new PLVECMorePopupView();
        morePopupView.initPlaybackMoreLayout(moreIv);
        commodityPushLayout2 = findViewById(R.id.plvec_commodity_push_layout);
        commodityPushLayout2.setAnchor(commodityIv);
        commodityPushLayout2.init(liveRoomDataManager);
        commodityPushLayout2.setCommodityPushListener(new PLVECCommodityPushLayout2.ICommodityPushListener() {
            @Override
            public void showJobDetail(PLVProductContentBean bean) {
                if (onViewActionListener != null) {
                    PLVShowJobDetailEvent event = new PLVShowJobDetailEvent();
                    event.setData(bean);
                    onViewActionListener.onShowJobDetail(event);
                }
            }
        });

        // 追踪商品卡片曝光事件
        PLVTrackLogHelper.trackReadProductPush(commodityPushLayout2, true, liveRoomDataManager);

        previousPresenter = new PLVPreviousPlaybackPresenter(liveRoomDataManager);
        previousPopupView = new PLVECPreviousDialogFragment();
        dataList = new ArrayList<>();

        chatImgScanPopupView = new PLVECChatImgScanPopupView();
        if (getContext() != null) {
            chatOverLengthMessageLayout = new PLVECChatOverLengthMessageLayout(getContext());
            commodityPopupLayout = new PLVECCommodityPopupLayout2(getContext());
            commodityPopupLayout = new PLVECCommodityPopupLayout2(getContext());
            commodityPopupLayout.setOnViewActionListener(new PLVECCommodityPopupLayout2.OnViewActionListener() {
                @Override
                public void onShowJobDetail(PLVShowJobDetailEvent param) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowJobDetail(param);
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
        greetLy = findViewById(R.id.greet_ly);
        chatMsgRv = findViewById(R.id.chat_msg_rv);
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv).setStackFromEnd(true);
        chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4)));
        chatMessageAdapter = new PLVECChatMessageAdapter();
        chatMsgRv.setAdapter(chatMessageAdapter);
        chatMessageAdapter.setOnViewActionListener(new PLVECChatMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(View view, String imgUrl) {
                chatImgScanPopupView.showImgScanLayout(view, imgUrl);
            }

            @Override
            public void callOnReplyMessage(PLVChatQuoteVO chatQuoteVO) {

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
        });
        // 追踪红包曝光事件
        PLVTrackLogHelper.trackReadRedpack(chatMsgRv, chatMessageAdapter.getDataList(), liveRoomDataManager);
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
                if (isChatPlaybackEnabled) {
                    if (chatPlaybackManager != null) {
                        chatPlaybackManager.loadPrevious();
                    }
                }
            }
        });
        swipeLoadView.setEnabled(isChatPlaybackEnabled);
        //聊天回放tips
        chatPlaybackTipsTv = findViewById(R.id.plvlc_chat_playback_tips_tv);
        //卡片推送
        cardPushManager.registerView((ImageView) findViewById(R.id.card_enter_view), (TextView) findViewById(R.id.card_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.card_enter_tips_view));

        //无条件抽奖
        lotteryManager.registerView((ImageView) findViewById(R.id.plvec_playback_lottery_enter_view), (TextView) findViewById(R.id.plvec_playback_lottery_enter_cd_tv), (PLVTriangleIndicateTextView) findViewById(R.id.plvec_playback_lottery_enter_tips_view));

        chatroomRedPackWidgetView = findViewById(R.id.plvec_chatroom_red_pack_widget_view);
        chatroomRedPackWidgetView.initData(liveRoomDataManager);

        //评论上墙
        toTopView = findViewById(R.id.plvec_chatroom_to_top_view);
        toTopView.setIsLiveType(false);
        if (chatPlaybackManager != null && toTopView != null) {
            chatPlaybackManager.addOnCallDataListener(toTopView.getChatPlaybackDataListener());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据">

    @Override
    public void init(final IPLVLiveRoomDataManager liveRoomDataManager) {
        super.init(liveRoomDataManager);

        runAfterOnActivityCreated(new Runnable() {
            @Override
            public void run() {
                updateControlVisibility();
                commodityPopupLayout.init(liveRoomDataManager);
            }
        });
    }

    private void updateControlVisibility() {
        String channelId = liveRoomDataManager.getConfig().getChannelId();
        boolean enableSpeedControl = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_SPEED_CONTROL_ENABLE);
        boolean enablePlayButton = PLVChannelFeatureManager.onChannel(channelId).isFeatureSupport(PLVChannelFeature.LIVE_PLAYBACK_PLAY_BUTTON_ENABLE);
        PLVLivePlaybackSeekBarStrategy seekBarStrategy = PLVChannelFeatureManager.onChannel(channelId)
                .getOrDefault(PLVChannelFeature.LIVE_PLAYBACK_SEEK_BAR_STRATEGY, PLVLivePlaybackSeekBarStrategy.ALLOW_SEEK);

        playControlIv.setVisibility(enablePlayButton ? View.VISIBLE : View.GONE);
        morePopupView.setEnableSpeedControl(enableSpeedControl);
        playProgressSb.setVisibility(seekBarStrategy != PLVLivePlaybackSeekBarStrategy.INVISIBLE ? View.VISIBLE : View.GONE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API">
    @Override
    protected boolean isPlaybackFragment() {
        return true;
    }

    @Override
    protected void updateWatchInfo(String coverImage, String publisher) {
        watchInfoLy.updateWatchInfo(coverImage, publisher);
        watchInfoLy.setVisibility(View.VISIBLE);
    }

    @Override
    protected void updateWatchCount(long times) {
        watchInfoLy.updateWatchCount(times);
    }

    @Override
    protected void acceptOpenCommodity() {
        commodityIv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void acceptChatPlaybackEnable(boolean isChatPlaybackEnable) {
        this.isChatPlaybackEnabled = isChatPlaybackEnable;
        if (isChatPlaybackEnable) {
            if (chatPlaybackManager == null) {
                chatPlaybackManager = new PLVChatPlaybackManager();
                chatPlaybackManager.setOnGetDataListener(new IPLVChatPlaybackGetDataListener() {
                    @Override
                    public int currentTime() {
                        int currentTime = 0;
                        if (onViewActionListener != null) {
                            currentTime = onViewActionListener.getVideoCurrentPosition();
                        }
                        return currentTime;
                    }

                    @Override
                    public boolean canScrollBottom() {
                        return chatMsgRv.canScrollVertically(1);
                    }

                    @Override
                    public Object[] getParsedEmoObjects(String content) {
                        return new CharSequence[]{PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(content), ConvertUtils.dp2px(12), Utils.getApp())};
                    }
                });
                chatPlaybackManager.addOnCallDataListener(chatPlaybackCallDataListener);
            }
            if (toTopView != null) {
                chatPlaybackManager.addOnCallDataListener(toTopView.getChatPlaybackDataListener());
            }
            if (chatPlaybackTipsTv != null) {
                chatPlaybackTipsTv.setVisibility(View.VISIBLE);
                chatPlaybackTipsTv.postDelayed(playbackTipsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        chatPlaybackTipsTv.setVisibility(View.GONE);
                    }
                }, 5000);
            }
            //聊天回放重新设置播放器的位置，后续加上在线聊天室后，可以放在onActivityCreated方法中调用
            calculateLiveVideoViewRect();
        }
        if (swipeLoadView != null) {
            swipeLoadView.setEnabled(isChatPlaybackEnabled);
        }
        chatroomPresenter.registerView(chatroomView);
        //后于播放器准备完成回调时，这时检测是否要触发加载聊天回放
        checkStartChatPlayback();
    }

    @Override
    protected void acceptInteractStatusData(PLVWebviewUpdateAppStatusVO webviewUpdateAppStatusVO) {
        super.acceptInteractStatusData(webviewUpdateAppStatusVO);
        if (morePopupView != null) {
            morePopupView.acceptInteractStatusData(webviewUpdateAppStatusVO);
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

    // <editor-fold defaultstate="collapsed" desc="对外API">
    //设置播放状态
    @Override
    public void setPlayerState(PLVPlayerState state) {
        if (state == PLVPlayerState.PREPARED) {
            if (morePopupView != null) {
                morePopupView.updatePlaybackPlayStateView(View.VISIBLE);
            }
            if (onViewActionListener != null) {
                totalTimeTv.setText(PLVTimeUtils.generateTime(onViewActionListener.onGetDurationAction(), true));
            }
        }
    }

    //设置播放信息
    @Override
    public void setPlaybackPlayInfo(PLVPlayInfoVO playInfoVO) {
        if (playInfoVO == null) {
            return;
        }
        int position = playInfoVO.getPosition();
        int totalTime = playInfoVO.getTotalTime();
        int bufPercent = playInfoVO.getBufPercent();
        boolean isPlaying = playInfoVO.isPlaying();
        boolean isSubViewPlaying = playInfoVO.isSubVideoViewPlaying();
        if (isSubViewPlaying) {
            playControlIv.setSelected(false);
            playProgressSb.setProgress(0);
            moreIv.setClickable(false);
            morePopupView.hideAll();
        } else {
            playControlIv.setClickable(true);
            playProgressSb.setClickable(true);
            moreIv.setClickable(true);
            //在拖动进度条的时候，这里不更新
            if (!isPlaySbDragging) {
                playTimeTv.setText(PLVTimeUtils.generateTime(position, true));
                if (totalTime > 0) {
                    playProgressSb.setProgress((int) ((long) playProgressSb.getMax() * position / totalTime));
                } else {
                    playProgressSb.setProgress(0);
                }
            }
            playProgressSb.setSecondaryProgress(playProgressSb.getMax() * bufPercent / 100);
            playControlIv.setSelected(isPlaying);
        }

        //判断是否播放完成，播放完成通知
        if (position >= totalTime && totalTime > 0) {
            if (previousPresenter != null) {
                previousPresenter.onPlayComplete();
            }
        }
    }

    @Override
    public void onHasPreviousPage(boolean hasPreviousPage) {
        if (this.hasPreviousPage != null && this.hasPreviousPage == hasPreviousPage) {
            return;
        }
        this.hasPreviousPage = hasPreviousPage;
        if (hasPreviousPage) {
            moreVideoListIv.setOnClickListener(this);
            moreVideoListIv.setVisibility(View.VISIBLE);
            initPreviousView();
        } else {
            moreVideoListIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void setOnViewActionListener(PLVECCommonHomeFragment.OnViewActionListener listener) {
        this.onViewActionListener = (OnViewActionListener) listener;
    }

    @Override
    public void onPlaybackVideoPrepared(String sessionId, String channelId, String fileId) {
        if (needAddedChatPlaybackTask != null) {
            needAddedChatPlaybackTask.add(new Triple<>(sessionId, channelId, fileId));
        }
        if (isChatPlaybackEnabled && chatPlaybackManager != null) {
            chatPlaybackManager.start(sessionId, channelId, fileId);
        }
    }

    @Override
    public void onPlaybackVideoSeekComplete(int time) {
        if (isChatPlaybackEnabled && chatPlaybackManager != null) {
            chatPlaybackManager.seek(time);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天回放">
    private void checkStartChatPlayback() {
        if (needAddedChatPlaybackTask != null && needAddedChatPlaybackTask.size() > 0) {
            Triple<String, String, String> data = needAddedChatPlaybackTask.get(needAddedChatPlaybackTask.size() - 1);
            chatPlaybackManager.start(data.getFirst(), data.getSecond(), data.getThird());
        }
        needAddedChatPlaybackTask = null;
    }

    IPLVChatPlaybackCallDataListener chatPlaybackCallDataListener = new PLVChatPlaybackCallDataExListener() {
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
                addChatMessageToList(dataList, chatMessageAdapter.getItemCount() == 0);
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
        public void onManager(IPLVChatPlaybackManager chatPlaybackManager) {
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - 添加信息至列表">
    private void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
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
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd) {
        Runnable runnable = new Runnable() {
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
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private void addChatMessageToListHead(final List<PLVBaseViewData> chatMessageDataList) {
        if (chatMessageAdapter != null) {
            chatMessageAdapter.addDataListChangedAtHead(chatMessageDataList);
            chatMsgRv.scrollToPosition(0);
        }
    }

    private void removeChatMessageToList(int startPosition, int count) {
        if (chatMessageAdapter != null) {
            chatMessageAdapter.removeDataChanged(startPosition, count);
            if (!chatMsgRv.canScrollVertically(1)) {
                chatMsgRv.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
            }
        }
    }

    private void removeChatMessageToList(final String id, final boolean isRemoveAll) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isRemoveAll) {
                    chatMessageAdapter.removeAllDataChanged();
                } else {
                    chatMessageAdapter.removeDataChanged(id);
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
        //暂不支持在线聊天，因此也不显示非聊天回放的欢迎语
        if (!isChatPlaybackEnabled) {
            return;
        }
        //显示欢迎语
        greetLy.acceptGreetingMessage(loginEvent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室 - MVP模式的view层实现">
    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public int getSpeakEmojiSize() {
            return ConvertUtils.dp2px(12);//聊天列表里的文本信息textSize
        }

        @Override
        public void onLoginEvent(@NonNull PLVLoginEvent loginEvent) {
            super.onLoginEvent(loginEvent);
            acceptLoginMessage(loginEvent);
        }

        @Override
        public void onBulletinEvent(@NonNull PolyvBulletinVO bulletinVO) {
            super.onBulletinEvent(bulletinVO);
            acceptBulletinMessage(bulletinVO);
        }

        @Override
        public void onRemoveBulletinEvent() {
            super.onRemoveBulletinEvent();
            removeBulletin();
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

    // <editor-fold defaultstate="collapsed" desc="回放视频列表 - MVP模式view层的实现">

    /**
     * 设置PreviousView的参数
     */
    private void initPreviousView() {
        if (hasInitPreviousView) {
            return;
        }
        hasInitPreviousView = true;

        PLVPreviousView.Builder builder = new PLVPreviousView.Builder(getContext());
        //创建PLVPreviousView
        plvPreviousView = builder.create();
        PLVECPreviousAdapter plvecPreviousAdapter = new PLVECPreviousAdapter();
        plvecPreviousAdapter.setOnViewActionListener(new PLVPreviousAdapter.OnViewActionListener() {
            @Override
            public void changeVideoVidClick(String vid) {
                plvPreviousView.changePlaybackVideoVid(vid);
            }
        });
        builder.setAdapter(plvecPreviousAdapter)
                .setRecyclerViewLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false))
                .setRecyclerViewItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        outRect.left = ConvertUtils.dp2px(8);
                        outRect.bottom = ConvertUtils.dp2px(20);
                    }
                }).setThemeColor("#FFFFA611")
                .setOnPrepareChangeVidListener(new PLVPreviousView.PLVPreviousViewInterface.OnPrepareChangeVideoVidListener() {
                    @Override
                    public void onPrepareChangeVideoVid(String vid) {
                        if (onViewActionListener != null) {
                            onViewActionListener.onChangePlaybackVidAndPlay(vid);
                        }
                    }
                });
        plvPreviousView.setParams(builder);
        //注册presenter
        if (previousPresenter != null) {
            previousPresenter.registerView(plvPreviousView.getPreviousView());
        }
        //注意这里要判断vid是否为空，为空才会去请求
        if (liveRoomDataManager.getConfig().getVid() == null || liveRoomDataManager.getConfig().getVid().isEmpty()) {
            //当进入的时候没有输入vid，那么这里就要请求回放视频列表
            plvPreviousView.requestPreviousList();
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

    // <editor-fold defaultstate="collapsed" desc="播放器 - 进度条拖动事件处理">
    private SeekBar.OnSeekBarChangeListener playProgressChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            isPlaySbDragging = true;
            if (onViewActionListener != null) {
                int seekPosition = (int) ((long) onViewActionListener.onGetDurationAction() * progress / seekBar.getMax());
                playTimeTv.setText(PLVTimeUtils.generateTime(seekPosition, true));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setSelected(false);
            isPlaySbDragging = false;
            if (onViewActionListener != null) {
                onViewActionListener.onSeekToAction(seekBar.getProgress(), seekBar.getMax());
            }
        }
    };
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

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_control_iv) {
            if (onViewActionListener != null) {
                v.setSelected(onViewActionListener.onPauseOrResumeClick(v));
            }
        } else if (id == R.id.more_iv) {
            float currentSpeed = onViewActionListener == null ? 1 : onViewActionListener.onGetSpeedAction();
            morePopupView.showPlaybackMoreLayout(v, currentSpeed, liveRoomDataManager.getConfig().getChannelId(), new PLVECMorePopupView.OnPlaybackMoreClickListener() {
                @Override
                public void onChangeSpeedClick(View view, float speed) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onChangeSpeedClick(view, speed);
                    }
                }

                @Override
                public void onClickDynamicFunction(String event) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickDynamicFunction(event);
                    }
                }
            });
        } else if (id == R.id.more_video_list_iv) {
            //弹出更多回放视频的popview
            if (previousPopupView == null) {
                previousPopupView = new PLVECPreviousDialogFragment();
            }
            if (plvPreviousView != null) {
                previousPopupView.setPrviousView(plvPreviousView);
            }
            previousPopupView.showPlaybackMoreVideoDialog(dataList, currentVid, this);
            //设置DialogFragment隐藏时的回调方法
            previousPopupView.setDismissListener(new IPLVECPreviousDialogFragment.DismissListener() {
                @Override
                public void onDismissListener() {
                    //在销毁的时候将previousPopupView置空，防止previousPopupView销毁的时候因为PLVECPlaybackFragment
                    //持有previousPopupView导致不能销毁成功从而导致内存泄漏
                    previousPopupView = null;
                }
            });
        } else if (id == R.id.playback_commodity_iv) {
            showCommodityLayout();
        } else if (id == R.id.plvec_controller_back_iv) {
            if(PLVScreenUtils.isLandscape(getContext())){
                PLVOrientationManager.getInstance().setPortrait((Activity) getContext());
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener extends PLVECCommonHomeFragment.OnViewActionListener {
        //暂停/播放，true: doResume, false: doPause
        boolean onPauseOrResumeClick(View view);

        //切换倍速
        void onChangeSpeedClick(View view, float speed);

        //跳转播放
        void onSeekToAction(int progress, int max);

        //获取总视频时长
        int onGetDurationAction();

        /**
         * 获取视频当前播放时间
         *
         * @return 时间，单位：毫秒
         */
        int getVideoCurrentPosition();

        //设置播放器的位置
        void onSetVideoViewRectAction(Rect videoViewRect);

        //获取倍速
        float onGetSpeedAction();

        /**
         * 切换回放视频的vid并立即播放视频
         *
         * @param vid
         */
        void onChangePlaybackVidAndPlay(String vid);

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
         * 展示用于跳转微信复制的二维码
         */
        void onShowOpenLink();
    }
    // </editor-fold>
}
