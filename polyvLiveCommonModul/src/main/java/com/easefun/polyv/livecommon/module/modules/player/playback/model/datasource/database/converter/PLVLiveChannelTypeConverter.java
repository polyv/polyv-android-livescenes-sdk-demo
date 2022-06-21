package com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.converter;

import androidx.room.TypeConverter;

import com.plv.livescenes.config.PLVLiveChannelType;

/**
 * @author Hoshiiro
 */
public class PLVLiveChannelTypeConverter {

    @TypeConverter
    public String serialize(PLVLiveChannelType liveChannelType) {
        if (liveChannelType == null) {
            return "";
        }
        return liveChannelType.name();
    }

    @TypeConverter
    public PLVLiveChannelType deserialize(String value) {
        try {
            return PLVLiveChannelType.valueOf(value);
        } catch (Exception e) {
            return PLVLiveChannelType.ALONE;
        }
    }

}
