package com.easefun.polyv.livecommon.module.utils.imageloader;

public class PLVUrlTag {
    private String url;
    private Object data;

    public PLVUrlTag(String url) {
        this.url = url;
    }

    public PLVUrlTag(String url, Object data) {
        this.url = url;
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setTag(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (data == null) {
            return super.toString();
        } else {
            return data.toString();
        }
    }
}
