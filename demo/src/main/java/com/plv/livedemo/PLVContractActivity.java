package com.plv.livedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.plv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.plv.livecommon.ui.window.PLVBaseActivity;

/**
 * 隐私政策页
 */
public class PLVContractActivity extends PLVBaseActivity {
    public static final String KEY_IS_PRIVATE_POLICY = "key_is_private_policy";

    // TODO 填写 隐私政策 和 使用协议 的URL地址
    private static final String URL_PRIVATE_POLICY = "";
    private static final String URL_USAGE_CONTRACT = "";

    private WebView webview;
    private TextView tvBack;
    private TextView tvTitle;
    private ProgressBar progressWebview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_contract_activity);

        initView();

        Intent intent = getIntent();
        if (intent == null) return;

        boolean isPrivatePolicy = intent.getBooleanExtra(KEY_IS_PRIVATE_POLICY, false);

        tvTitle.setText(isPrivatePolicy ? "隐私政策" : "使用协议");

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PLVContractActivity.this.finish();
            }
        });


        PLVWebViewHelper.initWebView(this, webview);
        webview.loadUrl(isPrivatePolicy ? URL_PRIVATE_POLICY : URL_USAGE_CONTRACT);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressWebview.setVisibility(View.GONE);
                } else {
                    progressWebview.setVisibility(View.VISIBLE);
                    progressWebview.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

    }

    private void initView() {
        webview = findViewById(R.id.webview);
        tvBack = findViewById(R.id.tv_back);
        tvTitle = findViewById(R.id.tv_title);
        progressWebview = findViewById(R.id.progress_webview);

    }
}
