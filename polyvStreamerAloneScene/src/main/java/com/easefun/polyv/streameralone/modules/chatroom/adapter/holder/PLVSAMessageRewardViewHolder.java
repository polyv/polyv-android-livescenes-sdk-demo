package com.easefun.polyv.streameralone.modules.chatroom.adapter.holder;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
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
    private TextView chatNickTv;
    private ImageView chatImgIv;

    public PLVSAMessageRewardViewHolder(View itemView, PLVSAMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
        chatNickTv = findViewById(R.id.chat_nick_tv);
        chatImgIv = findViewById(R.id.chat_img_iv);
    }

    public void setMessageTextSizeSp(int sizeSp) {
        chatMsgTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        chatNickTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        CharSequence message = speakMsg;
        CharSequence nickname = "";
        if (data.getData() instanceof PLVCustomGiftEvent) {
            String nick = ((PLVCustomGiftEvent) data.getData()).getNickName();
            if (!TextUtils.isEmpty(nick) && message != null && message.toString().startsWith(nick)) {
                nickname = message.subSequence(0, nick.length());
                message = message.subSequence(nick.length(), message.length());
            }
        }
        // 使用独立的昵称 TextView，由布局测量剩余宽度后自动省略。
        chatNickTv.setText(nickname);
        chatMsgTv.setText(message);
        if (!TextUtils.isEmpty(giftImg)) {
            PLVImageLoader.getInstance().loadImage(giftImg, chatImgIv);
        } else {
            chatImgIv.setImageResource(giftDrawableId);
        }
    }
}
