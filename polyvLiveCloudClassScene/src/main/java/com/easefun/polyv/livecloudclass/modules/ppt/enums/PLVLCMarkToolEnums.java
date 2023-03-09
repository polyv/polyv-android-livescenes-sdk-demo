package com.easefun.polyv.livecloudclass.modules.ppt.enums;

import com.easefun.polyv.livecommon.module.modules.document.model.enums.PLVDocumentMarkToolType;

/**
 * @author Hoshiiro
 */
public class PLVLCMarkToolEnums {

    /**
     * 画笔工具显示类型枚举
     */
    public enum MarkToolGroupShowType {
        /**
         * 隐藏
         */
        NONE,

        /**
         * 显示画笔工具列表
         */
        MARK_TOOL,

        /**
         * 显示画笔工具颜色列表
         */
        COLOR
    }

    /**
     * 画笔工具枚举
     */
    public enum MarkTool {

        /**
         * 铅笔/自由画线 工具
         */
        PEN(PLVDocumentMarkToolType.BRUSH, true),

        /**
         * 箭头工具
         */
        ARROW(PLVDocumentMarkToolType.ARROW, true),

        /**
         * 文本工具
         */
        TEXT(PLVDocumentMarkToolType.TEXT, true),

        /**
         * 矩形工具
         */
        RECT(PLVDocumentMarkToolType.RECT, true),

        /**
         * 橡皮擦工具
         */
        ERASER(PLVDocumentMarkToolType.ERASER, false),

        /**
         * 清屏
         */
        CLEAR(null, true);

        @PLVDocumentMarkToolType.Range
        private final String markTool;
        private final boolean canShowColor;

        MarkTool(String markTool, boolean canShowColor) {
            this.markTool = markTool;
            this.canShowColor = canShowColor;
        }

        public static MarkTool getDefaultMarkTool() {
            return PEN;
        }

        public String getMarkTool() {
            return markTool;
        }

        public boolean isShowColor() {
            return canShowColor;
        }
    }

    /**
     * 画笔工具颜色枚举
     */
    public enum Color {
        RED("#FF6363"),
        BLUE("#4399FF"),
        GREEN("#5AE59C"),
        YELLOW("#FFE45B"),
        BLACK("#4A5060"),
        WHITE("#F0F1F5");

        private final String colorString;

        Color(String colorString) {
            this.colorString = colorString;
        }

        public static Color getDefaultColor() {
            return BLUE;
        }

        public String getColorString() {
            return colorString;
        }
    }

}
