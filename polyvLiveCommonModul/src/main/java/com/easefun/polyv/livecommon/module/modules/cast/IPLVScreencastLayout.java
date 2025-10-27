package com.easefun.polyv.livecommon.module.modules.cast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.live.contract.IPLVLivePlayerContract;

import net.polyv.android.media.cast.model.vo.PLVMediaCastDevice;

public interface IPLVScreencastLayout {

    // <editor-fold defaultstate="collapsed" desc="公开的方法">

    /**
     * 设置mvp模式中的直播播放器presenter
     *
     * @param livePlayerPresenter 直播播放器presenter
     */
    void init(IPLVLiveRoomDataManager liveRoomDataManager, @NonNull IPLVLivePlayerContract.ILivePlayerPresenter livePlayerPresenter);


    /**
     * 显示投屏状态Layout
     *
     * @param info
     */
    void show(@Nullable PLVMediaCastDevice info);

    /**
     * 隐藏投屏状态layout
     */
    void hide();

    /**
     * 搜索投屏设备，显示搜索popupWindow
     */
    void browse();

    /**
     * 退出投屏
     */
    void exitCast();

    /**
     * 同步是否RTC观看
     */
    void setRtcWatching(boolean isRtcWatching);

    /**
     * 是否拦截按键事件
     *
     * @param keyCode 事件code
     * @return true：拦截，false：不拦截
     */
    boolean onKeyDown(int keyCode);
    // </editor-fold >


}
