package com.easefun.polyv.livecommon.module.modules.chapter.view;

import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:lzj
 * Time:2021/12/29
 * Description:回放视频-章节的adapter
 */
public abstract class PLVChapterAdapter<Data extends PLVBaseViewData, Holder extends PLVBaseViewHolder> extends PLVBaseAdapter<Data, Holder> {

    //<editor-fold defaultstate="collapsed" desc="变量">
    private List<Data> dataList = new ArrayList<>();

    //当前播放的时间点
    private int currentPosition = 0;

    //点击监听
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
     * 设置章节数据列表
     *
     * @param list 章节数据类别
     */
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
     * 更新Adapter中的时间，当播放的时间等于某一个时间就更新adapter
     *
     * @param position 更新的位置
     */
    public void updataItmeTime(int position) {
        // 通过二分法来找出 适合当前的章节，他需要选中比他小的章节并且这个章节是所有小的章节中是最大的章节
        if (currentPosition != position) {
            updateItem(position);
        }
    }

    /**
     * 跳转进度的点击监听
     *
     * @param timeStamp 跳转的时间
     * @param position  位置
     */
    public void callChangeVideoSeekClick(int timeStamp, int position) {
        if (listener != null) {
            listener.changeVideoSeekClick(timeStamp);
            updateItem(position);
        }
    }

    /**
     * 设置点击事件
     *
     * @param actionListener 点击监听
     */
    public void setOnViewActionListener(OnViewActionListener actionListener) {
        this.listener = actionListener;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    /**
     * 更新item
     *
     * @param newPosition
     */
    private void updateItem(int newPosition) {
        int old = currentPosition;
        currentPosition = newPosition;
        notifyItemChanged(old);
        notifyItemChanged(currentPosition);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类 - view交互事件监听器">
    public interface OnViewActionListener {
        void changeVideoSeekClick(int timeStamp);
    }
    // </editor-fold>
}
