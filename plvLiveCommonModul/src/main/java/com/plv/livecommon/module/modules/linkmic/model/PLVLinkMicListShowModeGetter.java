package com.plv.livecommon.module.modules.linkmic.model;

import com.plv.livescenes.linkmic.manager.PLVLinkMicConfig;

/**
 * date: 2021/1/4
 * author: HWilliamgo
 * description: 获取当前连麦列表显示模式
 */
public class PLVLinkMicListShowModeGetter {

    /**
     * 获取加入连麦后的列表显示模式
     *
     * @param isAudio true为音频连麦，false为视频连麦
     */
    public static PLVLinkMicListShowMode getJoinedMicShowMode(boolean isAudio) {
        PLVLinkMicListShowMode result;
        if (PLVLinkMicConfig.getInstance().isPureRtcWatchEnabled()) {
            if (PLVLinkMicConfig.getInstance().isPureRtcOnlySubscribeMainScreenVideo()) {
                if (isAudio) {
                    result = PLVLinkMicListShowMode.SHOW_FIRST_SCREEN;
                } else {
                    result = PLVLinkMicListShowMode.SHOW_FIRST_SCREEN_AND_SELF;
                }
            } else {
                if (isAudio) {
                    result = PLVLinkMicListShowMode.SHOW_TEACHER_AND_GUEST;
                } else {
                    result = PLVLinkMicListShowMode.SHOW_ALL;
                }
            }
        } else {
            if (isAudio) {
                result = PLVLinkMicListShowMode.SHOW_TEACHER_AND_GUEST;
            } else {
                result = PLVLinkMicListShowMode.SHOW_ALL;
            }
        }

        return result;
    }

    /**
     * 获取离开连麦后的列表显示模式
     */
    public static PLVLinkMicListShowMode getLeavedMicShowMode() {
        if (PLVLinkMicConfig.getInstance().isPureRtcOnlySubscribeMainScreenVideo()) {
            return PLVLinkMicListShowMode.SHOW_FIRST_SCREEN;
        } else {
            return PLVLinkMicListShowMode.SHOW_ALL;
        }
    }
}
