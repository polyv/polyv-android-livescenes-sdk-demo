package com.easefun.polyv.liveecommerce.modules.chatroom;

import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.chat.PLVRewardEvent;

/**
 * 聊天室打赏消息ViewHolder
 */
public class PLVECChatMessageRewardViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {
    private TextView chatMsgTv;

    public PLVECChatMessageRewardViewHolder(View itemView, PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if (messageData instanceof PLVRewardEvent) {
            PLVRewardEvent rewardEvent = (PLVRewardEvent) messageData;
            if (chatMsgTv != null) {
                if (rewardEvent.getObjects() != null) {
                    chatMsgTv.setText((CharSequence) rewardEvent.getObjects()[0]);
                }
            }
        }
    }
}
