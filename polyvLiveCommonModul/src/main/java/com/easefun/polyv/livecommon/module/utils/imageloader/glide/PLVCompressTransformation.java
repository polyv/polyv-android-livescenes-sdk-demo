package com.easefun.polyv.livecommon.module.utils.imageloader.glide;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.thirdpart.blankj.utilcode.util.ImageUtils;

import java.io.File;
import java.security.MessageDigest;

/**
 * 压缩
 */
public class PLVCompressTransformation implements Transformation<Bitmap> {

    private static final String TAG = "PLVCompressTransformati";
    private static final int VERSION = 1;
    private static final String ID = "PLVCompressTransformation." + VERSION;
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private BitmapPool mBitmapPool;
    private String mUrl;

    public PLVCompressTransformation(Context context, String url) {
        this(url, Glide.get(context).getBitmapPool());
    }

    private PLVCompressTransformation(String url, BitmapPool pool) {
        mBitmapPool = pool;
        mUrl = url;
    }

    @Override
    public Resource<Bitmap> transform(Context context, Resource<Bitmap> resource, int outWidth, int outHeight) {
        if (new File(mUrl).isFile()) {
            try {
                String imageType = ImageUtils.getImageType(mUrl);
                if (imageType != null && "gif".equals(imageType.toLowerCase())) {
                    return resource;
                }
                Bitmap bitmap = PLVImageUtils.compressImage(mUrl);
                if (bitmap != null) {
                    return BitmapResource.obtain(bitmap, mBitmapPool);
                }
            } catch (Exception e) {
                PLVCommonLog.d(TAG,"transform："+e.getMessage());
            }
        }
        return resource;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PLVCompressTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}