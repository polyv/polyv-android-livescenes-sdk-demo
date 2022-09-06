package com.easefun.polyv.livecommon.module.modules.player;

import androidx.annotation.DrawableRes;

import com.plv.foundationsdk.annos.Sp;

/**
 * 播放失败View接口定义
 */
public interface IPLVPlayErrorView {

    /**
     * 设置占位图图片
     *
     * @param resId 图片ID
     */
    void setPlaceHolderImg(@DrawableRes int resId);

    /**
     * 设置占位图文本
     *
     * @param text 文本
     */
    void setPlaceHolderText(String text);

    /**
     * 暂未占位图文本字体大小
     */
    void setPlaceHolderTextSize(@Sp float size);

    /**
     * 设置切换线路按钮是否可见
     */
    void setChangeLinesViewVisibility(int visibility);

    /**
     * 设置刷新按钮是否可见
     */
    void setRefreshViewVisibility(int visibility);

    /**
     * 设置view是否可见
     */
    void setViewVisibility(int visibility);
}
