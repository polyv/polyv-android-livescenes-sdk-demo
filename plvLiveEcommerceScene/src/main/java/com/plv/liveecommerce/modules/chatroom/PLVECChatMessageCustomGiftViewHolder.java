package com.plv.liveecommerce.modules.chatroom;

import android.view.View;
import android.widget.TextView;

import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.liveecommerce.R;

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
