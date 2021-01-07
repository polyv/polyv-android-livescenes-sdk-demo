package com.easefun.polyv.livecommon.module.modules.chatroom;

//自定义信息类样例，由于涉及到gson的解析，所以当项目使用混淆时注意要keep住
public class PLVCustomGiftBean {
    public static final String EVENT = "GiftMessage";//送礼消息事件
    public static final String GIFTTYPE_FLOWER = "flower";
    public static final String GIFTTYPE_COFFEE = "coffee";
    public static final String GIFTTYPE_LIKES = "likes";
    public static final String GIFTTYPE_CLAP = "clap";
    public static final String GIFTTYPE_666 = "666";
    public static final String GIFTTYPE_STARLET = "starlet";
    public static final String GIFTTYPE_DIAMOND = "diamond";
    public static final String GIFTTYPE_SPORTSCAR = "sportscar";
    public static final String GIFTTYPE_ROCKET = "rocket";
    private String giftType;
    private String giftName;
    private int giftCount;

    public static String getGiftName(String giftType) {
        String giftName = "";
        switch (giftType) {
            case GIFTTYPE_FLOWER:
                giftName = "鲜花";
                break;
            case GIFTTYPE_COFFEE:
                giftName = "咖啡";
                break;
            case GIFTTYPE_LIKES:
                giftName = "点赞";
                break;
            case GIFTTYPE_CLAP:
                giftName = "掌声";
                break;
            case GIFTTYPE_666:
                giftName = "666";
                break;
            case GIFTTYPE_STARLET:
                giftName = "小星星";
                break;
            case GIFTTYPE_DIAMOND:
                giftName = "钻石";
                break;
            case GIFTTYPE_SPORTSCAR:
                giftName = "跑车";
                break;
            case GIFTTYPE_ROCKET:
                giftName = "火箭";
                break;
            default:
                break;
        }
        return giftName;
    }

    public PLVCustomGiftBean(String giftType, String giftName, int giftCount) {
        this.giftType = giftType;
        this.giftName = giftName;
        this.giftCount = giftCount;
    }

    public String getGiftType() {
        return giftType;
    }

    public void setGiftType(String giftType) {
        this.giftType = giftType;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getGiftCount() {
        return giftCount;
    }

    public void setGiftCount(int giftCount) {
        this.giftCount = giftCount;
    }

    @Override
    public String toString() {
        return "PLVCustomGiftBean{" +
                "giftType='" + giftType + '\'' +
                ", giftName='" + giftName + '\'' +
                ", giftCount=" + giftCount +
                '}';
    }
}
