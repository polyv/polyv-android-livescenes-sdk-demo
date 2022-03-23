package com.plv.livedemo.hiclass.fragments.share.vo;

import com.plv.livescenes.hiclass.vo.PLVHCStudentVerifyResultVO;
import com.plv.livescenes.hiclass.vo.PLVHCTeacherLoginResultVO;

/**
 * @author suhongtao
 */
public class PLVHCLoginLessonVO {

    private String imageUrl;
    private String lessonTitle;
    private String lessonTime;
    private String courseTitle;
    private long lessonId;
    private String channelId;

    public PLVHCLoginLessonVO() {
    }

    public static PLVHCLoginLessonVO fromTeacherLoginResultLessonVO(PLVHCTeacherLoginResultVO.DataVO.LessonVO lessonVO) {
        if (lessonVO == null) {
            return null;
        }
        PLVHCLoginLessonVO loginLessonVO = new PLVHCLoginLessonVO();
        loginLessonVO.imageUrl = lessonVO.getCover();
        loginLessonVO.lessonTitle = lessonVO.getName();
        loginLessonVO.lessonTime = lessonVO.getTime();
        loginLessonVO.courseTitle = lessonVO.getCourseNames();
        loginLessonVO.lessonId = lessonVO.getLessonId() == null ? 0 : lessonVO.getLessonId();
        loginLessonVO.channelId = lessonVO.getChannelId();
        return loginLessonVO;
    }

    public static PLVHCLoginLessonVO fromStudentLoginResultLessonVO(PLVHCStudentVerifyResultVO.DataVO.LessonVO lessonVO) {
        if (lessonVO == null) {
            return null;
        }
        PLVHCLoginLessonVO loginLessonVO = new PLVHCLoginLessonVO();
        loginLessonVO.imageUrl = lessonVO.getCover();
        loginLessonVO.lessonTitle = lessonVO.getName();
        loginLessonVO.lessonTime = lessonVO.getStartTime();
        loginLessonVO.lessonId = lessonVO.getLessonId() == null ? 0 : lessonVO.getLessonId();
        loginLessonVO.channelId = lessonVO.getChannelId();
        return loginLessonVO;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public String getLessonTime() {
        return lessonTime;
    }

    public void setLessonTime(String lessonTime) {
        this.lessonTime = lessonTime;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public long getLessonId() {
        return lessonId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public String toString() {
        return "PLVHCLoginLessonVO{" +
                "imageUrl='" + imageUrl + '\'' +
                ", lessonTitle='" + lessonTitle + '\'' +
                ", lessonTime='" + lessonTime + '\'' +
                ", courseTitle='" + courseTitle + '\'' +
                ", lessonId=" + lessonId +
                ", channelId='" + channelId + '\'' +
                '}';
    }
}
