package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;

import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVCalculateUnreadMessageCountUseCase {

    public int calculate(@NonNull List<PLVChatEventWrapVO> chatEvents, final long lastReadTime) {
        int unreadCount = 0;
        for (int i = chatEvents.size() - 1; i >= 0; i--) {
            final PLVChatEventWrapVO chatEvent = chatEvents.get(i);
            if (chatEvent.isValid() && chatEvent.getTime() > lastReadTime) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

}
