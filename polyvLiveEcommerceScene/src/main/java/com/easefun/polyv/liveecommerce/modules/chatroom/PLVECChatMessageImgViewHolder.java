package com.easefun.polyv.liveecommerce.modules.chatroom;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;

/**
 * 图片信息viewHolder
 */
public class PLVECChatMessageImgViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {
    private ImageView chatImgIv;
    private TextView chatMsgTv;

    public PLVECChatMessageImgViewHolder(View itemView, final PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatImgIv = findViewById(R.id.chat_img_iv);
        chatImgIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callOnChatImgClick(v, chatImgUrl);
            }
        });
        chatMsgTv = findViewById(R.id.chat_msg_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        fitChatImgWH(chatImgWidth, chatImgHeight, chatImgIv, 64, 36);
        PLVImageLoader.getInstance().loadImage(itemView.getContext(), chatImgUrl,
                R.drawable.plvec_img_site, R.drawable.plvec_img_site, chatImgIv);
        chatMsgTv.setText(nickSpan);
    }
}
