package com.easefun.polyv.livecommon.module.modules.player.floating;

import com.plv.foundationsdk.component.kv.PLVAutoSaveKV;

public class PLVFloatingPlayerConfig {
    // 开关1开启的情况下，显示开关2，开关1关闭的情况下，隐藏开关2
    private static final PLVAutoSaveKV<Boolean> isAutoFloatingWhenExitPage = new PLVAutoSaveKV<Boolean>("plv_config_is_auto_floating_when_exit_page") {
    };
    private static final PLVAutoSaveKV<Boolean> isAutoFloatingWhenGoHome = new PLVAutoSaveKV<Boolean>("plv_config_is_auto_floating_when_go_home") {
    };

    public static void setIsAutoFloatingWhenExitPage(boolean isAutoFloatingWhenExitPage) {
        PLVFloatingPlayerConfig.isAutoFloatingWhenExitPage.set(isAutoFloatingWhenExitPage);
    }

    public static boolean isAutoFloatingWhenExitPage() {
        return isAutoFloatingWhenExitPage.getOrDefault(true);
    }

    public static void setIsAutoFloatingWhenGoHome(boolean isAutoFloatingWhenGoHome) {
        PLVFloatingPlayerConfig.isAutoFloatingWhenGoHome.set(isAutoFloatingWhenGoHome);
    }

    public static boolean isAutoFloatingWhenGoHome() {
        if (!isAutoFloatingWhenExitPage()) {
            return false;
        }
        return isAutoFloatingWhenGoHome.getOrDefault(true);
    }
}
