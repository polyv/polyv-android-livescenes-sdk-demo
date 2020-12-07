package com.easefun.polyv.livecommon.ui.widget.itemview.holder;

import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;

/**
 * 基础viewHolder
 */
public class PLVBaseViewHolder<Data extends PLVBaseViewData, Adapter extends PLVBaseAdapter> extends RecyclerView.ViewHolder {
    protected Adapter adapter;
    protected Data data;

    public PLVBaseViewHolder(View itemView, Adapter adapter) {
        super(itemView);
        this.adapter = adapter;
    }

    protected int getVHPosition() {
        int position = 0;//item 移动时 position 需更新
        for (int i = 0; i < adapter.getDataList().size(); i++) {
            Object obj = adapter.getDataList().get(i);
            if (obj == data) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void processData(Data data, int position) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findViewById(@IdRes int id) {
        return (T) itemView.findViewById(id);
    }
}
