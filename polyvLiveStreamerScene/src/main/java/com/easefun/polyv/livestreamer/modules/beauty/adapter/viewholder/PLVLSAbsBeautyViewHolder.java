package com.easefun.polyv.livestreamer.modules.beauty.adapter.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livestreamer.modules.beauty.adapter.PLVLSBeautyOptionAdapter;
import com.easefun.polyv.livestreamer.modules.beauty.adapter.vo.PLVLSBeautyOptionVO;


/**
 * @author Hoshiiro
 */
public abstract class PLVLSAbsBeautyViewHolder extends RecyclerView.ViewHolder {

    public static class Factory {

        public static PLVLSAbsBeautyViewHolder create(@NonNull ViewGroup viewGroup, @PLVLSBeautyOptionAdapter.ItemType int itemType) {
            switch (itemType) {
                case PLVLSBeautyOptionAdapter.ItemType.TYPE_FILTER:
                    return PLVLSFilterViewHolder.create(viewGroup);
                case PLVLSBeautyOptionAdapter.ItemType.TYPE_BEAUTY:
                case PLVLSBeautyOptionAdapter.ItemType.TYPE_DETAIL:
                default:
                    return PLVLSBeautyViewHolder.create(viewGroup);
            }
        }

    }

    PLVLSAbsBeautyViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(PLVLSBeautyOptionVO beautyOptionVO);

}
