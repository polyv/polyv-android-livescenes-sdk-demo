package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.converter;

import androidx.room.TypeConverter;

import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;

/**
 * @author Hoshiiro
 */
public class PLVRedPaperReceiveTypeConverter {

    @TypeConverter
    public String serialize(PLVRedPaperReceiveType redPaperReceiveType) {
        if (redPaperReceiveType == null) {
            return PLVRedPaperReceiveType.AVAILABLE.name();
        }
        return redPaperReceiveType.name();
    }

    @TypeConverter
    public PLVRedPaperReceiveType deserialize(String value) {
        try {
            return PLVRedPaperReceiveType.valueOf(value);
        } catch (Exception e) {
            return PLVRedPaperReceiveType.AVAILABLE;
        }
    }

}
