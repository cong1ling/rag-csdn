package com.example.ragcsdn.enums;

/**
 * 会话类型枚举
 */
public enum SessionType {
    /**
     * 单文章对话
     */
    SINGLE_ARTICLE("SINGLE_ARTICLE", "单文章对话"),

    /**
     * 全部文章对话
     */
    ALL_ARTICLES("ALL_ARTICLES", "全部文章对话");

    public static final String LEGACY_SINGLE_VIDEO = "SINGLE_VIDEO";
    public static final String LEGACY_ALL_VIDEOS = "ALL_VIDEOS";

    private final String code;
    private final String description;

    SessionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isSingleArticle(String code) {
        return SINGLE_ARTICLE.code.equals(code) || LEGACY_SINGLE_VIDEO.equals(code);
    }

    public static boolean isAllArticles(String code) {
        return ALL_ARTICLES.code.equals(code) || LEGACY_ALL_VIDEOS.equals(code);
    }

    public static boolean isValid(String code) {
        return isSingleArticle(code) || isAllArticles(code);
    }

    public static String normalize(String code) {
        if (isSingleArticle(code)) {
            return SINGLE_ARTICLE.code;
        }
        if (isAllArticles(code)) {
            return ALL_ARTICLES.code;
        }
        return code;
    }
}

