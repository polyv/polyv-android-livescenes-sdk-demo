package com.plv.livestreamer.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.plv.business.sub.gif.GifSpanTextView;
import com.plv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.plv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.plv.livecommon.module.utils.PLVToast;
import com.plv.livecommon.module.utils.PLVWebUtils;
import com.plv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.plv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.plv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.plv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.plv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageListener;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.livestreamer.R;
import com.plv.livestreamer.modules.chatroom.adapter.PLVLSMessageAdapter;
import com.plv.livestreamer.modules.chatroom.widget.PLVLSChatMsgTipsWindow;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.IPLVIdEvent;
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
    public PLVLSMessageViewHolder(View itemView, final PLVLSMessageAdapter adapter) {
        super(itemView, adapter);
        //land item
        chatMsgTv = (GifSpanTextView) findViewById(R.id.chat_msg_tv);
        chatNickTv = (TextView) findViewById(R.id.chat_nick_tv);
        quoteChatMsgTv = (GifSpanTextView) findViewById(R.id.quote_chat_msg_tv);
        quoteChatNickTv = (TextView) findViewById(R.id.quote_chat_nick_tv);
        prohibitedWordTipsIv = findViewById(R.id.prohibited_word_tips_iv);
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
                    boolean onlyShowCopyItem = prohibitedWordVO != null;//严禁词的信息不能回复
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
        if (localImgStatus != PLVSendLocalImgEvent.SENDSTATUS_SUCCESS) {
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
        PLVChatroomManager.getInstance().addSendChatImageListener(new PLVSendChatImageListener() {
            @Override
            public void onUploadFail(PLVSendLocalImgEvent localImgEvent, Throwable t) {
                localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
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
            public void onSendFail(PLVSendLocalImgEvent localImgEvent, int sendValue) {
                localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
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
            public void onSuccess(PLVSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
                localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_SUCCESS);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onProgress(PLVSendLocalImgEvent localImgEvent, float progress) {
                localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_SENDING);
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
        //设置昵称
        String setNickName = nickName;
        if (nickName != null) {
            if (PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(userId)) {
                setNickName = nickName + "(我)";
            }
            if (actor != null) {
                setNickName = actor + "-" + setNickName;
            }
            if (chatNickTv != null && chatImgUrl != null) {
                chatNickTv.setVisibility(View.VISIBLE);
                chatNickTv.setText(setNickName + ": ");
                chatNickTv.setTextColor(Color.parseColor(actor != null ? "#FFD36D" : "#6DA7FF"));
            }
        }
        //设置发言文本信息
        if (speakMsg != null) {
            if (chatMsgTv != null) {
                chatMsgTv.setVisibility(View.VISIBLE);
                SpannableStringBuilder nickSpan = new SpannableStringBuilder(setNickName);
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
            if (PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(chatQuoteVO.getUserId())) {
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
                imgLoadingView.setVisibility(localImgStatus == PLVSendLocalImgEvent.SENDSTATUS_SENDING ? View.VISIBLE : View.GONE);
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
