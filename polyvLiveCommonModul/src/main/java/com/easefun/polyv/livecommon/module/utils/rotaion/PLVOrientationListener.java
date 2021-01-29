package com.easefun.polyv.livecommon.module.utils.rotaion;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * @author df
 * @create 2018/10/8
 * @Describe
 */
public class PLVOrientationListener extends OrientationEventListener {
    private static final String TAG = "PLVOrientationListener";
    private Activity context;
    private int orientation;

    public PLVOrientationListener(Context context) {
        this(context, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public PLVOrientationListener(Context context, int rate) {
        super(context, rate);
        initial(context);
    }

    void initial(Context context) {
        if (context instanceof Activity) {
            this.context = (Activity) context;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        int clips = Math.abs(this.orientation - orientation);
        if (listener == null || context == null || clips < 30 || clips > 330) {
            return;
        }
        this.orientation = orientation;
        int screenOrientation = context.getRequestedOrientation();
        PLVCommonLog.d(TAG, "onOrientationChanged:" + orientation);
        if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {    //设置竖屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                listener.onOrientationChanged(new Orientation(false, false));
            }
        } else if (orientation > 225 && orientation < 315) { //设置横屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                listener.onOrientationChanged(new Orientation(true, false));
            }
        } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                listener.onOrientationChanged(new Orientation(true, true));
            }
        } else if (orientation > 135 && orientation < 225) { //反向竖屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                listener.onOrientationChanged(new Orientation(false, true));
            }
        }
    }

    private OrientationListener listener;

    public void setOnOrientationListener(OrientationListener listener) {
        this.listener = listener;
    }

    public interface OrientationListener {
        void onOrientationChanged(Orientation orientation);
    }

    public class Orientation {
        private boolean isLandscape;
        private boolean isReverse;

        public Orientation(boolean isLandscape, boolean isReverse) {
            this.isLandscape = isLandscape;
            this.isReverse = isReverse;
        }

        public boolean isLandscape() {
            return isLandscape;
        }

        public boolean isReverse() {
            return isReverse;
        }
    }
}
