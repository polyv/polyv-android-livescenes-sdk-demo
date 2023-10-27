package com.easefun.polyv.liveecommerce.modules.chatroom;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.setOnLongClickListenerRecursively;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;

import android.graphics.Color;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVSpecialTypeTag;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.liveecommerce.R;
import com.easefun.polyv.liveecommerce.modules.chatroom.layout.PLVECChatOverLengthMessageLayout;
import com.easefun.polyv.liveecommerce.modules.chatroom.widget.PLVECChatMessageQuoteLayout;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.ppt.PLVPptShareFileVO;

import java.lang.ref.WeakReference;

/**
 * 发言信息viewHolder
 */
public class PLVECChatMessageSpeakViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {

    private TextView chatMsgTv;
    private ImageView chatMsgFileShareIv;
    private View chatMsgOverLengthSplitLine;
    private LinearLayout chatMsgOverLengthControlLl;
    private TextView chatMsgOverLengthCopyBtn;
    private TextView chatMsgOverLengthMoreBtn;
    private View chatMsgQuoteSplitLine;
    private PLVECChatMessageQuoteLayout chatMsgQuoteLayout;

    private boolean isOverLengthContentFolding = true;

    public PLVECChatMessageSpeakViewHolder(View itemView, PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatMsgTv = findViewById(R.id.chat_msg_tv);
        chatMsgFileShareIv = findViewById(R.id.plvec_chat_msg_file_share_iv);
        chatMsgOverLengthSplitLine = findViewById(R.id.plvec_chat_msg_over_length_split_line);
        chatMsgOverLengthControlLl = findViewById(R.id.plvec_chat_msg_over_length_control_ll);
        chatMsgOverLengthCopyBtn = findViewById(R.id.plvec_chat_msg_over_length_copy_btn);
        chatMsgOverLengthMoreBtn = findViewById(R.id.plvec_chat_msg_over_length_more_btn);
        chatMsgQuoteSplitLine = findViewById(R.id.plvec_chat_msg_quote_split_line);
        chatMsgQuoteLayout = findViewById(R.id.plvec_chat_msg_quote_layout);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        chatMsgTv.setTextColor(Color.WHITE);
        if (speakFileData == null) {
            chatMsgTv.setText(nickSpan.append(speakMsg));
            // 提问提示语
            if (data.getTag() instanceof PLVSpecialTypeTag) {
                if (((PLVSpecialTypeTag) data.getTag()).getUserId() == null) {
                    chatMsgTv.setText(speakMsg);
                    chatMsgTv.setTextColor(Color.parseColor("#FFD16B"));
                }
            }
        } else {
            chatMsgTv.setText(nickSpan.append(speakFileData.getName()));
        }
        bindFileShareIcon();
        bindChatMessageOnClick();
        bindQuoteMessage();
        processOverLengthMessage();
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
            setOnLongClickListenerRecursively((ViewGroup) itemView, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final boolean showCopyButton = !isOverLengthFoldingMessage;
                    final boolean showReplyButton = adapter.isAllowReplyMessage();
                    hideCopyBoardPopupWindow();
                    final PopupWindow popupWindow = PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, !showReplyButton, showCopyButton ? speakMsg.toString() : null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                            if (messageData instanceof IPLVIdEvent) {
                                chatQuoteVO.setMessageId(((IPLVIdEvent) messageData).getId());
                            }
                            chatQuoteVO.setUserId(userId);
                            chatQuoteVO.setNick(nickName);
                            chatQuoteVO.setContent(speakMsg.toString());
                            chatQuoteVO.setObjects(speakMsg);
                            adapter.callOnReplyMessage(chatQuoteVO);
                        }
                    });
                    copyBoardPopupWindowRef = new WeakReference<>(popupWindow);
                    return true;
                }
            });
        } else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (speakFileData != null) {
                        PLVWebUtils.openWebLink(speakFileData.getUrl(), itemView.getContext());
                    }
                }
            });
            setOnLongClickListenerRecursively((ViewGroup) itemView, null);
        }
    }

    private void bindQuoteMessage() {
        chatMsgQuoteSplitLine.setVisibility(chatQuoteVO == null ? View.GONE : View.VISIBLE);
        chatMsgQuoteLayout.setQuoteMessage(chatQuoteVO);
        chatMsgQuoteLayout.setOnActionListener(new PLVECChatMessageQuoteLayout.OnViewActionListener() {
            @Override
            public void onImageClick(PLVChatQuoteVO chatQuoteVO) {
                adapter.callOnChatImgClick(chatMsgQuoteLayout, chatQuoteVO.getImage().getUrl());
            }
        });
    }

    private void processOverLengthMessage() {
        isOverLengthContentFolding = true;

        chatMsgOverLengthCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isFullMessage || fullMessageOnOverLength == null) {
                    PLVCopyBoardPopupWindow.copy(v.getContext(), speakMsg.toString());
                } else {
                    fullMessageOnOverLength.getAsync(new PLVSugarUtil.Consumer<String>() {
                        @Override
                        public void accept(final String s) {
                            postToMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    PLVCopyBoardPopupWindow.copy(v.getContext(), s);
                                }
                            });
                        }
                    });
                }
            }
        });
        chatMsgOverLengthMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOverLengthShowAloneMessage) {
                    adapter.callOnShowOverLengthMessage(createShowAloneOverLengthMessage());
                } else {
                    isOverLengthContentFolding = !isOverLengthContentFolding;
                    updateOverLengthView();
                }
            }
        });

        updateOverLengthView();
    }

    private PLVECChatOverLengthMessageLayout.BaseChatMessageDataBean createShowAloneOverLengthMessage() {
        return new PLVECChatOverLengthMessageLayout.BaseChatMessageDataBean.Builder()
                .setAvatar(avatar)
                .setNick(nickName)
                .setUserType(userType)
                .setActor(actor)
                .setMessage(speakMsg)
                .setOverLength(!isFullMessage)
                .setOnOverLengthFullMessage(fullMessageOnOverLength)
                .build();
    }

    private void updateOverLengthView() {
        if (!isOverLengthFoldingMessage) {
            chatMsgOverLengthSplitLine.setVisibility(View.GONE);
            chatMsgOverLengthControlLl.setVisibility(View.GONE);
            return;
        }
        chatMsgOverLengthControlLl.setVisibility(View.VISIBLE);
        chatMsgOverLengthSplitLine.setVisibility(View.VISIBLE);
        chatMsgOverLengthMoreBtn.setText(isOverLengthContentFolding ? R.string.plv_chat_msg_over_length_more : R.string.plv_chat_msg_over_length_fold);
        chatMsgTv.setMaxLines(isOverLengthContentFolding ? 5 : Integer.MAX_VALUE);
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
