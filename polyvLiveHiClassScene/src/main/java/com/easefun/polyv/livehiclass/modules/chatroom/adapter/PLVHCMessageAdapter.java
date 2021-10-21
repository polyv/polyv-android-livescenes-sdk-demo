package com.easefun.polyv.livehiclass.modules.chatroom.adapter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.holder.PLVHCMessageViewHolder;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.holder.PLVHCTipsMessageViewHolder;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室通用聊天信息adapter
 */
public class PLVHCMessageAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter>> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = "PLVHCMessageAdapter";
    private static final int VIEW_TYPE_EMPTY = 111;
    private List<PLVBaseViewData> dataList;//adapter使用的数据列表
    private List<PLVBaseViewData> fullDataList;//全部信息的数据列表
    private List<PLVBaseViewData> specialDataList;//只看讲师信息的数据列表(包括我、讲师类型、嘉宾类型、助教类型、管理员类型的信息)

    private boolean isDisplaySpecialType;//是否只看讲师

    private int msgIndex;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCMessageAdapter() {
        fullDataList = new ArrayList<>();
        specialDataList = new ArrayList<>();
        dataList = isDisplaySpecialType ? specialDataList : fullDataList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现PLVBaseAdapter定义的方法">
    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter> viewHolder;
        switch (viewType) {
            case VIEW_TYPE_EMPTY:
                viewHolder = new PLVChatMessageBaseViewHolder<>(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_chatroom_chat_msg_list_empty_layout, parent, false), this);
                break;
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG:
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK:
                viewHolder = new PLVHCMessageViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_chatroom_receive_message_item, parent, false), this);
                break;
            case PLVChatMessageItemType.ITEMTYPE_SEND_IMG:
            case PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK:
                viewHolder = new PLVHCMessageViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_chatroom_send_message_item, parent, false), this);
                break;
            case PLVChatMessageItemType.ITEMTYPE_TIPS_MSG:
                viewHolder = new PLVHCTipsMessageViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvhc_chatroom_tips_message_item, parent, false), this);
                break;
            default:
                PLVCommonLog.exception(new RuntimeException("itemType error"));
                viewHolder = new PLVChatMessageBaseViewHolder<>(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_horizontal_linear_layout, parent, false), this);
                break;
        }
        viewHolder.setMsgIndex(msgIndex);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter> holder, int position) {
        if (dataList.isEmpty()) {
            return;
        }
        holder.processData(dataList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.isEmpty() ? VIEW_TYPE_EMPTY : dataList.get(position).getItemType();
    }

    @Override
    public int getItemCount() {
        return dataList.isEmpty() ? 1 : dataList.size();
    }

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 设置信息索引">
    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    public void changeDisplayType(boolean isDisplaySpecialType) {
        if (this.isDisplaySpecialType == isDisplaySpecialType) {
            return;
        }
        this.isDisplaySpecialType = isDisplaySpecialType;
        if (isDisplaySpecialType) {
            dataList = specialDataList;
            notifyDataSetChanged();
        } else {
            dataList = fullDataList;
            notifyDataSetChanged();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 列表数据更新">
    public boolean addDataChangedAtLast(PLVBaseViewData baseViewData) {
        int oldSize = dataList.size();
        fullDataList.add(baseViewData);
        if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
            specialDataList.add(baseViewData);
        }
        if (dataList.size() != oldSize) {
            if (oldSize == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemInserted(dataList.size() - 1);
            }
            return true;
        }
        return false;
    }

    public boolean addDataListChangedAtLast(List<PLVBaseViewData> list) {
        int oldSize = dataList.size();
        fullDataList.addAll(list);
        for (PLVBaseViewData baseViewData : list) {
            if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
                specialDataList.add(baseViewData);
            }
        }
        if (dataList.size() != oldSize) {
            if (oldSize == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(oldSize, dataList.size() - oldSize);
            }
            return true;
        }
        return false;
    }

    public boolean addDataListChangedAtFirst(List<PLVBaseViewData<PLVBaseEvent>> list) {
        int oldSize = dataList.size();
        fullDataList.addAll(0, list);
        for (int i = list.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = list.get(i);
            if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
                specialDataList.add(0, baseViewData);
            }
        }
        if (dataList.size() != oldSize) {
            if (oldSize == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(0, dataList.size() - oldSize);
            }
            return true;
        }
        return false;
    }

    public boolean removeDataChanged(String id) {
        if (TextUtils.isEmpty(id)) {
            PLVCommonLog.e(TAG, "removeDataChanged fail, id is empty");
            return false;
        }
        int oldSize = dataList.size();
        int removeFullDataPosition = -1;
        for (PLVBaseViewData baseViewData : fullDataList) {
            removeFullDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())) {
                fullDataList.remove(baseViewData);
                break;
            }
        }
        int removeSpecialDataPosition = -1;
        for (PLVBaseViewData baseViewData : specialDataList) {
            removeFullDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())) {
                specialDataList.remove(baseViewData);
                break;
            }
        }
        if (dataList.size() != oldSize) {
            notifyItemRemoved(isDisplaySpecialType ? removeSpecialDataPosition : removeFullDataPosition);
            return true;
        }
        PLVCommonLog.e(TAG, "removeDataChanged fail, no find remove data");
        return false;
    }

    public boolean removeAllDataChanged() {
        int oldSize = dataList.size();
        fullDataList.clear();
        specialDataList.clear();
        if (dataList.size() != oldSize) {//if dataList=fullDataList or dataList=specialDataList
            notifyDataSetChanged();
            return true;
        }
        PLVCommonLog.e(TAG, "removeAllDataChanged fail, no find remove data");
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg);

        void onShowAnswerWindow(PLVChatQuoteVO chatQuoteVO, String quoteId);
    }

    public void callOnChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg) {
        if (onViewActionListener != null) {
            onViewActionListener.onChatImgClick(position, view, imgUrl, isQuoteImg);
        }
    }

    public void callOnShowAnswerWindow(PLVChatQuoteVO chatQuoteVO, String quoteId) {
        if (onViewActionListener != null) {
            onViewActionListener.onShowAnswerWindow(chatQuoteVO, quoteId);
        }
    }
    // </editor-fold>
}
