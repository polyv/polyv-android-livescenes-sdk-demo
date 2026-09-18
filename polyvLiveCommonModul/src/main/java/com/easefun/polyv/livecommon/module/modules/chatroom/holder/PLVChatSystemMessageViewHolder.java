package com.easefun.polyv.livecommon.module.modules.chatroom.holder;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;

/** 云课堂和带货聊天列表共用的本地系统消息。 */
public class PLVChatSystemMessageViewHolder<Adapter extends PLVBaseAdapter>
        extends PLVBaseViewHolder<PLVBaseViewData, Adapter> {

    private final TextView messageView;

    public PLVChatSystemMessageViewHolder(ViewGroup parent, Adapter adapter, boolean darkBackground) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.plv_chat_system_message_item, parent, false), adapter);
        messageView = findViewById(R.id.plv_chat_system_message_content);
        TextView labelView = findViewById(R.id.plv_chat_system_message_label);
        View bubble = findViewById(R.id.plv_chat_system_message_bubble);
        setRoundedBackground(bubble, darkBackground ? 0x33000000 : 0xFFF5F5F5, 20);
        setRoundedBackground(labelView, darkBackground ? 0xFF454545 : 0xFFE0E0E0, 10);
        labelView.setTextColor(darkBackground ? Color.WHITE : 0xFF666666);
        messageView.setTextColor(darkBackground ? Color.WHITE : 0xFF333333);
        if (!darkBackground) {
            int padding = Math.round(16 * itemView.getResources().getDisplayMetrics().density);
            itemView.setPadding(padding, 0, padding, 0);
        }
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        messageView.setText((String) data.getData());
    }

    private void setRoundedBackground(View view, int color, int radiusDp) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(radiusDp * view.getResources().getDisplayMetrics().density);
        view.setBackground(background);
    }
}
