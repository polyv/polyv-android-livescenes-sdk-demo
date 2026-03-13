package com.easefun.polyv.livecommon.module.modules.interact;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;

/**
 * date: 2020/10/9
 * author: HWilliamgo
 * 针对互动应用封装的Interface，可以在各个场景使用，定义了：
 * 1. 外部直接调用的方法
 */
public interface IPLVIARLayout {
    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">

    /**
     * 初始化
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager, @Nullable PLVLiveScene scene);

    /**
     * 显示我的奖励
     */
    void showLotteryRecord();

    /**
     * 销毁
     */
    void destroy();

    /**
     * 点击返回
     *
     * @return 返回true表示拦截事件
     */
    boolean onBackPress();
    // </editor-fold>
}
