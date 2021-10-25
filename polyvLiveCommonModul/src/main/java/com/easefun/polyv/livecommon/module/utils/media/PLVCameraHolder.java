package com.easefun.polyv.livecommon.module.utils.media;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.easefun.polyv.livecommon.module.utils.media.exception.PLVCameraHardwareException;
import com.easefun.polyv.livecommon.module.utils.media.exception.PLVCameraNotSupportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CameraHolder
 * @Package com.youku.crazytogether.app.modules.sopCastV2
 * @Description:
 * @Author Jim
 * @Date 16/3/23
 * @Time 上午11:57
 * @Version
 */
@TargetApi(14)
public class PLVCameraHolder {
    private static final String TAG = "CameraHolder";
    private final static int FOCUS_WIDTH = 80;
    private final static int FOCUS_HEIGHT = 80;

    private List<PLVCameraData> mCameraDatas;
    private Camera mCameraDevice;
    private PLVCameraData mCameraData;
    private State mState;
    private SurfaceTexture mTexture;
    private boolean isTouchMode = false;
    private boolean isOpenBackFirst = false;
    private PLVCameraConfiguration mConfiguration = PLVCameraConfiguration.createDefault();

    public enum State {
        INIT,
        OPENED,
        PREVIEW
    }

    private static PLVCameraHolder sHolder;

    public static synchronized PLVCameraHolder instance() {
        if (sHolder == null) {
            sHolder = new PLVCameraHolder();
        }
        return sHolder;
    }

    private PLVCameraHolder() {
        mState = State.INIT;
    }

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public PLVCameraData getCameraData() {
        return mCameraData;
    }

    public boolean isLandscape() {
        return (mConfiguration.orientation != PLVCameraConfiguration.Orientation.PORTRAIT);
    }

    public synchronized Camera openCamera()
            throws PLVCameraHardwareException, PLVCameraNotSupportException {
        if (mCameraDatas == null || mCameraDatas.size() == 0) {
            mCameraDatas = PLVCameraUtils.getAllCamerasData(isOpenBackFirst);
        }
        PLVCameraData cameraData = mCameraDatas.get(0);
        if (mCameraDevice != null && mCameraData == cameraData) {
            return mCameraDevice;
        }
        if (mCameraDevice != null) {
            releaseCamera();
        }
        try {
            Log.d(TAG, "open camera " + cameraData.cameraID);
            mCameraDevice = Camera.open(cameraData.cameraID);
        } catch (RuntimeException e) {
            Log.e(TAG, "fail to connect Camera");
            throw new PLVCameraHardwareException(e);
        }
        if (mCameraDevice == null) {
            throw new PLVCameraNotSupportException("init camera fail");
        }
        try {
            PLVCameraUtils.initCameraParams(mCameraDevice, cameraData, isTouchMode, mConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
            mCameraDevice.release();
            mCameraDevice = null;
            throw new PLVCameraNotSupportException(e.getMessage());
        }
        mCameraData = cameraData;
        mState = State.OPENED;
        return mCameraDevice;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        mTexture = texture;
        if (mState == State.PREVIEW && mCameraDevice != null && mTexture != null) {
            try {
                mCameraDevice.setPreviewTexture(mTexture);
            } catch (IOException e) {
                releaseCamera();
            }
        }
    }

    public State getState() {
        return mState;
    }

    public void setConfiguration(PLVCameraConfiguration configuration) {
        isTouchMode = (configuration.focusMode != PLVCameraConfiguration.FocusMode.AUTO);
        isOpenBackFirst = (configuration.facing != PLVCameraConfiguration.Facing.FRONT);
        mConfiguration = configuration;
    }

    public synchronized void startPreview() {
        startPreview(null);
    }

    public synchronized void startPreview(final Runnable runnable) {
        if (mState != State.OPENED) {
            return;
        }
        if (mCameraDevice == null) {
            return;
        }
        if (mTexture == null) {
            return;
        }
        try {
            mCameraDevice.setPreviewTexture(mTexture);
            mCameraDevice.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
            mCameraDevice.startPreview();
            mState = State.PREVIEW;
        } catch (Exception e) {
            releaseCamera();
            e.printStackTrace();
        }
    }

    public synchronized void stopPreview() {
        if (mState != State.PREVIEW) {
            return;
        }
        if (mCameraDevice == null) {
            return;
        }
        mCameraDevice.setOneShotPreviewCallback(null);
        mCameraDevice.setPreviewCallback(null);
        Camera.Parameters cameraParameters = null;
        try {
            cameraParameters = mCameraDevice.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            mCameraDevice.stopPreview();
            mState = State.OPENED;
            return;
        }
        if (cameraParameters != null && cameraParameters.getFlashMode() != null
                && !cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mCameraDevice.setParameters(cameraParameters);
        mCameraDevice.stopPreview();
        mState = State.OPENED;
    }

    public synchronized void releaseCamera() {
        if (mState == State.PREVIEW) {
            stopPreview();
        }
        if (mState != State.OPENED) {
            return;
        }
        if (mCameraDevice == null) {
            return;
        }
        mCameraDevice.release();
        mCameraDevice = null;
        mCameraData = null;
        mState = State.INIT;
    }

    public void release() {
        mCameraDatas = null;
        mTexture = null;
        isTouchMode = false;
        isOpenBackFirst = false;
        mConfiguration = PLVCameraConfiguration.createDefault();
    }

    public void setFocusPoint(int x, int y) {
        if (mState != State.PREVIEW || mCameraDevice == null) {
            return;
        }
        if (x < -1000 || x > 1000 || y < -1000 || y > 1000) {
            Log.w(TAG, "setFocusPoint: values are not ideal " + "x= " + x + " y= " + y);
            return;
        }
        Camera.Parameters params = mCameraDevice.getParameters();

        if (params != null && params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusArea = new ArrayList<Camera.Area>();
            focusArea.add(new Camera.Area(new Rect(x, y, x + FOCUS_WIDTH, y + FOCUS_HEIGHT), 1000));

            params.setFocusAreas(focusArea);

            try {
                mCameraDevice.setParameters(params);
            } catch (Exception e) {
                // Ignore, we might be setting it too
                // fast since previous attempt
            }
        } else {
            Log.w(TAG, "Not support Touch focus mode");
        }
    }

    public boolean doAutofocus(Camera.AutoFocusCallback focusCallback) {
        if (mState != State.PREVIEW || mCameraDevice == null) {
            return false;
        }
        // Make sure our auto settings aren't locked
        Camera.Parameters params = mCameraDevice.getParameters();
        if (params.isAutoExposureLockSupported()) {
            params.setAutoExposureLock(false);
        }

        if (params.isAutoWhiteBalanceLockSupported()) {
            params.setAutoWhiteBalanceLock(false);
        }

        mCameraDevice.setParameters(params);
        mCameraDevice.cancelAutoFocus();
        mCameraDevice.autoFocus(focusCallback);
        return true;
    }

    public void changeFocusMode(boolean touchMode) {
        if (mState != State.PREVIEW || mCameraDevice == null || mCameraData == null) {
            return;
        }
        isTouchMode = touchMode;
        mCameraData.touchFocusMode = touchMode;
        if (touchMode) {
            PLVCameraUtils.setTouchFocusMode(mCameraDevice);
        } else {
            PLVCameraUtils.setAutoFocusMode(mCameraDevice);
        }
    }

    public void switchFocusMode() {
        changeFocusMode(!isTouchMode);
    }

    public float cameraZoom(boolean isBig) {
        if (mState != State.PREVIEW || mCameraDevice == null || mCameraData == null) {
            return -1;
        }
        Camera.Parameters params = mCameraDevice.getParameters();
        if (isBig) {
            params.setZoom(Math.min(params.getZoom() + 1, params.getMaxZoom()));
        } else {
            params.setZoom(Math.max(params.getZoom() - 1, 0));
        }
        mCameraDevice.setParameters(params);
        return (float) params.getZoom() / params.getMaxZoom();
    }

    public boolean switchCamera() {
        return switchCamera(null);
    }

    public boolean switchCamera(Runnable runnable) {
        if (mState != State.PREVIEW) {
            return false;
        }
        try {
            PLVCameraData camera = mCameraDatas.remove(1);
            mCameraDatas.add(0, camera);
            openCamera();
            startPreview(runnable);
            return true;
        } catch (Exception e) {
            try {
                PLVCameraData camera = mCameraDatas.remove(1);
                mCameraDatas.add(0, camera);
                openCamera();
                startPreview(runnable);
            } catch (Exception e1) {
                //todo 部分android设备会切换失败
                e1.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean switchLight() {
        if (mState != State.PREVIEW || mCameraDevice == null || mCameraData == null) {
            return false;
        }
        if (!mCameraData.hasLight) {
            return false;
        }
        Camera.Parameters cameraParameters = mCameraDevice.getParameters();
        if (cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        try {
            mCameraDevice.setParameters(cameraParameters);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
