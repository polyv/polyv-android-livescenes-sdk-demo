package com.easefun.polyv.liveecommerce.modules.chatroom;

import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;

/**
 * 自定义礼物打赏viewHolder
 */
public class PLVECChatMessageCustomGiftViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {
    private TextView chatMsgTv;

    public PLVECChatMessageCustomGiftViewHolder(View itemView, PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        chatMsgTv.setText(speakMsg);
    }
}
