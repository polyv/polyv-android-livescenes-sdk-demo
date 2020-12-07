package com.easefun.polyv.liveecommerce.modules.reward;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftBean;
import com.easefun.polyv.livecommon.ui.widget.PLVMessageRecyclerView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 打赏礼物列表adapter
 */
public class PLVECRewardGiftAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVECRewardGiftAdapter>> {
    private List<PLVBaseViewData> dataList;
    private View lastSelectView;

    public PLVECRewardGiftAdapter() {
        dataList = new ArrayList<>();
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    public void setDataList(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
    }

    public View getLastSelectView() {
        return lastSelectView;
    }

    public void setLastSelectView(View lastSelectView) {
        this.lastSelectView = lastSelectView;
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onRewardClick(View view, PLVCustomGiftBean giftBean);
    }

    public void callOnRewardClick(View view, PLVCustomGiftBean giftBean) {
        if (onViewActionListener != null) {
            onViewActionListener.onRewardClick(view, giftBean);
        }
    }
    // </editor-fold>

    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVECRewardGiftAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PLVECRewardGiftViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_live_reward_gift_item, parent, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECRewardGiftAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECRewardGiftAdapter> holder) {
        super.onViewAttachedToWindow(holder);
        //适配item的宽度及间隔
        if (holder.itemView != null && holder.itemView.getParent() instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) holder.itemView.getParent();
            final int width = recyclerView.getWidth();
            if (width > 0 && recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                final int spanCount = ((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount();
                final int itemWidth = ConvertUtils.dp2px(72);
                if (itemWidth * spanCount <= width) {
                    holder.itemView.getLayoutParams().width = itemWidth;
                    if (recyclerView.getItemDecorationCount() == 0) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (recyclerView.getItemDecorationCount() == 0) {
                                    int space = width - itemWidth * spanCount;
                                    recyclerView.addItemDecoration(new PLVMessageRecyclerView.GridSpacingItemDecoration(spanCount, (int) (space * 1f / (spanCount - 1)), false, 0));
                                }
                            }
                        });
                    }
                } else {
                    holder.itemView.getLayoutParams().width = width / spanCount;
                }
            }
        }
    }
}
