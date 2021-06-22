package com.easefun.polyv.livecommon.module.modules.chatroom;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.R;
import com.plv.socket.event.PLVBaseEvent;
import com.plv.socket.event.PLVMessageBaseEvent;
import com.plv.socket.event.chat.PLVRewardEvent;

/**
 * (自定义)打赏礼物事件，用于聊天室列表显示礼物信息
 */
public class PLVCustomGiftEvent extends PLVBaseEvent {
    private SpannableStringBuilder span;
    private String giftImg;
    private int giftDrawableId;

    public PLVCustomGiftEvent(SpannableStringBuilder span) {
        this.span = span;
    }

    public SpannableStringBuilder getSpan() {
        return span;
    }

    public String getGiftImg() {
        return giftImg;
    }

    public void setGiftImg(String giftImg) {
        this.giftImg = giftImg;
    }

    public int getGiftDrawableId() {
        return giftDrawableId;
    }

    public void setGiftDrawableId(int giftDrawableId) {
        this.giftDrawableId = giftDrawableId;
    }

    public static PLVCustomGiftEvent generateCustomGiftEvent(PLVRewardEvent rewardEvent) {
        String uNick = rewardEvent.getContent().getUnick();
        String gImg = rewardEvent.getContent().getGimg();
        boolean isGift = !TextUtils.isEmpty(gImg);
        String rewardContent = rewardEvent.getContent().getRewardContent();
        if (!isGift) {
            try {
                Double.parseDouble(rewardContent);
                rewardContent = rewardContent + "元";
            } catch (Exception e) {
            }
        }
        SpannableStringBuilder span = new SpannableStringBuilder(uNick + (isGift ? " 赠送了 " : " 打赏 ") + rewardContent + " ");
        PLVCustomGiftEvent customGiftEvent = new PLVCustomGiftEvent(span);
        customGiftEvent.setGiftImg(gImg);
        if (!isGift) {
            customGiftEvent.setGiftDrawableId(R.drawable.plv_icon_money);
        }
        return customGiftEvent;
    }

    @Override
    public String getEVENT() {
        return PLVRewardEvent.EVENT;
    }

    @Override
    public String getListenEvent() {
        return PLVMessageBaseEvent.LISTEN_EVENT;
    }
}
