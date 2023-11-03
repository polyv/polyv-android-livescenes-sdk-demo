package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.SkipQueryVerification;
import androidx.room.TypeConverters;

import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.converter.PLVRedPaperReceiveTypeConverter;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.dao.IPLVRedpackCacheDAO;
import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.entity.PLVRedpackCacheVO;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * @author Hoshiiro
 */
@Database(entities = {PLVRedpackCacheVO.class}, version = 1, exportSchema = false)
@SkipQueryVerification
@TypeConverters({
        PLVRedPaperReceiveTypeConverter.class
})
public abstract class PLVRedpackCacheDataBase extends RoomDatabase {

    public static PLVRedpackCacheDataBase getInstance() {
        return Room.databaseBuilder(Utils.getApp(), PLVRedpackCacheDataBase.class, "plv_redpack_cache.db").build();
    }

    public abstract IPLVRedpackCacheDAO getRedpackCacheDAO();

}
