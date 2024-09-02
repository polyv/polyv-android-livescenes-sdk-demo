package com.easefun.polyv.livecommon.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.modules.chatroom.contract.IPLVChatroomContract;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.modules.chatroom.view.PLVAbsChatroomView;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurUtils;
import com.easefun.polyv.livecommon.ui.widget.blurview.PLVBlurView;
import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackCallDataListener;
import com.plv.livescenes.playback.chat.IPLVChatPlaybackManager;
import com.plv.livescenes.playback.chat.PLVChatPlaybackCallDataExListener;
import com.plv.livescenes.playback.chat.PLVChatPlaybackData;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.chat.PLVCancelTopEvent;
import com.plv.socket.event.chat.PLVToTopEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * 评论上墙View
 */
public class PLVToTopView extends FrameLayout {
    //是否聊天回放布局
    private boolean isChatPlaybackLayout;
    private boolean isLiveType;
    private GifSpanTextView messageTv;
    private TextView closeTv;
    private TextView cancelTv;
    private boolean showEnabled = true;
    private boolean isShowStatus = false;

    public PLVToTopView(@NonNull Context context) {
        this(context, null);
    }

    public PLVToTopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVToTopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_to_top_layout, this, true);
        PLVBlurUtils.initBlurView((PLVBlurView) findViewById(R.id.blur_ly));

        messageTv = (GifSpanTextView) findViewById(R.id.message_tv);
        closeTv = (TextView) findViewById(R.id.close_tv);
        cancelTv = (TextView) findViewById(R.id.cancel_tv);
        messageTv.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
            @Override
            public void webLinkOnClick(String url) {
                PLVWebUtils.openWebLink(url, messageTv.getContext());
            }
        });
        closeTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        cancelTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVChatroomManager.getInstance().cancelTopMessage();
                if (PLVSocketWrapper.getInstance().isOnlineStatus()) {
                    close();
                } else {
                    PLVToast.Builder.context(getContext())
                            .setText(R.string.plv_common_toast_network_error)
                            .show();
                }
            }
        });
    }

    public void setShowEnabled(boolean showEnabled) {
        this.showEnabled = showEnabled;
        if (showEnabled && isShowStatus) {
            show();
        } else if (!showEnabled) {
            close();
        }
    }

    public void setCancelTopStyle() {
        closeTv.setVisibility(View.GONE);
        cancelTv.setVisibility(View.VISIBLE);
    }

    public void setMessage(final String message) {
        messageTv.setText(message);
    }

    public void showByToTopEvent(final PLVToTopEvent toTopEvent) {
        post(new Runnable() {
            @Override
            public void run() {
                SpannableStringBuilder span = new SpannableStringBuilder(toTopEvent.getNick() + ": ");
                span.setSpan(new ForegroundColorSpan(PLVFormatUtils.parseColor("#FFD16B")), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                CharSequence content = PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(toTopEvent.getContent()), ConvertUtils.dp2px(14), Utils.getApp());
                messageTv.setTextInner(span.append(content), true);
                show();
            }
        });
    }

    public void show() {
        if (showEnabled) {
            setVisibility(View.VISIBLE);
        }
        isShowStatus = true;
    }

    public void close() {
        isShowStatus = false;
        post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        });
    }

    public void setIsChatPlaybackLayout(boolean isChatPlaybackLayout) {
        this.isChatPlaybackLayout = isChatPlaybackLayout;
    }

    public void setIsLiveType(boolean isLiveType) {
        this.isLiveType = isLiveType;
    }

    public IPLVChatroomContract.IChatroomView getChatroomView() {
        return chatroomView;
    }

    public IPLVChatPlaybackCallDataListener getChatPlaybackDataListener() {
        return chatPlaybackDataListener;
    }

    private IPLVChatroomContract.IChatroomView chatroomView = new PLVAbsChatroomView() {

        @Override
        public void onToTopEvent(PLVToTopEvent toTopEvent) {
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            showByToTopEvent(toTopEvent);
        }

        @Override
        public void onCancelTopEvent(@NonNull PLVCancelTopEvent cancelTopEvent) {
            if (isChatPlaybackLayout || !isLiveType) {
                return;
            }
            close();
        }
    };

    private IPLVChatPlaybackCallDataListener chatPlaybackDataListener = new PLVChatPlaybackCallDataExListener() {

        @Override
        public void onToTopEvent(PLVToTopEvent event) {
            showByToTopEvent(event);
        }

        @Override
        public void onCancelTopEvent(@Nullable PLVCancelTopEvent event) {
            close();
        }

        @Override
        public void onLoadPreviousEnabled(boolean enabled, boolean isByClearData) {

        }

        @Override
        public void onHasNotAddedData() {

        }

        @Override
        public void onLoadPreviousFinish() {

        }

        @Override
        public void onDataInserted(int startPosition, int count, List<PLVChatPlaybackData> insertDataList, boolean inHead, int time) {

        }

        @Override
        public void onDataRemoved(int startPosition, int count, List<PLVChatPlaybackData> removeDataList, boolean inHead) {

        }

        @Override
        public void onDataCleared() {

        }

        @Override
        public void onData(List<PLVChatPlaybackData> dataList) {

        }

        @Override
        public void onManager(IPLVChatPlaybackManager chatPlaybackManager) {

        }
    };
}
