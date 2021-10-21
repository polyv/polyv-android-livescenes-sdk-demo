package com.easefun.polyv.livedemo.hiclass.model.vo;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.plv.socket.user.PLVSocketUserConstant;

/**
 * @author suhongtao
 */
public class PLVHCLoginDataVO {

    /**
     * @see com.plv.socket.user.PLVSocketUserConstant#USERTYPE_SCSTUDENT
     * @see com.plv.socket.user.PLVSocketUserConstant#USERTYPE_TEACHER
     */
    private String role;

    private String viewerId;
    private String nickname;
    private String avatarUrl;
    private String token;

    @Nullable
    private String courseCode;

    public PLVHCLoginDataVO recreateByLaunchHiClassVO(PLVHCLaunchHiClassVO launchHiClassVO) {
        this.setRole(launchHiClassVO.getUserType());
        this.setViewerId(launchHiClassVO.getViewerId());
        this.setNickname(launchHiClassVO.getViewerName());
        this.setAvatarUrl(launchHiClassVO.getAvatarUrl());
        this.setToken(launchHiClassVO.getToken());
        this.setCourseCode(launchHiClassVO.getCourseCode());
        return this;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        if (TextUtils.isEmpty(avatarUrl)) {
            return PLVSocketUserConstant.USERTYPE_TEACHER.equals(role) ?
                    PLVSocketUserConstant.TEACHER_AVATAR_URL_V2 : PLVSocketUserConstant.STUDENT_AVATAR_URL_V2;
        }
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Nullable
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(@Nullable String courseCode) {
        this.courseCode = courseCode;
    }
}
