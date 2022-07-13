package com.easefun.polyv.livecommon.module.modules.commodity.model.vo;

import com.easefun.polyv.livecommon.module.modules.socket.PLVSocketMessage;
import com.plv.socket.event.commodity.PLVProductEvent;

/**
 * @author Hoshiiro
 */
public class PLVCommodityProductVO {

    private final PLVProductEvent productEvent;
    private final PLVSocketMessage message;

    public PLVCommodityProductVO(
            final PLVProductEvent productEvent,
            final PLVSocketMessage message
    ) {
        this.productEvent = productEvent;
        this.message = message;
    }

    public PLVProductEvent getProductEvent() {
        return productEvent;
    }

    public PLVSocketMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "PLVCommodityProductVO{" +
                "productEvent=" + productEvent +
                ", message=" + message +
                '}';
    }

}
