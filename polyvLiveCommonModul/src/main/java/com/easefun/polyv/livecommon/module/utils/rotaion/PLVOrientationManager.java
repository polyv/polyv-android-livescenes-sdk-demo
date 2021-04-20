package com.easefun.polyv.livecommon.module.utils.rotaion;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面方向管理器
 */
public class PLVOrientationManager {
    private static volatile PLVOrientationManager singleton = null;
    // 重力感应旋转观察者
    private List<PLVRotationObserver> rotationObservers;
    private PLVRotationObserver lastRotationObserver;
    // 即将要请求页面方向的监听器
    private List<OnRequestedOrientationListener> orientationListeners;
    // 页面方向改变之后的监听器
    private List<OnConfigurationChangedListener> configurationChangedListeners;

    private PLVOrientationManager() {
        rotationObservers = new ArrayList<>();
        orientationListeners = new ArrayList<>();
        configurationChangedListeners = new ArrayList<>();
    }

    public static PLVOrientationManager getInstance() {
        if (singleton == null) {
            synchronized (PLVOrientationManager.class) {
                if (singleton == null) {
                    singleton = new PLVOrientationManager();
                }
            }
        }
        return singleton;
    }

    // 添加重力感应旋转观察者，在页面中使用PLVOrientationManager的API，需先添加RotationObserver
    public void addRotationObserver(final PLVRotationObserver observer) {
        if (observer != null && !rotationObservers.contains(observer)) {
            observer.setOrientationListener(new PLVOrientationListener.OrientationListener() {
                @Override
                public void onOrientationChanged(PLVOrientationListener.Orientation orientation) {
                    if (orientation.isLandscape()) {
                        setLandscape(observer.getContext(), orientation.isReverse());
                    } else {
                        setPortrait(observer.getContext(), orientation.isReverse());
                    }
                }
            });
            rotationObservers.add(observer);
            updateLastRationObserver();
        }
    }

    // 移除重力感应旋转观察者，离开页面时，需要移除RotationObserver
    public void removeRotationObserver(PLVRotationObserver observer) {
        rotationObservers.remove(observer);
        updateLastRationObserver();
    }

    private void updateLastRationObserver() {
        if (!rotationObservers.isEmpty()) {
            lastRotationObserver = rotationObservers.get(rotationObservers.size() - 1);
        }
    }

    // 开启重力感应旋屏
    public void start() {
        if (lastRotationObserver != null) {
            lastRotationObserver.start();
        }
    }

    // 停止重力感应旋屏
    public void stop() {
        stop(true);
    }

    // 停止重力感应旋屏
    public void stop(boolean isLifecycleStop) {
        if (lastRotationObserver != null) {
            lastRotationObserver.stop(isLifecycleStop);
        }
    }

    // 锁定屏幕旋转
    public void lockOrientation() {
        if (lastRotationObserver != null) {
            lastRotationObserver.lockOrientation();
        }
    }

    // 解锁屏幕旋转
    public void unlockOrientation() {
        if (lastRotationObserver != null) {
            lastRotationObserver.unlockOrientation();
        }
    }

    // 设置竖屏
    public void setPortrait(Activity activity) {
        setPortrait(activity, false);
    }

    // 设置竖屏
    public void setPortrait(Activity activity, boolean isReverse) {
        if (lastRotationObserver != null && lastRotationObserver.isLockOrientation()) {
            return;
        }
        activity.setRequestedOrientation(isReverse ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        if (orientationListeners != null) {
            for (OnRequestedOrientationListener orientationListener : orientationListeners) {
                orientationListener.onCall(activity, false);
            }
        }
    }

    // 设置横屏
    public void setLandscape(Activity activity) {
        setLandscape(activity, false);
    }

    // 设置横屏
    public void setLandscape(Activity activity, boolean isReverse) {
        if (lastRotationObserver != null && lastRotationObserver.isLockOrientation()) {
            return;
        }
        activity.setRequestedOrientation(isReverse ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        if (orientationListeners != null) {
            for (OnRequestedOrientationListener orientationListener : orientationListeners) {
                orientationListener.onCall(activity, true);
            }
        }
    }

    // 通知页面方向发生改变
    public void notifyConfigurationChanged(Activity activity, Configuration newConfig) {
        if (configurationChangedListeners != null) {
            for (OnConfigurationChangedListener configurationChangedListener : configurationChangedListeners) {
                configurationChangedListener.onCall(activity, newConfig.orientation
                        == Configuration.ORIENTATION_LANDSCAPE);
            }
        }
    }

    // 添加请求页面方向监听器
    public void addOnRequestedOrientationListener(OnRequestedOrientationListener listener) {
        if (listener != null && !orientationListeners.contains(listener)) {
            orientationListeners.add(listener);
        }
    }

    // 移除请求页面方向监听器
    public void removeOnRequestedOrientationListener(OnRequestedOrientationListener listener) {
        orientationListeners.remove(listener);
    }

    // 添加页面方向改变之后的监听器
    public void addOnConfigurationChangedListener(OnConfigurationChangedListener listener) {
        if (listener != null && !configurationChangedListeners.contains(listener)) {
            configurationChangedListeners.add(listener);
        }
    }

    // 移除页面方向改变之后的监听器
    public void removeOnConfigurationChangedListener(OnConfigurationChangedListener listener) {
        configurationChangedListeners.remove(listener);
    }

    // 即将要请求页面方向的监听器
    public interface OnRequestedOrientationListener {
        // 即将要请求的页面方向
        void onCall(Context context, boolean isRequestedLandscape);
    }

    // 页面方向改变之后的监听器
    public interface OnConfigurationChangedListener {
        // 改变之后的页面方向
        void onCall(Context context, boolean isLandscape);
    }
}
