package com.easefun.polyv.livecommon.module.modules.redpack.model.datasource.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.plv.socket.event.redpack.enums.PLVRedPaperReceiveType;

/**
 * @author Hoshiiro
 */
@Entity(tableName = "plv_redpack_cache_table")
public class PLVRedpackCacheVO {

    @PrimaryKey
    @NonNull
    private String primaryKey = "";

    private String redpackId;
    private String redCacheId;
    private String roomId;
    private String viewerId;
    private PLVRedPaperReceiveType redPaperReceiveType;

    @NonNull
    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(@NonNull String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getRedpackId() {
        return redpackId;
    }

    public void setRedpackId(String redpackId) {
        this.redpackId = redpackId;
    }

    public String getRedCacheId() {
        return redCacheId;
    }

    public void setRedCacheId(String redCacheId) {
        this.redCacheId = redCacheId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public PLVRedPaperReceiveType getRedPaperReceiveType() {
        return redPaperReceiveType;
    }

    public void setRedPaperReceiveType(PLVRedPaperReceiveType redPaperReceiveType) {
        this.redPaperReceiveType = redPaperReceiveType;
    }

    @Override
    public String toString() {
        return "PLVRedpackCacheVO{" +
                "primaryKey='" + primaryKey + '\'' +
                ", redpackId='" + redpackId + '\'' +
                ", redCacheId='" + redCacheId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", viewerId='" + viewerId + '\'' +
                ", redPaperReceiveType=" + redPaperReceiveType +
                '}';
    }
}
