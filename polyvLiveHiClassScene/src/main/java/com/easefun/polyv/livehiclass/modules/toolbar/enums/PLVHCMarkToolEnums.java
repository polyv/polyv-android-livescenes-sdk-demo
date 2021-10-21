package com.easefun.polyv.livehiclass.modules.toolbar.enums;

import com.plv.livescenes.document.event.PLVChangeApplianceEvent;

/**
 * @author suhongtao
 */
public class PLVHCMarkToolEnums {

    /**
     * 工具类显示类型枚举
     */
    public enum ControllerShowType {
        /**
         * 隐藏
         */
        NONE,

        /**
         * 显示标注工具列表
         */
        MARK_TOOL,

        /**
         * 显示标注工具颜色列表
         */
        COLOR
    }

    /**
     * 标注工具枚举
     */
    public enum MarkTool {
        /**
         * 画板移动工具
         */
        MOVE(PLVChangeApplianceEvent.Appliance.MOVE, false),

        /**
         * 画板选区工具
         */
        SELECT(PLVChangeApplianceEvent.Appliance.CHOICE, false),

        /**
         * 铅笔/自由画线 工具
         */
        PEN(PLVChangeApplianceEvent.Appliance.FREE_LINE, true),

        /**
         * 箭头工具
         */
        ARROW(PLVChangeApplianceEvent.Appliance.ARROW, true),

        /**
         * 文本工具
         */
        TEXT(PLVChangeApplianceEvent.Appliance.TEXT, true),

        /**
         * 橡皮擦工具
         */
        ERASER(PLVChangeApplianceEvent.Appliance.ERASER, false),

        /**
         * 清屏
         */
        CLEAR(null, false);

        private final PLVChangeApplianceEvent.Appliance appliance;
        private final boolean canShowColor;

        MarkTool(PLVChangeApplianceEvent.Appliance appliance, boolean canShowColor) {
            this.appliance = appliance;
            this.canShowColor = canShowColor;
        }

        public static MarkTool getDefaultMarkTool(boolean isTeacher) {
            return isTeacher ? MOVE : PEN;
        }

        public PLVChangeApplianceEvent.Appliance getAppliances() {
            return appliance;
        }

        public boolean isShowColor() {
            return canShowColor;
        }
    }

    /**
     * 标注工具颜色枚举
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

        public static Color getDefaultColor(boolean isTeacher) {
            return isTeacher ? RED : BLUE;
        }

        public String getColorString() {
            return colorString;
        }
    }

}
