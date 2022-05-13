package com.easefun.polyv.livecommon.module.config;

import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.socket.user.PLVSocketUserConstant;

/**
 * date: 2020/7/14
 * author: hwj
 * description:[PLVLiveChannelConfig]的数据初始化器。负责初始化直播数据和生成[PLVLiveChannelConfig]对象
 */
public class PLVLiveChannelConfigFiller {
    private static PLVLiveChannelConfig channelConfig;

    static {
        channelConfig = new PLVLiveChannelConfig();
    }

    /**
     * 配置保利威账号参数
     *
     * @param userId    直播账号userId
     * @param appId     直播账号appId
     * @param appSecret 直播账号appSecret
     */
    public static void setupAccount(String userId, String appId, String appSecret) {
        channelConfig.setupAccount(userId, appId, appSecret);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId   用户的userId，用于登录socket、发送日志
     * @param viewerName 用户昵称，用于登录socket、发送日志
     */
    public static void setupUser(String viewerId, String viewerName) {
        channelConfig.setupUser(viewerId, viewerName, PLVSocketUserConstant.STUDENT_AVATAR_URL, PLVSocketUserConstant.USERTYPE_SLICE);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar) {
        channelConfig.setupUser(viewerId, viewerName, viewerAvatar, PLVSocketUserConstant.USERTYPE_SLICE);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     * @param viewerType   用户的类型，用于登录socket，需要为指定的类型，例如：{@link PLVSocketUserConstant#USERTYPE_STUDENT}， {@link PLVSocketUserConstant#USERTYPE_SLICE}
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType) {
        channelConfig.setupUser(viewerId, viewerName, viewerAvatar, viewerType);
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     * @param viewerType   用户的类型，用于登录socket，需要为指定的类型，例如：{@link PLVSocketUserConstant#USERTYPE_STUDENT}， {@link PLVSocketUserConstant#USERTYPE_SLICE}
     * @param actor        用户的头衔，一般观看场景不需填写，开播场景从登录接口获取
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType, String actor) {
        channelConfig.setupUser(viewerId, viewerName, viewerAvatar, viewerType, actor);
    }

    /**
     *
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     * @param viewerType   用户的类型，用于登录socket，需要为指定的类型，例如：{@link PLVSocketUserConstant#USERTYPE_STUDENT}， {@link PLVSocketUserConstant#USERTYPE_SLICE}
     * @param actor        用户的头衔，一般观看场景不需填写，开播场景从登录接口获取
     * @param param4       自定义统计参数4
     * @param param5       自定义统计参数5
     */
    public static void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType, String actor, String param4, String param5){
        channelConfig.setupUser(viewerId, viewerName, viewerAvatar, viewerType, actor, param4, param5);
    }

    /**
     * 配置频道号
     */
    public static void setupChannelId(String channelId) {
        channelConfig.setupChannelId(channelId);
    }

    /**
     * 配置频道名称
     */
    public static void setupChannelName(String channelName) {
        channelConfig.setupChannelName(channelName);
    }

    /**
     * 配置vid
     */
    public static void setupVid(String vid) {
        channelConfig.setupVid(vid);
    }

    /**
     * 配置回放视频所在的列表的类型
     */
    public static void setupVideoListType(PLVPlaybackListType videoListType) {
        channelConfig.setupVideoListType(videoListType);
    }

    /**
     * 设置是否是直播
     *
     * @param isLive true为直播，false为回放
     */
    public static void setIsLive(boolean isLive) {
        channelConfig.setIsLive(isLive);
    }

    /**
     * 设置频道类型
     *
     * @param channelType 频道类型
     */
    public static void setChannelType(PLVLiveChannelType channelType) {
        channelConfig.setChannelType(channelType);
    }

    /**
     * 设置嘉宾连麦类型
     *
     * @param colinMicType 嘉宾连麦类型
     */
    public static void setColinMicType(String colinMicType) {
        channelConfig.setColinMicType(colinMicType);
    }

    /**
     * 设置互动学堂课堂信息
     *
     * @param token      token
     * @param lessonId   课节Id
     * @param courseCode 课程号
     */
    public static void setHiClassConfig(String token, long lessonId, String courseCode) {
        channelConfig.setHiClassConfig(token, lessonId, courseCode);
    }

    /**
     * 开播登录时，此频道是否仍在直播（仅开播场景使用）
     */
    public static void setLiveStreamingWhenLogin(boolean liveStreamingWhenLogin){
        channelConfig.setLiveStreamingWhenLogin(liveStreamingWhenLogin);
    }

    /**
     * 生成新的config对象
     *
     * @return PLVLiveChannelConfig
     */
    public static PLVLiveChannelConfig generateNewChannelConfig() {
        return new PLVLiveChannelConfig(channelConfig);
    }
}
