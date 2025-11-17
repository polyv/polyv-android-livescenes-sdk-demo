package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.window.PLVInputWindow;
import com.easefun.polyv.liveecommerce.R;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.impl.PLVSocketManager;

public class PLVECChatInputWindow extends PLVInputWindow {

    private ConstraintLayout chatInputQuoteMsgLayout;
    private TextView chatQuoteNameContentTv;
    private ImageView chatQuoteCloseIv;
    private ImageView quizToggleIv;
    private View sendView;

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
        quizToggleIv = findViewById(R.id.quiz_toggle_iv);

        chatQuoteCloseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MessageSendListener) inputListener).onCloseQuote();
                updateChatQuoteContent();
            }
        });

        quizToggleIv.setVisibility(((MessageSendListener) inputListener).hasQuiz() ? View.VISIBLE : View.GONE);
        quizToggleIv.setSelected(((MessageSendListener) inputListener).isSelectedQuiz());
        quizToggleIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizToggleIv.setSelected(!quizToggleIv.isSelected());
                ((MessageSendListener) inputListener).onQuizToggle(quizToggleIv.isSelected());
            }
        });
        Pair<String, Boolean> hintPair = ((MessageSendListener) inputListener).getHintPair();
        updateHintPair(hintPair.first, hintPair.second);

        updateChatQuoteContent();
    }

    // <editor-folder defaultstate="collapsed" desc="对外API">
    public void updateHintPair(String text, boolean enabled) {
        if (inputView != null) {
            inputView.setHint(text);
            inputView.setEnabled(enabled);
            if (!enabled) {
                lastInputText = new SpannableStringBuilder(inputView.getText());//Spannable避免内存泄漏
                inputView.setText("");
            } else {
                if (lastInputText != null) {
                    inputView.setText(lastInputText);
                    inputView.setSelection(inputView.getText().length());
                    lastInputText = null;
                }
            }
        }
    }
    // </editor-folder>

    // <editor-fold defaultstate="collapsed" desc="父类方法重写">

    @Override
    public void finish() {
        if (inputListener instanceof MessageSendListener) {
            ((MessageSendListener) inputListener).onInputContext(null);
        }
        super.finish();
    }

    @Override
    public View sendView() {
        if (sendView == null) {
            sendView = findViewById(R.id.chat_send_tv);
        }
        return sendView;
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
            quoteMsg = "[图片]";// no need i18n
        }
        String nick = maskViewerName(chatQuoteVO);
        chatQuoteNameContentTv.setText(new SpannableStringBuilder(nick).append("：").append(quoteMsg));
    }

    private String maskViewerName(PLVChatQuoteVO chatQuoteVO) {
        PLVViewerNameMaskMapper mapper = PLVChannelFeatureManager.onChannel(PLVSocketManager.getInstance().getLoginRoomId())
                .getOrDefault(PLVChannelFeature.LIVE_VIEWER_NAME_MASK_TYPE, PLVViewerNameMaskMapper.KEEP_SOURCE);
        return mapper.invoke(
                chatQuoteVO.getNick(),
                chatQuoteVO.getUserType(),
                PLVSocketManager.getInstance().getLoginVO().getUserId().equals(chatQuoteVO.getUserId())
        );
    }

    // <editor-fold defaultstate="collapsed" desc="内部类 - 信息发送监听器">
    public interface MessageSendListener extends SoftKeyboardListener {
        void onInputContext(PLVECChatInputWindow inputWindow);

        boolean hasQuiz();

        boolean isSelectedQuiz();

        void onQuizToggle(boolean isSelectedQuiz);

        Pair<String, Boolean> getHintPair();

        @Nullable
        PLVChatQuoteVO getChatQuoteContent();

        void onCloseQuote();
    }
    // </editor-fold>
}
