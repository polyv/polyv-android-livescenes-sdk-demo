package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.converter;

import androidx.room.TypeConverter;

import com.plv.livescenes.playback.video.PLVPlaybackListType;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackListTypeConverter {

    @TypeConverter
    public String serialize(PLVPlaybackListType playbackListType) {
        if (playbackListType == null) {
            return "";
        }
        return playbackListType.name();
    }

    @TypeConverter
    public PLVPlaybackListType deserialize(String value) {
        try {
            return PLVPlaybackListType.valueOf(value);
        } catch (Exception e) {
            return PLVPlaybackListType.TEMP_STORE;
        }
    }

}
