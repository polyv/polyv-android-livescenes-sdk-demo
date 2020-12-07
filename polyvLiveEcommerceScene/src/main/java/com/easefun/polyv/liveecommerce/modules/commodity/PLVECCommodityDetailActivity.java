package com.easefun.polyv.liveecommerce.modules.commodity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewActivity;
import com.easefun.polyv.liveecommerce.scenes.PLVECLiveEcommerceActivity;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

/**
 * 商品详情页
 */
public class PLVECCommodityDetailActivity extends PLVSimpleWebViewActivity {
    private static final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 1;
    private static final String EXTRA_COMMODITY_DETAIL_URL = "commodityDetailUrl";
    private String commodityDetailUrl;

    public static void start(Context context, String commodityDetailUrl) {
        Intent intent = new Intent(context, PLVECCommodityDetailActivity.class);
        intent.putExtra(EXTRA_COMMODITY_DETAIL_URL, commodityDetailUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData();
        initFloatingWindowPermission();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!(Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this))) {
            sendFloatingWindowBroadcast(true, false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendFloatingWindowBroadcast(false, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MANAGE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                ToastUtils.showLong("授权失败，无法使用悬浮小窗播放功能");
            } else {
                sendFloatingWindowBroadcast(true, true);
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        commodityDetailUrl = intent.getStringExtra(EXTRA_COMMODITY_DETAIL_URL);
    }

    private void initFloatingWindowPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setMessage("悬浮小窗播放功能需要在应用设置中开启悬浮窗权限，是否前往开启权限？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_CODE_MANAGE_OVERLAY_PERMISSION);
                        }
                    })
                    .setNegativeButton("否", null)
                    .show();
        } else {
            sendFloatingWindowBroadcast(true, true);
        }
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

    private void sendFloatingWindowBroadcast(boolean isShowFloatingWindow, boolean isResetLocation) {
        Intent intent = new Intent(PLVECLiveEcommerceActivity.ACTION_FLOATING_WINDOW);
        intent.putExtra(PLVECLiveEcommerceActivity.EXTRA_IS_SHOW_FLOATING_WINDOW, isShowFloatingWindow);
        intent.putExtra(PLVECLiveEcommerceActivity.EXTRA_IS_RESET_FLOATING_WINDOW_LOCATION, isResetLocation);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
