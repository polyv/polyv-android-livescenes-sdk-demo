package com.easefun.polyv.livecommon.module.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * 系统方法工具类
 */
public class PLVSystemUtils {

    /**
     * 获取AndroidId
     */
    public static String getAndroidId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
