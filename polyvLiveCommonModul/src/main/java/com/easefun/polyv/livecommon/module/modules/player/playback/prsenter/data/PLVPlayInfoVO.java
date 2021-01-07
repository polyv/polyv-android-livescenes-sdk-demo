package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data;

/**
 * 播放信息，封装当前播放时间、总时长、缓冲进度，播放状态等数据
 */
public class PLVPlayInfoVO {
    private int position;
    private int totalTime;
    private int bufPercent;
    private boolean isPlaying;
    private boolean isSubVideoViewPlaying;

    private PLVPlayInfoVO(Builder builder) {
        position = builder.position;
        totalTime = builder.totalTime;
        bufPercent = builder.bufPercent;
        isPlaying = builder.isPlaying;
        isSubVideoViewPlaying = builder.isSubVideoViewPlaying;
    }

    public int getPosition() {
        return position;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getBufPercent() {
        return bufPercent;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isSubVideoViewPlaying() {
        return isSubVideoViewPlaying;
    }

    public static final class Builder {
        private int position;
        private int totalTime;
        private int bufPercent;
        private boolean isPlaying;
        private boolean isSubVideoViewPlaying;

        public Builder() {
        }

        public Builder position(int val) {
            position = val;
            return this;
        }

        public Builder totalTime(int val) {
            totalTime = val;
            return this;
        }

        public Builder bufPercent(int val) {
            bufPercent = val;
            return this;
        }

        public Builder isPlaying(boolean val) {
            isPlaying = val;
            return this;
        }

        public Builder isSubViewPlaying(boolean subViewPlaying) {
            isSubVideoViewPlaying = subViewPlaying;
            return this;
        }

        public PLVPlayInfoVO build() {
            return new PLVPlayInfoVO(this);
        }
    }
}
