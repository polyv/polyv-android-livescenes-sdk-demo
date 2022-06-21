package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity;

import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.playback.video.PLVPlaybackListType;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheViewerInfoVO {

    private String channelId;
    private PLVLiveChannelType channelType;
    private String vid;
    private String viewerId;
    private String viewerName;
    private String viewerAvatar;
    private PLVPlaybackListType playbackListType;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public PLVLiveChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(PLVLiveChannelType channelType) {
        this.channelType = channelType;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public String getViewerName() {
        return viewerName;
    }

    public void setViewerName(String viewerName) {
        this.viewerName = viewerName;
    }

    public String getViewerAvatar() {
        return viewerAvatar;
    }

    public void setViewerAvatar(String viewerAvatar) {
        this.viewerAvatar = viewerAvatar;
    }

    public PLVPlaybackListType getPlaybackListType() {
        return playbackListType;
    }

    public void setPlaybackListType(PLVPlaybackListType playbackListType) {
        this.playbackListType = playbackListType;
    }

    @Override
    public String toString() {
        return "PLVPlaybackCacheViewerInfoVO{" +
                "channelId='" + channelId + '\'' +
                ", channelType=" + channelType +
                ", vid='" + vid + '\'' +
                ", viewerId='" + viewerId + '\'' +
                ", viewerName='" + viewerName + '\'' +
                ", viewerAvatar='" + viewerAvatar + '\'' +
                ", playbackListType=" + playbackListType +
                '}';
    }

    public PLVPlaybackCacheViewerInfoVO copy() {
        PLVPlaybackCacheViewerInfoVO vo = new PLVPlaybackCacheViewerInfoVO();
        vo.setChannelId(channelId);
        vo.setChannelType(channelType);
        vo.setVid(vid);
        vo.setViewerId(viewerId);
        vo.setViewerName(viewerName);
        vo.setViewerAvatar(viewerAvatar);
        vo.setPlaybackListType(playbackListType);
        return vo;
    }

}
