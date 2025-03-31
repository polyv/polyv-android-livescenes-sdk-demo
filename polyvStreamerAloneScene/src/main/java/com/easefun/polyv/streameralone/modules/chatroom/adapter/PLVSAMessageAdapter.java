package com.easefun.polyv.streameralone.modules.chatroom.adapter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.ui.widget.PLVEmptyViewGroup;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.holder.PLVSAMessageRewardViewHolder;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.holder.PLVSAMessageViewHolder;
import com.easefun.polyv.streameralone.modules.chatroom.layout.PLVSAChatOverLengthMessageLayout;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.IPLVMessageIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVProhibitedWordVO;
import com.plv.socket.event.chat.PLVRewardEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室通用聊天信息adapter
 */
public class PLVSAMessageAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter>> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String PAYLOAD_PROHIBITED_CHANGED = "prohibitedChanged";
    public static final int DISPLAY_DATA_TYPE_FULL = 1;
    public static final int DISPLAY_DATA_TYPE_SPECIAL = 2;
    public static final int DISPLAY_DATA_TYPE_REMOVE_REWARD = 3;//仅适配全部消息的数据列表
    private List<PLVBaseViewData> dataList;//adapter使用的数据列表
    private List<PLVBaseViewData> fullDataList;//全部信息的数据列表
    private List<PLVBaseViewData> specialDataList;//只看讲师信息的数据列表(包括我、讲师类型、嘉宾类型、助教类型、管理员类型的信息)
    private List<PLVBaseViewData> excludeRewardDataList;//移除了打赏信息的数据列表

    private int displayDataType = DISPLAY_DATA_TYPE_FULL;//显示数据类型

    private int msgIndex;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVSAMessageAdapter() {
        fullDataList = new ArrayList<>();
        specialDataList = new ArrayList<>();
        excludeRewardDataList = new ArrayList<>();
        if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
            dataList = fullDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
            dataList = specialDataList;
        } else {
            dataList = excludeRewardDataList;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现PLVBaseAdapter定义的方法">
    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> viewHolder;
        switch (viewType) {
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG:
            case PLVChatMessageItemType.ITEMTYPE_SEND_IMG:
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK:
            case PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK:
            case PLVChatMessageItemType.ITEMTYPE_EMOTION:
                viewHolder = new PLVSAMessageViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvsa_chatroom_message_item, parent, false), this);
                break;
            case PLVChatMessageItemType.ITEMTYPE_CUSTOM_GIFT:
                viewHolder = new PLVSAMessageRewardViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.plvsa_chatroom_message_custom_gift_item, parent, false), this);
                break;
            default:
                PLVCommonLog.exception(new RuntimeException("itemType error"));
                viewHolder = new PLVChatMessageBaseViewHolder<>(new PLVEmptyViewGroup(parent.getContext()), this);
                break;
        }
        viewHolder.setMsgIndex(msgIndex);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> holder, int position) {
        holder.processData(dataList.get(position), position);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        if (holder instanceof PLVSAMessageViewHolder) {
            ((PLVSAMessageViewHolder) holder).processData(dataList.get(position), position, payloads);
        }
    }

    @Override
    public void onViewRecycled(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> holder) {
        super.onViewRecycled(holder);
        if (holder instanceof PLVSAMessageViewHolder) {
            ((PLVSAMessageViewHolder) holder).recycle();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getItemType();
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    // </editor-fold>

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }

    // <editor-fold defaultstate="collapsed" desc="对外API - 设置信息索引">
    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }
    // </editor-fold>

    public void changeDisplayType(int displayDataType) {
        if (this.displayDataType == displayDataType) {
            return;
        }
        this.displayDataType = displayDataType;
        if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
            dataList = fullDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
            dataList = specialDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_REMOVE_REWARD) {
            dataList = excludeRewardDataList;
        }
        notifyDataSetChanged();
    }

    // <editor-fold defaultstate="collapsed" desc="对外API - 列表数据更新">
    public boolean addDataChangedAtLast(PLVBaseViewData baseViewData) {
        int oldSize = dataList.size();
        fullDataList.add(baseViewData);
        if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
            specialDataList.add(baseViewData);
        }
        if (!(baseViewData.getData() instanceof PLVCustomGiftEvent)
                && !(baseViewData.getData() instanceof PLVRewardEvent)) {
            excludeRewardDataList.add(baseViewData);
        }
        if (dataList.size() != oldSize) {
            notifyItemInserted(dataList.size() - 1);
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
        for (PLVBaseViewData baseViewData : list) {
            if (!(baseViewData.getData() instanceof PLVCustomGiftEvent)
                    && !(baseViewData.getData() instanceof PLVRewardEvent)) {
                excludeRewardDataList.add(baseViewData);
            }
        }
        if (dataList.size() != oldSize) {
            notifyItemRangeInserted(oldSize, dataList.size() - oldSize);
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
        for (int i = list.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = list.get(i);
            if (!(baseViewData.getData() instanceof PLVCustomGiftEvent)
                    && !(baseViewData.getData() instanceof PLVRewardEvent)) {
                excludeRewardDataList.add(0, baseViewData);
            }
        }
        if (dataList.size() != oldSize) {
            notifyItemRangeInserted(0, dataList.size() - oldSize);
            return true;
        }
        return false;
    }

    public void notifyProhibitedChanged(String prohibitedMessage, String hintMsg, String status) {
        int changedFullDataPosition = -1;
        for (int i = fullDataList.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = fullDataList.get(i);
            if (baseViewData.getData() instanceof PolyvLocalMessage) {
                String speakMsg = ((PolyvLocalMessage) baseViewData.getData()).getSpeakMessage();
                if (prohibitedMessage != null && speakMsg != null && speakMsg.contains(prohibitedMessage)) {
                    PLVProhibitedWordVO prohibitedWordVO = new PLVProhibitedWordVO(prohibitedMessage, hintMsg, status);
                    ((PolyvLocalMessage) baseViewData.getData()).setProhibitedWord(prohibitedWordVO);
                    changedFullDataPosition = i;
                    break;
                }
            }
        }
        int changedSpecialDataPosition = -1;
        for (int i = specialDataList.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = fullDataList.get(i);
            if (baseViewData.getData() instanceof PolyvLocalMessage) {
                String speakMsg = ((PolyvLocalMessage) baseViewData.getData()).getSpeakMessage();
                if (prohibitedMessage != null && speakMsg != null && speakMsg.contains(prohibitedMessage)) {
                    PLVProhibitedWordVO prohibitedWordVO = new PLVProhibitedWordVO(prohibitedMessage, hintMsg, status);
                    ((PolyvLocalMessage) baseViewData.getData()).setProhibitedWord(prohibitedWordVO);
                    changedSpecialDataPosition = i;
                    break;
                }
            }
        }
        int changedExcludeRewardDataPosition = -1;
        for (int i = excludeRewardDataList.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = excludeRewardDataList.get(i);
            if (baseViewData.getData() instanceof PolyvLocalMessage) {
                String speakMsg = ((PolyvLocalMessage) baseViewData.getData()).getSpeakMessage();
                if (prohibitedMessage != null && speakMsg != null && speakMsg.contains(prohibitedMessage)) {
                    PLVProhibitedWordVO prohibitedWordVO = new PLVProhibitedWordVO(prohibitedMessage, hintMsg, status);
                    ((PolyvLocalMessage) baseViewData.getData()).setProhibitedWord(prohibitedWordVO);
                    changedExcludeRewardDataPosition = i;
                    break;
                }
            }
        }
        int changedPosition = -1;
        if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
            changedPosition = changedFullDataPosition;
        } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
            changedPosition = changedSpecialDataPosition;
        } else if (displayDataType == DISPLAY_DATA_TYPE_REMOVE_REWARD) {
            changedPosition = changedExcludeRewardDataPosition;
        }
        if (changedPosition != -1) {
            notifyItemChanged(changedPosition);
        }
    }

    public boolean removeDataChanged(String id) {
        if (TextUtils.isEmpty(id)) {
            return false;
        }
        int oldSize = dataList.size();
        int removeFullDataPosition = -1;
        for (PLVBaseViewData baseViewData : fullDataList) {
            removeFullDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())
                    || (baseViewData.getData() instanceof IPLVMessageIdEvent
                    && id.equals(((IPLVMessageIdEvent) baseViewData.getData()).getMessageId()))) {
                fullDataList.remove(baseViewData);
                break;
            }
        }
        int removeSpecialDataPosition = -1;
        for (PLVBaseViewData baseViewData : specialDataList) {
            removeSpecialDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())
                    || (baseViewData.getData() instanceof IPLVMessageIdEvent
                    && id.equals(((IPLVMessageIdEvent) baseViewData.getData()).getMessageId()))) {
                specialDataList.remove(baseViewData);
                break;
            }
        }
        int removeExcludeRewardDataPosition = -1;
        for (PLVBaseViewData baseViewData : excludeRewardDataList) {
            removeExcludeRewardDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())
                    || (baseViewData.getData() instanceof IPLVMessageIdEvent
                    && id.equals(((IPLVMessageIdEvent) baseViewData.getData()).getMessageId()))) {
                excludeRewardDataList.remove(baseViewData);
                break;
            }
        }
        if (dataList.size() != oldSize) {
            int changedPosition = -1;
            if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
                changedPosition = removeFullDataPosition;
            } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
                changedPosition = removeSpecialDataPosition;
            } else if (displayDataType == DISPLAY_DATA_TYPE_REMOVE_REWARD) {
                changedPosition = removeExcludeRewardDataPosition;
            }
            if (changedPosition != -1) {
                notifyItemRemoved(changedPosition);
                return true;
            }
        }
        return false;
    }
    // </editor-fold>

    public boolean removeDataChanged(int maxLength) {
        int oldSize = dataList.size();
        if (fullDataList.size() > maxLength) {
//            fullDataList.removeAll(fullDataList.subList(0, fullDataList.size() - maxLength));
            List<PLVBaseViewData> retainList = new ArrayList<>(fullDataList.subList(fullDataList.size() - maxLength, fullDataList.size()));
            fullDataList.clear();
            fullDataList.addAll(retainList);
        }
        if (specialDataList.size() > maxLength) {
//            specialDataList.removeAll(specialDataList.subList(0, specialDataList.size() - maxLength));
            List<PLVBaseViewData> retainList = new ArrayList<>(specialDataList.subList(specialDataList.size() - maxLength, specialDataList.size()));
            specialDataList.clear();
            specialDataList.addAll(retainList);
        }
        if (excludeRewardDataList.size() > maxLength) {
//            excludeRewardDataList.removeAll(excludeRewardDataList.subList(0, excludeRewardDataList.size() - maxLength));
            List<PLVBaseViewData> retainList = new ArrayList<>(excludeRewardDataList.subList(excludeRewardDataList.size() - maxLength, excludeRewardDataList.size()));
            excludeRewardDataList.clear();
            excludeRewardDataList.addAll(retainList);
        }
        if (dataList.size() != oldSize) {
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public boolean removeAllDataChanged() {
        int oldSize = dataList.size();
        fullDataList.clear();
        specialDataList.clear();
        excludeRewardDataList.clear();
        if (dataList.size() != oldSize) {//if dataList=fullDataList or dataList=specialDataList
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
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

    public void callOnShowOverLengthMessage(PLVSAChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean) {
        if (onViewActionListener != null) {
            onViewActionListener.onShowOverLengthMessage(chatMessageDataBean);
        }
    }

    public boolean callIsStreamerStartSuccess() {
        if (onViewActionListener != null) {
            return onViewActionListener.isStreamerStartSuccess();
        }
        return false;
    }

    public boolean callIsPinMsgEnabled() {
        if (onViewActionListener != null) {
            return onViewActionListener.isPinMsgEnabled();
        }
        return false;
    }

    public interface OnViewActionListener {
        void onChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg);

        void onShowAnswerWindow(PLVChatQuoteVO chatQuoteVO, String quoteId);

        void onShowOverLengthMessage(PLVSAChatOverLengthMessageLayout.BaseChatMessageDataBean chatMessageDataBean);

        boolean isStreamerStartSuccess();

        boolean isPinMsgEnabled();
    }
    // </editor-fold>
}
