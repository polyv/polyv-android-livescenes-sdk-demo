package com.easefun.polyv.livecommon.module.utils.imageloader.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.easefun.polyv.livecommon.module.utils.imageloader.IPLVImageLoadEngine;
import com.easefun.polyv.livecommon.module.utils.imageloader.PLVAbsProgressListener;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVMyProgressManager;
import com.easefun.polyv.livecommon.module.utils.imageloader.glide.progress.PLVOnProgressListener;
import com.plv.foundationsdk.log.PLVCommonLog;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 用Glide做为图片加载引擎
 */
public class PLVGlideImageLoadEngine implements IPLVImageLoadEngine {
    private static final String TAG = "PLVGlideImageLoadEngine";

    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resId, ImageView imageView) {
        Glide.with(context).load(resId).into(imageView);
    }

    @Override
    @WorkerThread
    public File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException {
        return Glide.with(context)
                .asFile()
                .load(url)
                .submit()
                .get();
    }

    @Override
    public File saveImageAsFile(Context context, String url, Object urlTag) throws ExecutionException, InterruptedException {
        return Glide.with(context)
                .asFile()
                .load(url)
                .apply(new RequestOptions().signature(new ObjectKey(urlTag)))
                .submit()
                .get();
    }

    @Override
    public void loadImage(Context context, final String moduleTag, @NonNull final Object urlTag, @DrawableRes int errorRes, @NonNull PLVAbsProgressListener listener, final ImageView view) {
        final String url = listener.getUrl();
        PLVCommonLog.i(TAG, "moduleTag：" + moduleTag + "**urlTag：" + urlTag + "**url：" + url);
        PLVMyProgressManager.addListener(moduleTag, urlTag, new PLVOnProgressListener(listener.getUrl()) {
            @Override
            public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                PLVCommonLog.i(TAG, "onProgress url：" + url + "**" + isComplete + "**" + percentage + "**" + urlTag);
                if (getTransListener() != null) {//use final listener, only clear PLVOnProgressListener can leak, try use weakReference
                    getTransListener().onProgress(url, isComplete, percentage, bytesRead, totalBytes);
                }
            }


            @Override
            public void onStart(String url) {
                PLVCommonLog.i(TAG, "onStart url：" + url + "**" + urlTag);
                if (getTransListener() != null) {
                    getTransListener().onStart(url);
                }
            }
        }.transListener(listener));
        Glide.with(context)
                .load(listener.getUrl())//same url can call existed listener, no call this listener, can use signature process
                .apply(new RequestOptions().fitCenter()//fit center drawable wh, and imageView scaleType fit drawable display
                        .error(errorRes)
                        .signature(new ObjectKey(urlTag))
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)//if use, big img reopen can reload
//                        .skipMemoryCache(true)
//                        .transform(new PLVCompressTransformation(context, url))
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        PLVCommonLog.i(TAG, "onLoadFailed url：" + url + "**" + urlTag + "**" + (e != null ? e.getMessage() : ""));
                        PLVOnProgressListener l = PLVMyProgressManager.getProgressListener(moduleTag, urlTag);
                        //use PLVMyProgressManager.getProgressListener(moduleTag, urlTag) instead listener, because RequestListener and listener can not cancel
                        if (l instanceof PLVAbsProgressListener) {
                            ((PLVAbsProgressListener) l).onFailed(e, model);//loadFailed can auto reload, no remove listener, but destroy no touch
                        }
                        PLVMyProgressManager.removeListener(moduleTag, urlTag);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        PLVCommonLog.i(TAG, "onResourceReady1 url：" + url + "**" + urlTag + "**" + view.getWidth() + "**" + view.getHeight());
                        PLVOnProgressListener l = PLVMyProgressManager.getProgressListener(moduleTag, urlTag);
                        if (l != null) {
                            l.onProgress(l.getUrl(), true, 100, 0, 0);
                        }
                        return false;
                    }
                })
                .into(new ViewTarget<ImageView, Drawable>(view) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        PLVCommonLog.i(TAG, "onResourceReady2 url：" + url + "**" + urlTag + "**" + resource.getIntrinsicWidth() + "**" + resource.getIntrinsicHeight());
                        PLVOnProgressListener l = PLVMyProgressManager.getProgressListener(moduleTag, urlTag);
                        if (l instanceof PLVAbsProgressListener) {
                            ((PLVAbsProgressListener) l).onResourceReady(resource);
                        }
                        PLVMyProgressManager.removeListener(moduleTag, urlTag);
                        if (resource instanceof GifDrawable) {
                            ((GifDrawable) resource).start();//显示gif
                        }
                    }

                    @Override
                    public void onDestroy() {
                        PLVCommonLog.i(TAG, "onDestroy url：" + url + "**" + urlTag);
                        PLVMyProgressManager.removeListener(moduleTag, urlTag);//only touch failed or ready task...
                    }
                }.waitForLayout());//(can not need, or wh unset. wh correct but display wh error)(iv code pb cause)
    }


    @Override
    public void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeHolder)
                        .error(error)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, String url, int placeHolder, int error, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeHolder)
                        .error(error))
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, String url, int placeHolder, int error, ImageView imageView, int radius) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeHolder)
                        .error(error).transform(new RoundedCorners(radius)))
                .into(imageView);
    }
}
