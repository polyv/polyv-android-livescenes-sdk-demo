package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.enums.PLVPlaybackCacheDownloadStatusEnum;
import com.plv.thirdpart.blankj.utilcode.constant.MemoryConstants;

import java.util.Locale;

/**
 * @author Hoshiiro
 */
@Entity(tableName = "playback_cache_video_table")
public class PLVPlaybackCacheVideoVO {

    /**
     * 暂存视频：fileId，32字符长度
     * <p>
     * 回放视频：videoPoolId，32字符长度
     */
    @PrimaryKey
    @NonNull
    private String videoPoolId;

    /**
     * 暂存视频：fileId，32字符长度
     * <p>
     * 回放视频：videoId，10字符长度
     */
    private String videoId;
    private String title;
    private String firstImageUrl;
    private String videoDuration;
    private String liveType;
    private String channelSessionId;
    private String originSessionId;
    private Boolean enableDownload;

    @IntRange(from = 0, to = 100)
    private Integer progress;
    private Long downloadedBytes;
    private Long totalBytes;
    private PLVPlaybackCacheDownloadStatusEnum downloadStatusEnum;

    private String videoPath;
    private String pptPath;
    private String jsPath;

    @Embedded
    private PLVPlaybackCacheViewerInfoVO viewerInfoVO = new PLVPlaybackCacheViewerInfoVO();

    public String getVideoPoolId() {
        return videoPoolId;
    }

    public void setVideoPoolId(String videoPoolId) {
        this.videoPoolId = videoPoolId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstImageUrl() {
        return firstImageUrl;
    }

    public void setFirstImageUrl(String firstImageUrl) {
        this.firstImageUrl = firstImageUrl;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getLiveType() {
        return liveType;
    }

    public void setLiveType(String liveType) {
        this.liveType = liveType;
    }

    public String getChannelSessionId() {
        return channelSessionId;
    }

    public void setChannelSessionId(String channelSessionId) {
        this.channelSessionId = channelSessionId;
    }

    public String getOriginSessionId() {
        return originSessionId;
    }

    public void setOriginSessionId(String originSessionId) {
        this.originSessionId = originSessionId;
    }

    public Boolean isEnableDownload() {
        return enableDownload;
    }

    public void setEnableDownload(Boolean enableDownload) {
        this.enableDownload = enableDownload;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(Long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getPptPath() {
        return pptPath;
    }

    public void setPptPath(String pptPath) {
        this.pptPath = pptPath;
    }

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public PLVPlaybackCacheDownloadStatusEnum getDownloadStatusEnum() {
        return downloadStatusEnum;
    }

    public void setDownloadStatusEnum(PLVPlaybackCacheDownloadStatusEnum downloadStatusEnum) {
        this.downloadStatusEnum = downloadStatusEnum;
    }

    public PLVPlaybackCacheViewerInfoVO getViewerInfoVO() {
        return viewerInfoVO;
    }

    public void setViewerInfoVO(PLVPlaybackCacheViewerInfoVO viewerInfoVO) {
        this.viewerInfoVO = viewerInfoVO;
    }

    public void clearDownloadStatus() {
        progress = 0;
        downloadedBytes = 0L;
        downloadStatusEnum = PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST;
        videoPath = null;
        jsPath = null;
        pptPath = null;
    }

    public PLVPlaybackCacheVideoVO copy() {
        PLVPlaybackCacheVideoVO copy = new PLVPlaybackCacheVideoVO();
        copy.setVideoPoolId(videoPoolId);
        copy.setVideoId(videoId);
        copy.setTitle(title);
        copy.setFirstImageUrl(firstImageUrl);
        copy.setVideoDuration(videoDuration);
        copy.setLiveType(liveType);
        copy.setChannelSessionId(channelSessionId);
        copy.setOriginSessionId(originSessionId);
        copy.setEnableDownload(enableDownload);
        copy.setProgress(progress);
        copy.setDownloadedBytes(downloadedBytes);
        copy.setTotalBytes(totalBytes);
        copy.setVideoPath(videoPath);
        copy.setPptPath(pptPath);
        copy.setJsPath(jsPath);
        copy.setDownloadStatusEnum(downloadStatusEnum);
        copy.setViewerInfoVO(viewerInfoVO.copy());
        return copy;
    }

    public PLVPlaybackCacheVideoVO mergeFrom(PLVPlaybackCacheVideoVO old) {
        if (old == null) {
            return this;
        }
        if (this.getVideoId() == null) {
            this.setVideoId(old.getVideoId());
        }
        if (this.getTitle() == null) {
            this.setTitle(old.getTitle());
        }
        if (this.getFirstImageUrl() == null) {
            this.setFirstImageUrl(old.getFirstImageUrl());
        }
        if (this.getVideoDuration() == null) {
            this.setVideoDuration(old.getVideoDuration());
        }
        if (this.getLiveType() == null) {
            this.setLiveType(old.getLiveType());
        }
        if (this.getChannelSessionId() == null) {
            this.setChannelSessionId(old.getChannelSessionId());
        }
        if (this.getOriginSessionId() == null) {
            this.setOriginSessionId(old.getOriginSessionId());
        }
        if (this.isEnableDownload() == null) {
            this.setEnableDownload(old.isEnableDownload());
        }
        if (this.getProgress() == null) {
            this.setProgress(old.getProgress());
        }
        if (this.getDownloadedBytes() == null) {
            this.setDownloadedBytes(old.getDownloadedBytes());
        }
        if (this.getTotalBytes() == null) {
            this.setTotalBytes(old.getTotalBytes());
        }
        if (this.getVideoPath() == null) {
            this.setVideoPath(old.getVideoPath());
        }
        if (this.getPptPath() == null) {
            this.setPptPath(old.getPptPath());
        }
        if (this.getJsPath() == null) {
            this.setJsPath(old.getJsPath());
        }
        if (this.getDownloadStatusEnum() == null) {
            this.setDownloadStatusEnum(old.getDownloadStatusEnum());
        }
        if (this.getViewerInfoVO() == null) {
            this.setViewerInfoVO(old.getViewerInfoVO());
        }
        return this;
    }

    @NonNull
    public static String bytesToFitSizeString(@Nullable Long byteNum) {
        if (byteNum == null) {
            return "-";
        }

        final long bytes = byteNum;
        if (bytes < 0) {
            return "-";
        } else if (bytes < MemoryConstants.KB) {
            return String.format(Locale.CHINA, "%.1fB", (double) bytes);
        } else if (bytes < MemoryConstants.MB) {
            return String.format(Locale.CHINA, "%.1fK", (double) bytes / MemoryConstants.KB);
        } else if (bytes < MemoryConstants.GB) {
            return String.format(Locale.CHINA, "%.1fM", (double) bytes / MemoryConstants.MB);
        } else {
            return String.format(Locale.CHINA, "%.1fG", (double) bytes / MemoryConstants.GB);
        }
    }
}
