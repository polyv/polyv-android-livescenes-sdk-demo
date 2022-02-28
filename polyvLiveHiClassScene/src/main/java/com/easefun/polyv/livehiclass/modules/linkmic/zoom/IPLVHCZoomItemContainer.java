package com.easefun.polyv.livehiclass.modules.linkmic.zoom;

import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.ui.widget.PLVSwitchViewAnchorLayout;

/**
 * 连麦列表区域，摄像头画面容器接口，提供交换布局支持
 *
 * @author suhongtao
 */
public interface IPLVHCZoomItemContainer {

    /**
     * 获取容器内的交换布局
     */
    @Nullable
    PLVSwitchViewAnchorLayout getSwitchAnchorLayout();

}
