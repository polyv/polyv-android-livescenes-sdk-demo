package com.easefun.polyv.livecloudclass.modules.download.adapter.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

/**
 * @author Hoshiiro
 */
public abstract class PLVAbsPlaybackCacheViewHolder extends RecyclerView.ViewHolder {

    public static final int TYPE_DOWNLOADING_CACHE = 1;
    public static final int TYPE_DOWNLOADED_CACHE = 2;

    public static class Factory {
        public static PLVAbsPlaybackCacheViewHolder create(@NonNull ViewGroup viewGroup, int cacheType) {
            switch (cacheType) {
                case TYPE_DOWNLOADING_CACHE:
                    return new PLVDownloadingCacheViewHolder(PLVDownloadingCacheViewHolder.createView(viewGroup));
                case TYPE_DOWNLOADED_CACHE:
                default:
                    return new PLVDownloadedCacheViewHolder(PLVDownloadedCacheViewHolder.createView(viewGroup));
            }
        }
    }

    public PLVAbsPlaybackCacheViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(PLVPlaybackCacheVideoVO playbackCacheDownloadStatusVO);

}
