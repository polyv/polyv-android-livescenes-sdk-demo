package com.easefun.polyv.livecommon.module.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.permission.PLVFastPermission;
import com.plv.foundationsdk.rx.PLVRxTimer;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.AppUtils;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * date: 2020/7/24
 * author: hwj
 * description:  前台服务。防止华为机型上锁屏超过1分钟导致摄像头预览画面卡住和断流。
 */
public class PLVForegroundService extends Service {
    private static final String TAG = "PLVLCLinkMicForegroundService";
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static Class<? extends Activity> activityToJump;
    private static String title;
    private static int icon;
    private static final ForegroundHandler foregroundHandler = new ForegroundHandler();

    /**
     * 启动前台服务
     *
     * @param activityToJump 当点击前台服务的通知，要跳转的activity
     */
    public static void startForegroundService(Class<? extends Activity> activityToJump, String title, int icon) {
        // 检查摄像头权限
        if (!PLVFastPermission.hasPermission(Utils.getApp(), Manifest.permission.CAMERA)) {
            PLVCommonLog.e(TAG, "no camera permission");
            return;
        }
        PLVForegroundService.activityToJump = activityToJump;
        PLVForegroundService.title = title;
        PLVForegroundService.icon = icon;
        final Context context = Utils.getApp();
        final Intent serviceIntent = new Intent(context, PLVForegroundService.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                foregroundHandler.start(new Runnable() {
                    @Override
                    public void run() {
                        ContextCompat.startForegroundService(context, serviceIntent);
                    }
                });
            }
        }, 500);
    }

    /**
     * 停止前台服务
     */
    public static void stopForegroundService() {
        Intent serviceIntent = new Intent(ActivityUtils.getTopActivity(), PLVForegroundService.class);
        Utils.getApp().stopService(serviceIntent);
        foregroundHandler.stop();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (activityToJump == null) {
            PLVCommonLog.e(TAG, "activityToJump = null");
            return START_NOT_STICKY;
        }

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, activityToJump);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);
        } else {
            startForeground(1, notification);
        }
        LogUtils.d("onStartCommand");
        /***
         * do heavy work on a background thread
         * stopSelf();
         */
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    public static class ForegroundHandler {
        private Runnable runnable;
        private Disposable disposable;

        public void start(@NonNull Runnable runnable) {
            if (AppUtils.isAppForeground()) {
                runnable.run();
            } else {
                this.runnable = runnable;
                startTimer();
            }
        }

        public void stop() {
            runnable = null;
            dispose();
            disposable = null;
        }

        private void dispose() {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }

        private void startTimer() {
            dispose();
            this.disposable = PLVRxTimer.timer(3000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    if (AppUtils.isAppForeground()) {
                        if (runnable != null) {
                            runnable.run();
                            runnable = null;
                        }
                        dispose();
                    }
                }
            });
        }
    }
}
