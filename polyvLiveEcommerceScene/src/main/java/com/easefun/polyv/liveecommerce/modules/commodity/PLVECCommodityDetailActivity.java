package com.easefun.polyv.liveecommerce.modules.commodity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.easefun.polyv.livecommon.module.data.IPLVLiveRoomDataManager;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerConfig;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewActivity;
import com.easefun.polyv.liveecommerce.modules.player.floating.PLVECFloatingWindow;
import com.plv.foundationsdk.component.di.PLVDependManager;

/**
 * 商品详情页
 */
public class PLVECCommodityDetailActivity extends PLVSimpleWebViewActivity {
    private static final String EXTRA_COMMODITY_DETAIL_URL = "commodityDetailUrl";
    private String commodityDetailUrl;

    private PLVECFloatingWindow floatingWindow;

    private boolean showFlag = false;

    public static void start(Context context, String commodityDetailUrl, IPLVLiveRoomDataManager liveRoomDataManager) {
        Intent intent = new Intent(context, PLVECCommodityDetailActivity.class);
        intent.putExtra(EXTRA_COMMODITY_DETAIL_URL, liveRoomDataManager != null ? liveRoomDataManager.appendIFrameParams(commodityDetailUrl) : commodityDetailUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();
        initFloatingWindow();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome() || !showFlag) {
            showFlag = true;
            floatingWindow.showByCommodityPage(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome()) {
            floatingWindow.showByCommodityPage(false);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        commodityDetailUrl = intent.getStringExtra(EXTRA_COMMODITY_DETAIL_URL);
    }

    private void initFloatingWindow() {
        floatingWindow = PLVDependManager.getInstance().get(PLVECFloatingWindow.class);
    }

    @Override
    protected boolean isLoadUrl() {
        return true;
    }

    @Override
    protected String urlOrHtmlText() {
        return commodityDetailUrl;
    }

    @Override
    protected boolean isUseActionView() {
        return false;
    }

}
