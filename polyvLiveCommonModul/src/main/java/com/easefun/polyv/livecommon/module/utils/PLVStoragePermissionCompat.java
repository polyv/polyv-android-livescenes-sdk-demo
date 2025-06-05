package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.easefun.polyv.livecommon.R;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.permission.PLVOnPermissionCallback;
import com.plv.foundationsdk.permission.PLVOnStoragePermissionCallback;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;

import java.util.ArrayList;

public class PLVStoragePermissionCompat {

    public static boolean hasPermission(Context context) {
        return PLVFastPermission.hasStoragePermissionCompat13(context);
    }

    public static void start(final Context context, final PLVOnPermissionCallback callback) {
        PLVFastPermission.hasStoragePermissionCompat13(context, new PLVOnStoragePermissionCallback() {

            @Override
            public void onResult(boolean isGrant, boolean isAPI13) {
                if (isGrant) {
                    callback.onAllGranted();
                } else {
                    requestPermissionWhenNoGrand(context, isAPI13, callback);
                }
            }
        });
    }

    public static void jump2Settings(Context context) {
        if (PLVFastPermission.isAndroid13()) {
            PLVFastPermission.getInstance().jump2FilesAccessPermission(context);
        } else {
            PLVFastPermission.getInstance().jump2Settings(context);
        }
    }

    public static void requestPermissionWhenNoGrand(final Context context, boolean isAPI13, final PLVOnPermissionCallback callback) {
        if (isAPI13) {
            final Activity topActivity = ActivityUtils.getTopActivity();
            new AlertDialog.Builder(topActivity != null ? topActivity : context).setMessage(R.string.plv_common_storage_permission_apply_tips).setPositiveButton(R.string.plv_common_dialog_confirm_3, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startStoragePermissionCompat13(context, callback);
                }
            }).setNegativeButton(R.string.plv_common_dialog_cancel_2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).setCancelable(false).show();
        } else {
            startStoragePermissionCompat13(context, callback);
        }
    }

    private static void startStoragePermissionCompat13(Context context, final PLVOnPermissionCallback callback) {
        PLVFastPermission.getInstance().startStoragePermissionCompat13((Activity) context, new PLVOnPermissionCallback() {
            @Override
            public void onAllGranted() {
                if (callback != null) {
                    callback.onAllGranted();
                }
            }

            @Override
            public void onPartialGranted(ArrayList<String> grantedPermissions, ArrayList<String> deniedPermissions, ArrayList<String> deniedForeverP) {
                if (callback != null) {
                    callback.onPartialGranted(grantedPermissions, deniedPermissions, deniedForeverP);
                }
            }
        });
    }
}
