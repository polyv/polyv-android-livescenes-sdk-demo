package com.easefun.polyv.livestreamer.modules.liveroom.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.streamer.model.vo.PLVSipLinkMicViewerVO;
import com.easefun.polyv.livestreamer.modules.liveroom.adapter.viewholder.PLVLSSipLinkMicViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVLSSipLinkMicAdapter extends RecyclerView.Adapter<PLVLSSipLinkMicViewHolder> {

    private final List<PLVSipLinkMicViewerVO> sipLinkMicViewerList = new ArrayList<>();

    @NonNull
    @Override
    public PLVLSSipLinkMicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
        return PLVLSSipLinkMicViewHolder.Factory.create(viewGroup, itemType);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVLSSipLinkMicViewHolder sipLinkMicViewHolder, int index) {
        sipLinkMicViewHolder.bind(sipLinkMicViewerList.get(index));
    }

    @Override
    public int getItemCount() {
        return sipLinkMicViewerList.size();
    }

    public void updateListWithRefresh(List<PLVSipLinkMicViewerVO> list) {
        sipLinkMicViewerList.clear();
        sipLinkMicViewerList.addAll(list);
        notifyDataSetChanged();
    }

    public void updateListWithDiff(final List<PLVSipLinkMicViewerVO> list) {
        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return sipLinkMicViewerList.size();
            }

            @Override
            public int getNewListSize() {
                return list.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int i1) {
                return sipLinkMicViewerList.get(i) == list.get(i1);
            }

            @Override
            public boolean areContentsTheSame(int i, int i1) {
                return sipLinkMicViewerList.get(i).equals(list.get(i1));
            }
        }).dispatchUpdatesTo(this);
        sipLinkMicViewerList.clear();
        sipLinkMicViewerList.addAll(list);
    }

}
