package com.easefun.polyv.livecommon.module.modules.interact;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.PLVMenuDrawer;
import com.easefun.polyv.livecommon.ui.widget.menudrawer.Position;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.plv.foundationsdk.utils.PLVScreenUtils;
import com.plv.thirdpart.blankj.utilcode.util.ConvertUtils;

/**
 * 内部链接WebView布局
 */
public class PLVInsideWebViewLayout extends FrameLayout {
    // <editor-fold defaultstate="collapsed" desc="变量">
    //布局弹层
    private PLVMenuDrawer menuDrawer;
    private PLVMenuDrawer portraitMenuDrawer;
    private PLVMenuDrawer landscapeMenuDrawer;
    //listener
    private PLVMenuDrawer.OnDrawerStateChangeListener onDrawerStateChangeListener;
    private int portraitTop;
    private PLVOrientationManager.OnConfigurationChangedListener onConfigurationChangedListener;
    //webview
    private PLVSafeWebView webView;
    private ViewGroup parentLy;
    //view
    private ImageView closeIv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PLVInsideWebViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public PLVInsideWebViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVInsideWebViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.plv_interact_inside_webview_layout, this);
        //webview
        parentLy = findViewById(R.id.plv_webview_parent);
        parentLy.setBackgroundColor(Color.BLACK);
        webView = new PLVSafeWebView(getContext());
        webView.setBackgroundColor(0);

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(llp);
        parentLy.addView(webView, 0);
        PLVWebViewHelper.initWebView(getContext(), webView, false);
        //view
        closeIv = findViewById(R.id.plv_close_iv);
        closeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        observeOnOrientationChanged();
    }

    private void loadWebView(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    private void observeOnOrientationChanged() {
        PLVOrientationManager.getInstance().addOnConfigurationChangedListener(
                onConfigurationChangedListener = new PLVOrientationManager.OnConfigurationChangedListener() {
                    @Override
                    public void onCall(Context context, boolean isLandscape) {
                        if (context == getContext() && isShowing()) {
                            if (isLandscape && menuDrawer != landscapeMenuDrawer
                                    || (!isLandscape && menuDrawer != portraitMenuDrawer)) {
                                close();
                            }
                        }
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="对外API - 布局控制、监听设置等">
    public void open(int portraitTop, String url, ViewGroup containerView) {
        open(portraitTop, url, containerView, PLVScreenUtils.isLandscape(getContext()));
    }

    public void open(int portraitTop, String url, ViewGroup containerView, boolean isLandscape) {
        this.portraitTop = portraitTop;
        if (closeIv != null) {
            closeIv.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        }
        loadWebView(url);
        View containView = ((Activity) getContext()).findViewById(Window.ID_ANDROID_CONTENT);
        final int portraitHeight = Math.max(containView.getWidth(), containView.getHeight());
        if (landscapeMenuDrawer == null) {
            landscapeMenuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.RIGHT,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    containerView
            );
            landscapeMenuDrawer.setMenuSize(ConvertUtils.dp2px(375));
        }
        if (portraitMenuDrawer == null) {
            portraitMenuDrawer = PLVMenuDrawer.attach(
                    (Activity) getContext(),
                    PLVMenuDrawer.Type.OVERLAY,
                    Position.BOTTOM,
                    PLVMenuDrawer.MENU_DRAG_CONTAINER,
                    containerView
            );
        }
        portraitMenuDrawer.setMenuSize(portraitHeight - portraitTop);
        menuDrawer = isLandscape ? landscapeMenuDrawer : portraitMenuDrawer;
        menuDrawer.setMenuView(this);
        menuDrawer.setTouchMode(PLVMenuDrawer.TOUCH_MODE_BEZEL);
        menuDrawer.setDrawOverlay(false);
        menuDrawer.setDropShadowEnabled(false);
        menuDrawer.setOnDrawerStateChangeListener(new PLVMenuDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (onDrawerStateChangeListener != null) {
                    onDrawerStateChangeListener.onDrawerStateChange(oldState, newState);
                }
                if (newState == PLVMenuDrawer.STATE_CLOSED) {
                    menuDrawer.detachToContainer();
                    if (webView != null) {
                        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                        webView.clearHistory();
                    }
                } else if (newState == PLVMenuDrawer.STATE_OPEN) {
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                if (onDrawerStateChangeListener != null) {
                    onDrawerStateChangeListener.onDrawerSlide(openRatio, offsetPixels);
                }
            }
        });
        menuDrawer.attachToContainer();
        menuDrawer.openMenu();
    }

    public void setOnDrawerStateChangeListener(PLVMenuDrawer.OnDrawerStateChangeListener listener) {
        this.onDrawerStateChangeListener = listener;
    }

    public void close() {
        if (menuDrawer != null) {
            menuDrawer.closeMenu();
        }
    }

    public boolean isShowing() {
        if (menuDrawer == null) {
            return false;
        }
        return menuDrawer.getDrawerState() != PLVMenuDrawer.STATE_CLOSED;
    }

    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }

    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    public boolean onBackPressed() {
        if (webView != null && webView.canGoBack() && isShowing()) {
            webView.goBack();
            return true;
        }
        if (menuDrawer != null
                && (menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPEN
                || menuDrawer.getDrawerState() == PLVMenuDrawer.STATE_OPENING)) {
            close();
            return true;
        }
        return false;
    }

    public void destroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        close();
        PLVOrientationManager.getInstance().removeOnConfigurationChangedListener(onConfigurationChangedListener);
    }
    // </editor-fold>
}
