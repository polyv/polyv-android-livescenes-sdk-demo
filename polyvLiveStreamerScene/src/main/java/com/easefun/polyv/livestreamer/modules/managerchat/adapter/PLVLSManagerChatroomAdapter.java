package com.easefun.polyv.livestreamer.modules.managerchat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.vo.PLVChatEventWrapVO;
import com.easefun.polyv.livestreamer.modules.managerchat.adapter.viewholder.PLVLSAbsManagerChatroomViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro
 */
public class PLVLSManagerChatroomAdapter extends RecyclerView.Adapter<PLVLSAbsManagerChatroomViewHolder> {

    // <editor-fold defaultstate="collapsed" desc="变量">

    private final PLVLSAbsManagerChatroomViewHolder.Factory viewHolderFactory = new PLVLSAbsManagerChatroomViewHolder.Factory();

    private final List<PLVChatEventWrapVO> chatMessages = new ArrayList<>();

    private String userId;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adapter重写方法">

    @NonNull
    @Override
    public PLVLSAbsManagerChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewHolderFactory.create(viewGroup, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PLVLSAbsManagerChatroomViewHolder viewHolder, int i) {
        viewHolder.bindData(chatMessages.get(i));
    }

    @Override
    public int getItemCount() {
        return this.chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        position = Math.min(position, getItemCount() - 1);
        return getItemType(chatMessages.get(position));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setChatMessages(List<PLVChatEventWrapVO> chatMessages) {
        this.chatMessages.clear();
        if (chatMessages != null) {
            this.chatMessages.addAll(chatMessages);
        }
        notifyDataSetChanged();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    private int getItemType(PLVChatEventWrapVO vo) {
        if (isMessageSendByMe(vo)) {
            return PLVLSAbsManagerChatroomViewHolder.Factory.VIEW_TYPE_SEND_MSG;
        } else {
            return PLVLSAbsManagerChatroomViewHolder.Factory.VIEW_TYPE_RECEIVE_MSG;
        }
    }

    private boolean isMessageSendByMe(PLVChatEventWrapVO chatMessage) {
        return chatMessage.isSameUserWith(userId);
    }

    // </editor-fold>

}
