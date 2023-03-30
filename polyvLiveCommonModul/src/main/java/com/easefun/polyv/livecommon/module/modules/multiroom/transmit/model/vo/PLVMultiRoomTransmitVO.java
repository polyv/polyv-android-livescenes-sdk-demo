package com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.vo;

import static com.plv.foundationsdk.utils.PLVSugarUtil.requireNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.multiroom.transmit.model.enums.PLVMultiRoomTransmitMode;

import javax.annotation.concurrent.Immutable;

/**
 * @author Hoshiiro
 */
@Immutable
public final class PLVMultiRoomTransmitVO {

    @Nullable
    public final String mainRoomChannelId;
    @Nullable
    public final String mainRoomStream;
    @Nullable
    public final String mainRoomSessionId;
    @NonNull
    public final PLVMultiRoomTransmitMode transmitMode;

    private PLVMultiRoomTransmitVO(
            @Nullable String mainRoomChannelId,
            @Nullable String mainRoomStream,
            @Nullable String mainRoomSessionId,
            @NonNull PLVMultiRoomTransmitMode transmitMode
    ) {
        this.mainRoomChannelId = mainRoomChannelId;
        this.mainRoomStream = mainRoomStream;
        this.mainRoomSessionId = mainRoomSessionId;
        this.transmitMode = transmitMode;
    }

    public boolean isWatchMainRoom() {
        return mainRoomChannelId != null && transmitMode == PLVMultiRoomTransmitMode.LISTEN_MAIN;
    }

    public static class Builder {

        @Nullable
        private String mainRoomChannelId = null;
        @Nullable
        private String mainRoomStream = null;
        @Nullable
        private String mainRoomSessionId = null;
        @NonNull
        private PLVMultiRoomTransmitMode transmitMode = PLVMultiRoomTransmitMode.LISTEN_CHILD;

        public Builder copy(@NonNull PLVMultiRoomTransmitVO source) {
            return this.setMainRoomChannelId(source.mainRoomChannelId)
                    .setMainRoomStream(source.mainRoomStream)
                    .setMainRoomSessionId(source.mainRoomSessionId)
                    .setTransmitMode(source.transmitMode);
        }

        public Builder setMainRoomChannelId(@Nullable String mainRoomChannelId) {
            this.mainRoomChannelId = mainRoomChannelId;
            return this;
        }

        public Builder setMainRoomStream(@Nullable String mainRoomStream) {
            this.mainRoomStream = mainRoomStream;
            return this;
        }

        public Builder setMainRoomSessionId(@Nullable String mainRoomSessionId) {
            this.mainRoomSessionId = mainRoomSessionId;
            return this;
        }

        public Builder setTransmitMode(@NonNull PLVMultiRoomTransmitMode transmitMode) {
            this.transmitMode = requireNotNull(transmitMode);
            return this;
        }

        public PLVMultiRoomTransmitVO build() {
            return new PLVMultiRoomTransmitVO(
                    mainRoomChannelId,
                    mainRoomStream,
                    mainRoomSessionId,
                    transmitMode
            );
        }

    }

}
