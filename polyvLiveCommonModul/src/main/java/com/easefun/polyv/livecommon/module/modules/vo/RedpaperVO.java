package com.easefun.polyv.livecommon.module.modules.vo;


/**
 * 红包雨对象
 */
public class RedpaperVO implements java.io.Serializable {
    private String content; //"恭喜发财，大吉大利",
    private String number;  //1
    private String totalAmount; //0.98

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RedpaperVO{" +
                "content='" + content + '\'' +
                ", number='" + number + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                '}';
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
