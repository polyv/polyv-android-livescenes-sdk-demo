package com.easefun.polyv.livecommon.module.modules.socket;

/**
 * socket回调信息封装类
 */
public class PLVSocketMessage {
    public String listenEvent;
    public String event;
    public String message;

    public PLVSocketMessage(String listenEvent, String message, String event) {
        this.listenEvent = listenEvent;
        this.message = message;
        this.event = event;
    }
}
