package com.easefun.polyv.livecommon.module.modules.interact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSimpleUrlWebViewActivity;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 外部链接WebView页面
 */
public class PLVOutsideWebViewActivity extends PLVSimpleUrlWebViewActivity {

    public static void start(Context context, @NonNull String url) {
        Intent intent = new Intent(context, PLVOutsideWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout frameLayout = findViewById(Window.ID_ANDROID_CONTENT);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.plv_back_ic);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ConvertUtils.dp2px(32), ConvertUtils.dp2px(32));
        int padding = ConvertUtils.dp2px(4);
        layoutParams.topMargin = padding;
        layoutParams.leftMargin = padding;
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setLayoutParams(layoutParams);
        frameLayout.addView(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PLVOutsideWebViewActivity.this.onBackPressed();
            }
        });
    }
}
