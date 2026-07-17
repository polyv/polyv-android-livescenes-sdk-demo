package com.easefun.polyv.livecommon.module.utils.kickban;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVViewInitUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectLayout;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 开播端封禁用户列表弹层，复用聊天室 presenter 的踢出列表和用户禁言列表接口。
 */
public class PLVKickBannedUsersLayout extends FrameLayout {

    private static final int TAB_KICK = 0;
    private static final int TAB_BANNED = 1;

    private PopupWindow popupWindow;
    private View popupView;
    private PLVRoundRectLayout contentLayout;
    private View tabLy;
    private TextView kickTabTv;
    private TextView bannedTabTv;
    private View tabIndicator;
    private TextView hintTv;
    private RecyclerView userRv;
    private View loadingPb;
    private View emptyLy;
    private TextView emptyTv;

    private final UserAdapter userAdapter = new UserAdapter();
    private IPLVChatroomContract.IChatroomPresenter chatroomPresenter;
    private int currentTab = TAB_KICK;

    public PLVKickBannedUsersLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVKickBannedUsersLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVKickBannedUsersLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        popupWindow = new PopupWindow(getContext());
        popupView = PLVViewInitUtils.initPopupWindow(this, R.layout.plv_kick_banned_users_popup_layout, popupWindow, new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        contentLayout = popupView.findViewById(R.id.plv_kick_banned_users_ly);
        tabLy = popupView.findViewById(R.id.plv_kick_banned_users_tab_ly);
        kickTabTv = popupView.findViewById(R.id.plv_kick_banned_users_kick_tab_tv);
        bannedTabTv = popupView.findViewById(R.id.plv_kick_banned_users_banned_tab_tv);
        tabIndicator = popupView.findViewById(R.id.plv_kick_banned_users_tab_indicator);
        hintTv = popupView.findViewById(R.id.plv_kick_banned_users_hint_tv);
        userRv = popupView.findViewById(R.id.plv_kick_banned_users_rv);
        loadingPb = popupView.findViewById(R.id.plv_kick_banned_users_loading_pb);
        emptyLy = popupView.findViewById(R.id.plv_kick_banned_users_empty_ly);
        emptyTv = popupView.findViewById(R.id.plv_kick_banned_users_empty_tv);

        userRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        userRv.setAdapter(userAdapter);

        contentLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 消费内容区域点击，避免触发外层关闭。
            }
        });
        kickTabTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(TAB_KICK);
            }
        });
        bannedTabTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTab(TAB_BANNED);
            }
        });
    }

    public void bindChatroomPresenter(@Nullable IPLVChatroomContract.IChatroomPresenter presenter) {
        if (chatroomPresenter == presenter) {
            return;
        }
        if (chatroomPresenter != null) {
            chatroomPresenter.unregisterView(chatroomView);
        }
        chatroomPresenter = presenter;
        if (chatroomPresenter != null) {
            chatroomPresenter.registerView(chatroomView);
        }
    }

    public void open() {
        switchTab(TAB_KICK);
        if (ScreenUtils.isPortrait()) {
            onPortrait();
        } else {
            onLandscape();
        }
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public void destroy() {
        if (chatroomPresenter != null) {
            chatroomPresenter.unregisterView(chatroomView);
            chatroomPresenter = null;
        }
        dismiss();
    }

    private void switchTab(int tab) {
        currentTab = tab;
        final boolean isKickTab = currentTab == TAB_KICK;
        kickTabTv.setTextColor(isKickTab ? 0xFFFFFFFF : 0x99FFFFFF);
        kickTabTv.setTypeface(null, isKickTab ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        bannedTabTv.setTextColor(!isKickTab ? 0xFFFFFFFF : 0x99FFFFFF);
        bannedTabTv.setTypeface(null, !isKickTab ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        hintTv.setText(isKickTab ? R.string.plv_kick_banned_users_kick_hint : R.string.plv_kick_banned_users_banned_hint);
        emptyTv.setText(isKickTab ? R.string.plv_kick_banned_users_no_kick : R.string.plv_kick_banned_users_no_banned);
        updateTabIndicatorPosition(isKickTab ? kickTabTv : bannedTabTv);
        showLoading();
        requestCurrentTabData();
    }

    private void updateTabIndicatorPosition(final TextView tabTv) {
        tabIndicator.post(new Runnable() {
            @Override
            public void run() {
                int indicatorLeft = tabLy.getLeft() + tabTv.getLeft() + (tabTv.getWidth() - tabIndicator.getWidth()) / 2;
                tabIndicator.setX(indicatorLeft);
            }
        });
    }

    private void requestCurrentTabData() {
        if (chatroomPresenter == null) {
            updateData(new ArrayList<PLVSocketUserBean>());
            return;
        }
        if (currentTab == TAB_KICK) {
            chatroomPresenter.requestKickUsers();
        } else {
            chatroomPresenter.requestBannedUsers();
        }
    }

    private void updateData(List<PLVSocketUserBean> users) {
        userAdapter.setData(users);
        loadingPb.setVisibility(GONE);
        updateEmptyView();
    }

    private void showLoading() {
        userAdapter.setData(new ArrayList<PLVSocketUserBean>());
        userRv.setVisibility(GONE);
        emptyLy.setVisibility(GONE);
        loadingPb.setVisibility(VISIBLE);
    }

    private void updateEmptyView() {
        final boolean empty = userAdapter.getItemCount() == 0;
        userRv.setVisibility(empty ? GONE : VISIBLE);
        emptyLy.setVisibility(empty ? VISIBLE : GONE);
    }

    private void onPortrait() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentLayout.getLayoutParams();
        layoutParams.height = Math.min(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth());
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;
        contentLayout.setRoundMode(PLVRoundRectLayout.MODE_ALL);
        contentLayout.setBackgroundColor(0xFF2C2C2C);
        contentLayout.setLayoutParams(layoutParams);

        PLVBlurView blurView = popupView.findViewById(R.id.blur_ly);
        blurView.setVisibility(View.INVISIBLE);
    }

    private void onLandscape() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update();
        }
        final int landscapeWidth = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentLayout.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = (int) (landscapeWidth * 0.44f);
        layoutParams.gravity = Gravity.RIGHT;
        contentLayout.setRoundMode(PLVRoundRectLayout.MODE_NONE);
        contentLayout.setBackground(null);
        contentLayout.setLayoutParams(layoutParams);

        PLVBlurView blurView = popupView.findViewById(R.id.blur_ly);
        FrameLayout.LayoutParams blurLayoutParams = (FrameLayout.LayoutParams) blurView.getLayoutParams();
        blurLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        blurLayoutParams.width = (int) (landscapeWidth * 0.44f);
        blurLayoutParams.gravity = Gravity.RIGHT;
        blurView.setLayoutParams(blurLayoutParams);
        blurView.setVisibility(View.VISIBLE);
        PLVBlurUtils.initBlurView(blurView);
    }

    private final IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {
        @Override
        public void onKickUsersList(final List<PLVSocketUserBean> dataList) {
            popupView.post(new Runnable() {
                @Override
                public void run() {
                    if (currentTab == TAB_KICK) {
                        updateData(dataList);
                    }
                }
            });
        }

        @Override
        public void onBannedUsersList(final List<PLVSocketUserBean> dataList) {
            popupView.post(new Runnable() {
                @Override
                public void run() {
                    if (currentTab == TAB_BANNED) {
                        updateData(dataList);
                    }
                }
            });
        }
    };

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<PLVSocketUserBean> users = new ArrayList<>();
        private Set<String> releasedUserKeys = new HashSet<>();

        private void setData(List<PLVSocketUserBean> users) {
            this.users = users == null ? new ArrayList<PLVSocketUserBean>() : users;
            releasedUserKeys.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_kick_banned_users_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final int bindTab = currentTab;
            PLVSocketUserBean user = users.get(position);
            String nick = user == null ? "" : user.getNick();
            String userId = user == null ? "" : user.getUserId();
            holder.nickTv.setText(TextUtils.isEmpty(nick) ? userId : nick);
            holder.userIdTv.setText(holder.itemView.getContext().getString(R.string.plv_kick_banned_users_user_id, userId));
            final boolean isPlatformProhibited = user != null && user.isAccount();
            final boolean isReleased = isReleased(bindTab, userId);
            holder.cancelTv.setSelected(isPlatformProhibited);
            holder.cancelTv.setEnabled(!isPlatformProhibited);
            holder.cancelTv.setText(getActionTextRes(bindTab, isPlatformProhibited, isReleased));
            holder.cancelTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.isSelected()) {
                        return;
                    }
                    final int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition < 0 || adapterPosition >= users.size()) {
                        return;
                    }
                    PLVSocketUserBean socketUserBean = users.get(adapterPosition);
                    String userId = socketUserBean == null ? "" : socketUserBean.getUserId();
                    if (TextUtils.isEmpty(userId)) {
                        return;
                    }
                    final boolean isKickTab = bindTab == TAB_KICK;
                    final boolean isReleased = isReleased(bindTab, userId);
                    int sendResult;
                    if (isKickTab) {
                        sendResult = isReleased
                                ? PolyvChatroomManager.getInstance().kick(userId)
                                : PolyvChatroomManager.getInstance().unKick(userId);
                    } else {
                        sendResult = isReleased
                                ? PolyvChatroomManager.getInstance().shield(userId)
                                : PolyvChatroomManager.getInstance().removeShield(userId);
                    }
                    if (sendResult > 0) {
                        PLVToast.Builder.context(v.getContext())
                                .setText(getSuccessTextRes(isKickTab, isReleased))
                                .build()
                                .show();
                        setReleased(bindTab, userId, !isReleased);
                        notifyItemChanged(adapterPosition);
                    } else {
                        String failMsg = v.getContext().getString(getFailTextRes(isKickTab, isReleased)) + "(" + sendResult + ")";
                        PLVToast.Builder.context(v.getContext())
                                .setText(failMsg)
                                .build()
                                .show();
                    }
                }
            });
        }

        private int getActionTextRes(int tab, boolean isPlatformProhibited, boolean isReleased) {
            if (isPlatformProhibited) {
                return tab == TAB_KICK ? R.string.plv_chat_kick_platform_prohibited : R.string.plv_chat_ban_platform_prohibited;
            }
            if (isReleased) {
                return tab == TAB_KICK ? R.string.plv_chat_kick : R.string.plv_chat_ban;
            }
            return tab == TAB_KICK ? R.string.plv_chat_unkick : R.string.plv_chat_unban;
        }

        private int getSuccessTextRes(boolean isKickTab, boolean isReleased) {
            if (isKickTab) {
                return isReleased ? R.string.plv_chat_kick_success_2 : R.string.plv_chat_unkick_success;
            }
            return isReleased ? R.string.plv_chat_ban_success : R.string.plv_chat_unban_success;
        }

        private int getFailTextRes(boolean isKickTab, boolean isReleased) {
            if (isKickTab) {
                return isReleased ? R.string.plv_chat_kick_fail : R.string.plv_chat_unkick_fail;
            }
            return isReleased ? R.string.plv_chat_ban_fail : R.string.plv_chat_unban_fail;
        }

        private boolean isReleased(int tab, String userId) {
            return releasedUserKeys.contains(getUserKey(tab, userId));
        }

        private void setReleased(int tab, String userId, boolean released) {
            String userKey = getUserKey(tab, userId);
            if (released) {
                releasedUserKeys.add(userKey);
            } else {
                releasedUserKeys.remove(userKey);
            }
        }

        private String getUserKey(int tab, String userId) {
            return tab + "_" + userId;
        }

        @Override
        public int getItemCount() {
            return users == null ? 0 : users.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView nickTv;
            private TextView userIdTv;
            private TextView cancelTv;

            private ViewHolder(View itemView) {
                super(itemView);
                nickTv = itemView.findViewById(R.id.plv_kick_banned_users_nick_tv);
                userIdTv = itemView.findViewById(R.id.plv_kick_banned_users_user_id_tv);
                cancelTv = itemView.findViewById(R.id.plv_kick_banned_users_cancel_tv);
            }
        }
    }
}
