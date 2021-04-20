package com.easefun.polyv.livecommon.module.modules.linkmic.model;

/**
 * date: 2021/1/5
 * author: HWilliamgo
 * description:
 * 作为{@link PLVLinkMicMuteCacheList}维护的列表的mute缓存数据模型
 */
public class PLVLinkMicMuteCacheBean {
    private String linkMicId = "";
    private boolean muteAudio = true;
    private boolean muteVideo = true;

    public PLVLinkMicMuteCacheBean(String linkMicId) {
        this.linkMicId = linkMicId;
    }

    public String getLinkMicId() {
        return linkMicId;
    }

    public void setLinkMicId(String linkMicId) {
        this.linkMicId = linkMicId;
    }

    public boolean isMuteAudio() {
        return muteAudio;
    }

    public void setMuteAudio(boolean muteAudio) {
        this.muteAudio = muteAudio;
    }

    public boolean isMuteVideo() {
        return muteVideo;
    }

    public void setMuteVideo(boolean muteVideo) {
        this.muteVideo = muteVideo;
    }
}
