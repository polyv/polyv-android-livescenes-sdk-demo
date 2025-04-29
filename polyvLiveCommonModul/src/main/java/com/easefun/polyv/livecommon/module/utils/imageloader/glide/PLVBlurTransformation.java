package com.easefun.polyv.livecommon.module.utils.imageloader.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class PLVBlurTransformation extends BitmapTransformation {
    private static final String ID = "PLVBlurTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final int blurRadius;
    private final Context context;
    private final boolean needBlur;

    public PLVBlurTransformation(Context context, int blurRadius) { // 0-50
        this.context = context.getApplicationContext();
        this.blurRadius = Math.min(Math.max(blurRadius / 2, 1), 25); // 限制模糊半径1-25
        this.needBlur = blurRadius > 0;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (!needBlur) {
            return toTransform;
        }
        // 创建缩放后的Bitmap（提升性能）
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(toTransform,
                toTransform.getWidth() / 4,
                toTransform.getHeight() / 4,
                false);

        // 使用RenderScript进行模糊
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, scaledBitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(blurRadius);
        script.setInput(input);
        script.forEach(output);

        // 将结果复制到新Bitmap
        Bitmap blurredBitmap = Bitmap.createBitmap(scaledBitmap.getWidth(),
                scaledBitmap.getHeight(),
                scaledBitmap.getConfig());
        output.copyTo(blurredBitmap);

        // 清理资源
        rs.destroy();
        scaledBitmap.recycle();

        return blurredBitmap;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        messageDigest.update((byte) blurRadius);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PLVBlurTransformation && ((PLVBlurTransformation) o).blurRadius == blurRadius;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + blurRadius * 10;
    }
}
