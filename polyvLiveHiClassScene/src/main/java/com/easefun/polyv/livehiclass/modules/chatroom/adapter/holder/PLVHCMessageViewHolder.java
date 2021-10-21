package com.easefun.polyv.livehiclass.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.presenter.PLVChatroomPresenter;
import com.easefun.polyv.livecommon.module.utils.PLVWebUtils;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressStatusListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.module.utils.span.PLVTextFaceLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVCopyBoardPopupWindow;
import com.easefun.polyv.livecommon.ui.widget.gif.GifSpanTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livehiclass.R;
import com.easefun.polyv.livehiclass.modules.chatroom.adapter.PLVHCMessageAdapter;
import com.easefun.polyv.livehiclass.ui.widget.PLVHCToast;
import com.plv.livescenes.chatroom.PLVChatroomManager;
import com.plv.livescenes.chatroom.send.img.PLVSendChatImageListener;
import com.plv.livescenes.chatroom.send.img.PLVSendLocalImgEvent;
import com.plv.livescenes.socket.PLVSocketWrapper;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.event.chat.IPLVIdEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.net.model.PLVSocketLoginVO;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * 聊天室通用聊天信息的viewHolder
 */
public class PLVHCMessageViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVHCMessageAdapter> {
    // <editor-fold defaultstate="collapsed" desc="变量">
    public static final String LOADIMG_MOUDLE_TAG = "PLVHCMessageViewHolder";

    //聊天信息的父布局
    private ViewGroup msgParentLy;

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

    //图片信息
    private ImageView imgMessageIv;
    //图片加载进度
    private ProgressBar imgLoadingView;
    //被回复分割线
    private View quoteSplitView;
    //被回复人的图片信息
    private ImageView quoteImgMessageIv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVHCMessageViewHolder(View itemView, final PLVHCMessageAdapter adapter) {
        super(itemView, adapter);
        msgParentLy = findViewById(R.id.plvhc_chatroom_msg_parent_ly);
        avatarIv = findViewById(R.id.plvhc_chatroom_avatar_iv);
        nickTv = findViewById(R.id.plvhc_chatroom_nick_tv);
        textMessageTv = findViewById(R.id.plvhc_chatroom_text_message_tv);
        quoteNickTv = findViewById(R.id.plvhc_chatroom_quote_nick_tv);
        quoteTextMessageTv = findViewById(R.id.plvhc_chatroom_quote_text_message_tv);
        imgMessageIv = findViewById(R.id.plvhc_chatroom_img_message_iv);
        imgLoadingView = findViewById(R.id.plvhc_chatroom_img_loading_view);
        quoteSplitView = findViewById(R.id.plvhc_chatroom_quote_split_view);
        quoteImgMessageIv = findViewById(R.id.plvhc_chatroom_quote_img_message_iv);

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
                    int[] parentLayoutLocation = new int[2];
                    ((View) itemView.getParent()).getLocationInWindow(parentLayoutLocation);
                    PLVCopyBoardPopupWindow.showAndAnswer(msgParentLy, true, parentLayoutLocation[1], !canAnswer(), textMessageTv.getText().toString(), R.drawable.plvhc_chatroom_cp_ly_shape, R.drawable.plvhc_chatroom_inverted_triangle_layer, Color.parseColor("#333333"), new PLVCopyBoardPopupWindow.CopyBoardClickListener() {
                        @Override
                        public void onClickAnswerButton() {
                            PLVChatQuoteVO chatQuoteVO = new PLVChatQuoteVO();
                            chatQuoteVO.setUserId(userId);
                            chatQuoteVO.setNick(nickName);
                            chatQuoteVO.setContent(speakMsg.toString());
                            chatQuoteVO.setObjects(PLVTextFaceLoader.messageToSpan(PLVChatroomPresenter.convertSpecialString(chatQuoteVO.getContent()), ConvertUtils.dp2px(12), Utils.getApp()));
                            adapter.callOnShowAnswerWindow(chatQuoteVO, ((IPLVIdEvent) messageData).getId());
                        }

                        @Override
                        public boolean onClickCopyButton() {
                            PLVHCToast.Builder.context(textMessageTv.getContext())
                                    .setText("已复制")
                                    .build()
                                    .show();
                            return true;
                        }
                    });
                    return true;
                }
            });
        }

        if (imgMessageIv != null) {
            imgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.callOnChatImgClick(getVHPosition(), v, chatImgUrl, false);
                }
            });

            imgMessageIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (canAnswer()) {
                        showAndAnswerWithImg();
                        return true;
                    }
                    return false;
                }
            });
        }

        if (quoteImgMessageIv != null) {
            quoteImgMessageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chatQuoteVO != null && chatQuoteVO.getImage() != null) {
                        String imageUrl = chatQuoteVO.getImage().getUrl();
                        adapter.callOnChatImgClick(getVHPosition(), v, imageUrl, true);
                    }
                }
            });
        }
    }

    private boolean canAnswer() {
        PLVSocketLoginVO loginVO = PLVSocketWrapper.getInstance().getLoginVO();
        if (loginVO != null) {
            return PLVSocketUserConstant.USERTYPE_TEACHER.equals(loginVO.getUserType());
        }
        return false;
    }

    private void showAndAnswerWithImg() {
        int[] parentLayoutLocation = new int[2];
        ((View) itemView.getParent()).getLocationInWindow(parentLayoutLocation);
        PLVCopyBoardPopupWindow.showAndAnswer(msgParentLy, true, parentLayoutLocation[1], null, R.drawable.plvhc_chatroom_cp_ly_shape, R.drawable.plvhc_chatroom_inverted_triangle_layer, Color.parseColor("#333333"), new View.OnClickListener() {
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
                    ToastUtils.showLong("发送图片失败: " + t.getMessage());
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
                    ToastUtils.showLong("发送图片失败: " + sendValue);
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

            @Override
            public void onCheckFail(PLVSendLocalImgEvent localImgEvent, Throwable t) {
                localImgEvent.setSendStatus(PLVSendLocalImgEvent.SENDSTATUS_FAIL);
                if (localImgEvent == messageData) {
                    localImgStatus = localImgEvent.getSendStatus();
                    if (imgLoadingView != null) {
                        imgLoadingView.setVisibility(View.GONE);
                    }
                    ToastUtils.showLong("发送图片失败: " + t.getMessage());
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
                defaultAvatar = R.drawable.plvhc_chatroom_ic_teacher;
            } else if (PLVSocketUserConstant.USERTYPE_ASSISTANT.equals(userType)) {
                defaultAvatar = R.drawable.plvhc_chatroom_ic_assistant;
            } else if (PLVSocketUserConstant.USERTYPE_GUEST.equals(userType)) {
                defaultAvatar = R.drawable.plvhc_chatroom_ic_guest;
            } else {
                defaultAvatar = R.drawable.plvhc_chatroom_ic_viewer;
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
        String setNickName = nickName;
        if (nickName != null) {
            if (PLVSocketWrapper.getInstance().getLoginVO().getUserId().equals(userId)) {
                setNickName = nickName + "(我)";
            }
            if (actor != null) {
                setNickName = actor + "-" + setNickName;
            }
            if (nickTv != null) {
                nickTv.setText(setNickName);
            }
        }
        //设置发言文本信息
        if (speakMsg != null) {
            if (textMessageTv != null) {
                textMessageTv.setVisibility(View.VISIBLE);
                textMessageTv.setTextInner(speakMsg, isSpecialType);
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
            if (quoteNickTv != null) {
                quoteNickTv.setVisibility(View.VISIBLE);
                quoteNickTv.setText(nickName + ": ");
            }
            if (chatQuoteVO.isSpeakMessage()) {
                if (quoteTextMessageTv != null) {
                    quoteTextMessageTv.setVisibility(View.VISIBLE);
                    quoteTextMessageTv.setText(quoteSpeakMsg);
                }
            } else {
                if (quoteImgMessageIv != null && chatQuoteVO.getImage() != null) {
                    quoteImgMessageIv.setVisibility(View.VISIBLE);
                    fitChatImgWH((int) chatQuoteVO.getImage().getWidth(), (int) chatQuoteVO.getImage().getHeight(), quoteImgMessageIv, 40, 0);//适配图片视图的宽高
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
        if (quoteNickTv != null) {
            quoteNickTv.setVisibility(View.GONE);
        }
        if (quoteTextMessageTv != null) {
            quoteTextMessageTv.setVisibility(View.GONE);
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
            fitChatImgWH(chatImgWidth, chatImgHeight, imgMessageIv, 80, 0);//适配图片视图的宽高
            if (isLocalChatImg) {
                PLVImageLoader.getInstance().loadImage(chatImgUrl, imgMessageIv);
            } else {
                PLVImageLoader.getInstance().loadImage(
                        itemView.getContext(),
                        LOADIMG_MOUDLE_TAG,
                        LOADIMG_MOUDLE_TAG + messageData,
                        R.drawable.plvhc_chatroom_image_load_err,
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
                imgMessageIv.setImageResource(R.drawable.plvhc_chatroom_image_load_err);//fail can no set
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
