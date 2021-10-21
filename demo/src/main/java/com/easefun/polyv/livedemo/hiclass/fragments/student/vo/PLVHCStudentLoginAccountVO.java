package com.easefun.polyv.livedemo.hiclass.fragments.student.vo;

/**
 * @author suhongtao
 */
public class PLVHCStudentLoginAccountVO {

    // 无条件观看
    public static final String AUTH_TYPE_NONE = "none";
    // 验证码登录
    public static final String AUTH_TYPE_CODE = "code";
    // 白名单观看
    public static final String AUTH_TYPE_WHITE_LIST = "white_list";

    private String authType;
    private String courseCode;
    private Long lessonId;

    public PLVHCStudentLoginAccountVO() {
    }

    public PLVHCStudentLoginAccountVO(String authType) {
        this.authType = authType;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
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

    @Override
    public String toString() {
        return "PLVHCStudentLoginDataVO{" +
                "authType='" + authType + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", lessonId=" + lessonId +
                '}';
    }
}
