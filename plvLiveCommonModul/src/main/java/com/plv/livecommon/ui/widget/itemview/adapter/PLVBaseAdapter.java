package com.plv.livecommon.ui.widget.itemview.adapter;

import android.support.v7.widget.RecyclerView;

import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

import java.util.List;

/**
 * 基础adapter
 */
public abstract class PLVBaseAdapter<Data extends PLVBaseViewData, Holder extends PLVBaseViewHolder> extends RecyclerView.Adapter<Holder> {

    public abstract List<Data> getDataList();
}
