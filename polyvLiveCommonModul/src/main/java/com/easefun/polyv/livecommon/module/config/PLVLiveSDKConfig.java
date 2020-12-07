package com.easefun.polyv.livecommon.module.config;

import android.app.Application;

import com.easefun.polyv.livescenes.config.PolyvLiveSDKClient;
import com.plv.foundationsdk.log.PLVCommonLog;

/**
 * sdk配置类
 */
public class PLVLiveSDKConfig {

    /**
     * 初始化sdk配置
     *
     * @param parameter 初始化参数
     */
    public static void init(Parameter parameter) {
        PLVCommonLog.setDebug(parameter.isOpenDebugLog);
        PolyvLiveSDKClient liveSDKClient = PolyvLiveSDKClient.getInstance();
        liveSDKClient.initContext(parameter.application);
        liveSDKClient.enableHttpDns(parameter.isEnableHttpDns);
    }

    public static class Parameter {
        private Application application;
        private boolean isOpenDebugLog = true;
        private boolean isEnableHttpDns = false;

        public Parameter(Application application) {
            this.application = application;
        }

        public Parameter isOpenDebugLog(boolean isOpenDebugLog) {
            this.isOpenDebugLog = isOpenDebugLog;
            return this;
        }

        public Parameter isEnableHttpDns(boolean isEnableHttpDns) {
            this.isEnableHttpDns = isEnableHttpDns;
            return this;
        }
    }
}
