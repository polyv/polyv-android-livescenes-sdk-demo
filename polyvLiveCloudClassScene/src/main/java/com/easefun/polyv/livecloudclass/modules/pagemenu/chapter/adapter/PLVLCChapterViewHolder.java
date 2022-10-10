package com.easefun.polyv.livecloudclass.modules.pagemenu.chapter.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.plv.livescenes.previous.model.PLVChapterDataVO;

/**
 * Author:lzj
 * Time:2021/12/29
 * Description: 直播三分屏场景章节的ViewHolder
 */
public class PLVLCChapterViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVLCChapterAdapter> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private PLVChapterDataVO chapterDataVO;
    private ImageView chapterStatusIm;
    private TextView titleTv;
    private TextView timeTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVLCChapterViewHolder(View itemView, PLVLCChapterAdapter adapter) {
        super(itemView, adapter);
        chapterStatusIm = findViewById(R.id.plvlc_chapter_item_status_im);
        titleTv = findViewById(R.id.plvlc_chapter_item_title_tv);
        timeTv = findViewById(R.id.plvlc_chapter_item_time_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据处理">
    @Override
    public void processData(PLVBaseViewData data, final int position) {
        super.processData(data, position);
        chapterDataVO = (PLVChapterDataVO) data.getData();
        if (adapter.getCurrentPosition() == position) {
            chapterStatusIm.setImageResource(R.drawable.plvlc_chapter_playing_icon);
            timeTv.setTextColor(itemView.getResources().getColor(R.color.colorMalibu));
            titleTv.setTextColor(itemView.getResources().getColor(R.color.colorMalibu));
        } else {
            chapterStatusIm.setImageResource(R.drawable.plvlc_chapter_play_icon);
            timeTv.setTextColor(itemView.getResources().getColor(R.color.colorSpunPearl));
            titleTv.setTextColor(itemView.getResources().getColor(R.color.colorSpunPearl));
        }
        timeTv.setText(timeFormat(String.valueOf(chapterDataVO.getTimeStamp())));
        titleTv.setText(chapterDataVO.getTitle());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.callChangeVideoSeekClick(chapterDataVO.getTimeStamp(), position);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    /**
     * 格式化时间 66s -> 00:01:06
     *
     * @param timeStamp 时间
     * @return
     */
    private String timeFormat(String timeStamp) {
        int time = Integer.parseInt(timeStamp);
        int second = time % 60;
        int min = time / 60 % 60;
        int hour = time / 3600;
        String s = String.format("%02d", second);
        String m = String.format("%02d", min);
        String h = String.format("%02d", hour);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(h);
        stringBuffer.append(":");
        stringBuffer.append(m);
        stringBuffer.append(":");
        stringBuffer.append(s);
        return stringBuffer.toString();
    }
    // </editor-fold>
}
