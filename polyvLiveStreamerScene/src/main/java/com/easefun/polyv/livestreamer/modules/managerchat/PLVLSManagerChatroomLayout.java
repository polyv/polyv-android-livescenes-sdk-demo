package com.easefun.polyv.livestreamer.modules.managerchat;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVManagerChatViewModel;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVManagerChatUiState;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVManagerChatVO;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.network.PLVNetworkObserver;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.chatroom.widget.PLVLSChatMsgInputWindow;
import com.easefun.polyv.livestreamer.modules.managerchat.adapter.PLVLSManagerChatroomAdapter;
import com.plv.foundationsdk.component.livedata.Event;
import com.plv.foundationsdk.component.viewmodel.PLVViewModels;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVNetworkUtils;
import com.plv.livescenes.model.PLVEmotionImageVO;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

/**
 * @author Hoshiiro
 */
public class PLVLSManagerChatroomLayout extends FrameLayout implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private static final int LOAD_HISTORY_MSG_COUNT_PER_TIME = 20;

    private PLVBlurView managerChatroomBlurView;
    private TextView managerChatroomTitleTv;
    private View managerChatroomTitleDivider;
    private SwipeRefreshLayout managerChatroomContentRefreshLayout;
    private RecyclerView managerChatroomContentRv;
    private PLVRoundRectLayout managerChatroomInputLayout;
    private ImageView managerChatroomInputIv;
    private TextView managerChatroomInputTv;
    private PLVRoundRectLayout managerChatroomMoreMessageLayout;
    private TextView managerChatroomMoreMessageHintTv;

    private final PLVLSManagerChatroomAdapter managerChatroomAdapter = new PLVLSManagerChatroomAdapter();
    private LinearLayoutManager managerChatroomLayoutManager;

    private final PLVNetworkObserver networkObserver = new PLVNetworkObserver();
    private PLVManagerChatViewModel managerChatViewModel;

    // 布局弹层
    @Nullable
    private PLVMenuDrawer menuDrawer;
    @Nullable
    private Disposable refreshBackgroundTimerDisposable;
    private boolean isMenuDrawerShowing = false;

    private String channelId;
    @Nullable
    private PLVManagerChatVO managerChatVO;
    private SpannableStringBuilder lastInputText = new SpannableStringBuilder();
    private boolean canScrollDown = false;
    private int lowestItemPositionToShowScrollToBottom = -1;
    private int requestLoadHistoryMessageTimes = 0;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLSManagerChatroomLayout(Context context) {
        this(context, null);
    }

    public PLVLSManagerChatroomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLSManagerChatroomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化方法">

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvls_manager_chatroom_layout, this, true);
        findView();

        initViewModel();
        initMessageRecyclerView();

        observeNetworkState();
        observeMessage();
        observeUiState();
        observeNotifyEvent();
    }

    private void findView() {
        managerChatroomBlurView = findViewById(R.id.plvls_manager_chatroom_blur_view);
        managerChatroomTitleTv = findViewById(R.id.plvls_manager_chatroom_title_tv);
        managerChatroomTitleDivider = findViewById(R.id.plvls_manager_chatroom_title_divider);
        managerChatroomContentRefreshLayout = findViewById(R.id.plvls_manager_chatroom_content_refresh_layout);
        managerChatroomContentRv = findViewById(R.id.plvls_manager_chatroom_content_rv);
        managerChatroomInputLayout = findViewById(R.id.plvls_manager_chatroom_input_layout);
        managerChatroomInputIv = findViewById(R.id.plvls_manager_chatroom_input_iv);
        managerChatroomInputTv = findViewById(R.id.plvls_manager_chatroom_input_tv);
        managerChatroomMoreMessageLayout = findViewById(R.id.plvls_manager_chatroom_more_message_layout);
        managerChatroomMoreMessageHintTv = findViewById(R.id.plvls_manager_chatroom_more_message_hint_tv);

        managerChatroomInputLayout.setOnClickListener(this);
        managerChatroomMoreMessageLayout.setOnClickListener(this);
    }

    private void initViewModel() {
        managerChatViewModel = PLVViewModels.on((ViewModelStoreOwner) getContext()).get(PLVManagerChatViewModel.class);
    }

    private void initMessageRecyclerView() {
        managerChatroomLayoutManager = new LinearLayoutManager(getContext());
        managerChatroomContentRv.setLayoutManager(managerChatroomLayoutManager);
        managerChatroomContentRv.setAdapter(managerChatroomAdapter);

        managerChatroomContentRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        managerChatroomContentRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processRequestLoadMoreMessage();
            }
        });

        managerChatroomContentRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                final int lastVisiblePos = managerChatroomLayoutManager.findLastVisibleItemPosition();
                setMessageAlreadyRead(lastVisiblePos);
                updateScrollToBottomButton(lastVisiblePos);
                canScrollDown = recyclerView.canScrollVertically(1);
            }
        });
    }

    private void observeNetworkState() {
        networkObserver.start(getContext(), (LifecycleOwner) getContext());
        networkObserver.addNetworkCallback(new PLVNetworkObserver.NetworkCallback() {
            @Override
            public void onNetworkConnected() {
                updateInputHintView();
            }

            @Override
            public void onNetworkDisconnected() {
                updateInputHintView();
            }
        });
    }

    private void observeMessage() {
        managerChatViewModel.getManagerChatLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVManagerChatVO>() {
            @Override
            public void onChanged(@Nullable PLVManagerChatVO managerChatVO) {
                PLVLSManagerChatroomLayout.this.managerChatVO = managerChatVO;
                if (managerChatVO == null) {
                    return;
                }
                processMessageUpdate(managerChatVO);
            }
        });
    }

    private void observeUiState() {
        managerChatViewModel.getUiStateLiveData().observe((LifecycleOwner) getContext(), new Observer<PLVManagerChatUiState>() {
            @Override
            public void onChanged(@Nullable PLVManagerChatUiState uiState) {
                if (uiState == null) {
                    return;
                }
                updateUnreadMessage(uiState.getUnreadMessageCount());
                updateIsLoadingHistoryMessage(uiState.isHistoryMessageLoading());
                updateCanLoadMoreHistoryMessage(uiState.isCanLoadMoreHistoryMessage());
            }
        });
    }

    private void observeNotifyEvent() {
        managerChatViewModel.getNotifyMsgLiveData().observe((LifecycleOwner) getContext(), new Observer<List<Event<String>>>() {
            @Override
            public void onChanged(@Nullable List<Event<String>> events) {
                if (events == null || events.isEmpty()) {
                    return;
                }
                final Event<String> event = events.get(0);
                if (event == null) {
                    return;
                }
                final String content = event.get();
                if (content == null) {
                    managerChatViewModel.removeNotifyMsg(event);
                    return;
                }
                PLVToast.Builder.context(getContext())
                        .setText(content)
                        .show();
                managerChatViewModel.removeNotifyMsg(event);
            }
        });
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void init(
            IPLVLiveRoomDataManager liveRoomDataManager,
            IPLVChatroomContract.IChatroomPresenter chatroomPresenter
    ) {
        this.channelId = liveRoomDataManager.getConfig().getChannelId();
        if (managerChatroomAdapter != null) {
            managerChatroomAdapter.setUserId(liveRoomDataManager.getConfig().getUser().getViewerId());
        }
        if (managerChatViewModel != null) {
            managerChatViewModel.init(chatroomPresenter);
        }
    }

    public void show() {
        if (menuDrawer == null) {
            initMenuDrawerAndShow();
            processRequestLoadMoreMessage();
        } else {
            menuDrawer.attachToContainer();
            menuDrawer.openMenu();
        }
        processScrollToBottom();
    }

    public void hide() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isShowing() {
        return isMenuDrawerShowing;
    }

    public void destroy() {
        if (refreshBackgroundTimerDisposable != null) {
            refreshBackgroundTimerDisposable.dispose();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private void initMenuDrawerAndShow() {
        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        menuDrawer = PLVMenuDrawer.attach(
                (Activity) getContext(),
                PLVMenuDrawer.Type.OVERLAY,
                Position.RIGHT,
                PLVMenuDrawer.MENU_DRAG_CONTAINER,
                (ViewGroup) ((Activity) getContext()).findViewById(R.id.plvls_live_room_popup_container)
        );
        menuDrawer.setMenuView(this);
        menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
        menuDrawer.setMenuSize((int) (landscapeWidth * 0.56));
        menuDrawer.setDrawOverlay(false);
        menuDrawer.setDropShadowEnabled(false);
        menuDrawer.openMenu();
        menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                isMenuDrawerShowing = newState != PLVMenuDrawer.STATE_CLOSED;
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    menuDrawer.detachToContainer();
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });

        isMenuDrawerShowing = true;

        PLVBlurUtils.initBlurView(managerChatroomBlurView);

        refreshBackgroundTimerDisposable = Observable.timer(100, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        if (isMenuDrawerShowing && managerChatroomBlurView != null) {
                            managerChatroomBlurView.invalidate();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        PLVCommonLog.exception(throwable);
                    }
                });
    }

    private void setMessageAlreadyRead(int lastVisiblePosition) {
        if (managerChatVO == null
                || managerChatVO.getChatEventWrapVOList() == null
                || lastVisiblePosition < 0
                || lastVisiblePosition >= managerChatVO.getChatEventWrapVOList().size()) {
            return;
        }
        final PLVChatEventWrapVO chatEventWrapVO = managerChatVO.getChatEventWrapVOList().get(lastVisiblePosition);
        managerChatViewModel.setMessageAlreadyRead(chatEventWrapVO);
    }

    private void updateScrollToBottomButton(int lastVisiblePosition) {
        if (managerChatVO == null
                || managerChatVO.getChatEventWrapVOList() == null
                || !needShowScrollToBottomButton(lastVisiblePosition)) {
            managerChatroomMoreMessageLayout.setVisibility(GONE);
            return;
        }
        managerChatroomMoreMessageLayout.setVisibility(VISIBLE);
    }

    private boolean needShowScrollToBottomButton(int lastVisiblePosition) {
        if (managerChatVO == null || managerChatVO.getChatEventWrapVOList() == null) {
            return false;
        }
        if (lastVisiblePosition <= lowestItemPositionToShowScrollToBottom) {
            return true;
        }
        final List<PLVChatEventWrapVO> chatEvents = managerChatVO.getChatEventWrapVOList();
        int invisibleMessageCount = 0;
        for (int i = lastVisiblePosition + 1; i < chatEvents.size(); i++) {
            invisibleMessageCount += chatEvents.get(i).getEvents().size();
            if (invisibleMessageCount >= 8) {
                lowestItemPositionToShowScrollToBottom = Math.max(lowestItemPositionToShowScrollToBottom, lastVisiblePosition);
                return true;
            }
        }
        return false;
    }

    private void updateUnreadMessage(int unreadMessageCount) {
        final String text = unreadMessageCount > 0 ? format(getContext().getString(R.string.plvls_manager_chatroom_more_message_has_unread_message), unreadMessageCount) : getContext().getString(R.string.plvls_manager_chatroom_more_message_scroll_to_bottom);
        managerChatroomMoreMessageHintTv.setText(text);
    }

    private void updateCanLoadMoreHistoryMessage(boolean canLoadMoreMessage) {
        managerChatroomContentRefreshLayout.setEnabled(canLoadMoreMessage);
    }

    private void updateIsLoadingHistoryMessage(boolean isLoadingMessage) {
        managerChatroomContentRefreshLayout.setRefreshing(isLoadingMessage);
    }

    private void processMessageUpdate(@NonNull PLVManagerChatVO managerChatVO) {
        if (managerChatroomAdapter != null) {
            managerChatroomAdapter.setChatMessages(managerChatVO.getChatEventWrapVOList());
        }
        if (isShowing() && !canScrollDown) {
            processScrollToBottom();
        }
    }

    private void processClickInput() {
        final boolean isNetworkConnected = PLVNetworkUtils.isConnected(getContext());
        if (!isNetworkConnected) {
            PLVToast.Builder.context(getContext())
                    .setText(R.string.plvls_manager_chatroom_input_hint_check_network)
                    .show();
            return;
        }

        PLVLSChatMsgInputWindow.setLastInputText(lastInputText);
        final Intent intent = new Intent(getContext(), PLVLSChatMsgInputWindow.class);
        intent.putExtra(PLVLSChatMsgInputWindow.SHOW_EMOTION_TAB, false);
        PLVLSChatMsgInputWindow.show((Activity) getContext(), intent, new PLVLSChatMsgInputWindow.MessageSendListener() {
            @Override
            public void onSendImg(PolyvSendLocalImgEvent imgEvent) {
                managerChatViewModel.sendImageMessage(imgEvent);
                processScrollToBottom();
            }

            @Override
            public boolean onSendQuoteMsg(String message) {
                // not support
                return false;
            }

            @Override
            public boolean onSendEmotion(PLVEmotionImageVO.EmotionImage emotionImage) {
                // not support
                return false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastInputText = new SpannableStringBuilder(s);
                updateInputHintView();
            }

            @Override
            public boolean onSendMsg(String message) {
                final boolean success = managerChatViewModel.sendTextMessage(message);
                if (success) {
                    lastInputText.clear();
                    updateInputHintView();
                    processScrollToBottom();
                }
                return success;
            }
        });
    }

    private void updateInputHintView() {
        final boolean isNetworkConnected = PLVNetworkUtils.isConnected(getContext());
        final boolean isInputTextEmpty = TextUtils.isEmpty(lastInputText);
        final boolean showInputHint = !isNetworkConnected || isInputTextEmpty;
        if (showInputHint) {
            final String hintText = isNetworkConnected ? getContext().getString(R.string.plvls_manager_chatroom_input_tv_hint) : getContext().getString(R.string.plvls_manager_chatroom_input_tv_hint_network_disconnect);
            managerChatroomInputTv.setText("");
            managerChatroomInputTv.setHint(hintText);
        } else {
            managerChatroomInputTv.setText(lastInputText);
        }
        managerChatroomInputIv.setVisibility(showInputHint ? VISIBLE : GONE);
    }

    private void processRequestLoadMoreMessage() {
        final int alreadyLoadedHistoryMessageCount = requestLoadHistoryMessageTimes * LOAD_HISTORY_MSG_COUNT_PER_TIME;
        managerChatViewModel.requestChatHistory(channelId, alreadyLoadedHistoryMessageCount, alreadyLoadedHistoryMessageCount + LOAD_HISTORY_MSG_COUNT_PER_TIME);
        requestLoadHistoryMessageTimes++;
    }

    private void processScrollToBottom() {
        managerChatroomContentRv.scrollBy(0, Integer.MAX_VALUE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件回调">

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if (viewId == managerChatroomInputLayout.getId()) {
            processClickInput();
        } else if (viewId == managerChatroomMoreMessageLayout.getId()) {
            processScrollToBottom();
        }
    }

    // </editor-fold>
}
