package com.easefun.polyv.livecommon.module.modules.multirolelinkmic.model;

/**
 * 多角色连麦常量
 */
public class PLVMultiRoleLinkMicConstant {
    //未初始化
    public static final int LINK_MIC_UNINITIATED = 1;
    //初始化中
    public static final int LINK_MIC_INITIATING = 2;
    //已经初始化
    public static final int LINK_MIC_INITIATED = 3;
    //未加入频道
    public static final int JOIN_CHANNEL_UN = 1;
    //加入频道中
    public static final int JOIN_CHANNEL_ING = 2;
    //已加入频道
    public static final int JOIN_CHANNEL_ED = 3;

    //加入频道超时时间
    public static final int TIME_OUT_JOIN_CHANNEL = 20 * 1000;
    //轮询连麦列表时间
    public static final int INTERVAL_TO_GET_LINK_MIC_LIST = 20 * 1000;
    //延迟1秒请求连麦列表
    public static final int DELAY_TO_GET_LINK_MIC_LIST = 1000;

    //轮询在线列表时间
    public static final int INTERVAL_TO_GET_USER_LIST = 20 * 1000;
    //延迟20秒再请求在线列表
    public static final int DELAY_TO_GET_USER_LIST = 20 * 1000;
}
