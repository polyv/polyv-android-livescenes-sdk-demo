package com.easefun.polyv.livecommon.module.modules.commodity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.PLVLanguageUtil;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.plv.foundationsdk.rx.PLVRxBus;
import com.plv.livescenes.feature.pagemenu.product.PLVProductExplainWebView;

import net.plv.android.jsbridge.BridgeHandler;
import net.plv.android.jsbridge.CallBackFunction;

public class PLVProductExplainActivity extends PLVBaseActivity {
    private static final String EXTRA_PRODUCT_ID = "productId";
    private static final String EXTRA_APP_PARAMS = "appParams";
    private PLVProductExplainWebView productExplainWebView;
    private ImageView backIv;

    public static void start(Context context, int productId, String appParams) {
        PLVRxBus.get().post(new PLVProductExplainEvent(true));
        Intent intent = new Intent(context, PLVProductExplainActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        intent.putExtra(EXTRA_APP_PARAMS, appParams);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_product_explain_layout);
        initView();
    }

    @Override
    public void finish() {
        PLVRxBus.get().post(new PLVProductExplainEvent(false));
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productExplainWebView != null) {
            productExplainWebView.removeAllViews();
            ViewParent viewParent = productExplainWebView.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(productExplainWebView);
            }
            productExplainWebView.destroy();
            productExplainWebView = null;
        }
    }

    private void initView() {
        int productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, 0);
        final String appParams = getIntent().getStringExtra(EXTRA_APP_PARAMS);
        productExplainWebView = findViewById(R.id.plv_product_explain_web);
        productExplainWebView.setProductId(productId);
        productExplainWebView.setLang(PLVLanguageUtil.isENLanguage() ? PLVProductExplainWebView.LANG_EN : PLVProductExplainWebView.LANG_ZH);
        productExplainWebView.setOnNeedUpdateNativeAppParamsInfoHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        callBackFunction.onCallBack(appParams);
                    }
                })
                .setOnReceiverEventBackToLiveHandler(new BridgeHandler() {
                    @Override
                    public void handler(String s, CallBackFunction callBackFunction) {
                        finish();
                    }
                });
        productExplainWebView.loadWeb();

        backIv = findViewById(R.id.plv_back_iv);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static class PLVProductExplainEvent {
        public boolean isShow;

        public PLVProductExplainEvent(boolean isShow) {
            this.isShow = isShow;
        }
    }
}
