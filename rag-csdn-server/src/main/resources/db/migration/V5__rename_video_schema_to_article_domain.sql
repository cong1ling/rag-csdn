SET @rename_video_table_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'video'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
        ),
        'RENAME TABLE `video` TO `article`',
        'DO 0'
    )
);
PREPARE rename_video_table_stmt FROM @rename_video_table_sql;
EXECUTE rename_video_table_stmt;
DEALLOCATE PREPARE rename_video_table_stmt;

SET @rename_article_source_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
              AND COLUMN_NAME = 'bvid'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
              AND COLUMN_NAME = 'source_id'
        ),
        'ALTER TABLE `article` CHANGE COLUMN `bvid` `source_id` VARCHAR(50) NOT NULL COMMENT ''来源标识''',
        'DO 0'
    )
);
PREPARE rename_article_source_id_stmt FROM @rename_article_source_id_sql;
EXECUTE rename_article_source_id_stmt;
DEALLOCATE PREPARE rename_article_source_id_stmt;

SET @rename_article_unique_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
              AND INDEX_NAME = 'uk_user_bvid'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
              AND INDEX_NAME = 'uk_user_source_id'
        ),
        'ALTER TABLE `article` RENAME INDEX `uk_user_bvid` TO `uk_user_source_id`',
        'DO 0'
    )
);
PREPARE rename_article_unique_index_stmt FROM @rename_article_unique_index_sql;
EXECUTE rename_article_unique_index_stmt;
DEALLOCATE PREPARE rename_article_unique_index_stmt;

SET @update_article_comment_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
        ),
        'ALTER TABLE `article` COMMENT = ''文章表''',
        'DO 0'
    )
);
PREPARE update_article_comment_stmt FROM @update_article_comment_sql;
EXECUTE update_article_comment_stmt;
DEALLOCATE PREPARE update_article_comment_stmt;

SET @rename_chunk_article_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND COLUMN_NAME = 'video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND COLUMN_NAME = 'article_id'
        ),
        'ALTER TABLE `chunk` CHANGE COLUMN `video_id` `article_id` BIGINT NOT NULL COMMENT ''文章ID''',
        'DO 0'
    )
);
PREPARE rename_chunk_article_id_stmt FROM @rename_chunk_article_id_sql;
EXECUTE rename_chunk_article_id_stmt;
DEALLOCATE PREPARE rename_chunk_article_id_stmt;

SET @rename_chunk_source_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND COLUMN_NAME = 'bvid'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND COLUMN_NAME = 'source_id'
        ),
        'ALTER TABLE `chunk` CHANGE COLUMN `bvid` `source_id` VARCHAR(50) NOT NULL COMMENT ''来源标识（冗余存储）''',
        'DO 0'
    )
);
PREPARE rename_chunk_source_id_stmt FROM @rename_chunk_source_id_sql;
EXECUTE rename_chunk_source_id_stmt;
DEALLOCATE PREPARE rename_chunk_source_id_stmt;

SET @rename_chunk_article_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND INDEX_NAME = 'idx_video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND INDEX_NAME = 'idx_article_id'
        ),
        'ALTER TABLE `chunk` RENAME INDEX `idx_video_id` TO `idx_article_id`',
        'DO 0'
    )
);
PREPARE rename_chunk_article_index_stmt FROM @rename_chunk_article_index_sql;
EXECUTE rename_chunk_article_index_stmt;
DEALLOCATE PREPARE rename_chunk_article_index_stmt;

SET @rename_chunk_source_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND INDEX_NAME = 'idx_bvid'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chunk'
              AND INDEX_NAME = 'idx_source_id'
        ),
        'ALTER TABLE `chunk` RENAME INDEX `idx_bvid` TO `idx_source_id`',
        'DO 0'
    )
);
PREPARE rename_chunk_source_index_stmt FROM @rename_chunk_source_index_sql;
EXECUTE rename_chunk_source_index_stmt;
DEALLOCATE PREPARE rename_chunk_source_index_stmt;

SET @rename_vector_mapping_article_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'vector_mapping'
              AND COLUMN_NAME = 'video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'vector_mapping'
              AND COLUMN_NAME = 'article_id'
        ),
        'ALTER TABLE `vector_mapping` CHANGE COLUMN `video_id` `article_id` BIGINT NOT NULL COMMENT ''文章ID''',
        'DO 0'
    )
);
PREPARE rename_vector_mapping_article_id_stmt FROM @rename_vector_mapping_article_id_sql;
EXECUTE rename_vector_mapping_article_id_stmt;
DEALLOCATE PREPARE rename_vector_mapping_article_id_stmt;

SET @rename_vector_mapping_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'vector_mapping'
              AND INDEX_NAME = 'idx_video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'vector_mapping'
              AND INDEX_NAME = 'idx_article_id'
        ),
        'ALTER TABLE `vector_mapping` RENAME INDEX `idx_video_id` TO `idx_article_id`',
        'DO 0'
    )
);
PREPARE rename_vector_mapping_index_stmt FROM @rename_vector_mapping_index_sql;
EXECUTE rename_vector_mapping_index_stmt;
DEALLOCATE PREPARE rename_vector_mapping_index_stmt;

SET @rename_session_article_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND COLUMN_NAME = 'video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND COLUMN_NAME = 'article_id'
        ),
        'ALTER TABLE `session` CHANGE COLUMN `video_id` `article_id` BIGINT NULL COMMENT ''关联文章ID（单文章对话时有值）''',
        'DO 0'
    )
);
PREPARE rename_session_article_id_stmt FROM @rename_session_article_id_sql;
EXECUTE rename_session_article_id_stmt;
DEALLOCATE PREPARE rename_session_article_id_stmt;

SET @rename_session_index_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND INDEX_NAME = 'idx_video_id'
        )
        AND NOT EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND INDEX_NAME = 'idx_article_id'
        ),
        'ALTER TABLE `session` RENAME INDEX `idx_video_id` TO `idx_article_id`',
        'DO 0'
    )
);
PREPARE rename_session_index_stmt FROM @rename_session_index_sql;
EXECUTE rename_session_index_stmt;
DEALLOCATE PREPARE rename_session_index_stmt;

UPDATE `session`
SET `session_type` = 'SINGLE_ARTICLE'
WHERE `session_type` = 'SINGLE_VIDEO';

UPDATE `session`
SET `session_type` = 'ALL_ARTICLES'
WHERE `session_type` = 'ALL_VIDEOS';

ALTER TABLE `session`
    MODIFY COLUMN `session_type` VARCHAR(20) NOT NULL COMMENT '会话类型：SINGLE_ARTICLE/ALL_ARTICLES';
