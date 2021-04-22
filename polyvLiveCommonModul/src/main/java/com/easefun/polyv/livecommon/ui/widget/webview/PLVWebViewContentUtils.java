package com.easefun.polyv.livecommon.ui.widget.webview;

import android.text.TextUtils;

public class PLVWebViewContentUtils {

    public static String toWebViewContent(String content) {
        return toWebViewContent(content, "#000000");
    }

    public static String toWebViewContent(String content, String color) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        String style = "style=\" width:100%;\"";
        String breakStyle = "word-break:break-all;";
        String colorStyle = "color:" + color + ";";
        content = content.replaceAll("img src=\"//", "img src=\\\"https://");
        content = content.replace("<img ", "<img " + style + " ");
        content = content.replaceAll("<li>", "<li style=\"" + colorStyle + "\">");
        content = content.replaceAll("<p>", "<p style=\"" + breakStyle + colorStyle + "\">");
        content = content.replaceAll("<div>", "<div style=\"" + breakStyle + colorStyle + "\">");
        content = content.replaceAll("<table>", "<table border='1' rules=all style=\"" + colorStyle + "\">");
        content = content.replaceAll("<td>", "<td width=\"36\">");
        content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "        <title>Document</title>\n" +
                "</head>\n" +
                "<body style=\" color:"+color+";margin:0;padding:0\">" +/*fit no label text color*/
                content + "</body>\n" +
                "</html>";
        return content;
    }
}
