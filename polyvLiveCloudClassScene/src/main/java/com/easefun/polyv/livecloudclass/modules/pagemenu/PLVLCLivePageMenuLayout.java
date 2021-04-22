package com.easefun.polyv.livecloudclass.modules.pagemenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.PLVLCChatFragment;
import com.easefun.polyv.livecloudclass.modules.chatroom.PLVLCQuizFragment;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCChatCommonMessageList;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder.PLVLCMessageViewHolder;
import com.easefun.polyv.livecloudclass.modules.pagemenu.desc.PLVLCLiveDescFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.iframe.PLVLCIFrameFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.text.PLVLCTextFragment;
import com.easefun.polyv.livecloudclass.modules.pagemenu.tuwen.PLVLCTuWenFragment;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.data.PLVStatefulData;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.socket.IPLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.modules.socket.PLVAbsOnSocketEventListener;
import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketLoginManager;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.module.utils.listener.IPLVOnDataChangedListener;
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
import com.easefun.polyv.livescenes.model.PolyvLiveClassDetailVO;
import com.plv.socket.event.login.PLVKickEvent;
import com.plv.socket.event.login.PLVLoginRefuseEvent;
import com.plv.socket.event.login.PLVReloginEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播页面菜单布局，实现 IPLVLCLivePageMenuLayout 接口
 */
public class PLVLCLivePageMenuLayout extends FrameLayout implements IPLVLCLivePageMenuLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //直播间数据管理器
    private IPLVLiveRoomDataManager liveRoomDataManager;

    //socket登录管理器
    private IPLVSocketLoginManager socketLoginManager;

    //聊天室presenter
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;

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

    //tab
    private PLVLCLiveDescFragment liveDescFragment; //直播介绍tab页
    private PLVLCTextFragment textFragment;//自定义图文菜单tab页
    private PLVLCIFrameFragment iFrameFragment;//推广外链tab页
    private PLVLCTuWenFragment tuWenFragment;//图文直播tab页
    private PLVLCQuizFragment quizFragment;//咨询提问tab页
    private PLVLCChatFragment chatFragment;//互动聊天tab页
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
            setVisibility(View.GONE);
        }
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_page_menu_layout, this, true);

        pageMenuTabIndicator = findViewById(R.id.chatroom_tab);
        pageMenuTabViewPager = findViewById(R.id.chatroom_vp);
        pageMenuTabTitleList = new ArrayList<>();
        pageMenuTabFragmentList = new ArrayList<>();
        pageMenuTabAdapter = new PLVViewPagerAdapter(((AppCompatActivity) getContext()).getSupportFragmentManager(), pageMenuTabFragmentList);
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
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部处理 - 重建时的初始化逻辑">

    /**
     * 重建 chatFragment 的 chatCommonMessageList
     *
     * @param messageList
     * @see #addChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean)
     */
    private void restoreChatTabForMessageList(PLVLCChatCommonMessageList messageList) {
        if (chatFragment == null) {
            chatFragment = tryGetRestoreFragment(PLVLCChatFragment.class);
        }
        if (chatFragment == null || messageList == null) {
            return;
        }
        chatFragment.init(messageList);
    }

    /**
     * 重建 chatFragment 的 chatroomPresenter
     *
     * @param presenter
     * @see #addChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean)
     */
    private void restoreChatTabForPresenter(IPLVChatroomContract.IChatroomPresenter presenter) {
        if (chatFragment == null) {
            chatFragment = tryGetRestoreFragment(PLVLCChatFragment.class);
        }
        if (chatFragment == null || presenter == null) {
            return;
        }
        presenter.registerView(chatFragment.getChatroomView());
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

        this.chatroomPresenter = new PLVChatroomPresenter(liveRoomDataManager);
        this.chatroomPresenter.init();
        restoreChatTabForPresenter(chatroomPresenter);
        restoreQuizTabForPresenter(chatroomPresenter);

        observeClassDetailVO();

        initSocketLoginManager();
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
    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    @Override
    public void addOnViewerCountListener(IPLVOnDataChangedListener<Long> listener) {
        chatroomPresenter.getData().getViewerCountData().observe((LifecycleOwner) getContext(), listener);
    }

    @Override
    public void updateLiveStatusWithLive() {
        if (liveDescFragment != null) {
            liveDescFragment.updateStatusViewWithLive();
        }
    }

    @Override
    public void updateLiveStatusWithNoLive() {
        if (liveDescFragment != null) {
            liveDescFragment.updateStatusViewWithNoLive();
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
        if (chatroomPresenter != null) {
            chatroomPresenter.destroy();
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

    private void addTuWenTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        tuWenFragment = new PLVLCTuWenFragment();
        tuWenFragment.init(liveRoomDataManager.getConfig().getChannelId());
        pageMenuTabFragmentList.add(tuWenFragment);
    }

    private void addQuizTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        if (quizFragment == null) {
            quizFragment = new PLVLCQuizFragment();
            chatroomPresenter.registerView(quizFragment.getChatroomView());
        }
        pageMenuTabFragmentList.add(quizFragment);
    }

    private void addChatTab(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        pageMenuTabTitleList.add(channelMenusBean.getName());
        if (chatFragment == null) {
            chatFragment = new PLVLCChatFragment();
            chatFragment.init(chatCommonMessageList);
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
        });
        pageMenuTabFragmentList.add(chatFragment);
    }

    private void refreshPageMenuTabAdapter() {
        if (pageMenuTabAdapter.getCount() > 0) {
            pageMenuTabAdapter.notifyDataSetChanged();
            pageMenuTabIndicator.setBackgroundColor(Color.parseColor("#3E3E4E"));
            pageMenuTabIndicator.getNavigator().notifyDataSetChanged();
            findViewById(R.id.split_view).setVisibility(View.VISIBLE);
            pageMenuTabViewPager.setOffscreenPageLimit(pageMenuTabAdapter.getCount() - 1);
        }
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
                if (!chatFragment.isDisplaySpecialType()
                        || (chatFragment.isDisplaySpecialType() && isSpecialType)) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onSendDanmuAction(charSequence);
                    }
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
                    return;
                }
                PolyvLiveClassDetailVO liveClassDetail = liveClassDetailVO.getData();
                if (liveClassDetail == null || liveClassDetail.getData() == null) {
                    return;
                }
                //直播详情中的直播菜单列表
                List<PolyvLiveClassDetailVO.DataBean.ChannelMenusBean> channelMenusBeans = liveClassDetail.getData().getChannelMenus();
                if (channelMenusBeans != null) {
                    for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : channelMenusBeans) {
                        if (channelMenusBean == null) {
                            continue;
                        }
                        if (PolyvLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                            addDescTab(liveClassDetail, channelMenusBean);
                        } else if (PolyvLiveClassDetailVO.MENUTYPE_CHAT.equals(channelMenusBean.getMenuType())) {
                            addChatTab(channelMenusBean);
                        } else if (PolyvLiveClassDetailVO.MENUTYPE_QUIZ.equals(channelMenusBean.getMenuType())) {
                            addQuizTab(channelMenusBean);
                        } else if (PolyvLiveClassDetailVO.MENUTYPE_TEXT.equals(channelMenusBean.getMenuType())) {
                            addTextTab(channelMenusBean);
                        } else if (PolyvLiveClassDetailVO.MENUTYPE_IFRAME.equals(channelMenusBean.getMenuType())) {
                            addIFrameTab(channelMenusBean);
                        } else if (PolyvLiveClassDetailVO.MENUTYPE_TUWEN.equals(channelMenusBean.getMenuType())) {
                            addTuWenTab(channelMenusBean);
                        }
                    }
                    refreshPageMenuTabAdapter();
                    observeChatroomData();
                }
            }
        });
    }
    // </editor-fold>
}
