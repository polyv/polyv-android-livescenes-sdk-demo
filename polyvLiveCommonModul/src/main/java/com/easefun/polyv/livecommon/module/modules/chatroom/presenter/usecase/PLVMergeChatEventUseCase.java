package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVMergeChatEventUseCase {

    public void merge(@NonNull List<PLVChatEventWrapVO> chatEvents, @NonNull PLVChatEventWrapVO chatEvent) {
        int insertIndex = 0;
        boolean isReplaceSameItem = false;

        for (int i = 0; i < chatEvents.size(); i++) {
            final PLVChatEventWrapVO event = chatEvents.get(i);
            if (isSendBySameUserAtSameTime(event, chatEvent) || isSameIdItem(event, chatEvent)) {
                chatEvents.set(i, chatEvent);
                isReplaceSameItem = true;
                break;
            }
            // 假定列表内已经按时间顺序排序
            if (event.getTime() < chatEvent.getTime()) {
                insertIndex = i + 1;
            }
        }

        if (!isReplaceSameItem) {
            chatEvents.add(insertIndex, chatEvent);
        }

        sort(chatEvents);
    }

    private void sort(@NonNull List<PLVChatEventWrapVO> chatEvents) {
        Collections.sort(chatEvents, new Comparator<PLVChatEventWrapVO>() {
            @Override
            public int compare(PLVChatEventWrapVO o1, PLVChatEventWrapVO o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
    }

    private static boolean isSendBySameUserAtSameTime(PLVChatEventWrapVO vo1, PLVChatEventWrapVO vo2) {
        return vo1.getUser().getUserId().equals(vo2.getUser().getUserId()) && vo1.getTime().equals(vo2.getTime());
    }

    private static boolean isSameIdItem(PLVChatEventWrapVO vo1, PLVChatEventWrapVO vo2) {
        return !TextUtils.isEmpty(vo1.getId()) && vo1.getId().equals(vo2.getId());
    }

}
