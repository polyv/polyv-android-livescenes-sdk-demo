package com.easefun.polyv.livecommon.module.modules.previous.customview;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.View;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.plv.livescenes.model.PLVPlaybackListVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:lzj
 * Time:2021/12/28
 * Description: 回放视频列表的adapter
 */
public abstract class PLVPreviousAdapter<Data extends PLVBaseViewData, Holder extends PLVBaseViewHolder> extends PLVBaseAdapter<Data, Holder> {
    //<editor-fold defaultstate="collapsed" desc="变量">
    private final List<Data> dataList;

    //当前播放的位置
    private int currentPosition = 0;

    //点击监听事件
    private OnViewActionListener listener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVPreviousAdapter() {
        dataList = new ArrayList<>();
    }
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

    /**
     * 添加列表（注意这个方法会重置列表数据
     *
     * @param list 数据列表
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<Data> list) {
        if (list.isEmpty()) {
            return;
        }
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 返回当前播放的位置
     *
     * @return 当前播放位置
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * 设置当前位置
     *
     * @param position 当前位置
     */
    public void setCurrentPosition(int position) {
        currentPosition = position;
    }

    /**
     * 回放视频的点击监听回调
     *
     * @param view     view
     * @param bean     回放视频
     * @param position 点击的位置
     */
    public void callChangeVideoVidClick(View view, PLVPlaybackListVO.DataBean.ContentsBean bean, int position) {
        if (listener != null) {
            listener.changeVideoVidClick(bean.getVideoPoolId());
            int old = currentPosition;
            currentPosition = position;
            notifyItemChanged(old);
            notifyItemChanged(currentPosition);
        }
    }

    /**
     * 设置点击事件监听
     * @param actionListener
     */
    public void setOnViewActionListener(OnViewActionListener actionListener) {
        this.listener = actionListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">

    public interface OnViewActionListener {
        void changeVideoVidClick(String vid);
    }

    // </editor-fold>
}
