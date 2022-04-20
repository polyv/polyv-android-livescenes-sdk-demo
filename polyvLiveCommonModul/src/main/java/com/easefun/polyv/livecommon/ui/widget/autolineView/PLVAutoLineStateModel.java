package com.easefun.polyv.livecommon.ui.widget.autolineView;

import android.support.annotation.IdRes;

public class PLVAutoLineStateModel {

    private int id;

    @IdRes
    private int imageSource;

    private String name;

    private boolean isActive = true;

    private boolean isShow = true;

    public PLVAutoLineStateModel() {
    }

    public PLVAutoLineStateModel(int imageSource, String name) {
        this.imageSource = imageSource;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImageSource() {
        return imageSource;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
