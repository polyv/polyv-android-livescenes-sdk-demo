package com.easefun.polyv.livecommon.module.modules.streamer.model.enums;

/**
 * @author Hoshiiro
 */
public enum PLVSipLinkMicState {

    /**
     * 来电，待接通
     */
    ON_CALLING_IN,

    /**
     * 呼出，呼叫中
     */
    ON_CALLING_OUT,

    /**
     * 呼出，未响应
     */
    CALL_OUT_NOT_RESPONSE,

    /**
     * 呼出，已被拒绝
     */
    CALL_OUT_REFUSED,

    /**
     * 已接通
     */
    CONNECTED,

    /**
     * 挂断
     */
    HANG_UP,

    ;

    PLVSipLinkMicState() {
    }

}
