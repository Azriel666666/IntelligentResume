package app.intelligent.resume.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * HTML清理工具类
 * 防止XSS攻击和HTML注入
 *
 * @author Intelligent Resume Team
 */
public class HtmlSanitizer {

    /**
     * 允许的安全标签白名单（用于富文本）
     */
    private static final Safelist RICH_TEXT_WHITELIST = Safelist.relaxed()
            .addTags("span", "div")
            .addAttributes(":all", "class", "style")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    /**
     * 纯文本白名单（不允许任何HTML标签）
     */
    private static final Safelist PLAIN_TEXT_WHITELIST = Safelist.none();

    /**
     * 清理聊天消息内容
     * 只保留纯文本，移除所有HTML标签
     * 保留emoji表情（Unicode字符）
     *
     * @param content 原始内容
     * @return 清理后的安全内容
     */
    public static String sanitizeChatMessage(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 1. 移除所有HTML标签，只保留纯文本
        String cleaned = Jsoup.clean(content, PLAIN_TEXT_WHITELIST);

        // 2. 转义特殊HTML字符（防止二次注入）
        cleaned = escapeHtml(cleaned);

        // 3. 限制长度（防止超长消息攻击）
        if (cleaned.length() > 5000) {
            cleaned = cleaned.substring(0, 5000);
        }

        return cleaned;
    }

    /**
     * 通用清理方法（别名，用于聊天消息）
     *
     * @param content 原始内容
     * @return 清理后的安全内容
     */
    public static String sanitize(String content) {
        return sanitizeChatMessage(content);
    }

    /**
     * 清理富文本内容（用于求职信等）
     * 允许基本的格式化标签
     *
     * @param content 原始内容
     * @return 清理后的安全内容
     */
    public static String sanitizeRichText(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        return Jsoup.clean(content, RICH_TEXT_WHITELIST);
    }

    /**
     * 转义HTML特殊字符
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    public static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '&' -> escaped.append("&amp;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#x27;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * 反转义HTML特殊字符（用于显示）
     *
     * @param text 转义后的文本
     * @return 原始文本
     */
    public static String unescapeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#x27;", "'");
    }

    /**
     * 检查内容是否包含潜在的XSS攻击
     *
     * @param content 内容
     * @return 是否包含危险内容
     */
    public static boolean containsXss(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        String lowerContent = content.toLowerCase();
        
        // 检查常见的XSS攻击模式
        String[] dangerousPatterns = {
                "<script", "</script>", "javascript:", "onerror=", "onload=",
                "onclick=", "onmouseover=", "onfocus=", "onblur=",
                "eval(", "expression(", "vbscript:", "data:text/html"
        };

        for (String pattern : dangerousPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 清理文件名（防止路径遍历攻击）
     *
     * @param fileName 原始文件名
     * @return 安全的文件名
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unnamed";
        }

        // 移除路径分隔符和危险字符
        return fileName
                .replaceAll("[/\\\\:*?\"<>|]", "_")
                .replaceAll("\\.\\.", "_")
                .trim();
    }
}
