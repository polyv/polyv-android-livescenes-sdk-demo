package com.easefun.polyv.livecommon.module.utils;

public class PLVStringTruncator {
    /**
     * 截断字符串，确保显示宽度不超过 6 个中文字符的宽度。
     * 规则：汉字、全角符号宽度为 1；英文、数字、半角符号宽度为 0.5。
     *
     * @param text 原始字符串
     * @return 截断后的字符串（末尾追加 ...），若未超宽则返回原字符串
     */
    public static String truncateToMax6ChineseWidth(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }

        float maxWidth = 6.0f;           // 6个中文字符宽度
        float ellipsisWidth = 1.5f;      // "..." 三个点占 1.5 个宽度（每个点 0.5）
        float currentWidth = 0.0f;
        int lastValidIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float charWidth = getCharWidth(c);

            // 如果加上当前字符会超出最大宽度（需为省略号预留空间）
            if (currentWidth + charWidth > maxWidth - ellipsisWidth) {
                break;
            }
            currentWidth += charWidth;
            lastValidIndex = i + 1; // 记录可保留的字符数
        }

        if (lastValidIndex == text.length()) {
            return text; // 未超宽
        }
        return text.substring(0, lastValidIndex) + "...";
    }

    /**
     * 获取单个字符的宽度（相对于中文字符）
     *
     * @param c 字符
     * @return 宽度值：1（汉字/全角）或 0.5（英文/数字/半角符号）
     */
    private static float getCharWidth(char c) {
        // 判断是否为全角字符（包括汉字、全角标点等）
        if ((c >= 0x4E00 && c <= 0x9FFF) ||   // 常用汉字
                (c >= 0xFF00 && c <= 0xFFEF) ||   // 全角字符
                (c >= 0x3040 && c <= 0x309F) ||   // 日文平假名
                (c >= 0x30A0 && c <= 0x30FF)) {   // 日文片假名
            return 1.0f;
        }
        // 英文、数字、半角标点等
        return 0.5f;
    }
}
