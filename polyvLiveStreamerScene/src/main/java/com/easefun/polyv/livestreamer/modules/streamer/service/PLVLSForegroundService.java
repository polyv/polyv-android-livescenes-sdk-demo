package com.easefun.polyv.livestreamer.modules.streamer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.easefun.polyv.livestreamer.R;
import com.easefun.polyv.livestreamer.scenes.PLVLSLiveStreamerActivity;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * date: 2019/12/31
 *
 * @author hwj
 * description 前台服务。防止华为机型上锁屏超过1分钟导致摄像头预览画面卡住和断流。
 */
public class PLVLSForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public static void startService() {
        Context context = Utils.getApp();
        Intent serviceIntent = new Intent(context, PLVLSForegroundService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void stopService() {
        Intent serviceIntent = new Intent(ActivityUtils.getTopActivity(), PLVLSForegroundService.class);
        Utils.getApp().stopService(serviceIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, PLVLSLiveStreamerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("手机开播")
                .setSmallIcon(R.drawable.plvls_ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
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