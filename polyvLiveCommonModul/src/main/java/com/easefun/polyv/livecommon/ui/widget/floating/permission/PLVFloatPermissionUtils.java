package com.easefun.polyv.livecommon.ui.widget.floating.permission;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.util.Log;

import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.HuaweiUtils;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.MeizuUtils;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.MiuiUtils;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.OppoUtils;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.QikuUtils;
import com.easefun.polyv.livecommon.ui.widget.floating.permission.rom.RomUtils;

/**
 * 悬浮窗权限管理工具类
 */
public class PLVFloatPermissionUtils {

    private static String TAG = "FloatPermissionUtils";
    public static final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 1010;

    /**
     * 请求悬浮窗权限
     *
     * @param activity
     * @param listener
     */
    public static void requestPermission(Activity activity, IPLVOverlayPermissionListener listener) {
        PLVPermissionFragment.requestPermission(activity, listener);
    }


    /**
     * 检查悬浮窗权限
     *
     * @param activity
     * @return
     */
    public static boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsHuaweiRom()) return huaweiPermissionCheck(activity);
            if (RomUtils.checkIsMiuiRom()) return miuiPermissionCheck(activity);
            if (RomUtils.checkIsOppoRom()) return oppoROMPermissionCheck(activity);
            if (RomUtils.checkIsMeizuRom()) return meizuPermissionCheck(activity);
            if (RomUtils.checkIs360Rom()) return qikuPermissionCheck(activity);
            else return true;
        } else {
            return commonROMPermissionCheck(activity);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="权限检查">

    private static boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private static boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }


    private static boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }


    private static boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }


    private static boolean oppoROMPermissionCheck(Context context) {
        return OppoUtils.checkFloatWindowPermission(context);
    }


    /**
     * 6.0以后，通用悬浮窗权限检测
     * 但是魅族6.0的系统这种方式不好用，需要单独适配一下
     */
    private static boolean commonROMPermissionCheck(Context context) {
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                result = Settings.canDrawOverlays(context);
            }
            return result;
        }
    }
    // </editor-fold >

    // <editor-fold defaultstate="collapsed" desc="申请权限">
    private static void requestPermission(Fragment fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (RomUtils.checkIsHuaweiRom()) {
                HuaweiUtils.applyPermission(fragment);
            } else if (RomUtils.checkIsMiuiRom()) {
                MiuiUtils.applyMiuiPermission(fragment);
            } else if (RomUtils.checkIsOppoRom()) {
                OppoUtils.applyOppoPermission(fragment);
            } else if (RomUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(fragment);
            } else if (RomUtils.checkIs360Rom()) {
                QikuUtils.applyPermission(fragment);
            } else {
                Log.i(TAG, "原生 Android 6.0 以下无需权限申请");
            }
        } else {
            commonROMPermissionApply(fragment);
        }
    }

    /**
     * 通用 rom 权限申请
     */
    private static void commonROMPermissionApply(Fragment fragment) {
        // 这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            MeizuUtils.applyPermission(fragment);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            commonROMPermissionApplyInternal(fragment);
        } else {
            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION");
        }
    }

    public static void commonROMPermissionApplyInternal(Fragment fragment) {
        fragment.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + fragment.getActivity().getPackageName())), REQUEST_CODE_MANAGE_OVERLAY_PERMISSION);
    }


    // </editor-fold >


    public static class PLVPermissionFragment extends Fragment {


        private static IPLVOverlayPermissionListener permissionListener;

        public static void requestPermission(Activity activity, IPLVOverlayPermissionListener listener) {
            permissionListener = listener;
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (fragmentManager.findFragmentByTag(activity.getLocalClassName()) == null) {
                fragmentManager.beginTransaction()
                        .add(new PLVPermissionFragment(), activity.getLocalClassName())
                        .commitAllowingStateLoss();
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PLVFloatPermissionUtils.requestPermission(this);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_MANAGE_OVERLAY_PERMISSION) {
                // 需要延迟执行，不然即使授权，仍有部分机型获取不到权限
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (permissionListener != null) {
                            boolean result = PLVFloatPermissionUtils.checkPermission(getActivity());
                            permissionListener.onResult(result);
                            permissionListener = null;
                        }
                        getFragmentManager().beginTransaction().remove(PLVPermissionFragment.this).commitAllowingStateLoss();
                    }
                }, 500);
            }

        }


    }

    public interface IPLVOverlayPermissionListener {

        void onResult(boolean isGrant);
    }


}
