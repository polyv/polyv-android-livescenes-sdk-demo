package com.easefun.polyv.livecommon.module.modules.player.playback.prsenter.config;

import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.playback.video.PLVPlaybackListType;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheVideoConfig {

    private String vid;
    private String videoPoolId;
    private String channelId;
    private PLVLiveChannelType channelType;
    private String viewerId;
    private String viewerName;
    private String viewerAvatar;
    private PLVPlaybackListType playbackListType;

    public String getVid() {
        return vid;
    }

    public PLVPlaybackCacheVideoConfig setVid(String vid) {
        this.vid = vid;
        return this;
    }

    public String getVideoPoolId() {
        return videoPoolId;
    }

    public PLVPlaybackCacheVideoConfig setVideoPoolId(String videoPoolId) {
        this.videoPoolId = videoPoolId;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public PLVPlaybackCacheVideoConfig setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public PLVLiveChannelType getChannelType() {
        return channelType;
    }

    public PLVPlaybackCacheVideoConfig setChannelType(PLVLiveChannelType channelType) {
        this.channelType = channelType;
        return this;
    }

    public String getViewerId() {
        return viewerId;
    }

    public PLVPlaybackCacheVideoConfig setViewerId(String viewerId) {
        this.viewerId = viewerId;
        return this;
    }

    public String getViewerName() {
        return viewerName;
    }

    public PLVPlaybackCacheVideoConfig setViewerName(String viewerName) {
        this.viewerName = viewerName;
        return this;
    }

    public String getViewerAvatar() {
        return viewerAvatar;
    }

    public PLVPlaybackCacheVideoConfig setViewerAvatar(String viewerAvatar) {
        this.viewerAvatar = viewerAvatar;
        return this;
    }

    public PLVPlaybackListType getPlaybackListType() {
        return playbackListType;
    }

    public PLVPlaybackCacheVideoConfig setPlaybackListType(PLVPlaybackListType playbackListType) {
        this.playbackListType = playbackListType;
        return this;
    }

    public PLVPlaybackCacheVideoConfig setVideoPoolIdByVid(final String vid) {
        if (vid == null) {
            return this;
        }
        if (vid.contains("_")) {
            setVideoPoolId(vid.substring(0, vid.lastIndexOf("_")));
        } else {
            setVideoPoolId(vid);
        }
        return this;
    }
}
