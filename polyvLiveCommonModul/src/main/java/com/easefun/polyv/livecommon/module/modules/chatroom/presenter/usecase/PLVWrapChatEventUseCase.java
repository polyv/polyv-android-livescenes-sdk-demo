package com.easefun.polyv.livecommon.module.modules.chatroom.presenter.usecase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.livescenes.chatroom.PLVLocalMessage;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.socket.net.model.PLVSocketLoginVO;
import com.plv.socket.user.PLVSocketUserBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoshiiro
 */
public class PLVWrapChatEventUseCase {

    @NonNull
    public PLVChatEventWrapVO wrap(PLVBaseEvent event) {
        final PLVChatEventWrapVO vo = Wrapper.wrapEvent(event);
        if (vo == null) {
            return new PLVChatEventWrapVO();
        } else {
            return vo;
        }
    }

    private static abstract class Wrapper {

        private static final Map<Class<? extends PLVBaseEvent>, Wrapper> wrapperMap = new HashMap<Class<? extends PLVBaseEvent>, Wrapper>() {{
            put(PLVSpeakEvent.class, new PLVSpeakEventWrapper());
            put(PLVChatImgEvent.class, new PLVChatImgEventWrapper());
            put(PLVSpeakHistoryEvent.class, new PLVSpeakHistoryEventWrapper());
            put(PLVChatImgHistoryEvent.class, new PLVChatImgHistoryEventWrapper());
            put(PLVLocalMessage.class, new PLVLocalSpeakMessageWrapper());
            put(PolyvLocalMessage.class, new PLVLocalSpeakMessageWrapper());
            put(PLVSendLocalImgEvent.class, new PLVLocalChatImgEventWrapper());
            put(PolyvSendLocalImgEvent.class, new PLVLocalChatImgEventWrapper());
        }};

        protected abstract PLVChatEventWrapVO wrap(PLVBaseEvent event);

        public static PLVChatEventWrapVO wrapEvent(PLVBaseEvent event) {
            final Wrapper wrapper = wrapperMap.get(event.getClass());
            if (wrapper != null) {
                return wrapper.wrap(event);
            }
            return null;
        }

        private static PLVSocketUserBean createLocalUser() {
            final PLVSocketLoginVO loginVO = PLVSocketWrapper.getInstance().getLoginVO();
            final PLVSocketUserBean user = new PLVSocketUserBean();
            if (loginVO == null) {
                return user;
            }
            user.setUserType(loginVO.getUserType());
            user.setNick(loginVO.getNickName());
            user.setUserId(loginVO.getUserId());
            user.setPic(loginVO.getAvatarUrl());
            user.setActor(loginVO.getActor());
            user.setAuthorization(loginVO.getAuthorization());
            return user;
        }

        @Nullable
        private static Wrapper findWrapperForEvent(PLVBaseEvent event) {
            Class eventClass = event.getClass();
            while (eventClass != null && eventClass != Object.class) {
                if (wrapperMap.containsKey(eventClass)) {
                    return wrapperMap.get(eventClass);
                }
                eventClass = eventClass.getSuperclass();
            }
            return null;
        }
    }

    private static class PLVSpeakEventWrapper extends Wrapper {
        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakEvent)) {
                return null;
            }
            final PLVSpeakEvent speakEvent = (PLVSpeakEvent) event;
            return new PLVChatEventWrapVO()
                    .setEvent(speakEvent)
                    .setId(speakEvent.getId())
                    .setTime(speakEvent.getTime())
                    .setLastEventTime(speakEvent.getTime())
                    .setUser(speakEvent.getUser());
        }
    }

    private static class PLVLocalSpeakMessageWrapper extends Wrapper {

        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVLocalMessage)) {
                return null;
            }
            final PLVLocalMessage localMessage = (PLVLocalMessage) event;
            return new PLVChatEventWrapVO()
                    .setEvent(localMessage)
                    .setId(localMessage.getId())
                    .setTime(localMessage.getTime())
                    .setLastEventTime(localMessage.getTime())
                    .setUser(Wrapper.createLocalUser());
        }

    }

    private static class PLVChatImgEventWrapper extends Wrapper {

        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgEvent)) {
                return null;
            }
            final PLVChatImgEvent chatImgEvent = (PLVChatImgEvent) event;
            return new PLVChatEventWrapVO()
                    .setEvent(chatImgEvent)
                    .setId(chatImgEvent.getId())
                    .setTime(chatImgEvent.getTime())
                    .setLastEventTime(chatImgEvent.getTime())
                    .setUser(chatImgEvent.getUser());
        }
    }

    private static class PLVLocalChatImgEventWrapper extends Wrapper {
        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVSendLocalImgEvent)) {
                return null;
            }
            final PLVSendLocalImgEvent chatImgEvent = (PLVSendLocalImgEvent) event;
            return new PLVChatEventWrapVO()
                    .setEvent(chatImgEvent)
                    .setId(chatImgEvent.getId())
                    .setTime(chatImgEvent.getTime())
                    .setLastEventTime(chatImgEvent.getTime())
                    .setUser(Wrapper.createLocalUser());
        }
    }

    private static class PLVSpeakHistoryEventWrapper extends Wrapper {

        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVSpeakHistoryEvent)) {
                return null;
            }
            final PLVSpeakHistoryEvent speakHistoryEvent = (PLVSpeakHistoryEvent) event;
            return new PLVChatEventWrapVO()
                    .setEvent(speakHistoryEvent)
                    .setId(speakHistoryEvent.getId())
                    .setTime(speakHistoryEvent.getTime())
                    .setLastEventTime(speakHistoryEvent.getTime())
                    .setUser(speakHistoryEvent.getUser());
        }

    }

    private static class PLVChatImgHistoryEventWrapper extends Wrapper {

        @Override
        protected PLVChatEventWrapVO wrap(PLVBaseEvent event) {
            if (!(event instanceof PLVChatImgHistoryEvent)) {
                return null;
            }
            final PLVChatImgHistoryEvent chatImgHistoryEvent = (PLVChatImgHistoryEvent) event;
            return new PLVChatEventWrapVO()
                    .setEvent(chatImgHistoryEvent)
                    .setId(chatImgHistoryEvent.getId())
                    .setTime(chatImgHistoryEvent.getTime())
                    .setLastEventTime(chatImgHistoryEvent.getTime())
                    .setUser(chatImgHistoryEvent.getUser());
        }

    }

}
