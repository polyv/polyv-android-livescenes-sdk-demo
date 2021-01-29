package com.easefun.polyv.liveecommerce.modules.chatroom;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.liveecommerce.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.IPLVIdEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天信息adapter
 */
public class PLVECChatMessageAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVECChatMessageAdapter>> {
    private static final String TAG = "PLVECChatMessageAdapter";
    private List<PLVBaseViewData> dataList;

    private int msgIndex;

    public PLVECChatMessageAdapter() {
        dataList = new ArrayList<>();
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    public void setDataList(List<PLVBaseViewData> dataList) {
        this.dataList = dataList;
    }

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onChatImgClick(View view, String imgUrl);
    }

    public void callOnChatImgClick(View view, String imgUrl) {
        if (onViewActionListener != null) {
            onViewActionListener.onChatImgClick(view, imgUrl);
        }
    }
    // </editor-fold>

    public void addDataListChanged(List<PLVBaseViewData> dataList) {
        int size = this.dataList.size();
        this.dataList.addAll(dataList);
        notifyItemRangeInserted(size, dataList.size());
    }

    public void addDataListChangedAtFirst(List<PLVBaseViewData<PLVBaseEvent>> dataList) {
        this.dataList.addAll(0, dataList);
        notifyItemRangeInserted(0, dataList.size());
    }

    public void removeDataChanged(String id) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        int removeFullDataPosition = -1;
        for (PLVBaseViewData baseViewData : dataList) {
            removeFullDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())) {
                dataList.remove(baseViewData);
                break;
            }
        }
        notifyItemRemoved(removeFullDataPosition);
    }

    public void removeAllDataChanged() {
        this.dataList.clear();
        notifyDataSetChanged();
    }

    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> viewHolder;
        switch (viewType) {
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK:
            case PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK:
                viewHolder = new PLVECChatMessageSpeakViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_chat_message_speak_item, parent, false),
                        this
                );
                break;
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG:
            case PLVChatMessageItemType.ITEMTYPE_SEND_IMG:
                viewHolder = new PLVECChatMessageImgViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_chat_message_img_item, parent, false),
                        this
                );
                break;
            case PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT:
                viewHolder = new PLVECChatMessageCustomGiftViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvec_chat_message_custom_gift_item, parent, false),
                        this
                );
                break;
            default:
                PLVCommonLog.exception(new RuntimeException("itemType error"));
                viewHolder = new PLVECChatMessageCommonViewHolder<>(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_horizontal_linear_layout, parent, false),
                        this);
                break;
        }
        viewHolder.setMsgIndex(msgIndex);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getItemType();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
