package com.easefun.polyv.liveecommerce.modules.chatroom.widget;

import static com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder.fitChatImgWH;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.chat.PLVChatQuoteVO;

/**
 * @author Hoshiiro
 */
public class PLVECChatMessageQuoteLayout extends FrameLayout {

    private TextView chatQuoteMsgNameContentTv;
    private ImageView chatQuoteMsgImageIv;

    private OnViewActionListener onViewActionListener;

    public PLVECChatMessageQuoteLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVECChatMessageQuoteLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVECChatMessageQuoteLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.plvec_chat_message_quote_layout, this);

        chatQuoteMsgNameContentTv = findViewById(R.id.plvec_chat_quote_msg_name_content_tv);
        chatQuoteMsgImageIv = findViewById(R.id.plvec_chat_quote_msg_image_iv);
    }

    public void setQuoteMessage(@Nullable final PLVChatQuoteVO chatQuoteVO) {
        if (chatQuoteVO == null) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);

        final CharSequence quoteMsg = chatQuoteVO.getObjects() == null || chatQuoteVO.getObjects().length == 0 ? "" : (CharSequence) chatQuoteVO.getObjects()[0];
        chatQuoteMsgNameContentTv.setText(new SpannableStringBuilder(chatQuoteVO.getNick()).append("：").append(quoteMsg));

        final boolean hasImage = chatQuoteVO.getImage() != null && chatQuoteVO.getImage().getUrl() != null;
        chatQuoteMsgImageIv.setVisibility(hasImage ? VISIBLE : GONE);
        if (hasImage) {
            fitChatImgWH((int) chatQuoteVO.getImage().getWidth(), (int) chatQuoteVO.getImage().getHeight(), chatQuoteMsgImageIv, 60, 40);//适配图片视图的宽高
            PLVImageLoader.getInstance().loadImage(chatQuoteVO.getImage().getUrl(), chatQuoteMsgImageIv);
            chatQuoteMsgImageIv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onViewActionListener != null) {
                        onViewActionListener.onImageClick(chatQuoteVO);
                    }
                }
            });
        }
    }

    public void setOnActionListener(OnViewActionListener onViewActionListener) {
        this.onViewActionListener = onViewActionListener;
    }

    public interface OnViewActionListener {
        void onImageClick(PLVChatQuoteVO chatQuoteVO);
    }

}
