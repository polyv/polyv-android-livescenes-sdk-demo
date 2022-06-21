package com.easefun.polyv.liveecommerce.modules.playback.fragments.previous;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.liveecommerce.R;
import com.plv.livescenes.model.PLVPlaybackListVO;

/**
 * 纯视频-往期视频列表的ViewHolder
 */
public class PLVECPreviousViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVECPreviousAdapter> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVRoundImageView coverIv;
    private TextView durationTv;
    private TextView titleTv;
    private TextView maskTv;
    private Context context;

    private PLVPlaybackListVO.DataBean.ContentsBean contentBean;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造函数">
    public PLVECPreviousViewHolder(View itemView, PLVECPreviousAdapter adapter) {
        super(itemView, adapter);
        coverIv = findViewById(R.id.plvec_previous_item_im);
        durationTv = findViewById(R.id.plvec_previous_item_time_tv);
        titleTv = findViewById(R.id.plvec_previous_item_title_tv);
        maskTv = findViewById(R.id.plvec_playback_item_mask);
        context = itemView.getContext();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="往期视频数据解析处理">
    @Override
    public void processData(PLVBaseViewData data, final int position) {
        super.processData(data, position);
        contentBean = (PLVPlaybackListVO.DataBean.ContentsBean) data.getData();
        PLVImageLoader.getInstance().loadImage(contentBean.getFirstImage(), coverIv);
        titleTv.setText(contentBean.getTitle());
        durationTv.setText(contentBean.getDuration());
        if (adapter.getCurrentPosition() == position) {
            maskTv.setVisibility(View.VISIBLE);
            titleTv.setTextColor(context.getResources().getColor(R.color.plvec_playback_more_video_text_color));
        } else {
            maskTv.setVisibility(View.GONE);
            titleTv.setTextColor(context.getResources().getColor(R.color.plvec_playback_item_title_color));

        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callChangeVideoVidClick(v, contentBean, position);
            }
        });
    }
    // </editor-fold>
}
