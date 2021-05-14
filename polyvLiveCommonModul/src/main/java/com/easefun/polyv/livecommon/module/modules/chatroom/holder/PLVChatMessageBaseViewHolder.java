package com.easefun.polyv.livecommon.module.modules.chatroom.holder;

import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.livecommon.module.modules.chatroom.PLVCustomGiftEvent;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.adapter.PLVBaseAdapter;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livescenes.chatroom.PolyvLocalMessage;
import com.easefun.polyv.livescenes.chatroom.PolyvQuestionMessage;
import com.easefun.polyv.livescenes.chatroom.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.livescenes.socket.PolyvSocketWrapper;
import com.plv.socket.event.chat.PLVChatImgContent;
import com.plv.socket.event.chat.PLVChatImgEvent;
import com.plv.socket.event.chat.PLVChatQuoteVO;
import com.plv.socket.event.chat.PLVSpeakEvent;
import com.plv.socket.event.chat.PLVTAnswerEvent;
import com.plv.socket.event.history.PLVChatImgHistoryEvent;
import com.plv.socket.event.history.PLVSpeakHistoryEvent;
import com.plv.socket.net.model.PLVSocketLoginVO;
import com.plv.socket.user.PLVAuthorizationBean;
import com.plv.socket.user.PLVSocketUserBean;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 聊天信息基础viewHolder
 */
public class PLVChatMessageBaseViewHolder<Data extends PLVBaseViewData, Adapter extends PLVBaseAdapter>
        extends PLVBaseViewHolder<Data, Adapter> {
    private int msgIndex;//信息索引

    protected Object messageData;//信息数据
    protected String userType;//用户类型
    protected String nickName;//昵称
    protected String userId;//聊天室用户id
    protected String actor;//头衔
    protected String fColor;//头衔前景色
    protected String bgColor;//头衔背景色
    protected String avatar;//头像url
    protected CharSequence speakMsg;//文本发言信息

    protected String chatImgUrl;//图片发言信息的url //vh self cache
    protected int chatImgWidth;
    protected int chatImgHeight;//图片的宽、高
    protected boolean isLocalChatImg;//是否是本地的图片发言信息
    protected int localImgProgress;//本地图片发送进度，不能重置
    protected int localImgStatus;//本地图片发送状态，不能重置

    protected PLVChatQuoteVO chatQuoteVO;//被回复人的信息
    protected CharSequence quoteSpeakMsg;//被回复人发送的文本信息

    public PLVChatMessageBaseViewHolder(View itemView, Adapter adapter) {
        super(itemView, adapter);
    }

    //设置信息索引
    //由于presenter对应多个view，如果每个view显示的信息的文本大小(用来生成表情图标大小)不一致，在presenter中就会保存多个不同文本大小的信息
    //因此需要用索引获取对应文本大小的信息
    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    private void resetParams() {
        userType = null;
        nickName = null;
        speakMsg = null;
        userId = null;
        actor = null;
        fColor = PLVAuthorizationBean.FCOLOR_DEFAULT;
        bgColor = PLVAuthorizationBean.BGCOLOR_DEFAULT;
        avatar = null;
        chatImgUrl = null;
        chatImgWidth = 0;
        chatImgHeight = 0;
        isLocalChatImg = false;
        chatQuoteVO = null;
        quoteSpeakMsg = null;
    }

    @Override
    public void processData(Data data, int position) {
        super.processData(data, position);
        messageData = data.getData();
        resetParams();
        if (messageData instanceof PLVSpeakEvent) {//接收的发言事件信息
            PLVSpeakEvent speakEvent = (PLVSpeakEvent) messageData;
            fillFieldFromUser(speakEvent.getUser());
            int validIndex = Math.min(speakEvent.getObjects().length - 1, msgIndex);
            speakMsg = (CharSequence) speakEvent.getObjects()[validIndex];
            chatQuoteVO = speakEvent.getQuote();
            if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                quoteSpeakMsg = (CharSequence) chatQuoteVO.getObjects()[validIndex];
            }
        } else if (messageData instanceof PolyvLocalMessage) {//本地的发言事件信息
            fillFieldFromLoginVO(PolyvSocketWrapper.getInstance().getLoginVO());
            int validIndex = Math.min(((PolyvLocalMessage) messageData).getObjects().length - 1, msgIndex);
            speakMsg = (CharSequence) ((PolyvLocalMessage) messageData).getObjects()[validIndex];
            chatQuoteVO = ((PolyvLocalMessage) messageData).getQuote();
            if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                quoteSpeakMsg = (CharSequence) chatQuoteVO.getObjects()[validIndex];
            }
        } else if (messageData instanceof PLVChatImgEvent) {//接收的图片事件信息
            PLVChatImgEvent chatImgEvent = (PLVChatImgEvent) messageData;
            fillFieldFromUser(chatImgEvent.getUser());
            if (chatImgEvent.getValues() != null && chatImgEvent.getValues().size() > 0) {
                PLVChatImgContent valuesBean = chatImgEvent.getValues().get(0);
                if (valuesBean != null) {
                    chatImgUrl = valuesBean.getUploadImgUrl();
                    if (valuesBean.getSize() != null) {
                        chatImgWidth = (int) valuesBean.getSize().getWidth();
                        chatImgHeight = (int) valuesBean.getSize().getHeight();
                    }
                }
            }
        } else if (messageData instanceof PolyvSendLocalImgEvent) {//本地的发送图片事件信息
            isLocalChatImg = true;
            PolyvSendLocalImgEvent localImgEvent = (PolyvSendLocalImgEvent) messageData;
            fillFieldFromLoginVO(PolyvSocketWrapper.getInstance().getLoginVO());
            chatImgUrl = localImgEvent.getImageFilePath();
            chatImgWidth = localImgEvent.getWidth();
            chatImgHeight = localImgEvent.getHeight();
        } else if (messageData instanceof PolyvQuestionMessage) {//本地的提问信息
            fillFieldFromLoginVO(PolyvSocketWrapper.getInstance().getLoginVO());
            int validIndex = Math.min(((PolyvQuestionMessage) messageData).getObjects().length - 1, msgIndex);
            speakMsg = (CharSequence) ((PolyvQuestionMessage) messageData).getObjects()[validIndex];
        } else if (messageData instanceof PLVTAnswerEvent) {//接收的回答信息
            PLVTAnswerEvent tAnswerEvent = (PLVTAnswerEvent) messageData;
            fillFieldFromUser(tAnswerEvent.getUser());
            int validIndex = Math.min(tAnswerEvent.getObjects().length - 1, msgIndex);
            speakMsg = (CharSequence) tAnswerEvent.getObjects()[validIndex];
        } else if (messageData instanceof PLVSpeakHistoryEvent) {//历史发言信息
            PLVSpeakHistoryEvent speakHistoryEvent = (PLVSpeakHistoryEvent) messageData;
            fillFieldFromUser(speakHistoryEvent.getUser());
            int validIndex = Math.min(speakHistoryEvent.getObjects().length - 1, msgIndex);
            speakMsg = (CharSequence) speakHistoryEvent.getObjects()[validIndex];
            chatQuoteVO = speakHistoryEvent.getQuote();
            if (chatQuoteVO != null && chatQuoteVO.isSpeakMessage()) {
                quoteSpeakMsg = (CharSequence) chatQuoteVO.getObjects()[validIndex];
            }
        } else if (messageData instanceof PLVChatImgHistoryEvent) {//历史图片信息
            PLVChatImgHistoryEvent chatImgHistoryEvent = (PLVChatImgHistoryEvent) messageData;
            fillFieldFromUser(chatImgHistoryEvent.getUser());
            PLVChatImgContent chatImgContent = chatImgHistoryEvent.getContent();
            if (chatImgContent != null) {
                chatImgUrl = chatImgContent.getUploadImgUrl();
                if (chatImgContent.getSize() != null) {
                    chatImgWidth = (int) chatImgContent.getSize().getWidth();
                    chatImgHeight = (int) chatImgContent.getSize().getHeight();
                }
            }
        } else if (messageData instanceof PLVCustomGiftEvent) {//自定义打赏礼物信息
            speakMsg = ((PLVCustomGiftEvent) messageData).getSpan();
        }
    }

    private void fillFieldFromLoginVO(PLVSocketLoginVO loginVO) {
        if (loginVO != null) {
            userType = loginVO.getUserType();
            nickName = loginVO.getNickName();
            userId = loginVO.getUserId();
            avatar = loginVO.getAvatarUrl();
            actor = loginVO.getActor();
            PLVAuthorizationBean authorizationBean = loginVO.getAuthorization();
            if (authorizationBean != null) {
                actor = authorizationBean.getActor();
                fColor = authorizationBean.getfColor();
                bgColor = authorizationBean.getBgColor();
            }
        }
    }

    private void fillFieldFromUser(PLVSocketUserBean userBean) {
        if (userBean != null) {
            userType = userBean.getUserType();
            nickName = userBean.getNick();
            userId = userBean.getUserId();
            actor = userBean.getActor();
            avatar = userBean.getPic();
            PLVAuthorizationBean authorizationBean = userBean.getAuthorization();
            if (authorizationBean != null) {
                actor = authorizationBean.getActor();
                fColor = authorizationBean.getfColor();
                bgColor = authorizationBean.getBgColor();
            }
        }
    }

    public static void fitChatImgWH(int width, int height, View view, int maxLengthDp, int minLengthDp) {
        int maxLength = ConvertUtils.dp2px(maxLengthDp);
        int minLength = ConvertUtils.dp2px(minLengthDp);
        //计算显示的图片大小
        float percentage = width * 1f / height;
        if (percentage == 1) {//方图
            if (width < minLength) {
                width = height = minLength;
            } else if (width > maxLength) {
                width = height = maxLength;
            }
        } else if (percentage < 1) {//竖图
            height = maxLength;
            width = (int) Math.max(minLength, height * percentage);
        } else {//横图
            width = maxLength;
            height = (int) Math.max(minLength, width / percentage);
        }
        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        vlp.width = width;
        vlp.height = height;
        view.setLayoutParams(vlp);
    }
}
