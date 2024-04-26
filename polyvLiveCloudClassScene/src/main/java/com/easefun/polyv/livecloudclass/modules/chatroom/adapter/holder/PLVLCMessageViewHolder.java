package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import static com.plv.foundationsdk.ext.PLVViewGroupExt.setOnLongClickListenerRecursively;
import static com.plv.foundationsdk.utils.PLVAppUtils.postToMainThread;
import static com.plv.foundationsdk.utils.PLVSugarUtil.nullable;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecloudclass.modules.chatroom.layout.PLVLCChatOverLengthMessageLayout;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livescenes.chatroom.PolyvChatroomManager;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.google.gson.reflect.TypeToken;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVGsonUtil;
import com.plv.foundationsdk.utils.PLVSugarUtil;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.ppt.PLVPptShareFileVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * 聊天室通用聊天信息的viewHolder
 */
public class PLVLCMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String LOADIMG_MOUDLE_TAG = "PLVLCMessageViewHolder";

    // 全局存储长按复制/回复的弹层
    @Nullable
    private static WeakReference<PopupWindow> copyBoardPopupWindowRef;

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
    //回复文本气泡框
    private View chatLayout;
    private LinearLayout chatMsgLl;
    private ImageView chatMsgFileShareIv;
    @Nullable
    private View chatMsgOverLengthMask;

    //横屏item
    //文本信息
    private GifSpanTextView chatMsgTv;
    //图片信息的昵称
    private TextView chatNickTv;
    //被回复人的文本信息
    private GifSpanTextView quoteChatMsgTv;
    //被回复人图片信息的昵称
    private TextView quoteChatNickTv;
    private LinearLayout chatMsgLandLl;
    private ImageView chatMsgFileShareLandIv;
    @Nullable
    private View chatMsgOverLengthSplitLine;

    //横/竖屏图片信息
    private ImageView imgMessageIv;
    //横/竖图片加载进度
    private ProgressBar imgLoadingView;
    //横/竖被回复分割线
    private View quoteSplitView;
    //横/竖被回复人的图片信息
    private ImageView quoteImgMessageIv;
    private LinearLayout chatMsgOverLengthControlLl;
    private TextView chatMsgOverLengthCopyBtn;
    private TextView chatMsgOverLengthMoreBtn;

    private boolean isOverLengthContentFolding = true;
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
        chatMsgLl = findViewById(R.id.plvlc_chat_msg_ll);
        chatMsgFileShareIv = findViewById(R.id.plvlc_chat_msg_file_share_iv);
        chatLayout = findViewById(R.id.chat_msg_ll);
        chatMsgOverLengthMask = findViewById(R.id.plvlc_chat_msg_over_length_mask);

        //land item
        chatMsgTv = (GifSpanTextView) findViewById(R.id.chat_msg_tv);
        chatNickTv = (TextView) findViewById(R.id.chat_nick_tv);
        quoteChatMsgTv = (GifSpanTextView) findViewById(R.id.quote_chat_msg_tv);
        quoteChatNickTv = (TextView) findViewById(R.id.quote_chat_nick_tv);
        chatMsgLandLl = findViewById(R.id.plvlc_chat_msg_land_ll);
        chatMsgFileShareLandIv = findViewById(R.id.plvlc_chat_msg_file_share_land_iv);
        chatMsgOverLengthSplitLine = findViewById(R.id.plvlc_chat_msg_over_length_split_line);

        //common item
        imgMessageIv = (ImageView) findViewById(R.id.img_message_iv);
        imgLoadingView = (ProgressBar) findViewById(R.id.img_loading_view);
        quoteSplitView = findViewById(R.id.quote_split_view);
        quoteImgMessageIv = (ImageView) findViewById(R.id.quote_img_message_iv);
        chatMsgOverLengthControlLl = findViewById(R.id.plvlc_chat_msg_over_length_control_ll);
        chatMsgOverLengthCopyBtn = findViewById(R.id.plvlc_chat_msg_over_length_copy_btn);
        chatMsgOverLengthMoreBtn = findViewById(R.id.plvlc_chat_msg_over_length_more_btn);

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
            setOnLongClickListenerRecursively((ViewGroup) chatLayout, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickItem(chatLayout);
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
            setOnLongClickListenerRecursively((ViewGroup) itemView, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickItem(itemView);
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
            imgMessageIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onLongClickItem(imgMessageIv);
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
                    ToastUtils.showLong(PLVAppUtils.formatString(R.string.plv_chat_send_img_fail, t.getMessage()));
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
                    ToastUtils.showLong(PLVAppUtils.formatString(R.string.plv_chat_send_img_fail, sendValue + ""));
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
                localImgEvent.setSendStatus(PolyvSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    ToastUtils.showLong(PLVAppUtils.formatString(R.string.plv_chat_send_img_fail, t.getMessage()));
                }
            }
        });
    }

    private void onLongClickItem(View anchor) {
        if (speakFileData != null) {
            // 文件类型消息
            return;
        }
        if (chatImgUrl != null) {
            onLongClickImageMessage(anchor);
        } else if (speakMsg != null) {
            onLongClickTextMessage(anchor);
        }
    }

    private void onLongClickTextMessage(View anchor) {
        final boolean showCopyButton = !isOverLengthFoldingMessage;
        final boolean showReplyButton = adapter.isAllowReplyMessage();
        hideCopyBoardPopupWindow();
        final PopupWindow popupWindow = PLVCopyBoardPopupWindow.showAndAnswer(anchor, true, !showReplyButton, showCopyButton ? speakMsg.toString() : null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                if (messageData instanceof IPLVIdEvent) {
                    chatQuoteVO.setMessageId(((IPLVIdEvent) messageData).getId());
                }
                chatQuoteVO.setUserId(userId);
                chatQuoteVO.setNick(nickName);
                chatQuoteVO.setContent(speakMsg.toString());
                chatQuoteVO.setObjects(speakMsg);
                adapter.callOnReplyMessage(chatQuoteVO);
            }
        });
        copyBoardPopupWindowRef = new WeakReference<>(popupWindow);
    }

    private void onLongClickImageMessage(View anchor) {
        if (localImgStatus != PolyvSendLocalImgEvent.SENDSTATUS_SUCCESS) {
            //图片发送成功后才可回复
            return;
        }
        final boolean showReplyButton = adapter.isAllowReplyMessage();
        hideCopyBoardPopupWindow();
        final PopupWindow popupWindow = PLVCopyBoardPopupWindow.showAndAnswer(anchor, true, !showReplyButton, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                if (messageData instanceof IPLVIdEvent) {
                    chatQuoteVO.setMessageId(((IPLVIdEvent) messageData).getId());
                }
                chatQuoteVO.setUserId(userId);
                chatQuoteVO.setNick(nickName);
                PLVChatQuoteVO.ImageBean imageBean = new PLVChatQuoteVO.ImageBean();
                imageBean.setUrl(chatImgUrl);
                imageBean.setWidth(chatImgWidth);
                imageBean.setHeight(chatImgHeight);
                chatQuoteVO.setImage(imageBean);
                adapter.callOnReplyMessage(chatQuoteVO);
            }
        });
        copyBoardPopupWindowRef = new WeakReference<>(popupWindow);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部API - 实现PLVChatMessageBaseViewHolder定义的方法">
    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        resetView();
        //是否是特殊身份类型
        boolean isSpecialType = PLVEventHelper.isSpecialType(userType);//管理员、讲师、助教、嘉宾都视为特殊身份类型

        final String loginUserId = nullable(new PLVSugarUtil.Supplier<String>() {
            @Override
            public String get() {
                return PLVSocketWrapper.getInstance().getLoginVO().getUserId();
            }
        });

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
            String showName = nickName;
            if (loginUserId != null && loginUserId.equals(userId)) {
                showName = showName + PLVAppUtils.getString(R.string.plv_chat_me_2);
            }
            if (actor != null) {
                showName = actor + "-" + showName;
            }
            if (nickTv != null) {
                nickTv.setText(showName);
                nickTv.setTextColor(Color.parseColor(actor != null ? "#78A7ED" : "#ADADC0"));
            }
            if (chatNickTv != null && chatImgUrl != null) {
                chatNickTv.setVisibility(View.VISIBLE);
                chatNickTv.setText(showName + ": ");
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
        // ppt文件分享信息
        if (speakFileData != null) {
            if (textMessageTv != null) {
                textMessageTv.setVisibility(View.VISIBLE);
                textMessageTv.setTextColor(Color.WHITE);
                textMessageTv.setTextInner(speakFileData.getName(), false);
            }
            if (chatMsgTv != null) {
                chatMsgTv.setVisibility(View.VISIBLE);
                SpannableStringBuilder nickSpan = new SpannableStringBuilder(nickName);
                nickSpan.append(": ");
                nickSpan.setSpan(new ForegroundColorSpan(Color.parseColor(actor != null ? "#FFD36D" : "#6DA7FF")), 0, nickSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                chatMsgTv.setTextInner(nickSpan.append(speakFileData.getName()), false);
            }
            if (chatMsgLl != null) {
                chatMsgLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (speakFileData != null) {
                            PLVWebUtils.openWebLink(speakFileData.getUrl(), chatMsgLl.getContext());
                        }
                    }
                });
            }
            if (chatMsgLandLl != null) {
                chatMsgLandLl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (speakFileData != null) {
                            PLVWebUtils.openWebLink(speakFileData.getUrl(), chatMsgLandLl.getContext());
                        }
                    }
                });
            }
            final Integer fileIconRes = getSpeakFileIconRes(speakFileData);
            if (chatMsgFileShareIv != null) {
                if (fileIconRes != null) {
                    chatMsgFileShareIv.setVisibility(View.VISIBLE);
                    chatMsgFileShareIv.setImageResource(fileIconRes);
                } else {
                    chatMsgFileShareIv.setVisibility(View.GONE);
                }
            }
            if (chatMsgFileShareLandIv != null) {
                if (fileIconRes != null) {
                    chatMsgFileShareLandIv.setVisibility(View.VISIBLE);
                    chatMsgFileShareLandIv.setImageResource(fileIconRes);
                } else {
                    chatMsgFileShareLandIv.setVisibility(View.GONE);
                }
            }
        } else {
            if (chatMsgFileShareIv != null) {
                chatMsgFileShareIv.setVisibility(View.GONE);
            }
            if (chatMsgFileShareLandIv != null) {
                chatMsgFileShareLandIv.setVisibility(View.GONE);
            }
            if (chatMsgLl != null) {
                chatMsgLl.setOnClickListener(null);
            }
            if (chatMsgLandLl != null) {
                chatMsgLandLl.setOnClickListener(null);
            }
        }
        //设置图片信息
        setImgMessage();

        //设置被回复人相关的信息
        if (chatQuoteVO != null) {
            String nickName = chatQuoteVO.getNick();
            if (loginUserId != null && loginUserId.equals(chatQuoteVO.getUserId())) {
                nickName = nickName + PLVAppUtils.getString(R.string.plv_chat_me_2);
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
                    quoteTextMessageTv.setText(fixQuoteMessageForFileShare(quoteSpeakMsg));
                }
                if (quoteChatMsgTv != null) {
                    quoteChatMsgTv.setVisibility(View.VISIBLE);
                    quoteChatMsgTv.setText(new SpannableStringBuilder(nickName).append(": ").append(fixQuoteMessageForFileShare(quoteSpeakMsg)));
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

        processOverLengthMessage();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UI - 重置view">
    public static void hideCopyBoardPopupWindow() {
        if (copyBoardPopupWindowRef == null) {
            return;
        }
        PopupWindow popupWindow = copyBoardPopupWindowRef.get();
        if (popupWindow == null) {
            return;
        }
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        copyBoardPopupWindowRef = null;
    }

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

    private void updateOverLengthView() {
        if (!isOverLengthFoldingMessage) {
            if (chatMsgOverLengthMask != null) {
                chatMsgOverLengthMask.setVisibility(View.GONE);
            }
            if (chatMsgOverLengthSplitLine != null) {
                chatMsgOverLengthSplitLine.setVisibility(View.GONE);
            }
            chatMsgOverLengthControlLl.setVisibility(View.GONE);
            return;
        }
        chatMsgOverLengthControlLl.setVisibility(View.VISIBLE);

        if (chatMsgOverLengthMask != null) {
            chatMsgOverLengthMask.setVisibility(isOverLengthContentFolding ? View.VISIBLE : View.GONE);
        }
        if (chatMsgOverLengthSplitLine != null) {
            chatMsgOverLengthSplitLine.setVisibility(View.VISIBLE);
        }
        chatMsgOverLengthMoreBtn.setText(isOverLengthContentFolding ? R.string.plv_chat_msg_over_length_more : R.string.plv_chat_msg_over_length_fold);
        if (textMessageTv != null) {
            textMessageTv.setMaxLines(isOverLengthContentFolding ? 6 : Integer.MAX_VALUE);
        }
        if (chatMsgTv != null) {
            chatMsgTv.setMaxLines(isOverLengthContentFolding ? 6 : Integer.MAX_VALUE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据交互 - 设置图片信息">
    private void setImgMessage() {
        if (chatImgUrl != null && imgMessageIv != null) {
            if (!isLandscapeLayout) {
                chatLayout.setVisibility(View.INVISIBLE);
            }
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
        } else {
            if(!isLandscapeLayout) {
                chatLayout.setVisibility(View.VISIBLE);
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

    private void processOverLengthMessage() {
        isOverLengthContentFolding = true;

        chatMsgOverLengthCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isFullMessage || fullMessageOnOverLength == null) {
                    PLVCopyBoardPopupWindow.copy(v.getContext(), speakMsg.toString());
                } else {
                    fullMessageOnOverLength.getAsync(new PLVSugarUtil.Consumer<String>() {
                        @Override
                        public void accept(final String s) {
                            postToMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    PLVCopyBoardPopupWindow.copy(v.getContext(), s);
                                }
                            });
                        }
                    });
                }
            }
        });
        chatMsgOverLengthMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOverLengthShowAloneMessage) {
                    adapter.callOnShowOverLengthMessage(createShowAloneOverLengthMessage());
                } else {
                    isOverLengthContentFolding = !isOverLengthContentFolding;
                    updateOverLengthView();
                }
            }
        });

        updateOverLengthView();
    }

    private PLVLCChatOverLengthMessageLayout.BaseChatMessageDataBean createShowAloneOverLengthMessage() {
        return new PLVLCChatOverLengthMessageLayout.BaseChatMessageDataBean.Builder()
                .setAvatar(avatar)
                .setNick(nickName)
                .setUserType(userType)
                .setActor(actor)
                .setMessage(speakMsg)
                .setOverLength(!isFullMessage)
                .setOnOverLengthFullMessage(fullMessageOnOverLength)
                .build();
    }

    @Nullable
    @DrawableRes
    private static Integer getSpeakFileIconRes(@Nullable PLVPptShareFileVO fileData) {
        if (fileData == null) {
            return null;
        }
        switch (fileData.getSuffix()) {
            case "ppt":
            case "pptx":
                return R.drawable.plvlc_chatroom_file_share_ppt_icon;
            case "doc":
            case "docx":
                return R.drawable.plvlc_chatroom_file_share_doc_icon;
            case "xls":
            case "xlsx":
                return R.drawable.plvlc_chatroom_file_share_xls_icon;
            case "pdf":
                return R.drawable.plvlc_chatroom_file_share_pdf_icon;
            default:
                return null;
        }
    }

    private static CharSequence fixQuoteMessageForFileShare(CharSequence originMessage) {
        try {
            final String originMessageStr = originMessage.toString();
            final Map<String, String> fileShareDataMap = PLVGsonUtil.fromJson(new TypeToken<Map<String, String>>() {}, originMessageStr);
            if (fileShareDataMap == null) {
                return originMessage;
            }
            final boolean isFileShareQuoteMessage = fileShareDataMap.size() == 2 && fileShareDataMap.containsKey("url") && fileShareDataMap.containsKey("name");
            return isFileShareQuoteMessage ? fileShareDataMap.get("name") : originMessage;
        } catch (Exception ignored) {
            return originMessage;
        }
    }
    // </editor-fold>
}
