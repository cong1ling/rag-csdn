package com.example.ragcsdn.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分片实体类
 */
@Data
public class Chunk {
    /**
     * 分片主键
     */
    private Long id;

    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 来源标识（冗余存储）
     */
    private String sourceId;

    /**
     * 文章标题（冗余存储）
     */
    private String title;

    /**
     * 分片序号
     */
    private Integer chunkIndex;

    /**
     * 总分片数（冗余存储）
     */
    private Integer totalChunks;

    /**
     * 分片文本
     */
    private String chunkText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 关键词检索得分
     */
    private Double keywordScore;
}

