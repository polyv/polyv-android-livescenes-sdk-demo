package com.easefun.polyv.livecloudclass.modules.liveroom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.livecloudclass.modules.chatroom.widget.PLVLCLikeIconView;

/**
 * date: 2020/9/8
 * author: HWilliamgo
 * description: 横屏播放器控制器
 * 云课堂场景下定义的横屏播放器控制器
 * 定义了：
 * 1. 外部直接调用的方法
 */
public interface IPLVLiveLandscapePlayerController {

    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">

    /**
     * 获取横屏控制器根布局
     */
    ViewGroup getLandRoot();

    /**
     * 获取渐变条
     */
    View getGradientBar();

    /**
     * 获取返回按钮
     */
    ImageView getBackView();

    /**
     * 获取直播名称view
     */
    TextView getNameView();

    /**
     * 获取观看热度view
     */
    TextView getViewerCountView();

    /**
     * 获取打开公告按钮
     */
    ImageView getBulletinView();

    /**
     * 获取弹幕开关按钮
     */
    ImageView getDanmuSwitchView();

    /**
     * 获取点赞view
     */
    PLVLCLikeIconView getLikesView();

    /**
     * 获取暂停按钮
     */
    ImageView getPauseView();

    /**
     * 获取刷新按钮
     */
    ImageView getRefreshView();

    /**
     * 获取切换按钮
     */
    ImageView getSwitchView();

    /**
     * 获取信息发送器
     */
    TextView getMessageSender();

    /**
     * 获取更多按钮
     */
    ImageView getMoreView();

    /**
     * 显示
     */
    void show();

    /**
     * 隐藏
     */
    void hide();
    // </editor-fold>

}
