package com.easefun.polyv.livecommon.module.utils.imageloader;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 图片加载引擎
 */
public interface IPLVImageLoadEngine {
    void loadImage(Context context, String url, ImageView imageView);

    void loadImage(Context context, @DrawableRes int resId, ImageView imageView);

    @WorkerThread
    File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException;

    @WorkerThread
    File saveImageAsFile(Context context, String url, Object urlTag) throws ExecutionException, InterruptedException;

    void loadImage(Context context, final String moduleTag, final Object urlTag, @DrawableRes int errorRes,
                   @NonNull final PLVAbsProgressListener listener, final ImageView view);

    void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder,
                              @DrawableRes int error, ImageView imageView);

    void loadImage(Context context, String url, @DrawableRes int placeHolder,
                   @DrawableRes int error, ImageView imageView);

    void loadImage(Context context, String url, @DrawableRes int placeHolder,
                   @DrawableRes int error, ImageView imageView, int radius);
}
