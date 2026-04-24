SET @session_fk_column = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND COLUMN_NAME = 'article_id'
        ) THEN 'article_id'
        ELSE 'video_id'
    END
);

SET @add_conversation_summary_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND COLUMN_NAME = 'conversation_summary'
        ),
        'DO 0',
        CONCAT(
            'ALTER TABLE `session` ADD COLUMN `conversation_summary` TEXT NULL COMMENT ''对话摘要'' AFTER `',
            @session_fk_column,
            '`'
        )
    )
);
PREPARE add_conversation_summary_stmt FROM @add_conversation_summary_sql;
EXECUTE add_conversation_summary_stmt;
DEALLOCATE PREPARE add_conversation_summary_stmt;

SET @add_summary_update_time_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'session'
              AND COLUMN_NAME = 'summary_update_time'
        ),
        'DO 0',
        'ALTER TABLE `session` ADD COLUMN `summary_update_time` DATETIME NULL COMMENT ''摘要更新时间'' AFTER `conversation_summary`'
    )
);
PREPARE add_summary_update_time_stmt FROM @add_summary_update_time_sql;
EXECUTE add_summary_update_time_stmt;
DEALLOCATE PREPARE add_summary_update_time_stmt;
