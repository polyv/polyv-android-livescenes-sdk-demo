package com.easefun.polyv.livecommon.module.modules.reward.view.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

import java.util.List;

/**
 * 打赏礼物列表adapter
 */
public class PLVRewardListAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVRewardListAdapter>> {

    //打赏类型（由于不是聊天消息，所以没有加到PLVChatMessageItemType，而是在这里单独管理
    //礼物-积分打赏
    public static final int ITEM_GIFT_POINT_REWARD = 1;
    //礼物-现金支付
    public static final int ITEM_GIFT_CASH_REWARD = 2;
    //现金打赏
    public static final int ITEM_CASH_REWARD = 3;
    //自定义打赏消息（道具打赏
    @Deprecated
    public static final int ITEM_PROP_REWARD = 3;

    private List<PLVBaseViewData> dataList;

    private OnCheckItemListener onCheckItemListener;

    private PLVBaseViewData selectData;

    //横屏标记
    private boolean isLandscape;

    public void setDataList(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
    }

    public void setOnCheckItemListener(OnCheckItemListener onCheckItemListener) {
        this.onCheckItemListener = onCheckItemListener;
    }

    public PLVBaseViewData getSelectData(){
        return selectData;
    }

    public void clearSelectState(){
        if(selectData != null){
            int item = (int) selectData.getTag();
            selectData.setTag(-1);
            notifyItemChanged(item);
        }
    }

    public PLVRewardListAdapter() {
    }

    public PLVRewardListAdapter(boolean isLandscape) {
        this.isLandscape = isLandscape;
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVRewardListAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case ITEM_GIFT_POINT_REWARD:
            case ITEM_GIFT_CASH_REWARD:
                if(!isLandscape) {
                    return new PLVRewardPointViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_point_reward_item_portrait, parent, false), this);
                } else {
                    return new PLVRewardPointViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_point_reward_item_landscape, parent, false), this);
                }
            case ITEM_PROP_REWARD:
                //道具打赏实现
                return null;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final PLVBaseViewHolder<PLVBaseViewData, PLVRewardListAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);

        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(pos);

                if (onCheckItemListener != null) {
                    onCheckItemListener.onItemCheck(selectData, pos);
                }
            }
        });

        if(selectData != null){
            int selectPosition = (int) selectData.getTag();
            holder.itemView.setSelected(selectPosition == position);
        } else {
            holder.itemView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getItemType();
    }

    private void selectItem(int position){
        if(selectData != null){
            int item = (int) selectData.getTag();
            selectData.setTag(position);
            notifyItemChanged(item);
        }

        selectData = dataList.get(position);
        selectData.setTag(position);
        notifyItemChanged(position);
    }

    /**
     * 选择打赏礼物item监听
     */
    public interface OnCheckItemListener{
        /**
         * 返回当前
         * @param position
         */
        void onItemCheck(PLVBaseViewData selectData, int position);
    }

}
