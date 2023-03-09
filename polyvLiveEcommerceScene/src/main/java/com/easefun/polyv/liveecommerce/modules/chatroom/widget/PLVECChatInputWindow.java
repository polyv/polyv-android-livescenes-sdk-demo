package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.chat.PLVChatQuoteVO;

public class PLVECChatInputWindow extends PLVInputWindow {

    private ConstraintLayout chatInputQuoteMsgLayout;
    private TextView chatQuoteNameContentTv;
    private ImageView chatQuoteCloseIv;
    private EditText chatInputEt;
    private View chatInputBg;

    private PLVChatQuoteVO chatQuoteVO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onInputContext(this);
        }
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        chatInputQuoteMsgLayout = findViewById(R.id.plvec_chat_input_quote_msg_layout);
        chatQuoteNameContentTv = findViewById(R.id.plvec_chat_quote_name_content_tv);
        chatQuoteCloseIv = findViewById(R.id.plvec_chat_quote_close_iv);
        chatInputEt = findViewById(R.id.chat_input_et);
        chatInputBg = findViewById(R.id.chat_input_bg);

        chatQuoteCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MessageSendListener) inputListener).onCloseQuote();
                updateChatQuoteContent();
            }
        });

        updateChatQuoteContent();
    }

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public void finish() {
        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onInputContext(null);
        }
        super.finish();
    }

    @Override
    public boolean firstShowInput() {
        return true;
    }

    @Override
    public int layoutId() {
        return R.layout.plvec_chat_input_layout;
    }

    @Override
    public int bgViewId() {
        return R.id.chat_input_bg;
    }

    @Override
    public int inputViewId() {
        return R.id.chat_input_et;
    }

    // </editor-fold>

    private void updateChatQuoteContent() {
        final PLVChatQuoteVO chatQuoteVO = ((MessageSendListener) inputListener).getChatQuoteContent();
        chatInputQuoteMsgLayout.setVisibility(chatQuoteVO == null ? View.GONE : View.VISIBLE);
        if (chatQuoteVO == null) {
            return;
        }

        CharSequence quoteMsg = chatQuoteVO.getObjects() == null || chatQuoteVO.getObjects().length == 0 ? "" : (CharSequence) chatQuoteVO.getObjects()[0];
        final boolean isImageContent = chatQuoteVO.getContent() == null && chatQuoteVO.getImage() != null && chatQuoteVO.getImage().getUrl() != null;
        if (isImageContent) {
            quoteMsg = "[图片]";
        }
        chatQuoteNameContentTv.setText(new SpannableStringBuilder(chatQuoteVO.getNick()).append("：").append(quoteMsg));
    }

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends InputListener {
        void onInputContext(PLVECChatInputWindow inputWindow);

        @Nullable
        PLVChatQuoteVO getChatQuoteContent();

        void onCloseQuote();
    }
    // </editor-fold>
}
