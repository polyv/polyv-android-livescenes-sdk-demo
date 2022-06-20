package com.easefun.polyv.livecloudclass.modules.download.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.modules.download.adapter.viewholder.PLVAbsPlaybackCacheViewHolder;
import com.easefun.polyv.livecommon.module.modules.player.playback.model.datasource.database.entity.PLVPlaybackCacheVideoVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVPlaybackCacheAdapter extends RecyclerView.Adapter<PLVAbsPlaybackCacheViewHolder> {

    private final List<PLVPlaybackCacheVideoVO> playbackCacheList = new ArrayList<>();

    private final int cacheType;

    public PLVPlaybackCacheAdapter(int cacheType) {
        this.cacheType = cacheType;
    }

    @NonNull
    @Override
    public PLVAbsPlaybackCacheViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        return PLVAbsPlaybackCacheViewHolder.Factory.create(viewGroup, type);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVAbsPlaybackCacheViewHolder viewHolder, int index) {
        viewHolder.bind(playbackCacheList.get(index));
    }

    @Override
    public int getItemCount() {
        return playbackCacheList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return cacheType;
    }

    public void updateData(final List<PLVPlaybackCacheVideoVO> playbackCacheList) {
        this.playbackCacheList.clear();
        this.playbackCacheList.addAll(playbackCacheList);
        notifyDataSetChanged();
    }
}
