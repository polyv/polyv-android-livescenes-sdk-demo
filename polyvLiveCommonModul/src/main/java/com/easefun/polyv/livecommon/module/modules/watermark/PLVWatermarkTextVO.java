package com.easefun.polyv.livecommon.module.modules.watermark;

import android.graphics.Color;

/**
 * author: fangfengrui
 * date: 2021/12/27
 */

public class PLVWatermarkTextVO {

    //<editor-fold defaultstate="collapsed" desc="变量">
    private String content;
    private String fontSize;
    private String fontAlpha;
    private int fontColor;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="构造方法">
    public PLVWatermarkTextVO() {
        content = " ";
        fontSize = "middle";
        fontAlpha = "10";
        fontColor = Color.BLACK;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="get-set">
    public String getContent() {
        return content;
    }

    public PLVWatermarkTextVO setContent(String content) {
        this.content = content;
        return this;
    }

    public int getFontColor() {
        return fontColor;
    }

    public PLVWatermarkTextVO setFontColor(int fontColor) {
        this.fontColor = fontColor;
        return this;
    }

    public String getFontAlpha() {
        return fontAlpha;
    }

    public PLVWatermarkTextVO setFontAlpha(String fontAlpha) {
        this.fontAlpha = fontAlpha;
        return this;
    }

    public int getFontSize() {
        switch (fontSize) {
            case "large":
                return 20;
            case "middle":
                return 16;
            case "small":
                return 12;
            default:
                break;
        }
        return 30;
    }

    public PLVWatermarkTextVO setFontSize(String fontSize) {
        this.fontSize = fontSize;
        return this;
    }
    //</editor-fold>
}
