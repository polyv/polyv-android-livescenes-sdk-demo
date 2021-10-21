package com.easefun.polyv.livedemo.hiclass.fragments.teacher.vo;

/**
 * @author suhongtao
 */
public class PLVHCLoginAreaCodeVO {

    private String name;
    private String code;

    public PLVHCLoginAreaCodeVO(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "PLVHCLoginAreaCodeVO{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
