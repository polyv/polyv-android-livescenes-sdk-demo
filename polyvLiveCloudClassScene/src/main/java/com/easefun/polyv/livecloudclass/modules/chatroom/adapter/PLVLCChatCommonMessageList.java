package com.easefun.polyv.livecloudclass.modules.chatroom.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ScrollView;

import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.imageScan.PLVChatImageViewerFragment;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.util.List;

/**
 * 横/竖屏聊天共用的信息列表
 */
public class PLVLCChatCommonMessageList {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private int lastPosition = -1;//位置
    private int lastOffset = -1;//偏移量
    private PLVMessageRecyclerView chatMsgRv;//聊天信息recyclerView
    private PLVLCMessageAdapter messageAdapter;//信息adapter
    private boolean isLandscapeLayout;//是否横屏布局
    private boolean isLandscapeLastScrollChanged;//最后一次滚动是否是在横屏布局触发
    private PLVChatImageViewerFragment chatImageViewerFragment;//聊天图片查看fragment
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCChatCommonMessageList(final Context context) {
        chatMsgRv = new PLVMessageRecyclerView(context);
        chatMsgRv.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        chatMsgRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                View topView = chatMsgRv.getLayoutManager().getChildAt(0); //获取可视的第一个view
                if (topView != null) {
                    isLandscapeLastScrollChanged = isLandscapeLayout;
                    lastOffset = topView.getTop(); //获取与该view的顶部的偏移量
                    lastPosition = chatMsgRv.getLayoutManager().getPosition(topView);  //得到该View的数组位置
                }
                if (!chatMsgRv.canScrollVertically(1)) {
                    lastPosition = -1;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!chatMsgRv.canScrollVertically(1)) {
                    lastPosition = -1;
                }
            }
        });
        PLVMessageRecyclerView.setLayoutManager(chatMsgRv);
        messageAdapter = new PLVLCMessageAdapter();
        messageAdapter.setOnViewActionListener(new PLVLCMessageAdapter.OnViewActionListener() {
            @Override
            public void onChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg) {
                if (isQuoteImg) {
                    chatImageViewerFragment = PLVChatImageViewerFragment.show((AppCompatActivity) context, messageAdapter.getDataList().get(position), Window.ID_ANDROID_CONTENT);
                } else {
                    chatImageViewerFragment = PLVChatImageViewerFragment.show((AppCompatActivity) context, messageAdapter.getDataList(), messageAdapter.getDataList().get(position), Window.ID_ANDROID_CONTENT);
                }
            }
        });
        chatMsgRv.setAdapter(messageAdapter);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 列表数据操作">
    public void addChatMessageToList(final List<PLVBaseViewData> chatMessageDataList, final boolean isScrollEnd, boolean isLandscapeLayout) {
        if (chatMsgRv.getParent() == null) {
            return;
        }
        if (this.isLandscapeLayout != isLandscapeLayout) {
            return;
        }
        boolean result = messageAdapter.addDataListChangedAtLast(chatMessageDataList);
        if (result) {
            if (isScrollEnd) {
                chatMsgRv.scrollToPosition(messageAdapter.getItemCount() - 1);
            } else {
                chatMsgRv.scrollToBottomOrShowMore(chatMessageDataList.size());
            }
        }
    }

    public void addChatHistoryToList(final List<PLVBaseViewData<PLVBaseEvent>> chatMessageDataList, final boolean isScrollEnd, boolean isLandscapeLayout) {
        if (chatMsgRv.getParent() == null) {
            return;
        }
        if (this.isLandscapeLayout != isLandscapeLayout) {
            return;
        }
        boolean result = messageAdapter.addDataListChangedAtFirst(chatMessageDataList);
        if (result) {
            if (isScrollEnd) {
                chatMsgRv.scrollToPosition(messageAdapter.getItemCount() - 1);
            } else {
                chatMsgRv.scrollToPosition(0);
            }
        }
    }

    public void removeChatMessage(String id, boolean isLandscapeLayout) {
        if (chatMsgRv.getParent() == null) {
            return;
        }
        if (this.isLandscapeLayout != isLandscapeLayout) {
            return;
        }
        messageAdapter.removeDataChanged(id);
    }

    public void removeAllChatMessage(boolean isLandscapeLayout) {
        if (chatMsgRv.getParent() == null) {
            return;
        }
        if (this.isLandscapeLayout != isLandscapeLayout) {
            return;
        }
        messageAdapter.removeAllDataChanged();
    }

    public void changeDisplayType(boolean isDisplaySpecialType) {
        messageAdapter.changeDisplayType(isDisplaySpecialType);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 信息列表view附加到父控件">
    public boolean attachToParent(ViewGroup parent, boolean isLandscapeLayout) {
        if (ScreenUtils.isLandscape() != isLandscapeLayout) {
            return false;
        }
        if (chatMsgRv.getParent() == parent) {
            return false;
        }
        this.isLandscapeLayout = isLandscapeLayout;
        if (chatMsgRv.getParent() != null) {
            ((ViewGroup) chatMsgRv.getParent()).removeView(chatMsgRv);
        }
        parent.addView(chatMsgRv, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        messageAdapter.setLandscapeLayout(isLandscapeLayout);
        if (chatMsgRv.getItemDecorationCount() > 0) {
            for (int i = 0; i < chatMsgRv.getItemDecorationCount(); i++) {
                chatMsgRv.removeItemDecorationAt(i);
            }
        }
        if (isLandscapeLayout) {
            chatMsgRv.setVerticalFadingEdgeEnabled(true);
            chatMsgRv.setStackFromEnd(true);
            chatMsgRv.setFadingEdgeLength(ConvertUtils.dp2px(26));
            chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(4), 0));
        } else {
            chatMsgRv.setVerticalFadingEdgeEnabled(false);
            chatMsgRv.setStackFromEnd(false);
            chatMsgRv.setFadingEdgeLength(ConvertUtils.dp2px(0));
            chatMsgRv.addItemDecoration(new PLVMessageRecyclerView.SpacesItemDecoration(ConvertUtils.dp2px(16), ConvertUtils.dp2px(16)));
        }
        chatMsgRv.setAdapter(messageAdapter);
        if (lastPosition != -1) {
            int position = lastPosition;
            int offset = lastOffset;
            if (isLandscapeLastScrollChanged == isLandscapeLayout) {
                if (isLandscapeLayout) {
                    if (lastPosition != 0) {
                        offset = offset - ConvertUtils.dp2px(4);
                    }
                } else {
                    offset = offset - ConvertUtils.dp2px(16);
                }
            } else {
                if (isLandscapeLayout) {
                    offset = (offset >= 0 ? offset / (16 / 4) : offset / 2) - ConvertUtils.dp2px(4) + ConvertUtils.dp2px(26 / 6f)/*FadingEdge*/;
                } else {
                    offset = (offset >= 0 ? offset * (16 / 4) : offset * 2) - ConvertUtils.dp2px(16)/* - ConvertUtils.dp2px(26 / 6f)*/;
                }
            }
            ((LinearLayoutManager) chatMsgRv.getLayoutManager()).scrollToPositionWithOffset(position, offset);//need remove itemMargin
        } else {
            chatMsgRv.post(new Runnable() {
                @Override
                public void run() {
                    //直接使用scrollToPosition有时不会滚动最底部
                    chatMsgRv.scrollToPosition(messageAdapter.getItemCount() - 1);
                }
            });
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 配置参数方法">
    public void addOnUnreadCountChangeListener(PLVMessageRecyclerView.OnUnreadCountChangeListener listener) {
        chatMsgRv.addOnUnreadCountChangeListener(listener);
    }

    public void addUnreadView(View view) {
        chatMsgRv.addUnreadView(view);
    }

    //设置信息索引，在attachToParent后设置，并且需要在chatroomPresenter.registerView后设置
    public void setMsgIndex(int msgIndex) {
        messageAdapter.setMsgIndex(msgIndex);
    }

    //当前聊天列表是否是附在横屏的layout中
    public boolean isLandscapeLayout() {
        return isLandscapeLayout;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 是否需要拦截返回事件">
    public boolean onBackPressed() {
        if (chatImageViewerFragment != null && chatImageViewerFragment.isVisible()) {
            chatImageViewerFragment.hide();
            return true;
        }
        return false;
    }
    // </editor-fold>
}
