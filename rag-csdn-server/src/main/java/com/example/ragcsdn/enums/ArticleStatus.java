package com.example.ragcsdn.enums;

/**
 * 文章状态枚举
 */
public enum ArticleStatus {
    /**
     * 导入中
     */
    IMPORTING("IMPORTING", "导入中"),

    /**
     * 导入成功
     */
    SUCCESS("SUCCESS", "导入成功"),

    /**
     * 导入失败
     */
    FAILED("FAILED", "导入失败");

    private final String code;
    private final String description;

    ArticleStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

