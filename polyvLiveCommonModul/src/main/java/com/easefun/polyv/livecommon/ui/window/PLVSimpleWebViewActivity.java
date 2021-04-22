package com.easefun.polyv.livecommon.ui.window;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;

/**
 * 仅包含webView的Activity
 */
public abstract class PLVSimpleWebViewActivity extends PLVBaseActivity {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private PLVSafeWebView webView;
    private ViewGroup parentLy;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plv_horizontal_linear_layout);
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView == null || !webView.canGoBack()) {
            super.onBackPressed();
            return;
        }
        webView.goBack();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(R.id.parent_ly);
        parentLy.setBackgroundColor(getBackgroundColor());
        webView = new PLVSafeWebView(this);
        webView.setBackgroundColor(0);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
        webView.setLayoutParams(llp);
        parentLy.addView(webView);
        PLVWebViewHelper.initWebView(this, webView, isUseActionView());
        loadWebView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="加载webView">
    private void loadWebView() {
        if (TextUtils.isEmpty(urlOrHtmlText())) {
            return;
        }
        if (!isLoadUrl()) {
            String content = PLVWebViewContentUtils.toWebViewContent(urlOrHtmlText());
            webView.loadDataWithBaseURL(null, content, "text/html; charset=UTF-8", null, null);
        } else {
            webView.loadUrl(urlOrHtmlText());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="抽象方法 - 加载webView的方式">
    protected abstract boolean isLoadUrl();

    protected abstract String urlOrHtmlText();

    protected int getBackgroundColor() {
        return 0;
    }

    protected boolean isUseActionView() {
        return true;
    }
    // </editor-fold>
}
