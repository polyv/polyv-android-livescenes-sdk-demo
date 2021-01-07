package com.easefun.polyv.livecommon.ui.widget.magicindicator.buildins.commonnavigator.model;

/**
 * 保存指示器标题的坐标
 * 博客: http://hackware.lucode.net
 * Created by hackware on 2016/6/26.
 */
public class PLVPositionData {
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    private int mContentLeft;
    private int mContentTop;
    private int mContentRight;
    private int mContentBottom;

    public int width() {
        return mRight - mLeft;
    }

    public int height() {
        return mBottom - mTop;
    }

    public int contentWidth() {
        return mContentRight - mContentLeft;
    }

    public int contentHeight() {
        return mContentBottom - mContentTop;
    }

    public int horizontalCenter() {
        return mLeft + width() / 2;
    }

    public int verticalCenter() {
        return mTop + height() / 2;
    }

    public int getLeft() {
        return mLeft;
    }

    public void setLeft(int left) {
        this.mLeft = left;
    }

    public int getTop() {
        return mTop;
    }

    public void setTop(int top) {
        this.mTop = top;
    }

    public int getRight() {
        return mRight;
    }

    public void setRight(int right) {
        this.mRight = right;
    }

    public int getBottom() {
        return mBottom;
    }

    public void setBottom(int bottom) {
        this.mBottom = bottom;
    }

    public int getContentLeft() {
        return mContentLeft;
    }

    public void setContentLeft(int contentLeft) {
        this.mContentLeft = contentLeft;
    }

    public int getContentTop() {
        return mContentTop;
    }

    public void setContentTop(int contentTop) {
        this.mContentTop = contentTop;
    }

    public int getContentRight() {
        return mContentRight;
    }

    public void setContentRight(int contentRight) {
        this.mContentRight = contentRight;
    }

    public int getContentBottom() {
        return mContentBottom;
    }

    public void setContentBottom(int contentBottom) {
        this.mContentBottom = contentBottom;
    }
}
