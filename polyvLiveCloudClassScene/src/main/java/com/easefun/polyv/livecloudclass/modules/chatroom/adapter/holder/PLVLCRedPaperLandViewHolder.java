package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.adapter.PLVLCMessageAdapter;
import com.easefun.polyv.livecommon.module.modules.chatroom.holder.PLVChatMessageBaseViewHolder;
import com.easefun.polyv.livecommon.module.modules.chatroom.model.enums.PLVRedPaperType;
import com.easefun.polyv.livecommon.module.utils.span.PLVRelativeImageSpan;
import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.foundationsdk.utils.PLVFormatUtils;
import com.plv.socket.event.redpack.PLVRedPaperEvent;
import com.plv.socket.event.redpack.PLVRedPaperHistoryEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCRedPaperLandViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {

    private TextView chatroomRedPaperContentTvLand;

    public PLVLCRedPaperLandViewHolder(View itemView, PLVLCMessageAdapter adapter) {
        super(itemView, adapter);
        initView();
    }

    private void initView() {
        chatroomRedPaperContentTvLand = itemView.findViewById(R.id.plvlc_chatroom_red_paper_content_tv_land);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        final PLVRedPaperEvent redPaperEvent;
        if (messageData instanceof PLVRedPaperEvent) {
            redPaperEvent = (PLVRedPaperEvent) messageData;
        } else if (messageData instanceof PLVRedPaperHistoryEvent) {
            redPaperEvent = ((PLVRedPaperHistoryEvent) messageData).asRedPaperEvent();
        } else {
            return;
        }

        final CharSequence text = new PLVSpannableStringBuilder()
                .appendExclude("[红包]", new PLVRelativeImageSpan(itemView.getContext().getResources().getDrawable(R.drawable.plvlc_chatroom_red_pack_icon), PLVRelativeImageSpan.ALIGN_CENTER) {// no need i18n
                    @Override
                    public Drawable getDrawable() {
                        final Drawable drawable = super.getDrawable();
                        drawable.setBounds(0, 0, ConvertUtils.dp2px(20), ConvertUtils.dp2px(20));
                        return drawable;
                    }
                })
                .append(PLVAppUtils.formatString(R.string.plv_red_paper_send_msg, redPaperEvent.getUser().getNick(), PLVRedPaperType.matchOrDefault(redPaperEvent.getType(), PLVRedPaperType.DEFAULT_RED_PAPER).getTypeName()))
                .appendExclude(PLVAppUtils.formatString(R.string.plv_red_paper_get), new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        adapter.callOnReceiveRedPaper(redPaperEvent);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(PLVFormatUtils.parseColor("#FF5459"));
                        ds.setUnderlineText(false);
                    }
                });
        chatroomRedPaperContentTvLand.setText(text);
        chatroomRedPaperContentTvLand.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
