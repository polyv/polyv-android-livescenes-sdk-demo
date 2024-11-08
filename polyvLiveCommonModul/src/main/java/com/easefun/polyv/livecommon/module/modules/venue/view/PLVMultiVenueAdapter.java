package com.easefun.polyv.livecommon.module.modules.venue.view;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class PLVMultiVenueAdapter<Data extends PLVBaseViewData, Holder extends PLVBaseViewHolder, T> extends PLVBaseAdapter<Data, Holder> {
    //<editor-fold defaultstate="collapsed" desc="变量">
    private List<Data> dataList = new ArrayList<>();

    private String mainChannelId;

    //当前播放的位置
    private int currentPosition = 0;

    //点击监听事件
    private OnViewActionListener listener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="需要实现的方法">

    @Override
    public List<Data> getDataList() {
        return dataList;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外提供的方法">

    /**
     * 加载更多
     *
     * @param moreList 需要增加的列表数据
     */
    public void loadMore(List<Data> moreList) {
        if (moreList.isEmpty()) {
            return;
        }
        int position = dataList.size();
        this.dataList.addAll(moreList);
        notifyItemRangeChanged(position, dataList.size());
    }

    public void setDataList(List<Data> list) {
        if (list.isEmpty()) {
            return;
        }
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void updateStatusList(List<T> newList) {

    }

    public void setMainChannelId(String mainChannelId) {
        this.mainChannelId = mainChannelId;
    }

    public String getMainChannelId() {
        return this.mainChannelId;
    }

    /**
     * 返回当前播放的位置
     *
     * @return 当前播放位置
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        currentPosition = position;
    }

    /**
     * 改变会场点击监听回调
     */
    public void callChangeVenueClick(String channelId, boolean isPlayback) {
        if (listener != null) {
            listener.changeVenueClick(channelId, isPlayback);

        }
    }

    public void setOnViewActionListener(OnViewActionListener actionListener) {
        this.listener = actionListener;
    }

    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="点击事件">

    public interface OnViewActionListener {
        void changeVenueClick(String channelId, boolean isPlayback);
    }

    // </editor-fold>


}
