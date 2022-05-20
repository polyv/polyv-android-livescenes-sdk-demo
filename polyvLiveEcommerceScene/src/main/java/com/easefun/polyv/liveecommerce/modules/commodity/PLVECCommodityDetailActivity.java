package com.easefun.polyv.liveecommerce.modules.commodity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

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

    public static void start(Context context, String commodityDetailUrl) {
        Intent intent = new Intent(context, PLVECCommodityDetailActivity.class);
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
        floatingWindow.showByCommodityPage(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        floatingWindow.showByCommodityPage(false);
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
