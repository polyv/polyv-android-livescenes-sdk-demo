package com.easefun.polyv.livecommon.module.modules.streamer.model;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.socket.user.PLVSocketUserBean;

/**
 * 成员列表item对应的数据实体
 */
public class PLVMemberItemDataBean {
    //连麦信息
    private PLVLinkMicItemDataBean linkMicItemDataBean;

    //讲师是否是前置摄像头
    private boolean isFrontCamera = true;

    //socket用户信息
    private PLVSocketUserBean socketUserBean;

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    public PLVSocketUserBean getSocketUserBean() {
        return socketUserBean;
    }

    public void setSocketUserBean(PLVSocketUserBean socketUserBean) {
        this.socketUserBean = socketUserBean;
    }

    public PLVLinkMicItemDataBean getLinkMicItemDataBean() {
        return linkMicItemDataBean;
    }

    public void setLinkMicItemDataBean(PLVLinkMicItemDataBean linkMicItemDataBean) {
        this.linkMicItemDataBean = linkMicItemDataBean;
    }
}
