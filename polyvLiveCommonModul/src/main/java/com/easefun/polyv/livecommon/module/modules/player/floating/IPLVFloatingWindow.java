package com.easefun.polyv.livecommon.module.modules.player.floating;

import com.easefun.polyv.livecommon.ui.widget.floating.enums.PLVFloatingEnums;
import com.plv.business.api.common.player.listener.IPLVVideoViewListenerEvent;

/**
 * FloatingWindow接口定义
 */
public interface IPLVFloatingWindow {

    /**
     * 静音播放器/恢复音量
     *
     * @param mute true：静音，false：恢复音量
     */
    void mutePlayer(boolean mute);

    /**
     * 设置由音频焦点引起的播放状态改变监听器
     *
     * @param listener 监听器
     */
    void setOnPlayStatusChangeByAudioFocusListener(IPLVVideoViewListenerEvent.OnPlayStatusChangeByAudioFocusListener listener);

    /**
     * 设置窗口方向
     *
     * @param orientation 方向
     */
    void setOrientation(PLVFloatingEnums.Orientation orientation);

    /**
     * 关闭悬浮窗
     */
    void close();
}
