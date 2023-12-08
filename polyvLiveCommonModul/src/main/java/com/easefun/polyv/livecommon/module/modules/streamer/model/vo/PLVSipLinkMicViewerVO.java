package com.easefun.polyv.livecommon.module.modules.streamer.model.vo;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.easefun.polyv.livecommon.module.modules.streamer.model.enums.PLVSipLinkMicState;
import com.plv.thirdpart.blankj.utilcode.util.RegexUtils;

import org.intellij.lang.annotations.Identifier;

/**
 * @author Hoshiiro
 */
public class PLVSipLinkMicViewerVO {

    /**
     * 手机号唯一标识一个用户
     */
    @Identifier
    private String phone;

    /**
     * 后端自增id，不需要使用
     */
    private String id;
    @Nullable
    private String contactName;
    private Boolean audioMuted;
    private PLVSipLinkMicState sipLinkMicStatus;

    public PLVSipLinkMicViewerVO() {
    }

    public PLVSipLinkMicViewerVO(PLVSipLinkMicViewerVO source) {
        this.id = source.id;
        this.phone = source.phone;
        this.contactName = source.contactName;
        this.audioMuted = source.audioMuted;
        this.sipLinkMicStatus = source.sipLinkMicStatus;
    }

    public String getPhone() {
        return phone;
    }

    public PLVSipLinkMicViewerVO setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getId() {
        return id;
    }

    public PLVSipLinkMicViewerVO setId(String id) {
        this.id = id;
        return this;
    }

    @Nullable
    public String getContactName() {
        return contactName;
    }

    public PLVSipLinkMicViewerVO setContactName(@Nullable String contactName) {
        this.contactName = contactName;
        return this;
    }

    public Boolean getAudioMuted() {
        return audioMuted;
    }

    public PLVSipLinkMicViewerVO setAudioMuted(Boolean audioMuted) {
        this.audioMuted = audioMuted;
        return this;
    }

    public PLVSipLinkMicState getSipLinkMicStatus() {
        return sipLinkMicStatus;
    }

    public PLVSipLinkMicViewerVO setSipLinkMicStatus(PLVSipLinkMicState sipLinkMicStatus) {
        this.sipLinkMicStatus = sipLinkMicStatus;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PLVSipLinkMicViewerVO viewerVO = (PLVSipLinkMicViewerVO) o;

        if (id != null ? !id.equals(viewerVO.id) : viewerVO.id != null) return false;
        if (phone != null ? !phone.equals(viewerVO.phone) : viewerVO.phone != null) return false;
        if (contactName != null ? !contactName.equals(viewerVO.contactName) : viewerVO.contactName != null)
            return false;
        if (audioMuted != null ? !audioMuted.equals(viewerVO.audioMuted) : viewerVO.audioMuted != null) return false;
        return sipLinkMicStatus == viewerVO.sipLinkMicStatus;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (contactName != null ? contactName.hashCode() : 0);
        result = 31 * result + (audioMuted != null ? audioMuted.hashCode() : 0);
        result = 31 * result + (sipLinkMicStatus != null ? sipLinkMicStatus.hashCode() : 0);
        return result;
    }

    public String getNameString() {
        if (contactName == null) {
            return getFormattedPhone();
        }
        return contactName + "(" + getFormattedPhone() + ")";
    }

    public String getAvatarString() {
        if (contactName != null && !TextUtils.isEmpty(contactName) && RegexUtils.isZh(contactName.substring(contactName.length() - 1))) {
            return contactName.substring(contactName.length() - 1);
        }
        if (contactName != null) {
            return contactName.substring(Math.max(contactName.length() - 2, 0));
        }
        return phone.substring(Math.max(phone.length() - 2, 0));
    }

    private String getFormattedPhone() {
        if (!RegexUtils.isMobileSimple(phone)) {
            return phone;
        }
        return phone.substring(0, 3) +
                " " +
                phone.substring(3, 7) +
                " " +
                phone.substring(7, 11);
    }

}
