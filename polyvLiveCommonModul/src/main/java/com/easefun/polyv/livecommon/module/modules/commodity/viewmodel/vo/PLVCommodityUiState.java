package com.easefun.polyv.livecommon.module.modules.commodity.viewmodel.vo;

import android.support.annotation.Nullable;

import com.plv.socket.event.commodity.PLVProductContentBean;

/**
 * @author Hoshiiro
 */
public class PLVCommodityUiState {

    @Nullable
    public PLVProductContentBean productContentBeanPushToShow = null;
    public boolean hasProductView = false;
    public boolean showProductViewOnLandscape = false;

    public PLVCommodityUiState copy() {
        final PLVCommodityUiState copy = new PLVCommodityUiState();
        copy.productContentBeanPushToShow = this.productContentBeanPushToShow;
        copy.hasProductView = this.hasProductView;
        copy.showProductViewOnLandscape = this.showProductViewOnLandscape;
        return copy;
    }

}
