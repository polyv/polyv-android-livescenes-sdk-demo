package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinStatus;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;
import com.plv.socket.user.PLVSocketUserBean;

/**
 * date: 2020/7/27
 * author: hwj
 * description:连麦数据映射器。将服务端返回的数据模型转换成我们业务上需要的数据模型
 */
public class PLVLinkMicDataMapper {

    // <editor-fold defaultstate="collapsed" desc="转成PLVLinkMicItemDataBean">
    public static PLVLinkMicItemDataBean map2LinkMicItemData(PLVLinkMicJoinSuccess joinSuccess) {
        PLVLinkMicItemDataBean itemDataBean = new PLVLinkMicItemDataBean();
        itemDataBean.setLinkMicId(joinSuccess.getUser().getUserId());
        itemDataBean.setNick(joinSuccess.getUser().getNick());
        itemDataBean.setUserType(joinSuccess.getUser().getUserType());
        return itemDataBean;
    }

    public static PLVLinkMicItemDataBean map2LinkMicItemData(PLVJoinInfoEvent joinInfoEvent) {
        PLVLinkMicItemDataBean itemDataBean = new PLVLinkMicItemDataBean();
        itemDataBean.setLinkMicId(joinInfoEvent.getUserId());
        itemDataBean.setNick(joinInfoEvent.getNick());
        itemDataBean.setUserType(joinInfoEvent.getUserType());
        itemDataBean.setActor(joinInfoEvent.getActor());
        itemDataBean.setStatus(joinInfoEvent.getStatus());
        return itemDataBean;
    }

    public static PLVLinkMicItemDataBean map2LinkMicItemData(PLVLinkMicJoinStatus.WaitListBean waitListBean) {
        PLVLinkMicItemDataBean itemDataBean = new PLVLinkMicItemDataBean();
        itemDataBean.setLinkMicId(waitListBean.getUserId());
        itemDataBean.setNick(waitListBean.getNick());
        itemDataBean.setUserType(waitListBean.getUserType());
        itemDataBean.setActor(waitListBean.getActor());
        itemDataBean.setStatus(waitListBean.getStatus());
        return itemDataBean;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="转成PLVSocketUserBean">
    public static PLVSocketUserBean map2SocketUserBean(PLVJoinInfoEvent joinInfoEvent) {
        PLVSocketUserBean socketUserBean = new PLVSocketUserBean();
        socketUserBean.setBanned(joinInfoEvent.isBanned());
        socketUserBean.setNick(joinInfoEvent.getNick());
        socketUserBean.setPic(joinInfoEvent.getPic());
        socketUserBean.setActor(joinInfoEvent.getActor());
        socketUserBean.setUserType(joinInfoEvent.getUserType());
        socketUserBean.setChannelId(joinInfoEvent.getChannelId());
        socketUserBean.setClientIp(joinInfoEvent.getClientIp());
        socketUserBean.setUserId(joinInfoEvent.getLoginId());//loginId为socket的userId
        socketUserBean.setUid(joinInfoEvent.getUid());
        socketUserBean.setRoomId(joinInfoEvent.getRoomId());
        return socketUserBean;
    }

    public static PLVSocketUserBean map2SocketUserBean(PLVLinkMicJoinStatus.WaitListBean waitListBean) {
        PLVSocketUserBean socketUserBean = new PLVSocketUserBean();
        socketUserBean.setNick(waitListBean.getNick());
        socketUserBean.setPic(waitListBean.getPic());
        socketUserBean.setActor(waitListBean.getActor());
        socketUserBean.setUserType(waitListBean.getUserType());
        socketUserBean.setClientIp(waitListBean.getClientIp());
        socketUserBean.setUserId(waitListBean.getLoginId());//loginId为socket的userId
        socketUserBean.setUid(waitListBean.getUid());
        socketUserBean.setRoomId(waitListBean.getRoomId());
        return socketUserBean;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="转成WaitListBean">
    public static PLVLinkMicJoinStatus.WaitListBean map2WaitListBean(PLVJoinInfoEvent joinInfoEvent) {
        PLVLinkMicJoinStatus.WaitListBean waitListBean = new PLVLinkMicJoinStatus.WaitListBean();
        waitListBean.setLoginId(joinInfoEvent.getLoginId());
        waitListBean.setActor(joinInfoEvent.getActor());
        waitListBean.setClientIp(joinInfoEvent.getClientIp());
        waitListBean.setNick(joinInfoEvent.getNick());
        waitListBean.setPic(joinInfoEvent.getPic());
        waitListBean.setRoomId(joinInfoEvent.getRoomId());
        waitListBean.setSessionId(joinInfoEvent.getSessionId());
        waitListBean.setStatus(joinInfoEvent.getStatus());
        waitListBean.setUid(joinInfoEvent.getUid());
        waitListBean.setUserId(joinInfoEvent.getUserId());
        waitListBean.setUserType(joinInfoEvent.getUserType());
        waitListBean.setCupNum(joinInfoEvent.getCupNum());
        return waitListBean;
    }
    // </editor-fold>
}
