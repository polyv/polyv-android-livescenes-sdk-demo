package com.easefun.polyv.liveecommerce.modules.chatroom;

import static com.plv.foundationsdk.component.exts.StringExtKt.getString;
import static com.plv.foundationsdk.utils.PLVFormatUtils.parseColor;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.span.PLVSpannableStringBuilder;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectSpan;
import com.easefun.polyv.liveecommerce.R;
import com.plv.socket.event.chat.PLVChatNoticeEvent;

/**
 * @author Hoshiiro
 */
public class PLVECChatNoticeViewHolder extends PLVECChatMessageCommonViewHolder<PLVBaseViewData, PLVECChatMessageAdapter> {

    private TextView chatroomNoticeTv;

    public PLVECChatNoticeViewHolder(View itemView, PLVECChatMessageAdapter adapter) {
        super(itemView, adapter);
        chatroomNoticeTv = itemView.findViewById(R.id.plvec_chatroom_notice_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        final PLVChatNoticeEvent event = (PLVChatNoticeEvent) data.getData();
        final CharSequence display = new PLVSpannableStringBuilder()
                .appendExclude(getString(R.string.plv_live_notify), new PLVRoundRectSpan()
                        .textSize(10)
                        .textColor(Color.WHITE)
                        .paddingLeft(6)
                        .paddingRight(6)
                        .radius(20)
                        .backgroundColor(parseColor("#33FFFFFF"))
                        .marginRight(4)
                )
                .append(event.getContent());
        chatroomNoticeTv.setText(display);
    }
}
