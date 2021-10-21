package com.easefun.polyv.livecommon.module.modules.streamer.model;

import com.easefun.polyv.livecommon.module.modules.linkmic.model.PLVLinkMicItemDataBean;
import com.plv.socket.user.PLVClassStatusBean;
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
        syncClassStatusToLinkMicBean(socketUserBean, linkMicItemDataBean);
    }

    public PLVLinkMicItemDataBean getLinkMicItemDataBean() {
        return linkMicItemDataBean;
    }

    public void setLinkMicItemDataBean(PLVLinkMicItemDataBean linkMicItemDataBean) {
        this.linkMicItemDataBean = linkMicItemDataBean;
        syncClassStatusToLinkMicBean(socketUserBean, linkMicItemDataBean);
    }

    public void updateBaseLinkMicBean(PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (this.linkMicItemDataBean == null || linkMicItemDataBean == null) {
            return;
        }
        this.linkMicItemDataBean.setUserType(linkMicItemDataBean.getUserType());
        this.linkMicItemDataBean.setActor(linkMicItemDataBean.getActor());
        this.linkMicItemDataBean.setNick(linkMicItemDataBean.getNick());
        this.linkMicItemDataBean.setPic(linkMicItemDataBean.getPic());
        this.linkMicItemDataBean.setLinkMicId(linkMicItemDataBean.getLinkMicId());
    }

    public void addBaseLinkMicBean(PLVSocketUserBean socketUserBean) {
        if (socketUserBean == null) {
            return;
        }
        PLVLinkMicItemDataBean linkMicItemDataBean = new PLVLinkMicItemDataBean();
        linkMicItemDataBean.setUserType(socketUserBean.getUserType());
        linkMicItemDataBean.setActor(socketUserBean.getActor());
        linkMicItemDataBean.setNick(socketUserBean.getNick());
        linkMicItemDataBean.setPic(socketUserBean.getPic());
        linkMicItemDataBean.setLinkMicId(socketUserBean.getUserId());
        setLinkMicItemDataBean(linkMicItemDataBean);
    }

    private void syncClassStatusToLinkMicBean(PLVSocketUserBean socketUserBean, PLVLinkMicItemDataBean linkMicItemDataBean) {
        if (socketUserBean == null || linkMicItemDataBean == null) {
            return;
        }
        PLVClassStatusBean classStatusBean = socketUserBean.getClassStatus();
        if (classStatusBean != null) {
            linkMicItemDataBean.setHasPaint(classStatusBean.hasPaint());
            linkMicItemDataBean.setCupNum(classStatusBean.getCup());
        }
    }
}
