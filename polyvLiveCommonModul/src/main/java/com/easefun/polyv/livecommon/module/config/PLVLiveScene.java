package com.easefun.polyv.livecommon.module.config;

import com.easefun.polyv.livescenes.config.PolyvLiveChannelType;

/**
 * date: 2020/11/25
 * author: HWilliamgo
 * description: 直播多场景枚举类型定义
 */
public enum PLVLiveScene {
    /**
     * 云课堂 —— 支持 云课堂（三分屏）频道类型、直播助手（纯视频）频道类型
     * <br/>注：这里提及到的 云课堂（三分屏）频道类型 和 直播助手（纯视频）频道类型 是对应的在保利威后台创建频道时所选择的 直播场景
     */
    CLOUDCLASS,

    /**
     * 直播带货 —— 支持 直播助手（纯视频）频道类型
     */
    ECOMMERCE;

    /**
     * 是否是云课堂场景支持的频道类型
     *
     * @param channelType 频道类型
     * @return true:支持，false:不支持
     */
    public static boolean isCloudClassSceneSupportType(PolyvLiveChannelType channelType) {
        return channelType == PolyvLiveChannelType.PPT || channelType == PolyvLiveChannelType.ALONE;
    }

    /**
     * 是否是直播带货场景支持的频道类型
     *
     * @param channelType 频道类型
     * @return true:支持，false:不支持
     */
    public static boolean isLiveEcommerceSceneSupportType(PolyvLiveChannelType channelType) {
        return channelType == PolyvLiveChannelType.ALONE;
    }
}
