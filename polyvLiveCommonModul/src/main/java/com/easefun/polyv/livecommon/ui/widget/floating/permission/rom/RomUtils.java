package com.easefun.polyv.livecommon.ui.widget.floating.permission.rom;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f/EasyFloat
 * @function: 判断手机ROM
 * @date: 2020-01-07  22:30
 */
public class RomUtils {

    private static String TAG = "RomUtils";

    /**
     * 获取 emui 版本号
     */
    public static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            if (emuiVersion != null) {
                String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
                return Double.parseDouble(version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (Exception ex) {
            Log.e(TAG, "Unable to read sysprop $propName", ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    public static boolean checkIsHuaweiRom() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    public static boolean checkIsMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static boolean checkIsMeizuRom() {
        String systemProperty = getSystemProperty("ro.build.display.id");
        if (!TextUtils.isEmpty(systemProperty)) {
            return false;
        } else {
            return systemProperty.contains("flyme") || systemProperty.toLowerCase().contains("flyme");
        }
    }

    public static boolean checkIs360Rom() {
        return Build.MANUFACTURER.contains("QiKU") || Build.MANUFACTURER.contains("360");
    }


    public static boolean checkIsOppoRom() {
        return Build.MANUFACTURER.contains("OPPO") || Build.MANUFACTURER.contains("oppo");
    }

    public static boolean checkIsVivoRom() {
        return Build.MANUFACTURER.contains("VIVO") || Build.MANUFACTURER.contains("vivo");
    }

}
