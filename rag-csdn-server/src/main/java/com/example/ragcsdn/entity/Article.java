package com.example.ragcsdn.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
public class Article {
    /**
     * 文章主键
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 文章来源标识
     */
    private String sourceId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 原始来源URL
     */
    private String sourceUrl;

    /**
     * 文章简介
     */
    private String description;

    /**
     * 导入时间
     */
    private LocalDateTime importTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 导入失败原因
     */
    private String failReason;
}

