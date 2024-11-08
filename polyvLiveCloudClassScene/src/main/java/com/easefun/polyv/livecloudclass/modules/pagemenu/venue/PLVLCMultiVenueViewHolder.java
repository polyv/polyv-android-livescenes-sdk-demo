package com.easefun.polyv.livecloudclass.modules.pagemenu.venue;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecommon.module.modules.venue.enums.PLVVenueStatusEnum;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVImageLoader;
import com.easefun.polyv.livecommon.ui.widget.PLVRoundRectGradientTextView;
import com.easefun.polyv.livecommon.ui.widget.itemview.PLVBaseViewData;
import com.easefun.polyv.livecommon.ui.widget.itemview.holder.PLVBaseViewHolder;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundImageView;
import com.easefun.polyv.livecommon.ui.widget.roundview.PLVRoundRectConstraintLayout;
import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.feature.venues.model.PLVVenueDataVO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PLVLCMultiVenueViewHolder extends PLVBaseViewHolder<PLVBaseViewData, PLVLCMultiVenueAdapter> {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String DEFAULT_COVER_IMAGE = "https://s1.videocc.net/default-img/channel/default-splash.png";
    private static final String PLAYBACK = "playback";

    private String mainChannelId;
    private PLVRoundRectConstraintLayout coverRy;
    private PLVRoundImageView coverIv;
    private PLVRoundRectGradientTextView coverStatusTv;
    private PLVRoundRectConstraintLayout coverSelectLayout;
    private ImageView coverSelectIv;
    private TextView coverSelectTv;

    private TextView titleTv;
    private TextView timeTv;
    private TextView countTv;

    private View itemSelectBgView;

    private View itemView;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">

    public PLVLCMultiVenueViewHolder(View itemView, PLVLCMultiVenueAdapter adapter) {
        super(itemView, adapter);
        this.mainChannelId = adapter.getMainChannelId();
        coverSelectLayout = findViewById(R.id.plvlc_venue_item_cover_select_rl);
        coverSelectTv = findViewById(R.id.plvlc_venue_item_cover_select_tv);
        coverSelectIv = findViewById(R.id.plvlc_venue_item_cover_select_iv);
        itemSelectBgView = findViewById(R.id.plvlc_venue_item_cover_select_bg);

        coverIv = findViewById(R.id.plvlc_venue_item_cover_image_iv);
        coverRy = findViewById(R.id.plvlc_venue_item_cover_cl);
        coverStatusTv = findViewById(R.id.plvlc_venue_item_cover_status_tv);

        titleTv = findViewById(R.id.plvlc_venue_item_title_tv);
        timeTv = findViewById(R.id.plvlc_venue_item_time_tv);
        countTv = findViewById(R.id.plvlc_venue_item_watch_count_tv);
        this.itemView = itemView;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据处理">

    @Override
    public void processData(final PLVBaseViewData data, int position) {
        super.processData(data, position);
        final PLVVenueDataVO venue = (PLVVenueDataVO) data.getData();
        final String channelId = venue.getChannelId().toString();
        if (channelId.equals(PolyvLiveSDKClient.getInstance().getChannelId())) {
            itemSelectBgView.setEnabled(true);
            coverSelectLayout.setVisibility(View.VISIBLE);
            if (venue.getLiveStatusDesc() != null) {
                coverSelectTv.setText(venue.getLiveStatusDesc());
            }
        } else {
            itemSelectBgView.setEnabled(false);
            coverSelectLayout.setVisibility(View.GONE);
        }
        String title = venue.getMultiMeetingName();
        if (channelId.equals(mainChannelId)) {
            title =  PLVAppUtils.getString(R.string.plv_multi_venue_main_title) + title;
        }
        titleTv.setText(title);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPlayback = venue.getLiveStatus().equals(PLAYBACK);
                adapter.callChangeVenueClick(channelId, isPlayback);
            }
        });
        PLVVenueStatusEnum status = PLVVenueStatusEnum.DEFAULT_STATUS;
        try {
            status = PLVVenueStatusEnum.valueOf(venue.getLiveStatus());
        } catch (Exception e) {
        }
        int[] colors = {status.getColor().getFirst(), status.getColor().getSecond()};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        coverStatusTv.setBackground(gradientDrawable);

        coverStatusTv.setText(status.getStatusDesc());

        String coverImage = DEFAULT_COVER_IMAGE;
        if (venue.getSplashImg() != null) {
            if (venue.getSplashImg().startsWith("//")) {
                coverImage = "https:" + venue.getSplashImg();
            }
        }
        PLVImageLoader.getInstance().loadImage(coverImage, coverIv);
        timeTv.setText(formatDateUtil(venue.getStartTime()));
        countTv.setText(venue.getPv() + "");

        if (itemSelectBgView.isEnabled() &&
                (status == PLVVenueStatusEnum.live || status == PLVVenueStatusEnum.playback)) {
            coverSelectTv.setText(R.string.plv_multi_venue_select_status_playing);
            coverSelectIv.setVisibility(View.VISIBLE);
        } else {
            coverSelectTv.setText(R.string.plv_multi_venue_select_status_no_start);
            coverSelectIv.setVisibility(View.GONE);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部方法">

    /**
     * 日期格式转换
     * xxx -> yyyy/mm/dd hh:mm:ss
     * @param timestamp
     * @return
     */
    private String formatDateUtil(long timestamp) {
        if (timestamp == 0) {
            return "--";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
    // </editor-fold>


}
