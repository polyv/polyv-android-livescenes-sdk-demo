package com.easefun.polyv.livecloudclass.modules.pagemenu.commodity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecloudclass.modules.media.floating.PLVLCFloatingWindow;
import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerConfig;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewActivity;
import com.plv.foundationsdk.component.di.PLVDependManager;

/**
 * 商品详情页
 */
public class PLVLCCommodityDetailActivity extends PLVSimpleWebViewActivity {

    private static final String EXTRA_COMMODITY_DETAIL_URL = "commodityDetailUrl";
    private String commodityDetailUrl;

    private PLVLCFloatingWindow floatingWindow;

    private boolean showFlag = false;

    public static void start(Context context, String commodityDetailUrl) {
        Intent intent = new Intent(context, PLVLCCommodityDetailActivity.class);
        intent.putExtra(EXTRA_COMMODITY_DETAIL_URL, commodityDetailUrl);
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
        floatingWindow = PLVDependManager.getInstance().get(PLVLCFloatingWindow.class);
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
