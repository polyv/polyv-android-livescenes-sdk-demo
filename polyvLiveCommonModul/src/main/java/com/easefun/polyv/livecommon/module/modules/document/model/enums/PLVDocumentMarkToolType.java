package com.easefun.polyv.livecommon.module.modules.document.model.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author suhongtao
 */
public class PLVDocumentMarkToolType {

    /**
     * 画笔模式
     */
    public static final String BRUSH = "line";

    /**
     * 箭头模式
     */
    public static final String ARROW = "arrowLine";

    /**
     * 文本模式
     */
    public static final String TEXT = "text";

    /**
     * 橡皮擦模式
     */
    public static final String ERASER = "eraser";

    /**
     * 清除所有标注数据
     */
    public static final String CLEAR = "clear";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            BRUSH, ARROW, TEXT, ERASER, CLEAR
    })
    public @interface Range {
    }
}
