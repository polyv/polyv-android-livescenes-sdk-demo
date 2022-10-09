package com.easefun.polyv.livecommon.ui.window;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.livecommon.R;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVSafeWebView;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewContentUtils;
import com.easefun.polyv.livecommon.ui.widget.webview.PLVWebViewHelper;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.thirdpart.blankj.utilcode.util.CloseUtils;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * 仅包含webView的Fragment
 */
public abstract class PLVSimpleWebViewFragment extends PLVBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private static final String TAG = PLVSimpleWebViewFragment.class.getSimpleName();
    private PLVSafeWebView webView;
    private ViewGroup parentLy;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.plv_horizontal_linear_layout, null);
        initView();
        return view;
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        parentLy = findViewById(R.id.parent_ly);
        parentLy.setBackgroundColor(getBackgroundColor());
        webView = new PLVSafeWebView(getContext());
        webView.setBackgroundColor(0);
        if (!canTouch()) {
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(llp);
        parentLy.addView(webView);
        PLVWebViewHelper.initWebView(getContext(), webView, isUseActionView());
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

    protected boolean canTouch() {
        return true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="是否拦截返回事件">
    public boolean onBackPressed() {
        if (webView == null || !webView.canGoBack()) {
            return false;
        }
        webView.goBack();
        return true;
    }
    // </editor-fold>

    // <editor-folder defaultstate="collapsed" desc="截图">
    public void captureWebViewAndSave() {
        if (webView == null) {
            return;
        }
        final boolean isSaveToMediaStore = true;
        ArrayList<String> permissions = new ArrayList<>(1);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        PLVFastPermission.getInstance()
                .start((Activity) webView.getContext(), permissions, new PLVOnPermissionCallback() {
                    @Override
                    public void onAllGranted() {
                        Bitmap bitmap = captureWebView();
                        if (isSaveToMediaStore) {
                            saveBimapToMediaStore(bitmap);
                        } else {
                            saveBitmapToCustomPath(bitmap);
                        }
                    }

                    @Override
                    public void onPartialGranted(ArrayList<String> grantedPermissions,
                                                 ArrayList<String> deniedPermissions,
                                                 ArrayList<String> deniedForeverP) {
                        if (deniedForeverP != null && !deniedForeverP.isEmpty()) {
                            new AlertDialog.Builder(webView.getContext()).setTitle("提示")
                                    .setMessage("保存图片所需的存储权限被拒绝，请到应用设置的权限管理中恢复")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PLVFastPermission.getInstance().jump2Settings(webView.getContext());
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PLVCommonLog.d(TAG, "cancel");
                                        }
                                    }).setCancelable(false).show();
                        } else {
                            ToastUtils.showShort("请允许存储权限后再保存图片");
                        }
                    }
                });
    }

    public Bitmap captureWebView() {
        if (webView == null) {
            return null;
        }
        int width = webView.getWidth();
        int height = webView.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }

    private void saveBimapToMediaStore(Bitmap bitmap) {
        OutputStream outputStream = null;
        try {
            if (getContext() == null) {
                return;
            }
            if (Build.VERSION.SDK_INT < 29) {
                String insertImage = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, System.currentTimeMillis() + "", "");
                if (!TextUtils.isEmpty(insertImage)) {
                    ToastUtils.showShort("图片已保存到相册");
                }
            } else {
                Uri insertUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                outputStream = getContext().getContentResolver().openOutputStream(insertUri, "rw");
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)) {
                    ToastUtils.showShort("图片已保存到相册");
                }
            }
        } catch (Exception e) {
            PLVCommonLog.exception(e);
        } finally {
            CloseUtils.closeIO(outputStream);
        }
    }

    private void saveBitmapToCustomPath(Bitmap bitmap) {
        final String fileName = System.currentTimeMillis() + ".jpg";
        final String savePath = PLVSDCardUtils.createPath(webView.getContext(), "PLVChatImg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(savePath, fileName));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            ToastUtils.showShort("保存图片成功");
        } catch (Exception e) {
            PLVCommonLog.exception(e);
            ToastUtils.showShort("保存图片失败");
        } finally {
            CloseUtils.closeIO(fos);
        }
    }
    // </editor-folder>
}
