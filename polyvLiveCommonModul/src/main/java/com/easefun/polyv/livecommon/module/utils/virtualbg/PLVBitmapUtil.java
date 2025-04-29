package com.easefun.polyv.livecommon.module.utils.virtualbg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PLVBitmapUtil {

    /**
     * 根据资源 ID 生成 ARGB_8888 格式的 Bitmap
     *
     * @param context 上下文
     * @param resId   图片资源 ID
     * @return ARGB_8888 格式的 Bitmap，如果生成失败则返回 null
     */
    public static Bitmap getBitmapFromResource(Context context, int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (bitmap != null && bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            // 转换为 ARGB_8888 格式
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        return bitmap;
    }

    /**
     * 根据图片文件路径生成 ARGB_8888 格式的 Bitmap
     *
     * @param path 图片文件的完整路径
     * @return ARGB_8888 格式的 Bitmap，如果生成失败则返回 null
     */
    public static Bitmap getBitmapFromPath(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null && bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            // 转换为 ARGB_8888 格式
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }
        return bitmap;
    }
}

