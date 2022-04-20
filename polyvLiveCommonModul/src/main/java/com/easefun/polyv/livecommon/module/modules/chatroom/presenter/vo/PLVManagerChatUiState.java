package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatUiState {

    private int unreadMessageCount = 0;
    private boolean isHistoryMessageLoading = false;
    private boolean canLoadMoreHistoryMessage = true;

    public PLVManagerChatUiState() {
    }

    public PLVManagerChatUiState(PLVManagerChatUiState oldState) {
        this.unreadMessageCount = oldState.unreadMessageCount;
        this.isHistoryMessageLoading = oldState.isHistoryMessageLoading;
        this.canLoadMoreHistoryMessage = oldState.canLoadMoreHistoryMessage;
    }

    public PLVManagerChatUiState copy() {
        return new PLVManagerChatUiState(this);
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public PLVManagerChatUiState setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
        return this;
    }

    public boolean isHistoryMessageLoading() {
        return isHistoryMessageLoading;
    }

    public PLVManagerChatUiState setHistoryMessageLoading(boolean historyMessageLoading) {
        isHistoryMessageLoading = historyMessageLoading;
        return this;
    }

    public boolean isCanLoadMoreHistoryMessage() {
        return canLoadMoreHistoryMessage;
    }

    public PLVManagerChatUiState setCanLoadMoreHistoryMessage(boolean canLoadMoreHistoryMessage) {
        this.canLoadMoreHistoryMessage = canLoadMoreHistoryMessage;
        return this;
    }

    @Override
    public String toString() {
        return "PLVManagerChatUiState{" +
                "unreadMessageCount=" + unreadMessageCount +
                ", isHistoryMessageLoading=" + isHistoryMessageLoading +
                ", canLoadMoreHistoryMessage=" + canLoadMoreHistoryMessage +
                '}';
    }
}
