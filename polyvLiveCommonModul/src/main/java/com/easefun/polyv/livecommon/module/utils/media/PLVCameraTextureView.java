package com.easefun.polyv.livecommon.module.utils.media;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;

import com.easefun.polyv.livecommon.module.utils.media.exception.PLVCameraDisabledException;
import com.easefun.polyv.livecommon.module.utils.media.exception.PLVCameraHardwareException;
import com.easefun.polyv.livecommon.module.utils.media.exception.PLVCameraNotSupportException;
import com.easefun.polyv.livecommon.module.utils.media.exception.PLVNoCameraException;
import com.plv.thirdpart.blankj.utilcode.util.ScreenUtils;

public class PLVCameraTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private boolean curCameraFront = true;
    private PLVCameraListener mCameraOpenListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isSurfaceTextureAvailable;
    private boolean isCallCameraStart;

    public PLVCameraTextureView(Context context) {
        this(context, null);
    }

    public PLVCameraTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVCameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        isSurfaceTextureAvailable = true;
        if (isCallCameraStart) {
            startCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setCameraOpenListener(PLVCameraListener cameraOpenListener) {
        this.mCameraOpenListener = cameraOpenListener;
    }

    public boolean switchCamera(boolean front) {
        return switchCamera(front, null);
    }

    public boolean switchCamera(boolean front, Runnable runnable) {
        if (curCameraFront == front) {
            return false;
        }
        boolean result = PLVCameraHolder.instance().switchCamera(runnable);
        if (!result) {
            return false;
        }
        curCameraFront = front;
        if (mCameraOpenListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCameraOpenListener.onCameraChange();
                }
            });
        }
        return true;
    }

    public void startPreview() {
        PLVCameraHolder.instance().startPreview();
    }

    public void stopPreview() {
        PLVCameraHolder.instance().stopPreview();
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        PLVCameraHolder.instance().releaseCamera();
        PLVCameraHolder.instance().release();
    }

    public void startCamera() {
        if (!isSurfaceTextureAvailable) {
            isCallCameraStart = true;
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    PLVCameraUtils.checkCameraService(getContext());
                } catch (PLVCameraDisabledException e) {
                    postOpenCameraError(e, PLVCameraListener.CAMERA_DISABLED);
                    e.printStackTrace();
                    return;
                } catch (PLVNoCameraException e) {
                    postOpenCameraError(e, PLVCameraListener.NO_CAMERA);
                    e.printStackTrace();
                    return;
                }

                PLVCameraConfiguration.Orientation orientation = ScreenUtils.isLandscape() ? PLVCameraConfiguration.Orientation.LANDSCAPE : PLVCameraConfiguration.Orientation.PORTRAIT;
                PLVCameraConfiguration cameraConfiguration = new PLVCameraConfiguration.Builder()
                        .setOrientation(orientation)
                        .setPreview(getHeight(), getWidth())
                        .build();
                PLVCameraHolder.instance().setConfiguration(cameraConfiguration);

                PLVCameraHolder.State state = PLVCameraHolder.instance().getState();
                PLVCameraHolder.instance().setSurfaceTexture(getSurfaceTexture());
                if (state != PLVCameraHolder.State.PREVIEW) {
                    try {
                        PLVCameraHolder.instance().openCamera();
                        PLVCameraHolder.instance().startPreview();
                        if (mCameraOpenListener != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mCameraOpenListener.onOpenSuccess();
                                }
                            });
                        }
                    } catch (PLVCameraHardwareException e) {
                        e.printStackTrace();
                        postOpenCameraError(e, PLVCameraListener.CAMERA_OPEN_FAILED);
                    } catch (PLVCameraNotSupportException e) {
                        e.printStackTrace();
                        postOpenCameraError(e, PLVCameraListener.CAMERA_NOT_SUPPORT);
                    }
                }
            }
        });
    }

    private void postOpenCameraError(final Throwable t, final int error) {
        if (mCameraOpenListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCameraOpenListener != null) {
                        mCameraOpenListener.onOpenFail(t, error);
                    }
                }
            });
        }
    }
}
