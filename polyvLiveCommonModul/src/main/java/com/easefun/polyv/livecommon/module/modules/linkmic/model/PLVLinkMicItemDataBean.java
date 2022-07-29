package com.easefun.polyv.livecommon.module.modules.linkmic.model;

import androidx.annotation.IntRange;

import com.plv.linkmic.PLVLinkMicConstant;
import com.plv.socket.event.PLVEventHelper;
import com.plv.socket.user.PLVSocketUserConstant;

import java.util.HashMap;
import java.util.Map;

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
    //用户Id
    private String userId;
    //是否mute视频
    private boolean muteVideo;
    //是否mute音频
    private boolean muteAudio;
    //奖杯数量
    private int cupNum;
    //举手状态
    private boolean isRaiseHand;
    //画笔授权状态
    private boolean isHasPaint;
    //主讲授权状态
    private boolean isHasSpeaker;
    //参考[PLVSocketUserConstant]
    private String userType;
    //头衔
    private String actor;
    //头像
    private String pic;
    //最大值为[MAX_VOLUME]
    @IntRange(from = 0, to = 100)
    private int curVolume = 0;

    // 是否第一画面
    private boolean isFirstScreen = false;
    //是否在屏幕共享
    private boolean isScreenShare = false;
    // 是否全屏观看
    private transient boolean isFullScreen = false;

    //linkMic status
    private String status = STATUS_IDLE;
    private Map<Integer, MuteMedia> muteVideoInRtcJoinListMap = new HashMap<>();
    private Map<Integer, MuteMedia> muteAudioInRtcJoinListMap = new HashMap<>();

    //流类型
    private int streamType = PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX;

    public MuteMedia getMuteVideoInRtcJoinList() {
        return getMuteVideoInRtcJoinList(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
    }

    public MuteMedia getMuteVideoInRtcJoinList(int streamType) {
        return muteVideoInRtcJoinListMap.get(streamType);
    }

    public void setMuteVideoInRtcJoinList(MuteMedia muteVideoInRtcJoinList) {
        this.muteVideoInRtcJoinListMap.put(muteVideoInRtcJoinList.getStreamType(), muteVideoInRtcJoinList);
        if (includeStreamType(muteVideoInRtcJoinList.getStreamType())) {
            setMuteVideo(muteVideoInRtcJoinList.isMute);
        }
    }

    public MuteMedia getMuteAudioInRtcJoinList() {
        return getMuteAudioInRtcJoinList(PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
    }

    public MuteMedia getMuteAudioInRtcJoinList(int streamType) {
        return muteAudioInRtcJoinListMap.get(streamType);
    }

    public void setMuteAudioInRtcJoinList(MuteMedia muteAudioInRtcJoinList) {
        this.muteAudioInRtcJoinListMap.put(muteAudioInRtcJoinList.getStreamType(), muteAudioInRtcJoinList);
        if (includeStreamType(muteAudioInRtcJoinList.getStreamType())) {
            setMuteAudio(muteAudioInRtcJoinList.isMute);
        }
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public boolean equalStreamType(int streamType) {
        return this.streamType == streamType;
    }

    public boolean includeStreamType(int streamType) {
        return equalStreamType(streamType)
                || PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX == this.streamType;
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

    public boolean isRaiseHand() {
        return isRaiseHand;
    }

    public void setRaiseHand(boolean raiseHand) {
        isRaiseHand = raiseHand;
    }

    public boolean isHasPaint() {
        return isHasPaint;
    }

    public void setHasPaint(boolean hasPaint) {
        this.isHasPaint = hasPaint;
    }

    public boolean isHasSpeaker() {
        return isHasSpeaker;
    }

    public void setHasSpeaker(boolean hasSpeaker) {
        isHasSpeaker = hasSpeaker;
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

    public String getPic() {
        return PLVEventHelper.fixChatPic(pic);
    }

    public void setPic(String pic) {
        this.pic = pic;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public boolean isScreenShare() {
        return isScreenShare;
    }

    public void setScreenShare(boolean screenShared) {
        isScreenShare = screenShared;
    }

    public boolean isFirstScreen() {
        return isFirstScreen;
    }

    public PLVLinkMicItemDataBean setFirstScreen(boolean firstScreen) {
        isFirstScreen = firstScreen;
        return this;
    }

    public PLVLinkMicItemDataBean setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
        return this;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public static class MuteMedia {
        boolean isMute;
        int streamType;

        public MuteMedia(boolean isMute) {
            this(isMute, PLVLinkMicConstant.RenderStreamType.STREAM_TYPE_MIX);
        }

        public MuteMedia(boolean isMute, int streamType) {
            this.isMute = isMute;
            this.streamType = streamType;
        }

        public boolean isMute() {
            return isMute;
        }

        public void setMute(boolean mute) {
            isMute = mute;
        }

        public int getStreamType() {
            return streamType;
        }

        public void setStreamType(int streamType) {
            this.streamType = streamType;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="连麦状态方法被调用触发回调">
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
                ", userId='" + userId + '\'' +
                ", muteVideo=" + muteVideo +
                ", muteAudio=" + muteAudio +
                ", cupNum=" + cupNum +
                ", isRaiseHand=" + isRaiseHand +
                ", isHasPaint=" + isHasPaint +
                ", isHasSpeaker=" + isHasSpeaker +
                ", userType='" + userType + '\'' +
                ", actor='" + actor + '\'' +
                ", pic='" + pic + '\'' +
                ", curVolume=" + curVolume +
                ", isFirstScreen=" + isFirstScreen +
                ", isScreenShare=" + isScreenShare +
                ", isFullScreen=" + isFullScreen +
                ", status='" + status + '\'' +
                ", muteVideoInRtcJoinListMap=" + muteVideoInRtcJoinListMap +
                ", muteAudioInRtcJoinListMap=" + muteAudioInRtcJoinListMap +
                ", streamType=" + streamType +
                ", statusMethodCallListener=" + statusMethodCallListener +
                '}';
    }
}
