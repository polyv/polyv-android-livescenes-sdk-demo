package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.utils.PLVStringTruncator;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.livescenes.access.PLVChannelFeature;
import com.plv.livescenes.access.PLVChannelFeatureManager;
import com.plv.livescenes.chatroom.PLVViewerNameMaskMapper;
import com.plv.socket.event.commodity.PLVProductClickEvent;
import com.plv.socket.impl.PLVSocketManager;

public class PLVLCProductClickViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {
    private TextView productClickTipsTv;

    public PLVLCProductClickViewHolder(View itemView, PLVLCMessageAdapter adapter) {
        super(itemView, adapter);
        productClickTipsTv = itemView.findViewById(R.id.product_click_tips_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if (messageData instanceof PLVProductClickEvent) {
            PLVProductClickEvent productClickEvent = (PLVProductClickEvent) messageData;
            String nickName = maskViewerName(productClickEvent.getNickName());
            nickName = PLVStringTruncator.truncateToMax6ChineseWidth(nickName);
            String buyType = productClickEvent.isFinanceProduct() ? "正在选购" : productClickEvent.isPositionProduct() ? "正在投递" : "正在购买";
            String positionName = productClickEvent.getPositionName();
            String symbol = ">";
            // 1. 构造完整文本
            String fullText = String.format("%s %s %s %s", nickName, buyType, positionName, symbol);
            SpannableStringBuilder spannable = new SpannableStringBuilder(fullText);

            // 2. 设置 NickName 颜色（灰色）
            int nickNameEnd = nickName.length();
            spannable.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#ABABAE")), // 灰色
                    0,
                    nickNameEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // 3. 设置后续文字颜色（白色）并添加点击事件
            int actionStart = nickNameEnd + 1; // 加1跳过空格
            int actionEnd = fullText.length();

            // 设置白色
            spannable.setSpan(
                    new ForegroundColorSpan(Color.WHITE),
                    actionStart,
                    actionEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // 设置点击事件
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (productClickEvent.isFinanceProduct()) {
                        return;
                    }
                    adapter.callOnClickProductDetail(productClickEvent.getProductId());
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(Color.WHITE);
                }
            }, actionStart, actionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 4. 赋值给 TextView
            productClickTipsTv.setText(spannable);
            // 关键：必须设置这个才能响应点击
            productClickTipsTv.setMovementMethod(LinkMovementMethod.getInstance());
            // 防止点击时出现背景色
            productClickTipsTv.setHighlightColor(Color.TRANSPARENT);
        }
    }

    private String maskViewerName(String nickName) {
        PLVViewerNameMaskMapper mapper = PLVChannelFeatureManager.onChannel(PLVSocketManager.getInstance().getLoginRoomId())
                .getOrDefault(PLVChannelFeature.LIVE_VIEWER_NAME_MASK_TYPE, PLVViewerNameMaskMapper.KEEP_SOURCE);
        return mapper.invoke(
                nickName,
                userType,
                PLVSocketManager.getInstance().getLoginVO().getUserId().equals(userId)
        );
    }
}
