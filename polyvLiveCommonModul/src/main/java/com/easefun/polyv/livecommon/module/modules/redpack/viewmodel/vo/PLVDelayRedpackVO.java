package com.easefun.polyv.livecommon.module.modules.redpack.viewmodel.vo;

import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.chatroom.model.enums.PLVRedPaperType;

/**
 * @author Hoshiiro
 */
public class PLVDelayRedpackVO {

    private long redpackSendTime = 0;
    @Nullable
    private PLVRedPaperType redPaperType;
    @Nullable
    private String blessing;

    public long getRedpackSendTime() {
        return redpackSendTime;
    }

    public PLVDelayRedpackVO setRedpackSendTime(long redpackSendTime) {
        this.redpackSendTime = redpackSendTime;
        return this;
    }

    @Nullable
    public PLVRedPaperType getRedPaperType() {
        return redPaperType;
    }

    public PLVDelayRedpackVO setRedPaperType(@Nullable PLVRedPaperType redPaperType) {
        this.redPaperType = redPaperType;
        return this;
    }

    @Nullable
    public String getBlessing() {
        return blessing;
    }

    public PLVDelayRedpackVO setBlessing(@Nullable String blessing) {
        this.blessing = blessing;
        return this;
    }

    public PLVDelayRedpackVO copy() {
        return new PLVDelayRedpackVO()
                .setRedpackSendTime(redpackSendTime)
                .setRedPaperType(redPaperType)
                .setBlessing(blessing);
    }

}
