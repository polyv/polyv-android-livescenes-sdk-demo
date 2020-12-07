package com.easefun.polyv.livecloudclass.modules.pagemenu.text;

import android.graphics.Color;

import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewFragment;

/**
 * 自定义图文菜单tab页
 */
public class PLVLCTextFragment extends PLVSimpleWebViewFragment {
    private String htmlText;

    public void init(String htmlText) {
        this.htmlText = PLVWebViewContentUtils.toWebViewContent(htmlText, "#ADADC0");
    }

    @Override
    protected int getBackgroundColor() {
        return Color.parseColor("#202127");
    }

    @Override
    protected boolean isLoadUrl() {
        return false;
    }

    @Override
    protected String urlOrHtmlText() {
        return htmlText;
    }
}
