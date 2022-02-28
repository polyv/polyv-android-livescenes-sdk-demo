package com.easefun.polyv.livehiclass.modules.linkmic.zoom;

import com.plv.socket.event.linkmic.PLVUpdateMicSiteEvent;

/**
 * 连麦摄像头放大容器承载布局接口
 *
 * @author suhongtao
 */
public interface IPLVHCLinkMicZoomLayout {

    /**
     * 创建连麦摄像头放大容器
     */
    PLVHCLinkMicZoomItemContainer createZoomItemContainer();

    /**
     * 释放连麦摄像头放大容器
     */
    void removeZoomItemContainer(PLVHCLinkMicZoomItemContainer zoomItemContainer);

    /**
     * 是否可以放大更多摄像头画面
     *
     * @return true:可以放大更多摄像头画面，false:已达到上限
     */
    boolean canZoomMoreItem();

    /**
     * 更新摄像头放大画面位置
     *
     * @param event             更新事件
     * @param zoomItemContainer 需要更新的容器
     */
    void onUpdateMicSite(PLVUpdateMicSiteEvent event, PLVHCLinkMicZoomItemContainer zoomItemContainer);

    /**
     * 销毁方法
     */
    void destroy();

}
