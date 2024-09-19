package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.utils.PLVSDCardUtils;
import com.plv.foundationsdk.utils.PLVScreenshotUtil;
import com.plv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.File;

/**
 * 截屏帮助类
 */
public class PLVScreenshotHelper {
    private static final String TAG = "PLVScreenshotHelper";
    public static boolean DISABLE_SCREEN_CAP = false; // 是否开启防录屏
    public static boolean SHOW_SCREENSHOT_VIEW = false; // 是否显示截屏按钮
    private final PLVScreenshotUtil screenshotUtil;

    public static boolean isDisableScreenCap() {
        return DISABLE_SCREEN_CAP;
    }

    public static void setDisableScreenCap(boolean disableScreenCap) {
        DISABLE_SCREEN_CAP = disableScreenCap;
    }

    public static boolean isShowScreenshotView() {
        return SHOW_SCREENSHOT_VIEW;
    }

    public static void setShowScreenshotView(boolean showScreenshotView) {
        SHOW_SCREENSHOT_VIEW = showScreenshotView;
    }

    public PLVScreenshotHelper() {
        screenshotUtil = new PLVScreenshotUtil();
        screenshotUtil.setListener(new PLVScreenshotUtil.PLVScreenshotListener() {
            @Override
            public void onPermissionDenied(boolean needJump2SettingsRecover) {
                if (needJump2SettingsRecover) {
                    new AlertDialog.Builder(screenshotUtil.activity).setTitle(R.string.plv_common_dialog_tip).setMessage(R.string.plv_live_no_save_img_permission_hint).setPositiveButton(R.string.plv_common_dialog_confirm_2, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVFastPermission.getInstance().jump2Settings(screenshotUtil.activity);
                        }
                    }).setNegativeButton(R.string.plv_common_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PLVCommonLog.d(TAG, "cancel");
                        }
                    }).setCancelable(false).show();
                } else {
                    ToastUtils.showShort(R.string.plv_live_allow_permission_save_img_hint);
                }
            }

            @Override
            public void onCaptureSuccess(Bitmap bitmap) {
                final String fileName = System.currentTimeMillis() + ".jpg";
                final String savePath = PLVSDCardUtils.createPath(screenshotUtil.activity, "PLVChatImg");
                boolean result = screenshotUtil.saveBitmapToCustomPath(bitmap, savePath, fileName);
                if (result) {
                    ToastUtils.showShort(R.string.plv_live_save_img_success, "\n" + savePath + File.separator + fileName);
                } else {
                    ToastUtils.showShort(R.string.plv_live_save_img_failed);
                }
            }
        });
    }

    /**
     * 开始屏幕捕获截取屏幕，可以在防录屏的情况下进行截屏
     */
    public void startScreenCapture(@NonNull Activity activity) {
        screenshotUtil.startScreenCapture(activity);
    }

    /**
     * 开始屏幕捕获截取屏幕，可以在防录屏的情况下进行截屏
     */
    public void startScreenCaptureToFragment(@NonNull Fragment fragment) {
        if (fragment.getActivity() != null) {
            screenshotUtil.startScreenCapture(fragment.getActivity(), fragment);
        }
    }

    /**
     * 屏幕捕获授权结果回调
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return screenshotUtil.onActivityResult(requestCode, resultCode, data);
    }
}
