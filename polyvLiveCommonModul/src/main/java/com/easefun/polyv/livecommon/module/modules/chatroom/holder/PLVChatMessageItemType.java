package com.easefun.polyv.livecommon.module.modules.chatroom.holder;

/**
 * 聊天信息item类型
 */
public class PLVChatMessageItemType {
    public static final int ITEMTYPE_UNDEFINED = 0;

    public static final int ITEMTYPE_SEND_SPEAK = 1;//自己发送的发言文本信息
    public static final int ITEMTYPE_RECEIVE_SPEAK = 2;//接收的发言文本信息

    public static final int ITEMTYPE_SEND_IMG = 3;//自己发送的图片信息
    public static final int ITEMTYPE_RECEIVE_IMG = 4;//接收的图片信息

    public static final int ITEMTYPE_SEND_QUIZ = 5;//自己发送的提问信息
    public static final int ITEMTYPE_RECEIVE_QUIZ = 6;//接收的回答信息

    public static final int ITEMTYPE_CUSTOM_GIFT = 100;//自定义打赏礼物信息
}
