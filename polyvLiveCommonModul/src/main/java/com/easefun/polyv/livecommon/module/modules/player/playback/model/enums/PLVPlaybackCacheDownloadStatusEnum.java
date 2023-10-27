package com.easefun.polyv.livecommon.module.modules.player.playback.model.enums;

import androidx.room.TypeConverter;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.utils.PLVAppUtils;

/**
 * @author Hoshiiro
 */
public enum PLVPlaybackCacheDownloadStatusEnum {

    NOT_IN_DOWNLOAD_LIST("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_un_download);
        }
    },

    WAITING("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_waiting);
        }
    },

    PAUSING("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_pausing);
        }
    },

    DOWNLOADING("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_downloading);
        }
    },

    DOWNLOADED("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_downloaded);
        }
    },

    DOWNLOAD_FAIL("") {
        @Override
        public String getStatusName() {
            return PLVAppUtils.getString(R.string.plv_download_fail);
        }
    };

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
