package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.socket.event.chat.PLVRewardEvent;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

/**
 * 聊天室打赏消息ViewHolder
 */
public class PLVLCRewardViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {

    private TextView tvMessagePortrait;
    private TextView tvMessageLandscape;


    public PLVLCRewardViewHolder(View itemView, PLVLCMessageAdapter adapter) {
        super(itemView, adapter);

        tvMessagePortrait = itemView.findViewById(R.id.plvlc_tv_reward_message_portrait);
        tvMessageLandscape = itemView.findViewById(R.id.plvlc_tv_reward_message_land);

    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if(messageData instanceof PLVRewardEvent){
            PLVRewardEvent rewardEvent = (PLVRewardEvent) messageData;
            if(tvMessagePortrait != null) {
                if (rewardEvent.getObjects() != null) {
                    tvMessagePortrait.setText((CharSequence) rewardEvent.getObjects()[0]);
                }
                return;
            }

            if(ScreenUtils.isLandscape()){
                itemView.setVisibility(View.GONE);
            }

            /* 全屏模式暂不显示打赏
            if(tvMessageLandscape != null){
                if(rewardEvent.getObjects()[0] != null ){
                    SpannableStringBuilder message = (SpannableStringBuilder) rewardEvent.getObjects()[0];
                    String msg = message.toString();
                    String nick = msg.substring(0, msg.lastIndexOf("赠送"));
                    if(nick.length() > 10){
                        String omitNick = nick.substring(0,8) + "... ";
                        SpannableStringBuilder result = new SpannableStringBuilder(omitNick);
                        result.append(message, nick.length(), message.length());
                        tvMessageLandscape.setText(result);
                    }
                }
            }

             */
        }
    }
}
