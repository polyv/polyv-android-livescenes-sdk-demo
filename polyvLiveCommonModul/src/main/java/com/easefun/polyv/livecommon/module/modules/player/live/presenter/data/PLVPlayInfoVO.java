package com.easefun.polyv.livecommon.module.modules.player.live.presenter.data;

/**
 * 播放信息
 */
public class PLVPlayInfoVO {
    private boolean isPlaying;

    private PLVPlayInfoVO(Builder builder) {
        isPlaying = builder.isPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public static final class Builder {
        private boolean isPlaying;

        public Builder() {
        }

        public Builder isPlaying(boolean val) {
            isPlaying = val;
            return this;
        }

        public PLVPlayInfoVO build() {
            return new PLVPlayInfoVO(this);
        }
    }
}
