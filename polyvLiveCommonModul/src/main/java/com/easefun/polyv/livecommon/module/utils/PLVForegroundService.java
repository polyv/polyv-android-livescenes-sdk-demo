package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.LogUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

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

    /**
     * 启动前台服务
     *
     * @param activityToJump 当点击前台服务的通知，要跳转的activity
     */
    public static void startForegroundService(Class<? extends Activity> activityToJump, String title, int icon) {
        PLVForegroundService.activityToJump = activityToJump;
        PLVForegroundService.title = title;
        PLVForegroundService.icon = icon;
        final Context context = Utils.getApp();
        final Intent serviceIntent = new Intent(context, PLVForegroundService.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }, 500);
    }

    /**
     * 停止前台服务
     */
    public static void stopForegroundService() {
        Intent serviceIntent = new Intent(ActivityUtils.getTopActivity(), PLVForegroundService.class);
        Utils.getApp().stopService(serviceIntent);
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
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
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
}
