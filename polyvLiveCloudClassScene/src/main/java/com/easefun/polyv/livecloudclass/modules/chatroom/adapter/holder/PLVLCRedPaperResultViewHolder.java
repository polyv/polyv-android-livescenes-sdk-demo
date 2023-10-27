package com.easefun.polyv.livecloudclass.modules.chatroom.adapter.holder;

import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;
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
import com.plv.socket.event.redpack.PLVRedPaperResultEvent;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * @author Hoshiiro
 */
public class PLVLCRedPaperResultViewHolder extends PLVChatMessageBaseViewHolder<PLVBaseViewData, PLVLCMessageAdapter> {

    private TextView chatroomRedPaperResultTv;

    public PLVLCRedPaperResultViewHolder(View itemView, PLVLCMessageAdapter adapter) {
        super(itemView, adapter);
        initView();
    }

    private void initView() {
        chatroomRedPaperResultTv = itemView.findViewById(R.id.plvlc_chatroom_red_paper_result_tv);
    }

    @Override
    public void processData(PLVBaseViewData data, int position) {
        super.processData(data, position);
        if (!(messageData instanceof PLVRedPaperResultEvent)) {
            return;
        }
        final PLVRedPaperResultEvent redPaperResultEvent = (PLVRedPaperResultEvent) messageData;
        final PLVSpannableStringBuilder spannableStringBuilder = new PLVSpannableStringBuilder()
                .appendExclude("[红包]", new PLVRelativeImageSpan(itemView.getContext().getResources().getDrawable(R.drawable.plvlc_chatroom_red_pack_receive_result_icon), PLVRelativeImageSpan.ALIGN_CENTER) {// no need i18n
                    @Override
                    public Drawable getDrawable() {
                        final Drawable drawable = super.getDrawable();
                        drawable.setBounds(0, 0, ConvertUtils.dp2px(14), ConvertUtils.dp2px(14));
                        return drawable;
                    }
                })
                .append(PLVAppUtils.formatString(R.string.plv_red_paper_receive_msg, redPaperResultEvent.getNick(), PLVRedPaperType.matchOrDefault(redPaperResultEvent.getType(), PLVRedPaperType.DEFAULT_RED_PAPER).getTypeName()))
                .appendExclude(PLVAppUtils.getString(R.string.plv_red_paper_name), new ForegroundColorSpan(PLVFormatUtils.parseColor("#FF5353")));
        if (redPaperResultEvent.isRedPaperRunOut()) {
            spannableStringBuilder.append(PLVAppUtils.getString(R.string.plv_red_paper_run_out_2));
        }
        chatroomRedPaperResultTv.setText(spannableStringBuilder);
    }
}
