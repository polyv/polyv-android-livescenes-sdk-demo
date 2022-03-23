package com.plv.livecloudclass.modules.pagemenu.previous.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.plv.livecloudclass.R;
import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livecommon.module.modules.previous.customview.PLVChapterAdapter;

/**
 * 回放章节列表适配器
 */
public class PLVLCChapterAdapter extends PLVChapterAdapter<PLVBaseViewData, PLVLCChapterViewHolder> {
    @NonNull
    @Override
    public PLVLCChapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PLVLCChapterViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvlc_chapter_list_item, viewGroup, false), this);
    }
}
