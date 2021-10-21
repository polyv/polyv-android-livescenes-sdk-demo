package com.easefun.polyv.livehiclass.modules.chatroom.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCMessageAdapter;
import com.plv.socket.event.chat.PLVCloseRoomEvent;

/**
 * 聊天提示信息viewHolder
 */
public class PLVHCTipsMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //view
    private TextView plvhcChatroomTipsMessageTv;
    //model
    private CharSequence tipsMessage;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCTipsMessageViewHolder(View itemView, PLVHCMessageAdapter adapter) {
        super(itemView, adapter);
        plvhcChatroomTipsMessageTv = findViewById(R.id.plvhc_chatroom_tips_message_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVBaseViewHolder定义的方法">
    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if (messageData instanceof PLVCloseRoomEvent) {
            boolean isClose = ((PLVCloseRoomEvent) messageData).getValue().isClosed();
            tipsMessage = itemView.getContext().getString(isClose ? R.string.plv_chat_toast_chatroom_close : R.string.plv_chat_toast_chatroom_open);
        }

        plvhcChatroomTipsMessageTv.setText(tipsMessage);
    }
    // </editor-fold>
}
