package com.easefun.polyv.livecommon.module.modules.socket;

/**
 * socket回调信息封装类
 */
public class PLVSocketMessage {
    private String listenEvent;
    private String event;
    private String message;

    public PLVSocketMessage(String listenEvent, String message, String event) {
        this.listenEvent = listenEvent;
        this.message = message;
        this.event = event;
    }

    public String getListenEvent() {
        return listenEvent;
    }

    public String getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }
}
