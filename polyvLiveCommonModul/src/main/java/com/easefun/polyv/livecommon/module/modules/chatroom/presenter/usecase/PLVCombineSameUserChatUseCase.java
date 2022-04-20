package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase;

import android.support.annotation.NonNull;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.plv.socket.event.PLVBaseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVCombineSameUserChatUseCase {

    @NonNull
    public List<PLVChatEventWrapVO> combine(List<PLVChatEventWrapVO> originList) {
        final List<PLVChatEventWrapVO> result = new ArrayList<>();
        if (originList == null || originList.isEmpty()) {
            return result;
        }
        PLVChatEventWrapVO vo = null;
        for (int i = 0; i < originList.size(); i++) {
            final PLVChatEventWrapVO item = originList.get(i);
            if (vo == null || !vo.isSameUserWith(item)) {
                vo = new PLVChatEventWrapVO()
                        .setId(item.getId())
                        .setTime(item.getTime())
                        .setUser(item.getUser())
                        .setEvents(new ArrayList<PLVBaseEvent>());
                result.add(vo);
            }
            vo.setLastEventTime(item.getLastEventTime());
            vo.getEvents().add(item.getEvents().get(0));
        }
        return result;
    }

}
