package com.easefun.polyv.livecommon.module.modules.player.live.presenter.data;

/**
 * 播放信息
 */
public class PLVPlayInfoVO {
    private boolean isPlaying;
    private boolean isSubVideoViewPlaying;

    private PLVPlayInfoVO(Builder builder) {
        isPlaying = builder.isPlaying;
        isSubVideoViewPlaying = builder.isSubVideoViewPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isSubVideoViewPlaying() {
        return isSubVideoViewPlaying;
    }

    public static final class Builder {
        private boolean isPlaying = false;
        private boolean isSubVideoViewPlaying =false;

        public Builder() { }

        public Builder isPlaying(boolean val) {
            isPlaying = val;
            return this;
        }

        public Builder isSubVideoViewPlaying(boolean val) {
            isSubVideoViewPlaying = val;
            return this;
        }

        public PLVPlayInfoVO build() {
            return new PLVPlayInfoVO(this);
        }
    }
}
