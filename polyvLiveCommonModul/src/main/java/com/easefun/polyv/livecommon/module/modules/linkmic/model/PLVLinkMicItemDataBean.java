package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import android.support.annotation.IntRange;

import com.plv.socket.user.PLVSocketUserConstant;

/**
 * date: 2020/7/27
 * author: hwj
 * description:连麦列表item对应的数据实体
 */
public class PLVLinkMicItemDataBean {
    public static final String STATUS_IDLE = "idle";//空闲
    public static final String STATUS_WAIT = "wait";//等待同意
    public static final String STATUS_JOINING = "joining";//加入中
    public static final String STATUS_JOIN = "join";//已加入列表
    public static final String STATUS_RTC_JOIN = "rtcJoin";//已加入rtc
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

    //linkMic status
    private String status = STATUS_IDLE;
    private MuteMedia muteVideoInRtcJoinList;
    private MuteMedia muteAudioInRtcJoinList;

    public MuteMedia getMuteVideoInRtcJoinList() {
        return muteVideoInRtcJoinList;
    }

    public void setMuteVideoInRtcJoinList(MuteMedia muteVideoInRtcJoinList) {
        this.muteVideoInRtcJoinList = muteVideoInRtcJoinList;
        setMuteVideo(muteVideoInRtcJoinList.isMute);
    }

    public MuteMedia getMuteAudioInRtcJoinList() {
        return muteAudioInRtcJoinList;
    }

    public void setMuteAudioInRtcJoinList(MuteMedia muteAudioInRtcJoinList) {
        this.muteAudioInRtcJoinList = muteAudioInRtcJoinList;
        setMuteAudio(muteAudioInRtcJoinList.isMute);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        callStatusMethodTouch();
    }

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

    public boolean isJoinStatus() {
        return STATUS_JOIN.equals(status);
    }

    public boolean isJoiningStatus() {
        return STATUS_JOINING.equals(status);
    }

    public boolean isWaitStatus() {
        return STATUS_WAIT.equals(status);
    }

    public boolean isIdleStatus() {
        return STATUS_IDLE.equals(status);
    }

    public boolean isRtcJoinStatus() {
        return STATUS_RTC_JOIN.equals(status);
    }

    public static class MuteMedia {
        boolean isMute;

        public MuteMedia(boolean isMute) {
            this.isMute = isMute;
        }

        public boolean isMute() {
            return isMute;
        }

        public void setMute(boolean mute) {
            isMute = mute;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="状态方法被调用触发回调">
    private Runnable statusMethodCallListener;

    public void setStatusMethodCallListener(Runnable runnable) {
        this.statusMethodCallListener = runnable;
    }

    private void callStatusMethodTouch() {
        if (statusMethodCallListener != null) {
            statusMethodCallListener.run();
        }
    }
    // </editor-fold>

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
