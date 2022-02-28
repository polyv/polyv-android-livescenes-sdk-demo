package com.easefun.polyv.livecommon.module.modules.watermark;

/**
 * author: fangfengrui
 * date: 2021/12/27
 */
public interface IPLVWatermarkView {
    /**
     * 更新水印字体
     */
    void setPLVWatermarkVO(PLVWatermarkTextVO plvWatermarkVO);

    /**
     * 添加展示水印
     */
    void showWatermark();

    /**
     * 移除水印
     */
    void removeWatermark();
}
