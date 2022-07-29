package com.easefun.polyv.livecommon.ui.window;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVRotationObserver;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.util.List;

/**
 * 基础activity
 */
public class PLVBaseActivity extends AppCompatActivity {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private static final String TAG = "PLVBaseActivity";
    private static final int APP_STATUS_KILLED = 0; // 表示应用是被杀死后在启动的
    private static final int APP_STATUS_RUNNING = 1; // 表示应用时正常的启动流程
    private static int APP_STATUS = APP_STATUS_KILLED; // 记录App的启动状态
    // 页面方向管理器
    private PLVOrientationManager orientationManager;
    private PLVRotationObserver rotationObserver;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理异常启动时的相关方法">
    private void stopOnAbnormalLaunch() {
        PLVCommonLog.e(TAG, "App stop on abnormal launch");
        if (isLaunchActivityInheritFromBase() && restartApp()) {
            return;
        }
        stopApp();
    }

    private boolean restartApp() {
        try {
            Intent intent = new Intent(this, Class.forName(getLaunchActivityName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            stopApp();
            return true;
        } catch (Exception e) {
            PLVCommonLog.e(TAG, "restartApp:" + e.getMessage());
        }
        return false;
    }

    private static void stopApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private String getLaunchActivityName() {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(getPackageName());
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            return resolveInfos.get(0).activityInfo.name;
        }
        return null;
    }

    private boolean isLaunchActivityInheritFromBase() {
        try {
            return getLaunchActivityName() != null && PLVBaseActivity.class.isAssignableFrom(Class.forName(getLaunchActivityName()));
        } catch (Exception e) {
            PLVCommonLog.exception(e);
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && APP_STATUS == APP_STATUS_KILLED) {
            // 非正常启动流程，直接重新初始化应用界面
            stopOnAbnormalLaunch();
            return;
        }
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
            savedInstanceState.putParcelable("android:fragments", null);
        }
        super.onCreate(savedInstanceState);
        APP_STATUS = APP_STATUS_RUNNING;

        initOrientationManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orientationManager != null && enableRotationObserver()) {
            orientationManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orientationManager != null) {
            orientationManager.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationManager != null) {
            orientationManager.removeRotationObserver(rotationObserver);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (orientationManager != null) {
            orientationManager.notifyConfigurationChanged(this, newConfig);
        }
    }

    //新增的findViewById()方法，用于兼容support 25
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(@IdRes int id) {
        return (T) super.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面方向管理器">
    private void initOrientationManager() {
        rotationObserver = new PLVRotationObserver(this);
        orientationManager = PLVOrientationManager.getInstance();
        orientationManager.addRotationObserver(rotationObserver, enableRotationObserver());
    }

    /**
     * 是否开启重力感应屏幕旋转
     *
     * @return true表示开启屏幕旋转，false表示关闭屏幕旋转
     */
    protected boolean enableRotationObserver() {
        return false;
    }
    // </editor-fold>
}
