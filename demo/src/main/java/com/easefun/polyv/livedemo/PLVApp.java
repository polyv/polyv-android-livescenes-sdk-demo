package com.easefun.polyv.livedemo;

import androidx.multidex.MultiDexApplication;

import com.easefun.polyv.livecommon.module.config.PLVLiveSDKConfig;

/**
 * date: 2020-04-29
 * author: hwj
 * description:
 */
public class PLVApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        PLVLiveSDKConfig.init(
                new PLVLiveSDKConfig.Parameter(this)
                        .isOpenDebugLog(true)
                        .isEnableHttpDns(true)
        );
    }
}
