package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.plv.foundationsdk.log.PLVCommonLog;

import com.plv.foundationsdk.log.PLVCommonLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * date: 2020/11/18
 * author: HWilliamgo
 * description: 刘海屏工具类
 */
public class PLVNotchUtils {
    private static final String TAG = PLVNotchUtils.class.getSimpleName();
    private static final String HUAWEI = "HUAWEI";
    private static final String XIAOMI = "xiaomi";
    private static final String OPPO = "oppo";
    private static final String VIVO = "vivo";
    private static final String MEIZU = "meizu";

    private static final String GET_INT = "getInt";

    private static final String ANDROID_UTIL_FT_FEATURE = "android.util.FtFeature";
    private static final String ANDROID_OS_SYSTEM_PROPERTIES = "android.os.SystemProperties";
    private static final String COM_HUAWEI_ANDROID_UTIL_HW_NOTCH_SIZE_UTIL = "com.huawei.android.util.HwNotchSizeUtil";
    public static final String COM_OPPO_FEATURE_SCREEN_HETEROMORPHISM = "com.oppo.feature.screen.heteromorphism";
    private static final String FLYME_CONFIG_FLYME_FEATURE = "flyme.config.FlymeFeature";

    private static final String HAS_NOTCH_IN_SCREEN = "hasNotchInScreen";
    private static final String HAS_NOTCH_HW = "hasNotchHw:";
    private static final String HAS_NOTCH_XIAO_MI = "hasNotchXiaoMi:";
    private static final String HAS_NOTCH_MEIZU = "hasNotchMeizu:";
    private static final String RO_MIUI_NOTCH = "ro.miui.notch";
    private static final String HAS_NOTCH_VIVO = "hasNotchVIVO:";

    private static final String IS_FRINGE_DEVICE = "IS_FRINGE_DEVICE";
    private static final String IS_FEATURE_SUPPORT = "isFeatureSupport";

    /**
     * 是否有刘海屏
     *
     * @return
     */
    public static boolean hasNotchInScreen(Activity activity) {
        // 通过其他方式判断是否有刘海屏  目前官方提供有开发文档的就 小米，vivo，华为（荣耀），oppo
        String manufacturer = Build.MANUFACTURER;
        if (TextUtils.isEmpty(manufacturer)) {
            return false;
        } else if (manufacturer.equalsIgnoreCase(HUAWEI)) {
            return hasNotchHw(activity);
        } else if (manufacturer.equalsIgnoreCase(XIAOMI)) {
            return hasNotchXiaoMi(activity);
        } else if (manufacturer.equalsIgnoreCase(OPPO)) {
            return hasNotchOPPO(activity);
        } else if (manufacturer.equalsIgnoreCase(VIVO)) {
            return hasNotchVIVO(activity);
        } else if (manufacturer.equalsIgnoreCase(MEIZU)) {
            return hasNotchMeizu(activity);
        } else {
            return false;
        }
    }

    /**
     * 判断vivo是否有刘海屏
     * https://swsdl.vivo.com.cn/appstore/developer/uploadfile/20180328/20180328152252602.pdf
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchVIVO(Activity activity) {
        try {
            Class<?> c = Class.forName(ANDROID_UTIL_FT_FEATURE);
            Method get = c.getMethod(IS_FEATURE_SUPPORT, int.class);
            return (boolean) (get.invoke(c, 0x20));
        } catch (InvocationTargetException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_VIVO + e.getMessage());
        } catch (NoSuchMethodException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_VIVO + e.getMessage());
        } catch (IllegalAccessException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_VIVO + e.getMessage());
        } catch (ClassNotFoundException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_VIVO + e.getMessage());
        }
        return false;
    }

    /**
     * 判断oppo是否有刘海屏
     * https://open.oppomobile.com/wiki/doc#id=10159
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchOPPO(Activity activity) {
        return activity.getPackageManager().hasSystemFeature(COM_OPPO_FEATURE_SCREEN_HETEROMORPHISM);
    }

    /**
     * 判断xiaomi是否有刘海屏
     * https://dev.mi.com/console/doc/detail?pId=1293
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchXiaoMi(Activity activity) {
        try {
            Class<?> c = Class.forName(ANDROID_OS_SYSTEM_PROPERTIES);
            Method get = c.getMethod(GET_INT, String.class, int.class);
            return (int) (get.invoke(c, RO_MIUI_NOTCH, 0)) == 1;
        } catch (InvocationTargetException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_XIAO_MI + e.getMessage());
        } catch (NoSuchMethodException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_XIAO_MI + e.getMessage());
        } catch (IllegalAccessException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_XIAO_MI + e.getMessage());
        } catch (ClassNotFoundException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_XIAO_MI + e.getMessage());
        }
        return false;
    }

    /**
     * 判断华为是否有刘海屏
     * https://devcenter-test.huawei.com/consumer/cn/devservice/doc/50114
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchHw(Activity activity) {

        try {
            ClassLoader cl = activity.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass(COM_HUAWEI_ANDROID_UTIL_HW_NOTCH_SIZE_UTIL);
            Method get = HwNotchSizeUtil.getMethod(HAS_NOTCH_IN_SCREEN);
            return (boolean) get.invoke(HwNotchSizeUtil);
        } catch (Exception e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_HW + e.getMessage());
            return false;
        }
    }

    private static boolean hasNotchMeizu(Activity activity) {
        boolean fringeDevice = false;
        try {
            Class clazz = Class.forName(FLYME_CONFIG_FLYME_FEATURE);
            Field field = clazz.getDeclaredField(IS_FRINGE_DEVICE);
            fringeDevice = (boolean) field.get(null);
        } catch (IllegalAccessException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_MEIZU + e.getMessage());
        } catch (NoSuchFieldException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_MEIZU + e.getMessage());
        } catch (ClassNotFoundException e) {
            PLVCommonLog.d(TAG, HAS_NOTCH_MEIZU + e.getMessage());
        }
        return fringeDevice;
    }
}
