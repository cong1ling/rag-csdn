SET @add_csdn_cookie_encrypted_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'user'
              AND COLUMN_NAME = 'csdn_cookie_encrypted'
        ),
        'DO 0',
        'ALTER TABLE `user` ADD COLUMN `csdn_cookie_encrypted` TEXT NULL COMMENT ''加密后的CSDN登录态Cookie'' AFTER `password`'
    )
);
PREPARE add_csdn_cookie_encrypted_stmt FROM @add_csdn_cookie_encrypted_sql;
EXECUTE add_csdn_cookie_encrypted_stmt;
DEALLOCATE PREPARE add_csdn_cookie_encrypted_stmt;

SET @add_csdn_cookie_update_time_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'user'
              AND COLUMN_NAME = 'csdn_cookie_update_time'
        ),
        'DO 0',
        'ALTER TABLE `user` ADD COLUMN `csdn_cookie_update_time` DATETIME NULL COMMENT ''CSDN登录态更新时间'' AFTER `csdn_cookie_encrypted`'
    )
);
PREPARE add_csdn_cookie_update_time_stmt FROM @add_csdn_cookie_update_time_sql;
EXECUTE add_csdn_cookie_update_time_stmt;
DEALLOCATE PREPARE add_csdn_cookie_update_time_stmt;
