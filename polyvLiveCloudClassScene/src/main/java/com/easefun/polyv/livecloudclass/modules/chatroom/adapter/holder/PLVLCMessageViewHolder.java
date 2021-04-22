package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.sub.gif.GifSpanTextView;
import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * 聊天室通用聊天信息的viewHolder
 */
public class PLVLCMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String LOADIMG_MOUDLE_TAG = "PLVLCMessageViewHolder";

    //是否是横屏布局
    private boolean isLandscapeLayout;

    //竖屏item
    //头像
    private ImageView avatarIv;
    //昵称
    private TextView nickTv;
    //文本信息
    private GifSpanTextView textMessageTv;
    //被回复人昵称
    private TextView quoteNickTv;
    //被回复人的文本信息
    private GifSpanTextView quoteTextMessageTv;

    //横屏item
    //文本信息
    private GifSpanTextView chatMsgTv;
    //图片信息的昵称
    private TextView chatNickTv;
    //被回复人的文本信息
    private GifSpanTextView quoteChatMsgTv;
    //被回复人图片信息的昵称
    private TextView quoteChatNickTv;

    //横/竖屏图片信息
    private ImageView imgMessageIv;
    //横/竖图片加载进度
    private ProgressBar imgLoadingView;
    //横/竖被回复分割线
    private View quoteSplitView;
    //横/竖被回复人的图片信息
    private ImageView quoteImgMessageIv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCMessageViewHolder(View itemView, final PLVLCMessageAdapter adapter) {
        super(itemView, adapter);
        isLandscapeLayout = itemView.getId() == R.id.chat_landscape_item;
        //port item
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
        nickTv = (TextView) findViewById(R.id.nick_tv);
        textMessageTv = (GifSpanTextView) findViewById(R.id.text_message_tv);
        quoteNickTv = (TextView) findViewById(R.id.quote_nick_tv);
        quoteTextMessageTv = (GifSpanTextView) findViewById(R.id.quote_text_message_tv);
        //land item
        chatMsgTv = (GifSpanTextView) findViewById(R.id.chat_msg_tv);
        chatNickTv = (TextView) findViewById(R.id.chat_nick_tv);
        quoteChatMsgTv = (GifSpanTextView) findViewById(R.id.quote_chat_msg_tv);
        quoteChatNickTv = (TextView) findViewById(R.id.quote_chat_nick_tv);
        //common item
        imgMessageIv = (ImageView) findViewById(R.id.img_message_iv);
        imgLoadingView = (ProgressBar) findViewById(R.id.img_loading_view);
        quoteSplitView = findViewById(R.id.quote_split_view);
        quoteImgMessageIv = (ImageView) findViewById(R.id.quote_img_message_iv);

        initView();
        addOnSendImgListener();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化数据、view">
    private void initView() {
        if (textMessageTv != null) {
            textMessageTv.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
                @Override
                public void webLinkOnClick(String url) {
                    PLVWebUtils.openWebLink(url, textMessageTv.getContext());
                }
            });

            textMessageTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PLVCopyBoardPopupWindow.show(textMessageTv, true, textMessageTv.getText().toString());
                    return true;
                }
            });
        }

        if (chatMsgTv != null) {
            chatMsgTv.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
                @Override
                public void webLinkOnClick(String url) {
                    PLVWebUtils.openWebLink(url, chatMsgTv.getContext());
                }
            });

            chatMsgTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PLVCopyBoardPopupWindow.show(chatMsgTv, true, chatMsgTv.getText().toString());
                    return true;
                }
            });
        }

        if (imgMessageIv != null) {
            imgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PLVLCMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, chatImgUrl, false);
                }
            });
        }

        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chatQuoteVO != null && chatQuoteVO.getImage() != null) {
                        String imageUrl = chatQuoteVO.getImage().getUrl();
                        ((PLVLCMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, imageUrl, true);
                    }
                }
            });
        }
    }

    private void addOnSendImgListener() {
        PolyvChatroomManager.getInstance().addSendChatImageListener(new PolyvSendChatImageListener() {
            @Override
            public void onUploadFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    ToastUtils.showLong("发送图片失败: " + t.getMessage());
                }
            }

            @Override
            public void onSendFail(PolyvSendLocalImgEvent localImgEvent, int sendValue) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    ToastUtils.showLong("发送图片失败: " + sendValue);
                }
            }

            @Override
            public void onSuccess(PolyvSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onProgress(PolyvSendLocalImgEvent localImgEvent, float progress) {
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_SENDING);
                localImgEvent.setSendProgress((int) (progress * 100));
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    localImgProgress = localImgEvent.getSendProgress();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.VISIBLE);
                        imgLoadingView.setProgress((int) (progress * 100));
                    }
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVChatMessageBaseViewHolder定义的方法">
    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        resetView();
        //是否是特殊身份类型
        boolean isSpecialType = PLVEventHelper.isSpecialType(userType);//管理员、讲师、助教、嘉宾都视为特殊身份类型
        //设置头像
        if (avatar != null && avatarIv != null) {
            int defaultAvatar;
            //根据用户类型使用不同的占位图
            if (PLVSocketUserConstant.USERTYPE_MANAGER.equals(userType) || PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType)) {
                defaultAvatar = R.drawable.plvlc_chatroom_ic_teacher;
            } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
                defaultAvatar = R.drawable.plvlc_chatroom_ic_assistant;
            } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                defaultAvatar = R.drawable.plvlc_chatroom_ic_guest;
            } else {
                defaultAvatar = R.drawable.plvlc_chatroom_ic_viewer;
            }
            PLVImageLoader.getInstance().loadImageNoDiskCache(
                    itemView.getContext(),
                    avatar,
                    defaultAvatar,
                    defaultAvatar,
                    avatarIv
            );
        }
        //设置昵称
        if (nickName != null) {
            if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(userId)) {
                nickName = nickName + "(我)";
            }
            if (actor != null) {
                nickName = actor + "-" + nickName;
            }
            if (nickTv != null) {
                nickTv.setText(nickName);
                nickTv.setTextColor(Color.parseColor(actor != null ? "#78A7ED" : "#ADADC0"));
            }
            if (chatNickTv != null && chatImgUrl != null) {
                chatNickTv.setVisibility(View.VISIBLE);
                chatNickTv.setText(nickName + ": ");
                chatNickTv.setTextColor(Color.parseColor(actor != null ? "#FFD36D" : "#6DA7FF"));
            }
        }
        //设置发言文本信息
        if (speakMsg != null) {
            if (textMessageTv != null) {
                textMessageTv.setVisibility(View.VISIBLE);
                textMessageTv.setTextColor(Color.parseColor(actor != null ? "#78A7ED" : "#ADADC0"));
                textMessageTv.setTextInner(speakMsg, isSpecialType);
            }
            if (chatMsgTv != null) {
                chatMsgTv.setVisibility(View.VISIBLE);
                SpannableStringBuilder nickSpan = new SpannableStringBuilder(nickName);
                nickSpan.append(": ");
                nickSpan.setSpan(new ForegroundColorSpan(Color.parseColor(actor != null ? "#FFD36D" : "#6DA7FF")), 0, nickSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                chatMsgTv.setTextInner(nickSpan.append(speakMsg), isSpecialType);
            }
        }
        //设置图片信息
        setImgMessage();

        //设置被回复人相关的信息
        if (chatQuoteVO != null) {
            String nickName = chatQuoteVO.getNick();
            if (PolyvSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatQuoteVO.getUserId())) {
                nickName = nickName + "(我)";
            }
            if (quoteSplitView != null) {
                quoteSplitView.setVisibility(View.VISIBLE);
            }
            if (quoteNickTv != null) {
                quoteNickTv.setVisibility(View.VISIBLE);
                quoteNickTv.setText(nickName + ": ");
            }
            if (chatQuoteVO.isSpeakMessage()) {
                if (quoteTextMessageTv != null) {
                    quoteTextMessageTv.setVisibility(View.VISIBLE);
                    quoteTextMessageTv.setText(quoteSpeakMsg);
                }
                if (quoteChatMsgTv != null) {
                    quoteChatMsgTv.setVisibility(View.VISIBLE);
                    quoteChatMsgTv.setText(new SpannableStringBuilder(nickName).append(": ").append(quoteSpeakMsg));
                }
            } else {
                if (quoteChatNickTv != null) {
                    quoteChatNickTv.setVisibility(View.VISIBLE);
                    quoteChatNickTv.setText(nickName + ": ");
                }
                if (quoteImgMessageIv != null && chatQuoteVO.getImage() != null) {
                    quoteImgMessageIv.setVisibility(View.VISIBLE);
                    fitChatImgWH((int) chatQuoteVO.getImage().getWidth(), (int) chatQuoteVO.getImage().getHeight(), quoteImgMessageIv, 60, 40);//适配图片视图的宽高
                    PLVImageLoader.getInstance().loadImage(chatQuoteVO.getImage().getUrl(), quoteImgMessageIv);
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI - 重置view">
    private void resetView() {
        if (textMessageTv != null) {
            textMessageTv.setVisibility(View.GONE);
        }
        if (chatMsgTv != null) {
            chatMsgTv.setVisibility(View.GONE);
        }
        if (chatNickTv != null) {
            chatNickTv.setVisibility(View.GONE);
        }
        if (imgMessageIv != null) {
            imgMessageIv.setVisibility(View.GONE);
            if (imgMessageIv.getDrawable() != null) {
                imgMessageIv.setImageDrawable(null);
            }
        }
        if (imgLoadingView != null) {
            imgLoadingView.setTag(messageData);
        }
        if (isLocalChatImg) {
            if (imgLoadingView != null) {
                imgLoadingView.setVisibility(localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_SENDING ? View.VISIBLE : View.GONE);
                imgLoadingView.setProgress(localImgProgress);
            }
        } else {
            if (imgLoadingView != null) {
                imgLoadingView.setVisibility(View.GONE);
                imgLoadingView.setProgress(0);
            }
        }
        //被回复人信息相关view reset
        if (quoteNickTv != null) {
            quoteNickTv.setVisibility(View.GONE);
        }
        if (quoteTextMessageTv != null) {
            quoteTextMessageTv.setVisibility(View.GONE);
        }
        if (quoteChatMsgTv != null) {
            quoteChatMsgTv.setVisibility(View.GONE);
        }
        if (quoteChatNickTv != null) {
            quoteChatNickTv.setVisibility(View.GONE);
        }
        if (quoteSplitView != null) {
            quoteSplitView.setVisibility(View.GONE);
        }
        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据交互 - 设置图片信息">
    private void setImgMessage() {
        if (chatImgUrl != null && imgMessageIv != null) {
            imgMessageIv.setVisibility(View.VISIBLE);
            fitChatImgWH(chatImgWidth, chatImgHeight, imgMessageIv, 120, 80);//适配图片视图的宽高
            if (isLocalChatImg) {
                PLVImageLoader.getInstance().loadImage(chatImgUrl, imgMessageIv);
            } else {
                PLVImageLoader.getInstance().loadImage(
                        itemView.getContext(),
                        LOADIMG_MOUDLE_TAG,
                        LOADIMG_MOUDLE_TAG + messageData,
                        R.drawable.plvlc_image_load_err,
                        createStatusListener(chatImgUrl),
                        imgMessageIv);
            }
        }
    }

    private PLVAbsProgressStatusListener createStatusListener(String imgUrl) {
        return new PLVAbsProgressStatusListener(imgUrl) {
            @Override
            public void onStartStatus(String url) {
                if (imgLoadingView.getTag() != messageData) {//addFistData position can replace
                    return;
                }
                if (imgLoadingView.getProgress() == 0 && imgLoadingView.getVisibility() != View.VISIBLE) {
                    imgLoadingView.setVisibility(View.VISIBLE);
                    imgMessageIv.setImageDrawable(null);
                }
            }

            @Override
            public void onProgressStatus(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                if (isComplete) {
                    imgLoadingView.setProgress(100);
                    imgLoadingView.setVisibility(View.GONE);
                } else {//onFailed之后可能触发onProgress
                    if (imgMessageIv.getDrawable() != null) {
                        imgMessageIv.setImageDrawable(null);
                    }
                    imgLoadingView.setVisibility(View.VISIBLE);
                    imgLoadingView.setProgress(percentage);
                }
            }

            @Override
            public void onFailedStatus(@Nullable Exception e, Object model) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                imgLoadingView.setVisibility(View.GONE);
                imgLoadingView.setProgress(0);
                imgMessageIv.setImageResource(R.drawable.plvlc_image_load_err);//fail can no set
            }

            @Override
            public void onResourceReadyStatus(Drawable drawable) {
                if (imgLoadingView.getTag() != messageData) {
                    return;
                }
                imgMessageIv.setImageDrawable(drawable);
            }
        };
    }
    // </editor-fold>
}
