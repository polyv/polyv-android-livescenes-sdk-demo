package com.easefun.polyv.livecommon.module.modules.interact;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;

/**
 * date: 2020/10/9
 * author: HWilliamgo
 * 针对互动应用封装的Interface，可以在各个场景使用，定义了：
 * 1. 外部直接调用的方法
 */
public interface IPLVInteractLayout {
    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">
    /**
     * 初始化
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 显示公告
     */
    void showBulletin();

    /**
     * 回调动态东
     * @param event
     */
    void onCallDynamicFunction(String event);

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
