package com.easefun.polyv.livecommon.module.modules.player.playback.model;

import com.plv.livescenes.config.PLVLivePlaybackSeekBarStrategy;

/** SDK 覆盖只影响显示开关，不能越过频道的禁止拖动策略。 */
public class PLVPlaybackSeekPolicy {
    public static boolean isEnabled(boolean live, Boolean sdkOverride, boolean channelEnabled,
                                    PLVLivePlaybackSeekBarStrategy seekStrategy) {
        return !live && (sdkOverride != null ? sdkOverride : channelEnabled)
                && (seekStrategy == PLVLivePlaybackSeekBarStrategy.ALLOW_SEEK
                || seekStrategy == PLVLivePlaybackSeekBarStrategy.ALLOW_SEEK_PLAYED);
    }
}
