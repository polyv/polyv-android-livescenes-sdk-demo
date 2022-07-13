package com.easefun.polyv.livecommon.module.modules.interact;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.config.PLVLiveScene;
import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.plv.socket.event.interact.PLVShowPushCardEvent;

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
     * 初始化
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager, @Nullable PLVLiveScene scene);

    /**
     * 设置打开链接所需的参数监听器
     */
    void setOnOpenInsideWebViewListener(PLVInteractLayout2.OnOpenInsideWebViewListener listener);

    /**
     * 显示公告
     */
    void showBulletin();

    /**
     * 显示卡片推送
     */
    void showCardPush(PLVShowPushCardEvent showPushCardEvent);

    /**
     * 回调动态东
     *
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
