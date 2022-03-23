package com.plv.liveecommerce.modules.playback.fragments.previous;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livecommon.module.modules.previous.customview.PLVPreviousAdapter;
import com.plv.liveecommerce.R;

/**
 * 纯视频的往期列表适配器
 */
public class PLVECPreviousAdapter extends PLVPreviousAdapter<PLVBaseViewData, PLVECPreviousViewHolder> {
    @NonNull
    @Override
    public PLVECPreviousViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PLVECPreviousViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvec_playback_previous_list_item, viewGroup, false), this);
    }
}
