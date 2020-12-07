package com.easefun.polyv.livecommon.module.modules.player.playback.listener;

/**
 * 播放进度监听器
 */
public interface IPLVPlayProgressListener {

    void callback(int position, int totalTime, int bufPercent, boolean isPlaying);
}
