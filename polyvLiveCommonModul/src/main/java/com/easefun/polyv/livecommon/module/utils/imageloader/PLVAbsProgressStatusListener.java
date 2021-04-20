package com.easefun.polyv.livecommon.module.utils.imageloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * 图片加载进度状态监听，避免相同url+position的图片加载，会影响到另外一张已经加载完成/失败的图片，以及进度冲突
 * <p>
 * 内部下载失败/完成时会主动移除监听器
 */
public abstract class PLVAbsProgressStatusListener extends PLVAbsProgressListener {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_READY = 1;
    private static final int STATUS_FAILED = 2;
    private int status = STATUS_IDLE;
    private int progress = -1;//1-100

    public PLVAbsProgressStatusListener(String url) {
        super(url);
    }

    @Override
    public void onStart(String url) {
        if (status == STATUS_READY || status == STATUS_FAILED) {
            return;
        }
        onStartStatus(url);
    }

    @Override
    public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
        if (status == STATUS_READY || status == STATUS_FAILED || percentage <= progress) {
            return;
        }
        this.progress = percentage;
        onProgressStatus(url, isComplete, percentage, bytesRead, totalBytes);
    }

    @Override
    public void onFailed(@Nullable Exception e, Object model) {
        if (status == STATUS_READY) {
            return;
        }
        status = STATUS_FAILED;
        onFailedStatus(e, model);
    }

    @Override
    public void onResourceReady(Drawable drawable) {
        status = STATUS_READY;
        onResourceReadyStatus(drawable);
    }

    public abstract void onStartStatus(String url);

    public abstract void onProgressStatus(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    public abstract void onFailedStatus(@Nullable Exception e, Object model);

    public abstract void onResourceReadyStatus(Drawable drawable);
}
