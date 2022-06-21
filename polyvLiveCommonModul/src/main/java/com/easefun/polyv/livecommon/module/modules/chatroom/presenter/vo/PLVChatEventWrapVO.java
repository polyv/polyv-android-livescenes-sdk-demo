package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo;

import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.List;

import static com.plv.foundationsdk.utils.PLVSugarUtil.listOf;

/**
 * @author Hoshiiro
 */
public class PLVChatEventWrapVO {

    private String id;
    private Long time;
    private Long lastEventTime;
    private PLVSocketUserBean user;
    private List<PLVBaseEvent> events;

    public String getId() {
        return id;
    }

    public PLVChatEventWrapVO setId(String id) {
        this.id = id;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public PLVChatEventWrapVO setTime(Long time) {
        this.time = time;
        return this;
    }

    public Long getLastEventTime() {
        return lastEventTime;
    }

    public PLVChatEventWrapVO setLastEventTime(Long lastEventTime) {
        this.lastEventTime = lastEventTime;
        return this;
    }

    public PLVSocketUserBean getUser() {
        return user;
    }

    public PLVChatEventWrapVO setUser(PLVSocketUserBean user) {
        this.user = user;
        return this;
    }

    public List<PLVBaseEvent> getEvents() {
        return events;
    }

    public PLVChatEventWrapVO setEvent(PLVBaseEvent event) {
        this.events = listOf(event);
        return this;
    }

    public PLVChatEventWrapVO setEvents(List<PLVBaseEvent> events) {
        this.events = events;
        return this;
    }

    public boolean isValid() {
        return time != null && user != null && events != null;
    }

    public boolean isSameUserWith(PLVChatEventWrapVO vo) {
        return vo != null && vo.user != null && isSameUserWith(vo.user.getUserId());
    }

    public boolean isSameUserWith(String userId) {
        return user != null && user.getUserId() != null && user.getUserId().equals(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PLVChatEventWrapVO vo = (PLVChatEventWrapVO) o;

        if (id != null ? !id.equals(vo.id) : vo.id != null) return false;
        return events != null ? events.equals(vo.events) : vo.events == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (events != null ? events.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PLVChatEventWrapVO{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", user=" + user +
                ", events=" + events +
                '}';
    }
}
