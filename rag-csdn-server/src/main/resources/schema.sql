-- 创建数据库
CREATE DATABASE IF NOT EXISTS rag_csdn DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE rag_csdn;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `csdn_cookie_encrypted` TEXT NULL COMMENT '加密后的CSDN登录态Cookie',
    `csdn_cookie_update_time` DATETIME NULL COMMENT 'CSDN登录态更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `source_id` VARCHAR(50) NOT NULL COMMENT '来源标识',
    `source_url` VARCHAR(1000) NULL COMMENT '原始来源URL',
    `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
    `description` TEXT COMMENT '文章简介',
    `import_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'IMPORTING' COMMENT '状态：IMPORTING/SUCCESS/FAILED',
    `fail_reason` VARCHAR(500) COMMENT '导入失败原因',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_source_id` (`user_id`, `source_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 分片表
CREATE TABLE IF NOT EXISTS `chunk` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分片主键',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `source_id` VARCHAR(50) NOT NULL COMMENT '来源标识（冗余存储）',
    `title` VARCHAR(255) NOT NULL COMMENT '文章标题（冗余存储）',
    `chunk_index` INT NOT NULL COMMENT '分片序号',
    `total_chunks` INT NOT NULL COMMENT '总分片数（冗余存储）',
    `chunk_text` TEXT NOT NULL COMMENT '分片文本',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_source_id` (`source_id`),
    FULLTEXT KEY `ft_title_chunk_text` (`title`, `chunk_text`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分片表';

-- 向量映射表
CREATE TABLE IF NOT EXISTS `vector_mapping` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '映射主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `chunk_id` BIGINT NOT NULL COMMENT '分片ID',
    `vector_id` VARCHAR(255) NOT NULL COMMENT '向量ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vector_id` (`vector_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_chunk_id` (`chunk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量映射表';

-- 会话表
CREATE TABLE IF NOT EXISTS `session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型：SINGLE_ARTICLE/ALL_ARTICLES',
    `article_id` BIGINT COMMENT '关联文章ID（单文章对话时有值）',
    `conversation_summary` TEXT COMMENT '对话摘要',
    `summary_update_time` DATETIME COMMENT '摘要更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息主键',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色类型：USER/ASSISTANT',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';
