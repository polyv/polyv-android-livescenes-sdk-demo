package com.easefun.polyv.livecloudclass.modules.pagemenu.previous.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;

/**
 * 往期视频列表具体实现的Adapter
 * 用于设置想要使用的ViewHolder
 */
public class PLVLCPreviousAdapter extends PLVPreviousAdapter<PLVBaseViewData, PLVLCPreviousViewHolder> {
    @NonNull
    @Override
    public PLVLCPreviousViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PLVLCPreviousViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvlc_previous_list_item, viewGroup, false), this);
    }

}
