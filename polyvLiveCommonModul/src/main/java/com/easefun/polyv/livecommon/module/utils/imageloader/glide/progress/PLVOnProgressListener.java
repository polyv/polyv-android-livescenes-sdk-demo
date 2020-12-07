package com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress;

import android.support.annotation.NonNull;

public abstract class PLVOnProgressListener {
    private PLVOnProgressListener listener;
    private String url;

    public PLVOnProgressListener(@NonNull String url) {
        this.url = url;
    }

    public PLVOnProgressListener transListener(PLVOnProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public PLVOnProgressListener getTransListener() {
        return listener;
    }

    public String getUrl() {
        return url;
    }

    public abstract void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    public abstract void onStart(String url);
}