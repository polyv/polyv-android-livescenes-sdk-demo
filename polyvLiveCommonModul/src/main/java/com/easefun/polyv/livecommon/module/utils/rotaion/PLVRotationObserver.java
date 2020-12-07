package com.easefun.polyv.livecommon.module.utils.rotaion;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

public class PLVRotationObserver {
    private PLVOrientationListener orientationListener;
    private Activity context;
    private RotationObserver rotationObserver;
    // 是否是生命周期方法中调用了stop
    private boolean isLifecycleStop;
    // 锁定旋转屏幕
    private boolean lockOrientation;

    public PLVRotationObserver(Activity context) {
        this.context = context;
        rotationObserver = new RotationObserver(new Handler());
        orientationListener = new PLVOrientationListener(context);
    }

    public Activity getContext() {
        return context;
    }

    public void setOrientationListener(PLVOrientationListener.OrientationListener listener) {
        orientationListener.setOnOrientationListener(listener);
    }

    public void start() {
        if (lockOrientation) {
            return;
        }
        isLifecycleStop = false;
        boolean autoRotateOn = (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        // 检查系统是否开启自动旋转
        if (autoRotateOn) {
            orientationListener.enable();
        } else {
            orientationListener.disable();
        }
        rotationObserver.startObserver();
    }

    public void stop() {
        stop(true);
    }

    public void stop(boolean isLifecycleStop) {
        rotationObserver.stopObserver();
        orientationListener.disable();
        if (isLifecycleStop) {
            this.isLifecycleStop = true;
        }
    }

    // 锁定屏幕旋转
    public void lockOrientation() {
        lockOrientation = true;
        stop(false);
    }

    // 解锁屏幕旋转
    public void unlockOrientation() {
        lockOrientation = false;
        if (!isLifecycleStop) {
            start();
        }
    }

    public boolean isLockOrientation() {
        return lockOrientation;
    }

    private class RotationObserver extends ContentObserver {
        ContentResolver mResolver;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public RotationObserver(Handler handler) {
            super(handler);
            mResolver = context.getContentResolver();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean autoRotateOn = (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
            //检查系统是否开启自动旋转
            if (autoRotateOn) {
                if (orientationListener != null) {
                    orientationListener.enable();
                }
            } else {
                if (orientationListener != null) {
                    orientationListener.disable();
                }
            }
        }
    }
}
