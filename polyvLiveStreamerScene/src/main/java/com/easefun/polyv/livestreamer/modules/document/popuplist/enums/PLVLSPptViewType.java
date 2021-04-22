package com.easefun.polyv.livestreamer.modules.document.popuplist.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文档列表常量枚举
 *
 * @author suhongtao
 */
public class PLVLSPptViewType {
    /**
     * PPT所有文档列表
     */
    public static final int COVER = 1;

    /**
     * PPT文档每页详情
     */
    public static final int PAGE = 2;

    /**
     * 上传文档按钮类型
     */
    public static final int UPLOAD = 101;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COVER, PAGE})
    public @interface Range {
    }
}
