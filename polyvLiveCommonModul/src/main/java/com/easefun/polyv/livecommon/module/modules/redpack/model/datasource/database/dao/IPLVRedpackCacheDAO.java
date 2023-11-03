package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.entity.PLVRedpackCacheVO;

/**
 * @author Hoshiiro
 */
@Dao
public interface IPLVRedpackCacheDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PLVRedpackCacheVO redpackCacheVO);

    @Delete
    void delete(PLVRedpackCacheVO redpackCacheVO);

    @Update
    void update(PLVRedpackCacheVO redpackCacheVO);

    @Query("SELECT * FROM plv_redpack_cache_table WHERE primaryKey = :primaryKey LIMIT 1")
    PLVRedpackCacheVO get(String primaryKey);

}
