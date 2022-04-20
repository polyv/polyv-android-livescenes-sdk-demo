package com.easefun.polyv.livestreamer.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVToast;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectSpan;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.modules.chatroom.adapter.PLVLSMessageAdapter;
import com.easefun.polyv.livestreamer.modules.chatroom.widget.PLVLSChatMsgTipsWindow;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.IPLVManagerChatEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import java.util.List;


/**
 * 聊天室通用聊天信息的viewHolder
 */
public class PLVLSMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLSMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String LOADIMG_MOUDLE_TAG = "PLVLCMessageViewHolder";

    //横屏item
    //文本信息
    private GifSpanTextView chatMsgTv;
    //图片信息的昵称
    private TextView chatNickTv;
    //被回复人的文本信息
    private GifSpanTextView quoteChatMsgTv;
    //被回复人图片信息的昵称
    private TextView quoteChatNickTv;
    //严禁词触发的提示图标
    private ImageView prohibitedWordTipsIv;

    //图片发送失败（审核不通过
    private View failedImageItemLl;
    private ImageView failedImageTipIv;

    //横/竖屏图片信息
    private View imgMessageItem;
    private ImageView imgMessageIv;
    //横/竖图片加载进度
    private ProgressBar imgLoadingView;
    //横/竖被回复分割线
    private View quoteSplitView;
    //横/竖被回复人的图片信息
    private ImageView quoteImgMessageIv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLSMessageViewHolder(View itemView, final PLVLSMessageAdapter adapter) {
        super(itemView, adapter);
        //land item
        chatMsgTv = (GifSpanTextView) findViewById(R.id.chat_msg_tv);
        chatNickTv = (TextView) findViewById(R.id.chat_nick_tv);
        quoteChatMsgTv = (GifSpanTextView) findViewById(R.id.quote_chat_msg_tv);
        quoteChatNickTv = (TextView) findViewById(R.id.quote_chat_nick_tv);
        prohibitedWordTipsIv = findViewById(R.id.prohibited_word_tips_iv);
        imgMessageItem = findViewById(R.id.chat_image_item_fl);
        failedImageItemLl = findViewById(R.id.failed_image_ll);
        failedImageTipIv = findViewById(R.id.failed_image_tips_iv);
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
                    final boolean isManagerChatMessage = messageData instanceof IPLVManagerChatEvent && ((IPLVManagerChatEvent) messageData).isManagerChatMsg();
                    final boolean isProhibited = prohibitedWordVO != null;
                    // 严禁词、管理员私聊的信息不能回复
                    boolean onlyShowCopyItem = isProhibited || isManagerChatMessage;
                    PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, onlyShowCopyItem, chatMsgTv.getText().toString(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                            chatQuoteVO.setUserId(userId);
                            chatQuoteVO.setNick(nickName);
                            chatQuoteVO.setContent(speakMsg.toString());
                            chatQuoteVO.setObjects(PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(chatQuoteVO.getContent()), ConvertUtils.dp2px(12), Utils.getApp()));
                            adapter.callOnShowAnswerWindow(chatQuoteVO, ((IPLVIdEvent) messageData).getId());
                        }
                    });
                    return true;
                }
            });
        }

        if (prohibitedWordTipsIv != null) {
            prohibitedWordTipsIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (prohibitedWordVO == null) {
                        return;
                    }
                    String msg = prohibitedWordVO.getMessage() + "：" + prohibitedWordVO.getValue();
                    int[] location = new int[2];
                    itemView.getLocationInWindow(location);
                    new PLVLSChatMsgTipsWindow(v).show(v, msg, location[0], location[0] + itemView.getWidth(), location[1] + itemView.getHeight());
                }
            });
        }
        if(failedImageTipIv != null){
            failedImageTipIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_FAIL) {
                        String msg = localImgFailMessage+"";
                        int[] location = new int[2];
                        itemView.getLocationInWindow(location);
                        new PLVLSChatMsgTipsWindow(v).show(v, msg, location[0], location[0] + itemView.getWidth(), location[1] + itemView.getHeight());
                    }
                }
            });
        }

        if (imgMessageIv != null) {
            imgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((PLVLSMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, chatImgUrl, false);
                }
            });

            imgMessageIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAndAnswerWithImg();
                    return true;
                }
            });
        }

        if (chatNickTv != null) {
            chatNickTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAndAnswerWithImg();
                    return true;
                }
            });
        }

        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chatQuoteVO != null && chatQuoteVO.getImage() != null) {
                        String imageUrl = chatQuoteVO.getImage().getUrl();
                        ((PLVLSMessageAdapter) adapter).callOnChatImgClick(getVHPosition(), v, imageUrl, true);
                    }
                }
            });
        }
    }

    private void showAndAnswerWithImg() {
        if (localImgStatus != PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS) {
            return;//图片发送成功后才可回复
        }
        PLVCopyBoardPopupWindow.showAndAnswer(itemView, true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                chatQuoteVO.setUserId(userId);
                chatQuoteVO.setNick(nickName);
                PLVChatQuoteVO.ImageBean imageBean = new PLVChatQuoteVO.ImageBean();
                imageBean.setUrl(chatImgUrl);
                imageBean.setWidth(chatImgWidth);
                imageBean.setHeight(chatImgHeight);
                chatQuoteVO.setImage(imageBean);
                adapter.callOnShowAnswerWindow(chatQuoteVO, ((IPLVIdEvent) messageData).getId());
            }
        });
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
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("发送图片失败: " + t.getMessage())
                            .build()
                            .show();
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
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("发送图片失败: " + sendValue)
                            .build()
                            .show();
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

            @Override
            public void onCheckFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                //审核不通过
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    localImgFailMessage = t.getMessage();
                    ((PolyvSendLocalImgEvent) messageData).setObj1(localImgFailMessage);
                    if (imgMessageItem != null) {
                        imgMessageItem.setVisibility(View.GONE);
                    }
                    if(failedImageItemLl != null){
                        failedImageItemLl.setVisibility(View.VISIBLE);
                    }
                    PLVToast.Builder.context(itemView.getContext())
                            .setText("发送图片失败: " + t.getMessage())
                            .build()
                            .show();
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
        final boolean isSpecialType = PLVEventHelper.isSpecialType(userType);//管理员、讲师、助教、嘉宾都视为特殊身份类型
        final boolean isManagerChatMsg = data.getData() instanceof PLVBaseEvent && PLVEventHelper.isManagerChatMsg(((PLVBaseEvent) data.getData()));
        final boolean isMsgSendByMyself = userId != null && userId.equals(PLVSocketWrapper.getInstance().getLoginVO().getUserId());
        //设置昵称
        final PLVSpannableStringBuilder textSpan = new PLVSpannableStringBuilder();
        final int textColor = Color.parseColor(actor != null ? "#FFD36D" : "#6DA7FF");
        if (isManagerChatMsg) {
            textSpan.appendExclude("私聊",
                    new PLVRoundRectSpan()
                            .backgroundColor(Color.parseColor("#57A2FF"))
                            .textColor(Color.parseColor("#313540"))
                            .textSize(10)
                            .paddingLeft(6)
                            .paddingRight(6)
                            .radius(7)
                            .marginRight(4)
            );
        }
        if (actor != null) {
            textSpan.appendExclude(actor + "-", new ForegroundColorSpan(textColor));
        }
        textSpan.appendExclude(nickName, new ForegroundColorSpan(textColor));
        if (isMsgSendByMyself) {
            textSpan.appendExclude("(我)", new ForegroundColorSpan(textColor));
        }
        textSpan.appendExclude(": ", new ForegroundColorSpan(textColor));
        if (nickName != null) {
            if (chatNickTv != null && chatImgUrl != null) {
                chatNickTv.setVisibility(View.VISIBLE);
                chatNickTv.setText(textSpan);
            }
        }
        //设置发言文本信息
        if (speakMsg != null) {
            if (chatMsgTv != null) {
                chatMsgTv.setVisibility(View.VISIBLE);
                chatMsgTv.setTextInner(textSpan.append(speakMsg), isSpecialType);
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
            if (chatQuoteVO.isSpeakMessage()) {
                if (quoteChatMsgTv != null) {
                    quoteChatMsgTv.setVisibility(View.VISIBLE);
                    quoteChatMsgTv.setText(new SpannableStringBuilder(nickName).append(": ").append(quoteSpeakMsg));
                }
            } else {
                if (quoteChatNickTv != null) {
                    quoteChatNickTv.setVisibility(View.VISIBLE);
                    quoteChatNickTv.setText(nickName + ": ");
                }
                if (chatQuoteVO.getImage() != null) {
                    if (quoteImgMessageIv != null) {
                        quoteImgMessageIv.setVisibility(View.VISIBLE);
                        fitChatImgWH((int) chatQuoteVO.getImage().getWidth(), (int) chatQuoteVO.getImage().getHeight(), quoteImgMessageIv, 40, 0);//适配图片视图的宽高
                        PLVImageLoader.getInstance().loadImage(chatQuoteVO.getImage().getUrl(), quoteImgMessageIv);
                    }
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API">
    public void processData(PLVBaseViewData data, int position, @NonNull List<Object> payloads) {
        for (Object payload : payloads) {
            switch (payload.toString()) {
                case PLVLSMessageAdapter.PAYLOAD_PROHIBITED_CHANGED:
                    super.processData(data, position);
                    if (chatMsgTv != null) {
                        if (chatMsgTv.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) chatMsgTv.getLayoutParams());
                            layoutParams.rightMargin = prohibitedWordVO == null ? 0 : ConvertUtils.dp2px(20);
                            chatMsgTv.setLayoutParams(layoutParams);
                        }
                    }
                    if (prohibitedWordTipsIv != null) {
                        prohibitedWordTipsIv.setVisibility(prohibitedWordVO == null ? View.GONE : View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI - 重置view">
    private void resetView() {
        if (chatMsgTv != null) {
            chatMsgTv.setVisibility(View.GONE);
            if (chatMsgTv.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) chatMsgTv.getLayoutParams());
                layoutParams.rightMargin = prohibitedWordVO == null ? 0 : ConvertUtils.dp2px(20);
                chatMsgTv.setLayoutParams(layoutParams);
            }
        }
        if (prohibitedWordTipsIv != null) {
            prohibitedWordTipsIv.setVisibility(prohibitedWordVO == null ? View.GONE : View.VISIBLE);
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
        if(isLocalChatImg){
            if(failedImageItemLl != null && imgMessageIv != null){
                if(localImgStatus == PolyvSendLocalImgEvent.SENDSTATUS_FAIL){
                    failedImageItemLl.setVisibility(View.VISIBLE);
                    imgMessageIv.setVisibility(View.GONE);
                    return;
                }
            }
        }
        if (chatImgUrl != null) {
            if (imgMessageIv != null) {
                imgMessageIv.setVisibility(View.VISIBLE);
                fitChatImgWH(chatImgWidth, chatImgHeight, imgMessageIv, 80, 0);//适配图片视图的宽高
                if (isLocalChatImg) {
                    PLVImageLoader.getInstance().loadImage(chatImgUrl, imgMessageIv);
                } else {
                    PLVImageLoader.getInstance().loadImage(
                            itemView.getContext(),
                            LOADIMG_MOUDLE_TAG,
                            LOADIMG_MOUDLE_TAG + messageData,
                            R.drawable.plv_icon_image_load_err,
                            createStatusListener(chatImgUrl),
                            imgMessageIv);
                }
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
                imgMessageIv.setImageResource(R.drawable.plv_icon_image_load_err);//fail can no set
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
