package com.easefun.polyv.livecommon.ui.widget;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.easefun.polyv.livecommon.R;


/**
 * date: 2019/8/5 0005
 *
 * @author hwj
 * description 加载中
 */
public class PLVLoadingWindow {

    private final AppCompatActivity activity;
    private final View anchor;
    private final PopupWindow window;

    private ProgressBar loadingPb;

    public PLVLoadingWindow(AppCompatActivity activity) {
        this.activity = activity;
        this.anchor = activity.findViewById(android.R.id.content);
        this.window = new PopupWindow(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.plv_loading_window_layout, null);
        window.setContentView(root);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(null);
        window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        activity.getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                switch (event) {
                    case ON_DESTROY:
                    case ON_PAUSE:
                        window.dismiss();
                        break;
                }
            }
        });
        initView(root);
    }

    private void initView(View root) {
        loadingPb = root.findViewById(R.id.loading_pb);
    }

    public void show() {
        window.showAtLocation(anchor, Gravity.CENTER, 0, 0);
    }

    public void hide() {
        window.dismiss();
    }
}
