package com.easefun.polyv.livecommon.module.modules.ppt.enums;

public enum PLVPPTLayoutEnum {

    PPT("ppt"),

    VIDEO("video"),

    FOLLOWTEACHER("followTeacher");

    private String value;

    PLVPPTLayoutEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
