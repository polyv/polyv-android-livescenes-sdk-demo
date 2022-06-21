package com.easefun.polyv.livecommon.module.modules.player.playback.model.enums;

import androidx.room.TypeConverter;

/**
 * @author Hoshiiro
 */
public enum PLVPlaybackCacheDownloadStatusEnum {

    NOT_IN_DOWNLOAD_LIST("未下载"),

    WAITING("等待中"),

    PAUSING("已暂停"),

    DOWNLOADING("下载中"),

    DOWNLOADED("已下载"),

    DOWNLOAD_FAIL("下载失败");

    private final String statusName;

    PLVPlaybackCacheDownloadStatusEnum(
            final String statusName
    ) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    public static class Converter {

        @TypeConverter
        public String serialize(PLVPlaybackCacheDownloadStatusEnum statusEnum) {
            if (statusEnum == null) {
                return "";
            }
            return statusEnum.name();
        }

        @TypeConverter
        public PLVPlaybackCacheDownloadStatusEnum deserialize(String value) {
            try {
                return PLVPlaybackCacheDownloadStatusEnum.valueOf(value);
            } catch (Exception e) {
                return PLVPlaybackCacheDownloadStatusEnum.NOT_IN_DOWNLOAD_LIST;
            }
        }

    }

}
