package com.easefun.polyv.livecloudclass.modules.pagemenu.previous.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.plv.livescenes.model.PLVPlaybackListVO;

/**
 * 回放章节的ViewHolder
 */
public class PLVLCPreviousViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVLCPreviousAdapter> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVPlaybackListVO.DataBean.ContentsBean contentBean;
    private ImageView previousCoverIv;
    private TextView titleTv;
    private TextView startTimeTv;
    private TextView durationTv;
    private TextView maskTv;
    private View itemView;
    private Context context;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCPreviousViewHolder(View itemView, final PLVLCPreviousAdapter adapter) {
        super(itemView, adapter);
        previousCoverIv = findViewById(R.id.plvlc_previous_item_cover_Im);
        titleTv = findViewById(R.id.plvlc_previous_item_title_tv);
        startTimeTv = findViewById(R.id.plvlc_previous_item_startTime_tv);
        durationTv = findViewById(R.id.plvlc_previous_item_duration_tv);
        maskTv = findViewById(R.id.plvlc_previous_item_mask);
        context = itemView.getContext();
        this.itemView = itemView;

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据处理">
    @Override
    public void processData(PLVBaseViewData data, final int position) {
        super.processData(data, position);
        contentBean = (PLVPlaybackListVO.DataBean.ContentsBean) data.getData();
        PLVImageLoader.getInstance().loadImage(contentBean.getFirstImage(), previousCoverIv);
        titleTv.setText(contentBean.getTitle());
        durationTv.setText(contentBean.getDuration());
        String startTime = formatDateUtil(contentBean.getStartTime());
        startTimeTv.setText(startTime);
        if (adapter.getCurrentPosition() == position) {
            maskTv.setVisibility(View.VISIBLE);
            titleTv.setTextColor(context.getResources().getColor(R.color.colorPortage));
            startTimeTv.setTextColor(context.getResources().getColor(R.color.colorPortage));
        } else {
            maskTv.setVisibility(View.GONE);
            titleTv.setTextColor(context.getResources().getColor(R.color.colorSpunPearl));
            startTimeTv.setTextColor(context.getResources().getColor(R.color.colorSoftSpunPearl));
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callChangeVideoVidClick(v, contentBean, position);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    /**
     * 日期格式转换
     * yyyyMMddHHmmss -> yyyy-MM-DD HH:mm
     *
     * @param startTime
     * @return
     */
    private String formatDateUtil(String startTime) {
        StringBuffer buffer = new StringBuffer();
        if (startTime.length() >= 12) {
            buffer.append(startTime, 0, 4);
            buffer.append("-");
            buffer.append(startTime, 4, 6);
            buffer.append("-");
            buffer.append(startTime, 6, 8);
            buffer.append(" ");
            buffer.append(startTime, 8, 10);
            buffer.append(":");
            buffer.append(startTime, 10, 12);
            return buffer.toString();
        } else {
            return startTime;
        }
    }
    // </editor-fold>
}
