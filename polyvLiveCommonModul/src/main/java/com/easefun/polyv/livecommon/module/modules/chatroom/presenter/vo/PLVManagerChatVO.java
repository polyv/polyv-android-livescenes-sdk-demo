package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVManagerChatVO {

    private List<PLVChatEventWrapVO> chatEventWrapVOList;

    public PLVManagerChatVO() {
    }

    public PLVManagerChatVO(final List<PLVChatEventWrapVO> chatEventWrapVOList) {
        this.chatEventWrapVOList = chatEventWrapVOList;
    }

    public List<PLVChatEventWrapVO> getChatEventWrapVOList() {
        return chatEventWrapVOList;
    }

    public PLVManagerChatVO setChatEventWrapVOList(List<PLVChatEventWrapVO> chatEventWrapVOList) {
        this.chatEventWrapVOList = chatEventWrapVOList;
        return this;
    }

    @Override
    public String toString() {
        return "PLVManagerChatVO{" +
                "chatEventWrapVOList=" + chatEventWrapVOList +
                '}';
    }
}
