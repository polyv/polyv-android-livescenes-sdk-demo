package com.easefun.polyv.livecloudclass.modules.pagemenu.venue;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.venue.view.PLVMultiVenueAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.feature.venues.model.PLVVenueDataVO;

import java.util.ArrayList;
import java.util.List;

public class PLVLCMultiVenueAdapter extends PLVMultiVenueAdapter<PLVBaseViewData, PLVLCMultiVenueViewHolder, PLVVenueDataVO> {

    @NonNull
    @Override
    public PLVLCMultiVenueViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new PLVLCMultiVenueViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plvlc_multi_venue_item, viewGroup, false), this);
    }

    /**
     * 比较新旧数据变化，有变化就动态变换状态
     * @param newDataList
     */
    @Override
    public void updateStatusList(List<PLVVenueDataVO> newDataList) {
        List<PLVBaseViewData> list = getDataList();
        DiffUtil.Callback callback = new VenueDiffCallback(list, newDataList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        list.clear();
        list.addAll(toBaseViewData(newDataList));
        result.dispatchUpdatesTo(this);
    }

    private List<PLVBaseViewData> toBaseViewData(List<PLVVenueDataVO> list) {
        List<PLVBaseViewData> venueList = new ArrayList<>();
        for (PLVVenueDataVO s : list) {
            venueList.add(new PLVBaseViewData(s, PLVBaseViewData.ITEMTYPE_UNDEFINED));
        }
        return venueList;
    }


    // <editor-fold defaultstate="collapsed" desc="差分更新">
    private class VenueDiffCallback extends DiffUtil.Callback {

        private List<PLVBaseViewData> oldList;
        private List<PLVVenueDataVO> newList;

        public VenueDiffCallback(List<PLVBaseViewData> oldList, List<PLVVenueDataVO> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            PLVVenueDataVO oldValue = (PLVVenueDataVO) oldList.get(oldItemPosition).getData();
            PLVVenueDataVO newValue = newList.get(newItemPosition);
            return oldValue.getChannelId().equals(newValue.getChannelId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            PLVVenueDataVO oldValue = (PLVVenueDataVO) oldList.get(oldItemPosition).getData();
            PLVVenueDataVO newValue = newList.get(newItemPosition);
            boolean diff = !oldValue.getLiveStatus().equals(newValue.getLiveStatus());
            if (diff) {
                oldValue.setLiveStatusDesc(newValue.getLiveStatusDesc());
                oldValue.setLiveStatus(newValue.getLiveStatus());
            }
            return !diff;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // 取消更新时闪烁动画
            return new Object();
        }
    }
    // </editor-fold>

}
