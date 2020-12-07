package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import com.plv.linkmic.model.PLVJoinInfoEvent;
import com.plv.linkmic.model.PLVLinkMicJoinSuccess;

/**
 * date: 2020/7/27
 * author: hwj
 * description:连麦数据映射器。将服务端返回的数据模型转换成我们业务上需要的数据模型
 */
public class PLVLinkMicDataMapper {
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
        return itemDataBean;
    }
}
