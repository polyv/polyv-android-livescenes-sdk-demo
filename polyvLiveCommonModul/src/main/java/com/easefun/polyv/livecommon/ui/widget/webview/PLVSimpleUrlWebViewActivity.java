package com.easefun.polyv.livecommon.ui.widget.webview;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.easefun.polyv.livecommon.ui.window.PLVSimpleWebViewActivity;

/**
 * @author Hoshiiro
 */
public class PLVSimpleUrlWebViewActivity extends PLVSimpleWebViewActivity {

    public static final String EXTRA_URL = "extra_url";

    public static void start(Context context, @NonNull String url) {
        Intent intent = new Intent(context, PLVSimpleUrlWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected boolean isLoadUrl() {
        return true;
    }

    @Override
    protected boolean isUseActionView() {
        return false;
    }

    @Override
    protected String urlOrHtmlText() {
        return getIntent().getExtras().getString(EXTRA_URL);
    }

}
