package com.easefun.polyv.livecloudclass.modules.media.danmu;

import androidx.annotation.Nullable;

import com.plv.socket.event.chat.PLVChatQuoteVO;

/**
 * date: 2019/6/6 0006
 *
 * @author hwj
 * description 信息发送器抽象
 */
public interface IPLVLCLandscapeMessageSender {

    /**
     * 设置发送信息监听器
     *
     * @param listener listener
     */
    void setOnSendMessageListener(OnSendMessageListener listener);

    /**
     * 打开信息发送器
     */
    void openMessageSender();


    /**
     * 隐藏消息发送器
     */
    void hideMessageSender();

    /**
     * 隐藏
     */
    void dismiss();

    /**
     * 发送信息监听器
     */
    interface OnSendMessageListener {
        /**
         * 横屏发送的消息应同步到聊天室
         *
         * @param message 消息
         */
        void onSend(String message, @Nullable PLVChatQuoteVO chatQuoteVO);

        /**
         * 获取当前引用消息
         */
        @Nullable
        PLVChatQuoteVO getChatQuoteContent();

        /**
         * 回调 取消引用消息
         */
        void onCloseChatQuote();
    }
}
