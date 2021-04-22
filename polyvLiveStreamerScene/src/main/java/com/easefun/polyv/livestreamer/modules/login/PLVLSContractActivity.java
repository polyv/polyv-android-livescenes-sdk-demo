package com.easefun.polyv.livestreamer.modules.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.easefun.polyv.livecommon.ui.window.PLVBaseActivity;
import com.easefun.polyv.livestreamer.R;

/**
 * 隐私政策页
 */
public class PLVLSContractActivity extends PLVBaseActivity {
    public static final String KEY_IS_PRIVATE_POLICY = "key_is_private_policy";

    private static final String URL_PRIVATE_POLICY = "https://s2.videocc.net/app-simple-pages/privacy-policy/index.html";
    private static final String URL_USAGE_CONTRACT = "https://s2.videocc.net/app-simple-pages/user-agreement/index.html";

    private WebView webview;
    private TextView tvBack;
    private TextView tvTitle;
    private ProgressBar progressWebview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plvls_contract_activity);

        initView();

        Intent intent = getIntent();
        if (intent == null) return;

        boolean isPrivatePolicy = intent.getBooleanExtra(KEY_IS_PRIVATE_POLICY, false);

        tvTitle.setText(isPrivatePolicy ? "隐私政策" : "使用协议");

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PLVLSContractActivity.this.finish();
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
