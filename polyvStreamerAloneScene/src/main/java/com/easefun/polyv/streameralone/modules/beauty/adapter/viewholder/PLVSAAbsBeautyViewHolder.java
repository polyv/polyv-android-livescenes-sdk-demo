package com.easefun.polyv.streameralone.modules.beauty.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.streameralone.modules.beauty.adapter.PLVSABeautyOptionAdapter;
import com.easefun.polyv.streameralone.modules.beauty.adapter.vo.PLVSABeautyOptionVO;

/**
 * @author Hoshiiro
 */
public abstract class PLVSAAbsBeautyViewHolder extends RecyclerView.ViewHolder {

    public static class Factory {

        public static PLVSAAbsBeautyViewHolder create(@NonNull ViewGroup viewGroup, @PLVSABeautyOptionAdapter.ItemType int itemType) {
            switch (itemType) {
                case PLVSABeautyOptionAdapter.ItemType.TYPE_FILTER:
                    return PLVSAFilterViewHolder.create(viewGroup);
                case PLVSABeautyOptionAdapter.ItemType.TYPE_BEAUTY:
                case PLVSABeautyOptionAdapter.ItemType.TYPE_DETAIL:
                default:
                    return PLVSABeautyViewHolder.create(viewGroup);
            }
        }

    }

    PLVSAAbsBeautyViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(PLVSABeautyOptionVO beautyOptionVO);

}
