package com.easefun.polyv.livecommon.module.modules.interact;

import android.content.Intent;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;

public interface IPLVStreamerInteractLayout {
    // <editor-fold defaultstate="collapsed" desc="1. 外部直接调用的方法">
    /**
     * 初始化
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager);

    /**
     * 显示签到
     */
    void showSignIn();

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

    /**
     * ActivityResult回调触发时调用
     */
    void onActivityResult(final int requestCode, final int resultCode, final Intent intent);
    // </editor-fold>
}
