package com.easefun.polyv.livecommon.module.modules.cast.manager;

/**
 * 对外开发者监听投屏状态的接口
 */
public interface IPLVCastStatusListener {

    /**
     * 初始化投屏SDK结果
     *
     * @param result ture-投屏SDK初始化成功
     */
    void castAuthorize(boolean result);

    /**
     * 开始连接投屏设备
     */
    void castConnectStart();

    /**
     * 投屏设备连接成功
     */
    void castConnectSuccess();

    /**
     * 投屏设备连接失败
     */
    void castConnectError(PLVCastError error);

    /**
     * 投屏设备连接断开
     * （ 当接收端设备关机或者网络断开发送端不会立即产生回调，因为要做多次检测回调时间大概在10到3秒之内）
     */
    void castDisconnect();

    /**
     * 投屏播放错误
     */
    void castPlayError(PLVCastError error);

    /**
     * 投屏播放状态变化
     */
    void castPlayStatus(int state);

    /**
     * 进入当前投屏的直播观看页
     */
    void castEnterLiveRoom();

    /**
     * 退出当前投屏的直播观看页
     */
    void castLeaveLiveRoom();

}
