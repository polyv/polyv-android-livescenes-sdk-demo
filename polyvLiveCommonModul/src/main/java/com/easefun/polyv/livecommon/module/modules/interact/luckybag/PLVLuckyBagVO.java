package com.easefun.polyv.livecommon.module.modules.interact.luckybag;

public class PLVLuckyBagVO {
    private String content;
    private boolean hasPendant;
    private long countdownTime;
    private String iconUrl;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasPendant() {
        return hasPendant;
    }

    public void setHasPendant(boolean hasPendant) {
        this.hasPendant = hasPendant;
    }

    public long getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(long countdownTime) {
        this.countdownTime = countdownTime;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
