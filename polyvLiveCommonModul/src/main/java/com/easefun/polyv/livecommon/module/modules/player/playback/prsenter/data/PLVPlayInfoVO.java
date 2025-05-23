package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.data;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * 播放信息，封装当前播放时间、总时长、缓冲进度，播放状态等数据
 */
public class PLVPlayInfoVO {
    private int position;
    private int totalTime;
    private transient int bufPercent;
    private transient boolean isPlaying;
    private transient boolean isSubVideoViewPlaying;
    @NonNull
    private List<String> subtitles;

    private PLVPlayInfoVO(Builder builder) {
        position = builder.position;
        totalTime = builder.totalTime;
        bufPercent = builder.bufPercent;
        isPlaying = builder.isPlaying;
        isSubVideoViewPlaying = builder.isSubVideoViewPlaying;
        subtitles = builder.subtitles;
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

    @NonNull
    public List<String> getSubtitles() {
        return subtitles;
    }

    public static final class Builder {
        private int position;
        private int totalTime;
        private int bufPercent;
        private boolean isPlaying;
        private boolean isSubVideoViewPlaying;
        @NonNull
        private List<String> subtitles = Collections.emptyList();

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

        public Builder setSubtitles(@NonNull List<String> subtitles) {
            this.subtitles = subtitles;
            return this;
        }

        public PLVPlayInfoVO build() {
            return new PLVPlayInfoVO(this);
        }
    }
}
