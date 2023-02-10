package com.easefun.polyv.livecloudclass.modules.chatroom.layout;

import static com.plv.foundationsdk.utils.PLVSugarUtil.format;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * @author Hoshiiro
 */
public class PLVLCChatReplyMessageLayout extends FrameLayout {

    private TextView chatReplyNameTv;
    private TextView chatReplyContentTv;
    private ImageView chatReplyCloseIv;

    @Nullable
    private PLVChatQuoteVO chatQuoteVO;

    public PLVLCChatReplyMessageLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLCChatReplyMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCChatReplyMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_chat_reply_layout, this);

        chatReplyNameTv = findViewById(R.id.plvlc_chat_reply_name_tv);
        chatReplyContentTv = findViewById(R.id.plvlc_chat_reply_content_tv);
        chatReplyCloseIv = findViewById(R.id.plvlc_chat_reply_close_iv);

        chatReplyCloseIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chatQuoteVO = null;
                updateContent();
            }
        });
    }

    public void setChatQuoteContent(PLVChatQuoteVO chatQuoteVO) {
        this.chatQuoteVO = chatQuoteVO;
        updateContent();
    }

    @Nullable
    public PLVChatQuoteVO getChatQuoteContent() {
        return chatQuoteVO;
    }

    private void updateContent() {
        setVisibility(chatQuoteVO == null ? GONE : VISIBLE);
        if (chatQuoteVO == null) {
            return;
        }

        if (chatQuoteVO.getContent() != null && chatQuoteVO.getObjects() == null) {
            chatQuoteVO.setObjects(PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(chatQuoteVO.getContent()), ConvertUtils.dp2px(12), Utils.getApp()));
        }

        final boolean isImageContent = chatQuoteVO.getContent() == null && chatQuoteVO.getImage() != null && chatQuoteVO.getImage().getUrl() != null;

        chatReplyNameTv.setText(format("{}：", chatQuoteVO.getNick()));
        if (isImageContent) {
            chatReplyContentTv.setText("[图片]");
        } else {
            chatReplyContentTv.setText((CharSequence) chatQuoteVO.getObjects()[0]);
        }
    }

}
