package com.easefun.polyv.livecommon.module.modules.chatroom;

import com.plv.livescenes.socket.PLVSocketWrapper;

/**
 * 特殊类型标志类(我、讲师、助教、管理员、嘉宾)
 */
public class PLVSpecialTypeTag {
    private String userId;

    public PLVSpecialTypeTag(String userId) {
        this.userId = userId;
    }

    public boolean isMySelf() {
        return userId != null && userId.equals(PLVSocketWrapper.getInstance().getLoginVO().getUserId());
    }
}
