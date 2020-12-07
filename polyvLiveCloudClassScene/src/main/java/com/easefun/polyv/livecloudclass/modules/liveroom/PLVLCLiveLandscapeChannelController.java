package com.easefun.polyv.livecloudclass.modules.liveroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.R;
import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;

/**
 * date: 2020/9/8
 * author: HWilliamgo
 * description: 云课堂场景下定义的横屏播放器控制器
 */
public class PLVLCLiveLandscapeChannelController extends FrameLayout implements IPLVLiveLandscapePlayerController {

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVLCLiveLandscapeChannelController(@NonNull Context context) {
        this(context, null);
    }

    public PLVLCLiveLandscapeChannelController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVLCLiveLandscapeChannelController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plvlc_live_land_channel_controller, this, true);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 1. 外部直接调用的方法">
    @Override
    public ViewGroup getLandRoot() {
        return findViewById(R.id.video_controller_land_ly);
    }

    @Override
    public View getGradientBar() {
        return findViewById(R.id.gradient_bar_land_ly);
    }

    @Override
    public ImageView getBackView() {
        return findViewById(R.id.back_land_iv);
    }

    @Override
    public TextView getNameView() {
        return findViewById(R.id.video_name_land_tv);
    }

    @Override
    public TextView getViewerCountView() {
        return findViewById(R.id.video_viewer_count_land_tv);
    }

    @Override
    public ImageView getBulletinView() {
        return findViewById(R.id.bulletin_land_iv);
    }

    @Override
    public ImageView getDanmuSwitchView() {
        return findViewById(R.id.danmu_switch_land_iv);
    }

    @Override
    public PLVLCLikeIconView getLikesView() {
        return findViewById(R.id.likes_land_iv);
    }

    @Override
    public ImageView getPauseView() {
        return findViewById(R.id.video_pause_land_iv);
    }

    @Override
    public ImageView getRefreshView() {
        return findViewById(R.id.video_refresh_land_iv);
    }

    @Override
    public ImageView getSwitchView() {
        return findViewById(R.id.video_ppt_switch_land_iv);
    }

    @Override
    public TextView getMessageSender() {
        return findViewById(R.id.start_send_message_land_tv);
    }

    @Override
    public ImageView getMoreView() {
        return findViewById(R.id.more_land_iv);
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(GONE);
    }
    // </editor-fold>

}
