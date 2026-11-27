package com.easefun.polyv.livecommon.module.modules.player.playback.model;

/** 快进快退尚未完成的目标；不把目标预览当作实际观看进度。单位：毫秒。 */
public class PLVPlaybackStepSeekState {
    private int target = -1;
    private int submittedTarget = -1;

    public int step(int actualPosition, int duration, int maximumPosition, int offset) {
        long base = isPending() ? target : Math.max(0, actualPosition);
        long limit = Math.max(0, Math.min(duration, maximumPosition));
        target = (int) Math.max(0, Math.min(limit, base + offset));
        return target;
    }

    public boolean isPending() {
        return target >= 0;
    }

    public int getTarget() {
        return target;
    }

    /** 同一时间只提交一次实际跳转，期间的连点累加到 target。 */
    public int takeTargetForSeek() {
        if (!isPending() || submittedTarget >= 0) {
            return -1;
        }
        submittedTarget = target;
        return submittedTarget;
    }

    /** 按已提交目标确认完成，不依赖回调时内核进度是否已刷新。 */
    public void onSeekComplete() {
        if (submittedTarget < 0) {
            return;
        }
        int completedTarget = submittedTarget;
        submittedTarget = -1;
        if (target == completedTarget) {
            reset();
        }
    }

    public void reset() {
        target = -1;
        submittedTarget = -1;
    }
}
