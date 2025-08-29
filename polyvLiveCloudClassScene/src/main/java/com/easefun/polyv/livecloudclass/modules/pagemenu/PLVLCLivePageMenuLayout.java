package com.easefun.polyv.livecloudclass.modules.pagemenu;

import static com.plv.foundationsdk.utils.PLVSugarUtil.getNullableOrDefault;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.PLVLCChatFragment;
import com.easefun.polyv.livecloudclass.modules.chatroom.PLVLCQuizFragment;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder.PLVLCMessageViewHolder;
import com.easefun.polyv.livecloudclass.modules.chatroom.chatmore.PLVLCChatMoreFloatingView;
import com.easefun.polyv.livecloudclass.modules.pagemenu.aisummary.PLVLCAISummaryFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.chapter.PLVLCPlaybackChapterFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.commodity.PLVLCProductPushCardLayout;
import com.easefun.polyv.livecloudclass.modules.pagemenu.desc.PLVLCLiveDescFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.desc.PLVLCLiveDescOfflineFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.iframe.PLVLCIFrameFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.member.PLVLCMemberListPortFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.previous.PLVLCPlaybackPreviousFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.product.PLVLCProductFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.product.PLVLCProductLayout;
import com.easefun.polyv.livecloudclass.modules.pagemenu.question.PLVLCQAFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.text.PLVLCTextFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.tuwen.PLVLCTuWenFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.venue.PLVLCMultipleVenueFragment;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVLiveRoomDataMapper;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.PLVCommodityViewModel;
import com.easefun.polyv.livecommon.module.modules.interact.cardpush.PLVCardPushManager;
import com.easefun.polyv.livecommon.module.modules.interact.lottery.PLVLotteryManager;
import com.easefun.polyv.livecommon.module.modules.player.live.enums.PLVLiveStateEnum;
import com.easefun.polyv.livecommon.module.modules.previous.contract.IPLVPreviousPlaybackContract;
import com.easefun.polyv.livecommon.module.modules.previous.presenter.PLVPreviousPlaybackPresenter;
import com.easefun.polyv.livecommon.module.modules.socket.IPLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.modules.socket.PLVAbsOnSocketEventListener;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVLanguageSwitchPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.PLVPlaySettingPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVViewPagerAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVMagicIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.PLVViewPagerHelper;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.PLVCommonNavigator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.IPLVPagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.abs.PLVCommonNavigatorAdapter;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.indicators.PLVLinePagerIndicator;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.PLVColorTransitionPagerTitleView;
import com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.titles.PLVSimplePagerTitleView;
import com.easefun.polyv.livescenes.model.PLVEmotionImageVO;
import com.easefun.polyv.livescenes.model.PolyvChatFunctionSwitchVO;
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.foundationsdk.component.di.PLVDependManager;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.feature.interact.vo.PLVInteractNativeAppParams;
import com.plv.livescenes.feature.pagemenu.PLVAISummaryWebView;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.livescenes.model.interact.PLVWebviewUpdateAppStatusVO;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackGetDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.playback.chat.PLVChatPlaybackFootDataListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackManager;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.commodity.PLVProductMenuSwitchEvent;
import com.plv.socket.event.interact.PLVCallAppEvent;
import com.plv.socket.event.interact.PLVShowJobDetailEvent;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.NetworkUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * 直播页面菜单布局，实现 IPLVLCLivePageMenuLayout 接口
 */
public class PLVLCLivePageMenuLayout extends FrameLayout implements IPLVLCLivePageMenuLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVCommodityViewModel commodityViewModel = PLVDependManager.getInstance().get(PLVCommodityViewModel.class);

    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //socket登录管理器
    private IPLVSocketLoginManager socketLoginManager;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    private IPLVChatroomContract.IChatroomView chatroomMvpView;
    //卡片推送管理器
    private PLVCardPushManager cardPushManager = new PLVCardPushManager();
    //无条件抽奖挂件管理器
    private PLVLotteryManager lotteryManager = new PLVLotteryManager();
    //更多浮层view
    private PLVLCChatMoreFloatingView chatMoreFloatingView;
    //语言切换弹窗
    private PLVLanguageSwitchPopupWindow languageSwitchPopupWindow;
    //播放设置弹窗
    private PLVPlaySettingPopupWindow playSettingPopupWindow;

    //聊天回放管理器
    private IPLVChatPlaybackManager chatPlaybackManager;
    private boolean chatPlaybackEnabled;
    //播放器准备完成的聊天回放任务
    private List<Triple<String, String, String>> needAddedChatPlaybackTask = new ArrayList<>();

    //播放器准备完成的AI看任务
    private List<Pair<String, String>> needAddedAISummaryTask = new ArrayList<>();

    //回放Presenter
    private IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter previousPlaybackPresenter;

    //横竖屏聊天共用的列表
    private PLVLCChatCommonMessageList chatCommonMessageList;

    //view事件监听器
    private OnViewActionListener onViewActionListener;

    //直播页面菜单tabView、viewpager
    private PLVMagicIndicator pageMenuTabIndicator;
    private ViewPager pageMenuTabViewPager;
    private PLVViewPagerAdapter pageMenuTabAdapter;
    //直播页面菜单tabFragment列表
    private List<Fragment> pageMenuTabFragmentList;
    //直播页面菜单tab标题列表
    private List<String> pageMenuTabTitleList;

    //空布局
    private ViewGroup emptyLy;

    //tab
    private PLVLCLiveDescFragment liveDescFragment; //直播介绍tab页
    private PLVLCLiveDescOfflineFragment liveDescOfflineFragment;
    private PLVLCTextFragment textFragment;//自定义图文菜单tab页
    private PLVLCIFrameFragment iFrameFragment;//推广外链tab页
    private PLVLCTuWenFragment tuWenFragment;//图文直播tab页
    private PLVLCAISummaryFragment aiSummaryFragment;//AI看tab页
    private PLVLCQuizFragment quizFragment;//咨询提问tab页
    private PLVLCChatFragment chatFragment;//互动聊天tab页
    private PLVLCQAFragment questionsAndAnswersFragment;//问答tab页
    private PLVLCPlaybackPreviousFragment previousFragment;//往期视频tab页
    private PLVLCPlaybackChapterFragment chapterFragment;//章节tab页
    private PLVLCProductFragment productFragment;
    private PLVLCMemberListPortFragment memberListFragment;
    private PLVLCMultipleVenueFragment venueFragment;

    private PLVLCProductPushCardLayout productPushCardLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLivePageMenuLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLivePageMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLivePageMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        if (ScreenUtils.isPortrait()) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.INVISIBLE);
        }
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_page_menu_layout, this, true);

        pageMenuTabIndicator = findViewById(R.id.chatroom_tab);
        pageMenuTabViewPager = findViewById(R.id.chatroom_vp);
        pageMenuTabTitleList = new ArrayList<>();
        pageMenuTabFragmentList = new ArrayList<>();
        pageMenuTabAdapter = new PLVViewPagerAdapter(((AppCompatActivity) getContext()).getSupportFragmentManager(), pageMenuTabFragmentList);
        pageMenuTabViewPager.setOffscreenPageLimit(Integer.MAX_VALUE >> 1);
        pageMenuTabViewPager.setAdapter(pageMenuTabAdapter);
        PLVCommonNavigator commonNavigator = new PLVCommonNavigator(getContext());
        commonNavigator.setAdapter(new PLVCommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return pageMenuTabAdapter.getCount();
            }

            @Override
            public IPLVPagerTitleView getTitleView(Context context, final int index) {
                if (pageMenuTabTitleList.isEmpty() || pageMenuTabTitleList.size() < index + 1) {
                    return null;
                }
                PLVSimplePagerTitleView simplePagerTitleView = new PLVColorTransitionPagerTitleView(context);
                simplePagerTitleView.setPadding(ConvertUtils.dp2px(16), 0, ConvertUtils.dp2px(16), 0);
                simplePagerTitleView.setNormalColor(Color.parseColor("#ADADC0"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#FFFFFF"));
                simplePagerTitleView.setText(pageMenuTabTitleList.get(index));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pageMenuTabViewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPLVPagerIndicator getIndicator(Context context) {
                PLVLinePagerIndicator linePagerIndicator = new PLVLinePagerIndicator(context);
                linePagerIndicator.setMode(PLVLinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setLineHeight(ConvertUtils.dp2px(2));
                linePagerIndicator.setXOffset(0);
                linePagerIndicator.setRoundRadius(ConvertUtils.dp2px(1f));
                linePagerIndicator.setColors(Color.parseColor("#FFFFFF"));
                return linePagerIndicator;
            }
        });
        pageMenuTabIndicator.setNavigator(commonNavigator);
        PLVViewPagerHelper.bind(pageMenuTabIndicator, pageMenuTabViewPager);

        chatCommonMessageList = new PLVLCChatCommonMessageList(getContext());
        restoreChatTabForMessageList(chatCommonMessageList);

        emptyLy = findViewById(R.id.plvlc_chatroom_empty_ly);
        productPushCardLayout = findViewById(R.id.plvlc_product_push_card_layout);
    }

    private void initChatroomMvpView(IPLVChatroomContract.IChatroomPresenter presenter) {
        chatroomMvpView = new PLVAbsChatroomView() {
            @Override
            public void onProductMenuSwitchEvent(@NonNull final PLVProductMenuSwitchEvent productMenuSwitchEvent) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        final boolean enable = getNullableOrDefault(new PLVSugarUtil.Supplier<Boolean>() {
                            @Override
                            public Boolean get() {
                                return productMenuSwitchEvent.getContent().isEnabled();
                            }
                        }, false);
                        final String menuName = getNullableOrDefault(new PLVSugarUtil.Supplier<String>() {
                            @Override
                            public String get() {
                                return productMenuSwitchEvent.getContent().getName();
                            }
                        }, "");

                        final boolean needUpdate;
                        if (enable) {
                            needUpdate = addBuyProductTab(menuName);
                        } else {
                            needUpdate = removeBuyProductTab();
                        }
                        if (needUpdate) {
                            refreshPageMenuTabAdapter();
                        }
                    }
                });
            }
        };

        presenter.registerView(chatroomMvpView);
    }

    private void initChatMoreFloatingView() {
        chatMoreFloatingView = findViewById(R.id.plvlc_chat_more_floating_view);
        chatMoreFloatingView.init(liveRoomDataManager);
        chatMoreFloatingView.setOnViewActionListener(new PLVLCChatMoreFloatingView.OnViewActionListener() {
            @Override
            public void onShowBulletinAction() {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowBulletinAction();
                }
            }

            @Override
            public void onShowLanguageAction() {
                languageSwitchPopupWindow.show(liveRoomDataManager.getConfig().getChannelId());
            }

            @Override
            public void onShowPlaySettingAction() {
                playSettingPopupWindow.show();
            }

            @Override
            public void onScreenshot() {
                if (onViewActionListener != null) {
                    onViewActionListener.onScreenshot();
                }
            }

            @Override
            public void onClickDynamicFunction(String event) {
                if (onViewActionListener != null) {
                    onViewActionListener.onClickChatMoreDynamicFunction(event);
                }
            }
        });
    }

    private void initLanguageSwitchPopupWindow() {
        languageSwitchPopupWindow = new PLVLanguageSwitchPopupWindow(this);
    }

    private void initPlaySettingPopupWindow() {
        playSettingPopupWindow = new PLVPlaySettingPopupWindow(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - 重建时的初始化逻辑">

    /**
     * 重建 chatFragment 的 chatCommonMessageList
     *
     * @param messageList
     * @see #checkAddChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean, boolean)
     */
    private void restoreChatTabForMessageList(PLVLCChatCommonMessageList messageList) {
        if (chatFragment == null) {
            chatFragment = tryGetRestoreFragment(PLVLCChatFragment.class);
        }
        if (chatFragment == null || messageList == null || liveRoomDataManager == null) {
            return;
        }
        chatFragment.init(messageList, liveRoomDataManager);
    }

    /**
     * 重建 chatFragment 的 chatroomPresenter
     *
     * @param presenter
     * @see #checkAddChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean, boolean)
     */
    private void restoreChatTabForPresenter(IPLVChatroomContract.IChatroomPresenter presenter) {
        if (chatFragment == null) {
            chatFragment = tryGetRestoreFragment(PLVLCChatFragment.class);
        }
        if (chatFragment == null) {
            return;
        }
        chatFragment.setCardPushManager(cardPushManager);
        chatFragment.setIsChatPlaybackLayout(isChatPlaybackEnabled());
        if (chatPlaybackManager != null) {
            chatPlaybackManager.addOnCallDataListener(chatFragment.getChatPlaybackDataListener());
        }
        if (presenter != null) {
            presenter.registerView(chatFragment.getChatroomView());
        }
    }

    /**
     * 重建 quizFragment 的 chatroomPresenter
     *
     * @param presenter
     * @see #addQuizTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean)
     */
    private void restoreQuizTabForPresenter(IPLVChatroomContract.IChatroomPresenter presenter) {
        if (quizFragment == null) {
            quizFragment = tryGetRestoreFragment(PLVLCQuizFragment.class);
        }
        if (quizFragment == null || presenter == null) {
            return;
        }
        presenter.registerView(quizFragment.getChatroomView());
    }

    /**
     * 尝试从FragmentManager中取出需要恢复状态的Fragment
     *
     * @param expectedClass
     * @param <T>           should be a {@link Fragment}
     * @return expected fragment if found, otherwise null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Fragment> T tryGetRestoreFragment(@NonNull Class<T> expectedClass) {
        if (!(getContext() instanceof FragmentActivity)) {
            return null;
        }
        try {
            List<Fragment> fragments = ((FragmentActivity) getContext()).getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (expectedClass.equals(fragment.getClass())) {
                    return (T) fragment;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现IPLVLCLivePageMenuLayout定义的方法">
    @Override
    public void init(IPLVLiveRoomDataManager liveRoomDataManager) {
        this.liveRoomDataManager = liveRoomDataManager;

        initChatPlaybackManager();
        initChatMoreFloatingView();
        initLanguageSwitchPopupWindow();
        initPlaySettingPopupWindow();

        chatCommonMessageList.init(liveRoomDataManager);
        productPushCardLayout.init(liveRoomDataManager);

        this.chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        this.chatroomPresenter.init();
        //获取表情列表
        chatroomPresenter.getChatEmotionImages();
        initChatroomMvpView(chatroomPresenter);
        restoreChatTabForPresenter(chatroomPresenter);
        restoreQuizTabForPresenter(chatroomPresenter);

        if (!NetworkUtils.isConnected()) {
            tryAddOfflineDescTabForPlaybackCache();
        }

        initSocketLoginManager();
        observeClassDetailVO();
        observePointRewardOpen();
        observeInteractData();
    }

    @Override
    public PLVLCChatCommonMessageList getChatCommonMessageList() {
        return chatCommonMessageList;
    }

    @Override
    public IPLVChatroomContract.IChatroomPresenter getChatroomPresenter() {
        return chatroomPresenter;
    }

    @Override
    public PLVCardPushManager getCardPushManager() {
        return cardPushManager;
    }

    @Override
    public PLVLotteryManager getLotteryManager() {
        return lotteryManager;
    }

    @Override
    public IPLVChatPlaybackManager getChatPlaybackManager() {
        return chatPlaybackManager;
    }

    @Override
    public IPLVPreviousPlaybackContract.IPreviousPlaybackPresenter getPreviousPresenter() {
        return previousPlaybackPresenter;
    }

    @Override
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void addOnViewerCountListener(IPLVOnDataChangedListener<Long> listener) {
        chatroomPresenter.getData().getViewerCountData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void addOnViewOnlineCountData(IPLVOnDataChangedListener<Integer> listener) {
        chatroomPresenter.getData().getOnlineCountData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void onPlaybackVideoPrepared(String sessionId, String channelId, String fileId) {
        if (needAddedChatPlaybackTask != null) {
            needAddedChatPlaybackTask.add(new Triple<>(sessionId, channelId, fileId));
        }
        if (isChatPlaybackEnabled() && chatPlaybackManager != null) {
            chatPlaybackManager.start(sessionId, channelId, fileId);
        }
    }

    @Override
    public void onAISummaryPrepared(String playDataId, String playDataType) {
        if (needAddedAISummaryTask != null) {
            needAddedAISummaryTask.add(new Pair<>(playDataId, playDataType));
        }
        if (aiSummaryFragment != null) {
            aiSummaryFragment.setPlayDataIdAndType(playDataId, playDataType);
        }
    }

    @Override
    public void onPlaybackVideoSeekComplete(int time) {
        if (isChatPlaybackEnabled() && chatPlaybackManager != null) {
            chatPlaybackManager.seek(time);
        }
    }

    @Override
    public boolean isChatPlaybackEnabled() {
        return chatPlaybackEnabled;
    }

    @Override
    public void updateLiveStatus(PLVLiveStateEnum liveStateEnum) {
        if (liveDescFragment != null) {
            liveDescFragment.updateLiveStatus(liveStateEnum);
        }
        if (tuWenFragment != null) {
            tuWenFragment.updateLiveStatus(liveStateEnum);
        }
    }

    @Nullable
    @Override
    public PLVChatQuoteVO getChatQuoteContent() {
        if (chatFragment != null) {
            return chatFragment.getChatQuoteContent();
        }
        return null;
    }

    @Override
    public void onCloseChatQuote() {
        if (chatFragment != null) {
            chatFragment.onCloseChatQuote();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (chatCommonMessageList != null && chatCommonMessageList.onBackPressed()) {
            return true;
        }
        if (pageMenuTabAdapter != null && pageMenuTabAdapter.getCount() > 1) {
            Fragment selFragment = pageMenuTabAdapter.getItem(pageMenuTabViewPager.getCurrentItem());
            if (selFragment instanceof PLVLCIFrameFragment) {
                return ((PLVLCIFrameFragment) selFragment).onBackPressed();
            } else if (selFragment instanceof PLVLCChatFragment) {
                return ((PLVLCChatFragment) selFragment).onBackPressed();
            } else if (selFragment instanceof PLVLCQuizFragment) {
                return ((PLVLCQuizFragment) selFragment).onBackPressed();
            } else if (selFragment instanceof PLVLCLiveDescFragment) {
                return ((PLVLCLiveDescFragment) selFragment).onBackPressed();
            } else if (selFragment instanceof PLVLCTextFragment) {
                return ((PLVLCTextFragment) selFragment).onBackPressed();
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        destroySocketLoginManager();
        if (cardPushManager != null) {
            cardPushManager.disposeCardPushAllTask();
        }
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
        }
        if (chatPlaybackManager != null) {
            chatPlaybackManager.destroy();
        }
        if (lotteryManager != null) {
            lotteryManager.destroy();
        }
        //移除聊天室加载图片进度的监听器，避免内存泄漏
        PLVMyProgressManager.removeModuleListener(PLVLCMessageViewHolder.LOADIMG_MOUDLE_TAG);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="直播页面菜单 - 添加tab页">
    private void addDescTab(PolyvLiveClassDetailVO liveClassDetail, PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        liveDescFragment = new PLVLCLiveDescFragment();
        liveDescFragment.init(liveClassDetail);
        pageMenuTabFragmentList.add(liveDescFragment);
    }

    private void addTextTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        textFragment = new PLVLCTextFragment();
        textFragment.init(channelMenusBean.getContent());
        pageMenuTabFragmentList.add(textFragment);
    }

    private void addIFrameTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        iFrameFragment = new PLVLCIFrameFragment();
        iFrameFragment.init(channelMenusBean.getContent());
        pageMenuTabFragmentList.add(iFrameFragment);
    }

    private void addQATab(PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        questionsAndAnswersFragment = new PLVLCQAFragment();

        PLVLiveClassDetailVO.DataBean.QADataBean qaDataBean = new PLVLiveClassDetailVO.DataBean.QADataBean();
        qaDataBean.setChannelId(liveRoomDataManager.getConfig().getChannelId());
        qaDataBean.setRoomId(liveRoomDataManager.getConfig().getChannelId());
        qaDataBean.setSessionId(liveRoomDataManager.getSessionId());
        qaDataBean.setUserId(liveRoomDataManager.getConfig().getUser().getViewerId());
        qaDataBean.setUserPic(liveRoomDataManager.getConfig().getUser().getViewerAvatar());
        qaDataBean.setUserNick(liveRoomDataManager.getConfig().getUser().getViewerName());
        qaDataBean.setSocketMsg();
        questionsAndAnswersFragment.init(qaDataBean.getSocketMsg(), liveRoomDataManager, getContext());
        pageMenuTabFragmentList.add(questionsAndAnswersFragment);
    }

    private void addAISummaryTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        if (liveRoomDataManager.getConfig().isLive()) {
            return;
        }
        pageMenuTabTitleList.add(channelMenusBean.getName());
        aiSummaryFragment = new PLVLCAISummaryFragment();
        aiSummaryFragment.setOnSeekVideoListener(new PLVAISummaryWebView.OnSeekVideoListener() {
            @Override
            public void onSeekVideo(int timeSecond) {
                if (onViewActionListener != null) {
                    onViewActionListener.onSeekToAction(timeSecond);
                }
            }
        });
        aiSummaryFragment.init(liveRoomDataManager, getContext());
        pageMenuTabFragmentList.add(aiSummaryFragment);
    }

    private void checkStartAISummary() {
        if (needAddedAISummaryTask != null && needAddedAISummaryTask.size() > 0 && aiSummaryFragment != null) {
            Pair<String, String> data = needAddedAISummaryTask.get(needAddedAISummaryTask.size() - 1);
            aiSummaryFragment.setPlayDataIdAndType(data.first, data.second);
        }
        needAddedChatPlaybackTask = null;
    }


    private void addTuWenTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        tuWenFragment = new PLVLCTuWenFragment();
        PLVInteractNativeAppParams appParams = PLVLiveRoomDataMapper.toInteractNativeAppParams(liveRoomDataManager);
        tuWenFragment.init(liveRoomDataManager, appParams, getContext());
        pageMenuTabFragmentList.add(tuWenFragment);
    }

    private void addQuizTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean, PLVLiveClassDetailVO detailVO) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        if (quizFragment == null) {
            quizFragment = new PLVLCQuizFragment();
            quizFragment.setTips(channelMenusBean.getContent());
            quizFragment.setTeacherInfo(detailVO.getData().getTeacher());
            chatroomPresenter.registerView(quizFragment.getChatroomView());
        }
        pageMenuTabFragmentList.add(quizFragment);
    }

    private void checkAddChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean, boolean isDisplayEnabled) {
        if (isDisplayEnabled) {
            pageMenuTabTitleList.add(channelMenusBean.getName());
            if (chatFragment == null) {
                chatFragment = new PLVLCChatFragment();
                chatFragment.init(chatCommonMessageList, liveRoomDataManager);
                chatFragment.setCardPushManager(cardPushManager);
                chatFragment.setLotteryManager(lotteryManager);
                chatFragment.setIsChatPlaybackLayout(isChatPlaybackEnabled());
                chatPlaybackManager.addOnCallDataListener(chatFragment.getChatPlaybackDataListener());
                chatroomPresenter.registerView(chatFragment.getChatroomView());
            }
            chatFragment.setIsLiveType(liveRoomDataManager.getConfig().isLive());
            chatFragment.setOnViewActionListener(new PLVLCChatFragment.OnViewActionListener() {
                @Override
                public void onShowBulletinAction() {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowBulletinAction();
                    }
                }

                @Override
                public void onShowLanguageAction() {
                    languageSwitchPopupWindow.show(liveRoomDataManager.getConfig().getChannelId());
                }

                @Override
                public void onShowPlaySettingAction() {
                    playSettingPopupWindow.show();
                }

                @Override
                public void onScreenshot() {
                    if (onViewActionListener != null) {
                        onViewActionListener.onScreenshot();
                    }
                }

                @Override
                public void onClickDynamicFunction(String event) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onClickChatMoreDynamicFunction(event);
                    }
                }

                @Override
                public void onReplyMessage(PLVChatQuoteVO chatQuoteVO) {
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

                @Override
                public void onShowRewardAction() {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowRewardAction();
                    }
                }

                @Override
                public void onShowEffectAction(boolean isShow) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowEffectAction(isShow);
                    }
                }

                @Override
                public void onShowQuestionnaire() {
                    if (onViewActionListener != null) {
                        onViewActionListener.onShowQuestionnaire();
                    }
                }
            });
            pageMenuTabFragmentList.add(chatFragment);
        }
        if (chatMoreFloatingView != null) {
            chatMoreFloatingView.setVisibility(!isDisplayEnabled ? View.VISIBLE : View.GONE);
        }
        if (onViewActionListener != null) {
            onViewActionListener.onChatTabPrepared(isChatPlaybackEnabled(), isDisplayEnabled);
        }
    }

    /**
     * 插入往期Fragment
     *
     * @param channelMenusBean 频道的菜单详情
     */
    private void addPreviousTab(PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        if (previousFragment == null) {
            previousFragment = new PLVLCPlaybackPreviousFragment();
        }
        pageMenuTabFragmentList.add(previousFragment);
        if (previousPlaybackPresenter == null) {
            previousPlaybackPresenter = new PLVPreviousPlaybackPresenter(liveRoomDataManager);
        }
        //初始化
        previousFragment.init(previousPlaybackPresenter);
        observerPreviousData();
    }

    /**
     * 检查是否要插入往期Fragment
     */
    private void checkAddPreviousTab() {
        if (previousFragment != null && pageMenuTabFragmentList.contains(previousFragment)) {
            return;
        }
        //1.SDK中未开启点播列表时——默认选择“读取直播后台回放设置”的参数
        //2.SDK中开启“点播列表”/通过vid观看时——优先响应“点播列表”/vid观看开关配置
        if (liveRoomDataManager.getConfig().getVideoListType() == PLVPlaybackListType.VOD
                && !liveRoomDataManager.getConfig().isLive()
                && liveRoomDataManager.getConfig().getVid().isEmpty()) {
            PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean = new PLVLiveClassDetailVO.DataBean.ChannelMenusBean();
            channelMenusBean.setName(PLVAppUtils.getString(R.string.plv_previous_title));
            addPreviousTab(channelMenusBean);
        }
    }

    /**
     * @return {@code true} -> 需要更新列表
     */
    private boolean addBuyProductTab(String menuName) {
        if (productFragment != null && pageMenuTabFragmentList.contains(productFragment)) {
            return false;
        }
        if (productFragment == null) {
            productFragment = new PLVLCProductFragment();
        }
        productFragment.init(liveRoomDataManager);
        productFragment.setOnViewActionListener(new PLVLCProductLayout.OnViewActionListener() {
            @Override
            public void onShowJobDetail(PLVShowJobDetailEvent param) {
                if (onViewActionListener != null) {
                    onViewActionListener.onShowJobDetail(param);
                }
            }

            @Override
            public void onShowOpenLink() {
                onViewActionListener.onShowOpenLink();
            }
        });
        pageMenuTabTitleList.add(menuName);
        pageMenuTabFragmentList.add(productFragment);
        return true;
    }

    /**
     * @return {@code true} -> 需要更新列表
     */
    private boolean removeBuyProductTab() {
        if (productFragment == null) {
            return false;
        }
        pageMenuTabTitleList.remove(pageMenuTabFragmentList.indexOf(productFragment));
        pageMenuTabFragmentList.remove(productFragment);
        productFragment = null;
        return true;
    }

    /**
     * 插入章节Fragment
     */
    private void addChapterTab() {
        if (chapterFragment != null && pageMenuTabFragmentList.contains(chapterFragment)) {
            return;
        }
        pageMenuTabTitleList.add(getResources().getString(R.string.tab_chapter));
        if (chapterFragment == null) {
            chapterFragment = new PLVLCPlaybackChapterFragment();
        }
        pageMenuTabFragmentList.add(chapterFragment);
        refreshPageMenuTabAdapter();
    }

    private void addMemberListTab(String tabName) {
        if (memberListFragment != null && pageMenuTabFragmentList.contains(memberListFragment)) {
            return;
        }
        if (!liveRoomDataManager.getConfig().isLive()) {
            return;
        }
        if (memberListFragment == null) {
            memberListFragment = new PLVLCMemberListPortFragment();
            memberListFragment.init(chatroomPresenter);
        }
        pageMenuTabTitleList.add(tabName);
        pageMenuTabFragmentList.add(memberListFragment);
        refreshPageMenuTabAdapter();
    }

    /**
     * 插入多会场Fragment
     */
    private void checkAddMultiVenueTab(PLVLiveClassDetailVO.DataBean.MultiMeetingSettingBean bean) {
        if (bean == null || !bean.isMultiMeetingEnable()) {
            return;
        }
        if (venueFragment != null && pageMenuTabFragmentList.contains(venueFragment)) {
            return;
        }
        if (venueFragment == null) {
            venueFragment = new PLVLCMultipleVenueFragment();
            venueFragment.init(liveRoomDataManager.getConfig().getChannelId(), bean.getMainChannelId() + "");
        }
        pageMenuTabTitleList.add(getResources().getString(R.string.plv_multi_venue_title));
        pageMenuTabFragmentList.add(venueFragment);
    }

    private void tryAddOfflineDescTabForPlaybackCache() {
        if (liveRoomDataManager.getConfig().isLive()
                || !pageMenuTabTitleList.isEmpty()
                || !pageMenuTabFragmentList.isEmpty()) {
            return;
        }
        if (liveDescOfflineFragment == null) {
            liveDescOfflineFragment = new PLVLCLiveDescOfflineFragment();
        }
        pageMenuTabTitleList.add(PLVAppUtils.getString(R.string.plv_live_intro_2));
        pageMenuTabFragmentList.add(liveDescOfflineFragment);
        refreshPageMenuTabAdapter();
    }

    private void refreshPageMenuTabAdapter() {
        pageMenuTabAdapter.notifyDataSetChanged();
        pageMenuTabIndicator.getNavigator().notifyDataSetChanged();

        if (pageMenuTabViewPager.getCurrentItem() >= pageMenuTabFragmentList.size()
                && pageMenuTabAdapter.getCount() > 0) {
            pageMenuTabViewPager.setCurrentItem(0);
            pageMenuTabIndicator.onPageSelected(0);
        }
        if (pageMenuTabAdapter.getCount() > 0) {
            pageMenuTabIndicator.setBackgroundColor(Color.parseColor("#3E3E4E"));
            findViewById(R.id.split_view).setVisibility(View.VISIBLE);
        }
        emptyLy.setVisibility(pageMenuTabAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天回放 - 初始化">
    private void checkStartChatPlayback() {
        if (needAddedChatPlaybackTask != null && needAddedChatPlaybackTask.size() > 0) {
            Triple<String, String, String> data = needAddedChatPlaybackTask.get(needAddedChatPlaybackTask.size() - 1);
            chatPlaybackManager.start(data.getFirst(), data.getSecond(), data.getThird());
        }
        needAddedChatPlaybackTask = null;
    }

    private void initChatPlaybackManager() {
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
                return chatCommonMessageList == null || chatCommonMessageList.canScrollVertically(1);
            }

            @Override
            public Object[] getParsedEmoObjects(String content) {
                if (chatroomPresenter != null) {
                    return PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(content), chatroomPresenter.getSpeakEmojiSizes(), Utils.getApp());
                } else {
                    return new CharSequence[]{PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(content), ConvertUtils.dp2px(16), Utils.getApp())};
                }
            }
        });
        chatPlaybackManager.addOnCallDataListener(new PLVChatPlaybackFootDataListener(10) {
            @Override
            public void onFootAtTimeDataInserted(List<PLVChatPlaybackData> insertDataList) {
                if (chatFragment != null) {
                    for (PLVChatPlaybackData chatPlaybackData : insertDataList) {
                        boolean isSpecialTypeOrMe = PLVEventHelper.isSpecialType(chatPlaybackData.getUserType())
                                || PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatPlaybackData.getUserId());
                        if (!chatFragment.isDisplaySpecialType()
                                || (chatFragment.isDisplaySpecialType() && isSpecialTypeOrMe)) {
                            if (onViewActionListener != null) {
                                onViewActionListener.onSendDanmuAction((CharSequence) chatPlaybackData.getObjects()[0]);
                            }
                        }
                    }
                }
            }
        });
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
                ToastUtils.showShort(R.string.plv_chat_toast_reconnecting);
            } else {
                ToastUtils.showShort(R.string.plv_chat_toast_logging);
            }
        }

        @Override
        public void handleLoginSuccess(boolean isReconnect) {
            super.handleLoginSuccess(isReconnect);
            if (isReconnect) {
                ToastUtils.showShort(R.string.plv_chat_toast_reconnect_success);
            } else {
                ToastUtils.showShort(R.string.plv_chat_toast_login_success);
            }
        }

        @Override
        public void handleLoginFailed(@NonNull Throwable throwable) {
            super.handleLoginFailed(throwable);
            ToastUtils.showShort(getResources().getString(R.string.plv_chat_toast_login_failed) + ":" + throwable.getMessage());
        }

        @Override
        public void onKickEvent(@NonNull PLVKickEvent kickEvent, boolean isOwn) {
            super.onKickEvent(kickEvent, isOwn);
            if (isOwn) {
                PLVToast.Builder.context(Utils.getApp())
                        .shortDuration()
                        .setText(R.string.plv_chat_toast_been_kicked)
                        .build()
                        .show();
                ((Activity) getContext()).finish();
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
            PLVToast.Builder.context(Utils.getApp())
                    .shortDuration()
                    .setText(R.string.plv_chat_toast_account_login_elsewhere)
                    .build()
                    .show();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((Activity) getContext() != null) {
                        ((Activity) getContext()).finish();
                    }
                }
            }, 3000);
        }
    };

    private void showExitDialog(int messageId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.plv_common_dialog_tip_warm)
                .setMessage(messageId)
                .setPositiveButton(R.string.plv_common_dialog_confirm_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) getContext()).finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转处理">
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        //在横屏时直接隐藏。因为页面菜单位于连麦布局底部，因此在横屏点击播放器右下角因此连麦布局时，可能会把页面菜单露出来。
        // 因此页面菜单在横屏时直接隐藏，竖屏时才显示。
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听聊天室点赞数据、监听文本发言信息">
    private void observeChatroomData() {
        chatroomPresenter.getData().getLikesCountData().observe((LifecycleOwner) getContext(), new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long l) {
                if (l == null) {
                    return;
                }
                if (liveDescFragment != null) {
                    liveDescFragment.setLikesCount(l);
                }
                if (chatFragment != null) {
                    chatFragment.setLikesCount(l);
                }
            }
        });
        chatroomPresenter.getData().getSpeakMessageData().observe((LifecycleOwner) getContext(), new Observer<Pair<CharSequence, Boolean>>() {
            @Override
            public void onChanged(@Nullable Pair<CharSequence, Boolean> charSequenceBooleanPair) {
                if (charSequenceBooleanPair == null) {
                    return;
                }
                CharSequence charSequence = charSequenceBooleanPair.first;
                boolean isSpecialType = charSequenceBooleanPair.second;
                if (chatFragment != null && !isChatPlaybackEnabled()) {
                    if (!chatFragment.isDisplaySpecialType()
                            || (chatFragment.isDisplaySpecialType() && isSpecialType)) {
                        if (onViewActionListener != null) {
                            onViewActionListener.onSendDanmuAction(charSequence);
                        }
                    }
                } else if (!isChatPlaybackEnabled()) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onSendDanmuAction(charSequence);
                    }
                }
            }
        });
        chatroomPresenter.getData().getFunctionSwitchData().observe((LifecycleOwner) getContext(), new Observer<List<PolyvChatFunctionSwitchVO.DataBean>>() {
            @Override
            public void onChanged(@Nullable List<PolyvChatFunctionSwitchVO.DataBean> dataBeans) {
                if (chatroomPresenter != null) {
                    chatroomPresenter.getData().getFunctionSwitchData().removeObserver(this);
                }
                if (chatFragment != null) {
                    chatFragment.acceptFunctionSwitchData(dataBeans);
                }
            }
        });
        chatroomPresenter.getData().getEmotionImages().observe((LifecycleOwner) getContext(), new Observer<List<PLVEmotionImageVO.EmotionImage>>() {
            @Override
            public void onChanged(@Nullable List<PLVEmotionImageVO.EmotionImage> emotionImages) {
                if (chatroomPresenter != null) {
                    chatroomPresenter.getData().getEmotionImages().removeObserver(this);
                }
                if (chatFragment != null) {
                    chatFragment.acceptEmotionImageData(emotionImages);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 互动应用消息">
    private void observeInteractData(){
        liveRoomDataManager.getInteractStatusData().observe((LifecycleOwner) getContext(), new Observer<PLVWebviewUpdateAppStatusVO>() {
            @Override
            public void onChanged(@Nullable PLVWebviewUpdateAppStatusVO plvWebviewUpdateAppStatusVO) {
                if (chatFragment != null && plvWebviewUpdateAppStatusVO != null) {
                    chatFragment.updateChatMoreFunction(plvWebviewUpdateAppStatusVO);
                }
                if (lotteryManager != null) {
                    lotteryManager.acceptLotteryVo(plvWebviewUpdateAppStatusVO);
                }
            }
        });
        liveRoomDataManager.getInteractEntranceData().observe((LifecycleOwner) getContext(), new Observer<List<PLVCallAppEvent.ValueBean.DataBean>>() {
            @Override
            public void onChanged(@Nullable List<PLVCallAppEvent.ValueBean.DataBean> dataBeanList) {
                if (chatFragment != null) {
                    chatFragment.acceptInteractEntranceData(dataBeanList);
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听回放视频的信息：vid变更，seek跳转">
    private void observerPreviousData() {
        if (previousPlaybackPresenter != null) {
            //监听vid的变更
            previousPlaybackPresenter.getData().getPlaybackVideoVidData()
                    .observe((LifecycleOwner) getContext(), new Observer<Pair<String, String>>() {
                        @Override
                        public void onChanged(@Nullable Pair<String, String> vidAndFileId) {
                            if (vidAndFileId == null || TextUtils.isEmpty(vidAndFileId.first)) {
                                return;
                            }
                            onViewActionListener.onChangeVideoVidAction(vidAndFileId.first, vidAndFileId.second);
                        }
                    });

            //监听进度的变更
            previousPlaybackPresenter.getData().getPlayBackVidoSeekData()
                    .observe((LifecycleOwner) getContext(), new Observer<Integer>() {
                        @Override
                        public void onChanged(@Nullable Integer position) {
                            if (position == null) {
                                return;
                            }
                            onViewActionListener.onSeekToAction(position);
                        }
                    });
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听章节tab">
    private void observerChapters() {
        // 监听是否开启章节功能
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> polyvLiveClassDetailVOPLVStatefulData) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (polyvLiveClassDetailVOPLVStatefulData == null
                        || !polyvLiveClassDetailVOPLVStatefulData.isSuccess()
                        || polyvLiveClassDetailVOPLVStatefulData.getData() == null
                        || polyvLiveClassDetailVOPLVStatefulData.getData().getData() == null) {
                    return;
                }
                PLVLiveClassDetailVO.DataBean dataBean = polyvLiveClassDetailVOPLVStatefulData.getData().getData();
                final boolean hasPlaybackVideo = dataBean.isHasPlayback();
                final boolean sectionEnabled = "Y".equals(dataBean.getSectionEnabled());
                final boolean hasRecordFile = dataBean.getRecordFileSimpleModel() != null;
                if (!liveRoomDataManager.getConfig().isLive()
                        && liveRoomDataManager.getConfig().getChannelType() == PLVLiveChannelType.PPT
                        && sectionEnabled
                        && (hasPlaybackVideo || hasRecordFile)) {
                    //开启章节tab
                    addChapterTab();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听直播详情信息">
    private void observeClassDetailVO() {
        //监听 直播间数据管理器对象中的直播详情数据变化
        liveRoomDataManager.getClassDetailVO().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<PolyvLiveClassDetailVO>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable PLVStatefulData<PolyvLiveClassDetailVO> liveClassDetailVO) {
                liveRoomDataManager.getClassDetailVO().removeObserver(this);
                if (liveClassDetailVO == null || !liveClassDetailVO.isSuccess()) {
                    tryAddOfflineDescTabForPlaybackCache();
                    return;
                }
                PolyvLiveClassDetailVO liveClassDetail = liveClassDetailVO.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }
                //频道未开播and回放开关开启and有回放视频and开启聊天重放开关
                chatPlaybackEnabled = liveClassDetail.getData().isChatPlaybackEnabled() && !liveRoomDataManager.getConfig().isLive();

                List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenusBeans = liveClassDetail.getData().getChannelMenus();
                if (channelMenusBeans != null) {
                    setupMenuTabs(liveClassDetail);
                    if (!liveRoomDataManager.getConfig().isLive()) {
                        observerChapters();
                    }
                    observeChatroomData();
                    observePointRewardOpen();
                    checkStartChatPlayback();
                    checkStartAISummary();
                }
            }
        });
    }

    private void setupMenuTabs(final PolyvLiveClassDetailVO liveClassDetail) {
        pageMenuTabTitleList.clear();
        pageMenuTabFragmentList.clear();
        //直播详情中的直播菜单列表
        List<PLVLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenusBeans = liveClassDetail.getData().getChannelMenus();
        PLVLiveClassDetailVO.DataBean.MultiMeetingSettingBean multiMeetBean = liveClassDetail.getData().getMultiMeetingSetting();
        for (PLVLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : channelMenusBeans) {
            if (channelMenusBean == null) {
                continue;
            }
            if (PLVLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                addDescTab(liveClassDetail, channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_CHAT.equals(channelMenusBean.getMenuType())) {
                checkAddChatTab(channelMenusBean, channelMenusBean.isDisplayEnabled());
            } else if (PLVLiveClassDetailVO.MENUTYPE_QUIZ.equals(channelMenusBean.getMenuType())) {
                addQuizTab(channelMenusBean, liveClassDetail);
            } else if (PLVLiveClassDetailVO.MENUTYPE_TEXT.equals(channelMenusBean.getMenuType())) {
                addTextTab(channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_IFRAME.equals(channelMenusBean.getMenuType())) {
                addIFrameTab(channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_TUWEN.equals(channelMenusBean.getMenuType())) {
                addTuWenTab(channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_QA.equals(channelMenusBean.getMenuType())) {
                addQATab(channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_PREVIOUS.equals(channelMenusBean.getMenuType())
                    && !liveRoomDataManager.getConfig().isLive()
                    && liveRoomDataManager.getConfig().getVid().isEmpty()) {
                addPreviousTab(channelMenusBean);
            } else if (PLVLiveClassDetailVO.MENUTYPE_BUY.equals(channelMenusBean.getMenuType())) {
                commodityViewModel.notifyHasProductLayout(true);
                addBuyProductTab(channelMenusBean.getName());
            } else if (PLVLiveClassDetailVO.MENUTYPE_MEMBERS.equals(channelMenusBean.getMenuType())) {
                addMemberListTab(channelMenusBean.getName());
            } else if (PLVLiveClassDetailVO.MENUTYPE_AI_SUMMARY.equals(channelMenusBean.getMenuType())) {
                addAISummaryTab(channelMenusBean);
            }
        }
        if (PLVChannelFeatureManager.onChannel(liveRoomDataManager.getConfig().getChannelId())
                .isFeatureSupport(PLVChannelFeature.LIVE_SHOW_VIEWER_LIST)) {
            addMemberListTab(getContext().getString(R.string.plv_sdk_live_menu_name_members));
        }
        checkAddMultiVenueTab(multiMeetBean);
        checkAddPreviousTab();
        refreshPageMenuTabAdapter();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据监听 - 监听积分打赏是否开启">
    private void observePointRewardOpen() {
        liveRoomDataManager.getPointRewardEnableData().observe((LifecycleOwner) getContext(), new Observer<PLVStatefulData<Boolean>>() {
            @Override
            public void onChanged(@Nullable PLVStatefulData<Boolean> booleanPLVStatefulData) {
                liveRoomDataManager.getPointRewardEnableData().removeObserver(this);
                if (chatFragment != null && booleanPLVStatefulData != null && booleanPLVStatefulData.getData() != null) {
                    chatFragment.setOpenPointReward(booleanPLVStatefulData.getData());
                }

            }
        });
    }
    // </editor-fold>
}
