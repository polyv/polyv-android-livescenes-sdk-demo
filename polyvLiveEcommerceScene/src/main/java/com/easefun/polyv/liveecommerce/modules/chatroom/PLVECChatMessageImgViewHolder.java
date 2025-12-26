package com.easefun.polyv.liveecommerce.modules.chatroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.setOnLongClickListenerRecursively;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.IPLVMessageIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;

import java.lang.ref.WeakReference;

/**
 * 图片信息viewHolder
 */
public class PLVECChatMessageImgViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {
    private ImageView chatImgIv;
    private TextView chatMsgTv;

    public PLVECChatMessageImgViewHolder(final View itemView, final PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatImgIv = findViewById(R.id.chat_img_iv);
        chatMsgTv = findViewById(R.id.chat_msg_tv);

        chatImgIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callOnChatImgClick(v, chatImgUrl);
            }
        });
        setOnLongClickListenerRecursively((ViewGroup) itemView, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (localImgStatus != PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS) {
                    //图片发送成功后才可回复
                    return true;
                }
                final boolean showReplyButton = adapter.isAllowReplyMessage();
                hideCopyBoardPopupWindow();
                final PopupWindow popupWindow = PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, !showReplyButton, null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                        if (messageData instanceof IPLVMessageIdEvent) {
                            chatQuoteVO.setMessageId(((IPLVMessageIdEvent) messageData).getMessageId());
                        } else if (messageData instanceof IPLVIdEvent) {
                            chatQuoteVO.setMessageId(((IPLVIdEvent) messageData).getId());
                        }
                        chatQuoteVO.setUserId(userId);
                        chatQuoteVO.setUserType(userType);
                        chatQuoteVO.setNick(nickName);
                        PLVChatQuoteVO.ImageBean imageBean = new PLVChatQuoteVO.ImageBean();
                        imageBean.setUrl(chatImgUrl);
                        imageBean.setWidth(chatImgWidth);
                        imageBean.setHeight(chatImgHeight);
                        chatQuoteVO.setImage(imageBean);
                        adapter.callOnReplyMessage(chatQuoteVO);
                    }
                });
                copyBoardPopupWindowRef = new WeakReference<>(popupWindow);
                return true;
            }
        });
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
