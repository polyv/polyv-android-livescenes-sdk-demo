package com.easefun.polyv.liveecommerce.modules.commodity;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.commodity.PLVProductContentBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品列表adapter
 */
public class PLVECCommodityAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter>> {
    private List<PLVBaseViewData> dataList;
    private int lastExistedRank = -1;//列表中最后存在过的商品rank

    public PLVECCommodityAdapter() {
        dataList = new ArrayList<>();
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    public void setDataList(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
        if (!this.dataList.isEmpty()) {
            lastExistedRank = ((PLVProductContentBean) this.dataList.get(this.dataList.size() - 1).getData()).getRank();
        } else {
            lastExistedRank = -1;
        }
    }

    public void addDataList(List<PLVBaseViewData> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        int oldSize = this.dataList.size();
        this.dataList.addAll(dataList);
        lastExistedRank = ((PLVProductContentBean) this.dataList.get(this.dataList.size() - 1).getData()).getRank();
        notifyItemRangeInserted(oldSize, dataList.size());
    }

    //新增
    public void add(int index, PLVProductContentBean contentBean) {
        dataList.add(index, new PLVBaseViewData(contentBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
        notifyItemRangeInserted(index, 1);
        if (index == 0) {//only use insert 0, old index 0 padding not update
            notifyItemChanged(1);
        } else {
            for (int i = 0; i < index; i++) {//更新显示序号
                PLVProductContentBean content = (PLVProductContentBean) dataList.get(i).getData();
                content.setShowId(content.getShowId() + 1);
            }
            notifyItemRangeChanged(0, index, "payload");

            updateLastExistedRank(index, contentBean);
        }
    }

    //更新
    public void update(int index, PLVProductContentBean contentBean) {
        dataList.remove(index);
        dataList.add(index, new PLVBaseViewData(contentBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
        notifyItemChanged(index);

        updateLastExistedRank(index, contentBean);
    }

    //删除
    public void delete(int index) {
        dataList.remove(index);
        notifyItemRemoved(index);
        if (index == 0) {//only use delete 0, old index 0 padding not update
            notifyItemChanged(0);
        } else {
            for (int i = 0; i < index; i++) {//更新显示序号
                PLVProductContentBean content = (PLVProductContentBean) dataList.get(i).getData();
                content.setShowId(content.getShowId() - 1);
            }
            notifyItemRangeChanged(0, index, "payload");
        }
    }

    //上/下移动
    public void move(PLVProductContentBean handleContent, PLVProductContentBean contentBean) {
        int handleIndex = isExistProduct(handleContent.getProductId());
        int index = isExistProduct(contentBean.getProductId());
        if (handleIndex == -1 && index == -1) {//操作的商品都不在列表中
            return;
        }
        if (handleIndex != -1 && index != -1) {//操作的商品都在列表中
            dataList.remove(handleIndex);
            dataList.add(index, new PLVBaseViewData(handleContent, PLVBaseViewData.ITEMTYPE_UNDEFINED));

            dataList.remove(handleIndex > index ? index + 1 : index - 1);
            dataList.add(handleIndex, new PLVBaseViewData(contentBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));

            notifyItemChanged(handleIndex);
            notifyItemChanged(index);

            updateLastExistedRank(index, handleContent);
            updateLastExistedRank(handleIndex, contentBean);
        } else {//只有一个操作的商品在列表中
            if (handleIndex != -1) {
                if (contentBean.isPullOffShelvesStatus()) {//未上架的不添加到列表中
                    ((PLVProductContentBean) dataList.get(handleIndex).getData()).setRank(handleContent.getRank());//更新rank
                    return;
                }
                dataList.remove(handleIndex);
                dataList.add(handleIndex, new PLVBaseViewData(contentBean, PLVBaseViewData.ITEMTYPE_UNDEFINED));
                notifyItemChanged(handleIndex);

                updateLastExistedRank(handleIndex, contentBean);
            } else {
                if (handleContent.isPullOffShelvesStatus()) {//未上架的不添加到列表中
                    ((PLVProductContentBean) dataList.get(index).getData()).setRank(contentBean.getRank());//更新rank
                    return;
                }
                dataList.remove(index);
                dataList.add(index, new PLVBaseViewData(handleContent, PLVBaseViewData.ITEMTYPE_UNDEFINED));
                notifyItemChanged(index);

                updateLastExistedRank(index, handleContent);
            }
        }
    }

    //是否存在相同商品id在列表中
    public int isExistProduct(int productId) {
        int index = -1;
        for (PLVBaseViewData baseViewData : dataList) {
            index++;
            PLVProductContentBean contentBean = (PLVProductContentBean) baseViewData.getData();
            if (contentBean.getProductId() == productId) {
                return index;
            }
        }
        return -1;
    }

    //获取列表中最后存在过的商品rank，即时被删除
    public int getLastExistedRank() {
        return lastExistedRank;
    }

    private void updateLastExistedRank(int index, PLVProductContentBean contentBean) {
        if (index == dataList.size() - 1) {
            lastExistedRank = lastExistedRank == -1 ? contentBean.getRank() : Math.min(contentBean.getRank(), lastExistedRank);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onBuyCommodityClick(View view, PLVProductContentBean contentsBean);

        void onLoadMoreData(int rank);
    }

    public void callOnBuyCommodityClick(View view, PLVProductContentBean contentsBean) {
        if (onViewActionListener != null) {
            onViewActionListener.onBuyCommodityClick(view, contentsBean);
        }
    }
    // </editor-fold>

    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PLVECCommodityViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_live_commodity_list_item, parent, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if (holder instanceof PLVECCommodityViewHolder) {
                ((PLVECCommodityViewHolder) holder).updateNumberView(((PLVProductContentBean) dataList.get(position).getData()).getShowId());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECCommodityAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
