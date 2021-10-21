package com.easefun.polyv.livecommon.module.utils.media;

/**
 * @Title: CameraListener
 * @Package com.easefun.polyvrtmp.sopcast.camera
 * @Description:
 * @Author Jim
 * @Date 16/7/18
 * @Time 上午10:42
 * @Version
 */
public interface PLVCameraListener {
    int CAMERA_NOT_SUPPORT = 1;
    int NO_CAMERA = 2;
    int CAMERA_DISABLED = 3;
    int CAMERA_OPEN_FAILED = 4;

    void onOpenSuccess();

    void onOpenFail(Throwable t, int error);

    void onCameraChange();
}
