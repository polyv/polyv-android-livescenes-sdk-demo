package com.easefun.polyv.liveecommerce.modules.chatroom;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.ppt.PLVPptShareFileVO;

/**
 * 发言信息viewHolder
 */
public class PLVECChatMessageSpeakViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {
    private TextView chatMsgTv;
    private ImageView chatMsgFileShareIv;

    public PLVECChatMessageSpeakViewHolder(View itemView, PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
        chatMsgFileShareIv = findViewById(R.id.plvec_chat_msg_file_share_iv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if (speakFileData == null) {
            chatMsgTv.setText(nickSpan.append(speakMsg));
        } else {
            chatMsgTv.setText(nickSpan.append(speakFileData.getName()));
        }
        bindFileShareIcon();
        bindChatMessageOnClick();
    }

    private void bindFileShareIcon() {
        final Integer speakFileIconRes = getSpeakFileIconRes(speakFileData);
        if (speakFileIconRes == null) {
            chatMsgFileShareIv.setVisibility(View.GONE);
        } else {
            chatMsgFileShareIv.setVisibility(View.VISIBLE);
            chatMsgFileShareIv.setImageResource(speakFileIconRes);
        }
    }

    private void bindChatMessageOnClick() {
        if (speakFileData == null) {
            itemView.setOnClickListener(null);
        } else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (speakFileData != null) {
                        PLVWebUtils.openWebLink(speakFileData.getUrl(), itemView.getContext());
                    }
                }
            });
        }
    }

    @Nullable
    @DrawableRes
    private static Integer getSpeakFileIconRes(@Nullable PLVPptShareFileVO fileData) {
        if (fileData == null) {
            return null;
        }
        switch (fileData.getSuffix()) {
            case "ppt":
            case "pptx":
                return R.drawable.plvec_chatroom_file_share_ppt_icon;
            case "doc":
            case "docx":
                return R.drawable.plvec_chatroom_file_share_doc_icon;
            case "xls":
            case "xlsx":
                return R.drawable.plvec_chatroom_file_share_xls_icon;
            case "pdf":
                return R.drawable.plvec_chatroom_file_share_pdf_icon;
            default:
                return null;
        }
    }
}
