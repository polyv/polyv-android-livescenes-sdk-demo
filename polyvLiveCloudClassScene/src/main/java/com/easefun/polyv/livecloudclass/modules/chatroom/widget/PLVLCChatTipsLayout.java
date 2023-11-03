package com.easefun.polyv.livecloudclass.modules.chatroom.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.util.PLVViewUtil;

/**
 * @author Hoshiiro
 */
public class PLVLCChatTipsLayout extends FrameLayout {

    private TextView chatTipsTv;
    private ImageView chatTipsCloseBtn;

    @Nullable
    private ShowTipsConfiguration tipsShowingConfiguration = null;

    public PLVLCChatTipsLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PLVLCChatTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PLVLCChatTipsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_chatroom_tips_layout, this);

        chatTipsTv = findViewById(R.id.plvlc_chat_tips_tv);
        chatTipsCloseBtn = findViewById(R.id.plvlc_chat_tips_close_btn);

        chatTipsCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public void show(@NonNull ShowTipsConfiguration showTipsConfiguration) {
        chatTipsTv.setText(showTipsConfiguration.content);
        chatTipsTv.setGravity(showTipsConfiguration.contentGravity);
        chatTipsCloseBtn.setVisibility(showTipsConfiguration.closable ? View.VISIBLE : View.GONE);
        if (showTipsConfiguration.autoHideMillis > 0) {
            PLVViewUtil.showViewForDuration(this, showTipsConfiguration.autoHideMillis);
        } else {
            setVisibility(View.VISIBLE);
        }
        tipsShowingConfiguration = showTipsConfiguration;
    }

    public void hide() {
        setVisibility(View.GONE);
        tipsShowingConfiguration = null;
    }

    @Nullable
    public ShowTipsConfiguration getTipsShowingConfiguration() {
        return tipsShowingConfiguration;
    }

    public static class ShowTipsConfiguration {

        private String content;
        private int contentGravity = Gravity.NO_GRAVITY;
        private boolean closable = true;
        // value>0 自动隐藏
        private long autoHideMillis = 0;

        public String getContent() {
            return content;
        }

        public ShowTipsConfiguration setContent(String content) {
            this.content = content;
            return this;
        }

        public int getContentGravity() {
            return contentGravity;
        }

        public ShowTipsConfiguration setContentGravity(int contentGravity) {
            this.contentGravity = contentGravity;
            return this;
        }

        public boolean isClosable() {
            return closable;
        }

        public ShowTipsConfiguration setClosable(boolean closable) {
            this.closable = closable;
            return this;
        }

        public long getAutoHideMillis() {
            return autoHideMillis;
        }

        public ShowTipsConfiguration setAutoHideMillis(long autoHideMillis) {
            this.autoHideMillis = autoHideMillis;
            return this;
        }
    }

}
