package com.easefun.polyv.livecommon.module.modules.streamer.presenter.vo;

import androidx.annotation.NonNull;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicUiState implements Cloneable {

    public boolean sipEnable = true;
    public String sipCallInNumber;

    @NonNull
    @Override
    public PLVSipLinkMicUiState clone() {
        try {
            return (PLVSipLinkMicUiState) super.clone();
        } catch (CloneNotSupportedException e) {
            // should not happen
            return this;
        }
    }
}
