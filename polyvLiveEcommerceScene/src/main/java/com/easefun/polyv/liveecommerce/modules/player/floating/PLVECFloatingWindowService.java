package com.easefun.polyv.liveecommerce.modules.player.floating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * 浮窗服务
 */
public class PLVECFloatingWindowService extends Service {
    private PLVECFloatingWindowBinder floatingWindowBinder;

    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent();
        intent.setClass(context, PLVECFloatingWindowService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return floatingWindowBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        floatingWindowBinder = new PLVECFloatingWindowBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }
}
