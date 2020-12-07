package com.easefun.polyv.livecommon.module.utils.imageloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVOnProgressListener;


/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 图片加载监听
 */
public abstract class PLVAbsProgressListener extends PLVOnProgressListener {
    public PLVAbsProgressListener(String url) {
        super(url);
    }

    public abstract void onFailed(@Nullable Exception e, Object model);

    public abstract void onResourceReady(Drawable drawable);
}
