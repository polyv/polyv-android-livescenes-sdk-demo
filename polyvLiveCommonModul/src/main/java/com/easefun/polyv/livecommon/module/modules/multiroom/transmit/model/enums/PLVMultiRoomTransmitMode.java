package com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.enums;

import android.support.annotation.Nullable;

/**
 * 转播频道拉流模式
 *
 * @author Hoshiiro
 */
public enum PLVMultiRoomTransmitMode {

    /**
     * 拉大房间/主房间的流
     */
    LISTEN_MAIN("listenMain"),

    /**
     * 拉小房间的流
     */
    LISTEN_CHILD("listenChild"),

    ;

    public final String serverName;

    PLVMultiRoomTransmitMode(
            String serverName
    ) {
        this.serverName = serverName;
    }

    @Nullable
    public static PLVMultiRoomTransmitMode match(@Nullable String serverName) {
        for (PLVMultiRoomTransmitMode value : values()) {
            if (value.serverName.equals(serverName)) {
                return value;
            }
        }
        return null;
    }


}
