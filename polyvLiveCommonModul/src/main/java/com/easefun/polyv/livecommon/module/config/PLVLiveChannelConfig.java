package com.easefun.polyv.livecommon.module.config;

import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.utils.PLVSystemUtils;
import com.plv.livescenes.config.PLVLiveChannelType;
import com.plv.livescenes.feature.login.model.PLVLoginVO;
import com.plv.livescenes.playback.video.PLVPlaybackListType;
import com.plv.socket.user.PLVSocketUserConstant;
import com.plv.thirdpart.blankj.utilcode.util.Utils;

/**
 * 直播频道相关信息配置类
 */
public class PLVLiveChannelConfig {
    /**
     * 保利威账号信息
     */
    private Account account;
    /**
     * 用户(观众)信息
     */
    private User user;
    /**
     * 直播频道号，即推流的频道号
     */
    private String channelId;
    /**
     * 回放vid
     */
    private String vid;

    /**
     * 回放视频所在的列表类型
     */
    private PLVPlaybackListType videoListType;

    /**
     * 当前是否是直播，true为直播，false为回放。
     */
    private boolean isLive;

    /**
     * 频道类型
     */
    private PLVLiveChannelType channelType;

    /**
     * 频道名称
     */
    private String channelName;

    /**
     * 嘉宾连麦类型
     */
    private String colinMicType;
    /**
     * 是否跳过自动连麦
     */
    private boolean isSkipAutoLinkMic = false;

    /**
     * 互动学堂课堂信息
     */
    private HiClassConfig hiClassConfig;

    /**
     * 开播登录时，此频道是否仍在直播（如讲师异常退出后重新进入）
     * （仅开播场景使用）
     */
    private boolean isLiveStreamingWhenLogin = false;

    public PLVLiveChannelConfig() {
        account = new Account();
        user = new User();
        hiClassConfig = new HiClassConfig();
    }

    public PLVLiveChannelConfig(PLVLiveChannelConfig old) {
        account = new Account(old.account);
        user = new User(old.user);
        channelId = old.channelId;
        vid = old.vid;
        videoListType = old.videoListType;
        isLive = old.isLive;
        channelType = old.channelType;
        channelName = old.channelName;
        colinMicType = old.colinMicType;
        hiClassConfig = new HiClassConfig(old.hiClassConfig);
        isLiveStreamingWhenLogin = old.isLiveStreamingWhenLogin;
    }

    // <editor-fold defaultstate="collapsed" desc="set">

    /**
     * 配置保利威账号参数
     *
     * @param userId    直播账号userId
     * @param appId     直播账号appId
     * @param appSecret 直播账号appSecret
     */
    public void setupAccount(String userId, String appId, String appSecret) {
        account.userId = userId;
        account.appId = appId;
        account.appSecret = appSecret;
    }

    /**
     * 配置用户参数
     *
     * @param viewerId     用户的userId，用于登录socket、发送日志
     * @param viewerName   用户昵称，用于登录socket、发送日志
     * @param viewerAvatar 用户的头像url，用于登录socket、发送日志
     * @param viewerType   用户的类型，用于登录socket，需要为指定的类型，例如：{@link PLVSocketUserConstant#USERTYPE_STUDENT}， {@link PLVSocketUserConstant#USERTYPE_SLICE}
     */
    public void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType) {
        setupUser(viewerId, viewerName, viewerAvatar, viewerType, null);
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
    public void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType, String actor) {
        setupUser(viewerId, viewerName, viewerAvatar, viewerType, actor, "", "");
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
    public void setupUser(String viewerId, String viewerName, String viewerAvatar, String viewerType, String actor, String param4, String param5){
        user.viewerId = TextUtils.isEmpty(viewerId) ? PLVSystemUtils.getAndroidId(Utils.getApp()) + "" : viewerId;
        user.viewerName = TextUtils.isEmpty(viewerName) ? "观众" + PLVSystemUtils.getAndroidId(Utils.getApp()) : viewerName;
        user.viewerAvatar = TextUtils.isEmpty(viewerAvatar) ? PLVSocketUserConstant.STUDENT_AVATAR_URL : viewerAvatar;
        user.viewerType = TextUtils.isEmpty(viewerType) ? PLVSocketUserConstant.USERTYPE_STUDENT : viewerType;
        user.actor = actor;
        user.param4 = param4;
        user.param5= param5;
    }

    /**
     * 配置频道号
     */
    public void setupChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * 配置频道名称
     */
    public void setupChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * 配置vid
     */
    public void setupVid(String vid) {
        this.vid = vid;
    }

    /**
     * 配置回放视频所在的列表的类型
     */
    public void setupVideoListType(PLVPlaybackListType videoListType) {
        this.videoListType = videoListType;
    }

    /**
     * 设置是否是直播
     *
     * @param isLive true为直播，false为回放
     */
    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    /**
     * 设置频道类型
     */
    public void setChannelType(PLVLiveChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * 设置嘉宾连麦类型
     * @param colinMicType 嘉宾连麦类型
     */
    public void setColinMicType(String colinMicType) {
        this.colinMicType = colinMicType;
    }

    /**
     * 设置互动学堂课堂信息
     *
     * @param token      token
     * @param lessonId   课节Id
     * @param courseCode 课程号
     */
    public void setHiClassConfig(String token, long lessonId, String courseCode) {
        hiClassConfig.token = token;
        hiClassConfig.lessonId = lessonId;
        hiClassConfig.courseCode = courseCode;
    }

    /**
     * 开播登录时，此频道是否仍在直播（仅开播场景使用）
     */
    public void setLiveStreamingWhenLogin(boolean liveStreamingWhenLogin) {
        isLiveStreamingWhenLogin = liveStreamingWhenLogin;
    }

    /**
     * 设置嘉宾跳过自动连麦，设置后即使开启了自动连麦，嘉宾也不会自动上麦
     * 默认为false
     */
    public void setSkipAutoLinkMic(boolean skipAutoLinkMic) {
        isSkipAutoLinkMic = skipAutoLinkMic;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="get">
    public Account getAccount() {
        return account;
    }

    public User getUser() {
        return user;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getVid() {
        return vid;
    }

    public PLVPlaybackListType getVideoListType() {
        return videoListType;
    }

    public boolean isLive() {
        return isLive;
    }

    //获取频道类型
    public PLVLiveChannelType getChannelType() {
        return channelType;
    }

    public HiClassConfig getHiClassConfig() {
        return hiClassConfig;
    }

    //是否是三分屏频道类型
    public boolean isPPTChannelType() {
        return channelType == PLVLiveChannelType.PPT;
    }

    //是否是纯视频频道类型
    public boolean isAloneChannelType() {
        return channelType == PLVLiveChannelType.ALONE;
    }

    //嘉宾是否是自动上麦
    public boolean isAutoLinkToGuest() {
        return TextUtils.isEmpty(colinMicType) || (PLVLoginVO.COLINMICTYPE_AUTO.equals(colinMicType) && !isSkipAutoLinkMic);
    }

    /**
     * 开播登录时，此频道是否仍在直播（仅开播场景使用）
     */
    public boolean isLiveStreamingWhenLogin() {
        return isLiveStreamingWhenLogin;
    }
    // </editor-fold>

    /**
     * 保利威直播账号信息
     */
    public static class Account {

        Account() {
        }

        Account(Account old) {
            userId = old.getUserId();
            appId = old.getAppId();
            appSecret = old.getAppSecret();
        }

        /**
         * 直播账号userId
         */
        private String userId;
        /**
         * 直播账号appId
         */
        private String appId;
        /**
         * 直播账号appSecret
         */
        private String appSecret;

        public String getUserId() {
            return userId;
        }

        public String getAppId() {
            return appId;
        }

        public String getAppSecret() {
            return appSecret;
        }
    }

    /**
     * 用户(观众)信息
     */
    public static class User {

        User() {
        }

        User(User old) {
            viewerId = old.viewerId;
            viewerName = old.viewerName;
            viewerAvatar = old.viewerAvatar;
            viewerType = old.viewerType;
            actor = old.actor;
            param4 = old.param4;
            param5 = old.param5;
        }

        /**
         * 用户Id，用于登录socket、发送日志<br>
         * 注意{@link #viewerId}不能和{@link Account#userId}一致)
         */
        private String viewerId;
        /**
         * 用户昵称，用于登录socket、发送日志
         */
        private String viewerName;
        /**
         * 用户的头像url，用于登录socket、发送日志
         */
        private String viewerAvatar;
        /**
         * 用户的类型，用于登录socket
         */
        private String viewerType;
        /**
         * 用户的头衔，用于登录socket
         */
        private String actor;
        /**
         * 自定义统计参数4
         */
        private String param4;
        /**
         * 自定义统计参数5
         */
        private String param5;

        public String getViewerId() {
            return viewerId;
        }

        public String getViewerName() {
            return viewerName;
        }

        public String getViewerAvatar() {
            return viewerAvatar;
        }

        public String getViewerType() {
            return viewerType;
        }

        public String getActor() {
            return actor;
        }

        public String getParam4() {
            return param4;
        }

        public String getParam5() {
            return param5;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /**
     * 互动学堂课堂信息
     */
    public static class HiClassConfig {

        HiClassConfig() {
        }

        HiClassConfig(HiClassConfig old) {
            token = old.token;
            lessonId = old.lessonId;
            courseCode = old.courseCode;
        }

        /**
         * token
         */
        private String token;
        /**
         * 课节Id
         */
        private long lessonId;
        /**
         * 课程号
         */
        private String courseCode;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public long getLessonId() {
            return lessonId;
        }

        public void setLessonId(long lessonId) {
            this.lessonId = lessonId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }
    }
}
