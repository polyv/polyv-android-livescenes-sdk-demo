package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import androidx.annotation.IntRange;

import com.plv.socket.user.PLVSocketUserConstant;

/**
 * date: 2020/7/27
 * author: hwj
 * description:连麦列表item对应的数据实体
 */
public class PLVLinkMicItemDataBean {
    //最大音量值
    public static final int MAX_VOLUME = 100;

    //昵称
    private String nick;
    //连麦Id
    private String linkMicId;
    //是否mute视频
    private boolean muteVideo;
    //是否mute音频
    private boolean muteAudio;
    //奖杯数量
    private int cupNum;
    //参考[PLVSocketUserConstant]
    private String userType;
    //头衔
    private String actor;
    //最大值为[MAX_VOLUME]
    @IntRange(from = 0, to = 100)
    private int curVolume = 0;

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getLinkMicId() {
        return linkMicId;
    }

    public void setLinkMicId(String linkMicId) {
        this.linkMicId = linkMicId;
    }

    public boolean isMuteAudio() {
        return muteAudio;
    }

    public void setMuteAudio(boolean muteAudio) {
        this.muteAudio = muteAudio;
    }

    public boolean isMuteVideo() {
        return muteVideo;
    }

    public void setMuteVideo(boolean muteVideo) {
        this.muteVideo = muteVideo;
    }

    public int getCupNum() {
        return cupNum;
    }

    public void setCupNum(int cupNum) {
        this.cupNum = cupNum;
    }

    public int getCurVolume() {
        return curVolume;
    }

    public void setCurVolume(int curVolume) {
        this.curVolume = curVolume;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * 是否是讲师
     */
    public boolean isTeacher() {
        return PLVSocketUserConstant.USERTYPE_TEACHER.equals(userType);
    }

    /**
     * 是否是嘉宾
     */
    public boolean isGuest() {
        return PLVSocketUserConstant.USERTYPE_GUEST.equals(userType);
    }

    @Override
    public String toString() {
        return "PLVLinkMicItemDataBean{" +
                "nick='" + nick + '\'' +
                ", linkMicId='" + linkMicId + '\'' +
                ", muteVideo=" + muteVideo +
                ", muteAudio=" + muteAudio +
                ", cupNum=" + cupNum +
                ", userType='" + userType + '\'' +
                ", actor='" + actor + '\'' +
                ", curVolume=" + curVolume +
                '}';
    }
}
