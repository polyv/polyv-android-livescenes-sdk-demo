package com.easefun.polyv.livecommon.module.modules.chatroom.model.vo;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatHistoryLoadStatus {

    private boolean isLoading = false;
    private boolean canLoadMore = true;

    public PLVManagerChatHistoryLoadStatus() {

    }

    public PLVManagerChatHistoryLoadStatus(PLVManagerChatHistoryLoadStatus oldStatus) {
        isLoading = oldStatus.isLoading;
        canLoadMore = oldStatus.canLoadMore;
    }

    public PLVManagerChatHistoryLoadStatus copy() {
        return new PLVManagerChatHistoryLoadStatus(this);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public PLVManagerChatHistoryLoadStatus setLoading(boolean loading) {
        isLoading = loading;
        return this;
    }

    public boolean isCanLoadMore() {
        return canLoadMore;
    }

    public PLVManagerChatHistoryLoadStatus setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
        return this;
    }
}
