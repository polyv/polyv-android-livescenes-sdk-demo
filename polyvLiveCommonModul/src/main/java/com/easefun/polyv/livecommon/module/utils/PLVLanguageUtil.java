package com.easefun.polyv.livecommon.module.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.RequiresApi;
import android.support.v4.os.ConfigurationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.easefun.polyv.livecommon.module.modules.player.floating.PLVFloatingPlayerManager;
import com.easefun.polyv.livecommon.module.utils.rotaion.PLVOrientationManager;
import com.plv.foundationsdk.log.PLVCommonLog;
import com.plv.foundationsdk.utils.PLVAppUtils;
import com.plv.livescenes.model.PLVLiveClassDetailVO;
import com.plv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.plv.thirdpart.blankj.utilcode.util.SPUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 语言切换工具
 */
public class PLVLanguageUtil {
    private static final String TAG = PLVLanguageUtil.class.getSimpleName();
    private static final String SP_KEY = "plv_language_switch_type_key";
    private static final String LANGUAGE_ZH = "zh";
    private static final boolean DISPLAY_SIMPLIFIED_FOR_TRADITIONAL = true;
    public static final int LANGUAGE_FOLLOW_SYSTEM = 0; // 跟随系统
    public static final int LANGUAGE_EN = 1; //英文
    public static final int LANGUAGE_CHINESE_SIMPLIFIED = 2; // 简体
    public static final int LANGUAGE_CHINESE_TRADITIONAL = 3; // 繁体
    public static final int LANGUAGE_JAPANESE = 4; // 日文
    public static final int LANGUAGE_KOREAN = 5; // 韩文
    private static String lastSaveKeyOrType = "";
    private static Locale userLocale = null;

    private static final Map<String, Integer> LANG_MAP = new HashMap<String, Integer>() {
        {
            // 目前后台还不支持添加 日文、韩文、繁体
            put(PLVLiveClassDetailVO.LangType.LANG_TYPE_FOLLOW_SYSTEM, LANGUAGE_FOLLOW_SYSTEM);
            put(PLVLiveClassDetailVO.LangType.LANG_TYPE_ZH_CN, LANGUAGE_CHINESE_SIMPLIFIED);
            put(PLVLiveClassDetailVO.LangType.LANG_TYPE_EN, LANGUAGE_EN);
        }
    };

    public static void recreateWithLanguage(String channelId, int languageType, Activity activity) {
        String key = SP_KEY + "_" + channelId;
        lastSaveKeyOrType = key;
        SPUtils.getInstance().put(key, languageType);
        updateConfiguration(activity);
        if (PLVFloatingPlayerManager.getInstance().isFloatingWindowShowing()) {
            PLVFloatingPlayerManager.getInstance().hide();
        }
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity == null) {
            return;
        }
        if (topActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            PLVOrientationManager.getInstance().unlockOrientation();
            PLVOrientationManager.getInstance().setPortrait(topActivity);
        }
        activity.recreate();
    }

    public static boolean checkOverrideLanguage(String channelId, String langType) {
        int languageType = LANGUAGE_FOLLOW_SYSTEM;
        if (LANG_MAP.containsKey(langType)) {
            Integer lang = LANG_MAP.get(langType);
            languageType = lang == null ? languageType : lang;
        }
        return checkOverrideLanguage(channelId, languageType);
    }

    public static boolean checkOverrideLanguage(String channelId, int languageType) {
        String key = SP_KEY + "_" + channelId;
        int saveLanguageType = SPUtils.getInstance().getInt(key, LANGUAGE_FOLLOW_SYSTEM);
        if (saveLanguageType != LANGUAGE_FOLLOW_SYSTEM) {
            lastSaveKeyOrType = key;
            return true;
        } else {
            lastSaveKeyOrType = languageType + "";
        }
        return false;
    }

    public static Context useAttachLanguage(Context baseContext) {
        if (userLocale == null) {
            return baseContext;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return createConfigurationContext(baseContext).first;
        } else {
            updateConfiguration(baseContext);
            return baseContext;
        }
    }

    public static Context attachLanguageActivity(Context baseContext, Activity languageActivity) {
        PLVAppUtils.updateLanguageActivity(languageActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Pair<Context, Locale> createConfigurationContext = createConfigurationContext(baseContext);
            userLocale = createConfigurationContext.second;
            return createConfigurationContext.first;
        } else {
            userLocale = updateConfiguration(baseContext);
            return baseContext;
        }
    }

    public static void detachLanguageActivity() {
        userLocale = null;
        PLVAppUtils.updateLanguageActivity(null);
    }

    public static Configuration setToConfiguration(Configuration newConfig, Activity activity) {
        activity.getResources().getConfiguration().setTo(newConfig);
        return newConfig;
    }

    public static boolean isENLanguage() {
        return userLocale != null && !LANGUAGE_ZH.equals(userLocale.getLanguage());
    }

    public static boolean isEqualsLanguage(int languageType) {
        int currentLanguageType = LANGUAGE_FOLLOW_SYSTEM;
        if (userLocale != null) {
            currentLanguageType = getCurrentLanguageCode();
        }
        return languageType == currentLanguageType;
    }

    private static int getCurrentLanguageCode() {
        if (userLocale != null) {
            String language = userLocale.getLanguage();
            String country = userLocale.getCountry();
            PLVCommonLog.d(TAG, "=== language: " + language +  " country: " + country);
            PLVCommonLog.d(TAG, "=== CN language: " + Locale.CHINESE.getLanguage() +  " country: " + Locale.CHINESE.getCountry());
            PLVCommonLog.d(TAG, "=== TW language: " + Locale.TRADITIONAL_CHINESE.getLanguage() +  " TW country: " + Locale.TRADITIONAL_CHINESE.getCountry());
            if (Locale.CHINESE.getLanguage().equals(language)) {
                return country.equals(Locale.TRADITIONAL_CHINESE.getLanguage()) ? LANGUAGE_CHINESE_TRADITIONAL : LANGUAGE_CHINESE_SIMPLIFIED;
            } else if (Locale.ENGLISH.getLanguage().equals(language)) {
                return LANGUAGE_EN;
            } else if (Locale.JAPANESE.getLanguage().equals(language)) {
                return LANGUAGE_JAPANESE;
            } else if (Locale.KOREAN.getLanguage().equals(language)) {
                return LANGUAGE_KOREAN;
            }
        }
        return LANGUAGE_FOLLOW_SYSTEM;
    }

    private static Locale updateConfiguration(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getLanguageLocale();
        PLVCommonLog.d(TAG, "PLVLanguageUtil.updateConfiguration locale:" + locale.getLanguage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale 注意此处是setLocales
            configuration.setLocales(new LocaleList(locale));
        } else {
            // updateConfiguration
            configuration.locale = locale;
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);
        }
        return locale;
    }

    // 注意此处不是Build.VERSION_CODES.N
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private static Pair<Context, Locale> createConfigurationContext(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getLanguageLocale();
        PLVCommonLog.d(TAG, "PLVLanguageUtil.createConfigurationContext locale:" + locale);
        LocaleList localeList = new LocaleList(locale);
        // 注意此处setLocales
        configuration.setLocales(localeList);
        return new Pair<>(context.createConfigurationContext(configuration), locale);
    }

    private static Locale getLanguageLocale() {
        int languageType;
        try {
            languageType = Integer.parseInt(lastSaveKeyOrType);
        } catch (Exception e) {
            languageType = SPUtils.getInstance().getInt(lastSaveKeyOrType, LANGUAGE_FOLLOW_SYSTEM);
        }
        Locale locale = Locale.SIMPLIFIED_CHINESE;
        if (languageType == LANGUAGE_FOLLOW_SYSTEM) {
            locale = getSysLocale();
            boolean displaySimplifiedForTraditional = DISPLAY_SIMPLIFIED_FOR_TRADITIONAL && (locale == null || LANGUAGE_ZH.equals(locale.getLanguage()));
            locale = displaySimplifiedForTraditional ? Locale.SIMPLIFIED_CHINESE : locale;
        } else if (languageType == LANGUAGE_EN) {
            locale = Locale.ENGLISH;
        } else if (languageType == LANGUAGE_CHINESE_SIMPLIFIED) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (languageType == LANGUAGE_CHINESE_TRADITIONAL) {
            locale = Locale.TRADITIONAL_CHINESE;
        } else if (languageType == LANGUAGE_JAPANESE) {
            locale = Locale.JAPAN;
        } else if (languageType == LANGUAGE_KOREAN) {
            locale = Locale.KOREA;
        }
        return locale;
    }

    private static Locale getSysLocale() {
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }
}
