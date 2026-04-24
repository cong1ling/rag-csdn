SET @target_article_table = (
    SELECT CASE
        WHEN EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'article'
        ) THEN 'article'
        WHEN EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'video'
        ) THEN 'video'
        ELSE NULL
    END
);

SET @target_source_column = IF(@target_article_table = 'article', 'source_id', 'bvid');

SET @add_source_url_sql = (
    SELECT IF(
        @target_article_table IS NULL
        OR EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = @target_article_table
              AND COLUMN_NAME = 'source_url'
        ),
        'DO 0',
        CONCAT(
            'ALTER TABLE `',
            @target_article_table,
            '` ADD COLUMN `source_url` VARCHAR(1000) NULL COMMENT ''原始来源URL'' AFTER `',
            @target_source_column,
            '`'
        )
    )
);
PREPARE add_source_url_stmt FROM @add_source_url_sql;
EXECUTE add_source_url_stmt;
DEALLOCATE PREPARE add_source_url_stmt;
