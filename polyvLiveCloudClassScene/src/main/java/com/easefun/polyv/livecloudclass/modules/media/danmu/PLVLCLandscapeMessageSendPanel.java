package com.easefun.polyv.livecloudclass.modules.media.danmu;

import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.PLVOrientationSensibleLinearLayout;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.impl.PLVSocketManager;
import com.plv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * date: 2019/6/6 0006
 *
 * @author hwj
 * description 横屏发送信息输入框
 */
public class PLVLCLandscapeMessageSendPanel implements IPLVLCLandscapeMessageSender, View.OnClickListener {
    private PopupWindow window;
    private AppCompatActivity activity;

    private View root;
    private ConstraintLayout chatSendMsgQuoteLayout;
    private TextView chatSendMsgQuoteTv;
    private ImageView chatSendMsgQuoteCloseIv;
    private PLVOrientationSensibleLinearLayout llSendMessage;
    private EditText etSendMessage;
    private TextView tvSendMessage;
    private View anchor;

    private OnSendMessageListener sendMessageListener;

    private int usableHeightPrevious;

    public PLVLCLandscapeMessageSendPanel(AppCompatActivity appCompatActivity, View anchor) {
        this.anchor = anchor;
        this.activity = appCompatActivity;
        this.window = new PopupWindow(activity);
        root = LayoutInflater.from(activity).inflate(R.layout.plvlc_player_message_send_layout, null);
        window.setContentView(root);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(null);

        int width = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int height = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        window.setWidth(width);
        window.setHeight(height);

        initView(root);

        FrameLayout content = activity.findViewById(android.R.id.content);
        final View childOfContent = content.getChildAt(0);
        final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
        childOfContent.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {//all call
                int usableHeightNow = computeUsableHeight(childOfContent);
                if (usableHeightPrevious != usableHeightNow) {
                    ViewGroup.LayoutParams flp = root.getLayoutParams();
                    if (flp == null) {
                        return;
                    }
                    int usableHeightSansKeyboard = childOfContent.getRootView().getHeight();
                    int heightDifference = Math.abs(usableHeightSansKeyboard - usableHeightNow);
                    if (heightDifference > (usableHeightNow / 4)) {
                        // keyboard probably just became visible
                        flp.height = usableHeightSansKeyboard - heightDifference;//can invalid
                    } else {
                        flp.height = usableHeightSansKeyboard;
                    }
                    childOfContent.requestLayout();
                    usableHeightPrevious = usableHeightNow;
                }
            }
        });
        childOfContent.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                childOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        });
    }

    private int computeUsableHeight(View view) {
        Rect r = new Rect();
        view.getWindowVisibleDisplayFrame(r);
        //隐藏状态栏情况下才需计算
        return r.bottom;//待完善，横屏为r.bottom，竖屏时为r.bottom-r.top+navbarHeight，存在刘海屏需-r.top，不存在刘海时需使用0(r.top有值)
    }

    @Override
    public void dismiss() {
        if (window != null) {
            window.dismiss();
        }
    }

    @Override
    public void setOnSendMessageListener(OnSendMessageListener listener) {
        this.sendMessageListener = listener;
    }

    @Override
    public void openMessageSender() {
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        updateChatQuote();
        llSendMessage.post(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showSoftInput(etSendMessage);
            }
        });
    }

    @Override
    public void hideMessageSender() {
        hide();
    }

    private void initView(View root) {
        chatSendMsgQuoteLayout = root.findViewById(R.id.plvlc_chat_send_msg_quote_layout);
        chatSendMsgQuoteTv = root.findViewById(R.id.plvlc_chat_send_msg_quote_tv);
        chatSendMsgQuoteCloseIv = root.findViewById(R.id.plvlc_chat_send_msg_quote_close_iv);
        llSendMessage = root.findViewById(R.id.ll_send_message);
        etSendMessage = root.findViewById(R.id.et_send_message);
        tvSendMessage = root.findViewById(R.id.tv_send_message);

        root.setOnClickListener(this);
        chatSendMsgQuoteCloseIv.setOnClickListener(this);
        tvSendMessage.setOnClickListener(this);

        etSendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {/**/}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {/**/}

            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = TextUtils.isEmpty(etSendMessage.getText());
                tvSendMessage.setEnabled(!enable);
            }
        });
        etSendMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        llSendMessage.setOnPortrait(new Runnable() {
            @Override
            public void run() {
                PLVLCLandscapeMessageSendPanel.this.hide();
            }
        });
    }

    private void updateChatQuote() {
        final PLVChatQuoteVO chatQuoteVO = sendMessageListener == null ? null : sendMessageListener.getChatQuoteContent();
        if (chatQuoteVO == null) {
            chatSendMsgQuoteLayout.setVisibility(View.GONE);
            return;
        }
        chatSendMsgQuoteLayout.setVisibility(View.VISIBLE);

        CharSequence quoteMsg = chatQuoteVO.getObjects() == null || chatQuoteVO.getObjects().length == 0 ? "" : (CharSequence) chatQuoteVO.getObjects()[0];
        final boolean isImageContent = chatQuoteVO.getContent() == null && chatQuoteVO.getImage() != null && chatQuoteVO.getImage().getUrl() != null;
        if (isImageContent) {
            quoteMsg = "[图片]";// no need i18n
        }
        String nick = maskViewerName(chatQuoteVO);
        chatSendMsgQuoteTv.setText(new SpannableStringBuilder(nick).append("：").append(quoteMsg));
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

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == root.getId()) {
            hide();
        } else if (i == R.id.tv_send_message) {
            sendMessage();
        } else if (i == chatSendMsgQuoteCloseIv.getId()) {
            if (sendMessageListener != null) {
                sendMessageListener.onCloseChatQuote();
                updateChatQuote();
            }
        }
    }

    private void sendMessage() {
        String msg = etSendMessage.getText().toString();
        if (msg.trim().length() == 0) {
            ToastUtils.showLong(R.string.plv_chat_toast_send_text_empty);
            return;
        }
        etSendMessage.setText("");
        //发送消息
        if (sendMessageListener != null) {
            sendMessageListener.onSend(msg, sendMessageListener.getChatQuoteContent());
        }
        KeyboardUtils.hideSoftInput(etSendMessage);
        hide();
    }

    private void hide() {
        KeyboardUtils.hideSoftInput(etSendMessage);
        window.dismiss();
    }

}
