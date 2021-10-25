package com.easefun.polyv.livehiclass.modules.document.popuplist.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文档列表常量枚举
 *
 * @author suhongtao
 */
public class PLVHCPptViewType {
    /**
     * PPT所有文档列表
     */
    public static final int COVER = 1;

    /**
     * 上传文档按钮类型
     */
    public static final int UPLOAD = 101;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COVER})
    public @interface Range {
    }
}
