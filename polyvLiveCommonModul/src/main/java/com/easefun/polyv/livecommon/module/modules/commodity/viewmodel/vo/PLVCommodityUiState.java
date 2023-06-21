package com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo;

import androidx.annotation.Nullable;

import com.plv.socket.event.commodity.PLVProductContentBean;

/**
 * @author Hoshiiro
 */
public class PLVCommodityUiState {

    @Nullable
    public PLVProductContentBean productContentBeanPushToShow = null;
    public boolean hasProductView = false;
    public boolean showProductViewOnLandscape = false;
    // 是否推送商品，该属性不被复制，只在推送商品时使用赋值
    public boolean isPush = false;

    public PLVCommodityUiState copy() {
        final PLVCommodityUiState copy = new PLVCommodityUiState();
        copy.productContentBeanPushToShow = this.productContentBeanPushToShow;
        copy.hasProductView = this.hasProductView;
        copy.showProductViewOnLandscape = this.showProductViewOnLandscape;
        return copy;
    }

    public PLVCommodityUiState copyWithPushState() {
        final PLVCommodityUiState copy = copy();
        copy.isPush = true;
        return copy;
    }
}
