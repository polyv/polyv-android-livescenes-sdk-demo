package com.easefun.polyv.streameralone.modules.chatroom.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.streameralone.R;
import com.easefun.polyv.streameralone.modules.chatroom.adapter.PLVSAMessageAdapter;

/**
 * 打赏viewHolder
 */
public class PLVSAMessageRewardViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVSAMessageAdapter> {
    private TextView chatMsgTv;
    private ImageView chatImgIv;

    public PLVSAMessageRewardViewHolder(View itemView, PLVSAMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
        chatImgIv = findViewById(R.id.chat_img_iv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        chatMsgTv.setText(speakMsg);
        if (!TextUtils.isEmpty(giftImg)) {
            PLVImageLoader.getInstance().loadImage(giftImg, chatImgIv);
        } else {
            chatImgIv.setImageResource(giftDrawableId);
        }
    }
}
