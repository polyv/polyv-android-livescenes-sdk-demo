package com.easefun.polyv.livecloudclass.modules.media.danmu;

/**
 * 弹幕控制接口
 */
public interface IPLVLCDanmuController {

    /**
     * 显示
     */
    void show();

    /**
     * 隐藏
     */
    void hide();

    /**
     * 发送弹幕
     *
     * @param message 弹幕信息
     */
    void sendDanmaku(CharSequence message);

    /**
     * 释放
     */
    void release();

    /**
     * 设置弹幕速度
     *
     * @param speed 弹幕速度
     */
    void setDanmuSpeed(int speed);
}
