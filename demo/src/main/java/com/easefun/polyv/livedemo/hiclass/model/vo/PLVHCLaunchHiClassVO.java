package com.easefun.polyv.livedemo.hiclass.model.vo;

/**
 * @author suhongtao
 */
public class PLVHCLaunchHiClassVO {

    private String channelId;
    private String courseCode;
    private Long lessonId;
    private String token;
    private String sessionId;
    private String userType;
    private String viewerId;
    private String viewerName;
    private String avatarUrl;

    private long tokenCreateTimestamp;

    public PLVHCLaunchHiClassVO() {
    }

    public PLVHCLaunchHiClassVO(String channelId, String courseCode, Long lessonId, String token, String sessionId, String userType, String viewerId, String viewerName, String avatarUrl) {
        this.channelId = channelId;
        this.courseCode = courseCode;
        this.lessonId = lessonId;
        this.token = token;
        this.sessionId = sessionId;
        this.userType = userType;
        this.viewerId = viewerId;
        this.viewerName = viewerName;
        this.avatarUrl = avatarUrl;
    }

    public PLVHCLaunchHiClassVO copyFrom(PLVHCLaunchHiClassVO vo) {
        if (vo == null) {
            vo = new PLVHCLaunchHiClassVO();
        }
        this.channelId = vo.channelId;
        this.courseCode = vo.courseCode;
        this.lessonId = vo.lessonId;
        this.token = vo.token;
        this.sessionId = vo.sessionId;
        this.userType = vo.userType;
        this.viewerId = vo.viewerId;
        this.viewerName = vo.viewerName;
        this.avatarUrl = vo.avatarUrl;
        return this;
    }

    public PLVHCLaunchHiClassVO createByLoginDataVO(PLVHCLoginDataVO loginDataVO) {
        this.setUserType(loginDataVO.getRole());
        this.setViewerId(loginDataVO.getViewerId());
        this.setViewerName(loginDataVO.getNickname());
        this.setAvatarUrl(loginDataVO.getAvatarUrl());
        this.setToken(loginDataVO.getToken());
        this.setCourseCode(loginDataVO.getCourseCode());
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public String getViewerName() {
        return viewerName;
    }

    public void setViewerName(String viewerName) {
        this.viewerName = viewerName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getTokenCreateTimestamp() {
        return tokenCreateTimestamp;
    }

    public PLVHCLaunchHiClassVO setTokenCreateTimestamp(long tokenCreateTimestamp) {
        this.tokenCreateTimestamp = tokenCreateTimestamp;
        return this;
    }

}
