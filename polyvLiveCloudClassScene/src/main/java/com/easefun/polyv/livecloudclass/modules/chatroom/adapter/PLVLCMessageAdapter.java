package com.easefun.polyv.livecloudclass.modules.chatroom.adapter;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder.PLVLCMessageViewHolder;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder.PLVLCRewardViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageItemType;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.chat.IPLVIdEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室通用聊天信息adapter
 */
public class PLVLCMessageAdapter extends PLVBaseAdapter<PLVBaseViewData, PLVBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter>> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final int DISPLAY_DATA_TYPE_FULL = 1;
    public static final int DISPLAY_DATA_TYPE_SPECIAL = 2;
    public static final int DISPLAY_DATA_TYPE_FOCUS_MODE = 3;
    private List<PLVBaseViewData> dataList;//adapter使用的数据列表
    private List<PLVBaseViewData> fullDataList;//全部信息的数据列表
    private List<PLVBaseViewData> specialDataList;//只看讲师信息的数据列表(包括我、讲师类型、嘉宾类型、助教类型、管理员类型的信息)
    private List<PLVBaseViewData> focusModeDataList;//专注模式的数据列表(包括讲师类型、嘉宾类型、助教类型、管理员类型的信息)

    private int displayDataType = DISPLAY_DATA_TYPE_FULL;//显示数据类型

    private int msgIndex;

    private boolean isLandscapeLayout;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCMessageAdapter() {
        fullDataList = new ArrayList<>();
        specialDataList = new ArrayList<>();
        focusModeDataList = new ArrayList<>();
        if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
            dataList = fullDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
            dataList = specialDataList;
        } else {
            dataList = focusModeDataList;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 实现PLVBaseAdapter定义的方法">
    @NonNull
    @Override
    public PLVBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> viewHolder;
        switch (viewType) {
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_IMG:
            case PLVChatMessageItemType.ITEMTYPE_SEND_IMG:
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_SPEAK:
            case PLVChatMessageItemType.ITEMTYPE_SEND_SPEAK:
            case PLVChatMessageItemType.ITEMTYPE_RECEIVE_QUIZ:
            case PLVChatMessageItemType.ITEMTYPE_SEND_QUIZ:
            case PLVChatMessageItemType.ITEMTYPE_EMOTION:
                viewHolder = new PLVLCMessageViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(isLandscapeLayout ? R.layout.plvlc_chatroom_message_landscape_item : R.layout.plvlc_chatroom_message_portrait_item, parent, false), this);
                break;
            case PLVChatMessageItemType.ITEMTYPE_REWARD:
                viewHolder = new PLVLCRewardViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(isLandscapeLayout ? R.layout.plvlc_chatroom_reward_landscape_item : R.layout.plvlc_chatroom_reward_item,parent, false),
                        this
                );
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
    public void onBindViewHolder(@NonNull PLVBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> holder, int position) {
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

    @Override
    public List<PLVBaseViewData> getDataList() {
        return dataList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 设置信息索引、是否横屏布局">
    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    public void setLandscapeLayout(boolean landscapeLayout) {
        isLandscapeLayout = landscapeLayout;
    }

    public void changeDisplayType(int displayDataType) {
        if (this.displayDataType == displayDataType) {
            return;
        }
        this.displayDataType = displayDataType;
        if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
            dataList = fullDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
            dataList = specialDataList;
        } else if (displayDataType == DISPLAY_DATA_TYPE_FOCUS_MODE) {
            dataList = focusModeDataList;
        }
        notifyDataSetChanged();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 列表数据更新">
    public boolean addDataChangedAtLast(PLVBaseViewData baseViewData) {
        int oldSize = dataList.size();
        fullDataList.add(baseViewData);
        if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
            specialDataList.add(baseViewData);
            if (!((PLVSpecialTypeTag) baseViewData.getTag()).isMySelf()) {
                focusModeDataList.add(baseViewData);
            }
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
                if (!((PLVSpecialTypeTag) baseViewData.getTag()).isMySelf()) {
                    focusModeDataList.add(baseViewData);
                }
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
                if (!((PLVSpecialTypeTag) baseViewData.getTag()).isMySelf()) {
                    focusModeDataList.add(0, baseViewData);
                }
            }
        }
        if (dataList.size() != oldSize) {
            notifyItemRangeInserted(0, dataList.size() - oldSize);
            return true;
        }
        return false;
    }

    public boolean addDataListChangedAtHead(List<PLVBaseViewData> list) {
        int oldSize = dataList.size();
        fullDataList.addAll(0, list);
        for (int i = list.size() - 1; i >= 0; i--) {
            PLVBaseViewData baseViewData = list.get(i);
            if (baseViewData.getTag() instanceof PLVSpecialTypeTag) {
                specialDataList.add(0, baseViewData);
                if (!((PLVSpecialTypeTag) baseViewData.getTag()).isMySelf()) {
                    focusModeDataList.add(0, baseViewData);
                }
            }
        }
        if (dataList.size() != oldSize) {
            notifyItemRangeInserted(0, dataList.size() - oldSize);
            return true;
        }
        return false;
    }

    public boolean removeDataChanged(int startPosition, int count) {
        if (startPosition < 0 || count <= 0) {
            return false;
        }
        int oldSize = dataList.size();
        List<PLVBaseViewData> removeList = new ArrayList<>();
        int removeCount = count;
        while (removeCount > 0) {
            removeList.add(fullDataList.remove(startPosition));
            removeCount--;
        }
        specialDataList.removeAll(removeList);
        focusModeDataList.removeAll(removeList);
        if (dataList.size() != oldSize) {
            if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
                notifyItemRangeRemoved(startPosition, count);
            } else {
                notifyDataSetChanged();
            }
        }
        return true;
    }

    public boolean removeDataChanged(String id) {
        if (TextUtils.isEmpty(id)) {
            return false;
        }
        int oldSize = dataList.size();
        int removeFullDataPosition = remove(id, fullDataList);
        int removeSpecialDataPosition = remove(id, specialDataList);
        int removeFocusModeDataPosition = remove(id, focusModeDataList);
        if (dataList.size() != oldSize) {
            int removePosition = -1;
            if (displayDataType == DISPLAY_DATA_TYPE_FULL) {
                removePosition = removeFullDataPosition;
            } else if (displayDataType == DISPLAY_DATA_TYPE_SPECIAL) {
                removePosition = removeSpecialDataPosition;
            } else if (displayDataType == DISPLAY_DATA_TYPE_FOCUS_MODE) {
                removePosition = removeFocusModeDataPosition;
            }
            notifyItemRemoved(removePosition);
            return true;
        }
        return false;
    }

    public boolean removeAllDataChanged() {
        int oldSize = dataList.size();
        fullDataList.clear();
        specialDataList.clear();
        focusModeDataList.clear();
        if (dataList.size() != oldSize) {//if dataList=fullDataList or dataList=specialDataList
            notifyDataSetChanged();
            return true;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private int remove(String id, List<PLVBaseViewData> dataList) {
        int removeDataPosition = -1;
        for (PLVBaseViewData baseViewData : dataList) {
            removeDataPosition++;
            if (baseViewData.getData() instanceof IPLVIdEvent
                    && id.equals(((IPLVIdEvent) baseViewData.getData()).getId())) {
                dataList.remove(baseViewData);
                break;
            }
        }
        return removeDataPosition;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    private OnViewActionListener onViewActionListener;

    public void setOnViewActionListener(OnViewActionListener listener) {
        this.onViewActionListener = listener;
    }

    public interface OnViewActionListener {
        void onChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg);
    }

    public void callOnChatImgClick(int position, View view, String imgUrl, boolean isQuoteImg) {
        if (onViewActionListener != null) {
            onViewActionListener.onChatImgClick(position, view, imgUrl, isQuoteImg);
        }
    }
    // </editor-fold>
}
